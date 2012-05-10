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
	 * @return the color of the background line
	 */
	public void setLighter(final Color color) {
		final int red = Math.min(255, (int) (color.getRed() * 1.2));
		final int green = Math.min(255, (int) (color.getGreen() * 1.2));
		final int blue = Math.min(255, (int) (color.getBlue() * 1.2));

		this.setColor(new Color(red, green, blue));
	}

	// Paint the background highlight

	public void paint(final Graphics g, final int p0, final int p1,
			final Shape bounds, final JTextComponent c) {
		try {
			final Rectangle r = c.modelToView(c.getCaretPosition());
			g.setColor(this.color);
			g.fillRect(0, r.y, c.getWidth(), r.height);

			if (this.lastView == null)
				this.lastView = r;
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
			public void run() {
				try {
					final int offset = component.getCaretPosition();
					final Rectangle currentView = component.modelToView(offset);

					// Remove the highlighting from the previously highlighted
					// line
					if (lastView != null && lastView.y != currentView.y) {
						component.repaint(0, lastView.y, component.getWidth(),
								lastView.height);
					}
					lastView = currentView;
				} catch (final BadLocationException ble) {
				}
			}
		});
	}

	// Implement CaretListener
	public void caretUpdate(final CaretEvent e) {
		this.resetHighlight();
	}

	// Implement MouseListener
	public void mousePressed(final MouseEvent e) {
		this.resetHighlight();
	}

	// Implement MouseMotionListener
	public void mouseDragged(final MouseEvent e) {
		this.resetHighlight();
	}

	public void mouseClicked(final MouseEvent e) {
	}

	public void mouseEntered(final MouseEvent e) {
	}

	public void mouseExited(final MouseEvent e) {
	}

	public void mouseReleased(final MouseEvent e) {
	}

	public void mouseMoved(final MouseEvent e) {
	}
}