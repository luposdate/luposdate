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

/**
 * Interface for message-oriented transport protocol implementations.
 *
 * @author groppe
 * @version $Id: $Id
 */
public interface IMessageTransport {
	/**
	 * <p>connect.</p>
	 *
	 * @param connectInfo a {@link lupos.event.communication.IConnectInfo} object.
	 * @throws java.lang.Exception if any.
	 */
	void connect(IConnectInfo connectInfo) throws Exception;
	/**
	 * <p>disconnect.</p>
	 */
	void disconnect();
	/**
	 * <p>waitForConnection.</p>
	 *
	 * @return a boolean.
	 */
	boolean waitForConnection();
	/**
	 * <p>sendMessage.</p>
	 *
	 * @param msg a {@link java.nio.ByteBuffer} object.
	 */
	void sendMessage(ByteBuffer msg);
	/**
	 * <p>messageReceived.</p>
	 *
	 * @param msg a {@link java.nio.ByteBuffer} object.
	 */
	void messageReceived(ByteBuffer msg);
	/**
	 * <p>addHandler.</p>
	 *
	 * @param handler a {@link lupos.event.communication.IMessageReceivedHandler} object.
	 */
	void addHandler(IMessageReceivedHandler<ByteBuffer> handler);
	/**
	 * <p>addHandler.</p>
	 *
	 * @param handler a {@link lupos.event.communication.IDisconnectedHandler} object.
	 */
	void addHandler(IDisconnectedHandler handler);
	/**
	 * <p>isConnected.</p>
	 *
	 * @return a boolean.
	 */
	boolean isConnected();
	/**
	 * Gets the host of this tcp connection
	 *
	 * @return the tcp hostname
	 */
	String getHost();
}
