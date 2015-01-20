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
package lupos.datastructures.bindings;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.rdf.Prefix;

public abstract class Bindings implements Serializable, Comparable<Bindings> {
	/**
	 *
	 */
	private static final long serialVersionUID = -5710553016507627379L;

	/** The subclass extending Bindings to be actually used */
	public static Class<? extends Bindings> instanceClass = BindingsArray.class;

	@Override
	public abstract Bindings clone();

	public abstract void add(final Variable var, final Literal lit);

	public abstract Literal get(final Variable var);

	public abstract Bindings createInstance();

	/**
	 * Returns the set of bound variables
	 *
	 * @return the set of bound variables
	 */
	public abstract Set<Variable> getVariableSet();

	public Iterator<Variable> getVariables() {
		return this.getVariableSet().iterator();
	}

	public void addAll(final Bindings other) {
		for (final Variable v : other.getVariableSet()) {
			this.add(v, other.get(v));
		}
	}

	@Override
	public String toString() {
		return this.toStringOnlyBindings();
	}

	public String toString(final Prefix prefix) {
		return this.toStringOnlyBindings(prefix);
	}

	public String toStringOnlyBindings() {
		String s = "{";
		final Iterator<Variable> it = this.getVariables();
		while (it.hasNext()) {
			final Variable var = it.next();
			s += var + "=" + this.get(var).originalString();
			if (it.hasNext()) {
				s += ", ";
			}
		}
		s += "}";
		return s;
	}

	public String toStringOnlyBindings(final Prefix prefix) {
		String s = "{";
		final Iterator<Variable> it = this.getVariables();
		while (it.hasNext()) {
			final Variable var = it.next();
			s += var + "=";
			if(this.get(var).originalStringDiffers()) {
				s += this.get(var).originalString();
			} else {
				s += this.get(var).toString(prefix);
			}
			if (it.hasNext()) {
				s += ", ";
			}
		}
		s += "}";
		return s;
	}

	@Override
	public int compareTo(final Bindings b) {
		final Set<Variable> sv = this.getVariableSet();
		final Set<Variable> svb = b.getVariableSet();
		// different number of variables?
		if (sv.size() > svb.size()) {
			return 1;
		} else if (svb.size() > sv.size()) {
			return -1;
		}
		sv.removeAll(svb);
		// different variables?
		if (sv.size() > 0) {
			svb.removeAll(this.getVariableSet());
			// let minimum variable of both bindings decide which bindings is
			// smaller!
			Variable min1 = null;
			for (final Variable v : sv) {
				if (min1 == null || min1.compareTo(v) > 0) {
					min1 = v;
				}
			}
			Variable min2 = null;
			for (final Variable v : svb) {
				if (min2 == null || min2.compareTo(v) > 0) {
					min2 = v;
				}
			}
			return min1.compareTo(min2);
		}
		final Iterator<Variable> it = this.getVariables();
		while (it.hasNext()) {
			final Variable var = it.next();
			final int compare = this.get(var)
					.compareToNotNecessarilySPARQLSpecificationConform(
							b.get(var));
			if (compare != 0) {
				return compare;
			}
		}
		return 0;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		final Iterator<Variable> it = this.getVariables();
		while (it.hasNext()) {
			final Variable var = it.next();
			hashCode += var.hashCode() + this.get(var).hashCode();
		}
		return hashCode;
	}

