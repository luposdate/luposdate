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
package lupos.autocomplete.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lupos.autocomplete.misc.Item;
import lupos.misc.Tuple;

public abstract class Strategy {

	public List<String> reservedWords = new ArrayList<String>();

	public abstract List<Entry<Item, Integer>> createAutoCompletionList(String textDocument, int cursorPosition);
	public abstract List<Entry<Item, Integer>> generateWeight(List<Item> list);

	/*
	 * gibt das aktuelle Wort zurueck, dabei werden auch carriage return \n und tab \t
	 * beachtet
	 */
	public static String getCurrentWord(final String textDocument, final int cursorPosition) {
		final String textTillCurserPosition = textDocument.substring(0, cursorPosition);
		if (textTillCurserPosition.length() == 0) {
			return "";
		} else {
			if (textTillCurserPosition.substring(
					(textTillCurserPosition.length() - 1)).equals(" ")) {
				return " ";
			} else if (textTillCurserPosition.substring(
					textTillCurserPosition.length() - 1).equals("\n")
					|| textTillCurserPosition.substring(
							textTillCurserPosition.length() - 1).equals("\t")) {
				return "";

			} else {
				final String[] splittedDocument = textTillCurserPosition
						.split(" |\t|\n");
				if (splittedDocument.length != 0) {
					return splittedDocument[splittedDocument.length - 1];
				} else {
					return " ";
				}
			}
		}
	}

	/*
	 * Rechnet fuer gegebenen Index Spalte und Zeile aus
	 */
	public static Tuple<Integer, Integer> indexToLineAndColumn(String document, final int index) {
		document = document.replaceAll("\r\n", "\n").substring(0, index);
		final String[] temp = document.split("\r|\n");
		final int line = temp.length;
		int columnTemp = 0;
		for (int i = 0; i < temp.length - 1; i++) {
			columnTemp += temp[i].length();
		}
		columnTemp += (line - 1);
		final int column = index - columnTemp;
		return new Tuple<Integer, Integer>(line, column);
	}

	/*
	 * Wandelt gegebene Spalte und Zeile in Index um
	 */
	public int lineAndColumnToIndex(final String document, final int line, int column) {
		final String[] splitArray = document.replaceAll("\r\n", "\n").split("\n|\r", line);
		int counter = 0;
		for (int i = 0; i < splitArray.length - 1; i++) {
			counter += splitArray[i].length();
		}
		// zeilenumbrueche draufaddieren
		counter += line - 1;
		for (int j = 0; j < column; j++) {
			if (splitArray[line - 1].charAt(j) == '\t') {
				column -= 7;
			}
			counter++;
		}
		return counter;
	}
}
