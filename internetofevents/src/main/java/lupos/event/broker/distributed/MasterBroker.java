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
package lupos.event.broker.distributed;

import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import lupos.event.broker.distributed.model.BConsumer;
import lupos.event.broker.distributed.model.BProducer;
import lupos.event.broker.distributed.model.SubBrokerData;
import lupos.event.communication.BrokerNetworkUpdateMessage;
import lupos.event.communication.BrokerUpdateMessage;
import lupos.event.communication.BrokerUpdateMessageCollector;
import lupos.event.communication.IDisconnectedHandler;
import lupos.event.communication.IMessageReceivedHandler;
import lupos.event.communication.ConnectionRequest;
import lupos.event.communication.ModelUpdateMessage;
import lupos.event.communication.SerializingMessageService;
import lupos.event.communication.TcpConnectInfo;
import lupos.event.communication.TcpMessageTransport;

/**
 * This class represents the single master broker which
 * only handles management of severeal connecting producers,
 * brokers and consumers
 *
 * @author Kevin
 * @version $Id: $Id
 */
public class MasterBroker implements IMessageReceivedHandler<Serializable>, Runnable, IDisconnectedHandler{

	private Hashtable<Integer, SubBrokerData> subBrokers;
	
	private boolean threadToggle = false;
	
	/**
	 * <p>Constructor for MasterBroker.</p>
	 */
	public MasterBroker(){
		this.subBrokers = new Hashtable<Integer, SubBrokerData>();
	}
	
