package lupos.engine.operators.multiinput;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;

public abstract class MultiInputOperator extends Operator {
	public int getNumberOfOperands() {
		int maxOperandID = 0;
		for (final BasicOperator bo : getPrecedingOperators()) {
			maxOperandID = Math.max(maxOperandID, bo.getOperatorIDTuple(this)
					.getId());
		}
		return maxOperandID + 1;
	}
}
