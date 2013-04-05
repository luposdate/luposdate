/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/* Generated By:JavaCC: Do not edit this line. JJTTSVParserState.java Version 5.0 */
package lupos.endpoint.client.formatreader.tsv;

public class JJTTSVParserState {
  private java.util.List<Node> nodes;
  private java.util.List<Integer> marks;

  private int sp;        // number of nodes on stack
  private int mk;        // current mark
  private boolean node_created;

  public JJTTSVParserState() {
    nodes = new java.util.ArrayList<Node>();
    marks = new java.util.ArrayList<Integer>();
    sp = 0;
    mk = 0;
  }

  /* Determines whether the current node was actually closed and
     pushed.  This should only be called in the final user action of a
     node scope.  */
  public boolean nodeCreated() {
    return node_created;
  }

  /* Call this to reinitialize the node stack.  It is called
     automatically by the parser's ReInit() method. */
  public void reset() {
    nodes.clear();
    marks.clear();
    sp = 0;
    mk = 0;
  }

  /* Returns the root node of the AST.  It only makes sense to call
     this after a successful parse. */
  public Node rootNode() {
    return nodes.get(0);
  }

  /* Pushes a node on to the stack. */
  public void pushNode(Node n) {
    nodes.add(n);
    ++sp;
  }

  /* Returns the node on the top of the stack, and remove it from the
     stack.  */
  public Node popNode() {
    if (--sp < mk) {
      mk = marks.remove(marks.size()-1);
    }
    return nodes.remove(nodes.size()-1);
  }

  /* Returns the node currently on the top of the stack. */
  public Node peekNode() {
    return nodes.get(nodes.size()-1);
  }

  /* Returns the number of children on the stack in the current node
     scope. */
  public int nodeArity() {
    return sp - mk;
  }


  public void clearNodeScope(Node n) {
    while (sp > mk) {
      popNode();
    }
    mk = marks.remove(marks.size()-1);
  }


  public void openNodeScope(Node n) {
    marks.add(mk);
    mk = sp;
    n.jjtOpen();
  }


  /* A definite node is constructed from a specified number of
     children.  That number of nodes are popped from the stack and
     made the children of the definite node.  Then the definite node
     is pushed on to the stack. */
  public void closeNodeScope(Node n, int num) {
    mk = marks.remove(marks.size()-1);
    while (num-- > 0) {
      Node c = popNode();
      c.jjtSetParent(n);
      n.jjtAddChild(c, num);
    }
    n.jjtClose();
    pushNode(n);
    node_created = true;
  }


  /* A conditional node is constructed if its condition is true.  All
     the nodes that have been pushed since the node was opened are
     made children of the conditional node, which is then pushed
     on to the stack.  If the condition is false the node is not
     constructed and they are left on the stack. */
  public void closeNodeScope(Node n, boolean condition) {
    if (condition) {
      int a = nodeArity();
      mk = marks.remove(marks.size()-1);
      while (a-- > 0) {
        Node c = popNode();
        c.jjtSetParent(n);
        n.jjtAddChild(c, a);
      }
      n.jjtClose();
      pushNode(n);
      node_created = true;
    } else {
      mk = marks.remove(marks.size()-1);
      node_created = false;
    }
  }
}
/* JavaCC - OriginalChecksum=db3230f4b58e785e0cd710e1052e3926 (do not edit this line) */
