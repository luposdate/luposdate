
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.gui.operatorgraph.visualeditor.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
public class StatusBar extends JPanel {
	private static final long serialVersionUID = 4602066192309533345L;
	private JLabel label = new JLabel();

	/**
	 * <p>Constructor for StatusBar.</p>
	 */
	public StatusBar() {
		this(" ");
	}

	/**
	 * <p>Constructor for StatusBar.</p>
	 *
	 * @param text a {@link java.lang.String} object.
	 */
	public StatusBar(String text) {
		super(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridheight = gbc.gridwidth = 1;
		gbc.weightx = gbc.weighty = 1.0;
		gbc.gridx = gbc.gridy = 0;
		gbc.insets = new Insets(0, 1, 0, 1);

		this.setBorder(new BevelBorder(BevelBorder.LOWERED));
		this.add(this.label, gbc);

		this.setText(text);
	}

	/**
	 * <p>setText.</p>
	 *
	 * @param text a {@link java.lang.String} object.
	 */
	public void setText(String text) {
		this.label.setText(text);

		this.revalidate();
	}

	/**
	 * <p>clear.</p>
	 */
	public void clear() {
		this.setText(" ");
	}
}
