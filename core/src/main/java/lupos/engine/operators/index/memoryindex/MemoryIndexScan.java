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
package lupos.engine.operators.index.memoryindex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Indices.MAP_PATTERN;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.util.ImmutableIterator;

/**
 * Instances of this class are used to process queries by using a special index
 * structure for enhancement.<br>
 * The index structure has to be initialized previously. It is stored in a
 * static way so new instantiations of this class to not yield a change in the
 * original index structure.
 */
public class MemoryIndexScan extends BasicIndexScan {

	private static final long serialVersionUID = 4275399525492937163L;

	/**
	 * Constructor
	 */
	public MemoryIndexScan(final lupos.engine.operators.index.Root root) {
		super(root);
	}

	public MemoryIndexScan(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, final Item rdfGraph, final lupos.engine.operators.index.Root root) {
		super(succeedingOperator, triplePattern, rdfGraph, root);
	}

	public MemoryIndexScan(final List<OperatorIDTuple> succeedingOperators,
			final Collection<TriplePattern> triplePattern, final Item rdfGraph, final lupos.engine.operators.index.Root root) {
		super(succeedingOperators, triplePattern, rdfGraph, root);
	}

	public MemoryIndexScan(final Root root, final Collection<TriplePattern> triplePatterns) {
		super(root, triplePatterns);
	}

	@Override
	public MemoryIndexScan clone() {
		final MemoryIndexScan clone = new MemoryIndexScan(this.succeedingOperators, this.triplePatterns, this.rdfGraph, this.root);
		clone.bindingsFactory = this.bindingsFactory;
		return clone;
	}

