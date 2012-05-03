/**
 * 
 */
package lupos.engine.operators;

import java.io.Serializable;

/**
 * @author groppe This is the interface of the visitor...
 */
public interface OperatorGraphVisitor<E> extends Serializable {
	public Object visit(BasicOperator basicOperator, E data);
}
