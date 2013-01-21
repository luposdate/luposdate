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
package lupos.event.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.Timer;

import lupos.datastructures.queryresult.QueryResult;
import lupos.event.Consumer;
import lupos.event.pubsub.Subscription;
import lupos.event.util.TimedWrapper;


/**
 * A panel which displays a list with the query results of specific subscription.
 */
@SuppressWarnings("serial")
public class SubscriptionResultsView extends JPanel implements ActionListener {
	
	private static final int UPDATE_INTERVAL = 500;
	
	private final Consumer consumer;
	private Subscription subscription = null;
	
	private Timer timer;
	private JList resultsList;
	private DefaultListModel resultsListModel;
	private QueryResultView resultView = null;
	private JSplitPane splitPane;
	

	/**
	 * Constructor.
	 * @param consumer The consumer from which to obtain query results.
	 */
	public SubscriptionResultsView(Consumer consumer) {
		super(new BorderLayout());
		this.consumer = consumer;

		this.resultsListModel = new DefaultListModel();
        this.resultsList = new JList(this.resultsListModel);

        this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.splitPane.setResizeWeight(1);
		this.splitPane.setTopComponent(new JScrollPane(this.resultsList));
		        
        super.add(this.splitPane, BorderLayout.CENTER);
        
        this.timer = new Timer(UPDATE_INTERVAL, this);
        this.timer.start();
	}
	
	/**
	 * Sets the subscription
	 * @param subscription
	 */
	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
		if(this.subscription == null){
			this.resultsListModel.clear();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(this.subscription == null || !this.consumer.isConnected()) 
			return;
		
		System.out.println("Updating subscription results view..");
		
		this.resultsListModel.clear();
		
		// get results of the current subscription
		List<TimedWrapper<QueryResult>> l = this.consumer.getQueryResults(this.subscription);
		
		if(l==null){
			return;
		}
		
		List<TimedWrapper<QueryResult>> shallowCopy = new ArrayList<TimedWrapper<QueryResult>>(l);
		for(TimedWrapper<QueryResult> r : shallowCopy){
			this.resultsListModel.add(0, r);
		}
		
		// remove current detailed view if there is one
		if(this.resultView != null) {
			super.remove(this.resultView);	
			this.resultView = null;
		}
		
		// if any query results exists, then create a detailed view of the newest result
		if(!shallowCopy.isEmpty()) {
			QueryResult qr = shallowCopy.get(shallowCopy.size()-1).getWrappedObject();
			this.resultView = new QueryResultView(qr);
			this.splitPane.setBottomComponent(this.resultView);
		}
	}
}
