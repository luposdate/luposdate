package lupos.engine.operators.multiinput.optional;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.multiinput.join.NestedLoopJoin;

public class NaiveOptional extends Optional {
		public NaiveOptional() {
			super(new NestedLoopJoin(2));
		}

	public void cloneFrom(BasicOperator op) {
		super.cloneFrom(op);
		((NestedLoopJoin) join).init();
	}
}
