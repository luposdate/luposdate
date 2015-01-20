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
 */
package lupos.engine.operators.singleinput.sort.fastsort;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.datastructures.dbmergesortedds.tosort.ToSort.TOSORT;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class FastSortLazyLiteral extends FastSort {

	public FastSortLazyLiteral(final BasicOperator root,
			final Collection<TriplePattern> triplePatterns,
			final Collection<Variable> sortCriterium) {
		super(triplePatterns, sortCriterium);
	}

	private BasicIndexScan findIndex(final BasicOperator root,
			final TriplePattern triplePattern) {
		for (final OperatorIDTuple opID : root.getSucceedingOperators()) {
			final BasicOperator bo = opID.getOperator();
			if (bo instanceof BasicIndexScan) {
				if (((BasicIndexScan) bo).getTriplePattern()
						.contains(triplePattern))
					return (BasicIndexScan) bo;
			}
		}
		return null;
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		
		SortConfiguration sortConfiguration = new SortConfiguration();
		sortConfiguration.useExternalMergeSort();

		final DBMergeSortedBag<Bindings> bag = new DBMergeSortedBag<Bindings>(
				sortConfiguration, new Comparator<Bindings>() {
					public int compare(final Bindings arg0, final Bindings arg1) {
						for (final Variable var : sortCriterium) {
							final Literal l1 = arg0.get(var);
							final Literal l2 = arg1.get(var);
							if (l1 != null && l2 != null) {
								final int compare = l1
										.compareToNotNecessarilySPARQLSpecificationConform(l2);
								if (compare != 0)
									return compare;
							} else if (l1 != null)
								return -1;
							else if (l2 != null)
								return 1;
						}
						return 0;
					}
				}, Bindings.class);

		final Iterator<Bindings> it = bindings.oneTimeIterator();

		while (it.hasNext()) {
			bag.add(it.next());
		}

		return QueryResult.createInstance(bag.iterator());
	}
}
