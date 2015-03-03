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

import java.awt.CardLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.JPanel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class for creating {@link JPanel} handling template and the queries layout.
 */
public class WorkPanel {

	private final JPanel workPanel;
	private CardLayout workPanelLayout;
	private GeneratePanel generatePanel;

	/**
	 * Constructor for creating the Panel.
	 */
	public WorkPanel() {
		this.workPanel = this.buildMainPanel();
	}

	/**
	 * Creates the whole {@link JPanel} including generate panel.
	 *
	 * @return the created panel
	 */
	private JPanel buildMainPanel() {
		JPanel p = new JPanel();

		this.generatePanel = new GeneratePanel();
		final JPanel emptyPanel = new JPanel();
		emptyPanel.setName("empty");

		this.workPanelLayout = new CardLayout();
		p = new JPanel(this.workPanelLayout);

		p.add(emptyPanel, "empty");
		this.generatePanel.getPanel().setName("generatePanel");
		p.add(this.generatePanel.getPanel(), "generatePanel");

		return p;
	}

	/**
	 * Sets the template to show.
	 *
	 * @param templateName
	 *            the template to show
	 */
	public void showTemplate(final String templateName) {
		this.workPanelLayout.show(this.workPanel, templateName);
	}

	/**
	 * Sets an empty template to show.
	 */
	public void showNothing() {
		this.workPanelLayout.show(this.workPanel, "empty");
	}
	/**
	 * Shows the generate Panel for the user.
	 */
	public void showGeneratePanel() {
		this.generatePanel.clear();
		for (final Component component : this.workPanel.getComponents()) {
			if (component instanceof TemplatePanel) {
				this.generatePanel.addTemplate(component.getName());
			}
		}
		this.workPanelLayout.show(this.workPanel, "generatePanel");
	}

	/**
	 * Creates of chosen templates a config file by given path.
	 *
	 * @param path
	 *            the path to save
	 * @return JSONObject to save
	 * @throws JSONException
	 */
	public JSONObject toJSON(final String path) throws JSONException {
		final JSONObject saveObject = new JSONObject();
		final JSONArray templates = new JSONArray();

		TemplatePanel curTemplate;

		for (final Component component : this.workPanel.getComponents()) {
			if (component instanceof TemplatePanel) {
				curTemplate = (TemplatePanel) component;
				templates.put(curTemplate.toJSON(path));
			}
		}
		saveObject.put("templates", templates);

		return saveObject;
	}

	/**
	 * Creates a template HTML file by given path.
	 *
	 * @param path
	 *            the path to save the HTML file
	 */
	public void saveTemplateHTML(final String path){
		for (final Component component : this.workPanel.getComponents()) {
			if (component instanceof TemplatePanel) {
				((TemplatePanel) component).writeHTML(path);
			}
		}
	}

	/**
	 * Loads from a given {@link JSONObject} the data for setting them into
	 * template panel.
	 *
	 * @param loadObject
	 *            the object to load
	 * @throws JSONException
	 */
	public void fromJSON(final JSONObject loadObject) throws JSONException {

		final JSONArray templates = loadObject.getJSONArray("templates");

		for (int i = 0; i < templates.length(); i++) {
			final JSONObject template = templates.getJSONObject(i);

			final String templateName = template.getString("name");
			final TemplatePanel newTemplatePanel = new TemplatePanel();
			newTemplatePanel.setName(templateName);
			newTemplatePanel.fromJSON(template);
			this.workPanel.add(newTemplatePanel, templateName);
		}
	}

	/**
	 * For getting a template of the template panel.
	 *
	 * @param templateName
	 *            the name of the template
	 * @return the template panel component
	 */
	public TemplatePanel getTemplate(final String templateName) {
		for (final Component component : this.workPanel.getComponents()) {
			if (component.getName().equals(templateName)) {
				return (TemplatePanel) component;
			}
		}
		return null;
	}

	/**
	 * For removing a template of the template panel.
	 *
	 * @param templateName
	 *            the template to remove
	 */
	public void removeTemplate(final String templateName) {
		for (final Component component : this.workPanel.getComponents()) {
			if (component.getName().equals(templateName)) {
				this.workPanel.remove(component);
			}
		}
	}

	/**
	 * Adds a template to the template panel.
	 *
	 * @param templateName
	 *            the name of the template
	 */
	public void addTemplate(final String templateName) {
		final TemplatePanel newTemplatePanel = new TemplatePanel();
		newTemplatePanel.setTemplateName(templateName);
		newTemplatePanel.setName(templateName);
		this.workPanel.add(newTemplatePanel, templateName);
	}

	/**
	 * For getting the selected Subscriptions and creates {@link JSONObject}
	 *
	 * @return result as JSONObject
	 */
	public JSONObject getSubscriptions() {
		final JSONObject result = new JSONObject();
		final JSONArray templates = new JSONArray();

		TemplatePanel tmpTemplate = null;
		JSONObject tmpJSONTemplate = null;

		final List<String> subscriptionNames = this.generatePanel.getSubscriptions();

		if(subscriptionNames.isEmpty()){
			return null;
		}

		for (final String templateName : subscriptionNames) {

			tmpTemplate = this.getTemplate(templateName);
			try {
				tmpJSONTemplate = tmpTemplate.toJSON();
				tmpJSONTemplate.put("htmlCode", tmpTemplate.getHTMLCode());
				templates.put(tmpJSONTemplate);
			} catch (final JSONException e) {
				e.printStackTrace();
			}
		}

		try {
			result.put("templates", templates);
		} catch (final JSONException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Clears the panel completely.
	 */
	public void clear() {
		this.generatePanel.clear();
		for (final Component component : this.workPanel.getComponents()) {
			if (component instanceof TemplatePanel) {
				this.workPanel.remove(component);
			}
		}
	}

	/**
	 * For getting the selected HTML template.
	 *
	 * @return the selected HTML template as String
	 */
	public String getSelectedHTMLTemplate() {
		return this.generatePanel.getSelectedHTMLTemplate();
	}

	/**
	 * For getting the selected send option.
	 *
	 * @return the selected send option as String
	 */
	public String getSelectedSendOption(){
		return this.generatePanel.getSelectedSendOption();
	}

	/**
	 * For getting the whole panel.
	 *
	 * @return workPanel as the panel
	 */
	public JPanel getPanel() {
		return this.workPanel;
	}

	/**
	 * For getting the included generate panel.
	 *
	 * @return generatePanel used by workPanel
	 */
	public GeneratePanel getGeneratePanel(){
		return this.generatePanel;
	}
}
