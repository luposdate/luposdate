/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.engine.operators.multiinput.join;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.items.BindingsComparator;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.datastructures.sorteddata.SortedBag;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.optional.OptionalResult;
import lupos.misc.debug.DebugStep;
import lupos.misc.util.ImmutableIterator;

public class MergeJoin extends Join {

	protected SortedBag<Bindings> left = null;
	protected SortedBag<Bindings> right = null;

	protected BindingsComparator comp = new BindingsComparator();

	public void init(final SortedBag<Bindings> left, final SortedBag<Bindings> right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		this.comp.setVariables(this.intersectionVariables);
		if (operandID == 0) {
			final Iterator<Bindings> itb = bindings.oneTimeIterator();
			while (itb.hasNext()) {
				this.left.add(itb.next());
			}
		} else if (operandID == 1) {
			final Iterator<Bindings> itb = bindings.oneTimeIterator();
			while (itb.hasNext()) {
				this.right.add(itb.next());
			}
		} else {
			System.err.println("MergeJoin is a binary operator, but received the operand number "
							+ operandID);
		}
		return null;
	}

	@Override
	public OptionalResult processJoin(final QueryResult bindings,
			final int operandID) {
		this.comp.setVariables(this.intersectionVariables);
		if (operandID == 0) {
			this.left.addAll(bindings.getCollection());
		} else if (operandID == 1) {
			this.right.addAll(bindings.getCollection());
		} else {
			System.err.println("Embedded MergeJoin operator in Optional is a binary operator, but received the operand number "
							+ operandID);
		}
		return null;
	}

	@Override
	public OptionalResult joinBeforeEndOfStream() {
		if (this.left != null && this.right != null) {
			this.comp.setVariables(this.intersectionVariables);
			final OptionalResult or = this.mergeJoinOptionalResult(this.left, this.right, this.comp);
			this.left.clear();
			this.right.clear();
			return or;
		} else {
			return null;
		}
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		if (this.left != null && this.right != null && this.left.size() > 0
				&& this.right.size() > 0) {
			this.comp.setVariables(this.intersectionVariables);
			final ParallelIterator<Bindings> currentResult = (this.intersectionVariables
					.size() == 0) ? MergeJoin.cartesianProductIterator(
					QueryResult.createInstance(this.left.iterator()), QueryResult
							.createInstance(this.right.iterator())) : MergeJoin
					.mergeJoinIterator(this.left.iterator(), this.right.iterator(), this.comp,
							this.intersectionVariables);
			if (currentResult != null && currentResult.hasNext()) {
				final QueryResult result = QueryResult
						.createInstance(new ParallelIterator<Bindings>() {

							int number = 0;

							@Override
							public void close() {
								currentResult.close();
							}

							@Override
							public boolean hasNext() {
								if (!currentResult.hasNext()) {
									MergeJoin.this.realCardinality = this.number;
									this.close();
								}
								return currentResult.hasNext();
							}

							@Override
							public Bindings next() {
								final Bindings b = currentResult.next();
								if (!currentResult.hasNext()) {
									MergeJoin.this.realCardinality = this.number;
									this.close();
								}
								if (b != null) {
									this.number++;
								}
								return b;
							}

							@Override
							public void remove() {
								currentResult.remove();
							}

							@Override
							public void finalize() {
								this.close();
							}
						});

				if (this.succeedingOperators.size() > 1) {
					result.materialize();
				}
				for (final OperatorIDTuple opId : this.succeedingOperators) {
					opId.processAll(result);
				}
			} else {
				if (this.left instanceof DBMergeSortedBag) {
					((DBMergeSortedBag) this.left).release();
				}
				if (this.right instanceof DBMergeSortedBag) {
					((DBMergeSortedBag) this.right).release();
				}
			}

		}
		// left.clear();
		// right.clear();
		return msg;
	}

	public static QueryResult mergeJoin(final Iterator<Bindings> ssb1it,
			final Iterator<Bindings> ssb2it, final Comparator<Bindings> comp) {

		if ((ssb1it == null) || (ssb2it == null) || !ssb1it.hasNext()
				|| !ssb2it.hasNext()) {
			return null;
		}

		final QueryResult result = QueryResult.createInstance();
		Bindings b1 = ssb1it.next();
		Bindings b2 = ssb2it.next();
		boolean processFurther = true;
		do {
			final int compare = comp.compare(b1, b2);
			// System.out.println("compare:"+compare+" b1:"+b1+" b2:"+b2);
			if (compare == 0) {

				final Collection<Bindings> bindings1 = new LinkedList<Bindings>();
				final Collection<Bindings> bindings2 = new LinkedList<Bindings>();

				final Bindings bindings = b1;
				int left = 0;
				do {
					bindings1.add(b1);
					left++;
					if (!ssb1it.hasNext()) {
						processFurther = false;
						break;
					}
					b1 = ssb1it.next();
				} while (comp.compare(b1, bindings) == 0);
				int right = 0;
				do {
					bindings2.add(b2);
					right++;
					if (!ssb2it.hasNext()) {
						processFurther = false;
						break;
					}
					b2 = ssb2it.next();
				} while (comp.compare(b2, bindings) == 0);
				for (final Bindings zb1 : bindings1) {
					for (final Bindings zb2 : bindings2) {
						final Bindings bnew = zb1.clone();
						bnew.addAll(zb2);
						bnew.addAllTriples(zb2);
						bnew.addAllPresortingNumbers(zb2);
						result.add(bnew);
					}
				}
			} else if (compare < 0) {
				if (ssb1it.hasNext()) {
					b1 = ssb1it.next();
				} else {
					processFurther = false;
				}
			} else if (compare > 0) {
				if (ssb2it.hasNext()) {
					b2 = ssb2it.next();
				} else {
					processFurther = false;
				}
			}
		} while (processFurther == true);
		if (result.size() > 0) {
			return result;
		} else {
			return null;
		}
	}

