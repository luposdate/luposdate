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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.generators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.AnnotationPanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.ImplementationPanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.RuleEditorPane;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.RulePackagePanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.RulePanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.JumpOverOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.ConnectionContainer;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.File;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleContainer;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.VariableContainer;
import lupos.misc.Triple;
import lupos.misc.util.OperatorIDTuple;
public class JavaCodeGenerator {
	private RuleEditor editor = null;
	private String targetDirectory = "";
	private String packageName = "";
	private boolean generateStartMap = false;
	private HashMap<String, String> templates = new HashMap<String, String>();
	private StringBuffer globalCode = null;
	private StringBuffer checkMethodCode = null;
	private StringBuffer replaceMethodCode = null;
	private LinkedList<StringBuffer> buffers = null;
	private int count_private = 0;
	private HashMap<String, VariableContainer> variableList_left = null;
	private HashMap<String, Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>>> validatedRules = new HashMap<String, Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>>>();

	/**
	 * <p>Constructor for JavaCodeGenerator.</p>
	 *
	 * @param editor a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor} object.
	 */
	public JavaCodeGenerator(RuleEditor editor) {
		this.editor = editor;
	}

	/**
	 * <p>generate.</p>
	 *
	 * @param targetDirectory a {@link java.lang.String} object.
	 * @param packageName a {@link java.lang.String} object.
	 * @param generateStartMap a boolean.
	 */
	public void generate(String targetDirectory, String packageName, boolean generateStartMap) {
		// correct targetDirectory if needed...
		if(!targetDirectory.endsWith("/")) {
			targetDirectory += "/";
		}

		this.targetDirectory = targetDirectory;


		// handle package name...
		this.packageName = (!packageName.equals("")) ? "package " + packageName + ";" : "";


		this.generateStartMap = generateStartMap;


		System.out.println("Starting to generate java code...");

		this.generate_rule_packages();
		this.generate_rules();

		System.out.println("DONE");
	}

