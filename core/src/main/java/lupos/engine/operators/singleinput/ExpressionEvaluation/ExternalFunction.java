package lupos.engine.operators.singleinput.ExpressionEvaluation;

import lupos.engine.operators.singleinput.TypeErrorException;

public interface ExternalFunction {
	public Object evaluate(Object[] args) throws TypeErrorException;
}
