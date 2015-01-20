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
package lupos.gui.operatorgraph.visualeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lupos.datastructures.items.Item;
import lupos.gui.anotherSyntaxHighlighting.ILuposParser;
import lupos.gui.anotherSyntaxHighlighting.LinePainter;
import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;
import lupos.gui.anotherSyntaxHighlighting.javacc.SPARQLParser;
import lupos.gui.anotherSyntaxHighlighting.javacc.TurtleParser;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperEditable;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.RDFTerm;
import lupos.gui.operatorgraph.visualeditor.util.ConfirmationDialog;
import lupos.gui.operatorgraph.visualeditor.util.Connection;
import lupos.misc.util.OperatorIDTuple;
import lupos.gui.operatorgraph.visualeditor.util.StatusBar;
import lupos.gui.operatorgraph.visualeditor.util.TopToolbar;

import org.json.JSONObject;

public abstract class VisualEditor<T> extends JPanel implements ClipboardOwner {
	private static final long serialVersionUID = 254637585333969890L;
	protected LinkedList<T> selectedOperatorsList = new LinkedList<T>();
	protected Hashtable<T, HashSet<T>> selectedAnnotationList = new Hashtable<T, HashSet<T>>();
	protected Class<? extends T> insertOperator;
	public Connection<T> connectionMode;
	public boolean isInInsertMode = false;
	protected LinkedList<VisualGraph<T>> visualGraphs = new LinkedList<VisualGraph<T>>();
	protected LinkedList<JMenuItem> jGraphMenus = new LinkedList<JMenuItem>();
	protected JMenuItem copyMI = new JMenuItem();
	protected JMenuItem pasteMI;
	protected JMenuItem deleteElementsMI = new JMenuItem();
	protected JMenuItem deleteOpsAndTreesMI = new JMenuItem();
	protected VisualEditor<T> myself;
	private static Clipboard clipboard;
	protected StatusBar statusBar = new StatusBar();
	protected boolean standAlone;
	protected TopToolbar<T> topToolbar = null;

	protected VisualEditor(final boolean standAlone) {
		super();

		this.standAlone = standAlone;
		this.myself = this;

		synchronized (this.myself) {
			if(VisualEditor.clipboard == null) {
				try {
					VisualEditor.clipboard = this.getToolkit().getSystemClipboard();
				}
				catch(final Exception e) {
					VisualEditor.clipboard = new Clipboard("clipboard");
				}
			}
		}
	}

	protected JFrame createMainWindowSingleGraph(final String title, final boolean showTopToolBar, final Image image) {
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(this.visualGraphs.get(0)), BorderLayout.CENTER);

		final VisualEditorFrame<T> frame = new VisualEditorFrame<T>(this, title, image, this.standAlone);

		if(showTopToolBar) {
			frame.getContentPane().add(this.createTopToolBar(), BorderLayout.NORTH);
//			JPanel innerPanel=new JPanel(new BorderLayout());
//			Box box=Box.createVerticalBox();
//			JPanel toolBar=this.createTopToolBar();
//			toolBar.setAlignmentX(LEFT_ALIGNMENT);
//			box.add(toolBar);
//			box.add(Box.createVerticalGlue());
//			this.setAlignmentX(LEFT_ALIGNMENT);
//			box.add(this);
//			
//			innerPanel.add(box, BorderLayout.CENTER);
//			//this.getStatusBar();
//			frame.setContentPane(innerPanel);
		}

