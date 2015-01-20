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
package lupos.autocomplete.strategies.ps;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import lupos.autocomplete.misc.Item;
import lupos.autocomplete.misc.RIFParserHelper;
import lupos.gui.anotherSyntaxHighlighting.ILuposParser;
import lupos.gui.anotherSyntaxHighlighting.ILuposToken;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__SemanticWeb;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.rif.generated.parser.ParseException;
import lupos.rif.generated.parser.RIFParser;
import lupos.rif.generated.parser.RIFParserConstants;

public class ParserIdentificationStrategyRIF extends ParserIdentificationStrategy {

	public ParserIdentificationStrategyRIF(final LuposDocumentReader r, final ILuposParser p) {
		super(r, p);
	}

	TYPE__SemanticWeb[] tokenMap = lupos.gui.anotherSyntaxHighlighting.javacc.RIFParser
			.getStaticTokenMap();
	ArrayList<String> possibilitiesList = null;

	/*
	 * fuellt die hashmap mit den Beispielen die ausprobiert werden
	 */
	@Override
	protected void fillMap() {
		this.hashmap.put("<Q_URIref>", "<a>");
		this.hashmap.put("<QNAME_NS>", "a:");
		this.hashmap.put("<QNAME>", "a:b");
		this.hashmap.put("<BNODE_LABEL>", "_:a");
		this.hashmap.put("<VAR>", "?a");
		this.hashmap.put("<LANGTAG>", "@de");
		this.hashmap.put("<INTEGER_10>", "1");
		this.hashmap.put("<FLOATING_POINT>", "1.1");
		this.hashmap.put("<DOUBLE>", "1.1E1");
		this.hashmap.put("<STRING_LITERAL1>", "\'a\'");
		this.hashmap.put("<STRING_LITERAL2>", "\"a\"");
		this.hashmap.put("<STRING_LITERALLONG1>", "\'\'\'a\'\'\'");
		this.hashmap.put("<STRING_LITERALLONG2>", "\"\"\"a\"\"\"");
	}

	/*
	 * dokument wird geparst, falls kein fehler wird "NE" zurueckgegeben,
	 * bei ParseException wird ein "PE" vor die Fehlermeldung angehaengt,
	 * bei lexikalischem Fehler wird ein "LE" vor die Fehlermeldung angehaengt
	 */
	@Override
	public String handleException(final String document) {
		try {
			final RIFParser parser = new RIFParser(new StringReader(document));
			parser.CompilationUnit();

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
			testWordRaw = RIFParserConstants.tokenImage[i];
			if (testWordRaw.startsWith("\"")
					&& (!(tokenMap[i].toString().equals("WHITESPACE")))) {
				hashmapKey = null;
				testWord = testWordRaw.substring(1, testWordRaw.length() - 1);
				if (testWord.equals("?")) {
					testWord = "?a";
					hashmapKey = "?";
				}
			//Faelle fuer die reale Identifier in hashmap eingesetzt werden muessen
			} else if (this.hashmap.containsKey(testWordRaw)) {
				if (testWordRaw.startsWith("<QNAME")) {
					if (testWordRaw.equals("<QNAME_NS>")) {
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
				documentTest = doc.substring(0,
						cursorPosition) + testWord;
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


	@Override
	public ArrayList<Item> testforTokensPE(final String exception, final String doc,
			final TYPE__SemanticWeb[] tokenMap, final int cursorPosition) {
		final String currentWord = this.getCurrentWord(doc, cursorPosition);
		final String falseWord = this.parseErrorWordPE(exception);
		final int falseWordlength = falseWord.length();

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
			testWordRaw = RIFParserConstants.tokenImage[i];
			//hashmapKey und testWord werden gesetzt
			if (testWordRaw.startsWith("\"")
					&& (!(tokenMap[i].toString().equals("WHITESPACE")))) {
				hashmapKey = null;
				testWord = testWordRaw.substring(1, testWordRaw.length() - 1);
				if (testWord.equals("?")) {
					hashmapKey = "?";
					testWord = "?a";
				}
			//Faelle fuer die reale Identifier in hashmap eingesetzt werden muessen
			} else if (this.hashmap.containsKey(testWordRaw)) {
				if (testWordRaw.startsWith("<QNAME")) {
					if (testWordRaw.equals("<QNAME_NS>")) {
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
							- falseWordlength - 1)
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
					mainError = this.parseErrorWordPE(exception);
					subError = this.parseErrorWordPE(testException);
					if (mainError.equals(subError) || subError.equals("<EOF>")) {
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
	 * hier werden fuer den Fall LexicalError die Tokens
	 * auf Validitaet als naechstes Wort geprueft
	 */
	@Override
	public ArrayList<Item> testForTokensLE(final String exception, final String doc,
			final TYPE__SemanticWeb[] tokenMap, final int cursorPosition) {
		final String currentWord = this.getCurrentWord(doc, cursorPosition);
		final String falseWord = this.parseErrorWordLE(exception);
		final int falseWordlength = falseWord.length();

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
			testWordRaw = RIFParserConstants.tokenImage[i];
			//hashmapKey und testWord werden gesetzt
			if (testWordRaw.startsWith("\"")
					&& (!(tokenMap[i].toString().equals("WHITESPACE")))) {
				hashmapKey = null;
				testWord = testWordRaw.substring(1, testWordRaw.length() - 1);
				if (testWord.equals("?")) {
					hashmapKey = "?";
					testWord = "?a";
				}
			//Faelle fuer die reale Identifier in hashmap eingesetzt werden muessen
			} else if (this.hashmap.containsKey(testWordRaw)) {
				if (testWordRaw.startsWith("<QNAME")) {
					if (testWordRaw.equals("<QNAME_NS>")) {
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
				} else if (currentWord.endsWith(falseWord)) {
					documentTest = doc.substring(0, cursorPosition
							- currentWord.length())
							+ testWord;
				} else {
					documentTest = doc.substring(0, cursorPosition
							- falseWordlength)
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
	@Override
	public List<Item> findExistingElements(final String hashmapKey,
			final String hashmapValue, final int indexBeforeCurrentWord,
			final LuposDocumentReader r, ILuposParser p) {
		final ArrayList<Item> arrayList = new ArrayList<Item>();
		String description;
		if (hashmapKey.equals("<Q_URIref>")) {
			description = "URI";
		} else if (hashmapKey.startsWith("<STRING_LITERAL")) {
			description = "LITERAL";
		} else if (hashmapKey.equals("<BNODE_LABEL>")) {
			description = "BLANKNODE";
		} else if (hashmapKey.equals("?")) {
			description = "VARIABLE";
		} else if (hashmapKey.equals("<INTEGER_10>")) {
			description = "INTEGER";
		} else if (hashmapKey.equals("<DECIMAL>")) {
			description = "DECIMAL";
		} else if (hashmapKey.equals("<QNAME>")) {
			description = "QUALIFIEDURI";
		} else {
			description = null;
		}
		if (description != null) {
			final String content = r.getText().substring(0,
					indexBeforeCurrentWord);
			p = new RIFParserHelper(p);
			p.setReaderTokenFriendly(r, 0, content.length());
			ILuposToken token;
			String[] splittedToken;
			while ((token = p.getNextToken(content)) != null) {
				splittedToken = token.toString().split(" ");
				if (splittedToken[2].equals(description)
						&& !(arrayList.contains(splittedToken[9]))) {
					arrayList.add(new Item(splittedToken[9], true));
				}
			}
		}
		return arrayList;
	}

	@Override
	public TYPE__SemanticWeb[] getTokenMap() {
		return this.tokenMap;
	}

}
