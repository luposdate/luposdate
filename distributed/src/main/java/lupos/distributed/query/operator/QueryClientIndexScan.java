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
package lupos.distributed.query.operator;

import java.util.Collection;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.IteratorQueryResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class represents an index scan operator for the distributed query evaluators...
 */
public class QueryClientIndexScan extends BasicIndexScan {

	public QueryClientIndexScan(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePatterns, final Item rdfGraph, final Root root) {
		super(succeedingOperator, triplePatterns, rdfGraph, root);
	}

	public QueryClientIndexScan(final Root root, final Collection<TriplePattern> triplePatterns) {
		super(root, triplePatterns);
	}

	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
		// a fetch as needed distributed join strategy is implemented
		final QueryClientIndices queryClientIndices = (QueryClientIndices) indices;
		QueryResult result = QueryResult.createInstance();
		result.add(bindings);

		for (final TriplePattern pattern : this.triplePatterns) {
			final QueryResult iResult = result;
			result = QueryResult.createInstance();

			for(final Bindings b: iResult){
				final TriplePattern tpWithReplacedVariables = this.determineTriplePatternToEvaluate(pattern, b);
				final QueryResult resultOfTP = queryClientIndices.evaluateTriplePattern(tpWithReplacedVariables);
				if(resultOfTP!=null){
					result.addAll(this.addBindings(b, resultOfTP));
				}
			}
		}
		return result;
	}

	/**
	 * Adds all bound values of variables in a given bindings to a query result
	 * @param bindings the given bindings
	 * @param queryResult the queryResult to be added with bindings
	 * @return the query result each bindings of which is added with the given bindings
	 */
	private IteratorQueryResult addBindings(final Bindings bindings, final QueryResult queryResult) {
		return new IteratorQueryResult(new Iterator<Bindings>(){

			Iterator<Bindings> it = queryResult.oneTimeIterator();

			@Override
			public boolean hasNext() {
				return this.it.hasNext();
			}

			@Override
			public Bindings next() {
				if(this.it.hasNext()){
					final Bindings b = this.it.next().clone();
					b.addAll(bindings);
					return b;
				} else {
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		});
	}

	private TriplePattern determineTriplePatternToEvaluate(final TriplePattern pattern, final Bindings b) {
		return new TriplePattern(this.getItem(pattern.getPos(0), b), this.getItem(pattern.getPos(1), b), this.getItem(pattern.getPos(2), b));
	}

	/**
	 * Replaces variables with their values in a given bindings, otherwise return the original literal.
	 * @param item the literal or variable
	 * @param bindings the intermediate solution
	 * @return the value in bindings if item is a variable and bound in bindings, otherwise just item
	 */
	private Item getItem(final Item item, final Bindings bindings){
		if(item.isVariable()){
			final Literal literal = bindings.get((Variable)item);
			if(literal!=null){
				return literal;
			}
		}
		return item;
	}
}
