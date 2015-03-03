
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

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.util.HashMap;
public abstract class Parser {

	private static final Logger log = LoggerFactory.getLogger(Parser.class);

	private int counter = 0;

	private static int maxTriples = 0;

	private BufferedReader reader = null;

	/**
	 * <p>getTripleNumber.</p>
	 *
	 * @return a int.
	 */
	public int getTripleNumber() {
		return this.counter - 1;
	}

	/**
	 * <p>parse.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 * @param tc a {@link lupos.engine.operators.tripleoperator.TripleConsumer} object.
	 * @param encoding a {@link java.lang.String} object.
	 * @throws java.io.UnsupportedEncodingException if any.
	 */
	public void parse(final InputStream in, final TripleConsumer tc,
			final String encoding) throws UnsupportedEncodingException {
		try {
			this.reader = new BufferedReader(new InputStreamReader(in, encoding));
			Triple t = this.nextTriple();
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
				t = this.nextTriple();
				// if (YagoParser.maxTriples > 0
				// && counter > YagoParser.maxTriples - 1)
				// System.out.println("Last triple:" + t);
				if (t != null
						&& (t.getSubject() == null || t.getPredicate() == null || t
								.getSubject() == null)) {
					log.debug("Triple:" + t);
					log.debug("Line:" + this.line + "###############pos:"
							+ this.pos);
				}

				// System.out.println(t);
			}
			this.prefixe = null;
			log.debug("Line:" + this.line + "###############pos:" + this.pos/*
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

	private Literal subject = null;
	private Literal predicate = null;

	private Triple nextTriple() throws EOFException {
		this.counter++;
		if (CommonCoreQueryEvaluator.printNumberOfTriples && this.counter % 1000000 == 0)
		 {
			log.error("#Triples:" + this.counter); // in order to display,
		}
		// but not be logged
		if (Parser.maxTriples > 0 && this.counter > Parser.maxTriples) {
			return null;
		}
		Literal nextLiteral;
		nextLiteral = this.nextLiteral();
		if (nextLiteral == null) {
			return null;
		}
		if(this.predicate != null) {
			return new Triple(this.subject, this.predicate, nextLiteral);
		}
		Literal nextLiteral2;
		nextLiteral2 = this.nextLiteral();
		if (this.subject != null) {
			this.predicate = nextLiteral;
			return new Triple(this.subject, nextLiteral, nextLiteral2);
		}
		this.subject = nextLiteral;
		this.predicate = nextLiteral2;
		Literal object;
		object = this.nextLiteral();
		return new Triple(nextLiteral, nextLiteral2, object);
	}

	private String line = null;
	private int pos = -1;
	private int linenumber = 0;

	/**
	 * <p>nextCharacter.</p>
	 *
	 * @return a char.
	 * @throws java.io.EOFException if any.
	 */
	protected char nextCharacter() throws EOFException {
		if (this.backFlag) {
			this.backFlag = false;
			return this.back;
		}
		if (this.line == null) {
			try {
				this.line = this.reader.readLine();
				this.linenumber++;
			} catch (final IOException e) {
				log.error(e.toString(),e);
				e.printStackTrace();
			}
		}
		this.pos++;
		if (this.line != null && this.pos >= this.line.length()) {
			try {
				this.line = this.reader.readLine();
				this.linenumber++;
				this.pos = -1;
				return '\n';
			} catch (final IOException e) {
				log.error(e.toString(),e);
			}
		}
		if (this.line == null) {
			throw new EOFException();
		}
		return this.line.charAt(this.pos);
	}

	protected HashMap<String, String> prefixe = new HashMap<String, String>();

	private char back;
	private boolean backFlag = false;

	/**
	 * <p>handlePrefix.</p>
	 *
	 * @return a char.
	 * @throws java.io.EOFException if any.
	 */
	protected abstract char handlePrefix() throws EOFException;

	private Literal nextLiteral() throws EOFException {
		char next = this.jumpOverBlanks();
		while (next == '@') {
			next = this.handlePrefix();
		}
		if (next == '.') {
			this.subject = null;
			this.predicate = null;
			next = this.jumpOverBlanks();
		}
		if (next == ';') {
			this.predicate = null;
			next = this.jumpOverBlanks();
		}
		if (next == ',') {
			next = this.jumpOverBlanks();
		}
		if (next == '\"') {
			// String!
			String s = "" + next;
			boolean marked = false;
			do {
				marked = (!marked && next == '\\');
				next = this.nextCharacter();
				s += next;
			} while (next != '\"' || marked);
			next = this.jumpOverBlanks();
			if (next == '^') {
				// typed literal!
				next = this.nextCharacter();
				if (next != '^') {
					log.error("Typed literal not recognized!");
					return null;
				}
				final Literal datatype = this.nextLiteral();
				try {
					return LiteralFactory.createTypedLiteralWithoutLazyLiteral(
							s, (URILiteral) datatype);
				} catch (final URISyntaxException e) {
					log.error(e.toString(),e);
					return null;
				}
			} if(next =='@') { // language-tagged literal
				String lang = "";
				while (!Parser.isSeparator(next)) {
					lang += next;
					next = this.nextCharacter();
				}
				return LiteralFactory.createLanguageTaggedLiteralWithoutLazyLiteral(s, lang);
			} else {
				this.back = next;
				this.backFlag = true;
				return LiteralFactory.createLiteralWithoutLazyLiteral(s);
			}
		}
		if (next == '<') {
			// IRI!
			final StringBuilder s = new StringBuilder("<");
			boolean marked = false;
			do {
				marked = (!marked && next == '\\');
				next = this.nextCharacter();
				// Provide escaping for some characters the SPARQL parser will not like in IRIs!
				// This is an automatic escaping which may confuse some users.
				// However, it is the only way such that LazyLiterals will work properly for iris with spaces and other (forbidden) characters which occur in real-world data
				switch(next){
					case ' ':
						s.append("%20");
						break;
					case '\n':
						s.append("%0A");
						break;
					case '\r':
						s.append("%0D");
						break;
					case '<':
						s.append("%3C");
						break;
					default:
						s.append(next);
						break;
				}
			} while (next != '>' || marked);
			try {
				return LiteralFactory.createURILiteralWithoutLazyLiteral(s.toString());
			} catch (final URISyntaxException e) {
				log.error(e.toString(),e);
				return null;
			}
		}
		if (next == '_') {
			String s = "" + next;
			next = this.nextCharacter();
			if (next == ':') {
				while (!Parser.isSeparator(next)) {
					s += next;
					next = this.nextCharacter();
				}

				this.back = next;
				this.backFlag = true;
				return LiteralFactory
						.createAnonymousLiteralWithoutLazyLiteral(s);
			} else {
				return null;
			}
		}
		// qualified uri!
		String namespace = "";
		String postfix = "";
		if (next == '>') {
			// qualified uri in the format >prefix:postfix<!
			next = this.nextCharacter();
			while (next != ':') {
				namespace += next;
				next = this.nextCharacter();
			}
			next = this.nextCharacter();
			while (next != '<' && next != '\n') {
				postfix += next;
				next = this.nextCharacter();
			}
		} else {
			// is it rdf:type???
			if(next=='a'){
				namespace += next;
				next = this.nextCharacter();
				if(Parser.isSeparator(next)){
					// rdf:type recognized!
					try {
						return LiteralFactory.createURILiteralWithoutLazyLiteral("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
					} catch (final URISyntaxException e) {
						log.error(e.toString(),e);
					}
				}
			}

			// qualified uri in the format prefix:postfix!
			while (next != ':') {
				namespace += next;
				next = this.nextCharacter();
			}
			next = this.nextCharacter();
			while (!Parser.isSeparator(next)) {
				postfix += next;
				next = this.nextCharacter();
			}
		}

		this.back = next;
		this.backFlag = true;

		try {
			// System.out.println(">>"+prefixe.get(namespace)+postfix+"<<");
			if (this.prefixe.get(namespace) == null) {
				log.debug("Prefix:" + namespace);
				log.debug("Postfix:" + postfix);
				log.debug("Position in line:" + this.pos);
				log.debug("Line Number:" + this.linenumber + " Line:" + this.line);
			}
			return LiteralFactory.createURILiteralWithoutLazyLiteral("<"
					+ this.prefixe.get(namespace) + postfix + ">");
		} catch (final URISyntaxException e) {
			log.error(e.toString(),e);
			return null;
		}
	}

	/**
	 * <p>isSeparator.</p>
	 *
	 * @param next a char.
	 * @return a boolean.
	 */
	protected final static boolean isSeparator(final char next){
		return (next == ' ' || next == '.' || next == ',' || next == ';' || next == '"' || next == '<' || next == '\n' || next == '\t');
	}

	/**
	 * <p>jumpOverBlanks.</p>
	 *
	 * @return a char.
	 * @throws java.io.EOFException if any.
	 */
	protected char jumpOverBlanks() throws EOFException {
		char next = this.nextCharacter();
		while (next == ' ' || next == '\n' || next == '\t'){
			next = this.nextCharacter();
		}
		if(next == '#'){ // jump over comments
			while(next!='\n'){
				next = this.nextCharacter();
			}
			return this.jumpOverBlanks();
		}
		return next;
	}

	/**
	 * <p>Getter for the field <code>maxTriples</code>.</p>
	 *
	 * @return a int.
	 */
	public static int getMaxTriples() {
		return Parser.maxTriples;
	}

	/**
	 * <p>Setter for the field <code>maxTriples</code>.</p>
	 *
	 * @param maxTriples a int.
	 */
	public static void setMaxTriples(final int maxTriples) {
		Parser.maxTriples = maxTriples;
	}
}
