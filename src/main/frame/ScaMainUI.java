package main.frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import main.frame.utils.ImageFactory;

public class ScaMainUI extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7301356288228314167L;
	private static final String ROOT_ELEMENT = "Root";
	private static final int WINDOW_WIDTH = 800;

	private static final int WINDOW_HEIGHT = 600;

	private static final int MAX_DEPTH = 2;

	private static final int TOOLBAR_WIDTH = 34;

	private static final Dimension COMPONENTS_DIM = new Dimension(WINDOW_WIDTH / 2 - TOOLBAR_WIDTH / 2, WINDOW_HEIGHT);
	private static final Dimension TOOLBAR_DIM = new Dimension(TOOLBAR_WIDTH, WINDOW_HEIGHT);
	private static final Dimension BUTTON_DIM = new Dimension(30, 30);

	private static final String MOVE_ALL_IMAGE = "/main/images/move_all.gif";
	private static final String MOVE_IMAGE = "/main/images/move.gif";
	private static final String REMOVE_IMAGE = "/main/images/remove.gif";
	private static final String REMOVE_ALL_IMAGE = "/main/images/remove_all.gif";

	private JTree m_tree = new JTree();
	private JTable m_table = new XMLTable();
	private JTextField m_xmlFileName = new JTextField();

	public ScaMainUI(String xmlFolder) {
		super("Main window");
		try {
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			 System.setProperty("sun.java2d.ddscale", "true");
//			 UIManager.setLookAndFeel("com.stefankrause.xplookandfeel.XPLookAndFeel");

			setGUIDetails();
			showMenus();
			showToolbar();
			showStatus();
			loadConfiguration(xmlFolder);

			setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadConfiguration(String xmlFolder) throws Exception {
		showTree(xmlFolder);
		showTable();
		repaint();
	}

	private void setGUIDetails() {
		setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		setLocation((dim.width - WINDOW_WIDTH) / 2, (dim.height - WINDOW_HEIGHT) / 2);
		getContentPane().setLayout(new BorderLayout());

		// TREE related details
		scrollPane.setPreferredSize(COMPONENTS_DIM);
		scrollPane.getViewport().add(m_tree);
		getContentPane().add(scrollPane, BorderLayout.WEST);
		m_tree.setRootVisible(false);
		m_tree.addMouseListener(new TreeMouseListener());

		// TABLE related staff
		scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getViewport().add(m_table);
		scrollPane.setPreferredSize(COMPONENTS_DIM);
		getContentPane().add(scrollPane, BorderLayout.EAST);
		m_table.addMouseListener(new TableMouseListener());
	}

	private void showMenus() {
		JMenuBar menus = new JMenuBar();
		JMenu loadMenu = new JMenu("Load");
		JMenu treeOperationsMenu = new JMenu("Tree Tools");
		JMenu lookAndFeelMenu = new JMenu("Look And Feel");
		JMenuItem loadData = new JMenuItem("Load Configuration ...");
		JMenuItem exit = new JMenuItem("Exit");
		JMenuItem expandAll = new JMenuItem("Expand Tree");
		JMenuItem collapseAll = new JMenuItem("Collapse Tree");
		JMenuItem windowsLNF = new JMenuItem("Windows");
		JMenuItem xpLNF = new JMenuItem("XP");

		loadData.addActionListener(new LoadDataAction());
		exit.addActionListener(new ExitAction());

		expandAll.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				// get tree's root
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) m_tree.getModel().getRoot();
				for (Enumeration enum1 = root.preorderEnumeration(); enum1.hasMoreElements();) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) enum1.nextElement();
					// if not is not defines a tree leaf - expand it
					if (!node.isLeaf()) {
						m_tree.expandPath(new TreePath(node.getPath()));
					}
				}
			}
		});

		collapseAll.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				// get tree's root
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) m_tree.getModel().getRoot();
				for (Enumeration enum1 = root.postorderEnumeration(); enum1.hasMoreElements();) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) enum1.nextElement();
					// if not is not defines a tree leaf - expand it
					if (!node.isLeaf() && !node.isRoot()) {
						m_tree.collapsePath(new TreePath(node.getPath()));
					}
				}
			}
		});

		windowsLNF.addActionListener(new WindowsLNFAction());
		windowsLNF.addActionListener(new XPLNFAction());

		loadMenu.add(loadData);
		loadMenu.add(exit);

		treeOperationsMenu.add(expandAll);
		treeOperationsMenu.add(collapseAll);

		// lookAndFeelMenu.add(windowsLNF);
		// lookAndFeelMenu.add(xpLNF);

		menus.add(loadMenu);
		menus.add(treeOperationsMenu);
		menus.add(lookAndFeelMenu);

		setJMenuBar(menus);
	}

	private void showTree(String xmlFolder) throws Exception {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
		XMLElement[] elements = loadXML(xmlFolder);
		int i, length = elements != null ? elements.length : 0;

		for (i = 0; i < length; i++)
			buildTreeModel(0, root, elements[i].getXMLElement());

		DefaultTreeModel model = new DefaultTreeModel(root);
		m_tree.setModel(model);

		// add mouse click adapter for the tree

	}

	private void showToolbar() {

		JToolBar menus = new JToolBar(JToolBar.VERTICAL);

		setButton(menus, MOVE_ALL_IMAGE, "Move All", new MoveAllListener());
		setButton(menus, MOVE_IMAGE, "Move", new MoveListener());
		setButton(menus, REMOVE_IMAGE, "Remove", new RemoveListener());
		setButton(menus, REMOVE_ALL_IMAGE, "Remove All", new RemoveAllListener());

		menus.setFloatable(false);
		menus.setPreferredSize(TOOLBAR_DIM);

		getContentPane().add(menus, BorderLayout.CENTER);
	}

	private void showTable() {
		DefaultTableModel model = new DefaultTableModel(0, 1);
		m_table.setModel(model);
	}

	private void showStatus() {
		JPanel panel = new JPanel(new GridLayout());
		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton locateButton = new JButton("Locate ...");
		JButton generateXMLButton = new JButton("Generate XML");
		JLabel label = new JLabel("XML file location : ");

		m_xmlFileName.setPreferredSize(new Dimension(100, 22));
		locateButton.addActionListener(new ResultXMLFileChooser());
		generateXMLButton.addActionListener(new GenerateXMLAction());

		leftPanel.add(label);
		leftPanel.add(m_xmlFileName);
		leftPanel.add(locateButton);
		rightPanel.add(generateXMLButton);

		panel.add(leftPanel);
		panel.add(rightPanel);

		getContentPane().add(panel, BorderLayout.SOUTH);
	}

	protected class ResultXMLFileChooser implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showOpenDialog(getThisComponent());

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String name = chooser.getSelectedFile().getAbsolutePath();
				m_xmlFileName.setText(name);
			}
		}
	}

	/**
	 * Set the button of the toolbar
	 * 
	 * @param btn
	 *            JButton
	 * @param gif
	 *            String
	 * @param tooltipText
	 *            String
	 */
	private void setButton(JToolBar toolBar, String gif, String tooltipText, ActionListener listener) {
		JButton btn = new JButton();

		btn.setIcon(ImageFactory.getInstance().getImage(gif, toolBar));
		btn.setToolTipText(tooltipText);
		btn.addActionListener(listener);
		btn.setPreferredSize(BUTTON_DIM);
		toolBar.add(btn);

	}

	/**
	 * @param xmlFolder
	 * @return array of xml elements of first hierarchy level
	 * @throws Exception
	 */
	private XMLElement[] loadXML(String xmlFolder) throws Exception {
		XMLElement[] elementsArray = null;
		ArrayList elements = new ArrayList();
		File folder = new File(xmlFolder);
		if (folder.isDirectory()) {
			String[] filenames = folder.list(new FileFilter());
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			int fileIndex, filesTotal = filenames != null ? filenames.length : 0;

			if (filesTotal > 0) {
				for (fileIndex = 0; fileIndex < filesTotal; fileIndex++) {
					Document doc = builder.parse(xmlFolder + File.separator + filenames[fileIndex]);
					NodeList nl = doc.getChildNodes();
					int nodesIndex, nodesCount = nl != null ? nl.getLength() : 0;
					for (nodesIndex = 0; nodesIndex < nodesCount; nodesIndex++) {
						Node node = nl.item(nodesIndex);

						if (isVisibleXMLElement(node)) {
							elements.add(new XMLElement((Element) node));
							break;
						}
					}
				}
			}

			int i, length = elements.size();
			if (length > 0) {
				elementsArray = new XMLElement[length];

				for (i = 0; i < length; i++)
					elementsArray[i] = (XMLElement) elements.get(i);
			}

		}

		return elementsArray;
	}

	private boolean isVisibleXMLElement(Node node) {
		String text = null;

		if (node != null && node.getFirstChild() != null)
			text = ((Element) node).getAttribute("name");

		boolean check = node != null && node.getNodeType() == Node.ELEMENT_NODE && text != null
				&& text.trim().length() > 0;
		return check;
	}

	public void buildTreeModel(int depth, DefaultMutableTreeNode root, Node elementToAdd) {
		NodeList childrens = elementToAdd != null ? elementToAdd.getChildNodes() : null;
		int i, length = childrens != null ? childrens.getLength() : 0;
		DefaultMutableTreeNode child;

		if (isVisibleXMLElement(elementToAdd)) {
			child = new DefaultMutableTreeNode(new XMLElement((Element) elementToAdd));
			root.add(child);

			if (depth < MAX_DEPTH) {
				for (i = 0; i < length; i++)
					buildTreeModel(depth + 1, child, childrens.item(i));
			}
		}

	}

	protected class MoveAllListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			DefaultTreeModel model = (DefaultTreeModel) m_tree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
			moveNode(root);
			m_table.revalidate();
			m_table.repaint();
		}
	}

	protected class TableMouseListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount() == 2)
				removeSelectedElements();
		}
	}

	protected class TreeMouseListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount() == 2) {
				// get selected element
				TreePath selPath = m_tree.getSelectionPath();
				if (selPath != null) {
					DefaultTreeModel treeModel = (DefaultTreeModel) m_tree.getModel();
					if (treeModel.isLeaf(selPath.getLastPathComponent())) {
						// if it's a leaf - process it
						moveSelectedElements();
						// }else{//if the clicked element is a node - open it
						// m_tree.expandPath( selPath );
					}

				}
			}
		}
	}

	protected class RemoveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			removeSelectedElements();
		}
	}

	private void removeSelectedElements() {
		DefaultTableModel model = (DefaultTableModel) m_table.getModel();
		int[] selectedRows = m_table.getSelectedRows();
		int i, length = selectedRows != null ? selectedRows.length : 0;

		for (i = length - 1; i >= 0; i--) {
			model.removeRow(selectedRows[i]);
		}
	}

	protected class RemoveAllListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			DefaultTableModel model = (DefaultTableModel) m_table.getModel();
			int i, length = model != null ? model.getRowCount() : 0;

			for (i = length - 1; i >= 0; i--) {
				model.removeRow(i);
			}
		}
	}

	protected class MoveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			moveSelectedElements();
		}
	}

	private void moveSelectedElements() {
		TreePath[] selected = m_tree.getSelectionPaths();
		int i, length = selected != null ? selected.length : 0;

		for (i = 0; i < length; i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) selected[i].getLastPathComponent();
			Object dataObject = node.getUserObject();
			if (dataObject instanceof XMLElement) {
				addSelectedElement((XMLElement) dataObject);
			}
		}
	}

	private void moveNode(DefaultMutableTreeNode node) {
		if (node == null)
			return;

		Object dataObject = node.getUserObject();

		if (dataObject instanceof XMLElement) {
			addSelectedElement((XMLElement) dataObject);
		}

		int i, length = node.getChildCount();

		for (i = 0; i < length; i++) {
			moveNode((DefaultMutableTreeNode) node.getChildAt(i));
		}
	}

	private boolean addSelectedElement(XMLElement element) {
		if (isElementSelected(element) == false) {
			DefaultTableModel model = (DefaultTableModel) m_table.getModel();
			model.addRow(new Object[] { element });
			return true;
		}
		return false;
	}

	private boolean isElementSelected(XMLElement element) {
		if (element == null)
			return false;

		DefaultTableModel model = (DefaultTableModel) m_table.getModel();
		int i, length = model != null ? model.getRowCount() : 0;
		XMLElement temp;

		for (i = 0; i < length; i++) {
			temp = (XMLElement) model.getValueAt(i, 0);
			if (element.equals(temp)) {
				return true;
			}
		}

		return false;
	}

	public static void main(String[] args) {
		String xmlFolder = "c:\\ws-test\\sca-GUI\\rules\\";

		if (args != null && args.length > 0) {
			xmlFolder = args[0];

		}

		new ScaMainUI(xmlFolder);
	}

	protected class ExitAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	protected class LoadDataAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = chooser.showOpenDialog(getThisComponent());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (file == null) {
					JOptionPane.showMessageDialog(getThisComponent(), "No file selected for loading");
					return;
				}

				try {
					loadConfiguration(file.getAbsolutePath());
				} catch (Exception exp) {
					JOptionPane.showMessageDialog(getThisComponent(), exp.getMessage());
				}
			}
		}
	}

	protected class GenerateXMLAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				String name = m_xmlFileName.getText();

				if (name.trim().length() == 0) {
					JOptionPane.showMessageDialog(getThisComponent(), "Result XML file not specified");
					return;
				}
				String xml = generateXML();
				RandomAccessFile file = new RandomAccessFile(name, "rw");
				file.write(xml.getBytes());
				file.close();
			} catch (Exception exp) {
				JOptionPane.showMessageDialog(getThisComponent(), exp.getMessage());
			}
		}
	}

	private String generateXML() throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.newDocument();
		Element root = document.createElement(ROOT_ELEMENT);

		DefaultTableModel model = (DefaultTableModel) m_table.getModel();
		int i, length = model != null ? model.getRowCount() : 0;
		Node temp;

		for (i = 0; i < length; i++) {
			temp = ((XMLElement) model.getValueAt(i, 0)).getXMLElement();
			temp = document.importNode(temp, true);
			root.appendChild(temp);
		}

		return root.toString();
	}

	protected class WindowsLNFAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
	}

	protected class XPLNFAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				System.setProperty("sun.java2d.ddscale", "true");
				UIManager.setLookAndFeel("com.stefankrause.xplookandfeel.XPLookAndFeel");
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
	}

	protected class XMLTable extends JTable {
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		public String getColumnName(int column) {
			return null;
		}
	}

	protected Component getThisComponent() {
		return this;
	}
}

