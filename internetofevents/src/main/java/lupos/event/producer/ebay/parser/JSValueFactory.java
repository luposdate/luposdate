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
 * Factory for incremental construction of a JSON string.
 */
public class JSValueFactory extends JSONFactory {

	/**
	 * Current character data.
	 */
	private StringBuilder value = new StringBuilder();
	
	/**
	 * States, whether the next character has been escaped
	 * (important for the termination character and the escape character itself).
	 */
	private boolean escaped = false;
	
	@Override
	public boolean append(char c) {
		boolean next = !this.finished;
		
		// Handle character c, if string isn't completed yet
		if (next) {
			// If c has been escaped, append it and turn off escape mode
			if (this.escaped) {
				this.value.append(c);
				this.escaped = false;
			}
			// If c is termination character, finish construction 
			else if (c == '"') {
				this.finished = true;
			}
			// If c is escape character, turn on escape mode
			else if (c == '\\') {
				this.escaped = true;
			}
			// Otherwise, just append c
			else {
				this.value.append(c);
			}
		}
		
		return next;
	}

	@Override
	public JSObject create() {
		return new JSValue(this.value.toString());
	}
}
