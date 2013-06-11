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
package lupos.rif.operator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.engine.operators.singleinput.ExpressionEvaluation.Helper;
import lupos.misc.util.ImmutableIterator;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.RIFException;
import lupos.rif.model.Conjunction;
import lupos.rif.model.Equality;
import lupos.rif.model.RuleVariable;

import com.google.common.collect.Multimap;

public class RuleFilter extends SingleInputOperator {

	protected IExpression expression;
	protected final Multimap<IExpression, IExpression> equalityMap;
	protected final Set<Variable> assignVariables = new HashSet<Variable>();
	protected int cardinality = -1;

	public RuleFilter(final IExpression expression,
			final Multimap<IExpression, IExpression> eqMap) {
		super();
		this.setExpression(expression);
		this.equalityMap = eqMap;
	}

	public RuleFilter() {
		this.expression = null;
		this.equalityMap = null;
	}

	public void setExpression(IExpression expression) {
		if (expression instanceof Conjunction) {
			if (((Conjunction) expression).exprs.size() == 1) {
				expression = ((Conjunction) expression).exprs.get(0);
			}
		}
		this.expression = expression;
	}

	public IExpression getExpression() {
		return this.expression;
	}

	public boolean equalFilterExpression(final RuleFilter r) {
		return (this.expression.equals(r.expression));
	}

	public boolean isAssignment() {
		return !this.assignVariables.isEmpty();
	}

	public Set<Variable> getAssignedVariables() {
		return this.assignVariables;
	}

	public Set<Variable> getVariablesInExpression() {
		final Set<Variable> vars = new HashSet<Variable>();
		for (final RuleVariable var : this.expression.getVariables()) {
			vars.add(var.getVariable());
		}
		return vars;
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final Iterator<Bindings> resultIterator = new ImmutableIterator<Bindings>() {
			final Iterator<Bindings> bindIt = bindings.oneTimeIterator();
			int number = 0;
			Bindings next = this.computeNext();

			@Override
			public boolean hasNext() {
				return (this.next != null);
			}

			@Override
			public Bindings next() {
				final Bindings zNext = this.next;
				this.next = this.computeNext();
				return zNext;
			}

			private Bindings computeNext() {
				while (this.bindIt.hasNext()) {
					final Bindings bind = this.bindIt.next();
					if (bind != null) {
						try{
							final boolean result = RuleFilter.this.filter(bind);
							if (result) {
								this.number++;
								RuleFilter.this.onAccepted(bind);
								return bind;
							} else {
								RuleFilter.this.onFilteredOut(bind);
							}
						} catch(final Exception e){
							RuleFilter.this.onFilteredOut(bind);
						} catch(final Error e){
							RuleFilter.this.onFilteredOut(bind);
						}
					}
				}
				RuleFilter.this.cardinality = this.number;
				return null;
			}
		};

		if (resultIterator.hasNext()) {
			return QueryResult.createInstance(resultIterator);
		} else {
			return null;
		}
	}

	protected boolean filter(final Bindings bind) {
		try {
			return Helper.booleanEffectiveValue(this.expression.evaluate(bind, null, this.equalityMap));
		} catch (final TypeErrorException e) {
			throw new RIFException(e.getMessage());
		}
	}

	protected void onFilteredOut(final Bindings bind) {
	}

	protected void onAccepted(final Bindings bind) {
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		this.unionVariables = new HashSet<Variable>(msg.getVariables());
		for (final RuleVariable var : this.expression.getVariables()) {
			if (!this.unionVariables.contains(var.getVariable())) {
				this.unionVariables.add(var.getVariable());
				this.assignVariables.add(var.getVariable());
			}
		}
		this.intersectionVariables = new HashSet<Variable>(this.unionVariables);
		result.getVariables().addAll(this.intersectionVariables);
		return result;
	}

	@Override
	public String toString() {
		String result = "Rulefilter\n" + this.expression.toString();

		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}

		return result;
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		String result = "Rulefilter\n" + this.expression.toString(prefixInstance);

		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}

		return result;
	}

	public Multimap<IExpression, IExpression> getEqualities() {
		return this.equalityMap;
	}

	// @Override
	// public boolean equals(Object arg0) {
	// if (arg0 instanceof RuleFilter)
	// return ((RuleFilter) arg0).getExpression().equals(getExpression());
	// return false;
	// }
	//
	// @Override
	// public int hashCode() {
	// return expression.hashCode();
	// }

	@Override
	public boolean remainsSortedData(final Collection<Variable> sortCriterium){
		if (this.getExpression() instanceof Equality) {
			if (((Equality) this.getExpression()).leftExpr instanceof RuleVariable) {
				final Variable assignVar = ((RuleVariable) ((Equality) this
						.getExpression()).leftExpr).getVariable();
				if (sortCriterium.contains(assignVar)) {
					return false;
				}
			}
		}
		return true;
	}
}