		return frame;
	}

	public JPanel createTopToolBar() {
		return this.createTopToolBar(null);
	}

	public JPanel createTopToolBar(final JSONObject loadObject) {
		this.topToolbar = new TopToolbar<T>(this, this.standAlone, loadObject);

		return this.topToolbar;
	}

	public Class<? extends T> getInsertOperator() {
		return this.insertOperator;
	}

	public void prepareOperatorForAdd(final Class<? extends T> clazz) {
		this.isInInsertMode = true;

		this.insertOperator = clazz;

		this.statusBar
		.setText("InsertMode: Click somewhere on the canvas to insert the selected operator.");
	}

	public synchronized void setOperatorForDeletion(final T op) {
		this.selectedOperatorsList.add(op);

		this.manageMenuItems();
	}

	public synchronized void unsetOperatorForDeletion(final T op) {
		this.selectedOperatorsList.remove(op);

		this.manageMenuItems();
	}

	public synchronized void clearDeletionOperatorsList() {
		this.selectedOperatorsList.clear();

		this.manageMenuItems();
	}

	public LinkedList<T> getDeletionOperatorsList() {
		return this.selectedOperatorsList;
	}

	public synchronized void setAnnotationForDeletion(final T op, final T child) {
		if (!this.selectedAnnotationList.containsKey(op)) {
			this.selectedAnnotationList.put(op, new HashSet<T>());
		}

		this.selectedAnnotationList.get(op).add(child);

		this.manageMenuItems();
	}

	public synchronized void unsetAnnotationForDeletion(final T op) {
		this.selectedAnnotationList.remove(op);

		this.manageMenuItems();
	}

	public synchronized void clearDeletionAnnotationList() {
		this.selectedAnnotationList.clear();

		this.manageMenuItems();
	}

	public Hashtable<T, HashSet<T>> getDeletionAnnotationList() {
		return this.selectedAnnotationList;
	}

	public void activateGraphMenus() {
		for (final JMenuItem jm : this.jGraphMenus) {
			jm.setEnabled(true);
		}
	}

	protected void deactivateGraphMenu() {
		for (final JMenuItem jm : this.jGraphMenus) {
			jm.setEnabled(false);
		}
	}

	public JMenu buildEditMenu() {
		this.copyMI = new JMenuItem("Copy");
		this.copyMI.setEnabled(false);
		this.copyMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				copyElements();
			}
		});

		this.pasteMI = new JMenuItem("Paste");
		this.pasteMI.setEnabled(true);
		this.pasteMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				final Transferable clipboardContent = clipboard
				.getContents(this);

				if (clipboardContent != null
						&& clipboardContent
						.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					try {
						final String tempString = (String) clipboardContent
						.getTransferData(DataFlavor.stringFlavor);

						pasteElements(tempString);
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		// create JMenuItem to delete the selected Operators...
		this.deleteElementsMI = new JMenuItem("Delete selected element(s)");
		this.deleteElementsMI.setEnabled(false);
		this.deleteElementsMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				final int returnValue = JOptionPane
				.showOptionDialog(
						VisualEditor.this,
						"Do you really want to delete the selected element(s)?",
						"Delete selected element(s)",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE, null,
						new String[] { "YES", "NO" }, 0);

				if (returnValue == 0) {
					deleteOperators(false);
					deleteAnnotations();
				}
			}
		});

		// create JMenuItem to delete the selected Operators and their
		// subtrees...
		this.deleteOpsAndTreesMI = new JMenuItem(
		"Delete selected element(s) with subtree(s)");
		this.deleteOpsAndTreesMI.setEnabled(false);
		this.deleteOpsAndTreesMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				final int returnValue = JOptionPane
				.showOptionDialog(
						VisualEditor.this,
						"Do you really want to delete the selected element(s) and subtree(s)?",
						"Delete selected element(s) and subtree(s)",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE, null,
						new String[] { "YES", "NO" }, 0);

				if (returnValue == 0) {
					deleteOperators(true);
				}
			}
		});

		// create Edit menu and add components to it...
		final JMenu editMenu = new JMenu("Edit");
		editMenu.add(this.copyMI);
		editMenu.add(this.pasteMI);
		editMenu.add(this.deleteElementsMI);
		editMenu.add(this.deleteOpsAndTreesMI);

		this.jGraphMenus.add(editMenu);

		return editMenu;
	}

	protected void deleteOperators(final boolean subtree) {
		for (final T op : this.selectedOperatorsList) {
			for (final VisualGraph<T> visualGraph : this.visualGraphs) {
				visualGraph.createGraphWrapper(op).delete(subtree);
			}
		}

		this.clearDeletionOperatorsList();
	}

	protected void deleteAnnotations() {
		for(final T op : this.selectedAnnotationList.keySet()) {
			for(final T child : this.selectedAnnotationList.get(op)) {
				for(final VisualGraph<T> visualGraph : this.visualGraphs) {
					visualGraph.createGraphWrapper(op).deleteAnnotation(visualGraph.createGraphWrapper(child));
				}
			}
		}

		this.clearDeletionAnnotationList();
	}

	protected void manageMenuItems() {
		if (this.selectedOperatorsList.size() > 0
				|| this.selectedAnnotationList.size() > 0) {
			this.deleteElementsMI.setEnabled(true);
		}

		if (this.selectedOperatorsList.size() > 0) {
			this.copyMI.setEnabled(true);
			this.deleteOpsAndTreesMI.setEnabled(true);
		}

		if (this.selectedOperatorsList.size() == 0
				&& this.selectedAnnotationList.size() == 0) {
			this.deleteElementsMI.setEnabled(false);
		}

		if (this.selectedOperatorsList.size() == 0) {
			this.copyMI.setEnabled(false);
			this.deleteOpsAndTreesMI.setEnabled(false);
		}
	}

	public void createShowTextFrame(final String title, final String text, Class<?> parserGenerator) {
		final JPanel panel = new JPanel();
		final JFrame frame = this.createSmallFrame(panel, title);

		final LuposDocument document = new LuposDocument();
		final JTextPane tp = new LuposJTextPane(document);
	    Method method;
		try {
			method = parserGenerator.getMethod("createILuposParser", LuposDocumentReader.class);
		    Object createdParser = method.invoke(null, new LuposDocumentReader(document));
			document.init((ILuposParser) createdParser, true);
		} catch (SecurityException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		
		tp.addKeyListener(this.getKeyListener(frame));
		tp.setFont(new Font("Courier New", Font.PLAIN, 12));
		tp.setEditable(false);
		tp.setText(text);

		new LinePainter(tp, new Color(202, 223, 245));

		final JScrollPane scroll = new JScrollPane(tp);

		// create OK button, which starts query evaluation...
		final JButton bt_ok = new JButton("OK");
		bt_ok.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				frame.setVisible(false); // hide query input frame
			}
		});

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(bt_ok);

		// create main panel and add components to it...
		panel.setLayout(new BorderLayout());
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		frame.setVisible(true);
	}

	protected int createShowTextDialog(final String title, final String text,
			final String confirmationText, final boolean query_or_n3) {
		final JPanel panel = new JPanel();

		final ConfirmationDialog dialog = new ConfirmationDialog(null, panel, title, confirmationText);
		dialog.addKeyListener(this.getKeyListener(dialog));

		final LuposDocument document = new LuposDocument();
		final JTextPane tp = new LuposJTextPane(document);
		document.init((query_or_n3) ? SPARQLParser.createILuposParser(new LuposDocumentReader(document)):TurtleParser.createILuposParser(new LuposDocumentReader(document)), false);

		tp.addKeyListener(this.getKeyListener(dialog));
		tp.setFont(new Font("Courier New", Font.PLAIN, 12));
		tp.setEditable(false);
		tp.setText(text);

		new LinePainter(tp, new Color(202, 223, 245));

		final JScrollPane scroll = new JScrollPane(tp);

		// create main panel and add components to it...
		panel.setLayout(new BorderLayout());
		panel.add(scroll, BorderLayout.CENTER);

		dialog.setVisible(true);

		return dialog.getReturnValue();
	}

	public JFrame createSmallFrame(final JPanel panel, final String title) {
		final JFrame frame = new JFrame(title);
		frame.addKeyListener(this.getKeyListener(frame));
		frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setSize(794, 200);
		frame.setLocationRelativeTo(this);

		return frame;
	}

	public KeyListener getKeyListener(final Window window) {
		return new KeyListener() {
			public void keyPressed(final KeyEvent ke) {
				if(ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
					window.setVisible(false);
				}
			}

			public void keyReleased(final KeyEvent ke) {}
			public void keyTyped(final KeyEvent ke) {}
		};
	}

	public void lostOwnership(final Clipboard clipboard,
			final Transferable transferable) {
		final Transferable clipboardContent = VisualEditor.clipboard
		.getContents(this);

		if (clipboardContent != null
				&& clipboardContent
				.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			// clip board has content
		}
	}

	protected void copyElements() {
		StringBuffer copiedText = new StringBuffer();

		final HashSet<T> copiedList = new HashSet<T>();
		copiedList.addAll(this.selectedOperatorsList);

		// walk through selected operators...
		for (final T op : this.selectedOperatorsList) {
			// if selected operator is RDFTerm and more than one operator was
			// selected...
			if (op instanceof RDFTerm && this.selectedOperatorsList.size() > 1) {
				final RDFTerm rdf1 = (RDFTerm) op;

				// walk through all selected operators to find children of the
				// selected RDFTerm...
				for (final T op2 : this.selectedOperatorsList) {
					// if a child of the selected RDFTerm is selected, too...
					if (rdf1.getSucceedingOperators().contains(
							new OperatorIDTuple<T>(op2, -1))) {
						final RDFTerm rdf2 = (RDFTerm) op2;

						final HashSet<T> annotationsRDF1 = this.selectedAnnotationList
						.get(rdf1);

						// if the predicates was also selected...
						if (annotationsRDF1 != null
								&& annotationsRDF1.contains(rdf2)) {
							copiedList.remove(rdf1);
							copiedList.remove(rdf2);

							// get predicates between these two RDFTerms...
							final LinkedList<Item> predicates = rdf1
							.getPredicates(rdf2);

							for (final VisualGraph<T> visualGraph : this.visualGraphs) {
								final GraphWrapperEditable opGW = visualGraph
								.createGraphWrapper(op);

								// walk through predicates...
								for (final Item pred : predicates) {
									copiedText
									.append(opGW.serializeOperator()
											+ " " + pred.toString()
											+ " "
											+ rdf2.serializeOperator()
											+ " .\n");
								}
							}
						}
					}
				}
			}
		}

		if (copiedText.length() > 0) {
			copiedText = new StringBuffer("{\n" + copiedText + "}\n");
		}

		for (final T op : this.selectedOperatorsList) {
			if (copiedList.contains(op)) {
				for (final VisualGraph<T> visualGraph : this.visualGraphs) {
					copiedText.append(visualGraph.createGraphWrapper(op)
							.serializeOperator()
							+ "\n");
				}
			}
		}

		final StringSelection fieldContent = new StringSelection(copiedText
				.toString());

		VisualEditor.clipboard.setContents(fieldContent, this);
	}

	public LinkedList<VisualGraph<T>> getVisualGraphs() {
		return this.visualGraphs;
	}
	
	public void updateSize(){
		for(VisualGraph<T> vg: this.visualGraphs){
			vg.updateSize();
		}
	}

	public StatusBar getStatusBar() {
		return this.statusBar;
	}

	public void cancelModi() {
		this.statusBar.clear();

		this.isInInsertMode = false;
		this.insertOperator = null;

		if (this.connectionMode != null) {
			this.connectionMode.cancel();
		}
	}

	public JMenuBar createMenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		menuBar.getSelectionModel().addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent ce) {
				cancelModi();
			}
		});

		return menuBar;
	}

	public abstract JMenuBar buildMenuBar();

	protected abstract void pasteElements(String content);

	public String getXPrefPrefix() {
		return this.getClass().getSimpleName();
	}
}