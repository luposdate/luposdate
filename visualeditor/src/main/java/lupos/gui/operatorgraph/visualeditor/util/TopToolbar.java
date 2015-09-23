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
package lupos.gui.operatorgraph.visualeditor.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import lupos.gui.anotherSyntaxHighlighting.ILuposParser;
import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;
import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.arrange.LayoutTest;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.viewer.Viewer;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;

import org.json.JSONException;
import org.json.JSONObject;

import xpref.IXPref;
import xpref.XPref;
import xpref.datatypes.BooleanDatatype;
import xpref.datatypes.IntegerDatatype;
public class TopToolbar<T> extends JPanel implements IXPref {
	private static final long serialVersionUID = 4732754047816183556L;

	private VisualEditor<T> editor = null;
	private LinkedList<VisualGraph<T>> visualGraphs = new LinkedList<VisualGraph<T>>();
	private JCheckBox lcCheckBox;
	private JComboBox zoomDropDown;
	private XPref preferences;
	private JSONObject loadObject = null;

	final JComboBox comboBox = new JComboBox(Arrange.values());

	/**
	 * <p>Constructor for TopToolbar.</p>
	 *
	 * @param editor a {@link lupos.gui.operatorgraph.visualeditor.VisualEditor} object.
	 * @param standAlone a boolean.
	 * @param loadObject a {@link org.json.JSONObject} object.
	 */
	public TopToolbar(final VisualEditor<T> editor, final boolean standAlone, final JSONObject loadObject) {
		super(new BorderLayout());

		final JPanel interPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		this.loadObject = loadObject;

		this.editor = editor;
		this.visualGraphs = editor.getVisualGraphs();

		try {
			this.preferences = XPref.getInstance();
			this.preferences.registerComponent(this);
		}
		catch(final Exception e) {
			e.printStackTrace();
		}

		interPanel.add(new JLabel("Zoom: ")); // add "Zoom: " label to toolBar
		interPanel.add(this.createZoomComboBox()); // add zoomDropDown to
													// toolBar
		interPanel.add(new JLabel(" %")); // add " %" to toolBar
		interPanel.add(Box.createRigidArea(new Dimension(20, 0))); // add separator
		
		interPanel.add(this.createRotateButton());
		
		interPanel.add(this.createColorCheckBox()); // add CheckBox for line
													// colors to toolBar
		interPanel.add(Box.createRigidArea(new Dimension(20, 0))); // add
																	// separator
		interPanel.add(this.createArrangeButton()); // add button for arranging
													// the tree

		if(standAlone) {
			interPanel.add(Box.createRigidArea(new Dimension(20, 0))); // add
																		// separator
			interPanel.add(this.createPreferencesButton()); // add button for
															// preferences
															// dialog
		}

		JScrollPane scrollPane=new JScrollPane(interPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(scrollPane, BorderLayout.CENTER);
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
			Integer zoomFactor;

			if(this.loadObject != null) {
				zoomFactor = this.loadObject.getInt("zoom");
			}
			else {
				zoomFactor = IntegerDatatype.getValues("viewer_zoom").get(0);
			}

			if(!zoomFactors.contains(zoomFactor)) {
				zoomFactors.add(zoomFactor);

				Collections.sort(zoomFactors);
			}

			this.zoomDropDown = new JComboBox(zoomFactors);
			this.zoomDropDown.setSelectedItem(zoomFactor);
			this.zoomDropDown.setEditable(true);
			this.zoomDropDown.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent ae) {
					editor.cancelModi();

					int zoom = 0;

					// try to get the zoom value, if it is an integer...
					try {
						zoom = (Integer) ((JComboBox) ae.getSource()).getSelectedItem();
					}
					catch(final Exception exception) {
						return;
					}

					setTextStatusBar("Zooming to " + zoom + "% ...");

					final double zFactor = ((double) zoom) / 100; // calculate zoom factor

					for(final VisualGraph<T> visualGraph : visualGraphs) {
						double factor = zFactor / visualGraph.getZoomFactor();
						if(visualGraph.updateZoomFactor(zFactor)) {
							final LinkedList<GraphWrapper> rootList = visualGraph.getRootList(true);
							
							Map<GraphWrapper, GraphBox> oldBoxes = (Map<GraphWrapper, GraphBox>) visualGraph.getBoxes().clone();

							visualGraph.clear();
							visualGraph.updateMainPanel(visualGraph
									.createGraph(rootList, (Arrange) comboBox.getSelectedItem(), factor, oldBoxes));
						}
					}

					clearStatusBar();
				}
			});

			for(final VisualGraph<T> visualGraph : this.visualGraphs) {
				if(visualGraph.updateZoomFactor((Integer) this.zoomDropDown.getSelectedItem() / 100.0)) {
					final LinkedList<GraphWrapper> rootList = visualGraph.getRootList(true);

					visualGraph.clear();
					visualGraph.updateMainPanel(visualGraph.createGraph(rootList, (Arrange) comboBox.getSelectedItem()));
				}
			}

			return this.zoomDropDown;
		}
		catch(final Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	private JPanel createRotateButton(){
		final Vector<Integer> rotateFactors = new Vector<Integer>();
		rotateFactors.add(new Integer(0));
		rotateFactors.add(new Integer(45));
		rotateFactors.add(new Integer(90));
		rotateFactors.add(new Integer(135));
		rotateFactors.add(new Integer(180));
		rotateFactors.add(new Integer(225));
		rotateFactors.add(new Integer(270));
		rotateFactors.add(new Integer(215));
		final JComboBox rotateDropDown = new JComboBox(rotateFactors);
		rotateDropDown.setEditable(true);
		
		final JButton button = new JButton("rotate"); 
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				int rotate = 0;
				try {
					rotate = (Integer) rotateDropDown.getSelectedItem();
				} catch (final Exception exception) {
					return;
				}
				rotate %= 360;

				for(final VisualGraph<T> visualGraph : visualGraphs) {
					final LinkedList<GraphWrapper> rootList = visualGraph.getRootList(true);
					
					Map<GraphWrapper, GraphBox> oldBoxes = (Map<GraphWrapper, GraphBox>) visualGraph.getBoxes().clone();
	
					visualGraph.clear();
					
					visualGraph.updateMainPanel(visualGraph.rotate(rotate, rootList, oldBoxes));
				}
			}			
		});
		JPanel result = new JPanel(new FlowLayout());
		result.add(rotateDropDown);
		result.add(button);
		return result;
	}

	/**
	 * Internal method to create the check box to enable colored arrows.
	 *
	 * @return the line color check box
	 */
	private JCheckBox createColorCheckBox() {
		try {
			boolean useColoredArrows = false;

			if(this.loadObject != null) {
				useColoredArrows = this.loadObject.getBoolean("use colored arrows");
			}
			else {
				useColoredArrows = BooleanDatatype.getValues("viewer_useColoredArrows").get(0).booleanValue();
			}

			for(final VisualGraph<T> g : this.visualGraphs) {
				g.setLineColorStatus(useColoredArrows);
			}

			this.lcCheckBox = new JCheckBox("use colored arrows", useColoredArrows);
			this.lcCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent ie) {
					editor.cancelModi();

					for(final VisualGraph<T> visualGraph : visualGraphs) {
						visualGraph.setLineColorStatus((ie.getStateChange() == ItemEvent.SELECTED) ? true : false);
						visualGraph.repaint(); // update the graph
					}
				}
			});

			return this.lcCheckBox;
		}
		catch(final Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Internal method to create the button to arrange the current graph.
	 *
	 * @return the arrange button
	 */
	private JPanel createArrangeButton() {
		final JPanel panel = new JPanel();

		panel.add(this.comboBox);

		final JButton arrangeButton = new JButton("arrange");
		arrangeButton.setToolTipText("arrange the shown graph");
		arrangeButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				editor.cancelModi();

				setTextStatusBar("Arranging graph ...");

				for(final VisualGraph<T> visualGraph : visualGraphs) {
					visualGraph.arrange((Arrange) comboBox.getSelectedItem());
				}

				clearStatusBar();
			}
		});

		panel.add(arrangeButton);
		
		final JButton qButton = new JButton("Graph quality");
		qButton.setToolTipText("determines quality of current graph...");
		qButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				editor.cancelModi();

				setTextStatusBar("Determining quality of current graph ...");

				for(final VisualGraph<T> visualGraph : visualGraphs) {
					Viewer.showString(LayoutTest.test(visualGraph), "Result of Graph Test");
				}

				clearStatusBar();
			}
		});

		panel.add(qButton);
		
		return panel;
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
				idList.add("viewer_useColoredArrows");

				String className = editor.getClass().getSimpleName();
				className = className.substring(0, 1).toLowerCase() + className.substring(1);

				idList.add(className + "_useStyledBoxes");
				try {
					preferences.showDialog(idList);
				}
				catch(final Exception e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		});

		return preferencesButton;
	}

	/**
	 * <p>preferencesChanged.</p>
	 */
	public void preferencesChanged() {
		try {
			this.lcCheckBox.setSelected(BooleanDatatype.getValues("viewer_useColoredArrows").get(0).booleanValue());
		}
		catch(final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private void setTextStatusBar(final String text) {
		if (editor.getStatusBar() != null)
			editor.getStatusBar().setText(text);
	}

	private void clearStatusBar() {
		if (editor.getStatusBar() != null)
			editor.getStatusBar().clear();
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public JSONObject toJSON() throws JSONException {
		final JSONObject saveObject = new JSONObject();
		saveObject.put("zoom", this.zoomDropDown.getSelectedItem());
		saveObject.put("use colored arrows", this.lcCheckBox.isSelected());

		return saveObject;
	}
}
