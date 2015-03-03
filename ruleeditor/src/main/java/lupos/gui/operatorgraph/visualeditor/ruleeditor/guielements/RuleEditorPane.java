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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import lupos.engine.operators.BasicOperator;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.JumpOverOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.ConnectionContainer;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.ModeEnum;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleConnection;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.VariableContainer;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.gui.operatorgraph.visualeditor.util.StatusBar;
import lupos.misc.Triple;
import lupos.misc.util.OperatorIDTuple;

import org.json.JSONException;
import org.json.JSONObject;

public class RuleEditorPane extends VisualEditor<Operator> {
	private static final long serialVersionUID = 2121817683005017716L;
	private final RuleEditorPane that = this;
	private JMenu graphMenu = null;
	private JMenuItem startNodeMI = null;
	private RuleOperator startNode = null;
	private LinkedList<HashSet<ConnectionContainer>> connections = null;

	public RuleEditorPane(final StatusBar statusBar) {
		super(true);

		this.statusBar = statusBar;

		this.visualGraphs.add(new RuleGraph(this));
		this.visualGraphs.add(new RuleGraph(this));
	}


	@Override
	public JMenuBar buildMenuBar() {
		final JMenuBar menuBar = this.createMenuBar();
		menuBar.add(this.buildEditMenu());
		menuBar.add(this.buildGraphMenu());
		menuBar.add(this.buildOperatorMenu());
		menuBar.setMinimumSize(menuBar.getPreferredSize());

		return menuBar; // return the MenuBar
	}

