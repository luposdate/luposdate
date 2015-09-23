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
package lupos.datastructures.items.literal;

import java.io.Externalizable;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorAST;
import lupos.rdf.Prefix;
public abstract class Literal implements Item, Comparable<Literal>, Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * <p>valueEquals.</p>
	 *
	 * @param lit a {@link lupos.datastructures.items.literal.Literal} object.
	 * @return a boolean.
	 */
	public boolean valueEquals(final Literal lit) {
		return (toString().compareTo(lit.toString()) == 0);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Literal) {
			return this.valueEquals((Literal) obj);
		} else
			return false;
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final Literal other) {

		return ComparatorAST.intComp(this, other);
	}

	/**
	 * <p>compareToNotNecessarilySPARQLSpecificationConform.</p>
	 *
	 * @param other a {@link lupos.datastructures.items.literal.Literal} object.
	 * @return a int.
	 */
	public int compareToNotNecessarilySPARQLSpecificationConform(final Literal other) {
		return ComparatorAST.intComp(this, other);
	}
	
	/**
	 * <p>getUsedStringRepresentations.</p>
	 *
	 * @return an array of {@link java.lang.String} objects.
	 */
	public abstract String[] getUsedStringRepresentations();

	/** {@inheritDoc} */
	@Override
	public Literal getLiteral(final Bindings b) {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return toString();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isVariable() {
		return false;
	}

	/**
	 * <p>isBlank.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isBlank() {
		return (this instanceof AnonymousLiteral);
	}

	/**
	 * <p>isURI.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isURI() {
		return (this instanceof URILiteral);
	}
	
	/**
	 * <p>isTypedLiteral.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isTypedLiteral(){
		return (this instanceof TypedLiteral);
	}
	
	/**
	 * <p>isLanguageTaggedLiteral.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isLanguageTaggedLiteral(){
		return (this instanceof LanguageTaggedLiteral);
	}
	
	/**
	 * <p>isSimpleLiteral.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isSimpleLiteral(){
		return !(this.isBlank() || this.isLanguageTaggedLiteral() || this.isTypedLiteral() || this.isURI());
	}
	
	/**
	 * <p>isXMLSchemaStringLiteral.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isXMLSchemaStringLiteral(){
		return false;
	}

	/**
	 * <p>originalString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String originalString() {
		return toString();
	}

	/**
	 * <p>originalStringDiffers.</p>
	 *
	 * @return a boolean.
	 */
	public boolean originalStringDiffers() {
		return false;
	}
	
	/**
	 * <p>getKey.</p>
	 *
	 * @return A key for this literal. Typically it is the return value of toString(), but LazyLiterals return their code for efficiency reasons.
	 */
	public String getKey(){
		return this.toString();
	}

	/**
	 * <p>getOriginalKey.</p>
	 *
	 * @return A key for the original representation of this literal. Typically it is the return value of toString(), but LazyLiterals return their code for efficiency reasons.
	 */
	public String getOriginalKey(){
		return this.originalString();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * <p>printYagoStringWithPrefix.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String printYagoStringWithPrefix() {
		return toString();
	}
	
	/**
	 * <p>toString.</p>
	 *
	 * @param prefix a {@link lupos.rdf.Prefix} object.
	 * @return a {@link java.lang.String} object.
	 */
	@SuppressWarnings("unused")
	public String toString(Prefix prefix){
		return toString();
	}

	/**
	 * <p>createThisLiteralNew.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public abstract Literal createThisLiteralNew();
}
