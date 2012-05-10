package lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class ComboRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = 1L;

	public ComboRenderer() {
		this.setOpaque(true);
		this.setBorder(new EmptyBorder(1, 1, 1, 1));
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if(isSelected) {
			this.setBackground(list.getSelectionBackground());
			this.setForeground(list.getSelectionForeground());
		}
		else {
			this.setBackground(list.getBackground());
			this.setForeground(list.getForeground());
		}

		if(!((ComboItem) value).isEnabled) {
			this.setBackground(list.getBackground());
			this.setForeground(UIManager.getColor("Label.disabledForeground"));
		}

		this.setFont(list.getFont());
		this.setText(value.toString());

		return this;
	}
}