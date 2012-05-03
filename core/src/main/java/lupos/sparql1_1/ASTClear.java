/* Generated By:JJTree: Do not edit this line. ASTClear.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package lupos.sparql1_1;

import lupos.sparql1_1.operatorgraph.SPARQL1_1OperatorgraphGeneratorVisitor;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

public
class ASTClear extends SimpleNode {

	private boolean silent = false;

	public void setSilent(){
		silent=true;
	}

	public boolean isSilent(){
		return silent;
	}

	public ASTClear(int id) {
		super(id);
	}

	public ASTClear(SPARQL1_1Parser p, int id) {
		super(p, id);
	}


	/** Accept the visitor. **/
	  public String accept(lupos.optimizations.sparql2core_sparql.SPARQL1_1ParserVisitorStringGenerator visitor) {
    return visitor.visit(this);
  }

	    public void accept(SPARQL1_1OperatorgraphGeneratorVisitor visitor, final OperatorConnection connection) {
	  	    visitor.visit(this, connection);
	    }

  public Object jjtAccept(SPARQL1_1ParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
/* JavaCC - OriginalChecksum=388b8ecba31c744fd864e5446bc643ef (do not edit this line) */
