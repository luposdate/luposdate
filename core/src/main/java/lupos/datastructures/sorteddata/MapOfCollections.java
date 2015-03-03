
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
package lupos.datastructures.sorteddata;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
public interface MapOfCollections<K, V, CV extends Collection<V>> extends Map<K, CV>{
	
	/**
	 * <p>putToCollection.</p>
	 *
	 * @param key a K object.
	 * @param value a V object.
	 * @throws java.lang.InstantiationException if any.
	 * @throws java.lang.IllegalAccessException if any.
	 */
	public void putToCollection(K key, V value) throws InstantiationException, IllegalAccessException;
	/**
	 * <p>removeFromCollection.</p>
	 *
	 * @param key a K object.
	 * @param value a V object.
	 * @return a boolean.
	 */
	public boolean removeFromCollection(K key, V value);
	/**
	 * <p>valuesInCollectionsIterator.</p>
	 *
	 * @return a {@link java.util.Iterator} object.
	 */
	public Iterator<V> valuesInCollectionsIterator();
	/**
	 * <p>valuesInCollections.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<V> valuesInCollections();
	/**
	 * <p>containsValueInCollections.</p>
	 *
	 * @param arg0 a {@link java.lang.Object} object.
	 * @return a boolean.
	 */
	public boolean containsValueInCollections(Object arg0);
	/**
	 * <p>putAllIntoCollections.</p>
	 *
	 * @param arg0 a {@link java.util.Map} object.
	 */
	public void putAllIntoCollections(Map<? extends K, ? extends CV> arg0);
	/**
	 * <p>sizeOfElementsInCollections.</p>
	 *
	 * @return a int.
	 */
	public int sizeOfElementsInCollections();
}