	public boolean equals(final Object other, final BindingsEqualComparison bindingsEqualComparison) {

		if (other instanceof Bindings || other instanceof BindingsMap
				|| other instanceof BindingsArray
				|| other instanceof BindingsCollection) {
			final Bindings otherBindings = (Bindings) other;

			// check the equality of their keysets first
			if (this.getVariableSet().equals(otherBindings.getVariableSet())) {

				// and check the equality of the actual bindings afterwards
				for (final Variable var : this.getVariableSet()) {
					if (!bindingsEqualComparison.equals(this.get(var), otherBindings.get(var))){
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean equals(final Object other) {
		return this.equals(other, Bindings.bindingsValueEqualComparison);
	}

	public boolean semanticallyEquals(final Object other) {
		return this.equals(other, Bindings.bindingsEqualComparisonSemanticInterpretation);
	}

	public boolean equalsExceptAnonymousLiterals(final Object other) {
		return this.equals(other, new Blanks(Bindings.bindingsEqualComparisonSemanticInterpretation));
	}

	public boolean equalsExceptAnonymousLiteralsAndInlineDataIRIs(final Object other) {
		return this.equals(other, new Iris(new Blanks(Bindings.bindingsEqualComparisonSemanticInterpretation)));
	}

	public boolean semanticallyEqualsExceptAnonymousLiterals(final Object other) {
		return this.equals(other, new Blanks(Bindings.bindingsEqualComparisonSemanticInterpretation));
	}

	public boolean semanticallyEqualsExceptAnonymousLiteralsAndInlineDataIRIs(final Object other) {
		return this.equals(other, new Iris(new Blanks(Bindings.bindingsEqualComparisonSemanticInterpretation)));
	}

	private static interface BindingsEqualComparison{
		public boolean equals(final Literal first, final Literal second);
	}

	private static class BindingsValueEqualComparison implements BindingsEqualComparison{
		@Override
		public boolean equals(final Literal first, final Literal second){
			return first.valueEquals(second);
		}
	}

	private static BindingsValueEqualComparison bindingsValueEqualComparison = new BindingsValueEqualComparison();

	private static class BindingsEqualComparisonSemanticInterpretation implements BindingsEqualComparison{
		@Override
		public boolean equals(final Literal first, final Literal second){
			return first.compareToNotNecessarilySPARQLSpecificationConform(second)==0;
		}
	}

	private static BindingsEqualComparisonSemanticInterpretation bindingsEqualComparisonSemanticInterpretation = new BindingsEqualComparisonSemanticInterpretation();

	private static class Blanks implements BindingsEqualComparison{
		private final BindingsEqualComparison pipedComparison;
		public Blanks(final BindingsEqualComparison pipedComparison){
			this.pipedComparison = pipedComparison;
		}
		@Override
		public boolean equals(final Literal first, final Literal second) {
			return this.pipedComparison.equals(first, second) || first.isBlank() || second.isBlank();
		}
	}

	private static class Iris implements BindingsEqualComparison{
		private final BindingsEqualComparison pipedComparison;
		public Iris(final BindingsEqualComparison pipedComparison){
			this.pipedComparison = pipedComparison;
		}
		@Override
		public boolean equals(final Literal first, final Literal second) {
			return this.pipedComparison.equals(first, second) || (first.isURI() && first.toString().startsWith("<inlinedata:")) || (second.isURI() && second.toString().startsWith("<inlinedata:"));
		}
	}

	/**
	 * This method adds a triple to the internal list of read triples for these
	 * bindings. This method must be overridden by Bindings-subclasses, which
	 * support this feature, e.g. BindingsArrayReadTriples
	 */
	public void addTriple(final Triple triple) {
	}

	/**
	 * This method adds all triples to the internal list of read triples for
	 * these bindings. This method must be overridden by Bindings-subclasses,
	 * which support this feature, e.g. BindingsArrayReadTriples
	 */
	public void addAllTriples(final Collection<Triple> triples) {
	}

	/**
	 * This method adds all triples of a given Bindings to the internal list of
	 * read triples for these bindings. This method must be overridden by
	 * Bindings-subclasses, which support this feature, e.g.
	 * BindingsArrayReadTriples
	 */
	public void addAllTriples(final Bindings bindings) {
	}

	public void addAllPresortingNumbers(final Bindings bindings) {
	}

	/**
	 * This method returns the internal list of read triples for these bindings.
	 * This method must be overridden by Bindings-subclasses, which support this
	 * feature, e.g. BindingsArrayReadTriples
	 */
	public List<Triple> getTriples() {
		return null;
	}

	/**
	 * adds a presorting number
	 *
	 * @param tp
	 *            the triple pattern from which this presorting number is
	 * @param orderPattern
	 *            the orderPattern of this presorting number
	 * @param pos
	 *            the presorting number itself
	 * @param max
	 *            its maximum value, i.e. the presorting number is in the range
	 *            [0, max]
	 * @param id
	 *            the id of the index used in case that e.g. there are several
	 *            default indices, which all have different presorting
	 *            numberings
	 */
	public void addPresortingNumber(final TriplePattern tp,
			final Object orderPattern, final int pos, final int max,
			final int id) {
	}

	public abstract void init();
}