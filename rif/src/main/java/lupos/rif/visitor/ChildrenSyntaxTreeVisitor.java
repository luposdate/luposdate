/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.rif.visitor;

import java.util.ArrayList;
import java.util.List;

import lupos.rif.generated.syntaxtree.CompilationUnit;
import lupos.rif.generated.syntaxtree.INode;
import lupos.rif.generated.syntaxtree.INodeList;
import lupos.rif.generated.syntaxtree.NodeChoice;
import lupos.rif.generated.syntaxtree.NodeList;
import lupos.rif.generated.syntaxtree.NodeListOptional;
import lupos.rif.generated.syntaxtree.NodeOptional;
import lupos.rif.generated.syntaxtree.NodeSequence;
import lupos.rif.generated.syntaxtree.NodeToken;
import lupos.rif.generated.syntaxtree.RIFAtomic;
import lupos.rif.generated.syntaxtree.RIFBase;
import lupos.rif.generated.syntaxtree.RIFClause;
import lupos.rif.generated.syntaxtree.RIFConclusion;
import lupos.rif.generated.syntaxtree.RIFDocument;
import lupos.rif.generated.syntaxtree.RIFExternal;
import lupos.rif.generated.syntaxtree.RIFFloatingPoint;
import lupos.rif.generated.syntaxtree.RIFFormula;
import lupos.rif.generated.syntaxtree.RIFFrame;
import lupos.rif.generated.syntaxtree.RIFGroup;
import lupos.rif.generated.syntaxtree.RIFImport;
import lupos.rif.generated.syntaxtree.RIFInteger;
import lupos.rif.generated.syntaxtree.RIFList;
import lupos.rif.generated.syntaxtree.RIFLiteralWithLangTag;
import lupos.rif.generated.syntaxtree.RIFNCName;
import lupos.rif.generated.syntaxtree.RIFNumericLiteral;
import lupos.rif.generated.syntaxtree.RIFPrefix;
import lupos.rif.generated.syntaxtree.RIFQName;
import lupos.rif.generated.syntaxtree.RIFQuotedURIref;
import lupos.rif.generated.syntaxtree.RIFRDFLiteral;
import lupos.rif.generated.syntaxtree.RIFRule;
import lupos.rif.generated.syntaxtree.RIFString;
import lupos.rif.generated.syntaxtree.RIFTerm;
import lupos.rif.generated.syntaxtree.RIFTypedLiteral;
import lupos.rif.generated.syntaxtree.RIFURI;
import lupos.rif.generated.syntaxtree.RIFUniterm;
import lupos.rif.generated.syntaxtree.RIFVar;
import lupos.rif.generated.syntaxtree.RIFVarOrURI;
import lupos.rif.generated.visitor.IRetArguVisitor;
public class ChildrenSyntaxTreeVisitor implements IRetArguVisitor<List<INode>, Object> {

	private List<INode> list(final INode... nodes) {
		final List<INode> ret = new ArrayList<INode>();
		for (int i = 0; i < nodes.length; i++) {
			final INode node = nodes[i];
			if (node instanceof INodeList) {
				ret.addAll(this.list(node.accept(this, null).toArray(new INode[] {})));
			} else if (node instanceof NodeOptional) {
				ret.addAll(this.list(node.accept(this, null).toArray(new INode[] {})));
			} else if (node instanceof NodeChoice) {
				ret.addAll(this.list(((NodeChoice) node).choice));
			} else if (node != null) {
				ret.add(node);
			}
		}
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final NodeList n, final Object argu) {
		return n.nodes;
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final NodeListOptional n, final Object argu) {
		return n.nodes != null ? n.nodes : new ArrayList<INode>();
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final NodeOptional n, final Object argu) {
		return n.node != null ? this.list(n.node) : new ArrayList<INode>();
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final NodeSequence n, final Object argu) {
		return n.nodes;
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final NodeToken n, final Object argu) {
		return this.list();
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final CompilationUnit n, final Object argu) {
		return this.list((INode) n.f0);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFDocument n, final Object argu) {
		return this.list(n.f0, n.f1, n.f2.node, n.f3, n.f4, n.f5.node, n.f6.node,
				n.f7);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFBase n, final Object argu) {
		return this.list(n.f0, n.f1, n.f2, n.f3);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFPrefix n, final Object argu) {
		return this.list(n.f0, n.f1, n.f2, n.f3, n.f4);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFImport n, final Object argu) {
		return this.list(n.f0, n.f1, n.f2, n.f3.node, n.f4);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFGroup n, final Object argu) {
		return this.list(n.f0, n.f1, n.f2, n.f3);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFRule n, final Object argu) {
		return this.list(n.f0.choice);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFClause n, final Object argu) {
		return this.list(n.f0.choice, n.f1.node);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFFormula n, final Object argu) {
		return this.list(n.f0.choice);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFAtomic n, final Object argu) {
		return this.list(n.f0, n.f1.node);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFUniterm n, final Object argu) {
		return this.list(n.f0, n.f1, n.f2, n.f3);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFFrame n, final Object argu) {
		return this.list(n.f0, n.f1, n.f2);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFTerm n, final Object argu) {
		return this.list(n.f0.choice);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFExternal n, final Object argu) {
		return this.list(n.f0, n.f1, n.f2, n.f3);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFList n, final Object argu) {
		return this.list(n.f0, n.f1, n.f2.choice);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFVar n, final Object argu) {
		return this.list(n.f0, n.f1);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFRDFLiteral n, final Object argu) {
		return this.list(n.f0.choice);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFTypedLiteral n, final Object argu) {
		return this.list(n.f0, n.f2);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFLiteralWithLangTag n, final Object argu) {
		return this.list(n.f0, n.f1);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFNumericLiteral n, final Object argu) {
		return this.list(n.f0.choice);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFString n, final Object argu) {
		return this.list(n.f0.choice);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFVarOrURI n, final Object argu) {
		return this.list(n.f0.choice);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFURI n, final Object argu) {
		return this.list(n.f0.choice);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFQName n, final Object argu) {
		return this.list(n.f0);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFInteger n, final Object argu) {
		return this.list(n.f0);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFFloatingPoint n, final Object argu) {
		return this.list(n.f0);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFNCName n, final Object argu) {
		return this.list(n.f0);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFQuotedURIref n, final Object argu) {
		return this.list((INode) n.f0);
	}

	/** {@inheritDoc} */
	@Override
	public List<INode> visit(final RIFConclusion n, final Object argu) {
		return this.list(n.f0, n.f1, n.f2, n.f3);
	}
}
