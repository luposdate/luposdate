/* Generated By:JJTree: Do not edit this line. ASTHaving.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package lupos.sparql1_1;

import lupos.sparql1_1.operatorgraph.SPARQL1_1OperatorgraphGeneratorVisitor;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

public
class ASTHaving extends SimpleNode {
  public ASTHaving(int id) {
    super(id);
  }

  public ASTHaving(SPARQL1_1Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
    public String accept(lupos.optimizations.sparql2core_sparql.SPARQL1_1ParserVisitorStringGenerator visitor) {
    return visitor.visit(this);
  }

  public Object jjtAccept(SPARQL1_1ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=35e0c927e726af07071f99ffe384b6b6 (do not edit this line) */
