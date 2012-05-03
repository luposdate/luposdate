/* Generated By:JavaCC: Do not edit this line. SPARQL1_1ParserVisitor.java Version 5.0 */
package lupos.sparql1_1.operatorgraph;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.sparql1_1.*;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

public interface SPARQL1_1OperatorgraphGeneratorVisitor
{
  public void visit(ASTGroupConstraint node, OperatorConnection connection, Item graphConstraint);
  public void visit(ASTQuery node, OperatorConnection connection);
  public void visit(ASTSelectQuery node, OperatorConnection connection, Item graphConstraint);
  public void visit(ASTSelectQuery node, OperatorConnection connection);
  public void visit(ASTConstructQuery node, OperatorConnection connection);
  public void visit(ASTAskQuery node, OperatorConnection connection);
  public void visit(ASTOrderConditions node, OperatorConnection connection);
  public void visit(ASTLimit node, OperatorConnection connection);
  public void visit(ASTOffset node, OperatorConnection connection);
  public void visit(ASTLoad node, OperatorConnection connection);
  public void visit(ASTClear node, OperatorConnection connection);
  public void visit(ASTDrop node, OperatorConnection connection);
  public void visit(ASTCreate node, OperatorConnection connection);
  public void visit(ASTInsert node, OperatorConnection connection);
  public void visit(ASTDelete node, OperatorConnection connection);
  public void visit(ASTModify node, OperatorConnection connection);
  public void visit(ASTGraphConstraint node, OperatorConnection connection);
  public void visit(ASTOptionalConstraint node, OperatorConnection connection, Item graphConstraint);
  public void visit(ASTService node, OperatorConnection connection);
  public void visit(ASTMinus node, OperatorConnection connection, Item graphConstraint);
  public void visit(ASTUnionConstraint node, OperatorConnection connection, Item graphConstraint);
  public void visit(ASTFilterConstraint node, OperatorConnection connection, Item graphConstraint);
  public BasicOperator visit(ASTArbitraryOccurences node, OperatorConnection connection, Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode);
  public BasicOperator visit(ASTOptionalOccurence node, OperatorConnection connection, Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode);
  public BasicOperator visit(ASTArbitraryOccurencesNotZero node, OperatorConnection connection, Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode);
  public BasicOperator visit(ASTGivenOccurences node, OperatorConnection connection, Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode);
  public BasicOperator visit(ASTPathSequence node, OperatorConnection connection, Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode);
  public BasicOperator visit(ASTPathAlternative node, OperatorConnection connection, Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode);
  public BasicOperator visit(ASTNegatedPath node, OperatorConnection connection, Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode);
  public BasicOperator visit(ASTInvers node, OperatorConnection connection, Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode);
  public BasicOperator visit(ASTQuotedURIRef node, OperatorConnection connection, Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode);
  public void visit(ASTDefaultGraph node, OperatorConnection connection);
  public void visit(ASTNamedGraph node, OperatorConnection connection);
  // for stream-based evaluation:
  public void visit(final ASTWindow node, OperatorConnection connection, Item graphConstraint);
}
