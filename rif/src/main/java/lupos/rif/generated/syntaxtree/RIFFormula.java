/* Generated by JTB 1.4.4 */
package lupos.rif.generated.syntaxtree;

import lupos.rif.generated.visitor.*;

/**
 * JTB node class for the production RIFFormula:<br>
 * Corresponding grammar :<br>
 * f0 -> . %0 #0 < AND > #1 < LPAREN ><br>
 * .. .. . .. #2 ( RIFFormula() )* #3 < RPAREN ><br>
 * .. .. | %1 #0 < OR > #1 < LPAREN ><br>
 * .. .. . .. #2 ( RIFFormula() )* #3 < RPAREN ><br>
 * .. .. | %2 #0 < EXISTS ><br>
 * .. .. . .. #1 ( RIFVar() )+ #2 < LPAREN > #3 RIFFormula() #4 < RPAREN ><br>
 * .. .. | %3 RIFAtomic()<br>
 */
public class RIFFormula implements INode {

  /** A child node */
  public NodeChoice f0;

  /** The parent pointer */
  private INode parent;

  /** The serial version uid */
  private static final long serialVersionUID = 144L;

  /**
   * Constructs the node with its child node.
   *
   * @param n0 the child node
   */
  public RIFFormula(final NodeChoice n0) {
    f0 = n0;
    if (f0 != null)
      f0.setParent(this);
  }

  /**
   * Accepts the IRetArguVisitor visitor.
   *
   * @param <R> the user return type
   * @param <A> the user argument type
   * @param vis the visitor
   * @param argu a user chosen argument
   * @return a user chosen return information
   */
  public <R, A> R accept(final IRetArguVisitor<R, A> vis, final A argu) {
    return vis.visit(this, argu);
  }

  /**
   * Accepts the IRetVisitor visitor.
   *
   * @param <R> the user return type
   * @param vis the visitor
   * @return a user chosen return information
   */
  public <R> R accept(final IRetVisitor<R> vis) {
    return vis.visit(this);
  }

  /**
   * Accepts the IVoidArguVisitor visitor.
   *
   * @param <A> the user argument type
   * @param vis the visitor
   * @param argu a user chosen argument
   */
  public <A> void accept(final IVoidArguVisitor<A> vis, final A argu) {
    vis.visit(this, argu);
  }

  /**
   * Accepts the IVoidVisitor visitor.
   *
   * @param vis the visitor
   */
  public void accept(final IVoidVisitor vis) {
    vis.visit(this);
  }

  /**
   * Setter for the parent node.
   *
   * @param n the parent node
   */
  public void setParent(final INode n) {
    parent = n;
  }

  /**
   * Getter for the parent node.
   *
   * @return the parent node
   */
  public INode getParent() {
    return parent;
  }

}
