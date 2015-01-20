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
package lupos.datastructures.queryresult;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.engine.operators.BasicOperator;
import lupos.misc.debug.DebugStep;
import lupos.misc.util.ImmutableIterator;

public class QueryResultDebug extends QueryResult {

	protected final QueryResult opp;
	protected final DebugStep debugstep;
	protected final BasicOperator from;
	protected final BasicOperator to;
	protected final boolean process;

	public QueryResultDebug(final QueryResult opp, final DebugStep debugstep,
			final BasicOperator from, final BasicOperator to, final boolean process) {
		this.opp = opp;
		this.debugstep = debugstep;
		this.from = from;
		this.to = to;
		this.process = process;
	}

	@Override
	public void reset() {
		if (this.opp != null) {
			this.opp.reset();
		}
	}

	@Override
	public Collection<Bindings> getCollection() {
		return this.opp.getCollection();
	}

	@Override
	public boolean contains(final Bindings b) {
		return this.opp.contains(b);
	}

	@Override
	public boolean add(final Bindings b) {
		return this.opp.add(b);
	}

	@Override
	public boolean add(final QueryResult qr) {
		return this.opp.add(qr);
	}

	@Override
	public boolean containsAll(final QueryResult qr) {
		return this.opp.containsAll(qr);
	}

	@Override
	public boolean containsAllExceptAnonymousLiterals(final QueryResult qr) {
		return this.opp.containsAllExceptAnonymousLiterals(qr);
	}

	@Override
	public boolean remove(final Bindings b) {
		return this.opp.remove(b);
	}

	@Override
	public boolean removeAll(final QueryResult res) {
		return this.opp.removeAll(res);
	}

	@Override
	public boolean addFirst(final Bindings b) {
		return this.opp.addFirst(b);
	}

	@Override
	public boolean addLast(final Bindings b) {
		return this.opp.addLast(b);
	}

	@Override
	public boolean add(final int pos, final Bindings b) {
		return this.opp.add(pos, b);
	}

	@Override
	public Bindings getFirst() {
		return this.opp.getFirst();
	}

	@Override
	public Bindings getLast() {
		return this.opp.getLast();
	}

	@Override
	public Bindings get(final int pos) {
		return this.opp.get(pos);
	}

	@Override
	public QueryResult clone() {
		final QueryResult ret = new QueryResultDebug(this.opp, this.debugstep, this.from, this.to,
				this.process);
		return ret;
	}

	@Override
	public int oneTimeSize() {
		int size = 0;
		final Iterator<Bindings> itb = this.oneTimeIterator();
		if (itb != null) {
			while (itb.hasNext()) {
				size++;
				itb.next();
			}
			if (itb instanceof ParallelIterator) {
				((ParallelIterator) itb).close();
			}
		}
		return size;
	}

	@Override
	public int size() {
		return this.opp.size();
	}

	@Override
	public boolean isEmpty() {
		return this.opp.isEmpty();
	}

	@Override
	public boolean addAll(final QueryResult res) {
		return this.opp.addAll(res);
	}

	@Override
	public String toString() {
		return this.opp.toString();
	}

	@Override
	public boolean equals(final Object o) {
		return this.opp.equals(o);
	}

	@Override
	public boolean sameOrder(final QueryResult qr) {
		return this.opp.sameOrder(qr);
	}

	@Override
	public boolean sameOrderExceptAnonymousLiterals(final QueryResult qr) {
		return this.opp.sameOrderExceptAnonymousLiterals(qr);
	}

	@Override
	public Collection<Collection<Triple>> getTriples(
			final LinkedList<Collection<Triple>> lct) {
		return this.opp.getTriples(lct);
	}

	@Override
	public void release() {
		this.opp.release();
	}

	@Override
	public void materialize() {
		this.opp.materialize();
	}

	@Override
	public Iterator<Bindings> oneTimeIterator() {
		return this.generateDebugIterator(this.opp.oneTimeIterator());
	}

	@Override
	public Iterator<Bindings> iterator() {
		return this.generateDebugIterator(this.opp.iterator());
	}

	private void step(final BasicOperator from, final BasicOperator to,
			final Bindings bindings) {
		if (this.process) {
			this.debugstep.step(from, to, bindings);
		} else {
			this.debugstep.stepDelete(from, to, bindings);
		}
	}

	private Iterator<Bindings> generateDebugIterator(
			final Iterator<Bindings> itb) {
		if (itb instanceof SIPParallelIterator) {
			return new SIPParallelIterator<Bindings, Bindings>() {

				@Override
				public Bindings next(final Bindings k) {
					final Bindings next = ((SIPParallelIterator<Bindings, Bindings>) itb)
							.next(k);
					if (next != null) {
						QueryResultDebug.this.step(QueryResultDebug.this.from, QueryResultDebug.this.to, next);
					}
					return next;
				}

				@Override
				public void close() {
					((SIPParallelIterator<Bindings, Bindings>) itb).close();

				}

				@Override
				public boolean hasNext() {
					return itb.hasNext();
				}

				@Override
				public Bindings next() {
					final Bindings next = itb.next();
					if (next != null) {
						QueryResultDebug.this.step(QueryResultDebug.this.from, QueryResultDebug.this.to, next);
					}
					return next;
				}

				@Override
				public void remove() {
					itb.remove();
				}
			};
		} else if (itb instanceof ParallelIterator) {
			return new ParallelIterator<Bindings>() {

				@Override
				public void close() {
					((ParallelIterator<Bindings>) itb).close();

				}

				@Override
				public boolean hasNext() {
					return itb.hasNext();
				}

				@Override
				public Bindings next() {
					final Bindings next = itb.next();
					if (next != null) {
						QueryResultDebug.this.step(QueryResultDebug.this.from, QueryResultDebug.this.to, next);
					}
					return next;
				}

				@Override
				public void remove() {
					itb.remove();
				}
			};
		} else {
			return new ImmutableIterator<Bindings>() {

				@Override
				public boolean hasNext() {
					return itb.hasNext();
				}

				@Override
				public Bindings next() {
					final Bindings next = itb.next();
					if (next != null) {
						QueryResultDebug.this.step(QueryResultDebug.this.from, QueryResultDebug.this.to, next);
					}
					return next;
				}
			};
		}
	}

	public QueryResult getOriginalQueryResult() {
		return this.opp;
	}
}
