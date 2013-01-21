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
package lupos.datastructures.queryresult;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.misc.debug.DebugStep;

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

	public void reset() {
		if (opp != null)
			opp.reset();
	}

	public Collection<Bindings> getCollection() {
		return opp.getCollection();
	}

	public boolean contains(final Bindings b) {
		return opp.contains(b);
	}

	public boolean add(final Bindings b) {
		return opp.add(b);
	}

	public boolean add(final QueryResult qr) {
		return opp.add(qr);
	}

	public boolean containsAll(final QueryResult qr) {
		return opp.containsAll(qr);
	}

	public boolean containsAllExceptAnonymousLiterals(final QueryResult qr) {
		return opp.containsAllExceptAnonymousLiterals(qr);
	}

	public boolean remove(final Bindings b) {
		return opp.remove(b);
	}

	public boolean removeAll(final QueryResult res) {
		return opp.removeAll(res);
	}

	public boolean addFirst(final Bindings b) {
		return opp.addFirst(b);
	}

	public boolean addLast(final Bindings b) {
		return opp.addLast(b);
	}

	public boolean add(final int pos, final Bindings b) {
		return opp.add(pos, b);
	}

	public Bindings getFirst() {
		return opp.getFirst();
	}

	public Bindings getLast() {
		return opp.getLast();
	}

	public Bindings get(final int pos) {
		return opp.get(pos);
	}

	@Override
	public QueryResult clone() {
		final QueryResult ret = new QueryResultDebug(opp, debugstep, from, to,
				process);
		return ret;
	}

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

	public int size() {
		return opp.size();
	}

	public boolean isEmpty() {
		return opp.isEmpty();
	}

	public boolean addAll(final QueryResult res) {
		return opp.addAll(res);
	}

	@Override
	public String toString() {
		return opp.toString();
	}

	@Override
	public boolean equals(final Object o) {
		return opp.equals(o);
	}

	public boolean sameOrder(final QueryResult qr) {
		return opp.sameOrder(qr);
	}

	public boolean sameOrderExceptAnonymousLiterals(final QueryResult qr) {
		return opp.sameOrderExceptAnonymousLiterals(qr);
	}

	public Collection<Collection<Triple>> getTriples(
			final LinkedList<Collection<Triple>> lct) {
		return opp.getTriples(lct);
	}

	public void release() {
		opp.release();
	}

	public void materialize() {
		opp.materialize();
	}

	public Iterator<Bindings> oneTimeIterator() {
		return generateDebugIterator(opp.oneTimeIterator());
	}

	public Iterator<Bindings> iterator() {
		return generateDebugIterator(opp.iterator());
	}

	private void step(final BasicOperator from, final BasicOperator to,
			final Bindings bindings) {
		if (process)
			debugstep.step(from, to, bindings);
		else
			debugstep.stepDelete(from, to, bindings);
	}

	private Iterator<Bindings> generateDebugIterator(
			final Iterator<Bindings> itb) {
		if (itb instanceof SIPParallelIterator)
			return new SIPParallelIterator<Bindings, Bindings>() {

				public Bindings next(final Bindings k) {
					final Bindings next = ((SIPParallelIterator<Bindings, Bindings>) itb)
							.next(k);
					if (next != null)
						step(from, to, next);
					return next;
				}

				public void close() {
					((SIPParallelIterator<Bindings, Bindings>) itb).close();

				}

				public boolean hasNext() {
					return itb.hasNext();
				}

				public Bindings next() {
					final Bindings next = itb.next();
					if (next != null) {
						step(from, to, next);
					}
					return next;
				}

				public void remove() {
					itb.remove();
				}
			};
		else if (itb instanceof ParallelIterator)
			return new ParallelIterator<Bindings>() {

				public void close() {
					((ParallelIterator<Bindings>) itb).close();

				}

				public boolean hasNext() {
					return itb.hasNext();
				}

				public Bindings next() {
					final Bindings next = itb.next();
					if (next != null)
						step(from, to, next);
					return next;
				}

				public void remove() {
					itb.remove();
				}
			};
		else
			return new Iterator<Bindings>() {

				public boolean hasNext() {
					return itb.hasNext();
				}

				public Bindings next() {
					final Bindings next = itb.next();
					if (next != null)
						step(from, to, next);
					return next;
				}

				public void remove() {
					itb.remove();
				}
			};
	}

	public QueryResult getOriginalQueryResult() {
		return opp;
	}
}
