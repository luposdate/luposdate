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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;
import lupos.gui.anotherSyntaxHighlighting.javacc.HTMLScanner;
import lupos.gui.operatorgraph.viewer.Viewer;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.File;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.TypeEnum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DocumentationPanel extends JPanel {
	private static final long serialVersionUID = 7878587266163569062L;

	private RuleEditorPane ruleEditorPane = null;
	private DocumentationPanel that = this;
	private String elementName = "";
	private TypeEnum elementType = null;

	private JPanel seeAlsoPanel_rules = null;
	private JPanel seeAlsoPanel_rulePackages = null;
	private LinkedList<String> seeAlsoList_rules = new LinkedList<String>();
	private LinkedList<String> seeAlsoList_rulePackages = new LinkedList<String>();

	private LuposJTextPane tp_shortDescription = null;
	private LuposJTextPane tp_longDescription = null;
	private JCheckBox jCB_showVisualRepresentation = null;
	private LuposJTextPane tp_transitionText = null;
	private JCheckBox jCB_useGeneratedVisualRepresentation = null;
	private JTextField jTF_visualRepr_left = null;
	private JTextField jTF_visualRepr_right = null;

	public DocumentationPanel(TypeEnum elementType, String elementName, RuleEditorPane ruleEditorPane, JSONObject loadObject) {
		super(new BorderLayout());

		this.ruleEditorPane = ruleEditorPane;
		this.elementType = elementType;
		this.elementName = elementName;

		this.buildComponent(loadObject);
	}

	private void buildComponent(JSONObject loadObject) {
		
		// Panel for short description...
		JPanel shortDescrPanel=new JPanel(new BorderLayout());
		shortDescrPanel.add(new JLabel("Short description:"), BorderLayout.NORTH);
		
		LuposDocument document_shortDescription = new LuposDocument();
		this.tp_shortDescription = new LuposJTextPane(document_shortDescription);
		document_shortDescription.init(HTMLScanner.createILuposParser(new LuposDocumentReader(document_shortDescription)), true);

		JScrollPane sp_shortDescription = new JScrollPane(this.tp_shortDescription);
		sp_shortDescription.setPreferredSize(new Dimension(sp_shortDescription.getPreferredSize().width, 75));
		
		shortDescrPanel.add(sp_shortDescription, BorderLayout.CENTER);

		// Panel for long description...
		JPanel longDescrPanel=new JPanel(new BorderLayout());
		longDescrPanel.add(new JLabel("Long description:"), BorderLayout.NORTH);
		
		LuposDocument document_longDescription = new LuposDocument();
		this.tp_longDescription = new LuposJTextPane(document_longDescription);
		document_longDescription.init(HTMLScanner.createILuposParser(new LuposDocumentReader(document_shortDescription)), true);

		JScrollPane sp_longDescription = new JScrollPane(this.tp_longDescription);
		sp_longDescription.setPreferredSize(new Dimension(sp_longDescription.getPreferredSize().width, 150));

		longDescrPanel.add(sp_longDescription, BorderLayout.CENTER);
		
		// Panel See also
		JPanel seeAlsoPanel=new JPanel(new BorderLayout());
		seeAlsoPanel.add(new JLabel("See also:"), BorderLayout.NORTH);
		
		JPanel rulesPanel=new JPanel(new BorderLayout());
		JPanel rulepackagesPanel=new JPanel(new BorderLayout());
				
		rulesPanel.add(new JLabel("Rules:"), BorderLayout.NORTH);
		rulepackagesPanel.add(new JLabel("Rule Packages:"), BorderLayout.NORTH);
		
		this.seeAlsoPanel_rules = new JPanel(new GridLayout(0,3));

		rulesPanel.add(this.seeAlsoPanel_rules, BorderLayout.CENTER);

		this.seeAlsoPanel_rulePackages = new JPanel(new GridLayout(0,1));

		rulepackagesPanel.add(this.seeAlsoPanel_rulePackages, BorderLayout.CENTER);
		
		JSplitPane seeAlsoSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		seeAlsoSplitPane.setTopComponent(rulesPanel);
		seeAlsoSplitPane.setBottomComponent(rulepackagesPanel);
		seeAlsoSplitPane.setOneTouchExpandable(true);
		seeAlsoSplitPane.setContinuousLayout(true);
		seeAlsoSplitPane.setDividerLocation(0.5);
		
		seeAlsoPanel.add(seeAlsoSplitPane, BorderLayout.CENTER);
		
		// create split panes...
		JSplitPane descrSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		descrSplitPane.setTopComponent(shortDescrPanel);
		descrSplitPane.setBottomComponent(longDescrPanel);
		descrSplitPane.setOneTouchExpandable(true);
		
		JSplitPane mainSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setTopComponent(descrSplitPane);
		mainSplitPane.setOneTouchExpandable(true);
		
		// Panel Visual Representation		
		final JPanel visualRepresentation = new JPanel(new BorderLayout());
		final JPanel filePanel = new JPanel();

		if(this.elementType == TypeEnum.Rule) {
			
			this.jCB_showVisualRepresentation = new JCheckBox("show visual representation", true);
			visualRepresentation.add(this.jCB_showVisualRepresentation, BorderLayout.NORTH);
			
			final JPanel innerVisualRepresPanel = new JPanel(new BorderLayout()); 
			this.jCB_showVisualRepresentation.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					innerVisualRepresPanel.setVisible(ie.getStateChange() == ItemEvent.SELECTED);
				}
			});
						
			innerVisualRepresPanel.add(new JLabel("Transition text:"), BorderLayout.NORTH);
			
			LuposDocument document_transitionText = new LuposDocument();
			this.tp_transitionText = new LuposJTextPane(document_transitionText);
			document_transitionText.init(HTMLScanner.createILuposParser(new LuposDocumentReader(document_transitionText)), true);

			JScrollPane sp_transitionText = new JScrollPane(this.tp_transitionText);
			sp_transitionText.setPreferredSize(new Dimension(sp_transitionText.getPreferredSize().width, 75));

			innerVisualRepresPanel.add(sp_transitionText, BorderLayout.CENTER);
			
			visualRepresentation.add(innerVisualRepresPanel, BorderLayout.CENTER);

			// -- use generated visual representation images --
			// - file panel - begin -
			final JFileChooser chooser = new JFileChooser("");
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			this.jTF_visualRepr_left = new JTextField("", 15);
			this.jTF_visualRepr_left.setEditable(false);

			JButton visualRepr_left_button = new JButton("Select");
			visualRepr_left_button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int ret = chooser.showDialog(that, "Select");

					if(ret == JFileChooser.APPROVE_OPTION) {
						String fileName = chooser.getSelectedFile().getAbsolutePath();

						that.jTF_visualRepr_left.setText(fileName);
					}
				}
			});

			this.jTF_visualRepr_right = new JTextField("", 15);
			this.jTF_visualRepr_right.setEditable(false);

			JButton visualRepr_right_button = new JButton("Select");
			visualRepr_right_button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int ret = chooser.showDialog(that, "Select");

					if(ret == JFileChooser.APPROVE_OPTION) {
						String fileName = chooser.getSelectedFile().getAbsolutePath();

						that.jTF_visualRepr_right.setText(fileName);
					}
				}
			});

			GridBagConstraints filePanelGBC = new GridBagConstraints();
			filePanelGBC.anchor = GridBagConstraints.WEST;
			filePanelGBC.gridx = filePanelGBC.gridy = 0;
			filePanelGBC.weightx = 1.0;
			filePanelGBC.insets = new Insets(5, 5, 0, 5);
			filePanelGBC.fill = GridBagConstraints.HORIZONTAL;

			filePanel.setLayout(new GridBagLayout());
			filePanel.setVisible(false);

			filePanel.add(new JLabel("left image:"), filePanelGBC);
			filePanelGBC.gridx++;
			filePanelGBC.insets = new Insets(5, 0, 5, 5);

			filePanel.add(this.jTF_visualRepr_left, filePanelGBC);
			filePanelGBC.gridx++;
			filePanelGBC.insets = new Insets(5, 0, 5, 15);

			filePanel.add(visualRepr_left_button, filePanelGBC);
			filePanelGBC.gridx++;
			filePanelGBC.insets = new Insets(5, 0, 5, 5);

			filePanel.add(new JLabel("right image:"), filePanelGBC);
			filePanelGBC.gridx++;

			filePanel.add(this.jTF_visualRepr_right, filePanelGBC);
			filePanelGBC.gridx++;

			filePanel.add(visualRepr_right_button, filePanelGBC);
			// - file panel - end -

			this.jCB_useGeneratedVisualRepresentation = new JCheckBox("use generated visual representation", true);
			this.jCB_useGeneratedVisualRepresentation.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					filePanel.setVisible(ie.getStateChange() != ItemEvent.SELECTED);
				}
			});
			
			JPanel interVisReprPanel = new JPanel(new BorderLayout());

			interVisReprPanel.add(this.jCB_useGeneratedVisualRepresentation, BorderLayout.NORTH);
			interVisReprPanel.add(filePanel, BorderLayout.SOUTH);

			innerVisualRepresPanel.add(interVisReprPanel, BorderLayout.SOUTH);

			JSplitPane bottomSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			bottomSplitPane.setTopComponent(seeAlsoPanel);
			bottomSplitPane.setBottomComponent(visualRepresentation);
			bottomSplitPane.setOneTouchExpandable(true);

			mainSplitPane.setBottomComponent(bottomSplitPane);			
			
		} else {
			mainSplitPane.setBottomComponent(seeAlsoPanel);			
		}

		this.add(mainSplitPane, BorderLayout.CENTER);
		
		if(loadObject != null) {
			try {
				this.tp_shortDescription.setText(loadObject.getString("short description"));
				this.tp_longDescription.setText(loadObject.getString("long description"));


				JSONObject seeAlsoLoadObject = loadObject.getJSONObject("see also");


				JSONArray seeAlso_rules_loadArray = seeAlsoLoadObject.getJSONArray("rules");
				this.seeAlsoList_rules.clear();

				for(int i = 0; i < seeAlso_rules_loadArray.length(); i += 1) {
					this.seeAlsoList_rules.add(seeAlso_rules_loadArray.getString(i));
				}


				JSONArray seeAlso_rulePackages_loadArray = seeAlsoLoadObject.getJSONArray("rule packages");
				this.seeAlsoList_rulePackages.clear();

				for(int i = 0; i < seeAlso_rulePackages_loadArray.length(); i += 1) {
					this.seeAlsoList_rulePackages.add(seeAlso_rulePackages_loadArray.getString(i));
				}


				if(this.elementType == TypeEnum.Rule) {
					boolean bool = loadObject.getBoolean("show visual representation");
					this.jCB_showVisualRepresentation.setSelected(bool);
					visualRepresentation.setVisible(bool);

					this.tp_transitionText.setText(loadObject.getString("transition text"));

					bool = loadObject.getBoolean("use generated visual representation");
					this.jCB_useGeneratedVisualRepresentation.setSelected(bool);
					filePanel.setVisible(!bool);

					this.jTF_visualRepr_left.setText(loadObject.getString("left image"));
					this.jTF_visualRepr_right.setText(loadObject.getString("right image"));


				}
			}
			catch(JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void rebuild(LinkedList<RulePanel> rules, LinkedList<RulePackagePanel> rulePackages) {
		// --- rules - begin ---
		this.seeAlsoPanel_rules.removeAll();

		for(RulePanel rule : rules) {
			final String ruleName = rule.toString();

			if(ruleName == this.elementName) {
				continue;
			}

			boolean state = this.seeAlsoList_rules.contains(ruleName);

			JCheckBox jCheckBox = new JCheckBox(ruleName, state);
			jCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					if(ie.getStateChange() == ItemEvent.SELECTED) {
						that.seeAlsoList_rules.add(ruleName);
					}
					else {
						that.seeAlsoList_rules.remove(ruleName);
					}
				}
			});

			this.seeAlsoPanel_rules.add(jCheckBox);

		}
		// --- rules - end ---

		// --- rule packages - begin ---
		this.seeAlsoPanel_rulePackages.removeAll();

		for(RulePackagePanel rulePackage : rulePackages) {
			final String rulePackageName = rulePackage.toString();

			if(rulePackageName == this.elementName) {
				continue;
			}

			boolean state = this.seeAlsoList_rulePackages.contains(rulePackageName);

			JCheckBox jCheckBox = new JCheckBox(rulePackageName, state);
			jCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					if(ie.getStateChange() == ItemEvent.SELECTED) {
						that.seeAlsoList_rulePackages.add(rulePackageName);
					}
					else {
						that.seeAlsoList_rulePackages.remove(rulePackageName);
					}
				}
			});

			this.seeAlsoPanel_rulePackages.add(jCheckBox);//, this.seeAlsoGBC);

		}
		// --- rule packages - end ---
	}

	public void setElementName(String newName) {
		this.elementName = newName;
	}

	public void updateRuleName(String oldName, String newName) {
		if(this.seeAlsoList_rules.contains(oldName)) {
			this.seeAlsoList_rules.remove(oldName);

			this.seeAlsoList_rules.add(newName);
		}
	}

	public void updateRulePackageName(String oldName, String newName) {
		if(this.seeAlsoList_rulePackages.contains(oldName)) {
			this.seeAlsoList_rulePackages.remove(oldName);

			this.seeAlsoList_rulePackages.add(newName);
		}
	}


	public String getShortDescription() {
		return this.tp_shortDescription.getText();
	}

	public String getContent(String targetDirectory, JTabbedPane[] rules) {
		StringBuffer content = new StringBuffer();

		if(!this.tp_shortDescription.getText().equalsIgnoreCase("")) {
			content.append("<h2>Short Description:</h2>\n");
			content.append(this.tp_shortDescription.getText() + "\n");
		}

		if(!this.tp_longDescription.getText().equalsIgnoreCase("")) {
			content.append("<h2>Long Description:</h2>\n");
			content.append(this.tp_longDescription.getText() + "\n");
		}

		if(rules != null && rules.length > 0) {
			content.append("<h2>Rules:</h2>\n");

			for(JTabbedPane rulePane : rules) {
				RulePanel rule = (RulePanel) rulePane;
				String ruleName = rule.toString();
				String ruleLink = ruleName.replaceAll(" ", "").toLowerCase() + "Rule.html";

				content.append("<a href=\"" + ruleLink + "\">" + ruleName + "</a>: " + rule.getDocumentationPanel().getShortDescription() + "<br>\n");
			}
		}

		if(this.elementType == TypeEnum.Rule && this.jCB_showVisualRepresentation.isSelected()) {
			String leftImageTargetPath = "";
			String rightImageTargetPath = "";
			String namebit = this.elementName.replaceAll(" ", "").toLowerCase();
			String namebit_left = namebit + "_leftImage.";
			String namebit_right = namebit + "_rightImage.";
			String leftImageName = "";
			String rightImageName = "";
			boolean showVisualRepresentation = true;

			if(this.jCB_useGeneratedVisualRepresentation.isSelected()) {
				leftImageName = namebit_left + "png";
				rightImageName = namebit_right + "png";
				leftImageTargetPath = targetDirectory + leftImageName;
				rightImageTargetPath = targetDirectory + rightImageName;

				LinkedList<VisualGraph<Operator>> visualGraphs = this.ruleEditorPane.getVisualGraphs();

				if(!visualGraphs.get(0).hasElements() && !visualGraphs.get(1).hasElements()) {
					showVisualRepresentation = false;
				}
				else {
					
					try {
						visualGraphs.get(0).saveGraph(leftImageTargetPath);
						visualGraphs.get(1).saveGraph(rightImageTargetPath);
					} catch (IOException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
			}
			else {
				String leftImagePath = this.jTF_visualRepr_left.getText();
				String rightImagePath = this.jTF_visualRepr_right.getText();

				if(leftImagePath.equals("") && rightImagePath.equals("")) {
					showVisualRepresentation = false;
				}
				else {
					File leftImage = new File(leftImagePath);
					leftImageName = namebit_left + leftImage.getFileExtension();
					leftImageTargetPath = targetDirectory + leftImageName;
					leftImage.copyFileTo(leftImageTargetPath);

					File rightImage = new File(rightImagePath);
					rightImageName = namebit_right + rightImage.getFileExtension();
					rightImageTargetPath = targetDirectory + rightImageName;
					rightImage.copyFileTo(rightImageTargetPath);
				}
			}

			if(showVisualRepresentation) {
				content.append("<table vertical-align=\"top\"><tr>\n");
				content.append("<td><img src=\"" + leftImageName + "\"></td>\n");
				content.append("<td>" + this.tp_transitionText.getText() + "<br><img src=\"ruleTransitionArrow.png\"></td>\n");
				content.append("<td><img src=\"" + rightImageName + "\"></td>\n");
				content.append("</tr></table>");
			}
		}

		LinkedList<String> seeAlsoList_RulePackages = this.seeAlsoList_rulePackages;
		LinkedList<String> seeAlsoList_Rules = this.seeAlsoList_rules;

		if(seeAlsoList_RulePackages.size() > 0 || seeAlsoList_Rules.size() > 0) {
			content.append("<h2>See Also:</h2>\n");

			if(seeAlsoList_RulePackages.size() > 0) {
				content.append("<b>RulePackages:</b> " + this.formatSeeAlso(TypeEnum.RulePackage, seeAlsoList_RulePackages) + "<br>\n");
			}

			if(seeAlsoList_Rules.size() > 0) {
				content.append("<b>Rules:</b> " + this.formatSeeAlso(TypeEnum.Rule, seeAlsoList_Rules) + "<br>\n");
			}
		}

		return content.toString();
	}

	private StringBuffer formatSeeAlso(TypeEnum e, LinkedList<String> seeAlso_List) {
		StringBuffer buffer = new StringBuffer();
		String linkbit = (e == TypeEnum.Rule) ? "Rule" : "PackageDescription";

		for(int i = 0; i < seeAlso_List.size(); i += 1) {
			String s = seeAlso_List.get(i);

			buffer.append("<a href=\"" + s.replaceAll(" ", "").toLowerCase() + linkbit + ".html\">" + s + "</a>");

			if(i != seeAlso_List.size()-1) {
				buffer.append(", ");
			}
		}

		return buffer;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject seeAlsoSaveObject = new JSONObject();
		seeAlsoSaveObject.put("rules", this.seeAlsoList_rules);
		seeAlsoSaveObject.put("rule packages", this.seeAlsoList_rulePackages);

		JSONObject saveObject = new JSONObject();
		saveObject.put("short description", this.tp_shortDescription.getText());
		saveObject.put("long description", this.tp_longDescription.getText());
		saveObject.put("see also", seeAlsoSaveObject);

		if(this.elementType == TypeEnum.Rule) {
			saveObject.put("show visual representation", this.jCB_showVisualRepresentation.isSelected());
			saveObject.put("transition text", this.tp_transitionText.getText());
			saveObject.put("use generated visual representation", this.jCB_useGeneratedVisualRepresentation.isSelected());
			saveObject.put("left image", this.jTF_visualRepr_left.getText());
			saveObject.put("right image", this.jTF_visualRepr_right.getText());
		}

		return saveObject;
	}
}