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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;
import lupos.gui.anotherSyntaxHighlighting.javacc.JavaScanner;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleEnum;

import org.json.JSONException;
import org.json.JSONObject;
public class ImplementationPanel extends JPanel {
	private static final long serialVersionUID = 7960538499474821569L;

	private ImplementationPanel that = this;
	private ButtonGroup buttonGroup_checkMethod = new ButtonGroup();
	private ButtonGroup buttonGroup_replaceMethod = new ButtonGroup();
	private JPanel leftPanel_checkMethod = null;
	private JPanel leftPanel_replaceMethod = null;
	private JPanel rightPanel_checkMethod = null;
	private JPanel rightPanel_replaceMethod = null;
	private LuposJTextPane jTP_globalJavaCode = null;
	private JRadioButton jRB_useGeneratedCode_checkMethod = null;
	private JRadioButton jRB_useOwnCode_checkMethod = null;
	private JRadioButton jRB_useGeneratedCode_replaceMethod = null;
	private JRadioButton jRB_useOwnCode_replaceMethod = null;
	private LuposJTextPane jTP_additionalCheckJavaCode = null;
	private LuposJTextPane jTP_additionalReplaceJavaCode = null;
	private LuposJTextPane jTP_checkJavaCode = null;
	private LuposJTextPane jTP_replaceJavaCode = null;
	private LuposJTextPane jTP_additionalImportDeclarations = null;
	private JComboBox jCB_startNode = null;

	/**
	 * <p>Constructor for ImplementationPanel.</p>
	 *
	 * @param loadObject a {@link org.json.JSONObject} object.
	 */
	public ImplementationPanel(JSONObject loadObject) {
		super(new BorderLayout());
		
		JPanel globalImportCodePanel=new JPanel(new BorderLayout());
		globalImportCodePanel.add(new JLabel("Additional import declarations:"), BorderLayout.NORTH);

		LuposDocument document_globalImportCode = new LuposDocument();
		this.jTP_additionalImportDeclarations = new LuposJTextPane(document_globalImportCode);
		document_globalImportCode.init(JavaScanner.createILuposParser(new LuposDocumentReader(document_globalImportCode)), true);

		JScrollPane jSP_globalImportCode = new JScrollPane(this.jTP_additionalImportDeclarations);

		globalImportCodePanel.add(jSP_globalImportCode, BorderLayout.CENTER);
		
		JPanel globalJavaCodePanel=new JPanel(new BorderLayout());
		globalJavaCodePanel.add(new JLabel("Additional global java code:"), BorderLayout.NORTH);

		LuposDocument document_globalJavaCode = new LuposDocument();
		this.jTP_globalJavaCode = new LuposJTextPane(document_globalJavaCode);
		document_globalJavaCode.init(JavaScanner.createILuposParser(new LuposDocumentReader(document_globalJavaCode)), true);

		JScrollPane jSP_globalJavaCode = new JScrollPane(this.jTP_globalJavaCode);

		globalJavaCodePanel.add(jSP_globalJavaCode, BorderLayout.CENTER);

		JPanel[] panelsLeft=this.buildLeftSide();

		JPanel[] panelsRight=this.buildRightSide();
		
		// create splitPanes...
		
		JSplitPane vert1=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		vert1.setOneTouchExpandable(true);
		vert1.setTopComponent(panelsLeft[0]);
		vert1.setBottomComponent(panelsRight[0]);

		JSplitPane vert2=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		vert2.setOneTouchExpandable(true);
		vert2.setTopComponent(panelsLeft[1]);
		vert2.setBottomComponent(panelsRight[1]);

		JSplitPane bottom=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		bottom.setOneTouchExpandable(true);
		bottom.setTopComponent(vert1);
		bottom.setBottomComponent(vert2);
				
		JSplitPane innerTop=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		innerTop.setOneTouchExpandable(true);
		innerTop.setTopComponent(globalImportCodePanel);
		innerTop.setBottomComponent(globalJavaCodePanel);
		
		JSplitPane top=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		top.setOneTouchExpandable(true);
		top.setTopComponent(innerTop);
		top.setBottomComponent(bottom);

		this.add(top);
		
		if(loadObject != null) {
			try {
				this.jTP_additionalImportDeclarations.setText(loadObject.getString("additional import declarations"));
				
				this.jTP_globalJavaCode.setText(loadObject.getString("additional global java code"));

				JSONObject methodLoadObject = loadObject.getJSONObject("check method");

				boolean useGeneratedCode = methodLoadObject.getBoolean("use generated code");
				this.jRB_useGeneratedCode_checkMethod.setSelected(useGeneratedCode);
				this.jRB_useOwnCode_checkMethod.setSelected(!useGeneratedCode);
				this.enableSidePanel(this.leftPanel_checkMethod, useGeneratedCode);
				this.enableSidePanel(this.rightPanel_checkMethod, !useGeneratedCode);

				this.jTP_additionalCheckJavaCode.setText(methodLoadObject.getString("additional code"));
				this.jTP_checkJavaCode.setText(methodLoadObject.getString("code"));
				this.jCB_startNode.setSelectedItem(RuleEnum.valueOf(methodLoadObject.getString("start node")));


				methodLoadObject = loadObject.getJSONObject("replace method");

				useGeneratedCode = methodLoadObject.getBoolean("use generated code");
				this.jRB_useGeneratedCode_replaceMethod.setSelected(useGeneratedCode);
				this.jRB_useOwnCode_replaceMethod.setSelected(!useGeneratedCode);
				this.enableSidePanel(this.leftPanel_replaceMethod, useGeneratedCode);
				this.enableSidePanel(this.rightPanel_replaceMethod, !useGeneratedCode);

				this.jTP_additionalReplaceJavaCode.setText(methodLoadObject.getString("additional code"));
				this.jTP_replaceJavaCode.setText(methodLoadObject.getString("code"));
			}
			catch(JSONException e) {
				e.printStackTrace();
			}
		}
		else {
			this.enableSidePanel(this.rightPanel_checkMethod, false);
			this.enableSidePanel(this.rightPanel_replaceMethod, false);
		}
	}

