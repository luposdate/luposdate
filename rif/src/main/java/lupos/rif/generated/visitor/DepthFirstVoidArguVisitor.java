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
/* Generated by JTB 1.4.4 */
package lupos.rif.generated.visitor;

import lupos.rif.generated.syntaxtree.*;
import java.util.*;

/**
 * Provides default methods which visit each node in the tree in depth-first order.<br>
 * In your "VoidArgu" visitors extend this class and override part or all of these methods.
 *
 * @param <A> The user argument type
 */
public class DepthFirstVoidArguVisitor<A> implements IVoidArguVisitor<A> {


  /*
   * Base nodes classes visit methods (to be overridden if necessary)
   */

  /**
   * Visits a {@link NodeChoice} node, passing it an argument.
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final NodeChoice n, final A argu) {
    n.choice.accept(this, argu);
    return;
  }

  /**
   * Visits a {@link NodeList} node, passing it an argument.
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final NodeList n, final A argu) {
    for (final Iterator<INode> e = n.elements(); e.hasNext();) {
      e.next().accept(this, argu);
    }
    return;
  }

  /**
   * Visits a {@link NodeListOptional} node, passing it an argument.
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final NodeListOptional n, final A argu) {
    if (n.present()) {
      for (final Iterator<INode> e = n.elements(); e.hasNext();) {
        e.next().accept(this, argu);
        }
      return;
    } else
      return;
  }

  /**
   * Visits a {@link NodeOptional} node, passing it an argument.
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final NodeOptional n, final A argu) {
    if (n.present()) {
      n.node.accept(this, argu);
      return;
    } else
    return;
  }

  /**
   * Visits a {@link NodeSequence} node, passing it an argument.
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final NodeSequence n, final A argu) {
    for (final Iterator<INode> e = n.elements(); e.hasNext();) {
      e.next().accept(this, argu);
    }
    return;
  }

  /**
   * Visits a {@link NodeToken} node, passing it an argument.
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final NodeToken n, @SuppressWarnings("unused") final A argu) {
    @SuppressWarnings("unused")
    final String tkIm = n.tokenImage;
    return;
  }

  /*
   * User grammar generated visit methods (to be overridden if necessary)
   */

