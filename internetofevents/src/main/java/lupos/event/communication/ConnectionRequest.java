
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.event.communication;

import java.io.Serializable;
public class ConnectionRequest implements Serializable {

	private static final long serialVersionUID = -282986518722016527L;
	private int requestType = 1;
	private int port = -1;

	/** Constant <code>REQUESTTYPE_PRODUCER=0</code> */
	public static final int REQUESTTYPE_PRODUCER = 0;
	/** Constant <code>REQUESTTYPE_BROKER=1</code> */
	public static final int REQUESTTYPE_BROKER = 1;
	/** Constant <code>REQUESTTYPE_CONSUMER=2</code> */
	public static final int REQUESTTYPE_CONSUMER = 2;
	/** Constant <code>REQUESTTYPE_DISCONNECT=3</code> */
	public static final int REQUESTTYPE_DISCONNECT = 3;
	
	/**
	 * <p>Constructor for ConnectionRequest.</p>
	 *
	 * @param requestType a int.
	 */
	public ConnectionRequest(int requestType){
		if (requestType > 3) throw new IllegalArgumentException("RequestType must be valid");
		this.requestType = requestType;
	}
	
	/**
	 * <p>Setter for the field <code>port</code>.</p>
	 *
	 * @param port a int.
	 */
	public void setPort(int port){
		this.port = port;
	}
	
	/**
	 * <p>Getter for the field <code>requestType</code>.</p>
	 *
	 * @return a int.
	 */
	public int getRequestType(){
		return this.requestType;
	}
	
	/**
	 * <p>Getter for the field <code>port</code>.</p>
	 *
	 * @return a int.
	 */
	public int getPort(){
		return this.port;
	}

}
