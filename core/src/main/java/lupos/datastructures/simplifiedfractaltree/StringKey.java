/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.datastructures.simplifiedfractaltree;

import java.io.Serializable;
import java.util.Comparator;

public class StringKey implements Comparator<StringKey>, Serializable, Comparable<StringKey> {
	/**
	 *
	 */
	private static final long serialVersionUID = 9657947515041781L;
	public String string = null;
	transient Comparator<StringKey> comp = null;

	public StringKey(final String string){
		this.string = string;
	}

	public StringKey(final String string, final Comparator<StringKey> comp){
		this.string = string;
		this.comp = comp;
	}

	@Override
	public int compare(final StringKey o1, final StringKey o2) {
		if(this.comp == null){
			return o1.string.compareTo(o2.string);
		} else {
			return this.comp.compare(o1, o2);
		}
	}

	@Override
	public int compareTo(final StringKey o) {
		return this.compare(this, o);
	}

	@Override
	public String toString(){
		return this.string;
	}

	public byte[] getBytes(final String utf8) {
		return this.string.getBytes();
	}
}
