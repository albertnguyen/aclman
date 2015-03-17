package albertnguyen.aclman.device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import albertnguyen.net.CiscoIOSTelnet;
import albertnguyen.net.Telnet;

/**
 * Represents a logical device on the network
 * */
public class Device {
	
	private String group;
	private String address;
	private String name;
	private String user;
	private String pass;
	private String enable;
	private String description;
	private ArrayList<ACL> acls;
	private ArrayList<Interface> interfaces;
	private String accessintf;
	
	/** Simple constructions */
	public Device(String group, String address, String name, String user, String pass,
			String enable, String description, String accessintf, ArrayList<ACL> acls,
			ArrayList<Interface> interfaces) {
		this.group = group;
		this.address = address;
		this.name = name;
		this.user = user;
		this.pass = pass;
		this.enable = enable;
		this.acls = acls;
		this.interfaces = interfaces;
		this.description = description;
		this.accessintf = accessintf;
	}
	
	public Device(String group, String address, String user, String pass, String enable) {
		this.group = group;
		this.address = address;
		this.user = user;
		this.pass = pass;
		this.enable = enable;
	}
	
	/** Group getter */
	public String getGroup() {
		return group;
	}
	
	/** Address getter */
	public String getAddress() {
		return address;
	}
	
	/** Name getter */
	public String getName() {
		return name;
	}
	
	/** User getter */
	public String getUser() {
		return user;
	}
	
	/** Pass getter */
	public String getPass() {
		return pass;
	}
	
	/** Enable getter */
	public String getEnable() {
		return enable;
	}
	
	/** Description getter */
	public String getDescription() {
		return description;
	}
	
	/** Access lists getter */
	public ArrayList<ACL> getACLs() {
		return acls;
	}
	
	/** Convert to text */
	@Override
	public String toString() {
		String res = "Device:" + Telnet.EOL;
		res += "Address: " + address + Telnet.EOL;
		res += "Description: " + description + Telnet.EOL;
		res += "Access Interface: " + accessintf + Telnet.EOL + Telnet.EOL;
		res += "Interfaces:";
		for(Interface intf: interfaces)
			res += Telnet.EOL + Telnet.EOL + intf.toString();
		res += Telnet.EOL + Telnet.EOL + "Access lists:";
		for(ACL acl: acls)
			res += Telnet.EOL + Telnet.EOL + acl.toString();
		return res;
	}
	
	/** Get host name, interfaces and access-lists */
	public static Device getDevice(String group, String address, String user, String pass,
			String enable) throws IOException {
		Device res = new Device(group, address, user, pass, enable);		
		CiscoIOSTelnet telnet = new CiscoIOSTelnet(address);
		telnet.login(user, pass, enable);
		
		// Create host name and IOS description
		String showversion = telnet.execute("show version");
		BufferedReader buff = new BufferedReader(new StringReader(showversion));
		res.description = buff.readLine().
				replaceFirst(".+\\((.+)\\), Version.+", "$1");
		String line;
		do {
			line = buff.readLine();
			if(line == null)
				break;
			if(line.contains("uptime is")) {
				res.name = line.substring(0, line.indexOf(' '));
				break;
			}
		}
		while (true);
		
		// Construct access-lists
		res.acls = ACL.getACLs(res, telnet);
		
		// Construct interfaces info
		res.interfaces = Interface.getInterfaces(res, telnet);
		res.accessintf = res.interfaces.get(0).getName() + " inbound";
		
		telnet.disconnect();
		return res;
	}

	/** Interfaces getter */
	public ArrayList<Interface> getInterfaces() {
		return interfaces;
	}
	
	/** Access interface setter */
	public void setAccessInterface(String access) {
		this.accessintf = access;
	}
	
	/** Access interface getter */
	public String getAccessInterface() {
		return accessintf;
	}
	
	/** Apply configuration */
	public void pushConfig() throws IOException {
		CiscoIOSTelnet telnet = new CiscoIOSTelnet(address);
		telnet.login(user, pass, enable);
		ArrayList<Interface> interfaces = Interface.getInterfaces(this, telnet);
		ArrayList<ACL> acls = ACL.getACLs(this, telnet);
		telnet.execute("config t");
		
		// Remove access lists application
		for(Interface real: interfaces) {
			String in = null;
			if(real.getInboundACL() != null)
				in = real.getInboundACL().getName();
			if(in != null) {
				telnet.execute("interface " + real.getName());
				telnet.execute("no ip access-group " + in + " in");
			}
			String out = null;
			if(real.getOutboundACL() != null)
				out = real.getOutboundACL().getName();
			if(out != null) {
				telnet.execute("interface " + real.getName());
				telnet.execute("no ip access-group " + out + " out");
			}
		}
		
		// Update access lists 
		OUT_1: for(ACL real: acls) {
			for(ACL acl: this.acls)
				if(acl.getName().equals(real.getName())) {
					// Check for equality
					if(!acl.equals(real)) {
						String type = null;
						if(real.getType() == ACL.CISCO_STANDARD)
							type = "standard";
						else
							type = "extended";
						telnet.execute("no ip access-list " + type + ' ' + real.getName());
						if(acl.getType() == ACL.CISCO_STANDARD)
							type = "standard";
						else
							type = "extended";
						telnet.execute("ip access-list " + type + ' ' + acl.getName());
						for(String line: acl.getRules())
							telnet.execute(line);
					}
					continue OUT_1;
				}
			// No matching access list, do remove
			String type = null;
			if(real.getType() == ACL.CISCO_STANDARD)
				type = "standard";
			else
				type = "extended";
			telnet.execute("no ip access-list " + type + ' ' + real.getName());
		}
		// Brand new access lists
		OUT_2: for(ACL acl: this.acls) {
			for(ACL real: acls)
				if(acl.getName().equals(real.getName()))
					continue OUT_2;
			String type = null;
			if(acl.getType() == ACL.CISCO_STANDARD)
				type = "standard";
			else
				type = "extended";
			telnet.execute("ip access-list " + type + ' ' + acl.getName());
			for(String line: acl.getRules())
				telnet.execute(line);
		}
		
		// Apply access lists back again
		for(Interface intf: this.interfaces) {
			String in = null;
			if(intf.getInboundACL() != null)
				in = intf.getInboundACL().getName();
			if(in != null) {
				telnet.execute("interface " + intf.getName());
				telnet.execute("ip access-group " + in + " in");
			}
			String out = null;
			if(intf.getOutboundACL() != null)
				out = intf.getOutboundACL().getName();
			if(out != null) {
				telnet.execute("interface " + intf.getName());
				telnet.execute("ip access-group " + out + " out");
			}
		}
		
		// Save and exit
		telnet.execute("end");
		telnet.setTimeout(10000); // Building configuration can take long
		telnet.execute("write");
		telnet.setTimeout(2000);
		telnet.disconnect();
	}

}