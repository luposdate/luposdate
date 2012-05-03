/**
 * 
 */
package lupos.engine.operators;

import java.io.Serializable;

/**
 * This is the interface of the visitor...
 */
public interface OperatorGraphVisitor<E> extends Serializable {
	public Object visit(BasicOperator basicOperator, E data);
}
