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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import lupos.autocomplete.misc.Item;
import lupos.autocomplete.strategies.Strategy;
import lupos.gui.anotherSyntaxHighlighting.ILuposParser;
import lupos.gui.anotherSyntaxHighlighting.ILuposToken;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__SemanticWeb;
import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.misc.Tuple;
public abstract class ParserIdentificationStrategy extends Strategy {

	protected TYPE__SemanticWeb[] tokenMap;
	/**
	 * <p>Getter for the field <code>tokenMap</code>.</p>
	 *
	 * @return an array of {@link lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__SemanticWeb} objects.
	 */
	protected abstract TYPE__SemanticWeb[] getTokenMap();

	protected HashMap<String, String> hashmap = new HashMap<String,String>();
	protected LuposDocumentReader readerLocal;
	protected ILuposParser parserLocal;
	protected LuposDocument document;

	/**
	 * <p>Constructor for ParserIdentificationStrategy.</p>
	 *
	 * @param r a {@link lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader} object.
	 * @param p a {@link lupos.gui.anotherSyntaxHighlighting.ILuposParser} object.
	 */
	public ParserIdentificationStrategy(final LuposDocumentReader r, final ILuposParser p){
		this.readerLocal = r;
		this.parserLocal = p;
		this.fillMap();
	}

	/*
	 * fuellt die hashmap mit den Beispielen die ausprobiert werden
	 */
	/**
	 * <p>fillMap.</p>
	 */
	protected abstract void fillMap();
	/*
	 * hier wird ermittelt welche Art des Fehlers vorliegt und
	 * die Fehlermeldung zurueckgegeben
	 */
	/**
	 * <p>handleException.</p>
	 *
	 * @param document a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public abstract String handleException(String document);
	/**
	 * <p>testforTokensNE.</p>
	 *
	 * @param doc a {@link java.lang.String} object.
	 * @param tokenMap an array of {@link lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__SemanticWeb} objects.
	 * @param cursorPosition a int.
	 * @return a {@link java.util.ArrayList} object.
	 */
	public abstract ArrayList<Item> testforTokensNE(String doc, TYPE__SemanticWeb[] tokenMap, int cursorPosition);
	/**
	 * <p>testforTokensPE.</p>
	 *
	 * @param exception a {@link java.lang.String} object.
	 * @param doc a {@link java.lang.String} object.
	 * @param tokenMap an array of {@link lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__SemanticWeb} objects.
	 * @param cursorPosition a int.
	 * @return a {@link java.util.ArrayList} object.
	 */
	public abstract ArrayList<Item> testforTokensPE(String exception, String doc, TYPE__SemanticWeb[] tokenMap, int cursorPosition);
	/**
	 * <p>testForTokensLE.</p>
	 *
	 * @param exception a {@link java.lang.String} object.
	 * @param doc a {@link java.lang.String} object.
	 * @param tokenMap an array of {@link lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__SemanticWeb} objects.
	 * @param cursorPosition a int.
	 * @return a {@link java.util.ArrayList} object.
	 */
	public abstract ArrayList<Item> testForTokensLE(String exception, String doc, TYPE__SemanticWeb[] tokenMap, int cursorPosition);
	/**
	 * <p>findExistingElements.</p>
	 *
	 * @param hashmapKey a {@link java.lang.String} object.
	 * @param hashmapValue a {@link java.lang.String} object.
	 * @param indexBeforeCurrentWord a int.
	 * @param r a {@link lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader} object.
	 * @param p a {@link lupos.gui.anotherSyntaxHighlighting.ILuposParser} object.
	 * @return a {@link java.util.List} object.
	 */
	public abstract List<Item> findExistingElements(String hashmapKey, String hashmapValue , int indexBeforeCurrentWord, LuposDocumentReader r, ILuposParser p);


	/*
	 * durchsucht das Dokument nach validen Bezeichnern
	 */
	/**
	 * <p>setExistingIdentifier.</p>
	 *
	 * @param hashmapKey a {@link java.lang.String} object.
	 * @param description a {@link java.lang.String} object.
	 * @param r a {@link lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader} object.
	 * @param p a {@link lupos.gui.anotherSyntaxHighlighting.ILuposParser} object.
	 */
	public void setExistingIdentifier(final String hashmapKey, final String description, final LuposDocumentReader r, final ILuposParser p){
		final String content = r.getText();
		p.setReaderTokenFriendly(r, 0, content.length());
		ILuposToken token;
		String[] splittedToken;
		boolean exists = false;
		while ((token = p.getNextToken(content))!= null) {
			splittedToken = token.toString().split(" ");
			if (splittedToken[2].equals(description)) {
				this.hashmap.put(hashmapKey, splittedToken[9]);
				exists = true;
				break;
			}
		}
		if (exists==false) {
			this.hashmap.remove(hashmapKey);
		}
	}
	/*
	 * extrahiert aus einer gegebenen Exception- oder Fehlernachricht
	 * zeile und spalte der falschen Zeichensequenz
	 */
	/**
	 * <p>parseLineAndColumn.</p>
	 *
	 * @param msg a {@link java.lang.String} object.
	 * @return a {@link lupos.misc.Tuple} object.
	 */
	public Tuple<Integer, Integer> parseLineAndColumn(final String msg){

			final int indexOfLineNumber = msg.indexOf("line") + 5;
			final int indexOfColumn = msg.indexOf("column");
			final int indexOfColumnNumber = indexOfColumn + 7;
			int line = 0;
			int column = 0;
			if (msg.startsWith("PE")) {
				//WINDOWS(\r) != LINUX(\n)
				if (msg.contains("Undefined")) {
					line = Integer.parseInt(msg.substring(indexOfLineNumber, indexOfColumn-3));
					column = Integer.parseInt(msg.substring(indexOfColumnNumber));
				}else {
					line = Integer.parseInt(msg.substring(indexOfLineNumber, indexOfColumn-2));
					if (System.getProperty("os.name").equals("Linux")) {
						column = Integer.parseInt(msg.substring(indexOfColumnNumber, msg.indexOf(".\n")));
					} else {
						column = Integer.parseInt(msg.substring(indexOfColumnNumber, msg.indexOf(".\r")));
					}
				}
			} else if (msg.startsWith("LE")) {
				line = Integer.parseInt(msg.substring(indexOfLineNumber, indexOfColumn-2));
				column = Integer.parseInt(msg.substring(indexOfColumnNumber, msg.indexOf(".  E")));
			}
			return new Tuple<Integer, Integer>(line, column);
	}

