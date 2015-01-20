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

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.BooleanResult;

/**
 * Checks input token list for potentially important substrings such as names or
 * places, tries to detect its type
 * 
 * @return the DBAnswer resulting from querying for either the substring that
 *         was not on the stoplist and also contained in the FeedMessage's title
 *         or the first one that was found in token list.
 */
public class SemanticInterpretation {

	private final ArrayList<String> tokens;
	private ArrayList<String> foundNames = new ArrayList<String>();

	public SemanticInterpretation(ArrayList<String> tokens) {
		this.tokens = tokens;
	}

	/**
	 * Checks if the single tokens can be associated with persons or places.
	 * 
	 * @return the list of suggested types (person or place)
	 */
	public ArrayList<DBAnswer> detectType() {
		Iterator<String> it = this.tokens.iterator();
		QueryBuilder builder = new QueryBuilder();
		ArrayList<DBAnswer> types = new ArrayList<DBAnswer>();
		BooleanResult askpersonresult = new BooleanResult();
		BooleanResult askplaceresult = new BooleanResult();
		QueryResult result = new QueryResult();
		while (it.hasNext()) {
			String toCheck = it.next();

			/** Try if person or place with name (token substring) exists in DB */
			askpersonresult = (BooleanResult) builder.query(toCheck,
					"askperson");
			System.out.println(askpersonresult);
			askplaceresult = (BooleanResult) builder.query(toCheck, "askplace");
			System.out.println(askplaceresult);

			/**
			 * If results for askpersonresult & askplaceresult are both true,
			 * prefer the person result, else choose the one whose QueryResult
			 * contains true
			 */
			if (askpersonresult.isTrue() && !(askplaceresult.isTrue())) {
				result = builder.query(toCheck, "person");
			} else if (!(askpersonresult.isTrue()) && askplaceresult.isTrue()) {
				result = builder.query(toCheck, "place");
			} else if (!(askpersonresult.isTrue())
					&& !(askplaceresult.isTrue()))
				continue;
			else {
				result = builder.query(toCheck, "place");
				if(result.isEmpty()){
					result = builder.query(toCheck, "person");
				}
			}
			if (!(result.isEmpty())) {
				for (Bindings b : result) {
					try {
						DBAnswer ans = new DBAnswer(b);
						types.add(ans);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return types;
	}

	/**
	 * Detects names (two or more consecutive uppercase substrings), initiates
	 * type detection process and checks if substrings are also contained in
	 * feed title.
	 * 
	 * @param message
	 * @return if exists: DBAnswer for substring that is also contained in title
	 *         element, else the first substring found in the cleaned feed
	 *         description.
	 * @throws Exception
	 */
	public DBAnswer interpret(FeedMessage message) throws Exception {
		for (int i = 0; i < (this.tokens.size() - 3); i++) {
			int j = i + 1;
			if (j < this.tokens.size()) {
				String s1 = this.tokens.get(i);
				String s2 = this.tokens.get(j);
				if (Character.isUpperCase(s1.charAt(0))
						&& Character.isUpperCase(s2.charAt(0))
						&& !(s1.equals(s2))) {
					this.foundNames.add(s1);
					this.foundNames.add(s2);
					this.foundNames.add(s1 + "_" + s2);
				} else if (j + 2 < this.tokens.size()) {
					String s3 = this.tokens.get(j + 1);
					if (Character.isUpperCase(s1.charAt(0)) && s2.equals("&")
							&& Character.isUpperCase(s3.charAt(0))) {
						this.foundNames.add(s1 + "_" + s2 + "_" + s3);
					}
				}
			}
		}
		ArrayList<DBAnswer> detected = detectType();
		ArrayList<DBAnswer> closerChoice = new ArrayList<DBAnswer>();
		String longestPart = "";
		DBAnswer matchingAnswer = null;
		for (DBAnswer db : detected) {

			/**
			 * check if label of dbAnswer is contained in title element of
			 * FeedMessage
			 */
			String toCheck = db.getLabel();
			ArrayList<String> singleTokens = new ArrayList<String>();

			/** split title & label... */
			if (toCheck != null)
				for (String s : toCheck.split("[ ]+")) {
					if (singleTokens.size() > 0) {
						singleTokens.add(singleTokens.size() - 1 + "_" + s);
					}
					singleTokens.add(s);
				}
			String messageTitle = message.getTitle();
			ArrayList<String> splitTitle = new ArrayList<String>();
			if (!(messageTitle.equals("")))
				for (String s : messageTitle.split("[ ]+")) {
					splitTitle.add(s);
					if (splitTitle.size() > 0) {
						splitTitle.add(splitTitle.get(splitTitle.size() - 1)
								+ "_" + s);
					}
				}

			/** ... and compare each segment to the ones in the other ArrayList */
			Iterator<String> it1 = singleTokens.iterator();
			while (it1.hasNext()) {
				String labelpart = it1.next();
				Iterator<String> it2 = splitTitle.iterator();
				while (it2.hasNext()) {
					String titlepart = it2.next();
					if (labelpart.equals(titlepart)) {
						/**
						 * if current search string is longer than the previous,
						 * prefer its DBAnswer as a return value.
						 */
						if (labelpart.length() > longestPart.length())
							longestPart = labelpart;
						matchingAnswer = db;
					}
					closerChoice.add(db);
				}
			}
		}
		if (matchingAnswer != null)
			return matchingAnswer;
		else if (closerChoice.isEmpty() == false)
			return closerChoice.get(0);

		/**
		 * if none of the label's segments matches one of the title's, return
		 * the first successful DBResult
		 */
		if(!(detected.isEmpty())) return (detected.get(0));
		return new DBAnswer(new BindingsMap());
	}
}