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
package lupos.event.consumer.app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionListener;

import lupos.datastructures.queryresult.QueryResult;
import lupos.event.action.Action;
import lupos.event.communication.IResultReceivedHandler;
import lupos.event.consumer.Consumer;
import lupos.event.consumer.app.SubscriptionChartView;
import lupos.event.pubsub.Subscription;



@SuppressWarnings("serial")
public class ClientView extends JFrame implements Observer, IResultReceivedHandler {
		
	private Consumer consumer;
	
	private final JLabel notConnectedLabel = new JLabel("Not connected.", SwingConstants.CENTER);
	private JPanel mainPanel;
	private CardLayout mainPanelCardLayout;
	private JPanel manageSubscriptionsPanel;
	private JList subscriptionList;
	private JTextField hostField, portField;
	private JButton connectButton, newSubscription;
	private JLabel label;
	private SubscriptionEditView subscriptionEditView;
	private SubscriptionResultsView subscriptionResultsView;
	private SubscriptionChartView subscriptionChartView;
	private JTabbedPane subscriptionTabbedPane;	
	
	public ClientView(Consumer consumer) {		
		this.consumer = consumer;
		initComponents();
		this.consumer.addObserver(this);
	}
	
	private void initComponents() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("Consumer");
        this.setMinimumSize(new Dimension(500,400));
        this.setPreferredSize(new Dimension(500,400));
        
        this.subscriptionEditView = new SubscriptionEditView();
        this.subscriptionResultsView = new SubscriptionResultsView(this.consumer);
        this.subscriptionChartView = new SubscriptionChartView(this.consumer);
        getContentPane().setLayout(new BorderLayout(5,5));
        
        JPanel connectPanel = new JPanel();
        connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.X_AXIS));
        connectPanel.setBorder(BorderFactory.createTitledBorder("Connect to a broker:"));
        this.hostField = new JTextField("localhost");
        this.portField = new JTextField("4444");
		this.connectButton = new JButton("Connect");
		connectPanel.add(new JLabel("Host:"));
        connectPanel.add(this.hostField);
        connectPanel.add(new JLabel("Port:"));
        connectPanel.add(this.portField);
        connectPanel.add(this.connectButton);
                        
        this.manageSubscriptionsPanel = new JPanel(new BorderLayout(3,3));
        this.manageSubscriptionsPanel.setBorder(BorderFactory.createTitledBorder("Manage Subscriptions:"));
        
        this.mainPanelCardLayout = new CardLayout();
        this.mainPanel = new JPanel(this.mainPanelCardLayout);
        this.mainPanel.add(this.notConnectedLabel, Boolean.FALSE.toString());
        this.mainPanel.add(this.manageSubscriptionsPanel, Boolean.TRUE.toString());
                
        this.subscriptionList = new JList();
        this.subscriptionList.setPreferredSize(new Dimension(100,1));
        this.subscriptionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.subscriptionList.setLayoutOrientation(JList.VERTICAL);
        this.subscriptionList.setVisibleRowCount(-1);
        
        JScrollPane subscriptionListScroller = new JScrollPane(this.subscriptionList);
        subscriptionListScroller.setPreferredSize(new Dimension(100, 10));
        
        JPanel panel = new JPanel(new BorderLayout(3,3));
        this.newSubscription = new JButton("New Subscription");
        panel.add(this.newSubscription, BorderLayout.NORTH);
        panel.add(subscriptionListScroller, BorderLayout.CENTER);
        
        this.manageSubscriptionsPanel.add(panel, BorderLayout.WEST);
        //this.manageSubscriptionsPanel.add(new JLabel("PLATZHALTER"), BorderLayout.CENTER);


        this.subscriptionTabbedPane = new JTabbedPane();
        this.subscriptionTabbedPane.addTab("Edit", this.subscriptionEditView);
        this.subscriptionTabbedPane.addTab("Results", this.subscriptionResultsView);
        this.subscriptionTabbedPane.addTab("Charts", this.subscriptionChartView);
		
		getContentPane().add(connectPanel, BorderLayout.NORTH);
		getContentPane().add(this.mainPanel, BorderLayout.CENTER);
		
		pack();
	}
	
	
	public void setActiveSubscription(Subscription sub) {
		this.subscriptionResultsView.setSubscription(sub);
		this.subscriptionChartView.setSubscription(sub);		
		if(sub==null) {
			this.manageSubscriptionsPanel.remove(this.subscriptionTabbedPane);
		} else {
			this.subscriptionEditView.setSubscriptionName(sub.getName());
		    this.subscriptionEditView.setSubscriptionQuery(sub.getQuery());
		    this.manageSubscriptionsPanel.add(this.subscriptionTabbedPane, BorderLayout.CENTER);		    
		}
		this.manageSubscriptionsPanel.updateUI();
	}
	

	@Override
	public void resultReceived(QueryResult result) {
		this.label.setText(result.toString());		
	}
	
	void showError(String errMessage) {
        JOptionPane.showMessageDialog(this, errMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

	public void addConnectActionListener(ActionListener al) {
		this.connectButton.addActionListener(al);
	}
	
	public void addNewSubscriptionActionListener(ActionListener al) {
		this.newSubscription.addActionListener(al);
	}
	
	public void addSubmitActionListener(ActionListener al) {
		this.subscriptionEditView.addSubmitActionListener(al);
	}
	
	public String getHost() {
		return this.hostField.getText();
	}
	
	public int getPort() {
		return Integer.parseInt(this.portField.getText());
	}
	
	public String getSubscriptionName() {
		return this.subscriptionEditView.getSubscriptionName();
	}
	
	public String getSubscriptionQuery() {
		return this.subscriptionEditView.getSubscriptionQuery();
	}
	
	public Action getAction() {
		return this.subscriptionEditView.getAction();
	}
	
	@Override
	public void update(Observable o, Object arg) {
		boolean connected = this.consumer.isConnected();
		
		this.connectButton.setText(connected ? "Disconnect" : "Connect");
		this.hostField.setEnabled(!connected);
		this.portField.setEnabled(!connected);
		
		// set content of mainPanel according to the connection state
		this.mainPanelCardLayout.show(this.mainPanel, Boolean.toString(connected));
	}	

	public void setSubscriptionListModel(DefaultListModel model) {
		this.subscriptionList.setModel(model);
	}
	
	public void addSubscriptionSelectionListModel(ListSelectionListener l) {
		this.subscriptionList.addListSelectionListener(l);
	}	
}
