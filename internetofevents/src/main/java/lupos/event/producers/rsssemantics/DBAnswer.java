/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
/**
 * This class represents a single result from querying the DBPedia database.
 */

package lupos.event.producers.rsssemantics;

import java.util.ArrayList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.util.Literals;

/**
 * Represents an answer from the database.
 */
public class DBAnswer {

	private Literal thumbnail;
	private Literal wikiarticle = Literals.createTyped("", Literals.XSD.String);
	private Literal subject = Literals.createTyped("", Literals.XSD.String);
	private Literal name = Literals.createTyped("", Literals.XSD.String);
	private Literal birthDate = Literals.createTyped("", Literals.XSD.String);
	private Literal deathDate = Literals.createTyped("", Literals.XSD.String);
	private Literal label = Literals.createTyped("", Literals.XSD.String);
	private Literal comment = Literals.createTyped("", Literals.XSD.String);
	private Literal birthPlace = Literals.createTyped("", Literals.XSD.String);
	public static final String NAMESPACE = "http://www.ifis.uni-luebeck.de/events/RSSSemantics/";

	public static class Predicates {
		public static final URILiteral THUMBNAIL = Literals.createURI(
				DBAnswer.NAMESPACE, "Thumbnail");
		public static final URILiteral WIKIARTICLE = Literals.createURI(
				DBAnswer.NAMESPACE, "WikiArticle");
		public static final URILiteral SUBJECT = Literals.createURI(
				DBAnswer.NAMESPACE, "Subject");
		public static final URILiteral NAME = Literals.createURI(
				DBAnswer.NAMESPACE, "Name");
		public static final URILiteral BIRTHDATE = Literals.createURI(
				DBAnswer.NAMESPACE, "BirthDate");
		public static final URILiteral DEATHDATE = Literals.createURI(
				DBAnswer.NAMESPACE, "DeathDate");
		public static final URILiteral LABEL = Literals.createURI(
				DBAnswer.NAMESPACE, "Label");
		public static final URILiteral COMMENT = Literals.createURI(
				DBAnswer.NAMESPACE, "Comment");
		public static final URILiteral BIRTHPLACE = Literals.createURI(
				DBAnswer.NAMESPACE, "BirthPlace");
	}

	/**
	 * read data from input binding and assign them to Object variables
	 */

	public DBAnswer(Bindings b) throws Exception {
		String[] variables = { "thumbnail", "wikiarticle", "subject", "name",
				"birthDate", "deathDate", "birthPlace", "label",  "comment" };
		int i = 0;
		Literal k = b.get(new Variable(variables[i]));
		if (k != Literals.createTyped("", Literals.XSD.String))
			this.thumbnail = k;
		i++;
		k = b.get(new Variable(variables[i]));
		if (k != Literals.createTyped("", Literals.XSD.String))
			this.wikiarticle = k;
		i++;
		k = b.get(new Variable(variables[i]));
		if (k != Literals.createTyped("", Literals.XSD.String))
			this.subject = k;
		i++;
		k = b.get(new Variable(variables[i]));
		if (k != Literals.createTyped("", Literals.XSD.String))
			this.name = k;
		i++;
		k = b.get(new Variable(variables[i]));
		if (k != Literals.createTyped("", Literals.XSD.String))
			this.birthDate = k;
		i++;
		k = b.get(new Variable(variables[i]));
		if (k != Literals.createTyped("", Literals.XSD.String))
			this.deathDate = k;
		i++;
		k = b.get(new Variable(variables[i]));
		if (k != Literals.createTyped("", Literals.XSD.String))
			this.birthPlace = k;
		i++;
		k = b.get(new Variable(variables[i]));
		if (k != Literals.createTyped("", Literals.XSD.String))
			this.label = k;
		i++;
		k = b.get(new Variable(variables[i]));
		if (k != Literals.createTyped("", Literals.XSD.String))
			this.comment = k;
	}

	public String getLabel() throws Exception {
		if (this.label != Literals.createTyped("", Literals.XSD.String))
			return this.label.originalString();
		else
			return "undefined";
	}

	@Override
	public String toString() {
		return "Thumbnail: " + this.thumbnail + " wikiarticle: " + this.wikiarticle
				+ " subject: " + this.subject + " name: " + this.name + " birthDate "
				+ this.birthDate;
	}

	public ArrayList<Triple> generateTriples() throws Exception {

		ArrayList<Triple> t = new ArrayList<Triple>();

		/**
		 * generate Triple List to return
		 */

		if (this.thumbnail != null) {
			t.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS,
					Predicates.THUMBNAIL, this.thumbnail));
		}
		if (this.wikiarticle != null) {
			t.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS,
					Predicates.WIKIARTICLE, this.wikiarticle));
		}
		if (this.subject != null) {
			t.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS,
					Predicates.SUBJECT, this.subject));
		}
		if (this.name != null) {
			t.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS,
					Predicates.NAME, this.name));
		}
		if (this.birthDate != null) {
			t.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS,
					Predicates.BIRTHDATE, this.birthDate));
		}
		if (this.deathDate != null) {
			t.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS,
					Predicates.DEATHDATE, this.deathDate));
		}

		if (this.label != null) {
			t.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS,
					Predicates.LABEL, this.label));
		}
		if (this.comment != null) {
			t.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS,
					Predicates.COMMENT, this.comment));
		}
		if (this.birthPlace != null) {
			t.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS,
					Predicates.BIRTHPLACE, this.birthPlace));
		}
		return t;
	}
}