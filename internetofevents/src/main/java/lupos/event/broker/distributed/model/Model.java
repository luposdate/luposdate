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
package lupos.event.broker.distributed.model;

import java.util.LinkedList;
import java.util.List;

import lupos.event.communication.ModelUpdateMessage;


/**
 * This class represents the active model
 * of a SubBroker and provides information
 * for the routing process
 * @author Kevin
 *
 */
public class Model {

	private final List<IModelChangedListener> changeListeners = new LinkedList<IModelChangedListener>();

	/**
	 * Stores all currently active BConsumers
	 */
	private final List<BConsumer> bConsumers = new LinkedList<BConsumer>();
	
	/**
	 * Stores all currently active BProducers
	 */
	private final List<BProducer> bProducers = new LinkedList<BProducer>();
	
	/**
	 * Gets a modelupdatemessage which can be directly sent
	 * to the master broker sending all the model data
	 * @return
	 */
	public ModelUpdateMessage getUpdateMessage(){
		return new ModelUpdateMessage(this.bProducers, this.bConsumers);
	}
	
	public void addBConsumer(BConsumer con){
		if (this.bConsumers.contains(con)){
			return;
		}
		this.bConsumers.add(con);
		notifyListeners();
	}
	
	public void removeBConsumer(BConsumer con){
		this.bConsumers.remove(con);
		notifyListeners();
	}
	
	public void addBProducer(BProducer producer){
		if (this.bProducers.contains(producer)){
			return;
		}
		this.bProducers.add(producer);
		notifyListeners();
	}
	
	public void removeBProducer(BProducer producer){
		this.bProducers.remove(producer);
		notifyListeners();
	}
	
	public List<BConsumer> getBConsumers(){
		 return this.bConsumers;
	 }
	 
	public List<BProducer> getBProducers(){
		 return this.bProducers;
	 }
	
	private void notifyListeners(){
		for (IModelChangedListener listener : this.changeListeners){
			listener.modelChanged(this);
		}
	}
	
	/**
	 * Registers a model changed listener
	 * @param listener the listener which will be called
	 * when the model has been changed
	 */
	public void addModelChangeListener(IModelChangedListener listener){
		this.changeListeners.add(listener);
	}
	
	/**
	 * Removes a registered model change listener
	 * @param listener the listener to remove
	 */
	public void removeChangeListener(IModelChangedListener listener){
		this.changeListeners.remove(listener);
	}
}
