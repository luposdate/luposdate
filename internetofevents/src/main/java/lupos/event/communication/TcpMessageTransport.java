/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import lupos.event.util.HandlerList;

/**
 * Implements a TCP-based message transport.
 */
public class TcpMessageTransport implements IMessageTransport {
	
	private Socket socket = null;
	private ProcessingThread processingThread = null;
	private boolean isConnected = false;
	
	private final BlockingQueue<ByteBuffer> outgoingMessageQueue = new LinkedBlockingQueue<ByteBuffer>();
	
	private final MessageReceivedHandlerList<ByteBuffer> msgReceivedHandlers = new MessageReceivedHandlerList<ByteBuffer>();
	private final HandlerList<IDisconnectedHandler> disconnectedHandlers;
	
	
	public TcpMessageTransport() throws SecurityException, NoSuchMethodException {
		this.disconnectedHandlers = new HandlerList<IDisconnectedHandler>(IDisconnectedHandler.class.getMethod("disconnected"));
	}
	
	/**
	 * Adds a handler object, which gets notified when a message was received.
	 */
	@Override
	public void addHandler(IMessageReceivedHandler<ByteBuffer> handler) {
		this.msgReceivedHandlers.add(handler);
	}
	
	/**
	 * Adds a handler object, which gets notified when the underlying socket got disconnected.
	 */
	@Override
	public void addHandler(IDisconnectedHandler handler) {
		this.disconnectedHandlers.add(handler);
	}
	
	private void setConnected(boolean connected) {
		this.isConnected = connected;
		// start or end the processing thread according to connection state
		if(connected) {
			this.processingThread = new ProcessingThread();
			this.processingThread.start();
		} else {
			if(this.processingThread != null) {
				this.processingThread.interrupt();
				this.processingThread = null;
			}
		}
	}
	
	/**
	 * Connects the underlying socket with given connection info.
	 * @param connectInfo Has to be an instance of {@link TcpConnectInfo}
	 */
	@Override
	public void connect(IConnectInfo connectInfo) throws Exception {
		TcpConnectInfo connectInfo2 = (TcpConnectInfo)connectInfo;
		try {
			System.out.println("connecting to "+connectInfo2.getHost()+"..");
			this.socket = new Socket(connectInfo2.getHost(), connectInfo2.getPort());
		} catch (Exception e) {
			this.setConnected(false);
			throw e;
		}
		// at this point a connection should be already established
		this.setConnected(true);
	}
	
	/**
	 * Closes the underlying socket.
	 */
	@Override
	public void disconnect() {
		
		System.out.println("now disconnecting..");
				
		this.setConnected(false);
		
		// close the socket
		try {
			this.socket.close();
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		
		this.disconnectedHandlers.callAll();
	}


	/**
	 * Waits for a connection and accepts it. The method blocks until a connection is made.
	 */
	@Override
	public boolean waitForConnection() {
		System.out.println("Waiting for incoming connection..");
		try {
			ServerSocket serverSocket = new ServerSocket(4444);
			this.socket = serverSocket.accept();
			serverSocket.close();			
		} catch (Exception e) {
			return false;
		}
		System.out.println("Connection established");
		this.setConnected(true);
		return true;
	}
	
	/**
	 * Sends a message.
	 * @param msg The message to be sent.
	 */
	@Override
	public void sendMessage(ByteBuffer msg) {
		if(msg == null)// || msg.length == 0)
			return;		
		
		this.outgoingMessageQueue.add(msg);
	}
	

	@Override
	public void messageReceived(ByteBuffer msg) {
		this.msgReceivedHandlers.callAll(this, msg);
	}
	
	
	/**
	 * This thread sends and receives pending messages in an infinite loop via a simple message-oriented protocol.
	 *
	 */
	private class ProcessingThread extends Thread  {			
		@Override
		public void run() {
			System.out.println("PROCESSING THREAD STARTED");
			try {
				DataInputStream inputStream = new DataInputStream(TcpMessageTransport.this.socket.getInputStream());
				DataOutputStream outputStream = new DataOutputStream(TcpMessageTransport.this.socket.getOutputStream());
				int bytesLeftToRead = 0;
				final ByteArrayOutputStream incomingMsg = new ByteArrayOutputStream();
				while(true) {
					// End this thread if the transport is not connected
					if(!TcpMessageTransport.this.isConnected || !TcpMessageTransport.this.socket.isConnected())
						break;
					
					// Transmit a message to be sent, if available
					if(false == TcpMessageTransport.this.outgoingMessageQueue.isEmpty()) {										
						ByteBuffer msg = TcpMessageTransport.this.outgoingMessageQueue.take();
						byte[] msgArr = new byte[msg.capacity()];
						msg.get(msgArr);
						
						outputStream.writeInt(msg.capacity());
						outputStream.write(msgArr);			
					}
					
					// if currently no message is received and at least 4 bytes are available..
					if(bytesLeftToRead == 0 && inputStream.available() >= 4) {
						System.out.println("reading size of next msg..");
						
						bytesLeftToRead = inputStream.readInt();
						incomingMsg.reset();
					}
					
					// Read so many bytes as available or are missing for the message					
					int bytesToRead = Math.min(inputStream.available(), bytesLeftToRead);
					if(bytesToRead > 0) {
						System.out.println("receiving data ("+bytesToRead+" bytes)..");
						
						byte[] buffer = new byte[bytesToRead];
						int bytesActuallyRead = inputStream.read(buffer);
						incomingMsg.write(buffer, 0, bytesActuallyRead);
						bytesLeftToRead -= bytesActuallyRead;
					}
					
					// if message has been completely received...
					if(bytesLeftToRead == 0 && incomingMsg.size() > 0) {
						System.out.println("incoming msg complete: ");
						byte[] msgArr = incomingMsg.toByteArray();
						ByteBuffer msg = ByteBuffer.wrap(msgArr);
						
						messageReceived(msg);
						incomingMsg.reset();
					}
					
					Thread.sleep(50);
				}				
			} catch (Exception e) {
				System.err.println(e);
				e.printStackTrace();
			}			
			disconnect();
			System.out.println("PROCESSING THREAD ENDED");
		}
	}


	@Override
	public boolean isConnected() {
		return this.isConnected;
	}
}
