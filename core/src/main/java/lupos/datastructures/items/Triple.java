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
package lupos.datastructures.items;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.Literal;

public class Triple implements Iterable<Literal>, Serializable,
		Comparable<Triple> {
	private static final long serialVersionUID = 8477763054076434668L;
	protected Literal subject;
	protected Literal predicate;
	protected Literal object;

	public Triple(final Literal subject, final Literal predicate,
			final Literal object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	public Triple(final List<Literal> spo) {
		this.subject = spo.get(0);
		this.predicate = spo.get(1);
		this.object = spo.get(2);
	}

	public Triple() {
	}

	public Literal getSubject() {
		return subject;
	}

	public Literal getPredicate() {
		return predicate;
	}

	public Literal getObject() {
		return object;
	}

	public Iterator<Literal> iterator() {
		return new Iterator<Literal>() {
			int pos = 0;

			public boolean hasNext() {
				return pos <= 2;
			}

			public Literal next() {
				return getPos(pos++);
			}

			public void remove() {
			}
		};
	}

	public Literal getPos(final int i) {
		if (i == 0)
			return subject;
		else if (i == 1)
			return predicate;
		else if (i == 2)
			return object;
		else
			return null;
	}

	public void setPos(final int i, final Literal lit) {
		if (i == 0)
			subject = lit;
		else if (i == 1)
			predicate = lit;
		else if (i == 2)
			object = lit;
	}

	@Override
	public String toString() {
		return "(" + subject + "," + predicate + "," + object + ")";
	}

	public String toN3String() {
		return subject + " " + predicate + " " + object + " .";
	}

	public String toString(final lupos.rdf.Prefix prefixInstance) {
		return "(" + subject.toString(prefixInstance) + ","
				+ predicate.toString(prefixInstance) + ","
				+ object.toString(prefixInstance) + ")";
	}

	public String toN3String(final lupos.rdf.Prefix prefixInstance) {
		return subject.toString(prefixInstance) + " "
				+ predicate.toString(prefixInstance) + " "
				+ object.toString(prefixInstance) + " .";
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public Triple clone() {
		final Triple t = new Triple(subject, predicate, object);
		return t;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Triple) {
			final Triple t = (Triple) obj;
			// consider null values such that prefix search can be done!
			return ((subject == null || t.getSubject() == null || subject
					.equals(t.getSubject()))
					&& (predicate == null || t.getPredicate() == null || predicate
							.equals(t.getPredicate())) && (object == null
					|| t.getObject() == null || object.equals(t.getObject())));
		} else
			return false;
	}

	public boolean equivalentExceptAnonymousLiterals(final Triple t) {
		return ((subject == null || t.getSubject() == null
				|| subject.equals(t.getSubject()) || (subject instanceof AnonymousLiteral && t.subject instanceof AnonymousLiteral))
				&& (predicate == null || t.getPredicate() == null
						|| predicate.equals(t.getPredicate()) || (predicate instanceof AnonymousLiteral && t.predicate instanceof AnonymousLiteral)) && (object == null
				|| t.getObject() == null || object.equals(t.getObject()) || (object instanceof AnonymousLiteral && t.object instanceof AnonymousLiteral)));
	}

	public int compareTo(final Triple arg0) {
		for (int pos = 0; pos < 3; pos++) {
			final int compare = this.getPos(pos)
					.compareToNotNecessarilySPARQLSpecificationConform(
							arg0.getPos(pos));
			if (compare != 0)
				return compare;
		}
		return 0;
	}

	// is overriden by TimestampedTriple for returning a variable with timestamp
	public Variable getVariable(final Variable var) {
		return var;
	}
}