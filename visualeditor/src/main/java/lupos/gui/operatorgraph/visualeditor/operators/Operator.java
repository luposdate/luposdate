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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lupos.datastructures.items.Variable;
import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.IPrefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.AnnotationPanel;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.misc.util.OperatorIDTuple;
import lupos.gui.operatorgraph.visualeditor.util.SimpleOperatorGraphVisitor;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.TokenMgrError;
public abstract class Operator implements IPrefix {
	/**
	 * succeedingOperators contains all succeeding operators (together with an
	 * operand number) of this operator. Let us assume that the current operator
	 * is C, another operator is A and a succeeding operator is B = A OPTIONAL
	 * C. Note that it is important to know that A is the left operand and C is
	 * the right operand of B. Then a succeeding operator of C is B with operand
	 * number 1 (A has a succeeding operator B with operand number 0).
	 */
	protected LinkedList<OperatorIDTuple<Operator>> succeedingOperators = new LinkedList<OperatorIDTuple<Operator>>();

	/**
	 * The preceding operators are stored in this variable.
	 */
	protected LinkedList<Operator> precedingOperators = new LinkedList<Operator>();

	protected AbstractGuiComponent<Operator> panel;

	private OperatorContainer parentContainer = null;

	protected Hashtable<Operator, AbstractGuiComponent<Operator>> annotationLabels = new Hashtable<Operator, AbstractGuiComponent<Operator>>();

	/**
	 * <p>Setter for the field <code>parentContainer</code>.</p>
	 *
	 * @param parentContainer a {@link lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer} object.
	 * @param visited a {@link java.util.HashSet} object.
	 */
	public void setParentContainer(OperatorContainer parentContainer, HashSet<Operator> visited) {
		if(visited.contains(this)) {
			return;
		}

		visited.add(this);

		this.parentContainer = parentContainer;

		for(OperatorIDTuple<Operator> opidt : this.succeedingOperators) {
			opidt.getOperator().setParentContainer(parentContainer, visited);
		}
	}

	/**
	 * <p>Getter for the field <code>parentContainer</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer} object.
	 */
	public OperatorContainer getParentContainer() {
		return this.parentContainer;
	}

	/**
	 * <p>getGUIComponent.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent} object.
	 */
	public AbstractGuiComponent<Operator> getGUIComponent() {
		return this.panel;
	}

	/**
	 * <p>getAnnotationLabel.</p>
	 *
	 * @param child a {@link lupos.gui.operatorgraph.visualeditor.operators.Operator} object.
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent} object.
	 */
	public AbstractGuiComponent<Operator> getAnnotationLabel(Operator child) {
		return this.annotationLabels.get(child);
	}

	/**
	 * This method adds one succeeding operator
	 *
	 * @param succeedingOperator
	 *            the succeeding operator
	 */
	public void addSucceedingOperator(OperatorIDTuple<Operator> succeedingOperator) {
		this.succeedingOperators.add(succeedingOperator);

		if(this.panel != null) {
			VisualGraph<Operator> visualGraph = this.panel.getParentQG();
			visualGraph.getBoxes().get(visualGraph.createGraphWrapper(this)).updateColorIndizes();
		}
	}

	/**
	 * This method returns the succeeding operators
	 *
	 * @return the succeeding operators
	 */
	public LinkedList<OperatorIDTuple<Operator>> getSucceedingOperators() {
		return this.succeedingOperators;
	}

	/**
	 * This method adds one preceding operator
	 *
	 * @param precedingOperator
	 *            the preceding operator
	 */
	public void addPrecedingOperator(Operator precedingOperator) {
		if(precedingOperator != null) {
			this.precedingOperators.add(precedingOperator);
		}
	}

	/**
	 * This method returns the preceding operators
	 *
	 * @return the preceding operators
	 */
	public LinkedList<Operator> getPrecedingOperators() {
		return this.precedingOperators;
	}

