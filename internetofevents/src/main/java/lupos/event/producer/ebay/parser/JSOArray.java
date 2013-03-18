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

import java.util.Arrays;

/**
 * Representing an array of JSON data structures.
 * The array has a fixed length.
 */
public class JSOArray extends JSObject {

	/**
	 * Array of JSON data structure representatives
	 */
	private final JSObject[] array;
	
	/**
	 * Number of JSON data structures stored in this array
	 */
	public final int length;

	/**
	 * Constructor.
	 * 
	 * @param	array	JSON data structure representatives
	 */
	public JSOArray(JSObject[] array) {
		this.array = array;
		this.length = (array != null) ? array.length : -1;
	}
	
	/**
	 * Returns the data in the <code>index</code>-th field of the array.
	 * If <code>index</code> is out of the array's range <strong>null</strong> is
	 * returned.
	 * 
	 * @param	index	Index of the requested array field
	 * @return	The requested array field's data
	 */
	@Override
	public JSObject get(int index) {
		return (this.array == null || index < 0 || index >= this.length)
				? null
				: this.array[index];
	}

	@Override
	public String toString() {
		return Arrays.toString(this.array);
	}

	@Override
	public JSObject get(String key) {
		JSObject got = null;
		
		try {
			got = this.get(Integer.parseInt(key));
		}
		catch (NumberFormatException nfe) {
			got = this.get(0).get(key);
		}
		
		return got;
	}

	@Override
	public String toString(String indent) {
		StringBuilder builder = new StringBuilder().append('[');
		
		if (this.array.length > 0) {
			builder.append('\n').append(indent).append('\t').append(this.array[0].toString(indent + '\t'));
			
			for (int i = 1; i < this.array.length; i++) {
				builder.append(",\n").append(indent).append('\t').append(this.array[i].toString(indent + '\t'));
			}
			
			builder.append('\n').append(indent);
		}
		
		return builder.append(']').toString();
	}
}
