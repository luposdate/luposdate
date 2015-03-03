
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
package lupos.autocomplete.strategies.diao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import lupos.autocomplete.misc.Item;
import lupos.autocomplete.strategies.Strategy;
import lupos.gui.anotherSyntaxHighlighting.ILuposParser;
import lupos.gui.anotherSyntaxHighlighting.ILuposToken;
import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
public abstract class DocumentInputAndAlphabeticOrderStrategy extends Strategy {

	protected LuposDocumentReader readerLocal;
	protected ILuposParser parserLocal;
	protected LuposDocument document;

	/**
	 * <p>Constructor for DocumentInputAndAlphabeticOrderStrategy.</p>
	 *
	 * @param r a {@link lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader} object.
	 * @param p a {@link lupos.gui.anotherSyntaxHighlighting.ILuposParser} object.
	 */
	public DocumentInputAndAlphabeticOrderStrategy(final LuposDocumentReader r, final ILuposParser p){
		this.readerLocal = r;
		this.parserLocal = p;
		this.initReservedWords();
	}

	/*
	 * reservedWords sind urspruenglich durch Anfuehrungszeichen eingeschlossen,
	 * diese werden natuerlich entfernt
	 */
	/**
	 * <p>initReservedWords.</p>
	 */
	public abstract void initReservedWords();

	/*
	 * findet die gesuchten Tokens mit Beschreibung:
	 * QUALIFIEDURI, VARIABLE, PREFIXLABEL, URI
	 */
	/**
	 * <p>tokensToList.</p>
	 *
	 * @param r a {@link lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader} object.
	 * @param p a {@link lupos.gui.anotherSyntaxHighlighting.ILuposParser} object.
	 * @param indexBeforeCurrentWord a int.
	 * @return a {@link java.util.List} object.
	 */
	public List<Item> tokensToList(final LuposDocumentReader r, final ILuposParser p, final int indexBeforeCurrentWord) {
		final String content = r.getText().substring(0, indexBeforeCurrentWord);
		p.setReaderTokenFriendly(r, 0, content.length());
		ILuposToken token;
		final List<Item> tokensList = new ArrayList<Item>();
		while ((token = p.getNextToken(content)) != null) {
			final String[] splittedTokenLine = token.toString().split(" ");
			if ((splittedTokenLine[2].equals("QUALIFIEDURI") || splittedTokenLine[2]
					.equals("VARIABLE") || splittedTokenLine[2].equals("PREFIXLABEL")|| splittedTokenLine[2].equals("URI"))
					&& !(tokensList.contains(splittedTokenLine[9]))) {
				tokensList.add(new Item(splittedTokenLine[9], true));
			}
		}
		return tokensList;
	}

	/*
	 * erstellt die Vorschlagsliste
	 */
	/** {@inheritDoc} */
	@Override
	public List<Entry<Item, Integer>> createAutoCompletionList(
			final String textDocument, final int cursorPosition) {
		final String currentWord = this.getCurrentWord(textDocument, cursorPosition);
		final int cwLength = currentWord.length();
		final List<Item> returnList = new ArrayList<Item>();

		final int indexBeforeCurrentWord = textDocument.substring(0, cursorPosition - currentWord.length()).length();
		final List<Item> tokensList = this.tokensToList(this.readerLocal, this.parserLocal, indexBeforeCurrentWord);

		for (int i = 0; i < tokensList.size(); i++) {
			final Item tl_element_i = tokensList.get(i);
			/*
			 * Falls currenWord leer ist bzw aus einem Leerzeichen besteht
			 * werden alle Elemente von tokensList hinzugefuegt
			 */
			if (currentWord.equals("") || currentWord.equals(" ")) {
				returnList.add(tl_element_i);
				/*
				 * die Laenge von currentWord muss <= aller Laengen der Elemente
				 * von tokensList sein
				 */
			} else if ((cwLength) <= tl_element_i.getValue().length()) {
				/*
				 * Vergleicht i-tes Element von tokensList mit currentWord, auch
				 * komplett gross bzw klein geschrieben
				 */
				if (currentWord.substring(0, cwLength).equals(
						tl_element_i.getValue().substring(0, cwLength))) {
					returnList.add(tl_element_i);
				}
			}
		}
		for (int i = 0; i < this.reservedWords.size(); i++) {
			final Item rw_element_i = new Item(this.reservedWords.get(i).toString(), false);
			/*
			 * Falls currenWord leer ist bzw aus einem Leerzeichen besteht
			 * werden alle reservedWords hinzugefuegt
			 */
			if (currentWord.equals("") || currentWord.equals(" ")) {
				returnList.add(rw_element_i);
				/*
				 * die Laenge von currentWord muss <= aller Laengen der
				 * reservedWords sein
				 */
			} else if ((cwLength) <= rw_element_i.getValue().length()) {
				/*
				 * Vergleicht i-tes reservedWord mit currentWord, auch komplett
				 * gross bzw klein geschrieben
				 */
				if (currentWord.substring(0, cwLength).equalsIgnoreCase(
						rw_element_i.getValue().substring(0, cwLength))) {
					returnList.add(rw_element_i);
				}
			}
		}

		Collections.sort(returnList, new Comparator<Item>() {

			@Override
			public int compare(final Item arg0, final Item arg1) {
				return arg0.getValue().compareTo(arg1.getValue());
			}
		});
		return this.generateWeight(returnList);
	}

	/*
	 * generiert die Gewichtungen
	 */
	/** {@inheritDoc} */
	@Override
	public List<Entry<Item, Integer>> generateWeight(final List<Item> list) {

		final HashMap<Item, Integer> map = new HashMap<Item, Integer>();
		int basis = list.size();
		for (final Item element : list) {
			map.put(element, basis);
			basis--;
		}
		return new ArrayList<Entry<Item, Integer>>(map.entrySet());
	}
}
