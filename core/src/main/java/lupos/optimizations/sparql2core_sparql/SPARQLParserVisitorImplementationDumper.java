
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

import lupos.sparql1_1.*;
public class SPARQLParserVisitorImplementationDumper implements
		SPARQL1_1ParserVisitorStringGenerator {

	protected String tab = "";

	/**
	 * <p>visitChildren.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String visitChildren(final Node node) {
		final int numberChildren = node.jjtGetNumChildren();
		String value = "";
		for (int i = 0; i < numberChildren; i++) {
			value += visitChild(node, i);
		}
		return value;
	}
	
	/**
	 * <p>visitRemainingChildren.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 * @param start a int.
	 * @return a {@link java.lang.String} object.
	 */
	protected String visitRemainingChildren(final Node node, int start) {
		final int numberChildren = node.jjtGetNumChildren();
		String value = "";
		for (int i = start; i < numberChildren; i++) {
			value += visitChild(node, i);
		}
		return value;
	}
	
	/**
	 * <p>visitChildrenCommaSeparated.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String visitChildrenCommaSeparated(final Node node) {
		return visitChildrenSeparated(node, ", ");
	}

	/**
	 * <p>visitChildrenSeparated.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 * @param sep a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String visitChildrenSeparated(final Node node, String sep) {
		final int numberChildren = node.jjtGetNumChildren();
		String value = "";
		if(numberChildren>0){
			value += visitChild(node, 0);
			for (int i = 1; i < numberChildren; i++) {
				value += sep+visitChild(node, i);
			}
		}
		return value;
	}
	
	/**
	 * <p>visitChildrenSep.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String visitChildrenSep(final Node node) {
		return visitChildrenSeparated(node, " ");
	}
	
	/**
	 * <p>visitChild.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 * @param index a int.
	 * @return a {@link java.lang.String} object.
	 */
	protected String visitChild(final Node node,
			final int index) {
		return ((String) node.jjtGetChild(index).accept((SPARQL1_1ParserVisitorStringGenerator) this));
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.SimpleNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final SimpleNode node) {
		if(node.getClass()==SimpleNode.class)
			return visitChildren(node);
		else {
			return node.accept(this);
		}
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTBaseDecl} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTBaseDecl node) {
		return "BASE\t" + visitChildren(node) + "\n";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTPrefixDecl} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTPrefixDecl node) {
		final String pref = node.getPrefix();
		return "PREFIX " + pref + ": " + visitChild(node, 0) + "\n";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTSelectQuery} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTSelectQuery node) {
		String ret = "SELECT ";
		int i = 0;
		if (node.isDistinct()) {
			ret += "DISTINCT ";
		}
		if (node.isReduced()) {
			ret += "REDUCED ";
		}
		if (node.isSelectAll()) {
			ret += "*";
		} else {
			while (i < node.jjtGetNumChildren()
					&& node.jjtGetChild(i) instanceof ASTVar) {
				ret += visitChild(node, i++) + " ";
			}
		}
		ret += "\n";
		while (i < node.jjtGetNumChildren()) {
			ret += visitChild(node, i++);
		}
		return ret;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTConstructQuery} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTConstructQuery node) {
		return "CONSTRUCT " + visitChildren(node);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTDescribeQuery} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTDescribeQuery node) {
		return "DESCRIBE\t" + visitChildren(node) + "\n";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTAskQuery} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTAskQuery node) {
		String ret = "";
		ret = "ASK \n";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			ret += visitChild(node, i) + "\n";
		}
		return ret;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTDefaultGraph} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTDefaultGraph node) {
		if(node.jjtGetParent() instanceof ASTModify)
			return "\nUSING\t" + visitChildren(node);
		else return "\nFROM\t" + visitChildren(node);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTNamedGraph} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTNamedGraph node) {
		if(node.jjtGetParent() instanceof ASTModify)
			return "\nUSING NAMED\t" + visitChildren(node);
		else return "\nFROM NAMED\t" + visitChildren(node);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTOrderConditions} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTOrderConditions node) {
		String ret = "ORDER BY";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			ret+=" ";
			if (node.jjtGetChild(i) instanceof ASTDescOrder) {
				ret += "DESC(" + visitChild(node, ++i) + ")";
			} else if (node.jjtGetChild(i) instanceof ASTAscOrder) {
				ret += "ASC(" + visitChild(node, ++i) + ")";
			} else {
				ret += visitChild(node, i);
			}
		}
		return ret + "\n";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTAscOrder} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTAscOrder node) {
		return node.toString();
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTDescOrder} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTDescOrder node) {
		return node.toString();
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLimit} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTLimit node) {
		return "LIMIT " + node.getLimit() + "\n";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTOffset} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTOffset node) {
		return "OFFSET " + node.getOffset() + "\n";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTGroupConstraint} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTGroupConstraint node) {
		String ret = tab + "{\n";
		tab += "\t";
		final String retEnd = "}\n";
		if (node.jjtGetParent() instanceof ASTSelectQuery
				|| node.jjtGetParent() instanceof ASTConstructQuery
				|| node.jjtGetParent() instanceof ASTDescribeQuery
				|| node.jjtGetParent() instanceof ASTQuery) {
			ret = "WHERE {\n";
		}
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			ret += visitChild(node, i);
		}
		return ret + retEnd;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTOptionalConstraint} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTOptionalConstraint node) {
		return tab + "OPTIONAL " + visitChildren(node);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTUnionConstraint} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTUnionConstraint node) {
		return tab+visitChild(node, 0) + " UNION \n"+tab + visitChild(node, 1);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTFilterConstraint} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTFilterConstraint node) {
		String common = "(" + visitChildren(node) + ") .\n";
		Node parent = node.jjtGetParent();
		if(parent instanceof ASTOrderConditions || parent instanceof ASTHaving)
			return tab + common;
		else return tab + "FILTER" + common;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTFunctionCall} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTFunctionCall node) {
		return "(" + visitChildren(node) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTArguments} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTArguments node) {
		final int number = node.getChildren().length;
		String name = "(";
		for (int i = 0; i < number; i++) {
			name = name + visitChild(node, i);
			if (number > 1 && i < number - 1)
				name = name + ",";
		}
		name = name + ")";
		return name;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTConstructTemplate} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTConstructTemplate node) {
		final String ret = "{" + visitChildrenSep(node) + "}";
		return ret;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTTripleSet} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTTripleSet node) {
		String ret = "";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if(i%2==1 && i>1) // property lists?
				ret+=" ;";
			ret += " "+visitChild(node, i);
		}		
		return ret + " .\n";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTNodeSet} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTNodeSet node) {
		return tab + visitChildren(node);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTObjectList} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTObjectList node) {
		if (node.jjtGetNumChildren() == 1) {
			return visitChild(node, 0);
		}
		int j = 0;
		String ret = "";
		Node child = node.jjtGetChild(j);
		if (//!(child instanceof ASTLiteralInterface)&&
				 !(child instanceof ASTVar)) {
			ret += visitChild(node, j++) + visitChild(node, j++);
		} else {
			ret += visitChild(node, j);
		}
		for (int i = j; i < node.jjtGetNumChildren(); i++) {
			child = node.jjtGetChild(i);
			if (//!(child instanceof ASTLiteralInterface)&&
					 !(child instanceof ASTVar)) {
				ret += ", " + visit((SimpleNode) child)
						+ visitChild(node, ++i);
			} else {
				ret += ", " + visitChild(node, i);
			}
		}
		return ret;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTAVerbType} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTAVerbType node) {
		return "a";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTBlankNodePropertyList} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTBlankNodePropertyList node) {
		return "[" + visitChildrenSep(node) + "] ";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTCollection} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTCollection node) {
		return "(" + visitChildrenSep(node) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTVar} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTVar node) {
		return "?" + node.getName();
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTOrNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTOrNode node) {
		if (node.jjtGetNumChildren() == 2) {
			return "(" + visitChild(node, 0) + " || "
					+ visitChild(node, 1) + ")";
		} else {
			String ret = visitChild(node, 0);
			for (int i = 1; i < node.jjtGetNumChildren(); i++) {
				ret += " || " + visitChild(node, i);
			}
			return "(" + ret + ")";
		}
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTAndNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTAndNode node) {
		if (node.jjtGetNumChildren() == 2) {
			return "(" + visitChild(node, 0) + " && "
					+ visitChild(node, 1) + ")";
		} else {
			String ret = visitChild(node, 0);
			for (int i = 1; i < node.jjtGetNumChildren(); i++) {
				ret += " && " + visitChild(node, i);
			}
			return "(" + ret + ")";
		}
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTEqualsNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTEqualsNode node) {
		if (node.jjtGetChild(1) instanceof ASTDoubleCircumflex) {
			return "=" + visitChildren(node);
		}
		return "(" + visitChild(node, 0) + " = "
				+ visitChild(node, 1) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTNotEqualsNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTNotEqualsNode node) {
		if (node.jjtGetChild(1) instanceof ASTDoubleCircumflex) {
			return "!=" + visitChildren(node);
		}
		return "(" + visitChild(node, 0) + " != "
				+ visitChild(node, 1) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLessThanNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTLessThanNode node) {
		if (node.jjtGetChild(1) instanceof ASTDoubleCircumflex) {
			return "<" + visitChildren(node);
		}
		return "(" + visitChild(node, 0) + " < "
				+ visitChild(node, 1) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLessThanEqualsNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTLessThanEqualsNode node) {
		if (node.jjtGetChild(1) instanceof ASTDoubleCircumflex) {
			return "<=" + visitChildren(node);
		}
		return "(" + visitChild(node, 0) + " <= "
				+ visitChild(node, 1) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTGreaterThanNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTGreaterThanNode node) {
		if (node.jjtGetChild(1) instanceof ASTDoubleCircumflex) {
			return ">" + visitChildren(node);
		}
		return "(" + visitChild(node, 0) + " > "
				+ visitChild(node, 1) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTGreaterThanEqualsNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTGreaterThanEqualsNode node) {
		if (node.jjtGetChild(1) instanceof ASTDoubleCircumflex) {
			return ">=" + visitChildren(node);
		}
		return "(" + visitChild(node, 0) + " >= "
				+ visitChild(node, 1) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTAdditionNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTAdditionNode node) {
		return "(" + visitChild(node, 0) + " + "
				+ visitChild(node, 1) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTSubtractionNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTSubtractionNode node) {
		return "(" + visitChild(node, 0) + " - "
				+ visitChild(node, 1) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTMultiplicationNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTMultiplicationNode node) {
		return "(" + visitChild(node, 0) + " * "
				+ visitChild(node, 1) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTDivisionNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTDivisionNode node) {
		return "(" + visitChild(node, 0) + " / "
				+ visitChild(node, 1) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTNotNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTNotNode node) {
		return "!(" + visitChildren(node) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTPlusNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTPlusNode node) {
		return "+" + visitChildren(node);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTMinusNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTMinusNode node) {
		return "-" + visitChildren(node);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStrFuncNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTStrFuncNode node) {
		return "STR(" + visitChildren(node) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLangFuncNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTLangFuncNode node) {
		return "LANG(" + visitChildren(node) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLangMatchesFuncNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTLangMatchesFuncNode node) {
		int i = 0;
		String ret = "LANGMATCHES(" + visitChild(node, i++);
		while (i < node.jjtGetNumChildren()) {
			ret += "," + visitChild(node, i++);
		}
		return ret + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTDataTypeFuncNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTDataTypeFuncNode node) {
		return "DATATYPE(" + visitChildren(node) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTBoundFuncNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTBoundFuncNode node) {
		return "BOUND(" + visitChildren(node) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTisURIFuncNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTisURIFuncNode node) {
		return "isIRI(" + visitChildren(node) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTisBlankFuncNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTisBlankFuncNode node) {
		return "isBLANK(" + visitChildren(node) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTisLiteralFuncNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTisLiteralFuncNode node) {
		return "isLITERAL(" + visitChildren(node) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTRegexFuncNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTRegexFuncNode node) {
		int i = 0;
		String ret = "REGEX(" + visitChild(node, i++);
		while (i < node.jjtGetNumChildren()) {
			ret += "," + visitChild(node, i++);
		}
		return ret + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTDoubleCircumflex} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTDoubleCircumflex node) {
		if (node.jjtGetNumChildren() > 1)
			return visitChild(node, 0) + "^^" + visitChild(node, 1);
		return "^^" + visitChild(node, 0);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLangTag} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTLangTag node) {
		return visitChildren(node) + node.getLangTag();
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTBooleanLiteral} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTBooleanLiteral node) {
		return String.valueOf(node.getState());
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStringLiteral} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTStringLiteral node) {
		return node.getStringLiteral();
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTQName} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTQName node) {
		return node.getNameSpace() + ":" + node.getLocalName();
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTEmptyNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTEmptyNode node) {
		return "[]";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTQuotedURIRef} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTQuotedURIRef node) {
		return "<" + node.getQRef() + ">";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTInteger} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTInteger node) {
		return String.valueOf(node.getValue());
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTFloatingPoint} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTFloatingPoint node) {
		return node.getValue();
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTSameTermFuncNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTSameTermFuncNode node) {
		return "sameTerm(" + visitChild(node, 0) + ","
				+ visitChild(node, 1) + ")";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTisIRIFuncNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTisIRIFuncNode node) {
		return "isIRI(" + visitChildren(node) + ")";
	}


	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStream} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTStream node) {
		String ret = " STREAM ";
		if (node.isDuration() || node.isTriples()) {
			ret += "INTERMEDIATERESULT ";
			if (node.isDuration())
				ret += "DURATION ";
			else
				ret += "TRIPLES ";
			ret += node.getValue();
		}
		return ret;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTWindow} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTWindow node) {
		String ret = " WINDOW ";
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if ((node.jjtGetChild(i) instanceof ASTStart) || (node.jjtGetChild(i) instanceof ASTEnd) || (node.jjtGetChild(i) instanceof ASTType))
				ret += visitChild(node, i);
		}
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if (!((node.jjtGetChild(i) instanceof ASTStart) || (node.jjtGetChild(i) instanceof ASTEnd) || (node.jjtGetChild(i) instanceof ASTType)))
				ret += visitChild(node, i);
		}
		return ret;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStart} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTStart node) {
		return "START " + visitChild(node, 0);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTEnd} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTEnd node) {
		return "END " + visitChild(node, 0);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTType} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTType node) {
		String ret = " TYPE ";
		if (node.isDuration()){
			ret += "SLIDINGDURATION ";
		} else if (node.isTriples()){
			ret += "SLIDINGTRIPLES ";
		} else {
			ret += "INSTANCE " + visitChild(node, 0) + " ";
			if (node.isInstancesNumber()){
				ret += "SLIDINGINSTANCES ";
			} else {
				ret += "SLIDINGDURATION ";
			}
		}
		return ret + node.getValue();
	}

