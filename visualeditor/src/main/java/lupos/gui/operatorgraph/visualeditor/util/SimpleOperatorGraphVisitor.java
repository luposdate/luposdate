package lupos.gui.operatorgraph.visualeditor.util;

import java.io.Serializable;

import lupos.gui.operatorgraph.visualeditor.operators.Operator;

/**
 * @author groppe This is the interface of the simple visitor...
 */
public interface SimpleOperatorGraphVisitor extends Serializable {
	public Object visit(Operator basicOperator);
}
