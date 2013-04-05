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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import org.json.JSONObject;

import lupos.datastructures.queryresult.QueryResult;
import lupos.event.communication.IResultReceivedHandler;
import lupos.event.consumer.Consumer;

/**
 * Creates the view of the user including all {@link JPanel}s,{@link JButton}s
 * and the {@link JFrame}.
 */
@SuppressWarnings("serial")
public class ClientView implements Observer, IResultReceivedHandler {

	private Consumer consumer;

	private WorkPanel workPanel;
	private JPanel clientViewPanel;
	private JPanel connectPanel;
	private JPanel configPanel;
	private JPanel templatesPanel;
	private JPanel subscriptionsPanel;

	private JButton connectButton;
	private JButton addTemplateButton;
	private JButton removeTemplateButton;
	private JButton generateButton;

	private JButton newConfigButton;
	private JButton loadConfigButton;
	private JButton saveConfigButton;

	// private JLabel resultLabel;

	private JTextField hostField;
	private JTextField portField;
	private JTextField configField;
	private JTextField locationField;

	private JList templateList;
	private DefaultListModel templateListModel;

	private JList subscriptionList;
	private DefaultListModel subscriptionListModel;

	private JFrame frame;

	/**
	 * Constructor for the class.
	 * 
	 * @param consumer
	 *            the consumer of the clientView
	 */
	public ClientView(Consumer consumer) {
		this.frame = buildFrame();
		this.consumer = consumer;
		this.consumer.addObserver(this);
		this.frame.setVisible(true);
		
		//bugged..
		this.locationField.setVisible(false);
	}

	/**
	 * Initializes the buttons of the view panel.
	 */
	private void initButtons() {
		this.connectButton = new JButton("Connect");
		this.addTemplateButton = new JButton("Add");
		this.removeTemplateButton = new JButton("Delete");
		this.generateButton = new JButton("Generate");
		this.newConfigButton = new JButton("New");
		this.loadConfigButton = new JButton("Load");
		this.saveConfigButton = new JButton("Save");
		
	}

	/**
	 * Creates a {@link JFrame} for the panels.
	 * 
	 * @return the created frame
	 */
	private JFrame buildFrame() {
		JFrame result = new JFrame();

		result.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		result.setTitle("Consumer");
		result.setMinimumSize(new Dimension(850, 620));
		result.setPreferredSize(new Dimension(850, 620));
		result.setLocationRelativeTo(null);
		this.clientViewPanel = buildContentPanel();

		result.setContentPane(this.clientViewPanel);
		result.pack();

		return result;
	}

	/**
	 * Creates the content panel including all other panel classes.
	 * 
	 * @return result as panel
	 */
	private JPanel buildContentPanel() {
		JPanel result = new JPanel();
		result.setLayout(new GridBagLayout());
		GridBagConstraints c;

		initButtons();
		this.connectPanel = buildConnectPanel();

		this.configPanel = buildConfigPanel();
		this.templatesPanel = buildTemplatesPanel();
		this.subscriptionsPanel = buildSubscriptionsPanel();
		this.workPanel = new WorkPanel();

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
		leftPanel.add(this.configPanel);
		leftPanel.add(this.templatesPanel);
		leftPanel.add(this.subscriptionsPanel);

		c = Utils.createGBC(0, 0, GridBagConstraints.HORIZONTAL,
				GridBagConstraints.NORTH);
		c.weightx = 1.0;
		c.gridwidth = 2;
		result.add(this.connectPanel, c);

		c = Utils.createGBC(0, 1, GridBagConstraints.NONE,
				GridBagConstraints.NORTHWEST);
		c.weighty = 1.0;
		result.add(leftPanel, c);

		c = Utils.createGBC(1, 1, GridBagConstraints.BOTH,
				GridBagConstraints.CENTER);
		c.weightx = 1.0;
		c.weighty = 1.0;
		result.add(this.workPanel.getPanel(), c);

		return result;
	}


