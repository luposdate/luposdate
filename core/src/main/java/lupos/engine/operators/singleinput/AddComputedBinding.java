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
package lupos.engine.operators.singleinput;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.ExpressionEvaluation.Helper;
import lupos.misc.debug.DebugStep;
import lupos.sparql1_1.Node;

public class AddComputedBinding extends SingleInputOperator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7826058701554340200L;
	
	public final Map<Variable, Filter> projections = new HashMap<Variable, Filter>();
	protected QueryResult queryResult = null;
	private boolean pipelineBreaker = false;

	public void addProjectionElement(final Variable var, final Node constraint) {
		final Filter filter = new Filter(constraint);
		projections.put(var, filter);
		if (filter.isPipelineBreaker())
			pipelineBreaker = true;
	}

	public Message preProcessMessage(final BoundVariablesMessage msg) {
		for (final Map.Entry<Variable, Filter> entry : projections.entrySet()) {
			msg.getVariables().add(entry.getKey());
		}
		intersectionVariables = new LinkedList<Variable>();
		intersectionVariables.addAll(msg.getVariables());
		unionVariables = new LinkedList<Variable>();
		unionVariables.addAll(intersectionVariables);
		return msg;
	}

	public QueryResult process(final QueryResult bindings, final int operandID) {
		boolean aggregationFunctions = false;
		for (final Filter filter : projections.values()) {
			if (filter.isPipelineBreaker()) {
				aggregationFunctions = true;
				break;
			}
		}
		if (!aggregationFunctions) {
			final Iterator<Bindings> resultIterator = new Iterator<Bindings>() {
				final Iterator<Bindings> bindIt = bindings.oneTimeIterator();
				Bindings next = computeNext();

				public boolean hasNext() {
					return (next != null);
				}

				public Bindings next() {
					final Bindings zNext = next;
					next = computeNext();
					return zNext;
				}

				private Bindings computeNext() {
					while (bindIt.hasNext()) {
						final Bindings bind = bindIt.next();
						try {
							if (bind != null) {
								for (final Map.Entry<Variable, Filter> entry : projections
										.entrySet()) {
									bind.add(entry.getKey(), Helper
											.getLiteral(Filter.staticEvalTree(
													bind, entry.getValue()
															.getNodePointer(),
													null)));
								}
								return bind;
							}
						} catch (final NotBoundException nbe) {
							return bind;
						} catch (final TypeErrorException tee) {
							return bind;
						}
					}
					return null;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			};

			if (resultIterator.hasNext())
				return QueryResult.createInstance(resultIterator);
			else
				return null;
		} else {
			if (queryResult == null) {
				bindings.materialize();
				queryResult = bindings;
			} else
				queryResult.addAll(bindings);
			return null;
		}
	}

	protected QueryResult getQueryResultForAggregatedFilter(final QueryResult queryResult) {
		if (queryResult != null) {
			final List<HashMap<lupos.sparql1_1.Node, Object>> resultsOfAggregationFunctionsList = new LinkedList<HashMap<lupos.sparql1_1.Node, Object>>(); 
			for (final Map.Entry<Variable, Filter> entry : projections
					.entrySet()) {
				final HashMap<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions = new HashMap<lupos.sparql1_1.Node, Object>();
				Filter.computeAggregationFunctions(queryResult, entry.getValue().aggregationFunctions, resultsOfAggregationFunctions, entry.getValue().getUsedEvaluationVisitor());
				resultsOfAggregationFunctionsList.add(resultsOfAggregationFunctions);
			}
			final Iterator<Bindings> resultIterator = new Iterator<Bindings>() {
				final Iterator<Bindings> bindIt = queryResult.oneTimeIterator();

				Bindings next = computeNext();

				public boolean hasNext() {
					return (next != null);
				}

				public Bindings next() {
					final Bindings zNext = next;
					next = computeNext();
					return zNext;
				}

				private Bindings computeNext() {
					while (bindIt.hasNext()) {
						final Bindings bind = bindIt.next();
						try {
							if (bind != null) {
								final Bindings bindNew = bind.clone();
								Iterator<HashMap<lupos.sparql1_1.Node, Object>> resultsOfAggregationFunctionsIterator = resultsOfAggregationFunctionsList.iterator();
								for (final Map.Entry<Variable, Filter> entry : projections
										.entrySet()) {
									HashMap<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions = resultsOfAggregationFunctionsIterator.next();
									bindNew.add(entry.getKey(),
												Helper.getLiteral(Filter.staticEvalTree(
																			bind,
																			entry.getValue().getNodePointer(),
																			resultsOfAggregationFunctions, entry.getValue().getUsedEvaluationVisitor())));
								}
								return bindNew;
							}
						} catch (final NotBoundException nbe) {
							return bind;
						} catch (final TypeErrorException tee) {
							return bind;
						}
					}
					return null;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			};

			if (resultIterator.hasNext())
				return QueryResult.createInstance(resultIterator);
		}
		return null;
	}

	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		final QueryResult qr = getQueryResultForAggregatedFilter(queryResult);
		if (qr != null) {
			if (succeedingOperators.size() > 1)
				qr.materialize();
			for (final OperatorIDTuple opId : succeedingOperators) {
				opId.processAll(qr);
			}
		}
		return msg;
	}

	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		this.deleteAllAtSucceedingOperators();
		preProcessMessage(new EndOfEvaluationMessage());
		return msg;
	}

	public QueryResult deleteQueryResult(final QueryResult queryResultToDelete,
			final int operandID) {
		if (this.queryResult != null)
			this.queryResult.removeAll(queryResultToDelete);
		return queryResultToDelete;
	}

	public void deleteQueryResult(final int operandID) {
		if (this.queryResult != null)
			this.queryResult.release();
		this.queryResult = null;
	}

	public String toString() {
		String s = super.toString();
		boolean comma = false;
		for (final Map.Entry<Variable, Filter> entry : projections.entrySet()) {
			if (comma)
				s += ",";
			comma = true;
			s += " " + entry.getKey() + "=" + entry.getValue().toString();
		}
		return s;
	}

	public String toString(final lupos.rdf.Prefix prefixInstance) {
		String s = super.toString();
		boolean comma = false;
		for (final Map.Entry<Variable, Filter> entry : projections.entrySet()) {
			if (comma)
				s += ",";
			comma = true;
			s += " " + entry.getKey() + "="
					+ entry.getValue().toString(prefixInstance);
		}
		return s;
	}

	public Map<Variable, Filter> getProjections() {
		return projections;
	}

	public boolean isPipelineBreaker() {
		return pipelineBreaker;
	}

	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		this.deleteAllDebugAtSucceedingOperators(debugstep);
		preProcessMessageDebug(new EndOfEvaluationMessage(), debugstep);
		return msg;
	}

	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		final QueryResult qr = getQueryResultForAggregatedFilter(queryResult);
		if (qr != null) {
			if (succeedingOperators.size() > 1)
				qr.materialize();
			for (final OperatorIDTuple opId : succeedingOperators) {
				final QueryResultDebug qrDebug = new QueryResultDebug(qr,
						debugstep, this, opId.getOperator(), true);
				((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
						.getId(), debugstep);
			}
		}
		return msg;
	}
}
