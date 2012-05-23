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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.IndexCollection;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.engine.operators.singleinput.ExpressionEvaluation.EvaluationVisitor;
import lupos.engine.operators.singleinput.ExpressionEvaluation.EvaluationVisitorImplementation;
import lupos.engine.operators.singleinput.ExpressionEvaluation.Helper;
import lupos.misc.debug.DebugStep;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumperShort;
import lupos.sparql1_1.ASTAggregation;
import lupos.sparql1_1.ASTFilterConstraint;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;

/**
 * This Class implements the 'FILTER(...)' expression used by SPARQL queries
 */
public class Filter extends SingleInputOperator {

	private static final long serialVersionUID = 2118104495454058506L;
	private int cardinality = -1;
	private lupos.sparql1_1.Node np;
	private Set<Variable> usedVariables = new HashSet<Variable>();

	protected List<List<lupos.sparql1_1.Node>> aggregationFunctions = null;
	protected QueryResult queryResult = null;

	public static Class<? extends EvaluationVisitor<Map<Node, Object>, Object>> evaluationVisitorClass = EvaluationVisitorImplementation.class;
	private final EvaluationVisitor<Map<Node, Object>, Object> evaluationVisitor;

	public Filter(final lupos.sparql1_1.Node node) {
		evaluationVisitor = getEvaluationVisitor();
		this.setNodePointer(node);
	}

	public Filter() {
		evaluationVisitor = getEvaluationVisitor();
		np = null;
	}

	public Filter(final String filter) throws ParseException {
		evaluationVisitor = getEvaluationVisitor();
		ASTFilterConstraint ASTfilter;
		ASTfilter = (ASTFilterConstraint) SPARQL1_1Parser.parseFilter(filter);
		this.setNodePointer(ASTfilter);
	}

