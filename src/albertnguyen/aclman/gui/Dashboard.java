package albertnguyen.aclman.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import albertnguyen.aclman.device.ACL;
import albertnguyen.aclman.device.Device;
import albertnguyen.aclman.device.Interface;
import albertnguyen.net.Telnet;

/**
 * This is the main window for users to interact with the system
 * */
public class Dashboard extends JFrame {
	
	private static final long serialVersionUID = 8953739558993603128L;
	
	private JComboBox<String> group = new JComboBox<String>();
	private JComboBox<String> device = new JComboBox<String>();
	private JTree resource;
	private DefaultTreeModel treemodel = new DefaultTreeModel(null);
	private JTextArea display = new JTextArea();
	private JTextArea template = new JTextArea();
	private JComboBox<String> templategroup = new JComboBox<String>();
	private JComboBox<String> templatename = new JComboBox<String>();
	private JComboBox<String> syncgroup = new JComboBox<String>(
			new String[] {"-- All Groups --"});
	private JTextArea logger = new JTextArea();
	private boolean consistent = true;
	private boolean disposed = false;
	
	/** Create the board */
	public Dashboard() {
		super();
		
		// Various parameters
		setTitle("Access Control List Manipulator");
		setSize(1024, 768);
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

		// Menu
		JMenuBar menubar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem connect = new JMenuItem("Connect...");
		connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				connect();
			}
		});
		file.add(connect);
		file.addSeparator();
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}			
		});
		file.add(exit);
		menubar.add(file);
		
		JMenu inventory = new JMenu("Inventory");
		JMenuItem wizard = new JMenuItem("Device management wizard...");
		wizard.setIcon(new ImageIcon(getClass().
				getResource("/albertnguyen/aclman/res/wizard.png")));
		if(Main.ro)
			wizard.setEnabled(false);
		wizard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				wizard();
			}
		});
		inventory.add(wizard);
		JMenuItem addgroupitem = new JMenuItem("Add new group...");
		if(Main.ro)
			addgroupitem.setEnabled(false);
		addgroupitem.setIcon(new ImageIcon(getClass().
				getResource("/albertnguyen/aclman/res/addgroup.png")));
		addgroupitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addGroup();
			}
		});
		inventory.add(addgroupitem);
		JMenuItem deletegroupitem = new JMenuItem("Remove current group");
		if(Main.ro)
			deletegroupitem.setEnabled(false);
		deletegroupitem.setIcon(new ImageIcon(getClass().
				getResource("/albertnguyen/aclman/res/removegroup.png")));
		deletegroupitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeGroup();
			}
		});
		inventory.add(deletegroupitem);
		JMenuItem renamegroupitem = new JMenuItem("Rename current group");
		if(Main.ro)
			renamegroupitem.setEnabled(false);
		renamegroupitem.setIcon(new ImageIcon(getClass().
				getResource("/albertnguyen/aclman/res/renamegroup.png")));
		renamegroupitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				renameGroup();
			}
		});
		inventory.add(renamegroupitem);
		JMenuItem adddeviceitem = new JMenuItem("Add new device...");
		if(Main.ro)
			adddeviceitem.setEnabled(false);
		adddeviceitem.setIcon(new ImageIcon(getClass().
				getResource("/albertnguyen/aclman/res/adddevice.jpg")));
		adddeviceitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addDevice();
			}
		});
		inventory.add(adddeviceitem);
		JMenuItem removedeviceitem = new JMenuItem("Remove current device");
		if(Main.ro)
			removedeviceitem.setEnabled(false);
		removedeviceitem.setIcon(new ImageIcon(getClass().
				getResource("/albertnguyen/aclman/res/removedevice.jpg")));
		removedeviceitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeDevice();
			}
		});
		inventory.add(removedeviceitem);
		menubar.add(inventory);
		
		JMenu help = new JMenu("Help");
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(((JMenuItem) e.getSource()).getParent(),
						"(C) 2012 - Nguyen Binh Minh\nNetwork & Security Department\n" +
								"VCB IT Center, 198 Tran Quang Khai, Hanoi, Vietnam\n" +
								"Email: minhnb.ho@vietcombank.com.vn",
						"About",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		help.add(about);
		menubar.add(help);
		// TODO More menu bar items
		
		// Tabbed Pane
		JTabbedPane tab = new JTabbedPane();
		tab.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent e) {
		        JTabbedPane pane = (JTabbedPane) e.getSource();
		        int index = pane.getSelectedIndex();
		        select(index);
		    }
		});
		
		// Devices
		JPanel left = new JPanel(new BorderLayout());
		left.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 2));
		JPanel groupdevice = new JPanel(new GridLayout(2, 0, 0, 5));
		JPanel grouppanel = new JPanel(new BorderLayout());
		JPanel devicepanel = new JPanel(new BorderLayout());
		grouppanel.add(new JLabel("Group:  "), BorderLayout.WEST);
		group.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				groupSelected();
			}
		});
		grouppanel.add(group, BorderLayout.CENTER);
		devicepanel.add(new JLabel("Device: "), BorderLayout.WEST);
		device.setMaximumRowCount(25);
		devicepanel.add(device, BorderLayout.CENTER);
		device.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deviceSelected();
			}
		});
		groupdevice.add(grouppanel);
		groupdevice.add(devicepanel);
		groupdevice.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		JPanel resourcepanel = new JPanel(new BorderLayout());
		resource = new JTree(treemodel);
		resource.getSelectionModel().setSelectionMode(TreeSelectionModel.
				SINGLE_TREE_SELECTION);
		resource.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				resourceSelected();
			}
		});
		resource.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					treeRightClicked(e);
				}
			}
		});
		resourcepanel.add(new JScrollPane(resource), BorderLayout.CENTER);
		left.add(groupdevice, BorderLayout.NORTH);
		left.add(resourcepanel, BorderLayout.CENTER);
		
		// Right Panel
		JPanel right = new JPanel(new BorderLayout());
		display.setEditable(false);
		JScrollPane aclscroll = new JScrollPane(display);
		right.add(aclscroll, BorderLayout.CENTER);
		right.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 5));
		JSplitPane split = new InvisibleJSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setLeftComponent(left);
		split.setRightComponent(right);
		split.setBorder(null);		
		tab.add("Devices", split);
		
		// ACL Templates
		JPanel templates = new JPanel(new BorderLayout());
		templates.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		JPanel templatetop = new JPanel(new GridLayout(1, 2));
		JPanel templatetopleft = new JPanel(new WrapLayout());
		JPanel templatetopright = new JPanel(new WrapLayout());
		templatetopleft.add(new JLabel("Template:"));
		templatename.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				templateSelected();
			}
		});
		templatetopleft.add(templatename);
		JButton templatenew = new JButton("New");
		if(Main.ro)
			templatenew.setEnabled(false);
		templatenew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addTemplate();
			}
		});
		templatetopleft.add(templatenew);
		JButton templatesave = new JButton("Save");
		if(Main.ro)
			templatesave.setEnabled(false);
		templatesave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveTemplate();
			}
		});
		templatetopleft.add(templatesave);
		JButton templatedelete = new JButton("Delete");
		if(Main.ro)
			templatedelete.setEnabled(false);
		templatedelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeTemplate();
			}
		});
		templatetopleft.add(templatedelete);
		templatetopright.add(new JLabel("Apply to group: "));
		templatetopright.add(templategroup);
		JButton preview = new JButton("Preview");
		preview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				preview();
			}
		});
		templatetopright.add(preview);
		JButton create = new JButton("Create");
		if(Main.ro)
			create.setEnabled(false);
		create.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						createACLs();
					}

				}).start();
			}
		});
		templatetopright.add(create);
		JButton remove = new JButton("Remove");
		if(Main.ro)
			remove.setEnabled(false);
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						removeACLs();
					}

				}).start();
			}
		});
		templatetopright.add(remove);
		templatetop.add(templatetopleft);
		templatetop.add(templatetopright);
		templates.add(templatetop, BorderLayout.NORTH);
		templates.add(new JScrollPane(template), BorderLayout.CENTER);
		tab.add("ACL Templates", templates);
		
		// Synchronization
		JPanel syncpanel = new JPanel(new BorderLayout());
		syncpanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		JPanel synccontrol =  new JPanel(new WrapLayout());
		synccontrol.add(new JLabel("Select Group:"));
		synccontrol.add(syncgroup);
		JButton push = new JButton("Push Configuration");
		if(Main.ro)
			push.setEnabled(false);
		push.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						pushConfig();
					}
				}).start();
			}
		});
		synccontrol.add(push);
		JButton get = new JButton("Get Configuration");
		if(Main.ro)
			get.setEnabled(false);
		get.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						getConfig();
					}
				}).start();
			}
		});
		synccontrol.add(get);
		syncpanel.add(synccontrol, BorderLayout.NORTH);
		logger.setEditable(false);
		syncpanel.add(new JScrollPane(logger), BorderLayout.CENTER);
		tab.add("Synchronization", syncpanel);
		
		// Show off!
		setJMenuBar(menubar);
		getContentPane().add(tab);
		setVisible(true);
		split.setDividerLocation(.33);
		resource.requestFocusInWindow();
	}
	
	/** Disposal */
	@Override
	public void dispose() {
		while(!consistent) {}
		if(disposed == true)
			return;
		disposed = true;
		Main.login.dispose();
		Main.data.dispose();
		super.dispose();
	}
	
	/** Tab changed */
	public void select(int index) {
		switch(index) {
			case 0:
				resource.requestFocusInWindow();
				break;
			case 1:
				template.requestFocusInWindow();
				break;
			case 2:
				logger.requestFocusInWindow();
				break;
			default:
				break;
		}
	}
	
	/** Login to another database server */
	public void connect() {
		Main.login.setModal(true);
		Main.login.setVisible(true);
	}
	
	/** Update info from new connection */
	public void updateConnection() {
		while(!consistent) {}
		group.removeAllItems();
		device.removeAllItems();
		for(String grp: Main.data.getGroups()) {
			group.addItem(grp);
			templategroup.addItem(grp);
			syncgroup.addItem(grp);
		}
		for(String tmpl: Main.data.getTemplates())
			templatename.addItem(tmpl);
		
	}
	
	/** User selected an item on the group combo box */
	public void groupSelected() {
		while(!consistent) {}
		device.removeAllItems();
		for(String dev: Main.data.getDeviceList((String) group.getSelectedItem()))
			device.addItem(dev);
	}
	
	/** User selected an item on the device combo box */
	@SuppressWarnings("unchecked")
	public void deviceSelected() {
		while(!consistent) {}
		String address = (String) device.getSelectedItem();
		// None selected
		if(address == null)
			return;
		address = address.substring(address.indexOf('[') + 1, address.indexOf(']'));
		Device dev = Main.data.getDevice((String) group.getSelectedItem(), address); 
		
		// Resource update
		TreeNode<Device> root = new TreeNode<Device>(dev, dev.getName() + " [" +
				dev.getAddress() + "]: " + dev.getDescription());
		treemodel.setRoot(root);
		TreeNode<String> defaultintf = new TreeNode<String>("Access Interface: " +
				dev.getAccessInterface(),
				"Access Interface: " + dev.getAccessInterface());
		treemodel.insertNodeInto(defaultintf, root, root.getChildCount());
		TreeNode<Object> intfs = new TreeNode<Object>(null, "Interfaces");
		treemodel.insertNodeInto(intfs, root, root.getChildCount());
		TreeNode<Object> acls = new TreeNode<Object>(null, "Access lists");
		treemodel.insertNodeInto(acls, root, root.getChildCount());
		for(Interface intf: dev.getInterfaces()) {
			TreeNode<Interface> intfnode = new TreeNode<Interface>(intf, intf.getName());
			treemodel.insertNodeInto(intfnode, intfs, intfs.getChildCount());
		}
		for(ACL acl: dev.getACLs()) {
			TreeNode<ACL> aclnode = new TreeNode<ACL>(acl, acl.getName());
			treemodel.insertNodeInto(aclnode, acls, acls.getChildCount());
		}
		for(int i = 0; i < resource.getRowCount(); i++)
			resource.expandRow(i);
		display.setText(((TreeNode<Device>) treemodel.getRoot()).getLink().toString());
		display.setCaretPosition(0);
	}
	
	/** Add a group */
	public void addGroup() {
		while(!consistent) {}
		String name = JOptionPane.showInputDialog(this, "New group name:");
		if(name == null)
			return;
		if(name.trim().equals("")) {
			JOptionPane.showMessageDialog(this,
					"Name error",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		name = name.trim();
		for(int i = 0; i < group.getItemCount(); i++)
			if(name.equals(group.getItemAt(i))) {
				JOptionPane.showMessageDialog(this,
						"Group exists!",
						"Duplicate",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
		Main.data.addGroup(name);
		group.addItem(name);
		templategroup.addItem(name);
		syncgroup.addItem(name);
	}
	
	/** Delete current group */
	public void removeGroup() {
		while(!consistent) {}
		String name = (String) group.getSelectedItem();
		if(name == null || name.equals(""))
			return;
		if(JOptionPane.showConfirmDialog(this,
				"Do you really want to delete group " + name,
				"Confirm removal",
				JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
			return;
		}
		Main.data.removeGroup(name);
		group.removeItem(name);
		templategroup.removeItem(name);
		syncgroup.removeItem(name);
		if(group.getItemCount() == 0)
			device.removeAllItems();
	}
	
	/** Rename a group */
	public void renameGroup() {
		while(!consistent) {}
		String name = (String) group.getSelectedItem();
		if(name == null || name.equals(""))
			return;
		String newname = JOptionPane.showInputDialog(this, "Change " + name + " to:",
				"Rename group",	JOptionPane.PLAIN_MESSAGE);
		if(newname == null)
			return;
		if(newname.trim().equals("")) {
			JOptionPane.showMessageDialog(this,
					"Name error",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		newname = newname.trim();
		Main.data.renameGroup(name, newname);
		group.removeItem(name);
		group.addItem(newname);
		group.setSelectedItem(newname);
		templategroup.removeItem(name);
		templategroup.addItem(newname);
		syncgroup.removeItem(name);
		syncgroup.addItem(newname);
	}
	
	/** Add a device */
	public void addDevice() {
		while(!consistent) {}
		JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
		panel.add(new JLabel("Group:"));
		JComboBox<String> group = new JComboBox<String>();
		for(int i = 0; i < this.group.getItemCount(); i++)
			group.addItem(this.group.getItemAt(i));
		if(this.group.getSelectedIndex() >= 0)
			group.setSelectedIndex(this.group.getSelectedIndex());
		else
			group.setSelectedIndex(-1);
		panel.add(group);
		panel.add(new JLabel("Address:"));
		JTextField address = new JTextField();
		panel.add(address);
		panel.add(new JLabel("User:"));
		JTextField user = new JTextField();
		panel.add(user);
		panel.add(new JLabel("Password:"));
		JPasswordField pass = new JPasswordField();
		panel.add(pass);
		panel.add(new JLabel("Enable Password:"));
		JPasswordField enable = new JPasswordField();
		panel.add(enable);
		if(JOptionPane.showConfirmDialog(this, panel, "Add new device",
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
			return;
		if(address.getText() == null || address.getText().equals("")) {
			JOptionPane.showMessageDialog(this,
					"Please enter the address of the device.",
					"Address cannot be empty",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// Get info from real device
		Device dev = Main.shell.getDevice((String) group.getSelectedItem(),
				address.getText(), user.getText(), String.valueOf(pass.getPassword()),
				String.valueOf(enable.getPassword()));
		
		// Prompt user to select default interface
		JComboBox<String> accessintf = new JComboBox<String>();
		for(Interface intf: dev.getInterfaces()) {
			String in = intf.getName() + " inbound";
			if(intf.getInboundACL() != null)
				in += ": " + intf.getInboundACL().getName();
			accessintf.addItem(in);			
			String out = intf.getName() + " outbound";
			if(intf.getOutboundACL() != null)
				out += ": " + intf.getOutboundACL().getName();
			accessintf.addItem(out);
		}
		JOptionPane.showMessageDialog(this, accessintf,
				"Please select access interface", JOptionPane.QUESTION_MESSAGE);
		dev.setAccessInterface(((String) accessintf.getSelectedItem()).
				replaceFirst(": .+", ""));

		// Push to database
		Main.data.addDevice(dev);
		
		// Show on combo box
		device.addItem(dev.getName() + " [" + dev.getAddress() + ']');
		device.setSelectedIndex(device.getItemCount() - 1);
	}
	
	/** Resource selected */
	public void resourceSelected() {
		while(!consistent) {}
		@SuppressWarnings("unchecked")
		TreeNode<Object> node = (TreeNode<Object>)
                resource.getLastSelectedPathComponent();
		if(node == null || node.getLink() == null)
			display.setText("");
		else {
			display.setText(node.getLink().toString());
			display.setCaretPosition(0);
		}
	}
	
	/** Remove device */
	public void removeDevice() {
		while(!consistent) {}
		String currentgroup = (String) group.getSelectedItem();
		String currentdevice = ((String) device.getSelectedItem());
		if(JOptionPane.showConfirmDialog(this,
				"Do you really want to remove device " + currentdevice + '?',
				"Confirm removal", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
		String currentaddress = currentdevice.substring(currentdevice.indexOf('[') + 1,
				currentdevice.indexOf(']'));
		Main.data.removeDevice(currentgroup, currentaddress);
		device.removeItem(currentdevice);
		treemodel.setRoot(null);
	}
	
	/** Add new access list template */
	public void addTemplate() {
		while(!consistent) {}
		String name = JOptionPane.showInputDialog(this, "Template name");
		if(name == null)
			return;
		if(name.trim().equals("")) {
			JOptionPane.showMessageDialog(this, "Name error!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		name = name.trim();
		Main.data.addTemplate(name);
		templatename.addItem(name);
		templatename.setSelectedItem(name);
		template.setText("");
	}
	
	/** Remove access list template */
	public void removeTemplate() {
		while(!consistent) {}
		if(JOptionPane.showConfirmDialog(this,
				"Do you really want to delete current template?",
				"Confirm removal",
				JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
		String name = (String) templatename.getSelectedItem();
		Main.data.removeTemplate(name);
		template.setText("");
		templatename.removeItem(name);
	}
	
	/** Show template */
	public void templateSelected() {
		while(!consistent) {}
		template.setText(Main.data.getTemplate((String) templatename.getSelectedItem()));
		template.setCaretPosition(0);
	}
	
	/** Save template */
	public void saveTemplate() {
		while(!consistent) {}
		Main.data.saveTemplate((String) templatename.getSelectedItem(), template.getText());
		JOptionPane.showMessageDialog(this, "Template " + templatename.getSelectedItem() +
				" saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/** Template application preview */
	public void preview() {
		while(!consistent) {}
		String group = (String) templategroup.getSelectedItem();
		ArrayList<String> devices = Main.data.getDeviceAddresses(group);
		new PreviewDialog(this, group, devices, template.getText());
	}
	
	/** Apply template on selected group */
	public void createACLs() {
		while(!consistent) {}
		consistent = false;
		String group = (String) templategroup.getSelectedItem();
		ArrayList<String> devices = Main.data.getDeviceAddresses(group);
		/*int i = 99;
		monitor = new ProgressMonitor(this, null, "", 0,
				devices.size() * 100);*/
		for(String address: devices) {
			ACL acl = ACL.create(template.getText(), group, address);
			if(acl == null) {
				JOptionPane.showMessageDialog(this, "Encounter error when applying to " +
						address + ". Please check the syntax of your template.",
						"Parse error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Main.data.removeACL(group, address, acl.getName());
			// Add new access list and apply to access interface
			Main.data.addACL(acl);
			Main.data.applyACL(group, address, acl);
			if(this.group.getSelectedIndex() == templategroup.getSelectedIndex())
				if(device.getSelectedIndex() >= 0)
					device.setSelectedIndex(device.getSelectedIndex());
		}
		JOptionPane.showMessageDialog(this, "Applied successfully.",
				"Success", JOptionPane.INFORMATION_MESSAGE);
		consistent = true;
	}
	
	/** Remove access lists from group based on template */
	public void removeACLs() {
		while(!consistent) {}
		consistent = false;
		String group = (String) templategroup.getSelectedItem();
		ArrayList<String> devices = Main.data.getDeviceAddresses(group);
		// Delete old access lists
		for(String address: devices) {
			ACL acl = ACL.create(template.getText(), group, address);
			if(acl == null) {
				JOptionPane.showMessageDialog(this, "Encounter error when applying to " +
						address + ". Please check the syntax of your template.",
						"Parse error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Main.data.removeACL(group, address, acl.getName());
			if(this.group.getSelectedIndex() == templategroup.getSelectedIndex())
				if(device.getSelectedIndex() >= 0)
					device.setSelectedIndex(device.getSelectedIndex());
		}
		JOptionPane.showMessageDialog(this, "Applied successfully.",
				"Success", JOptionPane.INFORMATION_MESSAGE);
		consistent = true;
	}
	
	/** Pop up menu for resources */
	public void treeRightClicked(MouseEvent e) {
        TreePath selPath = resource.getPathForLocation(e.getX(), e.getY());
		resource.setSelectionPath(selPath);
		@SuppressWarnings("unchecked")
		TreeNode<Object> node = (TreeNode<Object>) selPath.getLastPathComponent();
		Object link = node.getLink();
		if(link == null)
			return;
		try {
			if(link.getClass().equals(Class.forName("albertnguyen.aclman.device." +
					"Interface"))) {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem inbound = new JMenuItem("Set Access Interface: Inbound");
				if(Main.ro)
					inbound.setEnabled(false);
				inbound.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						setAccessIntfIn();
					}
				});
				popup.add(inbound);
				JMenuItem outbound = new JMenuItem("Set Access Interface: Outbound");
				if(Main.ro)
					outbound.setEnabled(false);
				outbound.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						setAccessIntfOut();
					}
				});
				popup.add(outbound);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
			else if(link.getClass().equals(Class.forName("albertnguyen.aclman.device." +
					"ACL"))) {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem remove = new JMenuItem("Remove");
				if(Main.ro)
					remove.setEnabled(false);
				remove.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						removeACL();
					}
				});
				popup.add(remove);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
		catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	/** Set an interface as access interface with inbound direction */
	@SuppressWarnings("unchecked")
	public void setAccessIntfIn() {
		while(!consistent) {}
		consistent = false;
		TreeNode<Interface> node = (TreeNode<Interface>) resource.getSelectionPath().
			getLastPathComponent();
		Interface intf = node.getLink();
		Main.data.setAccessIntf(intf.getGroup(), intf.getAddress(), intf.getName() +
				" inbound");
		treemodel.removeNodeFromParent((TreeNode<Device>) treemodel.
				getChild(treemodel.getRoot(), 0));
		String access = "Access Interface: " + intf.getName() + " inbound";
		treemodel.insertNodeInto(new TreeNode<String>(access, access), (TreeNode<Device>)
				treemodel.getRoot(), 0);
		consistent = true;
	}
	
	/** Set an interface as access interface with Outbound direction */
	@SuppressWarnings("unchecked")
	public void setAccessIntfOut() {
		while(!consistent) {}
		consistent = false;
		TreeNode<Interface> node = (TreeNode<Interface>) resource.getSelectionPath().
				getLastPathComponent();
		Interface intf = node.getLink();
		Main.data.setAccessIntf(intf.getGroup(), intf.getAddress(), intf.getName() +
				" outbound");
		treemodel.removeNodeFromParent((TreeNode<Device>) treemodel.
				getChild(treemodel.getRoot(), 0));
		String access = "Access Interface: " + intf.getName() + " outbound";
		treemodel.insertNodeInto(new TreeNode<String>(access, access), (TreeNode<Device>)
				treemodel.getRoot(), 0);
		consistent = true;
	}
	
	/** Remove a single access list */
	public void removeACL() {
		while(!consistent) {}
		consistent = false;
		@SuppressWarnings("unchecked")
		TreeNode<ACL> node = (TreeNode<ACL>) resource.getSelectionPath().
			getLastPathComponent();
		ACL acl = node.getLink();
		Main.data.removeACL(acl.getGroup(), acl.getAddress(), acl.getName());
		if(device.getSelectedIndex() >= 0)
			device.setSelectedIndex(device.getSelectedIndex());
		consistent = true;
	}
	
	/** Get configuration from real devices */
	public void getConfig() {
		while(!consistent) {}
		consistent = false;
		Main.shell.interruptible = false;
		String selectedgroup = (String) syncgroup.getSelectedItem();
		ArrayList<String> devices = null;
		ArrayList<String> groups = new ArrayList<String>();
		if(selectedgroup.equals("-- All Groups --")) {
			// All
			devices = new ArrayList<String>();
			ArrayList<String> grouplist = Main.data.getGroups();
			for(String group: grouplist) {
				ArrayList<String> devlst = Main.data.getDeviceList(group); 
				devices.addAll(devlst);
				for(int i = 0; i < devlst.size(); i++)
					groups.add(group);
			}
		}
		else {
			// Single group
			devices = Main.data.getDeviceList(selectedgroup);
			for(int i = 0; i < devices.size(); i++)
				groups.add(selectedgroup);
		}
		if(devices.size() == 0) {
			logger.setText("Empty set of devices. " + Telnet.EOL + Telnet.EOL +
					"DONE!");
			Main.shell.interruptible = true;
			return;
		}
		ProgressMonitor monitor = new ProgressMonitor(this, null, "", 0,
				devices.size() * 100);
		monitor.setMillisToDecideToPopup(0);
		monitor.setMillisToPopup(0);
		if(selectedgroup.equals("-- All Groups --"))
			logger.setText("Get configuration of all groups:" + Telnet.EOL + Telnet.EOL);
		else
			logger.setText("Get configuration of group: " + selectedgroup + Telnet.EOL +
					Telnet.EOL);
		int i = 99;
		for(String address: devices) {
			if(monitor.isCanceled()) {
				logger.append(Telnet.EOL + "CANCELED!");
				consistent = true;
				Main.shell.interruptible = true;
				return;
			}
			monitor.setNote("Processing device " + ((i + 1) / 100) + " of total " +
					devices.size());
			monitor.setProgress(i);
			i += 100;
			String group = groups.get(devices.indexOf(address));
			logger.append("Getting group " + group + ", device " + address + "... ");
			address = address.substring(address.indexOf('[') + 1, address.indexOf(']'));
			Device device = Main.data.getDevice(group, address);
			Device real = Main.shell.getDevice(group, address, device.getUser(),
					device.getPass(), device.getEnable());
			if(real == null) {
				logger.append("FAILED!" + Telnet.EOL);
				continue;
			}
			real.setAccessInterface(device.getAccessInterface());
			Main.data.removeDevice(group, address);
			Main.data.addDevice(real);
			logger.append("Success!" + Telnet.EOL);
		}
		logger.append(Telnet.EOL + "DONE!");
		monitor.close();
		
		if(syncgroup.getSelectedIndex() == 0 ||
				this.group.getSelectedIndex() == syncgroup.getSelectedIndex() - 1)
			if(device.getSelectedIndex() >= 0)
				device.setSelectedIndex(device.getSelectedIndex());
		Main.shell.interruptible = true;
		consistent = true;
	}
	
	/** Push configuration to real devices */
	public void pushConfig() {
		while(!consistent) {}
		consistent = false;
		Main.shell.interruptible = false;
		String selectedgroup = (String) syncgroup.getSelectedItem();
		ArrayList<String> devices = null;
		ArrayList<String> groups = new ArrayList<String>();
		if(selectedgroup.equals("-- All Groups --")) {
			// All
			devices = new ArrayList<String>();
			ArrayList<String> grouplist = Main.data.getGroups();
			for(String group: grouplist) {
				ArrayList<String> devlst = Main.data.getDeviceList(group); 
				devices.addAll(devlst);
				for(int i = 0; i < devlst.size(); i++)
					groups.add(group);
			}
		}
		else {
			// Single group
			devices = Main.data.getDeviceList(selectedgroup);
			for(int i = 0; i < devices.size(); i++)
				groups.add(selectedgroup);
		}
		if(devices.size() == 0) {
			logger.setText("Empty set of devices. " + Telnet.EOL + Telnet.EOL +
					"DONE!");
			Main.shell.interruptible = true;
			return;
		}
		ProgressMonitor monitor = new ProgressMonitor(this, null, "", 0,
				devices.size() * 100);
		monitor.setMillisToDecideToPopup(0);
		monitor.setMillisToPopup(0);
		monitor.setProgress(99);
		if(selectedgroup.equals("-- All Groups --"))
			logger.setText("Push configuration to all groups:" + Telnet.EOL + Telnet.EOL);
		else
			logger.setText("Push configuration to group: " + selectedgroup + Telnet.EOL +
					Telnet.EOL);
		int i = 99;
		for(String address: devices) {
			if(monitor.isCanceled()) {
				logger.append(Telnet.EOL + "CANCELED!");
				Main.shell.interruptible = true;
				consistent = true;
				return;
			}
			monitor.setNote("Processing device " + ((i + 1) / 100) + " of total " +
					devices.size());
			monitor.setProgress(i);
			i += 100;
			String group = groups.get(devices.indexOf(address));
			logger.append("Setting group " + group + ", device " + address + "... ");
			address = address.substring(address.indexOf('[') + 1, address.indexOf(']'));
			Device device = Main.data.getDevice(group, address);
			if(!Main.shell.pushConfig(device))
				logger.append("FAILED!" + Telnet.EOL);
			else
				logger.append("Success!" + Telnet.EOL);
		}
		logger.append(Telnet.EOL + "DONE!");
		monitor.close();
		if(syncgroup.getSelectedIndex() == 0 ||
				this.group.getSelectedIndex() == syncgroup.getSelectedIndex() - 1)
			if(device.getSelectedIndex() >= 0)
				device.setSelectedIndex(device.getSelectedIndex());
		Main.shell.interruptible = true;
		consistent = true;
	}
	
	/** Open wizard dialog */
	public void wizard() {
		new Wizard(this);
	}
	
}