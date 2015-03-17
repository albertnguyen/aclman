package albertnguyen.aclman.gui;

import javax.swing.UIManager;

import albertnguyen.aclman.connection.Data;
import albertnguyen.aclman.connection.Shell;

/**
 * Main class provide entrance into program
 * */
public class Main {
	
	/** Login dialog */
	public static Login login;
	
	/** Main dashboard */
	public  static Dashboard dashboard;

	/** Database connection */
	public static Data data;
	
	/** Device shell connection */
	public static Shell shell = new Shell();
	
	/** Read-only mode */
	public static boolean ro = false;
	
	/** Let's start */
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// Login dialog
			login = new Login();
		}
		catch(Exception e) {
			// TODO Error notification
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

}