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
package lupos.autocomplete.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.AbstractBorder;

import lupos.autocomplete.strategies.Strategy;
import lupos.autocomplete.strategies.StrategyManager;
import lupos.autocomplete.strategies.StrategyManager.LANGUAGE;
import lupos.gui.anotherSyntaxHighlighting.LuposDocument;

public class JTextPanePreparer extends Observable implements ActionListener, KeyListener {

	protected final JTextPane textPane;
	protected JPopupMenu popupMenu;
	protected JList jl;
	protected int index;
	protected final LuposDocument document;
	protected final StrategyManager sm;

	public JTextPanePreparer(final JTextPane textPane, final LANGUAGE language, final LuposDocument document) {
		this.textPane = textPane;
		this.document = document;
		this.sm = new StrategyManager(language, document);
		((JComponent) textPane).registerKeyboardAction(this, "open",
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE , Event.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
//		// Binding Alt F4
//		((JComponent) textPane).registerKeyboardAction(this, "close",
//				alt(KeyEvent.VK_F4), JComponent.WHEN_FOCUSED);
		this.textPane.addKeyListener(this);
	}

	// was passiert wenn tasten gedrueckt werden
	@Override
	public void actionPerformed(final ActionEvent event) {

		final String cmd = event.getActionCommand();
		try {
			if (cmd.equals("open")) {
				this.openPopup();
			} else if (cmd.equals("close")) {
				System.exit(0);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void keyTyped(final KeyEvent e) {
	}

	@SuppressWarnings("serial")
	@Override
	public void keyReleased(final KeyEvent e) {

		/*
		 * Workaround fuer den Bug mit der CaretPosition
		 */
//		if (JTextPanePreparer.this.popupMenu != null && !JTextPanePreparer.this.popupMenu.isVisible()){
//			if (JTextPanePreparer.this.textPane.getCaretPosition() == JTextPanePreparer.this.textPane.getText().length()) {
//				JTextPanePreparer.this.textPane.setCaretPosition(JTextPanePreparer.this.textPane.getCaretPosition() - 1);
//				JTextPanePreparer.this.textPane.setCaretPosition(JTextPanePreparer.this.textPane.getCaretPosition() + 1);
//			} else {
//				JTextPanePreparer.this.textPane.setCaretPosition(JTextPanePreparer.this.textPane.getCaretPosition() + 1);
//				JTextPanePreparer.this.textPane.setCaretPosition(JTextPanePreparer.this.textPane.getCaretPosition() - 1);
//			}
//		}

		/*
		 * Stellt sicher, dass Keybindings nur aktiviert sind wenn Popup
		 * sichtbar
		 *
		 * ENTER und TAB fuegen aktuelles Listenelement ein, UP und DOWN
		 * navigieren in der Liste, RIGHT und LEFT schliessen das Popup
		 */
		if ((this.popupMenu != null) && (this.popupMenu.isVisible())) {

			if((e.getKeyCode() != KeyEvent.VK_DOWN) && (e.getKeyCode() != KeyEvent.VK_UP)) {
				this.jl.setModel(this.sm.listToJList(this.textPane.getText().replaceAll("\r\n", "\n"), this.textPane.getCaretPosition()));
				this.jl.setSelectedIndex(0);
			}

			this.textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "myEnter");
			this.textPane.getActionMap().put("myEnter", new AbstractAction() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if(!JTextPanePreparer.this.popupMenu.isVisible()){
						JTextPanePreparer.this.textPane.getInputMap().clear();
						// this is the dirty way to pass "enter" to the JTextPane!
						Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new KeyEvent(
								JTextPanePreparer.this.textPane,
                                KeyEvent.KEY_PRESSED,
                                System.currentTimeMillis(),
                                0,
                                KeyEvent.VK_ENTER,
                                KeyEvent.CHAR_UNDEFINED));
						return;
					}
					if (JTextPanePreparer.this.jl.getSelectedValue() == null) {
						JTextPanePreparer.this.popupMenu.setVisible(false);
					} else {
						JTextPanePreparer.this.insertSelectedWord();
						JTextPanePreparer.this.popupMenu.setVisible(false);
					}

				}
			});

			this.textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "myTab");
			this.textPane.getActionMap().put("myTab", new AbstractAction() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					JTextPanePreparer.this.insertSelectedWord();
					JTextPanePreparer.this.popupMenu.setVisible(false);
				}
			});

			this.textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "myUp");
			this.textPane.getActionMap().put("myUp", new AbstractAction() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if(!JTextPanePreparer.this.popupMenu.isVisible()){
						JTextPanePreparer.this.textPane.getInputMap().clear();
						// this is the dirty way to pass "up" to the JTextPane!
						Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new KeyEvent(
								JTextPanePreparer.this.textPane,
                                KeyEvent.KEY_PRESSED,
                                System.currentTimeMillis(),
                                0,
                                KeyEvent.VK_UP,
                                KeyEvent.CHAR_UNDEFINED));
						return;
					}
					JTextPanePreparer.this.previousListItem();
				}
			});

			this.textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "myDown");
			this.textPane.getActionMap().put("myDown", new AbstractAction() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if(!JTextPanePreparer.this.popupMenu.isVisible()){
						JTextPanePreparer.this.textPane.getInputMap().clear();
						// this is the dirty way to pass "down" to the JTextPane!
						Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new KeyEvent(
								JTextPanePreparer.this.textPane,
                                KeyEvent.KEY_PRESSED,
                                System.currentTimeMillis(),
                                0,
                                KeyEvent.VK_DOWN,
                                KeyEvent.CHAR_UNDEFINED));
						return;
					}
					JTextPanePreparer.this.nextListItem();
				}
			});

			this.textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "myRight");
			this.textPane.getActionMap().put("myRight", new AbstractAction() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					JTextPanePreparer.this.popupMenu.setVisible(false);
					JTextPanePreparer.this.textPane.setCaretPosition(JTextPanePreparer.this.textPane.getCaretPosition()+1);
				}
			});

			this.textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "myLeft");
			this.textPane.getActionMap().put("myLeft", new AbstractAction() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					JTextPanePreparer.this.popupMenu.setVisible(false);
					JTextPanePreparer.this.textPane.setCaretPosition(JTextPanePreparer.this.textPane.getCaretPosition()-1);
				}
			});

		} else {
			/*
			 * sorgt dafuer, dass die Bindings wieder verschwinden wenn
			 * das Popup nicht mehr geöffnet ist
			 */
			this.textPane.getInputMap().clear();
		}
	}

	@Override
	public void keyPressed(final KeyEvent e) {
	}


	/*
	 * doppelter Mausklick fuegt ausgewähltes Wort ein
	 */
	public void handleMouseKlicks() {
		this.jl.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(final MouseEvent e) {
				JTextPanePreparer.this.textPane.requestFocusInWindow();
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				JTextPanePreparer.this.textPane.requestFocusInWindow();
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				JTextPanePreparer.this.textPane.requestFocusInWindow();
			}

			@Override
			public void mouseEntered(final MouseEvent e) {
			}

			@Override
			public void mouseClicked(final MouseEvent e) {
				JTextPanePreparer.this.textPane.requestFocusInWindow();
				if (e.getClickCount() == 2 && JTextPanePreparer.this.popupMenu.isVisible()) {
					JTextPanePreparer.this.insertSelectedWord();
					JTextPanePreparer.this.popupMenu.setVisible(false);
				} else {
					JTextPanePreparer.this.textPane.requestFocusInWindow();
				}
			}
		});
	}

	/*
	 * fuegt das ausgewaehlte Listenelement an
	 * der aktuellen Textcursorposition ein
	 */
	public void insertSelectedWord() {
		try {
			final String currentWord = this.getCurrentWord();
			final String text = this.textPane.getText().replaceAll("\r\n", "\n");
			final int caret = this.textPane.getCaretPosition();
			String textBefore;
			String textAfter;
			String newText;
			// immer leerzeichen nach eingefügtem Wort
			final String addWord = this.jl.getSelectedValue().toString() + " ";
			/*
			 * Wenn Cursor am Ende, textAfter dementsprechend leer
			 */
			if (text.length() == 0) {
				textAfter = "";
				textBefore = "";
				this.textPane.setText(addWord);
			} else if (caret == 0) {
				textBefore = "";
				textAfter = text.substring(caret);
			} else if (text.substring(caret).length() == 0) {
				textBefore = text.substring(0, caret - (currentWord.length()));
				textAfter = "";
				// Ansonsten textAfter = hinterer Text
			} else {
				textBefore = text.substring(0, caret - (currentWord.length()));
				textAfter = text.substring(caret);
			}
			// Falls aktuelles Wort leerzeichen
			if (currentWord.equals(" ")) {
				// neuer Text incl leerzeichen vor hinzuzufuegendem Wort
				newText = textBefore + " " + addWord + textAfter;
				this.textPane.setText(newText);
				// Falls Cursor am Ende->bleibt Cursor am Ende
				if (caret == text.length()) {
					this.textPane.setCaretPosition(newText.length());
					// Ansonsten von Caret die Laenge des akt Wortes abziehen
					// und
					// Laenge vom neuen Wort +1 raufaddieren ->neue
					// Caretposition
				} else {
					this.textPane.setCaretPosition(caret - currentWord.length()
							+ (addWord.length()) + 1);
				}
				/*
				 * Hier wird reingesprungen falls akt. Wort kein Leerzeichen
				 * Eigentlich alles wie oben, bis auf das zusaetzliche
				 * leerzeichen und caret +1
				 */
			} else {
				newText = textBefore + addWord + textAfter;
//				if (textBefore.length() > 0
//						&& textBefore.substring(textBefore.length() - 1)
//								.equals("\n")) {
//					newText = textBefore + addWord + textAfter;
//				}
				this.textPane.setText(newText);
				if (caret == text.length()) {
					this.textPane.setCaretPosition(newText.length());
				} else {
					this.textPane.setCaretPosition(caret - currentWord.length() + (addWord.length()));
				}
			}
		} catch (final Exception e) {
			System.out.println("INSERT FEHLER= " + e);
			e.printStackTrace();
		}
	}

	/*
	 * navigiert zum naechsten Listenelement
	 */
	public void nextListItem() {
		this.index = this.jl.getSelectedIndex();
		if (this.index == (this.jl.getModel().getSize() - 1)) {
			this.index = 0;
		} else {
			this.index += 1;
		}
		this.jl.setSelectedIndex(this.index);
		this.jl.ensureIndexIsVisible(this.index);
	}

	/*
	 * navigiert zum vorigen Listenelement
	 */
	public void previousListItem() {
		this.index = this.jl.getSelectedIndex();
		if (this.index > 0) {
			this.index -= 1;
		} else if (this.index == 0) {
			this.index = this.jl.getModel().getSize() - 1;
		}
		this.jl.setSelectedIndex(this.index);
		this.jl.ensureIndexIsVisible(this.index);
	}

	/*
	 * oeffnet das Popup
	 */
	public void openPopup() {
		/*
		 * wenn popupMenu nicht existiert, wird es erstellt
		 * und auf unsichtbar gestellt, ansonsten immer bei
		 * der aktuellen caret Position mit show() aufgerufen
		 */
		if (this.popupMenu == null) {
			this.buildPopup();
			this.popupMenu.setVisible(false);
		}
		this.jl.setModel(this.sm.listToJList(this.textPane.getText().replaceAll("\r\n", "\n"),
				this.textPane.getCaretPosition()));
		this.jl.setSelectedIndex(0);
		if (this.textPane.getCaretPosition() == 0) {
			this.popupMenu.show(this.textPane, 0, 15);
		} else {
			if ((this.textPane.getCaret().getMagicCaretPosition().x == 0)) {
				this.popupMenu.show(this.textPane, 0, 15);

			} else {
				this.jl.setSelectedIndex(0);
				this.popupMenu.show(this.textPane, this.textPane.getCaret()
						.getMagicCaretPosition().x, (this.textPane.getCaret()
								.getMagicCaretPosition().y + 15));
			}
		}
		this.jl.ensureIndexIsVisible(this.jl.getSelectedIndex());
		this.textPane.requestFocusInWindow();
	}

	/*
	 * Initialisiert das Popup mit seinen Einstellungen
	 */
	public void buildPopup() {
		this.popupMenu = new JPopupMenu();
		final JPanel panel = new JPanel();
		this.jl = new JList();
		this.jl.setModel(this.sm.listToJList(this.textPane.getText().replaceAll("\r\n", "\n"),
				this.textPane.getCaretPosition()));
		this.jl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.jl.setSelectedIndex(0);
		panel.setBorder(new RoundedBorder());
		this.popupMenu.setBorder(new RoundedBorder());
		this.popupMenu.setOpaque(false);
		this.popupMenu.setFocusable(true);
		this.popupMenu.add(panel, Component.LEFT_ALIGNMENT);
		this.popupMenu.show(this.textPane, 0, 0);
		this.popupMenu.setVisible(true);

		final JScrollPane scrollPane = new JScrollPane(this.jl);
		scrollPane.setPreferredSize(new Dimension(300, 300));
		panel.add(scrollPane);

		this.popupMenu.setLocation(this.getPositionX(), this.getPositionY());
		this.jl.ensureIndexIsVisible(this.jl.getSelectedIndex());
		this.textPane.requestFocusInWindow();
		this.handleMouseKlicks();
	}

	/*
	 *  returnt die genaue X Koordinate on Screen
	 */
	public int getPositionX() {
		if (this.textPane.getCaretPosition() == 0) {
			return (int) this.textPane.getLocationOnScreen().getX();
		}
		return (int) (this.textPane.getLocationOnScreen().getX() + this.textPane
				.getCaret().getMagicCaretPosition().x);
	}

	/*
	 *  returnt die genaue Y Koordinate on Screen + 15px damit es eine Spalte
	 *  weiter unten ist
	 */
	public int getPositionY() {
		if (this.textPane.getCaretPosition() == 0) {
			return (int) this.textPane.getLocationOnScreen().getY() + 16;
		}
		return (int) (this.textPane.getLocationOnScreen().getY()
				+ this.textPane.getCaret().getMagicCaretPosition().y + 15);
	}

	// gibt das aktuelle Wort zurueck
	public String getCurrentWord() {
		return Strategy.getCurrentWord(this.textPane.getText().replaceAll("\r\n", "\n"), this.textPane.getCaretPosition());
	}

	@SuppressWarnings("serial")
	public static class RoundedBorder extends AbstractBorder {
		/*
		 * runde Ecken fuer PopupMenu
		 */
		@Override
		public void paintBorder(final Component c, final Graphics g, final int x, final int y,
				final int width, final int height) {
			final Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.darkGray);
			final int arc = 10;
			g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
		}
	}
}
