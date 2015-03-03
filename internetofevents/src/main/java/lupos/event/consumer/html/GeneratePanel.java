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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * GUI class for creating a template with {@link JButton}s,{@link JComboBox}es
 * and JListss.
 */
public class GeneratePanel implements ActionListener {

	private final JPanel generatePanel;

	private JButton activateTemplateButton;
	private JButton deactivateTemplateButton;
	private JButton submitButton;

	private final JComboBox htmlDesigncBox;
	private final JComboBox sendOptioncBox;

	private JList templateList;
	private JList newSubscriptionList;

	private DefaultListModel newSubscriptionListModel;
	private DefaultListModel templateListModel;

	/**
	 * Constructor for initializing the GUI.
	 */
	public GeneratePanel() {
		this.initButtons();
		this.htmlDesigncBox = this.buildHTMLDesigncBox();
		final String[] options = { "Generate HTML", "Sliding Window", "Send EMail" };
		this.sendOptioncBox = new JComboBox(options);
		this.generatePanel = this.buildMainPanel();
	}

	/**
	 * Creates the GUI elements of this panel.
	 *
	 * @return p as the created panel
	 */
	private JPanel buildMainPanel() {
		final JPanel p = new JPanel();

		p.setLayout(new GridBagLayout());
		GridBagConstraints c;
		p.setBorder(BorderFactory.createTitledBorder("Manage Subscriptions:"));

		final JPanel activityButtons = new JPanel();
		activityButtons.add(this.activateTemplateButton);
		activityButtons.add(this.deactivateTemplateButton);
		activityButtons.setLayout(new BoxLayout(activityButtons,
				BoxLayout.PAGE_AXIS));

		this.templateListModel = new DefaultListModel();

		this.templateList = new JList(this.templateListModel);
		this.templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.templateList.setLayoutOrientation(JList.VERTICAL);
		this.templateList.setVisibleRowCount(-1);

		final JScrollPane templateListScroller = new JScrollPane(this.templateList);
		templateListScroller.setPreferredSize(new Dimension(150, 80));

		this.newSubscriptionListModel = new DefaultListModel();

		this.newSubscriptionList = new JList(this.newSubscriptionListModel);
		this.newSubscriptionList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.newSubscriptionList.setLayoutOrientation(JList.VERTICAL);
		this.newSubscriptionList.setVisibleRowCount(-1);

		final JScrollPane submitListScroller = new JScrollPane(this.newSubscriptionList);
		submitListScroller.setPreferredSize(new Dimension(150, 80));

		c = Utils.createGBC(0, 0, GridBagConstraints.NONE,
				GridBagConstraints.NORTHWEST);
		p.add(new JLabel("Templates"), c);

		c = Utils.createGBC(3, 0, GridBagConstraints.NONE,
				GridBagConstraints.NORTHWEST);
		p.add(new JLabel("New Subscriptions"), c);

		c = Utils.createGBC(0, 1, GridBagConstraints.BOTH,
				GridBagConstraints.WEST);
		c.gridwidth = 2;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p.add(templateListScroller, c);

		c = Utils.createGBC(2, 1, GridBagConstraints.NONE,
				GridBagConstraints.CENTER);
		p.add(activityButtons, c);

		c = Utils.createGBC(3, 1, GridBagConstraints.BOTH,
				GridBagConstraints.EAST);
		c.gridwidth = 2;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p.add(submitListScroller, c);

		c = Utils.createGBC(2, 2, GridBagConstraints.NONE,
				GridBagConstraints.EAST);
		p.add(new JLabel("Output:"), c);

		c = Utils.createGBC(3, 2, GridBagConstraints.NONE,
				GridBagConstraints.WEST);
		p.add(this.sendOptioncBox, c);

		c = Utils.createGBC(4, 2, GridBagConstraints.NONE,
				GridBagConstraints.EAST);
		p.add(this.submitButton, c);

		return p;
	}

