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
package lupos.autocomplete.strategies.diof;

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

public abstract class DocumentInputAndOrderByFrequencyStrategy extends Strategy {

	protected LuposDocumentReader readerLocal;
	protected ILuposParser parserLocal;
	protected LuposDocument document;

	public DocumentInputAndOrderByFrequencyStrategy(final LuposDocumentReader r, final ILuposParser p){
		this.readerLocal = r;
		this.parserLocal = p;
		this.initReservedWords();
	}

	public abstract void initReservedWords();

	/*
	 * erstellt die Vorschlagsliste
	 *
	 */
	@Override
	public List<Entry<Item, Integer>> createAutoCompletionList(
			final String textDocument, final int cursorPosition) {
		final String currentWord = Strategy.getCurrentWord(textDocument, cursorPosition);
		final int cwLength = currentWord.length();
		final List<Entry<Item, Integer>> returnList = new ArrayList<Entry<Item, Integer>>();

		final int indexBeforeCurrentWord = textDocument.substring(0,
				cursorPosition - currentWord.length()).length();
		//tokens des Dokuments bis zum aktuellen Wort werden in hashmap gespeichert
		final HashMap<Item, Integer> hashmap = this.tokensToMap(this.readerLocal, this.parserLocal,
				indexBeforeCurrentWord);

		Item rw_element_i;
		//restliche reservedWords werden ergaenzt
		for (int i = 0; i < this.reservedWords.size(); i++) {
			rw_element_i = new Item(this.reservedWords.get(i).toString(), false);
			if (!hashmap.containsKey(rw_element_i)) {
				hashmap.put(rw_element_i, 0);
			}
		}
		final List<Entry<Item, Integer>> list = new ArrayList<Entry<Item, Integer>>(
				hashmap.entrySet());

		//Elemente werden verglichen mit dem aktuellen Wort
		for (int i = 0; i < list.size(); i++) {

			final Entry<Item, Integer> element_i = list.get(i);

			if (currentWord.equals("") || currentWord.equals(" ")) {
				returnList.add(element_i);
			}

			else if ((cwLength) <= element_i.getKey().getValue().length()) {
				if (element_i.getKey().getCaseSensitiv()) {
					if (currentWord.substring(0, cwLength).equals(
							element_i.getKey().getValue()
									.substring(0, cwLength))) {
						returnList.add(element_i);
					}
				} else {
					if (currentWord.substring(0, cwLength).equalsIgnoreCase(
							element_i.getKey().getValue()
									.substring(0, cwLength))) {
						returnList.add(element_i);
					}
				}
			}
		}
		return returnList;
	}

	/*
	 * hier wird das aktuelle Dokument bis zu indexBeforeCurrentWord auf geeignete
	 * Tokens ueberprueft. Kommen welche vor, werden sie mit der Zahl ihres Vorkommens
	 * in hashmap geschrieben
	 */
	public HashMap<Item, Integer> tokensToMap(final LuposDocumentReader r,
			final ILuposParser p, final int indexBeforeCurrentWord) {
		final HashMap<Item, Integer> hashmap = new HashMap<Item, Integer>();
		final String content = r.getText().substring(0, indexBeforeCurrentWord);
		p.setReaderTokenFriendly(r, 0, content.length());
		ILuposToken token;
		while ((token = p.getNextToken(content)) != null) {

			final String[] splittedTokenLine = token.toString().split(" ");
			int hashTemp;
			final String semanticWebType = splittedTokenLine[2];

			if ((splittedTokenLine[2].equals("QUALIFIEDURI")
					|| splittedTokenLine[2].equals("VARIABLE")
					|| splittedTokenLine[2].equals("PREFIXLABEL")
					|| splittedTokenLine[2].equals("URI")
					|| splittedTokenLine[2].equals("RESERVEDWORD"))) {

				final String tokenString = splittedTokenLine[9];
				Item item_i;
				if (semanticWebType.equals("RESERVEDWORD")) {
					item_i = new Item(tokenString, false);
				} else {
					item_i = new Item(tokenString, true);
				}

				if (!(hashmap.containsKey(item_i))) {
					hashmap.put(item_i, 1);

				} else {
					hashTemp = hashmap.get(item_i);
					hashmap.put(item_i, hashTemp + 1);
				}

			}

		}
		return hashmap;
	}

	/*
	 * Ueberfuehrt die Token-Elemente in eine nach Haeufigkeit der Benutzung
	 * sortierte Liste
	 *
	 * @return sortierte Liste<Entry<String,Integer>>
	 */
	public List<Entry<String, Integer>> hashMapToSortedList(
			final HashMap<String, Integer> hashMap) {
		final List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>();
		list.addAll(hashMap.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(final Entry<String, Integer> arg0,
					final Entry<String, Integer> arg1) {
				return arg1.getValue().compareTo(arg0.getValue());
			}

		});
		return list;

	}

	//wird hier nicht benötigt
	@Override
	public List<Entry<Item, Integer>> generateWeight(final List<Item> list) {
		return null;

	}
}
