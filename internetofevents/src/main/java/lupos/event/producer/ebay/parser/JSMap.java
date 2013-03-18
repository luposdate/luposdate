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
package lupos.event.producer.ebay.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Represents a whole JSON object, i.e. a mapping of string identifiers to JSON objects.
 */
public class JSMap extends JSObject {

	/**
	 * Map, containing the mapping the JSON object means
	 */
	private final HashMap<String, JSObject> map;

	/**
	 * Constructor for an empty JSON object.
	 */
	public JSMap() {
		this.map = new HashMap<String, JSObject>();
	}
	
	/**
	 * Adds a mapping to this JSON object.
	 * 
	 * @param	key		String identifier
	 * @param	value	JSON data structure identified by <code>key</code>
	 */
	public void set(String key, JSObject value) {
		this.map.put(key, value);
	}
	
	/**
	 * Returns the JSON data structure in this JSON object identified by <code>key</code>.
	 * If no such JSON data structure exists, <strong>null</strong> is returned.
	 * 
	 * @param	key		String identifier of the requested JSON data
	 * @return	The requested JSON data structure
	 */
	@Override
	public JSObject get(String key) {
		return this.map.get(key);
	}
	
	/**
	 * States, whether this JSON object contains JSON data identified by <code>key</code>.
	 * 
	 * @param	key		String identifier of the requested JSON data
	 * @return	<strong>true</strong>, if there's JSON data identified by <code>key</code>
	 * 			in this JSON object, <strong>false</strong> otherwise
	 */
	public boolean contains(String key) {
		return this.get(key) != null;
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	@Override
	public String toString(String indent) {
		StringBuilder builder = new StringBuilder().append('{');
		
		if (!this.map.isEmpty()) {
			Iterator<Entry<String, JSObject>> iterator = this.map.entrySet().iterator(); //this.map.keySet().iterator();
			Entry<String, JSObject> next = iterator.next();
			
			builder.append('\n').append(indent).append('\t').append(next.getKey()).append(" = ").append(next.getValue().toString(indent + '\t'));
			
			while (iterator.hasNext()) {
				next = iterator.next();
				
				builder.append(",\n").append(indent).append('\t').append(next.getKey()).append(" = ").append(next.getValue().toString(indent + '\t'));
			}
			
			builder.append('\n').append(indent);
		}
		
		return builder.append('}').toString();
	}
}
