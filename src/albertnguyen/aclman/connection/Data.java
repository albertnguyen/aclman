package albertnguyen.aclman.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import albertnguyen.aclman.device.ACL;
import albertnguyen.aclman.device.Device;
import albertnguyen.aclman.device.Interface;
import albertnguyen.aclman.gui.Main;
import albertnguyen.net.Telnet;

/**
 * MySQL database connection
 * */
public class Data {
	
	private Connection con = null;
	private Statement st = null;
	private ResultSet rs = null;
	private String user;
	
	/** Utilize database connection */
	public Data(String host, String port, String user, String pass) throws SQLException {
		this.user = user;
		String url = "jdbc:mysql://" + host + ":" + port + "/" + user;
		con = DriverManager.getConnection(url, user, pass);
		st = con.createStatement();
		
		// Create database if not exists
		st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + user + ";");
		st.executeUpdate("CREATE TABLE IF NOT EXISTS `group` (" +
				"`name` varchar(50) NOT NULL," +
				"PRIMARY KEY (`name`)" +
				") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		st.executeUpdate("CREATE TABLE IF NOT EXISTS `device` (" +
				"`group` varchar(50) NOT NULL," +
				"`address` varchar(50) NOT NULL," +
				"`name` varchar(50) NOT NULL," +
				"`user` varchar(50)," +
				"`pass` varchar(50)," +
				"`enable` varchar(50)," +
				"`description` varchar(255)," +
				"`accessintf` varchar(50)," +
				"PRIMARY KEY (`group`, `address`)" +
				") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		st.executeUpdate("CREATE TABLE IF NOT EXISTS `interface` (" +
				"`group` varchar(50) NOT NULL," +
				"`address` varchar(50) NOT NULL," +
				"`name` varchar(50) NOT NULL," +
				"`ip` varchar(50)," +
				"`description` varchar(255)," +
				"`status` tinyint(1)," +
				"`protocol` tinyint(1)," +
				"`inboundacl` varchar(50)," +
				"`outboundacl` varchar(50)," +
				"PRIMARY KEY (`group`, `address`, `name`)" +
				") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		st.executeUpdate("CREATE TABLE IF NOT EXISTS `acl` (" +
				"`group` varchar(50) NOT NULL," +
				"`address` varchar(50) NOT NULL," +
				"`name` varchar(50) NOT NULL," +
				"`type` int(11) NOT NULL," +
				"`rules` text NOT NULL," +
				"PRIMARY KEY (`group`, `address`, `name`)" +
				") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		st.executeUpdate("CREATE TABLE IF NOT EXISTS `template` (" +
				"`name` varchar(50) NOT NULL," +
				"`expression` text," +
				"PRIMARY KEY (`name`)" +
				") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		st.executeUpdate("CREATE TABLE IF NOT EXISTS `session` (" +
				"`host` varchar(50) NOT NULL," +
				"PRIMARY KEY (`host`)" +
				") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		
		// Check if other one logged in to server
		rs = st.executeQuery("SELECT is_free_lock('" + user + "');");
		rs.next();
		if(rs.getInt(1) == 0) {
			rs = st.executeQuery("SELECT * FROM `session`;");
			rs.next();
			JOptionPane.showMessageDialog(Main.login,
					"Entering read-only mode due to another active session from " +
							rs.getString(1),
					"READ ONLY",
					JOptionPane.INFORMATION_MESSAGE);
			Main.ro = true;
		}
		// Free --> lock server
		else {
			st.executeUpdate("DELETE FROM `session` WHERE TRUE;");
			rs = st.executeQuery("SELECT host FROM information_schema.processlist " +
					"WHERE id=connection_id();");
			rs.next();
			st.executeUpdate("INSERT INTO `session` (`host`) VALUES (\'" +
					rs.getString("host").replaceAll(":.*$", "") +
					"\');");
			st.executeQuery("SELECT get_lock('" + user + "', 1)");
		}
	}
	
	/** Disposal */
	public void dispose() {
		try {
			if (rs != null) {
				rs.close();
			}
			if (st != null) {
				// Unlock tables
				if(!Main.ro) {
					st.executeQuery("SELECT release_lock('" + user + "')");
				}
				st.close();
			}
			if (con != null) {
				con.close();
			}

		} catch (SQLException ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	
	/** DB transaction interrupted  */
	public void notifyTransactionError() {
		JOptionPane.showMessageDialog(Main.dashboard,
				"Database connection error, please try again.", "ERROR",
				JOptionPane.ERROR_MESSAGE);
		Main.dashboard.connect();
	}
	
	/** Get groups */
	public ArrayList<String> getGroups() {
		ArrayList<String> res = new ArrayList<String>();
		try {
			rs = st.executeQuery("SELECT * FROM `group`;");
			while(rs.next())
				res.add(rs.getString(1));
		}
		catch(SQLException e) {
			notifyTransactionError();	
			e.printStackTrace(System.err);
		}
		return res;
	}
	
	/** Add a group */
	public void addGroup(String name) {
		try {
			st.executeUpdate("INSERT INTO `group` (`name`) VALUES ('" + name + "');");
		}
		catch(SQLException e) {
			notifyTransactionError();	
			e.printStackTrace(System.err);
		}
	}
	
	/** Remove a group */
	public void removeGroup(String name) {
		try {
			st.executeUpdate("DELETE FROM `group` WHERE `name`='" + name + "';");
			st.executeUpdate("DELETE FROM `device` WHERE `group`='" + name + "';");
			st.executeUpdate("DELETE FROM `interface` WHERE `group`='" + name + "';");
			st.executeUpdate("DELETE FROM `acl` WHERE `group`='" + name + "';");
		}
		catch(SQLException e) {
			notifyTransactionError();	
			e.printStackTrace(System.err);
		}
	}
	
	/** Get device list to show on combo box */
	public ArrayList<String> getDeviceList(String group) {
		ArrayList<String> res = new ArrayList<String>();		
		try {
			rs = st.executeQuery("SELECT `address`, `name` " +
					"FROM `device` " +
					"WHERE `group`='" + group + "';");
			while(rs.next()) {
				res.add(rs.getString("name") + " [" + rs.getString("address") + ']');
			}
		}
		catch(SQLException e) {
			notifyTransactionError();	
			e.printStackTrace(System.err);
		}
		return res;
	}
	
	/** Get information about a device to show on tree */
	public Device getDevice(String group, String address) {
		Device res = null;
		try {
			ArrayList<ACL> acls = getACLs(group, address);
			ArrayList<Interface> interfaces = getInterfaces(group, address, acls);
			rs = st.executeQuery("SELECT `name`, `user`, `pass`, `enable`, `description`, " +
					"`accessintf` FROM `device` " +
					"WHERE `group`='" + group + "' AND `address`='" + address + "' ");
			if(rs.next() == false)
				return res;
			res = new Device(group, address, rs.getString("name"), rs.getString("user"),
					rs.getString("pass"), rs.getString("enable"),
					rs.getString("description"), rs.getString("accessintf"),
					acls, interfaces);
		} catch (SQLException e) {
			notifyTransactionError();
			e.printStackTrace();
		}
		return res;
	}
	
	/** Get access lists belong to a device */
	public ArrayList<ACL> getACLs(String group, String address) throws SQLException {
		ArrayList<ACL> res = new ArrayList<ACL>();
		rs = st.executeQuery("SELECT `name`, `type`, `rules` FROM `acl` " +
				"WHERE `group`='" + group + "' AND `address`='" + address +	"';");
		while(rs.next()) {
			ArrayList<String> rules = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new StringReader(
					rs.getString("rules")));
			try {
				String line = reader.readLine();
				while(line != null) {
					rules.add(line);
					line = reader.readLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			res.add(new ACL(group, address, rs.getInt("type"), rs.getString("name"), rules));
		}
		return res;
	}
	
	/** Get interfaces belong to a device */
	public ArrayList<Interface> getInterfaces(String group, String address,
			ArrayList<ACL> acls) throws SQLException {
		ArrayList<Interface> res = new ArrayList<Interface>();
		rs = st.executeQuery("SELECT `name`, `ip`, `description`, `status`, `protocol`, " +
				"`inboundacl`, `outboundacl` FROM `interface` WHERE `group`='" + group +
				"' AND `address`='" + address + "';");
		while(rs.next()) {
			Interface intf = new Interface(group, address, rs.getString("name"),
					rs.getString("ip"),	rs.getString("description"), rs.getInt("status"),
					rs.getInt("protocol"));
			String in = rs.getString("inboundacl");
			String out = rs.getString("outboundacl");
			boolean foundin = false, foundout = false;
			for(ACL acl: acls) {
				if(acl.getName().equals(in)) {
					intf.setInboundACL(acl);
					foundin = true;
				}
			}
			if(!foundin && !in.equals("")) {
				ACL acl = new ACL(group, address, ACL.CISCO_STUB, in, null);
				intf.setInboundACL(acl);
			}
			for(ACL acl: acls) {
				if(acl.getAddress().equals(out)) {
					intf.setOutboundACL(acl);
					foundout = true;
				}
			}
			if(!foundout && !out.equals("")) {
				ACL acl = new ACL(group, address, ACL.CISCO_STUB, out, null);
				intf.setOutboundACL(acl);
			}
			res.add(intf);
		}
		return res;
	}
	
	/** Add new device */
	public void addDevice(Device dev) {
		try {
			for(ACL acl: dev.getACLs())
				addACL(acl);
			for(Interface intf: dev.getInterfaces())
				addInterface(intf);
			st.executeUpdate("INSERT INTO `device` (`group`, `address`, `name`, " +
					"`user`, `pass`, `enable`, `description`, `accessintf`) VALUES ('" +
					dev.getGroup() + "', '" + dev.getAddress() + "', '" + dev.getName() +
					"', '" + dev.getUser() + "', '" + dev.getPass() + "', '" +
					dev.getEnable() + "', '" + dev.getDescription() + "', '" +
					dev.getAccessInterface() + "');");
		} catch (SQLException e) {
			notifyTransactionError();
			e.printStackTrace();
		}
	}
	
	/** Add new access list */
	public void addACL(ACL acl) {
		try {
			st.executeUpdate("INSERT INTO `acl` (`group`, `address`, `name`, `type`, " +
					"`rules`) VALUES ('" + acl.getGroup() + "', '" + acl.getAddress() +
					"', '" + acl.getName() + "', '" + acl.getType() + "', '" +
					acl.getRulesString() + "');");
		} catch (SQLException e) {
			notifyTransactionError();
			e.printStackTrace();
		}
	}
	
	/** Add new interface */
	public void addInterface(Interface intf) {
		try {
			String in;
			String out;
			ACL inboundacl = intf.getInboundACL();
			if(inboundacl == null)
				in = "";
			else
				in = inboundacl.getName();
			ACL outboundacl = intf.getOutboundACL();
			if(outboundacl == null)
				out = "";
			else
				out = outboundacl.getName();
			st.executeUpdate("INSERT INTO `interface` (`group`, `address`, " +
					"`name`, `ip`, `description`, `status`, `protocol`, " +
					"`inboundacl`, `outboundacl`) VALUES ('" + intf.getGroup() + "', " +
					"'" + intf.getAddress() + "', '" + intf.getName().
					replace("'", "\\'") + "', '" + 	intf.getIP() + "', '" + 
					intf.getDescription().replace("'", "\\'") + "', '" +
					intf.getStatus() + "', '" + intf.getProtocol() + "', " +
					"'" + in + "', '" +	out + "');");
		} catch (SQLException e) {
			notifyTransactionError();
			e.printStackTrace(System.err);
		}
	}

	/** Remove device */
	public void removeDevice(String group, String address) {
		try {
			st.executeUpdate("DELETE FROM `acl` WHERE `group`='" + group +
					"' AND `address`='" + address + "';");
			st.executeUpdate("DELETE FROM `interface` WHERE `group`='" + group +
					"' AND `address`='" + address + "';");
			st.executeUpdate("DELETE FROM `device` WHERE `group`='" + group +
					"' AND `address`='" + address + "';");
		}
		catch(SQLException e) {
			notifyTransactionError();
			e.printStackTrace(System.err);
		}
	}

	/** Add new access list template */
	public void addTemplate(String name) {
		try {
			st.executeUpdate("INSERT INTO `template` (`name`) VALUES ('" + name + "');");
		}
		catch (SQLException e) {
			notifyTransactionError();
			e.printStackTrace(System.err);
		}
	}

	/** Remove template from database */
	public void removeTemplate(String name) {
		try {
			st.executeUpdate("DELETE FROM `template` WHERE `name`='" + name + "'");
		}
		catch (SQLException e) {
			notifyTransactionError();
			e.printStackTrace(System.err);
		}
	}
	
	/** Read template */
	public String getTemplate(String name) {
		String res = "";
		try {
			rs = st.executeQuery("SELECT `expression` FROM `template` WHERE `name`='" +
					name + "'");
			rs.next();
			String expression = rs.getString("expression");
			if(expression == null)
				return res;
			BufferedReader reader = new BufferedReader(new StringReader(expression));
			String line;
			do {
				line = reader.readLine();
				if(line == null)
					break;
				res += line + Telnet.EOL;
			}
			while(true);
		}
		catch (SQLException | IOException e) {
			notifyTransactionError();
			e.printStackTrace(System.err);
		}
		if(res.endsWith(Telnet.EOL))
			res = res.substring(0, res.length() - Telnet.EOL.length());
		return res;
	}
	
	/** Save template */
	public void saveTemplate(String name, String expression) {
		try {
			st.executeUpdate("UPDATE `template` SET `expression`='" +
					expression + "' WHERE `name`='" + name + "';");
		}
		catch (SQLException e) {
			notifyTransactionError();
			e.printStackTrace(System.err);
		}
	}
	
	/** Get list of templates */
	public ArrayList<String> getTemplates() {
		ArrayList<String> res = new ArrayList<String>();
		try {
			rs = st.executeQuery("SELECT `name` FROM `template`;");
			while(rs.next()) {
				res.add(rs.getString("name"));
			}
		}
		catch (SQLException e) {
			notifyTransactionError();
			e.printStackTrace(System.err);
		}
		return res;
	}

	/** Remove access lists in a group */
	public void removeACL(String group, String address, String name) {
		try {
			st.executeUpdate("DELETE FROM `acl` WHERE `name`='" + name +
					"' AND `group`='" + group + "' AND `address`='" + address + "';");
			st.executeUpdate("UPDATE `interface` SET `inboundacl`='' WHERE `group`='" +
					group + "' AND `address`='" + address +	"' AND `inboundacl`='" +
					name +	"'");
			st.executeUpdate("UPDATE `interface` SET `outboundacl`='' WHERE `group`='" +
					group + "' AND `address`='" + address +	"' AND `outboundacl`='" +
					name +	"'");

		}
		catch (SQLException e) {
			notifyTransactionError();
			e.printStackTrace(System.err);
		}
	}

	/** Get IP address of access interface */
	public String getDeviceIP(String group, String address) {
		String res = null;
		try {
			rs = st.executeQuery("SELECT `accessintf` FROM `device` WHERE `group`='" +
					group + "' AND `address`='" + address + "';");
			rs.next();
			rs = st.executeQuery("SELECT `ip` FROM `interface` WHERE `group`='" +
					group + "' AND `address`='" + address + "' AND `name`='" +
					rs.getString("accessintf").split(" ")[0] +
					"';");
			rs.next();
			res = rs.getString("ip");
		}
		catch (SQLException e) {
			notifyTransactionError();
			e.printStackTrace(System.err);
		}
		return res;
	}
	
	/** Get name of device */
	public String getDeviceName(String group, String address) {
		String res = null;
		try {
			rs = st.executeQuery("SELECT `name` FROM `device` WHERE `group`='" +
					group + "' AND `address`='" + address + "';");
			rs.next();
			res = rs.getString("name");
		}
		catch (SQLException e) {
			notifyTransactionError();
			e.printStackTrace(System.err);
		}
		return res;
	}
	
	/** Get devices in a group */
	public ArrayList<String> getDeviceAddresses(String group) {
		ArrayList<String> res = new ArrayList<String>();		
		try {
			rs = st.executeQuery("SELECT `address` FROM `device` " +
					"WHERE `group`='" + group + "';");
			while(rs.next()) {
				res.add(rs.getString("address"));
			}
		}
		catch(SQLException e) {
			notifyTransactionError();	
			e.printStackTrace(System.err);
		}
		return res;
	}
	
	/*** Apply access list to access interface */ 
	public void applyACL(String group, String address, ACL acl) {
		try {
			// Select access interface
			rs = st.executeQuery("SELECT `accessintf` FROM `device` " +
					"WHERE `group`='" + group + "' AND `address`='" + address + "';");
			rs.next();
			String accessintf[] = rs.getString("accessintf").split(" ");
			// Modify
			if(accessintf[1].equals("inbound")) {
				st.executeUpdate("UPDATE `interface` SET `inboundacl`='" +
						acl.getName() + "' WHERE `name`='" + accessintf[0] +
						"' AND `group`='" + group + "' AND `address`='" + address + "';");
			}
			else {
				// Outbound
				st.executeUpdate("UPDATE `interface` SET `outboundacl`='" +
						acl.getName() + "' WHERE `name`='" + accessintf[0] +
						"' AND `group`='" + group + "' AND `address`='" + address + "';");
			}
		}
		catch(SQLException e) {
			notifyTransactionError();	
			e.printStackTrace(System.err);
		}
	}
	
	/** Set access interface */
	public void setAccessIntf(String group, String address, String intf) {
		try {
			st.executeUpdate("UPDATE `device` SET `accessintf`='" + intf + "' " +
					"WHERE `group`='" +	group + "' AND `address`='" + address + "';");
		}
		catch(SQLException e) {
			notifyTransactionError();	
			e.printStackTrace(System.err);
		}
	}
	
	/** Rename a group */
	public void renameGroup(String name, String newname) {
		try {
			st.executeUpdate("UPDATE `group` SET `name`='" + newname +
					"' WHERE `name`='" + name + "';");
			st.executeUpdate("UPDATE `device` SET `group`='" + newname +
					"' WHERE `group`='" + name + "';");
			st.executeUpdate("UPDATE `interface` SET `group`='" + newname +
					"' WHERE `group`='" + name + "';");
			st.executeUpdate("UPDATE `acl` SET `group`='" + newname +
					"' WHERE `group`='" + name + "';");
		}
		catch(SQLException e) {
			notifyTransactionError();	
			e.printStackTrace(System.err);
		}
	}
	
}