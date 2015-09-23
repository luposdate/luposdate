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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;
import lupos.rif.builtin.BooleanLiteral;

import com.google.common.collect.Multimap;
public class Equality extends AbstractRuleNode implements IExpression {
	public IExpression rightExpr;
	public IExpression leftExpr;

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
		return leftExpr.toString() + " = " + rightExpr.toString();
	}

	/**
	 * <p>containsOnlyVariables.</p>
	 *
	 * @return a boolean.
	 */
	public boolean containsOnlyVariables() {
		return rightExpr.containsOnlyVariables() && leftExpr.containsOnlyVariables();
	}

	/**
	 * <p>getVariables.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<RuleVariable> getVariables() {
		Set<RuleVariable> variables = new HashSet<RuleVariable>();
		variables.addAll(rightExpr.getVariables());
		variables.addAll(leftExpr.getVariables());
		return variables;
	}

	/**
	 * <p>getPredicates.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<Uniterm> getPredicates() {
		List<Uniterm> terms = new ArrayList<Uniterm>();
		terms.addAll(rightExpr.getPredicates());
		terms.addAll(leftExpr.getPredicates());
		return terms;
	}

	/** {@inheritDoc} */
	public Object evaluate(Bindings binding) {
		return evaluate(binding, null);
	}

	/** {@inheritDoc} */
	public Object evaluate(Bindings binding, Object optionalResult) {
		return evaluate(binding, optionalResult, null);
	}

	/** {@inheritDoc} */
	public Object evaluate(Bindings binding, Object result, Multimap<IExpression, IExpression> equalities) {
		// Mehrere M�glichkeiten die auftreten k�nnen
		boolean leftHasUnbound = false;
		boolean rightHasUnbound = false;
		boolean leftAssign = false;
		boolean rightAssign = false;
		Set<Variable> bindingVars = binding.getVariableSet();
		for (RuleVariable var : leftExpr.getVariables())
			if (!bindingVars.contains(var.getVariable())) {
				leftHasUnbound = true;
				// Linke ungebunden, muss allein stehen
				if (leftExpr instanceof RuleVariable)
					leftAssign = true;
			}
		for (RuleVariable var : rightExpr.getVariables())
			if (!bindingVars.contains(var.getVariable())) {
				rightHasUnbound = true;
				// Linke ungebunden, muss allein stehen
				if (rightExpr instanceof RuleVariable)
					rightAssign = true;
			}
		if (rightAssign && leftAssign)
			throw new RIFException("All Variables in Assignment " + toString()
					+ " are unbound!");
		if (rightAssign ^ leftAssign) {
			// Einzelne ungebundene variable auf einer seite, dann einfach die
			// andere auswerten und ergebniss reinschreiben
			Literal assignValue = (Literal) (leftAssign ? rightExpr
					.evaluate(binding) : leftExpr.evaluate(binding));
			RuleVariable var = (RuleVariable) (leftAssign ? leftExpr
					: rightExpr);
			binding.add(var.getVariable(), assignValue);
			return BooleanLiteral.TRUE;
		} else {
			// fester wert auf jeder seite, also erst aussage mit gebundenen
			// variable auswerten und anderem als result mitgeben.
			Item left = null;
			Item right = null;
			if (rightHasUnbound) {
				left = (Item) leftExpr.evaluate(binding);
				right = (Item) rightExpr.evaluate(binding, left);
			} else if (leftHasUnbound) {
				right = (Item) rightExpr.evaluate(binding);
				left = (Item) leftExpr.evaluate(binding, right);
			} else {
				left = (Item) leftExpr.evaluate(binding);
				right = (Item) rightExpr.evaluate(binding);
			}
			return BooleanLiteral.create(left.equals(right));
		}
	}

	/** {@inheritDoc} */
	public boolean isBound(RuleVariable var, Collection<RuleVariable> boundVars) {
		if (leftExpr.equals(var) && !boundVars.contains(var)) {
			if (rightExpr instanceof RuleVariable)
				if (!boundVars.contains(rightExpr))
					return false;
			boundVars.add(var);
			return true;
		}
		if (rightExpr.equals(var) && !boundVars.contains(var)) {
			if (leftExpr instanceof RuleVariable)
				if (!boundVars.contains(leftExpr))
					return false;
			boundVars.add(var);
			return true;
		}
		// TODO:Testen ob ?var = External() muster vorhanden ist, ?x gebunden
		// ist und External die gesuchte Variable enth�lt, die restlichen aber
		// gebunden sind, dann gebunden.
		if (leftExpr instanceof RuleVariable && boundVars.contains(leftExpr)
				&& rightExpr instanceof External) {
			Set<RuleVariable> extVars = rightExpr.getVariables();
			boolean found = false;
			boolean bound = true;
			for (final RuleVariable ruleVar : extVars)
				if (ruleVar.equals(var))
					found = true;
				else if (!boundVars.contains(ruleVar))
					bound = false;
			if (found && bound && rightExpr.isBound(var, boundVars)) {
				return true;
			}
		}
		if (rightExpr instanceof RuleVariable && boundVars.contains(rightExpr)
				&& leftExpr instanceof External) {
			Set<RuleVariable> extVars = leftExpr.getVariables();
			boolean found = false;
			boolean bound = true;
			for (final RuleVariable ruleVar : extVars)
				if (ruleVar.equals(var))
					found = true;
				else if (!boundVars.contains(ruleVar))
					bound = false;
			if (found && bound && leftExpr.isBound(var, boundVars)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>isPossibleAssignment.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPossibleAssignment() {
		return rightExpr instanceof RuleVariable || leftExpr instanceof RuleVariable;
	}

	/** {@inheritDoc} */
	public String toString(Prefix prefixInstance) {
		return leftExpr.toString(prefixInstance) + " = " + rightExpr.toString(prefixInstance);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Equality) {
			final Equality eq = (Equality) obj;
			return leftExpr.equals(eq.leftExpr)
			&& rightExpr.equals(eq.rightExpr);
		} else
			return false;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
