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
package lupos.rif.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;

import com.google.common.collect.Multimap;

/**
 * Represents a Variable, as used in a Rule. Internally a Lupos Variable is
 * used.
 *
 * @author jenskluttig
 * @version $Id: $Id
 */
public class RuleVariable extends AbstractRuleNode implements IExpression {
	private Variable internalVariable;

	/**
	 * <p>Constructor for RuleVariable.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public RuleVariable(String name) {
		internalVariable = new Variable(name);
	}

	/**
	 * <p>getName.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return internalVariable.getName();
	}

	/**
	 * <p>getVariable.</p>
	 *
	 * @return a {@link lupos.datastructures.items.Variable} object.
	 */
	public Variable getVariable() {
		return internalVariable;
	}

	/**
	 * <p>setVariable.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setVariable(String name) {
		internalVariable = new Variable(name);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return getVariable().hashCode();
	}

	/**
	 * <p>accept.</p>
	 *
	 * @param visitor a {@link lupos.rif.IRuleVisitor} object.
	 * @param arg a A object.
	 * @param <R> a R object.
	 * @param <A> a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public <R, A> R accept(IRuleVisitor<R, A> visitor, A arg) throws RIFException {
		return visitor.visit(this, arg);
	}

	/**
	 * <p>getLabel.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLabel() {
		return internalVariable.toString();
	}

	/**
	 * <p>containsOnlyVariables.</p>
	 *
	 * @return a boolean.
	 */
	public boolean containsOnlyVariables() {
		return true;
	}

	/**
	 * <p>getVariables.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<RuleVariable> getVariables() {
		Set<RuleVariable> ret = new HashSet<RuleVariable>();
		ret.add(this);
		return ret;
	}

	/**
	 * <p>getPredicates.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<Uniterm> getPredicates() {
		return Arrays.asList();
	}

	/** {@inheritDoc} */
	public Object evaluate(Bindings binding) {
		Object ret = binding.get(internalVariable);
		if (ret == null)
			return getVariable();
		else
			return ret;
	}

	/** {@inheritDoc} */
	public boolean isBound(RuleVariable var, Collection<RuleVariable> boundVars) {
		return false;
	}

	/**
	 * <p>isPossibleAssignment.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPossibleAssignment() {
		return false;
	}

	/** {@inheritDoc} */
	public String toString(Prefix prefixInstance) {
		return toString();
	}

	/** {@inheritDoc} */
	public boolean equals(Object expr) {
		if (expr instanceof RuleVariable)
			return ((RuleVariable) expr).getVariable().equals(internalVariable);
		else
			return false;
	}

	/** {@inheritDoc} */
	public Object evaluate(Bindings binding, Object optionalResult) {
		return evaluate(binding);
	}

	/** {@inheritDoc} */
	public Object evaluate(Bindings binding, Object optionalResult, Multimap<IExpression, IExpression> equalities) {
		return evaluate(binding);
	}
}
