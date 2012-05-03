package lupos.datastructures.items.literal.string;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URISyntaxException;

import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.codemap.CodeMapURILiteral;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

//import java.util.*;

public class StringURILiteral extends URILiteral implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7170680344509876823L;
	protected Literal content;

	public StringURILiteral() {
	}

	public StringURILiteral(final String content)
			throws java.net.URISyntaxException {
		update(content);
	}

	@Override
	public void update(final String content) throws java.net.URISyntaxException {
		if (content == null
				|| !(content.startsWith("<") && content.endsWith(">"))) {
			System.out.println("Error: Expected URI, but " + content
					+ " is not an URI!");
			throw new java.net.URISyntaxException("Error: Expected URI, but "
					+ content + " is not an URI!", content);
		}
		this.content = LiteralFactory.createStringLiteral(content
				.substring(1, content.length() - 1));
	}

	public StringURILiteral(final int code) {
		this.content = new CodeMapLiteral(code);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof StringURILiteral) {
			final StringURILiteral lit = (StringURILiteral) obj;
			return this.content.equals(lit.content);
		} else if (obj instanceof URILiteral) {
			return super.equals(obj);
		} else if (obj instanceof LazyLiteral) {
			return (this.toString().compareTo(obj.toString()) == 0);
		} else
			return false;
	}

	@Override
	public String getString() {
		return content.toString();
	}

	@Override
	public String[] getUsedStringRepresentations() {
		try {
			return CodeMapURILiteral.getPreAndPostfix("<" + content.toString()
					+ ">");
		} catch (final URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		content = LuposObjectInputStream.readLuposLiteral(in);
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		LuposObjectOutputStream.writeLuposLiteral(content, out);
	}
}