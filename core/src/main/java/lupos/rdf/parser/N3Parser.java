package lupos.rdf.parser;

import java.io.EOFException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import lupos.engine.operators.tripleoperator.TripleConsumer;

public class N3Parser extends Parser {

	public static int parseRDFData(final InputStream in, final TripleConsumer tc,
			final String encoding) throws UnsupportedEncodingException {
		N3Parser parser=new N3Parser();
		parser.parse(in, tc, encoding);
		return parser.getTripleNumber();
	}
	
	protected char handlePrefix() throws EOFException {
			String s = "" + nextCharacter() + nextCharacter()
					+ nextCharacter() + nextCharacter(); 
			
			if (s.toLowerCase().compareTo("base") == 0) {
				String prefix = "";
				char next = jumpOverBlanks();
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
				prefixe.put("", prefix);
				if (next == ' ' || next == '\n') {
					next=jumpOverBlanks();
				}
				if(next!='.'){
					System.err.println("Dot (.) expected in the end of a base declaration!");
					throw new EOFException();
				}
				next=jumpOverBlanks();
				return next;				
			}
			
			s+=""+nextCharacter()+ nextCharacter();
			
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
					next=jumpOverBlanks();
				}
				if(next!='.'){
					System.err.println("Dot (.) expected in the end of a prefix declaration!");
					throw new EOFException();
				}
				next=jumpOverBlanks();
				return next;
			} else {
				System.err.println("Prefix or base not recognized!");
				throw new EOFException();
			}
	}
}
