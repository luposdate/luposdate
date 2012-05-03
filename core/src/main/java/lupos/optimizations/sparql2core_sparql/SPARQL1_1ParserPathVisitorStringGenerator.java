package lupos.optimizations.sparql2core_sparql;

import lupos.sparql1_1.*;

public interface SPARQL1_1ParserPathVisitorStringGenerator
{
  public String visit(SimpleNode node, String subject, String object);
  public String visit(ASTAVerbType node, String subject, String object);
  public String visit(ASTPathAlternative node, String subject, String object);
  public String visit(ASTPathSequence node, String subject, String object);
  public String visit(ASTInvers node, String subject, String object);
  public String visit(ASTArbitraryOccurences node, String subject, String object);
  public String visit(ASTOptionalOccurence node, String subject, String object);
  public String visit(ASTArbitraryOccurencesNotZero node, String subject, String object);
  public String visit(ASTGivenOccurences node, String subject, String object);
  public String visit(ASTNegatedPath node, String subject, String object);
  public String visit(ASTVar node, String subject, String object);
  public String visit(ASTQuotedURIRef node, String subject, String object);
  public String visit(ASTQName node, String subject, String object);
}