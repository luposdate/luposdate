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
import lupos.datastructures.items.literal.LiteralFactory;
public class PlainStringLiteral extends Literal implements Item,
		Comparable<Literal>, Externalizable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7110464399527264155L;
	protected String content;

	/**
	 * <p>Constructor for PlainStringLiteral.</p>
	 */
	public PlainStringLiteral() {
	}

	/**
	 * <p>Constructor for PlainStringLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 */
	public PlainStringLiteral(final String content) {
		this.content = content;
	}

	/**
	 * <p>valueEquals.</p>
	 *
	 * @param lit a {@link lupos.datastructures.items.literal.string.PlainStringLiteral} object.
	 * @return a boolean.
	 */
	public boolean valueEquals(final PlainStringLiteral lit) {
		return (toString().compareTo(lit.toString()) == 0);
	}

	/**
	 * <p>compareTo.</p>
	 *
	 * @param other a {@link lupos.datastructures.items.literal.string.PlainStringLiteral} object.
	 * @return a int.
	 */
	public int compareTo(final PlainStringLiteral other) {
		return toString().compareTo(other.toString());
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final Literal other) {
		if (other instanceof PlainStringLiteral)
			return toString().compareTo(other.toString());
		else
			return super.compareTo(other);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return content;
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode(){
		return content.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return toString();
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public Literal getLiteral(final Bindings b) {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public String[] getUsedStringRepresentations() {
		return new String[] { content };
	}

	/** {@inheritDoc} */
	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		content = (String) in.readObject();
	}

	/** {@inheritDoc} */
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeObject(content);
	}

	/**
	 * <p>getLiteral.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public Literal getLiteral() {
		return LazyLiteral.getLiteral(content);
	}

	/** {@inheritDoc} */
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

	/**
	 * <p>transformToPlainStringLiteralTriple.</p>
	 *
	 * @param triple a {@link lupos.datastructures.items.Triple} object.
	 * @return a {@link lupos.datastructures.items.Triple} object.
	 */
	public static Triple transformToPlainStringLiteralTriple(final Triple triple) {
		final PlainStringLiteral subject = new PlainStringLiteral(triple
				.getSubject().originalString());
		final PlainStringLiteral predicate = new PlainStringLiteral(triple
				.getPredicate().originalString());
		final PlainStringLiteral object = new PlainStringLiteral(triple
				.getObject().originalString());
		return new Triple(subject, predicate, object);
	}

	/** {@inheritDoc} */
	@Override
	public Literal createThisLiteralNew() {
		return LiteralFactory.createLiteral(this.content);
	}
}
