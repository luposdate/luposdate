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
package lupos.event.action.send;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import lupos.event.action.send.Send;
import lupos.event.util.data.Handler;

import com.sun.awt.AWTUtilities;

/**
 * A toplevel notification box, sliding in vertically from the upper or lower
 * left corner, right corner or middle. Capable of displaying plain text, html
 * or rtf. Stays for a given amount of time or as long as the mouse is hovering
 * over it. Transparency is reduced while the mouse is hovering over it,
 * allowing to read it more easily on demand while being less disturbing
 * otherwise.
 */
@SuppressWarnings("restriction")
public class SlidingWindow extends JWindow implements ActionListener,
		MouseListener, Send {

	/**
	 * Convenience type for easier content type selection of displayed messages.
	 */
	public enum ContentType {
		PLAIN_TEXT, HTML, RTF
	}

	/**
	 * Origin screen border (defining the side of the screen from which the
	 * window slides in).
	 */
	public enum ScreenBorder {
		Top, Bottom
	}

	/**
	 * Popup position on the origin border (left corner, right corner or in the
	 * middle).
	 */
	public enum BorderPosition {
		Left, Middle, Right
	}

	/**
	 * Animation states.
	 */
	private enum AnimState {
		IDLE, SLIDING_OUT, DISPLAYING, SLIDING_IN
	}

	private static final long serialVersionUID = 1239937364810642875L;

	// transparency values if mouse is (not) hovering over the window
	private static final float hoveringTransparecy = 0.95f;
	private static final float normalTransparecy = 0.75f;

	// maximum window size
	private int maxHeight;
	private int maxWidth;

	// start/end height for sliding animation (reset internally based on content
	// size and max window size)
	private int startHeight = 1;
	private int endHeight;

	// animation parameters
	private Timer timer;
	private int slideStep = 1;
	private int slideTime = 1000; // time (in ms) it takes to fully slide out/in
	private int slideDelay; // (dynamically computed) timer delay to ensure the
							// slideTime at the current text size
	private int displayTime = 2000;

	// origin opffset, sliding direction
	private ScreenBorder originBorder = ScreenBorder.Top;
	private BorderPosition originPos = BorderPosition.Right;
	private int posOffset = 100;

	// textcolor is ignored for html/rtf contents
	private Color backgroundcolor = Color.YELLOW;
	private Color textcolor = Color.DARK_GRAY;

	// content type
	ContentType contentType = ContentType.HTML;

	// display components
	private JEditorPane textPane;
	private JScrollPane scrollPane;

	// current states
	private AnimState state = AnimState.IDLE;
	private boolean mouseHovering = false;

	/**
	 * Constructor. Sets maximum window width/height to (screenWidth/4) /
	 * (screenHeight/2).
	 */
	public SlidingWindow() {
		this(-1, -1);
	}

	/**
	 * Constructor.
	 * 
	 * @param maxW
	 *            Maximum window width.
	 * @param maxH
	 *            Maximum window height.
	 */
	public SlidingWindow(final int maxW, final int maxH) {
		super();

		// we want a toplevel notification window
		setAlwaysOnTop(true);
		getContentPane().setBackground(this.backgroundcolor);

		// ... with a semi-transparent background
		AWTUtilities.setWindowOpacity(this, normalTransparecy);

		// need to display the text somehow ...
		this.textPane = new JEditorPane();
		this.textPane.setContentType("text/html");
		this.textPane.setEditable(false);
		this.textPane.setOpaque(true);
		this.textPane.setBackground(this.backgroundcolor);
		this.textPane.setForeground(this.textcolor);

		this.scrollPane = new JScrollPane(this.textPane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		setLayout(new GridLayout(1, 1));
		add(this.scrollPane);

		this.textPane.addMouseListener(this);

		// determine the screen dimensions
		Rectangle screenRect = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		// set size constraints
		if (maxW > 0) {
			this.maxWidth = maxW;
		} else {
			this.maxWidth = (int) (screenRect.getWidth() / 4);
		}

		if (maxH > 0) {
			this.maxHeight = maxH;
		} else {
			this.maxHeight = (int) (screenRect.getHeight() / 2);
		}

		setSize(this.maxWidth, 1);

		// initialize animation timer
		this.timer = new Timer(100, this);
		this.timer.setInitialDelay(0);

		// hide until exec() is called
		setVisible(false);
	}

	/**
	 * Convert a {@link ContentType} value to a content type string which can be
	 * handed to {@link JEditorPane#setContentType(String)}. Default (fallback)
	 * if contentType is not HTML or RTF: "text/plain".
	 * 
	 * @param contentType_param
	 *            {@link ContentType} to be converted.
	 * @return The string representing the given {@link ContentType}.
	 */
	@SuppressWarnings("incomplete-switch")
	private String contentTypeToStr(final ContentType contentType_param) {
		String contentTypeStr = "text/plain";
		switch (contentType_param) {
		case HTML:
			contentTypeStr = "text/html; charset=UTF-8";
			Handler.install();
			break;
		case RTF:
			contentTypeStr = "text/rtf";
			break;
		}
		return contentTypeStr;
	}

	/**
	 * Change position settings.
	 * 
	 * @param border
	 *            The {@link ScreenBorder} to slide in from.
	 * @param basePos
	 *            The basic {@link BorderPosition} on the border.
	 * @param offset
	 *            A manual offset from the basePos (in px).
	 */
	public void setPosition(final ScreenBorder border,
			final BorderPosition basePos, final int offset) {
		this.originBorder = border;
		this.originPos = basePos;
		this.posOffset = offset;
	}

	/**
	 * Set the content type, i.e. the way a content string is interpreted and
	 * displayed.
	 * 
	 * @param type
	 *            {@link ContentType} to be set.
	 */
	public void setContentType(final ContentType type) {
		this.contentType = type;
	}

	/**
	 * Set the display time, i.e. the amount of time between sliding in and
	 * sliding out again.
	 * 
	 * @param ms
	 *            Number of milliseconds
	 */
	public void setDisplayTime(final int ms) {
		this.displayTime = ms;
	}

	/**
	 * Slide out, displaying the given content based on the currently set
	 * content type, display time and position parameters.
	 * 
	 * @param content
	 * @see #setPosition(ScreenBorder, BorderPosition, int)
	 * @see #setContentType(ContentType)
	 * @see #setDisplayTime(int)
	 */
	public void exec(final String content) {
		exec(content, this.displayTime, this.contentType);
	}

	/**
	 * Slide out, display the given content according to the given content type
	 * for the specified time (in ms), then sliding in again. Global position
	 * parameters are used.
	 * 
	 * @param content
	 *            Content to be displayed according to contentType.
	 * @param time
	 *            Time (in ms) to display the text (time between sliding out and
	 *            in again).
	 * @param contentType_param
	 *            {@link ContentType} telling how the content is to be
	 *            interpreted and displayed.
	 * @see #setPosition(ScreenBorder, BorderPosition, int)
	 */
	public void exec(final String content, final int time,
			final ContentType contentType_param) {
		exec(content, time, contentTypeToStr(contentType_param));
	}

	/**
	 * Slide out from a specified border, display the given content according to
	 * the given content type for the specified time (int ms), then sliding in
	 * again. Overriding all global settings (content type, display time,
	 * position paremters).
	 * 
	 * @param content
	 *            Content to be displayed according to contentType.
	 * @param time
	 *            Time (in ms) to display the text (time between sliding out and
	 *            in again).
	 * @param contentType_param
	 *            {@link ContentType}, specifying how the content is to be
	 *            interpreted and displayed.
	 * @param fromBorder
	 *            {@link ScreenBorder} from which to slide in.
	 * @param basePos
	 *            {@link BorderPosition} on fromBorder.
	 * @param positionOffset
	 *            Manual offset from basePos (in px).
	 */
	public void exec(final String content, final int time,
			final ContentType contentType_param, final ScreenBorder fromBorder,
			final BorderPosition basePos, final int positionOffset) {
		exec(content, time, contentTypeToStr(contentType_param), fromBorder, basePos,
				positionOffset);
	}

	/**
	 * Slide out from a specified border, display the given content according to
	 * the given content type for the specified time (int ms), then sliding in
	 * again. Overriding all global settings (content type, display time,
	 * position paremters).
	 * 
	 * @param content
	 *            Content to be displayed according to contentType.
	 * @param time
	 *            Time (in ms) to display the text (time between sliding out and
	 *            in again).
	 * @param contentType_param
	 *            Content type (string) to be used. Currently supported:
	 *            "text/plain", "text/html", "text/rtf".
	 * @param fromBorder
	 *            {@link ScreenBorder} from which to slide in.
	 * @param basePos
	 *            {@link BorderPosition} on fromBorder.
	 * @param positionOffset
	 *            Manual offset from basePos (in px).
	 */
	public void exec(final String content, final int time,
			final String contentType_param, final ScreenBorder fromBorder,
			final BorderPosition basePos, final int positionOffset) {
		setPosition(fromBorder, basePos, positionOffset);
		exec(content, time, contentType_param);
	}

	/**
	 * Slide out, display the given content according to the given content type
	 * for the specified time (in ms), then sliding in again. Global position
	 * parameters are used.
	 * 
	 * @param content
	 *            Content to be displayed.
	 * @param time
	 *            Time (in ms) to display the text (time between sliding out and
	 *            in again).
	 * @param contentType_param
	 *            String containing the content type, telling how the content is
	 *            to be interpreted and displayed.
	 * @see #setPosition(ScreenBorder, BorderPosition, int)
	 */
	public void exec(final String content, final int time,
			final String contentType_param) {
		// abort if an animation is already running
		if (this.timer.isRunning())
			return;

		// unhide if hidden
		setVisible(true);

		// textPane.setText(text);
		this.displayTime = time;

		// set desired content type
		this.textPane.setContentType(contentType_param);

		// calculate size constraints
		Rectangle screenRect = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		// initial size to leave as much vertical space as possible for the
		// content
		setSize(this.maxWidth, Short.MAX_VALUE);

		// set the content so we can now obtain the text pane's preferred
		// size
		this.textPane.setText(content);

		// set preferred width, clamping to maxWidth
		int newWidth = (int) Math.min(
				this.textPane.getPreferredSize().getWidth() + 10, this.maxWidth);

		setSize(newWidth, 1);

		// compute final height, clamping to maxHeight
		this.endHeight = Math.min(
				(int) this.textPane.getPreferredSize().getHeight() + 10, this.maxHeight);

		setSize(getWidth(), this.startHeight);

		// determine origin position by origin border
		int xPos = 0;
		int yPos = 0;

		int margin = 3;
		switch (this.originBorder) {
		case Top:
			yPos = (int) screenRect.getY();
			break;

		case Bottom:
			yPos = (int) (screenRect.getY() + screenRect.getHeight());
			break;
		}

		switch (this.originPos) {
		case Left:
			xPos = (int) screenRect.getX() + margin + this.posOffset;
			break;

		case Middle:
			xPos = (int) ((screenRect.getX() + screenRect.getWidth() - getWidth()) * 0.5f)
					+ this.posOffset;
			break;

		case Right:
			xPos = (int) (screenRect.getX() + screenRect.getWidth()
					- getWidth() - margin)
					+ this.posOffset;
		}
		setLocation(xPos, yPos);

		// start animating
		this.state = AnimState.SLIDING_OUT;
		this.slideDelay = this.slideTime * this.slideStep / this.endHeight;
		this.timer.setDelay(this.slideDelay);
		this.timer.setInitialDelay(this.slideDelay);
		this.timer.restart();
	}

	// simple state machine for animation controlling
	@SuppressWarnings("fallthrough")
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (this.state) {
		case IDLE:
			break;

		case SLIDING_OUT: {
			// when the final height is reached, switch to DISPLAYING
			// mode and show scrollbars if needed
			if (getHeight() >= this.endHeight) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						SlidingWindow.this.timer.stop();
						SlidingWindow.this.state = AnimState.DISPLAYING;

						SlidingWindow.this.scrollPane
								.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						SlidingWindow.this.scrollPane
								.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

						SlidingWindow.this.timer.start();
					}
				});
			} else {
				// otherwise, grow by slideStep
				switch (this.originBorder) {
				case Bottom:
					// if coming from the bottom, we have to move the window,
					// too
					this.setLocation(getX(), getY() - this.slideStep);

				case Top:
					this.setSize(getWidth(), getHeight() + this.slideStep);
					break;
				}
			}

			break;
		}

		case DISPLAYING: {
			// restart the timer to fire after the desired displayTime has
			// elapsed, after which the window will slide back in

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					SlidingWindow.this.timer.stop();
					SlidingWindow.this.state = AnimState.SLIDING_IN;
					SlidingWindow.this.timer.setInitialDelay(SlidingWindow.this.displayTime);
					SlidingWindow.this.timer.restart();
				}
			});
			break;
		}

		case SLIDING_IN: {
			// mouse hovering makes the window slide back out
			if (this.mouseHovering) {
				this.state = AnimState.SLIDING_OUT;
			} else {
				// hide scroll bars for visual elegance
				this.scrollPane
						.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				this.scrollPane
						.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

				// if the minimal height (usually 1px) is reached, stop
				// the timer and hide the window completely
				if (getHeight() <= this.startHeight) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							SlidingWindow.this.timer.stop();
							SlidingWindow.this.state = AnimState.IDLE;
							setVisible(false);
						}
					});
				} else {
					// otherwise, shrink by slideStep
					switch (this.originBorder) {
					case Bottom:
						// if coming from the bottom, we have to move the
						// window, too
						this.setLocation(getX(), getY() + this.slideStep);

					case Top:
						this.setSize(getWidth(), getHeight() - this.slideStep);
						break;
					}
				}
			}
			break;
		}
		}
	}

	// ---------------------
	// MouseListener methods
	// ---------------------
	@Override
	public void mouseClicked(MouseEvent e) {
		// ignore...
	}

	// watch for mouseEntered/mosueExited events, so we know when the mouse is
	// hovering over the window and adjust its transparency accordingly
	@Override
	public void mouseEntered(MouseEvent e) {
		this.mouseHovering = true;

		// ... with a still semi-transparent, but more opaque background
		AWTUtilities.setWindowOpacity(this, hoveringTransparecy);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		this.mouseHovering = false;

		// ... with a semi-transparent background, a little less opaque
		AWTUtilities.setWindowOpacity(this, normalTransparecy);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// ignore
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// ignore
	}

	// ------------
	// Send methods
	// ------------
	@Override
	public void init() {
		// show an option pane to let the user choose the origin border,
		// position, offset, content type and display time
		final String[] strBorders = { "Top", "Bottom" };
		final String[] strPositions = { "Left", "Middle", "Right" };
		final String[] strContentTypes = { "Plain Text", "HTML", "RTF" };

		JComboBox cbBorder = new JComboBox(strBorders);
		JComboBox cbPosition = new JComboBox(strPositions);
		JSpinner spOffset = new JSpinner();

		JComboBox cbContentType = new JComboBox(strContentTypes);

		JSpinner spDisplayTime = new JSpinner(new SpinnerNumberModel(2500, 100,
				Integer.MAX_VALUE, 100));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(0, 2));

		mainPanel.add(new JLabel("Origin Border:"));
		mainPanel.add(cbBorder);

		mainPanel.add(new JLabel("Position:"));
		mainPanel.add(cbPosition);

		mainPanel.add(new JLabel("Position Offset:"));
		mainPanel.add(spOffset);

		mainPanel.add(new JLabel());
		mainPanel.add(new JLabel());

		mainPanel.add(new JLabel("Content Type:"));
		mainPanel.add(cbContentType);

		mainPanel.add(new JLabel());
		mainPanel.add(new JLabel());

		mainPanel.add(new JLabel("Display Time:"));
		mainPanel.add(spDisplayTime);

		JOptionPane.showMessageDialog(null, mainPanel,
				"Sliding window settings:", JOptionPane.OK_OPTION);

		// obtain actual values from selected indexes
		ScreenBorder border = ScreenBorder.Top;
		switch (cbBorder.getSelectedIndex()) {
		case 1:
			border = ScreenBorder.Bottom;
			break;
		}

		BorderPosition pos = BorderPosition.Left;
		switch (cbPosition.getSelectedIndex()) {
		case 1:
			pos = BorderPosition.Middle;
			break;
		case 2:
			pos = BorderPosition.Right;
			break;
		}
		int offset = (Integer) spOffset.getValue();

		ContentType type = ContentType.PLAIN_TEXT;
		switch (cbContentType.getSelectedIndex()) {
		case 1:
			type = ContentType.HTML;
			break;
		case 2:
			type = ContentType.RTF;
			break;
		}

		int time = (Integer) spDisplayTime.getValue();

		// apply settings
		setPosition(border, pos, offset);
		setContentType(type);
		setDisplayTime(time);
	}

	@Override
	public void sendContent(String content) {
		exec(content);
	}
}