package lupos.gui.operatorgraph.visualeditor.queryeditor.guielements;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.MultiInputOperator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Optional;

public class OptionalPanel extends MultiInputPanel {
	private static final long serialVersionUID = 1L;

	public OptionalPanel(MultiInputOperator operator, GraphWrapper gw, VisualGraph<Operator> parent, String string, Operator child) {
		super(operator, gw, parent, string, false);

		this.parentOp = operator;
		this.child = child;
	}

	private void showPopupMenu(MouseEvent me) {
		JMenuItem switchMI = new JMenuItem("switch children positions");
		switchMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				((Optional) operator).switchChildrenPositions(parent);
			}
		});

		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(switchMI);
		popupMenu.show(this, me.getX(), me.getY());
	}

	public void mousePressed(MouseEvent me) {
		if(me.isPopupTrigger()) {
			this.showPopupMenu(me);
		}
		else {
			super.mousePressed(me);
		}
	}

	public void mouseReleased(MouseEvent me) {
		if(me.isPopupTrigger()) {
			this.showPopupMenu(me);
		}
		else {
			super.mouseReleased(me);
		}
	}
}