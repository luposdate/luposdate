
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
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteralOriginalContent;
import lupos.engine.operators.tripleoperator.TripleConsumer;
public class TurtleParser {
	/** Constant <code>readFileNumber=0</code> */
	public static int readFileNumber=0; 
	
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
		return TurtleParser.readTriplesFromInputWithFormat(in, tc, "Turtle", encoding, readFileNumber++);
	}
	
	/**
	 * <p>readTriplesFromInputWithFormat.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 * @param tc a {@link lupos.engine.operators.tripleoperator.TripleConsumer} object.
	 * @param format a {@link java.lang.String} object.
	 * @param encoding a {@link java.lang.String} object.
	 * @param id a int.
	 * @return a int.
	 * @throws java.io.UnsupportedEncodingException if any.
	 */
	public static int readTriplesFromInputWithFormat(final InputStream in,
			final TripleConsumer tc, final String format, final String encoding, int id)throws UnsupportedEncodingException {
		final Model model = ModelFactory.createDefaultModel();
		model.read(new InputStreamReader(in, encoding), "", format);
		return triplesFromModel(model, tc, id);
	}

	/**
	 * <p>triplesFromModel.</p>
	 *
	 * @param model a {@link com.hp.hpl.jena.rdf.model.Model} object.
	 * @param tc a {@link lupos.engine.operators.tripleoperator.TripleConsumer} object.
	 * @param id a int.
	 * @return a int.
	 */
	public static int triplesFromModel(final Model model,
			final TripleConsumer tc, int id) {
		final StmtIterator sit = model.listStatements();
		final HashMap<Resource, Integer> blanknodeNames = new HashMap<Resource, Integer>();
		int number=0;
		while (sit.hasNext()) {
			final Statement s = sit.next();

			Literal subject, predicate, object;

			subject = printResource2Literal(s.getSubject(), blanknodeNames, id);

			predicate = printResource2Literal(s.getPredicate(), blanknodeNames, id);

			if (s.getObject().isLiteral())
				object = printNTripleLiteral2Literal((com.hp.hpl.jena.rdf.model.Literal) s
						.getObject());
			else
				object = printResource2Literal((Resource) s.getObject(),
						blanknodeNames, id);

			final lupos.datastructures.items.Triple t = new lupos.datastructures.items.Triple(
					subject, predicate, object);

			tc.consume(t);
			number++;
		}
		return number;
	}

	/**
	 * <p>printResource2Literal.</p>
	 *
	 * @param r a {@link com.hp.hpl.jena.rdf.model.Resource} object.
	 * @param blanknodeNames a {@link java.util.HashMap} object.
	 * @param global_id a int.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal printResource2Literal(final Resource r, final HashMap<Resource, Integer> blanknodeNames, int global_id) {
		if (r.isAnon()) {
			Integer id = blanknodeNames.get(r);
			if (id == null) {
				id = blanknodeNames.size();
				blanknodeNames.put(r, id);
			}
			return LiteralFactory.createAnonymousLiteral("_:b"+global_id+"_" + id);
		} else
			try {
				return LiteralFactory.createURILiteral("<" + r.getURI() + ">");
			} catch (final Exception e) {
				return LiteralFactory.createLiteral("<" + r.getURI() + ">");
			}
	}

	/**
	 * <p>printNTripleLiteral2Literal.</p>
	 *
	 * @param l a {@link com.hp.hpl.jena.rdf.model.Literal} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal printNTripleLiteral2Literal(final com.hp.hpl.jena.rdf.model.Literal l) {
		String s1 = "\"";
		final char ar[] = l.getLexicalForm().toCharArray();

		for (int i = 0; i < ar.length; i++) {
			switch (ar[i]) {
			case '"':
				s1 += "\"";
				break;
			default:
				s1 += ar[i];
			}
		}
		s1 += "\"";
		if (l.getLanguage() != null && l.getLanguage().length() > 0) {// add
			// language
			// info
			// if it
			// exists
			return LiteralFactory.createLanguageTaggedLiteral(s1, l.getLanguage());
		}
		if (l.getDatatypeURI() != null) {// add datatype info if it exists
			final String s2 = l.getDatatypeURI();
			if (s2.length() > 0) {
				try {
					return LiteralFactory.createTypedLiteral(s1, "<" + s2 + ">");
					// }
				} catch (final Exception e) {
					return LiteralFactory.createLiteral(s1 + "^^" + s2);
				}
			}
		}
		return LiteralFactory.createLiteral(s1);
	}
	
}
