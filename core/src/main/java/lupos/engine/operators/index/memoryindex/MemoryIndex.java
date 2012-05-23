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
package lupos.engine.operators.index.memoryindex;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Indices.MAP_PATTERN;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * Instances of this class are used to process queries by using a special index
 * structure for enhancement.<br>
 * The index structure has to be initialized previously. It is stored in a
 * static way so new instantiations of this class to not yield a change in the
 * original index structure.
 */
public class MemoryIndex extends BasicIndex {

	private static final long serialVersionUID = 4275399525492937163L;

	/**
	 * Constructor
	 */
	public MemoryIndex(final lupos.engine.operators.index.IndexCollection indexCollection) {
		super(indexCollection);
	}

	public MemoryIndex(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, final Item rdfGraph, final lupos.engine.operators.index.IndexCollection indexCollection) {
		super(succeedingOperator, triplePattern, rdfGraph, indexCollection);
	}

	public MemoryIndex(final List<OperatorIDTuple> succeedingOperators,
			final Collection<TriplePattern> triplePattern, final Item rdfGraph, final lupos.engine.operators.index.IndexCollection indexCollection) {
		super(succeedingOperators, triplePattern, rdfGraph, indexCollection);
	}

	@Override
	public BasicIndex clone() {
		return new MemoryIndex(this.succeedingOperators,
				this.triplePatterns, this.rdfGraph, this.indexCollection);
	}

	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
		final QueryResult queryResult = QueryResult.createInstance();
		queryResult.add(bindings); // empty since no bindings exist yet
		return join(indices, queryResult);
	}

	/**
	 * Returns the result of a join operation using triple patterns and results
	 * of previous queries
	 * 
	 * @param tps
	 *            a collection of triple patterns
	 * @param queryResult
	 *            the result of previous queries
	 * @return the result of a join operation using triple patterns and results
	 *         of previous queries
	 */
	protected QueryResult join(final Indices indices, QueryResult queryResult) {
		try {

			// move over the collection of the provided triple patterns
			for (final TriplePattern tp : triplePatterns) {

				final QueryResult zQueryResult = queryResult;

				final Iterator<Bindings> itb = new Iterator<Bindings>() {
					Iterator<Bindings> oldBindings = zQueryResult
					.oneTimeIterator();
					Bindings currentBindings = null;
					Iterator<Triple> newTriples = null;
					Bindings next = null;

					public boolean hasNext() {
						if (next != null)
							return true;
						next = computeNext();
						return (next != null);
					}

					public Bindings next() {
						if (next != null) {
							final Bindings znext = next;
							next = null;
							return znext;
						}
						return computeNext();
					}

					public Bindings computeNext() {
						while ((newTriples == null || !newTriples.hasNext())
								&& oldBindings.hasNext()) {
							retrieveNewTriples();
						}
						if (newTriples == null || !newTriples.hasNext())
							return null;
						final Triple triple = newTriples.next();
						final Bindings cB = currentBindings.clone();
						for (int i = 0; i < 3; i++) {
							if (tp.getPos(i).isVariable()) {
								final Literal l = cB.get((Variable) tp
										.getPos(i));
								if (l != null) {
									if (!triple.getPos(i).equals(l)) {
										return computeNext();
									}
								} else
									cB.add((Variable) tp.getPos(i), triple
											.getPos(i));
							}
						}
						cB.addTriple(triple);
						return cB;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}

					private void retrieveNewTriples() {
						currentBindings = oldBindings.next();

						final StringBuffer key = new StringBuffer();
						int mapPattern = MAP_PATTERN.NONE.ordinal();

						// compute a key which is as restrictive as possible
						// to cut down the amount of triple pattern which
						// have
						// to be processed to produce the result
						if (computeKey4Maps(currentBindings, key, tp.getPos(0))) {
							mapPattern += MAP_PATTERN.SMAP.ordinal();
						}
						if (computeKey4Maps(currentBindings, key, tp.getPos(1))) {
							mapPattern += MAP_PATTERN.PMAP.ordinal();
						}
						if (computeKey4Maps(currentBindings, key, tp.getPos(2))) {
							mapPattern += MAP_PATTERN.OMAP.ordinal();
						}
						final Collection<Triple> tec = getFromMap(MAP_PATTERN
								.values()[mapPattern], key.toString(), indices);
						if (tec == null)
							newTriples = null;
						else
							newTriples = tec.iterator();
					}
				};

				// use a "fresh" object to gather the result of the join
				// operation
				// when using the current triple pattern and the previous query
				// results
				final QueryResult qresult = QueryResult.createInstance(itb);

				// replace the previous query results with the new ones
				queryResult.release();
				queryResult = qresult;
			}
			return queryResult;
		} catch (final Exception e) {
			System.err.println("Error while joining triple patterns"+e);
			return null;
		}
	}

	private Collection<Triple> getFromMap(final MAP_PATTERN mapPattern,
			final String keyString, final Indices indicesG) {
		return ((SevenMemoryIndices) indicesG).getFromMap(mapPattern, keyString);
	}

	/**
	 * Computes and alters the key, respectively used to query the maps.<br>
	 * To do this the information about the currently bound variables and the
	 * information if the item is used.
	 * 
	 * @param bindings
	 *            - the currently available bindings
	 * @param key
	 *            the currently computed key which is to be extended
	 * @param item
	 *            the item used to extend the key
	 * @return <code>true</code> if the key was extended.i
	 */
	private boolean computeKey4Maps(final Bindings bindings,
			final StringBuffer key, final Item item) {

		// if the item is a variable, check if this variable
		// is bound
		if (item.isVariable()) {

			final Literal literal = bindings.get((Variable) item);

			// if the variable is bound, use the literal for the key used
			// later on to query the map
			if (literal != null) {
				key.append(literal.toString());
				return true;
			}

		}

		// otherwise if it is a literal which is no blank node
		// use the literal for the key used later on to query the map
		else if (!((Literal) item).isBlank()) {
			key.append(((Literal) item).toString());
			return true;
		}
		return false;
	}

	public void optimizeJoinOrderAccordingToMostRestrictionsAndLeastEntries(
			final Dataset dataset) {
		if (triplePatterns == null)
			return;
		final HashSet<String> usedVariables = new HashSet<String>();
		final Collection<TriplePattern> remainingTP = new LinkedList<TriplePattern>();
		remainingTP.addAll(triplePatterns);
		final Collection<TriplePattern> newTriplePattern = new LinkedList<TriplePattern>();
		while (remainingTP.size() > 0) {
			int minOpenPositions = 4;
			int minEntries = 0;
			TriplePattern best = null;
			for (final TriplePattern tp : remainingTP) {
				final HashSet<String> v = tp.getVariableNames();
				v.retainAll(usedVariables);
				final int openPositions = tp.getVariableNames().size()
				- v.size();
				if (openPositions < minOpenPositions) {
					final LinkedList<TriplePattern> tpc = new LinkedList<TriplePattern>();
					tpc.add(tp);
					final Collection<TriplePattern> zTP = triplePatterns;
					triplePatterns = tpc;
					final QueryResult queryResult = join(dataset);
					triplePatterns = zTP;
					if (queryResult != null)
						minEntries = queryResult.size();
					minOpenPositions = openPositions;
					best = tp;
				} else if (openPositions == minOpenPositions) {
					final LinkedList<TriplePattern> tpc = new LinkedList<TriplePattern>();
					tpc.add(tp);
					final Collection<TriplePattern> zTP = triplePatterns;
					triplePatterns = tpc;
					final QueryResult queryResult = join(dataset);
					triplePatterns = zTP;
					final int entries = (queryResult == null) ? 0 : queryResult
							.size();
					if (entries < minEntries) {
						minEntries = entries;
						best = tp;
					}
				}
			}
			if (best != null) {
				usedVariables.addAll(best.getVariableNames());
				newTriplePattern.add(best);
				remainingTP.remove(best);
			}
		}
		triplePatterns = newTriplePattern;
	}

	public void optimizeJoinOrderAccordingToLeastEntries(final Dataset dataset) {
		if (triplePatterns == null)
			return;
		final Collection<TriplePattern> remainingTP = new LinkedList<TriplePattern>();
		remainingTP.addAll(triplePatterns);
		final Collection<TriplePattern> newTriplePattern = new LinkedList<TriplePattern>();
		while (remainingTP.size() > 0) {
			int minEntries = -1;
			TriplePattern best = null;
			for (final TriplePattern tp : remainingTP) {
				final LinkedList<TriplePattern> tpc = new LinkedList<TriplePattern>();
				tpc.add(tp);
				final Collection<TriplePattern> zTP = triplePatterns;
				triplePatterns = tpc;
				final QueryResult queryResult = join(dataset);
				triplePatterns = zTP;
				final int entries = queryResult.size();
				if (minEntries == -1 || entries < minEntries) {
					minEntries = entries;
					best = tp;
				}
			}
			newTriplePattern.add(best);
			remainingTP.remove(best);
		}
		triplePatterns = newTriplePattern;
	}

	@Override
	public String toString() {
		return "Memory " + super.toString();
	}
	
	@Override
	public String toString(lupos.rdf.Prefix prefixInstance) {
		return "Memory " + super.toString(prefixInstance);
	}
}
