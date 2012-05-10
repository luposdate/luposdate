package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.misc.util.OperatorIDTuple;

public class Union extends MultiInputOperator {
	public void addAvailableOperators(JPopupMenu popupMenu, final VisualGraph<Operator> parent, final GraphWrapper oldGW) {
		JMenuItem joinOpMI = new JMenuItem("change operator to JOIN");
		joinOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				replaceOperator(new Join(), parent, oldGW);
			}
		});

		popupMenu.add(joinOpMI);

		JMenuItem optionalOpMI = new JMenuItem("change operator to OPTIONAL");
		optionalOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				replaceOperator(new Optional(), parent, oldGW);
			}
		});

		popupMenu.add(optionalOpMI);
	}

	public StringBuffer serializeOperator() {
		StringBuffer ret = new StringBuffer();

		ret.append("{\n");
		ret.append(this.succeedingOperators.get(0).getOperator().serializeOperator());
		ret.append("} UNION {\n");
		ret.append(this.succeedingOperators.get(1).getOperator().serializeOperator());
		ret.append("}\n");

		return ret;
	}

	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer ret = new StringBuffer();

		ret.append("{\n");
		ret.append(this.succeedingOperators.get(0).getOperator().serializeOperatorAndTree(visited));
		ret.append("}\n");

		for(int i = 1; i < this.succeedingOperators.size(); ++i) {
			ret.append("UNION {\n");
			ret.append(this.succeedingOperators.get(i).getOperator().serializeOperatorAndTree(visited));
			ret.append("}\n");
		}

		return ret;
	}

	public boolean validateOperator(boolean showErrors, HashSet<Operator> visited, Object data) {
		return super.validateOperator(showErrors, visited, data);
	}

	public boolean canAddSucceedingOperator() {
		return true;
	}

	public int getFreeOpID() {
		int prevID = -1;

		for(OperatorIDTuple<Operator> opIDt : this.succeedingOperators) {
			int id = opIDt.getId();

			if(prevID + 1 < id)
				return prevID + 1;

			prevID = id;
		}

		return prevID;
	}
}