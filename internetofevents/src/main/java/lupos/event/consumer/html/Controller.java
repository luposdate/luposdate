/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lupos.event.*;
import lupos.event.action.PageAction;
import lupos.event.action.send.GenerateHTML;
import lupos.event.action.send.Send;
import lupos.event.action.send.SendEMail;
import lupos.event.action.send.SlidingWindow;
import lupos.event.consumer.Consumer;
import lupos.event.pubsub.Subscription;

/**
 * Controller class for handling actions between GUI and logic parts.
 */
public class Controller {

	private Consumer consumer;
	private ClientView view;

	private int subcount = 1;

	/**
	 * Constructor of the controller.
	 * 
	 * @param model the consumer
	 * @param view the Clientview
	 */
	public Controller(Consumer model, final ClientView view) {
		this.consumer = model;
		this.view = view;
		this.view.useController(this);
		this.view.getWorkPanel().getGeneratePanel().useController(this);
	}

	/**
	 * Handles the event for setting a output folder after submitting.
	 * Also reads out {@link JSONObject} for sending options which will
	 * be produced.
	 * 
	 * @param subscriptions {@link JSONObject} for reading content to send.
	 */
	public void submit(JSONObject subscriptions) {
		String outPutFolder = null;
		String selectedSendOption = this.view.getSelectedSendOption();
		String name = null;
		String query = null;
		String htmlCode = null;
		List<String> subscriptionNames = new ArrayList<String>();
		JSONObject template;
		JSONArray templates = null;
		Send send = null;

		if (selectedSendOption.equals("Generate HTML")) {

			JFileChooser chooser = new JFileChooser(".");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);

			if (chooser.showDialog(this.view.getFrame(), "Select output directory.") == JFileChooser.APPROVE_OPTION) {
				outPutFolder = chooser.getSelectedFile().getAbsolutePath()
						+ "/";
				outPutFolder = outPutFolder.replace(File.separatorChar, '/');
			} else {
				return;
			}
		}

		try {
			templates = subscriptions.getJSONArray("templates");

		} catch (JSONException e) {
			this.view.showError(e.toString());
			e.printStackTrace();
		}

		for (int i = 0; i < templates.length(); i++) {

			try {
				template = templates.getJSONObject(i);
				name = template.getString("name");
				name = name.replaceAll(" ", "");
				subscriptionNames.add(name);
				query = template.getString("query");
				htmlCode = template.getString("htmlCode");

			} catch (JSONException e) {
				this.view.showError(e.toString());
				e.printStackTrace();
			}

			if (selectedSendOption.equals("Generate HTML")) {

				send = new GenerateHTML(name, outPutFolder);

			} else if (selectedSendOption.equals("Send EMail")) {

				send = new SendEMail();

			} else if (selectedSendOption.equals("Sliding Window")) {

				send = new SlidingWindow();
			}

			send.init();

			subscribe(new Subscription("#" + this.subcount++ + " " + name,
					query), new PageAction(name, htmlCode, send));

		}

