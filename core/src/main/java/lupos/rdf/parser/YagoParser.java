package lupos.rdf.parser;

import java.io.EOFException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import lupos.engine.operators.tripleoperator.TripleConsumer;

public class YagoParser extends Parser {
	
	public static int parseRDFData(final InputStream in, final TripleConsumer tc,
			final String encoding) throws UnsupportedEncodingException {
		YagoParser parser=new YagoParser();
		parser.parse(in, tc, encoding);
		return parser.getTripleNumber();
	}

	protected char handlePrefix() throws EOFException {
			final String s = "" + nextCharacter() + nextCharacter()
					+ nextCharacter() + nextCharacter() + nextCharacter()
					+ nextCharacter();
			if (s.toLowerCase().compareTo("prefix") == 0) {
				String name = "";
				char next = jumpOverBlanks();
				while (next != ':') {
					name += next;
					next = nextCharacter();
				}
				String prefix = "";
				next = jumpOverBlanks();
				if (next != '<') {
					while (next != ' ' && next != '\n') {
						prefix += next;
						next = nextCharacter();
					}
				} else {
					next = jumpOverBlanks();
					while (next != '>') {
						prefix += next;
						next = nextCharacter();
					}
					next = nextCharacter();
				}
				prefixe.put(name, prefix);
				if (next == ' ' || next == '\n') {
					next = jumpOverBlanks();
				}
				return next;
			} else {
				System.err.println("Prefix not recognized!");
				throw new EOFException();
			}
	}
}
