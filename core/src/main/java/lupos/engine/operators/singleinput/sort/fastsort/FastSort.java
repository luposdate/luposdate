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
package lupos.engine.operators.singleinput.sort.fastsort;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class sorts its input according to presorting numbers or according to
 * the code of LazyLiterals.
 * 
 * It is used for the optimizations MergeJoinSort, MergeJoinSortSimple and
 * MergeJoinSortLazyLiteral
 * 
 */
public abstract class FastSort extends SingleInputOperator {

	protected List<TriplePattern> triplePatterns;
	protected Collection<Variable> sortCriterium;

	public FastSort(final Collection<TriplePattern> triplePatterns,
			final Collection<Variable> sortCriterium) {
		super();
		this.triplePatterns = new LinkedList<TriplePattern>();
		for (final TriplePattern tp : triplePatterns) {
			for (final Variable v : sortCriterium) {
				if (tp.getVariables().contains(v)) {
					this.triplePatterns.add(tp);
					break;
				}
			}
		}
		this.sortCriterium = sortCriterium;
	}

	public Collection<Variable> getSortCriterium() {
		return sortCriterium;
	}

	@Override
	public String toString() {
		return super.toString() + " on " + this.sortCriterium;
	}

	public static FastSort createInstance(final BasicOperator root,
			final Collection<TriplePattern> triplePatterns,
			final Collection<Variable> sortCriterium) {
		if (LiteralFactory.getMapType() == LiteralFactory.MapType.LAZYLITERAL
				|| LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
			return new FastSortLazyLiteral(root, triplePatterns, sortCriterium);
		} else {
			System.err.println("FastSort: Not supported literal type!");
			return null;
//			return new FastSortPresortingNumbers(triplePatterns, sortCriterium,
//					presortion);
		}
	}
}