	@Override
	public QueryResult join(final Collection<Indices> indicesC){
		try{
			QueryResult queryResult = QueryResult.createInstance();
			queryResult.add(this.bindingsFactory.createInstance());
			// move over the collection of the provided triple patterns
			for (final TriplePattern tp : this.triplePatterns) {

				final QueryResult zQueryResult = queryResult;

				final Iterator<Bindings> itb = new ImmutableIterator<Bindings>() {
					Iterator<Bindings> oldBindings = zQueryResult.oneTimeIterator();
					Bindings currentBindings = null;
					Iterator<Triple> newTriples = null;
					Bindings next = null;

					@Override
					public boolean hasNext() {
						if (this.next != null) {
							return true;
						}
						this.next = this.computeNext();
						return (this.next != null);
					}

					@Override
					public Bindings next() {
						if (this.next != null) {
							final Bindings znext = this.next;
							this.next = null;
							return znext;
						}
						return this.computeNext();
					}

					public Bindings computeNext() {
						while ((this.newTriples == null || !this.newTriples.hasNext()) && this.oldBindings.hasNext()) {
							this.retrieveNewTriples();
						}
						if (this.newTriples == null || !this.newTriples.hasNext()) {
							return null;
						}
						final Triple triple = this.newTriples.next();
						final Bindings cB = this.currentBindings.clone();
						for (int i = 0; i < 3; i++) {
							if (tp.getPos(i).isVariable()) {
								final Literal l = cB.get((Variable) tp.getPos(i));
								if (l != null) {
									if (!triple.getPos(i).equals(l)) {
										return this.computeNext();
									}
								} else {
									cB.add((Variable) tp.getPos(i), triple.getPos(i));
								}
							}
						}
						cB.addTriple(triple);
						return cB;
					}

					private void retrieveNewTriples() {
						this.currentBindings = this.oldBindings.next();

						final StringBuffer key = new StringBuffer();
						int mapPattern = MAP_PATTERN.NONE.ordinal();

						// compute a key which is as restrictive as possible
						// to cut down the amount of triple pattern which
						// have
						// to be processed to produce the result
						if (MemoryIndexScan.this.computeKey4Maps(this.currentBindings, key, tp.getPos(0))) {
							mapPattern += MAP_PATTERN.SMAP.ordinal();
						}
						if (MemoryIndexScan.this.computeKey4Maps(this.currentBindings, key, tp.getPos(1))) {
							mapPattern += MAP_PATTERN.PMAP.ordinal();
						}
						if (MemoryIndexScan.this.computeKey4Maps(this.currentBindings, key, tp.getPos(2))) {
							mapPattern += MAP_PATTERN.OMAP.ordinal();
						}
						final Collection<Triple> tec = MemoryIndexScan.this.getFromMap(MAP_PATTERN.values()[mapPattern], key.toString(), indicesC);
						if (tec == null) {
							this.newTriples = null;
						} else {
							this.newTriples = tec.iterator();
						}
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

	private Collection<Triple> getFromMap(final MAP_PATTERN mapPattern, final String keyString, final Collection<Indices> indicesG) {
		if(indicesG.size()==1){
			return this.getFromMap(mapPattern, keyString, indicesG.iterator().next());
		}
		@SuppressWarnings("unchecked")
		final Collection<Triple>[] triples = new Collection[indicesG.size()];
		int i = 0;
		int size = 0;
		for(final Indices indices: indicesG){
			triples[i] = ((SevenMemoryIndices) indices).getFromMap(mapPattern, keyString);
			size+=triples[i].size();
			i++;
		}
		// could be optimized by returning a collection which works directly with above given array triples
		// must implement an own class for this purpose...
		final ArrayList<Triple> result = new ArrayList<Triple>(size);
		for(final Collection<Triple> tripleCol: triples){
			for(final Triple t: tripleCol){
				result.add(t);
			}
		}
		return result;
	}


	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
		final QueryResult queryResult = QueryResult.createInstance();
		queryResult.add(bindings); // empty since no bindings exist yet
		return this.join(indices, queryResult);
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
			for (final TriplePattern tp : this.triplePatterns) {

				final QueryResult zQueryResult = queryResult;

				final Iterator<Bindings> itb = new ImmutableIterator<Bindings>() {
					Iterator<Bindings> oldBindings = zQueryResult.oneTimeIterator();
					Bindings currentBindings = null;
					Iterator<Triple> newTriples = null;
					Bindings next = null;

					@Override
					public boolean hasNext() {
						if (this.next != null) {
							return true;
						}
						this.next = this.computeNext();
						return (this.next != null);
					}

					@Override
					public Bindings next() {
						if (this.next != null) {
							final Bindings znext = this.next;
							this.next = null;
							return znext;
						}
						return this.computeNext();
					}

					public Bindings computeNext() {
						while ((this.newTriples == null || !this.newTriples.hasNext()) && this.oldBindings.hasNext()) {
							this.retrieveNewTriples();
						}
						if (this.newTriples == null || !this.newTriples.hasNext()) {
							return null;
						}
						final Triple triple = this.newTriples.next();
						final Bindings cB = this.currentBindings.clone();
						for (int i = 0; i < 3; i++) {
							if (tp.getPos(i).isVariable()) {
								final Literal l = cB.get((Variable) tp.getPos(i));
								if (l != null) {
									if (!triple.getPos(i).equals(l)) {
										return this.computeNext();
									}
								} else {
									cB.add((Variable) tp.getPos(i), triple.getPos(i));
								}
							}
						}
						cB.addTriple(triple);
						return cB;
					}

					private void retrieveNewTriples() {
						this.currentBindings = this.oldBindings.next();

						final StringBuffer key = new StringBuffer();
						int mapPattern = MAP_PATTERN.NONE.ordinal();

						// compute a key which is as restrictive as possible
						// to cut down the amount of triple pattern which
						// have
						// to be processed to produce the result
						if (MemoryIndexScan.this.computeKey4Maps(this.currentBindings, key, tp.getPos(0))) {
							mapPattern += MAP_PATTERN.SMAP.ordinal();
						}
						if (MemoryIndexScan.this.computeKey4Maps(this.currentBindings, key, tp.getPos(1))) {
							mapPattern += MAP_PATTERN.PMAP.ordinal();
						}
						if (MemoryIndexScan.this.computeKey4Maps(this.currentBindings, key, tp.getPos(2))) {
							mapPattern += MAP_PATTERN.OMAP.ordinal();
						}
						final Collection<Triple> tec = MemoryIndexScan.this.getFromMap(MAP_PATTERN.values()[mapPattern], key.toString(), indices);
						if (tec == null) {
							this.newTriples = null;
						} else {
							this.newTriples = tec.iterator();
						}
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

	@Override
	public String toString() {
		return "Memory " + super.toString();
	}

	@Override
	public String toString(final lupos.rdf.Prefix prefixInstance) {
		return "Memory " + super.toString(prefixInstance);
	}
}
