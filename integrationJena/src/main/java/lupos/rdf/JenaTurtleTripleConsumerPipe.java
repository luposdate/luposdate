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
package lupos.rdf;

import java.io.Reader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.engine.operators.tripleoperator.TripleConsumer;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.n3.turtle.TurtleEventHandler;
import com.hp.hpl.jena.n3.turtle.parser.ParseException;
import com.hp.hpl.jena.n3.turtle.parser.TurtleParser;

public class JenaTurtleTripleConsumerPipe extends TurtleParser {

	public JenaTurtleTripleConsumerPipe(final Reader arg0,
			final TripleConsumer tc) {
		super(arg0);
		this.setEventHandler(new TripleConsumerHandler(tc));
	}

	public static Literal transformToLiteral(
			final com.hp.hpl.jena.graph.Node node,
			final HashMap<String, Integer> blanknodeNames) {
		if (node.isConcrete()) {
			if (node.isBlank()) {
				Integer number = blanknodeNames.get(node.getBlankNodeLabel());
				if (number == null) {
					number = blanknodeNames.size();
					blanknodeNames.put(node.getBlankNodeLabel(), number);
				}
				return LiteralFactory.createAnonymousLiteral("_:b" + number);
			} else if (node.isURI()) {
				try {
					return LiteralFactory.createURILiteral("<" + node.getURI()
							+ ">");
				} catch (final URISyntaxException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			} else if (node.isLiteral()) {
				final String datatypeURI = node.getLiteralDatatypeURI();
				if (datatypeURI != null) {
					try {
						return LiteralFactory.createTypedLiteral(
								"\"" + node.getLiteralLexicalForm() + "\"", "<"
										+ datatypeURI
										+ ">");
					} catch (final URISyntaxException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				} else {
					final String lang = node.getLiteralLanguage();
					if (lang != null && lang.length() > 0) {
						return LiteralFactory.createLanguageTaggedLiteral(
"\""
								+ node.getLiteralLexicalForm() + "\"", lang);
					} else {
						return LiteralFactory.createLiteral("\""
								+ node.getLiteralLexicalForm() + "\"");
					}
				}
			} else
				System.err.println("This node should not occurr here:" + node);
		} else {
			System.err.println("Variables should not occurr here!");
		}
		System.err.println("Error in parsing data!");
		return null;
	}

	public static Map<String, String> retrievePrefixes(final Reader arg0)
			throws ParseException {
		final TurtleParser parser = new TurtleParser(arg0);
		final HashMap<String, String> map = new HashMap<String, String>();
		parser.setEventHandler(new BasicHandler() {

			@Override
			public void prefix(final int arg0, final int arg1,
					final String arg2, final String arg3) {
				map.put(arg2, arg3);
			}

		});
		parser.parse();
		return map;
	}

	public static class BasicHandler implements TurtleEventHandler {

		@Override
		public void endFormula(final int arg0, final int arg1) {
		}

		@Override
		public void prefix(final int arg0, final int arg1,
				final java.lang.String arg2, final java.lang.String arg3) {
		}

		@Override
		public void startFormula(final int arg0, final int arg1) {
		}

		@Override
		public void triple(final int arg0, final int arg1, final Triple arg2) {
		}
	}

	public static class TripleConsumerHandler extends BasicHandler {

		private final HashMap<String, Integer> blanknodeNames = new HashMap<String, Integer>();
		private final TripleConsumer tc;

		public TripleConsumerHandler(final TripleConsumer tc) {
			this.tc = tc;
		}

		@Override
		public void triple(final int arg0, final int arg1,
				final com.hp.hpl.jena.graph.Triple arg2) {

			tc.consume(new lupos.datastructures.items.Triple(
					transformToLiteral(arg2.getSubject(), blanknodeNames),
					transformToLiteral(arg2.getPredicate(), blanknodeNames),
					transformToLiteral(arg2.getObject(), blanknodeNames)));
		}
	}
}