	/**
	 * Creates the {@link JPanel} to handle with config files.
	 * 
	 * @return result as the panel
	 */
	private JPanel buildConfigPanel() {
		JPanel result = new JPanel();

		result.setLayout(new GridBagLayout());
		GridBagConstraints c;
		result.setBorder(BorderFactory.createTitledBorder("Config:"));

		JPanel buttonPanel = new JPanel();
		GridLayout buttonPanelLayout = new GridLayout(2, 2);
		buttonPanel.setLayout(buttonPanelLayout);

		buttonPanelLayout.setHgap(2);
		buttonPanelLayout.setVgap(2);

		buttonPanel.add(this.newConfigButton);
		buttonPanel.add(this.loadConfigButton);
		buttonPanel.add(this.saveConfigButton);

		this.configField = new JTextField("configName");

		//configField.setMinimumSize(new Dimension(100, fm.getMaxDescent()));
		//configField.setPreferredSize(new Dimension(100, fm.getMaxDescent()));
		//configField.setMaximumSize(new Dimension(100, fm.getMaxDescent()));

		this.locationField = new JTextField("location");

		//locationField.setMinimumSize(new Dimension(100, fm.getMaxDescent()));
		//locationField.setPreferredSize(new Dimension(100, fm.getMaxDescent()));
		//locationField.setMaximumSize(new Dimension(100, fm.getMaxDescent()));

		c = Utils.createGBC(0, 0, GridBagConstraints.NONE,
				GridBagConstraints.WEST);
		result.add(new JLabel("Name:"), c);

		c = Utils.createGBC(1, 0, GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST);
		result.add(this.configField, c);

		c = Utils.createGBC(0, 1, GridBagConstraints.NONE,
				GridBagConstraints.WEST);
		result.add(new JLabel("Location:"), c);

		c = Utils.createGBC(1, 1, GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST);
		result.add(this.locationField, c);
		
		c = Utils.createGBC(0, 2, GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST);
		c.gridwidth = 2;
		result.add(buttonPanel, c);

		return result;
	}

