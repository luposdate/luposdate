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
package lupos.engine.operators.multiinput.optional;

import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.join.Join;
import lupos.misc.debug.DebugStep;

public class UsingJoinOptional extends Optional {
	private QueryResult notJoinedFromLeftOperand = null;
	private QueryResult joinedFromLeftOperand = null;
	protected Join join;

	public UsingJoinOptional() {
		super();
	}

	public UsingJoinOptional(final Join join) {
		super();
		this.join = join;
	}

	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		if (this.join == null) {
			System.out.println("The embedded join operator has not been initialized in the optional operator:"+ this);
			return null;
		}
		if (this.precedingOperators.size() == 1) {
			return bindings;
		} else {
			bindings.materialize();
			if (bindings.size() == 0) {
				return null;
			}
			this.join.setUnionVariables(this.unionVariables);
			this.join.setIntersectionVariables(this.intersectionVariables);
			final OptionalResult or = this.join.processJoin(bindings, operandID);

			if (operandID == 0) {
				if (or != null && or.getJoinPartnerFromLeftOperand() != null) {
					bindings.removeAll(or.getJoinPartnerFromLeftOperand());
				}
				if (this.notJoinedFromLeftOperand == null) {
					this.notJoinedFromLeftOperand = new QueryResult();
				}
				this.notJoinedFromLeftOperand.addAll(bindings);
			} else if (or != null && or.getJoinPartnerFromLeftOperand() != null) {
				if (this.notJoinedFromLeftOperand != null) {
					this.notJoinedFromLeftOperand.removeAll(or
							.getJoinPartnerFromLeftOperand());
				}
				if (this.joinedFromLeftOperand == null) {
					this.joinedFromLeftOperand = or.getJoinPartnerFromLeftOperand();
				} else {
					this.joinedFromLeftOperand.addAll(or
							.getJoinPartnerFromLeftOperand());
				}
			}
			if (or == null || or.getJoinResult() == null
					|| or.getJoinResult().size() == 0) {
				return null;
			} else {
				return or.getJoinResult();
			}
		}
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		return this.preProcessMessage(msg, true);
	}

	public Message preProcessMessage(final EndOfEvaluationMessage msg,
			final boolean delete) {
		if (this.join != null) {
			this.join.setUnionVariables(this.unionVariables);
			this.join.setIntersectionVariables(this.intersectionVariables);
			final OptionalResult or = this.join.joinBeforeEndOfStream();

			if (or != null) {
				if (or.getJoinPartnerFromLeftOperand() != null
						&& or.getJoinPartnerFromLeftOperand().size() > 0) {
					this.notJoinedFromLeftOperand.removeAll(or
							.getJoinPartnerFromLeftOperand());
					if (!delete) {
						if (this.joinedFromLeftOperand == null) {
							this.joinedFromLeftOperand = QueryResult
									.createInstance();
						}
						this.joinedFromLeftOperand.addAll(or
								.getJoinPartnerFromLeftOperand());
					}
				}
				if (or.getJoinResult() != null && or.getJoinResult().size() > 0) {
					if (this.succeedingOperators.size() > 1) {
						or.getJoinResult().materialize();
					}
					for (final OperatorIDTuple opId : this.succeedingOperators) {
						opId.processAll(or.getJoinResult());
					}
				}
			}
		}
		if (this.notJoinedFromLeftOperand != null) {
			if (this.joinedFromLeftOperand != null) {
				this.notJoinedFromLeftOperand.removeAll(this.joinedFromLeftOperand);
				if (delete) {
					this.joinedFromLeftOperand.release();
					this.joinedFromLeftOperand = null;
				}
			}
			if (this.succeedingOperators.size() > 1) {
				this.notJoinedFromLeftOperand.materialize();
			}
			for (final OperatorIDTuple opId : this.succeedingOperators) {
				opId.processAll(this.notJoinedFromLeftOperand);
			}
			if (delete) {
				// notJoinedFromLeftOperand.release();
				this.notJoinedFromLeftOperand = null;
			}
		}
		return msg;
	}

	@Override
	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		return this.preProcessMessage(new EndOfEvaluationMessage(), false);
	}

	@Override
	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		if (this.join == null) {
			System.out
					.println("The embedded join operator has not been initialized in the optional operator:"
							+ this);
			return null;
		}
		if (this.precedingOperators.size() == 1) {
			return queryResult;
		} else {
			queryResult.materialize();
			if (queryResult.size() == 0) {
				return null;
			}
			this.join.setUnionVariables(this.unionVariables);
			this.join.setIntersectionVariables(this.intersectionVariables);
			this.join.deleteQueryResult(queryResult, operandID);
			if (operandID == 0) {
				if (this.notJoinedFromLeftOperand != null) {
					this.notJoinedFromLeftOperand.removeAll(queryResult);
				}
				if (this.joinedFromLeftOperand != null) {
					this.joinedFromLeftOperand.removeAll(queryResult);
				}
			}
			return null;
		}
	}

	@Override
	protected boolean isPipelineBreaker() {
		return true;
	}

	public QueryResult getNotJoinedFromLeftOperand() {
		return this.notJoinedFromLeftOperand;
	}

	public QueryResult getJoinedFromLeftOperand() {
		return this.joinedFromLeftOperand;
	}

	public Join getJoin() {
		return this.join;
	}

	@Override
	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		return this.preProcessMessageDebug(new EndOfEvaluationMessage(), debugstep,
				false);
	}

	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep, final boolean delete) {
		if (this.join != null) {
			this.join.setUnionVariables(this.unionVariables);
			this.join.setIntersectionVariables(this.intersectionVariables);
			final OptionalResult or = this.join.joinBeforeEndOfStream();

			if (or != null) {
				if (or.getJoinPartnerFromLeftOperand() != null
						&& or.getJoinPartnerFromLeftOperand().size() > 0) {
					this.notJoinedFromLeftOperand.removeAll(or
							.getJoinPartnerFromLeftOperand());
					if (!delete) {
						if (this.joinedFromLeftOperand == null) {
							this.joinedFromLeftOperand = QueryResult
									.createInstance();
						}
						this.joinedFromLeftOperand.addAll(or
								.getJoinPartnerFromLeftOperand());
					}
				}
				if (or.getJoinResult() != null && or.getJoinResult().size() > 0) {
					if (this.succeedingOperators.size() > 1) {
						or.getJoinResult().materialize();
					}
					for (final OperatorIDTuple opId : this.succeedingOperators) {
						final QueryResultDebug qrDebug = new QueryResultDebug(
								or.getJoinResult(), debugstep, this, opId
										.getOperator(), true);
						((Operator) opId.getOperator()).processAllDebug(
								qrDebug, opId.getId(), debugstep);
					}
				}
			}
		}
		if (this.notJoinedFromLeftOperand != null) {
			if (this.joinedFromLeftOperand != null) {
				this.notJoinedFromLeftOperand.removeAll(this.joinedFromLeftOperand);
				if (delete) {
					this.joinedFromLeftOperand.release();
					this.joinedFromLeftOperand = null;
				}
			}
			if (this.succeedingOperators.size() > 1) {
				this.notJoinedFromLeftOperand.materialize();
			}
			for (final OperatorIDTuple opId : this.succeedingOperators) {
				final QueryResultDebug qrDebug = new QueryResultDebug(
						this.notJoinedFromLeftOperand, debugstep, this, opId
								.getOperator(), true);
				((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
						.getId(), debugstep);
			}
			if (delete) {
				// notJoinedFromLeftOperand.release();
				this.notJoinedFromLeftOperand = null;
			}
		}
		return msg;
	}

	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		return this.preProcessMessageDebug(msg, debugstep, true);
	}
}
