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
package lupos.gui.operatorgraph.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import lupos.engine.operators.BasicOperator;
import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.arrange.LayeredDrawing;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperBasicOperator;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperBasicOperatorByteArray;
import lupos.optimizations.logical.rules.DebugContainer;
import xpref.IXPref;
import xpref.XPref;
import xpref.datatypes.BooleanDatatype;
import xpref.datatypes.IntegerDatatype;

/**
 * This Class represents a Viewer for the OperatorGraph. It can also be used to
 * show the Abstract Syntax Tree.
 */
public class Viewer extends JFrame implements IXPref {
	/**
	 * Serial Version UID. Eclipse wants that.
	 */
	private static final long serialVersionUID = 5656699929754318373L;

	private OperatorGraphWithPrefix operatorGraph = null;

	private LinkedList<GraphWrapper> startGWs = null;

	private final Viewer that = this;

	private JCheckBox lcCheckBox = null;

	/**
	 * Constructor to show a tree with the viewer. The tree is given by the
	 * first node which is provides as a GraphWrapper object. This constructor
	 * also can take a title for the window.
	 * 
	 * @param startGW
	 *            the start node of the tree to be shown
	 * @param title
	 *            the title of the viewer window
	 * @param standAlone
	 *            should be true if the viewer is called as the only active GUI
	 *            component
	 * @param fromJar
	 *            should be true, if the viewer is loaded from a jar file
	 */
	public Viewer(final GraphWrapper startGW, final String title,
			final boolean standAlone, final boolean fromJar) {
		super();

		this.startGWs = new LinkedList<GraphWrapper>();
		this.startGWs.add(startGW);

		// initiate OperatorGraph class
		this.operatorGraph = new OperatorGraphWithPrefix();

		this.constructFrame(title, standAlone, fromJar);

		final JPanel topToolBar = this.createTopToolBar(standAlone, fromJar);
		this.getContentPane().add(topToolBar, BorderLayout.NORTH);

		this.finalizeFrame(topToolBar);
	}

	/**
	 * Constructor to show a tree with the viewer. The tree is given by the
	 * first node which is provides as a GraphWrapper object. This constructor
	 * also can take a title for the window. This constructor takes a list of
	 * DebugContainers and shows the tree in the last entry of that list.
	 * 
	 * @param debugContainerList
	 *            list of Debug containers
	 * @param title
	 *            the title of the viewer window
	 * @param standAlone
	 *            should be true if the viewer is called as the only active GUI
	 *            component
	 * @param fromJar
	 *            should be true, if the viewer is loaded from a jar file
	 */
	public Viewer(final List<DebugContainer<BasicOperator>> debugContainerList,
			final String title, final boolean standAlone, final boolean fromJar) {
		this(debugContainerList, new ViewerPrefix(true), title, standAlone,
				fromJar);
	}

	public Viewer(
			final String title,
			final List<DebugContainer<BasicOperatorByteArray>> debugContainerList,
			final boolean standAlone, final boolean fromJar) {
		this(debugContainerList, title, standAlone, fromJar, new ViewerPrefix(
				true));
	}

	/**
	 * Constructor to show a tree with the viewer. The tree is given by the
	 * first node which is provides as a GraphWrapper object. This constructor
	 * also can take a title for the window. This constructor takes a list of
	 * DebugContainers and shows the tree in the last entry of that list.
	 * 
	 * @param debugContainerList
	 *            list of Debug containers
	 * @param prefix
	 *            instance of an old prefix class that should be recycled
	 * @param title
	 *            the title of the viewer window
	 * @param standAlone
	 *            should be true if the viewer is called as the only active GUI
	 *            component
	 * @param fromJar
	 *            should be true, if the viewer is loaded from a jar file
	 */
	public Viewer(final List<DebugContainer<BasicOperator>> debugContainerList,
			final ViewerPrefix prefix, final String title,
			final boolean standAlone, final boolean fromJar) {
		super();

		this.startGWs = new LinkedList<GraphWrapper>();
		this.startGWs.add(new GraphWrapperBasicOperator(debugContainerList.get(
				debugContainerList.size() - 1).getRoot()));
		this.operatorGraph = new OperatorGraphWithPrefix(prefix); // initiate
		// OperatorGraph
		// class

		this.constructFrame(title, standAlone, fromJar);

		final JPanel topToolBar = this.createTopToolBar(standAlone, fromJar);
		this.getContentPane().add(topToolBar, BorderLayout.NORTH);

		final JPanel bottomToolBar = new DebugContainerToolBar<BasicOperator>(
				this, debugContainerList, fromJar);
		this.getContentPane().add(bottomToolBar, BorderLayout.SOUTH);

		this.finalizeFrame(topToolBar, bottomToolBar);
	}

