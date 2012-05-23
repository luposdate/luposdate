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
package lupos.gui.operatorgraph.visualeditor.queryeditor.parsing;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Ask;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Construct;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.ConstructTemplateContainer;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Describe;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Filter;
import lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.QueryRDFTerm;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.RetrieveData;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.RetrieveDataWithProjectionAndSolutionModifier;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.RetrieveDataWithSolutionModifier;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Select;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Select.DistinctState;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Union;
import lupos.gui.operatorgraph.visualeditor.queryeditor.util.SortContainer;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.misc.util.OperatorIDTuple;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.ASTAdditionNode;
import lupos.sparql1_1.ASTAndNode;
import lupos.sparql1_1.ASTArguments;
import lupos.sparql1_1.ASTAs;
import lupos.sparql1_1.ASTAscOrder;
import lupos.sparql1_1.ASTAskQuery;
import lupos.sparql1_1.ASTBaseDecl;
import lupos.sparql1_1.ASTBnodeFuncNode;
import lupos.sparql1_1.ASTBlankNode;
import lupos.sparql1_1.ASTBooleanLiteral;
import lupos.sparql1_1.ASTBoundFuncNode;
import lupos.sparql1_1.ASTConstructQuery;
import lupos.sparql1_1.ASTConstructTemplate;
import lupos.sparql1_1.ASTDataTypeFuncNode;
import lupos.sparql1_1.ASTDefaultGraph;
import lupos.sparql1_1.ASTDescOrder;
import lupos.sparql1_1.ASTDescribeQuery;
import lupos.sparql1_1.ASTDivisionNode;
import lupos.sparql1_1.ASTDoubleCircumflex;
import lupos.sparql1_1.ASTEmptyNode;
import lupos.sparql1_1.ASTEqualsNode;
import lupos.sparql1_1.ASTFilterConstraint;
import lupos.sparql1_1.ASTFloatingPoint;
import lupos.sparql1_1.ASTFunctionCall;
import lupos.sparql1_1.ASTGraphConstraint;
import lupos.sparql1_1.ASTGreaterThanEqualsNode;
import lupos.sparql1_1.ASTGreaterThanNode;
import lupos.sparql1_1.ASTGroupConstraint;
import lupos.sparql1_1.ASTIriFuncNode;
import lupos.sparql1_1.ASTInteger;
import lupos.sparql1_1.ASTLangFuncNode;
import lupos.sparql1_1.ASTLangMatchesFuncNode;
import lupos.sparql1_1.ASTLangTag;
import lupos.sparql1_1.ASTLessThanEqualsNode;
import lupos.sparql1_1.ASTLessThanNode;
import lupos.sparql1_1.ASTLimit;
import lupos.sparql1_1.ASTMinusNode;
import lupos.sparql1_1.ASTMultiplicationNode;
import lupos.sparql1_1.ASTNamedGraph;
import lupos.sparql1_1.ASTNotEqualsNode;
import lupos.sparql1_1.ASTNotNode;
import lupos.sparql1_1.ASTOffset;
import lupos.sparql1_1.ASTOptionalConstraint;
import lupos.sparql1_1.ASTOrNode;
import lupos.sparql1_1.ASTOrderConditions;
import lupos.sparql1_1.ASTPlusNode;
import lupos.sparql1_1.ASTPrefixDecl;
import lupos.sparql1_1.ASTQuery;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.ASTRDFLiteral;
import lupos.sparql1_1.ASTRegexFuncNode;
import lupos.sparql1_1.ASTSameTermFuncNode;
import lupos.sparql1_1.ASTSelectQuery;
import lupos.sparql1_1.ASTStrFuncNode;
import lupos.sparql1_1.ASTStringLiteral;
import lupos.sparql1_1.ASTSubtractionNode;
import lupos.sparql1_1.ASTTripleSet;
import lupos.sparql1_1.ASTUriFuncNode;
import lupos.sparql1_1.ASTUnionConstraint;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.SPARQL1_1ParserVisitor;
import lupos.sparql1_1.SimpleNode;

public abstract class SPARQLCoreParserVisitorImplementation implements SPARQL1_1ParserVisitor {
	protected HashMap<String, String> prefixList = new HashMap<String, String>();
	protected Prefix prefix;

