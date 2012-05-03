package lupos.engine.operators.singleinput;

import java.util.Collection;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
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
						bv
								.set((Math.abs(b.get(v).hashCode()) % NUMBEROFBITSFORBLOOMFILTER));
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
