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
package lupos.autocomplete.strategies.aos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import lupos.autocomplete.misc.Item;
import lupos.autocomplete.strategies.Strategy;
public abstract class AlphabeticOrderStrategy extends Strategy {

	protected List<String> reservedWords = new ArrayList<String>();

	/**
	 * <p>Constructor for AlphabeticOrderStrategy.</p>
	 */
	public AlphabeticOrderStrategy(){
		this.initReservedWords();
	}

	/**
	 * <p>initReservedWords.</p>
	 */
	public abstract void initReservedWords();

	/*
	 * erstellt die Vorschlagsliste
	 */
	/** {@inheritDoc} */
	@Override
	public List<Entry<Item, Integer>> createAutoCompletionList(
			final String textDocument, final int cursorPosition) {

		final String currentWord = Strategy.getCurrentWord(textDocument, cursorPosition);
		final int cwLength = currentWord.length();
		final List<Item> returnList = new ArrayList<Item>();

		for (int i = 0; i < this.reservedWords.size(); i++) {
			final String rw_element_i = this.reservedWords.get(i).toString();

			/*
			 * Falls currenWord leer ist bzw aus einem Leerzeichen besteht
			 * werden alle reservedWords hinzugefuegt
			 */
			if (currentWord.equals("") || currentWord.equals(" ")) {
				returnList.add(new Item(rw_element_i, false));
				// System.out.println(rw_element_i);

				/*
				 * die Laenge von currentWord muss <= aller Laengen der
				 * reservedWords sein
				 */
			} else if ((cwLength) <= rw_element_i.length()) {

				/*
				 * Vergleicht i-tes reservedWord mit currentWord, auch komplett
				 * gross bzw klein geschrieben
				 */
				if (currentWord.substring(0, cwLength).equalsIgnoreCase(
						rw_element_i.substring(0, cwLength))) {
					returnList.add(new Item(rw_element_i, false));
				}
			}
		}
		return this.generateWeight(returnList);
	}

	/*
	 * generiert die Gewichte
	 */
	/** {@inheritDoc} */
	@Override
	public List<Entry<Item, Integer>> generateWeight(final List<Item> list) {

		final HashMap<Item, Integer> map = new HashMap<Item, Integer>();
		int basis = list.size();

		for (final Item element : list) {
			basis--;
			map.put(element, basis);
		}
		final List<Entry<Item, Integer>> weightList = new ArrayList<Entry<Item, Integer>>(
				map.entrySet());
		return weightList;
	}
}
