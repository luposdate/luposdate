package lupos.gui.operatorgraph.visualeditor.guielements;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.IPrefixPanel;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;

public class PrefixPanel extends AbstractGuiComponent<Operator> implements
IPrefixPanel {
	private static final long serialVersionUID = 1L;
	protected Prefix prefix;
	private final GridBagConstraints gbc;
	private final JPanel prefixRowsPanel;

	public PrefixPanel(final Prefix prefix, final GraphWrapper gw,
			final VisualGraph<Operator> parent) {
		super(parent, gw, null, true);

		this.prefix = prefix;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.prefixRowsPanel = new JPanel(new GridBagLayout());
		this.prefixRowsPanel.setOpaque(false);

		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.WEST;
		this.gbc.gridwidth = this.gbc.gridheight = 1;
		this.gbc.weightx = this.gbc.weighty = 1.0;
		this.gbc.insets = new Insets(5, 5, 5, 5);
		this.gbc.gridx = this.gbc.gridy = 0;

		if (this.prefix.hasElements()) {
			for (final String namespace : this.prefix.getPrefixList().keySet()) {
				this.createPrefixRow(
						this.prefix.getPrefixList().get(namespace), namespace);
			}
		}

		// create add button...
		final JLabel addLabel = new JLabel(this.parent.addIcon);
		addLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(final MouseEvent me) {
				createPrefixRow("", "");
				updateSize();
			}
		});

		final JLabel titleLabel = new JLabel("Prefixes");
		titleLabel.setFont(parent.getFONT());

		this.add(titleLabel);
		this.add(this.prefixRowsPanel);
		this.add(addLabel); // add add-button to row panel
		this.updateSize();
	}

	public void createPrefixRow(final String prefixString,
			final String namespaceString) {
		this.gbc.gridx = 0;
		this.gbc.gridy++;

		final JTextFieldResizing prefixTF = new JTextFieldResizing(
				prefixString, this.parent.getFONT(), this);

		final JTextFieldResizing namespaceTF = new JTextFieldResizing(
				namespaceString, this.parent.getFONT(), this);

		prefixTF.addFocusListener(new FocusAdapter() {
			private String oldValue = prefixString;

			public void focusLost(final FocusEvent fe) {
				final String newPrefix = prefixTF.getText();

				if (!namespaceTF.getText().equals("")
						&& !newPrefix.equals(this.oldValue)) {
					final int choice = showPrefixNameChangedOptionDialog();

					if (choice == JOptionPane.YES_OPTION) { // change prefix
						// name and notify
						// operators...
						prefix.changeEntryName(this.oldValue, newPrefix, true);
						this.oldValue = newPrefix;
					} else if (choice == JOptionPane.NO_OPTION) { // change
						// prefix
						// name but
						// don't
						// notify
						// operators
						// ...
						prefix.changeEntryName(this.oldValue, newPrefix, false);
						this.oldValue = newPrefix;
					} else if (choice == JOptionPane.CANCEL_OPTION) { // don't
						// change
						// prefix
						// name
						// ...
						prefixTF.setText(this.oldValue);
						prefixTF.grabFocus();
					}
				}
			}
		});

		namespaceTF.addFocusListener(new FocusAdapter() {
			private String oldValue = namespaceString;

			public void focusLost(final FocusEvent fe) {
				String newNamespace = namespaceTF.getText();

				if (this.oldValue.equals("") && !newNamespace.equals("")) {
					final int choice = showPrefixAddedOptionDialog();

					if (choice == JOptionPane.YES_OPTION) { // add prefix and
						// notify
						// operators...
						prefix.addEntry(prefixTF.getText(), newNamespace, true);
					} else if (choice == JOptionPane.NO_OPTION) { // add prefix
						// but don't
						// notify
						// operators
						// ...
						prefix
						.addEntry(prefixTF.getText(), newNamespace,
								false);
					} else if (choice == JOptionPane.CANCEL_OPTION) { // don't
						// add
						// prefix
						// ...
						prefixTF.setText("");
						namespaceTF.setText("");
						namespaceTF.grabFocus();
						newNamespace = "";
					}
				} else if (!this.oldValue.equals("") && newNamespace.equals("")) {
					final int choice = showPrefixRemovedOptionDialog();

					if (choice == JOptionPane.YES_OPTION) { // remove prefix and
						// notify
						// operators...
						prefixTF.setText("");
						prefix.removeEntry(this.oldValue, true);
					} else if (choice == JOptionPane.NO_OPTION) { // remove
						// prefix
						// but don't
						// notify
						// operators
						// ...
						prefixTF.setText("");
						prefix.removeEntry(this.oldValue, false);
					} else if (choice == JOptionPane.CANCEL_OPTION) { // don't
						// remove
						// prefix
						// ...
						namespaceTF.setText(this.oldValue);
						namespaceTF.grabFocus();
						newNamespace = this.oldValue;
					}
				}

				this.oldValue = newNamespace;
			}
		});

		final JLabel deleteLabel = new JLabel(this.parent.delIcon);
		deleteLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(final MouseEvent me) {
				if (!namespaceTF.getText().equals("")) {
					final int choice = showPrefixRemovedOptionDialog();

					if (choice == JOptionPane.YES_OPTION) { // remove prefix and
						// notify
						// operators...
						prefix.removeEntry(namespaceTF.getText(), true);

						removeRow(prefixTF, namespaceTF, deleteLabel);
					} else if (choice == JOptionPane.NO_OPTION) { // remove
						// prefix
						// but don't
						// notify
						// operators
						// ...
						prefix.removeEntry(namespaceTF.getText(), false);

						removeRow(prefixTF, namespaceTF, deleteLabel);
					}
				} else {
					removeRow(prefixTF, namespaceTF, deleteLabel);
				}
			}
		});

		this.prefixRowsPanel.add(prefixTF, this.gbc);

		this.gbc.gridx++;

		this.prefixRowsPanel.add(namespaceTF, this.gbc);

		this.gbc.gridx++;

		this.prefixRowsPanel.add(deleteLabel, this.gbc);
	}

	private void removeRow(final JTextField prefixTF,
			final JTextField namespaceTF, final JLabel deleteLabel) {
		this.prefixRowsPanel.remove(prefixTF);
		this.prefixRowsPanel.remove(namespaceTF);
		this.prefixRowsPanel.remove(deleteLabel);

		this.updateSize();
	}

	public void updateSize() {
		this.setMinimumSize(this.prefixRowsPanel.getSize());

		// --- update width of the JTextFieldResizing to the max size per
		// column
		// - begin ---
		if (this.prefixRowsPanel.getComponentCount() >= 3) {
			// -- get max width for each column - begin --
			int maxWidthLeftColumn = 0;
			int maxWidthRightColumn = 0;
			Container textField = null;
			Dimension d = null;

			// walk through rows...
			for (int i = 0; i < this.prefixRowsPanel.getComponentCount(); i += 1) {
				// left text field...
				textField = (Container) this.prefixRowsPanel.getComponent(i);

				final Dimension leftSize = (textField instanceof JTextFieldResizing) ? ((JTextFieldResizing) textField)
						.calculateSize()
						: textField.getPreferredSize();

						maxWidthLeftColumn = Math.max(maxWidthLeftColumn,
								leftSize.width);

						// right text field...
						i += 1;
						textField = (Container) this.prefixRowsPanel.getComponent(i);

						final Dimension rightSize = (textField instanceof JTextFieldResizing) ? ((JTextFieldResizing) textField)
								.calculateSize()
								: textField.getPreferredSize();

								maxWidthRightColumn = Math.max(maxWidthRightColumn,
										rightSize.width);

								i += 1; // skip delete-label
			}
			// -- get max width for each column - end --

			// -- update elements of each column - begin --
			// walk through rows...
			for (int i = 0; i < this.prefixRowsPanel.getComponentCount(); i += 1) {
				// left text field...
				textField = (Container) this.prefixRowsPanel.getComponent(i);
				d = new Dimension(maxWidthLeftColumn, textField
						.getPreferredSize().height);
				textField.setPreferredSize(d);
				textField.setSize(d);
				textField.setMaximumSize(d);
				textField.setMinimumSize(d);
				textField.repaint();

				// right text field...
				i += 1;
				textField = (Container) this.prefixRowsPanel.getComponent(i);
				d = new Dimension(maxWidthRightColumn, textField
						.getPreferredSize().height);
				textField.setPreferredSize(d);
				textField.setSize(d);
				textField.setMaximumSize(d);
				textField.setMinimumSize(d);
				textField.repaint();

				i += 1; // skip delete-label
			}
			// -- update elements of each column - end --
		}
		// --- update width of the JTextFieldResizing to the max size per
		// column
		// - begin ---

		// update height of the GraphBox...
		if (this.getBox() != null) {
			this.getBox().height = this.getPreferredSize().height;
		}

		this.setSize(this.getPreferredSize());
		this.revalidate(); // re-validate the PrefixPanel
	}

	private int showPrefixAddedOptionDialog() {
		return JOptionPane
		.showOptionDialog(
				this.parent.visualEditor,
				"A prefix has been added. Do you want to replace all occurences of the prefix with the defined prefix name?",
				"Prefix added", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE, null, new Object[] {
						"Yes", "No and ignore warning",
				"Don't add prefix" }, 0);
	}

	private int showPrefixRemovedOptionDialog() {
		return JOptionPane
		.showOptionDialog(
				this.parent.visualEditor,
				"A prefix has been removed. Do you want to replace all occurences of the prefix name with it's prefix?",
				"Prefix removed", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE, null, new Object[] {
						"Yes", "No and ignore warning",
				"Don't remove prefix" }, 0);
	}

	private int showPrefixNameChangedOptionDialog() {
		return JOptionPane
		.showOptionDialog(
				this.parent.visualEditor,
				"A prefix name has been chanced. Do you want to replace all occurences of the old prefix name with the new one?",
				"Prefix name changed",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE, null, new Object[] {
						"Yes", "No and ignore warning",
				"Don't change prefix name" }, 0);
	}

	public void mouseClicked(final MouseEvent me) {
	}

	public boolean validateOperatorPanel(final boolean showErrors, Object data) {
		return true;
	}
}