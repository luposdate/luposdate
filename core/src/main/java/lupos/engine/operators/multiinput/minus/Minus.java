package lupos.engine.operators.multiinput.minus;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.MultiInputOperator;
import lupos.rdf.Prefix;

public class Minus extends MultiInputOperator {

	protected QueryResult lChild;
	protected QueryResult rChild;

	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {

		// wait for all query results and process them when
		// EndOfEvaluationMessage arrives
		if (operandID == 0) {
			if (lChild == null)
				lChild = bindings;
			else
				lChild.addAll(bindings);
		} else if (operandID == 1) {
			if (rChild == null)
				rChild = bindings;
			else
				rChild.addAll(bindings);
		}

		return null;
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		if (rChild != null && lChild != null) {
			QueryResult result = QueryResult.createInstance();
			Iterator<Bindings> iteratorLeftChild = lChild.oneTimeIterator();
			while (iteratorLeftChild.hasNext()) {
				Bindings leftItem = iteratorLeftChild.next();
				boolean found = false;
				for (Bindings rightItem : rChild) {
					// compute intersection of the variable sets
					Set<Variable> vars = rightItem.getVariableSet();
					vars.retainAll(leftItem.getVariableSet());

					boolean isEqual = true;
					for (Variable v : vars) {
						if ((v.getLiteral(leftItem)
								.compareToNotNecessarilySPARQLSpecificationConform(v
										.getLiteral(rightItem))) != 0) {
							isEqual = false;
						}
					}

					// if intersection is empty then isEqual should always be
					// false,
					// workaround: check whether vars is empty
					if (isEqual && !vars.isEmpty())
						found = true;
				}
				if (!found)
					result.add(leftItem);
			}

			for (final OperatorIDTuple opId : succeedingOperators) {
				opId.processAll(result);
			}
		} else if (lChild != null) { // happens when the group constraint which
										// follows the
			// minus is empty
			for (final OperatorIDTuple opId : succeedingOperators) {
				opId.processAll(lChild);
			}
		}
		return msg;
	}

	@Override
	public Message preProcessMessage(BoundVariablesMessage msg) {
		BoundVariablesMessage msg_result = new BoundVariablesMessage(msg);
		Set<Variable> variableSet = null;
		// the variables, which are always bound are the intersection
		// variables of the left hand side
		for (BasicOperator parent : this.getPrecedingOperators()) {
			OperatorIDTuple opID = parent.getOperatorIDTuple(this);
			if (opID.getId() == 0) {
				if (variableSet == null) {
					variableSet = new HashSet<Variable>();
					variableSet.addAll(parent.getIntersectionVariables());
				} else {
					variableSet.retainAll(parent.getIntersectionVariables());
				}
			}
		}

		intersectionVariables = variableSet;

		msg_result.setVariables(intersectionVariables);
		return msg_result;
	}
	
	@Override
	public String toString(){
		return super.toString()+" "+this.intersectionVariables;
	}
}
