package albertnguyen.aclman.device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import albertnguyen.aclman.gui.Main;
import albertnguyen.net.CiscoIOSTelnet;
import albertnguyen.net.Telnet;

/**
 * Represents a Cisco access-list
 * */
public class ACL {
	
	/** Group */
	private String group;
	
	/** Device */
	private String address;
	
	/** Type */
	private int type;
	
	/** Name */
	private String name;
	
	/** Content */
	private ArrayList<String> rules;
	
	/** Constants */
	public static final int CISCO_STUB = 0;
	public static final int CISCO_STANDARD = 1;
	public static final int CISCO_EXTENDED = 2;
	
	/** Use info passed from device object */
	public ACL(Device device) {
		this.group = device.getGroup();
		this.address = device.getAddress();
		this.type = CISCO_STANDARD;
		this.name = null;
		this.rules = new ArrayList<String>();
	}
	
	/** Simple constructor */
	public ACL(String group, String address, int type, String name,
			ArrayList<String> rules) {
		this.group = group;
		this.address = address;
		this.type = type;
		this.name = name;
		this.rules = rules;
	}
	
	/** Convert to printable format */
	@Override
	public String toString() {
		String res = "Access list:" + Telnet.EOL;
		switch (type) {
			case CISCO_STANDARD: res += "Standard IP access list ";
				break;
			case CISCO_EXTENDED: res += "Extended IP access list ";
				break;
			default: res += "";
				break;
		}
		res += name + Telnet.EOL;
		for(String rule: rules)
			res += rule + Telnet.EOL;
		res = res.substring(0, res.length() - Telnet.EOL.length());
		return res;
	}
	
	/** Parse from show ip access-lists command */
	public static ArrayList<ACL> parseToList(Device device, CiscoIOSTelnet telnet)
			throws IOException {
		ArrayList<ACL> res = new ArrayList<ACL>();
		String string = telnet.execute("show ip access-list");
		if(string == null || string.equals(""))
			return res;
		// Parse Cisco ACL
		String standards[] = string.split("Standard IP access list");
		for(String standard: standards) {
			if(standard.equals(""))
				continue;
			if(!standard.startsWith("Extended IP access list"))
				standard = "Standard IP access list" + standard;
			String acls[] = standard.split("Extended IP access list");
			for(String acl: acls) {
				if(acl.equals(""))
					continue;
				if(!acl.startsWith("Standard IP access list"))
					acl = "Extended IP access list" + acl;
				if(acl.endsWith(Telnet.EOL))
					acl = acl.substring(0, acl.length() - Telnet.EOL.length());
				ACL access = parse(device, acl);
				if(access != null)
					res.add(access);
			}
		}
		
		// TODO Parse other ACL
		return res;
	}
	