	public Viewer(
			final List<DebugContainer<BasicOperatorByteArray>> debugContainerList,
			final String title, final boolean standAlone,
			final boolean fromJar, final ViewerPrefix prefix) {
		super();

		this.startGWs = new LinkedList<GraphWrapper>();
		this.startGWs
		.add(new GraphWrapperBasicOperatorByteArray(debugContainerList
				.get(debugContainerList.size() - 1).getRoot()));
		this.operatorGraph = new OperatorGraphWithPrefix(prefix); // initiate
		// OperatorGraph
		// class

		this.constructFrame(title, standAlone, fromJar);

		final JPanel topToolBar = this.createTopToolBar(standAlone, fromJar);
		this.getContentPane().add(topToolBar, BorderLayout.NORTH);

		final JPanel bottomToolBar = new DebugContainerToolBar<BasicOperatorByteArray>(
				this, debugContainerList, fromJar);
		this.getContentPane().add(bottomToolBar, BorderLayout.SOUTH);

		this.finalizeFrame(topToolBar, bottomToolBar);
	}

	public Viewer(final GraphWrapper startGW, final ViewerPrefix prefix,
			final String title, final boolean standAlone,
			final boolean fromJar, final JPanel toolbar) {
		super();
		this.startGWs = new LinkedList<GraphWrapper>();
		this.startGWs.add(startGW);
		this.operatorGraph = new OperatorGraphWithPrefix(prefix); // initiate
		// OperatorGraph
		// class

		this.constructFrame(title, standAlone, fromJar);

		final JPanel topToolBar = this.createTopToolBar(standAlone, fromJar);
		this.getContentPane().add(topToolBar, BorderLayout.NORTH);

		this.getContentPane().add(toolbar, BorderLayout.SOUTH);

		this.finalizeFrame(topToolBar, toolbar);
	}

	public Viewer(final LinkedList<GraphWrapper> startGW,
			final ViewerPrefix prefix, final String title,
			final boolean standAlone, final boolean fromJar,
			final JPanel toolbar) {
		super();

		this.startGWs = startGW;
		this.operatorGraph = new OperatorGraphWithPrefix(prefix); // initiate
		// OperatorGraph
		// class

		this.constructFrame(title, standAlone, fromJar);

		final JPanel topToolBar = this.createTopToolBar(standAlone, fromJar);
		this.getContentPane().add(topToolBar, BorderLayout.NORTH);

		this.getContentPane().add(toolbar, BorderLayout.SOUTH);

		this.finalizeFrame(topToolBar, toolbar);
	}

	/**
	 * This constructor generates the graph for the given filename and saves it
	 * as image to the given filename.
	 * 
	 * @param startGW
	 *            the first node of the graph
	 * @param filename
	 *            the filename to save the graph as image to
	 * @throws IOException 
	 */
	public Viewer(final GraphWrapper startGW, String filename) throws IOException {
		super();

		if (!(filename.endsWith(".png") || filename.endsWith(".jpeg") || filename.endsWith(".gif"))) {
			filename += ".png";
		}
		
		String format = filename.endsWith(".jpeg")?"jpeg":filename.substring(filename.length()-3);

		OutputStream out = new FileOutputStream(new File(filename));
		
		this.saveGraph(startGW, format, out);

		out.close();
	}

