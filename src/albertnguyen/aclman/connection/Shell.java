package albertnguyen.aclman.connection;

import java.io.IOException;

import javax.swing.JOptionPane;

import albertnguyen.aclman.device.Device;
import albertnguyen.aclman.gui.Main;

/**
 * Extend to use with this application
 * */
public class Shell {
	
	private String address;
	
	/** Allow notifying error by a session dialog*/
	public boolean interruptible = true;
	
	/** Connection failed */
	public void notifySessionError() {
		if(interruptible)
			JOptionPane.showMessageDialog(Main.dashboard,
					"Connection failed. Check telnet paramenters to " + address,
					"ERROR", JOptionPane.ERROR_MESSAGE);
	}
	
	/** Retrieve device info */
	public Device getDevice(String group, String address, String user, String pass,
			String enable) {
		this.address = address;
		Device dev = null;
		try {
			dev = Device.getDevice(group, address, user, pass, enable);
		} catch (IOException e) {
			notifySessionError();
			e.printStackTrace();
			return null;
		}
		return dev;
	}
	
	/** Push device configuration */
	public boolean pushConfig(Device device) {
		address = device.getAddress(); 
		try {
			device.pushConfig();
			return true;
		} catch (IOException e) {
			notifySessionError();
			e.printStackTrace();
			return false;
		}
	}
	
}