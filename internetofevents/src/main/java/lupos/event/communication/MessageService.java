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
package lupos.event.communication;

import java.nio.ByteBuffer;
import java.util.*;

public class MessageService extends Observable implements IDisconnectedHandler{
	
	private IMessageTransport transport = null;
	private int transportPort = -1;
	
		
	public MessageService(Class<? extends IMessageTransport> transportClass) throws InstantiationException, IllegalAccessException {
		this.transport = transportClass.newInstance();
		this.transport.addHandler(this);
	}
	
	public void addHandler(IMessageReceivedHandler<ByteBuffer> handler) {		
		this.transport.addHandler(handler);
	} 
		
	public <T extends IConnectInfo> void connect(T connectInfo) throws Exception {
		this.transport.connect(connectInfo);
	}
	
	public void disconnect() {
		this.transport.disconnect();
	}

	public void sendMessage(ByteBuffer msg) {
		this.transport.sendMessage(msg);
	}

	public boolean waitForConnection() {
		return this.transport.waitForConnection();		
	}
	
	public boolean isConnected() {
		return this.transport.isConnected();
	}
	
	/**
	 * Adds a handler object which will be notified, if
	 * this message service recieves a disconnect
	 * @param handler the observing handler
	 */
	public void addDisconnectHandler(IDisconnectedHandler handler){
		this.transport.addHandler(handler);
	}

	@Override
	public void disconnected() {
		this.setChanged();
		this.notifyObservers();
	}
	
	/**
	 * Gets the tcp connection data of this message service
	 * @return the tcp connect info object or <code>null</code>
	 * if no port has been sent
	 */
	public TcpConnectInfo getConnectionInfo(){
		if (this.transportPort < 0){
			return null;
		}
		TcpConnectInfo tcpInfo = new TcpConnectInfo(this.transport.getHost(), this.transportPort);
		return tcpInfo;
	}
	
	/**
	 * Sets the port for this message service
	 * @param port a positive port number
	 */
	public void setConnectionPort(int port){
		this.transportPort = port;
	}
	
}
