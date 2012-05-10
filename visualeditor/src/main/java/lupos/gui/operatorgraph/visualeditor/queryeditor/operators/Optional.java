package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Hashtable;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.OptionalPanel;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.misc.util.OperatorIDTuple;

public class Optional extends MultiInputOperator {
	@Override
	public void addAvailableOperators(final JPopupMenu popupMenu, final VisualGraph<Operator> parent, final GraphWrapper oldGW) {
		final JMenuItem joinOpMI = new JMenuItem("change operator to JOIN");
		joinOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent me) {
				replaceOperator(new Join(), parent, oldGW);
			}
		});

		popupMenu.add(joinOpMI);


		final JMenuItem optionalOpMI = new JMenuItem("change operator to UNION");
		optionalOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent me) {
				replaceOperator(new Union(), parent, oldGW);
			}
		});

		popupMenu.add(optionalOpMI);
	}

	@Override
	public StringBuffer serializeOperator() {
		final StringBuffer ret = new StringBuffer();

		for(final OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			if(opIDT.getId() == 0)
				ret.append(opIDT.getOperator().serializeOperator());

		ret.append("OPTIONAL {\n");

		for(final OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			if(opIDT.getId() == 1)
				ret.append(opIDT.getOperator().serializeOperator());

		ret.append("}\n");

		return ret;
	}

	@Override
	public StringBuffer serializeOperatorAndTree(final HashSet<Operator> visited) {
		final StringBuffer ret = new StringBuffer();

		for(final OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			if(opIDT.getId() == 0)
				ret.append(opIDT.getOperator().serializeOperatorAndTree(visited));

		ret.append("OPTIONAL {\n");

		for(final OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			if(opIDT.getId() == 1)
				ret.append(opIDT.getOperator().serializeOperatorAndTree(visited));

		ret.append("}\n");

		return ret;
	}

	@Override
	public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawAnnotations(final VisualGraph<Operator> parent) {
		final Hashtable<GraphWrapper, AbstractSuperGuiComponent> lineLables = new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();

		for(final OperatorIDTuple<Operator> opIDt : this.succeedingOperators) {
			final String text = (opIDt.getId() == 0) ? "left" : "right";

			final GraphWrapper gw = new GraphWrapperOperator(opIDt.getOperator());

			final AbstractGuiComponent<Operator> element = new OptionalPanel(this, gw, parent, text, opIDt.getOperator());

			this.annotationLabels.put(opIDt.getOperator(), element);

			lineLables.put(gw, element);
		}

		return lineLables;
	}

	public void switchChildrenPositions(final VisualGraph<Operator> parent) {
		for(final OperatorIDTuple<Operator> opIDt : this.succeedingOperators) {
			if(opIDt.getId() == 0)
				opIDt.setId(1);
			else if(opIDt.getId() == 1)
				opIDt.setId(0);
		}

		final GraphBox box = parent.getBoxes().get(new GraphWrapperOperator(this));
		box.setLineAnnotations(this.drawAnnotations(parent));

		parent.arrange(false, false, false, Arrange.values()[0]);
	}

	@Override
	public int getFreeOpID() {
		if(this.succeedingOperators.size() == 0)
			return 0;

		if(this.succeedingOperators.get(0).getId() == 0)
			return 1;
		else
			return 0;
	}
}