
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
package lupos.endpoint.server.format;

import java.io.IOException;
import java.io.OutputStream;
import java.text.StringCharacterIterator;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.rif.datatypes.ListLiteral;
public abstract class HeadBodyFormatter extends Formatter {

	/**
	 * Whether or not query triples should be also written by the formatter...
	 * Note that writing query triples is in no way standard!
	 * This is a proprietary feature, which is useful in some application scenarios!
	 */
	protected final boolean writeQueryTriples;

	/**
	 * <p>Constructor for HeadBodyFormatter.</p>
	 *
	 * @param formatName a {@link java.lang.String} object.
	 * @param writeQueryTriples a boolean.
	 */
	public HeadBodyFormatter(final String formatName, final boolean writeQueryTriples){
		super(formatName);
		this.writeQueryTriples = writeQueryTriples;
	}

	/**
	 * <p>Constructor for HeadBodyFormatter.</p>
	 *
	 * @param formatName a {@link java.lang.String} object.
	 */
	public HeadBodyFormatter(final String formatName){
		this(formatName, false);
	}

	/**
	 * <p>Constructor for HeadBodyFormatter.</p>
	 *
	 * @param formatName a {@link java.lang.String} object.
	 * @param key a {@link java.lang.String} object.
	 * @param writeQueryTriples a boolean.
	 */
	public HeadBodyFormatter(final String formatName, final String key, final boolean writeQueryTriples){
		super(formatName, key);
		this.writeQueryTriples = writeQueryTriples;
	}

	/**
	 * <p>Constructor for HeadBodyFormatter.</p>
	 *
	 * @param formatName a {@link java.lang.String} object.
	 * @param key a {@link java.lang.String} object.
	 */
	public HeadBodyFormatter(final String formatName, final String key){
		this(formatName, key, false);
	}

