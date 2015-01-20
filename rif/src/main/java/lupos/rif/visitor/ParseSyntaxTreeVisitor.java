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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;
import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.RIFException;
import lupos.rif.generated.syntaxtree.CompilationUnit;
import lupos.rif.generated.syntaxtree.INode;
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
import lupos.rif.model.AbstractExpressionContainer;
import lupos.rif.model.Conjunction;
import lupos.rif.model.Constant;
import lupos.rif.model.Disjunction;
import lupos.rif.model.Document;
import lupos.rif.model.Equality;
import lupos.rif.model.ExistExpression;
import lupos.rif.model.External;
import lupos.rif.model.Rule;
import lupos.rif.model.RuleList;
import lupos.rif.model.RulePredicate;
import lupos.rif.model.RuleVariable;
import lupos.rif.model.Uniterm;

/**
 * Visitor zum aufbauen der Datenstruktur in lupos.rif.model, welche als
 * Zwischenschicht zwischen AST und Operatorbaum steht.
 * 
 * @author jenskluttig
 * 
 */
public class ParseSyntaxTreeVisitor implements
		IRetArguVisitor<Object, IRuleNode> {
	/**
	 * Basisnamespace
	 */
	private String baseNamespace = null;
	/**
	 * Prefix -> Namespace
	 */
	public Map<String, String> prefixMap = null;

	public Object visit(final CompilationUnit n, final IRuleNode argu) {
		final Document resultDoc = (Document) n.f0.accept(this, null);
		return resultDoc;
	}

	public Object visit(final RIFDocument n, final IRuleNode argu) {
		final Document doc = new Document();
		doc.setParent(argu);

		final Constant baseNS = (Constant) n.f2.accept(this, doc);
		doc.setBaseNamespace(baseNS != null ? ((URILiteral) baseNS.getLiteral())
				.getString() : null);
		baseNamespace = doc.getBaseNamespace();

		for (final INode node : (List<INode>) n.f3.accept(this, doc)) {
			final String[] prefix = (String[]) node.accept(this, doc);
			doc.getPrefixMap().put(prefix[0], prefix[1]);
		}
		// standardprefixe
		if (!doc.getPrefixMap().containsKey("rif"))
			doc.getPrefixMap().put("rif", "http://www.w3.org/2007/rif#");
		if (!doc.getPrefixMap().containsKey("xs"))
			doc.getPrefixMap().put("xs", "http://www.w3.org/2001/XMLSchema#");
		if (!doc.getPrefixMap().containsKey("rdfs"))
			doc.getPrefixMap().put("rdfs",
					"http://www.w3.org/2000/01/rdf-schema#");
		if (!doc.getPrefixMap().containsKey("rdf"))
			doc.getPrefixMap().put("rdf",
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixMap = new HashMap<String, String>(doc.getPrefixMap());
		doc.setConclusion((IExpression) n.f5.accept(this, doc));
		final List<Rule> ruleList = (List<Rule>) n.f6.accept(this, doc);
		if (ruleList != null) {
			for (final Rule rule : ruleList)
				if (!rule.isImplication()
						&& rule.getDeclaredVariables().isEmpty())
					doc.getFacts().add(rule.getHead());
				else
					doc.getRules().add(rule);
		}
		return doc;
	}

	public Object visit(final RIFBase n, final IRuleNode argu) {
		return n.f2.accept(this, argu);
	}

	public Object visit(final RIFPrefix n, final IRuleNode argu) {
		return new String[] {
				(String) n.f2.accept(this, argu),
				((URILiteral) ((Constant) n.f3.accept(this, argu)).getLiteral())
						.getString() };
	}

	public Object visit(final RIFGroup n, final IRuleNode argu) {
		final List<Rule> ruleList = new ArrayList<Rule>();
		if (n.f2.present())
			for (final INode node : n.f2.nodes) {
				final Object sub = node.accept(this, argu);
				if (sub instanceof Rule)
					ruleList.add((Rule) sub);
				else
					ruleList.addAll((List<Rule>) sub);
			}
		return ruleList;
	}

	public Object visit(final RIFRule n, final IRuleNode argu) {
		final Rule rule = new Rule();
		rule.setParent(argu);
		RIFClause clause = null;
		// Wenn Variablen vorhanden
		if (n.f0.which == 0) {
			final List<INode> seq = (List<INode>) n.f0.choice.accept(this, rule);
			for (final INode node : ((List<INode>) seq.get(1).accept(this, rule)))
				rule.addVariable((RuleVariable) node.accept(this, rule));
			clause = (RIFClause) seq.get(3);
		} else
			clause = (RIFClause) n.f0.choice;
		clause.accept(this, rule);
		return rule;
	}

	public Object visit(final RIFClause n, final IRuleNode argu) {
		final Rule parent = (Rule) argu;
		// nur eine Aussage
		if (n.f0.which == 0)
			parent.setHead((IExpression) n.f0.choice.accept(this, argu));
		else {
			// And-verknuepfte Aussagen		
			final Conjunction conj = new Conjunction();
			conj.setParent(parent);
			for (final INode node : (List<INode>) ((List<INode>) n.f0.choice.accept(this, argu)).get(2).accept(this, argu))
				conj.addExpr((IExpression) node.accept(this, argu));
			if (!conj.isEmpty())
				parent.setHead(conj);
		}
		if (n.f1.present()){
			List<INode> bodyList = (List<INode>) n.f1.node.accept(this, argu);
			parent.setBody((IExpression) bodyList.get(1).accept(this, argu));
			for (final INode node : (List<INode>)(bodyList.get(2).accept(this, argu))){
				parent.addNot((IExpression)(((NodeSequence) node).nodes.get(1).accept(this, argu)));
			}
		}		
		return parent;
	}

	public Object visit(final RIFTerm n, final IRuleNode argu) {
		return n.f0.choice.accept(this, argu);
	}

	public Object visit(final RIFFormula n, final IRuleNode argu) {
		switch (n.f0.which) {
		case 0:
			final NodeListOptional andFormulas = (NodeListOptional) ((List<INode>) n.f0.choice
					.accept(this, argu)).get(2);
			final Conjunction conj = new Conjunction();
			conj.setParent(argu);
			for (final INode node : (List<INode>) andFormulas
					.accept(this, conj))
				conj.addExpr((IExpression) node.accept(this, conj));
			return conj;
		case 1:
			final NodeListOptional orFormulas = (NodeListOptional) ((List<INode>) n.f0.choice
					.accept(this, argu)).get(2);
			final Disjunction disj = new Disjunction();
			disj.setParent(argu);
			for (final INode node : (List<INode>) orFormulas.accept(this, disj))
				disj.addExpr((IExpression) node.accept(this, disj));
			return disj;
		case 2:
			final List<INode> existINodes = (List<INode>) n.f0.choice.accept(
					this, argu);
			final ExistExpression exists = new ExistExpression();
			exists.setParent(argu);
			for (final INode node : (List<INode>) existINodes.get(1).accept(
					this, exists))
				exists.addVariable((RuleVariable) node.accept(this, exists));
			exists.expr = (IExpression) existINodes.get(3).accept(this, exists);
			return exists;
		case 3:
			return n.f0.choice.accept(this, argu);
		default:
			return null;
		}
	}

	public Object visit(final RIFAtomic n, final IRuleNode argu) {
		if (n.f1.present()){
			if(((NodeChoice)n.f1.node).which == 1){			
				return ((NodeChoice)n.f1.node).choice.accept(this, argu);
			}
		}

		
		if (n.f1.node==null)
			return n.f0.accept(this, argu);
		else {
			final List<INode> seq = ((List<INode>)((NodeChoice) n.f1.node).choice.accept(this, argu));
			final String operator = (String) seq.get(0).accept(this, null);
			if (operator.equals("#") || operator.equals("##")) {
				final Uniterm term = new RulePredicate(true);
				term.setParent(argu);
				final IExpression leftTerm = (IExpression) n.f0.accept(this, term);
				final IExpression rightTerm = (IExpression) seq.get(1).accept(this, term);
				term.termParams.add(leftTerm);
				term.termParams.add(rightTerm);
				try {
					if (operator.equals("#"))
						term.termName = new Constant(
								LiteralFactory
										.createURILiteralWithoutLazyLiteral("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"),
								term);
					else

						term.termName = new Constant(
								LiteralFactory
										.createURILiteralWithoutLazyLiteral("<http://www.w3.org/2000/01/rdf-schema#subClassOf>"),
								term);
				} catch (URISyntaxException e) {
					throw new RIFException(e.getMessage());
				}
				return term;
			}
			final Equality comp = new Equality();
			comp.setParent(argu);
			comp.rightExpr = (IExpression) seq.get(1).accept(this, comp);
			comp.leftExpr = (IExpression) n.f0.accept(this, comp);
			return comp;
		}
	}

	public Object visit(final RIFUniterm n, final IRuleNode argu) {
		final Uniterm term = new RulePredicate(false);
		term.setParent(argu);
		term.termName = (IExpression) n.f0.accept(this, term);
		for (final INode node : (List<INode>) n.f2.accept(this, term)) {
			final NodeChoice choice = (NodeChoice) node;
			if (choice.which == 1)
				term.termParams.add((IExpression) choice.choice.accept(this,
						term));
			else
				term.termParams.add((IExpression) ((List<INode>) choice.choice
						.accept(this, term)).get(2).accept(this, term));
		}
		return term;
	}

	public Object visit(final RIFFrame n, final IRuleNode argu) {
		final List<INode> args = (List<INode>) n.f1.accept(this, argu);
		final AbstractExpressionContainer and = new Conjunction();
		for (final INode node : args) {
			final List<INode> nodeSeq = (List<INode>) node.accept(this, argu);
			final Uniterm term = new RulePredicate(true);
			term.setParent(argu);
			term.termName = (IExpression) nodeSeq.get(0).accept(this, term);
			term.termParams.add((IExpression) ((RIFAtomic)n.getParent().getParent().getParent()).f0.accept(this, term));
			term.termParams
					.add((IExpression) nodeSeq.get(2).accept(this, term));
			if (args.size() == 1)
				return term;
			else
				and.addExpr(term);
		}
		return and;
	}

	public Object visit(RIFConclusion n, IRuleNode argu) {
		return n.f2.accept(this, argu);
	}

	public Object visit(final RIFExternal n, final IRuleNode argu) {
		final Uniterm term = (Uniterm) n.f2.accept(this, argu);
		final External external = new External(term);
		external.setParent(argu);
		return external;
	}

	public Object visit(final RIFVar n, final IRuleNode argu) {
		final RuleVariable var = new RuleVariable((String) n.f1.accept(this,
				argu));
		var.setParent(argu);
		return var;
	}

	public Object visit(final RIFImport n, final IRuleNode argu) {
		throw new RIFException("IMPORT not supported!");
	}

	public Object visit(final RIFList n, final IRuleNode argu) {
		final RuleList result = new RuleList();
		result.setParent(argu);
		if (n.f2.which == 1) {
			List<INode> nodes = (List<INode>) n.f2.choice.accept(this, result);
			List<INode> terms = (List<INode>) nodes.get(0).accept(this, result);
			for (INode term : terms)
				result.addItem((IExpression) term.accept(this, result));
			// Tail anh�ngen
			if (((NodeOptional) nodes.get(1)).present()) {
				result.isOpen = true;
				result.addItem((IExpression) ((List<INode>) nodes.get(1)
						.accept(this, result)).get(1).accept(this, result));
			}
		}
		return result;
	}

	public Object visit(final RIFRDFLiteral n, final IRuleNode argu) {
		return n.f0.accept(this, argu);
	}

	public Object visit(final RIFTypedLiteral n, final IRuleNode argu) {
		final Literal content = ((Constant) n.f0.accept(this, argu))
				.getLiteral();
		final Literal type = ((Constant) n.f2.accept(this, argu)).getLiteral();
		Literal literal;
		try {
			literal = LiteralFactory.createTypedLiteralWithoutLazyLiteral(content.toString(),
					type.toString());
		} catch (final URISyntaxException e) {
			throw new RIFException(e.getMessage());
		}
		return new Constant(literal, argu);
	}

	public Object visit(final RIFLiteralWithLangTag n, final IRuleNode argu) {
		final TypedLiteral content = (TypedLiteral) ((Constant) n.f0.accept(
				this, argu)).getLiteral();
		final String langTag = (String) n.f1.accept(this, argu);
		final Literal literal = LiteralFactory.createLanguageTaggedLiteralWithoutLazyLiteral(
				content.getContent(), langTag);
		return new Constant(literal, argu);
	}

	public Object visit(final RIFNumericLiteral n, final IRuleNode argu) {
		return n.f0.accept(this, argu);
	}

	public Object visit(final RIFString n, final IRuleNode argu) {
		final String content = (String) n.f0.accept(this, argu);
//		Literal literal = null;
//		try {
//			literal = LiteralFactory.createTypedLiteralWithoutLazyLiteral(content,
//					"<http://www.w3.org/2001/XMLSchema#string>");
//		} catch (final URISyntaxException e) {
//			throw new RIFException(e.getMessage());
//		}
		Literal literal = LiteralFactory.createLiteral(content);
		return new Constant(literal, argu);
	}

	public Object visit(final RIFVarOrURI n, final IRuleNode argu) {
		return n.f0.accept(this, argu);
	}

	public Object visit(final RIFURI n, final IRuleNode argu) {
		return n.f0.accept(this, argu);
	}

	public Object visit(final RIFQName n, final IRuleNode argu) {
		final String content = (String) n.f0.accept(this, argu);
		Literal literal = null;
		final String[] parts = content.split(":");
		if (parts[0].equals("")) {
			if (baseNamespace == null)
				throw new RIFException("Literal " + content
						+ " requires Declaration of BASE!");
			try {
				literal = LiteralFactory.createURILiteralWithoutLazyLiteral("<" + baseNamespace
						+ parts[1] + ">");
			} catch (final URISyntaxException e) {
				throw new RIFException(e.toString());
			}
		} else {
			if (!prefixMap.containsKey(parts[0]))
				throw new RIFException("Undeclared Prefix " + parts[0]);
			try {
				literal = LiteralFactory.createURILiteralWithoutLazyLiteral("<"
						+ prefixMap.get(parts[0]) + parts[1] + ">");
			} catch (final URISyntaxException e) {
				throw new RIFException(e.toString());
			}
		}
		return new Constant(literal, argu);
	}

	public Object visit(final RIFInteger n, final IRuleNode argu) {
		final String content = (String) n.f0.accept(this, argu);
		Literal literal;
		try {
			literal = LiteralFactory.createTypedLiteralWithoutLazyLiteral("\"" + content + "\"",
					"<http://www.w3.org/2001/XMLSchema#integer>");
		} catch (final URISyntaxException e) {
			throw new RIFException(e.toString());
		}
		return new Constant(literal, argu);
	}

	public Object visit(final RIFFloatingPoint n, final IRuleNode argu) {
		final String content = (String) n.f0.accept(this, argu);
		Literal literal = null;
		if (content.contains("e") || content.contains("E"))
			try {
				literal = LiteralFactory.createTypedLiteralWithoutLazyLiteral("\"" + content
						+ "\"", "<http://www.w3.org/2001/XMLSchema#double>");
			} catch (final URISyntaxException e) {
				throw new RIFException(e.toString());
			}
		else
			try {
				literal = LiteralFactory.createTypedLiteralWithoutLazyLiteral("\"" + content
						+ "\"", "<http://www.w3.org/2001/XMLSchema#decimal>");
			} catch (final URISyntaxException e) {
				throw new RIFException(e.toString());
			}
		return new Constant(literal, argu);
	}

	public Object visit(final RIFNCName n, final IRuleNode argu) {
		return n.f0.accept(this, argu);
	}

	public Object visit(final RIFQuotedURIref n, final IRuleNode argu) {
		final String uriRef = (String) n.f0.accept(this, argu);
		Literal literal;
		try {
			literal = LiteralFactory.createURILiteralWithoutLazyLiteral(uriRef);
		} catch (final URISyntaxException e) {
			throw new RIFException(e.toString());
		}
		return new Constant(literal, argu);
	}

	/* JTB-spezifische Klassen */

	public Object visit(final NodeList n, final IRuleNode argu) {
		return n.nodes;
	}

	public Object visit(final NodeListOptional n, final IRuleNode argu) {
		return n.present() ? n.nodes : new ArrayList<INode>();
	}

	public Object visit(final NodeOptional n, final IRuleNode argu) {
		return n.present() ? n.node.accept(this, argu) : null;
	}

	public Object visit(final NodeSequence n, final IRuleNode argu) {
		return n.nodes;
	}

	public Object visit(final NodeToken n, final IRuleNode argu) {
		return n.tokenImage;
	}
}
