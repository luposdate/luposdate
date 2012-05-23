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
import lupos.sparql1_1.*;

public class SPARQL2CoreSPARQLParserVisitorImplementationDumper extends
		SPARQL2CoreSPARQL_rules implements SPARQL1_1ParserVisitorStringGenerator, SPARQL1_1ParserPathVisitorStringGenerator {
	protected final HashMap<Node, String> blankNodeHash = new HashMap<Node, String>();
	protected final HashMap<String, String> prefixHash = new HashMap<String, String>();
	protected final ArrayList<String> realVars = new ArrayList<String>();
	
	protected int bn_nmbr = 0;
	protected boolean construct = false;
	protected boolean mustAddProjectionOnRealVars = false;
	
	public static Class<? extends SPARQL2CoreSPARQLParserVisitorImplementationDumper> SPARQL2CoreSPARQLClass = SPARQL2CoreSPARQLParserVisitorImplementationDumper.class;
	
	public static SPARQL2CoreSPARQLParserVisitorImplementationDumper createInstance() throws InstantiationException, IllegalAccessException{
		SPARQL2CoreSPARQLParserVisitorImplementationDumper result = SPARQL2CoreSPARQLClass.newInstance();
		result.prefixHash.put("rdf", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		return result;
	}

	public static SPARQL2CoreSPARQLParserVisitorImplementationDumper createInstance(final boolean[] rules) throws InstantiationException, IllegalAccessException{
		SPARQL2CoreSPARQLParserVisitorImplementationDumper result = createInstance();
		if (rules.length != result.LENGTH) {
			System.err.println("Parameter rules should have length " + result.LENGTH
					+ ", but it has length " + rules.length);
		} else
			result.rules = rules;
		return result;
	}
	
	public static SPARQL2CoreSPARQLParserVisitorImplementationDumper createInstance(final boolean allRules) throws InstantiationException, IllegalAccessException{
		SPARQL2CoreSPARQLParserVisitorImplementationDumper result = createInstance();
		if (!allRules)
			result.rules = new boolean[] { false, false, false, false, false, false,
					false, false, false, false, false, false, false };
		return result;
	}
		
	protected SPARQL2CoreSPARQLParserVisitorImplementationDumper() {
		super();
	}

	protected String getBlankNodeLabel() {
		mustAddProjectionOnRealVars = true;
		final String ret = "b" + String.valueOf(bn_nmbr++);
		if (this.blankNodeHash.containsValue("_" + ret)
				|| this.realVars.contains("?_" + ret)) {
			return getBlankNodeLabel();
		}
		if (rules[BN_VARS] && !construct) {
			return "?_" + ret;
		} else {
			return "_:" + ret;
		}
	}

	protected void determineRealVariables(final Node node) {
		if (node instanceof ASTVar){
			final String name = ((ASTVar)node).getName();
			blankNodeHash.put(node, name);
			if (!realVars.contains("?" + name)) {
				realVars.add("?" + name);
			}
		}
		final int numberChildren = node.jjtGetNumChildren();
		for (int i = 0; i < numberChildren; i++) {
			determineRealVariables(node.jjtGetChild(i));
		}
	}

	public String visit(final ASTQuery node) {
		// first determine all "real" variables
		determineRealVariables(node);
		return super.visit(node);
	}

	protected URILiteral base = null;

	public String visit(final ASTBaseDecl node) {
		try {
			base = LiteralFactory
					.createURILiteralWithoutLazyLiteral(visitChildren(node));
		} catch (final URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		if (rules[BASE]) {
			return "";
		} else
			return "BASE\t" + visitChildren(node) + "\n";
	}

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

		if (rules[BASE] && base != null) {
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
							+ base.toString().substring(1,
									base.toString().length() - 1) + qRef + ">";
					prefixHash.put(pref, qRef);
				} catch (final Exception e) {
					System.err.println("Undeclared Prefix " + pref + " used");
				}
			}

		}
		// final Object test = visitChild(node, 0);
		else
			prefixHash.put(pref, visitChild(node, 0));
		return "";
	}

	public String visit(final ASTSelectQuery node) {
		String ret = "SELECT ";
		if (node.isDistinct()) {
			ret += "DISTINCT ";
		}
		if (node.isReduced()) {
			ret += "REDUCED ";
		}
		final String suff = visitChildrenSep(node);
		if (node.isSelectAll()) {
			if (mustAddProjectionOnRealVars) {
				final Iterator<String> iter = realVars.iterator();
				String vars = "";
				if (iter.hasNext())
					vars = iter.next();
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

	public String visit(final ASTDescribeQuery node) {
		if (!rules[DESCRIBE]) {
			String ret = "DESCRIBE\t";

			final String suff = visitChildrenSep(node);
			if (node.isDescribeAll()) {
				if (mustAddProjectionOnRealVars) {
					final Iterator<String> iter = realVars.iterator();
					String vars = "";
					if (iter.hasNext())
						vars = iter.next();
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
				determineRealVariables(node);
				vars.addAll(realVars);
			} else {
				for (final Node n : node.getChildren()) {
					if (n instanceof ASTVar) {
						vars.add("?" + ((ASTVar) n).getName());
					} else if (n instanceof ASTQuotedURIRef) {
						vars.add(((ASTQuotedURIRef) n).toQueryString());
					} else if (n instanceof ASTQName) {
						vars.add((String) visit(((ASTQName) n)));
					}
				}
			}
			String suff = "";
			for (final Node n : node.getChildren()) {
				if (n instanceof ASTGroupConstraint) {
					suff = (String) this.visit((ASTGroupConstraint) n);
				}
			}
			String templates = "";
			String optional = "";
			int index = 0;
			for (final String var : vars) {
				String helperVar1 = "?__h" + (index++);
				while (realVars.contains(helperVar1))
					helperVar1 = "?__h" + (index++);
				final String helperVar2 = "?__h" + (index++);
				while (realVars.contains(helperVar2))
					helperVar1 = "?__h" + (index++);
				templates += var + " " + helperVar1 + " " + helperVar2 + ".\n";
				optional += "OPTIONAL{" + var + " " + helperVar1 + " "
						+ helperVar2 + "}.\n";
			}
			final int lastOccurrence = suff.lastIndexOf("}");
			if (lastOccurrence >= 0)
				suff = suff.substring(0, lastOccurrence);
			String stream = "";
//			for (final Node n : node.getChildren()) {
//				if (n instanceof ASTStream) {
//					stream = (String) this.visit((ASTStream) n) + "\n";
//				}
//			}
			return "CONSTRUCT {" + templates + "}\n" + stream + "WHERE {"
					+ suff + optional + "}";
		}
	}

	public String visit(final ASTGroupConstraint node) {
		if (node.jjtGetParent() instanceof ASTSelectQuery
				|| node.jjtGetParent() instanceof ASTAskQuery
				|| node.jjtGetParent() instanceof ASTDescribeQuery
				|| node.jjtGetParent() instanceof ASTConstructQuery
				|| node.jjtGetParent() instanceof ASTQuery) {
			checkFilterOfGroupConstraint(node, new HashSet<String>());
		}
		String ret = "\n{";
		String retEnd = "}";
		if (node.jjtGetParent() instanceof ASTSelectQuery
				|| node.jjtGetParent() instanceof ASTConstructQuery
				|| (node.jjtGetParent() instanceof ASTDescribeQuery && !rules[DESCRIBE])
				|| node.jjtGetParent() instanceof ASTQuery) {
			ret = "\nWHERE \n{\n";
		} 
		final int numberOfChildren = node.jjtGetNumChildren();
		for (int i = 0; i < numberOfChildren; i++) {
			ret += visitChild(node, i);
		}
		return ret + retEnd;
	}

	private void boundVariables(final Node node, final HashSet<String> variables) {
		if (node instanceof ASTFilterConstraint)
			return;
		if (node instanceof ASTVar) {
			variables.add(((ASTVar) node).getName());
		}
		final int numberOfChildren = node.jjtGetNumChildren();
		for (int i = 0; i < numberOfChildren; i++) {
			boundVariables(node.jjtGetChild(i), variables);
		}
	}

	private boolean inScopeOfOptionalOrUnion(final Node node) {
		if (node == null)
			return false;
		else if (node instanceof ASTOptionalConstraint)
			return true;
		else if (node instanceof ASTUnionConstraint)
			return true;
		else if (node instanceof ASTGroupConstraint)
			return false;
		else
			return inScopeOfOptionalOrUnion(node.jjtGetParent());
	}

	private void checkFilterOfGroupConstraint(final Node node,
			final HashSet<String> variables) {
		if (node instanceof ASTGroupConstraint) {
			final HashSet<String> variablesNew = (inScopeOfOptionalOrUnion(node.jjtGetParent())) ? (HashSet<String>) variables
					.clone()
					: new HashSet<String>();
			boundVariables(node, variablesNew);

			final int numberOfChildren = node.jjtGetNumChildren();
			for (int i = 0; i < numberOfChildren; i++) {
				if (node.jjtGetChild(i) instanceof ASTFilterConstraint) {
					try {
						checkFilterConstraint(node.jjtGetChild(i)
								.jjtGetChild(0), variablesNew);
					} catch (final NotBoundException e) {
						node.jjtGetChild(i).clearChildren();
						final ASTBooleanLiteral bl = new ASTBooleanLiteral(0);
						bl.setState(false);
						bl.jjtSetParent(node.jjtGetChild(i));
						node.jjtGetChild(i).jjtAddChild(bl, 0);
					}
				} else
					checkFilterOfGroupConstraint(node.jjtGetChild(i),
							variablesNew);
			}
		} else if (node instanceof ASTUnionConstraint) {
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final HashSet<String> variablesClone = (HashSet<String>) variables
						.clone();
				checkFilterOfGroupConstraint(node.jjtGetChild(i),
						variablesClone);
			}
		} else if (node instanceof ASTOptionalConstraint) {
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final HashSet<String> variablesClone = (HashSet<String>) variables
						.clone();
				checkFilterOfGroupConstraint(node.jjtGetChild(i),
						variablesClone);
			}
		}
	}

	private void checkFilterConstraint(final Node n,
			final HashSet<String> variables) throws NotBoundException {
		if (n instanceof lupos.sparql1_1.ASTOrNode) {
			boolean deleteLeftOperand = false;
			boolean deleteRightOperand = false;
			try {
				checkFilterConstraint(n.jjtGetChild(0), variables);
			} catch (final NotBoundException nbe) {
				deleteLeftOperand = true;
			}
			try {
				checkFilterConstraint(n.jjtGetChild(1), variables);
			} catch (final NotBoundException nbe) {
				deleteRightOperand = true;
			}
			if (deleteLeftOperand && deleteRightOperand)
				throw new NotBoundException(
						"Both operands of an Or-Operator in a filter throw NotBoundException!");
			if (deleteLeftOperand) {
				n.jjtGetParent().replaceChild2(n, n.jjtGetChild(1));
				n.jjtGetChild(1).jjtSetParent(n.jjtGetParent());
			}
			if (deleteRightOperand) {
				n.jjtGetParent().replaceChild2(n, n.jjtGetChild(0));
				n.jjtGetChild(0).jjtSetParent(n.jjtGetParent());
			}
		} else if (n instanceof lupos.sparql1_1.ASTVar) {
			if (!variables.contains(((lupos.sparql1_1.ASTVar) n).getName()))
				throw new NotBoundException("Variable "
						+ ((lupos.sparql1_1.ASTVar) n).getName() + " not bound");
		} else {
			if(!(n instanceof ASTExists || n instanceof ASTNotExists || n instanceof ASTBoundFuncNode)){
				for(int i=0; i<n.jjtGetNumChildren(); i++){
					checkFilterConstraint(n.jjtGetChild(i), variables);
				}
			}
		}
	}

	private void nestedGroups(final ASTGroupConstraint node) {
		final ArrayList<ASTVar> vars_in_scope = new ArrayList<ASTVar>();
		final ArrayList<ASTVar> vars_in_filter = new ArrayList<ASTVar>();
		checkChildren(vars_in_scope, vars_in_filter, node, false);
		boolean indicator = true;
		if (vars_in_filter.size() > 0) {
			indicator = false;
			for (final Iterator<ASTVar> iter = vars_in_filter.iterator(); iter
					.hasNext();) {
				indicator = indicator || vars_in_scope.contains(iter.next());
			}
		}
	}

	private void checkChildren(final ArrayList<ASTVar> vars_in_scope,
			final ArrayList<ASTVar> vars_in_filter, final Node currentNode, boolean filter) {
		if (currentNode instanceof ASTVar) {
			if (filter) {
				if (!vars_in_filter.contains(currentNode))
					vars_in_filter.add((ASTVar) currentNode);
			} else {
				if (!vars_in_scope.contains(currentNode))
					vars_in_scope.add((ASTVar) currentNode);
			}
		}
		if (currentNode instanceof ASTGroupConstraint) {
			nestedGroups((ASTGroupConstraint) currentNode);
		}
		if (currentNode instanceof ASTFilterConstraint) {
			filter = true;
		}
		for (int i = 0; i < currentNode.jjtGetNumChildren(); i++) {
			checkChildren(vars_in_scope, vars_in_filter, currentNode
					.jjtGetChild(i), filter);
		}
	}

	public String visit(final ASTFilterConstraint node) {
		final Node n = new SimpleNode(0);
		final Node parent = node.jjtGetParent();
		n.jjtAddChild(node, 0);
		node.jjtSetParent(n);
		parent.replaceChild2(node, n);
		n.jjtSetParent(parent);
		applyRules(node);
		String ret = "";
		String prefix = (parent instanceof ASTOrderConditions || parent instanceof ASTHaving)? "" : "FILTER";
		String postfix = (parent instanceof ASTOrderConditions || parent instanceof ASTHaving)? "" : ".\n";
		final int numberOfChildren = n.jjtGetNumChildren();
		for (int i = 0; i < numberOfChildren; i++) {
			Node currentChild=n.jjtGetChild(i);
			for(int j=0; j<currentChild.jjtGetNumChildren();j++){
				ret += prefix + "(" + visitChild(currentChild, j) + ")"+postfix;
			}				
		}
		return ret;
	}

	public String dumpFilter(final ASTFilterConstraint node) {
		return "FILTER(" + visitChild(node.jjtGetChild(0), 0) + ").";
	}

	private int applyRules(final Node node) {
		if (rules[FILTER_AND] && node instanceof ASTFilterConstraint
				&& node.jjtGetChild(0) instanceof ASTAndNode) {
			final Node parent = node.jjtGetParent();
			if (filter_and((ASTFilterConstraint) node))
				return applyRules(parent);
		}
		if (rules[FILTER_OR] && node instanceof ASTAndNode) {
			if (nestedOr((ASTAndNode) node))
				return applyRules(node.jjtGetParent());
		}
		if (rules[FILTER_NOT] && node instanceof ASTNotNode) {
			if (downNot(node))
				return applyRules(node.jjtGetParent());
		}
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			applyRules(node.jjtGetChild(i));
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
		final int ind = checkNestedOr(node);
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
			if (node.jjtGetChild(i) instanceof ASTOrNode)
				return i;
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

	public String visit(final ASTConstructTemplate node) {
		this.construct = true;
		final String ret = "{" + visitChildrenSep(node) + "}";
		this.construct = false;
		return ret;
	}

	public String visit(final ASTNodeSet node) {
		return visitChild(node, 0)+this.solvePOAndOLists(node, getVarOrBlankNode(node.jjtGetChild(0)), 1);
	}

	private String getVarOrBlankNode(final Node node) {
		if (node instanceof ASTVar)
			return "?" + ((ASTVar) node).getName();
		final String sub;
		if (blankNodeHash.containsKey(node)) {
			sub = blankNodeHash.get(node);
		} else
			sub = getBlankNodeLabel();
		blankNodeHash.put(node, sub);
		return sub;
	}

	public String visit(final ASTObjectList node) {
		if (rules[PO_LISTS]) {
			throw new Error("This code position should not be reached!");
		} else {
			return super.visit(node);
		}
	}

	public String visit(final ASTAVerbType node) {
		if (rules[RDF_TYPE]) {
			return "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
		}
		return "a";
	}

	public String visit(final ASTBlankNodePropertyList node) {
		if (rules[BLANK_NODES]) {
			/* The BlankNodePropertyList has at least 2 Children
			 * Every uneven node holds a Predicate
			 * Every even node except the first one holds an ObjectList
			 */
			return this.solvePOAndOLists(node, this.getVarOrBlankNode(node), 0);
		}
		return super.visit(node);
	}

	public String visit(final ASTCollection node) {
		if (rules[RDF_COLLECTIONS]) {
			String bcur = getVarOrBlankNode(node);
			String ret = "";
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				if (node.jjtGetChild(i) instanceof ASTBlankNodePropertyList
						|| node.jjtGetChild(i) instanceof ASTCollection) {
					ret += bcur
							+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> "
							+ getVarOrBlankNode(node.jjtGetChild(i)) + " .\n"
							+ visitChild(node, i);
				} else {
					ret += bcur
							+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> "
							+ visitChild(node, i) + " .\n";
				}
				String bnext =(i < node.jjtGetNumChildren() - 1)? getBlankNodeLabel() : "<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>";
				ret += bcur
						+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> "
						+ bnext + " .\n";
				bcur = bnext;
			}
			return ret;
		} else return super.visit(node);
	}

	public String visit(final ASTVar node) {
		return "?" + node.getName();
	}

	public String visit(final ASTQName node) {
		String ret = prefixHash.get(node.getNameSpace());
		if (ret == null) {
			ret = "<>";
			if (rules[BASE]) {
				try {
					ret = base.toString();
				} catch (final Exception e) {
					System.err.println("Undeclared Prefix "
							+ node.getNameSpace() + " used");
				}
			}
		}
		if (node.getLocalName().equals(""))
			return ret;
		return ret.substring(0, ret.length() - 1) + node.getLocalName() + ">";
	}

	public String visit(final ASTEmptyNode node) {
		if (rules[BLANK_NODES]) {
			if (blankNodeHash.containsKey(node)) {
				return blankNodeHash.get(node);
			}
			final String tbn = getBlankNodeLabel();
			blankNodeHash.put(node, tbn);
			return tbn;
		}
		return "[]";
	}

	public String visit(final ASTQuotedURIRef node) {
		if (rules[BASE] && base != null) {
			return "<"
					+ base.toString()
							.substring(1, base.toString().length() - 1)
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

	public String visit(final ASTBlankNode node) {
		if (rules[BN_VARS]) {
			String name = variablesOfBlankNodes.get(node.getIdentifier());
			if (name == null) {
				name = getBlankNodeLabel();
				variablesOfBlankNodes.put(node.getIdentifier(), name);
			}
			return name;
		} else
			return node.getIdentifier();
	}
	
	public String getInsert(Node node){
		String vChild1=visitChild(node, 1);
		String vChild0=visitChild(node, 0);
		if(vChild1.compareTo(vChild0)==0){
			return " # Adding to the same graph does not have any effect!\n";
		}
		boolean flag1 =(node.jjtGetChild(1) instanceof ASTDefault);
		String start1=flag1?"":"GRAPH "+vChild1+" {";
		String end1=flag1?"":"}";
		boolean flag2 =(node.jjtGetChild(0) instanceof ASTDefault);
		String start2=flag2?"":"GRAPH "+vChild0+" {";
		String end2=flag2?"":"}";
		return " INSERT { "+start1+"?s ?p ?o."+end1+"} WHERE { "+start2+"?s ?p ?o."+end2+"}";		
	}

	@Override
	public String visit(ASTAdd node) {
		if(rules[ADD]){
			return getInsert(node);
		} else return super.toString();	
	}
	
	public String getDropAndInsert(Node node){
		String vChild1=visitChild(node, 1);
		if(vChild1.compareTo(visitChild(node, 0))==0){
			return " # Copying to the same graph does not have any effect!\n";
		}
		String dropOrClear = (vChild1.compareTo("DEFAULT")==0)?" CLEAR":" DROP";
		return dropOrClear+" SILENT "+(node.jjtGetChild(1) instanceof ASTDefault?"":"GRAPH ")+vChild1+";\n"+getInsert(node);
	}
	
	@Override
	public String visit(ASTCopy node) {
		if(rules[COPY]){
			return getDropAndInsert(node);
		} else return super.toString();
	}

	@Override
	public String visit(ASTMove node) {
		if(rules[MOVE]){
			String vChild0 = visitChild(node,0);
			if(vChild0.compareTo(visitChild(node, 1))==0){
				return " # Moving to the same graph does not have any effect!\n";
			}			
			String dropOrClear = (vChild0.compareTo("DEFAULT")==0)?"CLEAR":"DROP";
			return getDropAndInsert(node) + ";\n"+dropOrClear+" SILENT "+(node.jjtGetChild(0) instanceof ASTDefault?"":"GRAPH ")+vChild0;
		} else return super.toString();
	}

	/**
	 * 
	 * @return An unique variable which is definitely not used in the original query
	 */
	protected String getIntermediateVariable() {
		this.mustAddProjectionOnRealVars = true;
		final String ret = "b" + String.valueOf(bn_nmbr++);
		/* checks if variable is already used
		 * if true generate a new one
		 * else return the variable in form ?_charNumber
		 */
		if (this.blankNodeHash.containsValue("_" + ret)
				|| this.realVars.contains("?_" + ret)) {
			return getIntermediateVariable();
		}
		return "?_" + ret;
	}
	

	/** This method visits an ASTTripleSet and computes a String, representing the expression of the Triple set.
	 * @param node The visited ASTTripleSet
	 * @return a String put together by Subject Predicate and Object of the Triple Set and a break
	 */
	public String visit(final ASTTripleSet node) {
		if(rules[PO_LISTS]){
			/* The Triple Set has at least 3 Children
			 * The first child is always the subject
			 * Every even node holds a Predicate
			 * Every uneven node except the first one holds an ObjectList
			 */
			return this.solvePOAndOLists(node, visitChild(node,0), 1);
		} else return super.visit(node);
	}
	
	protected String solvePOAndOLists(Node node, String subject, int indexStart){
		String ret = "";
		/* The loop starts at 1, as explained above this is the first predicate
		 * It always jumps for 2 nodes, meaning it will always point at a predicate
		 * In a second loop every object of the ObjectList is visited and stored
		 * Then the predicate gets visited by teaching him about the curent subject and object
		 * The returned expression is stored in the ret String
		 * This way the loop will put together all predicates with every element of the correspondending ObjectList 
		 */
		for (int i = indexStart; i < node.jjtGetNumChildren(); i+=2) {
			Node currentObjectList =node.jjtGetChild(i+1);
			for(int j = 0; j < currentObjectList.jjtGetNumChildren(); j++){
				Node currentObject = currentObjectList.jjtGetChild(j);					
				if(currentObject instanceof ASTBlankNodePropertyList || currentObject instanceof ASTCollection){
					ret += node.jjtGetChild(i).accept(this, subject, getVarOrBlankNode(currentObject)) + "\n";
					ret += visitChild(currentObjectList,j);
				} else {
					ret += node.jjtGetChild(i).accept(this, subject, visitChild(currentObjectList,j)) + "\n";
				}
			}
		}
		return ret;
	}


	/**
	 * @param node
	 * @param subject
	 * @param object
	 * @return The variable inside this node surrounded by subject and object
	 */
	@Override
	public String visit(ASTAVerbType node, String subject, String object) {
		return subject + " " + visit(node) + " " + object + " .";
	}
	
	/**
	 * AlternativePaths are equal to UNIONS of the input
	 * @param node
	 * @param subject
	 * @param object
	 * @return The result string for AlternativePath Querys(x|y)
	 */
	@Override
	public String visit(ASTPathAlternative node, String subject, String object) {
		//example input (x|y) leads to {x}UNION{y}
		return "{" + node.jjtGetChild(0).accept(this, subject, object) + "}\nUNION\n{" 
				+ node.jjtGetChild(1).accept(this, subject, object) + "}";
	}

	/**
	 * Path sequences get split into two seperated paths using a blank node.
	 * One path leads from the subject to the blank node using the left side of the path sequence as it's predicate.
	 * The second path from the blank node to the object using the right side of the path sequence as it's predicate.
	 * @param node
	 * @param subject
	 * @param object
	 * @return 
	 */
	@Override
	public String visit(ASTPathSequence node, String subject, String object) {
		String blank = getIntermediateVariable();
		return node.jjtGetChild(0).accept(this, subject, blank) + "\n" + node.jjtGetChild(1).accept(this, blank, object);
	}

	/**
	 * @param node
	 * @param subject
	 * @param object
	 * @return A String where subject and object are exchanged around the predicate 
	 */
	@Override
	public String visit(ASTInvers node, String subject, String object) {
		return  node.jjtGetChild(0).accept(this, object, subject) ;
	}

	/**
	 * @return String containing the subquery for a zero path...
	 */
	private String getZeroPathSubquery(String subject, String object, Node n){
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
				Node child0 = n.jjtGetChild(0);
				if(child0 instanceof ASTVar)
					additionalGraphVariable = " " + ((ASTVar)child0).toQueryString();
			}
			// intermediate Strings are used for the special queries
			String intermediatePredicate = getIntermediateVariable();
			String intermediateObject = getIntermediateVariable();
			return "{SELECT DISTINCT " + subject + additionalGraphVariable + "\nWHERE{{ " + subject + " " + intermediatePredicate +" " 
					+ intermediateObject + " .}\nUNION\n{" +intermediateObject  + " " + intermediatePredicate + " " + subject 
					+" .}\n}}\nBIND(" + subject + " AS " + object + ").\n";
		}
	}

	
	@Override
	public String visit(ASTArbitraryOccurences node, String subject, String object) {
		return "{" + getZeroPathSubquery(subject, object, node) + "}\nUNION\n{" + subject + " (" + visitChild(node,0) + ")+ " + object +"}";
	}

	/**
	 * This method UNIONS a Zero-path and a path of length one.
	 * @param node
	 * @param subject
	 * @param object
	 * @return 
	 */
	@Override
	public String visit(ASTOptionalOccurence node, String subject, String object) {
		return "{" + getZeroPathSubquery(subject, object, node) + "}\nUNION\n{" + node.jjtGetChild(0).accept(this, subject, object) + "}";
	}

	@Override
	public String visit(ASTArbitraryOccurencesNotZero node, String subject,
			String object) {
		return  subject + " (" +node.jjtGetChild(0).accept(this) + ")+ " +object +".\n"; 
	}

	@Override
	public String visit(ASTGivenOccurences node, String subject, String object) {
		// Temporarely save the subject because it get later altered
		String savedSubject = subject;
		// Minimal given occurence, predefined as zero
		int minimalDepth = node.getLowerLimit();
		// Maximal given occurence
		int maximalDepth = node.getUpperLimit();
		String ret = "";
		/* If the minimalDepth is zero a special semantic rule applies
		 * A path of the length zero is recognized as every object by itself. 
		 */
		if(minimalDepth == 0){
			if(minimalDepth<maximalDepth || maximalDepth == ASTGivenOccurences.INFINITY){ 
				ret +="{" + getZeroPathSubquery(subject, object, node) + "}\nUNION\n";
				minimalDepth++; // we already dealt with zero path!
			} else return getZeroPathSubquery(subject, object, node)+"\n"; 
		}
		/* If the maximalDepth is Integer.MAX_Value then the upper bound is equivalent to the * operator
		 * Therefore we calculate the + Factor, because a path of length {0} was allready calculated if necessary
		 */
		if(maximalDepth == ASTGivenOccurences.INFINITY){
			String blank;
			// The differences between minimal depth and maximal depth is  the number of needed paths 
			ret += "{";
			for(int i = 1; i < minimalDepth; i++){
				// If this is a path of the form {n,} we need to UNION the next path to it
			
				// We create now a path of length i-1 with blank nodes connecting from subject to object
				blank = getIntermediateVariable();
				ret += node.jjtGetChild(0).accept(this, subject, blank) + "\n";
				subject = blank;
			}
			// To avoid ugly linebreaks we use the next step without a linebreak 
			ret += subject + " (" + node.jjtGetChild(0).accept(this)  + ")+ " + object ;
			ret += "}";
			// If there's another longer path left it needs to be UNIONed to this one
		}
		else{
			String blank;
			// The differences between minimal depth and maximal depth is  the number of needed paths 
			for(int i = minimalDepth; i <= maximalDepth; i++){
				// A path always begins with the subject
				subject = savedSubject;
				// If this is not a path of the form {n} we need to UNION the next path to it
				ret += "{";
				// We create now a path of length i-1 with blank nodes connecting from subject to object
				for(int j = 1; j < i; j++){
					blank = getIntermediateVariable();
					ret += node.jjtGetChild(0).accept(this, subject, blank) + "\n";
					subject = blank;
				}
				// To avoid ugly linebreaks we use the next step without a linebreak 
				ret += node.jjtGetChild(0).accept(this, subject, object);
				ret += "}";
				// If there's another longer path left it needs to be UNIONed to this one
				if(i < maximalDepth) ret +="\nUNION\n";
			}
		}
		return ret;
	}
	
	/**
	 * A negated path takes all possible paths and substracts all paths using the predicate via the MINUS operator.
	 * @param node
	 * @param subject
	 * @param object
	 * @return 
	 */
	@Override
	public String visit(ASTNegatedPath node, String subject, String object) {
		// Get a intermediatePredicate, serving the query later
		String intermediatePredicate = getIntermediateVariable();
		// Check out the number of alternative paths in this negation
		int numberOfChildren = node.jjtGetNumChildren();
		// Start the CoreSPARQL Query with all possible combinations of subject and object
		String ret = subject + " " + intermediatePredicate + " " + object + " . MINUS {" ;
		// Now substract all alternative paths via the MINUS operator
		// Using UNION for connecting alternative paths like normal
		for(int i = 0; i < numberOfChildren; i++){
			// { should be there if a UNION is needed later
			if (numberOfChildren > 1) 
				ret += "{";
			ret += node.jjtGetChild(i).accept(this, subject, object);
			// } If a UNION is needed
			if(numberOfChildren > 1) 
				ret += "}";
			// If there are alternative paths left a UNION is needed
			if(numberOfChildren > 1 && i < numberOfChildren - 1) 
				ret += "\nUNION\n";
		}
		// } closing the MINUS term
		ret += "}";
		return ret; 
	}

	/**
	 * @param node
	 * @param subject
	 * @param object
	 * @return A string surrounding the variable by subject and object
	 */
	@Override
	public String visit(ASTVar node, String subject, String object) {
		return subject + " " + visit(node) + " " + object + " .";
	}

	/**
	 * @param node
	 * @param subject
	 * @param object
	 * @return A string surrounding the variable by subject and object
	 */
	@Override
	public String visit(ASTQuotedURIRef node, String subject, String object) {
		return subject + " " + visit(node) + " " + object + " .";
	}

	/**
	 * @param node
	 * @param subject
	 * @param object
	 * @return A string surrounding the variable by subject and object
	 */
	@Override
	public String visit(ASTQName node, String subject, String object) {
		return subject + " " + visit(node) + " " + object + " .";
	}

	@Override
	public String visit(SimpleNode node, String subject, String object) {
		throw new UnsupportedOperationException("This class " + node.getClass() + " does not support an SPARQL1_1ParserPathVisitorStringGenerator!");
	}
}
