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
package lupos.engine.operators.singleinput.readtriplesdistinct;

import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleComparator;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;

public class DBSetBlockingDistinct extends BlockingDistinct {

	public DBSetBlockingDistinct() throws RemoteException {
		super(new DBMergeSortedSet<BindingsArrayReadTriples>(5,
				new Comparator<BindingsArrayReadTriples>() {

					public int compare(final BindingsArrayReadTriples arg0,
							final BindingsArrayReadTriples arg1) {
						final TripleComparator tc = new TripleComparator(
								RDF3XIndexScan.CollationOrder.SPO);
						final Iterator<Triple> it0 = arg0.getTriples()
								.iterator();
						final Iterator<Triple> it1 = arg1.getTriples()
								.iterator();
						while (it0.hasNext()) {
							if (!it1.hasNext())
								return -1;
							final Triple t0 = it0.next();
							final Triple t1 = it1.next();
							final int compare = tc.compare(t0, t1);
							if (compare != 0)
								return compare;
						}
						if (it1.hasNext())
							return 1;
						return 0;
					}

				}, BindingsArrayReadTriples.class));
	}

	@Override
	protected ParallelIterator<Bindings> getIterator() {
		final Iterator<BindingsArrayReadTriples> itb = this.bindings.iterator();
		return new ParallelIterator<Bindings>() {

			public void close() {
				((DBMergeSortedSet) bindings).release();
			}

			public boolean hasNext() {
				return itb.hasNext();
			}

			public Bindings next() {
				return itb.next();
			}

			public void remove() {
				itb.remove();
			}

			@Override
			public void finalize() {
				close();
			}
		};
	}

	@Override
	public String toString() {
		return super.toString()+" for read triples";
	}

}