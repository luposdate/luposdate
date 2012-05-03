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
}