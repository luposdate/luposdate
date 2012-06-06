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
package lupos.datastructures.items.literal;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URISyntaxException;

import lupos.datastructures.items.literal.codemap.IntegerStringMap;
import lupos.datastructures.items.literal.codemap.StringIntegerMap;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;
import lupos.sparql1_1.ASTBlankNode;
import lupos.sparql1_1.ASTBooleanLiteral;
import lupos.sparql1_1.ASTDoubleCircumflex;
import lupos.sparql1_1.ASTFloatingPoint;
import lupos.sparql1_1.ASTInteger;
import lupos.sparql1_1.ASTLangTag;
import lupos.sparql1_1.ASTNIL;
import lupos.sparql1_1.ASTObjectList;
import lupos.sparql1_1.ASTQName;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.ASTRDFLiteral;
import lupos.sparql1_1.ASTStringLiteral;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;

/**
 * This class determines the type of it (like URILiteral, AnonymousLiteral,
 * TypedLiteral, ...) lazy, i.e., only up on request by a special method.
 * Internally, it uses a code for its string representation.

 */
public class LazyLiteral extends Literal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2768495922178003010L;
	private int code;
	private String materializedContent = null;
	private Literal materializedLiteral = null;

	public LazyLiteral() {
		// nothing to initialize
	}

	public LazyLiteral(final String content) {
		final Integer codeFromHashMap = hm.get(content);
		if (codeFromHashMap != null && codeFromHashMap != 0) {
			this.code = codeFromHashMap.intValue();
		} else {
			this.code = v.size() + 1;
			hm.put(content, new Integer(this.code));
			if (this.code == Integer.MAX_VALUE)
				System.err.println("Literal code overflow! Not good!");
			v.put(new Integer(this.code), content);
		}
	}

	public LazyLiteral(final int code) {
		this.code = code;
	}

	public LazyLiteral(final int code, final Literal materializedLiteral) {
		this.code = code;
		this.materializedLiteral = materializedLiteral;
		this.materializedContent = materializedLiteral.toString();
	}

	@Override
	public String[] getUsedStringRepresentations() {
		return new String[] { toString() };
	}

	public Literal getLiteral() {
		if (this.materializedLiteral == null) {
			this.materializedLiteral = getLiteral(this.originalString());
		}
		return this.materializedLiteral;
	}

	@Override
	public int compareToNotNecessarilySPARQLSpecificationConform(final Literal l) {
		if (l instanceof LazyLiteral) {
			return (this.code - ((LazyLiteral) l).code);
		} else {
			return this.getLiteral().compareTo(l);
		}
	}

	@Override
	public String toString() {
		if (this.materializedContent == null)
			this.materializedContent = v.get(this.code);
		return this.materializedContent;
	}

	@Override
	public int hashCode() {
		return this.code;
	}

	@Override
	public boolean equals(final Object l) {
		if (!(l instanceof Literal))
			return false;
		return compareToNotNecessarilySPARQLSpecificationConform((Literal) l) == 0;
	}

	@Override
	public boolean valueEquals(final Literal lit) {
		return compareToNotNecessarilySPARQLSpecificationConform(lit) == 0;
	}

	public int getCode() {
		return this.code;
	}

	public boolean isMaterialized() {
		return (this.materializedLiteral != null);
	}

	protected static StringIntegerMap hm = null;
	protected static IntegerStringMap v = null;

	public static int maxID() {
		return v.size();
	}

	public static StringIntegerMap getHm() {
		return hm;
	}

	public static void setHm(final StringIntegerMap hm) {
		LazyLiteral.hm = hm;
	}

	public static IntegerStringMap getV() {
		return v;
	}

	public static void setV(final IntegerStringMap v) {
		LazyLiteral.v = v;
	}

	public static Literal getLiteral(final String content) {
		try {
			final SimpleNode node = SPARQL1_1Parser.parseGraphTerm(content, null);
			return getLiteral(node);
		} catch (final Throwable t) {
			System.err.println("Trying to parse string "+content+" for transforming it to a literal...");
			System.err.println(t);
			t.printStackTrace();
			return null;
		}
	}

	public static Literal getLiteral(final Node n) {
		return getLiteral(n, false);
	}

	public static Literal getLiteral(final Node node, final boolean allowLazyLiteral) {
		Literal literal = null;
		Node n = node;
		
		if (n instanceof ASTNIL) {
			try {
				literal = (allowLazyLiteral) ? LiteralFactory
						.createURILiteral("<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>")
						: LiteralFactory
								.createURILiteralWithoutLazyLiteral("<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>");
			} catch (final URISyntaxException e1) {
				e1.printStackTrace();
			}
		} else if (n instanceof ASTBlankNode) {
			final ASTBlankNode blankNode = (ASTBlankNode) n;
			final String name = blankNode.getIdentifier();
			literal = (allowLazyLiteral) ? LiteralFactory
					.createAnonymousLiteral(name) : LiteralFactory
					.createAnonymousLiteralWithoutLazyLiteral(name);
		} else if (n instanceof ASTQuotedURIRef) {
			final ASTQuotedURIRef uri = (ASTQuotedURIRef) n;
			final String name = uri.getQRef();

			if (URILiteral.isURI("<" + name + ">")) {
				try {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createURILiteral("<" + name + ">")
							: LiteralFactory
									.createURILiteralWithoutLazyLiteral("<"
											+ name + ">");
				} catch (final Exception e) {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createLiteral("<" + name + ">") : LiteralFactory
							.createLiteralWithoutLazyLiteral("<" + name + ">");
				}
			} else
				literal = (allowLazyLiteral) ? LiteralFactory.createLiteral("<"
						+ name + ">") : LiteralFactory
						.createLiteralWithoutLazyLiteral("<" + name + ">");
		} else if (n instanceof ASTRDFLiteral)
			n = n.jjtGetChild(0);

		if (literal != null)
			return literal;

		if (n instanceof ASTStringLiteral) {
			final ASTStringLiteral lit = (ASTStringLiteral) n;
			final String quotedContent = lit.getStringLiteral();

			literal = (allowLazyLiteral) ? 
					LiteralFactory.createLiteral(quotedContent)
					: LiteralFactory.createLiteralWithoutLazyLiteral(quotedContent);
		} else if (n instanceof ASTInteger) {
			final ASTInteger lit = (ASTInteger) n;
			final String content = String.valueOf(lit.getValue());

			try {
				literal = (allowLazyLiteral) ? LiteralFactory
						.createTypedLiteral("\"" + content + "\"",
								"<http://www.w3.org/2001/XMLSchema#integer>")
						: TypedLiteralOriginalContent.createTypedLiteral("\""
								+ content + "\"",
								"<http://www.w3.org/2001/XMLSchema#integer>");
			} catch (final URISyntaxException e) {
				literal = (allowLazyLiteral) ? LiteralFactory
						.createLiteral(content) : LiteralFactory
						.createLiteralWithoutLazyLiteral(content);
			}
		} else if (n instanceof ASTFloatingPoint) {
			final ASTFloatingPoint lit = (ASTFloatingPoint) n;
			final String content = lit.getValue();

			try {
				if (content.contains("e") || content.contains("E"))
					literal = (allowLazyLiteral) ? LiteralFactory
							.createTypedLiteral("\"" + content + "\"",
									"<http://www.w3.org/2001/XMLSchema#double>")
							: TypedLiteralOriginalContent
									.createTypedLiteral("\"" + content + "\"",
											"<http://www.w3.org/2001/XMLSchema#double>");
				else
					literal = (allowLazyLiteral) ? LiteralFactory
							.createTypedLiteral("\"" + content + "\"",
									"<http://www.w3.org/2001/XMLSchema#decimal>")
							: TypedLiteralOriginalContent
									.createTypedLiteral("\"" + content + "\"",
											"<http://www.w3.org/2001/XMLSchema#decimal>");
			} catch (final URISyntaxException e) {
				literal = (allowLazyLiteral) ? LiteralFactory
						.createLiteral(content) : LiteralFactory
						.createLiteralWithoutLazyLiteral(content);
			}
		} else if (n instanceof ASTBooleanLiteral) {
			final String content = ((ASTBooleanLiteral) n).getState() + "";

			try {
				literal = (allowLazyLiteral) ? LiteralFactory
						.createTypedLiteral("\"" + content + "\"",
								"<http://www.w3.org/2001/XMLSchema#boolean>")
						: TypedLiteralOriginalContent.createTypedLiteral("\""
								+ content + "\"",
								"<http://www.w3.org/2001/XMLSchema#boolean>");
			} catch (final URISyntaxException e) {
				literal = (allowLazyLiteral) ? LiteralFactory
						.createLiteral(content) : LiteralFactory
						.createLiteralWithoutLazyLiteral(content);
			}
		} else if (n instanceof ASTDoubleCircumflex) {
			if (n.jjtGetNumChildren() != 2)
				System.err.println(n + " is expected to have 2 children!");
			else {
				final String content = getLiteral(n.jjtGetChild(0)).toString();
				final String type = getLiteral(n.jjtGetChild(1)).toString();

				try {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createTypedLiteral(content, type)
							: TypedLiteralOriginalContent.createTypedLiteral(
									content, type);
				} catch (final Exception e) {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createLiteral(content + "^^" + type)
							: LiteralFactory
									.createLiteralWithoutLazyLiteral(content
											+ "^^" + type);
				}
			}
		} else if (n instanceof ASTLangTag) {
			final String content = getLiteral(n.jjtGetChild(0)).toString();
			final String lang = ((ASTLangTag) n).getLangTag();
			literal = (allowLazyLiteral) ? LiteralFactory
					.createLanguageTaggedLiteral(content, lang)
					: LanguageTaggedLiteralOriginalLanguage
							.createLanguageTaggedLiteral(content, lang);
		} else if (n instanceof ASTQName) {
			final ASTQName uri = (ASTQName) n;
			final String namespace = uri.getNameSpace();
			final String localName = uri.getLocalName();

			final String name = namespace + localName;

			if (URILiteral.isURI("<" + name + ">")) {
				try {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createURILiteral("<" + name + ">")
							: LiteralFactory
									.createURILiteralWithoutLazyLiteral("<"
											+ name + ">");
				} catch (final Exception e) {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createLiteral("<" + name + ">") : LiteralFactory
							.createLiteralWithoutLazyLiteral("<" + name + ">");
				}
			} else
				literal = (allowLazyLiteral) ? LiteralFactory.createLiteral("<"
						+ name + ">") : LiteralFactory
						.createLiteralWithoutLazyLiteral("<" + name + ">");
		} else if(n instanceof ASTObjectList){
			literal = getLiteral(n.jjtGetChild(0));
		}else
			System.err.println("Unexpected type! "
					+ n.getClass().getSimpleName());

		return literal;
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		this.code = LuposObjectInputStream.readLuposInt(in);
		// code = in.readInt();
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		LuposObjectOutputStream.writeLuposInt(this.code, out);
	}
	
	@Override
	public boolean isBlank() {
		return this.getLiteral().isBlank();
	}

	@Override
	public boolean isURI() {
		return this.getLiteral().isURI();
	}
	
	@Override
	public boolean isTypedLiteral(){
		return this.getLiteral().isTypedLiteral();
	}
	
	@Override
	public boolean isLanguageTaggedLiteral(){
		return this.getLiteral().isLanguageTaggedLiteral();
	}
	
	@Override
	public boolean isSimpleLiteral(){
		return this.getLiteral().isSimpleLiteral();
	}

	@Override
	public boolean isXMLSchemaStringLiteral(){
		return this.getLiteral().isXMLSchemaStringLiteral();
	}
	
	@Override
	public Literal createThisLiteralNew() {
		return this.getLiteral().createThisLiteralNew();
	}
}
