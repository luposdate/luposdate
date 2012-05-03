package lupos.datastructures.items.literal.string;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;

public class PlainStringLiteral extends Literal implements Item,
		Comparable<Literal>, Externalizable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7110464399527264155L;
	protected String content;

	public PlainStringLiteral() {
	}

	public PlainStringLiteral(final String content) {
		this.content = content;
	}

	public boolean valueEquals(final PlainStringLiteral lit) {
		return (toString().compareTo(lit.toString()) == 0);
	}

	public int compareTo(final PlainStringLiteral other) {
		return toString().compareTo(other.toString());
	}

	@Override
	public int compareTo(final Literal other) {
		if (other instanceof PlainStringLiteral)
			return toString().compareTo(other.toString());
		else
			return super.compareTo(other);
	}

	@Override
	public String toString() {
		return content;
	}
	
	@Override
	public int hashCode(){
		return content.hashCode();
	}

	@Override
	public String getName() {
		return toString();
	}

	@Override
	public boolean isVariable() {
		return false;
	}

	private void writeObject(final java.io.ObjectOutputStream out)
			throws IOException {
		out.writeObject(content);
	}

	private void readObject(final java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		content = (String) in.readObject();
	}

	@Override
	public Literal getLiteral(final Bindings b) {
		return this;
	}

	@Override
	public String[] getUsedStringRepresentations() {
		return new String[] { content };
	}

	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		content = (String) in.readObject();
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeObject(content);
	}

	public Literal getLiteral() {
		return LazyLiteral.getLiteral(content);
	}

	@Override
	public String printYagoStringWithPrefix() {
		final String s = toString();
		if (s.startsWith("\"") && s.endsWith("\"")) {
			return "\""
					+ s.substring(1, s.length() - 1).replaceAll(
							Pattern.quote("\""),
							Matcher.quoteReplacement("&quot;")) + "\"";
		} else
			return s;
	}

	public static Triple transformToPlainStringLiteralTriple(final Triple triple) {
		final PlainStringLiteral subject = new PlainStringLiteral(triple
				.getSubject().originalString());
		final PlainStringLiteral predicate = new PlainStringLiteral(triple
				.getPredicate().originalString());
		final PlainStringLiteral object = new PlainStringLiteral(triple
				.getObject().originalString());
		return new Triple(subject, predicate, object);
	}
}