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
	public QueryResult process(final QueryResult queryResult, final int operandID) {
		throw (new UnsupportedOperationException("This Operator(" + this + ") should have been replaced before being used."));
	}

	public QueryResult deleteQueryResult(final QueryResult queryResult, final int operandID) {
		// in the typical case, just apply the normal process method
		// this works well e.g. for filter operators, but does not work well
		// for those operators, which intermediately save the query results
		// as well as pipeline breakers...
		return this.process(queryResult, operandID);
	}

	public void deleteQueryResult(final int operandID) {
		// to be overriden if an operator intermediately stores a QueryResult (e.g. pipeline-breakers) and a QueryResult must be deleted...
	}

	/**
	 * The standard constructor
	 */
	public Operator() {
		// no initialization
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
		final QueryResult opp = this.process(queryResult, operandID);
		if (opp == null) {
			return;
		}
		if (this.succeedingOperators.size() > 1) {
			opp.materialize();
		}
		for (final OperatorIDTuple opId : this.succeedingOperators) {
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
		final QueryResult opp = this.deleteQueryResult(queryResult, operandID);
		if (!this.isPipelineBreaker()) {
			if (opp == null) {
				return;
			}
			if (this.succeedingOperators.size() > 1) {
				opp.materialize();
			}
		}
		for (final OperatorIDTuple opId : this.succeedingOperators) {
			if (this.isPipelineBreaker()) {
				((Operator) opId.getOperator()).deleteAll(opId.getId());
			} else {
				((Operator) opId.getOperator()).deleteAll(opp, opId.getId());
			}
		}
	}

	public void deleteAll(final int operandID) {
		this.deleteQueryResult(operandID);
		this.deleteAllAtSucceedingOperators();
	}

	public void deleteAllAtSucceedingOperators() {
		for (final OperatorIDTuple opId : this.succeedingOperators) {
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
	 * @param operandID
	 *            the operand number
	 */
	public void deleteAllDebug(final int operandID, final DebugStep debugstep) {
		this.deleteQueryResult(operandID);
		this.deleteAllDebugAtSucceedingOperators(debugstep);
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
		final QueryResult opp = this.deleteQueryResult(queryResult, operandID);
		if (!this.isPipelineBreaker()) {
			if (opp == null) {
				return;
			}
			if (this.succeedingOperators.size() > 1) {
				opp.materialize();
			}
		}
		for (final OperatorIDTuple opId : this.succeedingOperators) {
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
		for (final OperatorIDTuple opId : this.succeedingOperators) {
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
	public void processAllDebug(final QueryResult queryResult, final int operandID, final DebugStep debugstep) {
		final QueryResult opp = this.process(queryResult, operandID);
		if (opp == null){
			return;
		}
		if (this.succeedingOperators.size() > 1) {
			opp.materialize();
		}
		for (final OperatorIDTuple opId : this.succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(opp, debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId.getId(), debugstep);
		}
	}
}
