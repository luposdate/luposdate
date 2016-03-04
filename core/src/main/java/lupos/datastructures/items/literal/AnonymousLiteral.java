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
public class AnonymousLiteral extends Literal implements Externalizable {
	private static final long serialVersionUID = -2205699226774394906L;

	protected Literal content;

	/**
	 * <p>Constructor for AnonymousLiteral.</p>
	 */
	public AnonymousLiteral() {
	}

	/**
	 * <p>Constructor for AnonymousLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 */
	public AnonymousLiteral(final String content) {
		this.content = LiteralFactory.createLiteralWithoutLazyLiteral(content);
	}

	/**
	 * <p>Constructor for AnonymousLiteral.</p>
	 *
	 * @param code a int.
	 */
	protected AnonymousLiteral(final int code) {
		this.content = new CodeMapLiteral(code);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.content.toString();
	}

	/**
	 * <p>getBlankNodeLabel.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getBlankNodeLabel(){
		return this.toString().substring(2);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof AnonymousLiteral) {
			final AnonymousLiteral lit = (AnonymousLiteral) obj;
			return this.content.equals(lit.content);
		} else {
			return (this.toString().compareTo(obj.toString()) == 0);
		}
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (this.content != null ? this.content.hashCode() : 0);
		return result;
	}

	/**
	 * <p>Getter for the field <code>content</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public Literal getContent() {
		return this.content;
	}

	/** {@inheritDoc} */
	@Override
	public String[] getUsedStringRepresentations() {
		return this.content.getUsedStringRepresentations();
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

	/** {@inheritDoc} */
	@Override
	public Literal createThisLiteralNew() {
		return LiteralFactory.createAnonymousLiteral(this.content.originalString());
	}
}
