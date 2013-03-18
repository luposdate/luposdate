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
package lupos.event.producer;

import java.util.ArrayList;
import java.util.List;

import javax.bluetooth.*;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.event.communication.SerializingMessageService;
import lupos.event.util.Literals;

/**
 * Searches for bluetooth devices in range and builds events for it.
 */
public class BTDevicesProducer extends ProducerBase {
	
	public final static String NAMESPACE = "http://localhost/events/BTDevices/";
	
	private final Literal TYPE = Literals.createURI(NAMESPACE, "BTDevicesEvent");
	private final Literal PREDICATE = Literals.createURI(NAMESPACE, "inRange");
	
	public BTDevicesProducer(SerializingMessageService msgService) {
		super(msgService, 15000);
		
	}

	@Override
	public List<List<Triple>> produce() {
		if (!LocalDevice.isPowerOn()) {
			System.out.println("Local bluetooth device is powered off");
			return null;
		}

		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();
			System.out.println("Initializing local bluetooth device ...");
			DiscoveryAgent agent = localDevice.getDiscoveryAgent();
			final Object inquiryCompletedEvent = new Object();
			
			final List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

			// this is the call back for the bluetooth driver
			DiscoveryListener listener = new DiscoveryListener() {
				
				@Override
				public void deviceDiscovered(RemoteDevice rd, DeviceClass dc) {
						devices.add(new BluetoothDevice(rd.getBluetoothAddress(), rd.isTrustedDevice()));
						System.out.println("Device discovered: " + rd.getBluetoothAddress());
				}


				@Override
				public void inquiryCompleted(int discType) {
					synchronized (inquiryCompletedEvent) {
						// tell the main thread that we are done
						System.out.println("inquiryCompleted");
						inquiryCompletedEvent.notifyAll();
					}
				}

				@Override
				public void serviceSearchCompleted(int transID, int respCode) {
					// just ignore
				}

				@Override
				public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
					// just ignore
				}
			};


			synchronized (inquiryCompletedEvent) {
				try {
					System.out.println("Launching bluetooth device discovery...");
					boolean started = agent.startInquiry(DiscoveryAgent.GIAC, listener);
					if (started) {
						inquiryCompletedEvent.wait();
					}
				} catch (BluetoothStateException e) {
					System.err.println("Error while starting the bluetooth agent");
					System.err.println(e);
					e.printStackTrace();
				} 
			}
			
			List<Triple> triples = new ArrayList<Triple>();
			triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Literals.RDF.TYPE, this.TYPE));
			
			for(BluetoothDevice bd : devices) {
				Literal obj = LiteralFactory.createTypedLiteral("\"" + (bd.address) + "\"", Literals.XSD.String);
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, this.PREDICATE, obj));
				// System.out.println("A:" + new Triple(Literals.AnonymousLiteral.ANONYMOUS, this.PREDICATE, obj));
			}

			return ProducerBase.fold(triples);
		} catch (Exception ex) {
			System.err.println(ex);
			ex.printStackTrace();
			return null;
		}
	}

	
	public static void main(String[] args) throws Exception {
		
		// create communication channel
		SerializingMessageService msgService = ProducerBase.connectToMaster();
		
		// start producer
		new BTDevicesProducer(msgService).start();
	}
	
	
	public class BluetoothDevice {

		private String address;
		private boolean paired;

		/**
		 * Default constructor which directly initializes the fields
		 * 
		 * @param address the address of the bluetooth device, e.g. "ED3B6CE568D9"
		 * @param paired true, if the device has been paired with the host and is therefore trusted
		 */
		public BluetoothDevice(String address, boolean paired) {
			this.address = address;
			this.paired = paired;
		}

		/**
		 * @return the address of the bluetooth device
		 */
		public String getAddress() {
			return this.address;
		}
		
		/**
		 * @return true, if the device has been paired with the host and is therefore trusted
		 */
		public boolean isPaired() {
			return this.paired;
		}

		@Override
		public String toString() {
			return this.address + (this.paired ? "!" : "?");
		}
		
	}
}
