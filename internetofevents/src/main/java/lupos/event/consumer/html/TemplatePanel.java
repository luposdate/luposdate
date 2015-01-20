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
package lupos.event.consumer.html;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import lupos.event.consumer.querybuilder.EventQueryBuilderDialog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link JPanel} for the {@link JButton}s and {@link JTextArea}s handling of
 * the generated Templates Queries.
 */
@SuppressWarnings("serial")
public class TemplatePanel extends JSplitPane implements ActionListener {

	private JTextField templateNameField;

	private JTextArea queryTextArea;
	private JTextArea templateTextArea;

	private JScrollPane queryTextAreaScrollPane;
	private JScrollPane templateTextAreaScrollPane;

	private JButton queryBuilderButton;
	private JButton addForButton;
	private JButton addContentButton;
	private JButton addRefreshButton;
	private JButton addPredicateButton;

	public TemplatePanel() {
		initComponents();
		initLayout();
		this.templateNameField.setEnabled(false);
	}

	/**
	 * Initializes the Components of the Panel with certain values.
	 */
	private void initComponents() {

		this.queryTextArea = new JTextArea();
		this.queryTextArea.setColumns(20);
		this.queryTextArea.setTabSize(2);
		this.queryTextArea.setRows(7);
		this.queryTextArea.setLineWrap(false);
		this.queryTextArea.setWrapStyleWord(true);
		this.queryTextArea.setEditable(true);
		this.queryTextAreaScrollPane = new JScrollPane(this.queryTextArea);

		this.templateTextArea = new JTextArea();
		this.templateTextArea.setColumns(20);
		this.templateTextArea.setTabSize(2);
		this.templateTextArea.setRows(7);
		this.templateTextArea.setLineWrap(false);
		this.templateTextArea.setWrapStyleWord(true);
		this.templateTextArea.setEditable(true);
		this.templateTextAreaScrollPane = new JScrollPane(this.templateTextArea);

		this.templateNameField = new JTextField("templatename");

		this.queryBuilderButton = new JButton("Open query builder...");
		this.queryBuilderButton.addActionListener(this);

		this.addForButton = new JButton("FOR");
		this.addForButton.addActionListener(this);

		this.addContentButton = new JButton("CONTENT");
		this.addContentButton.addActionListener(this);

		this.addPredicateButton = new JButton("PREDICATE");
		this.addPredicateButton.addActionListener(this);
		
		this.addRefreshButton = new JButton("REFRESH");
		this.addRefreshButton.addActionListener(this);
	}

	/**
	 * Initializes the Layout of the Panel.
	 */
	private void initLayout() {
		this.setOrientation(HORIZONTAL_SPLIT);
		this.setLeftComponent(buildQueryPanel());
		this.setRightComponent(buildTemplatePanel());
		this.setOneTouchExpandable(true);
		this.setBorder(BorderFactory.createTitledBorder("Manage Templates:"));
	}

	/**
	 * Creates the Panel of the Queries.
	 * 
	 * @return p as the created Panel
	 */
	private JPanel buildQueryPanel() {
		JPanel p = new JPanel();

		p.setLayout(new GridBagLayout());
		GridBagConstraints c;

		c = Utils.createGBC(0, 0, GridBagConstraints.BOTH,
				GridBagConstraints.NORTHWEST);
		c.gridwidth = 2;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p.add(this.queryTextAreaScrollPane, c);

		c = Utils.createGBC(0, 1, GridBagConstraints.NONE,
				GridBagConstraints.EAST);
		p.add(new JLabel("Edit:"), c);

		c = Utils.createGBC(1, 1, GridBagConstraints.NONE,
				GridBagConstraints.WEST);
		p.add(this.queryBuilderButton, c);

		p.setBorder(BorderFactory.createTitledBorder("Query"));
		return p;
	}

	/**
	 * Creates the panel for the templates.
	 * 
	 * @return the created template panel
	 */
	private JPanel buildTemplatePanel() {
		JPanel p = new JPanel();

		p.setLayout(new GridBagLayout());
		GridBagConstraints c;

		JPanel addButtonsPanel = new JPanel();
		addButtonsPanel.add(this.addForButton);
		addButtonsPanel.add(this.addContentButton);
		addButtonsPanel.add(this.addPredicateButton);
		addButtonsPanel.add(this.addRefreshButton);
		addButtonsPanel.setLayout(new BoxLayout(addButtonsPanel,
				BoxLayout.LINE_AXIS));

		c = Utils.createGBC(0, 0, GridBagConstraints.BOTH,
				GridBagConstraints.NORTHWEST);
		c.gridwidth = 2;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p.add(this.templateTextAreaScrollPane, c);

		c = Utils.createGBC(0, 1, GridBagConstraints.NONE,
				GridBagConstraints.EAST);
		p.add(new JLabel("Add:"), c);

		c = Utils.createGBC(1, 1, GridBagConstraints.NONE,
				GridBagConstraints.WEST);
		p.add(addButtonsPanel, c);

		p.setBorder(BorderFactory.createTitledBorder("Template"));
		return p;
	}

