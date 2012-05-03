package lupos.engine.operators.multiinput.optional;

import lupos.datastructures.queryresult.QueryResult;

public class OptionalResult {
	private QueryResult joinResult;
	private QueryResult joinPartnerFromLeftOperand;

	public OptionalResult(final QueryResult joinResult,
			final QueryResult joinPartnerFromLeftOperand) {
		this.joinResult = joinResult;
		this.joinPartnerFromLeftOperand = joinPartnerFromLeftOperand;
	}

	public OptionalResult() {
	}

	public QueryResult getJoinResult() {
		return joinResult;
	}

	public void setJoinResult(final QueryResult joinResult) {
		this.joinResult = joinResult;
	}

	public QueryResult getJoinPartnerFromLeftOperand() {
		return joinPartnerFromLeftOperand;
	}

	public void setJoinPartnerFromLeftOperand(
			final QueryResult joinPartnerFromLeftOperand) {
		this.joinPartnerFromLeftOperand = joinPartnerFromLeftOperand;
	}

	public void addAll(final OptionalResult or) {
		joinResult.addAll(or.joinResult);
		joinPartnerFromLeftOperand.addAll(or.joinPartnerFromLeftOperand);
	}

	public void release() {
		joinResult.release();
		joinPartnerFromLeftOperand.release();
	}

	@Override
	public String toString() {
		return "joinResult: " + joinResult + " joinPartnerFromLeftOperand: "
				+ joinPartnerFromLeftOperand;
	}
}
