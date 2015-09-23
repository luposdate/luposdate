/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.gui.operatorgraph.visualeditor.ruleeditor;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import lupos.gui.anotherSyntaxHighlighting.LANGUAGE;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.generators.DocumentationGenerator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.generators.JavaCodeGenerator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.DocumentationFrame;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.JavaCodeFrame;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.RulePackagePanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.RulePanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.TreePane;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.AssociationsContainer;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleContainer;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RulePackageContainer;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.SaveLoader;
import lupos.gui.operatorgraph.visualeditor.util.SaveDialog;
import lupos.gui.operatorgraph.visualeditor.util.StatusBar;
import xpref.XPref;
public class RuleEditor extends JFrame {
	private static final long serialVersionUID = -1212916381422834360L;
	private RuleEditor that = this;
	private boolean standAlone = true;
	private JSplitPane splitPane = null;
	private StatusBar statusBar = null;
	private RuleContainer ruleContainer = null;
	private RulePackageContainer rulePackageContainer = null;
	private AssociationsContainer associationsContainer = null;
	private TreePane treePane = null;
	private JMenu editRuleMenu = null;
	private JMenu generationMenu = null;
	private DocumentationFrame docFrame = new DocumentationFrame(this);
	private DocumentationGenerator docGen = new DocumentationGenerator(this);
	private JavaCodeFrame javaCodeFrame = new JavaCodeFrame(this);
	private JavaCodeGenerator javaCodeGen = new JavaCodeGenerator(this);
	private SaveLoader saveLoader = new SaveLoader(this);
	
	/** Constant <code>PATH_RULEFILES="src/main/resources/"</code> */
	protected static final String PATH_RULEFILES = "src/main/resources/";

