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
package lupos.engine.operators.singleinput.filter;

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
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.engine.operators.singleinput.NotBoundException;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.engine.operators.singleinput.filter.expressionevaluation.EvaluationVisitor;
import lupos.engine.operators.singleinput.filter.expressionevaluation.EvaluationVisitorImplementation;
import lupos.engine.operators.singleinput.filter.expressionevaluation.Helper;
import lupos.misc.debug.DebugStep;
import lupos.misc.util.ImmutableIterator;
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
 *
 * @author groppe
 * @version $Id: $Id
 */
public class Filter extends SingleInputOperator {

	private static final long serialVersionUID = 2118104495454058506L;
	private int cardinality = -1;
	private lupos.sparql1_1.Node np;
	private Set<Variable> usedVariables = new HashSet<Variable>();

	public List<List<lupos.sparql1_1.Node>> aggregationFunctions = null;
	protected QueryResult queryResult = null;

	/** Constant <code>evaluationVisitorClass</code> */
	public static Class<? extends EvaluationVisitor<Map<Node, Object>, Object>> evaluationVisitorClass = EvaluationVisitorImplementation.class;
	private final EvaluationVisitor<Map<Node, Object>, Object> evaluationVisitor;

	/**
	 * <p>Constructor for Filter.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 */
	public Filter(final lupos.sparql1_1.Node node) {
		this.evaluationVisitor = getEvaluationVisitor();
		this.setNodePointer(node);
	}

	/**
	 * <p>Constructor for Filter.</p>
	 */
	public Filter() {
		this.evaluationVisitor = getEvaluationVisitor();
		this.np = null;
	}

	/**
	 * <p>Constructor for Filter.</p>
	 *
	 * @param filter a {@link java.lang.String} object.
	 * @throws lupos.sparql1_1.ParseException if any.
	 */
	public Filter(final String filter) throws ParseException {
		this.evaluationVisitor = getEvaluationVisitor();
		ASTFilterConstraint ASTfilter;
		ASTfilter = (ASTFilterConstraint) SPARQL1_1Parser.parseFilter(filter);
		this.setNodePointer(ASTfilter);
	}

