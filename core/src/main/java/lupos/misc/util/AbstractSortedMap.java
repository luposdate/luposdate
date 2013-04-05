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
package lupos.misc.util;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.SortedMap;

/**
 * This class provides a skeletal implementation of the <tt>SortedMap</tt>
 * interface, to minimize the effort required to implement this interface.
 * 
 * @param <K> the type of keys maintained by this sorted map
 * @param <V> the type of mapped values
 */
public abstract class AbstractSortedMap<K, V> extends AbstractMap<K, V> implements SortedMap<K, V>{

	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey) {
		return subMap(fromKey, toKey, false);
	}
	
	/**
	 * Returns a view of the portion of this map whose keys range from fromKey, inclusive, to toKey (inclusive if inclusiveLastKey is true, otherwise exclusive). 
	 * (If fromKey and toKey are equal and inclusiveLastKey is false, the returned map is empty.) 
	 * The returned map is backed by this map, so changes in the returned map are reflected in this map, and vice-versa. 
	 * The returned map supports all optional map operations that this map supports. 
	 * The returned map will throw an IllegalArgumentException on an attempt to insert a key outside its range.
	 * 
	 * @param fromKey
	 * @param toKey
	 * @param inclusiveLastKey
	 * @return the backed map
	 */
	public abstract SortedMap<K, V> subMap(K fromKey, K toKey, boolean inclusiveLastKey);

	@Override
	public SortedMap<K, V> headMap(K toKey) {
		return this.subMap(this.firstKey(), toKey);
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey) {
		return this.subMap(fromKey, lastKey(), true);
	}

	@Override
	public K firstKey() {
		Iterator<K> it = this.keySet().iterator();
		if(!it.hasNext()){
			return null;
		}
		return it.next();
	}

	@Override
	public K lastKey() {
		// This implementation is not efficient!
		// Please override this method if
		// determining the last key can be done in a more efficient way than iterating
		// through all keys...
		K lastKey = null;
		Iterator<K> it = this.keySet().iterator();
		while(it.hasNext()){
			lastKey = it.next();
		}
		return lastKey;
	}
}
