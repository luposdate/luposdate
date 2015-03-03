
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
package lupos.datastructures.items;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.misc.util.ImmutableIterator;
public class Triple implements Iterable<Literal>, Serializable,
		Comparable<Triple> {
	private static final long serialVersionUID = 8477763054076434668L;
	protected Literal subject;
	protected Literal predicate;
	protected Literal object;

	/**
	 * <p>Constructor for Triple.</p>
	 *
	 * @param subject a {@link lupos.datastructures.items.literal.Literal} object.
	 * @param predicate a {@link lupos.datastructures.items.literal.Literal} object.
	 * @param object a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public Triple(final Literal subject, final Literal predicate,
			final Literal object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	/**
	 * <p>Constructor for Triple.</p>
	 *
	 * @param spo a {@link java.util.List} object.
	 */
	public Triple(final List<Literal> spo) {
		this.subject = spo.get(0);
		this.predicate = spo.get(1);
		this.object = spo.get(2);
	}

	/**
	 * <p>Constructor for Triple.</p>
	 */
	public Triple() {
		// standard constructor without initialization...
	}

	/**
	 * <p>Constructor for Triple.</p>
	 *
	 * @param literals an array of {@link lupos.datastructures.items.literal.Literal} objects.
	 */
	public Triple(final Literal[] literals) {
		this(literals[0], literals[1], literals[2]);
	}

	/**
	 * <p>Getter for the field <code>subject</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public Literal getSubject() {
		return this.subject;
	}

	/**
	 * <p>Getter for the field <code>predicate</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public Literal getPredicate() {
		return this.predicate;
	}

	/**
	 * <p>Getter for the field <code>object</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public Literal getObject() {
		return this.object;
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<Literal> iterator() {
		return new ImmutableIterator<Literal>() {
			int pos = 0;

			@Override
			public boolean hasNext() {
				return this.pos <= 2;
			}

			@Override
			public Literal next() {
				return Triple.this.getPos(this.pos++);
			}
		};
	}

	/**
	 * <p>getPos.</p>
	 *
	 * @param i a int.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public Literal getPos(final int i) {
		if (i == 0) {
			return this.subject;
		} else if (i == 1) {
			return this.predicate;
		} else if (i == 2) {
			return this.object;
		} else {
			return null;
		}
	}

	/**
	 * <p>setPos.</p>
	 *
	 * @param i a int.
	 * @param lit a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public void setPos(final int i, final Literal lit) {
		if (i == 0) {
			this.subject = lit;
		} else if (i == 1) {
			this.predicate = lit;
		} else if (i == 2) {
			this.object = lit;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "(" + this.subject + "," + this.predicate + "," + this.object + ")";
	}

	/**
	 * <p>toN3String.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toN3String() {
		return this.subject + " " + this.predicate + " " + this.object + " .";
	}

	/**
	 * <p>toString.</p>
	 *
	 * @param prefixInstance a {@link lupos.rdf.Prefix} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String toString(final lupos.rdf.Prefix prefixInstance) {
		return "(" + this.subject.toString(prefixInstance) + ","
				+ this.predicate.toString(prefixInstance) + ","
				+ this.object.toString(prefixInstance) + ")";
	}

	/**
	 * <p>toN3String.</p>
	 *
	 * @param prefixInstance a {@link lupos.rdf.Prefix} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String toN3String(final lupos.rdf.Prefix prefixInstance) {
		return this.subject.toString(prefixInstance) + " "
				+ this.predicate.toString(prefixInstance) + " "
				+ this.object.toString(prefixInstance) + " .";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public Triple clone() {
		final Triple t = new Triple(this.subject, this.predicate, this.object);
		return t;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Triple) {
			final Triple t = (Triple) obj;
			// consider null values such that prefix search can be done!
			return ((this.subject == null || t.getSubject() == null || this.subject.equals(t.getSubject()))
					&& (this.predicate == null || t.getPredicate() == null || this.predicate
							.equals(t.getPredicate())) && (this.object == null
					|| t.getObject() == null || this.object.equals(t.getObject())));
		} else {
			return false;
		}
	}

	/**
	 * <p>equivalentExceptAnonymousLiterals.</p>
	 *
	 * @param t a {@link lupos.datastructures.items.Triple} object.
	 * @return a boolean.
	 */
	public boolean equivalentExceptAnonymousLiterals(final Triple t) {
		return ((this.subject == null || t.getSubject() == null
				|| this.subject.equals(t.getSubject()) || (this.subject instanceof AnonymousLiteral && t.subject instanceof AnonymousLiteral))
				&& (this.predicate == null || t.getPredicate() == null
						|| this.predicate.equals(t.getPredicate()) || (this.predicate instanceof AnonymousLiteral && t.predicate instanceof AnonymousLiteral)) &&
						(this.object == null || t.getObject() == null || this.object.equals(t.getObject()) || (this.object instanceof AnonymousLiteral && t.object instanceof AnonymousLiteral)));
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final Triple arg0) {
		for (int pos = 0; pos < 3; pos++) {
			final int compare = this.getPos(pos)
					.compareToNotNecessarilySPARQLSpecificationConform(
							arg0.getPos(pos));
			if (compare != 0) {
				return compare;
			}
		}
		return 0;
	}

	// is overriden by TimestampedTriple for returning a variable with timestamp
	/**
	 * <p>getVariable.</p>
	 *
	 * @param var a {@link lupos.datastructures.items.Variable} object.
	 * @return a {@link lupos.datastructures.items.Variable} object.
	 */
	public Variable getVariable(final Variable var) {
		return var;
	}
}