	/**
	 * <p>Constructor for RuleEditor.</p>
	 */
	public RuleEditor() {
		super();

		try {
			XPref.getInstance(RuleEditor.class.getResource("/preferencesMenu.xml"));						
		} catch(Exception e) {
			System.err.println(e);
			e.printStackTrace();
			System.out.println("Try to load from file...");
			try{
				XPref.getInstance(new URL("file:"+RuleEditor.class.getResource("/preferencesMenu.xml").getFile()));
			} catch(Exception e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
		}

		LANGUAGE.HTML.setDefaultStyles(new Font("Courier New", Font.PLAIN, 12));
		LANGUAGE.JAVA.setDefaultStyles(new Font("Courier New", Font.PLAIN, 12));

		this.statusBar = new StatusBar();
		this.ruleContainer = new RuleContainer(this);
		this.rulePackageContainer = new RulePackageContainer(this);
		this.associationsContainer = new AssociationsContainer(this);

		this.treePane = new TreePane(this, this.ruleContainer, this.rulePackageContainer);

		this.splitPane = new JSplitPane();
		this.splitPane.setContinuousLayout(true);
		this.splitPane.setOneTouchExpandable(true);
		this.splitPane.setLeftComponent(this.treePane);
		this.splitPane.setRightComponent(new JPanel());
		this.splitPane.setDividerLocation(160);

		this.setLayout(new BorderLayout());
		this.add(this.splitPane, BorderLayout.CENTER);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		this.setIconImage(new ImageIcon(RuleEditor.class.getResource("/logo.gif")).getImage());
		this.setJMenuBar(this.buildMenuBar());
		this.getContentPane().add(this.splitPane, BorderLayout.CENTER);
		this.getContentPane().add(this.statusBar, BorderLayout.SOUTH);
		this.setTitle("LUPOSDATE-RuleEditor");
		this.setSize(1000, 600);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	/**
	 * <p>buildMenuBar.</p>
	 *
	 * @return a {@link javax.swing.JMenuBar} object.
	 */
	public JMenuBar buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.getSelectionModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				that.ruleContainer.cancelModi();
			}
		});
		menuBar.add(this.buildFileMenu());
		menuBar.add(this.buildRuleEditMenu());
		menuBar.add(this.buildGenerationMenu());

		return menuBar; // return the MenuBar
	}

	/**
	 * <p>setRightComponent.</p>
	 *
	 * @param comp a {@link javax.swing.JComponent} object.
	 */
	public void setRightComponent(final JComponent comp) {
		int dividerLocation = this.splitPane.getDividerLocation();

		this.splitPane.setRightComponent(comp);
		this.splitPane.setDividerLocation(dividerLocation);
	}

	private JMenu buildFileMenu() {
		// create JMenuITem to add new Rule...
		JMenuItem newRuleMI = new JMenuItem("New Rule");
		newRuleMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				RulePanel newRule = that.ruleContainer.createNewRule();
				that.treePane.addNewRule(newRule);
				that.setRightComponent(newRule);
				that.editRuleMenu.setEnabled(true);
				that.generationMenu.setEnabled(true);
			}
		});

		// create JMenuITem to add new RulePackage...
		JMenuItem newRulePackageMI = new JMenuItem("New Rule Package");
		newRulePackageMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				RulePackagePanel newRulePackage = that.rulePackageContainer.createNewRulePackage();
				that.treePane.addNewRulePackage(newRulePackage);
				that.setRightComponent(newRulePackage);
				that.editRuleMenu.setEnabled(true);
				that.generationMenu.setEnabled(true);
			}
		});

		// create JMenuItem to load...
		JMenuItem loadMI = new JMenuItem("Load");
		loadMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				JFileChooser chooser = new JFileChooser(PATH_RULEFILES);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter("JSON Save files", "json"));

				if(chooser.showDialog(that, "Load") == JFileChooser.APPROVE_OPTION) {
					String fileName = chooser.getSelectedFile().getAbsolutePath();

					that.saveLoader.load(fileName);
				}
			}
		});

		// create JMenuItem to save...
		JMenuItem saveMI = new JMenuItem("Save");
		saveMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				SaveDialog chooser = new SaveDialog(PATH_RULEFILES);
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

		// create JMenuItem to end the program...
		JMenuItem endMI = new JMenuItem("End program");
		endMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if(that.standAlone) {
					System.exit(0);
				}
				else {
					that.setVisible(false);
				}
			}
		});

		// create File Menu and add components to it...
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(newRuleMI);
		fileMenu.add(newRulePackageMI);
		fileMenu.addSeparator();
		fileMenu.add(loadMI);
		fileMenu.add(saveMI);
		fileMenu.addSeparator();
		fileMenu.add(endMI);

		return fileMenu;
	}

	private JMenu buildRuleEditMenu() {
		JMenuItem renameElementMI = new JMenuItem("Rename Rule / Rule Package");
		renameElementMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				that.treePane.renameElement();
			}
		});

		JMenuItem deleteElementMI = new JMenuItem("Delete Rule / Rule Package");
		deleteElementMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				that.treePane.deleteElement();
			}
		});

		// create Edit Menu and add components to it...
		this.editRuleMenu = new JMenu("Edit");
		this.editRuleMenu.setEnabled(false);
		this.editRuleMenu.add(renameElementMI);
		this.editRuleMenu.add(deleteElementMI);

		return this.editRuleMenu;
	}

	private JMenu buildGenerationMenu() {
		JMenuItem generateDocumentationMI = new JMenuItem("Generate Documentation");
		generateDocumentationMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				that.docFrame.setVisible(true);
			}
		});

		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		JMenuItem generateJavaCodeMI = new JMenuItem("Generate Java Code");
		generateJavaCodeMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				that.javaCodeFrame.setVisible(true);
			}
		});

		// create Generate Menu and add components to it...
		this.generationMenu = new JMenu("Generate");
		this.generationMenu.setEnabled(false);
		this.generationMenu.add(generateDocumentationMI);
		this.generationMenu.add(generateJavaCodeMI);

		return this.generationMenu;
	}

	/**
	 * <p>getRuleEditMenu.</p>
	 *
	 * @return a {@link javax.swing.JMenu} object.
	 */
	public JMenu getRuleEditMenu() {
		return this.editRuleMenu;
	}

	/**
	 * <p>Getter for the field <code>generationMenu</code>.</p>
	 *
	 * @return a {@link javax.swing.JMenu} object.
	 */
	public JMenu getGenerationMenu() {
		return this.generationMenu;
	}

	/**
	 * <p>Getter for the field <code>treePane</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.TreePane} object.
	 */
	public TreePane getTreePane() {
		return this.treePane;
	}

	/**
	 * <p>Getter for the field <code>statusBar</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.util.StatusBar} object.
	 */
	public StatusBar getStatusBar() {
		return this.statusBar;
	}

	/**
	 * <p>Getter for the field <code>associationsContainer</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.util.AssociationsContainer} object.
	 */
	public AssociationsContainer getAssociationsContainer() {
		return this.associationsContainer;
	}

	/**
	 * <p>getRulePackages.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<RulePackagePanel> getRulePackages() {
		return this.rulePackageContainer.getRulePackages();
	}

	/**
	 * <p>getRules.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<RulePanel> getRules() {
		return this.ruleContainer.getRules();
	}

	/**
	 * <p>Getter for the field <code>ruleContainer</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleContainer} object.
	 */
	public RuleContainer getRuleContainer() {
		return this.ruleContainer;
	}

	/**
	 * <p>Getter for the field <code>rulePackageContainer</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RulePackageContainer} object.
	 */
	public RulePackageContainer getRulePackageContainer() {
		return this.rulePackageContainer;
	}

	/**
	 * <p>getDocumentationGenerator.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.generators.DocumentationGenerator} object.
	 */
	public DocumentationGenerator getDocumentationGenerator() {
		return this.docGen;
	}

	/**
	 * <p>getJavaCodeGenerator.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.generators.JavaCodeGenerator} object.
	 */
	public JavaCodeGenerator getJavaCodeGenerator() {
		return this.javaCodeGen;
	}

	/**
	 * <p>enableMenus.</p>
	 *
	 * @param state a boolean.
	 */
	public void enableMenus(boolean state) {
		this.editRuleMenu.setEnabled(state);
		this.generationMenu.setEnabled(state);
	}


	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] args) {
		new RuleEditor();
	}
}
