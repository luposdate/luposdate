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
package lupos.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import lupos.datastructures.items.literal.LiteralFactory;
import lupos.engine.evaluators.EvaluatorCreator;
import lupos.engine.evaluators.QueryEvaluator;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoin;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoinNonStandardSPARQL;
import lupos.misc.FileHelper;
import lupos.sparql1_1.operatorgraph.ServiceApproaches;
import xpref.IXPref;
import xpref.XPref;
import xpref.datatypes.BooleanDatatype;
import xpref.formatter.HTMLFormatter;
import xpref.util.CommandLineOptionException;

public class GUI implements IXPref {

	public static XPref pref;
	public static boolean accessToFileSystem;
	static protected Image icon;
	static protected ReentrantLock lock = new ReentrantLock();
	static public boolean editorPane = true;

	public static void main(String[] args) throws Exception {
		final String dataFile = (args.length > 0) ? args[0] : "";
		final String queryFile = (args.length > 1) ? args[1] : "";
		if (args.length > 2) {
			final String[] argsShortened = new String[args.length - 2];
			System.arraycopy(args, 2, argsShortened, 0, args.length - 2);
			args = argsShortened;
		} else
			args = new String[] {};

		final JTextComponent textarea = editorPane ? new JEditorPane("text/html", "") : new JTextArea();

		final StringBuilder content = new StringBuilder();
		if (editorPane)
			content.append("<html><code>");

		textarea.setEditable(false);
		final JPanel panel = new JPanel(new BorderLayout());

		final JButton buttonPref = new JButton("Preferences");
		buttonPref.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					pref.showDialog(accessToFileSystem);
				} catch (final Exception e1) {
					System.err.println(e1);
					e1.printStackTrace();
				}
			}
		});
		final JButton buttonGenerateDoc = new JButton("Generate Doc");
		buttonGenerateDoc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					lock.lock();
					try {
						final HTMLFormatter htmlformatter = new HTMLFormatter(GUI.pref);
						htmlformatter.setShortenDataTypes(true);
						new GUI.BrowserForCommandLineOptions(GUI.pref
								.parseCommandLineOptions(
										new String[] { "--helpall" },
										htmlformatter), "text/html",
										"Command Line Options for GUI", false);
					} catch (final CommandLineOptionException e1) {
						System.err.println(e1);
						e1.printStackTrace();
					} catch (final Exception e1) {
						System.err.println(e1);
						e1.printStackTrace();
					}
				} finally {
					lock.unlock();
				}
			}
		});
		final JPanel panel3 = new JPanel(new FlowLayout(5, 0, 5));
		panel3.add(buttonGenerateDoc);
		panel3.add(buttonPref);

		final JPanel panel5 = new JPanel(new BorderLayout());
		panel5.add(panel3, BorderLayout.EAST);

		final JPanel panel6 = new JPanel(new FlowLayout(5, 0, 5));
		panel6.add(new JLabel(" Query Evaluator: "));

		final JComboBox comboBoxEvaluator = new JComboBox(EvaluatorCreator.EVALUATORS.values());

		comboBoxEvaluator.setLightWeightPopupEnabled(false);
		panel6.add(comboBoxEvaluator);

		panel5.add(panel6, BorderLayout.WEST);

		panel.add(panel5, BorderLayout.NORTH);

		final TextField textfieldDataFile = createFileChooser(" Data File: ",
				dataFile, panel);

		final JPanel panel2 = new JPanel(new BorderLayout());

		final TextField textfieldQueryFile = createFileChooser(" Query File: ",
				queryFile, panel2);

		panel.add(panel2, BorderLayout.SOUTH);

		final JPanel outerPanel = new JPanel(new BorderLayout());
		outerPanel.add(panel, BorderLayout.NORTH);

		initJFrame(
				args,
				"preferencesLUPOSDATECore.xml",
				"LUPOSDATE Query Evaluators",
				new ActionListener() {
					volatile boolean processingQuery = false;

					@Override
					public void actionPerformed(final ActionEvent e) {
						if (!this.processingQuery) {
							try {
								content.delete(0, content.length());
								content.append("<html><code>");
								final QueryEvaluator evaluator =((EvaluatorCreator.EVALUATORS) comboBoxEvaluator.getSelectedItem()).create();
								this.processingQuery = true;
								// do some initialization for federated queries...
								ServiceApproaches serviceApproach = xpref.datatypes.EnumDatatype.getFirstValue("serviceCallApproach");
								FederatedQueryBitVectorJoin.APPROACH bitVectorApproach = xpref.datatypes.EnumDatatype.getFirstValue("serviceCallBitVectorApproach");
								bitVectorApproach.setup();
								serviceApproach.setup();
								FederatedQueryBitVectorJoin.substringSize = xpref.datatypes.IntegerDatatype.getFirstValue("serviceCallBitVectorSize");
								FederatedQueryBitVectorJoinNonStandardSPARQL.bitvectorSize = FederatedQueryBitVectorJoin.substringSize;
								LiteralFactory.semanticInterpretationOfLiterals = xpref.datatypes.BooleanDatatype.getFirstValue("semanticInterpretationOfDatatypes");
								System.out.println("Configuration:" + pref.toString() + "\n\n");
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										final Thread thread = new Thread() {
											@Override
											public void run() {
												QueryEvaluator._main(evaluator,
														textfieldDataFile
														.getText(),
														textfieldQueryFile
														.getText());
												processingQuery = false;
											}
										};
										thread.start();
									}
								});
							} catch (final Exception e1) {
								System.err.println(e1);
								e1.printStackTrace();
							}
						} else {
							JOptionPane
							.showMessageDialog(
									null,
									"A query is currently processed.\nPlease try again after current query processing has finished...");
						}
					}

				}, outerPanel, BorderLayout.NORTH, textarea, content,
				new IXPref() {
					@Override
					public void preferencesChanged() {
						// nothing to update...
					}

				});
	}

	public static Image getIcon(final boolean accessToFileSystem_param) {
		URL url = GUI.class.getResource("/icons/demo.gif");
		if (accessToFileSystem_param)
			return new ImageIcon(url.getFile()).getImage();
		else
			return new ImageIcon(url).getImage();
	}

	private static JPanel getMainPanel(final ActionListener startButtonAction,
			final JTextComponent textarea, final StringBuilder content,
			final JPanel panelNorth, final String borderLayoutConstant) {
		final JPanel mainpanel = new JPanel(new BorderLayout());

		final JPanel panel = new JPanel(new BorderLayout());

		panel.add(panelNorth, borderLayoutConstant);

		final JPanel panelCenter = new JPanel();
		final JButton buttonStart = new JButton("Start");
		buttonStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				buttonStart.setEnabled(false);
				startButtonAction.actionPerformed(e);
				try {
					lock.lock();
					textarea.setText(content.toString());
				} finally {
					lock.unlock();
				}
				buttonStart.setEnabled(true);
			}
		});

		panelCenter.add(buttonStart);
		panel.add(panelCenter, BorderLayout.CENTER);

		mainpanel.add(panel, BorderLayout.NORTH);

		final JPanel innerPanel = new JPanel(new BorderLayout());

		innerPanel.add(new JLabel(" Output"), BorderLayout.NORTH);
		innerPanel.add(new JScrollPane(textarea), BorderLayout.CENTER);

		final String lineSeparator = System.getProperty("line.separator");

		final JCheckBox checkbox = new JCheckBox(
		"Update output only after computation");
		checkbox.setSelected(true);
		checkbox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				try {
					lock.lock();
					final boolean selected = checkbox.isSelected();
					if (!selected)
						textarea.setText(content.toString());
				} finally {
					lock.unlock();
				}
			}
		});

		System.setErr(new PrintStream(new OutputStream() {

			@Override
			public void write(final int b) throws IOException {
				try {
					lock.lock();
					if (editorPane)
						content.append("<font color=\"red\">");
					content.append(new String(new byte[] { (byte) b }));
					if (editorPane)
						content.append("</font>");
					if (!checkbox.isSelected())
						textarea.setText(content.toString());
				} finally {
					lock.unlock();
				}
			}

			@Override
			public void write(final byte b[], final int off, final int len)
			throws IOException {
				try {
					lock.lock();
					final String toWrite = new String(b, off, len);
					if (toWrite.compareTo(lineSeparator) == 0) {
						if (editorPane)
							content.append("<br/>");
						content.append(lineSeparator);
					} else {
						if (editorPane) {
							content.append("<font color=\"red\">");
							content.append(GUI.replaceAllLineSeparators(
									toWrite, "<br/>"));
							content.append("</font>");
						} else
							content.append(toWrite);
					}
					if (!checkbox.isSelected())
						textarea.setText(content.toString());
				} finally {
					lock.unlock();
				}
			}
		}, true));

		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(final int b) throws IOException {
				try {
					lock.lock();
					content.append(new String(new byte[] { (byte) b }));
					if (!checkbox.isSelected())
						textarea.setText(content.toString());
				} finally {
					lock.unlock();
				}
			}

			@Override
			public void write(final byte b[], final int off, final int len)
			throws IOException {
				try {
					lock.lock();
					if (editorPane) {
						content.append("<code>");
						content.append(GUI.replaceAllLineSeparators(new String(
								b, off, len), "<br/>"));
						content.append("</code>");
					} else {
						content.append(new String(b, off, len));
					}
					if (!checkbox.isSelected())
						textarea.setText(content.toString());
				} finally {
					lock.unlock();
				}
			}
		}, true));

		final JButton buttonSaveOutput = new JButton("Save Output");

		buttonSaveOutput.addActionListener(new ActionListener() {
			final JFileChooser fileChooser = new JFileChooser();

			@Override
			public void actionPerformed(final ActionEvent e) {
				final int result = this.fileChooser.showSaveDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					final String filename = this.fileChooser.getSelectedFile()
					.getAbsolutePath();
					FileHelper.writeFile(filename, content.toString());
				}
			}

		});

		final JPanel panel7 = new JPanel(new FlowLayout());
		panel7.add(buttonSaveOutput);

		panel7.add(checkbox);

		innerPanel.add(panel7, BorderLayout.SOUTH);

		mainpanel.add(innerPanel, BorderLayout.CENTER);

		return mainpanel;
	}

	public static JFrame initJFrame(final String[] args,
			final String preferencesFile, final String title,
			final ActionListener startButtonAction, final JPanel panelNorth,
			final String borderLayoutConstant, final IXPref ixpref) {
		final JTextComponent textarea = editorPane ? new JEditorPane(
				"text/html", "") : new JTextArea();
				final StringBuilder content = new StringBuilder();
				if (editorPane)
					content.append("<html><code>");

				textarea.setEditable(false);

				return initJFrame(args, preferencesFile, title, startButtonAction,
						panelNorth, borderLayoutConstant, textarea, content, ixpref);
	}

	public static JFrame initJFrame(final String[] args,
			final String preferencesFile, final String title,
			final ActionListener startButtonAction, final JPanel panelNorth,
			final String borderLayoutConstant, final JTextComponent textArea,
			final StringBuilder content, final IXPref ixpref) {
		try {
			pref = XPref.getInstance(GUI.class.getResource("/" + preferencesFile));
			System.out.println("Preferences loaded from jar.");
			accessToFileSystem = false;
		} catch (final Exception e) {
			try {				
				pref = XPref.getInstance(new URL("file:"+GUI.class.getResource("/" + preferencesFile).getFile())); 
			} catch (final Exception e1) {
				System.out.println(e1);
				e1.printStackTrace();
				return null;
			}
			System.out.println("Preferences loaded from file system.");
			accessToFileSystem = true;
		}
		try {
			if (args.length > 0) {
				pref.parseCommandLineOptions(args);
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		try {
			if (!BooleanDatatype.getFirstValue("gui")) {
				startButtonAction.actionPerformed(null);
				return null;
			}
		} catch (final Exception e) {
			// ignore...
		}

		pref.registerComponent(ixpref);

		final JPanel panel = getMainPanel(startButtonAction, textArea, content,
				panelNorth, borderLayoutConstant);
		final JFrame frame = new JFrame();

		icon = GUI.getIcon(accessToFileSystem);
		frame.setIconImage(icon);

		frame.setTitle(title);

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		panel.setMinimumSize(panel.getPreferredSize());
		frame.setContentPane(panel);
		final Dimension d = panel.getPreferredSize();
		d.height += frame.getPreferredSize().height;
		d.width += frame.getPreferredSize().width;
		frame.setMinimumSize(d);

		frame.setVisible(true);
		return frame;
	}

	public static String replaceAllLineSeparators(final String originalString,
			final String replacement) {
		final StringBuilder newString = new StringBuilder();
		for (final char c : originalString.toCharArray()) {
			if (c == '\n') {
				newString.append(replacement);
				newString.append('\n');
			} else
				newString.append(c);
		}
		return newString.toString();
	}

	private static TextField createFileChooser(final String labeltext,
			final String initContent, final JPanel panel) {
		final TextField textfield = new TextField(initContent);

		final JButton button = new JButton("...");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser fileChooser = new JFileChooser(textfield
						.getText());

				// Show open file dialog
				final int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					textfield.setText(fileChooser.getSelectedFile()
							.getAbsolutePath());
				}

			}

		});
		final JPanel internalpanel = new JPanel(new BorderLayout());
		internalpanel.add(new JLabel(labeltext), BorderLayout.WEST);
		internalpanel.add(textfield, BorderLayout.CENTER);
		internalpanel.add(button, BorderLayout.EAST);

		panel.add(internalpanel, BorderLayout.CENTER);

		return textfield;
	}

	@Override
	public void preferencesChanged() {
		// nothing to update...
	}

	public static class BrowserForCommandLineOptions extends Browser {
		private static final long serialVersionUID = -2074132507314482398L;

		public BrowserForCommandLineOptions(final String content,
				final String type, final String title,
				final boolean programExitOnWindowClosing) {
			super(content, type, title, programExitOnWindowClosing, null,
					getPanelSouth());
		}

		private static JPanel getPanelSouth() {
			final JPanel panel = new JPanel(new GridBagLayout());
			final JButton buttonSaveHTMLPage = new JButton("Save HTML Page");

			buttonSaveHTMLPage.addActionListener(new ActionListener() {
				final JFileChooser fileChooser = new JFileChooser();

				@Override
				public void actionPerformed(final ActionEvent e) {
					final JButton button = (JButton) e.getSource();
					final Browser browser = (Browser) button.getParent()
					.getParent().getParent().getParent().getParent()
					.getParent();
					final int result = this.fileChooser.showSaveDialog(null);
					if (result == JFileChooser.APPROVE_OPTION) {
						final String filename = this.fileChooser.getSelectedFile().getAbsolutePath();
						FileHelper.writeFile(filename, browser.htmlPane.getText());
					}
				}

			});
			panel.add(buttonSaveHTMLPage);
			return panel;
		}

		@Override
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
						try {
							final Document doc = pane.getDocument();
							final String content = doc.getText(0, doc
									.getLength());
							String searchString = " \n"
								+ e.getDescription().substring(1);
							int pos = content.indexOf(searchString);
							if (pos == -1) {
								searchString = "Data types:\n"
									+ e.getDescription().substring(1);
								pos = content.indexOf(searchString);
							}

							final Rectangle r = pane.modelToView(pos);
							System.out.println();
							r.y += pane.getVisibleRect().height;
							pane.scrollRectToVisible(r);
							// pane.setCaretPosition(pos);
						} catch (final Throwable t2) {
							this.warnUser("Can't follow link to "
									+ e.getDescription() + ": " + t2);

							t.printStackTrace();
						}
					}
				}
			}
		}
	}
}
