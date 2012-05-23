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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;

import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.codemap.CodeMapURILiteral;
import lupos.datastructures.items.literal.string.StringURILiteral;
import lupos.engine.operators.singleinput.ExpressionEvaluation.Helper;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

public class TypedLiteral extends Literal implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2634525515134231815L;
	protected Literal content;
	protected URILiteral type;

	public TypedLiteral() {
	}

	protected TypedLiteral(final String content, final String type)
			throws java.net.URISyntaxException {
		this(content, LiteralFactory.createURILiteralWithoutLazyLiteral(type));
	}

	protected TypedLiteral(final String content, final URILiteral type) {
		this.type = type;
		final String uniqueRepresentation = checkContent(content, this.type);
		this.content = LiteralFactory
				.createLiteralWithoutLazyLiteral(uniqueRepresentation);
	}

	protected TypedLiteral(final int codeContent, final URILiteral type) {
		this.type = type;
		this.content = new CodeMapLiteral(codeContent);
	}

	protected static String checkContent(final String content2,
			final URILiteral type) {
		return checkContent(content2, type.toString());
	}

	protected static String checkContent(final String originalContent,
			final String type) {
		try {
			String content2 = originalContent;
			// transform to an unique representation for numeric values...
			if (Helper.isNumeric(type)) {
				if (content2.compareTo("\"NaN\"") == 0
						|| content2.compareTo("\"Infinity\"") == 0)
					return content2;
				// remove leading + and leading 0
				while (content2.substring(1, 2).compareTo("0") == 0
						|| content2.substring(1, 2).compareTo("+") == 0) {
					content2 = "\"" + content2.substring(2, content2.length());
				}
				// delete too much?
				if (content2.length() == 2)
					content2 = "\"0\"";
			}
			if (Helper.isFloatingPoint(type)) {
				if (content2.compareTo("\"NaN\"") == 0
						|| content2.compareTo("\"Infinity\"") == 0
						|| content2.compareTo("\"-INF\"") == 0)
					return content2;
				BigDecimal bd = new BigDecimal(content2.substring(1,
						content2.length() - 1));
				bd = bd.stripTrailingZeros();
				String bd_string = bd.toString();
				if (bd_string.compareTo("") == 0)
					bd_string = "0";
				// is bd_string distinguishable to an integer?
				if (bd.divide(new BigDecimal(1)).subtract(bd)
						.compareTo(new BigDecimal(0)) != 0
						&& !(bd_string.contains("e") || bd_string.contains("E")))
					content2 = "\"" + bd_string + ".0\"";
				else
					content2 = "\"" + bd_string + "\"";
			} else if (type
					.compareTo("<http://www.w3.org/2001/XMLSchema#boolean>") == 0) {
				if (content2.compareTo("\"true\"") == 0
						|| content2.compareTo("\"1\"") == 0)
					content2 = "\"true\"";
				else
					content2 = "\"false\"";
			}
			return content2;
		} catch (final NumberFormatException e) {
			System.out.println("Number format exception:" + originalContent);
			System.out.println("Type:" + type);
			throw e;
		}
	}

	private String getTypeString() {
		return "^^" + type.toString();
	}

	private String getTypeString(lupos.rdf.Prefix prefixInstance) {
		return "^^" + type.toString(prefixInstance);
	}
	
	public String getType() {
		return type.toString();
	}

	public URILiteral getTypeLiteral() {
		return type;
	}

	public String getContent() {
		return content.toString();
	}

	public Literal getContentLiteral() {
		return content;
	}

	public String getOriginalContent() {
		return content.toString();
	}

	protected String commonToString(final String superToString) {
		if (type.toString().compareTo("<http://www.w3.org/2001/XMLSchema#string>") == 0){
			return superToString;
		} else {
			return superToString + getTypeString();
		}
	}

	protected String commonToOriginalString(final String superToString) {
		return superToString + getTypeString();
	}

	@Override
	public String originalString() {
		return commonToOriginalString(content.toString());
	}

	public String printYagoStringWithPrefix() {
		return content.printYagoStringWithPrefix() + "^^"
				+ type.printYagoStringWithPrefix();
	}

	@Override
	public String toString() {
		return commonToString(content.toString());
	}
	
	protected String commonToOriginalString(String superToString, lupos.rdf.Prefix prefixInstance) {
		if (type.toString().compareTo("<http://www.w3.org/2001/XMLSchema#string>") == 0){
			return superToString;
		} else {
			return superToString + getTypeString(prefixInstance);
		}
	}
	
	@Override
	public String toString(lupos.rdf.Prefix prefixInstance) {
		return commonToOriginalString(content.toString(), prefixInstance);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof TypedLiteral) {
			final TypedLiteral lit = (TypedLiteral) obj;
			// for numerical XML Schema datatypes do not consider the datatypes
			// for comparison, but for other datatypes do!
			if (Helper.isNumeric(this.getType())
					&& Helper.isNumeric(lit.getType()))
				return lit.getContent().equals(this.getContent());
			else
				return content.equals(lit.content) && type.equals(lit.type);
		} else
			return (this.toString().compareTo(obj.toString()) == 0);
	}

	@Override
	public String[] getUsedStringRepresentations() {
		final String[] typeRepr = type.getUsedStringRepresentations();
		return new String[] { content.toString(), typeRepr[0], typeRepr[1] };
	}

	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		if (LiteralFactory.getMapType() == MapType.NOCODEMAP
				|| LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)
			type = new StringURILiteral();
		else
			type = new CodeMapURILiteral();
		type.readExternal(in);
		content = LuposObjectInputStream.readLuposLiteral(in);
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		type.writeExternal(out);
		LuposObjectOutputStream.writeLuposLiteral(content, out);
	}
}