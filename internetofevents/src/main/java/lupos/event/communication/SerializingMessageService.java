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

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Extends {@link MessageService} to serialize before sending an message and deserializing after receiving a message.
 */
public class SerializingMessageService extends MessageService implements IMessageReceivedHandler<ByteBuffer> {
	
	private final MessageReceivedHandlerList<Serializable> msgReceivedHandlers = new MessageReceivedHandlerList<Serializable>();
	

	public SerializingMessageService(Class<? extends IMessageTransport> transportClass) throws Exception {
		super(transportClass);
		super.addHandler(this);
	}
	
	public void addHandler2(IMessageReceivedHandler<Serializable> handler) {
		this.msgReceivedHandlers.add(handler);
	}


	/**
	 * Serializes the given object before sending it.
	 * @param obj
	 * @throws IOException
	 */
	public void sendMessage(Serializable obj) throws IOException {	
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);   
		out.writeObject(obj);
		byte[] serialized = bos.toByteArray();
		out.close();
		bos.close();
		
		ByteBuffer msg = ByteBuffer.wrap(serialized);
		super.sendMessage(msg);
	}
	
	/**
	 * Callback method for {@link MessageService}, which gets called after a message got received.
	 * The received message get deserialized 
	 */
	@Override
	public void messageReceived(Object src, ByteBuffer msg) {
		try {
			byte[] arr = new byte[msg.capacity()];
			msg.get(arr);
			ByteArrayInputStream bis = new ByteArrayInputStream(arr);
			
			ObjectInput in = new ObjectInputStream(bis);
			
			Serializable obj = (Serializable)in.readObject();
			in.close();
			bis.close();
			this.msgReceivedHandlers.callAll(this, obj);
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		} 		
	}
}
