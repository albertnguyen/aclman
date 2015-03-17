package albertnguyen.net;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Provide functions to manipulate IP address strings
 * */
public class IPv4 {
	
	/** Generate addresses according to wildcard mask */
	public static ArrayList<String> generate(String address, String wildcard) {
		ArrayList<String> res = new ArrayList<String>();
		Inet4Address inet = null;
		try {
			inet = (Inet4Address) Inet4Address.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return res;
		}
		if(!wildcard.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+"))
			return res;
		// To number
		byte a[] = inet.getAddress();
		String sw[] = wildcard.split("\\.");
		byte w[] = new byte[4];
		for(int i = 0; i < 4; i++)
			w[i] = Byte.valueOf(sw[i]);
		// To binary form
		String binaddr = Integer.toBinaryString(a[0] * 256 * 256 * 256 +
				a[1] * 256 * 256 + a[2] * 256 + a[3]);
		while(binaddr.length() < 32)
			binaddr = '0' + binaddr;
		String binwild = Integer.toBinaryString(w[0] * 256 * 256 * 256 +
				w[1] * 256 * 256 + w[2] * 256 + w[3]);
		while(binwild.length() < 32)
			binwild = '0' + binwild;
		// Call recursive function
		generate(res, binaddr, binwild, 0);
		for(int i = 0; i < res.size(); i++) {
			String addr = res.get(i);
			res.remove(i);
			String octet[] = new String[4];
			octet[0] = addr.substring(0, 8);
			octet[1] = addr.substring(8, 16);
			octet[2] = addr.substring(16, 24);
			octet[3] = addr.substring(24, 32);
			addr = "" + Integer.valueOf(octet[0], 2) + '.' +
					Integer.valueOf(octet[1], 2) + '.' +
					Integer.valueOf(octet[2], 2) + '.' +
					Integer.valueOf(octet[3], 2);
			res.add(i, addr);
		}
		return res;
	}
	
	/** Recursive function to generate addresses */
	public static void generate(ArrayList<String> addresses, String address,
			String wildcard, int index) {
		if(index == 32) {
			addresses.add(address);
			return;
		}
		if(wildcard.charAt(index) == '0')
			generate(addresses, address, wildcard, index + 1);
		else {
			generate(addresses, address, wildcard, index + 1);
			char critical = address.charAt(index);
			// Negate
			if(critical == '0')
				critical = '1';
			else
				critical = '0';
			if(index == 31)
				address = address.substring(0, 31) + critical;
			else
				address = address.substring(0, index) + critical +
					address.substring(index + 1);
			generate(addresses, address, wildcard, index + 1);
		}
	}
	
}