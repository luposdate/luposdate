/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
import java.awt.Image;
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
import lupos.misc.FileHelper;
import xpref.XPref;

public class VisualRifEditor extends JFrame {

	private static final long serialVersionUID = 136000538613610467L;

	private final VisualRifEditor that = this;

	private StatusBar statusBar = null;

	private TreePane treePane;

	private final JSplitPane splitPane;

	private DocumentContainer documentContainer;

	private RuleContainer ruleContainer;

	private SaveLoader saveLoader = new SaveLoader(this);

	public VisualRifEditor(){
		this(null, null);
	}


	/* Constructor */
	public VisualRifEditor(final String rules, final Image icon){
		super();

		try {
			final URL ressource = VisualRifEditor.class.getResource("/preferencesMenu.xml");
			XPref.getInstance(ressource);
		}
		catch(final Exception e) {
			try {
				XPref.getInstance(new URL("file:"+VisualRifEditor.class.getResource("/preferencesMenu.xml").getFile()));
			} catch(final Exception e1) {
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
		catch(final Exception e) {
			e.printStackTrace();
		}

		if(rules==null && icon==null){
			this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		}


		this.setJMenuBar(this.buildMenuBar());
		this.getContentPane().add(this.splitPane, BorderLayout.CENTER);
		this.getContentPane().add(this.statusBar, BorderLayout.SOUTH);
		this.setTitle("LUPOSDATE- Visual RIF");
		this.setSize(1000, 600);
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		if(rules != null){
			this.importNewDocument(rules);
		}

		if(icon!=null){
			this.setIconImage(icon);
		}
	}


	/**
	 * The MenuBar contains
	 * <li> FileMenu
	 * @see buildFileMenu
	 * @return MenuBar
	 */
	private JMenuBar buildMenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		menuBar.getSelectionModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent ce) {
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
		final JMenuItem newDocumentMI = new JMenuItem("New Document");
		newDocumentMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				final DocumentPanel newDocument = VisualRifEditor.this.that.documentContainer
						.createNewDocument();
				VisualRifEditor.this.that.treePane.addNewDocument(newDocument);
				VisualRifEditor.this.that.setRightComponent(newDocument);
				VisualRifEditor.this.that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().generateRif();
				VisualRifEditor.this.that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().evaluate();
				VisualRifEditor.this.that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().generateRif();
			}
		});

		final JMenuItem newFileMI = new JMenuItem("New File");
		newFileMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				VisualRifEditor.this.getDocumentContainer().setActiveDocument(null);
				VisualRifEditor.this.getDocumentContainer().getDocuments().clear();

				VisualRifEditor.this.getRuleContainer().getRulePanelList().clear();
				VisualRifEditor.this.getRuleContainer().setActiveRule(null);
				VisualRifEditor.this.getRuleContainer().getRules().clear();
				VisualRifEditor.this.setRightComponent(new JLabel());
				VisualRifEditor.this.getTreePane().clearTopComponent();
				final DocumentPanel newDocument = VisualRifEditor.this.that.documentContainer
						.createNewDocument();
				VisualRifEditor.this.that.treePane.addNewDocument(newDocument);
				VisualRifEditor.this.that.setRightComponent(newDocument);
				VisualRifEditor.this.that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().generateRif();
				VisualRifEditor.this.that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().evaluate();
				VisualRifEditor.this.that.getDocumentContainer().getActiveDocument().getDocumentEditorPane().generateRif();
			}
		});


		newFileMI.setAccelerator(
				  KeyStroke.getKeyStroke( 'N', InputEvent.CTRL_DOWN_MASK )
				);

		newDocumentMI.setAccelerator(
				  KeyStroke.getKeyStroke( 'D', InputEvent.CTRL_DOWN_MASK )
				);

		final JMenu submenu = new JMenu("New");
		submenu.add(newFileMI);
		submenu.add(newDocumentMI);

		// create JMenuItem to load...
		final JMenuItem loadMI = new JMenuItem("Open File");
		loadMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter("JSON Save files", "json"));

				if(chooser.showDialog(VisualRifEditor.this.that, "Open") == JFileChooser.APPROVE_OPTION) {
					final String fileName = chooser.getSelectedFile().getAbsolutePath();

					VisualRifEditor.this.that.saveLoader.load(fileName);
				}
			}
		});

		// create JMenuItem to save...
		final JMenuItem saveMI = new JMenuItem("Save File");
		saveMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {

				final SaveDialog chooser = new SaveDialog(System.getProperty("user.dir"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter("JSON Save files", "json"));

				if(chooser.showDialog(VisualRifEditor.this.that, "Save") == SaveDialog.APPROVE_OPTION) {
					String fileName = chooser.getSelectedFile().getAbsolutePath();

					if(!fileName.endsWith(".json")) {
						fileName += ".json";
					}

					VisualRifEditor.this.that.saveLoader.save(fileName);
				}
			}
		});


		final JMenuItem importMI = new JMenuItem("Import Document");
		importMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter("rif document", "txt","rif"));

				if(chooser.showDialog(VisualRifEditor.this.that, "Import") == JFileChooser.APPROVE_OPTION) {
					final String fileName = chooser.getSelectedFile().getAbsolutePath();

					VisualRifEditor.this.that.importNewDocument(FileHelper.fastReadFile(fileName));
				}
			}
		});

		final JMenuItem exportMI = new JMenuItem("Export Document");
		exportMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {

				final SaveDialog chooser = new SaveDialog(System.getProperty("user.dir"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter("rif document", "txt","rif"));

				if(chooser.showDialog(VisualRifEditor.this.that, "Export") == SaveDialog.APPROVE_OPTION) {
					String fileName = chooser.getSelectedFile().getAbsolutePath();

					if(!fileName.endsWith(".rif")) {
						fileName += ".rif";
					}

					VisualRifEditor.this.that.saveLoader.export(fileName);
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
		final JMenuItem endMI = new JMenuItem("Exit");
		endMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				VisualRifEditor.this.that.setVisible(false);
			}
		});

		// create File Menu and add components to it...
		final JMenu fileMenu = new JMenu("File");
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

	public void importNewDocument(final String rules){
		final DocumentPanel newDocument = this.documentContainer.createNewDocument();
		this.treePane.addNewDocument(newDocument);
		this.setRightComponent(newDocument);
		this.importDocument(rules);
	}


	public void importDocument(final String rules){
		this.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getRifCodeEditor().getTp_rifInput().setText(rules);
		this.getDocumentContainer().getActiveDocument().getDocumentEditorPane().evaluate();
	}

	/**
	 * Loads the component on the right side
	 *
	 * @param component
	 */
	public void setRightComponent(final JComponent component) {
		final int dividerLocation = this.splitPane.getDividerLocation();

		this.splitPane.setRightComponent(component);
		this.splitPane.setDividerLocation(dividerLocation);
	}

	public void enableMenus(final boolean state) {
		System.out.println("VisualrifEditor.enableMenus():");
	}


	/* *************** **
	 * Getter + Setter **
	 * *************** */

	public void setDocumentContainer(final DocumentContainer documentContainer){
		this.documentContainer = documentContainer;
	}

	public DocumentContainer getDocumentContainer(){
		return this.documentContainer;
	}

	public void setRuleContainer(final RuleContainer ruleContainer){
		this.ruleContainer = ruleContainer;
	}

	public RuleContainer getRuleContainer(){
		return this.ruleContainer;
	}

	public void setStatusBar(final StatusBar statusBar){
		this.statusBar = statusBar;
	}

	public StatusBar getStatusBar(){
		return this.statusBar;
	}

	public void setTreePane(final TreePane treePane){
		this.treePane = treePane;
	}

	public TreePane getTreePane(){
		return this.treePane;
	}

	public SaveLoader getSaveLoader() {
		return this.saveLoader;
	}

	public void setSaveLoader(final SaveLoader saveLoader) {
		this.saveLoader = saveLoader;
	}

	// Start
	public static void main(final String[] args){
		new VisualRifEditor();
	}
}
