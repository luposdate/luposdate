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
package lupos.event.producer.ebay.parser;

/**
 * Common interface for all structures possibly occurring in JSON data.
 *
 * @version $Id: $Id
 */
public abstract class JSObject {

	/**
	 * Returns the property <code>key</code> of this object.
	 *
	 * @return a {@link lupos.event.producer.ebay.parser.JSObject} object.
	 */
	abstract public JSObject get(String key);
	
	/**
	 * Returns the property <code>index</code> of this object.
	 *
	 * @return a {@link lupos.event.producer.ebay.parser.JSObject} object.
	 */
	public JSObject get(int index) {
		return this.get("" + index);
	}
	
	/**
	 * Returns a property within this object.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String access(String path) {
		JSObject current = this;
		String[] nodes = (path != null) ? path.split("\\.") : new String[0];
		
		for (int i = 0; i < nodes.length; i++) {
			if (current != null) {
				current = current.get(nodes[i]);
			}
			else {
				break;
			}
		}
		
		return (current == null) ? null : current.toString();
	}
	
	/**
	 * Returns a indented string representation of this object.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	abstract public String toString(String indent);
}
