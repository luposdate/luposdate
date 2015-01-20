/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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