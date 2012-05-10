package lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

public class ComboListener implements ActionListener {
	private JComboBox combo;
	private Object currentItem;
	private ActionListener additionalActionListener = null;

	public ComboListener(JComboBox combo) {
		combo.setSelectedIndex(0);

		this.combo  = combo;
		this.currentItem = combo.getSelectedItem();
	}

	public ComboListener(JComboBox combo, ActionListener additionalActionListener) {
		this(combo);

		this.additionalActionListener = additionalActionListener;
	}

	public void actionPerformed(ActionEvent ae) {
		Object tempItem = this.combo.getSelectedItem();

		if(!((ComboItem) tempItem).isEnabled)
			this.combo.setSelectedItem(this.currentItem);
		else
			this.currentItem = tempItem;

		if(this.additionalActionListener != null)
			this.additionalActionListener.actionPerformed(ae);
	}
}