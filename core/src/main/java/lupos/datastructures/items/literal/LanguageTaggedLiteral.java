/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

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
		if (language.startsWith("@"))
			language = language.substring(1);
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
			return content.equals(lit.content) && lang.equals(lit.lang);
		} else
			return (this.toString().compareTo(obj.toString()) == 0);
	}

	@Override
	public String toString() {
		return content.toString() + "@" + lang.toString();
	}

	public String printYagoStringWithPrefix() {
		return content.printYagoStringWithPrefix() + "@"
				+ lang.printYagoStringWithPrefix();
	}

	public String getLanguage() {
		return lang.toString();
	}

	public String getOriginalLanguage() {
		return lang.toString();
	}

	public String getContent() {
		return content.toString();
	}

	@Override
	public String[] getUsedStringRepresentations() {
		return new String[] { content.toString(), lang.toString() };
	}

	public Literal getLang() {
		return lang;
	}

	public Literal getContentLiteral() {
		return content;
	}

	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		content = LuposObjectInputStream.readLuposLiteral(in);
		lang = LuposObjectInputStream.readLuposLiteral(in);
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		LuposObjectOutputStream.writeLuposLiteral(content, out);
		LuposObjectOutputStream.writeLuposLiteral(lang, out);
	}

	@Override
	public Literal createThisLiteralNew() {
		return LiteralFactory.createLanguageTaggedLiteral(this.content.originalString(), this.lang.originalString());
	}
}