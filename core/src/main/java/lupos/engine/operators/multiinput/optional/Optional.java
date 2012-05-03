package lupos.engine.operators.multiinput.optional;

import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.MultiInputOperator;
import lupos.engine.operators.multiinput.join.Join;
import lupos.misc.debug.DebugStep;

public class Optional extends MultiInputOperator {

	private QueryResult notJoinedFromLeftOperand = null;
	private QueryResult joinedFromLeftOperand = null;
	protected Join join;

	public Optional() {
		super();
	}

	public Optional(final Join join) {
		super();
		this.join = join;
	}

	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		if (join == null) {
			System.out
					.println("The embedded join operator has not been initialized in the optional operator:"
							+ this);
			return null;
		}
		if (this.precedingOperators.size() == 1) {
			return bindings;
		} else {
			bindings.materialize();
			if (bindings.size() == 0)
				return null;
			join.setUnionVariables(unionVariables);
			join.setIntersectionVariables(intersectionVariables);
			final OptionalResult or = join.processJoin(bindings, operandID);

			if (operandID == 0) {
				if (or != null && or.getJoinPartnerFromLeftOperand() != null) {
					bindings.removeAll(or.getJoinPartnerFromLeftOperand());
				}
				if (notJoinedFromLeftOperand == null)
					notJoinedFromLeftOperand = new QueryResult();
				notJoinedFromLeftOperand.addAll(bindings);
			} else if (or != null && or.getJoinPartnerFromLeftOperand() != null) {
				if (notJoinedFromLeftOperand != null) {
					notJoinedFromLeftOperand.removeAll(or
							.getJoinPartnerFromLeftOperand());
				}
				if (joinedFromLeftOperand == null)
					joinedFromLeftOperand = or.getJoinPartnerFromLeftOperand();
				else
					joinedFromLeftOperand.addAll(or
							.getJoinPartnerFromLeftOperand());
			}
			if (or == null || or.getJoinResult() == null
					|| or.getJoinResult().size() == 0)
				return null;
			else
				return or.getJoinResult();
		}
	}

	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		return this.preProcessMessage(msg, true);
	}

	public Message preProcessMessage(final EndOfEvaluationMessage msg,
			final boolean delete) {
		if (join != null) {
			join.setUnionVariables(unionVariables);
			join.setIntersectionVariables(intersectionVariables);
			final OptionalResult or = join.joinBeforeEndOfStream();

			if (or != null) {
				if (or.getJoinPartnerFromLeftOperand() != null
						&& or.getJoinPartnerFromLeftOperand().size() > 0) {
					notJoinedFromLeftOperand.removeAll(or
							.getJoinPartnerFromLeftOperand());
					if (!delete) {
						if (joinedFromLeftOperand == null)
							joinedFromLeftOperand = QueryResult
									.createInstance();
						joinedFromLeftOperand.addAll(or
								.getJoinPartnerFromLeftOperand());
					}
				}
				if (or.getJoinResult() != null && or.getJoinResult().size() > 0) {
					if (succeedingOperators.size() > 1)
						or.getJoinResult().materialize();
					for (final OperatorIDTuple opId : succeedingOperators) {
						opId.processAll(or.getJoinResult());
					}
				}
			}
		}
		if (notJoinedFromLeftOperand != null) {
			if (joinedFromLeftOperand != null) {
				notJoinedFromLeftOperand.removeAll(joinedFromLeftOperand);
				if (delete) {
					joinedFromLeftOperand.release();
					joinedFromLeftOperand = null;
				}
			}
			if (succeedingOperators.size() > 1)
				notJoinedFromLeftOperand.materialize();
			for (final OperatorIDTuple opId : succeedingOperators) {
				opId.processAll(notJoinedFromLeftOperand);
			}
			if (delete) {
				// notJoinedFromLeftOperand.release();
				notJoinedFromLeftOperand = null;
			}
		}
		return msg;
	}

	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		return preProcessMessage(new EndOfEvaluationMessage(), false);
	}

	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		if (join == null) {
			System.out
					.println("The embedded join operator has not been initialized in the optional operator:"
							+ this);
			return null;
		}
		if (this.precedingOperators.size() == 1) {
			return queryResult;
		} else {
			queryResult.materialize();
			if (queryResult.size() == 0)
				return null;
			join.setUnionVariables(unionVariables);
			join.setIntersectionVariables(intersectionVariables);
			join.deleteQueryResult(queryResult, operandID);
			if (operandID == 0) {
				if (notJoinedFromLeftOperand != null)
					notJoinedFromLeftOperand.removeAll(queryResult);
				if (joinedFromLeftOperand != null)
					joinedFromLeftOperand.removeAll(queryResult);
			}
			return null;
		}
	}

	protected boolean isPipelineBreaker() {
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + " on " + intersectionVariables;
	}

	public QueryResult getNotJoinedFromLeftOperand() {
		return notJoinedFromLeftOperand;
	}

	public QueryResult getJoinedFromLeftOperand() {
		return joinedFromLeftOperand;
	}

	public Join getJoin() {
		return join;
	}
	
	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		return preProcessMessageDebug(new EndOfEvaluationMessage(), debugstep,
				false);
	}
	
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep, final boolean delete) {
		if (join != null) {
			join.setUnionVariables(unionVariables);
			join.setIntersectionVariables(intersectionVariables);
			final OptionalResult or = join.joinBeforeEndOfStream();

			if (or != null) {
				if (or.getJoinPartnerFromLeftOperand() != null
						&& or.getJoinPartnerFromLeftOperand().size() > 0) {
					notJoinedFromLeftOperand.removeAll(or
							.getJoinPartnerFromLeftOperand());
					if (!delete) {
						if (joinedFromLeftOperand == null)
							joinedFromLeftOperand = QueryResult
									.createInstance();
						joinedFromLeftOperand.addAll(or
								.getJoinPartnerFromLeftOperand());
					}
				}
				if (or.getJoinResult() != null && or.getJoinResult().size() > 0) {
					if (succeedingOperators.size() > 1)
						or.getJoinResult().materialize();
					for (final OperatorIDTuple opId : succeedingOperators) {
						final QueryResultDebug qrDebug = new QueryResultDebug(
								or.getJoinResult(), debugstep, this, opId
										.getOperator(), true);
						((Operator) opId.getOperator()).processAllDebug(
								qrDebug, opId.getId(), debugstep);
					}
				}
			}
		}
		if (notJoinedFromLeftOperand != null) {
			if (joinedFromLeftOperand != null) {
				notJoinedFromLeftOperand.removeAll(joinedFromLeftOperand);
				if (delete) {
					joinedFromLeftOperand.release();
					joinedFromLeftOperand = null;
				}
			}
			if (succeedingOperators.size() > 1)
				notJoinedFromLeftOperand.materialize();
			for (final OperatorIDTuple opId : succeedingOperators) {
				final QueryResultDebug qrDebug = new QueryResultDebug(
						notJoinedFromLeftOperand, debugstep, this, opId
								.getOperator(), true);
				((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
						.getId(), debugstep);
			}
			if (delete) {
				// notJoinedFromLeftOperand.release();
				notJoinedFromLeftOperand = null;
			}
		}
		return msg;
	}
	
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		return this.preProcessMessageDebug(msg, debugstep, true);
	}
}
