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
package lupos.engine.operators.singleinput.modifiers.distinct;

import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.util.ImmutableIterator;

/**
 * This class is developed to reduce the space consumption of NonBlockingDistinct operators in cycles below an index scan operator.
 *
 * This class first let all bindings pass which are generated from the index scan operator (operands with ID 0, the operator graph should be prepared in this way before using this operator).
 *
 * If a bindings is retrieved from within the cycle (ID>0):
 * This class first looks into a given index if a bindings is generated from the mentioned index scan operator.
 * If yes, then it does not pass this operator, otherwise it is checked whether or not it has already passed by using the functionality of the NonBlockingDistinct operator.
 *
 * @author groppe
 * @version $Id: $Id
 */
public abstract class NonBlockingDistinctWithIndexAccess extends NonBlockingDistinct {

	protected BasicIndexScan basicIndex;

	/**
	 * <p>Constructor for NonBlockingDistinctWithIndexAccess.</p>
	 *
	 * @param setOfBindings a {@link java.util.Set} object.
	 * @param basicIndex a {@link lupos.engine.operators.index.BasicIndexScan} object.
	 */
	public NonBlockingDistinctWithIndexAccess(final Set<Bindings> setOfBindings, final BasicIndexScan basicIndex) {
		super(setOfBindings);
		this.basicIndex = basicIndex;
	}

	/**
	 * <p>Constructor for NonBlockingDistinctWithIndexAccess.</p>
	 *
	 * @param setOfBindings a {@link java.util.Set} object.
	 */
	public NonBlockingDistinctWithIndexAccess(final Set<Bindings> setOfBindings) {
		super(setOfBindings);
	}

	/**
	 * <p>Getter for the field <code>basicIndex</code>.</p>
	 *
	 * @return a {@link lupos.engine.operators.index.BasicIndexScan} object.
	 */
	public BasicIndexScan getBasicIndex() {
		return this.basicIndex;
	}

	/**
	 * <p>Setter for the field <code>basicIndex</code>.</p>
	 *
	 * @param basicIndex a {@link lupos.engine.operators.index.BasicIndexScan} object.
	 */
	public void setBasicIndex(final BasicIndexScan basicIndex) {
		this.basicIndex = basicIndex;
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final QueryResult _bindings, final int operandID) {
		if(operandID==0){
			return _bindings;
		}
		final Set<Variable> vars = this.basicIndex.getVarsInTriplePatterns();

		final Iterator<Bindings> itb = _bindings.oneTimeIterator();
		if (!itb.hasNext()) {
			return null;
		} else {
			return QueryResult.createInstance(new ImmutableIterator<Bindings>() {
				Bindings next = null;

				@Override
				public boolean hasNext() {
					if (this.next != null) {
						return true;
					}
					if (itb.hasNext()) {
						this.next = this.next();
						if (this.next != null) {
							return true;
						}
					}
					return false;
				}

				@Override
				public Bindings next() {
					if (this.next != null) {
						final Bindings znext = this.next;
						this.next = null;
						return znext;
					}
					while (itb.hasNext()) {
						final Bindings b = itb.next();

						// first check if the same variables are bound in bindings and in the index scan operator!
						final Set<Variable> varsInBindings = b.getVariableSet();
						if(varsInBindings.containsAll(vars) && vars.containsAll(varsInBindings)){
							if(!NonBlockingDistinctWithIndexAccess.this.isGeneratedFromIndexScanOperator(b)){
								if (!NonBlockingDistinctWithIndexAccess.this.bindings.contains(b)) {
									NonBlockingDistinctWithIndexAccess.this.bindings.add(b);
									return b;
								}
							}
						} else {
							if (!NonBlockingDistinctWithIndexAccess.this.bindings.contains(b)) {
								NonBlockingDistinctWithIndexAccess.this.bindings.add(b);
								return b;
							}
						}
					}
					return null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			});
		}
	}

	/**
	 * <p>isGeneratedFromIndexScanOperator.</p>
	 *
	 * @param bindingsToCheck a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a boolean.
	 */
	protected boolean isGeneratedFromIndexScanOperator(final Bindings bindingsToCheck){
		for(final TriplePattern tp: this.basicIndex.getTriplePattern()){
			final Literal[] literals = new Literal[3];
			for(int i=0; i<3; i++){
				final Item item = tp.getPos(i);
				if(item.isVariable()){
					final Variable var = (Variable) item;
					final Literal literal = bindingsToCheck.get(var);
					if(literal == null){
						return false;
					}
					literals[i] = literal;
				} else {
					literals[i] = (Literal) item;
				}
			}
			final Triple t = new Triple(literals);
			boolean flag = false;
			for(final Indices indices: this.basicIndex.getRoot().dataset.getDefaultGraphIndices()){
				flag = indices.contains(t);
				if(flag){
					break;
				}
			}
			if(!flag){
				return false;
			}
		}
		return true;
	}
}
