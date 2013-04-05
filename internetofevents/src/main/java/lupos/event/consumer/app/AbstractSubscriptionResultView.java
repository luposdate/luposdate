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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import lupos.event.consumer.Consumer;
import lupos.event.pubsub.Subscription;


/** Abstract class for common parts of result views
 * 
 * @author heidemey
 *
 */
abstract public class AbstractSubscriptionResultView extends JPanel implements
		ActionListener {

	// timer for updating the view
	// can be removed once callback on receiving new results is implemented
	private static final int UPDATE_INTERVAL = 500;
	private Timer timer;
	
	final protected Consumer consumer;
	protected Subscription subscription = null;
	
	/** Constructor 
	 * 
	 * @param mgr LayoutManager
	 * @param consumer 
	 */
	public AbstractSubscriptionResultView(LayoutManager mgr, Consumer consumer){
		super(mgr);
		this.timer = new Timer(UPDATE_INTERVAL, this);
        this.timer.start();
        this.consumer =consumer;
	}
	/** Constructor with GridbagLayout Manager
	 * 
	 * @param consumer
	 */
	public AbstractSubscriptionResultView(Consumer consumer){
		this(new GridBagLayout(), consumer);
	}
	
	@Override
	/** Method for updating the GUI with new results. Gets called by the timer.
	 * 
	 */
	abstract public void actionPerformed(ActionEvent arg0);
	
	/**
	 * Sets the subscription
	 * @param subscription
	 */
	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
		if(this.subscription == null){
			this.ClearDataset();

		}
	}
	
	/** 
	 * Resets the view
	 */
	abstract protected void ClearDataset();
	
	protected static GridBagConstraints createGBC(int gridx, int gridy, int fill) {
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,2,2,2);
		c.gridx = gridx;
		c.gridy = gridy;
		c.fill= fill;
		return c;
	}
	
	/**
	 * Returns a GridBagContraints object with gridx and gridy. Other constraints
	 *  are set to default values.  
	 * @param gridx 
	 * @param gridy
	 * @return
	 */
	protected static GridBagConstraints createGBC(int gridx, int gridy){
		return createGBC(gridx, gridy, GridBagConstraints.NONE);
	}
	
}
