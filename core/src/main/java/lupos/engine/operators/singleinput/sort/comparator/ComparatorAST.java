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
package lupos.engine.operators.singleinput.sort.comparator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.TypedLiteralOriginalContent;
import lupos.engine.operators.singleinput.NotBoundException;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.engine.operators.singleinput.filter.Filter;
import lupos.engine.operators.singleinput.filter.expressionevaluation.Helper;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.ASTAs;
import lupos.sparql1_1.SimpleNode;

public class ComparatorAST implements ComparatorBindings {

	/**
	 * Compares two given bindings as specified in SPARQL-specification
	 */

	protected lupos.sparql1_1.Node node;

	public ComparatorAST(final lupos.sparql1_1.Node node) {
		this.node = node;
	}

	/**
	 * This method is used in order to check whether or not a previous join can
	 * replace this sort operator by using MergeJoinSort (for optional
	 * analogous).
	 * 
	 * @return the sort criterium as collection of variables or null if the sort
	 *         criterium is not so simple!
	 */
	public Collection<Variable> getSortCriterium() {
		final LinkedList<Variable> sortCriterium = new LinkedList<Variable>();
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			final lupos.sparql1_1.Node n = node.jjtGetChild(i);
			final int mod = 1;
			if (n instanceof lupos.sparql1_1.ASTDescOrder) {
				return null;
			}
			if (n instanceof lupos.sparql1_1.ASTVar) {
				sortCriterium.add(new Variable(((lupos.sparql1_1.ASTVar) n)
						.getName()));
			} else
				return null;
		}
		return sortCriterium;
	}

	/**
	 * Compares two bindings considering SPARQL-specifications
	 * 
	 * @param arg0
	 *            first Bindings to compare
	 * @param arg1
	 *            second Bindings to compare
	 * @return simlar to any other integer based compare method: <br>
	 *         -1 if l0 < l1<br>
	 *         1 if l0 > l1<br>
	 *         0 if l0 = l1<br>
	 *         but modified, as result will be multiplicated by -1 if descending
	 *         order has been chosen.
	 */
	public int compare(final Bindings arg0, final Bindings arg1) {

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			lupos.sparql1_1.Node n = node.jjtGetChild(i);
			if (n instanceof ASTAs) {
				n=n.jjtGetChild(0);
			}
			int mod = 1;
			if (n instanceof lupos.sparql1_1.ASTDescOrder) {
				mod = -1;
				i++;
				n = node.jjtGetChild(i);
			}
			if (n instanceof lupos.sparql1_1.ASTAscOrder) {
				i++;
				n = node.jjtGetChild(i);
			}
			Object o0;
			try {
				o0 = Filter.staticEvalTree(arg0, n, null);
			} catch (final NotBoundException e) {
				o0 = null;
			} catch (final TypeErrorException e) {
				o0 = null;
			}
			Object o1;
			try {
				o1 = Filter.staticEvalTree(arg1, n, null);
			} catch (final NotBoundException e) {
				o1 = null;
			} catch (final TypeErrorException e) {
				o1 = null;
			}
			final Literal l0 = getLiteral(o0);
			final Literal l1 = getLiteral(o1);
			final int ret = intComp(l0, l1);
			if (ret != 0)
				return ret * mod;
		}
		return 0;
	}

	public static Literal getLiteral(final Object o) {
		if (o instanceof Literal)
			return (Literal) o;
		if (o instanceof String)
			try {
				return TypedLiteralOriginalContent.createTypedLiteral(o
						.toString(),
						"<http://www.w3.org/2001/XMLSchema#string>");
			} catch (final URISyntaxException e) {
				return null;
			}
		else if (o instanceof BigInteger)
			try {
				return TypedLiteralOriginalContent.createTypedLiteral("\""
						+ o.toString() + "\"",
						"<http://www.w3.org/2001/XMLSchema#integer>");
			} catch (final URISyntaxException e) {
				return null;
			}
		else if (o instanceof BigDecimal)
			try {
				return TypedLiteralOriginalContent.createTypedLiteral("\""
						+ o.toString() + "\"",
						"<http://www.w3.org/2001/XMLSchema#decimal>");
			} catch (final URISyntaxException e) {
				return null;
			}
		else if (o instanceof Boolean)
			try {
				return TypedLiteralOriginalContent.createTypedLiteral("\""
						+ o.toString() + "\"",
						"<http://www.w3.org/2001/XMLSchema#boolean>");
			} catch (final URISyntaxException e) {
				return null;
			}
		return null;
	}

	/**
	 * Does the dirty work for compare
	 * 
	 * @param l0
	 *            first binding to compare
	 * @param l1
	 *            second binding to compare
	 * @return simlar to any other integer based compare method: <br>
	 *         -1 if l0 < l1<br>
	 *         1 if l0 > l1<br>
	 *         0 if l0 = l1
	 */
	public static int intComp(Literal l0, Literal l1) {
		if (l0 instanceof LazyLiteral) {
			if(l1 instanceof LazyLiteral){
				// if the two lazy literals are equal, then we can avoid to materialize them!
				if(((LazyLiteral)l0).getCode() == ((LazyLiteral)l1).getCode())
					return 0;
			}
			l0 = ((LazyLiteral) l0).getLiteral();
		}
		if (l1 instanceof LazyLiteral)
			l1 = ((LazyLiteral) l1).getLiteral();
		// Order concerning only prim-variable
		// Unbound second - But needs to be checked first
		if (l0 == null || l1 == null) {
			if (l0 == l1) {
				return 0;
			}
			if (l0 == null) {
				if (l1.isBlank()) {
					return 1;
				}
				return -1;
			} else {
				if (l0.isBlank()) {
					return -1;
				}
				return 1;
			}
		}

		// Blanks first!!!
		if (l0.isBlank() || l1.isBlank()) {
			if (!l0.isBlank()) {
				return 1;
			}
			if (!l1.isBlank()) {
				return -1;
			}

			return l0.toString().compareTo(l1.toString());
		}
		// IRIs third
		if ((l0.isURI()) || (l1.isURI())) {
			if (!(l0.isURI())) {
				return 1;
			}
			if (!(l1.isURI())) {
				return -1;
			}
			return l0.toString().compareTo(l1.toString());
		}
		
		// this is undefined in the SPARQL specification, but we define the following order between RDF literals of different types:
		// 1. Simple literals 2. language tagged literals 3. typed literals
		// first simple literals:
		if(l0.isSimpleLiteral() || l1.isSimpleLiteral()){
			if (!(l0.isSimpleLiteral())) {
				return 1;
			}
			if (!(l1.isSimpleLiteral())) {
				return -1;
			}
			return l0.toString().compareTo(l1.toString());
		}
		
		// second language tagged literals
		if(l0.isLanguageTaggedLiteral() || l1.isLanguageTaggedLiteral()){
			if (!(l0.isLanguageTaggedLiteral())) {
				return 1;
			}
			if (!(l1.isLanguageTaggedLiteral())) {
				return -1;
			}
			return l0.toString().compareTo(l1.toString());
		}
		
		// third typed literals:
		// Numeric comparison?
		if (l0 instanceof TypedLiteral && l1 instanceof TypedLiteral) {
			final TypedLiteral tl0 = (TypedLiteral) l0;
			final TypedLiteral tl1 = (TypedLiteral) l1;
			if (Helper.isNumeric(tl0.getType())
					&& Helper.isNumeric(tl1.getType())) {
				if (Helper.isInteger(tl0.getType())
						&& Helper.isInteger(tl1.getType())) {
						BigInteger a;
						try {
							a = Helper.getInteger(tl0);
						} catch (final NumberFormatException e) {
							a = null;
						} catch (final TypeErrorException e) {
							a = null;
						}
						BigInteger b;
						try {
							b = Helper.getInteger(tl1);
						} catch (final NumberFormatException e) {
							b = null;
						} catch (final TypeErrorException e) {
							b = null;
						}
						if (a == null) {
							if (b == null){
								return l0.toString().compareTo(l1.toString());
							} else {
								return 1;
							}
						} else if (b == null) {
							return -1;
						} else {
							final int comp = a.compareTo(b);
							return comp;
						}
				} else { // decimal comparison...
						BigDecimal a;
						try {
							a = Helper.getBigDecimal(tl0);
						} catch (final NumberFormatException e) {
							a = null;
						} catch (final TypeErrorException e) {
							a = null;
						}
						BigDecimal b;
						try {
							b = Helper.getBigDecimal(tl1);
						} catch (final NumberFormatException e) {
							b = null;
						} catch (final TypeErrorException e) {
							b = null;
						}
						if (a == null) {
							if (b == null){
								return l0.toString().compareTo(l1.toString());
							} else {
								return 1;
							}
						} else if (b == null){
							return -1;
						} else {
							final int comp = a.compareTo(b);
							return comp;
						}
				}
			}
		}
		// else simple String-compare
		return l0.toString().compareTo(l1.toString());
	}

	/**
	 * @return returns a String-representation of this comperator
	 */
	@Override
	public String toString() {
		final SPARQLParserVisitorImplementationDumper filterDumper = new SPARQLParserVisitorImplementationDumper();

		return (String) filterDumper.visit((SimpleNode) node);
	}

}
