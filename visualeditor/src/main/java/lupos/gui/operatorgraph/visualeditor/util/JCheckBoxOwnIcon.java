package lupos.gui.operatorgraph.visualeditor.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;

import lupos.gui.operatorgraph.util.VEImageIcon;

public class JCheckBoxOwnIcon extends JCheckBox {
	private static final long serialVersionUID = 5080828594396977595L;
	private JCheckBoxOwnIcon that = this;
	private int fontSize;
	private Icon checkedCB;
	private Icon uncheckedCB;
	private Icon moCheckedCB;
	private Icon moUncheckedCB;

	public JCheckBoxOwnIcon(String title, boolean state, Font font) {
		super(title, state);

		this.fontSize = font.getSize();

		this.checkedCB = VEImageIcon.getCheckedIcon(this.fontSize);
		this.uncheckedCB = VEImageIcon.getUncheckedIcon(this.fontSize);
		this.moCheckedCB = VEImageIcon.getMouseOverCheckedIcon(this.fontSize);
		this.moUncheckedCB = VEImageIcon.getMouseOverUncheckedIcon(this.fontSize);

		this.setIcon((state) ? this.checkedCB : this.uncheckedCB);
		this.setBackground(new Color(0, 0, 0, 0));
		this.setFont(font);

		this.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				that.setIcon((ie.getStateChange() == ItemEvent.SELECTED) ? that.moCheckedCB : that.moUncheckedCB);
			}
		});
		this.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent me) {
				that.setIcon((that.isSelected()) ? that.moCheckedCB : that.moUncheckedCB);
			}

			public void mouseExited(MouseEvent me) {
				that.setIcon((that.isSelected()) ? that.checkedCB : that.uncheckedCB);
			}

			public void mousePressed(MouseEvent me) {}
			public void mouseClicked(MouseEvent me) {}
			public void mouseReleased(MouseEvent me) {}
		});
	}
}