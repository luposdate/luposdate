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
package lupos.event.pubsub;

import java.io.Serializable;
import java.util.UUID;

public class Subscription implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Each subscription is identified by its ID //TODO UUID could result in collision
	 */
	private final String id = UUID.randomUUID().toString();
	

	private String name;
	
	private String query;
	
	
	public Subscription(String query) {
		this("NO_NAME", query);
	}
	
	public Subscription(String name, String query) {
		this.name = name;
		this.query = query;
	}
	
	public void setName(String name) { this.name = name; }
	public void setQuery(String query) { this.query = query; }

	public String getQuery() { return this.query; }
	public String getName() { return this.name; }
	public String getId() { return this.id; }
	
	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * Checks for equality of this Subscription and another Subscription OR a String.
	 * In case of obj being a String, it gets directly compared with the id of this Subscription instance. 
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Subscription) {
			Subscription other = (Subscription)obj;
			return this.id.equals(other.id);
		}
		else if(obj instanceof String) {
			String str = (String)obj;
			return this.id.equals(str);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
}
