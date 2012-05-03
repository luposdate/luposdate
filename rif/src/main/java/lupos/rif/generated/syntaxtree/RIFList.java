/* Generated by JTB 1.4.4 */
package lupos.rif.generated.syntaxtree;

import lupos.rif.generated.visitor.*;

/**
 * JTB node class for the production RIFList:<br>
 * Corresponding grammar :<br>
 * f0 -> < LIST ><br>
 * f1 -> < LPAREN ><br>
 * f2 -> ( %0 < RPAREN ><br>
 * .. .. | %1 #0 ( RIFTerm() )+<br>
 * .. .. . .. #1 ( $0 < S > $1 RIFTerm() )? #2 < RPAREN > )<br>
 */
public class RIFList implements INode {

  /** A child node */
  public NodeToken f0;

  /** A child node */
  public NodeToken f1;

  /** A child node */
  public NodeChoice f2;

  /** The parent pointer */
  private INode parent;

  /** The serial version uid */
  private static final long serialVersionUID = 144L;

  /**
   * Constructs the node with all its children nodes.
   *
   * @param n0 first child node
   * @param n1 next child node
   * @param n2 next child node
   */
  public RIFList(final NodeToken n0, final NodeToken n1, final NodeChoice n2) {
    f0 = n0;
    if (f0 != null)
      f0.setParent(this);
    f1 = n1;
    if (f1 != null)
      f1.setParent(this);
    f2 = n2;
    if (f2 != null)
      f2.setParent(this);
  }

  /**
   * Constructs the node with only its non NodeToken child node(s).
   *
   * @param n0 first child node
   */
  public RIFList(final NodeChoice n0) {
    f0 = new NodeToken("list");
    if (f0 != null)
        f0.setParent(this);
    f1 = new NodeToken("(");
    if (f1 != null)
        f1.setParent(this);
    f2 = n0;
    if (f2 != null)
        f2.setParent(this);
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