	/**
	 * Creates the buttons of this panel.
	 */
	private void initButtons() {
		this.activateTemplateButton = new JButton(">>");
		this.activateTemplateButton.addActionListener(this);
		this.deactivateTemplateButton = new JButton("<<");
		this.deactivateTemplateButton.addActionListener(this);
		this.submitButton = new JButton("Submit");
	}

	/**
	 * Builds the box for chosing a HTML template.
	 *
	 * @return JComboBox including the HTML template name
	 */
	public JComboBox buildHTMLDesigncBox() {

		JComboBox result = null;
		try {
			final JSONObject loadObject = new JSONObject(
					Utils.readFile("./src/main/resources/htmlTemplates/" + "HTMLConfig.json"));
			final JSONArray templates = loadObject.getJSONArray("templates");

			final String[] templateNames = new String[templates.length()];

			for (int i = 0; i < templates.length(); i++) {
				final JSONObject template = templates.getJSONObject(i);
				templateNames[i] = (String) template.get("name");
			}

			result = new JComboBox(templateNames);

		} catch (final JSONException e1) {
			e1.printStackTrace();
		}
		return result;
	}

	/**
	 * For getting this generatePanel.
	 *
	 * @return the generatePanel
	 */
	public JPanel getPanel() {
		return this.generatePanel;
	}

	/**
	 * For getting a new and empty subscrition list.
	 *
	 * @return newSubscriptionListModel
	 */
	public DefaultListModel getSubscriptionListModel() {
		return this.newSubscriptionListModel;
	}

	/**
	 * Adds a template to templateListModel element.
	 *
	 * @param templateName
	 *            the template to add
	 */
	public void addTemplate(final String templateName) {
		this.templateListModel.addElement(templateName);
	}

	/**
	 * Clears the template and subscriptions list.
	 */
	public void clear() {
		this.clearTemplateList();
		this.clearNewSubscriptionList();
	}

	/**
	 * Clears only the template list.
	 */
	public void clearTemplateList() {
		this.templateListModel.clear();
	}

	/**
	 * Clears only the subscriptions list.
	 */
	public void clearNewSubscriptionList() {
		this.newSubscriptionListModel.clear();
	}

	/**
	 * For getting the chosen subscriptions in the subscriptionListModel.
	 *
	 * @return results as all subscriptions
	 */
	public List<String> getSubscriptions() {
		final List<String> results = new ArrayList<String>();

		for (int i = 0; i < this.newSubscriptionListModel.size(); i++) {
			results.add((String) this.newSubscriptionListModel.getElementAt(i));
		}

		return results;
	}

	/**
	 * For getting the selected HTML template.
	 *
	 * @return the selected HTML template as String.
	 */
	public String getSelectedHTMLTemplate() {
		return this.htmlDesigncBox.getSelectedItem().toString();
	}

	/**
	 * For getting the selected send option.
	 *
	 * @return the selected send option
	 */
	public String getSelectedSendOption() {
		return this.sendOptioncBox.getSelectedItem().toString();
	}

	/**
	 * Handles the actions of the Buttons
	 *
	 * @param e
	 *            the triggered action
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == this.activateTemplateButton) {
			if (!this.templateList.isSelectionEmpty()) {
				this.newSubscriptionListModel.addElement(this.templateList.getSelectedValue());
				this.templateListModel.remove(this.templateList.getSelectedIndex());
			}
		}

		if (e.getSource() == this.deactivateTemplateButton) {
			if (!this.newSubscriptionList.isSelectionEmpty()) {
				this.templateListModel.addElement(this.newSubscriptionList.getSelectedValue());
				this.newSubscriptionListModel.remove(this.newSubscriptionList.getSelectedIndex());
			}
		}
	}

	/**
	 * For setting the controller to use.
	 *
	 * @param c
	 *            the controller
	 */
	public void useController(final Controller c) {
		this.submitButton.setAction(c.getSubmitAction());
	}
}
