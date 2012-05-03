package lupos.rdf.parser;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import lupos.engine.operators.tripleoperator.TripleConsumer;

public class RdfxmlParser {
	public static int parseRDFData(final InputStream in, final TripleConsumer tc,
			final String encoding) throws UnsupportedEncodingException {
		return TurtleParser.readTriplesFromInputWithFormat(in, tc, "RDF/XML", encoding, TurtleParser.readFileNumber++);
	}
}
