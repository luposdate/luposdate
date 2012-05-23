/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.util.Comparator;
import java.util.Iterator;

import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.trie.SuperTrie;

public class DBMergeSortedSetUsingStringSearch extends DBMergeSortedSet<String> {

	protected SuperTrie searchtree = SuperTrie.createInstance();

	public DBMergeSortedSetUsingStringSearch(final int heapHeight,
			final Class<? extends String> classOfElements){
		super(heapHeight, classOfElements);
	}

	public DBMergeSortedSetUsingStringSearch(){
		super();
	}

	public DBMergeSortedSetUsingStringSearch(
			final Class<? extends String> classOfElements){
		super(classOfElements);
	}

	public DBMergeSortedSetUsingStringSearch(final int heapHeight,
			final Comparator<? super String> comp,
			final Class<? extends String> classOfElements){
		super(heapHeight, comp, classOfElements);
	}

	public DBMergeSortedSetUsingStringSearch(
			final Comparator<? super String> comp,
			final Class<? extends String> classOfElements){
		super(comp, classOfElements);
	}

	@Override
	public boolean add(final String ele) {
		if (searchtree.isFull()) {
			writeToRun();
		}

		if (searchtree.add(ele))
			size++;
		return true;
	}

	private void writeToRun() {
		if (currentRun == null)
			currentRun = Run.createInstance(this);
		else
			closeAndNewCurrentRun();
		for (final String s : searchtree) {
			final Entry<String> entry = new Entry<String>(s,
					new StandardComparator<String>(), n++);
			currentRun.add(entry);
		}
		searchtree = SuperTrie.createInstance();
	}

	@Override
	public void clear() {
		super.clear();
		searchtree.clear();
	}

	public boolean sorted() {
		return searchtree.size()==0
				&& (currentRun == null || unsortedID == currentRun.runID);
	}
		
	@Override
	public void sort() {
		if (sorted() || currentRun == null)
			return;
		if (searchtree.size() > 0)
			this.writeToRun();
		super.sort();
	}

	@Override
	public ParallelIterator<String> iterator() {
		// Do we have a small sorted bag? In other words:
		// Did we already write entries to disk or is all still stored in main
		// memory? In the latter case, we do not need to store it on disk and
		// just "sort" in memory!
		if (currentRun == null) {
			return new ParallelIterator() {
				Iterator<String> it = searchtree.iterator();

				public void close() {
				}

				public boolean hasNext() {
					return it.hasNext();
				}

				public Object next() {
					return it.next();
				}

				public void remove() {
				}
			};
		} else
			return super.iterator();
	}
}
