
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.optimizations.sparql2core_sparql;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.operators.singleinput.NotBoundException;
import lupos.sparql1_1.ASTAVerbType;
import lupos.sparql1_1.ASTAdd;
import lupos.sparql1_1.ASTAndNode;
import lupos.sparql1_1.ASTArbitraryOccurences;
import lupos.sparql1_1.ASTArbitraryOccurencesNotZero;
import lupos.sparql1_1.ASTAskQuery;
import lupos.sparql1_1.ASTBaseDecl;
import lupos.sparql1_1.ASTBlankNode;
import lupos.sparql1_1.ASTBlankNodePropertyList;
import lupos.sparql1_1.ASTBooleanLiteral;
import lupos.sparql1_1.ASTBoundFuncNode;
import lupos.sparql1_1.ASTCollection;
import lupos.sparql1_1.ASTConstructQuery;
import lupos.sparql1_1.ASTConstructTemplate;
import lupos.sparql1_1.ASTCopy;
import lupos.sparql1_1.ASTDefault;
import lupos.sparql1_1.ASTDescribeQuery;
import lupos.sparql1_1.ASTEmptyNode;
import lupos.sparql1_1.ASTExists;
import lupos.sparql1_1.ASTFilterConstraint;
import lupos.sparql1_1.ASTGraphConstraint;
import lupos.sparql1_1.ASTGroupConstraint;
import lupos.sparql1_1.ASTHaving;
import lupos.sparql1_1.ASTInvers;
import lupos.sparql1_1.ASTMove;
import lupos.sparql1_1.ASTNegatedPath;
import lupos.sparql1_1.ASTNodeSet;
import lupos.sparql1_1.ASTNotExists;
import lupos.sparql1_1.ASTNotNode;
import lupos.sparql1_1.ASTObjectList;
import lupos.sparql1_1.ASTOptionalConstraint;
import lupos.sparql1_1.ASTOptionalOccurence;
import lupos.sparql1_1.ASTOrNode;
import lupos.sparql1_1.ASTOrderConditions;
import lupos.sparql1_1.ASTPathAlternative;
import lupos.sparql1_1.ASTPathSequence;
import lupos.sparql1_1.ASTPrefixDecl;
import lupos.sparql1_1.ASTQName;
import lupos.sparql1_1.ASTQuery;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.ASTSelectQuery;
import lupos.sparql1_1.ASTService;
import lupos.sparql1_1.ASTTripleSet;
import lupos.sparql1_1.ASTUnionConstraint;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.SimpleNode;
import lupos.sparql1_1.operatorgraph.SPARQLCoreParserVisitorImplementation;
public class SPARQL2CoreSPARQLParserVisitorImplementationDumper extends
		SPARQL2CoreSPARQL_rules implements SPARQL1_1ParserVisitorStringGenerator, SPARQL1_1ParserPathVisitorStringGenerator {
	protected final HashMap<Node, String> blankNodeHash = new HashMap<Node, String>();
	protected final HashMap<String, String> prefixHash = new HashMap<String, String>();
	protected final ArrayList<String> realVars = new ArrayList<String>();

	protected int bn_nmbr = 0;
	protected boolean construct = false;
	protected boolean mustAddProjectionOnRealVars = false;

	/** Constant <code>SPARQL2CoreSPARQLClass</code> */
	public static Class<? extends SPARQL2CoreSPARQLParserVisitorImplementationDumper> SPARQL2CoreSPARQLClass = SPARQL2CoreSPARQLParserVisitorImplementationDumper.class;

	/**
	 * <p>createInstance.</p>
	 *
	 * @return a {@link lupos.optimizations.sparql2core_sparql.SPARQL2CoreSPARQLParserVisitorImplementationDumper} object.
	 * @throws java.lang.InstantiationException if any.
	 * @throws java.lang.IllegalAccessException if any.
	 */
	public static SPARQL2CoreSPARQLParserVisitorImplementationDumper createInstance() throws InstantiationException, IllegalAccessException{
		final SPARQL2CoreSPARQLParserVisitorImplementationDumper result = SPARQL2CoreSPARQLClass.newInstance();
		result.prefixHash.put("rdf", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		return result;
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param rules an array of boolean.
	 * @return a {@link lupos.optimizations.sparql2core_sparql.SPARQL2CoreSPARQLParserVisitorImplementationDumper} object.
	 * @throws java.lang.InstantiationException if any.
	 * @throws java.lang.IllegalAccessException if any.
	 */
	public static SPARQL2CoreSPARQLParserVisitorImplementationDumper createInstance(final boolean[] rules) throws InstantiationException, IllegalAccessException{
		final SPARQL2CoreSPARQLParserVisitorImplementationDumper result = createInstance();
		if (rules.length != result.LENGTH) {
			System.err.println("Parameter rules should have length " + result.LENGTH
					+ ", but it has length " + rules.length);
		} else {
			result.rules = rules;
		}
		return result;
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param allRules a boolean.
	 * @return a {@link lupos.optimizations.sparql2core_sparql.SPARQL2CoreSPARQLParserVisitorImplementationDumper} object.
	 * @throws java.lang.InstantiationException if any.
	 * @throws java.lang.IllegalAccessException if any.
	 */
	public static SPARQL2CoreSPARQLParserVisitorImplementationDumper createInstance(final boolean allRules) throws InstantiationException, IllegalAccessException{
		final SPARQL2CoreSPARQLParserVisitorImplementationDumper result = createInstance();
		if (!allRules) {
			result.rules = new boolean[] { false, false, false, false, false, false,
					false, false, false, false, false, false, false };
		}
		return result;
	}

	/**
	 * <p>Constructor for SPARQL2CoreSPARQLParserVisitorImplementationDumper.</p>
	 */
	protected SPARQL2CoreSPARQLParserVisitorImplementationDumper() {
		super();
	}

	/**
	 * <p>getBlankNodeLabel.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	protected String getBlankNodeLabel() {
		this.mustAddProjectionOnRealVars = true;
		final String ret = "b" + String.valueOf(this.bn_nmbr++);
		if (this.blankNodeHash.containsValue("_" + ret)
				|| this.realVars.contains("?_" + ret)) {
			return this.getBlankNodeLabel();
		}
		if (this.rules[BN_VARS] && !this.construct) {
			return "?_" + ret;
		} else {
			return "_:" + ret;
		}
	}

	/**
	 * <p>determineRealVariables.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 */
	protected void determineRealVariables(final Node node) {
		if (node instanceof ASTVar){
			final String name = ((ASTVar)node).getName();
			this.blankNodeHash.put(node, name);
			if (!this.realVars.contains("?" + name)) {
				this.realVars.add("?" + name);
			}
		}
		final int numberChildren = node.jjtGetNumChildren();
		for (int i = 0; i < numberChildren; i++) {
			this.determineRealVariables(node.jjtGetChild(i));
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTQuery node) {
		// first determine all "real" variables
		this.determineRealVariables(node);
		return super.visit(node);
	}

	protected URILiteral base = null;

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTBaseDecl node) {
		try {
			this.base = LiteralFactory
					.createURILiteralWithoutLazyLiteral(this.visitChildren(node));
		} catch (final URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		if (this.rules[BASE]) {
			return "";
		} else {
			return "BASE\t" + this.visitChildren(node) + "\n";
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTPrefixDecl node) {
		final String pref = node.getPrefix();
		// final ASTQuotedURIRef content = (ASTQuotedURIRef)
		// node.jjtGetChild(0);
		// String qRef = content.getQRef();
		// if( pref.equals( "" ) ){
		// return "PREFIX\t:\t"+visitChild( node, 0 )+"\n";
		// }else{
		// return "PREFIX\t"+pref+":\t"+visitChild( node, 0 )+"\n";
		// }

		if (this.rules[BASE] && this.base != null) {
			final URI uri = URI.create(pref);
			final ASTQuotedURIRef content = (ASTQuotedURIRef) node
					.jjtGetChild(0);
			String qRef = content.getQRef();
			if (!uri.isAbsolute()) {
				// pref = "<"
				// + base.toString().substring(1,
				// base.toString().length() - 1) + pref + ">";
				try {
					qRef = "<"
							+ this.base.toString().substring(1,
									this.base.toString().length() - 1) + qRef + ">";
					this.prefixHash.put(pref, qRef);
				} catch (final Exception e) {
					System.err.println("Undeclared Prefix " + pref + " used");
				}
			}

		}
		// final Object test = visitChild(node, 0);
 else {
			this.prefixHash.put(pref, this.visitChild(node, 0));
		}
		return "";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTSelectQuery node) {
		String ret = "SELECT ";
		if (node.isDistinct()) {
			ret += "DISTINCT ";
		}
		if (node.isReduced()) {
			ret += "REDUCED ";
		}
		final String suff = this.visitChildrenSep(node);
		if (node.isSelectAll()) {
			if (this.mustAddProjectionOnRealVars) {
				final Iterator<String> iter = this.realVars.iterator();
				String vars = "";
				if (iter.hasNext()) {
					vars = iter.next();
				}
				while (iter.hasNext()) {
					vars += " " + iter.next();
				}
				ret += vars;
			} else {
				ret += "*";
			}
		}
		return ret + "\n" + suff;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTDescribeQuery node) {
		if (!this.rules[DESCRIBE]) {
			String ret = "DESCRIBE\t";

			final String suff = this.visitChildrenSep(node);
			if (node.isDescribeAll()) {
				if (this.mustAddProjectionOnRealVars) {
					final Iterator<String> iter = this.realVars.iterator();
					String vars = "";
					if (iter.hasNext()) {
						vars = iter.next();
					}
					while (iter.hasNext()) {
						vars += " " + iter.next();
					}
					ret += vars;
				} else {
					ret += "*";
				}
			}
			return ret + "\n" + suff;

			// return "DESCRIBE\t" + visitChildren(node) + "\n";
		} else {
			final LinkedList<String> vars = new LinkedList<String>();
			if (node.isDescribeAll()) {
				// case Describe * ...: determine all variables inside the
				// query!
				this.determineRealVariables(node);
				vars.addAll(this.realVars);
			} else {
				for (final Node n : node.getChildren()) {
					if (n instanceof ASTVar) {
						vars.add("?" + ((ASTVar) n).getName());
					} else if (n instanceof ASTQuotedURIRef) {
						vars.add(((ASTQuotedURIRef) n).toQueryString());
					} else if (n instanceof ASTQName) {
						vars.add(this.visit(((ASTQName) n)));
					}
				}
			}
			String suff = "";
			for (final Node n : node.getChildren()) {
				if (n instanceof ASTGroupConstraint) {
					suff = this.visit((ASTGroupConstraint) n);
				}
			}
			String templates = "";
			String optional = "";
			int index = 0;
			for (final String var : vars) {
				String helperVar1 = "?__h" + (index++);
				while (this.realVars.contains(helperVar1)) {
					helperVar1 = "?__h" + (index++);
				}
				final String helperVar2 = "?__h" + (index++);
				while (this.realVars.contains(helperVar2)) {
					helperVar1 = "?__h" + (index++);
				}
				templates += var + " " + helperVar1 + " " + helperVar2 + ".\n";
				optional += "OPTIONAL{" + var + " " + helperVar1 + " "
						+ helperVar2 + "}.\n";
			}
			final int lastOccurrence = suff.lastIndexOf("}");
			if (lastOccurrence >= 0) {
				suff = suff.substring(0, lastOccurrence);
			}
			final String stream = "";
//			for (final Node n : node.getChildren()) {
//				if (n instanceof ASTStream) {
//					stream = (String) this.visit((ASTStream) n) + "\n";
//				}
//			}
			return "CONSTRUCT {" + templates + "}\n" + stream + "WHERE {"
					+ suff + optional + "}";
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTGroupConstraint node) {
		if (node.jjtGetParent() instanceof ASTSelectQuery
				|| node.jjtGetParent() instanceof ASTAskQuery
				|| node.jjtGetParent() instanceof ASTDescribeQuery
				|| node.jjtGetParent() instanceof ASTConstructQuery
				|| node.jjtGetParent() instanceof ASTQuery) {
			this.checkFilterOfGroupConstraint(node, new HashSet<String>());
		}
		String ret = "\n{";
		final String retEnd = "}";
		if (node.jjtGetParent() instanceof ASTSelectQuery
				|| node.jjtGetParent() instanceof ASTConstructQuery
				|| (node.jjtGetParent() instanceof ASTDescribeQuery && !this.rules[DESCRIBE])
				|| node.jjtGetParent() instanceof ASTQuery) {
			ret = "\nWHERE \n{\n";
		} else if(node.jjtGetParent() instanceof ASTDescribeQuery){
			ret = "\n";
		}
		final int numberOfChildren = node.jjtGetNumChildren();
		for (int i = 0; i < numberOfChildren; i++) {
			ret += this.visitChild(node, i);
		}
		return ret + retEnd;
	}

	private void boundVariables(final Node node, final HashSet<String> variables) {
		if (node instanceof ASTFilterConstraint) {
			return;
		}
		if (node instanceof ASTVar) {
			variables.add(((ASTVar) node).getName());
		}
		final int numberOfChildren = node.jjtGetNumChildren();
		for (int i = 0; i < numberOfChildren; i++) {
			this.boundVariables(node.jjtGetChild(i), variables);
		}
	}

	private boolean inScopeOfOptionalOrUnion(final Node node) {
		if (node == null) {
			return false;
		} else if (node instanceof ASTOptionalConstraint) {
			return true;
		} else if (node instanceof ASTUnionConstraint) {
			return true;
		} else if(node instanceof ASTSelectQuery ||
				node instanceof ASTAskQuery ||
				node instanceof ASTConstructQuery ||
				node instanceof ASTDescribeQuery ||
				node instanceof ASTQuery ||
				node instanceof ASTExists ||
				node instanceof ASTNotExists ||
				node instanceof ASTService
				) {
			return false;
		} else {
			return this.inScopeOfOptionalOrUnion(node.jjtGetParent());
		}
	}

	private void checkFilterOfGroupConstraint(final Node node,
			final HashSet<String> variables) {
		if (node instanceof ASTGroupConstraint) {
			final HashSet<String> variablesNew = (this.inScopeOfOptionalOrUnion(node.jjtGetParent())) ? (HashSet<String>) variables.clone() : new HashSet<String>();
			this.boundVariables(node, variablesNew);

			final int numberOfChildren = node.jjtGetNumChildren();
			for (int i = 0; i < numberOfChildren; i++) {
				if (node.jjtGetChild(i) instanceof ASTFilterConstraint) {
					try {
						this.checkFilterConstraint(node.jjtGetChild(i)
								.jjtGetChild(0), variablesNew);
					} catch (final NotBoundException e) {
						node.jjtGetChild(i).clearChildren();
						final ASTBooleanLiteral bl = new ASTBooleanLiteral(0);
						bl.setState(false);
						bl.jjtSetParent(node.jjtGetChild(i));
						node.jjtGetChild(i).jjtAddChild(bl, 0);
					}
				} else {
					this.checkFilterOfGroupConstraint(node.jjtGetChild(i),
							variablesNew);
				}
			}
		} else if (node instanceof ASTUnionConstraint) {
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final HashSet<String> variablesClone = (HashSet<String>) variables.clone();
				this.checkFilterOfGroupConstraint(node.jjtGetChild(i), variablesClone);
			}
		} else if (node instanceof ASTOptionalConstraint) {
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final HashSet<String> variablesClone = (HashSet<String>) variables.clone();
				this.checkFilterOfGroupConstraint(node.jjtGetChild(i), variablesClone);
			}
		}
	}

	private void checkFilterConstraint(final Node n,
			final HashSet<String> variables) throws NotBoundException {
		if (n instanceof lupos.sparql1_1.ASTOrNode) {
			boolean deleteLeftOperand = false;
			boolean deleteRightOperand = false;
			try {
				this.checkFilterConstraint(n.jjtGetChild(0), variables);
			} catch (final NotBoundException nbe) {
				deleteLeftOperand = true;
			}
			try {
				this.checkFilterConstraint(n.jjtGetChild(1), variables);
			} catch (final NotBoundException nbe) {
				deleteRightOperand = true;
			}
			if (deleteLeftOperand && deleteRightOperand) {
				throw new NotBoundException(
						"Both operands of an Or-Operator in a filter throw NotBoundException!");
			}
			if (deleteLeftOperand) {
				n.jjtGetParent().replaceChild2(n, n.jjtGetChild(1));
				n.jjtGetChild(1).jjtSetParent(n.jjtGetParent());
			}
			if (deleteRightOperand) {
				n.jjtGetParent().replaceChild2(n, n.jjtGetChild(0));
				n.jjtGetChild(0).jjtSetParent(n.jjtGetParent());
			}
		} else if (n instanceof lupos.sparql1_1.ASTVar) {
			if (!variables.contains(((lupos.sparql1_1.ASTVar) n).getName())) {
				throw new NotBoundException("Variable "
						+ ((lupos.sparql1_1.ASTVar) n).getName() + " not bound");
			}
		} else {
			if(!(n instanceof ASTExists || n instanceof ASTNotExists || n instanceof ASTBoundFuncNode)){
				for(int i=0; i<n.jjtGetNumChildren(); i++){
					this.checkFilterConstraint(n.jjtGetChild(i), variables);
				}
			}
		}
	}

	private void nestedGroups(final ASTGroupConstraint node) {
		final ArrayList<ASTVar> vars_in_scope = new ArrayList<ASTVar>();
		final ArrayList<ASTVar> vars_in_filter = new ArrayList<ASTVar>();
		this.checkChildren(vars_in_scope, vars_in_filter, node, false);
		boolean indicator = true;
		if (vars_in_filter.size() > 0) {
			indicator = false;
			for (final Iterator<ASTVar> iter = vars_in_filter.iterator(); iter.hasNext();) {
				indicator = indicator || vars_in_scope.contains(iter.next());
			}
		}
	}

	private void checkChildren(final ArrayList<ASTVar> vars_in_scope, final ArrayList<ASTVar> vars_in_filter, final Node currentNode, boolean filter) {
		if (currentNode instanceof ASTFilterConstraint) {
			filter = true;
		} else if (currentNode instanceof ASTVar) {
			if (filter) {
				if (!vars_in_filter.contains(currentNode)) {
					vars_in_filter.add((ASTVar) currentNode);
				}
			} else {
				if (!vars_in_scope.contains(currentNode)) {
					vars_in_scope.add((ASTVar) currentNode);
				}
			}
		} else if (currentNode instanceof ASTGroupConstraint) {
			this.nestedGroups((ASTGroupConstraint) currentNode);
		}
		for (int i = 0; i < currentNode.jjtGetNumChildren(); i++) {
			this.checkChildren(vars_in_scope, vars_in_filter, currentNode
					.jjtGetChild(i), filter);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTFilterConstraint node) {
		final Node n = new SimpleNode(0);
		final Node parent = node.jjtGetParent();
		n.jjtAddChild(node, 0);
		node.jjtSetParent(n);
		parent.replaceChild2(node, n);
		n.jjtSetParent(parent);
		this.applyRules(node);
		String ret = "";
		final String prefix = (parent instanceof ASTOrderConditions || parent instanceof ASTHaving)? "" : "FILTER";
		final String postfix = (parent instanceof ASTOrderConditions || parent instanceof ASTHaving)? "" : ".\n";
		final int numberOfChildren = n.jjtGetNumChildren();
		for (int i = 0; i < numberOfChildren; i++) {
			final Node currentChild=n.jjtGetChild(i);
			for(int j=0; j<currentChild.jjtGetNumChildren();j++){
				ret += prefix + "(" + this.visitChild(currentChild, j) + ")"+postfix;
			}
		}
		return ret;
	}

	/**
	 * <p>dumpFilter.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTFilterConstraint} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String dumpFilter(final ASTFilterConstraint node) {
		return "FILTER(" + this.visitChild(node.jjtGetChild(0), 0) + ").";
	}

	private int applyRules(final Node node) {
		if (this.rules[FILTER_AND] && node instanceof ASTFilterConstraint
				&& node.jjtGetChild(0) instanceof ASTAndNode) {
			final Node parent = node.jjtGetParent();
			if (this.filter_and((ASTFilterConstraint) node)) {
				return this.applyRules(parent);
			}
		}
		if (this.rules[FILTER_OR] && node instanceof ASTAndNode) {
			if (this.nestedOr((ASTAndNode) node)) {
				return this.applyRules(node.jjtGetParent());
			}
		}
		if (this.rules[FILTER_NOT] && node instanceof ASTNotNode) {
			if (this.downNot(node)) {
				return this.applyRules(node.jjtGetParent());
			}
		}
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			this.applyRules(node.jjtGetChild(i));
		}
		return -1;
	}

	private boolean filter_and(final ASTFilterConstraint n) {
		final ASTAndNode and = (ASTAndNode) n.jjtGetChild(0);
		final ASTFilterConstraint a = (ASTFilterConstraint) n
				.cloneStillChild(true);
		final ASTFilterConstraint b = (ASTFilterConstraint) n
				.cloneStillChild(true);
		n.jjtGetParent().removeChild(n);
		a.addChild(and.jjtGetChild(0));
		b.addChild(and.jjtGetChild(0));
		return true;
	}

	private boolean nestedOr(final ASTAndNode node) {
		final int ind = this.checkNestedOr(node);
		if (ind >= 0) {
			final Node parent = node.jjtGetParent();
			final Node or = node.jjtGetChild(ind).clone(false);
			node.removeChild(ind);
			parent.addChild(or);
			or.addChild(node);
			final ASTAndNode addAnd = (ASTAndNode) node.cloneStillChild(false);
			node.addChild(or.jjtGetChild(0));
			addAnd.addChild(or.jjtGetChild(0));
		}
		return ind >= 0;
	}

	private int checkNestedOr(final ASTAndNode node) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if (node.jjtGetChild(i) instanceof ASTOrNode) {
				return i;
			}
		}
		return -1;
	}

	private boolean downNot(final Node node) {
		return false;

		// String[] ret = new String[2];
		// String replace = null;
		// String by = null;
		// if( node instanceof ASTNotNode &&
		// node.jjtGetChild(0).jjtGetNumChildren() > 0 ){
		// replace = (String)visit( (ASTNotNode)node );
		// Node child = node.jjtGetChild(0);
		// String concat = "";
		// if( child instanceof ASTOrNode ) concat = "||!";
		// else if( child instanceof ASTAndNode ) concat = "&&!";
		// else return null;
		// by = "!";
		// for( int i=0; i<child.jjtGetNumChildren(); i++ ){
		// if( i>0 ) by += concat;
		// by += visitChild( child, i );
		// }
		// ret[0] = replace; ret[1]=by;
		// return ret;
		// }
		// for( int i=0; i<node.jjtGetNumChildren(); i++ ){
		// String[] tmp = downNot( node.jjtGetChild(i) );
		// if( tmp != null ) return tmp;
		// }
		// return null;
	}

	// private String rebuildNestedOr( final Node node ){
	// int nextChild = checkNestedOr( (ASTAndNode)node );
	// final Node orChild = node.jjtGetChild( nextChild );
	// nextChild = ++nextChild % 2;
	// final String evalOther = visitChild( node, nextChild );
	// return "(("+visitChild( orChild, 0 ) + ")&&(" + evalOther +
	// "))||((" + visitChild( orChild, 1 ) + ")&&(" + evalOther + "))";
	// }

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTConstructTemplate node) {
		this.construct = true;
		final String ret = "{" + this.visitChildrenSep(node) + "}";
		this.construct = false;
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTNodeSet node) {
		return this.visitChild(node, 0)+this.solvePOAndOLists(node, this.getVarOrBlankNode(node.jjtGetChild(0)), 1);
	}

	private String getVarOrBlankNode(final Node node) {
		if (node instanceof ASTVar) {
			return "?" + ((ASTVar) node).getName();
		}
		final String sub;
		if (this.blankNodeHash.containsKey(node)) {
			sub = this.blankNodeHash.get(node);
		} else {
			sub = this.getBlankNodeLabel();
		}
		this.blankNodeHash.put(node, sub);
		return sub;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTObjectList node) {
		if (this.rules[PO_LISTS]) {
			throw new Error("This code position should not be reached!");
		} else {
			return super.visit(node);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTAVerbType node) {
		if (this.rules[RDF_TYPE]) {
			return "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
		}
		return "a";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTBlankNodePropertyList node) {
		if (this.rules[BLANK_NODES]) {
			/* The BlankNodePropertyList has at least 2 Children
			 * Every uneven node holds a Predicate
			 * Every even node except the first one holds an ObjectList
			 */
			return this.solvePOAndOLists(node, this.getVarOrBlankNode(node), 0);
		}
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTCollection node) {
		if (this.rules[RDF_COLLECTIONS]) {
			String bcur = this.getVarOrBlankNode(node);
			String ret = "";
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				if (node.jjtGetChild(i) instanceof ASTBlankNodePropertyList
						|| node.jjtGetChild(i) instanceof ASTCollection) {
					ret += bcur
							+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> "
							+ this.getVarOrBlankNode(node.jjtGetChild(i)) + " .\n"
							+ this.visitChild(node, i);
				} else {
					ret += bcur
							+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> "
							+ this.visitChild(node, i) + " .\n";
				}
				final String bnext =(i < node.jjtGetNumChildren() - 1)? this.getBlankNodeLabel() : "<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>";
				ret += bcur
						+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> "
						+ bnext + " .\n";
				bcur = bnext;
			}
			return ret;
		} else {
			return super.visit(node);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTVar node) {
		return "?" + node.getName();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTQName node) {
		String ret = this.prefixHash.get(node.getNameSpace());
		if (ret == null) {
			ret = "<>";
			if (this.rules[BASE]) {
				try {
					ret = this.base.toString();
				} catch (final Exception e) {
					System.err.println("Undeclared Prefix "
							+ node.getNameSpace() + " used");
				}
			}
		}
		if (node.getLocalName().equals("")) {
			return ret;
		}
		return ret.substring(0, ret.length() - 1) + node.getLocalName() + ">";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTEmptyNode node) {
		if (this.rules[BLANK_NODES]) {
			if (this.blankNodeHash.containsKey(node)) {
				return this.blankNodeHash.get(node);
			}
			final String tbn = this.getBlankNodeLabel();
			this.blankNodeHash.put(node, tbn);
			return tbn;
		}
		return "[]";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTQuotedURIRef node) {
		if (this.rules[BASE] && this.base != null) {
			return "<"
					+ this.base.toString()
							.substring(1, this.base.toString().length() - 1)
					+ node.getQRef() + ">";
		}
		return "<" + node.getQRef() + ">";
	}

//	public String visit(final ASTStream node) {
//		String ret = " STREAM ";
//		if (node.getDuration() || node.getTriples()) {
//			ret += "INTERMEDIATERESULT ";
//			if (node.getDuration())
//				ret += "DURATION ";
//			else
//				ret += "TRIPLES ";
//			ret += node.getValue();
//		}
//		return ret;
//	}
//
//	public String visit(final ASTWindow node) {
//		String ret = " WINDOW ";
//		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
//			if (!(node.jjtGetChild(i) instanceof ASTGroupConstraint))
//				ret += visitChild(node, i);
//		}
//		ret += "{";
//		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
//			if (node.jjtGetChild(i) instanceof ASTGroupConstraint)
//				ret += visitChild(node, i);
//		}
//		return ret + "}";
//	}
//
//	public String visit(final ASTStart node) {
//		return "START " + visitChild(node, 0);
//	}
//
//	public String visit(final ASTEnd node) {
//		return "END " + visitChild(node, 0);
//	}
//
//	public String visit(final ASTType node) {
//		String ret = " TYPE ";
//		if (node.getType() == ASTType.DurationOrTriples.DURATION)
//			ret += "SLIDINGDURATION ";
//		else
//			ret += "SLIDINGTRIPLES ";
//		return ret + node.getValue();
//	}
//
//	public String visit(final ASTTimeFuncNode node) {
//		return " getTime(" + visitChildren(node) + ") ";
//	}
//
//	public String visit(final ASTPosInStreamFuncNode node) {
//		return " getPosInStream(" + visitChildren(node) + ") ";
//	}

	private final HashMap<String, String> variablesOfBlankNodes = new HashMap<String, String>();

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTBlankNode node) {
		if (this.rules[BN_VARS]) {
			String name = this.variablesOfBlankNodes.get(node.getIdentifier());
			if (name == null) {
				name = this.getBlankNodeLabel();
				this.variablesOfBlankNodes.put(node.getIdentifier(), name);
			}
			return name;
		} else {
			return node.getIdentifier();
		}
	}

	/**
	 * <p>getInsert.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getInsert(final Node node){
		final String vChild1=this.visitChild(node, 1);
		final String vChild0=this.visitChild(node, 0);
		if(vChild1.compareTo(vChild0)==0){
			return " # Adding to the same graph does not have any effect!\n";
		}
		final boolean flag1 =(node.jjtGetChild(1) instanceof ASTDefault);
		final String start1=flag1?"":"GRAPH "+vChild1+" {";
		final String end1=flag1?"":"}";
		final boolean flag2 =(node.jjtGetChild(0) instanceof ASTDefault);
		final String start2=flag2?"":"GRAPH "+vChild0+" {";
		final String end2=flag2?"":"}";
		return " INSERT { "+start1+"?s ?p ?o."+end1+"} WHERE { "+start2+"?s ?p ?o."+end2+"}";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTAdd node) {
		if(this.rules[ADD]){
			return this.getInsert(node);
		} else {
			return super.toString();
		}
	}

	/**
	 * <p>getDropAndInsert.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getDropAndInsert(final Node node){
		final String vChild1=this.visitChild(node, 1);
		if(vChild1.compareTo(this.visitChild(node, 0))==0){
			return " # Copying to the same graph does not have any effect!\n";
		}
		final String dropOrClear = (vChild1.compareTo("DEFAULT")==0)?" CLEAR":" DROP";
		return dropOrClear+" SILENT "+(node.jjtGetChild(1) instanceof ASTDefault?"":"GRAPH ")+vChild1+";\n"+this.getInsert(node);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTCopy node) {
		if(this.rules[COPY]){
			return this.getDropAndInsert(node);
		} else {
			return super.toString();
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTMove node) {
		if(this.rules[MOVE]){
			final String vChild0 = this.visitChild(node,0);
			if(vChild0.compareTo(this.visitChild(node, 1))==0){
				return " # Moving to the same graph does not have any effect!\n";
			}
			final String dropOrClear = (vChild0.compareTo("DEFAULT")==0)?"CLEAR":"DROP";
			return this.getDropAndInsert(node) + ";\n"+dropOrClear+" SILENT "+(node.jjtGetChild(0) instanceof ASTDefault?"":"GRAPH ")+vChild0;
		} else {
			return super.toString();
		}
	}

	/**
	 * <p>getIntermediateVariable.</p>
	 *
	 * @return An unique variable which is definitely not used in the original query
	 */
	protected String getIntermediateVariable() {
		this.mustAddProjectionOnRealVars = true;
		final String ret = "b" + String.valueOf(this.bn_nmbr++);
		/* checks if variable is already used
		 * if true generate a new one
		 * else return the variable in form ?_charNumber
		 */
		if (this.blankNodeHash.containsValue("_" + ret)
				|| this.realVars.contains("?_" + ret)) {
			return this.getIntermediateVariable();
		}
		return "?_" + ret;
	}


	/**
	 * {@inheritDoc}
	 *
	 * This method visits an ASTTripleSet and computes a String, representing the expression of the Triple set.
	 */
	@Override
	public String visit(final ASTTripleSet node) {
		if(this.rules[PO_LISTS]){
			/* The Triple Set has at least 3 Children
			 * The first child is always the subject
			 * Every even node holds a Predicate
			 * Every uneven node except the first one holds an ObjectList
			 */
			return this.solvePOAndOLists(node, this.visitChild(node,0), 1);
		} else {
			return super.visit(node);
		}
	}

	/**
	 * <p>solvePOAndOLists.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 * @param subject a {@link java.lang.String} object.
	 * @param indexStart a int.
	 * @return a {@link java.lang.String} object.
	 */
	protected String solvePOAndOLists(final Node node, final String subject, final int indexStart){
		String ret = "";
		/* The loop starts at 1, as explained above this is the first predicate
		 * It always jumps for 2 nodes, meaning it will always point at a predicate
		 * In a second loop every object of the ObjectList is visited and stored
		 * Then the predicate gets visited by teaching him about the curent subject and object
		 * The returned expression is stored in the ret String
		 * This way the loop will put together all predicates with every element of the correspondending ObjectList
		 */
		for (int i = indexStart; i < node.jjtGetNumChildren(); i+=2) {
			final Node currentObjectList =node.jjtGetChild(i+1);
			for(int j = 0; j < currentObjectList.jjtGetNumChildren(); j++){
				final Node currentObject = currentObjectList.jjtGetChild(j);
				if(currentObject instanceof ASTBlankNodePropertyList || currentObject instanceof ASTCollection){
					ret += node.jjtGetChild(i).accept(this, subject, this.getVarOrBlankNode(currentObject)) + "\n";
					ret += this.visitChild(currentObjectList,j);
				} else {
					ret += node.jjtGetChild(i).accept(this, subject, this.visitChild(currentObjectList,j)) + "\n";
				}
			}
		}
		return ret;
	}


	/** {@inheritDoc} */
	@Override
	public String visit(final ASTAVerbType node, final String subject, final String object) {
		return subject + " " + this.visit(node) + " " + object + " .";
	}

	/**
	 * {@inheritDoc}
	 *
	 * AlternativePaths are equal to UNIONS of the input
	 */
	@Override
	public String visit(final ASTPathAlternative node, final String subject, final String object) {
		//example input (x|y) leads to {x}UNION{y}
		return "{" + node.jjtGetChild(0).accept(this, subject, object) + "}\nUNION\n{"
				+ node.jjtGetChild(1).accept(this, subject, object) + "}";
	}

	/**
	 * {@inheritDoc}
	 *
	 * Path sequences get split into two seperated paths using a blank node.
	 * One path leads from the subject to the blank node using the left side of the path sequence as it's predicate.
	 * The second path from the blank node to the object using the right side of the path sequence as it's predicate.
	 */
	@Override
	public String visit(final ASTPathSequence node, final String subject, final String object) {
		final String blank = this.getIntermediateVariable();
		return node.jjtGetChild(0).accept(this, subject, blank) + "\n" + node.jjtGetChild(1).accept(this, blank, object);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTInvers node, final String subject, final String object) {
		return  node.jjtGetChild(0).accept(this, object, subject) ;
	}

	/**
	 * @return String containing the subquery for a zero path...
	 */
	private String getZeroPathSubquery(final String subject, final String object, Node n){
		/* If the minimalDepth is zero a special semantic rule applies
		 * A path of the length zero is recognized as every object by itself.
		 */
		// If the subject and object are fixed parameters then the result is empty, because no variables get bound
		if((subject.charAt(0) != '?' && subject.charAt(0) != '$') && (object.charAt(0) != '?' && object.charAt(0) != '$')){
			return "";
		}
		// If the subject is a fixed parameter the object gets bound to the subject
		else if(subject.charAt(0) != '?' && subject.charAt(0) != '$'){
			return "BIND(" + subject + " AS " + object + ") .\n";
		}
		// If the object is a fixed parameter, the subject gets bound to the object
		else if (object.charAt(0) != '?' && object.charAt(0) != '$'){
			return "BIND(" + object + " AS " + subject + ") .\n";
		}
		/* If none of the above applies the subject and the object have to be variables
		 * Therefore a special SELECT operation is needed, which eliminates duplicates
		 * and binds every subject to itself and every object to itself
		 */
		else {
			// determine whether we are in a graph construct:
			while(n.jjtGetParent()!=null && !(n instanceof ASTGraphConstraint)){
				n = n.jjtGetParent();
			}
			String additionalGraphVariable = "";
			if(n instanceof ASTGraphConstraint){
				final Node child0 = n.jjtGetChild(0);
				if(child0 instanceof ASTVar) {
					additionalGraphVariable = " " + ((ASTVar)child0).toQueryString();
				}
			}
			// intermediate Strings are used for the special queries
			final String intermediatePredicate = this.getIntermediateVariable();
			final String intermediateObject = this.getIntermediateVariable();
			return "{SELECT DISTINCT " + subject + additionalGraphVariable + "\nWHERE{{ " + subject + " " + intermediatePredicate +" "
					+ intermediateObject + " .}\nUNION\n{" +intermediateObject  + " " + intermediatePredicate + " " + subject
					+" .}\n}}\nBIND(" + subject + " AS " + object + ").\n";
		}
	}


	/** {@inheritDoc} */
	@Override
	public String visit(final ASTArbitraryOccurences node, final String subject, final String object) {
		if(SPARQLCoreParserVisitorImplementation.USE_CLOSURE_AND_PATHLENGTHZERO_OPERATORS){
			return subject + " (" +node.jjtGetChild(0).accept(this) + ")* " +object +".\n";
		} else {
			return "{" + this.getZeroPathSubquery(subject, object, node) + "}\nUNION\n{" + subject + " (" + this.visitChild(node,0) + ")+ " + object +"}";
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method UNIONS a Zero-path and a path of length one.
	 */
	@Override
	public String visit(final ASTOptionalOccurence node, final String subject, final String object) {
		if(SPARQLCoreParserVisitorImplementation.USE_CLOSURE_AND_PATHLENGTHZERO_OPERATORS){
			return subject + " (" +node.jjtGetChild(0).accept(this) + ")? " +object +".\n";
		} else {
			return "{" + this.getZeroPathSubquery(subject, object, node) + "}\nUNION\n{" + node.jjtGetChild(0).accept(this, subject, object) + "}";
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTArbitraryOccurencesNotZero node, final String subject,
			final String object) {
		return  subject + " (" +node.jjtGetChild(0).accept(this) + ")+ " +object +".\n";
	}

	/**
	 * {@inheritDoc}
	 *
	 * A negated path takes all possible paths and substracts all paths using the predicate via the MINUS operator.
	 */
	@Override
	public String visit(final ASTNegatedPath node, final String subject, final String object) {
		// Get a intermediatePredicate, serving the query later
		final String intermediatePredicate = this.getIntermediateVariable();
		// Check out the number of alternative paths in this negation
		final int numberOfChildren = node.jjtGetNumChildren();
		// Start the CoreSPARQL Query with all possible combinations of subject and object
		String ret = subject + " " + intermediatePredicate + " " + object + " . MINUS {" ;
		// Now substract all alternative paths via the MINUS operator
		// Using UNION for connecting alternative paths like normal
		for(int i = 0; i < numberOfChildren; i++){
			// { should be there if a UNION is needed later
			if (numberOfChildren > 1) {
				ret += "{";
			}
			ret += node.jjtGetChild(i).accept(this, subject, object);
			// } If a UNION is needed
			if(numberOfChildren > 1) {
				ret += "}";
			}
			// If there are alternative paths left a UNION is needed
			if(numberOfChildren > 1 && i < numberOfChildren - 1) {
				ret += "\nUNION\n";
			}
		}
		// } closing the MINUS term
		ret += "}";
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTVar node, final String subject, final String object) {
		return subject + " " + this.visit(node) + " " + object + " .";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTQuotedURIRef node, final String subject, final String object) {
		return subject + " " + this.visit(node) + " " + object + " .";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ASTQName node, final String subject, final String object) {
		return subject + " " + this.visit(node) + " " + object + " .";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final SimpleNode node, final String subject, final String object) {
		throw new UnsupportedOperationException("This class " + node.getClass() + " does not support an SPARQL1_1ParserPathVisitorStringGenerator!");
	}
}
