package lupos.engine.operators;

import java.util.List;

import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.misc.debug.DebugStep;

/**
 * This class is the super class for all operators, which process bindings.
 */
public class Operator extends BasicOperator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8701923927442894567L;

	/**
	 * This method is overridden by derived classes. The overridden methods
	 * should process a received QueryResult object (from a preceding operator).
	 * Additionally the operand number is available. Let us assume that the
	 * current operator is C, another operator is A and a succeeding operator is
	 * B = A OPTIONAL C. Note that it is important to know that A is the left
	 * operand and C is the right operand of B. Then a succeeding operator of C
	 * is B with operand number 1 (A has a succeeding operator B with operand
	 * number 0).
	 * 
	 * @param queryResult
	 *            The received QueryResult
	 * @param operandID
	 *            The operand number
	 * @return
	 */
	public QueryResult process(final QueryResult queryResult,
			final int operandID) {
		throw (new UnsupportedOperationException("This Operator(" + this
				+ ") should have been replaced before being used."));
	}

	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		// in the typical case, just apply the normal process method
		// this works well e.g. for filter operators, but does not work well
		// for those operators, which intermediately save the query results
		// as well as pipeline breakers...
		return process(queryResult, operandID);
	}

	public void deleteQueryResult(final int operandID) {
	}

	/**
	 * The standard constructor
	 */
	public Operator() {
	}

	/**
	 * The constructor, which sets the succeeding operators
	 * 
	 * @param succeedingOperators
	 *            the succeeding operators
	 */
	public Operator(final List<OperatorIDTuple> succeedingOperators) {
		super(succeedingOperators);
	}

	/**
	 * This constructor can be used to set only one succeeding operator
	 * 
	 * @param succeedingOperator
	 *            the one and only succeeding operator
	 */
	public Operator(final OperatorIDTuple succeedingOperator) {
		super(succeedingOperator);
	}

	/**
	 * This method processes a received QueryResult with operand number and
	 * forwards the result to the succeeding operators.
	 * 
	 * @param queryResult
	 *            the received QueryResult
	 * @param operandID
	 *            the operand number
	 */
	public void processAll(final QueryResult queryResult, final int operandID) {
		final QueryResult opp = process(queryResult, operandID);
		if (opp == null)
			return;
		if (succeedingOperators.size() > 1) {
			opp.materialize();
		}
		for (final OperatorIDTuple opId : succeedingOperators) {
			((Operator) opId.getOperator()).processAll(opp, opId.getId());
		}
	}

	/**
	 * This method processes a received QueryResult with operand number and
	 * forwards the result to the succeeding operators.
	 * 
	 * @param queryResult
	 *            the received QueryResult
	 * @param operandID
	 *            the operand number
	 */
	public void deleteAll(final QueryResult queryResult, final int operandID) {
		final QueryResult opp = deleteQueryResult(queryResult, operandID);
		if (!this.isPipelineBreaker()) {
			if (opp == null)
				return;
			if (succeedingOperators.size() > 1) {
				opp.materialize();
			}
		}
		for (final OperatorIDTuple opId : succeedingOperators) {
			if (this.isPipelineBreaker()) {
				((Operator) opId.getOperator()).deleteAll(opId.getId());
			} else
				((Operator) opId.getOperator()).deleteAll(opp, opId.getId());
		}
	}

	public void deleteAll(final int operandID) {
		deleteQueryResult(operandID);
		deleteAllAtSucceedingOperators();
	}

	public void deleteAllAtSucceedingOperators() {
		for (final OperatorIDTuple opId : succeedingOperators) {
			((Operator) opId.getOperator()).deleteAll(opId.getId());
		}
	}

	protected boolean isPipelineBreaker() {
		return false;
	}
	
	/**
	 * This method processes a received QueryResult with operand number and
	 * forwards the result to the succeeding operators. It further sends debug
	 * messages for stepwise debugging!
	 * 
	 * @param queryResult
	 *            the received QueryResult
	 * @param operandID
	 *            the operand number
	 */
	public void deleteAllDebug(final int operandID, final DebugStep debugstep) {
		deleteQueryResult(operandID);
		deleteAllDebugAtSucceedingOperators(debugstep);
	}

	/**
	 * This method processes a received QueryResult with operand number and
	 * forwards the result to the succeeding operators. It further sends debug
	 * messages for stepwise debugging!
	 * 
	 * @param queryResult
	 *            the received QueryResult
	 * @param operandID
	 *            the operand number
	 */
	public void deleteAllDebug(final QueryResult queryResult,
			final int operandID, final DebugStep debugstep) {
		final QueryResult opp = deleteQueryResult(queryResult, operandID);
		if (!this.isPipelineBreaker()) {
			if (opp == null)
				return;
			if (succeedingOperators.size() > 1) {
				opp.materialize();
			}
		}
		for (final OperatorIDTuple opId : succeedingOperators) {
			if (this.isPipelineBreaker()) {
				debugstep.stepDeleteAll(this, opId.getOperator());
				((Operator) opId.getOperator()).deleteAllDebug(opId.getId(),
						debugstep);
			} else {
				final QueryResultDebug qrDebug = new QueryResultDebug(opp,
						debugstep, this, opId.getOperator(), false);
				((Operator) opId.getOperator()).deleteAllDebug(qrDebug, opId
						.getId(), debugstep);
			}
		}
	}
	
	public void deleteAllDebugAtSucceedingOperators(final DebugStep debugstep) {
		for (final OperatorIDTuple opId : succeedingOperators) {
			debugstep.stepDeleteAll(this, opId.getOperator());
			((Operator) opId.getOperator()).deleteAllDebug(opId.getId(),
					debugstep);
		}
	}
	
	/**
	 * This method processes a received QueryResult with operand number and
	 * forwards the result to the succeeding operators. It further sends debug
	 * messages for stepwise debugging!
	 * 
	 * @param queryResult
	 *            the received QueryResult
	 * @param operandID
	 *            the operand number
	 */
	public void processAllDebug(final QueryResult queryResult,
			final int operandID, final DebugStep debugstep) {
		final QueryResult opp = process(queryResult, operandID);
		if (opp == null)
			return;
		if (succeedingOperators.size() > 1) {
			opp.materialize();
		}
		for (final OperatorIDTuple opId : succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(opp,
					debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
					.getId(), debugstep);
		}
	}
}
