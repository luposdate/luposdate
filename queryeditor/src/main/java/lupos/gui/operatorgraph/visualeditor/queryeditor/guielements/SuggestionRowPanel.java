package lupos.gui.operatorgraph.visualeditor.queryeditor.guielements;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler.ComboItem;

public class SuggestionRowPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private int startX = -1;
	private int startY = -1;
	private int endX = -1;
	private int endY = -1;
	private int count = 0;
	private QueryResult queryResult;
	private JComboBox predBox;
	private JComboBox soBox;
	private Variable varPred;
	private Variable varSO;
	private LinkedList<ComboItem> predElements;
	private LinkedList<ComboItem> soElements;

	public SuggestionRowPanel(QueryResult queryResult, Variable varSO, Variable varPred) {
		super();

		this.queryResult = queryResult;
		this.varPred = varPred;
		this.varSO = varSO;
	}

	public Component add(Component comp) {
		super.add(comp);

		this.count++;

		if(this.count == 1) { // first component...
			this.startX = comp.getLocation().x + comp.getPreferredSize().width;
			this.startY = comp.getLocation().y + (comp.getPreferredSize().height / 2);

			if(comp instanceof SuggestionPanel) {
				SuggestionPanel panel = (SuggestionPanel) comp;
				this.soBox = (JComboBox) panel.getComponent(0);
				this.soElements = panel.getElements();
			}
		}
		else if(this.count == 2) { // the predicate component...
			SuggestionPanel panel = (SuggestionPanel) comp;
			this.predBox = (JComboBox) panel.getComponent(0);
			this.predElements = panel.getElements();
		}
		else if(this.count == 3) { // last component...
			this.endX = comp.getLocation().x;
			this.endY = comp.getLocation().y + (comp.getPreferredSize().height / 2);

			if(comp instanceof SuggestionPanel) {
				SuggestionPanel panel = (SuggestionPanel) comp;
				this.soBox = (JComboBox) panel.getComponent(0);
				this.soElements = panel.getElements();
			}
		}

		return comp;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		GraphBox.drawConnection((Graphics2D) g, this.startX, this.startY, this.endX, this.endY, true);
	}


	public void actionPerformed(ActionEvent ae) {
		ComboItem choice;

		String first = ((JComboBox) ae.getSource()).getItemAt(0).toString();

		if(first.equalsIgnoreCase(this.varSO.toString())) {
			choice = (ComboItem) this.soBox.getSelectedItem();

			if(choice.toString().equalsIgnoreCase(this.varSO.toString())) {
				for(ComboItem ci : this.predElements)
					ci.isEnabled = true;

				return;
			}

			for(ComboItem ci : this.predElements)
				ci.isEnabled = false;

			this.predElements.get(0).isEnabled = true;

			for(Bindings b : this.queryResult) {
				int i = this.predElements.indexOf(new ComboItem(b.get(this.varPred)));

				if(b.get(this.varSO).toString().equalsIgnoreCase(choice.toString()))
					this.predElements.get(i).isEnabled = true;
			}

			if(!((ComboItem) this.predBox.getSelectedItem()).isEnabled)
				this.predBox.setSelectedIndex(0);

			return;
		}

		if(first.equalsIgnoreCase(this.varPred.toString())) {
			choice = (ComboItem) this.predBox.getSelectedItem();

			if(choice.toString().equalsIgnoreCase(this.varPred.toString())) {
				for(ComboItem ci : this.soElements)
					ci.isEnabled = true;

				return;
			}

			for(ComboItem ci : this.soElements)
				ci.isEnabled = false;

			this.soElements.get(0).isEnabled = true;

			for(Bindings b : this.queryResult) {
				int i = this.soElements.indexOf(new ComboItem(b.get(this.varSO)));

				if(b.get(this.varPred).toString().equalsIgnoreCase(choice.toString()))
					this.soElements.get(i).isEnabled = true;
			}

			if(!((ComboItem) this.soBox.getSelectedItem()).isEnabled)
				this.soBox.setSelectedIndex(0);

			return;
		}
	}
}