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
package lupos.event.producers.ebay.parser;

/**
 * Parser to interpret JSON data.
 */
public class JSONParser {

	/**
	 * Hidden constructor.
	 */
	private JSONParser() {
		// just to hide the constructor
	}
	
	/**
	 * Parses the JSON data contained in the string <code>str</code>, building an object
	 * model of the data.
	 * 
	 * @param	str		JSON data
	 * @return	Root element of the object model
	 */
	public static JSObject parse(String str) {
		JSONFactory model = null;
		JSONFactory factory = null;

		// Read the JSON data character-wise
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			boolean goon = (factory == null) ? false : factory.append(c);
			
			/*
			 * If the currently constructed JSON data structure
			 * contained in the JSON data is completed (or there isn't any) ...
			 */
			if (!goon) {
				// ... and if no root element is set yet, set it
				if (factory != null && model == null) {
					model = factory;
				}
				
				/*
				 * Begin construction of the next JSON data structure
				 * contained in the JSON data
				 */
				factory = JSONFactory.openWith(c);
			}
		}
			
		if (model != null){
			return model.create();
		} else {
			if(factory!=null){
				return factory.create();
			} else {
				return null;
			}
		}
	}
}
