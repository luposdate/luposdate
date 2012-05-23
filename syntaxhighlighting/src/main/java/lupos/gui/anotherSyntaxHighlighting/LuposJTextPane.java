/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
import java.awt.Dimension;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

/**
 * Additional LuposJTextPane extends JTextPane
 *
 */
public class LuposJTextPane extends JTextPane {
	private static final long serialVersionUID = 1L;
	private LuposDocument doc = null;
	private Color errorLineColor;
	private ErrorLinePainter lp_error = null;
	private volatile boolean repaint = true;
	private boolean errorLineActive = true;

	/**
	 * Constructor for LuposJTextPane
	 */
	public LuposJTextPane() {
		super();
	}

	/**
	 * Constructor for LuposJTextPane
	 * @param doc StyledDocument
	 */
	public LuposJTextPane(final StyledDocument doc) {
		super(doc);
	}

	/**
	 * Constructor for LuposJTextPane
	 * @param doc {@link LuposDocument}
	 */
	public LuposJTextPane(final LuposDocument doc) {
		super(doc);

		this.doc = doc;

		this.doc.setLuposJTextPane(this);
	}

	/**
	 * Setter for size
	 * @param d set the {@link Dimension}
	 */
	public void setSize(final Dimension d) {
		if (d.width < getParent().getSize().width)
			d.width = getParent().getSize().width;

		super.setSize(d);
	}

	/**
	 * Getter for width
	 * @return false
	 */
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	/**
	 * Setter for Text, repaint off while writing
	 * @param t String t as text 
	 */
	public void setText(final String t) {
		this.setRepaint(false);
		
		if (this.doc != null) {
			this.doc.setIgnoreColoring(true);
		}

		super.setText(t);

		if (this.doc != null) {
			this.doc.setIgnoreColoring(false);
			this.doc.colorOneTimeAll();
		}

		this.setRepaint(true);
		this.repaint();
	}

	@Override
	public void repaint() {
		if (this.repaint){
			super.repaint();
		}
			
	}
	
	/**
	 * Getter for repaint
	 * @return repaint
	 */
	public boolean getRepaint() {
		return repaint;
	}

	/**
	 * Setter for repaint
	 * synchronized because its run in a thread
	 * @param repaint the repaint to set
	 */
	public synchronized void setRepaint(final boolean repaint) {
		this.repaint = repaint;
	}

	/**
	 * calculates the lineStartOffset
	 * because the lines have a different length
	 * @param line the line to calculate the offset
	 * @return the start offset
	 */
	public int getLineStartOffset(final int line) {
		int currentLine = 1;
		int pos = 0;
		final int length = this.getText().length();
		for (int i = 0; i < length; ++i) {
			try {
				if (this.getText(i, 1).equalsIgnoreCase("\n"))
					++currentLine;

				if (currentLine == line) {
					pos = i;

					break;
				}
			} catch (final BadLocationException e) {
				e.printStackTrace();
			}
		}

		return pos;
	}

	/**
	 * calculates the lineEndOffset
	 * because the lines have a different length
	 * @param line the line to calculate the offset
	 * @return the end offset
	 */
	public int getLineEndOffset(final int line) {
		int currentLine = 1;
		int pos = 0;

		for (int i = 0; i < this.getText().length(); ++i) {
			try {
				if (this.getText(i, 1).equalsIgnoreCase("\n")) {
					++currentLine;

					if (currentLine == line + 1)
						pos = i + 1;
				}
			} catch (final BadLocationException e) {
				e.printStackTrace();
			}
		}

		return pos;
	}

	/**
	 * Get Line of a specified position
	 * @param pos position
	 * @return the line where the position is
	 */
	public int getLine(final int pos) {
		int line = 0;

		for (int i = 0; i < this.getText().length(); ++i) {
			if (i == pos)
				break;

			try {
				if (this.getText(i, 1).equalsIgnoreCase("\n"))
					++line;
			} catch (final BadLocationException e) {
				e.printStackTrace();
			}
		}

		return line;
	}

	/**
	 * Calculate the Error
	 * @param line Line
	 * @param column Column
	 */
	public void setErrorPosition(final int line, final int column) {
		// calculate position...
		int pos = this.getLineStartOffset(line) + column;

		if (pos > this.getText().length())
			pos = this.getText().length();

		// go to position...
		this.setCaretPosition(pos);
		this.grabFocus();

		this.disableErrorLine();

		this.enableErrorLine(pos);
	}

	/**
	 * Updater for error position
	 */
	public void updateErrorPosition() {
		if (this.lp_error != null) {
			final int pos = this.lp_error.getPosition();

			this.disableErrorLine();

			this.enableErrorLine(pos);
		}
	}
	
	/**
	 * Show an error line  
	 * @param pos position of errorLine
	 */
	private void enableErrorLine(final int pos) {
		if (this.errorLineActive) {
			this.lp_error = new ErrorLinePainter(this, pos, this.errorLineColor);
		}
	}

	/**
	 * Disable showing error line
	 */
	public void disableErrorLine() {
		if (this.lp_error != null) {
			this.lp_error.disable();

			this.lp_error = null;
		}
	}

	/**
	 * Setter for errorlineStatus
	 * @param status bool to set errorLine
	 */
	public void setErrorLineStatus(final boolean status) {
		this.errorLineActive = status;
	}

	/**
	 * Setter for errorLineColor
	 * @param errorLineColor Color to set Errorline
	 */
	public void setErrorLineColor(final Color errorLineColor) {
		this.errorLineColor = errorLineColor;
	}
}