/* Generated By:JJTree: Do not edit this line. ASTOptionalOccurence.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package lupos.sparql1_1;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.optimizations.sparql2core_sparql.SPARQL1_1ParserPathVisitorStringGenerator;
import lupos.sparql1_1.operatorgraph.SPARQL1_1OperatorgraphGeneratorVisitor;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

public
class ASTOptionalOccurence extends SimpleNode {
  public ASTOptionalOccurence(int id) {
    super(id);
  }

  public ASTOptionalOccurence(SPARQL1_1Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
    public String accept(lupos.optimizations.sparql2core_sparql.SPARQL1_1ParserVisitorStringGenerator visitor) {
    return visitor.visit(this);
  }

    public BasicOperator accept(SPARQL1_1OperatorgraphGeneratorVisitor visitor, OperatorConnection connection, Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode) {
  	    return visitor.visit(this, connection, graphConstraint, subject, object, subjectNode, objectNode);
    }

  public Object jjtAccept(SPARQL1_1ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  public String accept(SPARQL1_1ParserPathVisitorStringGenerator visitor, String subject, String object){
	    return visitor.visit(this, subject, object);
  }
}
/* JavaCC - OriginalChecksum=a37f8ca5c3a3ef8c2c3447a287ed4ad4 (do not edit this line) */