	/**
	 * Handles the actions of the generated {@link JButton}s.
	 * 
	 * @param e
	 *            as the triggered action
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.queryBuilderButton) {
			Window owner = SwingUtilities.windowForComponent(this);
			EventQueryBuilderDialog dialog = new EventQueryBuilderDialog(owner);
			dialog.setVisible(true);

			if (dialog.getQuery() != null)
				this.queryTextArea.setText(dialog.getQuery());
		}

		if (e.getSource() == this.addForButton) {
			String selection = this.templateTextArea.getSelectedText();
			if (selection != null) {
				this.templateTextArea.replaceSelection(Encode.FOR_OF(selection));
			} else {
				this.templateTextArea.replaceSelection(Encode.FORSTART + "\n" + Encode.FOREND);
			}
		}

		if (e.getSource() == this.addContentButton) {
			String selection = this.templateTextArea.getSelectedText();
			if (selection != null) {
				this.templateTextArea.replaceSelection(Encode.CONTENT_OF(selection));
			} else {
				this.templateTextArea.replaceSelection(Encode.START + Encode.CONTENT + Encode.END);
			}

		}

		if (e.getSource() == this.addRefreshButton) {
			this.templateTextArea.replaceSelection("IN HEADER !!! \n" + Encode.START + Encode.REFRESH + Encode.END);
		}
		
		if (e.getSource() == this.addPredicateButton) {
			String selection = this.templateTextArea.getSelectedText();
			if (selection != null) {
				this.templateTextArea.replaceSelection(Encode.PREDICATE_OF(selection));
			} else {
				this.templateTextArea.replaceSelection(Encode.START + Encode.PREDICATE + Encode.END);
			}
		}
	}

	/**
	 * Creates a empty {@link JSONObject}.
	 * 
	 * @return empty JSONObject
	 * @throws JSONException
	 */
	public JSONObject toJSON() throws JSONException {
		return toJSON("");
	}

	/**
	 * Creates a {@link JSONObject} out of the read information of the query and
	 * the template.
	 * 
	 * @param path
	 *            to save the file at given location
	 * @return saveObject as created JSONObject.
	 * @throws JSONException
	 */
	public JSONObject toJSON(String path) throws JSONException {
		final JSONObject saveObject = new JSONObject();

		if (!this.templateNameField.getText().isEmpty()) {
			saveObject.put("name", this.templateNameField.getText());
			saveObject.put("query", this.queryTextArea.getText());
			saveObject.put("htmlPath", path + this.templateNameField.getText() + ".html");
		}

		return saveObject;
	}

	/**
	 * Writes the template as HTML file.
	 * 
	 * @param path
	 *            the location to save the file
	 */
	public void writeHTML(String path) {
		Utils.writeFile(path + this.templateNameField.getText() + ".html", this.templateTextArea.getText());
	}

	/**
	 * Reads of a {@link JSONObject} the needed information to it on panel.
	 * 
	 * @param loadObject
	 *            the JSONObject to read
	 */
	public void fromJSON(final JSONObject loadObject) {
		if (loadObject != null) {
			try {
				this.templateNameField.setText(loadObject.getString("name"));
			} catch (final JSONException e) {
				this.templateNameField.setText("loading error");
				e.printStackTrace();
			}
			try {
				this.queryTextArea.setText(loadObject.getString("query"));
			} catch (final JSONException e) {
				this.queryTextArea.setText("loading error");
				e.printStackTrace();
			}
			try {
				this.templateTextArea.setText(Utils.readFile(loadObject
						.getString("htmlPath")));
			} catch (final JSONException e) {
				this.templateTextArea.setText("loading error");
				e.printStackTrace();
			}
		}
	}

	/**
	 * For getting the {@link JTextField} of templateNameField.
	 * 
	 * @return the textfield of the templatename
	 */
	public JTextField getTemplateNameField() {
		return this.templateNameField;
	}

	/**
	 * For setting the name of templateNameField.
	 * 
	 * @param templateName
	 *            the text to set
	 */
	public void setTemplateName(String templateName) {
		this.templateNameField.setText(templateName);
	}

	/**
	 * For getting the written template HTML code.
	 * 
	 * @return the templateTextArea content
	 */
	public String getHTMLCode() {
		return this.templateTextArea.getText();
	}
}