	// helper method: visits all children of the current node
	public String visitChildren(final Node n, final Object data) {
		final int numberChildren = n.jjtGetNumChildren();

		String value = "";

		for (int i = 0; i < numberChildren; i++) {
			final Object o = n.jjtGetChild(i).jjtAccept(this, data);

			if (o != null)
				value += o.toString();
		}

		return value;
	}

	public String visitChild(final Node n, final Object data, final int index) {
		return (String) (n.jjtGetChild(index).jjtAccept(this, data));
	}

	public Object visit(final SimpleNode node, final Object data) {
		return visitChildren(node, data);
	}

	public abstract Object visit(ASTQuery node, Object data);

	public Object visit(final ASTBaseDecl node, final Object data) {
		if (node.jjtGetChild(0) instanceof ASTQuotedURIRef)
			prefixList.put("", visitChildren(node, data));

		return data;
	}

	public Object visit(final ASTPrefixDecl node, final Object data) {
		if (node.jjtGetChild(0) instanceof ASTQuotedURIRef)
			prefixList.put(node.getPrefix(), visitChildren(node, data));

		return data;
	}

	public Object visit(final ASTSelectQuery node, final Object data) {
		final Select s = new Select(this.prefix);

		this.handleWhereClause(node, s);

		if (!node.isSelectAll())
			this.handleProjections(node, s);

		if (node.isDistinct())
			s.distinctState = DistinctState.DISTINCT;
		else if (node.isReduced())
			s.distinctState = DistinctState.REDUCED;

		this.handleDatasetClause(node, s);
		this.handleSolutionModifier(node, s);

		return s;
	}

