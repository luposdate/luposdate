/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
public class ComboListener implements ActionListener {
	private JComboBox combo;
	private Object currentItem;
	private ActionListener additionalActionListener = null;

	/**
	 * <p>Constructor for ComboListener.</p>
	 *
	 * @param combo a {@link javax.swing.JComboBox} object.
	 */
	public ComboListener(JComboBox combo) {
		combo.setSelectedIndex(0);

		this.combo  = combo;
		this.currentItem = combo.getSelectedItem();
	}

	/**
	 * <p>Constructor for ComboListener.</p>
	 *
	 * @param combo a {@link javax.swing.JComboBox} object.
	 * @param additionalActionListener a {@link java.awt.event.ActionListener} object.
	 */
	public ComboListener(JComboBox combo, ActionListener additionalActionListener) {
		this(combo);

		this.additionalActionListener = additionalActionListener;
	}

	/** {@inheritDoc} */
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
