
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

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.engine.operators.singleinput.SIPFilterOperator;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.util.ImmutableIterator;
public class IteratorQueryResult extends QueryResult {

	protected Iterator<? extends Bindings> itb;

	/**
	 * <p>Constructor for IteratorQueryResult.</p>
	 *
	 * @param itb a {@link java.util.Iterator} object.
	 */
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

	/**
	 * <p>Constructor for IteratorQueryResult.</p>
	 *
	 * @param itt a {@link lupos.datastructures.queryresult.SIPParallelIterator} object.
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param order a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 * @param considerBloomFilters a boolean.
	 */
	public IteratorQueryResult(
			final SIPParallelIterator<Triple, TripleKey> itt,
			final TriplePattern tp,
			final RDF3XIndexScan.CollationOrder order,
			final boolean considerBloomFilters) {
		if (!considerBloomFilters) {
			this.createIterator(itt, tp, order);
		} else {
			this.itb = new SIPParallelIterator<Bindings, Bindings>() {
				Triple lastTriple = null;
				Bindings next = this.computeNext();

				@Override
				public boolean hasNext() {
					return (this.next != null);
				}

				@Override
				public Bindings next() {
					if (this.next == null) {
						return null;
					}
					final Bindings znext = this.next;
					this.next = this.computeNext();
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
								if (this.lastTriple == null) {
									this.lastTriple = itt.next();
									considerLastTriple = true;
								} else {
									considerLastTriple = false;
								}
								final Literal[] keyTriple = new Literal[3];
								boolean useKey = false;
								for (int j = 0; j < 3; j++) {
									final int i = positions[order.ordinal()][j];
									if (!useKey
											&& tp.getBloomFilters()[i] != null) {
										int index = (Math.abs(this.lastTriple
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
															.size() == startIndex) {
												return null;
											}
											// calculate distancepreserving jump over the gap!
											final int code = ((LazyLiteral) this.lastTriple
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
										} else {
											keyTriple[i] = null;
										}
									} else {
										keyTriple[i] = null;
									}
								}
								if (useKey) {
									// do distancepreserving jump over the gap!
									final TripleKey key = tp.getKey(keyTriple,
											order);
									t = itt.next(key);
								} else {
									t = (considerLastTriple) ? this.lastTriple : itt
											.next();
								}
								this.lastTriple = t;
								if (t == null) {
									return null;
								}
							} while ((tp.getBloomFilters()[0] != null && !tp
									.getBloomFilters()[0]
									.get((Math.abs(this.lastTriple.getPos(0)
											.hashCode()) % SIPFilterOperator.NUMBEROFBITSFORBLOOMFILTER)))
									|| (tp.getBloomFilters()[1] != null && !tp
											.getBloomFilters()[1]
											.get((Math.abs(this.lastTriple.getPos(1)
													.hashCode()) % SIPFilterOperator.NUMBEROFBITSFORBLOOMFILTER)))
									|| (tp.getBloomFilters()[2] != null && !tp
											.getBloomFilters()[2]
											.get((Math.abs(this.lastTriple.getPos(2)
													.hashCode()) % SIPFilterOperator.NUMBEROFBITSFORBLOOMFILTER))));
						} else {
							t = itt.next();
						}
						if (t != null) {
							this.lastTriple = t;
							final Bindings znext = tp.process(t, false);
							if (znext != null) {
								return znext;
							}
						}
					}
					return null;
				}

				private Bindings computeNext(final Bindings k) {
					if (!(itt instanceof SIPParallelIterator)) {
						return this.computeNext();
					}
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
																		.size())) {
											index++;
										}
										// all bits in the bloom filter are
										// set
										// to 0?
										if (index
												% tp.getBloomFilters()[i]
														.size() == startIndex) {
											return null;
										}
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
							this.lastTriple = t;
							final Bindings znext = tp.process(t, false);
							if (znext != null) {
								return znext;
							}
						}
					}
					return null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public Bindings next(final Bindings k) {
					if (this.next == null) {
						return null;
					}
					final Bindings znext = this.next;
					this.next = this.computeNext(k);
					return znext;
				}

				@Override
				public void close() {
				}
			};
		}
	}

	/**
	 * <p>Constructor for IteratorQueryResult.</p>
	 *
	 * @param itt a {@link java.util.Iterator} object.
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param order a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 */
	public IteratorQueryResult(final Iterator<Triple> itt,
			final TriplePattern tp,
			final RDF3XIndexScan.CollationOrder order) {
		this.createIterator(itt, tp, order);
	}

	private void createIterator(final Iterator<Triple> itt,
			final TriplePattern tp,
			final RDF3XIndexScan.CollationOrder order) {
		this.itb = new SIPParallelIterator<Bindings, Bindings>() {
			Bindings next = this.computeNext();

			@Override
			public boolean hasNext() {
				return (this.next != null);
			}

			@Override
			public Bindings next() {
				if (this.next == null) {
					return null;
				}
				final Bindings znext = this.next;
				this.next = this.computeNext();
				return znext;
			}

			private Bindings computeNext() {
				while (itt.hasNext()) {
					// also consider inner joins in triple patterns like ?a ?a
					// ?b.
					final Triple t = itt.next();
					if (t != null) {
						final Bindings znext = tp.process(t, false);
						if (znext != null) {
							return znext;
						}
					}
				}
				return null;
			}

			private Bindings computeNext(final Bindings k) {
				if (!(itt instanceof SIPParallelIterator)) {
					return this.computeNext();
				}
				final TripleKey key = tp.getKey(k, order);
				while (itt.hasNext()) {
					// also consider inner joins in triple patterns like ?a ?a
					// ?b.
					final Triple t = ((SIPParallelIterator<Triple, TripleKey>) itt)
							.next(key);
					if (t != null) {
						final Bindings znext = tp.process(t, false);
						if (znext != null) {
							return znext;
						}
					}
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Bindings next(final Bindings k) {
				if (this.next == null) {
					return null;
				}
				final Bindings znext = this.next;
				this.next = this.computeNext(k);
				return znext;
			}

			@Override
			public void close() {
			}
		};
	}

	/**
	 * <p>addRemainingFromIterator.</p>
	 */
	protected void addRemainingFromIterator() {
		if (this.itb != null) {
			while (this.itb.hasNext()) {
				this.add(this.itb.next());
			}
			if (this.itb instanceof ParallelIterator) {
				((ParallelIterator) this.itb).close();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Collection<Bindings> getCollection() {
		if (this.itb != null && this.itb.hasNext()) {
			this.addRemainingFromIterator();
		}
		return this.bindings;
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final Bindings b) {
		if (this.itb != null && this.itb.hasNext()) {
			this.addRemainingFromIterator();
		}
		return super.contains(b);
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final Bindings b) {
		if (this.itb != null && this.itb.hasNext()) {
			this.addRemainingFromIterator();
		}
		return super.remove(b);
	}

	/** {@inheritDoc} */
	@Override
	public boolean addFirst(final Bindings b) {
		if (this.itb != null && this.itb.hasNext()) {
			this.addRemainingFromIterator();
		}
		return super.addFirst(b);
	}

	/** {@inheritDoc} */
	@Override
	public boolean addLast(final Bindings b) {
		if (this.itb != null && this.itb.hasNext()) {
			this.addRemainingFromIterator();
		}
		return super.addLast(b);
	}

	/** {@inheritDoc} */
	@Override
	public boolean add(final int pos, final Bindings b) {
		if (this.itb != null && this.itb.hasNext()) {
			this.addRemainingFromIterator();
		}
		return super.add(pos, b);
	}

	/** {@inheritDoc} */
	@Override
	public Bindings getFirst() {
		if (this.bindings.isEmpty()) {
			if (this.itb == null) {
				return null;
			}
			if (this.itb.hasNext()) {
				final Bindings b = this.itb.next();
				if (!this.itb.hasNext()) {
					if (this.itb instanceof ParallelIterator) {
						((ParallelIterator) this.itb).close();
					}
				}
				super.add(b);
				return b;
			} else {
				return null;
			}
		} else {
			return super.getFirst();
		}

	}

	/** {@inheritDoc} */
	@Override
	public Bindings getLast() {
		if (this.itb != null && this.itb.hasNext()) {
			this.addRemainingFromIterator();
		}
		return super.getLast();
	}

	/** {@inheritDoc} */
	@Override
	public Bindings get(final int pos) {
		if (this.itb != null && this.itb.hasNext()) {
			this.addRemainingFromIterator();
		}
		return super.get(pos);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Because of optimization purposes, you must call this method only once,
	 * because the a second call of the method or other methods forgets
	 * bindings.
	 */
	@Override
	public int oneTimeSize() {
		int size = super.size();
		if (this.itb != null) {
			while (this.itb.hasNext()) {
				size++;
				this.itb.next();
			}
			if (this.itb instanceof ParallelIterator) {
				((ParallelIterator) this.itb).close();
			}
		}
		return size;
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		if (this.itb != null && this.itb.hasNext()) {
			this.addRemainingFromIterator();
		}
		return super.size();
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<Bindings> iterator() {
		if (this.itb != null && this.itb.hasNext()) {
			this.addRemainingFromIterator();
		}
		return super.iterator();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Because of optimization purposes, you must call this method only once,
	 * because the iterator returned from a second call of the method forgets
	 * bindings.
	 */
	@Override
	public Iterator<Bindings> oneTimeIterator() {
		if (this.bindings == null || this.bindings.size() == 0) {
			if (this.itb == null) {
				return new ImmutableIterator<Bindings>() {
					@Override
					public boolean hasNext() {
						return false;
					}

					@Override
					public Bindings next() {
						return null;
					}
				};
			} else if (this.itb instanceof SIPParallelIterator) {
				return new SIPParallelIterator<Bindings, Bindings>() {
					final SIPParallelIterator<Bindings, Bindings> itb_local = (SIPParallelIterator<Bindings, Bindings>) IteratorQueryResult.this.itb;

					@Override
					public boolean hasNext() {
						return this.itb_local.hasNext();
					}

					@Override
					public Bindings next() {
						if (this.itb_local.hasNext()) {
							final Bindings b = this.itb_local.next();
							if (!(this.itb_local.hasNext())) {
								this.itb_local.close();
							}
							return b;
						}
						return null;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

					@Override
					public void finalize() {
						this.close();
					}

					@Override
					public void close() {
						this.itb_local.close();
					}

					@Override
					public Bindings next(final Bindings k) {
						if (this.itb_local.hasNext()) {
							final Bindings b = this.itb_local.next(k);
							if (!(this.itb_local.hasNext())) {
								this.itb_local.close();
							}
							return b;
						}
						return null;
					}
				};

			} else {
				return new ParallelIterator<Bindings>() {
					@Override
					public boolean hasNext() {
						return IteratorQueryResult.this.itb.hasNext();
					}

					@Override
					public Bindings next() {
						if (IteratorQueryResult.this.itb.hasNext()) {
							final Bindings b = IteratorQueryResult.this.itb.next();
							if (!(IteratorQueryResult.this.itb.hasNext())) {
								if (IteratorQueryResult.this.itb instanceof ParallelIterator) {
									((ParallelIterator) IteratorQueryResult.this.itb).close();
								}
							}
							return b;
						}
						return null;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

					@Override
					public void finalize() {
						this.close();
					}

					@Override
					public void close() {
						if (IteratorQueryResult.this.itb instanceof ParallelIterator) {
							((ParallelIterator) IteratorQueryResult.this.itb).close();
						}
					}
				};
			}
		}

		if (this.itb == null) {
			return this.bindings.iterator();
		}

		return new ParallelIterator<Bindings>() {
			Iterator<Bindings> it1 = IteratorQueryResult.this.bindings.iterator();

			@Override
			public boolean hasNext() {
				return this.it1.hasNext() || IteratorQueryResult.this.itb.hasNext();
			}

			@Override
			public Bindings next() {
				if (this.it1.hasNext()) {
					return this.it1.next();
				}
				if (IteratorQueryResult.this.itb.hasNext()) {
					final Bindings b = IteratorQueryResult.this.itb.next();
					if (!(IteratorQueryResult.this.itb.hasNext())) {
						if (IteratorQueryResult.this.itb instanceof ParallelIterator) {
							((ParallelIterator) IteratorQueryResult.this.itb).close();
						}
					}
					return b;
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void finalize() {
				this.close();
			}

			@Override
			public void close() {
				if (IteratorQueryResult.this.itb instanceof ParallelIterator) {
					((ParallelIterator) IteratorQueryResult.this.itb).close();
				}
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	public void release() {
		if (this.itb instanceof ParallelIterator) {
			((ParallelIterator) this.itb).close();
		}
		super.release();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return (this.bindings.isEmpty() && (this.itb == null || !this.itb.hasNext()));
	}

	/** {@inheritDoc} */
	@Override
	public void materialize() {
		if (this.itb != null && this.itb.hasNext()) {
			this.addRemainingFromIterator();
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (this.itb != null && this.itb.hasNext()) {
			this.addRemainingFromIterator();
		}
		return super.toString();
	}
}
