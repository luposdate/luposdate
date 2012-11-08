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
package lupos.datastructures.queryresult;

import java.util.Collection;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.engine.operators.singleinput.SIPFilterOperator;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class IteratorQueryResult extends QueryResult {

	protected Iterator<? extends Bindings> itb;

	public IteratorQueryResult(final Iterator<? extends Bindings> itb) {
		super();
		this.itb = itb;
	}

	private final static int[][] positions = { new int[] { 0, 1, 2 }, // SPO
			new int[] { 0, 2, 1 }, // SOP
			new int[] { 1, 0, 2 }, // PSO
			new int[] { 1, 2, 0 }, // POS
			new int[] { 2, 0, 1 }, // OSP
			new int[] { 2, 1, 0 } };// OPS

	public IteratorQueryResult(
			final SIPParallelIterator<Triple, TripleKey> itt,
			final TriplePattern tp,
			final RDF3XIndexScan.CollationOrder order,
			final boolean considerBloomFilters) {
		if (!considerBloomFilters)
			createIterator(itt, tp, order);
		else
			this.itb = new SIPParallelIterator<Bindings, Bindings>() {
				Triple lastTriple = null;
				Bindings next = computeNext();

				public boolean hasNext() {
					return (next != null);
				}

				public Bindings next() {
					if (next == null)
						return null;
					final Bindings znext = next;
					next = computeNext();
					return znext;
				}

				private Bindings computeNext() {
					while (itt.hasNext()) {
						// also consider inner joins in triple patterns like ?a ?a ?b.
						Triple t;
						if (tp.getBloomFilters() != null
								&& (tp.getBloomFilters()[0] != null
										|| tp.getBloomFilters()[1] != null || tp
										.getBloomFilters()[2] != null)) {
							do {
								boolean considerLastTriple;
								if (lastTriple == null) {
									lastTriple = itt.next();
									considerLastTriple = true;
								} else
									considerLastTriple = false;
								final Literal[] keyTriple = new Literal[3];
								boolean useKey = false;
								for (int j = 0; j < 3; j++) {
									final int i = positions[order.ordinal()][j];
									if (!useKey
											&& tp.getBloomFilters()[i] != null) {
										int index = (Math.abs(lastTriple
												.getPos(i).hashCode()) % SIPFilterOperator.NUMBEROFBITSFORBLOOMFILTER);
										if (!tp.getBloomFilters()[i].get(index)) {
											final int startIndex = index;
											index++;
											while ((index % tp
													.getBloomFilters()[i]
													.size()) != startIndex
													&& !tp.getBloomFilters()[i]
															.get(index
																	% tp
																			.getBloomFilters()[i]
																			.size())) {
												index++;
											}
											// All bits are set to 0 in the
											// bloom filter?
											if (index
													% tp.getBloomFilters()[i]
															.size() == startIndex)
												return null;
											// calculate distancepreserving jump over the gap!
											final int code = ((LazyLiteral) lastTriple
													.getPos(i)).getCode()
													+ (index - startIndex);
											// if (code >=
											// LazyLiteral.maxID())
											// keyTriple[i] = lastTriple
											// .getPos(i);
											// else {
											keyTriple[i] = new LazyLiteral(code);
											useKey = true;
											// }
										} else
											keyTriple[i] = null;
									} else
										keyTriple[i] = null;
								}
								if (useKey) {
									// do distancepreserving jump over the gap!
									final TripleKey key = tp.getKey(keyTriple,
											order);
									t = itt.next(key);
								} else
									t = (considerLastTriple) ? lastTriple : itt
											.next();
								lastTriple = t;
								if (t == null)
									return null;
							} while ((tp.getBloomFilters()[0] != null && !tp
									.getBloomFilters()[0]
									.get((Math.abs(lastTriple.getPos(0)
											.hashCode()) % SIPFilterOperator.NUMBEROFBITSFORBLOOMFILTER)))
									|| (tp.getBloomFilters()[1] != null && !tp
											.getBloomFilters()[1]
											.get((Math.abs(lastTriple.getPos(1)
													.hashCode()) % SIPFilterOperator.NUMBEROFBITSFORBLOOMFILTER)))
									|| (tp.getBloomFilters()[2] != null && !tp
											.getBloomFilters()[2]
											.get((Math.abs(lastTriple.getPos(2)
													.hashCode()) % SIPFilterOperator.NUMBEROFBITSFORBLOOMFILTER))));
						} else
							t = itt.next();
						if (t != null) {
							lastTriple = t;
							final Bindings znext = tp.process(t, false);
							if (znext != null) {
								return znext;
							}
						}
					}
					return null;
				}

				private Bindings computeNext(final Bindings k) {
					if (!(itt instanceof SIPParallelIterator))
						return computeNext();
					final TripleKey key = tp.getKey(k, order);
					while (itt.hasNext()) {
						// also consider inner joins in triple patterns like
						// ?a ?a ?b.
						// Furthermore, do distancepreserving jump over the gap
						// using bloom filters!
						if (tp.getBloomFilters() != null) {
							for (int i = 0; i < 3; i++) {
								if (tp.getBloomFilters()[i] != null
										&& key.getTriple().getPos(i) != null) {
									int index = (Math.abs(key.getTriple()
											.getPos(i).hashCode()) % SIPFilterOperator.NUMBEROFBITSFORBLOOMFILTER);
									if (!tp.getBloomFilters()[i].get(index)) {
										final int startIndex = index;
										index++;
										while ((index % tp.getBloomFilters()[i]
												.size()) != startIndex
												&& !tp.getBloomFilters()[i]
														.get(index
																% tp
																		.getBloomFilters()[i]
																		.size()))
											index++;
										// all bits in the bloom filter are
										// set
										// to 0?
										if (index
												% tp.getBloomFilters()[i]
														.size() == startIndex)
											return null;
										// distancepreserving jump over the
										// gap!
										final int code = ((LazyLiteral) key
												.getTriple().getPos(i))
												.getCode()
												+ (index - startIndex);
										// if (code < LazyLiteral.maxID())
										key.getTriple().setPos(i,
												new LazyLiteral(code));
									}
								}
							}
						}
						final Triple t = itt.next(key);
						if (t != null) {
							lastTriple = t;
							final Bindings znext = tp.process(t, false);
							if (znext != null)
								return znext;
						}
					}
					return null;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}

				public Bindings next(final Bindings k) {
					if (next == null)
						return null;
					final Bindings znext = next;
					next = computeNext(k);
					return znext;
				}

				public void close() {
				}
			};
	}

	public IteratorQueryResult(final Iterator<Triple> itt,
			final TriplePattern tp,
			final RDF3XIndexScan.CollationOrder order) {
		createIterator(itt, tp, order);
	}

	private void createIterator(final Iterator<Triple> itt,
			final TriplePattern tp,
			final RDF3XIndexScan.CollationOrder order) {
		this.itb = new SIPParallelIterator<Bindings, Bindings>() {
			Bindings next = computeNext();

			public boolean hasNext() {
				return (next != null);
			}

			public Bindings next() {
				if (next == null)
					return null;
				final Bindings znext = next;
				next = computeNext();
				return znext;
			}

			private Bindings computeNext() {
				while (itt.hasNext()) {
					// also consider inner joins in triple patterns like ?a ?a
					// ?b.
					final Triple t = itt.next();
					if (t != null) {
						final Bindings znext = tp.process(t, false);
						if (znext != null)
							return znext;
					}
				}
				return null;
			}

			private Bindings computeNext(final Bindings k) {
				if (!(itt instanceof SIPParallelIterator))
					return computeNext();
				final TripleKey key = tp.getKey(k, order);
				while (itt.hasNext()) {
					// also consider inner joins in triple patterns like ?a ?a
					// ?b.
					final Triple t = ((SIPParallelIterator<Triple, TripleKey>) itt)
							.next(key);
					if (t != null) {
						final Bindings znext = tp.process(t, false);
						if (znext != null)
							return znext;
					}
				}
				return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public Bindings next(final Bindings k) {
				if (next == null)
					return null;
				final Bindings znext = next;
				next = computeNext(k);
				return znext;
			}

			public void close() {
			}
		};
	}

	protected void addRemainingFromIterator() {
		if (itb != null) {
			while (itb.hasNext()) {
				add(itb.next());
			}
			if (itb instanceof ParallelIterator) {
				((ParallelIterator) itb).close();
			}
		}
	}

	@Override
	public Collection<Bindings> getCollection() {
		if (itb != null && itb.hasNext())
			addRemainingFromIterator();
		return bindings;
	}

	@Override
	public boolean contains(final Bindings b) {
		if (itb != null && itb.hasNext())
			addRemainingFromIterator();
		return super.contains(b);
	}

	@Override
	public boolean remove(final Bindings b) {
		if (itb != null && itb.hasNext())
			addRemainingFromIterator();
		return super.remove(b);
	}

	@Override
	public boolean addFirst(final Bindings b) {
		if (itb != null && itb.hasNext())
			addRemainingFromIterator();
		return super.addFirst(b);
	}

	@Override
	public boolean addLast(final Bindings b) {
		if (itb != null && itb.hasNext())
			addRemainingFromIterator();
		return super.addLast(b);
	}

	@Override
	public boolean add(final int pos, final Bindings b) {
		if (itb != null && itb.hasNext())
			addRemainingFromIterator();
		return super.add(pos, b);
	}

	@Override
	public Bindings getFirst() {
		if (bindings.isEmpty()) {
			if (itb == null)
				return null;
			if (itb.hasNext()) {
				final Bindings b = itb.next();
				if (!itb.hasNext()) {
					if (itb instanceof ParallelIterator) {
						((ParallelIterator) itb).close();
					}
				}
				super.add(b);
				return b;
			} else
				return null;
		} else
			return super.getFirst();

	}

	@Override
	public Bindings getLast() {
		if (itb != null && itb.hasNext())
			addRemainingFromIterator();
		return super.getLast();
	}

	@Override
	public Bindings get(final int pos) {
		if (itb != null && itb.hasNext())
			addRemainingFromIterator();
		return super.get(pos);
	}

	/**
	 * Because of optimization purposes, you must call this method only once,
	 * because the a second call of the method or other methods forgets
	 * bindings.
	 */
	@Override
	public int oneTimeSize() {
		int size = super.size();
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
		if (itb != null && itb.hasNext())
			addRemainingFromIterator();
		return super.size();
	}

	@Override
	public Iterator<Bindings> iterator() {
		if (itb != null && itb.hasNext())
			addRemainingFromIterator();
		return super.iterator();
	}

	/**
	 * Because of optimization purposes, you must call this method only once,
	 * because the iterator returned from a second call of the method forgets
	 * bindings.
	 */
	@Override
	public Iterator<Bindings> oneTimeIterator() {
		if (bindings == null || bindings.size() == 0) {
			if (itb == null)
				return new Iterator<Bindings>() {
					public boolean hasNext() {
						return false;
					}

					public Bindings next() {
						return null;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};

			else if (itb instanceof SIPParallelIterator) {
				return new SIPParallelIterator<Bindings, Bindings>() {
					final SIPParallelIterator<Bindings, Bindings> itb_local = (SIPParallelIterator<Bindings, Bindings>) itb;

					public boolean hasNext() {
						return itb_local.hasNext();
					}

					public Bindings next() {
						if (itb_local.hasNext()) {
							final Bindings b = itb_local.next();
							if (!(itb_local.hasNext())) {
								itb_local.close();
							}
							return b;
						}
						return null;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}

					@Override
					public void finalize() {
						close();
					}

					public void close() {
						itb_local.close();
					}

					public Bindings next(final Bindings k) {
						if (itb_local.hasNext()) {
							final Bindings b = itb_local.next(k);
							if (!(itb_local.hasNext())) {
								itb_local.close();
							}
							return b;
						}
						return null;
					}
				};

			} else
				return new ParallelIterator<Bindings>() {
					public boolean hasNext() {
						return itb.hasNext();
					}

					public Bindings next() {
						if (itb.hasNext()) {
							final Bindings b = itb.next();
							if (!(itb.hasNext())) {
								if (itb instanceof ParallelIterator) {
									((ParallelIterator) itb).close();
								}
							}
							return b;
						}
						return null;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}

					@Override
					public void finalize() {
						close();
					}

					public void close() {
						if (itb instanceof ParallelIterator) {
							((ParallelIterator) itb).close();
						}
					}
				};
		}

		if (itb == null)
			return bindings.iterator();

		return new ParallelIterator<Bindings>() {
			Iterator<Bindings> it1 = bindings.iterator();

			public boolean hasNext() {
				return it1.hasNext() || itb.hasNext();
			}

			public Bindings next() {
				if (it1.hasNext())
					return it1.next();
				if (itb.hasNext()) {
					final Bindings b = itb.next();
					if (!(itb.hasNext())) {
						if (itb instanceof ParallelIterator) {
							((ParallelIterator) itb).close();
						}
					}
					return b;
				}
				return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void finalize() {
				close();
			}

			public void close() {
				if (itb instanceof ParallelIterator) {
					((ParallelIterator) itb).close();
				}
			}
		};
	}

	@Override
	public void release() {
		if (itb instanceof ParallelIterator) {
			((ParallelIterator) itb).close();
		}
		super.release();
	}

	@Override
	public boolean isEmpty() {
		return (bindings.isEmpty() && (itb == null || !itb.hasNext()));
	}

	@Override
	public void materialize() {
		if (itb != null && itb.hasNext())
			addRemainingFromIterator();
	}

	@Override
	public String toString() {
		if (itb != null && itb.hasNext())
			addRemainingFromIterator();
		return super.toString();
	}
}
