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
package lupos.rdf.parser;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.operators.tripleoperator.TripleConsumer;

public abstract class Parser {

	private int counter = 0;

	private static int maxTriples = 0;

	private BufferedReader reader = null;

	public int getTripleNumber() {
		return counter - 1;
	}

	public void parse(final InputStream in, final TripleConsumer tc,
			final String encoding) throws UnsupportedEncodingException {
		try {
			reader = new BufferedReader(new InputStreamReader(in, encoding));
			Triple t = nextTriple();
			// System.out.println(t);
			// Triple last=t;
			// String lastLine=line;
			// int lastPos=pos;
			while (t != null) {
				// last=t;
				// lastLine=line;
				// lastPos=pos;
				// System.out.println(t);
				tc.consume(t);
				t = nextTriple();
				// if (YagoParser.maxTriples > 0
				// && counter > YagoParser.maxTriples - 1)
				// System.out.println("Last triple:" + t);
				if (t != null
						&& (t.getSubject() == null || t.getPredicate() == null || t
								.getSubject() == null)) {
					System.out.println("Triple:" + t);
					System.out.println("Line:" + line + "###############pos:"
							+ pos);
				}

				// System.out.println(t);
			}
			prefixe = null;
			System.out
					.println("Line:" + line + "###############pos:" + pos/*
																		 * +"\nlast triple:"
																		 * +
																		 * last+
																		 * "\nlast line:"
																		 * +
																		 * lastLine
																		 * +
																		 * "############pos:"
																		 * +
																		 * lastPos
																		 */);
		} catch (final EOFException e) {
			prefixe = null;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (final IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private Literal subject = null;
	private Literal predicate = null;

	private Triple nextTriple() throws EOFException {
		counter++;
		if (counter % 1000000 == 0)
			System.err.println("#Triples:" + counter); // in order to display,
		// but not be logged
		if (Parser.maxTriples > 0 && counter > Parser.maxTriples)
			return null;
		Literal nextLiteral;
		nextLiteral = nextLiteral();
		if (nextLiteral == null)
			return null;
		if (predicate != null) {
			return new Triple(subject, predicate, nextLiteral);
		}
		Literal nextLiteral2;
		nextLiteral2 = nextLiteral();
		if (subject != null) {
			predicate = nextLiteral;
			return new Triple(subject, nextLiteral, nextLiteral2);
		}
		subject = nextLiteral;
		predicate = nextLiteral2;
		Literal object;
		object = nextLiteral();
		return new Triple(nextLiteral, nextLiteral2, object);
	}

	private String line = null;
	private int pos = -1;
	private int linenumber = 0;

	protected char nextCharacter() throws EOFException {
		if (backFlag) {
			backFlag = false;
			return back;
		}
		if (line == null)
			try {
				line = reader.readLine();
				linenumber++;
			} catch (final IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		pos++;
		if (line != null && pos >= line.length())
			try {
				line = reader.readLine();
				linenumber++;
				pos = -1;
				return '\n';
			} catch (final IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		if (line == null)
			throw new EOFException();
		return line.charAt(pos);
	}

	protected HashMap<String, String> prefixe = new HashMap<String, String>();

	private char back;
	private boolean backFlag = false;
	
	protected abstract char handlePrefix() throws EOFException;

	private Literal nextLiteral() throws EOFException {
		char next = jumpOverBlanks();
		while (next == '@') {
			next=handlePrefix();
		}
		if (next == '.') {
			subject = null;
			predicate = null;
			next = jumpOverBlanks();
		}
		if (next == ';') {
			predicate = null;
			next = jumpOverBlanks();
		}
		if (next == ',') {
			next = jumpOverBlanks();
		}
		if (next == '\"') {
			// String!
			String s = "" + next;
			boolean marked = false;
			do {
				marked = (!marked && next == '\\');
				next = nextCharacter();
				s += next;
			} while (next != '\"' || marked);
			next = jumpOverBlanks();
			if (next == '^') {
				// typed literal!
				next = nextCharacter();
				if (next != '^') {
					System.err.println("Typed literal not recognized!");
					return null;
				}
				final Literal datatype = nextLiteral();
				try {
					return LiteralFactory.createTypedLiteralWithoutLazyLiteral(
							s, (URILiteral) datatype);
				} catch (final URISyntaxException e) {
					System.err.println(e);
					e.printStackTrace();
					return null;
				}
			} if(next =='@') { // language-tagged literal
				String lang = "";
				while (next != ' ' && next != '.' && next != ',' && next != ';' && next != '"' && next != '<') {
					lang += next;
					next = nextCharacter();
				}
				return LiteralFactory.createLanguageTaggedLiteralWithoutLazyLiteral(s, lang);
			} else {
				back = next;
				backFlag = true;
				return LiteralFactory.createLiteralWithoutLazyLiteral(s);
			}
		}
		if (next == '<') {
			// URI!
			String s = "" + next;
			boolean marked = false;
			do {
				marked = (!marked && next == '\\');
				next = nextCharacter();
				s += next;
			} while (next != '>' || marked);
			try {
				return LiteralFactory.createURILiteralWithoutLazyLiteral(s);
			} catch (final URISyntaxException e) {
				System.out.println(e);
				e.printStackTrace();
				return null;
			}
		}
		if (next == '_') {
			String s = "" + next;
			next = nextCharacter();
			if (next == ':') {
				while (next != ' ' && next != '.' && next != ',' && next != ';'
						&& next != '"' && next != '<') {
					s += next;
					next = nextCharacter();
				}
				
				checkLists(next);				
				
				back = next;
				backFlag = true;
				return LiteralFactory
						.createAnonymousLiteralWithoutLazyLiteral(s);
			} else
				return null;
		}
		// qualified uri!
		String namespace = "";
		String postfix = "";
		if (next == '>') {
			// qualified uri in the format >prefix:postfix<!
			next = nextCharacter();
			while (next != ':') {
				namespace += next;
				next = nextCharacter();
			}
			next = nextCharacter();
			while (next != '<' && next != '\n') {
				postfix += next;
				next = nextCharacter();
			}
		} else {
			// is it rdf:type???
			if(next=='a'){
				namespace += next;
				next = nextCharacter();
				if(next == ' ' || next == '.' || next == ',' || next == ';'
					|| next == '"' || next == '<' || next == '\n'){
					// rdf:type recognized!
					try {
						return LiteralFactory.createURILiteralWithoutLazyLiteral("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
					} catch (URISyntaxException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
			}
			
			// qualified uri in the format prefix:postfix!
			while (next != ':') {
				namespace += next;
				next = nextCharacter();
			}
			next = nextCharacter();
			while (next != ' ' && next != '.' && next != ',' && next != ';'
				&& next != '"' && next != '<' && next != '\n') {
				postfix += next;
				next = nextCharacter();
			}
		}
		
		checkLists(next);

		// if (next != ' ') {
		// back = next;
		// backFlag = true;
		// }
		try {
			// System.out.println(">>"+prefixe.get(namespace)+postfix+"<<");
			if (prefixe.get(namespace) == null) {
				System.out.println("Prefix:" + namespace);
				System.out.println("Postfix:" + postfix);
				System.out.println("Position in line:" + pos);
				System.out.println("Line Number:" + linenumber + " Line:"
						+ line);
			}
			return LiteralFactory.createURILiteralWithoutLazyLiteral("<"
					+ prefixe.get(namespace) + postfix + ">");
		} catch (final URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}
	
	private void checkLists(char next){
		if (next == '.') {
			subject = null;
			predicate = null;
		}
		if (next == ';') {
			predicate = null;
		}		
	}

	protected char jumpOverBlanks() throws EOFException {
		char next = nextCharacter();
		while (next == ' ' || next == '\n')
			next = nextCharacter();
		return next;
	}

	public static int getMaxTriples() {
		return Parser.maxTriples;
	}

	public static void setMaxTriples(final int maxTriples) {
		Parser.maxTriples = maxTriples;
	}
}
