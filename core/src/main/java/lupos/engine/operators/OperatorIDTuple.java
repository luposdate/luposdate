package lupos.engine.operators;

import java.io.Serializable;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;

public class OperatorIDTuple extends lupos.misc.util.OperatorIDTuple<BasicOperator> implements Serializable {
	private static final long serialVersionUID = 1416179591924778885L;

	//	private BasicOperator op;
	//	private int id;

	public OperatorIDTuple(BasicOperator op, int id) {
		super(op, id);
	}

	//	@Override
	//	public String toString() {
	//		return id + ": " + op;
	//	}
	//
	//	public int getId() {
	//		return id;
	//	}
	//
	//	public void setId(final int id) {
	//		this.id = id;
	//	}
	//
	//	public BasicOperator getOperator() {
	//		return op;
	//	}
	//
	//	public void setOperator(final BasicOperator op) {
	//		this.op = op;
	//	}

	public void processAll(final Bindings b) {
		final QueryResult bindings = QueryResult.createInstance();
		bindings.add(b);
		((Operator) this.op).processAll(bindings, this.id);
	}

	public void processAll(final QueryResult qr) {
		((Operator) this.op).processAll(qr, this.id);
	}

	//	@Override
	//	public boolean equals(final Object o) {
	//		if (o instanceof OperatorIDTuple) {
	//			final OperatorIDTuple oid = (OperatorIDTuple) o;
	//			if (oid.getOperator().equals(this.getOperator())) {
	//				if (this.getId() < 0 || oid.getId() < 0)
	//					return true;
	//				else
	//					return this.getId() == oid.getId();
	//			} else
	//				return false;
	//		} else
	//			return false;
	//	}
}