	/**
	 * This constructor generates the graph for the given filename and saves it
	 * as image to an outputstream.
	 * 
	 * @param startGW
	 *            the first node of the graph
	 * @param format
	 *            the format of the picture (e.g. png, gif or jpg)
	 * @param output
	 *            the outputstream to save the graph as image to
	 */
	public Viewer(final GraphWrapper startGW, final String format, final OutputStream out) {
		super();

		this.saveGraph(startGW, format, out);
	}
	
	private void saveGraph(final GraphWrapper startGW, final String format, final OutputStream out) {		
		this.startGWs = new LinkedList<GraphWrapper>();
		this.startGWs.add(startGW);

		ViewerPrefix prefix = new ViewerPrefix(true);
		this.operatorGraph = new OperatorGraphWithPrefix(prefix);
		prefix.setStatus(true);
		this.constructFrame("Intermediate frame for saving graph", true, false);

		this.setVisible(true);

		this.operatorGraph.saveGraph(format, out);

		this.setVisible(false);
	}
	
	/**
	 * Internal method to create the main Frame.
	 * 
	 * @param title
	 *            the title of the frame
	 * @param standAlone
	 *            true, if this GUI component is the only active one
	 * @param fromJar
	 *            should be true, if the viewer is loaded from a jar file
	 */
	private void constructFrame(final String title, final boolean standAlone,
			final boolean fromJar) {
		if (standAlone) {
			// try to set look and feel...
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		if (fromJar) {
			this.setIconImage(new ImageIcon(Viewer.class
					.getResource("/demo.gif")).getImage());
		} else {
			this.setIconImage(new ImageIcon("data" + File.separator
					+ "demo.gif").getImage());
		}

		final JPanel mainPanel = new JPanel(new BorderLayout());
		final JScrollPane scrollPane = new JScrollPane(this.operatorGraph);
		mainPanel.add(scrollPane);

		// create main window...
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		this.createGraphElement();
		this
		.setDefaultCloseOperation((standAlone) ? WindowConstants.EXIT_ON_CLOSE
				: WindowConstants.HIDE_ON_CLOSE);
		this.setTitle(title);
	}

	private void finalizeFrame(final JPanel... panels) {
		this.preferencesChanged();

		int minWidth = 0;
		int minHeight = 0;

		for (int i = 0; i < panels.length; i += 1) {
			final JPanel panel = panels[i];

			minWidth = Math.max(minWidth, panel.getPreferredSize().width);
			minHeight += panel.getPreferredSize().height;
		}

		this.setMinimumSize(new Dimension(minWidth + 30, minHeight + 10));
		this.setSize(1000, 600);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	@Override
	public void processWindowEvent(final WindowEvent e) {
		super.processWindowEvent(e);
	}

	/**
	 * Internal method to create the top tool bar.
	 * 
	 * @param fromJar
	 *            should be true, if the viewer is loaded from a jar file
	 * @param standAlone
	 *            true, if this GUI component is the only active one
	 * 
	 * @return a JToolBar with the elements for the top tool bar in it
	 */
	private JPanel createTopToolBar(final boolean standAlone,
			final boolean fromJar) {
		try {
			XPref.getInstance().registerComponent(this);
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		final JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

		toolBar.add(new JLabel("Zoom: ")); // add "Zoom: " label to toolBar
		toolBar.add(this.createZoomComboBox()); // add zoomDropDown to toolBar
		toolBar.add(new JLabel(" %")); // add " %" to toolBar
		toolBar.add(Box.createRigidArea(new Dimension(20, 0))); // add separator

		if (this.startGWs.get(0).usePrefixesActive()) {
			toolBar.add(this.createPrefixCheckBox()); // add CheckBox for
			// prefixes to toolBar
			toolBar.add(Box.createRigidArea(new Dimension(20, 0))); // add
			// separator
		}

		toolBar.add(this.createColorCheckBox()); // add CheckBox for line colors
		// to toolBar
		toolBar.add(Box.createRigidArea(new Dimension(20, 0))); // add separator
		toolBar.add(this.createArrangeButton()); // add button for arranging the
		// tree

		if (!fromJar) {
			toolBar.add(Box.createRigidArea(new Dimension(20, 0))); // add
			// separator
			toolBar.add(this.createSaveButton()); // add button for save image
			// to toolBar
		}

		if (standAlone) {
			toolBar.add(Box.createRigidArea(new Dimension(20, 0))); // add
			// separator
			toolBar.add(this.createPreferencesButton()); // add button to show
			// preferences
			// dialog
		}

		return toolBar;
	}

	@SuppressWarnings("unchecked")
	private void createGraphElement() {
		final JPanel opGraph = this.operatorGraph.createGraph(
				(LinkedList<GraphWrapper>) this.startGWs.clone(), false, false,
				false,
				Arrange.values()[0]);

		this.operatorGraph.updateMainPanel(opGraph);
	}

	public void createGraphElement(final GraphWrapper startGW) {
		this.startGWs = new LinkedList<GraphWrapper>();
		this.startGWs.add(startGW);

		this.createGraphElement();
	}

	/**
	 * Internal method to create the ComboBox box for the zoom.
	 * 
	 * @return zoom ComboBox
	 */
	private JComboBox createZoomComboBox() {
		final Vector<Integer> zoomFactors = new Vector<Integer>();
		zoomFactors.add(new Integer(50));
		zoomFactors.add(new Integer(75));
		zoomFactors.add(new Integer(100));
		zoomFactors.add(new Integer(200));

		try {
			final Integer zoomFactor = IntegerDatatype.getValues("viewer_zoom")
			.get(0);

			if (!zoomFactors.contains(zoomFactor)) {
				zoomFactors.add(zoomFactor);

				Collections.sort(zoomFactors);
			}

			final JComboBox zoomDropDown = new JComboBox(zoomFactors);
			zoomDropDown.setSelectedItem(zoomFactor);
			zoomDropDown.setEditable(true);
			zoomDropDown.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent ae) {
					int zoom = 0;

					// try to get the zoom value, if it is an integer...
					try {
						zoom = (Integer) ((JComboBox) ae.getSource())
						.getSelectedItem();
					} catch (final Exception exception) {
						return;
					}

					final double zFactor = ((double) zoom) / 100; // calculate
					// zoom
					// factor

					if (operatorGraph.updateZoomFactor(zFactor)) {
						operatorGraph.clearAll();

						createGraphElement();
					}
				}
			});

			if (this.operatorGraph.updateZoomFactor((Integer) zoomDropDown
					.getSelectedItem() / 100.0)) {
				this.operatorGraph.clearAll();

				this.createGraphElement();
			}

			return zoomDropDown;
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Internal method to create the check box to enable the usage of prefixes.
	 * 
	 * @return the prefix check box
	 */
	private JCheckBox createPrefixCheckBox() {
		final JCheckBox prefixCheckBox = new JCheckBox("Use prefixes",
				this.operatorGraph.getPrefix().isActive());
		prefixCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent ie) {
				final boolean status = (ie.getStateChange() == ItemEvent.SELECTED) ? true
						: false;

				operatorGraph.setPrefixStatus(status);
				operatorGraph.clearAll();

				createGraphElement();
			}
		});

