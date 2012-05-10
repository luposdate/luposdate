package lupos.gui.operatorgraph.visualeditor.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;

public class JTextFieldResizing extends JTextField {
	private static final long serialVersionUID = 9125138588941985739L;

	private static volatile boolean ignoreFocus = false;

	private static ReentrantLock lock = new ReentrantLock();

	public JTextFieldResizing(final String text, final Font font,
			final AbstractSuperGuiComponent holder) {
		super(text);

		this.setFont(font);

		final Dimension d = this.calculateSize(font);

		this.setPreferredSize(d);
		this.setMinimumSize(d);
		this.setSize(d);

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

		return placeholder + "  ";
	}

	public Dimension calculateSize() {
		return this.calculateSize(getFont());
	}

	public Dimension calculateSize(final Font font) {
		final FontMetrics fm = getFontMetrics(font);

		final String placeholder = getPlaceholderString();

		return new Dimension(fm.stringWidth(placeholder), fm.getLeading()
				+ fm.getMaxAscent() + fm.getMaxDescent());
	}

	public String toString() {
		return this.getText();
	}
}