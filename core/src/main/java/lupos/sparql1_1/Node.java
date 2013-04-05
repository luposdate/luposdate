/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
/* Generated By:JJTree: Do not edit this line. Node.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package lupos.sparql1_1;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.NotBoundException;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.engine.operators.singleinput.ExpressionEvaluation.EvaluationVisitor;
import lupos.optimizations.sparql2core_sparql.SPARQL1_1ParserPathVisitorStringGenerator;
import lupos.sparql1_1.operatorgraph.SPARQL1_1OperatorgraphGeneratorVisitor;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

/* All AST nodes must implement this interface.  It provides basic
   machinery for constructing the parent and child relationships
   between nodes. */

public
interface Node {

	/** This method is called after the node has been made the current
    node.  It indicates that child nodes can now be added to it. */
	public void jjtOpen();

	/** This method is called after all the child nodes have been
    added. */
	public void jjtClose();

	/** This pair of methods are used to inform the node of its
    parent. */
	public void jjtSetParent(Node n);
	public Node jjtGetParent();

	/** This method tells the node to add its argument to the node's
    list of children.  */
	public void jjtAddChild(Node n, int i);

	/** This method returns a child node.  The children are numbered
     from zero, left to right. */
	public Node jjtGetChild(int i);

	/** Return the number of children the node has. */
	public int jjtGetNumChildren();

	/** Accept the visitor. **/
	public Object jjtAccept(SPARQL1_1ParserVisitor visitor, Object data);
	
	public String accept(lupos.optimizations.sparql2core_sparql.SPARQL1_1ParserVisitorStringGenerator visitor);
	
	public String accept(SPARQL1_1ParserPathVisitorStringGenerator visitor, String subject, String object);
	
	public void accept(lupos.sparql1_1.operatorgraph.SPARQL1_1OperatorgraphGeneratorVisitor visitor, OperatorConnection connection);
	
    public void accept(lupos.sparql1_1.operatorgraph.SPARQL1_1OperatorgraphGeneratorVisitor visitor, final OperatorConnection connection, Item graphConstraint);

    public BasicOperator accept(SPARQL1_1OperatorgraphGeneratorVisitor visitor, OperatorConnection connection, Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode);
	
	@SuppressWarnings("rawtypes")
	public Object accept(EvaluationVisitor visitor, Bindings b, Object data) throws NotBoundException, TypeErrorException;

	public Node[] getChildren();

	public void removeChild(int i);

	public void removeChild(Node n);

	public void clearChildren();

	public boolean replaceChild2(Node node, Node n);

	public SimpleNode clone(boolean clean);

	public SimpleNode cloneStillChild(boolean clean);

	public void addChild(Node n);

	public void addChild(Node n, final int i);

	public int getChildNumber(Node node);

}
/* JavaCC - OriginalChecksum=2e7de2c76147d43ac0b5c1ac0f346f1d (do not edit this line) */
