package lupos.engine.operators.singleinput;

import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.QueryResult;

public class MakeBooleanResult extends SingleInputOperator {

	@Override
	public QueryResult process(QueryResult qr, final int operandID) {
		final BooleanResult br = new BooleanResult();
		if (!qr.isEmpty())
			br.add(qr.oneTimeIterator().next());
		qr.release();
		qr = null;
		return br;
	}
}
