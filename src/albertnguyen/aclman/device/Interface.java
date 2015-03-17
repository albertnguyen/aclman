package albertnguyen.aclman.device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import albertnguyen.net.CiscoIOSTelnet;
import albertnguyen.net.Telnet;

/**
 * Represents an interface on a host
 * */
public class Interface {
	
	/** Group */
	private String group;
	
	/** Device address */
	private String address;
	
	/** Link to inbound access list */
	private ACL inboundacl;
	
	/** Link to outbound access list */
	private ACL outboundacl;

	/** Interface name */
	private String name;
	
	/** IP address */
	private String ip;
	
	/** Interface description */
	private String description;
	
	/** Physical and protocol status */
	private int status, protocol;
	
	/** Simple constructor */
	public Interface(String group, String address, String name, String ip,
			int status, int protocol) {
		this.name = name;
		this.ip = ip;
		this.status = status;
		this.protocol = protocol;
	}
	
	/** Simple constructor */
	public Interface(String group, String address, String name, String ip,
			String description, int status, int protocol) {
		this.group = group;
		this.address = address;
		this.name = name;
		this.ip = ip;
		this.description = description;
		this.status = status;
		this.protocol = protocol;
	}

	/** Convert to a string */
	@Override
	public String toString() {
		String res = "Interface:" + Telnet.EOL;
		res += "Name: " + name + Telnet.EOL;
		res += "IP: " + ip + Telnet.EOL;
		res += "Description: " + description + Telnet.EOL;
		res += "Status: ";
		if(status == 1)
			res += "Up";
		else
			res += "Down";
		res += Telnet.EOL;
		res += "Protocol: ";
		if(protocol == 1)
			res += "Up";
		else
			res += "Down";
		res += Telnet.EOL;
		res += "Inbound ACL: ";
		if(inboundacl != null)
			res += inboundacl.getName();
		res += Telnet.EOL;
		res += "Outbound ACL: ";
		if(outboundacl != null)
			res += outboundacl.getName();
		return res;
	}
	
	/** Parse all interfaces */
	public static ArrayList<Interface> parseToList(Device device, CiscoIOSTelnet telnet)
			throws IOException {
		ArrayList<Interface> res = new ArrayList<Interface>();
		String ipintbrief = telnet.execute("show ip interface brief");
		String intdesc = telnet.execute("show interface description");
		String access = telnet.execute("show ip interface | inc line protocol is|Inbound|" +
				"Outgoing");
		BufferedReader reader1 = new BufferedReader(new StringReader(ipintbrief));
		BufferedReader reader2 = new BufferedReader(new StringReader(intdesc));
		access = access.replace(Telnet.EOL + "  Outgoing", " Outgoing");
		access = access.replace(Telnet.EOL + "  Inbound", " Inbound");
		BufferedReader reader3 = new BufferedReader(new StringReader(access));
		try {
			reader1.readLine();
			reader2.readLine();
			do {
				String line1 = reader1.readLine();		
				String line2 = reader2.readLine();
				String line3 = reader3.readLine();
				if(line1 == null)
					break;
				Interface intf = parse(device, line1, line2, line3);
				if(intf != null)
					res.add(intf);
			}
			while(true);
		}
		catch(IOException e) {
			e.printStackTrace(System.err);
		}
		return res;
	}
	
	/** Parse a single interface configuration */
	public static Interface parse(Device device, String ipintbrief, String intdesc,
			String access) throws IOException {
		String str1[] = ipintbrief.split(" +");
		// Only utilize Ethernet, FE, GE, Serial, Tunnel, Vlan, Loopback, ATM
		/*if(!(str1[0].startsWith("Ethernet") || str1[0].startsWith("FastEthernet") ||
				str1[0].startsWith("GigabitEthernet") || str1[0].startsWith("Serial") ||
				str1[0].startsWith("Tunnel") || str1[0].startsWith("Vlan") ||
				str1[0].startsWith("Loopback") || str1[0].startsWith("ATM")))
			return null;*/
		int status, protocol;
		if(str1[4].equals("up"))
			status = 1;
		else
			status = 0;
		if(str1[5].equals("up"))
			protocol = 1;
		else
			protocol = 0;
		if(str1[1].equals("unassigned"))
			str1[1] = "";
		Interface res = new Interface(device.getGroup(), device.getAddress(), str1[0],
				str1[1], status, protocol);		
		// Group, device address, description
		res.group = device.getGroup();
		res.address = device.getAddress();
		String str2 = intdesc.replaceFirst("[^ ]+ +", "").
				replaceFirst("admin down +|up +|down +", "").
				replaceFirst("admin down +|up +|down +", "");
		res.description = str2;
		// Inbound & outbound ACL
		String str3 = access.replaceAll(".*line protocol is [^ ]+", "").trim();
		str3 = str3.replace("Outgoing access list is not set", "").trim();
		str3 = str3.replace("Inbound  access list is not set", "").trim();
		if(str3.equals("")) {
			res.inboundacl = null;
			res.outboundacl = null;
		}
		if(str3.contains("Inbound  access list is")) {
			String in = str3.replaceAll(".*Inbound  access list is ([^ ]+)", "$1");
			str3 = str3.replace("Inbound  access list is ([^ ]+)", "").trim();
			boolean stubby = true;
			for(ACL acl: device.getACLs())
				if(acl.getName().equals(in)) {
					res.inboundacl = acl;
					stubby = false;
				}
			if(stubby) {
				ACL acl = new ACL(res.group, res.address, ACL.CISCO_STUB, in, null);
				res.inboundacl = acl;
			}
		}
		if(str3.contains("Outgoing access list is")) {
			String out = str3.replaceAll("Outgoing access list is ([^ ]+)", "$1");
			boolean stubby = true;
			for(ACL acl: device.getACLs())
				if(acl.getName().equals(out)) {
					res.inboundacl = acl;
					stubby = false;
				}
			if(stubby) {
				ACL acl = new ACL(res.group, res.address, ACL.CISCO_STUB, out, null);
				res.outboundacl = acl;
			}
		}
		return res;
	}
	
	/** Retrieve interfaces information from a connection */
	public static ArrayList<Interface> getInterfaces(Device device, CiscoIOSTelnet telnet)
			throws IOException {
		return parseToList(device, telnet);
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

	/** IP getter */
	public String getIP() {
		return ip;
	}

	/** Description getter */
	public String getDescription() {
		return description;
	}

	/** Status getter */
	public int getStatus() {
		return status;
	}

	/** Protocol status getter */
	public int getProtocol() {
		return protocol;
	}

	/** Inbound ACL getter */
	public ACL getInboundACL() {
		return inboundacl;
	}

	/** Outbound ACL getter */
	public ACL getOutboundACL() {
		return outboundacl;
	}
	
	/** Inbound ACL setter */
	public void setInboundACL(ACL in) {
		this.inboundacl = in;
	}
	
	/** Outbound ACL setter */
	public void setOutboundACL(ACL out) {
		this.outboundacl = out;
	}
	
}