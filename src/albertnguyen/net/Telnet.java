package albertnguyen.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Provide telnet access to a server.
 * This is not a fully functional terminal emulator but
 * only consists of libraries to be used in Java programming.  
 * */
public class Telnet {
	
	/** Socket entity */
	private Socket sock;
	
	/** Stream to read from telnet server */
	private InputStream in;
	
	/** Stream to write to telnet server */
	private OutputStream out;
	
	/** System prompt, this is the hint to stop reading */
	private ArrayList<String> prompts;
	
	/** Initialize and connect to the server */
	public Telnet(String host, int port, ArrayList<String> prompts, int timeout)
			throws IOException {
		connect(host, port, prompts, timeout);
	}
	
	/** End Of Line constant*/
	public static final String EOL = System.getProperty("line.separator");
	
	/**
	 * Initialize and connect to the server
	 * Default parameters: Port: 23, Prompt: #, Timeout: 2000ms
	 * */
	public Telnet(String host) throws IOException {
		ArrayList<String> prompts = new ArrayList<String>();
		prompts.add("#");
		connect(host, 23, prompts, 2000);
	}
	
	/**
	 * Connect to the server
	 * @param host: Name or IP Address
	 * @param port: Port number
	 * @param user: Username
	 * @param pass: Password
	 * @param prompt: System prompt
	 * @param timeout: Reading time out
	 * */
	private void connect(String host, int port, ArrayList<String> prompts, int timeout) throws IOException {
		sock = new Socket();
		sock.connect(new InetSocketAddress(host, port), timeout);
		sock.setSoTimeout(timeout);
		in = sock.getInputStream();
		out = sock.getOutputStream();
		this.prompts = prompts;
	}
	
	/** Finish working */
	public void disconnect() throws IOException {
		in.close();
		out.close();
		sock.close();
	}
	
	/** Read message from server */
	public String read() throws IOException {
		String res = "";
		do {
			int ch = in.read();
			//if(ch == 10 || ch == 13 || ch > 31 && ch < 127)
				res += (char) ch;
			//else
				//res = res + '[' + ch + ']';
			for(String prompt: prompts)
				if(res.endsWith(prompt))
					return res;
		}
		while(true);
	}
	
	/** Write to output stream */
	public void write(String command) throws IOException {
		for(char ch: command.toCharArray())
			out.write(ch);
	}
	
	/** Send command to server and read back */
	public String rawResponseOf(String command) throws IOException {
		write(command);
		for(char ch: EOL.toCharArray())
			out.write(ch);
		return read();
	}
	
	/** Get Prompts */
	public ArrayList<String> getPrompts() {
		return prompts;
	}
	
	/** Set a single prompt */
	public void setPrompt(String prompt) {
		ArrayList<String> res = new ArrayList<String>();
		res.add(prompt);
		prompts = res;
	}
	
	/** Add new prompt*/
	public void addPrompt(String prompt) {
		prompts.add(prompt);
	}
	
	/** Remove a prompt */
	public void removePrompt(String prompt) {
		for(String p: prompts)
			if(p.equals(prompt))
				prompts.remove(p);
	}
	
	/** Get valuable result of response */
	public String execute(String command) throws IOException {
		String res = rawResponseOf(command);
		res = res.substring(res.indexOf(EOL) + EOL.length(), res.lastIndexOf(EOL) + EOL.length());
		return res;
	}
	
	/** Time out setter */
	public void setTimeout(int milisec) {
		try {
			sock.setSoTimeout(milisec);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}