  /**
   * Visits a {@link CompilationUnit} node, whose children are the following :
   * <p>
   * f0 -> RIFDocument()<br>
   * f1 -> < EOF ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final CompilationUnit n, final A argu) {
    // f0 -> RIFDocument()
    n.f0.accept(this, argu);
    // f1 -> < EOF >
    n.f1.accept(this, argu);
  }

  /**
   * Visits a {@link RIFDocument} node, whose children are the following :
   * <p>
   * f0 -> < DOCUMENT ><br>
   * f1 -> < LPAREN ><br>
   * f2 -> ( RIFBase() )?<br>
   * f3 -> ( RIFPrefix() )*<br>
   * f4 -> ( RIFImport() )*<br>
   * f5 -> ( RIFConclusion() )?<br>
   * f6 -> ( RIFGroup() )?<br>
   * f7 -> < RPAREN ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFDocument n, final A argu) {
    // f0 -> < DOCUMENT >
    n.f0.accept(this, argu);
    // f1 -> < LPAREN >
    n.f1.accept(this, argu);
    // f2 -> ( RIFBase() )?
    n.f2.accept(this, argu);
    // f3 -> ( RIFPrefix() )*
    n.f3.accept(this, argu);
    // f4 -> ( RIFImport() )*
    n.f4.accept(this, argu);
    // f5 -> ( RIFConclusion() )?
    n.f5.accept(this, argu);
    // f6 -> ( RIFGroup() )?
    n.f6.accept(this, argu);
    // f7 -> < RPAREN >
    n.f7.accept(this, argu);
  }

  /**
   * Visits a {@link RIFConclusion} node, whose children are the following :
   * <p>
   * f0 -> < CONC ><br>
   * f1 -> < LPAREN ><br>
   * f2 -> RIFFormula()<br>
   * f3 -> < RPAREN ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFConclusion n, final A argu) {
    // f0 -> < CONC >
    n.f0.accept(this, argu);
    // f1 -> < LPAREN >
    n.f1.accept(this, argu);
    // f2 -> RIFFormula()
    n.f2.accept(this, argu);
    // f3 -> < RPAREN >
    n.f3.accept(this, argu);
  }

  /**
   * Visits a {@link RIFBase} node, whose children are the following :
   * <p>
   * f0 -> < BASE ><br>
   * f1 -> < LPAREN ><br>
   * f2 -> RIFQuotedURIref()<br>
   * f3 -> < RPAREN ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFBase n, final A argu) {
    // f0 -> < BASE >
    n.f0.accept(this, argu);
    // f1 -> < LPAREN >
    n.f1.accept(this, argu);
    // f2 -> RIFQuotedURIref()
    n.f2.accept(this, argu);
    // f3 -> < RPAREN >
    n.f3.accept(this, argu);
  }

  /**
   * Visits a {@link RIFPrefix} node, whose children are the following :
   * <p>
   * f0 -> < PREFIX ><br>
   * f1 -> < LPAREN ><br>
   * f2 -> RIFNCName()<br>
   * f3 -> RIFQuotedURIref()<br>
   * f4 -> < RPAREN ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFPrefix n, final A argu) {
    // f0 -> < PREFIX >
    n.f0.accept(this, argu);
    // f1 -> < LPAREN >
    n.f1.accept(this, argu);
    // f2 -> RIFNCName()
    n.f2.accept(this, argu);
    // f3 -> RIFQuotedURIref()
    n.f3.accept(this, argu);
    // f4 -> < RPAREN >
    n.f4.accept(this, argu);
  }

  /**
   * Visits a {@link RIFImport} node, whose children are the following :
   * <p>
   * f0 -> < IMPORT ><br>
   * f1 -> < LPAREN ><br>
   * f2 -> RIFQuotedURIref()<br>
   * f3 -> ( RIFQuotedURIref() )?<br>
   * f4 -> < RPAREN ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFImport n, final A argu) {
    // f0 -> < IMPORT >
    n.f0.accept(this, argu);
    // f1 -> < LPAREN >
    n.f1.accept(this, argu);
    // f2 -> RIFQuotedURIref()
    n.f2.accept(this, argu);
    // f3 -> ( RIFQuotedURIref() )?
    n.f3.accept(this, argu);
    // f4 -> < RPAREN >
    n.f4.accept(this, argu);
  }

  /**
   * Visits a {@link RIFGroup} node, whose children are the following :
   * <p>
   * f0 -> < GROUP ><br>
   * f1 -> < LPAREN ><br>
   * f2 -> ( %0 RIFRule()<br>
   * .. .. | %1 RIFGroup() )*<br>
   * f3 -> < RPAREN ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFGroup n, final A argu) {
    // f0 -> < GROUP >
    n.f0.accept(this, argu);
    // f1 -> < LPAREN >
    n.f1.accept(this, argu);
    // f2 -> ( %0 RIFRule()
    // .. .. | %1 RIFGroup() )*
    n.f2.accept(this, argu);
    // f3 -> < RPAREN >
    n.f3.accept(this, argu);
  }

  /**
   * Visits a {@link RIFRule} node, whose children are the following :
   * <p>
   * f0 -> . %0 #0 < FORALL ><br>
   * .. .. . .. #1 ( RIFVar() )+ #2 < LPAREN > #3 RIFClause() #4 < RPAREN ><br>
   * .. .. | %1 RIFClause()<br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFRule n, final A argu) {
    // f0 -> . %0 #0 < FORALL >
    // .. .. . .. #1 ( RIFVar() )+ #2 < LPAREN > #3 RIFClause() #4 < RPAREN >
    // .. .. | %1 RIFClause()
    n.f0.accept(this, argu);
  }

  /**
   * Visits a {@link RIFClause} node, whose children are the following :
   * <p>
   * f0 -> ( %0 RIFAtomic()<br>
   * .. .. | %1 #0 < AND > #1 < LPAREN ><br>
   * .. .. . .. #2 ( RIFAtomic() )* #3 < RPAREN > )<br>
   * f1 -> ( #0 < IMPL > #1 RIFFormula()<br>
   * .. .. . #2 ( $0 < NOT > $1 RIFFormula() )* )?<br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFClause n, final A argu) {
    // f0 -> ( %0 RIFAtomic()
    // .. .. | %1 #0 < AND > #1 < LPAREN >
    // .. .. . .. #2 ( RIFAtomic() )* #3 < RPAREN > )
    n.f0.accept(this, argu);
    // f1 -> ( #0 < IMPL > #1 RIFFormula()
    // .. .. . #2 ( $0 < NOT > $1 RIFFormula() )* )?
    n.f1.accept(this, argu);
  }

  /**
   * Visits a {@link RIFFormula} node, whose children are the following :
   * <p>
   * f0 -> . %0 #0 < AND > #1 < LPAREN ><br>
   * .. .. . .. #2 ( RIFFormula() )* #3 < RPAREN ><br>
   * .. .. | %1 #0 < OR > #1 < LPAREN ><br>
   * .. .. . .. #2 ( RIFFormula() )* #3 < RPAREN ><br>
   * .. .. | %2 #0 < EXISTS ><br>
   * .. .. . .. #1 ( RIFVar() )+ #2 < LPAREN > #3 RIFFormula() #4 < RPAREN ><br>
   * .. .. | %3 RIFAtomic()<br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFFormula n, final A argu) {
    // f0 -> . %0 #0 < AND > #1 < LPAREN >
    // .. .. . .. #2 ( RIFFormula() )* #3 < RPAREN >
    // .. .. | %1 #0 < OR > #1 < LPAREN >
    // .. .. . .. #2 ( RIFFormula() )* #3 < RPAREN >
    // .. .. | %2 #0 < EXISTS >
    // .. .. . .. #1 ( RIFVar() )+ #2 < LPAREN > #3 RIFFormula() #4 < RPAREN >
    // .. .. | %3 RIFAtomic()
    n.f0.accept(this, argu);
  }

  /**
   * Visits a {@link RIFAtomic} node, whose children are the following :
   * <p>
   * f0 -> RIFTerm()<br>
   * f1 -> ( %0 ( #0 ( &0 < EQUAL ><br>
   * .. .. . .. | &1 < R ><br>
   * .. .. . .. | &2 < RR > ) #1 RIFTerm() )<br>
   * .. .. | %1 RIFFrame() )?<br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFAtomic n, final A argu) {
    // f0 -> RIFTerm()
    n.f0.accept(this, argu);
    // f1 -> ( %0 ( #0 ( &0 < EQUAL >
    // .. .. . .. | &1 < R >
    // .. .. . .. | &2 < RR > ) #1 RIFTerm() )
    // .. .. | %1 RIFFrame() )?
    n.f1.accept(this, argu);
  }

  /**
   * Visits a {@link RIFUniterm} node, whose children are the following :
   * <p>
   * f0 -> RIFVarOrURI()<br>
   * f1 -> < LPAREN ><br>
   * f2 -> ( %0 #0 RIFNCName() #1 < TO > #2 RIFTerm()<br>
   * .. .. | %1 RIFTerm() )*<br>
   * f3 -> < RPAREN ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFUniterm n, final A argu) {
    // f0 -> RIFVarOrURI()
    n.f0.accept(this, argu);
    // f1 -> < LPAREN >
    n.f1.accept(this, argu);
    // f2 -> ( %0 #0 RIFNCName() #1 < TO > #2 RIFTerm()
    // .. .. | %1 RIFTerm() )*
    n.f2.accept(this, argu);
    // f3 -> < RPAREN >
    n.f3.accept(this, argu);
  }

  /**
   * Visits a {@link RIFFrame} node, whose children are the following :
   * <p>
   * f0 -> < LBRACK ><br>
   * f1 -> ( #0 RIFTerm() #1 < TO > #2 RIFTerm() )*<br>
   * f2 -> < RBRACK ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFFrame n, final A argu) {
    // f0 -> < LBRACK >
    n.f0.accept(this, argu);
    // f1 -> ( #0 RIFTerm() #1 < TO > #2 RIFTerm() )*
    n.f1.accept(this, argu);
    // f2 -> < RBRACK >
    n.f2.accept(this, argu);
  }

  /**
   * Visits a {@link RIFTerm} node, whose children are the following :
   * <p>
   * f0 -> . %0 RIFUniterm()<br>
   * .. .. | %1 RIFRDFLiteral()<br>
   * .. .. | %2 RIFNumericLiteral()<br>
   * .. .. | %3 RIFVar()<br>
   * .. .. | %4 RIFURI()<br>
   * .. .. | %5 RIFExternal()<br>
   * .. .. | %6 RIFList()<br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFTerm n, final A argu) {
    // f0 -> . %0 RIFUniterm()
    // .. .. | %1 RIFRDFLiteral()
    // .. .. | %2 RIFNumericLiteral()
    // .. .. | %3 RIFVar()
    // .. .. | %4 RIFURI()
    // .. .. | %5 RIFExternal()
    // .. .. | %6 RIFList()
    n.f0.accept(this, argu);
  }

  /**
   * Visits a {@link RIFExternal} node, whose children are the following :
   * <p>
   * f0 -> < EXTERNAL ><br>
   * f1 -> < LPAREN ><br>
   * f2 -> RIFUniterm()<br>
   * f3 -> < RPAREN ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFExternal n, final A argu) {
    // f0 -> < EXTERNAL >
    n.f0.accept(this, argu);
    // f1 -> < LPAREN >
    n.f1.accept(this, argu);
    // f2 -> RIFUniterm()
    n.f2.accept(this, argu);
    // f3 -> < RPAREN >
    n.f3.accept(this, argu);
  }

  /**
   * Visits a {@link RIFList} node, whose children are the following :
   * <p>
   * f0 -> < LIST ><br>
   * f1 -> < LPAREN ><br>
   * f2 -> ( %0 < RPAREN ><br>
   * .. .. | %1 #0 ( RIFTerm() )+<br>
   * .. .. . .. #1 ( $0 < S > $1 RIFTerm() )? #2 < RPAREN > )<br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFList n, final A argu) {
    // f0 -> < LIST >
    n.f0.accept(this, argu);
    // f1 -> < LPAREN >
    n.f1.accept(this, argu);
    // f2 -> ( %0 < RPAREN >
    // .. .. | %1 #0 ( RIFTerm() )+
    // .. .. . .. #1 ( $0 < S > $1 RIFTerm() )? #2 < RPAREN > )
    n.f2.accept(this, argu);
  }

  /**
   * Visits a {@link RIFRDFLiteral} node, whose children are the following :
   * <p>
   * f0 -> . %0 RIFTypedLiteral()<br>
   * .. .. | %1 RIFLiteralWithLangTag()<br>
   * .. .. | %2 RIFString()<br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFRDFLiteral n, final A argu) {
    // f0 -> . %0 RIFTypedLiteral()
    // .. .. | %1 RIFLiteralWithLangTag()
    // .. .. | %2 RIFString()
    n.f0.accept(this, argu);
  }

  /**
   * Visits a {@link RIFTypedLiteral} node, whose children are the following :
   * <p>
   * f0 -> RIFString()<br>
   * f1 -> < H ><br>
   * f2 -> RIFURI()<br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFTypedLiteral n, final A argu) {
    // f0 -> RIFString()
    n.f0.accept(this, argu);
    // f1 -> < H >
    n.f1.accept(this, argu);
    // f2 -> RIFURI()
    n.f2.accept(this, argu);
  }

  /**
   * Visits a {@link RIFLiteralWithLangTag} node, whose children are the following :
   * <p>
   * f0 -> RIFString()<br>
   * f1 -> < LANGTAG ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFLiteralWithLangTag n, final A argu) {
    // f0 -> RIFString()
    n.f0.accept(this, argu);
    // f1 -> < LANGTAG >
    n.f1.accept(this, argu);
  }

  /**
   * Visits a {@link RIFNumericLiteral} node, whose children are the following :
   * <p>
   * f0 -> . %0 RIFInteger()<br>
   * .. .. | %1 RIFFloatingPoint()<br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFNumericLiteral n, final A argu) {
    // f0 -> . %0 RIFInteger()
    // .. .. | %1 RIFFloatingPoint()
    n.f0.accept(this, argu);
  }

  /**
   * Visits a {@link RIFString} node, whose children are the following :
   * <p>
   * f0 -> . %0 < STRING_LITERAL1 ><br>
   * .. .. | %1 < STRING_LITERAL2 ><br>
   * .. .. | %2 < STRING_LITERALLONG1 ><br>
   * .. .. | %3 < STRING_LITERALLONG2 ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFString n, final A argu) {
    // f0 -> . %0 < STRING_LITERAL1 >
    // .. .. | %1 < STRING_LITERAL2 >
    // .. .. | %2 < STRING_LITERALLONG1 >
    // .. .. | %3 < STRING_LITERALLONG2 >
    n.f0.accept(this, argu);
  }

  /**
   * Visits a {@link RIFVarOrURI} node, whose children are the following :
   * <p>
   * f0 -> . %0 RIFVar()<br>
   * .. .. | %1 RIFURI()<br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFVarOrURI n, final A argu) {
    // f0 -> . %0 RIFVar()
    // .. .. | %1 RIFURI()
    n.f0.accept(this, argu);
  }

  /**
   * Visits a {@link RIFURI} node, whose children are the following :
   * <p>
   * f0 -> . %0 RIFQuotedURIref()<br>
   * .. .. | %1 RIFQName()<br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFURI n, final A argu) {
    // f0 -> . %0 RIFQuotedURIref()
    // .. .. | %1 RIFQName()
    n.f0.accept(this, argu);
  }

  /**
   * Visits a {@link RIFQName} node, whose children are the following :
   * <p>
   * f0 -> < QNAME ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFQName n, final A argu) {
    // f0 -> < QNAME >
    n.f0.accept(this, argu);
  }

  /**
   * Visits a {@link RIFInteger} node, whose children are the following :
   * <p>
   * f0 -> < INTEGER_10 ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFInteger n, final A argu) {
    // f0 -> < INTEGER_10 >
    n.f0.accept(this, argu);
  }

  /**
   * Visits a {@link RIFFloatingPoint} node, whose children are the following :
   * <p>
   * f0 -> < FLOATING_POINT ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFFloatingPoint n, final A argu) {
    // f0 -> < FLOATING_POINT >
    n.f0.accept(this, argu);
  }

  /**
   * Visits a {@link RIFVar} node, whose children are the following :
   * <p>
   * f0 -> < QUESTION ><br>
   * f1 -> RIFNCName()<br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFVar n, final A argu) {
    // f0 -> < QUESTION >
    n.f0.accept(this, argu);
    // f1 -> RIFNCName()
    n.f1.accept(this, argu);
  }

  /**
   * Visits a {@link RIFNCName} node, whose children are the following :
   * <p>
   * f0 -> < NCNAME ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFNCName n, final A argu) {
    // f0 -> < NCNAME >
    n.f0.accept(this, argu);
  }

  /**
   * Visits a {@link RIFQuotedURIref} node, whose children are the following :
   * <p>
   * f0 -> < Q_URIref ><br>
   *
   * @param n the node to visit
   * @param argu the user argument
   */
  public void visit(final RIFQuotedURIref n, final A argu) {
    // f0 -> < Q_URIref >
    n.f0.accept(this, argu);
  }

}
