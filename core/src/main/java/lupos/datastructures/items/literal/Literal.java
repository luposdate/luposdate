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

	public boolean valueEquals(final Literal lit) {
		return (toString().compareTo(lit.toString()) == 0);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Literal) {
			return this.valueEquals((Literal) obj);
		} else
			return false;
	}

	@Override
	public int compareTo(final Literal other) {

		return ComparatorAST.intComp(this, other);
	}

	public int compareToNotNecessarilySPARQLSpecificationConform(final Literal other) {
		return ComparatorAST.intComp(this, other);
	}
	
	public abstract String[] getUsedStringRepresentations();

	@Override
	public Literal getLiteral(final Bindings b) {
		return this;
	}

	@Override
	public String getName() {
		return toString();
	}

	@Override
	public boolean isVariable() {
		return false;
	}

	public boolean isBlank() {
		return (this instanceof AnonymousLiteral);
	}

	public boolean isURI() {
		return (this instanceof URILiteral);
	}
	
	public boolean isTypedLiteral(){
		return (this instanceof TypedLiteral);
	}
	
	public boolean isLanguageTaggedLiteral(){
		return (this instanceof LanguageTaggedLiteral);
	}
	
	public boolean isSimpleLiteral(){
		return !(this.isBlank() || this.isLanguageTaggedLiteral() || this.isTypedLiteral() || this.isURI());
	}
	
	public boolean isXMLSchemaStringLiteral(){
		return false;
	}

	public String originalString() {
		return toString();
	}

	public boolean originalStringDiffers() {
		return false;
	}
	
	/**
	 * @return A key for this literal. Typically it is the return value of toString(), but LazyLiterals return their code for efficiency reasons.
	 */
	public String getKey(){
		return this.toString();
	}

	/**
	 * @return A key for the original representation of this literal. Typically it is the return value of toString(), but LazyLiterals return their code for efficiency reasons.
	 */
	public String getOriginalKey(){
		return this.originalString();
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public String printYagoStringWithPrefix() {
		return toString();
	}
	
	@SuppressWarnings("unused")
	public String toString(Prefix prefix){
		return toString();
	}

	public abstract Literal createThisLiteralNew();
}
