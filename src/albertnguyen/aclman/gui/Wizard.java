package albertnguyen.aclman.gui;

import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JRadioButton;
import javax.swing.BoxLayout;
import javax.swing.ProgressMonitor;

import java.awt.Toolkit;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.border.TitledBorder;

import albertnguyen.aclman.device.Device;
import albertnguyen.net.IPv4;
import albertnguyen.net.Telnet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Device management wizard dialog
 */
public class Wizard extends JDialog {

	private static final long serialVersionUID = 311972912123527000L;
	
	private JComboBox<String> group; 
	private JTextField address;
	private JTextField mask;
	private JTextField user;
	private JPasswordField pass;
	private JPasswordField enable;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private boolean isAdding = true;
	private JTextArea display;
	private boolean consistent = true;
	private boolean disposed = false;

	public Wizard(JFrame parent) {
		super(parent);
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setTitle("Device Management Wizard");
		setSize(800, 600);
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
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		
		display = new JTextArea();
		display.setEditable(false);
		scrollPane.setViewportView(display);
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.WEST);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.NORTH);
		panel_2.setBorder(new EmptyBorder(0, 0, 0, 5));
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
		
		JPanel panel_3 = new JPanel();
		panel_3.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_3.setBorder(new TitledBorder(null, "Action", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.Y_AXIS));
		panel_2.add(panel_3);
		
		JRadioButton add = new JRadioButton("Add new devices");
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addSelected();
			}
		});
		panel_3.add(add);
		JRadioButton remove = new JRadioButton("Remove devices");
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				removeSelected();
			}
		});
		panel_3.add(remove);
		buttonGroup.add(add);
		add.setSelected(true);
		buttonGroup.add(remove);
		
		Component verticalStrut_6 = Box.createVerticalStrut(5);
		panel_2.add(verticalStrut_6);
		
		JLabel lblGroup = new JLabel("Group:");
		panel_2.add(lblGroup);
		
		group = new JComboBox<String>();
		group.setAlignmentX(Component.LEFT_ALIGNMENT);
		for(String grp: Main.data.getGroups())
			group.addItem(grp);
		panel_2.add(group);
		
		Component verticalStrut_5 = Box.createVerticalStrut(5);
		panel_2.add(verticalStrut_5);
		
		JLabel lblNewLabel = new JLabel("Device address:");
		panel_2.add(lblNewLabel);
		
		address = new JTextField();
		address.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_2.add(address);
		address.setColumns(10);
		
		Component verticalStrut_4 = Box.createVerticalStrut(5);
		panel_2.add(verticalStrut_4);
		
		JLabel lblNewLabel_1 = new JLabel("Wildcard mask:");
		panel_2.add(lblNewLabel_1);
		
		mask = new JTextField();
		mask.setText("0.0.0.0");
		mask.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_2.add(mask);
		mask.setColumns(10);
		
		Component verticalStrut_3 = Box.createVerticalStrut(5);
		panel_2.add(verticalStrut_3);
		
		JLabel lblUsername = new JLabel("Username:");
		panel_2.add(lblUsername);
		
		user = new JTextField();
		user.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_2.add(user);
		user.setColumns(10);
		
		Component verticalStrut_2 = Box.createVerticalStrut(5);
		panel_2.add(verticalStrut_2);
		
		JLabel lblPassword = new JLabel("Password:");
		panel_2.add(lblPassword);
		
		pass = new JPasswordField();
		pass.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_2.add(pass);
		
		Component verticalStrut_1 = Box.createVerticalStrut(5);
		panel_2.add(verticalStrut_1);
		
		JLabel lblEnablePassword = new JLabel("Enable password:");
		panel_2.add(lblEnablePassword);
		
		enable = new JPasswordField();
		enable.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_2.add(enable);
		
		Component verticalStrut = Box.createVerticalStrut(5);
		panel_2.add(verticalStrut);
		
		JButton apply = new JButton("Apply");
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						apply();
					}
				}).start();
			}
		});
		panel_2.add(apply);
		
		setVisible(true);
		display.requestFocusInWindow();
	}
	
	/** Apply */
	public void apply() {
		while(!consistent) {}
		consistent = false;
		Main.shell.interruptible = false;
		String group = (String) this.group.getSelectedItem();
		String address = this.address.getText();
		String mask = this.mask.getText();
		ArrayList<String> addresses = new ArrayList<String>();
		// IPv4
		if(address.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+"))
			addresses = IPv4.generate(address, mask);
		else {
			// TODO IPv6
		}
		if(addresses.size() == 0) {
			display.setText("Empty set of devices." + Telnet.EOL + Telnet.EOL +
					"DONE.");
			Main.shell.interruptible = true;
			return;
		}
		if(isAdding) {
			String user = this.user.getText();
			String pass = String.valueOf(this.pass.getPassword());
			String enable = String.valueOf(this.enable.getPassword());
			display.setText("Adding devices..." + Telnet.EOL + Telnet.EOL);
			ProgressMonitor monitor = new ProgressMonitor(this, null, "", 0,
					addresses.size() * 100);
			int i = 99;
			monitor.setMillisToDecideToPopup(0);
			monitor.setMillisToPopup(0);
			monitor.setProgress(i);
			for(String addr: addresses) {
				if(monitor.isCanceled()) {
					display.append(Telnet.EOL + "CANCELED!");
					consistent = true;
					Main.shell.interruptible = true;
					return;
				}
				monitor.setNote("Adding device " + ((i + 1) / 100) + "/" +
							addresses.size());
				display.append("Adding device " + addr + "... ");
				Device dev = Main.shell.getDevice(group, addr, user, pass, enable);
				if(dev == null)
					display.append("FAILED!" + Telnet.EOL);
				else {
					Device old = Main.data.getDevice(group, addr);
					if(old != null) {
						Main.data.removeDevice(group, addr);
						display.append("(replaced) ");
					}
					Main.data.addDevice(dev);
					display.append("Success." + Telnet.EOL);
				}
				i += 100;
				monitor.setProgress(i);
			}
			display.append(Telnet.EOL + "DONE.");
		}
		// Removing
		else {
			display.setText("Removing devices..." + Telnet.EOL + Telnet.EOL);
			ProgressMonitor monitor = new ProgressMonitor(this, null, "", 0,
					addresses.size() * 100);
			int i = 99;
			monitor.setMillisToDecideToPopup(0);
			monitor.setProgress(i);
			for(String addr: addresses) {
				if(monitor.isCanceled()) {
					display.append(Telnet.EOL + "CANCELED!");
					consistent = true;
					Main.shell.interruptible = true;
					return;
				}
				monitor.setNote("Removing device " + ((i + 1) / 100) + "/" +
						addresses.size());
				display.append("Removing device " + addr + "... ");
				if(Main.data.getDevice(group, addr) == null)
					display.append("Not found." + Telnet.EOL);
				else {
					Main.data.removeDevice(group, addr);
					display.append("Done." + Telnet.EOL);
				}
				i += 100;
				monitor.setProgress(i);
			}
			display.append(Telnet.EOL + "DONE.");
		}
		Main.shell.interruptible = true;
		consistent = true;
	}
	
	/** Radio button */
	public void addSelected() {
		user.setEnabled(true);
		pass.setEnabled(true);
		enable.setEnabled(true);
		isAdding = true;
	}
	
	/** Radio button */
	public void removeSelected() {
		user.setEnabled(false);
		pass.setEnabled(false);
		enable.setEnabled(false);
		isAdding = false;
	}
	
	/** Disposal */
	@Override
	public void dispose() {
		while(!consistent) {}
		if(disposed == true)
			return;
		disposed = true;
		super.dispose();
	}

}