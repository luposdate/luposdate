
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.autocomplete.strategies.ps;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import lupos.autocomplete.misc.Item;
import lupos.gui.anotherSyntaxHighlighting.ILuposParser;
import lupos.gui.anotherSyntaxHighlighting.ILuposToken;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__SemanticWeb;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.n3.turtle.TurtleEventHandler;
import com.hp.hpl.jena.n3.turtle.parser.ParseException;
import com.hp.hpl.jena.n3.turtle.parser.TurtleParser;
import com.hp.hpl.jena.n3.turtle.parser.TurtleParserConstants;
public class ParserIdentificationStrategyRDF extends ParserIdentificationStrategy {

	/**
	 * <p>Constructor for ParserIdentificationStrategyRDF.</p>
	 *
	 * @param r a {@link lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader} object.
	 * @param p a {@link lupos.gui.anotherSyntaxHighlighting.ILuposParser} object.
	 */
	public ParserIdentificationStrategyRDF(final LuposDocumentReader r, final ILuposParser p) {
		super(r, p);
	}

	TYPE__SemanticWeb[] tokenMap = lupos.gui.anotherSyntaxHighlighting.javacc.TurtleParser
			.getStaticTokenMap();
	ArrayList<String> possibilitiesList = null;

	/*
	 * fuellt die hashmap mit den Beispielen die ausprobiert werden
	 */
	/** {@inheritDoc} */
	@Override
	protected void fillMap() {
		this.hashmap.put("<IRIref>", "<a>");
		this.hashmap.put("<PNAME_NS>", "a:");
		this.hashmap.put("<PNAME_LN>", "a:b");
		this.hashmap.put("<BLANK_NODE_LABEL>", "_:a");
		this.hashmap.put("<VAR>", "?a");
		this.hashmap.put("<LANGTAG>", "@de");
		this.hashmap.put("<INTEGER>", "1");
		this.hashmap.put("<DECIMAL>", "1.1");
		this.hashmap.put("<DOUBLE>", "1.1E1");
		this.hashmap.put("<STRING_LITERAL1>", "\'a\'");
		this.hashmap.put("<STRING_LITERAL2>", "\"a\"");
		this.hashmap.put("<STRING_LITERAL_LONG1>", "\'\'\'a\'\'\'");
		this.hashmap.put("<STRING_LITERAL_LONG2>", "\"\"\"a\"\"\"");
		this.hashmap.put("<NIL>", "()");
		this.hashmap.put("<ANON>", "[]");
	}

	/*
	 * dokument wird geparst, falls kein fehler wird "NE" zurueckgegeben,
	 * bei ParseException wird ein "PE" vor die Fehlermeldung angehaengt,
	 * bei lexikalischem Fehler wird ein "LE" vor die Fehlermeldung angehaengt
	 */
	/** {@inheritDoc} */
	@Override
	public String handleException(final String document) {
		try {
			final TurtleParser parser = new TurtleParser(new StringReader(
					document));
			parser.setEventHandler(new TurtleEventHandler() {

				@Override
				public void triple(final int line, final int col, final Triple triple) {
				}

				@Override
				public void startFormula(final int line, final int col) {
				}

				@Override
				public void prefix(final int line, final int col, final String prefix, final String iri) {
				}

				@Override
				public void endFormula(final int line, final int col) {
				}
			});
			parser.parse();
			return "NE";

		} catch (final ParseException e1) {
			return "PE " + e1.toString();
		} catch (final Error re) {
			return "LE " + re.toString();
		}
	}

