/* Generated By:JJTree: Do not edit this line. ASTGroup.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package lupos.sparql1_1;

import lupos.sparql1_1.operatorgraph.SPARQL1_1OperatorgraphGeneratorVisitor;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

public
class ASTGroup extends SimpleNode {
  public ASTGroup(int id) {
    super(id);
  }

  public ASTGroup(SPARQL1_1Parser p, int id) {
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
/* JavaCC - OriginalChecksum=8cc8c08549edb8f8f0ac20fc5631d7c2 (do not edit this line) */