	/**
	 * Replaces this operator with a replacement operator.
	 *
	 * @param replacement
	 *            The replacement operator
	 */
	protected void replaceWith(Operator replacement) {
		for(Operator preOp : this.precedingOperators) {
			for(OperatorIDTuple<Operator> opid : preOp.succeedingOperators) {
				if(opid.getOperator().equals(this)) {
					opid.setOperator(replacement);
				}
			}
		}

		for(OperatorIDTuple<Operator> sucOp : this.succeedingOperators) {
			for(int i = 0; i < sucOp.getOperator().precedingOperators.size(); i++) {
				if(sucOp.getOperator().precedingOperators.get(i).equals(this)) {
					sucOp.getOperator().precedingOperators.set(i, replacement);
				}
			}
		}
	}

	/**
	 * <p>removeSucceedingOperator.</p>
	 *
	 * @param operator a {@link lupos.gui.operatorgraph.visualeditor.operators.Operator} object.
	 */
	public void removeSucceedingOperator(Operator operator) {
		Iterator<OperatorIDTuple<Operator>> suIt = this.succeedingOperators.iterator();

		while(suIt.hasNext()) {
			Operator op = suIt.next().getOperator();

			if(op == operator) {
				suIt.remove();

				break;
			}
		}

		if(this.panel != null) {
			VisualGraph<Operator> visualGraph = this.panel.getParentQG();
			visualGraph.getBoxes().get(visualGraph.createGraphWrapper(this)).updateColorIndizes();
		}
	}

	/**
	 * Delete this Operator and remove it completely from the AST.
	 *
	 * @param subtree a boolean.
	 */
	public void delete(boolean subtree) {
		// remove the link from the preceding operators...
		for(Operator preOp : this.precedingOperators) {
			preOp.removeSucceedingOperator(this);
		}

		for(OperatorIDTuple<Operator> sucOp : this.succeedingOperators) {
			// remove the link from the succeeding operators...
			Iterator<Operator> preIt = sucOp.getOperator().precedingOperators.iterator();

			while(preIt.hasNext()) {
				if(preIt.next().equals(this)) {
					preIt.remove();
				}
			}

			if(subtree) { // delete subtree...
				sucOp.getOperator().delete(true);
			}
			else {
				if(sucOp.getOperator().getPrecedingOperators().size() == 0) {
					sucOp.getOperator().panel.getParentQG().addToRootList(sucOp.getOperator().panel.getParentQG().createGraphWrapper(sucOp.getOperator()));
				}
			}
		}

		this.panel.delete();
	}

	/**
	 * <p>dump.</p>
	 *
	 * @param prefix a {@link java.lang.String} object.
	 * @param visited a {@link java.util.HashSet} object.
	 */
	public void dump(String prefix, HashSet<Operator> visited) {
		if(visited.contains(this)) {
			return;
		}

		visited.add(this);

		System.out.println(prefix + this.toString());

		for(OperatorIDTuple<Operator> childIDT : this.succeedingOperators) {
			Operator child = childIDT.getOperator();

			if(child instanceof OperatorContainer) {
				HashSet<Operator> visitedInContainer = new HashSet<Operator>();

				for(Operator opContainerChild : ((OperatorContainer) child).getOperators()) {
					opContainerChild.dump(prefix + "-", visitedInContainer);
				}
			}
			else {
				child.dump(prefix + "-", visited);
			}
		}
	}

	/**
	 * All implementation classes that replace a super class which already holds
	 * relevant state, have to override this method to copy that state from the
	 * superclass. Make sure to call super so that children and parents don't
	 * get lost
	 *
	 * @param op
	 *            The Operator to copy state from
	 */
	public void cloneFrom(Operator op) {
		if(op.succeedingOperators != null) {
			this.succeedingOperators = new LinkedList<OperatorIDTuple<Operator>>();
			this.succeedingOperators.addAll(op.succeedingOperators);
		}

		if(op.precedingOperators != null) {
			this.precedingOperators = new LinkedList<Operator>();
			this.precedingOperators.addAll(op.precedingOperators);
		}
	}

