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
package lupos.event.util;

import java.net.URISyntaxException;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;

/**
 * Helper class which contains constants for some common literals and methods to create literals.
 */
public abstract class Literals {
	
	/**
	 * Literals from the rdf-namespace.
	 */
	public static class RDF {
		public static final Literal TYPE = createURI(Prefixes.RDF, "type");
	}
	
	/**
	 * Literals from the xsd-namespace.
	 */
	public static class XSD {
		public static final URILiteral INT = createURI(Prefixes.XSD, "integer");
		public static final URILiteral LONG = createURI(Prefixes.XSD, "long");
		public static final URILiteral FLOAT = createURI(Prefixes.XSD, "float");
		public static final URILiteral DECIMAL = createURI(Prefixes.XSD, "decimal");
		public static final URILiteral String = createURI(Prefixes.XSD, "string");
	}
	
	/**
	 * one anonymousliteral
	 */
	public static class AnonymousLiteral{
		public static final Literal ANONYMOUS = LiteralFactory.createAnonymousLiteral("_:a");
	}
	
	/**
	 * Creates a URILiteral.
	 * @param uri
	 * @return
	 */
	public static URILiteral createURI(String uri) {
		return LiteralFactory.createURILiteralWithoutLazyLiteralWithoutException(uri);
	}
	
	/**
	 * Creates a URILiteral.
	 * @param namespace
	 * @param str
	 * @return
	 */
	public static URILiteral createURI(String namespace, String str) {
		String uri = Utils.createURIString(namespace, str);
		return LiteralFactory.createURILiteralWithoutLazyLiteralWithoutException(uri);
	}
	
	public static Literal createTyped(String value, URILiteral typeLiteral) throws URISyntaxException {
		return LiteralFactory.createTypedLiteral("\"" + value + "\"", typeLiteral);		
	}
}