	/*
	 * hier werden fuer den Fall NoError die Tokens
	 * auf Validitaet als naechstes Wort geprueft
	 */
	/** {@inheritDoc} */
	@Override
	public ArrayList<Item> testforTokensNE(final String doc,
			final TYPE__SemanticWeb[] tokenMap, final int cursorPosition) {
		final String currentWord = this.getCurrentWord(doc, cursorPosition);
		String documentTest;
		final ArrayList<Item> possibilitiesList = new ArrayList<Item>();
		String testWordRaw;
		String testWord = null;
		String hashmapKey;
		boolean caseSensitiv;
		//tokenMap wird durchlaufen
		for (int i = 0; i < tokenMap.length; i++) {
			//hashmapKey und testWord werden gesetzt
			testWordRaw = TurtleParserConstants.tokenImage[i];
			if (testWordRaw.startsWith("\"")
					&& (!(tokenMap[i].toString().equals("WHITESPACE")))) {
				hashmapKey = null;
				testWord = testWordRaw.substring(1, testWordRaw.length() - 1);
				if (testWord.equals(":")) {
					hashmapKey = null;
					testWord = null;
				} else if (testWord.equals("=")) {
					testWord = null;
					hashmapKey = null;
				} else if (testWord.equals("=>")) {
					testWord = null;
					hashmapKey = null;
				}
				//Faelle fuer die reale Identifier in hashmap eingesetzt werden muessen
			} else if (this.hashmap.containsKey(testWordRaw)) {
				if (testWordRaw.startsWith("<PNAME")) {
					if (testWordRaw.equals("<PNAME_NS>")) {
						this.setExistingIdentifier(testWordRaw, "PREFIXLABEL",
								this.readerLocal, this.parserLocal);
					} else {
						this.setExistingIdentifier(testWordRaw, "QUALIFIEDURI",
								this.readerLocal, this.parserLocal);
					}
				}
				if (this.hashmap.containsKey(testWordRaw)) {
					testWord = this.hashmap.get(testWordRaw);
					hashmapKey = testWordRaw;
				} else {
					testWord = null;
					hashmapKey = null;
				}
			} else {
				testWord = null;
				hashmapKey = null;
			}

			//wenn testWord null ist wird nicht getestet
			if (testWord != null) {
				//setzt caseSensitiv Wahrheitswert
				if (tokenMap[i].toString().equals("RESERVEDWORD")) {
					caseSensitiv = false;
				} else {
					caseSensitiv = true;
				}
				//legt das Testdocument fest
				if (currentWord.equals("") || currentWord.equals(" ")) {
					documentTest = doc.substring(0, cursorPosition) + testWord;
				} else {
					documentTest = doc.substring(0, cursorPosition
							- currentWord.length())
							+ testWord;
				}
				//prueft testDokument
				final String testException = this.handleException(documentTest);
				final int indexBeforeCurrentWord = doc.substring(0,
						cursorPosition - currentWord.length()).length();
				//testException = NE
				if (testException.substring(0, 2).equals("NE")) {
					//Wenn hashmapKey existiert werden alle existierenden Elemente hinzugefuegt
					if (hashmapKey != null) {
						possibilitiesList.addAll(this.findExistingElements(
								hashmapKey, testWord, indexBeforeCurrentWord,
								this.readerLocal, this.parserLocal));

					//ansonsten nur testWord
					} else {
						possibilitiesList.add(new Item(testWord, caseSensitiv));
					}
					//TestException = PE
				} else if (testException.substring(0, 2).equals("PE")) {
					if (this.parseErrorWordPE(testException).equals("<EOF>")) {
						//Analog
						if (hashmapKey != null) {
							possibilitiesList.addAll(this.findExistingElements(
									hashmapKey, testWord,
									indexBeforeCurrentWord, this.readerLocal,
									this.parserLocal));

						} else {
							possibilitiesList.add(new Item(testWord,
									caseSensitiv));
						}
					}
				}

			}

		}
		return possibilitiesList;
	}

	/*
	 * extrahiert existierende Elemente des gegebenen Typs aus dem Dokument
	 */
	/** {@inheritDoc} */
	@Override
	public List<Item> findExistingElements(final String hashmapKey,
			final String hashmapValue, final int indexBeforeCurrentWord,
			final LuposDocumentReader r, final ILuposParser p) {
		final ArrayList<Item> arrayList = new ArrayList<Item>();
		if (hashmapKey.equals("<ANON>") || hashmapKey.equals("<NIL>")) {
			arrayList.add(new Item(hashmapValue, true));
			return arrayList;
		}
		String description;
		if (hashmapKey.equals("<IRI_REF>")) {
			description = "URI";
		} else if (hashmapKey.equals("<PNAME_LN>")) {
			description = "QUALIFIEDURI";
		} else if (hashmapKey.startsWith("<STRING_LITERAL")) {
			description = "LITERAL";
		} else if (hashmapKey.equals("<BLANKNODELABEL>")) {
			description = "BLANKNODE";
		} else if (hashmapKey.equals("<VAR>")) {
			description = "VARIABLE";
		} else if (hashmapKey.equals("<INTEGER>")) {
			description = "INTEGER";
		} else if (hashmapKey.equals("<DECIMAL>")
				|| hashmapKey.equals("<DOUBLE>")) {
			description = "DECIMAL";
		} else {
			description = null;
		}
		if (description != null) {
			final String content = r.getText().substring(0,
					indexBeforeCurrentWord);
			p.setReaderTokenFriendly(r, 0, content.length());
			ILuposToken token;
			String[] splittedToken;
			String tokenElement;
			int pos;
			while ((token = p.getNextToken(content)) != null) {
				splittedToken = token.toString().split(" ");
				if (!splittedToken[2].equals("ERROR")) {
					if (!splittedToken[9].startsWith("\"")) {
						tokenElement = splittedToken[9].toLowerCase();
					} else {
						pos = token.toString().indexOf("\"");
						tokenElement = token
								.toString()
								.substring(pos,
										token.toString().length())
								.toLowerCase();
					}
					if (splittedToken[2].equals(description)
							&& !(arrayList.contains(tokenElement))) {
						arrayList.add(new Item(tokenElement, true));
					}
				}

			}

		}

		return arrayList;
	}