	/**
	 * This method sets the preceding operators recursively in the whole
	 * operator graph: If A has the succeeding operator B, then A is added to
	 * the list of preceding operators in B.
	 */
	public void setParents() {
		final SimpleOperatorGraphVisitor sogv = new SimpleOperatorGraphVisitor() {
			private static final long serialVersionUID = -3649188246478511485L;

			public Object visit(Operator operator) {
				if(operator instanceof OperatorContainer) {
					for(Operator op : ((OperatorContainer) operator).getOperators()) {
						op.setParents();
					}
				}

				for(OperatorIDTuple<Operator> opid : operator.succeedingOperators) {
					Operator op = opid.getOperator();

					if(!op.precedingOperators.contains(operator)) {
						op.precedingOperators.add(operator);
					}
				}

				return null;
			}
		};

		visit(sogv);
	}

	/**
	 * This method starts processing a simple visitor in the whole operator
	 * graph. Depth-first visit is applied.
	 *
	 * @param visitor
	 *            The visitor to be applied to each node in the operator graph
	 * @return The object retrieved from processing the visitor on this
	 *         operator.
	 */
	public Object visit(SimpleOperatorGraphVisitor visitor) {
		return visit(visitor, new HashSet<Operator>());
	}

	/**
	 * This is a helper method of the method Object
	 * visit(SimpleOperatorGraphVisitor visitor)
	 * 
	 * @param visitor
	 *            The visitor to be applied to each node
	 * @param hs
	 *            the already visited operators
	 * @return The object retrieved from processing the visitor on this
	 *         operator.
	 */
	private Object visit(SimpleOperatorGraphVisitor visitor, HashSet<Operator> hs) {
		if(hs.contains(this)) {
			return null;
		}

		hs.add(this);

		Object result = visitor.visit(this);

		for(OperatorIDTuple<Operator> opid : this.succeedingOperators) {
			opid.getOperator().visit(visitor, hs);
		}

		return result;
	}

	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return this.getClass().getSimpleName();
	}

	/**
	 * <p>validateOperator.</p>
	 *
	 * @param showErrors a boolean.
	 * @param visited a {@link java.util.HashSet} object.
	 * @param data a {@link java.lang.Object} object.
	 * @return a boolean.
	 */
	public boolean validateOperator(boolean showErrors, HashSet<Operator> visited, Object data) {
		if(this.panel.validateOperatorPanel(showErrors, data) == false) {
			return false;
		}

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators) {
			if(opIDT.getOperator().validateOperator(showErrors, visited, data) == false) {
				return false;
			}
		}

		return true;
	}

	/**
	 * <p>handleParseError.</p>
	 *
	 * @param t a {@link java.lang.Throwable} object.
	 * @throws lupos.gui.operatorgraph.visualeditor.util.ModificationException if any.
	 */
	public void handleParseError(Throwable t) throws ModificationException {
		int line = 0;
		int column = 0;

		if(t instanceof ParseException) {
			ParseException pe = (ParseException) t;

			// get precise line and column...
			if(pe.currentToken.next == null) {
				line = pe.currentToken.beginLine;
				column = pe.currentToken.beginColumn;
			}
			else {
				line = pe.currentToken.next.beginLine;
				column = pe.currentToken.next.beginColumn;
			}
		}
		else if(t instanceof TokenMgrError) {
			TokenMgrError tme = (TokenMgrError) t;

			// create the pattern to match and create a matcher against the
			// string
			Pattern pattern = Pattern.compile("line (\\d+), column (\\d+)");
			Matcher matcher = pattern.matcher(tme.getMessage());

			if(matcher.find() == true) { // try to find the pattern in the
				// message...
				// get matches...
				line = Integer.parseInt(matcher.group(1));
				column = Integer.parseInt(matcher.group(2));
			}
		}
		else {
			t.printStackTrace();
		}

		throw new ModificationException(t.getMessage(), line, column, this);
	}

	/**
	 * <p>canAddSucceedingOperator.</p>
	 *
	 * @return a boolean.
	 */
	public boolean canAddSucceedingOperator() {
		return (this.succeedingOperators.size() < 1);
	}

	/**
	 * <p>drawAnnotations.</p>
	 *
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
	 * @return a {@link java.util.Hashtable} object.
	 */
	public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawAnnotations(VisualGraph<Operator> parent) {
		Hashtable<GraphWrapper, AbstractSuperGuiComponent> lineLables = new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();

		for(OperatorIDTuple<Operator> opIDt : this.succeedingOperators) {
			GraphWrapper gw = new GraphWrapperOperator(this);
			GraphWrapper childGW = new GraphWrapperOperator(opIDt.getOperator());

			AbstractGuiComponent<Operator> element = new AnnotationPanel<Operator>(parent, gw, this, opIDt.getOperator());

			this.annotationLabels.put(opIDt.getOperator(), element);

			lineLables.put(childGW, element);
		}

		return lineLables;
	}

	/**
	 * <p>deleteAnnotation.</p>
	 *
	 * @param child a {@link lupos.gui.operatorgraph.visualeditor.operators.Operator} object.
	 */
	public void deleteAnnotation(Operator child) {
		if(!this.annotationLabels.containsKey(child)) {
			return;
		}

		for(OperatorIDTuple<Operator> sucOpIDT : this.succeedingOperators) {
			Operator sucOp = sucOpIDT.getOperator();

			if(sucOp.equals(child)) {
				Iterator<Operator> preIt = sucOp.precedingOperators.iterator();

				while(preIt.hasNext()) {
					if(preIt.next().equals(this)) {
						preIt.remove();
					}
				}
			}
		}

		Iterator<OperatorIDTuple<Operator>> suIt = this.succeedingOperators.iterator();

		while(suIt.hasNext()) {
			Operator op = suIt.next().getOperator();

			if(op.equals(child)) {
				suIt.remove();
			}
		}

		this.annotationLabels.get(child).delete();
		
		if(child.getPrecedingOperators().size() == 0) {
			VisualGraph<Operator> parentGQ = this.annotationLabels.get(child).getParentQG();
			parentGQ.addToRootList(parentGQ.createGraphWrapper(child));
		}

		this.annotationLabels.remove(child);
	}

	/**
	 * <p>computeUsedVariables.</p>
	 *
	 * @param n a {@link lupos.sparql1_1.Node} object.
	 * @param variables a {@link java.util.HashSet} object.
	 */
	public static void computeUsedVariables(Node n, HashSet<Variable> variables) {
		if(n == null) {
			return;
		}

		if(n instanceof lupos.sparql1_1.ASTVar) {
			try {
				variables.add(new Variable(((lupos.sparql1_1.ASTVar) n).getName().toString()));
			}
			catch(Exception e) {
				e.printStackTrace();

				return;
			}

		}

		for(int i = 0; i < n.jjtGetNumChildren(); i++) {
			computeUsedVariables(n.jjtGetChild(i), variables);
		}
	}

	/**
	 * <p>draw.</p>
	 *
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent} object.
	 */
	public abstract AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent);

	/**
	 * <p>serializeOperator.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public abstract StringBuffer serializeOperator();

	/**
	 * <p>serializeOperatorAndTree.</p>
	 *
	 * @param visited a {@link java.util.HashSet} object.
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public abstract StringBuffer serializeOperatorAndTree(HashSet<Operator> visited);

	/**
	 * <p>variableInUse.</p>
	 *
	 * @param variable a {@link java.lang.String} object.
	 * @param visited a {@link java.util.HashSet} object.
	 * @return a boolean.
	 */
	public abstract boolean variableInUse(String variable, HashSet<Operator> visited);
	
	private String getXPrefIDPrefix() {
		String className = this.getGUIComponent().getParentQG().visualEditor.getXPrefPrefix();
		return className.substring(0, 1).toLowerCase() + className.substring(1);
	}
	
	/**
	 * <p>getXPrefID.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getXPrefID() {
		return getXPrefIDPrefix() + "_style_operator";
	}
	
	/**
	 * <p>getXPrefIDForAnnotation.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getXPrefIDForAnnotation(){		
		return getXPrefIDPrefix() + "_style_annotation";
	}
}
