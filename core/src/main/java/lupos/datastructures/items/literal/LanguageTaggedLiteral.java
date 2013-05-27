/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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

public class LanguageTaggedLiteral extends Literal implements Externalizable {

	/**
	 *
	 */
	private static final long serialVersionUID = -6816588722364640397L;
	protected Literal content;
	protected Literal lang;

	public LanguageTaggedLiteral() {
	}

	protected LanguageTaggedLiteral(final String content, String language) {
		this.content = LiteralFactory.createLiteralWithoutLazyLiteral(content);
		if (language.startsWith("@")) {
			language = language.substring(1);
		}
		final String languageUniqueRepresentation = language.toUpperCase();
		this.lang = LiteralFactory
				.createLiteralWithoutLazyLiteral(languageUniqueRepresentation);
	}

	protected LanguageTaggedLiteral(final int codeContent, final int codeLang) {
		this.content = new CodeMapLiteral(codeContent);
		this.lang = new CodeMapLiteral(codeLang);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof LanguageTaggedLiteral) {
			final LanguageTaggedLiteral lit = (LanguageTaggedLiteral) obj;
			return this.content.equals(lit.content) && this.lang.equals(lit.lang);
		} else {
			return (this.toString().compareTo(obj.toString()) == 0);
		}
	}

	@Override
	public String toString() {
		return this.content.toString() + "@" + this.lang.toString();
	}

	@Override
	public String printYagoStringWithPrefix() {
		return this.content.printYagoStringWithPrefix() + "@"
				+ this.lang.printYagoStringWithPrefix();
	}

	public String getLanguage() {
		return this.lang.toString();
	}

	public String getOriginalLanguage() {
		return this.lang.toString();
	}

	public String getContent() {
		return this.content.toString();
	}

	@Override
	public String[] getUsedStringRepresentations() {
		return new String[] { this.content.toString(), this.lang.toString() };
	}

	public Literal getLang() {
		return this.lang;
	}

	public Literal getContentLiteral() {
		return this.content;
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		this.content = InputHelper.readLuposLiteral(in);
		this.lang = InputHelper.readLuposLiteral(in);
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		OutHelper.writeLuposLiteral(this.content, out);
		OutHelper.writeLuposLiteral(this.lang, out);
	}

	@Override
	public Literal createThisLiteralNew() {
		return LiteralFactory.createLanguageTaggedLiteral(this.content.originalString(), this.lang.originalString());
	}
}