	/*
	 * hier werden fuer den Fall ParserError die Tokens
	 * auf Validitaet als naechstes Wort geprueft
	 */
	/** {@inheritDoc} */
	@Override
	public ArrayList<Item> testforTokensPE(final String exception, final String doc,
			final TYPE__SemanticWeb[] tokenMap, final int cursorPosition) {
		final String currentWord = this.getCurrentWord(doc, cursorPosition);
		final String falseWord = this.parseErrorWordPE(exception);
		String documentTest;
		final ArrayList<Item> possibilitiesList = new ArrayList<Item>();
		String testWordRaw;
		String testWord = null;
		String hashmapKey;
		String mainError;
		String subError;
		boolean caseSensitiv;
		//tokenMap wird durchlaufen
		for (int i = 0; i < tokenMap.length; i++) {
			//hashmapKey und testWord werden gesetzt
			testWordRaw = TurtleParserConstants.tokenImage[i];
			if (testWordRaw.startsWith("\"")
					&& (!(tokenMap[i].toString().equals("WHITESPACE")))) {
				hashmapKey = null;
				testWord = testWordRaw.substring(1, testWordRaw.length() - 1);
				if (testWord.equals(":")) {
					hashmapKey = null;
					testWord = null;
				} else if (testWord.equals("=")) {
					testWord = null;
					hashmapKey = null;
				} else if (testWord.equals("=>")) {
					testWord = null;
					hashmapKey = null;
				}
			//Faelle fuer die reale Identifier in hashmap eingesetzt werden muessen
			} else if (this.hashmap.containsKey(testWordRaw)) {
				if (testWordRaw.startsWith("<PNAME")) {
					if (testWordRaw.equals("<PNAME_NS>")) {
						this.setExistingIdentifier(testWordRaw, "PREFIXLABEL",
								this.readerLocal, this.parserLocal);
					} else {
						this.setExistingIdentifier(testWordRaw, "QUALIFIEDURI",
								this.readerLocal, this.parserLocal);
					}
				}
				if (this.hashmap.containsKey(testWordRaw)) {
					testWord = this.hashmap.get(testWordRaw);
					hashmapKey = testWordRaw;
				} else {
					testWord = null;
					hashmapKey = null;
				}

				testWord = this.hashmap.get(testWordRaw);

			} else {
				testWord = null;
				hashmapKey = null;
			}
			//wenn testWord null ist wird nicht getestet
			if (testWord != null) {
				//setzt caseSensitiv Wahrheitswert
				if (tokenMap[i].toString().equals("RESERVEDWORD")) {
					caseSensitiv = false;
				} else {
					caseSensitiv = true;
				}
				//legt das Testdocument fest
				if (currentWord.equals("") || currentWord.equals(" ")) {
					documentTest = doc.substring(0, cursorPosition) + testWord;
				} else if (currentWord.endsWith(falseWord)
						|| falseWord.equals("<EOF>")) {
					documentTest = doc.substring(0, cursorPosition
							- currentWord.length())
							+ testWord;
				} else {
					documentTest = doc.substring(0, cursorPosition
							- currentWord.length())
							+ testWord;
				}
				//prueft testDokument
				final String testException = this.handleException(documentTest);
				final int indexBeforeCurrentWord = doc.substring(0,
						cursorPosition - currentWord.length()).length();
				//testException = NE
				if (testException.substring(0, 2).equals("NE")) {
					if (hashmapKey != null) {
						//Wenn hashmapKey existiert werden alle existierenden Elemente hinzugefuegt
						possibilitiesList.addAll(this.findExistingElements(
								hashmapKey, testWord, indexBeforeCurrentWord,
								this.readerLocal, this.parserLocal));
					//ansonsten nur testWord
					} else {
						possibilitiesList.add(new Item(testWord, caseSensitiv));
					}
				} else if (testException.substring(0, 2).equals("PE")) {
					if (hashmapKey != null) {
						possibilitiesList.addAll(this.findExistingElements(
								hashmapKey, testWord, indexBeforeCurrentWord,
								this.readerLocal, this.parserLocal));

					} else {
						possibilitiesList.add(new Item(testWord, caseSensitiv));
					}
				//TestException = PE
				} else if (testException.substring(0, 2).equals("PE")) {
					mainError = this.parseErrorWordPE(exception);
					subError = this.parseErrorWordPE(testException);
					if (mainError.equals(subError) || subError.equals("<EOF>")) {
						//Analog
						if (hashmapKey != null) {
							possibilitiesList.addAll(this.findExistingElements(
									hashmapKey, testWord,
									indexBeforeCurrentWord, this.readerLocal,
									this.parserLocal));
							possibilitiesList.remove(currentWord);

						} else {
							possibilitiesList.add(new Item(testWord,
									caseSensitiv));
						}
					}
				}
			}
		}
		return possibilitiesList;

	}

