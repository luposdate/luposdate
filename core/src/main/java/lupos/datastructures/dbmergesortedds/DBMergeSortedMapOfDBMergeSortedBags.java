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
package lupos.datastructures.dbmergesortedds;

import java.io.Serializable;
import java.util.Comparator;

public class DBMergeSortedMapOfDBMergeSortedBags<K extends Serializable,V extends Serializable> extends DBMergeSortedMapOfCollections<K,V, DBMergeSortedBag<V>> {
	
	protected final SortConfiguration sortConfiguration;
	
	/**
	 * Create a new DBMergeSortedMapOfDBMergeSortedBags that sorts according to the elements' natural order. Both the map's and each bag's heap will have the same height. 
	 * @param heapHeight The height of the heap used to presort the elements in memory. (The maximum number of elements that are held in memory at any given time will be 2**heapHeight-1)
	 */
	public DBMergeSortedMapOfDBMergeSortedBags(final SortConfiguration sortConfiguration, final Class<? extends MapEntry<K,V>> classOfElements) {
		super(null, sortConfiguration,classOfElements);
		this.sortConfiguration = sortConfiguration;
	}
	
	/**
	 * Create a new DBMergeSortedMap that sorts using the specified Comparator. Both the map's and each bag's heap will have the same height.
	 * @param heapHeight The height of the heap used to presort the elements in memory. (The maximum number of elements that are held in memory at any given time will be 2**heapHeight-1)
	 * @param comp The Comparator to use for sorting.
	 */
	public DBMergeSortedMapOfDBMergeSortedBags(final SortConfiguration sortConfiguration, final Comparator<? super K> comp, final Class<? extends MapEntry<K,V>> classOfElements) {
		super(null, sortConfiguration, comp, classOfElements);
		this.sortConfiguration = sortConfiguration;
	}
	
	/**
	 * Create a new DBMergeSortedMapOfCollections that sorts according to the elements' natural order.
	 * @param heapHeightForMap The height of the heap used by the map to presort the elements in memory. (The maximum number of elements that are held in memory at any given time will be 2**heapHeight-1)
	 * @param heapHeightForBag The height of the heap used by each bag.
	 */
	public DBMergeSortedMapOfDBMergeSortedBags(final SortConfiguration sortConfigurationForMap, final SortConfiguration sortConfigurationForBag, final Class<? extends MapEntry<K,V>> classOfElements) {
		super(null, sortConfigurationForMap,classOfElements);
		this.sortConfiguration = sortConfigurationForBag;
	}
	
	/**
	 * Create a new DBMergeSortedMap that sorts using the specified Comparator.
	 * @param heapHeightForMap The height of the heap used by the map to presort the elements in memory. (The maximum number of elements that are held in memory at any given time will be 2**heapHeight-1)
	 * @param heap The height of the heap used by each bag.
	 * @param comp The Comparator to use for sorting.
	 */
	public DBMergeSortedMapOfDBMergeSortedBags(final SortConfiguration sortConfigurationForMap, final SortConfiguration sortConfigurationForBag, final Comparator<? super K> comp, final Class<? extends MapEntry<K,V>> classOfElements) {
		super(null, sortConfigurationForMap, comp,classOfElements);
		this.sortConfiguration = sortConfigurationForBag;
	}
	
	protected DBMergeSortedBag<V> createCollection(final Class<? extends V> classOfElements) {
		return new DBMergeSortedBag<V>(this.sortConfiguration, classOfElements);
	}
}
