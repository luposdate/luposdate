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

public class DBMergeSortedSetUsingStringSearchReplacementSelection extends
		DBMergeSortedSetUsingStringSearch {

	private String nextToRemove = null;

	public DBMergeSortedSetUsingStringSearchReplacementSelection(
			final SortConfiguration sortConfiguration, final Class<? extends String> classOfElements){
		super(sortConfiguration, classOfElements);
	}

	public DBMergeSortedSetUsingStringSearchReplacementSelection(){
		super();
	}

	public DBMergeSortedSetUsingStringSearchReplacementSelection(
			final Class<? extends String> classOfElements){
		super(classOfElements);
	}

	public DBMergeSortedSetUsingStringSearchReplacementSelection(
			final SortConfiguration sortConfiguration, final Comparator<? super String> comp,
			final Class<? extends String> classOfElements){
		super(sortConfiguration, comp, classOfElements);
	}

	public DBMergeSortedSetUsingStringSearchReplacementSelection(
			final Comparator<? super String> comp,
			final Class<? extends String> classOfElements){
		super(comp, classOfElements);
	}

	@Override
	public boolean add(final String ele) {

		if (currentRun != null) {
			final int compare = ele.compareTo(currentRun.max);
			if (compare == 0)
				return false;
			if (compare > 0) {
				if (nextToRemove == null || nextToRemove.compareTo(ele) > 0)
					nextToRemove = ele;
			}
		}

		if (searchtree.add(ele)) {
			size++;
			while (searchtree.isFull()) {
				if (nextToRemove == null) {
					if (currentRun == null)
						currentRun = Run.createInstance(this);
					else {
						closeAndNewCurrentRun();
					}
					final Iterator<String> it = searchtree.iterator();
					final String toRemove = it.next();
					nextToRemove = it.next();
					searchtree.remove(toRemove);
					currentRun.add(new Entry<String>(toRemove,
							new StandardComparator<String>(), n++));
				} else {
					currentRun.add(new Entry<String>(nextToRemove,
							new StandardComparator<String>(), n++));
					nextToRemove = searchtree
							.removeAndGetNextLargerOne(nextToRemove);
				}
			}
		}
		return true;
	}

}
