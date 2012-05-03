/* Generated By:JJTree: Do not edit this line. ASTHoursFuncNode.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package lupos.sparql1_1;

import lupos.datastructures.bindings.Bindings;
import lupos.engine.operators.singleinput.NotBoundException;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.engine.operators.singleinput.ExpressionEvaluation.EvaluationVisitor;

public
class ASTHoursFuncNode extends SimpleNode {
  public ASTHoursFuncNode(int id) {
    super(id);
  }

  public ASTHoursFuncNode(SPARQL1_1Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
    public String accept(lupos.optimizations.sparql2core_sparql.SPARQL1_1ParserVisitorStringGenerator visitor) {
    return visitor.visit(this);
  }

  public Object jjtAccept(SPARQL1_1ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Object accept(EvaluationVisitor visitor, Bindings b, Object data) throws NotBoundException, TypeErrorException {
	    return visitor.evaluate(this, b, data);
  }
}
/* JavaCC - OriginalChecksum=86b35db9b57d285ded8e32c49b995448 (do not edit this line) */
