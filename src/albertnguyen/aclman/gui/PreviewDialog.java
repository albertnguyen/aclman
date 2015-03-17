/**
 * 
 */
package albertnguyen.aclman.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import albertnguyen.aclman.device.ACL;
import albertnguyen.net.Telnet;

/**
 * Appears when user click preview button
 */
public class PreviewDialog extends JDialog {

	private static final long serialVersionUID = -5144956754483089540L;
	private JTextArea display = new JTextArea();
	private ArrayList<String> devices;
	private int index = 0;
	private String group;
	private String template;
	private JComboBox<String> jump;
	
	/** Create dialog */
	public PreviewDialog(JFrame parent, String group, ArrayList<String> devices,
			String template) {
		super(parent);
		this.group = group;
		this.devices = devices;
		this.template = template;
		this.setModalityType(DEFAULT_MODALITY_TYPE);
		setTitle("Template application preview");
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
		// Objects in dialog
		JPanel panel = new JPanel(new BorderLayout());
		display.setEditable(false);
		panel.add(new JScrollPane(display), BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		JPanel control = new JPanel(new WrapLayout());
		control.add(new JLabel("Device address:"));
		jump = new JComboBox<String>();
		jump.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				jump();
			}
		});
		control.add(jump);
		JButton previous = new JButton("<< Previous");
		previous.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				previous();
			}
		});
		control.add(previous);
		JButton next = new JButton("Next >>");
		next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				next();
			}
		});
		control.add(next);
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		if(devices.size() == 0) {
			display.setText("Empty set of devices!");
			jump.addItem("-- Empty --");
			jump.setEnabled(false);
			previous.setEnabled(false);
			next.setEnabled(false);
		}
		else {
			display.setText("Device 1/" + devices.size() + Telnet.EOL + "Address: " +
					devices.get(0) + Telnet.EOL + Telnet.EOL +
					ACL.create(template, group, devices.get(0)));
			for(String dev: devices)
				jump.addItem(dev);
		}
		control.add(close);
		panel.add(control, BorderLayout.SOUTH);
		getContentPane().add(panel);
		setVisible(true);
		display.requestFocusInWindow();
	}
	
	/** Set display text */
	public void setText(String text) {
		display.setText(text);
	}
	
	/** Disposal */
	@Override
	public void dispose() {
		super.dispose();
	}
	
	/** Previous device */
	public void previous() {
		index--;
		if(index < 0)
			index = devices.size() - 1;
		jump.setSelectedIndex(index);
	}
	
	/** Next device */
	public void next() {
		index++;
		if(index >= devices.size())
			index = 0;
		jump.setSelectedIndex(index);
	}
	
	/** Jump to a device*/
	public void jump() {
		index = jump.getSelectedIndex();
		display.setText("Device " + (index + 1) + '/' + devices.size() + Telnet.EOL +
				"Address: " + devices.get(index) + Telnet.EOL + Telnet.EOL +
				ACL.create(template, group, devices.get(index)));
	}
	
}