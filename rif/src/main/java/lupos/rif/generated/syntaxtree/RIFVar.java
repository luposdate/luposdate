/* Generated by JTB 1.4.4 */
package lupos.rif.generated.syntaxtree;

import lupos.rif.generated.visitor.*;

/**
 * JTB node class for the production RIFVar:<br>
 * Corresponding grammar :<br>
 * f0 -> < QUESTION ><br>
 * f1 -> RIFNCName()<br>
 */
public class RIFVar implements INode {

  /** A child node */
  public NodeToken f0;

  /** A child node */
  public RIFNCName f1;

  /** The parent pointer */
  private INode parent;

  /** The serial version uid */
  private static final long serialVersionUID = 144L;

  /**
   * Constructs the node with all its children nodes.
   *
   * @param n0 first child node
   * @param n1 next child node
   */
  public RIFVar(final NodeToken n0, final RIFNCName n1) {
    f0 = n0;
    if (f0 != null)
      f0.setParent(this);
    f1 = n1;
    if (f1 != null)
      f1.setParent(this);
  }

  /**
   * Constructs the node with only its non NodeToken child node(s).
   *
   * @param n0 first child node
   */
  public RIFVar(final RIFNCName n0) {
    f0 = new NodeToken("?");
    if (f0 != null)
        f0.setParent(this);
    f1 = n0;
    if (f1 != null)
        f1.setParent(this);
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