	private static EvaluationVisitor<Map<Node, Object>, Object> getEvaluationVisitor() {
		try {
			return evaluationVisitorClass.newInstance();
		} catch (final InstantiationException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return new EvaluationVisitorImplementation();
	}

	/**
	 * <p>setNodePointer.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 */
	public void setNodePointer(final lupos.sparql1_1.Node node) {
		this.np = node;
		this.usedVariables.clear();
		this.computeUsedVariables(this.np);
		this.aggregationFunctions = this.computeAggegrationFunctions(this.np);
	}

	private void computeUsedVariables(final lupos.sparql1_1.Node n) {
		if (n == null) {
			return;
		}
		if (n instanceof lupos.sparql1_1.ASTVar) {
			try {
				this.usedVariables.add(new Variable(((lupos.sparql1_1.ASTVar) n)
						.getName().toString()));
			} catch (final Exception e) {
				System.err.println(e);
				return;
			}

		}
		for (int i = 0; i < n.jjtGetNumChildren(); i++) {
			this.computeUsedVariables(n.jjtGetChild(i));
		}
	}

	private List<List<lupos.sparql1_1.Node>> computeAggegrationFunctions(
			final lupos.sparql1_1.Node n) {
		if (n == null) {
			return null;
		}
		List<List<lupos.sparql1_1.Node>> result = null;
		for (int i = 0; i < n.jjtGetNumChildren(); i++) {
			final List<List<lupos.sparql1_1.Node>> interResult = this.computeAggegrationFunctions(n
					.jjtGetChild(i));
			if (interResult != null) {
				if (result == null) {
					result = interResult;
				} else {
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
			if (result == null) {
				result = new LinkedList<List<lupos.sparql1_1.Node>>();
			}
			final List<lupos.sparql1_1.Node> currentLevel = new LinkedList<lupos.sparql1_1.Node>();
			currentLevel.add(n);
			result.add(currentLevel);
		}
		return result;
	}

	/**
	 * <p>Getter for the field <code>usedVariables</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Variable> getUsedVariables() {
		return this.usedVariables;
	}

	/**
	 * <p>getNodePointer.</p>
	 *
	 * @return ASTFilterConstraint: the node this instance belongs to
	 */
	public lupos.sparql1_1.Node getNodePointer() {
		return this.np;
	}

	/**
	 * {@inheritDoc}
	 *
	 * This Method processes the incoming Bindings-sequence and forwards the
	 * Bindings which meet the demands
	 */
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (this.aggregationFunctions == null) {
			final Iterator<Bindings> resultIterator = new ImmutableIterator<Bindings>() {
				final Iterator<Bindings> bindIt = bindings.oneTimeIterator();
				int number = 0;
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
								final Object o = Filter.this.evalTree(bind, Filter.this.np
										.jjtGetChild(0),
										null);
								if (Helper.booleanEffectiveValue(o)) {
									this.number++;
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
					Filter.this.cardinality = this.number;
					return null;
				}
			};

			if (resultIterator.hasNext()) {
				return QueryResult.createInstance(resultIterator);
			}
			else {
				return null;
			// forwarded bindings, return value
			}
		} else {
			if (this.queryResult == null) {
				bindings.materialize();
				this.queryResult = bindings;
			} else {
				this.queryResult.add(bindings);
			}
			return null;
		}
	}

	private static boolean isConstant(final lupos.sparql1_1.Node n) {
		if (n == null) {
			return true;
		}
		if (n instanceof ASTVar) {
			return false;
		}
		if (n.getChildren() == null) {
			return true;
		}
		for (final lupos.sparql1_1.Node node : n.getChildren()) {
			if (!isAggregationFunction(node) && !isConstant(node)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isAggregationFunction(final lupos.sparql1_1.Node n) {
		return (n instanceof ASTAggregation);
	}

	private static void processAggregationFunction(final QueryResult queryResult, final lupos.sparql1_1.Node n, final HashMap<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions, final EvaluationVisitor<Map<Node, Object>, Object> evaluationVisitor) {
		final boolean childAdded = false;
		if (isAggregationFunction(n)) {
			final ASTAggregation aggregation = (ASTAggregation) n;
			Object result = null;

			if (n.jjtGetNumChildren()>0 && isConstant(n.jjtGetChild(0))) {
				try {
					final lupos.sparql1_1.Node node=n.jjtGetChild(0);
					final Object operand = Filter.staticEvalTree(null, node,
							resultsOfAggregationFunctions, evaluationVisitor);
					if (childAdded) {
						n.clearChildren();
					}
					final Iterator<Object> values = new ImmutableIterator<Object>() {
						Object next = operand;

						@Override
						public boolean hasNext() {
							return this.next != null;
						}

						@Override
						public Object next() {
							final Object znext = this.next;
							this.next = null;
							return znext;
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

				Iterator<? extends Object> values = (n.jjtGetNumChildren()==0)?queryResult.iterator():new ImmutableIterator<Object>() {
					final lupos.sparql1_1.Node node=n.jjtGetChild(0);
					Iterator<Bindings> iterator = queryResult.iterator();
					Object next = null;

					@Override
					public boolean hasNext() {
						if (this.next != null) {
							return true;
						}
						this.next = this.next();
						return (this.next != null);
					}

					@Override
					public Object next() {
						if (this.next != null) {
							final Object znext = this.next;
							this.next = null;
							return znext;
						}
						while (this.iterator.hasNext()) {
							final Bindings b = this.iterator.next();
							try {
								return Filter.staticEvalTree(b, this.node,
										resultsOfAggregationFunctions, evaluationVisitor);

							} catch (final Exception e) {
								// just ignore bindings with error!
							}
						}
						return null;
					}
				};
				if (aggregation.isDistinct()) {
					// first just implement an in-memory distinct
					// TODO implement also disk-based duplicate elimination
					// (just like physical operators for DISTINCT)
					final Iterator<? extends Object> oldIterator = values;
					values = new ImmutableIterator<Object>() {

						HashSet<Object> alreadyUsedObjects = new HashSet<Object>();
						Object next = null;

						@Override
						public boolean hasNext() {
							if (this.next != null) {
								return true;
							}
							this.next = this.next();
							return (this.next != null);
						}

						@Override
						public Object next() {
							if (this.next != null) {
								final Object znext = this.next;
								this.next = null;
								return znext;
							}
							while (oldIterator.hasNext()) {
								final Object o = oldIterator.next();
								if (!this.alreadyUsedObjects.contains(o)) {
									this.alreadyUsedObjects.add(o);
									return o;
								}
							}
							return null;
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

	/**
	 * <p>computeAggregationFunctions.</p>
	 *
	 * @param queryResult a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param aggregationFunctions a {@link java.util.List} object.
	 * @param resultsOfAggregationFunctions a {@link java.util.HashMap} object.
	 * @param evaluationVisitor a {@link lupos.engine.operators.singleinput.filter.expressionevaluation.EvaluationVisitor} object.
	 */
	public static void computeAggregationFunctions(final QueryResult queryResult, final List<List<lupos.sparql1_1.Node>> aggregationFunctions, final HashMap<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions, final EvaluationVisitor<Map<Node, Object>, Object> evaluationVisitor) {
		if (aggregationFunctions != null) {
			for (final List<lupos.sparql1_1.Node> list : aggregationFunctions) {
				for (final lupos.sparql1_1.Node n : list) {
					processAggregationFunction(queryResult, n, resultsOfAggregationFunctions, evaluationVisitor);
				}
			}
		}
	}

	/**
	 * <p>getQueryResultForAggregatedFilter.</p>
	 *
	 * @param np a lupos$sparql1_1$Node object.
	 * @param queryResult a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param aggregationFunctions a {@link java.util.List} object.
	 * @param evaluationVisitor a {@link lupos.engine.operators.singleinput.filter.expressionevaluation.EvaluationVisitor} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	protected static QueryResult getQueryResultForAggregatedFilter(final Node np, final QueryResult queryResult, final List<List<lupos.sparql1_1.Node>> aggregationFunctions, final EvaluationVisitor<Map<Node, Object>, Object> evaluationVisitor) {
		if (queryResult != null) {
			final HashMap<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions = new HashMap<lupos.sparql1_1.Node, Object>();
			if (aggregationFunctions != null) {
				computeAggregationFunctions(queryResult, aggregationFunctions, resultsOfAggregationFunctions, evaluationVisitor);
				final Iterator<Bindings> resultIterator = new ImmutableIterator<Bindings>() {
					final Iterator<Bindings> bindIt = queryResult.oneTimeIterator();
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
				};
				if (resultIterator.hasNext()) {
					return QueryResult.createInstance(resultIterator);
				}
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final StartOfEvaluationMessage msg) {
		this.evaluationVisitor.init();
		return super.preProcessMessage(msg);
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		final QueryResult qr = getQueryResultForAggregatedFilter(this.np, this.queryResult, this.aggregationFunctions, this.evaluationVisitor);
		if (qr != null) {
			if (this.succeedingOperators.size() > 1) {
				qr.materialize();
			}
			for (final OperatorIDTuple opId : this.succeedingOperators) {
				opId.processAll(qr);
			}
		}
		this.evaluationVisitor.release();
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		return this.preProcessMessage(new EndOfEvaluationMessage());
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult deleteQueryResult(final QueryResult queryResultToDelete,
			final int operandID) {
		if (this.queryResult != null) {
			this.queryResult.removeAll(queryResultToDelete);
		}
		return queryResultToDelete;
	}

	/**
	 * <p>evalTree.</p>
	 *
	 * @param b
	 *            : List of Bindings which are tested
	 * @param n
	 *            : actual node, on which is tested
	 * @return Object: might be everything, but by logical reasons the final
	 *         return (outside recursion) is a boolean value which indicates
	 *         weather the binding maps the subtree or not.
	 * @param resultsOfAggregationFunctions a {@link java.util.Map} object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public Object evalTree(
			final Bindings b,
			final lupos.sparql1_1.Node n,
			final Map<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions)
			throws NotBoundException, TypeErrorException {
		return n.accept(this.evaluationVisitor, b, resultsOfAggregationFunctions);
	}

	/**
	 * <p>staticEvalTree.</p>
	 *
	 * @param b
	 *            : List of Bindings which are tested
	 * @param n
	 *            : actual node, on which is tested
	 * @return Object: might be everything, but by logical reasons the final
	 *         return (outside recursion) is a boolean value which indicates
	 *         weather the binding maps the subtree or not.
	 * @param resultsOfAggregationFunctions a {@link java.util.Map} object.
	 * @param evaluationVisitor a {@link lupos.engine.operators.singleinput.filter.expressionevaluation.EvaluationVisitor} object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public static Object staticEvalTree(
			final Bindings b,
			final lupos.sparql1_1.Node n,
			final Map<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions, final EvaluationVisitor<Map<Node, Object>, Object> evaluationVisitor)
			throws NotBoundException, TypeErrorException {
		return n.accept(evaluationVisitor, b, resultsOfAggregationFunctions);
	}

	/**
	 * <p>staticEvalTree.</p>
	 *
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param n a lupos$sparql1_1$Node object.
	 * @param resultsOfAggregationFunctions a {@link java.util.Map} object.
	 * @return a {@link java.lang.Object} object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public static Object staticEvalTree(
			final Bindings b,
			final lupos.sparql1_1.Node n,
			final Map<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions)
			throws NotBoundException, TypeErrorException {
		return n.accept(getEvaluationVisitor(), b,
				resultsOfAggregationFunctions);
	}

	/** {@inheritDoc} */
	@Override
	public void cloneFrom(final BasicOperator op) {
		final Filter filter = (Filter) op;
		super.cloneFrom(op);
		this.np = filter.np;
		this.usedVariables = filter.usedVariables;
		this.aggregationFunctions = filter.aggregationFunctions;
		this.setCollectionForExistNodes(filter.getCollectionForExistNodes());
	}

	/**
	 * <p>equalFilterExpression.</p>
	 *
	 * @param f a {@link lupos.engine.operators.singleinput.filter.Filter} object.
	 * @return a boolean.
	 */
	public boolean equalFilterExpression(final Filter f) {
		return (this.np.equals(f.np));
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final SPARQLParserVisitorImplementationDumper filterDumper = new SPARQLParserVisitorImplementationDumper();

		return this.np.accept(filterDumper);
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final lupos.rdf.Prefix prefixInstance) {
		final SPARQLParserVisitorImplementationDumper filterDumper = new SPARQLParserVisitorImplementationDumperShort(
				prefixInstance);
		String result = this.np.accept(filterDumper);

		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isPipelineBreaker() {
		return this.aggregationFunctions != null;
	}

	/**
	 * <p>Setter for the field <code>queryResult</code>.</p>
	 *
	 * @param queryResult a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public void setQueryResult(final QueryResult queryResult) {
		this.queryResult = queryResult;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessageDebug(
			final StartOfEvaluationMessage msg,
			final DebugStep debugstep) {
		return this.preProcessMessage(msg);
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		return this.preProcessMessageDebug(new EndOfEvaluationMessage(), debugstep);
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		final QueryResult qr = getQueryResultForAggregatedFilter(this.np, this.queryResult, this.aggregationFunctions, this.evaluationVisitor);
		if (qr != null) {
			if (this.succeedingOperators.size() > 1) {
				qr.materialize();
			}
			for (final OperatorIDTuple opId : this.succeedingOperators) {
				final QueryResultDebug qrDebug = new QueryResultDebug(qr,
						debugstep, this, opId.getOperator(), true);
				((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
						.getId(), debugstep);
			}
		}
		return msg;
	}

	/**
	 * <p>materializationOfLazyLiteralsNeeded.</p>
	 *
	 * @return a boolean.
	 */
	public boolean materializationOfLazyLiteralsNeeded(){
		if(LiteralFactory.getMapType() == MapType.LAZYLITERAL || LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP){
			final Object result = this.np.jjtAccept(new MaterializationNeededVisitor(), null);
			if(result != null && result instanceof Boolean){
				return (Boolean) result;
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean remainsSortedData(final Collection<Variable> sortCriterium) {
		return true;
	}

	/**
	 * <p>setEvaluator.</p>
	 *
	 * @param evaluator a {@link lupos.engine.evaluators.CommonCoreQueryEvaluator} object.
	 */
	public void setEvaluator(
			final lupos.engine.evaluators.CommonCoreQueryEvaluator<Node> evaluator) {
		this.evaluationVisitor.setEvaluator(evaluator);
	}

	/**
	 * <p>setCollectionForExistNodes.</p>
	 *
	 * @param root a {@link java.util.Map} object.
	 */
	public void setCollectionForExistNodes(final Map<SimpleNode, Root> root) {
		this.evaluationVisitor.setCollectionForExistNodes(root);
	}

	/**
	 * <p>getCollectionForExistNodes.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<SimpleNode, Root> getCollectionForExistNodes() {
		return this.evaluationVisitor.getCollectionForExistNodes();
	}

	/**
	 * <p>getUsedEvaluationVisitor.</p>
	 *
	 * @return a {@link lupos.engine.operators.singleinput.filter.expressionevaluation.EvaluationVisitor} object.
	 */
	public EvaluationVisitor<Map<Node, Object>, Object> getUsedEvaluationVisitor() {
		return this.evaluationVisitor;
	}
}