	/**
	 * Creates the template {@link JPanel}.
	 * 
	 * @return the created panel
	 */
	private JPanel buildTemplatesPanel() {
		JPanel result = new JPanel();

		result.setLayout(new GridBagLayout());
		GridBagConstraints c;
		result.setBorder(BorderFactory.createTitledBorder("Templates:"));

		this.templateListModel = new DefaultListModel();

		this.templateList = new JList(templateListModel);
		this.templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.templateList.setLayoutOrientation(JList.VERTICAL);
		this.templateList.setVisibleRowCount(-1);

		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				ClientView.this.templateList = (JList) mouseEvent.getSource();
				int index = ClientView.this.templateList.locationToIndex(mouseEvent.getPoint());
				if (index >= 0) {
					Object o = ClientView.this.templateListModel.getElementAt(index);
					ClientView.this.workPanel.showTemplate(o.toString());
					ClientView.this.addTemplateButton.setEnabled(true);
					ClientView.this.removeTemplateButton.setEnabled(true);
				}
			}
		};

		this.templateList.addMouseListener(mouseListener);

		JScrollPane templateListScroller = new JScrollPane(this.templateList);
		templateListScroller.setPreferredSize(new Dimension(171, 140));
		templateListScroller.setMaximumSize(new Dimension(171, 140));
		templateListScroller.setMinimumSize(new Dimension(171, 140));

		JPanel buttonPanel = new JPanel();
		GridLayout buttonPanelLayout = new GridLayout(1, 2);
		buttonPanel.setLayout(buttonPanelLayout);

		buttonPanelLayout.setHgap(2);
		buttonPanelLayout.setVgap(2);

		buttonPanel.add(this.addTemplateButton);
		buttonPanel.add(this.removeTemplateButton);

		c = Utils.createGBC(0, 0, GridBagConstraints.NONE,
				GridBagConstraints.NORTH);
		c.gridwidth = 2;
		result.add(templateListScroller, c);

		c = Utils.createGBC(0, 1, GridBagConstraints.NONE,
				GridBagConstraints.CENTER);
		result.add(buttonPanel, c);

		return result;
	}

	/**
	 * Creates the subscription {@link JPanel}.
	 * 
	 * @return result as panel
	 */
	private JPanel buildSubscriptionsPanel() {
		JPanel result = new JPanel();

		result.setLayout(new GridBagLayout());
		GridBagConstraints c;
		result.setBorder(BorderFactory.createTitledBorder("Subscriptions:"));

		this.subscriptionListModel = new DefaultListModel();

		this.subscriptionList = new JList(this.subscriptionListModel);
		this.subscriptionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.subscriptionList.setLayoutOrientation(JList.VERTICAL);
		this.subscriptionList.setVisibleRowCount(-1);

		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				ClientView.this.subscriptionList = (JList) mouseEvent.getSource();
				int index = ClientView.this.subscriptionList.locationToIndex(mouseEvent
						.getPoint());
				if (index >= 0) {
					//Object o = subscriptionListModel.getElementAt(index);
					// workPanel.showTemplate(o.toString());
					// addTemplateButton.setEnabled(true);
					// removeTemplateButton.setEnabled(true);
				}
			}
		};

		this.subscriptionList.addMouseListener(mouseListener);

		JScrollPane subScriptionListScroller = new JScrollPane(this.subscriptionList);
		subScriptionListScroller.setPreferredSize(new Dimension(171, 140));
		subScriptionListScroller.setMaximumSize(new Dimension(171, 140));
		subScriptionListScroller.setMinimumSize(new Dimension(171, 140));

		JPanel buttonPanel = new JPanel();
		GridLayout buttonPanelLayout = new GridLayout(1, 2);
		buttonPanel.setLayout(buttonPanelLayout);

		buttonPanelLayout.setHgap(2);
		buttonPanelLayout.setVgap(2);

		buttonPanel.add(this.generateButton);

		c = Utils.createGBC(0, 0, GridBagConstraints.NONE,
				GridBagConstraints.NORTH);
		c.gridwidth = 2;
		result.add(subScriptionListScroller, c);

		c = Utils.createGBC(0, 1, GridBagConstraints.NONE,
				GridBagConstraints.CENTER);
		result.add(buttonPanel, c);

		return result;
	}

	/**
	 * Creates the connect {@link JPanel}.
	 * 
	 * @return p as the panel
	 */
	private JPanel buildConnectPanel() {
		JPanel p = new JPanel();

		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.setBorder(BorderFactory.createTitledBorder("Connect to a broker:"));
		this.hostField = new JTextField("localhost");
		this.portField = new JTextField("4444");

		p.add(new JLabel("Host:"));
		p.add(this.hostField);
		p.add(new JLabel("Port:"));
		p.add(this.portField);
		p.add(this.connectButton);

		return p;
	}

	/**
	 * Handles the event a query receives.
	 * 
	 * @param result
	 *            the result that receives
	 */
	@Override
	public void resultReceived(QueryResult result) {
		// this.resultLabel.setText(result.toString());
	}

	/**
	 * Showing a error message.
	 * 
	 * @param errMessage
	 *            the message to show
	 */
	void showError(String errMessage) {
		JOptionPane.showMessageDialog(this.frame, errMessage, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Observer for certain actions.
	 * 
	 * @param o
	 *            the observeable
	 * @param arg
	 *            the object
	 */
	@Override
	public void update(Observable o, Object arg) {
		boolean connected = this.consumer.isConnected();

		this.connectButton.setText(connected ? "Disconnect" : "Connect");
		this.hostField.setEnabled(!connected);
		this.portField.setEnabled(!connected);

		// set content of mainPanel according to the connection state

	}

	/**
	 * For setting the controller to use.
	 * 
	 * @param c
	 *            the controller
	 */
	public void useController(Controller c) {
		this.connectButton.setAction(c.getConnectAction());
		this.addTemplateButton.setAction(c.getAddTemplateAction());
		this.removeTemplateButton.setAction(c.getRemoveTemplateAction());
		this.generateButton.setAction(c.getShowGeneratePanelAction());
		this.newConfigButton.setAction(c.getNewConfigAction());
		this.loadConfigButton.setAction(c.getLoadConfigAction());
		this.saveConfigButton.setAction(c.getSaveConfigAction());
	}

	/**
	 * For getting the selected send option.
	 * 
	 * @return the selected send option
	 */
	public String getSelectedSendOption() {
		return this.workPanel.getSelectedSendOption();
	}

	/**
	 * For getting the workPanel.
	 * 
	 * @return workPanel as itself
	 */
	public WorkPanel getWorkPanel() {
		return this.workPanel;
	}

	/**
	 * For getting the clientViewPanel.
	 * 
	 * @return clientViewPanel as itself
	 */
	public JPanel getClientViewPanel() {
		return this.clientViewPanel;
	}

	/**
	 * For getting the connectPanel.
	 * 
	 * @return connectPanel as itself
	 */
	public JPanel getConnectPanel() {
		return this.connectPanel;
	}

	/**
	 * For getting the configPanel.
	 * 
	 * @return configPanel as itself
	 */
	public JPanel getConfigPanel() {
		return this.configPanel;
	}

	/**
	 * For getting the templatesPanel.
	 * 
	 * @return templatesPanel as itself
	 */
	public JPanel getTemplatesPanel() {
		return this.templatesPanel;
	}

	/**
	 * For getting the subscriptionsPanel.
	 * 
	 * @return subscriptionsPanel as itself
	 */
	public JPanel getSubscriptionsPanel() {
		return this.subscriptionsPanel;
	}

	/**
	 * For getting the connectButton.
	 * 
	 * @return connectButton as itself
	 */
	public JButton getConnectButton() {
		return this.connectButton;
	}

	/**
	 * For getting the addTemplateButton.
	 * 
	 * @return addTemplateButton as itself
	 */
	public JButton getAddTemplateButton() {
		return this.addTemplateButton;
	}

	/**
	 * For getting the removeTemplateButton.
	 * 
	 * @return removeTemplateButton as itself
	 */
	public JButton getRemoveTemplateButton() {
		return this.removeTemplateButton;
	}

	/**
	 * For getting the generateTemplateButton.
	 * 
	 * @return generateTemplateButton as itself
	 */
	public JButton getGenerateButton() {
		return this.generateButton;
	}

	/**
	 * For getting the templateListModel.
	 * 
	 * @return templateListModel as itself
	 */
	public DefaultListModel getTemplateListModel() {
		return this.templateListModel;
	}

	/**
	 * For getting the subscriptionListModel.
	 * 
	 * @return subscriptionListModel as itself
	 */
	public DefaultListModel getSubscriptionListModel() {
		return this.subscriptionListModel;
	}

	/**
	 * For getting the templateList.
	 * 
	 * @return templateList as itself
	 */
	public JList getTemplateList() {
		return this.templateList;
	}

	/**
	 * For getting the subscriptionList.
	 * 
	 * @return subscriptionList as itself
	 */
	public JList getSubscriptionList() {
		return this.subscriptionList;
	}

	/**
	 * For getting the host.
	 * 
	 * @return host as itself
	 */
	public String getHost() {
		return this.hostField.getText();
	}

	/**
	 * For getting the port.
	 * 
	 * @return port as itself
	 */
	public int getPort() {
		return Integer.parseInt(this.portField.getText());
	}

	/**
	 * For getting subscriptions of the workPanel.
	 * 
	 * @return {@link JSONObject}
	 */
	public JSONObject getSubscriptions() {
		return this.workPanel.getSubscriptions();
	}

	/**
	 * For getting the frame.
	 * 
	 * @return frame as itself
	 */
	public JFrame getFrame() {
		return this.frame;
	}

	/**
	 * For getting the hostField.
	 * 
	 * @return hostField as itself
	 */
	public JTextField getHostField() {
		return this.hostField;
	}

	/**
	 * For getting the portField.
	 * 
	 * @return portField as itself
	 */
	public JTextField getPortField() {
		return this.portField;
	}

	/**
	 * For getting the configField.
	 * 
	 * @return configField as itself
	 */
	public JTextField getConfigField() {
		return this.configField;
	}

	/**
	 * For getting the locationField.
	 * 
	 * @return locationField as itself
	 */
	public JTextField getLocationField() {
		return this.locationField;
	}
}