	private void start() throws Exception{
		new Thread(this).start();
		System.out.println("Master Broker started. Waiting for incoming services...");
		while (true){
			// create communication endpoint and wait for an incoming connection 
			SerializingMessageService msgService = new SerializingMessageService(TcpMessageTransport.class);
			msgService.addHandler2(this);
			msgService.waitForConnection();
			
		}
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * This thread method is the main calculating thread
	 * in this master broker which does all networking
	 * updates
	 */
	@Override
	public void run(){
		while (true){
			if (this.threadToggle){
				// send forwarding table to all subbrokers
				updateBrokerNetwork();
				this.threadToggle = false;
			} else{
				// Drop disconnected brokers
				updateConnectedBrokers();
				// look for connection network
				distributeConsumers();
				this.threadToggle = true;
			}
			try{
				// this sleep or in general this thread
				// could be optimized for faster reaction times
				Thread.sleep(4000);
			} catch (InterruptedException ie){
				System.err.println("Warning: MasterBroker - Sleep time has been interrupted");
			}
		}
	}
	
	/**
	 * Iterates through all sub brokers and
	 * pings them with a dummy message to
	 * force a disconnected()-call on this broker
	 */
	private void updateConnectedBrokers(){
		for (SubBrokerData subBroker : this.subBrokers.values()){
			try {
				subBroker.getMessageService().sendMessage(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * updates Broker Network connections
	 */
	private void updateBrokerNetwork(){		
		for(SubBrokerData sub : this.subBrokers.values()){
			Hashtable<String, List<TcpConnectInfo>> table = new Hashtable<String, List<TcpConnectInfo>>();
			for(String produced : sub.getProducedEvents()){
				List<TcpConnectInfo> receiverBroker = new LinkedList<TcpConnectInfo>();
				for(SubBrokerData sub2 : this.subBrokers.values()){
					if(!sub.equals(sub2)){
						for(String consumed : sub2.getConsumedEvents()){
							if(produced.equals(consumed)){
								receiverBroker.add(sub2.getMessageService().getConnectionInfo());
							}
						}
					}
				}
				table.put(produced, receiverBroker);
			}
			BrokerNetworkUpdateMessage bnum = new BrokerNetworkUpdateMessage(table);
			try {
				sub.getMessageService().sendMessage(bnum);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Sent a network update to all SubBrokers");
	}
	
	/**
	 * Gets the connection info for a requesting producer
	 * to connect to a appropriate sub broker
	 * @return connection info to the free or least used broker
	 */
	private TcpConnectInfo getProducerConnectBroker(){
		
		int temp = Integer.MAX_VALUE;
		SubBrokerData broker = null;
		for (SubBrokerData subBroker : this.subBrokers.values()){
			int size = subBroker.getRegisteredProducers().size();
			if(size<temp){
				temp = size;
				broker = subBroker;	
			}
		}
		
		return broker.getMessageService().getConnectionInfo();
	}
	
	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] args){
		MasterBroker master = new MasterBroker();
		try{
			master.start();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void messageReceived(Object src, Serializable msg) {
		if (msg instanceof ConnectionRequest){
			SerializingMessageService msgService = (SerializingMessageService) src;
			msgService.setConnectionPort(((ConnectionRequest)msg).getPort());
			
			switch (((ConnectionRequest) msg).getRequestType()){
			
			case ConnectionRequest.REQUESTTYPE_PRODUCER:
				TcpConnectInfo tcpInfo = getProducerConnectBroker();
				try {
					msgService.sendMessage(tcpInfo);
					System.out.println("Producer redirected to "+tcpInfo.getHost()+":"+tcpInfo.getPort());
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
				
			case ConnectionRequest.REQUESTTYPE_BROKER:
				// put this sub broker in a hashtable
				SubBrokerData subBroker = new SubBrokerData(msgService);
				this.subBrokers.put(subBroker.getID(), subBroker);
				subBroker.getMessageService().addDisconnectHandler(this);
				System.out.println("Registered a SubBroker connection: "+msgService.getConnectionInfo().getHost()+":"+msgService.getConnectionInfo().getPort());
				
				break;
				
			case ConnectionRequest.REQUESTTYPE_CONSUMER:
				TcpConnectInfo tcpInfo2 = getConsumerConnectBroker();
				try {
					msgService.sendMessage(tcpInfo2);
					System.out.println("Consumer redirected to "+tcpInfo2.getHost()+":"+tcpInfo2.getPort());
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;	
			}
			
		}
		
		else if (msg instanceof ModelUpdateMessage){
			TcpConnectInfo tcpInfo = ((SerializingMessageService)src).getConnectionInfo();
			SubBrokerData subBroker = null;
			for (SubBrokerData sub : this.subBrokers.values()){
				if (sub.getMessageService().getConnectionInfo().equals(tcpInfo)){
					subBroker = sub;
					break;
				}
			}
			if (subBroker != null){
				// update model of this sub broker
				ModelUpdateMessage message = (ModelUpdateMessage)msg;
				subBroker.setRegisteredProducers(message.getProducers());
				subBroker.setRegisteredConsumers(message.getConsumers());
				System.out.println("Recieved and stored update from SubBroker "+tcpInfo.getHost()+":"+tcpInfo.getPort());
			}
		}
	}
	

	
	/**
	 * Gets the connection info for a requesting consumer
	 * to connect to a appropriate sub broker
	 * @return connection info for the broker with most producers and least consumers
	 */
	private TcpConnectInfo getConsumerConnectBroker(){
		int tempProd = 0;
		int tempCons = Integer.MAX_VALUE;
		SubBrokerData broker = null;
		for (SubBrokerData subBroker : this.subBrokers.values()){
			int sizeCons = subBroker.getRegisteredConsumers().size();
			int sizeProd = subBroker.getRegisteredProducers().size();
			if((sizeCons<=tempCons && sizeProd>tempProd)||(sizeCons<tempCons && sizeProd>=tempProd)){
				tempCons = sizeCons;
				tempProd = sizeProd;
				broker = subBroker;	
			}
		}
		
		return broker.getMessageService().getConnectionInfo();
	}
	

	/**
	 * distribute the consumers to brokers for efficient transmission
	 */
	public void distributeConsumers(){	
		
		for (SubBrokerData subBroker : this.subBrokers.values()){
			List<BConsumer> regCon = subBroker.getRegisteredConsumers();
			BrokerUpdateMessageCollector messageList = new BrokerUpdateMessageCollector();
			
			for(BConsumer curCon : regCon){
				int sum = 0;
				int ownSum = 0;
				SubBrokerData chosenBroker = null;
				
				for (SubBrokerData subBroker2 : this.subBrokers.values()){
					int tempSum=0;
					List<BProducer> regPro = subBroker2.getRegisteredProducers();
					for(BProducer curPro : regPro){
						for(String curConEvent : curCon.getWantedEvents()){
							if(curPro.getProducedEvent().equals(curConEvent)){
								if(!subBroker.equals(subBroker2)){
									tempSum++;
								} else{
									ownSum++;
								}
							}
						}
						
					}
					if(tempSum>sum){
						sum = tempSum;
						chosenBroker = subBroker2;
					}
				}
								
				if(sum>ownSum){
					BrokerUpdateMessage message = new BrokerUpdateMessage(chosenBroker.getMessageService().getConnectionInfo(),curCon);
					messageList.addMessage(message);
					System.out.println("Forwarded Consumer from "+subBroker.getMessageService().getConnectionInfo().getHost()
							+":"+subBroker.getMessageService().getConnectionInfo().getPort()+" to "
							+chosenBroker.getMessageService().getConnectionInfo().getHost()+":"
							+chosenBroker.getMessageService().getConnectionInfo().getPort());
				}
			}
			try {
				subBroker.getMessageService().sendMessage(messageList);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method will be called after a client has been
	 * disconnected. In fact this method will only handle
	 * disconnects by subbroker and will remove them from
	 * the connected list
	 */
	@Override
	public void disconnected() {
		List<SubBrokerData> toDrop = new LinkedList<SubBrokerData>();
		for (SubBrokerData subBroker : this.subBrokers.values()){
			if (!subBroker.getMessageService().isConnected()){
				toDrop.add(subBroker);
			}
		}
		for (SubBrokerData drop : toDrop){
			this.subBrokers.remove(drop);
			System.out.println("Dropped a SubBroker");
		}
	}
}
