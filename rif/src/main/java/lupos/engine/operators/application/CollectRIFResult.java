package lupos.engine.operators.application;

import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.rif.datatypes.EqualityResult;
import lupos.rif.datatypes.RuleResult;

public class CollectRIFResult extends CollectResult {
	protected RuleResult rr;
	protected EqualityResult er;

	public void call(final QueryResult res) {
		if (res != null) {
			if (res instanceof EqualityResult) {
				if (er == null)
					er = new EqualityResult();
				er.getEqualityResult().addAll(
						((EqualityResult) res).getEqualityResult());
			} else if (res instanceof RuleResult) {
				if (rr == null)
					rr = new RuleResult();
				rr.getPredicateResults().addAll(
						((RuleResult) res).getPredicateResults());
			} else super.call(res);
		}
	}

	public void start(final Type type) {
		super.start(type);
		er = null;
		rr = null;
	}

	public void stop() {
	}

	/**
	 * get result, if there are several types of QueryResults, one of them is
	 * returned...
	 * 
	 * @return
	 */
	public QueryResult getResult() {
		QueryResult result = rr;
		if (result == null)
			result = er;		
		if (result == null)
			return super.getResult();
		else return result;
	}

	public void deleteResult(final QueryResult res) {
		if (res instanceof EqualityResult) {
			if (er != null)
				er.removeAll(res);
		} else if (res instanceof RuleResult) {
			if (rr != null)
				rr.removeAll(res);
		} super.deleteResult(res);
	}

	public void deleteResult() {
		if (rr != null)
			rr.release();
		if (er != null)
			er.release();
		rr = null;
		er = null;
		super.deleteResult();
	}

	public QueryResult[] getQueryResults() {
		final int size = (qr == null ? 0 : 1) + (rr == null ? 0 : 1)
				+ (er == null ? 0 : 1) + (gr == null ? 0 : 1)
				+ (br_list == null ? 0 : br_list.size());
		final QueryResult[] result = new QueryResult[size];
		int index = 0;
		if (qr != null)
			result[index++] = qr;
		if (rr != null)
			result[index++] = rr;
		if (gr != null)
			result[index++] = gr;
		if (er != null)
			result[index++] = er;
		if (br_list != null) {
			for (final BooleanResult br : br_list)
				result[index++] = br;
		}
		return result;
	}
}
