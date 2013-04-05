/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
import lupos.datastructures.items.literal.Literal;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;
import lupos.rif.visitor.ReplaceVarsVisitor;

import com.google.common.collect.Multimap;

public class RuleList extends AbstractRuleNode implements IExpression, Item {
	private final ArrayList<IExpression> items = new ArrayList<IExpression>();
	public boolean isOpen = false;

	public void addItem(final IExpression expr) {
		if (!(expr instanceof AbstractExpressionContainer))
			items.add(expr);
		else
			throw new RIFException("And() and Or() forbidden in Lists!");
	}

	public ArrayList<IExpression> getItems() {
		return items;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof RuleList) {
			final RuleList list = (RuleList) obj;
			if (list.isOpen != isOpen)
				return false;
			if (list.getItems().size() != getItems().size())
				return false;
			for (int i = 0; i < getItems().size(); i++)
				if (!getItems().get(i).equals(list.getItems().get(i)))
					return false;
			return true;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public String getLabel() {
		return toString();
	}

	public <R, A> R accept(IRuleVisitor<R, A> visitor, A arg) throws RIFException {
		return visitor.visit(this, arg);
	}

	public boolean containsOnlyVariables() {
		for (IExpression expr : items)
			if (!expr.containsOnlyVariables())
				return false;
		return true;
	}

	public Set<RuleVariable> getVariables() {
		final Set<RuleVariable> vars = new HashSet<RuleVariable>();
		for (IExpression expr : items)
			vars.addAll(expr.getVariables());
		return vars;
	}

	public List<Uniterm> getPredicates() {
		final List<Uniterm> vars = new ArrayList<Uniterm>();
		for (IExpression expr : items)
			vars.addAll(expr.getPredicates());
		return vars;
	}

	public Object evaluate(Bindings binding) {
		return evaluate(binding, null);
	}

	public Object evaluate(Bindings binding, Object optionalResult) {
		return evaluate(binding, optionalResult, null);
	}

	public Object evaluate(Bindings binding, Object result, Multimap<IExpression, IExpression> equalities) {
		ReplaceVarsVisitor replace = new ReplaceVarsVisitor();
		replace.bindings = binding;
		return accept(replace, null);
	}

	public boolean isBound(RuleVariable var, Collection<RuleVariable> boundVars) {
		for (IExpression expr : items)
			if (expr instanceof RuleVariable && !boundVars.contains(var)) {
				boundVars.add(var);
				return true;
			} else if (expr.isBound(var, boundVars))
				return true;
		return false;
	}

	public boolean isPossibleAssignment() {
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder("List(");
		for (IExpression expr : items) {
			if (isOpen && items.indexOf(expr) == items.size() - 1)
				str.append("| ");
			str.append(expr.toString()).append(" ");
		}
		return str.append(")").toString();
	}

	public String toString(Prefix prefixInstance) {
		final StringBuilder str = new StringBuilder("List(");
		for (IExpression expr : items) {
			if (isOpen && items.indexOf(expr) == items.size() - 1)
				str.append("| ");
			str.append(expr.toString(prefixInstance)).append(" ");
		}
		return str.append(")").toString();

	}

	public boolean isVariable() {
		return false;
	}

	public Literal getLiteral(Bindings b) {
		return null;
	}

	public String getName() {
		return toString();
	}
}
