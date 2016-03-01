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
import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.QueryResult;

/**
 * Checks input token list for potentially important substrings such as names or
 * places, tries to detect its type
 *
 *  the DBAnswer resulting from querying for either the substring that
 *         was not on the stoplist and also contained in the FeedMessage's title
 *         or the first one that was found in token list.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class SemanticInterpretation {

	private final ArrayList<String> tokens;
	private final ArrayList<String> foundNames = new ArrayList<String>();

	/**
	 * <p>Constructor for SemanticInterpretation.</p>
	 *
	 * @param tokens a {@link java.util.ArrayList} object.
	 */
	public SemanticInterpretation(final ArrayList<String> tokens) {
		this.tokens = tokens;
	}

	/**
	 * Checks if the single tokens can be associated with persons or places.
	 *
	 * @return the list of suggested types (person or place)
	 */
	public ArrayList<DBAnswer> detectType() {
		final Iterator<String> it = this.tokens.iterator();
		final QueryBuilder builder = new QueryBuilder();
		final ArrayList<DBAnswer> types = new ArrayList<DBAnswer>();
		BooleanResult askpersonresult = new BooleanResult();
		BooleanResult askplaceresult = new BooleanResult();
		QueryResult result = new QueryResult();
		while (it.hasNext()) {
			final String toCheck = it.next();

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
					&& !(askplaceresult.isTrue())) {
				continue;
			} else {
				result = builder.query(toCheck, "place");
				if(result.isEmpty()){
					result = builder.query(toCheck, "person");
				}
			}
			if (!(result.isEmpty())) {
				for (final Bindings b : result) {
					try {
						final DBAnswer ans = new DBAnswer(b);
						types.add(ans);
					} catch (final Exception e) {
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
	 * @param message a {@link lupos.event.producer.rsssemantics.FeedMessage} object.
	 * @return if exists: DBAnswer for substring that is also contained in title
	 *         element, else the first substring found in the cleaned feed
	 *         description.
	 * @throws java.lang.Exception if any.
	 */
	public DBAnswer interpret(final FeedMessage message) throws Exception {
		for (int i = 0; i < (this.tokens.size() - 3); i++) {
			final int j = i + 1;
			if (j < this.tokens.size()) {
				final String s1 = this.tokens.get(i);
				final String s2 = this.tokens.get(j);
				if (Character.isUpperCase(s1.charAt(0))
						&& Character.isUpperCase(s2.charAt(0))
						&& !(s1.equals(s2))) {
					this.foundNames.add(s1);
					this.foundNames.add(s2);
					this.foundNames.add(s1 + "_" + s2);
				} else if (j + 2 < this.tokens.size()) {
					final String s3 = this.tokens.get(j + 1);
					if (Character.isUpperCase(s1.charAt(0)) && s2.equals("&")
							&& Character.isUpperCase(s3.charAt(0))) {
						this.foundNames.add(s1 + "_" + s2 + "_" + s3);
					}
				}
			}
		}
		final ArrayList<DBAnswer> detected = this.detectType();
		final ArrayList<DBAnswer> closerChoice = new ArrayList<DBAnswer>();
		String longestPart = "";
		DBAnswer matchingAnswer = null;
		for (final DBAnswer db : detected) {

			/**
			 * check if label of dbAnswer is contained in title element of
			 * FeedMessage
			 */
			final String toCheck = db.getLabel();
			final ArrayList<String> singleTokens = new ArrayList<String>();

			/** split title & label... */
			if (toCheck != null) {
				for (final String s : toCheck.split("[ ]+")) {
					if (!singleTokens.isEmpty()) {
						singleTokens.add(singleTokens.size() - 1 + "_" + s);
					}
					singleTokens.add(s);
				}
			}
			final String messageTitle = message.getTitle();
			final ArrayList<String> splitTitle = new ArrayList<String>();
			if (!(messageTitle.equals(""))) {
				for (final String s : messageTitle.split("[ ]+")) {
					splitTitle.add(s);
					if (!splitTitle.isEmpty()) {
						splitTitle.add(splitTitle.get(splitTitle.size() - 1)
								+ "_" + s);
					}
				}
			}

			/** ... and compare each segment to the ones in the other ArrayList */
			final Iterator<String> it1 = singleTokens.iterator();
			while (it1.hasNext()) {
				final String labelpart = it1.next();
				final Iterator<String> it2 = splitTitle.iterator();
				while (it2.hasNext()) {
					final String titlepart = it2.next();
					if (labelpart.equals(titlepart)) {
						/**
						 * if current search string is longer than the previous,
						 * prefer its DBAnswer as a return value.
						 */
						if (labelpart.length() > longestPart.length()) {
							longestPart = labelpart;
						}
						matchingAnswer = db;
					}
					closerChoice.add(db);
				}
			}
		}
		if (matchingAnswer != null) {
			return matchingAnswer;
		} else if (closerChoice.isEmpty() == false) {
			return closerChoice.get(0);
		}

		/**
		 * if none of the label's segments matches one of the title's, return
		 * the first successful DBResult
		 */
		if(!(detected.isEmpty())) {
			return (detected.get(0));
		}
		return new DBAnswer(new BindingsMap());
	}
}