	private static EvaluationVisitor<Map<Node, Object>, Object> getEvaluationVisitor() {
		try {
			return evaluationVisitorClass.newInstance();
		} catch (InstantiationException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return new EvaluationVisitorImplementation();
	}

	public void setNodePointer(lupos.sparql1_1.Node node) {
		np = node;
		computeUsedVariables(np);
		aggregationFunctions = computeAggegrationFunctions(this.np);
	}

	private void computeUsedVariables(final lupos.sparql1_1.Node n) {
		if (n == null)
			return;
		if (n instanceof lupos.sparql1_1.ASTVar) {
			try {
				usedVariables.add(new Variable(((lupos.sparql1_1.ASTVar) n)
						.getName().toString()));
			} catch (final Exception e) {
				System.err.println(e);
				return;
			}

		}
		for (int i = 0; i < n.jjtGetNumChildren(); i++) {
			computeUsedVariables(n.jjtGetChild(i));
		}
	}

	private List<List<lupos.sparql1_1.Node>> computeAggegrationFunctions(
			final lupos.sparql1_1.Node n) {
		if (n == null)
			return null;
		List<List<lupos.sparql1_1.Node>> result = null;
		for (int i = 0; i < n.jjtGetNumChildren(); i++) {
			final List<List<lupos.sparql1_1.Node>> interResult = computeAggegrationFunctions(n
					.jjtGetChild(i));
			if (interResult != null) {
				if (result == null)
					result = interResult;
				else {
					if (result.size() > interResult.size()) {
						final Iterator<List<lupos.sparql1_1.Node>> resultElemIt = result
								.iterator();
						for (final List<lupos.sparql1_1.Node> listToAdd : interResult) {
							resultElemIt.next().addAll(listToAdd);
						}
					} else {
						final Iterator<List<lupos.sparql1_1.Node>> resultElemIt = interResult
								.iterator();
						for (final List<lupos.sparql1_1.Node> listToAdd : result) {
							resultElemIt.next().addAll(listToAdd);
						}
						result = interResult;
					}
				}
			}
		}
		if (isAggregationFunction(n)) {
			if (result == null)
				result = new LinkedList<List<lupos.sparql1_1.Node>>();
			final List<lupos.sparql1_1.Node> currentLevel = new LinkedList<lupos.sparql1_1.Node>();
			currentLevel.add(n);
			result.add(currentLevel);
		}
		return result;
	}

	public Set<Variable> getUsedVariables() {
		return usedVariables;
	}

	/**
	 * 
	 * @return ASTFilterConstraint: the node this instance belongs to
	 */
	public lupos.sparql1_1.Node getNodePointer() {
		return np;
	}

	/**
	 * This Method processes the incoming Bindings-sequence and forwards the
	 * Bindings which meet the demands
	 * 
	 * @param List
	 *            : Bindings to evaluate.
	 * @return List: Bindings which fitted. If there is no such binding NULL
	 *         will be returned
	 */

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (aggregationFunctions == null) {
			final Iterator<Bindings> resultIterator = new Iterator<Bindings>() {
				final Iterator<Bindings> bindIt = bindings.oneTimeIterator();
				int number = 0;
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
								final Object o = evalTree(bind, np
										.jjtGetChild(0),
										null);
								if (Helper.booleanEffectiveValue(o)) {
									number++;
									return bind;
								}
							}
						} catch (final NotBoundException nbe) {
							// log.error("Variable has not beeen bound:" + nbe);
							// return null;
						} catch (final TypeErrorException tee) {
							// log.error("type error:" + tee);
							// return null;
						}
					}
					cardinality = number;
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
			// forwarded bindings, return value
		} else {
			if (queryResult == null) {
				bindings.materialize();
				queryResult = bindings;
			} else
				queryResult.add(bindings);
			return null;
		}
	}

	private static boolean isConstant(final lupos.sparql1_1.Node n) {
		if (n == null)
			return true;
		if (n instanceof ASTVar)
			return false;
		if (n.getChildren() == null)
			return true;
		for (final lupos.sparql1_1.Node node : n.getChildren()) {
			if (!isAggregationFunction(node) && !isConstant(node))
				return false;
		}
		return true;
	}

	private static boolean isAggregationFunction(final lupos.sparql1_1.Node n) {
		return (n instanceof ASTAggregation);
	}

	private static void processAggregationFunction(final QueryResult queryResult, final lupos.sparql1_1.Node n, final HashMap<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions, final EvaluationVisitor<Map<Node, Object>, Object> evaluationVisitor) {
		boolean childAdded = false;
		if (isAggregationFunction(n)) {
			ASTAggregation aggregation = (ASTAggregation) n;
			Object result = null;
			
			if (n.jjtGetNumChildren()>0 && isConstant(n.jjtGetChild(0))) {
				try {
					final lupos.sparql1_1.Node node=n.jjtGetChild(0);
					final Object operand = Filter.staticEvalTree(null, node,
							resultsOfAggregationFunctions, evaluationVisitor);
					if (childAdded) {
						n.clearChildren();
					}
					Iterator<Object> values = new Iterator<Object>() {
						Object next = operand;

						@Override
						public boolean hasNext() {
							return next != null;
						}

						@Override
						public Object next() {
							Object znext = next;
							next = null;
							return znext;
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
					result = aggregation.applyAggregation(
							evaluationVisitor, values);
				} catch (final NotBoundException e) {
					System.err.println(e);
					e.printStackTrace();
				} catch (final TypeErrorException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			} else {
				
				Iterator<? extends Object> values = (n.jjtGetNumChildren()==0)?queryResult.iterator():new Iterator<Object>() {
					final lupos.sparql1_1.Node node=n.jjtGetChild(0);
					Iterator<Bindings> iterator = queryResult.iterator();
					Object next = null;

					@Override
					public boolean hasNext() {
						if (next != null)
							return true;
						next = next();
						return (next != null);
					}

					@Override
					public Object next() {
						if (next != null) {
							Object znext = next;
							next = null;
							return znext;
						}
						while (iterator.hasNext()) {
							Bindings b = iterator.next();
							try {
								return Filter.staticEvalTree(b, node,
										resultsOfAggregationFunctions, evaluationVisitor);

							} catch (Exception e) {
							}
						}
						return null;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
				if (aggregation.isDistinct()) {
					// first just implement an in-memory distinct
					// TODO implement also disk-based duplicate elimination
					// (just like physical operators for DISTINCT)
					final Iterator<? extends Object> oldIterator = values;
					values = new Iterator<Object>() {

						HashSet<Object> alreadyUsedObjects = new HashSet<Object>();
						Object next = null;

						@Override
						public boolean hasNext() {
							if (next != null)
								return true;
							next = next();
							return (next != null);
						}

						@Override
						public Object next() {
							if (next != null) {
								Object znext = next;
								next = null;
								return znext;
							}
							while (oldIterator.hasNext()) {
								Object o = oldIterator.next();
								if (!alreadyUsedObjects.contains(o)) {
									alreadyUsedObjects.add(o);
									return o;
								}
							}
							return null;
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
				}
				result = aggregation.applyAggregation(evaluationVisitor,
						values);
			}

			if (result != null) {
				resultsOfAggregationFunctions.put(n, result);
			}
		}
	}

	public static void computeAggregationFunctions(final QueryResult queryResult, final List<List<lupos.sparql1_1.Node>> aggregationFunctions, final HashMap<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions, final EvaluationVisitor<Map<Node, Object>, Object> evaluationVisitor) {
		if (aggregationFunctions != null)
			for (final List<lupos.sparql1_1.Node> list : aggregationFunctions) {
				for (final lupos.sparql1_1.Node n : list) {
					processAggregationFunction(queryResult, n, resultsOfAggregationFunctions, evaluationVisitor);
				}
			}
	}

	protected static QueryResult getQueryResultForAggregatedFilter(final Node np, final QueryResult queryResult, final List<List<lupos.sparql1_1.Node>> aggregationFunctions, final EvaluationVisitor<Map<Node, Object>, Object> evaluationVisitor) {
		if (queryResult != null) {
			final HashMap<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions = new HashMap<lupos.sparql1_1.Node, Object>(); 
			if (aggregationFunctions != null) {
				computeAggregationFunctions(queryResult, aggregationFunctions, resultsOfAggregationFunctions, evaluationVisitor);
				final Iterator<Bindings> resultIterator = new Iterator<Bindings>() {
					final Iterator<Bindings> bindIt = queryResult
							.oneTimeIterator();
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
									final Object o = Filter.staticEvalTree(bind, np
											.jjtGetChild(0),
											resultsOfAggregationFunctions, evaluationVisitor);
									if (Helper.booleanEffectiveValue(o)) {
										return bind;
									}
								}
							} catch (final NotBoundException nbe) {
								// log.error("Variable has not beeen bound:" +
								// nbe);
								// return null;
							} catch (final TypeErrorException tee) {
								// log.error("type error:" + tee);
								// return null;
							}
						}
						return null;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
				if (resultIterator.hasNext()) {
					return QueryResult.createInstance(resultIterator);
				}
			}
		}
		return null;
	}

	public Message preProcessMessage(final StartOfEvaluationMessage msg) {
		this.evaluationVisitor.init();
		return super.preProcessMessage(msg);
	}

	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		final QueryResult qr = getQueryResultForAggregatedFilter(np, queryResult, aggregationFunctions, evaluationVisitor);
		if (qr != null) {
			if (succeedingOperators.size() > 1)
				qr.materialize();
			for (final OperatorIDTuple opId : succeedingOperators) {
				opId.processAll(qr);
			}
		}
		this.evaluationVisitor.release();
		return msg;
	}

	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		return preProcessMessage(new EndOfEvaluationMessage());
	}

	public QueryResult deleteQueryResult(final QueryResult queryResultToDelete,
			final int operandID) {
		if (this.queryResult != null)
			this.queryResult.removeAll(queryResultToDelete);
		return queryResultToDelete;
	}

	/**
	 * @param b
	 *            : List of Bindings which are tested
	 * @param n
	 *            : actual node, on which is tested
	 * @return Object: might be everything, but by logical reasons the final
	 *         return (outside recursion) is a boolean value which indicates
	 *         weather the binding maps the subtree or not.
	 */
	public Object evalTree(
			final Bindings b,
			final lupos.sparql1_1.Node n,
			final Map<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions)
			throws NotBoundException, TypeErrorException {
		return n.accept(evaluationVisitor, b, resultsOfAggregationFunctions);
	}
	
	/**
	 * @param b
	 *            : List of Bindings which are tested
	 * @param n
	 *            : actual node, on which is tested
	 * @return Object: might be everything, but by logical reasons the final
	 *         return (outside recursion) is a boolean value which indicates
	 *         weather the binding maps the subtree or not.
	 */
	public static Object staticEvalTree(
			final Bindings b,
			final lupos.sparql1_1.Node n,
			final Map<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions, final EvaluationVisitor<Map<Node, Object>, Object> evaluationVisitor)
			throws NotBoundException, TypeErrorException {
		return n.accept(evaluationVisitor, b, resultsOfAggregationFunctions);
	}

	public static Object staticEvalTree(
			final Bindings b,
			final lupos.sparql1_1.Node n,
			final Map<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions)
			throws NotBoundException, TypeErrorException {
		return n.accept(getEvaluationVisitor(), b,
				resultsOfAggregationFunctions);
	}

	@Override
	public void cloneFrom(final BasicOperator op) {
		final Filter filter = (Filter) op;
		super.cloneFrom(op);
		np = filter.np;
		usedVariables = filter.usedVariables;
		aggregationFunctions = filter.aggregationFunctions;
	}

	public boolean equalFilterExpression(final Filter f) {
		return (np.equals(f.np));
	}

	public String toString() {
		final SPARQLParserVisitorImplementationDumper filterDumper = new SPARQLParserVisitorImplementationDumper();

		return np.accept(filterDumper);
	}

	public String toString(final lupos.rdf.Prefix prefixInstance) {
		final SPARQLParserVisitorImplementationDumper filterDumper = new SPARQLParserVisitorImplementationDumperShort(
				prefixInstance);
		String result = np.accept(filterDumper);

		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}

		return result;
	}

	protected boolean isPipelineBreaker() {
		return aggregationFunctions != null;
	}

	public void setQueryResult(final QueryResult queryResult) {
		this.queryResult = queryResult;
	}

	public Message preProcessMessageDebug(
			final StartOfEvaluationMessage msg,
			final DebugStep debugstep) {
		return this.preProcessMessage(msg);
	}
		
	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		return preProcessMessageDebug(new EndOfEvaluationMessage(), debugstep);
	}

	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		final QueryResult qr = getQueryResultForAggregatedFilter(np, queryResult, aggregationFunctions, evaluationVisitor);
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

	public boolean remainsSortedData(Collection<Variable> sortCriterium) {
		return true;
	}

	public void setEvaluator(
			lupos.engine.evaluators.CommonCoreQueryEvaluator<Node> evaluator) {
		this.evaluationVisitor.setEvaluator(evaluator);
	}

	public void setCollectionForExistNodes(Map<SimpleNode, IndexCollection> indexCollections) {
		this.evaluationVisitor.setCollectionForExistNodes(indexCollections);
	}

	public Map<SimpleNode, IndexCollection> getCollectionForExistNodes() {
		return this.evaluationVisitor.getCollectionForExistNodes();
	}

	public EvaluationVisitor<Map<Node, Object>, Object> getUsedEvaluationVisitor() {
		return this.evaluationVisitor;
	}
}