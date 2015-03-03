
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
package lupos.datastructures.items.literal;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URISyntaxException;

import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
public class TypedLiteralOriginalContent extends TypedLiteral {

	/**
	 *
	 */
	private static final long serialVersionUID = -913934251866276647L;
	protected Literal originalContent;

	/**
	 * <p>Constructor for TypedLiteralOriginalContent.</p>
	 */
	public TypedLiteralOriginalContent() {
		// nothing to initialize for default constructor...
	}

	/**
	 * <p>Constructor for TypedLiteralOriginalContent.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param type a {@link java.lang.String} object.
	 * @throws java$net$URISyntaxException if any.
	 */
	protected TypedLiteralOriginalContent(final String content,
			final String type) throws java.net.URISyntaxException {
		this(content, LiteralFactory.createURILiteralWithoutLazyLiteral(type));
	}

	/**
	 * <p>Constructor for TypedLiteralOriginalContent.</p>
	 *
	 * @param content2 a {@link java.lang.String} object.
	 * @param type a {@link lupos.datastructures.items.literal.URILiteral} object.
	 */
	protected TypedLiteralOriginalContent(final String content2,
			final URILiteral type) {
		super(content2, type);
		this.originalContent = (this.content.toString().compareTo(content2) != 0) ?
				LiteralFactory.createLiteralWithoutLazyLiteral(content2)
				: this.content;
	}

	/**
	 * <p>Constructor for TypedLiteralOriginalContent.</p>
	 *
	 * @param codeContent a int.
	 * @param type a {@link lupos.datastructures.items.literal.URILiteral} object.
	 */
	protected TypedLiteralOriginalContent(final int codeContent,
			final URILiteral type) {
		super(codeContent, type);
		this.originalContent = this.content;
		final String uniqueRepresentation = checkContent(this.originalContent.toString(), this.type);
		this.content = (uniqueRepresentation.compareTo(this.originalContent.toString()) != 0) ?
				LiteralFactory.createLiteralWithoutLazyLiteral(uniqueRepresentation)
				: this.originalContent;
	}

	/**
	 * <p>createTypedLiteral.</p>
	 *
	 * @param content2 a {@link java.lang.String} object.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.TypedLiteral} object.
	 * @throws java$net$URISyntaxException if any.
	 */
	public static TypedLiteral createTypedLiteral(final String content2,
			final String type) throws URISyntaxException {
		if (checkContent(content2, type).compareTo(content2) != 0) {
			return new TypedLiteralOriginalContent(content2, type);
		} else {
			return new TypedLiteral(content2, type);
		}
	}

	/**
	 * <p>createTypedLiteral.</p>
	 *
	 * @param content2 a {@link java.lang.String} object.
	 * @param type a {@link lupos.datastructures.items.literal.URILiteral} object.
	 * @return a {@link lupos.datastructures.items.literal.TypedLiteral} object.
	 */
	public static TypedLiteral createTypedLiteral(final String content2,
			final URILiteral type) {
		if (checkContent(content2, type).compareTo(content2) != 0) {
			return new TypedLiteralOriginalContent(content2, type);
		} else {
			return new TypedLiteral(content2, type);
		}
	}

	/**
	 * <p>createTypedLiteral.</p>
	 *
	 * @param codeContent a int.
	 * @param type a {@link lupos.datastructures.items.literal.URILiteral} object.
	 * @return a {@link lupos.datastructures.items.literal.TypedLiteral} object.
	 */
	public static TypedLiteral createTypedLiteral(final int codeContent,
			final URILiteral type) {
		final String content2 = CodeMapLiteral.getValue(codeContent);
		if (checkContent(content2, type).compareTo(content2) != 0) {
			return new TypedLiteralOriginalContent(content2, type);
		} else {
			return new TypedLiteral(content2, type);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getOriginalContent() {
		return this.originalContent.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String originalString() {
		return this.commonToOriginalString(this.originalContent.toString());
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final lupos.rdf.Prefix prefixInstance) {
		return this.commonToOriginalString(this.originalContent.toString(), prefixInstance);
	}

	/** {@inheritDoc} */
	@Override
	public String[] getUsedStringRepresentations() {
		final String[] typeRepr = this.type.getUsedStringRepresentations();
		return new String[] { this.content.toString(), this.originalContent.toString(),
				typeRepr[0], typeRepr[1] };
	}

	/** {@inheritDoc} */
	@Override
	public String printYagoStringWithPrefix() {
		return this.originalContent.printYagoStringWithPrefix() + "^^" + this.type.printYagoStringWithPrefix();
	}

	/** {@inheritDoc} */
	@Override
	public boolean originalStringDiffers() {
		return true;
	}

	/**
	 * <p>getOriginalContentLiteral.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public Literal getOriginalContentLiteral() {
		return this.originalContent;
	}

	/** {@inheritDoc} */
	@Override
	public void readExternal(final ObjectInput in) throws IOException,
	ClassNotFoundException {
		super.readExternal(in);
		this.originalContent = InputHelper.readLuposLiteral(in);
	}

	/** {@inheritDoc} */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		OutHelper.writeLuposLiteral(this.originalContent, out);
	}
}
