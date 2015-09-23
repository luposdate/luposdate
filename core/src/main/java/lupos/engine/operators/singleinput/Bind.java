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
package lupos.engine.operators.singleinput;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.filter.Filter;
import lupos.engine.operators.singleinput.filter.expressionevaluation.Helper;
import lupos.misc.debug.DebugStep;
import lupos.misc.util.ImmutableIterator;
public class Bind extends AddComputedBinding {
	/**
	 * serial ID
	 */
	private static final long serialVersionUID = -3021781533018311163L;

	private final Variable var;

	/**
	 * <p>Constructor for Bind.</p>
	 *
	 * @param var a {@link lupos.datastructures.items.Variable} object.
	 */
	public Bind(final Variable var) {
		this.var = var;
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method filters if needed and calls evalTree
	 */
	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		bindings.materialize();
		final List<HashMap<lupos.sparql1_1.Node, Object>> resultsOfAggregationFunctionsList = new LinkedList<HashMap<lupos.sparql1_1.Node, Object>>();
		for (final Map.Entry<Variable, Filter> entry : this.projections.entrySet()) {
			final HashMap<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions = new HashMap<lupos.sparql1_1.Node, Object>();
			Filter.computeAggregationFunctions(bindings, entry.getValue().aggregationFunctions, resultsOfAggregationFunctions, entry.getValue().getUsedEvaluationVisitor());
			resultsOfAggregationFunctionsList.add(resultsOfAggregationFunctions);
		}
		final Iterator<Bindings> resultIterator = new ImmutableIterator<Bindings>() {
			final Iterator<Bindings> bindIt = bindings.oneTimeIterator();

			Bindings next = this.computeNext();

			@Override
			public boolean hasNext() {
				return (this.next != null);
			}

			@Override
			public Bindings next() {
				final Bindings zNext = this.next;
				this.next = this.computeNext();
				return zNext;
			}

			private Bindings computeNext() {
				while (this.bindIt.hasNext()) {
					final Bindings bind = this.bindIt.next();
					try {
						if (bind != null) {
							final Iterator<HashMap<lupos.sparql1_1.Node, Object>> resultsOfAggregationFunctionsIterator = resultsOfAggregationFunctionsList.iterator();
							for (final Map.Entry<Variable, Filter> entry : Bind.this.projections
									.entrySet()) {
								final HashMap<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions = resultsOfAggregationFunctionsIterator.next();
								final Literal boundValue = bind.get(Bind.this.var);
								final Literal toBound = Helper.getLiteral(Filter.staticEvalTree(bind, entry.getValue().getNodePointer(), resultsOfAggregationFunctions, entry.getValue().getUsedEvaluationVisitor()));

								if(boundValue==null){ // variable is not bound => bound with computed value
									final Bindings bindNew = bind.clone();
									bindNew.add(Bind.this.var, toBound);
									return bindNew;
								} else if (Helper.equals(boundValue, toBound)) {
									// the bound value is equal to the computed value => the bindings remains in the solution, otherwise it is omitted!
									return bind;
								}
							}
						}
					} catch (final NotBoundException nbe) {
						return bind;
					} catch (final TypeErrorException tee) {
						return bind;
					}
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};

		if (resultIterator.hasNext()) {
			return QueryResult.createInstance(resultIterator);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessageDebug(final ComputeIntermediateResultMessage msg, final DebugStep debugstep) {
		return msg;
	}
}
