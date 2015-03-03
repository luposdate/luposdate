
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.gui.operatorgraph.visualeditor.operators;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.IPrefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.guielements.ContainerPanel;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.util.SimpleOperatorGraphVisitor;
public abstract class OperatorContainer extends Operator {
	private HashSet<Operator> operators = new HashSet<Operator>();

	/**
	 * <p>Constructor for OperatorContainer.</p>
	 */
	public OperatorContainer() { // needed for insertOperator()...

	}

	/**
	 * <p>Constructor for OperatorContainer.</p>
	 *
	 * @param ops a {@link java.util.LinkedHashSet} object.
	 */
	public OperatorContainer(final LinkedHashSet<Operator> ops) {
		this.operators = RDFTerm.findRootNodes(ops);

		for(final Operator op : this.operators)
			op.setParentContainer(this, new HashSet<Operator>());
	}


	/**
	 * <p>determineRootNodes.</p>
	 */
	public void determineRootNodes() {
		final ContainerPanel panel = (ContainerPanel) this.panel;

		for(final Operator op : this.operators)
			panel.getQueryGraph().removeFromRootList(new GraphWrapperOperator(op));

		final LinkedHashSet<Operator> allOps = new LinkedHashSet<Operator>();

		for(final Operator rootNode : this.operators) {
			final SimpleOperatorGraphVisitor sogv = new SimpleOperatorGraphVisitor() {
				private static final long serialVersionUID = -3649188246478511485L;

				public Object visit(final Operator operator) {
					allOps.add(operator);

					return null;
				}
			};
			rootNode.visit(sogv);
		}

		this.operators = RDFTerm.findRootNodes(allOps);

		for(final Operator op : this.operators)
			panel.getQueryGraph().addToRootList(new GraphWrapperOperator(op));
	}

	/**
	 * <p>Getter for the field <code>operators</code>.</p>
	 *
	 * @return a {@link java.util.HashSet} object.
	 */
	public HashSet<Operator> getOperators() {
		return this.operators;
	}

	/**
	 * <p>removeOperator.</p>
	 *
	 * @param op a {@link lupos.gui.operatorgraph.visualeditor.operators.Operator} object.
	 */
	public void removeOperator(final Operator op) {
		this.operators.remove(op);

		this.determineRootNodes();
	}

	/**
	 * <p>addOperator.</p>
	 *
	 * @param op a {@link lupos.gui.operatorgraph.visualeditor.operators.Operator} object.
	 */
	public void addOperator(final Operator op) {
		this.operators.add(op);

		this.determineRootNodes();
	}

	/**
	 * <p>drawPanel.</p>
	 *
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix} object.
	 * @param bgColor a {@link java.awt.Color} object.
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent} object.
	 */
	protected AbstractGuiComponent<Operator> drawPanel(final GraphWrapper gw, final VisualGraphOperatorWithPrefix parent, final Color bgColor) {
		final VisualGraphOperatorWithPrefix recursiveQueryGraph = parent.newInstance(parent.visualEditor, parent.prefix);

		parent.addChildComponent(recursiveQueryGraph);

		final JPanel panel = recursiveQueryGraph.createGraph(
				gw.getContainerElements(), 
				Arrange.values()[0]);

		this.panel = new ContainerPanel(this, gw, panel, recursiveQueryGraph, parent);
		this.panel.setBorder(new LineBorder(bgColor));

		if(this.operators.size() == 0) {
			this.panel.setPreferredSize(new Dimension(150, 100));

			panel.setPreferredSize(new Dimension(150 - 14, 100 - 2));
		}
		else
			this.panel.setPreferredSize(this.panel.getPreferredSize());

		return this.panel;
	}


	/**
	 * <p>prefixAdded.</p>
	 */
	public void prefixAdded() {
		for(final IPrefix op : this.operators)
			op.prefixAdded();
	}

	/** {@inheritDoc} */
	public void prefixRemoved(final String prefix, final String namespace) {
		for(final IPrefix op : this.operators)
			op.prefixRemoved(prefix, namespace);
	}

	/** {@inheritDoc} */
	public void prefixModified(final String oldPrefix, final String newPrefix) {
		for(final IPrefix op : this.operators)
			op.prefixModified(oldPrefix, newPrefix);
	}


	/** {@inheritDoc} */
	@Override
	public StringBuffer serializeOperator() {
		final StringBuffer ret = new StringBuffer();

		for(final Operator op : this.operators) {
			ret.append(op.serializeOperatorAndTree(new HashSet<Operator>()));
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		final StringBuffer ret = new StringBuffer();

		visited = new HashSet<Operator>();

		for(final Operator op : this.operators)
			ret.append(op.serializeOperatorAndTree(visited));

		return ret;
	}


	/** {@inheritDoc} */
	@Override
	public boolean validateOperator(final boolean showErrors, HashSet<Operator> visited, final Object data) {
		visited = new HashSet<Operator>();

		for(final Operator op : this.operators) {
			if(!op.validateOperator(showErrors, visited, data)) {
				return false;
			}
		}

		return true;
	}


	/** {@inheritDoc} */
	@Override
	public boolean variableInUse(final String variable, HashSet<Operator> visited) {
		visited = new HashSet<Operator>();

		for(final Operator op : this.operators)
			if(op.variableInUse(variable, visited))
				return true;

		return false;
	}


	/** {@inheritDoc} */
	@Override
	public boolean canAddSucceedingOperator() {
		return false;
	}
}
