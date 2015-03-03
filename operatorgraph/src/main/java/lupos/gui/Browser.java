
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
package lupos.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
public class Browser extends JFrame implements HyperlinkListener {
	private static final long serialVersionUID = 1L;

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		if (args.length == 0) {
			new Browser("http://www.google.de", "Simple Swing Browser", true);
		} else {
			new Browser(args[0], "Simple Swing Browser", true);
		}
	}

	protected JEditorPane htmlPane;

	// private String initialURL;

	/**
	 * <p>Constructor for Browser.</p>
	 *
	 * @param initialURL a {@link java.lang.String} object.
	 * @param title a {@link java.lang.String} object.
	 * @param programExitOnWindowClosing a boolean.
	 */
	public Browser(final String initialURL, final String title,
			final boolean programExitOnWindowClosing) {
		super(title);

		// this.initialURL = initialURL;

		if (programExitOnWindowClosing) {
			this.addWindowListener(new ExitListener());
		}

		try {
			this.htmlPane = new JEditorPane(initialURL);
			this.htmlPane.setEditable(false);
			this.htmlPane.addHyperlinkListener(this);

			final JScrollPane scrollPane = new JScrollPane(htmlPane);

			this.getContentPane().add(scrollPane, BorderLayout.CENTER);
		} catch (final IOException ioe) {
			this.warnUser("Can't build HTML pane for " + initialURL + ": "
					+ ioe);
		}

		final Dimension screenSize = this.getToolkit().getScreenSize();

		final int width = screenSize.width * 8 / 10;
		final int height = screenSize.height * 8 / 10;

		this.setBounds(width / 8, height / 8, width, height);
		this.setVisible(true);
	}

	/**
	 * <p>Constructor for Browser.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param type a {@link java.lang.String} object.
	 * @param title a {@link java.lang.String} object.
	 * @param programExitOnWindowClosing a boolean.
	 */
	public Browser(final String content, final String type, final String title,
			final boolean programExitOnWindowClosing) {
		this(content, type, title, programExitOnWindowClosing, null, null);
	}

	/**
	 * <p>Constructor for Browser.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param type a {@link java.lang.String} object.
	 * @param title a {@link java.lang.String} object.
	 * @param programExitOnWindowClosing a boolean.
	 * @param panelNorth a {@link javax.swing.JPanel} object.
	 * @param panelSouth a {@link javax.swing.JPanel} object.
	 */
	public Browser(final String content, final String type, final String title,
			final boolean programExitOnWindowClosing, final JPanel panelNorth,
			final JPanel panelSouth) {
		super(title);

		// this.initialURL = initialURL;

		if (programExitOnWindowClosing) {
			this.addWindowListener(new ExitListener());
		}

		this.htmlPane = new JEditorPane(type, content);
		this.htmlPane.setEditable(false);
		this.htmlPane.addHyperlinkListener(this);

		final JScrollPane scrollPane = new JScrollPane(htmlPane);

		if (panelNorth != null || panelSouth != null) {
			final JPanel panelMain = new JPanel(new BorderLayout());
			if (panelNorth != null)
				panelMain.add(panelNorth, BorderLayout.NORTH);
			panelMain.add(scrollPane, BorderLayout.CENTER);
			if (panelSouth != null)
				panelMain.add(panelSouth, BorderLayout.SOUTH);
			this.getContentPane().add(panelMain, BorderLayout.CENTER);
		} else
			this.getContentPane().add(scrollPane, BorderLayout.CENTER);

		final Dimension screenSize = this.getToolkit().getScreenSize();

		final int width = screenSize.width * 8 / 10;
		final int height = screenSize.height * 8 / 10;

		this.setBounds(width / 8, height / 8, width, height);
		this.setVisible(true);
	}

	/** {@inheritDoc} */
	public void hyperlinkUpdate(final HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			final JEditorPane pane = (JEditorPane) e.getSource();

			if (e instanceof HTMLFrameHyperlinkEvent) {
				final HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
				final HTMLDocument doc = (HTMLDocument) pane.getDocument();
				doc.processHTMLFrameHyperlinkEvent(evt);
			} else {
				try {
					final String s = e.getURL().toExternalForm();
					pane.setPage(s);
				} catch (final Throwable t) {
					this.warnUser("Can't follow link to " + e.getDescription()
							+ ": " + t);

					t.printStackTrace();
				}
			}
		}
	}

	/**
	 * <p>warnUser.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 */
	protected void warnUser(final String message) {
		JOptionPane.showMessageDialog(this, message, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	public class ExitListener extends WindowAdapter {
		@Override
		public void windowClosing(final WindowEvent event) {
			System.exit(0);
		}
	}
}
