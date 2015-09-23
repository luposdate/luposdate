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
package lupos.datastructures.items.literal;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;

//import java.util.*;
public class LanguageTaggedLiteralOriginalLanguage extends
		LanguageTaggedLiteral implements Externalizable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2588014593133047329L;
	protected Literal originalLang;

	/**
	 * <p>Constructor for LanguageTaggedLiteralOriginalLanguage.</p>
	 */
	public LanguageTaggedLiteralOriginalLanguage() {
	}

	/**
	 * <p>Constructor for LanguageTaggedLiteralOriginalLanguage.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param language a {@link java.lang.String} object.
	 */
	protected LanguageTaggedLiteralOriginalLanguage(final String content,
			String language) {
		super(content, language);
		if (language.startsWith("@")) {
			language = language.substring(1);
		}
		final String languageUniqueRepresentation = (LiteralFactory.semanticInterpretationOfLiterals)?language.toUpperCase():language;
		this.originalLang = LiteralFactory.createLiteralWithoutLazyLiteral(language);
		this.lang = (languageUniqueRepresentation.compareTo(language) != 0) ? LiteralFactory
				.createLiteralWithoutLazyLiteral(languageUniqueRepresentation)
				: this.originalLang;
	}

	/**
	 * <p>Constructor for LanguageTaggedLiteralOriginalLanguage.</p>
	 *
	 * @param codeContent a int.
	 * @param codeLang a int.
	 */
	protected LanguageTaggedLiteralOriginalLanguage(final int codeContent,
			final int codeLang) {
		super(codeContent, codeLang);
		this.originalLang = this.lang;
		final String languageUniqueRepresentation = (LiteralFactory.semanticInterpretationOfLiterals)?this.originalLang.toString().toUpperCase():this.originalLang.toString();
		this.lang = (languageUniqueRepresentation.compareTo(this.originalLang.toString()) != 0) ?
				LiteralFactory.createLiteralWithoutLazyLiteral(languageUniqueRepresentation)
				: this.originalLang;
	}

	/**
	 * <p>originalLangDiffersFromUniqueRepresentation.</p>
	 *
	 * @param language a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean originalLangDiffersFromUniqueRepresentation(final String language) {
		if(LiteralFactory.semanticInterpretationOfLiterals==false){
			return false;
		}else {
			return (language.toUpperCase().compareTo(language) != 0);
		}
	}

	/**
	 * <p>originalLangDiffersFromUniqueRepresentation.</p>
	 *
	 * @param language a int.
	 * @return a boolean.
	 */
	public static boolean originalLangDiffersFromUniqueRepresentation(
			final int language) {
		final String originallanguage = CodeMapLiteral.getValue(language);
		return (originallanguage.toUpperCase().compareTo(originallanguage) != 0);
	}

	/**
	 * <p>Getter for the field <code>originalLang</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public Literal getOriginalLang() {
		return this.originalLang;
	}

	/**
	 * <p>createLanguageTaggedLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param lang a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.LanguageTaggedLiteral} object.
	 */
	public static LanguageTaggedLiteral createLanguageTaggedLiteral(
			final String content, final String lang) {
		if (LanguageTaggedLiteralOriginalLanguage
				.originalLangDiffersFromUniqueRepresentation(lang)) {
			return new LanguageTaggedLiteralOriginalLanguage(content, lang);
		} else {
			return new LanguageTaggedLiteral(content, lang);
		}
	}

	/**
	 * <p>createLanguageTaggedLiteral.</p>
	 *
	 * @param content a int.
	 * @param lang a int.
	 * @return a {@link lupos.datastructures.items.literal.LanguageTaggedLiteral} object.
	 */
	public static LanguageTaggedLiteral createLanguageTaggedLiteral(
			final int content, final int lang) {
		if (LanguageTaggedLiteralOriginalLanguage
				.originalLangDiffersFromUniqueRepresentation(lang)) {
			return new LanguageTaggedLiteralOriginalLanguage(content, lang);
		} else {
			return new LanguageTaggedLiteral(content, lang);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String originalString() {
		return this.content.toString() + "@" + this.originalLang.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String printYagoStringWithPrefix() {
		return this.content.printYagoStringWithPrefix() + "@"
				+ this.originalLang.printYagoStringWithPrefix();
	}

	/** {@inheritDoc} */
	@Override
	public String getOriginalLanguage() {
		return this.originalLang.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String[] getUsedStringRepresentations() {
		return new String[] { this.content.toString(), this.lang.toString(),
				this.originalLang.toString() };
	}

	/** {@inheritDoc} */
	@Override
	public boolean originalStringDiffers() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		this.originalLang = InputHelper.readLuposLiteral(in);
	}

	/** {@inheritDoc} */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		OutHelper.writeLuposLiteral(this.originalLang, out);
	}
}
