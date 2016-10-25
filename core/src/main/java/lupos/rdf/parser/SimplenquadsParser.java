package lupos.rdf.parser;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.tripleoperator.TripleConsumer;

public class SimplenquadsParser extends N3Parser {

	public static int parseRDFData(final InputStream in, final TripleConsumer tc,
			final String encoding) throws UnsupportedEncodingException {
		final SimplenquadsParser parser=new SimplenquadsParser();
		parser.parse(in, tc, encoding);
		return parser.getTripleNumber();
	}

	@Override
	public void parse(final InputStream in, final TripleConsumer tc,
			final String encoding) throws UnsupportedEncodingException {
		try {
			this.reader = new BufferedReader(new InputStreamReader(in, encoding));
			Triple t = this.nextTriple();
			while (t != null) {
				tc.consume(t);
				this.nextLiteral(); // throw away the remaining literal (the context) of nquads!
				t = this.nextTriple();
				if (t != null && (t.getSubject() == null || t.getPredicate() == null || t.getSubject() == null)) {
					log.debug("Triple:" + t);
				}
			}
			this.prefixe = null;
			log.error("Error! Not all triples have been parsed correctly!");
		} catch (final EOFException e) {
			this.prefixe = null;
		} finally {
			try {
				if (this.reader != null) {
					this.reader.close();
				}
			} catch (final IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