	/*
	 * extrahiert die fehlerhafte Zeichensequenz bei einem lexikalischen Fehler
	 */
	/**
	 * <p>parseErrorWordLE.</p>
	 *
	 * @param exception a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String parseErrorWordLE(final String exception){
		if (exception.contains("<EOF>")) {
			return "<EOF>";
		}
		final String searchelements = "after : \"";
		final int startIndexOfError = exception.indexOf(searchelements) + searchelements.length();
		final int endIndexOfError = exception.length()-1;
		String error;
		if (exception.charAt(startIndexOfError)=='\"') {
			error = "";
		} else {
			error = exception.substring((startIndexOfError),(endIndexOfError));
		}
		return error;
	}

	/*
	 * extrahiert die fehlerhafte Zeichensequenz bei einer Parse Exception
	 */
	/**
	 * <p>parseErrorWordPE.</p>
	 *
	 * @param exception a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String parseErrorWordPE(final String exception) {
		final String searchelements = "Encountered \"";
		int startIndexOfError = exception.indexOf(searchelements)
				+ searchelements.length();
		int endIndexOfError;
		String error;
		if (exception.charAt(startIndexOfError) == ' ') {
			if (exception.charAt(startIndexOfError + 1) == '<') {
				startIndexOfError = exception.indexOf("\"", startIndexOfError) + 1;
				endIndexOfError = exception.indexOf("\"", startIndexOfError);
				error = exception.substring(startIndexOfError, endIndexOfError);
			} else if (exception.charAt(startIndexOfError + 1) == '"') {
				startIndexOfError += 2;
				endIndexOfError = exception.indexOf("\"", startIndexOfError);
				error = exception.substring(startIndexOfError, endIndexOfError);
			} else {
				error = null;
			}
		} else {
			endIndexOfError = exception.indexOf('\"', startIndexOfError);
			error = exception.substring(startIndexOfError, endIndexOfError);
		}
		
		if (error != null) {
			error = error.replace(" ", "");
		}
		 return error;
	}

	/*
	 * erstellt die Vorschlagsliste
	 *
	 */
	/** {@inheritDoc} */
	@Override
	public List<Entry<Item, Integer>> createAutoCompletionList(
			final String textDocument, final int cursorPosition) {
		final String currentWord = this.getCurrentWord(textDocument, cursorPosition);
		final int cwLength = currentWord.length();
		final List<Item> returnList = new ArrayList<Item>();

		ArrayList<Item> possibilitiesList = null;
		Item element_i;
		//prueft das aktuelle Dokument bis zur (Text)cursorPosition auf Fehler
		final String exception = this.handleException(textDocument.substring(0, cursorPosition));

		if (exception.substring(0,2).equals("PE")) {
			possibilitiesList = this.testforTokensPE(exception, textDocument, this.getTokenMap(),cursorPosition);
		}else if (exception.substring(0,2).equals("LE")) {
			possibilitiesList = this.testForTokensLE(exception, textDocument, this.getTokenMap(), cursorPosition);
		}	else {
			possibilitiesList = this.testforTokensNE(textDocument, this.getTokenMap(), cursorPosition);
		}
		if (possibilitiesList!=null) {
			//hier werden unpassende Vorschlaege aussortiert
			for (int i = 0; i < possibilitiesList.size(); i++) {
				element_i = possibilitiesList.get(i);
				if (currentWord.equals("")||currentWord.equals(" ")) {
					returnList.add(element_i);
				} else if (cwLength <= element_i.getValue().length()) {

					if (element_i.getCaseSensitiv()) {
						if (currentWord.substring(0, cwLength).equals(
								element_i.getValue().substring(0, cwLength))) {
							returnList.add(element_i);
						}
						//Wenn nicht case Sensitiv equalsIgnoreCase-Methode statt equals
					} else {
						if (currentWord.substring(0, cwLength).equalsIgnoreCase(
								element_i.getValue().substring(0, cwLength))) {
							returnList.add(element_i);

					}
					}
				}

			}
		}
		return this.generateWeight(returnList);
	}

	/*
	 * generiert die Gewichte, alle vorgeschlagenen Elemente haben Gewicht 1
	 */
	/** {@inheritDoc} */
	@Override
	public List<Entry<Item, Integer>> generateWeight(final List<Item> list){
		final HashMap<Item, Integer> map = new HashMap<Item, Integer>();
		final int basis = 1;
		for (final Item element : list) {
			map.put(element, basis);
		}
		final List<Entry<Item, Integer>> weightList = new ArrayList<Entry<Item,Integer>>(map.entrySet());
		return weightList;
	}
}
