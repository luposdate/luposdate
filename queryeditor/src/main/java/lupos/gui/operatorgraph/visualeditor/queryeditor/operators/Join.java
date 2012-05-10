package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.MultiInputOperator;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.misc.util.OperatorIDTuple;

public class Join extends MultiInputOperator {
	public void addAvailableOperators(JPopupMenu popupMenu, final VisualGraph<Operator> parent, final GraphWrapper oldGW) {
		JMenuItem optionalOpMI = new JMenuItem("change operator to OPTIONAL");
		optionalOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				replaceOperator(new Optional(), parent, oldGW);
			}
		});

		popupMenu.add(optionalOpMI);


		JMenuItem unionOpMI = new JMenuItem("change operator to UNION");
		unionOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				replaceOperator(new Union(), parent, oldGW);
			}
		});

		popupMenu.add(unionOpMI);
	}

	public StringBuffer serializeOperator() {
		StringBuffer ret = new StringBuffer();

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			ret.append(opIDT.getOperator().serializeOperator());

		return ret;
	}

	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer ret = new StringBuffer();

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			ret.append(opIDT.getOperator().serializeOperatorAndTree(visited));

		return ret;
	}

	public boolean canAddSucceedingOperator() {
		return true;
	}

	public int getFreeOpID() {
		//		int prevID = -1;
		//
		//		for(OperatorIDTuple<Operator> opIDt : this.succeedingOperators) {
		//			int id = opIDt.getId();
		//
		//			if(prevID+1 < id)
		//				return prevID+1;
		//
		//			prevID = id;
		//		}
		//
		//		return prevID;

		int id = 0;
		boolean flag = false;

		while(true) {
			flag = false;

			for(OperatorIDTuple<Operator> opIDt : this.succeedingOperators) {
				if(opIDt.getId() == id) {
					flag = true;

					break;
				}
			}

			if(!flag) {
				return id;
			}

			id += 1;
		}
	}
}