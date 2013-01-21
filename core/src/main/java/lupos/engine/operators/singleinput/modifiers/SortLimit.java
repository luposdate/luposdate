/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.engine.operators.singleinput.modifiers;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorBindings;

public class SortLimit extends SingleInputOperator {

	private ComparatorBindings comparator;
	private Bindings[] smallestBindings;
	private Bindings max = null;
	private int posMax = -1;
	private int pos = 0;

	public SortLimit(final ComparatorBindings comparator, final int limit) {
		setComparator(comparator);
		setLimit(limit);
	}

	public SortLimit() {
		comparator=null;
		smallestBindings=null;
	}

	
	public void setComparator(final ComparatorBindings comp){
		this.comparator=comp;
	}
	
	public void setLimit(final int limit){
		smallestBindings = new Bindings[limit];
	}

	public QueryResult process(final QueryResult bindings, final int operandID) {
		final Iterator<Bindings> itb = bindings.oneTimeIterator();
		while (pos < smallestBindings.length && itb.hasNext()) {
			final Bindings b = itb.next();
			if (max == null || comparator.compare(b, max) > 0) {
				posMax = pos;
				max = b;
			}
			smallestBindings[pos++] = b;
		}
		if (itb.hasNext()) {
			while (itb.hasNext()) {
				final Bindings b = itb.next();
				if (comparator.compare(b, max) < 0) {
					smallestBindings[posMax] = b;
					max = b;
					// find new maximum
					for (int i = 0; i < smallestBindings.length; i++) {
						if (comparator.compare(smallestBindings[i], max) > 0) {
							max = smallestBindings[i];
							posMax = i;
						}
					}
				}
			}
		}
		if (itb instanceof ParallelIterator)
			((ParallelIterator) itb).close();
		return QueryResult.createInstance(new Iterator<Bindings>() {
			int i = 0;

			public boolean hasNext() {
				return i < pos;
			}

			public Bindings next() {
				if (hasNext())
					return smallestBindings[i++];
				else
					return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		});
	}

	public String toString() {
		return super.toString()+" " + smallestBindings.length;
	}
}
