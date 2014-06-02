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
package lupos.engine.operators.multiinput.mergeunion;

import java.util.Collection;
import java.util.Comparator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;

/**
 * This operator computes the union of its operands.
 * The input of this operator must be sorted according to a given sort criterium.
 * The sorted order remains also in the output.
 */
public class MergeUnion extends ComparatorMergeUnion {

	protected final Collection<Variable> sortCriterium;

	/**
	 * @param sortCriterium the sort criterium after which the input of this operator must be sorted. Furthermore, the result of this operator follows this sort criterium.
	 */
	public MergeUnion(final Collection<Variable> sortCriterium) {
		super(new Comparator<Bindings>() {
			@Override
			public int compare(final Bindings o1, final Bindings o2) {
				for (final Variable var : sortCriterium) {
					final Literal l1 = o1.get(var);
					final Literal l2 = o2.get(var);
					if (l1 != null && l2 != null) {
						final int compare = l1
								.compareToNotNecessarilySPARQLSpecificationConform(l2);
						if (compare != 0) {
							return compare;
						}
					} else if (l1 != null) {
						return -1;
					} else if (l2 != null) {
						return 1;
					}
				}
				return 0;
			}

			@Override
			public String toString() {
				return "Comparator on " + sortCriterium;
			}
		});
		this.sortCriterium = sortCriterium;
	}

	/**
	 * @return the sort criterium after which the input of this operator must be sorted. Furthermore, the result of this operator follows this sort criterium.
	 */
	public Collection<Variable> getSortCriterium(){
		return this.sortCriterium;
	}
}
