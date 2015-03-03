
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
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;

//import java.util.*;
public class StringURILiteral extends URILiteral implements Externalizable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7170680344509876823L;
	protected Literal content;

	/**
	 * <p>Constructor for StringURILiteral.</p>
	 */
	public StringURILiteral() {
	}

	/**
	 * <p>Constructor for StringURILiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @throws java$net$URISyntaxException if any.
	 */
	public StringURILiteral(final String content)
			throws java.net.URISyntaxException {
		this.update(content);
	}

	/** {@inheritDoc} */
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

	/**
	 * <p>Constructor for StringURILiteral.</p>
	 *
	 * @param code a int.
	 */
	public StringURILiteral(final int code) {
		this.content = new CodeMapLiteral(code);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof StringURILiteral) {
			final StringURILiteral lit = (StringURILiteral) obj;
			return this.content.equals(lit.content);
		} else if (obj instanceof URILiteral) {
			return super.equals(obj);
		} else if (obj instanceof LazyLiteral) {
			return (this.toString().compareTo(obj.toString()) == 0);
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getString() {
		return this.content.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String[] getUsedStringRepresentations() {
		try {
			return CodeMapURILiteral.getPreAndPostfix("<" + this.content.toString()
					+ ">");
		} catch (final URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		this.content = InputHelper.readLuposLiteral(in);
	}

	/** {@inheritDoc} */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		OutHelper.writeLuposLiteral(this.content, out);
	}
}
