package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

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
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.ContainerPanel;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.QueryGraph;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.util.SimpleOperatorGraphVisitor;

public abstract class OperatorContainer extends Operator {
	private HashSet<Operator> operators = new HashSet<Operator>();

	public OperatorContainer() { // needed for insertOperator()...

	}

	public OperatorContainer(final LinkedHashSet<Operator> ops) {
		this.operators = QueryRDFTerm.findRootNodes(ops);

		for(final Operator op : this.operators)
			op.setParentContainer(this, new HashSet<Operator>());
	}


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

		this.operators = QueryRDFTerm.findRootNodes(allOps);

		for(final Operator op : this.operators)
			panel.getQueryGraph().addToRootList(new GraphWrapperOperator(op));
	}

	public HashSet<Operator> getOperators() {
		return this.operators;
	}

	public void removeOperator(final Operator op) {
		this.operators.remove(op);

		this.determineRootNodes();
	}

	public void addOperator(final Operator op) {
		this.operators.add(op);

		this.determineRootNodes();
	}

	protected AbstractGuiComponent<Operator> drawPanel(final GraphWrapper gw, final QueryGraph parent, final Color bgColor) {
		final QueryGraph recursiveQueryGraph = new QueryGraph(parent.visualEditor, parent.prefix);

		parent.addChildComponent(recursiveQueryGraph);

		final JPanel panel = recursiveQueryGraph.createGraph(
				gw.getContainerElements(), false, false, false,
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


	public void prefixAdded() {
		for(final IPrefix op : this.operators)
			op.prefixAdded();
	}

	public void prefixRemoved(final String prefix, final String namespace) {
		for(final IPrefix op : this.operators)
			op.prefixRemoved(prefix, namespace);
	}

	public void prefixModified(final String oldPrefix, final String newPrefix) {
		for(final IPrefix op : this.operators)
			op.prefixModified(oldPrefix, newPrefix);
	}


	@Override
	public StringBuffer serializeOperator() {
		final StringBuffer ret = new StringBuffer();

		for(final Operator op : this.operators) {
			ret.append(op.serializeOperatorAndTree(new HashSet<Operator>()));
		}

		return ret;
	}

	@Override
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		final StringBuffer ret = new StringBuffer();

		visited = new HashSet<Operator>();

		for(final Operator op : this.operators)
			ret.append(op.serializeOperatorAndTree(visited));

		return ret;
	}


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


	@Override
	public boolean variableInUse(final String variable, HashSet<Operator> visited) {
		visited = new HashSet<Operator>();

		for(final Operator op : this.operators)
			if(op.variableInUse(variable, visited))
				return true;

		return false;
	}


	@Override
	public boolean canAddSucceedingOperator() {
		return false;
	}
}