		return prefixCheckBox;
	}

	/**
	 * Internal method to create the check box to enable colored arrows.
	 * 
	 * @return the line color check box
	 */
	private JCheckBox createColorCheckBox() {
		this.lcCheckBox = new JCheckBox("use colored arrows", false);
		this.lcCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent ie) {
				operatorGraph
				.setLineColorStatus((ie.getStateChange() == ItemEvent.SELECTED) ? true
						: false);

				operatorGraph.repaint(); // update the graph
			}
		});

		return this.lcCheckBox;
	}

	/**
	 * Internal method to create the button to arrange the current graph.
	 * 
	 * @return the arrange button
	 */
	private JPanel createArrangeButton() {
		final JPanel panel = new JPanel();

		panel.add(new JLabel("flip"));

		final JCheckBox checkBoxX = new JCheckBox("X");
		panel.add(checkBoxX);

		final JCheckBox checkBoxY = new JCheckBox("Y");
		panel.add(checkBoxY);

		final JCheckBox checkBoxRot = new JCheckBox("rot.");
		panel.add(checkBoxRot);

		final JComboBox comboBox = new JComboBox(Arrange.values());
		panel.add(comboBox);

		final JButton arrangeButton = new JButton("arrange");
		arrangeButton.setToolTipText("arrange the shown graph");
		arrangeButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				operatorGraph.arrange(checkBoxX.isSelected(),
						checkBoxY.isSelected(),
 checkBoxRot.isSelected(),
						(Arrange) comboBox.getSelectedItem());
				operatorGraph.arrange(checkBoxX.isSelected(),
						checkBoxY.isSelected(),
 checkBoxRot.isSelected(),
						(Arrange) comboBox.getSelectedItem());
			}
		});

		panel.add(arrangeButton);

		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		return panel;
	}

	/**
	 * Internal method to create the button to save the current graph as image.
	 * 
	 * @return the save button
	 */
	private JButton createSaveButton() {
		// set labels for FileChooser...
		UIManager.put("FileChooser.lookInLabelText", "search in:");
		UIManager.put("FileChooser.upFolderToolTipText",
		"One folder up in the hierarchy");
		UIManager.put("FileChooser.newFolderToolTipText", "create new folder");
		UIManager.put("FileChooser.fileNameLabelText", "file name:");
		UIManager.put("FileChooser.filesOfTypeLabelText", "file type:");
		UIManager.put("FileChooser.cancelButtonText", "Cancel");
		UIManager.put("FileChooser.cancelButtonToolTipText", "Cancel action");

		final SaveGraphDialog chooser = new SaveGraphDialog(); // create
		// FileChooser
		// for graph
		// saving

		final JButton saveButton = new JButton("save graph to file");
		saveButton
		.setToolTipText("save the current operator gmainPanelraph as image");
		saveButton.setMnemonic(KeyEvent.VK_S);
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				final int returnValue = chooser.showDialog(that, "Save"); // show
				// the
				// file
				// chooser
				// ...

				if (returnValue == JFileChooser.APPROVE_OPTION) { // get
					// filename
					// ...
					final String filename = chooser.getSelectedFile()
					.getAbsolutePath();

					that.operatorGraph.saveGraph(filename);
				}
			}
		});

		return saveButton;
	}

	/**
	 * Internal method to create the button to show the preferences dialog.
	 * 
	 * @return the preferences button
	 */
	private JButton createPreferencesButton() {
		final JButton preferencesButton = new JButton("Preferences");
		preferencesButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				final LinkedList<String> idList = new LinkedList<String>();

				for (final GraphWrapper gw : startGWs) {
					idList.add(gw.getWantedPreferencesID());
				}

				try {
					XPref.getInstance().showDialog(idList);
				} catch (final Exception e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		});

		return preferencesButton;
	}

	//	/**
	//	 * Internal method to save the graph to the given filename.
	//	 *
	//	 * @param filename
	//	 *            filename to save the file to
	//	 */
	//	private void saveGraph(String filename) {
	//		// add file extension, if necessary...
	//		if (!(filename.endsWith(".png") || filename.endsWith(".jpg") || filename
	//				.endsWith(".gif"))) {
	//			filename += ".png";
	//		}
	//
	//		try {
	//			// create image of graph to save it...
	//			final BufferedImage img = new BufferedImage(this.operatorGraph
	//					.getPreferredSize().width, this.operatorGraph
	//					.getPreferredSize().height, BufferedImage.TYPE_INT_RGB);
	//
	//			this.operatorGraph.paint(img.createGraphics()); // paint main panel
	//			// with graph image
	//
	//			ImageIO.write(img, filename.substring(filename.length() - 3),
	//					new File(filename));
	//		} catch (final Exception e) {
	//			e.printStackTrace();
	//		}
	//	}

	public void preferencesChanged() {
		try {
			this.lcCheckBox.setSelected(BooleanDatatype.getValues(
			"viewer_useColoredArrows").get(0).booleanValue());
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public OperatorGraphWithPrefix getOperatorGraph() {
		return operatorGraph;
	}
}