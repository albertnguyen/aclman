package albertnguyen.aclman.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.*;

import albertnguyen.aclman.connection.Data;

/**
 * Login to database server
 */
public class Login extends JDialog {

	private static final long serialVersionUID = -3251079245381386402L;

	private JTextField host;
	private JTextField port;
	private JTextField user;
	private JTextField pass = new JPasswordField();
	
	/** Create dialog */
	public Login() {
		super();
		Properties prop = new Properties();
		String h = "localhost";
		String p = "3306";
		String u = "aclman";
		try {
			prop.loadFromXML(new FileInputStream(new File("./ACLMan-config.xml")));
			h = prop.getProperty("host", "localhost");
			p = prop.getProperty("port", "3306");
			u = prop.getProperty("user", "aclman");
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		host = new JTextField(h);
		port = new JTextField(p);
		user = new JTextField(u);
		setTitle("ACLMan Login");
		setSize(240, 180);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		setLocation((screenSize.width - frameSize.width) / 2,
				(screenSize.height - frameSize.height) / 2);
		setIconImage(new ImageIcon(getClass().
				getResource("/albertnguyen/aclman/res/computer.png")).getImage());
		JPanel panelLogin = new JPanel();
		panelLogin.setLayout(new GridLayout(0, 2, 5, 5));
		panelLogin.add(new JLabel("MySQL Server:"));
		host.addActionListener(new LoginActionListener());
		panelLogin.add(host);
		panelLogin.add(new JLabel("Port:"));
		port.addActionListener(new LoginActionListener());
		panelLogin.add(port);
		panelLogin.add(new JLabel("User:"));
		user.addActionListener(new LoginActionListener());
		panelLogin.add(user);
		panelLogin.add(new JLabel("Password:"));
		pass.addActionListener(new LoginActionListener());
		panelLogin.add(pass);

		// Connect to MySQL database and show main window
		JButton connect = new JButton("Connect");
		connect.addActionListener(new LoginActionListener());
		panelLogin.add(connect);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		panelLogin.add(cancel);
		
		// Border
		panelLogin.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));		
		getContentPane().add(panelLogin);
		setVisible(true);
		pass.requestFocusInWindow();
	}

	/** Set visibility, dispose on cancel */
	@Override
	public void setVisible(boolean b) {
		if (b == false)
			if (Main.dashboard == null)
				dispose();
		super.setVisible(b);
	}

	/** Connect to database */
	public void connect() {

		try {
			if(Main.data != null)
				Main.data.dispose();
			// Start new connection
			Data data = new Data(host.getText(), port.getText(), user.getText(),
					pass.getText());
			Main.data = data;
			if (Main.dashboard == null) {
				Main.dashboard = new Dashboard();
			}
			Main.dashboard.updateConnection();
			setVisible(false);
			Properties prop = new Properties();
			prop.setProperty("host", host.getText());
			prop.setProperty("port", port.getText());
			prop.setProperty("user", user.getText());
			prop.storeToXML(new FileOutputStream(new File("./ACLMan-config.xml")), null);
		}
		catch (SQLException | IOException ex) {
			JOptionPane.showMessageDialog(this,
					"Error connecting to database, please try again.", "ERROR",
					JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace(System.err);
		}
	}

}

class LoginActionListener implements ActionListener {
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Main.login.connect();
	}
}
