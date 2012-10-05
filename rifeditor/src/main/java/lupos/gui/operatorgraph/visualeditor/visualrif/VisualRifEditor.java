/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.gui.operatorgraph.visualeditor.visualrif;



import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import lupos.gui.operatorgraph.visualeditor.util.SaveDialog;
import lupos.gui.operatorgraph.visualeditor.util.StatusBar;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.DocumentPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.TreePane;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.DocumentContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.RuleContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.SaveLoader;
import xpref.XPref;




public class VisualRifEditor extends JFrame {

	private static final long serialVersionUID = 136000538613610467L;

	private VisualRifEditor that = this;
	
	private StatusBar statusBar = null;

	private TreePane treePane;

	private JSplitPane splitPane;

	private DocumentContainer documentContainer;
	
	private RuleContainer ruleContainer;
	
	private SaveLoader saveLoader = new SaveLoader(this);

	
	
	/* Constructor */




	VisualRifEditor(){
		super();
		
		try {
			URL ressource = VisualRifEditor.class.getResource("/preferencesMenu.xml");
			XPref.getInstance(ressource);				
		}
		catch(Exception e) {
			try {
				XPref.getInstance(VisualRifEditor.class.getResource("/preferencesMenu.xml").getFile());
			} catch(Exception e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
		}
		
		
		
		this.statusBar = new StatusBar();
		
		this.documentContainer = new DocumentContainer(this);
		this.ruleContainer = new RuleContainer(this);

		
		this.treePane = new TreePane(this, this.documentContainer);
		
		
		this.splitPane = new JSplitPane();
		this.splitPane.setContinuousLayout(true);
		this.splitPane.setOneTouchExpandable(true);
		this.splitPane.setDividerLocation(170); // 160
		this.splitPane.setLeftComponent(this.treePane);
		this.splitPane.setRightComponent(new JPanel());
		
		
		this.setLayout(new BorderLayout());
		this.add(this.splitPane, BorderLayout.CENTER);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		
		this.setJMenuBar(this.buildMenuBar());
		this.getContentPane().add(this.splitPane, BorderLayout.CENTER);
		this.getContentPane().add(this.statusBar, BorderLayout.SOUTH);
		this.setTitle("LUPOSDATE- Visual RIF");
		this.setSize(1000, 600);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
		
	}


	/**
	 * The MenuBar contains
	 * <li> FileMenu
	 * @see buildFileMenu
	 * @return MenuBar
	 */
	private JMenuBar buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.getSelectionModel().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
//				that.documentContainer.cancelModi();
				
			}
		});

		menuBar.add(this.buildFileMenu());

		
		return menuBar;
	}//End Constructor
		
	
	/**
	 * The FileMenu contains 
	 * <li> New Document
	 * <li> Save
	 * <li> Load
	 * <li> Exit
	 * @return FileMenu
	 */
	private JMenu buildFileMenu() {
		
		// create JMenuITem to add new Document...
		JMenuItem newDocumentMI = new JMenuItem("New Document");
		newDocumentMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				DocumentPanel newDocument = that.documentContainer
						.createNewDocument();
				that.treePane.addNewDocument(newDocument);
				that.setRightComponent(newDocument);
				that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().generateRif();
				that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().evaluate();
				that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().generateRif();
			}
		});
		
		JMenuItem newFileMI = new JMenuItem("New File");
		newFileMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				getDocumentContainer().setActiveDocument(null);
				getDocumentContainer().getDocuments().clear();

				getRuleContainer().getRulePanelList().clear();
				getRuleContainer().setActiveRule(null);
				getRuleContainer().getRules().clear();
				setRightComponent(new JLabel());
				getTreePane().clearTopComponent();
				DocumentPanel newDocument = that.documentContainer
						.createNewDocument();
				that.treePane.addNewDocument(newDocument);
				that.setRightComponent(newDocument);
				that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().generateRif();
				that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().evaluate();
				that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().generateRif();
			}
		});
		
		
		newFileMI.setAccelerator(
				  KeyStroke.getKeyStroke( 'N', InputEvent.CTRL_DOWN_MASK )
				);
		
		newDocumentMI.setAccelerator(
				  KeyStroke.getKeyStroke( 'D', InputEvent.CTRL_DOWN_MASK )
				);
		
		JMenu submenu = new JMenu("New");
		submenu.add(newFileMI);
		submenu.add(newDocumentMI);
		
		// create JMenuItem to load...
		JMenuItem loadMI = new JMenuItem("Open File");
		loadMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter("JSON Save files", "json"));

				if(chooser.showDialog(that, "Open") == JFileChooser.APPROVE_OPTION) {
					String fileName = chooser.getSelectedFile().getAbsolutePath();

					that.saveLoader.load(fileName);
				}
			}
		});

		// create JMenuItem to save...
		JMenuItem saveMI = new JMenuItem("Save File");
		saveMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				
				SaveDialog chooser = new SaveDialog(System.getProperty("user.dir"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter("JSON Save files", "json"));

				if(chooser.showDialog(that, "Save") == SaveDialog.APPROVE_OPTION) {
					String fileName = chooser.getSelectedFile().getAbsolutePath();

					if(!fileName.endsWith(".json")) {
						fileName += ".json";
					}

					that.saveLoader.save(fileName);
				}
			}
		});
		

		JMenuItem importMI = new JMenuItem("Import Document");
		importMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				DocumentPanel newDocument = that.documentContainer
						.createNewDocument();
				that.treePane.addNewDocument(newDocument);
				that.setRightComponent(newDocument);
				JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter("rif document", "txt","rif"));

				if(chooser.showDialog(that, "Import") == JFileChooser.APPROVE_OPTION) {
					String fileName = chooser.getSelectedFile().getAbsolutePath();

					that.saveLoader.importFile(fileName);
					that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().evaluate();

				}
				
			}
		});

		JMenuItem exportMI = new JMenuItem("Export Document");
		exportMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			
				SaveDialog chooser = new SaveDialog(System.getProperty("user.dir"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter("rif document", "txt","rif"));

				if(chooser.showDialog(that, "Export") == SaveDialog.APPROVE_OPTION) {
					String fileName = chooser.getSelectedFile().getAbsolutePath();

					if(!fileName.endsWith(".rif")) {
						fileName += ".rif";
					}

					that.saveLoader.export(fileName);
				}
			}
		});
		
		importMI.setAccelerator(
				  KeyStroke.getKeyStroke( 'I', InputEvent.CTRL_DOWN_MASK )
				);
		exportMI.setAccelerator(
				  KeyStroke.getKeyStroke( 'E', InputEvent.CTRL_DOWN_MASK )
				);
		loadMI.setAccelerator(
				  KeyStroke.getKeyStroke( 'O', InputEvent.CTRL_DOWN_MASK )
				);
		saveMI.setAccelerator(
				  KeyStroke.getKeyStroke( 'S', InputEvent.CTRL_DOWN_MASK )
				);
		
		// create JMenuItem to end the program...
		JMenuItem endMI = new JMenuItem("Exit");
		endMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				System.out.println("VisualRifEditor.buildFileMenu(): endMI"); //TODO
				that.setVisible(false);


			}
		});

		// create File Menu and add components to it...
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		endMI.setMnemonic('e');
		submenu.setMnemonic('n');
		fileMenu.add(submenu);
		fileMenu.addSeparator();

		fileMenu.add(loadMI);
		fileMenu.add(saveMI);
		fileMenu.addSeparator();
		fileMenu.add(importMI);
		fileMenu.add(exportMI);
		fileMenu.addSeparator();
		fileMenu.add(endMI);

		return fileMenu;
	}


	/**
	 * Loads the component on the right side
	 * 
	 * @param component
	 */
	public void setRightComponent(final JComponent component) {
		int dividerLocation = this.splitPane.getDividerLocation();

		this.splitPane.setRightComponent(component);
		this.splitPane.setDividerLocation(dividerLocation);
	}
	
	public void enableMenus(boolean state) {
		System.out.println("VisualrifEditor.enableMenus(): TODO"); // TODO
	}
	
	
	/* *************** **
	 * Getter + Setter **
	 * *************** */
	
	public void setDocumentContainer(DocumentContainer documentContainer){
		this.documentContainer = documentContainer;
	}
	
	public DocumentContainer getDocumentContainer(){
		return this.documentContainer;
	}
		
	public void setRuleContainer(RuleContainer ruleContainer){
		this.ruleContainer = ruleContainer;
	}
	
	public RuleContainer getRuleContainer(){
		return this.ruleContainer;
	}
	
	public void setStatusBar(StatusBar statusBar){
		this.statusBar = statusBar;
	}
	
	public StatusBar getStatusBar(){
		return this.statusBar;
	}
	
	public void setTreePane(TreePane treePane){
		this.treePane = treePane;
	}
	
	public TreePane getTreePane(){
		return this.treePane;
	}
	
	public SaveLoader getSaveLoader() {
		return saveLoader;
	}


	public void setSaveLoader(SaveLoader saveLoader) {
		this.saveLoader = saveLoader;
	}
	
	
	
	// Start 
	public static void main(String[] args){
		new VisualRifEditor();
	}





	
	
}
