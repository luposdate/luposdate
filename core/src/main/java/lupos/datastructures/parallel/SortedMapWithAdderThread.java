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
package lupos.datastructures.parallel;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import lupos.misc.Tuple;

/**
 * This class uses a thread to insert key-value-pairs into a sorted map.
 * For this purpose, the key-value-pairs are put into a bounded buffer.
 * In this way, the key-value pairs are put into the sorted map in an asynchronous way.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public class SortedMapWithAdderThread<K, V> implements SortedMap<K,V>{

	final protected BoundedBuffer<Tuple<K,V>> toInsert;
	final protected SortedMap<K,V> sortedMap;

	public SortedMapWithAdderThread(final SortedMap<K,V> sortedMap, final int size){
		this.toInsert = new BoundedBuffer<Tuple<K,V>>(size);
		this.sortedMap = sortedMap;
		final AdderThread<K, V> adderThread = new AdderThread<K, V>(this.sortedMap, this.toInsert);
		adderThread.start();
	}

	protected void waitForAdderThread(){
		try {
			this.toInsert.awaitEmpty();
		} catch (final InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	@Override
	public int size() {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.size();
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.isEmpty();
		}
	}

	@Override
	public boolean containsKey(final Object key) {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.containsKey(key);
		}
	}

	@Override
	public boolean containsValue(final Object value) {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.containsValue(value);
		}
	}

	@Override
	public V get(final Object key) {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.get(key);
		}
	}

	@Override
	public V put(final K key, final V value) {
		synchronized(this){
			try {
				this.toInsert.put(new Tuple<K, V>(key, value));
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public V remove(final Object key) {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.remove(key);
		}
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m) {
		synchronized(this){
			try {
				for(final Entry<? extends K, ? extends V> entry: m.entrySet()){
					this.toInsert.put(new Tuple<K, V>(entry.getKey(), entry.getValue()));
				}
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void clear() {
		synchronized(this){
			this.waitForAdderThread();
			this.sortedMap.clear();
		}
	}

	@Override
	public Comparator<? super K> comparator() {
		return this.sortedMap.comparator();
	}

	@Override
	public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.subMap(fromKey, toKey);
		}
	}

	@Override
	public SortedMap<K, V> headMap(final K toKey) {
		synchronized(this){
			this.waitForAdderThread();
			return this.headMap(toKey);
		}
	}

	@Override
	public SortedMap<K, V> tailMap(final K fromKey) {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.tailMap(fromKey);
		}
	}

	@Override
	public K firstKey() {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.firstKey();
		}
	}

	@Override
	public K lastKey() {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.lastKey();
		}
	}

	@Override
	public Set<K> keySet() {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.keySet();
		}
	}

	@Override
	public Collection<V> values() {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.values();
		}
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		synchronized(this){
			this.waitForAdderThread();
			return this.sortedMap.entrySet();
		}
	}

	/**
	 * releases all resources and stops the adder thread...
	 */
	public void release(){
		this.toInsert.endOfData();
	}

	public static class AdderThread<K, V> extends Thread {

		final protected BoundedBuffer<Tuple<K,V>> toInsert;
		final protected SortedMap<K,V> sortedMap;

		public AdderThread(final SortedMap<K,V> sortedMap, final BoundedBuffer<Tuple<K,V>> toInsert){
			this.toInsert = toInsert;
			this.sortedMap = sortedMap;
		}

		@Override
		public void run(){
			try {
				while(this.toInsert.hasNext()){
					final Tuple<K, V> tuple = this.toInsert.get();
					this.sortedMap.put(tuple.getFirst(), tuple.getSecond());
				}
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}
}
