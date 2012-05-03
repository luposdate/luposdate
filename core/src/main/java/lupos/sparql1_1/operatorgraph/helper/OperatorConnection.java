package lupos.sparql1_1.operatorgraph.helper;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;

/**
 * This class holds the operator to which a new operator will be connected. 
 * Additionally this class holds which operandID (0 = left operand, 1 = right operand, ...)
 * will be used for the new connection.
 */
public class OperatorConnection {

	private int operandID = 0;
	private BasicOperator operator;
	
	public OperatorConnection(final BasicOperator operator, final int operandID){
		this.setOperatorConnection(operator, operandID);
	}
	
	public OperatorConnection(final BasicOperator operator){
		this(operator, 0);
	}
	
	public void setOperatorConnection(final BasicOperator operator, final int operandID){
		this.operandID = operandID;
		this.operator = operator;		
	}

	/**
	 * Per default, operandID = 0 is assumed. 
	 */
	public void setOperatorConnection(final BasicOperator operator){
		this.setOperatorConnection(operator, 0);
	}
	
	public OperatorIDTuple getOperatorIDTuple(){
		return new OperatorIDTuple(operator, operandID);
	}

	/**
	 * Creates a connection between the given operator and the operator held in this object 
	 */
	public void connect(BasicOperator newOperator){
		newOperator.addSucceedingOperator(this.getOperatorIDTuple());
	}
	
	/**
	 * Creates a connection between the given operator and the operator held in this object 
	 * Additionally the given operator and operandID is stored in this object for later
	 * connections. 
	 */
	public void connectAndSetAsNewOperatorConnection(BasicOperator newOperator, final int operandID){
		this.connect(newOperator);
		this.setOperatorConnection(newOperator, operandID);
	}

	/**
	 * Creates a connection between the given operator and the operator held in this object 
	 * Additionally the given operator (and per default operandID = 0) is stored in this object for later
	 * connections. 
	 */
	public void connectAndSetAsNewOperatorConnection(BasicOperator newOperator){
		this.connectAndSetAsNewOperatorConnection(newOperator, 0);
	}
	
	public OperatorConnection clone(){
		return new OperatorConnection(operator, operandID);
	}
	
	/**
	 * increments the stored operandID (operandID++).
	 */
	public void incrementOperandID(){
		operandID++;
	}
}