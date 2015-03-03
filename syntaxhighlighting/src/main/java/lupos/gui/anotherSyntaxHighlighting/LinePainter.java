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
package lupos.gui.anotherSyntaxHighlighting;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * Track the movement of the Caret by painting a background line at the current
 * caret position.
 */
public class LinePainter implements Highlighter.HighlightPainter,
		CaretListener, MouseListener, MouseMotionListener {
	private final JTextComponent component;
	private Color color;
	private Rectangle lastView;
	private Object highlightTag;

	/**
	 * The line color will be calculated automatically by attempting to make the
	 * current selection lighter by a factor of 1.2.
	 *
	 * @param component
	 *            text component that requires background line painting
	 */
	public LinePainter(final JTextComponent component) {
		this(component, null);

		this.setLighter(component.getSelectionColor());
	}

	/**
	 * Manually control the line color
	 *
	 * @param component
	 *            text component that requires background line painting
	 *
	 * @param color
	 *            the color of the background line
	 */
	public LinePainter(final JTextComponent component, final Color color) {
		this.component = component;

		this.setColor(color);

		// Add listeners so we know when to change highlighting
		component.addCaretListener(this);
		component.addMouseListener(this);
		component.addMouseMotionListener(this);

		// Turn highlighting on by adding a dummy highlight
		try {
			this.highlightTag = component.getHighlighter().addHighlight(0, 0,
					this);
		} catch (final BadLocationException ble) {
		}
	}

	public void removeLinePainter() {
		this.component.removeCaretListener(this);
		this.component.removeMouseListener(this);
		this.component.removeMouseMotionListener(this);
		this.component.getHighlighter().removeHighlight(this.highlightTag);
	}

	/**
	 * You can reset the line color at any time
	 *
	 * @param color
	 *            the color of the background line
	 */
	public void setColor(final Color color) {
		this.color = color;
	}

	/**
	 * Calculate the line color by making the selection color lighter
	 *
	 */
	public void setLighter(final Color color) {
		final int red = Math.min(255, (int) (color.getRed() * 1.2));
		final int green = Math.min(255, (int) (color.getGreen() * 1.2));
		final int blue = Math.min(255, (int) (color.getBlue() * 1.2));

		this.setColor(new Color(red, green, blue));
	}

	// Paint the background highlight

	@Override
	public void paint(final Graphics g, final int p0, final int p1,
			final Shape bounds, final JTextComponent c) {
		try {
			final Rectangle r = c.modelToView(c.getCaretPosition());
			g.setColor(this.color);
			g.fillRect(0, r.y, c.getWidth(), r.height);

			if (this.lastView == null) {
				this.lastView = r;
			}
		} catch (final BadLocationException ble) {
			ble.printStackTrace();
		}
	}

	/*
	 * Caret position has changed, remove the highlight
	 */
	private void resetHighlight() {
		// Use invokeLater to make sure updates to the Document are completed,
		// otherwise Undo processing causes the modelToView method to loop.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					final int offset = LinePainter.this.component.getCaretPosition();
					final Rectangle currentView = LinePainter.this.component.modelToView(offset);

					// Remove the highlighting from the previously highlighted
					// line
					if (LinePainter.this.lastView != null && LinePainter.this.lastView.y != currentView.y) {
						LinePainter.this.component.repaint(0, LinePainter.this.lastView.y, LinePainter.this.component.getWidth(),
								LinePainter.this.lastView.height);
					}
					LinePainter.this.lastView = currentView;
				} catch (final BadLocationException ble) {
				}
			}
		});
	}

	// Implement CaretListener
	@Override
	public void caretUpdate(final CaretEvent e) {
		this.resetHighlight();
	}

	// Implement MouseListener
	@Override
	public void mousePressed(final MouseEvent e) {
		this.resetHighlight();
	}

	// Implement MouseMotionListener
	@Override
	public void mouseDragged(final MouseEvent e) {
		this.resetHighlight();
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
	}
}