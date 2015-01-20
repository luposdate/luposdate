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
package lupos.datastructures.parallel;

import java.util.Iterator;

import lupos.datastructures.paged_dbbptree.PrefixSearchMinMax;

/**
 * This class uses a thread to insert key-value-pairs into a PrefixSearchMinMax.
 * For this purpose, the key-value-pairs are put into a bounded buffer.
 * In this way, the key-value pairs are put into the PrefixSearchMinMax in an asynchronous way.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public class PrefixSearchMinMaxWithAdderThread<K, V> extends SortedMapWithAdderThread<K, V> implements PrefixSearchMinMax<K, V>{

	final protected PrefixSearchMinMax<K, V> prefixSearchMinMax;

	public PrefixSearchMinMaxWithAdderThread(final PrefixSearchMinMax<K, V> prefixSearchMinMax, final int size) {
		super(prefixSearchMinMax, size);
		this.prefixSearchMinMax = prefixSearchMinMax;
	}

	@Override
	public Iterator<V> prefixSearch(final K arg0) {
		synchronized(this){
			this.waitForAdderThread();
			return this.prefixSearchMinMax.prefixSearch(arg0);
		}
	}

	@Override
	public Object[] getClosestElements(final K arg0) {
		synchronized(this){
			this.waitForAdderThread();
			return this.prefixSearchMinMax.getClosestElements(arg0);
		}
	}

	@Override
	public Iterator<V> prefixSearch(final K arg0, final K min) {
		synchronized(this){
			this.waitForAdderThread();
			return this.prefixSearchMinMax.prefixSearch(arg0, min);
		}
	}

	@Override
	public Iterator<V> prefixSearch(final K arg0, final K min, final K max) {
		synchronized(this){
			this.waitForAdderThread();
			return this.prefixSearchMinMax.prefixSearch(arg0, min, max);
		}
	}

	@Override
	public Iterator<V> prefixSearchMax(final K arg0, final K max) {
		synchronized(this){
			this.waitForAdderThread();
			return this.prefixSearchMinMax.prefixSearchMax(arg0, max);
		}
	}
}
