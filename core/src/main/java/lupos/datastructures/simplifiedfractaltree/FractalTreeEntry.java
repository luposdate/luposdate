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
package lupos.datastructures.simplifiedfractaltree;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This class represents an entry in the <tt>SimplifiedFractalTree</tt>.
 *
 * @author Denis Fäcke
 * @param <K> The type of the key
 * @param <V> The type of the value
 * @see Comparable
 * @see Serializable
 * @see SimplifiedFractalTree
 * @version $Id: $Id
 */
public class FractalTreeEntry<K extends Comparable<K>, V> implements Comparable<FractalTreeEntry<K, V>>, Serializable, Comparator<FractalTreeEntry<K, V>> {
	/**
	 * Serial Version ID.
	 */
	private static final long serialVersionUID = -864811306766897540L;
	public K key;
	public V value;
	public int pointer = -1;
	public boolean flag = false;

	/**
	 * Constructs a new <tt>FractalTreeEntry</tt>.
	 *
	 * @param key A key
	 * @param value A value
	 */
	public FractalTreeEntry(final K key, final V value) {
		this.key = key;
		this.value = value;
		this.pointer = -1;
	}

	/**
	 * Constructs a new <tt>FractalTreeEntry</tt>.
	 *
	 * @param key A key
	 * @param value A value
	 * @param pointer A pointer
	 */
	public FractalTreeEntry(final K key, final V value, final int pointer) {
		this.key = key;
		this.value = value;
		this.pointer = pointer;
	}

	/**
	 * Constructs a new <tt>FractalTreeEntry</tt>.
	 *
	 * @param key A key
	 * @param value A value
	 * @param pointer A pointer
	 * @param flag A flag stating if this entry is deleted or not
	 */
	public FractalTreeEntry(final K key, final V value, final int pointer, final boolean flag) {
		this.key = key;
		this.value = value;
		this.pointer = pointer;
		this.flag = flag;
	}

	/**
	 * Constructs a new <tt>FractalTreeEntry</tt> without a key, value and pointer.
	 */
	public FractalTreeEntry() {
		this.key = null;
		this.value = null;
		this.pointer = -1;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (this.key != null) {
			if (this.pointer != -1) {
				return this.key.toString() + " (" + this.value.toString() + ") -> " + this.pointer;
			} else {
				return this.key.toString() + " (" + this.value.toString() + ")";
			}
		} else {
			return null + " (" + null + ")";
		}
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final FractalTreeEntry<K, V> arg0) {
		return this.compare(this, arg0);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object arg0) {
		if (arg0 == null)
			return false;
		return this.compareTo((FractalTreeEntry<K, V>) arg0) == 0;
	}

	/** {@inheritDoc} */
	@Override
	public int compare(final FractalTreeEntry<K, V> o1, final FractalTreeEntry<K, V> o2) {
		if (o1.key.compareTo(o2.key) == -1) {
			return -1;
		} else if (o1.key.compareTo(o2.key) == 1) {
			return 1;
		} else if (o1.key.compareTo(o2.key) == 0) {
			return 0;
		}
		return 0;
	}
}