	private void handleProjections(final SimpleNode node,
			final RetrieveDataWithProjectionAndSolutionModifier operator) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if (node.jjtGetChild(i) instanceof ASTVar) {
				final ASTVar var = (ASTVar) node.jjtGetChild(i);

				try {
					operator.addProjectionElement(var.toQueryString());
				} catch (final ModificationException e) {
					e.printStackTrace();
				}
			} else if (node.jjtGetChild(i) instanceof ASTQuotedURIRef) {
				final ASTQuotedURIRef var = (ASTQuotedURIRef) node
				.jjtGetChild(i);

				try {
					operator.addProjectionElement(var.toQueryString());
				} catch (final ModificationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void handleWhereClause(final SimpleNode node,
			final RetrieveData operator) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if (node.jjtGetChild(i) instanceof ASTGroupConstraint) {
				final Operator testOP = (Operator) node.jjtGetChild(i)
				.jjtAccept(this, null);

				operator.addSucceedingOperator(new OperatorIDTuple<Operator>(testOP, 0));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void handleSolutionModifier(final SimpleNode node,
			final RetrieveDataWithSolutionModifier operator) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			try {
				if (node.jjtGetChild(i) instanceof ASTOrderConditions) // ORDER
					// BY...
					operator.setNewOrderByList((LinkedList<SortContainer>) node
							.jjtGetChild(i).jjtAccept(this, null));

				if (node.jjtGetChild(i) instanceof ASTOffset) // OFFSET...
					operator.setOffsetValue(((Integer) node.jjtGetChild(i)
							.jjtAccept(this, null)).toString());

				if (node.jjtGetChild(i) instanceof ASTLimit) // LIMIT...
					operator.setLimitValue(((Integer) node.jjtGetChild(i)
							.jjtAccept(this, null)).toString());
			} catch (final ModificationException me) {
				me.printStackTrace();
			}
		}
	}

	private void handleDatasetClause(final SimpleNode node,
			final RetrieveData operator) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			try {

				if (node.jjtGetChild(i) instanceof ASTDefaultGraph) // dealing
					// with FROM
					// clauses
					// ...
					operator.addFromItem((String) node.jjtGetChild(i)
							.jjtAccept(this, null));

				if (node.jjtGetChild(i) instanceof ASTNamedGraph) // dealing
					// with FROM
					// NAMED
					// clauses
					// ...
					operator.addFromNamedItem((String) node.jjtGetChild(i)
							.jjtAccept(this, null));
			} catch (final ModificationException me) {
				me.printStackTrace();
			}
		}
	}

	public Object visit(final ASTConstructQuery node, final Object data) {
		final Construct c = new Construct(this.prefix);

		this.handleWhereClause(node, c);

		this.handleDatasetClause(node, c);
		this.handleSolutionModifier(node, c);

		final OperatorContainer constructTemplateOp = (OperatorContainer) node
		.jjtGetChild(0).jjtAccept(this, data);
		c.addSucceedingOperator(new OperatorIDTuple<Operator>(constructTemplateOp, 0));

		return c;
	}

	public Object visit(final ASTConstructTemplate node, final Object data) {
		final LinkedHashSet<Operator> templates = new LinkedHashSet<Operator>();

		final HashMap<Item, QueryRDFTerm> rdfHash = new HashMap<Item, QueryRDFTerm>();

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			final Operator op = (Operator) node.jjtGetChild(i).jjtAccept(this,
					rdfHash);

			if (op != null)
				templates.add(op);
		}

		final ConstructTemplateContainer oc = new ConstructTemplateContainer(
				templates);

		return oc;
	}

	public Object visit(final ASTDescribeQuery node, final Object data) {
		final Describe d = new Describe(this.prefix);

		this.handleWhereClause(node, d);

		if (!node.isDescribeAll())
			this.handleProjections(node, d);

		this.handleDatasetClause(node, d);
		this.handleSolutionModifier(node, d);

		return d;
	}

	public Object visit(final ASTAskQuery node, final Object data) {
		final Ask a = new Ask(this.prefix);

		this.handleWhereClause(node, a);
		this.handleDatasetClause(node, a);

		return a;
	}

	public Object visit(final ASTDefaultGraph node, final Object data) {
		return visitChildren(node, data);
	}

	public Object visit(final ASTNamedGraph node, final Object data) {
		return visitChildren(node, data);
	}

	public Object visit(final ASTOrderConditions node, final Object data) {
		// create list for SortContainers...
		final LinkedList<SortContainer> scl = new LinkedList<SortContainer>();

		// walk through children...
		for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
			Node n = node.jjtGetChild(i); // get current child

			boolean desc = false; // set order ASC as default

			// current node is Order node...
			if (n instanceof ASTAscOrder || n instanceof ASTDescOrder) {
				if (n instanceof ASTDescOrder) // reset order value to DESC...
					desc = true;

				i++; // increase counter to get next child (which is sort
				// condition)
			}

			n = node.jjtGetChild(i); // get next child (which is sort condition)

			// parse node with the filter...
			final SPARQLParserVisitorImplementationDumper filterDumper = new SPARQLParserVisitorImplementationDumper();
			final String sortString = n.accept(filterDumper);

			try {
				final SortContainer sc = new SortContainer(this.prefix, desc,
						sortString); // create SortContainer

				scl.add(sc); // add SortContainer to list
			} catch (final ModificationException e) {
				e.printStackTrace();
			}
		}

		return scl; // return list of SortContainers
	}

	public Object visit(final ASTAscOrder node, final Object data) {
		return data;
	}

	public Object visit(final ASTDescOrder node, final Object data) {
		return data;
	}

	public Object visit(final ASTLimit node, final Object data) {
		return node.getLimit();
	}

	public Object visit(final ASTOffset node, final Object data) {
		return node.getOffset();
	}

	public abstract Object visit(ASTGroupConstraint node, Object data);

	public Object visit(final ASTOptionalConstraint node, final Object data) {
		return node.jjtGetChild(0).jjtAccept(this, data);
	}

	public Object visit(final ASTGraphConstraint node, final Object data) {
		return visitChildren(node, data);
	}

	public Object visit(final ASTUnionConstraint node, final Object data) {
		final Union unionOp = new Union();

		Operator testOP = (Operator) node.jjtGetChild(0).jjtAccept(this, data);

		if (testOP != null)
			unionOp.addSucceedingOperator(new OperatorIDTuple<Operator>(testOP, 0));

		testOP = (Operator) node.jjtGetChild(1).jjtAccept(this, data);

		if (testOP != null)
			unionOp.addSucceedingOperator(new OperatorIDTuple<Operator>(testOP, 1));

		return unionOp;
	}

	public Object visit(final ASTFilterConstraint node, final Object data) {
		final SPARQLParserVisitorImplementationDumper filterDumper = new SPARQLParserVisitorImplementationDumper();

		final String filterExpr = node.jjtGetChild(0).accept(filterDumper);

		final Filter filterOp = new Filter(this.prefix, filterExpr);

		return filterOp;
	}

	public Object visit(final ASTFunctionCall node, final Object data) {
		return null;
	}

	public Object visit(final ASTArguments node, final Object data) {
		return visitChildren(node, data);
	}

	@SuppressWarnings("unchecked")
	public Object visit(final ASTTripleSet node, final Object data) {
		final Item[] item = { null, null, null };

		for (int i = 0; i < 3; i++) {
			final Node n = node.jjtGetChild(i);
			item[i] = lupos.sparql1_1.operatorgraph.SPARQLCoreParserVisitorImplementation.getItem(n);
		}

		final HashMap<Item, QueryRDFTerm> rdfHash = (HashMap<Item, QueryRDFTerm>) data;

		QueryRDFTerm rdfTermSubject = rdfHash.get(item[0]);

		if (rdfTermSubject == null) {
			rdfTermSubject = new QueryRDFTerm(this.prefix, item[0]);
			rdfHash.put(item[0], rdfTermSubject);
		}

		QueryRDFTerm rdfTermObject = rdfHash.get(item[2]);

		if (rdfTermObject == null) {
			rdfTermObject = new QueryRDFTerm(this.prefix, item[2]);
			rdfHash.put(item[2], rdfTermObject);
		}

		rdfTermSubject.addPredicate(rdfTermObject, item[1]);

		final OperatorIDTuple<Operator> opIDT = new OperatorIDTuple<Operator>(rdfTermObject, 0);

		if (!rdfTermSubject.getSucceedingOperators().contains(opIDT))
			rdfTermSubject.addSucceedingOperator(opIDT);

		return rdfTermSubject;
	}

	public Object visit(final ASTVar node, final Object data) {
		return null;
	}

	public Object visit(final ASTOrNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTAndNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTEqualsNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTNotEqualsNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTLessThanNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTLessThanEqualsNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTGreaterThanNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTGreaterThanEqualsNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTAdditionNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTSubtractionNode node, final Object data) {
		return visitChildren(node, data);
	}

	public Object visit(final ASTMultiplicationNode node, final Object data) {
		return visitChildren(node, data);
	}

	public Object visit(final ASTDivisionNode node, final Object data) {
		return visitChildren(node, data);
	}

	public Object visit(final ASTNotNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTPlusNode node, final Object data) {
		return visitChildren(node, data);
	}

	public Object visit(final ASTMinusNode node, final Object data) {
		return visitChildren(node, data);
	}

	public Object visit(final ASTStrFuncNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTLangFuncNode node, final Object data) {
		return visitChildren(node, data);
	}

	public Object visit(final ASTLangMatchesFuncNode node, final Object data) {
		return visitChildren(node, data);
	}

	public Object visit(final ASTDataTypeFuncNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTBoundFuncNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTUriFuncNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTBnodeFuncNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTRegexFuncNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTLangTag node, final Object data) {
		return visitChildren(node, data) + node.getLangTag();
	}

	public Object visit(final ASTDoubleCircumflex node, final Object data) {
		return null;
	}

	public Object visit(final ASTBooleanLiteral node, final Object data) {
		if (node.getState())
			return "true";
		else
			return "false";
	}

	public Object visit(final ASTStringLiteral node, final Object data) {
		String stringLiteral = node.getStringLiteral();

		while (stringLiteral.startsWith("\"0") && stringLiteral.length() > 3)
			stringLiteral = "\""
				+ stringLiteral.substring(2, stringLiteral.length());

		return stringLiteral;
	}

	public Object visit(final ASTEmptyNode node, final Object data) {
		return visitChildren(node, data);
	}

	public Object visit(final ASTQuotedURIRef node, final Object data) {
		return node.getQRef();
	}

	public Object visit(final ASTInteger node, final Object data) {
		return String.valueOf(node.getValue());
	}

	public Object visit(final ASTFloatingPoint node, final Object data) {
		return String.valueOf(node.getValue());
	}

	public Object visit(final ASTSameTermFuncNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTIriFuncNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTRDFLiteral node, final Object data) {
		return visitChildren(node, data);
	}

	public Object visit(final ASTBlankNode node, final Object data) {
		return null;
	}

	public Object visit(final ASTAs node, final Object data) {
		return null;
	}
}