	private void generate_rule_packages() {
		String rulePackageBaseClassName = (this.generateStartMap) ? "RulePackageWithStartNodeMap" : "RulePackage";

		for(JTabbedPane rulePackagePane : this.editor.getRulePackages()) {
			RulePackagePanel rulePackage = (RulePackagePanel) rulePackagePane;
			String rulePackageName = rulePackage.toString();
			String rulePackageClassName = this.capitalizeWord(rulePackageName.replaceAll(" ", "") + "RulePackage");
			String rulePackageClassFileName = rulePackageClassName + ".java";
			RuleContainer ruleContainer = this.editor.getRuleContainer();

			System.out.println(":: generating " + rulePackageClassFileName + "...");

			StringBuffer rules = new StringBuffer();
			LinkedList<String> associatedRulePackages = this.editor.getAssociationsContainer().getAssociationsToRulePackage(rulePackageName);
			int failedRules = 0;

			for(int i = 0; i < associatedRulePackages.size(); i += 1) {
				String ruleName = associatedRulePackages.get(i).replaceAll(" ", "") + "Rule";
				Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>> resultTriple = this.validateRule(this.capitalizeWord(ruleName), ruleContainer.getRule(associatedRulePackages.get(i)));

				if(!resultTriple.getFirst()) {
					failedRules++;

					continue;
				}

				rules.append("		        new " + ruleName + "()");

				if(i != associatedRulePackages.size()-1) {
					rules.append(",\n");
				}
			}

			if(failedRules == associatedRulePackages.size()) {
				JOptionPane.showOptionDialog(this.editor, "The rule package '" + rulePackageName + "' has no rules with visual representation or code defined.\nIt will therefore be ignored in the code generation", "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);

				continue;
			}

			String content = String.format(this.getTemplate("rulePackageClass"), this.packageName, rulePackageClassName, rulePackageBaseClassName, rulePackageClassName, rules.toString());

			File.writeFile(this.targetDirectory + rulePackageClassFileName, content);
		}
	}

	private void generate_rules() {
		for(JTabbedPane rulePane : this.editor.getRules()) {
			RulePanel rule = (RulePanel) rulePane;
			String ruleClassName = this.capitalizeWord(rule.toString().replaceAll(" ", "") + "Rule");
			String ruleClassFileName = ruleClassName + ".java";
			ImplementationPanel rImplementationPanel = rule.getImplementationPanel();

			// -- validating rule - begin --
			Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>> resultTriple = this.validateRule(ruleClassName, rule);

			if(!resultTriple.getFirst()) {
				continue;
			}
			// -- validating rule - end --

			System.out.println(":: generating " + ruleClassFileName + "...");

			this.variableList_left = resultTriple.getSecond();
			this.globalCode = new StringBuffer(this.formatCode(rImplementationPanel.getAdditionalGlobalJavaCode(), "    ") + "\n");
			String additionalImportDeclarations = rImplementationPanel.getAdditionalImportDeclarations()+"\n";

			String startNodeClass = this.generate_check_method(rule);
			this.generate_replace_method(rule, resultTriple.getThird());

			String javaCode = String.format(this.getTemplate("ruleClass"), this.packageName, additionalImportDeclarations, ruleClassName, this.globalCode.toString(), ruleClassName, startNodeClass, rule.toString(), this.checkMethodCode.toString(), this.replaceMethodCode.toString());

			File.writeFile(this.targetDirectory + ruleClassFileName, javaCode);
		}
	}

	private String generate_check_method(RulePanel rule) {
		ImplementationPanel rImplementationPanel = rule.getImplementationPanel();
		RuleEditorPane rRuleEditorPane = rule.getRuleEditorPane();
		String startNodeClass = "";

		this.checkMethodCode = new StringBuffer();
		this.buffers = new LinkedList<StringBuffer>();

		if(rImplementationPanel.useGeneratedJavaCode_CheckMethod()) {
			RuleOperator startNode = rRuleEditorPane.getStartNode();
			startNodeClass = startNode.getClassType().getOpClass().getName();

			this.count_private = 0;

			int maxDimension = 0;
			StringBuffer spaces = new StringBuffer("    ");

			for(VariableContainer vc : this.variableList_left.values()) {
				this.globalCode.append(vc.declate_variable(spaces, true));

				maxDimension = Math.max(maxDimension, vc.getDimension());
			}

			for(int i = 0; i < maxDimension; i += 1) {
				this.globalCode.append("    private int _dim_" + i + " = -1;\n");
			}

			spaces = this.generate_checkPrivate(startNode, new HashSet<AbstractRuleOperator>(), new HashMap<Operator, HashSet<Operator>>(), 0);

			for(StringBuffer buffer : this.buffers) {
				this.globalCode.append(buffer);
			}

			if(!rImplementationPanel.getAdditionalCheckJavaCode().equals("")) {
				spaces.append("    ");

				this.checkMethodCode.append(
						spaces + "boolean _result = this._checkPrivate0(_op);\n" +
						"\n" +
						spaces + "if(_result) {\n" +
						spaces + "    // additional check method code...\n" +
						this.formatCode(rImplementationPanel.getAdditionalCheckJavaCode(), spaces.toString() + "    ").toString() + "\n" +
						spaces + "}\n" +
						"\n" +
						spaces + "return _result;"
				);
			}
			else {
				this.checkMethodCode.append(spaces + "    return this._checkPrivate0(_op);");
			}
		}
		else {
			startNodeClass = rImplementationPanel.getStartNodeClass();
			this.checkMethodCode.append(this.formatCode(rImplementationPanel.getCheckJavaCode(), "        ").toString());
		}

		return startNodeClass;
	}

	private StringBuffer generate_checkPrivate(AbstractRuleOperator startNode, HashSet<AbstractRuleOperator> visitedNodes, HashMap<Operator, HashSet<Operator>> visitedConnections, int currentDimension) {
		StringBuffer spaces = new StringBuffer("    ");
		StringBuffer buffer = new StringBuffer();
		this.buffers.add(buffer);

		buffer.append(
				"\n" +
				spaces + "private boolean _checkPrivate" + this.count_private + "(BasicOperator _op) {\n"
		);

		spaces.append("    ");

		this.count_private++;

		int bracesCount = this.manage_node(spaces, buffer, startNode, "_op", visitedNodes, visitedConnections, 0, currentDimension, "");

		buffer.append(
				"\n" +
				spaces + "return true;\n"
		);

		for(int i = 0; i < bracesCount; i += 1) {
			spaces.delete(spaces.length()-4, spaces.length());

			buffer.append(spaces + "}\n");
		}

		if(bracesCount > 0) {
			buffer.append(
					"\n" +
					spaces + "return false;\n"
			);
		}

		spaces.delete(spaces.length()-4, spaces.length());

		buffer.append(
				spaces + "}\n"
		);

		return spaces;
	}

	@SuppressWarnings("unchecked")
	private void generate_replace_method(RulePanel rule, HashMap<String, VariableContainer> variableList_right) {
		ImplementationPanel rImplementationPanel = rule.getImplementationPanel();
		RuleEditorPane rRuleEditorPane = rule.getRuleEditorPane();

		this.replaceMethodCode = new StringBuffer();
		StringBuffer spaces = new StringBuffer("        ");

		if(rImplementationPanel.useGeneratedJavaCode_ReplaceMethod()) {
			VisualGraph<Operator> leftGraph = rRuleEditorPane.getVisualGraphs().get(0);
			VisualGraph<Operator> rightGraph = rRuleEditorPane.getVisualGraphs().get(1);

			HashMap<String, AbstractRuleOperator> leftOperatorsMap = this.getOperatorsMap(leftGraph);
			HashMap<String, AbstractRuleOperator> rightOperatorsMap = this.getOperatorsMap(rightGraph);

			HashSet<String> leftOperators = new HashSet<String>(leftOperatorsMap.keySet());
			HashSet<String> rightOperators = new HashSet<String>(rightOperatorsMap.keySet());

			HashSet<String> originalLeftOperators = (HashSet<String>) leftOperators.clone();

			leftOperators.removeAll(rightOperators); // determine operators which are only on the left side
			rightOperators.removeAll(originalLeftOperators); // determine operators which are only on the right side


			LinkedList<HashSet<ConnectionContainer>> connections = rRuleEditorPane.getConnections();
			HashSet<ConnectionContainer> leftConnections = connections.get(0);
			HashSet<ConnectionContainer> rightConnections = connections.get(1);

			HashSet<ConnectionContainer> originalLeftConnections = (HashSet<ConnectionContainer>) leftConnections.clone();

			leftConnections.removeAll(rightConnections); // determine connections which are only on the left side
			rightConnections.removeAll(originalLeftConnections); // determine connections which are only on the right side


			// --- remove all connections that only occur on the left side - begin ---
			this.replaceMethodCode.append(
					spaces + "// remove obsolete connections...\n"
			);

			for(ConnectionContainer conn : leftConnections) {
				AbstractRuleOperator parentOp = conn.getParent();
				AbstractRuleOperator childOp = conn.getChild();
				String parentName = this.getJumpOverName(parentOp, false);
				String childName = this.getJumpOverName(childOp, true);

				String opIDLabel = conn.getOpIDLabel();
				boolean hasLabel = conn.getIsActive() && !opIDLabel.equals("");
				StringBuffer labelBrackets = new StringBuffer();

				String succeedingString = null;
				int dim = 0;
				StringBuffer arrayAccess = new StringBuffer();

				switch(conn.getMode()) {
				case ALL_PRECEDING:
					dim = this.variableList_left.get(childName).getDimension();

					if(hasLabel) {
						for(int i = 0; i <= dim; i += 1) {
							labelBrackets.append("[]");
						}

						this.replaceMethodCode.append(
								spaces + "int" + labelBrackets + " _label_" + opIDLabel + " = null;\n" +
								"\n"
						);
					}

					for(int i = 0; i < dim; i += 1) {
						if(hasLabel) {
							labelBrackets.delete(labelBrackets.length()-2, labelBrackets.length());

							this.replaceMethodCode.append(
									spaces + "_label_" + opIDLabel + arrayAccess + " = new int[this." + childName + ".length]" + labelBrackets + ";\n" +
									"\n"
							);
						}

						this.replaceMethodCode.append(spaces + "for(this._dim_" + i + " = 0; this._dim_" + i + " < this." + childName + ".length; this._dim_" + i + " += 1) {\n");

						arrayAccess.append("[this._dim_" + i + "]");

						spaces.append("    ");
					}

					succeedingString = (conn.getIsActive() && conn.getOpID() != -1) ? "new OperatorIDTuple(this." + childName + arrayAccess + ", " + conn.getOpID() + ")" : "this." + childName + arrayAccess;

					if(hasLabel) {
						labelBrackets.delete(labelBrackets.length()-2, labelBrackets.length());

						this.replaceMethodCode.append(
								spaces + "int _label_" + opIDLabel + "_count" + " = 0;\n" +
								spaces + "_label_" + opIDLabel + arrayAccess + " = new int[this." + parentName + arrayAccess + ".length]" + labelBrackets + ";\n" +
								"\n"
						);
					}

					this.replaceMethodCode.append(spaces + "for(" + parentOp.getClassType().getOpClass().getName() + " _parent : this." + parentName + arrayAccess + ") {\n");

					if(hasLabel) {
						this.replaceMethodCode.append(
								spaces + "    _label_" + opIDLabel + arrayAccess + "[_label_" + opIDLabel + "_count] = _parent.getOperatorIDTuple(this." + childName + arrayAccess + ").getId();\n" +
								spaces + "    _label_" + opIDLabel + "_count += 1;\n" +
								"\n"
						);
					}

					this.replaceMethodCode.append(
							spaces + "    _parent.removeSucceedingOperator(" + succeedingString + ");\n" +
							spaces + "    this." + childName + arrayAccess + ".removePrecedingOperator(_parent);\n" +
							spaces + "}\n" +
							"\n"
					);

					break;

				case ALL_SUCCEEDING:
					dim = this.variableList_left.get(parentName).getDimension();

					if(hasLabel) {
						for(int i = 0; i <= dim; i += 1) {
							labelBrackets.append("[]");
						}

						this.replaceMethodCode.append(
								spaces + "int" + labelBrackets + " _label_" + opIDLabel + " = null;\n" +
								"\n"
						);
					}

					for(int i = 0; i < dim; i += 1) {
						if(hasLabel) {
							labelBrackets.delete(labelBrackets.length()-2, labelBrackets.length());

							this.replaceMethodCode.append(
									spaces + "_label_" + opIDLabel + arrayAccess + " = new int[this." + childName + ".length]" + labelBrackets + ";\n" +
									"\n"
							);
						}

						this.replaceMethodCode.append(spaces + "for(this._dim_" + i + " = 0; this._dim_" + i + " < this." + parentName + ".length; this._dim_" + i + " += 1) {\n");

						arrayAccess.append("[this._dim_" + i + "]");

						spaces.append("    ");
					}

					succeedingString = (conn.getIsActive() && conn.getOpID() != -1) ? "new OperatorIDTuple(_child, " + conn.getOpID() + ")" : "_child";

					if(hasLabel) {
						labelBrackets.delete(labelBrackets.length()-2, labelBrackets.length());

						this.replaceMethodCode.append(
								spaces + "int _label_" + opIDLabel + "_count = 0;\n" +
								spaces + "_label_" + opIDLabel + arrayAccess + " = new int[this." + childName + arrayAccess + ".length]" + labelBrackets + ";\n" +
								"\n"
						);
					}

					this.replaceMethodCode.append(spaces + "for(" + childOp.getClassType().getOpClass().getName() + " _child : this." + childName + arrayAccess + ") {\n");

					if(hasLabel) {
						this.replaceMethodCode.append(
								spaces + "    _label_" + opIDLabel + arrayAccess + "[_label_" + opIDLabel + "_count] = this." + parentName + arrayAccess + ".getOperatorIDTuple(_child).getId();\n" +
								spaces + "    _label_" + opIDLabel + "_count += 1;\n" +
								"\n"
						);
					}

					this.replaceMethodCode.append(
							spaces + "    this." + parentName + arrayAccess + ".removeSucceedingOperator(" + succeedingString + ");\n" +
							spaces + "    _child.removePrecedingOperator(this." + parentName + arrayAccess + ");\n" +
							spaces + "}\n" +
							"\n"
					);

					break;

				default:
					dim = this.variableList_left.get(parentName).getDimension();

					if(hasLabel) {
						for(int i = 0; i <= dim; i += 1) {
							labelBrackets.append("[]");
						}

						this.replaceMethodCode.append(
								spaces + "int" + labelBrackets + " _label_" + opIDLabel + " = null;\n" +
								"\n"
						);
					}

					for(int i = 0; i < dim; i += 1) {
						if(hasLabel) {
							labelBrackets.delete(labelBrackets.length()-2, labelBrackets.length());

							this.replaceMethodCode.append(
									spaces + "_label_" + opIDLabel + arrayAccess + " = new int[this." + childName + ".length]" + labelBrackets + ";\n" +
									"\n"
							);
						}

						this.replaceMethodCode.append(spaces + "for(this._dim_" + i + " = 0; this._dim_" + i + " < this." + parentName + ".length; this._dim_" + i + " += 1) {\n");

						arrayAccess.append("[this._dim_" + i + "]");

						spaces.append("    ");
					}

					succeedingString = (conn.getIsActive() && conn.getOpID() != -1) ? "new OperatorIDTuple(this." + childName + arrayAccess + ", " + conn.getOpID() + ")" : "this." + childName + arrayAccess;

					if(hasLabel) {
						labelBrackets.delete(labelBrackets.length()-2, labelBrackets.length());

						this.replaceMethodCode.append(
								spaces + "    _label_" + opIDLabel + arrayAccess + " = this." + parentName + arrayAccess + ".getOperatorIDTuple(this." + childName + arrayAccess + ").getId();\n" +
								"\n"
						);
					}

					this.replaceMethodCode.append(
							spaces + "this." + parentName + arrayAccess + ".removeSucceedingOperator(" + succeedingString + ");\n" +
							spaces + "this." + childName + arrayAccess + ".removePrecedingOperator(this." + parentName + arrayAccess + ");\n"
					);

					break;
				}

				for(int i = 0; i < dim; i += 1) {
					spaces.delete(spaces.length()-4, spaces.length());

					this.replaceMethodCode.append(spaces + "}\n");
				}
			}
			// --- remove all connections that only occur on the left side - end ---

			// --- add operators that only occur on the right side - begin ---
			this.replaceMethodCode.append(
					"\n" +
					spaces + "// add new operators...\n"
			);

			for(String opName : rightOperators) {
				AbstractRuleOperator op = rightOperatorsMap.get(opName);
				String opClass = op.getClassType().getOpClass().getName();
				VariableContainer vc = variableList_right.get(opName);
				int dim = vc.getDimension();
				StringBuffer arrayAccess = new StringBuffer();

				for(int i = 0; i < dim; i += 1) {
					arrayAccess.append("[]");
				}

				this.replaceMethodCode.append(spaces + opClass + arrayAccess + " " + opName + " = null;\n");

				arrayAccess = new StringBuffer();

				for(int i = 0; i < dim; i += 1) {
					this.replaceMethodCode.append(
							vc.initiate_next_dimension(spaces, i, this.getOpName(vc.getCountProvider(), rightOperators, true) + arrayAccess + ".length", false) +
							"\n" +
							spaces + "for(this._dim_" + i + " = 0; this._dim_" + i + " < " + opName + arrayAccess + ".length; this._dim_" + i + " += 1) {\n"
					);

					spaces.append("    ");
					arrayAccess.append("[this._dim_" + i + "]");
				}

				this.replaceMethodCode.append(spaces + opName + arrayAccess + " = new " + opClass + "();\n");

				if(this.generateStartMap) {
					this.replaceMethodCode.append(spaces + "this.addNodeToStartNodeMap(" + opName + arrayAccess + ", _startNodes);\n");
				}

				for(int i = 0; i < dim; i += 1) {
					spaces.delete(spaces.length()-4, spaces.length());

					this.replaceMethodCode.append(spaces + "}\n");
				}
			}

			this.replaceMethodCode.append("\n");
			// --- add operators that only occur on the right side - end ---

			// --- add connections that only occur on the right side - begin ---
			this.replaceMethodCode.append(
					"\n" +
					spaces + "// add new connections...\n"
			);

			for(ConnectionContainer conn : rightConnections) {
				AbstractRuleOperator parentOp = conn.getParent();
				AbstractRuleOperator childOp = conn.getChild();
				String parentName = this.getOpName(parentOp, rightOperators, false);
				String childName = this.getOpName(childOp, rightOperators, true);
				VariableContainer parentVC = variableList_right.get(this.getJumpOverName(parentOp, false));
				VariableContainer childVC = variableList_right.get(this.getJumpOverName(childOp, true));

				String opIDLabel = conn.getOpIDLabel();
				boolean hasLabel = conn.getIsActive() && !opIDLabel.equals("");

				int dim = 0;
				StringBuffer arrayAccess = new StringBuffer();
				String succeedingString = "";

				switch(conn.getMode()) {
				case ALL_PRECEDING:
					dim = childVC.getDimension();

					for(int i = 0; i < dim; i += 1) {
						this.replaceMethodCode.append(spaces + "for(this._dim_" + i + " = 0; this._dim_" + i + " < " + childName + ".length; this._dim_" + i + " += 1) {\n");

						arrayAccess.append("[this._dim_" + i + "]");

						spaces.append("    ");
					}

					if(conn.getIsActive()) {
						if(hasLabel) {
							succeedingString = "new OperatorIDTuple(" + childName + arrayAccess + ", _label_" + conn.getOpIDLabel() + arrayAccess + "[_label_" + opIDLabel + "_count])";

							this.replaceMethodCode.append(
									spaces + "_label_" + opIDLabel + "_count = 0;\n" +
									"\n"
							);
						}
						else {
							succeedingString = "new OperatorIDTuple(" + childName + arrayAccess + ", " + conn.getOpID() + ")";
						}
					}
					else {
						succeedingString = childName + arrayAccess;
					}

					this.replaceMethodCode.append(
							spaces + "for(" + parentOp.getClassType().getOpClass().getName() + " _parent : " + parentName + arrayAccess + ") {\n" +
							spaces + "    _parent.addSucceedingOperator(" + succeedingString + ");\n" +
							spaces + "    " + childName + arrayAccess + ".addPrecedingOperator(_parent);\n"
					);

					if(hasLabel) {
						this.replaceMethodCode.append(
								"\n" +
								spaces + "    _label_" + opIDLabel + "_count += 1;\n"
						);
					}

					this.replaceMethodCode.append(
							spaces + "}\n" +
							"\n"
					);

					break;

				case ALL_SUCCEEDING:
					dim = parentVC.getDimension();

					for(int i = 0; i < dim; i += 1) {
						this.replaceMethodCode.append(spaces + "for(this._dim_" + i + " = 0; this._dim_" + i + " < " + parentName + ".length; this._dim_" + i + " += 1) {\n");

						arrayAccess.append("[this._dim_" + i + "]");

						spaces.append("    ");
					}

					if(conn.getIsActive()) {
						if(hasLabel) {
							succeedingString = "new OperatorIDTuple(_child, _label_" + conn.getOpIDLabel() + arrayAccess + "[_label_" + opIDLabel + "_count])";

							this.replaceMethodCode.append(
									spaces + "_label_" + opIDLabel + "_count = 0;\n" +
									"\n"
							);
						}
						else {
							succeedingString = "new OperatorIDTuple(_child, " + conn.getOpID() + ")";
						}
					}
					else {
						succeedingString = "_child";
					}

					this.replaceMethodCode.append(
							spaces + "for(" + childOp.getClassType().getOpClass().getName() + " _child : " + childName + arrayAccess + ") {\n" +
							spaces + "    " + parentName + arrayAccess + ".addSucceedingOperator(" + succeedingString + ");\n" +
							spaces + "    _child.addPrecedingOperator(" + parentName + arrayAccess + ");\n"
					);

					if(hasLabel) {
						this.replaceMethodCode.append(
								"\n" +
								spaces + "    _label_" + opIDLabel + "_count += 1;\n"
						);
					}

					this.replaceMethodCode.append(
							spaces + "}\n" +
							"\n"
					);

					break;

				default:
					dim = parentVC.getDimension();

					for(int i = 0; i < dim; i += 1) {
						this.replaceMethodCode.append(spaces + "for(this._dim_" + i + " = 0; this._dim_" + i + " < " + parentName + ".length; this._dim_" + i + " += 1) {\n");

						arrayAccess.append("[this._dim_" + i + "]");

						spaces.append("    ");
					}

					if(conn.getIsActive()) {
						if(hasLabel) {
							succeedingString = "new OperatorIDTuple(" + childName + arrayAccess + ", _label_" + conn.getOpIDLabel() + arrayAccess + ")";
						}
						else {
							succeedingString = "new OperatorIDTuple(" + childName + arrayAccess + ", " + conn.getOpID() + ")";
						}
					}
					else {
						succeedingString = childName + arrayAccess;
					}

					this.replaceMethodCode.append(
							spaces + parentName + arrayAccess + ".addSucceedingOperator(" + succeedingString + ");\n" +
							spaces + childName + arrayAccess + ".addPrecedingOperator(" + parentName + arrayAccess + ");\n"
					);
				}

				for(int i = 0; i < dim; i += 1) {
					spaces.delete(spaces.length()-4, spaces.length());

					this.replaceMethodCode.append(spaces + "}\n");
				}

				this.replaceMethodCode.append("\n");
			}
			// --- add connections that only occur on the right side - begin ---

			// --- delete operators from left side which are not needed anymore - begin ---
			if(leftOperators.size() > 0) {
				this.replaceMethodCode.append(
						"\n" +
						spaces + "// delete unreachable operators...\n"
				);

				for(String opName : leftOperators) {
					int dim = this.variableList_left.get(opName).getDimension();
					StringBuffer arrayAccess = new StringBuffer();

					for(int i = 0; i < dim; i += 1) {
						this.replaceMethodCode.append(spaces + "for(this._dim_" + i + " = 0; this._dim_" + i + " < " + opName + ".length; this._dim_" + i + " += 1) {\n");

						arrayAccess.append("[this._dim_" + i + "]");

						spaces.append("    ");
					}

					this.replaceMethodCode.append(spaces + "this.deleteOperatorWithoutParentsRecursive(this." + opName + arrayAccess + ", _startNodes);\n");

					for(int i = 0; i < dim; i += 1) {
						spaces.delete(spaces.length()-4, spaces.length());

						this.replaceMethodCode.append(spaces + "}\n");
					}

					if(dim > 0) {
						this.replaceMethodCode.append("\n");
					}
				}

				this.replaceMethodCode.append("\n");
			}
			// --- delete operators from left side which are not needed anymore - end ---

			this.replaceMethodCode.append(
					"\n" +
					spaces + "// additional replace method code...\n" +
					this.formatCode(rImplementationPanel.getAdditonalReplaceJavaCode(), spaces.toString()).toString()
			);
		}
		else {
			this.replaceMethodCode.append(this.formatCode(rImplementationPanel.getReplaceJavaCode(), spaces.toString()).toString());
		}
	}

	private HashMap<String, AbstractRuleOperator> getOperatorsMap(VisualGraph<Operator> vg) {
		HashMap<String, AbstractRuleOperator> operators = new HashMap<String, AbstractRuleOperator>();

		for(GraphWrapper gw : vg.getBoxes().keySet()) {
			AbstractRuleOperator op = (AbstractRuleOperator) gw.getElement();

			operators.put(op.getName(), op);
		}

		return operators;
	}

	private String getOpName(AbstractRuleOperator op, HashSet<String> opList) {
		String opName = op.getName();

		if(!opList.contains(opName)) {
			return "this." + opName;
		}
		else {
			return opName;
		}
	}

	private String getOpName(AbstractRuleOperator op, HashSet<String> opList, boolean begin) {
		String opName = this.getOpName(op, opList);

		if(op.getClass() == JumpOverOperator.class) {
			opName += (begin) ? "_begin" : "_end";
		}

		return opName;
	}

	private String getJumpOverName(AbstractRuleOperator op, boolean begin) {
		String opName = op.getName();

		if(op.getClass() == JumpOverOperator.class) {
			opName += (begin) ? "_begin" : "_end";
		}

		return opName;
	}


	private int manage_node(StringBuffer spaces, StringBuffer buffer, AbstractRuleOperator node, String nodeName, HashSet<AbstractRuleOperator> visitedNodes, HashMap<Operator, HashSet<Operator>> visitedConnections, int runNumber, int currentDimension, String testName) {
		runNumber++;

		int bracesCount = 0;
		String originalNodeName = nodeName;

		if(visitedNodes.contains(node)) {
			return bracesCount;
		}

		visitedNodes.add(node);

		// --- compare nodes - begin ---
		if(node.getClass() != JumpOverOperator.class) {
			String ruleNodeClass = node.getClassType().getOpClass().getName();

			if(node.alsoSubClasses()) {
				buffer.append(spaces + "if(!("+ nodeName + " instanceof " + ruleNodeClass + ")) {\n");
			}
			else {
				buffer.append(spaces + "if("+ nodeName + ".getClass() != " + ruleNodeClass + ".class) {\n");
			}

			buffer.append(
					spaces + "    " + ((runNumber != 1) ? "continue" : "return false") + ";\n" +
					spaces + "}\n"
			);
		}

		String opName = node.getName();

		if(node.getClass() != JumpOverOperator.class) {
			buffer.append(
					"\n" +
					spaces + this.variableList_left.get(opName).assign_variable(nodeName)
			);
		}
		// --- compare nodes - end ---


		LinkedList<Operator> precedingOperators = node.getPrecedingOperators();

		if(precedingOperators.size() > 0) {
			boolean added = false;
			int precedingIndex = 0;

			for(int i = 0; i < precedingOperators.size(); i += 1) {
				AbstractRuleOperator precOp = (AbstractRuleOperator) precedingOperators.get(i);


				HashSet<Operator> connectionNodes = visitedConnections.get(precOp);

				if(connectionNodes == null) {
					connectionNodes = new HashSet<Operator>();
					visitedConnections.put(precOp, connectionNodes);
				}
				else if(connectionNodes.contains(node)) {
					continue;
				}

				connectionNodes.add(node);


				if(!added) {
					buffer.append(
							"\n" +
							spaces + "List<BasicOperator> _precedingOperators_" + runNumber + "_" + i + " = " + nodeName + ".getPrecedingOperators();\n" +
							"\n"
					);

					added = true;
					precedingIndex = i;
				}


				AnnotationPanel panel = (AnnotationPanel) precOp.getAnnotationLabel(node);

				switch(panel.getMode()) {
				case ONLY_PRECEDING_AND_SUCCEEDING:
				case ONLY_PRECEDING:
					if(node.getClass() == JumpOverOperator.class) {
						buffer.append(
								spaces + "if(" + testName + " != " + originalNodeName + ") {\n"
						);

						spaces.append("    ");
					}

					buffer.append(
							spaces + "if(_precedingOperators_" + runNumber + "_" + precedingIndex + ".size() != 1) {\n" +
							spaces + "    " + ((runNumber != 1) ? "continue" : "return false") + ";\n" +
							spaces + "}\n"
					);

					if(node.getClass() == JumpOverOperator.class) {
						spaces.delete(spaces.length()-4, spaces.length());

						buffer.append(spaces + "}\n");
					}

					break;

				case ALL_PRECEDING:
					buffer.append(
							"\n" +
							spaces + "this._dim_" + currentDimension + " = -1;\n"
					);

					for(String variable : this.get_variables_for_next_dimension(precOp, true)) {
						VariableContainer vc = this.variableList_left.get(variable);

						buffer.append(vc.initiate_next_dimension(spaces, currentDimension, "_precedingOperators_" + runNumber + "_" + precedingIndex + ".size()", true));
					}

					break;
				}

				buffer.append(
						"\n" +
						spaces + "for(BasicOperator _precOp_" + runNumber + "_" + i + " : _precedingOperators_" + runNumber + "_" + precedingIndex + ") {\n"
				);

				bracesCount++;
				spaces.append("    ");

				switch(panel.getMode()) {
				case ALL_PRECEDING:
					buffer.append(
							spaces + "this._dim_" + currentDimension + " += 1;\n" +
							"\n"
					);
					break;
				}

				if(panel.isActive()) {
					int opID = panel.getOpID();

					if(opID != -1) {
						buffer.append(
								spaces + "if(_precOp_" + runNumber + "_" + i + ".getOperatorIDTuple(" + nodeName + ").getId() != " + opID + ") {\n" +
								spaces + "    continue;\n" +
								spaces + "}\n" +
								"\n"
						);
					}
				}

				String nextOpName = "_precOp_" + runNumber + "_" + i;
				testName = "";

				switch(panel.getMode()) {
				default:
				case EXISTS:
					if(precOp.getClass() == JumpOverOperator.class) {
						String[] data = this.handleJumpOverOperator(spaces, buffer, (JumpOverOperator) precOp, nextOpName, runNumber, 0, true);
						nextOpName = data[0];
						testName = data[1];
					}

					bracesCount += this.manage_node(spaces, buffer, precOp, nextOpName, visitedNodes, visitedConnections, runNumber, currentDimension, testName);

					break;
				case ALL_PRECEDING:
					buffer.append(
							spaces + "if(!this._checkPrivate" + this.count_private + "(" + nextOpName + ")) {\n" +
							spaces + "    return false;\n" +
							spaces + "}\n"
					);

					this.generate_checkPrivate(precOp, visitedNodes, visitedConnections, currentDimension+1);

					spaces.delete(spaces.length()-4, spaces.length());

					buffer.append(spaces + "}\n");
					bracesCount -= 1;

					break;
				case ALL_SUCCEEDING:
					System.err.println("WARNING: This case should never happen!");

					break;
				case ONLY_PRECEDING_AND_SUCCEEDING:
				case ONLY_SUCCEEDING:
					buffer.append(
							spaces + "if(" + nextOpName + ".getSucceedingOperators().size() != 1) {\n" +
							spaces + "    break;\n" +
							spaces + "}\n" +
							"\n"
					);

					if(precOp.getClass() == JumpOverOperator.class) {
						String[] data = this.handleJumpOverOperator(spaces, buffer, (JumpOverOperator) precOp, nextOpName, runNumber, 0, true);
						nextOpName = data[0];
						testName = data[1];
					}

					bracesCount += this.manage_node(spaces, buffer, precOp, nextOpName, visitedNodes, visitedConnections, runNumber, currentDimension, testName);

					break;
				}
			}
		}


		LinkedList<OperatorIDTuple<Operator>> succedingOperators = node.getSucceedingOperators();

		if(succedingOperators.size() > 0) {
			boolean added = false;
			int succedingIndex = 0;

			for(int i = 0; i < succedingOperators.size(); i += 1) {
				OperatorIDTuple<Operator> sucOpIDTup = succedingOperators.get(i);
				AbstractRuleOperator sucOp = (AbstractRuleOperator) sucOpIDTup.getOperator();


				HashSet<Operator> connectionNodes = visitedConnections.get(node);

				if(connectionNodes == null) {
					connectionNodes = new HashSet<Operator>();
					visitedConnections.put(node, connectionNodes);
				}
				else if(connectionNodes.contains(sucOp)) {
					continue;
				}

				connectionNodes.add(sucOp);


				if(!added) {
					buffer.append(
							"\n" +
							spaces + "List<OperatorIDTuple> _succedingOperators_" + runNumber + "_" + i + " = " + nodeName + ".getSucceedingOperators();\n" +
							"\n"
					);

					added = true;
					succedingIndex = i;
				}


				AnnotationPanel panel = (AnnotationPanel) node.getAnnotationLabel(sucOp);

				switch(panel.getMode()) {
				case ONLY_PRECEDING_AND_SUCCEEDING:
				case ONLY_SUCCEEDING:
					buffer.append(
							spaces + "if(_succedingOperators_" + runNumber + "_" + succedingIndex + ".size() != 1) {\n" +
							spaces + "    " + ((runNumber != 1) ? "continue" : "return false") + ";\n" +
							spaces + "}\n"
					);

					break;


				case ALL_SUCCEEDING:
					buffer.append(
							"\n" +
							spaces + "this._dim_" + currentDimension + " = -1;\n"
					);

					for(String variable : this.get_variables_for_next_dimension(sucOp, false)) {
						VariableContainer vc = this.variableList_left.get(variable);

						buffer.append(vc.initiate_next_dimension(spaces, currentDimension, "_succedingOperators_" + runNumber + "_" + succedingIndex + ".size()", true));
					}

					break;
				}


				buffer.append(
						"\n" +
						spaces + "for(OperatorIDTuple _sucOpIDTup_" + runNumber + "_" + i + " : _succedingOperators_" + runNumber + "_" + succedingIndex + ") {\n"
				);

				bracesCount++;
				spaces.append("    ");

				switch(panel.getMode()) {
				case ALL_SUCCEEDING:
					buffer.append(
							spaces + "this._dim_" + currentDimension + " += 1;\n" +
							"\n"
					);
					break;
				}


				if(panel.isActive()) {
					int opID = panel.getOpID();

					if(opID != -1) {
						buffer.append(
								spaces + "if(_sucOpIDTup_" + runNumber + "_" + i + ".getId() != " + opID + ") {\n" +
								spaces + "    continue;\n" +
								spaces + "}\n" +
								"\n"
						);
					}
				}

				String nextOpName = "_sucOpIDTup_" + runNumber + "_" + i + ".getOperator()";
				testName = "";

				switch(panel.getMode()) {
				default:
				case EXISTS:
					if(sucOp.getClass() == JumpOverOperator.class) {
						String[] data = this.handleJumpOverOperator(spaces, buffer, (JumpOverOperator) sucOp, nextOpName, runNumber, 0, false);
						nextOpName = data[0];
						testName = data[1];
					}

					bracesCount += this.manage_node(spaces, buffer, sucOp, nextOpName, visitedNodes, visitedConnections, runNumber, currentDimension, testName);

					break;
				case ALL_SUCCEEDING:
					buffer.append(
							spaces + "if(!this._checkPrivate" + this.count_private + "(" + nextOpName + ")) {\n" +
							spaces + "    return false;\n" +
							spaces + "}\n"
					);

					this.generate_checkPrivate(sucOp, visitedNodes, visitedConnections, currentDimension+1);

					spaces.delete(spaces.length()-4, spaces.length());

					buffer.append(spaces + "}\n");
					bracesCount -= 1;

					break;
				case ALL_PRECEDING:
					System.err.println("WARNING: This case should never happen!");

					break;
				case ONLY_PRECEDING_AND_SUCCEEDING:
				case ONLY_PRECEDING:
					buffer.append(
							spaces + "if(" + nextOpName + ".getPrecedingOperators().size() != 1) {\n" +
							spaces + "    break;\n" +
							spaces + "}\n" +
							"\n"
					);

					if(sucOp.getClass() == JumpOverOperator.class) {
						String[] data = this.handleJumpOverOperator(spaces, buffer, (JumpOverOperator) sucOp, nextOpName, runNumber, 0, false);
						nextOpName = data[0];
						testName = data[1];
					}

					bracesCount += this.manage_node(spaces, buffer, sucOp, nextOpName, visitedNodes, visitedConnections, runNumber, currentDimension, testName);

					break;
				}
			}
		}

		return bracesCount;
	}

	private HashSet<String> get_variables_for_next_dimension(AbstractRuleOperator node, boolean preceding) {
		HashSet<String> variables = new HashSet<String>();

		this.get_variables_for_next_dimension(node, preceding, variables, new HashSet<AbstractRuleOperator>());

		return variables;
	}

	private void get_variables_for_next_dimension(AbstractRuleOperator node, boolean preceding, HashSet<String> variables, HashSet<AbstractRuleOperator> visitedNodes) {
		if(visitedNodes.contains(node)) {
			return;
		}

		visitedNodes.add(node);


		String variableName = node.getName();

		if(node.getClass() != JumpOverOperator.class) {
			variables.add(variableName);
		}
		else {
			variables.add(variableName + "_begin");
			variables.add(variableName + "_end");
		}


		if(preceding) {
			for(Operator precOp : node.getPrecedingOperators()) {
				this.get_variables_for_next_dimension((AbstractRuleOperator) precOp, preceding, variables, visitedNodes);
			}
		}
		else {
			for(OperatorIDTuple<Operator> sucIDt : node.getSucceedingOperators()) {
				this.get_variables_for_next_dimension((AbstractRuleOperator) sucIDt.getOperator(), preceding, variables, visitedNodes);
			}
		}
	}

	private String[] handleJumpOverOperator(StringBuffer spaces, StringBuffer buffer, JumpOverOperator jumpOp, String opName, int runNumber, int i, boolean preceding) {
		String cardinality = jumpOp.getCardinality();

		StringBuffer condition = new StringBuffer("(");
		String getNextString = "";

		buffer.append(spaces + "// --- handle JumpOver - begin ---\n");

		if(preceding) {
			getNextString = ".getPrecedingOperators().get(0)";

			buffer.append(spaces + this.variableList_left.get(jumpOp.getName() + "_end").assign_variable(opName));

			for(Operator tmpOp : jumpOp.getPrecedingOperators()) {
				AbstractRuleOperator tmpOp2 = (AbstractRuleOperator) tmpOp;

				if(tmpOp2.alsoSubClasses()) {
					Class<?> clazz = jumpOp.getClassType().getOpClass().getSuperclass();

					boolean moreGeneric = false;

					while(clazz != Object.class) {
						if(clazz == tmpOp2.getClassType().getOpClass()) {
							moreGeneric = true;
							break;
						}

						clazz = clazz.getSuperclass();
					}

					if(moreGeneric) {
						if(jumpOp.alsoSubClasses()) {
							condition.append("_searchIndex_" + runNumber + "_" + i + " instanceof " + jumpOp.getClassType().getOpClass().getName());
						}
						else {
							condition.append("_searchIndex_" + runNumber + "_" + i + ".getClass() == " + jumpOp.getClassType().getOpClass().getName() + ".class");
						}
					}
					else {
						condition.append("!(_searchIndex_" + runNumber + "_" + i + " instanceof " + tmpOp2.getClassType().getOpClass().getName() + ")");
					}
				}
				else {
					condition.append("_searchIndex_" + runNumber + "_" + i + ".getClass() != " + tmpOp2.getClassType().getOpClass().getName() + ".class");
				}

				condition.append(" || ");
			}
		}
		else {
			getNextString = ".getSucceedingOperators().get(0).getOperator()";

			buffer.append(spaces + this.variableList_left.get(jumpOp.getName() + "_begin").assign_variable(opName));

			for(OperatorIDTuple<Operator> tmpOp : jumpOp.getSucceedingOperators()) {
				AbstractRuleOperator tmpOp2 = (AbstractRuleOperator) tmpOp.getOperator();

				if(tmpOp2.alsoSubClasses()) {
					Class<?> clazz = jumpOp.getClassType().getOpClass().getSuperclass();

					boolean moreGeneric = false;

					while(clazz != Object.class) {
						if(clazz == tmpOp2.getClassType().getOpClass()) {
							moreGeneric = true;
							break;
						}

						clazz = clazz.getSuperclass();
					}

					if(moreGeneric) {
						if(jumpOp.alsoSubClasses()) {
							condition.append("_searchIndex_" + runNumber + "_" + i + " instanceof " + jumpOp.getClassType().getOpClass().getName());
						}
						else {
							condition.append("_searchIndex_" + runNumber + "_" + i + ".getClass() == " + jumpOp.getClassType().getOpClass().getName() + ".class");
						}
					}
					else {
						condition.append("!(_searchIndex_" + runNumber + "_" + i + " instanceof " + tmpOp2.getClassType().getOpClass().getName() + ")");
					}
				}
				else {
					condition.append("_searchIndex_" + runNumber + "_" + i + ".getClass() != " + tmpOp2.getClassType().getOpClass().getName() + ".class");
				}

				condition.append(" || ");
			}
		}

		condition.delete(condition.length()-4, condition.length());

		condition.append(")");

		StringBuffer cardinalityCheckBegin = new StringBuffer();
		StringBuffer cardinalityCheckMiddle = new StringBuffer();
		StringBuffer cardinalityCheckEnd = new StringBuffer();

		buffer.append(spaces + "BasicOperator _searchIndex_" + runNumber + "_" + i + " = " + opName + ";\n");

		if(cardinality.equals("*")) {
			cardinalityCheckBegin.append(
					spaces + "boolean _continueFlag_" + runNumber + "_" + i + " = false;\n" +
					"\n" +
					spaces + "while(_searchIndex_" + runNumber + "_" + i + " != null && " + condition + ") {\n"
			);

			cardinalityCheckMiddle.append(
					spaces + "        _continueFlag_" + runNumber + "_" + i + " = true;\n" +
					spaces + "        break;\n"
			);

			cardinalityCheckEnd.append(
					spaces + "    _searchIndex_" + runNumber + "_" + i + " = _searchIndex_" + runNumber + "_" + i + getNextString + ";\n" +
					spaces + "}\n" +
					"\n" +
					spaces + "if(_continueFlag_" + runNumber + "_" + i + ") {\n" +
					spaces + "    continue;\n" +
					spaces + "}\n"
			);
		}
		else if(cardinality.equals("+")) {
			cardinalityCheckBegin.append(
					spaces + "boolean _continueFlag_" + runNumber + "_" + i + " = false;\n" +
					"\n" +
					spaces + "do {\n"
			);

			cardinalityCheckMiddle.append(
					spaces + "        _continueFlag_" + runNumber + "_" + i + " = true;\n" +
					spaces + "        break;\n"
			);

			cardinalityCheckEnd.append(
					spaces + "    _searchIndex_" + runNumber + "_" + i + " = _searchIndex_" + runNumber + "_" + i + getNextString + ";\n" +
					spaces + "} while(_searchIndex_" + runNumber + "_" + i + " != null && " + condition + ")\n" +
					"\n" +
					spaces + "if(_continueFlag_" + runNumber + "_" + i + ") {\n" +
					spaces + "    continue;\n" +
					spaces + "}\n"
			);
		}
		else if(cardinality.equals("?")) {
			cardinalityCheckBegin.append(spaces + "if(_searchIndex_" + runNumber + "_" + i + " != null && " + condition + ") {\n");

			cardinalityCheckMiddle.append(spaces + "        continue;\n");

			cardinalityCheckEnd.append(spaces + "}\n");
		}

		buffer.append(cardinalityCheckBegin);

		if(jumpOp.alsoSubClasses()) {
			buffer.append(spaces + "    if(!(_searchIndex_" + runNumber + "_" + i + " instanceof " + jumpOp.getClassType().getOpClass().getName() + ")) {\n");
		}
		else {
			buffer.append(spaces + "    if(_searchIndex_" + runNumber + "_" + i + ".getClass() != " + jumpOp.getClassType().getOpClass().getName() + ".class) {\n");
		}

		buffer.append(cardinalityCheckMiddle);
		buffer.append(
				spaces + "    }\n" +
				"\n"
		);

		if(!jumpOp.getConditions().equals("")) {
			buffer.append(
					spaces + "    " + jumpOp.getClassType().getOpClass().getName() + " " + jumpOp.getName() + " = (" + jumpOp.getClassType().getOpClass().getName() + ") _searchIndex_" + runNumber + "_" + i + ";\n" +
					"\n" +
					spaces + "    " + jumpOp.getConditions() + "\n" +
					"\n"
			);
		}

		buffer.append(
				spaces + "    if(_searchIndex_" + runNumber + "_" + i + ".getSucceedingOperators().size() != 1 || _searchIndex_" + runNumber + "_" + i + ".getPrecedingOperators().size() != 1) {\n" +
				cardinalityCheckMiddle +
				spaces + "    }\n" +
				"\n" +
				cardinalityCheckEnd +
				"\n"
		);

		String newNodeName = jumpOp.getName();

		if(preceding) {
			newNodeName += "_begin";

			buffer.append(spaces + this.variableList_left.get(newNodeName).assign_variable("_searchIndex_" + runNumber + "_" + i + ".getSucceedingOperators().get(0).getOperator()"));
		}
		else {
			newNodeName += "_end";

			buffer.append(spaces + this.variableList_left.get(newNodeName).assign_variable("_searchIndex_" + runNumber + "_" + i + ".getPrecedingOperators().get(0)"));
		}

		buffer.append(
				spaces + "// --- handle JumpOver - end ---\n" +
				"\n"
		);

		//		return "_searchIndex_" + runNumber + "_" + i;

		String[] ret = {"this." + newNodeName, "_searchIndex_" + runNumber + "_" + i};

		return ret;
	}

	private String getTemplate(String templateName) {
		if(!this.templates.containsKey(templateName)) {
			this.templates.put(templateName, File.readFile(RuleEditor.class.getResource("/code/code_" + templateName + ".tpl").getFile()));
		}

		return this.templates.get(templateName);
	}

	private String capitalizeWord(String word) {
		StringBuffer str = new StringBuffer(word);

		str.replace(0, 1, str.substring(0, 1).toUpperCase());

		return str.toString();
	}

	private StringBuffer formatCode(String code, String spaces) {
		StringBuffer buffer = new StringBuffer();

		String[] lines = code.split("\n");

		if(lines.length == 1 && lines[0].equals("")) {
			return buffer;
		}

		for(int i = 0; i < lines.length; i += 1) {
			buffer.append(spaces + lines[i]);

			if(i != lines.length-1) {
				buffer.append("\n");
			}
		}

		return buffer;
	}

	private Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>> validateRule(String ruleName, RulePanel rule) {
		Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>> resultTriple = this.validatedRules.get(ruleName);

		if(resultTriple == null) {
			System.out.print(":: validating rule '" + ruleName + "'... ");

			RuleEditorPane ruleEditorPane = rule.getRuleEditorPane();
			ImplementationPanel implementationPanel = rule.getImplementationPanel();

			resultTriple = ruleEditorPane.validateGraphs();

			if(resultTriple.getFirst()) {
				if(ruleEditorPane.getVisualGraphs().get(0).isEmpty() && ruleEditorPane.getVisualGraphs().get(1).isEmpty()
						&& implementationPanel.getAdditionalCheckJavaCode().isEmpty() && implementationPanel.getCheckJavaCode().isEmpty()
						&& implementationPanel.getAdditonalReplaceJavaCode().isEmpty() && implementationPanel.getReplaceJavaCode().isEmpty()) {
					JOptionPane.showOptionDialog(this.editor, "The rule '" + ruleName + "' has no visual representation and no code defined.\nIt will therefore be ignored in the code generation", "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);

					resultTriple = new Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>>(false, null, null);

					System.out.println("PASSED WITH WARNING");
				}
				else {
					System.out.println("OK");
				}
			}
			else if(!resultTriple.getFirst()) {
				System.err.println("FAILED");
			}

			this.validatedRules.put(ruleName, resultTriple);
		}

		return resultTriple;
	}
}
