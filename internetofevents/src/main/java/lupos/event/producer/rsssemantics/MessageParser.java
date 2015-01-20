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
package lupos.event.producer.rsssemantics;

import java.util.ArrayList;
import java.util.Iterator;
import lupos.datastructures.bindings.BindingsMap;


/**
 * Performs/initiates all relevant intepretation of RSSFeed's description
 *
 */
public class MessageParser {

	ArrayList<String>[] stoplist;
	ArrayList<String> tokenstemp = new ArrayList<String>();

	public MessageParser(ArrayList<String>[] stoplist) {
		this.stoplist = stoplist;
	}

	/**
	 * Checks if s is parseable as an Integer
	 * 
	 * @param s
	 * @return true if parseable, false if not
	 */
	public boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	/**
	 * Removes special characters from token list. tokens that contain a hyphen
	 * are formatted for further processing
	 * 
	 * @param ArrayList
	 *            <String> tokens
	 */
	public void cleanStrings() {

		Iterator<String> it = this.tokenstemp.iterator();
		String[] temparray = new String[this.tokenstemp.size()];
		// Array füllen
		for (int i = 0; i < this.tokenstemp.size(); i++) {
			temparray[i] = it.next();
		}

		for (int i = 0; i < temparray.length; i++) {
			temparray[i] = temparray[i].replace(";", "");
			temparray[i] = temparray[i].replace(":", "");
			temparray[i] = temparray[i].replace(",", "");
			temparray[i] = temparray[i].replace(".", "");
			temparray[i] = temparray[i].replace("\"", "");
			temparray[i] = temparray[i].replace("<", "");
			temparray[i] = temparray[i].replace(">", "");
			temparray[i] = temparray[i].replace("'", "");
			temparray[i] = temparray[i].replace("?", "");
			temparray[i] = temparray[i].replace("!", "");
			temparray[i] = temparray[i].replace("$", "");
			temparray[i] = temparray[i].replace("%", "");
			temparray[i] = temparray[i].replace("/", "");
			temparray[i] = temparray[i].replace("(", "");
			temparray[i] = temparray[i].replace(")", "");
			temparray[i] = temparray[i].replace("{", "");
			temparray[i] = temparray[i].replace("}", "");
			temparray[i] = temparray[i].replace("'", "");
			temparray[i] = temparray[i].replace("§", "");
			temparray[i] = temparray[i].replace("´", "");
			temparray[i] = temparray[i].replace("`", "");
			/**
			 * remove spaces between two or more tokens and hyphen
			 */
			temparray[i] = temparray[i].replace(" - ", "-");

		}

		this.tokenstemp.clear();

		/**
		 * fill tokenstemp with cleaned values
		 */
		for (String s2 : temparray) {
			this.tokenstemp.add(s2);
		}
	}

	public DBAnswer parseMessage(FeedMessage message) throws Exception {
		Frequency freq = new Frequency();
		String description;
		description = message.getDescription();
		if (!(description == null)) {
			String[] delim = { "[ ]+" };
			ArrayList<String> tokens = new ArrayList<String>();
			String languageTag = "de";

			/**
			 * Split description String into substrings, fill up tokenList
			 */
			for (int i = 0; i < delim.length; i++) {
				for (String t : description.split(delim[i])) {
					this.tokenstemp.add(t);
				}
				cleanStrings();

				/**
				 * Iterate through cleaned list of substrings and remove words
				 * contained by stoplist (stoplist.txt)
				 */
				Iterator<String> tokenstempit = this.tokenstemp.iterator();
				while (tokenstempit.hasNext()) {
					String t = tokenstempit.next();
					if ((!this.stoplist[0].contains(t.toLowerCase()))
							&& (languageTag == "de") && !isInteger(t)
							&& !(t.equals("-"))) {
						tokens.add(t);
					}

					else if ((!this.stoplist[1].contains(t.toLowerCase()))
							&& (languageTag == "en") && !isInteger(t)
							&& !(t.equals("-"))) {
						tokens.add(t);
					}

					else {
						tokens.remove(t);
					}
				}
			}

			/**
			 * Experimental: Create frequency list containing each substring's
			 * frequency (currently not used)
			 */
			Iterator<String> it = tokens.iterator();
			while (it.hasNext()) {
				String s = it.next();
				if (s != "") {
					int i = 0;
					while (i <= freq.getSubstrLength()) {
						if (i > 0) {
							if (freq.getSubstringAt(i - 1) != null
									&& freq.getSubstringAt(i - 1) == s) {

								// substring found, increase frequency
								freq.incFrequency(i - 1);
							}
						} else {
							// Add new substring to list...
							freq.addToSubstring(s);
							// including its frequency.
							freq.addToFrequency(1);
							break;
						}

						i++;
					}
				}
			}

			// initiate interpreting process
			SemanticInterpretation sem = new SemanticInterpretation(tokens);
			DBAnswer dbanswer = sem.interpret(message);

			// clean up
			tokens.clear();
			this.tokenstemp.clear();

			return dbanswer;
		} else {
			System.out.println("Article has no description.");
			return new DBAnswer(new BindingsMap());
		}
	}
}