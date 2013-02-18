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
package lupos.engine.operators.singleinput;

import java.util.Collection;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.BitVector;

public class SIPFilterOperatorIterator extends SIPFilterOperator {

	public SIPFilterOperatorIterator(final Collection<TriplePattern> ctp,
			final Collection<Variable> vars) {
		super(ctp, vars);
	}

	@Override
	public synchronized QueryResult process(final QueryResult res,
			final int operandID) {

		return QueryResult.createInstance(new ParallelIterator<Bindings>() {

			Iterator<Bindings> itb = res.oneTimeIterator();

			public void close() {
				if (itb instanceof ParallelIterator)
					((ParallelIterator) itb).close();
			}

			public boolean hasNext() {
				return itb.hasNext();
			}

			public Bindings next() {
				final Bindings b = itb.next();
				if (b != null) {
					final Iterator<BitVector> ibv = bloomFilters.iterator();
					for (final Variable v : vars) {
						final BitVector bv = ibv.next();
						Literal literal = b.get(v);
						if(literal!=null){
							bv.set((Math.abs(literal.hashCode()) % NUMBEROFBITSFORBLOOMFILTER));
						}
					}
					if (!itb.hasNext()) {
						for (final TriplePattern tp : ctp) {
							// inform triple patterns of bloom filter!
							int i = 0;
							for (final Item item : tp) {
								if (item.isVariable()) {
									final Iterator<BitVector> ibv2 = bloomFilters
											.iterator();
									for (final Variable v : vars) {
										final BitVector bloomFilter = ibv2
												.next();
										if (v.equals(item)) {
											BitVector[] bfa = tp
													.getBloomFilters();
											if (bfa == null) {
												bfa = new BitVector[3];
												bfa[i] = bloomFilter;
											} else {
												if (bfa[i] != null) {
													if (!bfa[i]
															.equals(bloomFilter))
														for (int j = 0; j < bloomFilter
																.size(); j++)
															if (!bloomFilter
																	.get(j))
																bfa[i].clear(j);
												} else
													bfa[i] = bloomFilter;
											}
											tp.setBloomFilters(bfa);
										}
									}
								}
								i++;
							}
						}
					}
				}
				return b;
			}

			public void remove() {
				itb.remove();
			}

			@Override
			public void finalize() {
				close();
			}

		});
	}

}
