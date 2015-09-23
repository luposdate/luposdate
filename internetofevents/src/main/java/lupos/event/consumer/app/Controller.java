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
package lupos.event.consumer.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lupos.event.*;
import lupos.event.action.Action;
import lupos.event.consumer.Consumer;
import lupos.event.pubsub.Subscription;
public class Controller {
	
	private Consumer consumer;
	private ClientView view;
	
	private DefaultListModel subscriptionsListModel = new DefaultListModel();
	private int subcount = 1;
	private Subscription activeSubscription;
	
	
	/**
	 * <p>Constructor for Controller.</p>
	 *
	 * @param model a {@link lupos.event.consumer.Consumer} object.
	 * @param view a {@link lupos.event.consumer.app.ClientView} object.
	 */
	public Controller(Consumer model, final ClientView view) {
		this.consumer = model;
		this.view = view;
		this.view.setSubscriptionListModel(this.subscriptionsListModel);
		addListeners();
	}
	
	private void addListeners() {
		this.view.addSubscriptionSelectionListModel(new ListSelectionListener() {			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()) {
					JList l = (JList)e.getSource();
					Controller.this.view.setActiveSubscription((Subscription)l.getSelectedValue());
				}
			}
		});
				
		this.view.addConnectActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(Controller.this.consumer.isConnected()) {
					disconnectFromBroker();
				} else {
					String host = Controller.this.view.getHost();
					int port = Controller.this.view.getPort();
					connectToBroker(host, port);
				}
			}
		});
		
		this.view.addNewSubscriptionActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				Controller.this.activeSubscription = new Subscription("Subscription #" + Controller.this.subcount++, "");
				Controller.this.view.setActiveSubscription(Controller.this.activeSubscription);
			}
		});
		
		this.view.addSubmitActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Controller.this.activeSubscription.setName(Controller.this.view.getSubscriptionName());
				Controller.this.activeSubscription.setQuery(Controller.this.view.getSubscriptionQuery());
				subscribe(Controller.this.activeSubscription, Controller.this.view.getAction());
			}
		});	
	}
	
	/**
	 * <p>disconnectFromBroker.</p>
	 */
	public void disconnectFromBroker() {
		this.consumer.disconnect();
		this.subscriptionsListModel.clear();
	}
	
	/**
	 * <p>connectToBroker.</p>
	 *
	 * @param host a {@link java.lang.String} object.
	 * @param port a int.
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
	 * <p>subscribe.</p>
	 *
	 * @param sub a {@link lupos.event.pubsub.Subscription} object.
	 * @param action a {@link lupos.event.action.Action} object.
	 */
	public void subscribe(Subscription sub, Action action) {
		try {
			this.consumer.subscribe(sub, action);
			this.subscriptionsListModel.removeElement(sub);
			this.subscriptionsListModel.add(0,sub);
			this.view.setActiveSubscription(sub);
		} catch (IOException e) {
			this.view.showError(e.toString());
			e.printStackTrace();
		}
	}
}
