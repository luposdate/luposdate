
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

	/**
	 * <p>Constructor for QueryResultDebug.</p>
	 *
	 * @param opp a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param debugstep a {@link lupos.misc.debug.DebugStep} object.
	 * @param from a {@link lupos.engine.operators.BasicOperator} object.
	 * @param to a {@link lupos.engine.operators.BasicOperator} object.
	 * @param process a boolean.
	 */
	public QueryResultDebug(final QueryResult opp, final DebugStep debugstep,
			final BasicOperator from, final BasicOperator to, final boolean process) {
		this.opp = opp;
		this.debugstep = debugstep;
		this.from = from;
		this.to = to;
		this.process = process;
	}

	/** {@inheritDoc} */
	@Override
	public void reset() {
		if (this.opp != null) {
			this.opp.reset();
		}
	}

	/** {@inheritDoc} */
	@Override
	public Collection<Bindings> getCollection() {
		return this.opp.getCollection();
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final Bindings b) {
		return this.opp.contains(b);
	}

	/** {@inheritDoc} */
	@Override
	public boolean add(final Bindings b) {
		return this.opp.add(b);
	}

	/** {@inheritDoc} */
	@Override
	public boolean add(final QueryResult qr) {
		return this.opp.add(qr);
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsAll(final QueryResult qr) {
		return this.opp.containsAll(qr);
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsAllExceptAnonymousLiterals(final QueryResult qr) {
		return this.opp.containsAllExceptAnonymousLiterals(qr);
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final Bindings b) {
		return this.opp.remove(b);
	}

	/** {@inheritDoc} */
	@Override
	public boolean removeAll(final QueryResult res) {
		return this.opp.removeAll(res);
	}

	/** {@inheritDoc} */
	@Override
	public boolean addFirst(final Bindings b) {
		return this.opp.addFirst(b);
	}

	/** {@inheritDoc} */
	@Override
	public boolean addLast(final Bindings b) {
		return this.opp.addLast(b);
	}

	/** {@inheritDoc} */
	@Override
	public boolean add(final int pos, final Bindings b) {
		return this.opp.add(pos, b);
	}

	/** {@inheritDoc} */
	@Override
	public Bindings getFirst() {
		return this.opp.getFirst();
	}

	/** {@inheritDoc} */
	@Override
	public Bindings getLast() {
		return this.opp.getLast();
	}

	/** {@inheritDoc} */
	@Override
	public Bindings get(final int pos) {
		return this.opp.get(pos);
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult clone() {
		final QueryResult ret = new QueryResultDebug(this.opp, this.debugstep, this.from, this.to,
				this.process);
		return ret;
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public int size() {
		return this.opp.size();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return this.opp.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAll(final QueryResult res) {
		return this.opp.addAll(res);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.opp.toString();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		return this.opp.equals(o);
	}

	/** {@inheritDoc} */
	@Override
	public boolean sameOrder(final QueryResult qr) {
		return this.opp.sameOrder(qr);
	}

	/** {@inheritDoc} */
	@Override
	public boolean sameOrderExceptAnonymousLiterals(final QueryResult qr) {
		return this.opp.sameOrderExceptAnonymousLiterals(qr);
	}

	/** {@inheritDoc} */
	@Override
	public Collection<Collection<Triple>> getTriples(
			final LinkedList<Collection<Triple>> lct) {
		return this.opp.getTriples(lct);
	}

	/** {@inheritDoc} */
	@Override
	public void release() {
		this.opp.release();
	}

	/** {@inheritDoc} */
	@Override
	public void materialize() {
		this.opp.materialize();
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<Bindings> oneTimeIterator() {
		return this.generateDebugIterator(this.opp.oneTimeIterator());
	}

	/** {@inheritDoc} */
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

	/**
	 * <p>getOriginalQueryResult.</p>
	 *
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public QueryResult getOriginalQueryResult() {
		return this.opp;
	}
}
