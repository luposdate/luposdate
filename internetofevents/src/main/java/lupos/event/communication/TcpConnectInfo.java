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

import java.io.Serializable;

/**
 * Holds information required to connect to a TCP endpoint.
 */
public class TcpConnectInfo implements IConnectInfo, Serializable {

	private static final long serialVersionUID = 940289196762068761L;
	private String host;
	private int port;

	public TcpConnectInfo(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() { 
		return this.host; 
	}

	public int getPort() { 
		return this.port; 
	}
	
	/**
	 * Checks whether two TcpConnectInfo
	 * objects are equal which means the connection
	 * data are the same
	 */
	@Override
	public boolean equals(Object o){
		if (o instanceof TcpConnectInfo){
			TcpConnectInfo obj = (TcpConnectInfo) o;
			return obj.host.equals(this.host) && obj.port == this.port;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return this.host.hashCode()+this.port;
	}
}