	/**
	 * <p>writeBooleanResult.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param result a boolean.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeBooleanResult(final OutputStream os, final boolean result) throws IOException;

	/**
	 * <p>writeStartHead.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeStartHead(final OutputStream os) throws IOException;

	/**
	 * <p>writeFirstVariableInHead.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param v a {@link lupos.datastructures.items.Variable} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeFirstVariableInHead(final OutputStream os, final Variable v) throws IOException{
		this.writeVariableInHead(os, v);
	}

	/**
	 * <p>writeVariableInHead.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param v a {@link lupos.datastructures.items.Variable} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeVariableInHead(final OutputStream os, final Variable v) throws IOException;

	/**
	 * <p>writeQueryTriplesHead.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTriplesHead(final OutputStream os) throws IOException {
		// Override method if the formatter supports to submit also the annotated triples!
		// This is in no way standard and a LUPOSDATE proprietary feature!
	}

	/**
	 * <p>writeEndHead.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeEndHead(final OutputStream os) throws IOException;

	/**
	 * <p>writeFirstStartResult.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeFirstStartResult(final OutputStream os) throws IOException{
		this.writeStartResult(os);
	}

	/**
	 * <p>writeStartResult.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeStartResult(final OutputStream os) throws IOException;

	/**
	 * <p>writeEndResult.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeEndResult(final OutputStream os) throws IOException;

	/**
	 * <p>writeQueryTriples.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param queryTriples a {@link java.util.List} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTriples(final OutputStream os, final List<Triple> queryTriples) throws IOException {
		// This is in no way standard and a LUPOSDATE proprietary feature!
		this.writeQueryTriplesStart(os);
		boolean firstTime = true;
		for(final Triple triple: queryTriples){
			if(firstTime){
				firstTime = false;
				this.writeQueryFirstTriple(os, triple);

			} else {
				this.writeQueryTriple(os, triple);
			}
		}
		this.writeQueryTriplesEnd(os);
	}

	/**
	 * <p>writeQueryTriplesStart.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTriplesStart(final OutputStream os) throws IOException {
		// Override method if the formatter supports to submit also the annotated triples!
		// This is in no way standard and a LUPOSDATE proprietary feature!
	}

	/**
	 * <p>writeQueryTriplesEnd.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTriplesEnd(final OutputStream os) throws IOException {
		// Override method if the formatter supports to submit also the annotated triples!
		// This is in no way standard and a LUPOSDATE proprietary feature!
	}

	/**
	 * <p>writeQueryTripleStart.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTripleStart(final OutputStream os) throws IOException {
		// Override method if the formatter supports to submit also the annotated triples!
		// This is in no way standard and a LUPOSDATE proprietary feature!
	}

	/**
	 * <p>writeQueryTripleFirstStart.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTripleFirstStart(final OutputStream os) throws IOException {
		// This is in no way standard and a LUPOSDATE proprietary feature!
		this.writeQueryTripleStart(os);
	}

	/**
	 * <p>writeQueryTripleEnd.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTripleEnd(final OutputStream os) throws IOException {
		// Override method if the formatter supports to submit also the annotated triples!
		// This is in no way standard and a LUPOSDATE proprietary feature!
	}

	/**
	 * <p>writeQueryTripleStartComponent.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTripleStartComponent(final OutputStream os) throws IOException {
		// Override method if the formatter supports to submit also the annotated triples!
		// This is in no way standard and a LUPOSDATE proprietary feature!
	}

	/**
	 * <p>writeQueryTripleEndComponent.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTripleEndComponent(final OutputStream os) throws IOException {
		// Override method if the formatter supports to submit also the annotated triples!
		// This is in no way standard and a LUPOSDATE proprietary feature!
	}

	/**
	 * <p>writeQueryTripleSubject.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param literal a {@link lupos.datastructures.items.literal.Literal} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTripleSubject(final OutputStream os, final Literal literal) throws IOException {
		// This is in no way standard and a LUPOSDATE proprietary feature!
		this.writeQueryTripleStartComponent(os);
		this.writeLiteral(os, literal);
		this.writeQueryTripleEndComponent(os);
	}

	/**
	 * <p>writeQueryTriplePredicate.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param literal a {@link lupos.datastructures.items.literal.Literal} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTriplePredicate(final OutputStream os, final Literal literal) throws IOException {
		// This is in no way standard and a LUPOSDATE proprietary feature!
		this.writeQueryTripleStartComponent(os);
		this.writeLiteral(os, literal);
		this.writeQueryTripleEndComponent(os);
	}

	/**
	 * <p>writeQueryTripleObject.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param literal a {@link lupos.datastructures.items.literal.Literal} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTripleObject(final OutputStream os, final Literal literal) throws IOException {
		// This is in no way standard and a LUPOSDATE proprietary feature!
		this.writeQueryTripleStartComponent(os);
		this.writeLiteral(os, literal);
		this.writeQueryTripleEndComponent(os);
	}

	/**
	 * <p>writeQueryTriple.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param triple a {@link lupos.datastructures.items.Triple} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryTriple(final OutputStream os, final Triple triple) throws IOException {
		// This is in no way standard and a LUPOSDATE proprietary feature!
		this.writeQueryTripleStart(os);
		this.writeQueryTripleSubject(os, triple.getSubject());
		this.writeQueryTriplePredicate(os, triple.getPredicate());
		this.writeQueryTripleObject(os, triple.getObject());
		this.writeQueryTripleEnd(os);
	}

	/**
	 * <p>writeQueryFirstTriple.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param triple a {@link lupos.datastructures.items.Triple} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeQueryFirstTriple(final OutputStream os, final Triple triple) throws IOException {
		// This is in no way standard and a LUPOSDATE proprietary feature!
		this.writeQueryTripleFirstStart(os);
		this.writeQueryTripleSubject(os, triple.getSubject());
		this.writeQueryTriplePredicate(os, triple.getPredicate());
		this.writeQueryTripleObject(os, triple.getObject());
		this.writeQueryTripleEnd(os);
	}

	/**
	 * <p>writeEpilogue.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeEpilogue(final OutputStream os) throws IOException;

	/**
	 * <p>writeFirstStartBinding.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param v a {@link lupos.datastructures.items.Variable} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeFirstStartBinding(final OutputStream os, final Variable v) throws IOException{
		this.writeStartBinding(os, v);
	}

	/**
	 * <p>writeStartBinding.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param v a {@link lupos.datastructures.items.Variable} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeStartBinding(final OutputStream os, final Variable v) throws IOException;

	/**
	 * <p>writeEndBinding.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeEndBinding(final OutputStream os) throws IOException;

	/**
	 * <p>writeBlankNode.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param blankNode a {@link lupos.datastructures.items.literal.AnonymousLiteral} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeBlankNode(final OutputStream os, AnonymousLiteral blankNode) throws IOException;

	/**
	 * <p>writeURI.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param uri a {@link lupos.datastructures.items.literal.URILiteral} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeURI(final OutputStream os, URILiteral uri) throws IOException;

	/**
	 * <p>writeSimpleLiteral.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param literal a {@link lupos.datastructures.items.literal.Literal} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeSimpleLiteral(final OutputStream os, Literal literal) throws IOException;

	public void writeListLiteral(final OutputStream os, final Literal literal) throws IOException {
		final String content = "\""+HeadBodyFormatter.forJSON(literal.originalString())+"\"";
		this.writeSimpleLiteral(os, LiteralFactory.createLiteralWithoutLazyLiteral(content));
	}

	public static String forJSON(final String aText){
	    final StringBuilder result = new StringBuilder();
	    final StringCharacterIterator iterator = new StringCharacterIterator(aText);
	    char character = iterator.current();
	    while (character != StringCharacterIterator.DONE){
	      if( character == '\"' ){
	        result.append("\\\"");
	      }
	      else if(character == '\\'){
	        result.append("\\\\");
	      }
	      else if(character == '/'){
	        result.append("\\/");
	      }
	      else if(character == '\b'){
	        result.append("\\b");
	      }
	      else if(character == '\f'){
	        result.append("\\f");
	      }
	      else if(character == '\n'){
	        result.append("\\n");
	      }
	      else if(character == '\r'){
	        result.append("\\r");
	      }
	      else if(character == '\t'){
	        result.append("\\t");
	      }
	      else {
	        //the char is not a special one
	        //add it to the result as is
	        result.append(character);
	      }
	      character = iterator.next();
	    }
	    return result.toString();
	  }


	/**
	 * <p>writeTypedLiteral.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param literal a {@link lupos.datastructures.items.literal.TypedLiteral} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeTypedLiteral(final OutputStream os, TypedLiteral literal) throws IOException;

	/**
	 * <p>writeLanguageTaggedLiteral.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param literal a {@link lupos.datastructures.items.literal.LanguageTaggedLiteral} object.
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeLanguageTaggedLiteral(final OutputStream os, LanguageTaggedLiteral literal) throws IOException;

	/**
	 * <p>getVariablesToIterateOnForOneBindings.</p>
	 *
	 * @param variables a {@link java.util.Collection} object.
	 * @param bindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a {@link java.util.Iterator} object.
	 */
	public Iterator<Variable> getVariablesToIterateOnForOneBindings(final Collection<Variable> variables, final Bindings bindings){
		return new Iterator<Variable>(){

			Iterator<Variable> it = variables.iterator();
			Variable next = this.computeNext();

			@Override
			public boolean hasNext() {
				return this.next!=null;
			}

			@Override
			public Variable next() {
				if(this.next==null){
					return null;
				} else {
					final Variable znext = this.next;
					this.next = this.computeNext();
					return znext;
				}
			}

			public Variable computeNext() {
				while(this.it.hasNext()){
					final Variable v = this.it.next();
					if(bindings.getVariableSet().contains(v)){
						return v;
					}
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	public void writeResult(final OutputStream os, final Collection<Variable> variables, final QueryResult queryResult) throws IOException {
		if(queryResult instanceof GraphResult){
			super.writeResult(os, variables, queryResult);
		} else if(queryResult instanceof BooleanResult){
			this.writeBooleanResult(os, ((BooleanResult) queryResult).isTrue());
		} else {
			this.writeStartHead(os);
			boolean firstTime = true;
			final Iterator<Variable> it_v = variables.iterator();
			while(it_v.hasNext()){
				final Variable v = it_v.next();
				if(firstTime){
					this.writeFirstVariableInHead(os, v);
					firstTime = false;
				} else {
					this.writeVariableInHead(os, v);
				}
			}
			if(this.writeQueryTriples){
				this.writeQueryTriplesHead(os);
			}
			this.writeEndHead(os);
			firstTime = true;
			final Iterator<Bindings> it = queryResult.oneTimeIterator();
			while(it.hasNext()){
				if(firstTime){
					this.writeFirstStartResult(os);
					firstTime = false;
				} else {
					this.writeStartResult(os);
				}
				final Bindings bindings = it.next();
				boolean firstTimeBinding = true;
				final Iterator<Variable> it_v2 = this.getVariablesToIterateOnForOneBindings(variables, bindings);
				while(it_v2.hasNext()){
					final Variable v = it_v2.next();
					if(firstTimeBinding){
						this.writeFirstStartBinding(os, v);
						firstTimeBinding = false;
					} else {
						this.writeStartBinding(os, v);
					}

					this.writeLiteral(os, bindings.get(v));

					this.writeEndBinding(os);
				}

				if(this.writeQueryTriples && bindings instanceof BindingsArrayReadTriples){
					this.writeQueryTriples(os, bindings.getTriples());
				}

				this.writeEndResult(os);
			}
			this.writeEpilogue(os);
		}
	}

	/**
	 * <p>writeLiteral.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param literal a {@link lupos.datastructures.items.literal.Literal} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeLiteral(final OutputStream os, final Literal literal) throws IOException{
		if(literal!=null){
			final Literal materializedLiteral = (literal instanceof LazyLiteral)?((LazyLiteral)literal).getLiteral(): literal;
			if (materializedLiteral.isBlank()){
				this.writeBlankNode(os, (AnonymousLiteral) materializedLiteral);
			} else if(materializedLiteral.isURI()){
				this.writeURI(os, (URILiteral) materializedLiteral);
			} else {
				// literal => <literal>
				if(materializedLiteral instanceof TypedLiteral){
					this.writeTypedLiteral(os, (TypedLiteral)materializedLiteral);
				} else
					if(materializedLiteral instanceof LanguageTaggedLiteral){
						this.writeLanguageTaggedLiteral(os, (LanguageTaggedLiteral)materializedLiteral);
					} else {
						if(materializedLiteral instanceof ListLiteral){
							this.writeListLiteral(os, materializedLiteral);
						} else {
							this.writeSimpleLiteral(os, materializedLiteral);
						}
					}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isWriteQueryTriples() {
		return this.writeQueryTriples;
	}
}
