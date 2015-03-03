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

/**
 * Experimental: Calculates the frequency of each substring contained in the
 * FeedMessage's description. Currently not executed in main method, can be used
 * for further improving the database results' quality.
 */
public class Frequency {
	private final ArrayList<String> substring = new ArrayList<String>();
	private final ArrayList<Integer> frequency = new ArrayList<Integer>();

	/**
	 * increments the substring's frequency value at (index) in ArrayList
	 * frequency
	 *
	 * @param index
	 */
	public void incFrequency(final int index) {
		this.frequency.set(index, this.frequency.get(index) + 1);
	}

	/**
	 * @return ArrayList frequency
	 */
	public ArrayList<Integer> getFrequency() {
		return this.frequency;
	}

	/**
	 *
	 * @param i
	 * @return Substring at position i in ArrayList substring
	 */
	public String getSubstringAt(final int i) {
		return this.substring.get(i);
	}

	/**
	 * @return ArrayList substring
	 */
	public ArrayList<String> getSubstring() {
		return this.substring;
	}

	/**
	 * @return Size of ArrayList substring
	 */
	public int getSubstrLength() {
		return this.substring.size();
	}

	/**
	 * Adds param s to ArrayList substring.
	 *
	 */
	public void addToSubstring(final String s) {
		this.substring.add(s);
	}

	/**
	 * Adds param i to ArrayList frequency.
	 *
	 * @param i
	 */
	public void addToFrequency(final int i) {
		this.frequency.add(i);
	}

	/**
	 * @return true if substring is empty, false if not
	 */
	public boolean isEmpty() {
		if (this.substring.size() == 0) {
			return true;
		} else {
			return false;
		}
	}
}