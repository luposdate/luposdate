package lupos.engine.operators.application;

import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;

public class CollectResult implements Application {
	protected QueryResult qr;
	protected GraphResult gr;
	protected List<BooleanResult> br_list;
	protected Application.Type type;

	public void call(final QueryResult res) {
		if (res != null) {
			if (res instanceof GraphResult) {
				if (gr == null)
					gr = new GraphResult(((GraphResult) res).getTemplate());
				gr.addAll((GraphResult) res);
			} else if (res instanceof BooleanResult) {
				if (br_list == null)
					br_list = new LinkedList<BooleanResult>();
				final BooleanResult br = new BooleanResult();
				br.addAll(res);
				br_list.add(br);
			} else {
				if (qr == null)
					qr = QueryResult.createInstance();
				qr.addAll(res);
			}
		}
	}

	public void start(final Type type) {
		qr = null;
		final QueryResult qr;
		gr = null;
		br_list = null;
		this.type = type;
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
		QueryResult result = qr;
		if (result == null)
			result = gr;
		if (result == null && br_list != null)
			result = br_list.get(0);

		if (result == null) {
			if (type == null)
				return null;
			switch (type) {
			case ASK:
				return new BooleanResult();
			case CONSTRUCT:
				return new GraphResult();
			default:
				return QueryResult.createInstance();
			}
		}
		return result;
	}

	public void deleteResult(final QueryResult res) {
		if (res instanceof GraphResult) {
			if (gr != null)
				gr.removeAll(res);
		} else if (res instanceof BooleanResult) {
			if (br_list != null) {
				for (final BooleanResult br : br_list)
					br.removeAll(res);
			}
		} else if (qr != null) {
			qr.removeAll(res);
		}
	}

	public void deleteResult() {
		if (qr != null)
			qr.release();
		if (gr != null)
			gr.release();
		if (br_list != null) {
			for (final BooleanResult br : br_list) {
				br.release();
			}
		}
		qr = null;
		gr = null;
		br_list = null;
	}

	public QueryResult[] getQueryResults() {
		final int size = (qr == null ? 0 : 1) + (gr == null ? 0 : 1)
				+ (br_list == null ? 0 : br_list.size());
		final QueryResult[] result = new QueryResult[size];
		int index = 0;
		if (qr != null)
			result[index++] = qr;
		if (gr != null)
			result[index++] = gr;
		if (br_list != null) {
			for (final BooleanResult br : br_list)
				result[index++] = br;
		}
		return result;
	}
}
