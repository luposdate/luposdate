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
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.ExpressionEvaluation.Helper;
import lupos.misc.debug.DebugStep;

public class Bind extends AddComputedBinding {
	/**
	 * serial ID
	 */
	private static final long serialVersionUID = -3021781533018311163L;

	private Variable var;

	public Bind(Variable var) {
		this.var = var;
	}

	/**
	 * This method filters if needed and calls evalTree
	 * 
	 * @param QueryResult
	 * @param int
	 * @return QueryResult
	 */
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		bindings.materialize();
		final List<HashMap<lupos.sparql1_1.Node, Object>> resultsOfAggregationFunctionsList = new LinkedList<HashMap<lupos.sparql1_1.Node, Object>>(); 
		for (final Map.Entry<Variable, Filter> entry : projections.entrySet()) {
			final HashMap<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions = new HashMap<lupos.sparql1_1.Node, Object>();
			Filter.computeAggregationFunctions(bindings, entry.getValue().aggregationFunctions, resultsOfAggregationFunctions, entry.getValue().getUsedEvaluationVisitor());
			resultsOfAggregationFunctionsList.add(resultsOfAggregationFunctions);
		}
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
							Iterator<HashMap<lupos.sparql1_1.Node, Object>> resultsOfAggregationFunctionsIterator = resultsOfAggregationFunctionsList.iterator();
							for (final Map.Entry<Variable, Filter> entry : projections
									.entrySet()) {
								HashMap<lupos.sparql1_1.Node, Object> resultsOfAggregationFunctions = resultsOfAggregationFunctionsIterator.next();
								Literal boundValue = bind.get(var);
								Literal toBound = Helper.getLiteral(Filter.staticEvalTree(bind, entry.getValue().getNodePointer(), resultsOfAggregationFunctions, entry.getValue().getUsedEvaluationVisitor()));

								if(boundValue==null){ // variable is not bound => bound with computed value
									final Bindings bindNew = bind.clone();
									bindNew.add(var, toBound);
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

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};

		if (resultIterator.hasNext())
			return QueryResult.createInstance(resultIterator);
		return null;
	}
	
	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		return msg;
	}
	
	public Message preProcessMessageDebug(final ComputeIntermediateResultMessage msg, final DebugStep debugstep) {
		return msg;
	}
}