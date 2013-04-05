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
package lupos.event.consumer.html;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class for creating the {@link JPanel} for generating a HTML template.
 */
public class HTMLOptionsPanel {

	private String outPutFolder;
	private List<String> subscriptionNames;
	private final String HTML_TEMPLATES_PATH = "src/main/resources/htmlTemplates/";

	/**
	 * Constructor for creating HTML template.
	 * 
	 * @param subscriptionNames
	 *            the name of a subscription
	 * @param outPutFolder
	 *            the outPutFolder to save
	 */
	public HTMLOptionsPanel(List<String> subscriptionNames, String outPutFolder) {
		this.outPutFolder = outPutFolder;
		this.subscriptionNames = subscriptionNames;
		buildDialog();
	}

	/**
	 * Creates the GUI elements for creating HTML templates.
	 */
	private void buildDialog() {
		JPanel p = new JPanel();
		JComboBox cb_styles = new JComboBox();
		JCheckBox checkb = new JCheckBox();

		JSONObject jsHTMLConfig;
		JSONObject jsHTMLTemplate;
		JSONArray jsHTMLTemplates;

		try {
			jsHTMLConfig = new JSONObject(Utils.readFile(this.HTML_TEMPLATES_PATH + "HTMLConfig.json"));
			jsHTMLTemplates = jsHTMLConfig.getJSONArray("templates");

			for (int j = 0; j < jsHTMLTemplates.length(); j++) {
				jsHTMLTemplate = jsHTMLTemplates.getJSONObject(j);
				cb_styles.addItem(jsHTMLTemplate.getString("name").toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		p.setLayout(new GridBagLayout());
		GridBagConstraints c;

		c = Utils.createGBC(0, 0, GridBagConstraints.NONE,
				GridBagConstraints.WEST);
		c.gridwidth = 1;
		p.add(new JLabel("With extra html files?"), c);

		c = Utils.createGBC(1, 0, GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST);
		p.add(checkb, c);

		c = Utils.createGBC(0, 1, GridBagConstraints.NONE,
				GridBagConstraints.WEST);
		p.add(new JLabel("Style"), c);

		c = Utils.createGBC(1, 1, GridBagConstraints.NONE,
				GridBagConstraints.WEST);
		p.add(cb_styles, c);

		JOptionPane.showMessageDialog(null, p, "Enter HTML Setup Information",
				JOptionPane.OK_OPTION);

		if (checkb.isSelected()) {
			generateExtraHTMLPages(cb_styles.getSelectedItem().toString());
		} else {
			File dir = new File(this.outPutFolder + "/HTML");
			dir.mkdir();
		}
	}

	/**
	 * Reads the files of a chosen style and creates the needed files to
	 * outPutFolder.
	 * 
	 * @param style
	 *            the chosen style
	 */
	public void generateExtraHTMLPages(String style) {

		JSONObject jsHTMLConfig;
		JSONObject jsHTMLTemplate;
		JSONArray jsHTMLTemplates;
		JSONArray pages;
		JSONArray linkPages;
		String indexPage = "";
		String selectedHTMLTemplate = style;
		String content = "";
		String link = "";

		try {
			jsHTMLConfig = new JSONObject(Utils.readFile(this.HTML_TEMPLATES_PATH + "HTMLConfig.json"));

			jsHTMLTemplates = jsHTMLConfig.getJSONArray("templates");

			for (int j = 0; j < jsHTMLTemplates.length(); j++) {
				jsHTMLTemplate = jsHTMLTemplates.getJSONObject(j);
				if (jsHTMLTemplate.getString("name").equals(
						selectedHTMLTemplate)) {

					indexPage = jsHTMLTemplate.getString("indexPage");
					content = Utils.readFile(this.HTML_TEMPLATES_PATH + selectedHTMLTemplate + "/" + indexPage);
					Utils.createHTMLFile(this.outPutFolder, indexPage, true);
					Utils.writeFile(this.outPutFolder + indexPage, content);

					pages = jsHTMLTemplate.getJSONArray("pages");
					linkPages = jsHTMLTemplate.getJSONArray("linkPages");

					for (int x = 0; x < pages.length(); x++) {
						JSONObject page = pages.getJSONObject(x);
						content = Utils.readFile(this.HTML_TEMPLATES_PATH
								+ selectedHTMLTemplate + "/"
								+ page.getString("name"));
						Utils.createHTMLFile(this.outPutFolder,
								page.getString("name"), false);
						Utils.writeFile(
								this.outPutFolder + "HTML/" + page.getString("name"),
								content);
					}

					for (int y = 0; y < linkPages.length(); y++) {
						JSONObject linkPage = linkPages.getJSONObject(y);
						link = linkPage.getString("name");
						Utils.createHTMLFile(this.outPutFolder, link, false);
						createLinkHTML(this.subscriptionNames, selectedHTMLTemplate, link);
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a link page for the HTML template.
	 * 
	 * @param outPutFolder
	 *            the outPutFolder to save
	 * @param pages
	 *            the generated page name
	 * @param selectedHTMLTemplate
	 *            the selected template name
	 * @param linkPage
	 *            the name for reading the file
	 */
	public void createLinkHTML(List<String> pages, String selectedHTMLTemplate,
			String linkPage) {

		String linksCode = Utils.readFile(this.HTML_TEMPLATES_PATH + selectedHTMLTemplate + "/" + linkPage);
		String link = "";
		for (String page : pages) {
			link = "<a href=\"" + page + ".html\" " + "target=\"Home\"><b>"
					+ page + "</b></a><br>";
			linksCode = linksCode.replaceFirst("%-Link-%", "%-Link-%" + "\n"
					+ link);
		}

		linksCode = linksCode.replaceAll("%-Link-%", "");

		Utils.writeFile(this.outPutFolder + "/HTML/" + linkPage, linksCode);
	}

}
