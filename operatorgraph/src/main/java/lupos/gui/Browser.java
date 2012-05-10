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

	public static void main(final String[] args) {
		if (args.length == 0) {
			new Browser("http://www.google.de", "Simple Swing Browser", true);
		} else {
			new Browser(args[0], "Simple Swing Browser", true);
		}
	}

	protected JEditorPane htmlPane;

	// private String initialURL;

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

	public Browser(final String content, final String type, final String title,
			final boolean programExitOnWindowClosing) {
		this(content, type, title, programExitOnWindowClosing, null, null);
	}

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