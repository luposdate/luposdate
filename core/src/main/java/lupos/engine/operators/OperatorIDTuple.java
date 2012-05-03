package lupos.engine.operators;

import java.io.Serializable;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;

public class OperatorIDTuple extends lupos.misc.util.OperatorIDTuple<BasicOperator> implements Serializable {
	private static final long serialVersionUID = 1416179591924778885L;

	public OperatorIDTuple(BasicOperator op, int id) {
		super(op, id);
	}

	public void processAll(final Bindings b) {
		final QueryResult bindings = QueryResult.createInstance();
		bindings.add(b);
		((Operator) this.op).processAll(bindings, this.id);
	}

	public void processAll(final QueryResult qr) {
		((Operator) this.op).processAll(qr, this.id);
	}
}
