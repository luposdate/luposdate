/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/*
 *  Track the movement of the Caret by painting a background line at the
 *  current caret position.
 */
public class ErrorLinePainter implements Highlighter.HighlightPainter {
	private JTextComponent component;
	private Color color;
	private Rectangle lastView;
	private Object highlighter = null;
	private int position = -1;

	/*
	 *  The line color will be calculated automatically by attempting
	 *  to make the current selection lighter by a factor of 1.2.
	 *
	 *  @param component  text component that requires background line painting
	 */
	public ErrorLinePainter(JTextComponent component, int position) {
		this(component, position, null);
		this.setLighter(component.getSelectionColor());
	}

	/*
	 *  Manually control the line color
	 *
	 *  @param component  text component that requires background line painting
	 *  @param color      the color of the background line
	 */
	public ErrorLinePainter(JTextComponent component, int position, Color color) {
		this.component = component;
		this.position = position;
		this.setColor(color);

		//  Turn highlighting on by adding a dummy highlight

		try {
			this.highlighter = component.getHighlighter().addHighlight(0, 0, this);
		}
		catch(BadLocationException ble) {

		}
	}

	/*
	 *	You can reset the line color at any time
	 *
	 *  @param color  the color of the background line
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/*
	 *  Calculate the line color by making the selection color lighter
	 *
	 *  @return the color of the background line
	 */
	public void setLighter(Color color) {
		int red   = Math.min(255, (int)(color.getRed() * 1.2));
		int green = Math.min(255, (int)(color.getGreen() * 1.2));
		int blue  = Math.min(255, (int)(color.getBlue() * 1.2));
		this.setColor(new Color(red, green, blue));
	}

	public void disable() {
		if(this.highlighter != null) {
			this.component.getHighlighter().removeHighlight(this.highlighter);
			this.component.repaint();
			this.highlighter = null;
		}
	}

	//  Paint the background highlight

	public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
		try {
			Rectangle r = c.modelToView(this.position);
			g.setColor(this.color);
			g.fillRect(0, r.y, c.getWidth(), r.height);

			if(this.lastView == null)
				this.lastView = r;
		}
		catch(BadLocationException ble) {
			System.out.println(ble);
		}
	}

	public int getPosition() {
		return this.position;
	}
}
