package albertnguyen.net;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Provide telnet access to a Cisco IOS device.
 * This is not a fully functional terminal emulator but
 * only consists of libraries to be used in Java programming.  
 * */
public class CiscoIOSTelnet extends Telnet {

	/** Initialize and connect to the server */
	public CiscoIOSTelnet(String host, int port, ArrayList<String> prompts,
			int timeout) throws IOException {
		super(host, port, prompts, timeout);
	}
	
	/**
	 * Initialize and connect to the device
	 * Default parameters:
	 * Port: 23
	 * Prompt: #
	 * Timeout: 2000ms
	 * */
	public CiscoIOSTelnet(String host) throws IOException {
		super(host);
	}
	
	/** Login to Cisco device */
	public String login(String user, String pass, String enable) throws IOException {
		String res = "";
		// AAA enabled
		if(!user.equals("")) {
			setPrompt("Username: ");
			res += read();
			setPrompt("Password: ");
			res += rawResponseOf(user);
			setPrompt(">");
			addPrompt("#");
			res += rawResponseOf(pass);
			if(!enable.equals("")) {
				res += rawResponseOf("enable");
				res += rawResponseOf(enable);
			}
			String hostname = res.substring(res.lastIndexOf(Telnet.EOL) +
					Telnet.EOL.length(), res.length() - 1);
			setPrompt(hostname + "#");
			addPrompt(hostname + "(config)#");
			addPrompt(hostname + "(config-if)#");
			addPrompt(hostname + "(config-subif)#");
			addPrompt(hostname + "(config-ext-nacl)#");
			addPrompt(hostname + "(config-std-nacl)#");
			addPrompt("--More-- ");
			return res;
		}
		// AAA disabled
		else {
			setPrompt("Password: ");
			res += read();
			setPrompt(">");
			res += rawResponseOf(pass);
			setPrompt("Password: ");
			res += rawResponseOf("enable");
			setPrompt("#");
			res += rawResponseOf(enable);
			addPrompt("--More-- ");
			return res;
		}
	}
	
	/** Send command to server and read back */
	@Override
	public String rawResponseOf(String command) throws IOException {
		String res = super.rawResponseOf(command);
		while(res.endsWith("--More-- ")) {
			write(" ");
			res = res.concat(read());
		}
		return res;
	}
	
	/**
	 * Get valuable result of server response
	 * Truncate original command string, system prompt and --More--
	 * Replace all unreadable characters
	 * */
	@Override
	public String execute(String command) throws IOException {
		String res = super.execute(command);
		char ascii[] = res.toCharArray();
		res = "";
		for(char ch: ascii)
			if(ch == 10 || ch == 13 || 31 < ch && ch < 127)
				res += ch;
		res = res.replaceAll(" --More--         ", "");
		while(res.endsWith(Telnet.EOL))
			res = res.substring(0, res.length() - Telnet.EOL.length());
		return res;
	}
	
}