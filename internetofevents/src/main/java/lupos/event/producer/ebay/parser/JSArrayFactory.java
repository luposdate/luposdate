/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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

import java.util.LinkedList;

/**
 * Factory for incremental construction of an array of JSON data structures.
 */
public class JSArrayFactory extends JSONFactory {

	/**
	 * List of JSON data structures yet appended to the array
	 */
	private LinkedList<JSObject> list = new LinkedList<JSObject>();
	
	@Override
	public boolean append(char c) {
		boolean next = !this.finished;
		
		// If the array is not yet completed ...
		if (next) {
			/*
			 * ... delegate construction to the currently constructed JSON data structure
			 * contained by the array
			 */
			boolean goon = (this.factory == null) ? false : this.factory.append(c);
			
			/*
			 * If the currently constructed JSON data structure contained in the array
			 * is completed (or there isn't any) ...
			 */
			if (!goon) {
				// ... and if there is a completed JSON data structure ...
				if (this.factory != null) {
					// ... add it to the list of array elements
					this.list.add(this.factory.create());
					this.factory = null;
				}
				
				// If c is termination character, finish construction
				if (c == ']') {
					this.finished = true;
				}
				/*
				 * Otherwise begin construction of te next JSON data structure contained
				 * by the array
				 */
				else {				
					this.factory = JSONFactory.openWith(c);
				}
			}
		}
		
		return next;
	}

	@Override
	public JSObject create() {
		return new JSOArray(this.list.toArray(new JSObject[this.list.size()]));
	}
}
