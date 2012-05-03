package lupos.rdf.parser;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.rdf.parser.SesameturtleParser.SesameParserType;

public class SesamerdfxmlParser {
	public static int parseRDFData(final InputStream in, final TripleConsumer tc,
			final String encoding) throws UnsupportedEncodingException {
		return SesameturtleParser.readTriplesUsingSesameParser(in, tc, SesameParserType.RDFXML, encoding);
	}
}
