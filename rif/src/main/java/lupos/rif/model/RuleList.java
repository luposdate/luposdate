
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
import lupos.rif.datatypes.ListLiteral;
import lupos.rif.visitor.ReplaceVarsVisitor;

import com.google.common.collect.Multimap;
public class RuleList extends AbstractRuleNode implements IExpression, Item {
	private final ArrayList<IExpression> items = new ArrayList<IExpression>();
	public boolean isOpen = false;

	/**
	 * <p>addItem.</p>
	 *
	 * @param expr a {@link lupos.rif.IExpression} object.
	 */
	public void addItem(final IExpression expr) {
		if (!(expr instanceof AbstractExpressionContainer)) {
			this.items.add(expr);
		} else {
			throw new RIFException("And() and Or() forbidden in Lists!");
		}
	}

	/**
	 * <p>Getter for the field <code>items</code>.</p>
	 *
	 * @return a {@link java.util.ArrayList} object.
	 */
	public ArrayList<IExpression> getItems() {
		return this.items;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (obj != null && obj instanceof RuleList) {
			final RuleList list = (RuleList) obj;
			if (list.isOpen != this.isOpen) {
				return false;
			}
			if (list.getItems().size() != this.getItems().size()) {
				return false;
			}
			for (int i = 0; i < this.getItems().size(); i++) {
				if (!this.getItems().get(i).equals(list.getItems().get(i))) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public String getLabel() {
		return this.toString();
	}

	/** {@inheritDoc} */
	@Override
	public <R, A> R accept(final IRuleVisitor<R, A> visitor, final A arg) throws RIFException {
		return visitor.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsOnlyVariables() {
		for (final IExpression expr : this.items) {
			if (!expr.containsOnlyVariables()) {
				return false;
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Set<RuleVariable> getVariables() {
		final Set<RuleVariable> vars = new HashSet<RuleVariable>();
		for (final IExpression expr : this.items) {
			vars.addAll(expr.getVariables());
		}
		return vars;
	}

	/** {@inheritDoc} */
	@Override
	public List<Uniterm> getPredicates() {
		final List<Uniterm> vars = new ArrayList<Uniterm>();
		for (final IExpression expr : this.items) {
			vars.addAll(expr.getPredicates());
		}
		return vars;
	}

	/** {@inheritDoc} */
	@Override
	public Object evaluate(final Bindings binding) {
		return this.evaluate(binding, null);
	}

	/** {@inheritDoc} */
	@Override
	public Object evaluate(final Bindings binding, final Object optionalResult) {
		return this.evaluate(binding, optionalResult, null);
	}

	/** {@inheritDoc} */
	@Override
	public Object evaluate(final Bindings binding, final Object result, final Multimap<IExpression, IExpression> equalities) {
		final ReplaceVarsVisitor replace = new ReplaceVarsVisitor();
		replace.bindings = binding;
		return this.accept(replace, null);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isBound(final RuleVariable var, final Collection<RuleVariable> boundVars) {
		for (final IExpression expr : this.items) {
			if (expr instanceof RuleVariable && !boundVars.contains(var)) {
				boundVars.add(var);
				return true;
			} else if (expr.isBound(var, boundVars)) {
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isPossibleAssignment() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder("List(");
		for (final IExpression expr : this.items) {
			if (this.isOpen && this.items.indexOf(expr) == this.items.size() - 1) {
				str.append("| ");
			}
			str.append(expr.toString()).append(" ");
		}
		return str.append(")").toString();
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final Prefix prefixInstance) {
		final StringBuilder str = new StringBuilder("List(");
		for (final IExpression expr : this.items) {
			if (this.isOpen && this.items.indexOf(expr) == this.items.size() - 1) {
				str.append("| ");
			}
			str.append(expr.toString(prefixInstance)).append(" ");
		}
		return str.append(")").toString();

	}

	/** {@inheritDoc} */
	@Override
	public boolean isVariable() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Literal getLiteral(final Bindings b) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return this.toString();
	}

	/** {@inheritDoc} */
	@Override
	public RuleList clone(){
		final RuleList rl2 = new RuleList();
		rl2.getItems().addAll(this.items);
		rl2.isOpen = this.isOpen;
		return rl2;
	}

	/**
	 * <p>createListLiteral.</p>
	 *
	 * @return a {@link lupos.rif.datatypes.ListLiteral} object.
	 */
	public ListLiteral createListLiteral(){
		final ArrayList<Literal> al = new ArrayList<Literal>(this.items.size());
		for(final IExpression ie: this.items){
			final Object o = ie.evaluate(null);
			if(o instanceof RuleList){
				al.add(((RuleList)o).createListLiteral());
			} else if(o instanceof Constant) {
				al.add(((Constant)o).getLiteral());
			} else {
				al.add((Literal)o);
			}
		}
		return new ListLiteral(al);
	}
}
