/**
 * 
 */
package lupos.engine.operators;

import java.io.Serializable;

/**
 * @author groppe This is the interface of the simple visitor...
 */
public interface SimpleOperatorGraphVisitor extends Serializable {
	public Object visit(BasicOperator basicOperator); 
}
