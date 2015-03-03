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
package lupos.gui.operatorgraph.visualeditor.visualrif.util;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.Utilities;

/**
 *  This class will display line numbers for a related text component. The text
 *  component must use the same line height for each line. TextLineNumber
 *  supports wrapped lines and will highlight the line number of the current
 *  line in the text component.
 *
 *  This class was designed to be used as a component added to the row header
 *  of a JScrollPane.
 */
public class TextLineNumber extends JPanel
	implements CaretListener, DocumentListener, PropertyChangeListener
{
	public final static float LEFT = 0.0f;
	public final static float CENTER = 0.5f;
	public final static float RIGHT = 1.0f;

	private final static Border OUTER = new MatteBorder(0, 0, 0, 2, Color.GRAY);

	private final static int HEIGHT = Integer.MAX_VALUE - 1000000;

	//  Text component this TextTextLineNumber component is in sync with

	private final JTextComponent component;

	//  Properties that can be changed

	private boolean updateFont;
	private int borderGap;
	private Color currentLineForeground;
	private float digitAlignment;
	private int minimumDisplayDigits;

	//  Keep history information to reduce the number of times the component
	//  needs to be repainted

    private int lastDigits;
    private int lastHeight;
    private int lastLine;

	private HashMap<String, FontMetrics> fonts;

	/**
	 *	Create a line number component for a text component. This minimum
	 *  display width will be based on 3 digits.
	 *
	 *  @param component  the related text component
	 */
	public TextLineNumber(final JTextComponent component)
	{
		this(component, 3);
	}

	/**
	 *	Create a line number component for a text component.
	 *
	 *  @param component  the related text component
	 *  @param minimumDisplayDigits  the number of digits used to calculate
	 *                               the minimum width of the component
	 */
	public TextLineNumber(final JTextComponent component, final int minimumDisplayDigits)
	{
		this.component = component;

		this.setFont( component.getFont() );

		this.setBorderGap( 5 );
		this.setCurrentLineForeground( Color.RED );
		this.setDigitAlignment( RIGHT );
		this.setMinimumDisplayDigits( minimumDisplayDigits );

		component.getDocument().addDocumentListener(this);
		component.addCaretListener( this );
		component.addPropertyChangeListener("font", this);
	}

	/**
	 *  Gets the update font property
	 *
	 *  @return the update font property
	 */
	public boolean getUpdateFont()
	{
		return this.updateFont;
	}

	/**
	 *  Set the update font property. Indicates whether this Font should be
	 *  updated automatically when the Font of the related text component
	 *  is changed.
	 *
	 *  @param updateFont  when true update the Font and repaint the line
	 *                     numbers, otherwise just repaint the line numbers.
	 */
	public void setUpdateFont(final boolean updateFont)
	{
		this.updateFont = updateFont;
	}

	/**
	 *  Gets the border gap
	 *
	 *  @return the border gap in pixels
	 */
	public int getBorderGap()
	{
		return this.borderGap;
	}

	/**
	 *  The border gap is used in calculating the left and right insets of the
	 *  border. Default value is 5.
	 *
	 *  @param borderGap  the gap in pixels
	 */
	public void setBorderGap(final int borderGap)
	{
		this.borderGap = borderGap;
		final Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
		this.setBorder( new CompoundBorder(OUTER, inner) );
		this.lastDigits = 0;
		this.setPreferredWidth();
	}

	/**
	 *  Gets the current line rendering Color
	 *
	 *  @return the Color used to render the current line number
	 */
	public Color getCurrentLineForeground()
	{
		return this.currentLineForeground == null ? this.getForeground() : this.currentLineForeground;
	}

	/**
	 *  The Color used to render the current line digits. Default is Coolor.RED.
	 *
	 *  @param currentLineForeground  the Color used to render the current line
	 */
	public void setCurrentLineForeground(final Color currentLineForeground)
	{
		this.currentLineForeground = currentLineForeground;
	}

	/**
	 *  Gets the digit alignment
	 *
	 *  @return the alignment of the painted digits
	 */
	public float getDigitAlignment()
	{
		return this.digitAlignment;
	}

	/**
	 *  Specify the horizontal alignment of the digits within the component.
	 *  Common values would be:
	 *
	 *  TextLineNumber.LEFT
	 *  TextLineNumber.CENTER
	 *  TextLineNumber.RIGHT (default)
	 */
	public void setDigitAlignment(final float digitAlignment)
	{
		this.digitAlignment =
			digitAlignment > 1.0f ? 1.0f : digitAlignment < 0.0f ? -1.0f : digitAlignment;
	}

	/**
	 *  Gets the minimum display digits
	 *
	 *  @return the minimum display digits
	 */
	public int getMinimumDisplayDigits()
	{
		return this.minimumDisplayDigits;
	}

	/**
	 *  Specify the mimimum number of digits used to calculate the preferred
	 *  width of the component. Default is 3.
	 *
	 *  @param minimumDisplayDigits  the number digits used in the preferred
	 *                               width calculation
	 */
	public void setMinimumDisplayDigits(final int minimumDisplayDigits)
	{
		this.minimumDisplayDigits = minimumDisplayDigits;
		this.setPreferredWidth();
	}

	/**
	 *  Calculate the width needed to display the maximum line number
	 */
	private void setPreferredWidth()
	{
		final Element root = this.component.getDocument().getDefaultRootElement();
		final int lines = root.getElementCount();
		final int digits = Math.max(String.valueOf(lines).length(), this.minimumDisplayDigits);

		//  Update sizes when number of digits in the line number changes

		if (this.lastDigits != digits)
		{
			this.lastDigits = digits;
			final FontMetrics fontMetrics = this.getFontMetrics( this.getFont() );
			final int width = fontMetrics.charWidth( '0' ) * digits;
			final Insets insets = this.getInsets();
			final int preferredWidth = insets.left + insets.right + width;

			final Dimension d = this.getPreferredSize();
			d.setSize(preferredWidth, HEIGHT);
			this.setPreferredSize( d );
			this.setSize( d );
		}
	}

	/**
	 *  Draw the line numbers
	 */
	@Override
	public void paintComponent(final Graphics g)
	{
		super.paintComponent(g);

		//	Determine the width of the space available to draw the line number

		final FontMetrics fontMetrics = this.component.getFontMetrics( this.component.getFont() );
		final Insets insets = this.getInsets();
		final int availableWidth = this.getSize().width - insets.left - insets.right;

		//  Determine the rows to draw within the clipped bounds.

		final Rectangle clip = g.getClipBounds();
		int rowStartOffset = this.component.viewToModel( new Point(0, clip.y) );
		final int endOffset = this.component.viewToModel( new Point(0, clip.y + clip.height) );

		while (rowStartOffset <= endOffset)
		{
			try
            {
    			if (this.isCurrentLine(rowStartOffset)) {
					g.setColor( this.getCurrentLineForeground() );
				} else {
					g.setColor( this.getForeground() );
				}

    			//  Get the line number as a string and then determine the
    			//  "X" and "Y" offsets for drawing the string.

    			final String lineNumber = this.getTextLineNumber(rowStartOffset);
    			final int stringWidth = fontMetrics.stringWidth( lineNumber );
    			final int x = this.getOffsetX(availableWidth, stringWidth) + insets.left;
				final int y = this.getOffsetY(rowStartOffset, fontMetrics);
    			g.drawString(lineNumber, x, y);

    			//  Move to the next row

    			rowStartOffset = Utilities.getRowEnd(this.component, rowStartOffset) + 1;
			}
			catch(final Exception e) {}
		}
	}

	/*
	 *  We need to know if the caret is currently positioned on the line we
	 *  are about to paint so the line number can be highlighted.
	 */
	private boolean isCurrentLine(final int rowStartOffset)
	{
		final int caretPosition = this.component.getCaretPosition();
		final Element root = this.component.getDocument().getDefaultRootElement();

		if (root.getElementIndex( rowStartOffset ) == root.getElementIndex(caretPosition)) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 *	Get the line number to be drawn. The empty string will be returned
	 *  when a line of text has wrapped.
	 */
	protected String getTextLineNumber(final int rowStartOffset)
	{
		final Element root = this.component.getDocument().getDefaultRootElement();
		final int index = root.getElementIndex( rowStartOffset );
		final Element line = root.getElement( index );

		if (line.getStartOffset() == rowStartOffset) {
			return String.valueOf(index + 1);
		} else {
			return "";
		}
	}

	/*
	 *  Determine the X offset to properly align the line number when drawn
	 */
	private int getOffsetX(final int availableWidth, final int stringWidth)
	{
		return (int)((availableWidth - stringWidth) * this.digitAlignment);
	}

	/*
	 *  Determine the Y offset for the current row
	 */
	private int getOffsetY(final int rowStartOffset, final FontMetrics fontMetrics)
		throws BadLocationException
	{
		//  Get the bounding rectangle of the row

		final Rectangle r = this.component.modelToView( rowStartOffset );
		final int lineHeight = fontMetrics.getHeight();
		final int y = r.y + r.height;
		int descent = 0;

		//  The text needs to be positioned above the bottom of the bounding
		//  rectangle based on the descent of the font(s) contained on the row.

		if (r.height == lineHeight)  // default font is being used
		{
			descent = fontMetrics.getDescent();
		}
		else  // We need to check all the attributes for font changes
		{
			if (this.fonts == null) {
				this.fonts = new HashMap<String, FontMetrics>();
			}

			final Element root = this.component.getDocument().getDefaultRootElement();
			final int index = root.getElementIndex( rowStartOffset );
			final Element line = root.getElement( index );

			for (int i = 0; i < line.getElementCount(); i++)
			{
				final Element child = line.getElement(i);
				final AttributeSet as = child.getAttributes();
				final String fontFamily = (String)as.getAttribute(StyleConstants.FontFamily);
				final Integer fontSize = (Integer)as.getAttribute(StyleConstants.FontSize);
				final String key = fontFamily + fontSize;

				FontMetrics fm = this.fonts.get( key );

				if (fm == null)
				{
					final Font font = new Font(fontFamily, Font.PLAIN, fontSize);
					fm = this.component.getFontMetrics( font );
					this.fonts.put(key, fm);
				}

				descent = Math.max(descent, fm.getDescent());
			}
		}

		return y - descent;
	}

//
//  Implement CaretListener interface
//
	@Override
	public void caretUpdate(final CaretEvent e)
	{
		//  Get the line the caret is positioned on

		final int caretPosition = this.component.getCaretPosition();
		final Element root = this.component.getDocument().getDefaultRootElement();
		final int currentLine = root.getElementIndex( caretPosition );

		//  Need to repaint so the correct line number can be highlighted

		if (this.lastLine != currentLine)
		{
			this.repaint();
			this.lastLine = currentLine;
		}
	}

//
//  Implement DocumentListener interface
//
	@Override
	public void changedUpdate(final DocumentEvent e)
	{
		this.documentChanged();
	}

	@Override
	public void insertUpdate(final DocumentEvent e)
	{
		this.documentChanged();
	}

	@Override
	public void removeUpdate(final DocumentEvent e)
	{
		this.documentChanged();
	}

	/*
	 *  A document change may affect the number of displayed lines of text.
	 *  Therefore the lines numbers will also change.
	 */
	private void documentChanged()
	{
		//  Preferred size of the component has not been updated at the time
		//  the DocumentEvent is fired

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
        		final int preferredHeight = TextLineNumber.this.component.getPreferredSize().height;

				//  Document change has caused a change in the number of lines.
				//  Repaint to reflect the new line numbers

        		if (TextLineNumber.this.lastHeight != preferredHeight)
        		{
        			TextLineNumber.this.setPreferredWidth();
        			TextLineNumber.this.repaint();
        			TextLineNumber.this.lastHeight = preferredHeight;
        		}
			}
		});
	}

//
//  Implement PropertyChangeListener interface
//
	@Override
	public void propertyChange(final PropertyChangeEvent evt)
	{
		if (evt.getNewValue() instanceof Font)
		{
			if (this.updateFont)
			{
				final Font newFont = (Font) evt.getNewValue();
				this.setFont(newFont);
				this.lastDigits = 0;
				this.setPreferredWidth();
			}
			else
			{
				this.repaint();
			}
		}
	}
}