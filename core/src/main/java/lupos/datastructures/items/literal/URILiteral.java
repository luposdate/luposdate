/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public abstract class URILiteral extends Literal implements Externalizable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2826285205704387677L;

	public static boolean isURI(final String content) {
		if (content.length() < 2
				|| content.substring(0, 1).compareTo("<") != 0
				|| content.substring(content.length() - 1, content.length())
				.compareTo(">") != 0)
			return false;
		try {
			URI.create(content.substring(1, content.length() - 1));
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "<" + getString() + ">";
	}
	
	@Override
	public String toString(lupos.rdf.Prefix prefixInstance) {
		return prefixInstance.add(this.toString());
	}

	public InputStream openStream() throws IOException {
		if (getString().startsWith("inlinedata:")) {
			return new ByteArrayInputStream(getString().substring(11)
					.getBytes());
		}

		if (getString().startsWith("file:")) {
			return new FileInputStream(getString().substring(5));
		}
		final URI u = URI.create(getString());
		if (u.getScheme().compareTo("file:") == 0) {
			return new FileInputStream(u.getSchemeSpecificPart()
					+ u.getFragment());
		} else
			return (u.toURL().openStream());
	}

	public abstract String getString();

	public abstract void update(String s) throws java.net.URISyntaxException;
	
	@Override
	public Literal createThisLiteralNew(){
		return LiteralFactory.createURILiteralWithoutException(this.originalString());
	}
}