class XMLElement {
	private Element xmlElement;
	int m_deep = 0;
	private int m_id;
	private static int m_idCounter = 0;

	private static final String NAME_ATTRIBUTE = "name";

	public XMLElement(Element element) {
		this(element, 0);
	}

	public XMLElement(Element element, int deep) {
		xmlElement = element;
		m_deep = deep;
		m_id = ++m_idCounter;
	}

	public String toString() {
		String text = xmlElement.getAttribute(NAME_ATTRIBUTE);
		/*
		 * Node parent = (Node)xmlElement.getParentNode(); String parentName =
		 * null;
		 * 
		 * if(parent instanceof Element) parentName =
		 * ((Element)parent).getAttribute(NAME_ATTRIBUTE);
		 * 
		 * if(parentName != null) { text = parentName + " - " + text; }
		 */
		return text;
	}

	public String getAbsoluteName() {
		String text = xmlElement.getAttribute(NAME_ATTRIBUTE);
		Node parent = (Node) xmlElement.getParentNode();
		String parentName = null;

		if (parent instanceof Element)
			parentName = ((Element) parent).getAttribute(NAME_ATTRIBUTE);

		if (parentName != null) {
			text = parentName + " - " + text;
		}

		return text;
	}

	public Element getXMLElement() {
		return xmlElement;
	}

	public int getId() {
		return m_id;
	}

	public boolean equals(Object element) {
		if (element instanceof XMLElement) {
			XMLElement other = (XMLElement) element;

			return m_id == other.m_id;
		}

		return false;
	}
}

class FileFilter implements FilenameFilter {
	private String XML = ".xml";

	public boolean accept(File dir, String name) {
		return name.endsWith(XML);
	}
}