	private JMenu buildGraphMenu() {
		// create JMenuItem to rearrange the QueryGraph...
		final JMenuItem rearrangeMI = new JMenuItem("Arrange Graph");
		rearrangeMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				RuleEditorPane.this.that.statusBar.setText("Arranging query ...");

				for(final VisualGraph<Operator> visualGraph : RuleEditorPane.this.that.getVisualGraphs()) {
					visualGraph.arrange(Arrange.values()[0]);
				}

				RuleEditorPane.this.that.statusBar.clear();
			}
		});

		// create JMenuItem to select start node...
		this.startNodeMI = new JMenuItem("Select current node as start node");
		this.startNodeMI.setEnabled(false);
		this.startNodeMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				if(RuleEditorPane.this.that.startNode != null) {
					RuleEditorPane.this.that.startNode.setAsStartNode(false);
				}

				RuleEditorPane.this.that.startNode = (RuleOperator) RuleEditorPane.this.that.selectedOperatorsList.get(0);
				RuleEditorPane.this.that.startNode.setAsStartNode(true);
			}
		});

		// create Graph menu and add components to it...
		this.graphMenu = new JMenu("Graph");
		this.graphMenu.setEnabled(false);
		this.graphMenu.add(rearrangeMI);
		this.graphMenu.addSeparator();
		this.graphMenu.add(this.startNodeMI);

		this.jGraphMenus.add(this.graphMenu);

		return this.graphMenu;
	}

	private JMenu buildOperatorMenu() {
		// create JMenuItem to add a connection between two Operators...
		final JMenuItem addConnectionMI = new JMenuItem("Add connection between two operators");
		addConnectionMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				RuleEditorPane.this.that.connectionMode = new RuleConnection(RuleEditorPane.this.that);
			}
		});

		// create JMenuItem to add Operator...
		final JMenuItem opMI = new JMenuItem("Operator");
		opMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				RuleEditorPane.this.that.prepareOperatorForAdd(RuleOperator.class);
			}
		});

		// create JMenuItem to add JumpOver-Operator...
		final JMenuItem joMI = new JMenuItem("JumpOverOperator");
		joMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				RuleEditorPane.this.that.prepareOperatorForAdd(JumpOverOperator.class);
			}
		});

		// create Operator menu and add components to it...
		final JMenu operatorMenu = new JMenu("Add");
		operatorMenu.add(addConnectionMI);
		operatorMenu.addSeparator();
		operatorMenu.add(opMI);
		operatorMenu.add(joMI);

		return operatorMenu;
	}

	@Override
	protected void manageMenuItems() {
		super.manageMenuItems();

		boolean empty = true;

		for(final VisualGraph<Operator> visualGraph : this.visualGraphs) {
			if(visualGraph.getBoxes().size() != 0) {
				empty = false;

				break;
			}
		}

		this.graphMenu.setEnabled(!empty);


		if(this.selectedOperatorsList.size() == 1
				&& this.selectedOperatorsList.get(0).getGUIComponent().getParentQG() == this.visualGraphs.get(0)
				&& this.selectedOperatorsList.get(0) instanceof RuleOperator) {
			this.startNodeMI.setEnabled(true);
		}

		if(this.selectedOperatorsList.size() != 1) {
			this.startNodeMI.setEnabled(false);
		}
	}

	@Override
	protected void pasteElements(final String content) {}

	public void addJGraphMenu(final JMenu menu) {
		this.jGraphMenus.add(menu);
	}

	public RuleOperator getStartNode() {
		if(this.startNode != null) {
			return this.startNode;
		}


		final LinkedList<GraphWrapper> rootNodes = this.visualGraphs.get(0).getRootList(false);

		if(rootNodes.size() > 0) {
			return (RuleOperator) rootNodes.get(0).getElement();
		}


		for(final GraphWrapper gw : this.visualGraphs.get(0).getBoxes().keySet()) {
			return (RuleOperator) gw.getElement();
		}


		return null;
	}

	public void setStartNode(final RuleOperator op) {
		this.startNode = op;
	}

	/**
	 * This method validates both sides of the visual representation of a rule.
	 * It validates the following things:
	 *
	 * Errors:
	 * - default validation (operator names and so on)
	 * - Two operators on both sides with the same name must have the same class type!
	 * - It is not allowed to add JumpOverOperators on the right side which are not present on the left side!
	 * - The modes ALL_SUCCEEDING and ALL_PRECEDING are not allowed in cycles!
	 * - It is not allowed to have first ALL_SUCCEEDING and then ALL_PRECEDING!
	 * - The dimension of the start node must be 0!
	 *
	 * Warnings:
	 * - The connections of equal operators on both sides should have the same mode!
	 * - The connections of equal operators on both sides should have the same active state!
	 * - The operandID of active connections of equal operators on both sides should be equal!
	 *
	 */
	public Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>> validateGraphs() {
		this.connections = null;

		final VisualGraph<Operator> visualGraph0 = this.getVisualGraphs().get(0);
		final VisualGraph<Operator> visualGraph1 = this.getVisualGraphs().get(1);

		// --- normal validation - begin ---
		if(!visualGraph0.validateGraph(true, new HashMap<String, Operator>())) {
			return new Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>>(false, null, null);
		}

		if(!visualGraph1.validateGraph(true, new HashMap<String, Operator>())) {
			return new Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>>(false, null, null);
		}
		// --- normal validation - end ---

		final HashMap<String, VariableContainer> variableList_right = new HashMap<String, VariableContainer>();
		final HashMap<String, VariableContainer> variableList_left = new HashMap<String, VariableContainer>();

		try {
			final Set<GraphWrapper> gws0 = visualGraph0.getBoxes().keySet();
			final Set<GraphWrapper> gws1 = visualGraph1.getBoxes().keySet();

			final HashSet<String> equalOps = new HashSet<String>();
			final HashSet<String> jumpOps_left = new HashSet<String>();
			final HashSet<String> jumpOps_right = new HashSet<String>();

			for(final GraphWrapper gw0 : gws0) {
				final AbstractRuleOperator op0 = (AbstractRuleOperator) gw0.getElement();
				final String op0name = op0.getName();

				if(op0.getClass() == JumpOverOperator.class) {
					jumpOps_left.add(op0.getName());
				}

				if(op0name.startsWith(AbstractRuleOperator.internal_name)) {
					continue;
				}

				for(final GraphWrapper gw1 : gws1) {
					final AbstractRuleOperator op1 = (AbstractRuleOperator) gw1.getElement();
					final String op1name = op1.getName();

					if(op1.getClass() == JumpOverOperator.class) {
						jumpOps_right.add(op1.getName());
					}

					if(op1name.startsWith(AbstractRuleOperator.internal_name)) {
						continue;
					}

					if(op0name.equals(op1name) && !op0.getClassType().equals(op1.getClassType())) {
						throw new ModificationException("ERROR: Two operators on both sides have the same name but not the same class type!", op1);
					}
					else if(op0name.equals(op1name) && op0.getClassType().equals(op1.getClassType())) {
						equalOps.add(op0name);
					}
				}
			}

			jumpOps_right.removeAll(jumpOps_left);

			if(jumpOps_right.size() > 0) {
				throw new ModificationException("ERROR: It is not allowed to add JumpOverOperators on the right side which are not present on the left side!", null);
			}


			for(final GraphWrapper gw : visualGraph0.getRootList(false)) {
				this.validateCycles((Operator) gw.getElement(), new LinkedHashSet<Operator>(), false);
			}

			for(final GraphWrapper gw : visualGraph1.getRootList(false)) {
				this.validateCycles((Operator) gw.getElement(), new LinkedHashSet<Operator>(), false);
			}


			final AbstractRuleOperator startNode = this.getStartNode();

			if(startNode == null) {
				return new Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>>(true, null, null);
			}

			this.analyze_manage_node(variableList_left, startNode, 0, new HashSet<AbstractRuleOperator>(), new HashMap<Operator, HashSet<Operator>>());

			if(variableList_left.get(startNode.getName()).getDimension() > 0) {
				throw new ModificationException("ERROR: The dimension of the start node must be 0!", null);
			}


			boolean changed = true;
			final LinkedList<HashSet<ConnectionContainer>> connections = this.getConnections();
			final HashSet<ConnectionContainer> rightConnections = connections.get(1);

			if(visualGraph1.getRootList(false).size() > 0) {
				this.analyze_manage_node(variableList_right, (AbstractRuleOperator) visualGraph1.getRootList(false).get(0).getElement(), 0, new HashSet<AbstractRuleOperator>(), new HashMap<Operator, HashSet<Operator>>());
			}

			for(final ConnectionContainer conn : rightConnections) {
				final String parentName = this.getJumpOverName(conn.getParent(), false);
				final String childName = this.getJumpOverName(conn.getChild(), true);

				if(variableList_left.containsKey(childName)) {
					if(!variableList_left.containsKey(parentName) && conn.getMode() != ModeEnum.ALL_PRECEDING) {
						variableList_right.get(parentName).setCountProvider(conn.getChild());
					}

					variableList_right.get(childName).setCountProvider(conn.getChild());
				}

				if(variableList_left.containsKey(parentName)) {
					if(!variableList_left.containsKey(childName) && conn.getMode() != ModeEnum.ALL_SUCCEEDING) {
						variableList_right.get(childName).setCountProvider(conn.getParent());
					}

					variableList_right.get(parentName).setCountProvider(conn.getParent());
				}
			}

			while(changed) {
				changed = false;

				for(final ConnectionContainer conn : rightConnections) {
					final String parentName = this.getJumpOverName(conn.getParent(), false);
					final String childName = this.getJumpOverName(conn.getChild(), true);

					if(variableList_right.get(parentName).getCountProvider() == null && variableList_right.get(childName).getCountProvider() != null) {
						variableList_right.get(parentName).setCountProvider(variableList_right.get(childName).getCountProvider());
						changed = true;
					}
					else if(variableList_right.get(childName).getCountProvider() == null && variableList_right.get(parentName).getCountProvider() != null) {
						variableList_right.get(childName).setCountProvider(variableList_right.get(parentName).getCountProvider());
						changed = true;
					}
				}
			}

			for(final VariableContainer vc : variableList_right.values()) {
				if(vc.getCountProvider() == null) {
					if(!variableList_left.containsKey(vc.getOpName())) {
						throw new ModificationException("ERROR: Can't determine dimension size for operator " + vc.getOpName() + "!", null);
					}
				}
			}


			final StringBuffer warnings = new StringBuffer();

			final Hashtable<GraphWrapper, LinkedList<GraphWrapper>> annotations0 = visualGraph0.getDrawnLineAnnotations();
			final Hashtable<GraphWrapper, LinkedList<GraphWrapper>> annotations1 = visualGraph1.getDrawnLineAnnotations();

			for(final GraphWrapper gw0 : annotations0.keySet()) {
				final AbstractRuleOperator op0 = (AbstractRuleOperator) gw0.getElement();
				final String op0name = op0.getName();

				// first op on left side has children...
				if(equalOps.contains(op0name)) {
					for(final GraphWrapper gw0child : annotations0.get(gw0)) {
						final AbstractRuleOperator op0child = (AbstractRuleOperator) gw0child.getElement();
						final String op0childName = op0child.getName();

						// second op on left side is child...
						if(equalOps.contains(op0childName)) {
							final AnnotationPanel annotation0 = (AnnotationPanel) op0.getAnnotationLabel(op0child);

							for(final GraphWrapper gw1 : annotations1.keySet()) {
								final AbstractRuleOperator op1 = (AbstractRuleOperator) gw1.getElement();
								final String op1name = op1.getName();
								boolean breakFlag = false;

								// first op on right side has children...
								if(equalOps.contains(op1name) && op0name.equals(op1name) && op0.getClassType().equals(op1.getClassType())) {
									for(final GraphWrapper gw1child : annotations1.get(gw1)) {
										final AbstractRuleOperator op1child = (AbstractRuleOperator) gw1child.getElement();
										final String op1childName = op1child.getName();

										// second op on right side is child...
										if(equalOps.contains(op1childName) && op0childName.equals(op1childName) && op0child.getClassType().equals(op1child.getClassType())) {
											final AnnotationPanel annotation1 = (AnnotationPanel) op1.getAnnotationLabel(op1child);

											if(annotation0.getMode() != annotation1.getMode()) {
												warnings.append("The connections of equal operators on both sides should have the same mode! The mode on the right side is being ignored if not.\n");

												breakFlag = true;
												break;
											}

											if(annotation0.isActive() != annotation1.isActive()) {
												warnings.append("The connections of equal operators on both sides should have the same active state!\n");

												breakFlag = true;
												break;
											}

											if(annotation0.isActive() && annotation0.getOpID() != annotation1.getOpID()) {
												warnings.append("The operandID of active connections of equal operators on both sides should be equal!\n");

												breakFlag = true;
												break;
											}
											else if(annotation0.isActive() && !annotation0.getOpLabel().equals(annotation1.getOpLabel())) {
												warnings.append("The operandLabel of active connections of equal operators on both sides should be equal!\n");

												breakFlag = true;
												break;
											}
										}
									}

									if(breakFlag) {
										break;
									}
								}
							}
						}
					}
				}
			}

			if(warnings.length() > 0) {
				JOptionPane.showOptionDialog(this, warnings.toString(), "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
			}
		}
		catch(final ModificationException me) {
			JOptionPane.showOptionDialog(this, me.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

			return new Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>>(false, null, null);
		}

		return new Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>>(true, variableList_left, variableList_right);
	}

	@SuppressWarnings("unchecked")
	private void validateCycles(final Operator op, final LinkedHashSet<Operator> visitedNodes, boolean foundAllSucceeding) throws ModificationException {
		if(visitedNodes.contains(op)) {
			boolean inLoop = false;

			final LinkedList<Operator> elements = new LinkedList<Operator>(visitedNodes);
			elements.add(op);

			for(int i = elements.indexOf(op); i < elements.size()-1; i += 1) {
				final AbstractRuleOperator element = (AbstractRuleOperator) elements.get(i);
				final AbstractRuleOperator nextElement = (AbstractRuleOperator) elements.get(i+1);

				if(element.equals(op)) {
					inLoop = true;
				}

				if(inLoop) {
					final ModeEnum mode = ((AnnotationPanel) element.getAnnotationLabel(nextElement)).getMode();

					if(mode == ModeEnum.ALL_PRECEDING || mode == ModeEnum.ALL_SUCCEEDING) {
						throw new ModificationException("The mode '" + mode + "' is not allowed in cycles!", element);
					}
				}
			}

			return;
		}

		visitedNodes.add(op);

		for(final OperatorIDTuple<Operator> sucOpIDt : op.getSucceedingOperators()) {
			final AbstractRuleOperator sucOp = (AbstractRuleOperator) sucOpIDt.getOperator();

			final ModeEnum mode = ((AnnotationPanel) ((AbstractRuleOperator) op).getAnnotationLabel(sucOp)).getMode();

			if(mode == ModeEnum.ALL_SUCCEEDING) {
				foundAllSucceeding = true;
			}
			else if(foundAllSucceeding && mode == ModeEnum.ALL_PRECEDING) {
				throw new ModificationException("It is not allowed to have first ALL_SUCCEEDING and then ALL_PRECEDING!", op);
			}

			this.validateCycles(sucOp, (LinkedHashSet<Operator>) visitedNodes.clone(), foundAllSucceeding);
		}
	}

	private void analyze_manage_node(final HashMap<String, VariableContainer> variableList, final AbstractRuleOperator node, final int currentDimension, final HashSet<AbstractRuleOperator> visitedNodes, final HashMap<Operator, HashSet<Operator>> visitedConnections) {
		if(visitedNodes.contains(node)) {
			return;
		}

		visitedNodes.add(node);


		// --- compare nodes - begin ---
		final String opName = node.getName();

		if(node.getClass() != JumpOverOperator.class) {
			final VariableContainer vc = variableList.get(opName);

			if(vc == null) {
				variableList.put(opName, new VariableContainer(opName, node.getClassType().getOpClass(), currentDimension));
			}
			else {
				vc.setDimension(currentDimension);
			}
		}
		else {
			VariableContainer vc = variableList.get(opName + "_begin");

			if(vc == null) {
				variableList.put(opName + "_begin", new VariableContainer(opName + "_begin", BasicOperator.class, currentDimension));
			}
			else {
				vc.setDimension(currentDimension);
			}

			vc = variableList.get(opName + "_end");

			if(vc == null) {
				variableList.put(opName + "_end", new VariableContainer(opName + "_end", BasicOperator.class, currentDimension));
			}
			else {
				vc.setDimension(currentDimension);
			}
		}
		// --- compare nodes - end ---


		final LinkedList<Operator> precedingOperators = node.getPrecedingOperators();

		if(precedingOperators.size() > 0) {
			for(int i = 0; i < precedingOperators.size(); i += 1) {
				final AbstractRuleOperator precOp = (AbstractRuleOperator) precedingOperators.get(i);


				HashSet<Operator> connectionNodes = visitedConnections.get(precOp);

				if(connectionNodes == null) {
					connectionNodes = new HashSet<Operator>();
					visitedConnections.put(precOp, connectionNodes);
				}
				else if(connectionNodes.contains(node)) {
					continue;
				}

				connectionNodes.add(node);


				switch(((AnnotationPanel) precOp.getAnnotationLabel(node)).getMode()) {
				default:
				case ONLY_PRECEDING_AND_SUCCEEDING:
				case ONLY_SUCCEEDING:
				case EXISTS:
					this.analyze_manage_node(variableList, precOp, currentDimension, visitedNodes, visitedConnections);

					break;
				case ALL_PRECEDING:
					this.analyze_manage_node(variableList, precOp, currentDimension+1, visitedNodes, visitedConnections);

					break;
				case ALL_SUCCEEDING:
					final HashMap<Operator, HashSet<Operator>> new_visitedConnections = new HashMap<Operator, HashSet<Operator>>();

					final HashSet<Operator> tmp = new HashSet<Operator>();
					tmp.add(node);

					new_visitedConnections.put(precOp, tmp);

					this.analyze_increment_dimension(variableList, precOp, currentDimension, new HashSet<AbstractRuleOperator>(), false);
					this.analyze_manage_node(variableList, precOp, currentDimension, new HashSet<AbstractRuleOperator>(), new_visitedConnections);

					break;
				}
			}
		}


		final LinkedList<OperatorIDTuple<Operator>> succedingOperators = node.getSucceedingOperators();

		if(succedingOperators.size() > 0) {
			for(int i = 0; i < succedingOperators.size(); i += 1) {
				final OperatorIDTuple<Operator> sucOpIDTup = succedingOperators.get(i);
				final AbstractRuleOperator sucOp = (AbstractRuleOperator) sucOpIDTup.getOperator();


				HashSet<Operator> connectionNodes = visitedConnections.get(node);

				if(connectionNodes == null) {
					connectionNodes = new HashSet<Operator>();
					visitedConnections.put(node, connectionNodes);
				}
				else if(connectionNodes.contains(sucOp)) {
					continue;
				}

				connectionNodes.add(sucOp);


				switch(((AnnotationPanel) node.getAnnotationLabel(sucOp)).getMode()) {
				default:
				case ONLY_PRECEDING_AND_SUCCEEDING:
				case ONLY_PRECEDING:
				case EXISTS:
					this.analyze_manage_node(variableList, sucOp, currentDimension, visitedNodes, visitedConnections);

					break;
				case ALL_SUCCEEDING:
					this.analyze_manage_node(variableList, sucOp, currentDimension+1, visitedNodes, visitedConnections);

					break;
				case ALL_PRECEDING:
					final HashMap<Operator, HashSet<Operator>> new_visitedConnections = new HashMap<Operator, HashSet<Operator>>();

					final HashSet<Operator> tmp = new HashSet<Operator>();
					tmp.add(sucOp);

					new_visitedConnections.put(node, tmp);

					this.analyze_increment_dimension(variableList, sucOp, currentDimension, new HashSet<AbstractRuleOperator>(), true);
					this.analyze_manage_node(variableList, sucOp, currentDimension, new HashSet<AbstractRuleOperator>(), new_visitedConnections);

					break;
				}
			}
		}
	}

	private void analyze_increment_dimension(final HashMap<String, VariableContainer> variableList, final AbstractRuleOperator node, final int currentDimension, final HashSet<AbstractRuleOperator> visitedNodes, final boolean preceding) {
		if(visitedNodes.contains(node)) {
			return;
		}

		visitedNodes.add(node);


		if(preceding) {
			for(final Operator precOp : node.getPrecedingOperators()) {
				final AbstractRuleOperator precRuleOp = (AbstractRuleOperator) precOp;
				final String opName = this.getJumpOverName(precRuleOp, false);
				final int newDimension = (((AnnotationPanel) precRuleOp.getAnnotationLabel(node)).getMode() == ModeEnum.ALL_PRECEDING) ? currentDimension+1 : currentDimension;
				final VariableContainer vc = variableList.get(opName);

				if(vc == null) {
					final Class<?> clazzType = (node.getClass() == JumpOverOperator.class) ? BasicOperator.class : precRuleOp.getClassType().getOpClass();

					variableList.put(opName, new VariableContainer(opName, clazzType, newDimension));
				}
				else {
					vc.setDimension(newDimension);
				}

				this.analyze_increment_dimension(variableList, precRuleOp, newDimension, visitedNodes, preceding);
			}
		}
		else {
			for(final OperatorIDTuple<Operator> opIDt : node.getSucceedingOperators()) {
				final AbstractRuleOperator sucOp = (AbstractRuleOperator) opIDt.getOperator();
				final String opName = this.getJumpOverName(sucOp, true);
				final int newDimension = (((AnnotationPanel) node.getAnnotationLabel(sucOp)).getMode() == ModeEnum.ALL_SUCCEEDING) ? currentDimension+1 : currentDimension;
				final VariableContainer vc = variableList.get(opName);

				if(vc == null) {
					final Class<?> clazzType = (node.getClass() == JumpOverOperator.class) ? BasicOperator.class : sucOp.getClassType().getOpClass();

					variableList.put(opName, new VariableContainer(opName, clazzType, newDimension));
				}
				else {
					vc.setDimension(newDimension);
				}

				this.analyze_increment_dimension(variableList, sucOp, newDimension, visitedNodes, preceding);
			}
		}
	}


	private String getJumpOverName(final AbstractRuleOperator op, final boolean begin) {
		String opName = op.getName();

		if(op.getClass() == JumpOverOperator.class) {
			opName += (begin) ? "_begin" : "_end";
		}

		return opName;
	}


	public LinkedList<HashSet<ConnectionContainer>> getConnections() {
		if(this.connections == null) {
			this.connections = new LinkedList<HashSet<ConnectionContainer>>();

			for(int i = 0; i <= 1; i += 1) {
				final VisualGraph<Operator> vg = this.visualGraphs.get(i);
				final HashSet<ConnectionContainer> connections = new HashSet<ConnectionContainer>();

				for(final GraphWrapper parentGW : vg.getDrawnLineAnnotations().keySet()) {
					final AbstractRuleOperator parentOp = (AbstractRuleOperator) parentGW.getElement();

					for(final GraphWrapper childGW : vg.getDrawnLineAnnotations().get(parentGW)) {
						final AbstractRuleOperator childOp = (AbstractRuleOperator) childGW.getElement();

						connections.add(new ConnectionContainer((AnnotationPanel) parentOp.getAnnotationLabel(childOp)));
					}
				}

				this.connections.add(connections);
			}
		}

		return this.connections;
	}


	public JSONObject toJSON() throws JSONException {
		final JSONObject saveObject = new JSONObject();
		saveObject.put("top toolbar", this.topToolbar.toJSON());
		saveObject.put("left side", ((RuleGraph) this.visualGraphs.get(0)).toJSON());
		saveObject.put("right side", ((RuleGraph) this.visualGraphs.get(1)).toJSON());

		return saveObject;
	}

	public void fromJSON(final JSONObject loadObject) {
		if(loadObject != null) {
			try {
				((RuleGraph) this.visualGraphs.get(0)).fromJSON(loadObject.getJSONObject("left side"));
				((RuleGraph) this.visualGraphs.get(1)).fromJSON(loadObject.getJSONObject("right side"));
				((RuleGraph) this.visualGraphs.get(0)).updateSize();
				((RuleGraph) this.visualGraphs.get(1)).updateSize();
			}
			catch(final JSONException e) {
				e.printStackTrace();
			}
		}
	}
}