	/** {@inheritDoc} */
	@Override
	public TYPE__SemanticWeb[] getTokenMap() {
		return this.tokenMap;
	}

	/*
	 * hier werden fuer den Fall LexicalError die Tokens
	 * auf Validitaet als naechstes Wort geprueft
	 */
	/** {@inheritDoc} */
	@Override
	public ArrayList<Item> testForTokensLE(final String exception, final String doc,
			final TYPE__SemanticWeb[] tokenMap, final int cursorPosition) {

		final String currentWord = this.getCurrentWord(doc, cursorPosition);
		final String falseWord = this.parseErrorWordLE(exception);
		String documentTest;
		final ArrayList<Item> possibilitiesList = new ArrayList<Item>();
		String testWordRaw;
		String testWord = null;
		String hashmapKey;
		String mainError;
		String subError;
		boolean caseSensitiv;
		//tokenMap wird durchlaufen
		for (int i = 0; i < tokenMap.length; i++) {
			testWordRaw = TurtleParserConstants.tokenImage[i];
			//hashmapKey und testWord werden gesetzt
			if (testWordRaw.startsWith("\"")
					&& (!(tokenMap[i].toString().equals("WHITESPACE")))) {
				hashmapKey = null;
				testWord = testWordRaw.substring(1, testWordRaw.length() - 1);
				if (testWord.equals(":")) {
					hashmapKey = null;
					testWord = null;
				} else if (testWord.equals("=")) {
					testWord = null;
					hashmapKey = null;
				} else if (testWord.equals("=>")) {
					testWord = null;
					hashmapKey = null;
				}
				//Faelle fuer die reale Identifier in hashmap eingesetzt werden muessen
			} else if (this.hashmap.containsKey(testWordRaw)) {
				if (testWordRaw.startsWith("<PNAME")) {
					if (testWordRaw.equals("<PNAME_NS>")) {
						this.setExistingIdentifier(testWordRaw, "PREFIXLABEL",
								this.readerLocal, this.parserLocal);
					} else {
						this.setExistingIdentifier(testWordRaw, "QUALIFIEDURI",
								this.readerLocal, this.parserLocal);
					}
				}
				if (this.hashmap.containsKey(testWordRaw)) {
					testWord = this.hashmap.get(testWordRaw);
					hashmapKey = testWordRaw;
				} else {
					testWord = null;
					hashmapKey = null;
				}

				testWord = this.hashmap.get(testWordRaw);

			} else {
				testWord = null;
				hashmapKey = null;
			}
			//wenn testWord null ist wird nicht getestet
			if (testWord != null) {
				//setzt caseSensitiv Wahrheitswert
				if (tokenMap[i].toString().equals("RESERVEDWORD")) {
					caseSensitiv = false;
				} else {
					caseSensitiv = true;
				}
				//legt das Testdocument fest
				if (currentWord.equals("") || currentWord.equals(" ")) {
					documentTest = doc.substring(0,
							cursorPosition) + testWord;
				} else if (currentWord.endsWith(falseWord)
						|| falseWord.equals("<EOF>")) {
					documentTest = doc.substring(0, cursorPosition
							- currentWord.length())
							+ testWord;
				} else {
					documentTest = doc.substring(0, cursorPosition
							- currentWord.length())
							+ testWord;
				}
				//prueft testDokument
				final String testException = this.handleException(documentTest);
				final int indexBeforeCurrentWord = doc.substring(0,
						cursorPosition - currentWord.length()).length();
				//testException = NE
				if (testException.substring(0, 2).equals("NE")) {
					//Wenn hashmapKey existiert werden alle existierenden Elemente hinzugefuegt
					if (hashmapKey != null) {
						possibilitiesList.addAll(this.findExistingElements(
								hashmapKey, testWord, indexBeforeCurrentWord,
								this.readerLocal, this.parserLocal));
					//ansonsten nur testWord
					} else {
						possibilitiesList.add(new Item(testWord, caseSensitiv));
					}
				//TestException = PE
				} else if (testException.substring(0, 2).equals("PE")) {
					mainError = this.parseErrorWordLE(exception);
					subError = this.parseErrorWordPE(testException);
					if (mainError.equals(subError) || subError.equals("<EOF>")) {
						//Analog
						if (hashmapKey != null) {
							possibilitiesList.addAll(this.findExistingElements(
									hashmapKey, testWord,
									indexBeforeCurrentWord, this.readerLocal,
									this.parserLocal));
							possibilitiesList.remove(currentWord);

						} else {
							possibilitiesList.add(new Item(testWord,
									caseSensitiv));
						}
					}
				}
			}
		}
		return possibilitiesList;
	}

}
