
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
package lupos.rdf.parser;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.engine.operators.tripleoperator.TripleConsumer;
public class NquadsParser {
	
	// Using the parser NxParser for NQUADS
	/**
	 * <p>parseRDFData.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 * @param tc a {@link lupos.engine.operators.tripleoperator.TripleConsumer} object.
	 * @param encoding a {@link java.lang.String} object.
	 * @return a int.
	 * @throws java.io.UnsupportedEncodingException if any.
	 */
	public static int parseRDFData(final InputStream in, final TripleConsumer tc,
			final String encoding) throws UnsupportedEncodingException {
		// ignore encoding as it is not supported by the NxParser!
		final NxParser nxp = new NxParser(in);
		int number = 0;
		while (nxp.hasNext()) {
			final Node[] ns = nxp.next();
			number++;
			if (number % 1000000 == 0){
				System.err.println("#triples:" + number);
			}
			try {
				tc.consume(new Triple(transformToLiteral(ns[0]),
						transformToLiteral(ns[1]),
						transformToLiteral(ns[2])));
			} catch (URISyntaxException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		return number;
	}

	/**
	 * <p>transformToLiteral.</p>
	 *
	 * @param node a {@link org.semanticweb.yars.nx.Node} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 * @throws java.net.URISyntaxException if any.
	 */
	public static Literal transformToLiteral(final Node node) throws URISyntaxException {
		if (node instanceof org.semanticweb.yars.nx.Resource) {
			return LiteralFactory.createURILiteral(node.toN3());
		} else if (node instanceof org.semanticweb.yars.nx.BNode) {
			return LiteralFactory.createAnonymousLiteral(node.toN3());
		} else if (node instanceof org.semanticweb.yars.nx.Literal) {
			final org.semanticweb.yars.nx.Literal literal = (org.semanticweb.yars.nx.Literal) node;
			if (literal.getDatatype() != null)
				return LiteralFactory.createTypedLiteral(
						"\"" + literal.getData() + "\"", literal.getDatatype()
						.toN3());
			if (literal.getLanguageTag() != null)
				return LiteralFactory.createLanguageTaggedLiteral("\""
						+ literal.getData() + "\"", literal.getLanguageTag());
			return LiteralFactory.createLiteral("\"" + literal.getData() + "\"");
		} else {
			return LiteralFactory.createLiteral(node.toN3());
		}
	}
}
