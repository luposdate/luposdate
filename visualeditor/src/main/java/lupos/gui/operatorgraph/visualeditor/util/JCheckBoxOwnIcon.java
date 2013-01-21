/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
 */
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