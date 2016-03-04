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
package lupos.gui.operatorgraph.visualeditor.operators;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.PredicatePanel;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.util.DummyItem;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.misc.util.OperatorIDTuple;
import lupos.gui.operatorgraph.visualeditor.util.SimpleOperatorGraphVisitor;
public abstract class RDFTerm extends JTFOperator {
	protected Item item;
	protected Hashtable<RDFTerm, LinkedList<Item>> predicates = new Hashtable<RDFTerm, LinkedList<Item>>();

	/**
	 * <p>Constructor for RDFTerm.</p>
	 *
	 * @param prefix a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 */
	protected RDFTerm(Prefix prefix) {
		super(prefix);

		this.item = new DummyItem();
	}

	/**
	 * <p>Constructor for RDFTerm.</p>
	 *
	 * @param prefix a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 * @param item a {@link lupos.datastructures.items.Item} object.
	 */
	protected RDFTerm(Prefix prefix, Item item) {
		super(prefix);

		this.item = item;
	}

	/**
	 * <p>Getter for the field <code>item</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.Item} object.
	 */
	public Item getItem() {
		return this.item;
	}

	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return this.item.toString();
	}

	/**
	 * <p>addPredicate.</p>
	 *
	 * @param child a {@link lupos.gui.operatorgraph.visualeditor.operators.RDFTerm} object.
	 * @param predicate a {@link lupos.datastructures.items.Item} object.
	 */
	public void addPredicate(RDFTerm child, Item predicate) {
		if(!this.predicates.containsKey(child)) {
			this.predicates.put(child, new LinkedList<Item>());
		}

		this.predicates.get(child).add(predicate);
	}

	/**
	 * <p>deletePredicate.</p>
	 *
	 * @param child a {@link lupos.gui.operatorgraph.visualeditor.operators.RDFTerm} object.
	 * @param index a int.
	 */
	public void deletePredicate(RDFTerm child, int index) {
		if(index == this.predicates.get(child).size()) {
			return;
		}

		this.predicates.get(child).remove(index);

		if(this.predicates.get(child).isEmpty()) {
			this.deleteAnnotation(child);
			child.getGUIComponent().getParentQG().addToRootList(new GraphWrapperOperator(child));
		}
	}

	/**
	 * <p>Getter for the field <code>predicates</code>.</p>
	 *
	 * @param child a {@link lupos.gui.operatorgraph.visualeditor.operators.RDFTerm} object.
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<Item> getPredicates(RDFTerm child) {
		return this.predicates.get(child);
	}

	/** {@inheritDoc} */
	public void deleteAnnotation(Operator child) {
		super.deleteAnnotation(child);

		this.predicates.remove(child);
	}

	/** {@inheritDoc} */
	public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawAnnotations(VisualGraph<Operator> parent) {
		Hashtable<GraphWrapper, AbstractSuperGuiComponent> predicates = new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();

		// walk through children of this RDFTerm...
		for(OperatorIDTuple<Operator> opIDTuple : this.getSucceedingOperators()) {
			RDFTerm child = (RDFTerm) opIDTuple.getOperator(); // get current children

			// create predicate panel...
			PredicatePanel predicatePanel = new PredicatePanel(parent, this, child, this.prefix);

			this.annotationLabels.put(child, predicatePanel);

			// add predicate panel to hash table with its GraphWrapper...
			predicates.put(new GraphWrapperOperator(child), predicatePanel);
		}

		return predicates;
	}

	/** {@inheritDoc} */
	public boolean equals(Object o) {
		if (this.getClass() != o.getClass())
			return false;
		try {
			return this.item == ((RDFTerm) o).item;
		}
		catch(Exception e) {
			return false;
		}
	}

	/**
	 * <p>hashCode.</p>
	 *
	 * @return a int.
	 */
	public int hashCode() {
		return System.identityHashCode(this);
	}

	/**
	 * <p>prefixAdded.</p>
	 */
	public void prefixAdded() {
		super.prefixAdded();

		for(AbstractGuiComponent<Operator> agc : this.annotationLabels.values()) {
			((PredicatePanel) agc).prefixAdded();
		}
	}

	/** {@inheritDoc} */
	public void prefixModified(String oldPrefix, String newPrefix) {
		super.prefixModified(oldPrefix, newPrefix);

		for(AbstractGuiComponent<Operator> agc : this.annotationLabels.values()) {
			((PredicatePanel) agc).prefixModified(oldPrefix, newPrefix);
		}
	}

	/** {@inheritDoc} */
	public void prefixRemoved(String prefix, String namespace) {
		super.prefixRemoved(prefix, namespace);

		for(AbstractGuiComponent<Operator> agc : this.annotationLabels.values()) {
			((PredicatePanel) agc).prefixRemoved(prefix, namespace);
		}
	}

	/**
	 * <p>serializeOperator.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public StringBuffer serializeOperator() {
		return new StringBuffer(this.item.toString());
	}

	/** {@inheritDoc} */
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer ret = new StringBuffer();

		if(visited.contains(this)) {
			return ret;
		}

		visited.add(this);

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators) {
			Operator object = opIDT.getOperator();

			for(Item predicate : this.predicates.get(object)) {
				ret.append(this.prefix.add(this.item.toString())); // subject
				ret.append(" " + this.prefix.add(predicate.toString())); // predicate
				ret.append(" " + this.prefix.add(object.toString()) + " .\n"); // object
			}

			ret.append(object.serializeOperatorAndTree(visited)); // succeeding operators
		}

		return ret;
	}

	/** {@inheritDoc} */
	public boolean validateOperator(boolean showErrors, HashSet<Operator> visited, Object data) {
		if(visited.contains(this)) {
			return true;
		}

		visited.add(this);

		if(this.panel.validateOperatorPanel(showErrors, data) == false) {
			return false;
		}

		if(this.precedingOperators.isEmpty() && this.succeedingOperators.isEmpty()) {
			JOptionPane.showOptionDialog(this.panel.getVisualEditor(), "A RDFTerm must have at least one child!", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null , null);

			return false;
		}

		for(AbstractGuiComponent<Operator> agc : this.annotationLabels.values()) {
			if(((PredicatePanel) agc).validateOperatorPanel(showErrors, data) == false) {
				return false;
			}
		}

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators) {
			if(this.annotationLabels.get(opIDT.getOperator()).validateOperatorPanel(showErrors, data) == false) {
				return false;
			}

			if(opIDT.getOperator().validateOperator(showErrors, visited, data) == false) {
				return false;
			}
		}

		return true;
	}

	/**
	 * <p>canAddSucceedingOperator.</p>
	 *
	 * @return a boolean.
	 */
	public boolean canAddSucceedingOperator() {
		return true;
	}

	/** {@inheritDoc} */
	public boolean variableInUse(String variable, HashSet<Operator> visited) {
		if(visited.contains(this)) {
			return false;
		}

		visited.add(this);

		if(this.item instanceof Variable && this.item.toString().equalsIgnoreCase(variable)) {
			return true;
		}

		for(LinkedList<Item> items : this.predicates.values()) {
			for(Item item : items) {
				if(item instanceof Variable && item.toString().equalsIgnoreCase(variable)) {
					return true;
				}
			}
		}

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators) {
			if(opIDT.getOperator().variableInUse(variable, visited)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * <p>findRootNodes.</p>
	 *
	 * @param nodeList a {@link java.util.LinkedHashSet} object.
	 * @return a {@link java.util.LinkedHashSet} object.
	 */
	@SuppressWarnings("unchecked")
	public static LinkedHashSet<Operator> findRootNodes(LinkedHashSet<Operator> nodeList) {
		LinkedHashSet<Operator> result = (LinkedHashSet<Operator>) nodeList.clone();

		for(Operator op : nodeList) {
			for(OperatorIDTuple<Operator> opIDt : op.getSucceedingOperators()) {
				result.remove(opIDt.getOperator());
			}
		}


		final LinkedHashSet<Operator> notVisitedOperators = nodeNotVisited(nodeList, result);

		while(!notVisitedOperators.isEmpty()) {
			final int[] numberReachable = new int[notVisitedOperators.size()];
			final Operator[] operators = notVisitedOperators.toArray(new Operator[0]);

			int i = 0;

			for(Operator notVisitedOp : operators) {
				final int count = i;

				final SimpleOperatorGraphVisitor sogv = new SimpleOperatorGraphVisitor() {
					private static final long serialVersionUID = -3649188246478511485L;

					public Object visit(Operator operator) {
						numberReachable[count]++;

						return null;
					}
				};

				notVisitedOp.visit(sogv);

				i++;
			}

			int max = 0;

			for(int c = 1; c < numberReachable.length; ++c) {
				if(numberReachable[max] < numberReachable[c]) {
					max = c;
				}
			}

			result.add(operators[max]);


			final SimpleOperatorGraphVisitor sogv = new SimpleOperatorGraphVisitor() {
				private static final long serialVersionUID = -3649188246478511485L;

				public Object visit(Operator operator) {
					notVisitedOperators.remove(operator);

					return null;
				}
			};

			operators[max].visit(sogv);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private static LinkedHashSet<Operator> nodeNotVisited(LinkedHashSet<Operator> nodeList, LinkedHashSet<Operator> rootNodes) {
		final LinkedHashSet<Operator> notVisitedNodes = (LinkedHashSet<Operator>) nodeList.clone();

		for(Operator rootNode : rootNodes) {
			final SimpleOperatorGraphVisitor sogv = new SimpleOperatorGraphVisitor() {
				private static final long serialVersionUID = -3649188246478511485L;

				public Object visit(Operator operator) {
					notVisitedNodes.remove(operator);

					return null;
				}
			};

			rootNode.visit(sogv);
		}

		return notVisitedNodes;
	}

	/**
	 * <p>addPredicate.</p>
	 *
	 * @param child a {@link lupos.gui.operatorgraph.visualeditor.operators.RDFTerm} object.
	 * @param predicate a {@link java.lang.String} object.
	 * @throws lupos.gui.operatorgraph.visualeditor.util.ModificationException if any.
	 */
	public abstract void addPredicate(RDFTerm child, String predicate) throws ModificationException;
	/**
	 * <p>setPredicate.</p>
	 *
	 * @param child a {@link lupos.gui.operatorgraph.visualeditor.operators.RDFTerm} object.
	 * @param predicate a {@link java.lang.String} object.
	 * @param index a int.
	 * @throws lupos.gui.operatorgraph.visualeditor.util.ModificationException if any.
	 */
	public abstract void setPredicate(RDFTerm child, String predicate, int index) throws ModificationException;
}