	private JPanel[] buildLeftSide() {
		JPanel[] panels=new JPanel[2];
		
		this.leftPanel_checkMethod = new JPanel(new BorderLayout());

		this.jRB_useGeneratedCode_checkMethod = new JRadioButton("Use generated code", true);
		this.jRB_useGeneratedCode_checkMethod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ee) {
				that.enableSidePanel(that.leftPanel_checkMethod, true);
				that.enableSidePanel(that.rightPanel_checkMethod, false);
			}
		});

		this.buttonGroup_checkMethod.add(this.jRB_useGeneratedCode_checkMethod);

		panels[0]=new JPanel(new BorderLayout());
		panels[0].add(this.jRB_useGeneratedCode_checkMethod, BorderLayout.NORTH);

		this.leftPanel_checkMethod.add(new JLabel("Additional code for check method:"), BorderLayout.NORTH);

		LuposDocument document_additionalCheckJavaCode = new LuposDocument();
		this.jTP_additionalCheckJavaCode = new LuposJTextPane(document_additionalCheckJavaCode);
		document_additionalCheckJavaCode.init(JavaScanner.createILuposParser(new LuposDocumentReader(document_additionalCheckJavaCode)), true);

		JScrollPane jSP_additionalCheckJavaCode = new JScrollPane(this.jTP_additionalCheckJavaCode);

		this.leftPanel_checkMethod.add(jSP_additionalCheckJavaCode, BorderLayout.CENTER);

		panels[0].add(this.leftPanel_checkMethod, BorderLayout.CENTER);

		this.leftPanel_replaceMethod = new JPanel(new BorderLayout());

		this.jRB_useGeneratedCode_replaceMethod = new JRadioButton("Use generated code", true);
		this.jRB_useGeneratedCode_replaceMethod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ee) {
				that.enableSidePanel(that.leftPanel_replaceMethod, true);
				that.enableSidePanel(that.rightPanel_replaceMethod, false);
			}
		});

		this.buttonGroup_replaceMethod.add(this.jRB_useGeneratedCode_replaceMethod);

		panels[1]=new JPanel(new BorderLayout());
		panels[1].add(this.jRB_useGeneratedCode_replaceMethod, BorderLayout.NORTH);

		this.leftPanel_replaceMethod.add(new JLabel("Additional code for replace method:"), BorderLayout.NORTH);

		LuposDocument document_additionalReplaceJavaCode = new LuposDocument();
		this.jTP_additionalReplaceJavaCode = new LuposJTextPane(document_additionalReplaceJavaCode);
		document_additionalReplaceJavaCode.init(JavaScanner.createILuposParser(new LuposDocumentReader(document_additionalReplaceJavaCode)), true);

		JScrollPane jSP_additionalReplaceJavaCode = new JScrollPane(this.jTP_additionalReplaceJavaCode);

		this.leftPanel_replaceMethod.add(jSP_additionalReplaceJavaCode, BorderLayout.CENTER);

		panels[1].add(this.leftPanel_replaceMethod, BorderLayout.CENTER);

		return panels;
	}

	private JPanel[] buildRightSide() {
		JPanel[] panels=new JPanel[2];
		
		this.rightPanel_checkMethod = new JPanel(new BorderLayout());

		this.jRB_useOwnCode_checkMethod = new JRadioButton("Use own code", false);
		this.jRB_useOwnCode_checkMethod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				that.enableSidePanel(that.leftPanel_checkMethod, false);
				that.enableSidePanel(that.rightPanel_checkMethod, true);
			}
		});

		this.buttonGroup_checkMethod.add(this.jRB_useOwnCode_checkMethod);

		panels[0]=new JPanel(new BorderLayout());
		panels[0].add(this.jRB_useOwnCode_checkMethod, BorderLayout.NORTH);

		this.rightPanel_checkMethod.add(new JLabel("Code for check method:"), BorderLayout.NORTH);

		LuposDocument document_checkJavaCode = new LuposDocument();
		this.jTP_checkJavaCode = new LuposJTextPane(document_checkJavaCode);
		document_checkJavaCode.init(JavaScanner.createILuposParser(new LuposDocumentReader(document_checkJavaCode)), true);


		JScrollPane jSP_checkJavaCode = new JScrollPane(this.jTP_checkJavaCode);

		this.rightPanel_checkMethod.add(jSP_checkJavaCode, BorderLayout.CENTER);

		JPanel innerPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		innerPanel.add(new JLabel("Start node:"));

		// build drop down menu for class names...
		this.jCB_startNode = new JComboBox(Arrays.asList(RuleEnum.class.getEnumConstants()).toArray());

		innerPanel.add(this.jCB_startNode);
		
		this.rightPanel_checkMethod.add(innerPanel, BorderLayout.SOUTH);

		panels[0].add(this.rightPanel_checkMethod, BorderLayout.CENTER);

		this.rightPanel_replaceMethod = new JPanel(new BorderLayout());

		this.jRB_useOwnCode_replaceMethod = new JRadioButton("Use own code", true);
		this.jRB_useOwnCode_replaceMethod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ee) {
				that.enableSidePanel(that.leftPanel_replaceMethod, false);
				that.enableSidePanel(that.rightPanel_replaceMethod, true);
			}
		});

		this.buttonGroup_replaceMethod.add(this.jRB_useOwnCode_replaceMethod);

		panels[1]=new JPanel(new BorderLayout());
		panels[1].add(this.jRB_useOwnCode_replaceMethod, BorderLayout.NORTH);

		this.rightPanel_replaceMethod.add(new JLabel("Code for replace method:"), BorderLayout.NORTH);

		LuposDocument document_replaceJavaCode = new LuposDocument();
		this.jTP_replaceJavaCode = new LuposJTextPane(document_replaceJavaCode);
		document_replaceJavaCode.init(JavaScanner.createILuposParser(new LuposDocumentReader(document_replaceJavaCode)), true);

		JScrollPane jSP_replaceJavaCode = new JScrollPane(this.jTP_replaceJavaCode);

		this.rightPanel_replaceMethod.add(jSP_replaceJavaCode, BorderLayout.CENTER);

		panels[1].add(this.rightPanel_replaceMethod, BorderLayout.CENTER);
		
		return panels;
	}

	private void enableSidePanel(Component comp, boolean state) {
		Container container = (Container) comp;

		comp.setEnabled(state);

		for(int i = 0; i < container.getComponentCount(); i += 1) {
			this.enableSidePanel(container.getComponent(i), state);
		}
	}

	/**
	 * <p>getAdditionalGlobalJavaCode.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getAdditionalGlobalJavaCode() {
		return this.jTP_globalJavaCode.getText();
	}

	/**
	 * <p>useGeneratedJavaCode_CheckMethod.</p>
	 *
	 * @return a boolean.
	 */
	public boolean useGeneratedJavaCode_CheckMethod() {
		return this.jRB_useGeneratedCode_checkMethod.isSelected();
	}

	/**
	 * <p>useGeneratedJavaCode_ReplaceMethod.</p>
	 *
	 * @return a boolean.
	 */
	public boolean useGeneratedJavaCode_ReplaceMethod() {
		return this.jRB_useGeneratedCode_replaceMethod.isSelected();
	}

	/**
	 * <p>getAdditionalCheckJavaCode.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getAdditionalCheckJavaCode() {
		return this.jTP_additionalCheckJavaCode.getText();
	}

	/**
	 * <p>getAdditonalReplaceJavaCode.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getAdditonalReplaceJavaCode() {
		return this.jTP_additionalReplaceJavaCode.getText();
	}

	/**
	 * <p>getCheckJavaCode.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCheckJavaCode() {
		return this.jTP_checkJavaCode.getText();
	}

	/**
	 * <p>getReplaceJavaCode.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getReplaceJavaCode() {
		return this.jTP_replaceJavaCode.getText();
	}

	/**
	 * <p>getStartNodeClass.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getStartNodeClass() {
		if(this.jRB_useGeneratedCode_checkMethod.isSelected()) {
			return "";
		}
		else {
			return ((RuleEnum) this.jCB_startNode.getSelectedItem()).getOpClass().getName();
		}
	}
	
	/**
	 * <p>getAdditionalImportDeclarations.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getAdditionalImportDeclarations(){
		return this.jTP_additionalImportDeclarations.getText();
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public JSONObject toJSON() throws JSONException {
		JSONObject checkMethodSaveObject = new JSONObject();
		checkMethodSaveObject.put("use generated code", this.jRB_useGeneratedCode_checkMethod.isSelected());
		checkMethodSaveObject.put("additional code", this.jTP_additionalCheckJavaCode.getText());
		checkMethodSaveObject.put("code", this.jTP_checkJavaCode.getText());
		checkMethodSaveObject.put("start node", this.jCB_startNode.getSelectedItem());

		JSONObject replaceMethodSaveObject = new JSONObject();
		replaceMethodSaveObject.put("use generated code", this.jRB_useGeneratedCode_replaceMethod.isSelected());
		replaceMethodSaveObject.put("additional code", this.jTP_additionalReplaceJavaCode.getText());
		replaceMethodSaveObject.put("code", this.jTP_replaceJavaCode.getText());

		JSONObject saveObject = new JSONObject();
		saveObject.put("additional import declarations", this.jTP_additionalImportDeclarations.getText());		
		saveObject.put("additional global java code", this.jTP_globalJavaCode.getText());
		saveObject.put("check method", checkMethodSaveObject);
		saveObject.put("replace method", replaceMethodSaveObject);

		return saveObject;
	}
}