//	public String visit(final ASTTimeFuncNode node) {
//		return " getTime(" + visitChildren(node) + ") ";
//	}
//
//	public String visit(final ASTPosInStreamFuncNode node) {
//		return " getPosInStream(" + visitChildren(node) + ") ";
//	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTRDFLiteral} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTRDFLiteral node) {
		return visitChildren(node);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTNIL} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTNIL node) {
		return "<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTBlankNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTBlankNode node) {
		return node.getIdentifier();
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTAs} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTAs node) {
		return "("+visitChild(node, 0) + " AS " + visitChild(node, 1)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTAdd node) {
		return " ADD "+(node.isSilent()?"SILENT ":"")+visitChild(node, 0)+" TO "+ visitChild(node, 1);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTMove node) {
		return " MOVE "+(node.isSilent()?"SILENT ":"")+visitChild(node, 0)+" TO "+ visitChild(node, 1);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTCopy node) {
		return " COPY "+(node.isSilent()?"SILENT ":"")+visitChild(node, 0)+" TO "+ visitChild(node, 1);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTService node) {
		return " SERVICE "+(node.isSilent()?"SILENT ":"")+visitChild(node, 0)+visitChild(node, 1);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTInNode node) {
		if (node.jjtGetChild(1) instanceof ASTDoubleCircumflex) {
			return " IN " + visitChildren(node);
		}
		return "(" + visitChild(node, 0) + " IN "
				+ visitChild(node, 1) + ")";	
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTNotInNode node) {
		if (node.jjtGetChild(1) instanceof ASTDoubleCircumflex) {
			return " NOT IN " + visitChildren(node);
		}
		return "(" + visitChild(node, 0) + " NOT IN "
				+ visitChild(node, 1) + ")";	
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTIriFuncNode node) {
		return "IRI("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTUriFuncNode node) {
		return "IRI("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTBnodeFuncNode node) {
		return "BNODE("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTABSFuncNode node) {
		return "ABS("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTCeilFuncNode node) {
		return "CEIL("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTFloorFuncNode node) {
		return "FLOOR("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTRoundFuncNode node) {
		return "ROUND("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTStrlenFuncNode node) {
		return "STRLEN("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTUcaseFuncNode node) {
		return "UCASE("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTLcaseFuncNode node) {
		return "LCASE("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTEncodeForUriFuncNode node) {
		return "ENCODE_FOR_URI("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTContainsFuncNode node) {
		return "CONTAINS("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTStrstartsFuncNode node) {
		return "STRSTARTS("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTStrEndsFuncNode node) {
		return "STRENDS("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTStrBeforeFuncNode node) {
		return "STRBEFORE("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTStrAfterFuncNode node) {
		return "STRAFTER("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTYearFuncNode node) {
		return "YEAR("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTMonthFuncNode node) {
		return "MONTH("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTDayFuncNode node) {
		return "DAY("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTHoursFuncNode node) {
		return "HOURS("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTMinutesFuncNode node) {
		return "MINUTES("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTSecondsFuncNode node) {
		return "SECONDS("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTTimeZoneFuncNode node) {
		return "TIMEZONE("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTTzFuncNode node) {
		return "TZ("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTNowFuncNode node) {
		return "NOW("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTUUIDFuncNode node) {
		return "UUID("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTSTRUUIDFuncNode node) {
		return "STRUUID("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTMD5FuncNode node) {
		return "MD5("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTSHA1FuncNode node) {
		return "SHA1("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTSHA256FuncNode node) {
		return "SHA256("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTSHA384FuncNode node) {
		return "SHA384("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTSHA512FuncNode node) {
		return "SHA512("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTIfFuncNode node) {
		return "IF("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTStrLangFuncNode node) {
		return "STRLANG("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTStrdtFuncNode node) {
		return "STRDT("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTisNumericFuncNode node) {
		return "ISNUMERIC("+visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTInvers node) {
		return "^("+visitChild(node,0)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTNegatedPath node) {
		return "!(" + visitChildrenSeparated(node, "|") + ")";
	}
	
	/** {@inheritDoc} */
	@Override
	public String visit(ASTBind node) {
		return tab+"BIND("+visitChild(node,0)+" AS "+visitChild(node, 1)+")\n";
	}
		
	/** {@inheritDoc} */
	@Override
	public String visit(ASTBindings node) {
		String result= tab+"VALUES ( ";
		int i=0;
		while(i<node.jjtGetNumChildren() && node.jjtGetChild(i) instanceof ASTVar){
			result+=(String) visit((ASTVar)node.jjtGetChild(i)) + " ";
			i++;
		}
		result+=")\n"+tab+" {";
		while(i<node.jjtGetNumChildren()){
			Node child=node.jjtGetChild(i);
			if(child instanceof ASTNIL){
				result+="()";
			} else {
				result+="(";
				for(int j=0; j<child.jjtGetNumChildren(); j++){
					result+=child.jjtGetChild(j).accept(this)+" ";
				}
				result+=")";
			}
			i++;
		}
		return result+"}\n";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTUndef node) {
		return "UNDEF";
	}
	
	/** {@inheritDoc} */
	@Override
	public String visit(ASTDefault node) {
		return "DEFAULT";
	}
	
	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTModify} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTModify node) {
		String result = "";
		for(int i=0; i<node.jjtGetNumChildren(); i++){
			Node child = node.jjtGetChild(i);
			if(child instanceof ASTGroupConstraint)
				result+="WHERE ";
			else if(child instanceof ASTQuotedURIRef || child instanceof ASTQName){
				result+="WITH ";
			}
			result += child.accept(this)+"\n"+tab;
		}
		
		return result;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTDelete} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTDelete node) {
		String vChildSep = visitChildrenSep(node);
		String ret = " DELETE "+(node.isDeleteData()?"DATA ":((node.jjtGetParent() instanceof ASTModify && node.jjtGetParent().jjtGetNumChildren()>1)?"":vChildSep+"\nWHERE "));
		return ret + vChildSep;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTInsert} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTInsert node) {		
		String ret = " INSERT ";
		if(!(node.jjtGetParent() instanceof ASTModify))
			ret+="DATA ";
		return ret + this.visitChildrenSep(node);
	}
	
	/** {@inheritDoc} */
	@Override
	public String visit(ASTAll node) {
		return "ALL";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTNamed node) {
		return "NAMED";
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTClear} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTClear node) {
		String silent = node.isSilent()?"SILENT ":"";
		if (node.jjtGetNumChildren() == 0)
			return " CLEAR " + silent;
		String graph=(node.jjtGetChild(0) instanceof ASTNamed || node.jjtGetChild(0) instanceof ASTAll || node.jjtGetChild(0) instanceof ASTDefault)?"":"GRAPH ";			
		return " CLEAR " + silent + graph + visitChild(node, 0);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTDrop} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTDrop node) {
		String silent = node.isSilent()?"SILENT ":"";
		if (node.jjtGetNumChildren() == 0)
			return " Drop " + silent;
		String graph=(node.jjtGetChild(0) instanceof ASTNamed || node.jjtGetChild(0) instanceof ASTAll || node.jjtGetChild(0) instanceof ASTDefault)?"":"GRAPH ";			
		return " Drop " + silent + graph + visitChild(node, 0);
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTCreate} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTCreate node) {
		String silent = node.isSilent()?"SILENT ":"";
		if (node.jjtGetNumChildren() == 0)
			return " Create " + silent;
		String graph=(node.jjtGetChild(0) instanceof ASTNamed || node.jjtGetChild(0) instanceof ASTAll || node.jjtGetChild(0) instanceof ASTDefault)?"":"GRAPH ";			
		return " Create " + silent + graph + visitChild(node, 0);
	}
	
	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTQuery} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTQuery node) {
		boolean first=true;
		final int numberChildren = node.jjtGetNumChildren();
		String value = "";
		for (int i = 0; i < numberChildren; i++) {
			Node child = node.jjtGetChild(i);
			if(child instanceof ASTLoad || child instanceof ASTClear ||
					child instanceof ASTDrop || child instanceof ASTAdd ||
					child instanceof ASTMove || child instanceof ASTCopy ||
					child instanceof ASTCreate || child instanceof ASTInsert ||
					child instanceof ASTDelete || child instanceof ASTModify){
				if(first)
					first=false;
				else value+=";\n";
			}			
			value += visitChild(node, i);
		}
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTExpressionList node) {
		return "("+this.visitChildrenCommaSeparated(node)+")";
	}
	
	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLoad} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String visit(final ASTLoad node) {
		String result = " LOAD " + (node.isSilent()?"SILENT ":"") + node.jjtGetChild(0).accept(this);
		if(node.jjtGetNumChildren()>1){
			String graph=(node.jjtGetChild(1) instanceof ASTNamed || node.jjtGetChild(1) instanceof ASTAll || node.jjtGetChild(1) instanceof ASTDefault)?"":"GRAPH ";
			result += " INTO " + graph + visitChild(node, 1); 
		}
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public String visit(ASTGroup node) {
		return "\nGROUP BY "+visitChildrenSep(node);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTHaving node) {
		return "\nHAVING "+visitChildrenSep(node)+"\n";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTAggregation node) {
		return node.getTYPE().toString()+"("+(node.isDistinct()?" DISTINCT":"")+((node.jjtGetNumChildren()>0)?visitChild(node,0)+((node.jjtGetNumChildren()>1)?";SEPARATOR="+visitChild(node, 1):""):"*")+")";
	}
	
	/** {@inheritDoc} */
	@Override
	public String visit(ASTExists node) {
		return "EXISTS "+visitChildrenSep(node);
	}
	
	/** {@inheritDoc} */
	@Override
	public String visit(ASTNotExists node) {
		return "NOT EXISTS "+visitChildrenSep(node);
	}
	
	/** {@inheritDoc} */
	@Override
	public String visit(final ASTGraphConstraint node) {
		if(node.jjtGetNumChildren()>1 && node.jjtGetChild(1) instanceof ASTGroupConstraint)
			return tab + "GRAPH " + visitChild(node, 0) + this.visitRemainingChildren(node, 1);
		else return tab + "GRAPH " + visitChild(node, 0) + "{"+this.visitRemainingChildren(node, 1)+"}";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTSubstringFuncNode node) {
		return "SUBSTR("+this.visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTStrReplaceFuncNode node) {
		return "REPLACE("+this.visitChildrenCommaSeparated(node)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTConcatFuncNode node) {
		return "CONCAT"+visitChildrenCommaSeparated(node);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTCoalesceFuncNode node) {
		return "COALESCE"+visitChildrenCommaSeparated(node);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTRandFuncNode node) {
		return "RAND("+visitChildrenCommaSeparated(node)+")";
	}
	
	/** {@inheritDoc} */
	@Override
	public String visit(ASTMinus node) {
		return "\n"+tab+"MINUS "+visitChildren(node);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTPathAlternative node) {
		return "("+visitChild(node, 0) + "|" + visitChild(node, 1)+")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTPathSequence node) {
		return "("+visitChild(node, 0) + "/" + visitChild(node, 1)+")";
	}
	
	/** {@inheritDoc} */
	@Override
	public String visit(ASTArbitraryOccurences node) {
		return "("+visitChild(node, 0)+")*";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTOptionalOccurence node) {
		return "("+visitChild(node, 0)+")?";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(ASTArbitraryOccurencesNotZero node) {
		return "("+visitChild(node, 0)+")+";
	}
}
