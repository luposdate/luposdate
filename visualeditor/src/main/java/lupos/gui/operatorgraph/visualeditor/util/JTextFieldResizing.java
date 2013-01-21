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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;

public class JTextFieldResizing extends JTextField {
	private static final long serialVersionUID = 9125138588941985739L;

	private static volatile boolean ignoreFocus = false;

	private static ReentrantLock lock = new ReentrantLock();
	
	/**
	 * maximal width of this text field
	 */
	public static int MAX_SIZE_TEXTFIELD = 20;

	public JTextFieldResizing(final String text, final Font font,
			final AbstractSuperGuiComponent holder) {
		super(text);

		this.setCaretPosition(0);
		
		this.setFont(font);

		final Dimension d = this.calculateSize(font);

		this.setPreferredSize(d);
		this.setMinimumSize(d);
		this.setSize(d);
		
		this.setToolTipText(text);
		
		this.addCaretListener(
				new CaretListener(){

					@Override
					public void caretUpdate(CaretEvent e) {
						setToolTipText(getText());
					}
			
		});
		
		this.addFocusListener(new FocusListener() {
			public void focusGained(final FocusEvent fe) {
			}

			public void focusLost(final FocusEvent fe) {
				lock.lock();

				try {
					if (!ignoreFocus) {
						ignoreFocus = true;

						final Dimension d = calculateSize();

						setPreferredSize(d);
						setMinimumSize(d);
						setSize(d);

						holder.updateSize();

						if (fe.getOppositeComponent() != null) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											fe.getOppositeComponent()
													.requestFocus();

											SwingUtilities
													.invokeLater(new Runnable() {
														public void run() {
															ignoreFocus = false;
														}
													});
										}
									});
								}
							});
						} else {
							ignoreFocus = false;
						}
					}
				} finally {
					lock.unlock();
				}
			}

		});
	}

	private String getPlaceholderString() {
		String placeholder = getText();

		for (int i = placeholder.length(); i < 3; i++) {
			placeholder += " ";
		}

		if(placeholder.length()>JTextFieldResizing.MAX_SIZE_TEXTFIELD){
			placeholder = placeholder.substring(0, JTextFieldResizing.MAX_SIZE_TEXTFIELD);
		}
		
		return placeholder + "  ";
	}

	public Dimension calculateSize() {
		return this.calculateSize(getFont());
	}

	public Dimension calculateSize(final Font font) {
		final FontMetrics fm = getFontMetrics(font);

		final String placeholder = getPlaceholderString();

		return new Dimension(fm.stringWidth(placeholder), fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent());
	}

	public String toString() {
		return this.getText();
	}
}