		if (selectedSendOption.equals("Generate HTML")) {
			new HTMLOptionsPanel(subscriptionNames, outPutFolder);
		}
	}

	/**
	 * Creates a subscription and a {@link PageAction}.
	 * 
	 * @param sub the subsription to create
	 * @param action the action to create
	 */
	public void subscribe(Subscription sub, PageAction action) {
		try {
			this.consumer.subscribe(sub, (lupos.event.action.Action) action);
			this.view.getSubscriptionListModel().add(0, sub);
		} catch (IOException e) {
			this.view.showError(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Connects to the broker.
	 * 
	 * @param host the name of the host
	 * @param port the port of the host
	 */
	public void connectToBroker(String host, int port) {
		try {
			this.consumer.connect(host, port);
		} catch (Exception e) {
			this.view.showError(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * For disconnecting from broker.
	 */
	public void disconnectFromBroker() {
		this.consumer.disconnect();
		this.view.getSubscriptionListModel().clear();
	}

	/**
	 * Handles the dialog of loading a config file.
	 */
	public void loadConfigDialog() {
		JFileChooser chooser = new JFileChooser(
				"src/main/resources/htmlTemplates");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileNameExtensionFilter("json", "json"));
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showDialog(this.view.getFrame(), "Open") == JFileChooser.APPROVE_OPTION) {
			load(chooser.getSelectedFile());
		}
	}

	/**
	 * Handles the events of loading a config for the GUI.
	 * 
	 * @param file the config file to load
	 */
	public void load(File file) {

		this.view.getTemplateListModel().clear();
		this.view.getWorkPanel().clear();

		this.view.getConfigField().setText(file.getName());
		this.view.getLocationField().setText(file.getPath());
		try {
			JSONObject loadObject = new JSONObject(Utils.readFile(file.getAbsolutePath()));

			JSONArray templates = loadObject.getJSONArray("templates");

			for (int i = 0; i < templates.length(); i++) {
				JSONObject template = templates.getJSONObject(i);
				this.view.getTemplateListModel().addElement(
						(String) template.get("name"));
			}

			this.view.getWorkPanel().fromJSON(loadObject);

			this.view.getAddTemplateButton().setEnabled(true);
			this.view.getRemoveTemplateButton().setEnabled(true);
			this.view.getWorkPanel().showNothing();

		} catch (JSONException e) {
			this.view.showError(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Handles the dialog for saving a config file.
	 */
	private void saveConfigDialog() {
		JFileChooser chooser = new JFileChooser(
				"src/main/resources/htmlTemplates");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileNameExtensionFilter("json", "json"));
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showSaveDialog(this.view.getFrame()) == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getPath();

			if (!path.toLowerCase().endsWith(".json"))
				path = path + ".json";

			save(new File(path));
		}
	}

	/**
	 * Handles the events of saving a config for the GUI.
	 * 
	 * @param file the config file to load
	 */
	private void save(File selectedFile) {
		JSONObject saveObject;
		String filePath = selectedFile.getPath();
		String directoryPath = filePath.replaceAll(selectedFile.getName(), "");
		try {
			saveObject = this.view.getWorkPanel().toJSON(directoryPath);
			this.view.getLocationField().setText(selectedFile.getAbsolutePath());
			this.view.getConfigField().setText(selectedFile.getName());
			this.view.getWorkPanel().saveTemplateHTML(directoryPath);
			Utils.writeFile(filePath, saveObject.toString(2));

		} catch (JSONException e1) {
			this.view.showError(e1.toString());
			e1.printStackTrace();
		}
	}

	/**
	 * Handles users mistsakes befor open the submit event.
	 * 
	 * @return GuiAction as an event
	 */
	public Action getSubmitAction() {
		return new GuiAction("Submit") {

			private static final long serialVersionUID = -2168680461457798615L;

			@Override
			public void actionPerformed(ActionEvent arg0) {

				JSONObject subscriptions = Controller.this.view.getSubscriptions();

				if (subscriptions == null) {
					JOptionPane.showMessageDialog(Controller.this.view.getFrame(),
							"No subscriptions added.");
				} else if (!Controller.this.consumer.isConnected()) {
					JOptionPane.showMessageDialog(Controller.this.view.getFrame(),
							"Connect to a broker first.");
				} else {
					submit(subscriptions);
				}
			}
		};
	}

	/**
	 * Handles connection and disconnection events.
	 * 
	 * @return GuiAction as an event
	 */
	public Action getConnectAction() {
		return new GuiAction("Connect") {

			private static final long serialVersionUID = -7773188967954478594L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Controller.this.consumer.isConnected()) {
					disconnectFromBroker();
				} else {
					String host = Controller.this.view.getHost();
					int port = Controller.this.view.getPort();
					connectToBroker(host, port);
				}
			}
		};
	}
	
	/**
	 * Handles event of adding a template.
	 * 
	 * @return GuiAction as an event
	 */
	public Action getAddTemplateAction() {
		return new GuiAction("Add") {

			private static final long serialVersionUID = -2521410481843005816L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String newTemplatename = JOptionPane
						.showInputDialog("Enter a name for the Template");
				if (Controller.this.view.getTemplateListModel().contains(newTemplatename)) {
					JOptionPane.showMessageDialog(Controller.this.view.getFrame(),
							"Name already exists.", "Error",
							JOptionPane.ERROR_MESSAGE);
				} else if (newTemplatename==null) {
					// do nothing
				} else {
					Controller.this.view.getWorkPanel().addTemplate(newTemplatename);
					Controller.this.view.getTemplateListModel().addElement(newTemplatename);
					Controller.this.view.getWorkPanel().showTemplate(newTemplatename);
				}
			}
		};
	}

	/**
	 * Handles event of removing a template.
	 * 
	 * @return GuiAction as an event
	 */
	public Action getRemoveTemplateAction() {
		return new GuiAction("Remove") {

			private static final long serialVersionUID = 4270471613186494113L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!Controller.this.view.getTemplateList().isSelectionEmpty()) {
					String templateName = Controller.this.view.getTemplateList()
							.getSelectedValue().toString();
					Controller.this.view.getWorkPanel().removeTemplate(templateName);
					Controller.this.view.getTemplateListModel().remove(
							Controller.this.view.getTemplateList().getSelectedIndex());
				}
			}
		};
	}

	/**
	 * Handles event of generating a template.
	 * 
	 * @return GuiAction as an event
	 */
	public Action getShowGeneratePanelAction() {
		return new GuiAction("Generate") {

			private static final long serialVersionUID = -5962617914877696535L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Controller.this.view.getAddTemplateButton().isEnabled()
						&& Controller.this.view.getRemoveTemplateButton().isEnabled()) {
					Controller.this.view.getWorkPanel().showGeneratePanel();
					Controller.this.view.getAddTemplateButton().setEnabled(false);
					Controller.this.view.getRemoveTemplateButton().setEnabled(false);
				} else {
					Controller.this.view.getWorkPanel().showNothing();
					Controller.this.view.getAddTemplateButton().setEnabled(true);
					Controller.this.view.getRemoveTemplateButton().setEnabled(true);
				}
			}
		};
	}

	/**
	 * Handles event of creating an empty config.
	 * 
	 * @return GuiAction as an event
	 */
	public Action getNewConfigAction() {
		return new GuiAction("New") {

			private static final long serialVersionUID = 3759250093223827030L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Controller.this.view.getWorkPanel().clear();
				Controller.this.view.getWorkPanel().showNothing();
				Controller.this.view.getTemplateListModel().clear();
				Controller.this.view.getAddTemplateButton().setEnabled(true);
				Controller.this.view.getRemoveTemplateButton().setEnabled(true);
				Controller.this.view.getConfigField().setText("new Config");
				Controller.this.view.getLocationField().setText("");
			}
		};
	}

	/**
	 * Handles event of loading a config.
	 * 
	 * @return GuiAction as an event
	 */
	public Action getLoadConfigAction() {
		return new GuiAction("Load") {

			private static final long serialVersionUID = 8286695006298110825L;

			@Override
			public void actionPerformed(ActionEvent e) {
				loadConfigDialog();
			}
		};
	}
	
	/**
	 * Handles event of saving a config.
	 * 
	 * @return GuiAction as an event
	 */
	public Action getSaveConfigAction() {
		return new GuiAction("Save") {

			private static final long serialVersionUID = -4661506270228591498L;

			@Override
			public void actionPerformed(ActionEvent e) {
				saveConfigDialog();
			}
		};
	}

	/**
	 * Inner class GuiAction to handles events.
	 */
	public abstract class GuiAction extends AbstractAction {

		private static final long serialVersionUID = -927615349927449698L;

		public GuiAction(String command) {
			super(command);
			putValue(ACTION_COMMAND_KEY, command);
		}
	}

}