	/** Parse from single access-list string */
	public static ACL parse(Device device, String string) {
		if(string == null || string.equals(""))
			return null;
		ACL res = new ACL(device);
		BufferedReader reader = new BufferedReader(new StringReader(string));
		try {
			// Get type and name
			String line = reader.readLine();
			if(line.startsWith("Standard"))
				res.type = CISCO_STANDARD;
			else
				res.type = CISCO_EXTENDED;
			res.name = line.substring(line.lastIndexOf(' ') + 1);
			// Get statements
			do {
				line = reader.readLine();
				if(line == null || line.equals(""))
					break;
				line = line.replaceFirst("\\(.+\\)", "");
				line = line.trim();
				res.rules.add(line);
			}
			while (true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		res.group = device.getGroup();
		res.address = device.getAddress();
		return res;
	}
	
	/** Name of current access list */
	public String getName() {
		return name;
	}
	
	/** Retrieve access lists information from a connection */
	public static ArrayList<ACL> getACLs(Device device, CiscoIOSTelnet telnet)
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

	/** Type getter */
	public int getType() {
		return type;
	}

	/** Rules */
	public String getRulesString() {
		String res = "";
		for(String rule: rules)
			res += rule + Telnet.EOL;
		if(res.endsWith(Telnet.EOL))
			res = res.substring(0, res.length() - Telnet.EOL.length());
		return res;
	}
	
	/** Rules */
	public ArrayList<String> getRules() {
		return rules;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param rules the rules to set
	 */
	public void setRules(ArrayList<String> rules) {
		this.rules = rules;
	}
	
	/** Create a concrete access list based on a template */
	public static ACL create(String template, String group, String address) {
		try {
			ACL res = new ACL(group, address, CISCO_STANDARD, null, null);
			// Look for functions and perform evaluation
			String quote[] = template.split("\"");
			for(int i = 0; i < quote.length; i++)
				// Outside of quotes
				if(i % 2 == 0) {
					quote[i] = quote[i].replace(" ", "");
					quote[i] = quote[i].replace(Telnet.EOL, "");
					Pattern pattern = Pattern.compile("=(.+?);");
					Matcher matcher = pattern.matcher(quote[i]);
					StringBuffer buff = new StringBuffer();
					while(matcher.find()) {
						String eval = evaluate(matcher.group(1), group, address);
						if(eval == null)
							return null;
						matcher.appendReplacement(buff, "=" + eval + ';');
					}
					matcher.appendTail(buff);
					quote[i] = buff.toString();
				}
				else {
					String bracket[] = quote[i].split("\\[|\\]");
					for(int j = 0; j < bracket.length; j++)
						// Inside of quotes & brackets
						if(j % 2 == 1) {
							bracket[j] = bracket[j].replace(" ", "");
							bracket[j] = bracket[j].replace(Telnet.EOL, "");
							bracket[j] = evaluate(bracket[j], group, address);
							if(bracket[j] == null)
								return null;
						}
					quote[i] = "";
					for(String b: bracket)
						quote[i] += b;
				}
			String normalized = "";
			for(String str: quote)
				normalized += str;
			// Process variables
			String variables = null;
			String content = null;
			String curly[] = normalized.split("\\{|\\}");
			for(int i = 0; i < curly.length - 1; i += 2)
				if(curly[i].equals("Variables"))
					variables = curly[i + 1];
				else if(curly[i].equals("Content"))
					content = curly[i + 1];
			if(content == null)
				return null;
			HashMap<String, String> map = new HashMap<String, String>();
			if(variables != null) {
				String assign[] = variables.split(";");
				for(String a: assign)
					if(a.contains("=")) {
						String var = a.substring(0, a.indexOf('='));
						String val = a.substring(a.indexOf('=') + 1);
						map.put(var, val);
					}
					else
						return null;
			}
			for(String var: map.keySet())
				content = content.replace(var, map.get(var));
			
			// Update type, name & rules of ACL
			String assign[] = content.split(";");
			for(String a: assign)
				if(a.startsWith("Name=")) {
					String name = a.substring(a.indexOf('=') + 1);
					if(!name.matches("[a-zA-Z0-9_\\-]+"))
						return null;
					res.setName(name);
				}
				else if(a.startsWith("Type=")) {
					String type = a.substring(a.indexOf('=') + 1);
					if(type.trim().toLowerCase().equals("extended"))
						res.setType(ACL.CISCO_EXTENDED);
					else if(type.trim().toLowerCase().equals("standard"))
						res.setType(ACL.CISCO_STANDARD);
					else
						return null;
				}
				else if(a.startsWith("Rules")) {
					String rules = a.substring(a.indexOf('=') + 1);
					ArrayList<String> list = new ArrayList<String>();
					BufferedReader reader = new BufferedReader(new StringReader(rules));
					String line;
					try {
						do {
							line = reader.readLine();
							if(line == null)
								break;
							line = line.trim();
							line = line.replaceAll("\\t", " ");
							line = line.replaceAll(" {2,}", " ");
							if(line.equals(""))
								continue;
							list.add(line);
						}
						while(true);
					}
					catch(IOException e) {
						e.printStackTrace(System.err);
					}
					res.setRules(list);
				}
			return res;
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}
	
	
	/** Evaluate a function */
	private static String evaluate(String function, String group, String address) {
		// Host name function
		if(function.matches("hostname\\(.*\\)"))
			return Main.data.getDeviceName(group, address);
		// IP function
		if(function.equals("ip()"))
			return Main.data.getDeviceIP(group, address);
		if(function.matches(".*ip\\(.+\\).*")) {
		    ScriptEngineManager mgr = new ScriptEngineManager();
		    ScriptEngine engine = mgr.getEngineByName("JavaScript");
			Pattern pattern = Pattern.compile("ip\\((.+?)\\)");
			Matcher matcher = pattern.matcher(function);
			StringBuffer buff = new StringBuffer();
			while(matcher.find()) {
				try {
					int first = Integer.valueOf(matcher.group(1));
					// IPv4
					String ip = Main.data.getDeviceIP(group, address);
					String octet[] = ip.split("\\.");
					int ipInt = Integer.parseInt(octet[first - 1]);
					// TODO IPv6
					matcher.appendReplacement(buff, String.valueOf(ipInt));
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return null;
				}
			}
			matcher.appendTail(buff);			
			try {
				return String.valueOf(Math.round((Double) engine.eval(buff.toString())));
			} catch (ScriptException e) {
				e.printStackTrace();
				return null;
			}
		}
		// TODO Other functions
		return null;
	}
	
	/** Check for equality. Not consider group and device. */
	public boolean equals(ACL acl) {
		if(!name.equals(acl.getName()))
			return false;
		if(type != acl.getType())
			return false;
		for(int i = 0; i < rules.size(); i++)
			if(!rules.get(i).equals(acl.getRules().get(i)))
				return false;
		return true;
	}
}