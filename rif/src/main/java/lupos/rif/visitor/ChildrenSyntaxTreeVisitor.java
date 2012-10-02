/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

	private List<INode> list(INode... nodes) {
		List<INode> ret = new ArrayList<INode>();
		for (int i = 0; i < nodes.length; i++) {
			INode node = nodes[i];
			if (node instanceof INodeList)
				ret.addAll(list(node.accept(this, null).toArray(new INode[] {})));
			else if (node instanceof NodeOptional)
				ret.addAll(list(node.accept(this, null).toArray(new INode[] {})));
			else if (node instanceof NodeChoice)
				ret.addAll(list(((NodeChoice) node).choice));
			else if (node != null)
				ret.add(node);
		}
		return ret;
	}

	public List<INode> visit(NodeList n, Object argu) {
		return n.nodes;
	}

	public List<INode> visit(NodeListOptional n, Object argu) {
		return n.nodes != null ? n.nodes : new ArrayList<INode>();
	}

	public List<INode> visit(NodeOptional n, Object argu) {
		return n.node != null ? list(n.node) : new ArrayList<INode>();
	}

	public List<INode> visit(NodeSequence n, Object argu) {
		return n.nodes;
	}

	public List<INode> visit(NodeToken n, Object argu) {
		return list();
	}

	public List<INode> visit(CompilationUnit n, Object argu) {
		return list((INode) n.f0);
	}

	public List<INode> visit(RIFDocument n, Object argu) {
		return list(n.f0, n.f1, n.f2.node, n.f3, n.f4, n.f5.node, n.f6.node,
				n.f7);
	}

	public List<INode> visit(RIFBase n, Object argu) {
		return list(n.f0, n.f1, n.f2, n.f3);
	}

	public List<INode> visit(RIFPrefix n, Object argu) {
		return list(n.f0, n.f1, n.f2, n.f3, n.f4);
	}

	public List<INode> visit(RIFImport n, Object argu) {
		return list(n.f0, n.f1, n.f2, n.f3.node, n.f4);
	}

	public List<INode> visit(RIFGroup n, Object argu) {
		return list(n.f0, n.f1, n.f2, n.f3);
	}

	public List<INode> visit(RIFRule n, Object argu) {
		return list(n.f0.choice);
	}

	public List<INode> visit(RIFClause n, Object argu) {
		return list(n.f0.choice, n.f1.node);
	}

	public List<INode> visit(RIFFormula n, Object argu) {
		return list(n.f0.choice);
	}

	public List<INode> visit(RIFAtomic n, Object argu) {
		return list(n.f0, n.f1.node);
	}

	public List<INode> visit(RIFUniterm n, Object argu) {
		return list(n.f0, n.f1, n.f2, n.f3);
	}

	public List<INode> visit(RIFFrame n, Object argu) {
		return list(((RIFAtomic)n.getParent().getParent().getParent()).f0, n.f0, n.f1, n.f2);
	}

	public List<INode> visit(RIFTerm n, Object argu) {
		return list(n.f0.choice);
	}

	public List<INode> visit(RIFExternal n, Object argu) {
		return list(n.f0, n.f1, n.f2, n.f3);
	}

	public List<INode> visit(RIFList n, Object argu) {
		return list(n.f0, n.f1, n.f2.choice);
	}

	public List<INode> visit(RIFVar n, Object argu) {
		return list(n.f0, n.f1);
	}

	public List<INode> visit(RIFRDFLiteral n, Object argu) {
		return list(n.f0.choice);
	}

	public List<INode> visit(RIFTypedLiteral n, Object argu) {
		return list(n.f0, n.f2);
	}

	public List<INode> visit(RIFLiteralWithLangTag n, Object argu) {
		return list(n.f0, n.f1);
	}

	public List<INode> visit(RIFNumericLiteral n, Object argu) {
		return list(n.f0.choice);
	}

	public List<INode> visit(RIFString n, Object argu) {
		return list(n.f0.choice);
	}

	public List<INode> visit(RIFVarOrURI n, Object argu) {
		return list(n.f0.choice);
	}

	public List<INode> visit(RIFURI n, Object argu) {
		return list(n.f0.choice);
	}

	public List<INode> visit(RIFQName n, Object argu) {
		return list(n.f0);
	}

	public List<INode> visit(RIFInteger n, Object argu) {
		return list(n.f0);
	}

	public List<INode> visit(RIFFloatingPoint n, Object argu) {
		return list(n.f0);
	}

	public List<INode> visit(RIFNCName n, Object argu) {
		return list(n.f0);
	}

	public List<INode> visit(RIFQuotedURIref n, Object argu) {
		return list((INode) n.f0);
	}

	public List<INode> visit(RIFConclusion n, Object argu) {
		return list(n.f0, n.f1, n.f2, n.f3);
	}
}