	public static ParallelIterator<Bindings> mergeJoinIterator(
			final Iterator<Bindings> ssb1it, final Iterator<Bindings> ssb2it,
			final Comparator<Bindings> comp, final Collection<Variable> vars) {

		if ((ssb1it == null) || (ssb2it == null) || !ssb1it.hasNext()
				|| !ssb2it.hasNext()) {
			return new ParallelIterator<Bindings>() {
				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public Bindings next() {
					return null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public void close() {
				}
			};
		}

		return new SIPParallelIterator<Bindings, Bindings>() {
			Bindings b1 = ssb1it.next();
			Bindings b2 = ssb2it.next();
			boolean processFurther = true;
			Iterator<Bindings> currentBinding = null;
			Collection<Bindings> bindings1 = null;
			Collection<Bindings> bindings2 = null;

			@Override
			public boolean hasNext() {
				if (this.currentBinding != null && this.currentBinding.hasNext()) {
					return true;
				}
				if (this.processFurther) {
					this.computeNextResults();
					if (this.currentBinding != null && this.currentBinding.hasNext()) {
						return true;
					}
				}
				return false;
			}

			@Override
			public void close() {
				if (ssb1it instanceof ParallelIterator) {
					((ParallelIterator<Bindings>) ssb1it).close();
				}
				if (ssb2it instanceof ParallelIterator) {
					((ParallelIterator<Bindings>) ssb2it).close();
				}
			}

			@Override
			public Bindings next() {
				if (this.currentBinding != null && this.currentBinding.hasNext()) {
					return this.currentBinding.next();
				}
				if (this.processFurther) {
					this.computeNextResults();
					if (this.currentBinding != null && this.currentBinding.hasNext()) {
						return this.currentBinding.next();
					}
				}
				return null;
			}

			public void computeNextResults() {
				while (this.processFurther == true) {
					final int compare = comp.compare(this.b1, this.b2);
					//System.out.println("compare:"+compare+" b1:"+b1+" b2:"+b2)
					// ;
					if (compare == 0) {

						this.bindings1 = new LinkedList<Bindings>();
						this.bindings2 = new LinkedList<Bindings>();

						final Bindings bindings = this.b1;
						do {
							this.bindings1.add(this.b1);
							if (!ssb1it.hasNext()) {
								this.processFurther = false;
								break;
							}
							this.b1 = ssb1it.next();
						} while (comp.compare(this.b1, bindings) == 0);
						do {
							this.bindings2.add(this.b2);
							if (!ssb2it.hasNext()) {
								this.processFurther = false;
								break;
							}
							this.b2 = ssb2it.next();
						} while (comp.compare(this.b2, bindings) == 0);
						this.currentBinding = new Iterator<Bindings>() {
							final Iterator<Bindings> itb1 = bindings1.iterator();
							Iterator<Bindings> itb2 = bindings2.iterator();
							Bindings zb1 = this.itb1.next();

							@Override
							public boolean hasNext() {
								return this.itb1.hasNext() || this.itb2.hasNext();
							}

							@Override
							public Bindings next() {
								if (!this.hasNext()) {
									return null;
								}
								if (!this.itb2.hasNext()) {
									this.zb1 = this.itb1.next();
									this.itb2 = bindings2.iterator();
								}
								final Bindings bnew = this.zb1.clone();
								final Bindings zb2 = this.itb2.next();
								bnew.addAll(zb2);
								bnew.addAllTriples(zb2);
								bnew.addAllPresortingNumbers(zb2);
								return bnew;
							}

							@Override
							public void remove() {
								throw new UnsupportedOperationException();
							}
						};
						return;
					} else if (compare < 0) {
						if (ssb1it.hasNext()) {
							if (ssb1it instanceof SIPParallelIterator) {
								final Bindings projected = Bindings
										.createNewInstance();
								for (final Variable v : vars) {
									projected.add(v, this.b2.get(v));
								}
								do {
									this.b1 = ((SIPParallelIterator<Bindings, Bindings>) ssb1it)
											.next(projected);
								} while (this.b1 != null
										&& comp.compare(this.b1, projected) < 0);
								if (this.b1 == null) {
									this.processFurther = false;
								}
							} else {
								this.b1 = ssb1it.next();
							}
						} else {
							this.processFurther = false;
						}
					} else if (compare > 0) {
						if (ssb2it.hasNext()) {
							if (ssb2it instanceof SIPParallelIterator) {
								final Bindings projected = Bindings
										.createNewInstance();
								for (final Variable v : vars) {
									projected.add(v, this.b1.get(v));
								}
								do {
									this.b2 = ((SIPParallelIterator<Bindings, Bindings>) ssb2it)
											.next(projected);
								} while (this.b2 != null
										&& comp.compare(this.b2, projected) < 0);
								if (this.b2 == null) {
									this.processFurther = false;
								}
							} else {
								this.b2 = ssb2it.next();
							}
						} else {
							this.processFurther = false;
						}
					}
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Bindings next(final Bindings k) {
				if (this.currentBinding != null && this.currentBinding.hasNext()) {
					return this.currentBinding.next();
				}
				if (this.processFurther) {
					if (ssb1it instanceof SIPParallelIterator) {
						while (comp.compare(this.b1, k) < 0) {
							this.b1 = ((SIPParallelIterator<Bindings, Bindings>) ssb1it)
									.next(k);
						}
					} else {
						while (comp.compare(this.b1, k) < 0) {
							this.b1 = ssb1it.next();
						}
					}
					if (ssb2it instanceof SIPParallelIterator) {
						while (comp.compare(this.b2, k) < 0) {
							this.b2 = ((SIPParallelIterator<Bindings, Bindings>) ssb1it).next(k);
						}
					} else {
						while (comp.compare(this.b2, k) < 0) {
							this.b2 = ssb1it.next();
						}
					}
					if (this.b1 == null || this.b2 == null) {
						this.processFurther = false;
						return null;
					}
					this.computeNextResults();
					if (this.currentBinding != null && this.currentBinding.hasNext()) {
						return this.currentBinding.next();
					}
				}
				return null;
			}
		};
	}

	public static ParallelIterator<Bindings> mergeJoinIterator(
			final Iterator<Bindings>[] ssbit, final Comparator<Bindings> comp,
			final Collection<Variable> vars) {

		for (final Iterator<Bindings> it : ssbit) {
			if (it == null || !it.hasNext()) {
				return new ParallelIterator<Bindings>() {
					@Override
					public boolean hasNext() {
						return false;
					}

					@Override
					public Bindings next() {
						return null;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

					@Override
					public void close() {
					}
				};
			}
		}

		final Bindings[] b = new Bindings[ssbit.length];
		for (int i = 0; i < b.length; i++) {
			b[i] = ssbit[i].next();
		}
		final Collection<Bindings>[] bindings = new Collection[b.length];

		return new SIPParallelIterator<Bindings, Bindings>() {
			boolean processFurther = true;
			Iterator<Bindings> currentBinding = null;

			@Override
			public boolean hasNext() {
				if (this.currentBinding != null && this.currentBinding.hasNext()) {
					return true;
				}
				if (this.processFurther) {
					this.computeNextResults();
					if (this.currentBinding != null && this.currentBinding.hasNext()) {
						return true;
					}
				}
				return false;
			}

			@Override
			public void close() {
				for (final Iterator<Bindings> it : ssbit) {
					if (it instanceof ParallelIterator) {
						((ParallelIterator<Bindings>) it).close();
					}
				}
			}

			@Override
			public Bindings next() {
				if (this.currentBinding != null && this.currentBinding.hasNext()) {
					return this.currentBinding.next();
				}
				if (this.processFurther) {
					this.computeNextResults();
					if (this.currentBinding != null && this.currentBinding.hasNext()) {
						return this.currentBinding.next();
					}
				}
				return null;
			}

			public void computeNextResults() {
				while (this.processFurther == true) {
					boolean equal = true;
					Bindings maximum = b[0];
					for (int i = 1; i < b.length; i++) {
						final int compare = comp.compare(maximum, b[i]);
						if (compare > 0) {
							equal = false;
						} else if (compare < 0) {
							equal = false;
							maximum = b[i];
						}
					}

					if (equal) {
						for (int i = 0; i < b.length; i++) {
							bindings[i] = new LinkedList<Bindings>();
							final Bindings bindingsCurrent = b[i];
							while (comp.compare(b[i], bindingsCurrent) == 0) {
								bindings[i].add(b[i]);
								if (!ssbit[i].hasNext()) {
									this.processFurther = false;
									break;
								}
								b[i] = ssbit[i].next();
							}
						}

						final Iterator<Bindings>[] itb = new Iterator[b.length];
						final Bindings zb[] = new Bindings[b.length - 1];
						for (int i = 0; i < b.length; i++) {
							itb[i] = bindings[i].iterator();
						}
						for (int i = 0; i < zb.length; i++) {
							zb[i] = itb[i].next();
						}

						this.currentBinding = new Iterator<Bindings>() {

							@Override
							public boolean hasNext() {
								for (final Iterator<Bindings> it : itb) {
									if (it.hasNext()) {
										return true;
									}
								}
								return false;
							}

							@Override
							public Bindings next() {
								if (!this.hasNext()) {
									return null;
								}
								int i = b.length - 1;
								for (; i >= 0 && !itb[i].hasNext(); i--) {
									itb[i] = bindings[i].iterator();
								}
								if (i < b.length - 1) {
									zb[i] = itb[i].next();
								}
								final Bindings bnew = zb[0].clone();
								for (int j = 1; j < zb.length; j++) {
									bnew.addAll(zb[j]);
									bnew.addAllTriples(zb[j]);
									bnew.addAllPresortingNumbers(zb[j]);
								}
								final Bindings zb2 = itb[b.length - 1].next();
								bnew.addAll(zb2);
								bnew.addAllTriples(zb2);
								bnew.addAllPresortingNumbers(zb2);
								return bnew;
							}

							@Override
							public void remove() {
								throw new UnsupportedOperationException();
							}
						};
						return;
					} else {
						for (int i = 0; i < b.length; i++) {
							if (comp.compare(maximum, b[i]) != 0) {
								if (ssbit[i].hasNext()) {
									if (ssbit[i] instanceof SIPParallelIterator) {
										final Bindings projected = Bindings
												.createNewInstance();
										for (final Variable v : vars) {
											projected.add(v, maximum.get(v));
										}
										do {
											b[i] = ((SIPParallelIterator<Bindings, Bindings>) ssbit[i]).next(projected);
										} while (b[i] != null
												&& comp
														.compare(b[i],
																projected) < 0);
										if (b[i] == null) {
											this.processFurther = false;
										}
									} else {
										b[i] = ssbit[i].next();
									}
								} else {
									this.processFurther = false;
								}
							}
						}
					}
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Bindings next(final Bindings k) {
				if (this.currentBinding != null && this.currentBinding.hasNext()) {
					return this.currentBinding.next();
				}
				if (this.processFurther) {
					for (int i = 0; i < b.length; i++) {
						if (ssbit[i] instanceof SIPParallelIterator) {
							while (comp.compare(b[i], k) < 0) {
								b[i] = ((SIPParallelIterator<Bindings, Bindings>) ssbit[i])
										.next(k);
							}
						} else {
							while (comp.compare(b[i], k) < 0) {
								b[i] = ssbit[i].next();
							}
						}
						if (b[i] == null) {
							this.processFurther = false;
							return null;
						}
					}
					this.computeNextResults();
					if (this.currentBinding != null && this.currentBinding.hasNext()) {
						return this.currentBinding.next();
					}
				}
				return null;
			}
		};
	}

	public static ParallelIterator<Bindings> mergeJoinIterator(
			final Iterator<Bindings>[] ssbit, final Comparator<Bindings> comp,
			final Collection<Variable> vars, final Bindings minimum,
			final Bindings maximum2) {

		for (final Iterator<Bindings> it : ssbit) {
			if (it == null || !it.hasNext()) {
				return new ParallelIterator<Bindings>() {
					@Override
					public boolean hasNext() {
						return false;
					}

					@Override
					public Bindings next() {
						return null;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

					@Override
					public void close() { // noting to close...
					}
				};
			}
		}

		final Bindings[] b = new Bindings[ssbit.length];
		for (int i = 0; i < b.length; i++) {
			if (ssbit[i] instanceof SIPParallelIterator) {
				b[i] = ((SIPParallelIterator<Bindings, Bindings>) ssbit[i]).next(minimum);
			} else {
				b[i] = ssbit[i].next();
			}
		}
		final Collection<Bindings>[] bindings = new Collection[b.length];

		return new SIPParallelIterator<Bindings, Bindings>() {
			boolean processFurther = true;
			Iterator<Bindings> currentBinding = null;

			@Override
			public boolean hasNext() {
				if (this.currentBinding != null && this.currentBinding.hasNext()) {
					return true;
				}
				if (this.processFurther) {
					this.computeNextResults();
					if (this.currentBinding != null && this.currentBinding.hasNext()) {
						return true;
					}
				}
				return false;
			}

			@Override
			public void close() {
				for (final Iterator<Bindings> it : ssbit) {
					if (it instanceof ParallelIterator) {
						((ParallelIterator<Bindings>) it).close();
					}
				}
			}

			@Override
			public Bindings next() {
				if (this.currentBinding != null && this.currentBinding.hasNext()) {
					return this.currentBinding.next();
				}
				if (this.processFurther) {
					this.computeNextResults();
					if (this.currentBinding != null && this.currentBinding.hasNext()) {
						return this.currentBinding.next();
					}
				}
				return null;
			}

			public void computeNextResults() {
				while (this.processFurther == true) {
					boolean equal = true;
					Bindings maximum = b[0];
					for (int i = 1; i < b.length; i++) {
						final int compare = comp.compare(maximum, b[i]);
						if (compare > 0) {
							equal = false;
						} else if (compare < 0) {
							equal = false;
							maximum = b[i];
						}
					}
					if (comp.compare(maximum, maximum2) > 0) {
						this.processFurther = false;
						break;
					}

					if (equal) {
						for (int i = 0; i < b.length; i++) {
							bindings[i] = new LinkedList<Bindings>();
							final Bindings bindingsCurrent = b[i];
							do {
								bindings[i].add(b[i]);
								if (!ssbit[i].hasNext()) {
									this.processFurther = false;
									break;
								}
								b[i] = ssbit[i].next();
							} while (comp.compare(b[i], bindingsCurrent) == 0);
						}

						final Iterator<Bindings>[] itb = new Iterator[b.length];
						final Bindings zb[] = new Bindings[b.length - 1];
						for (int i = 0; i < b.length; i++) {
							itb[i] = bindings[i].iterator();
						}
						for (int i = 0; i < zb.length; i++) {
							zb[i] = itb[i].next();
						}

						this.currentBinding = new Iterator<Bindings>() {

							@Override
							public boolean hasNext() {
								for (final Iterator<Bindings> it : itb) {
									if (it.hasNext()) {
										return true;
									}
								}
								return false;
							}

							@Override
							public Bindings next() {
								if (!this.hasNext()) {
									return null;
								}
								int i = b.length - 1;
								for (; i >= 0 && !itb[i].hasNext(); i--) {
									itb[i] = bindings[i].iterator();
									if (i != b.length - 1) {
										zb[i] = itb[i].next();
									}
								}
								if (i < b.length - 1) {
									zb[i] = itb[i].next();
								}
								final Bindings bnew = zb[0].clone();
								for (int j = 1; j < zb.length; j++) {
									bnew.addAll(zb[j]);
									bnew.addAllTriples(zb[j]);
									bnew.addAllPresortingNumbers(zb[j]);
								}
								final Bindings zb2 = itb[b.length - 1].next();
								bnew.addAll(zb2);
								bnew.addAllTriples(zb2);
								bnew.addAllPresortingNumbers(zb2);
								return bnew;
							}

							@Override
							public void remove() {
								throw new UnsupportedOperationException();
							}
						};
						return;
					} else {
						final Bindings projected = Bindings.createNewInstance();
						for (final Variable v : vars) {
							projected.add(v, maximum.get(v));
						}
						for (int i = 0; i < b.length; i++) {
							if (comp.compare(maximum, b[i]) != 0) {
								if (ssbit[i].hasNext()) {
									if (ssbit[i] instanceof SIPParallelIterator) {
										do {
											b[i] = ((SIPParallelIterator<Bindings, Bindings>) ssbit[i]).next(projected);
										} while (b[i] != null
												&& comp
														.compare(b[i],
																projected) < 0);
										if (b[i] == null) {
											this.processFurther = false;
										}
									} else {
										b[i] = ssbit[i].next();
									}
								} else {
									this.processFurther = false;
								}
							}
						}
					}
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Bindings next(final Bindings k) {
				if (comp.compare(k, maximum2) > 0) {
					this.processFurther = false;
					return null;
				}
				if (this.currentBinding != null && this.currentBinding.hasNext()) {
					return this.currentBinding.next();
				}
				if (this.processFurther) {
					for (int i = 0; i < b.length; i++) {
						if (ssbit[i] instanceof SIPParallelIterator) {
							while (comp.compare(b[i], k) < 0) {
								b[i] = ((SIPParallelIterator<Bindings, Bindings>) ssbit[i]).next(k);
								if (comp.compare(b[i], maximum2) > 0) {
									this.processFurther = false;
									return null;
								}
							}
						} else {
							while (comp.compare(b[i], k) < 0) {
								b[i] = ssbit[i].next();
							}
							if (comp.compare(b[i], maximum2) > 0) {
								this.processFurther = false;
								return null;
							}
						}
						if (b[i] == null) {
							this.processFurther = false;
							return null;
						}
					}
					this.computeNextResults();
					if (this.currentBinding != null && this.currentBinding.hasNext()) {
						return this.currentBinding.next();
					}
				}
				return null;
			}
		};
	}

	public static Iterator<Bindings> mergeOptionalIterator(
			final Iterator<Bindings> ssb1it, final Iterator<Bindings> ssb2it,
			final Comparator<Bindings> comp) {

		if ((ssb1it == null) || (ssb2it == null) || !ssb1it.hasNext()
				|| !ssb2it.hasNext()) {
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
		}

		return new ImmutableIterator<Bindings>() {
			Bindings b1 = ssb1it.next();
			Bindings b2 = ssb2it.next();
			boolean processFurther = true;
			Iterator<Bindings> currentBinding = null;
			Collection<Bindings> bindings1 = null;
			Collection<Bindings> bindings2 = null;

			@Override
			public boolean hasNext() {
				if (this.currentBinding != null && this.currentBinding.hasNext()) {
					return true;
				}
				if (this.processFurther) {
					this.computeNextResults();
					if (this.currentBinding != null && this.currentBinding.hasNext()) {
						return true;
					}
				}
				return false;
			}

			@Override
			public Bindings next() {
				if (this.currentBinding != null && this.currentBinding.hasNext()) {
					return this.currentBinding.next();
				}
				if (this.processFurther) {
					this.computeNextResults();
					if (this.currentBinding != null && this.currentBinding.hasNext()) {
						return this.currentBinding.next();
					}
				}
				return null;
			}

			public void computeNextResults() {
				while (this.processFurther == true) {

					final int compare = comp.compare(this.b1, this.b2);
					if (compare == 0) {

						this.bindings1 = new LinkedList<Bindings>();
						this.bindings2 = new LinkedList<Bindings>();
						Iterator<Bindings> currentBinding2 = null;

						final Bindings bindings = this.b1;
						do {
							this.bindings1.add(this.b1);
							if (!ssb1it.hasNext()) {
								this.b1 = null;
								this.processFurther = false;
								break;
							}
							this.b1 = ssb1it.next();
						} while (comp.compare(this.b1, bindings) == 0);
						do {
							this.bindings2.add(this.b2);
							if (!ssb2it.hasNext()) {
								this.processFurther = false;
								// rest from ssb1:
								currentBinding2 = new Iterator<Bindings>() {
									Bindings zb1 = b1;

									@Override
									public boolean hasNext() {
										return (this.zb1 != null || ssb1it.hasNext());
									}

									@Override
									public Bindings next() {
										if (this.zb1 != null) {
											final Bindings zzb1 = this.zb1;
											this.zb1 = null;
											return zzb1;
										}
										return ssb1it.next();
									}

									@Override
									public void remove() {
										throw new UnsupportedOperationException();
									}
								};
								break;
							}
							this.b2 = ssb2it.next();
						} while (comp.compare(this.b2, bindings) == 0);
						final Iterator<Bindings> currentBinding3 = currentBinding2;
						this.currentBinding = new Iterator<Bindings>() {
							final Iterator<Bindings> itb1 = bindings1.iterator();
							Iterator<Bindings> itb2 = bindings2.iterator();
							Bindings zb1 = this.itb1.next();
							Iterator<Bindings> restFrom1 = currentBinding3;

							@Override
							public boolean hasNext() {
								return this.itb1.hasNext()
										|| this.itb2.hasNext()
										|| (this.restFrom1 != null && this.restFrom1
												.hasNext());
							}

							@Override
							public Bindings next() {
								if (!this.hasNext()) {
									return null;
								}
								if (!(this.itb1.hasNext() || this.itb2.hasNext())) {
									if (this.restFrom1 != null
											&& this.restFrom1.hasNext()) {
										return this.restFrom1.next();
									}
								}
								if (!this.itb2.hasNext()) {
									this.zb1 = this.itb1.next();
									this.itb2 = bindings2.iterator();
								}
								final Bindings bnew = this.zb1.clone();
								final Bindings zb2 = this.itb2.next();
								bnew.addAll(zb2);
								bnew.addAllTriples(zb2);
								bnew.addAllPresortingNumbers(zb2);
								return bnew;
							}

							@Override
							public void remove() {
								throw new UnsupportedOperationException();
							}
						};
						return;
					} else if (compare < 0) {
						if (ssb1it.hasNext()) {
							this.currentBinding = new Iterator<Bindings>() {
								@Override
								public boolean hasNext() {
									return (b1 != null && comp.compare(b1, b2) < 0);
								}

								@Override
								public Bindings next() {
									if (comp.compare(b1, b2) >= 0) {
										return null;
									}
									final Bindings zb1 = b1;
									b1 = ssb1it.next();
									if (b1 == null) {
										processFurther = false;
									}
									return zb1;
								}

								@Override
								public void remove() {
									throw new UnsupportedOperationException();
								}
							};
							return;
						} else {
							this.processFurther = false;
						}
					} else if (compare > 0) {
						if (ssb2it.hasNext()) {
							this.b2 = ssb2it.next();
						} else {
							this.currentBinding = new Iterator<Bindings>() {
								Bindings zb1 = b1;

								@Override
								public boolean hasNext() {
									return (this.zb1 != null || ssb1it.hasNext());
								}

								@Override
								public Bindings next() {
									if (this.zb1 != null) {
										final Bindings zzb1 = this.zb1;
										this.zb1 = null;
										return zzb1;
									}
									return ssb1it.next();
								}

								@Override
								public void remove() {
									throw new UnsupportedOperationException();
								}
							};
							this.processFurther = false;
							return;
						}
					}
				}
			}
		};
	}

	public static QueryResult mergeJoin(final SortedBag<Bindings> ssb1,
			final SortedBag<Bindings> ssb2, final Comparator<Bindings> comp,
			final Collection<Variable> vars) {
		if (ssb1 == null || ssb2 == null || ssb1.size() == 0
				|| ssb2.size() == 0) {
			return null;
		}
		return QueryResult.createInstance(MergeJoin.mergeJoinIterator(ssb1
				.iterator(), ssb2.iterator(), comp, vars));
		// return mergeJoin(ssb1.iterator(), ssb2.iterator(), comp);
	}

	private OptionalResult mergeJoinOptionalResult(
			final SortedBag<Bindings> ssb1, final SortedBag<Bindings> ssb2,
			final Comparator<Bindings> comp) {
		if (ssb1 == null || ssb2 == null || ssb1.size() == 0
				|| ssb2.size() == 0) {
			return null;
		}

		// different from mergeJoin:
		final OptionalResult or = new OptionalResult();
		final QueryResult joinPartnerFromLeftOperand = QueryResult
				.createInstance();

		final QueryResult result = QueryResult.createInstance();
		final Iterator<Bindings> ssb1it = ssb1.iterator();
		final Iterator<Bindings> ssb2it = ssb2.iterator();
		Bindings b1 = ssb1it.next();
		Bindings b2 = ssb2it.next();
		boolean processFurther = true;
		do {
			final int compare = comp.compare(b1, b2);
			if (compare == 0) {

				final Collection<Bindings> bindings1 = new LinkedList<Bindings>();
				final Collection<Bindings> bindings2 = new LinkedList<Bindings>();

				final Bindings bindings = b1;
				int left = 0;
				while (comp.compare(b1, bindings) == 0) {
					bindings1.add(b1);

					// different from mergeJoin:
					joinPartnerFromLeftOperand.add(b1);

					left++;
					if (!ssb1it.hasNext()) {
						processFurther = false;
						break;
					}
					b1 = ssb1it.next();
				}
				int right = 0;
				while (comp.compare(b2, bindings) == 0) {
					bindings2.add(b2);
					right++;
					if (!ssb2it.hasNext()) {
						processFurther = false;
						break;
					}
					b2 = ssb2it.next();
				}
				for (final Bindings zb1 : bindings1) {
					for (final Bindings zb2 : bindings2) {
						final Bindings bnew = zb1.clone();
						bnew.addAll(zb2);
						bnew.addAllTriples(zb2);
						bnew.addAllPresortingNumbers(zb2);
						result.add(bnew);
					}
				}
			} else if (compare < 0) {
				if (ssb1it.hasNext()) {
					b1 = ssb1it.next();
				} else {
					processFurther = false;
				}
			} else if (compare > 0) {
				if (ssb2it.hasNext()) {
					b2 = ssb2it.next();
				} else {
					processFurther = false;
				}
			}
		} while (processFurther == true);

		// different from mergeJoin:
		or.setJoinPartnerFromLeftOperand(joinPartnerFromLeftOperand);
		or.setJoinResult(result);
		return or;
	}

	public static ParallelIterator<Bindings> cartesianProductIterator(
			final QueryResult left, final QueryResult right) {
		if (left == null || right == null) {
			return null;
		}
		final QueryResult smaller;
		final QueryResult larger;
		// if (left.size() < right.size()) {
		// smaller = left;
		// larger = right;
		// } else {
		// smaller = right;
		// larger = left;
		// }
		// assume left result to be bigger in order to avoid one unnecessary
		// materialization when calling left.size()
		smaller = right;
		larger = left;
		return cartesianProductIterator(smaller.oneTimeIterator(), larger);
	}

	public static ParallelIterator<Bindings> cartesianProductIterator(
			final Iterator<Bindings> left, final QueryResult right) {
		if (left == null || right == null) {
			return null;
		}
		return new ParallelIterator<Bindings>() {
			Iterator<Bindings> outer = left;
			Iterator<Bindings> inner = right.iterator();
			Bindings currentOuterElement = this.outer.next();

			@Override
			public boolean hasNext() {
				if (this.currentOuterElement == null) {
					return false;
				}
				if (this.outer.hasNext()) {
					if (!this.inner.hasNext()) {
						this.currentOuterElement = this.outer.next();
						if (this.inner instanceof ParallelIterator) {
							((ParallelIterator<Bindings>) this.inner).close();
						}
						this.inner = right.iterator();
						if (!this.inner.hasNext()) {
							return false;
						}
					}
					return true;
				}
				if (this.inner.hasNext()) {
					return true;
				}
				return false;
			}

			@Override
			public Bindings next() {
				if (!this.inner.hasNext()) {
					if (this.outer.hasNext()) {
						this.currentOuterElement = this.outer.next();
						if (this.inner instanceof ParallelIterator) {
							((ParallelIterator<Bindings>) this.inner).close();
						}
						this.inner = right.iterator();
						if (!this.inner.hasNext()) {
							return null;
						}
					} else {
						return null;
					}
				}
				if (this.currentOuterElement == null) {
					return null;
				}
				final Bindings innerElement = this.inner.next();
				final Bindings bnew = this.currentOuterElement.clone();
				bnew.addAll(innerElement);
				bnew.addAllTriples(innerElement);
				bnew.addAllPresortingNumbers(innerElement);

				return bnew;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close() {
				if (this.inner instanceof ParallelIterator) {
					((ParallelIterator<Bindings>) this.inner).close();
				}
				if (this.outer instanceof ParallelIterator) {
					((ParallelIterator<Bindings>) this.outer).close();
				}
			}

			@Override
			public void finalize() {
				this.close();
			}
		};
	}

	public static ParallelIterator<Bindings> cartesianProductIterator(
			final QueryResult[] operands) {
		for (int i = 0; i < operands.length; i++) {
			if (operands[i] == null) {
				return null;
			}
		}
		final Iterator<Bindings>[] itb = new Iterator[operands.length];
		itb[0] = operands[0].oneTimeIterator();
		for (int i = 1; i < operands.length; i++) {
			itb[i] = operands[i].iterator();
		}
		final Bindings[] currentOuterElements = new Bindings[operands.length - 1];
		for (int i = 0; i < currentOuterElements.length; i++) {
			currentOuterElements[i] = itb[i].next();
		}
		return new ParallelIterator<Bindings>() {

			@Override
			public boolean hasNext() {
				for (int i = 0; i < currentOuterElements.length; i++) {
					if (currentOuterElements[i] == null) {
						return false;
					}
				}
				for (int i = 0; i < itb.length; i++) {
					if (itb[i].hasNext()) {
						return true;
					}
				}
				return false;
			}

			@Override
			public Bindings next() {
				if (!this.hasNext()) {
					return null;
				}
				int i = itb.length - 1;
				for (; i >= 0 && !itb[i].hasNext(); i--) {
					itb[i] = operands[i].iterator();
					if (i != itb.length - 1) {
						currentOuterElements[i] = itb[i].next();
					}
				}
				if (i < itb.length - 1) {
					currentOuterElements[i] = itb[i].next();
				}
				if (!itb[itb.length - 1].hasNext()) {
					itb[itb.length - 1] = operands[itb.length - 1].iterator();
				}
				final Bindings bnew = itb[itb.length - 1].next().clone();
				for (int j = 0; j < currentOuterElements.length; j++) {
					bnew.addAll(currentOuterElements[j]);
					bnew.addAllTriples(currentOuterElements[j]);
					bnew.addAllPresortingNumbers(currentOuterElements[j]);
				}
				return bnew;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close() {
				for (int i = 0; i < itb.length; i++) {
					if (itb[i] instanceof ParallelIterator) {
						((ParallelIterator<Bindings>) itb[i]).close();
					}
				}
			}

			@Override
			public void finalize() {
				this.close();
			}
		};
	}

	@Override
	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		final Iterator<Bindings> itb = queryResult.oneTimeIterator();
		if (operandID == 0) {
			while (itb.hasNext()) {
				this.left.remove(itb.next());
			}
		} else {
			while (itb.hasNext()) {
				this.right.remove(itb.next());
			}
		}
		return null;
	}

	@Override
	public void deleteAll(final int operandID) {
		if (operandID == 0) {
			this.left.clear();
		} else {
			this.right.clear();
		}
	}

	@Override
	protected boolean isPipelineBreaker() {
		return true;
	}

	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		if (this.left != null && this.right != null && this.left.size() > 0
				&& this.right.size() > 0) {
			this.comp.setVariables(this.intersectionVariables);
			final ParallelIterator<Bindings> currentResult = (this.intersectionVariables.size() == 0) ? MergeJoin.cartesianProductIterator(
					QueryResult.createInstance(this.left.iterator()), QueryResult
							.createInstance(this.right.iterator())) : MergeJoin
					.mergeJoinIterator(this.left.iterator(), this.right.iterator(), this.comp, this.intersectionVariables);
			if (currentResult != null && currentResult.hasNext()) {
				final QueryResult result = QueryResult
						.createInstance(new ParallelIterator<Bindings>() {

							int number = 0;

							@Override
							public void close() {
								currentResult.close();
							}

							@Override
							public boolean hasNext() {
								if (!currentResult.hasNext()) {
									MergeJoin.this.realCardinality = this.number;
									this.close();
								}
								return currentResult.hasNext();
							}

							@Override
							public Bindings next() {
								final Bindings b = currentResult.next();
								if (!currentResult.hasNext()) {
									MergeJoin.this.realCardinality = this.number;
									this.close();
								}
								if (b != null) {
									this.number++;
								}
								return b;
							}

							@Override
							public void remove() {
								currentResult.remove();
							}

							@Override
							public void finalize() {
								this.close();
							}
						});

				if (this.succeedingOperators.size() > 1) {
					result.materialize();
				}
				for (final OperatorIDTuple opId : this.succeedingOperators) {
					final QueryResultDebug qrDebug = new QueryResultDebug(
							result, debugstep, this, opId.getOperator(), true);
					((Operator) opId.getOperator()).processAllDebug(qrDebug,
							opId.getId(), debugstep);
				}
			} else {
				if (this.left instanceof DBMergeSortedBag) {
					((DBMergeSortedBag<Bindings>) this.left).release();
				}
				if (this.right instanceof DBMergeSortedBag) {
					((DBMergeSortedBag<Bindings>) this.right).release();
				}
			}

		}
		// left.clear();
		// right.clear();
		return msg;
	}
}
