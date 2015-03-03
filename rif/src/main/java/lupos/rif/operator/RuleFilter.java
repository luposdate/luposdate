
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
package lupos.rif.operator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.engine.operators.singleinput.filter.expressionevaluation.Helper;
import lupos.misc.Tuple;
import lupos.misc.util.ImmutableIterator;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.RIFException;
import lupos.rif.model.Conjunction;
import lupos.rif.model.Equality;
import lupos.rif.model.RuleList;
import lupos.rif.model.RuleVariable;

import com.google.common.collect.Multimap;
public class RuleFilter extends SingleInputOperator {

	protected IExpression expression;
	protected final Multimap<IExpression, IExpression> equalityMap;
	protected final Set<Variable> assignVariables = new HashSet<Variable>();
	protected int cardinality = -1;

	/**
	 * <p>Constructor for RuleFilter.</p>
	 *
	 * @param expression a {@link lupos.rif.IExpression} object.
	 * @param eqMap a {@link com.google.common.collect.Multimap} object.
	 */
	public RuleFilter(final IExpression expression,
			final Multimap<IExpression, IExpression> eqMap) {
		super();
		this.setExpression(expression);
		this.equalityMap = eqMap;
	}

	/**
	 * <p>Constructor for RuleFilter.</p>
	 */
	public RuleFilter() {
		this.expression = null;
		this.equalityMap = null;
	}

	/**
	 * <p>Setter for the field <code>expression</code>.</p>
	 *
	 * @param expression a {@link lupos.rif.IExpression} object.
	 */
	public void setExpression(IExpression expression) {
		if (expression instanceof Conjunction) {
			if (((Conjunction) expression).exprs.size() == 1) {
				expression = ((Conjunction) expression).exprs.get(0);
			}
		}
		this.expression = expression;
	}

	/**
	 * <p>Getter for the field <code>expression</code>.</p>
	 *
	 * @return a {@link lupos.rif.IExpression} object.
	 */
	public IExpression getExpression() {
		return this.expression;
	}

	/**
	 * <p>equalFilterExpression.</p>
	 *
	 * @param r a {@link lupos.rif.operator.RuleFilter} object.
	 * @return a boolean.
	 */
	public boolean equalFilterExpression(final RuleFilter r) {
		return (this.expression.equals(r.expression));
	}

	/**
	 * <p>isAssignment.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isAssignment() {
		return !this.assignVariables.isEmpty();
	}

	/**
	 * <p>getAssignedVariables.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Variable> getAssignedVariables() {
		return this.assignVariables;
	}

	/**
	 * <p>getVariablesInExpression.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Variable> getVariablesInExpression() {
		final Set<Variable> vars = new HashSet<Variable>();
		for (final RuleVariable var : this.expression.getVariables()) {
			vars.add(var.getVariable());
		}
		return vars;
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final Iterator<Bindings> resultIterator = new ImmutableIterator<Bindings>() {
			// if several values are bound to a variable in an external function!
			Iterator<Bindings> iteratorBindings = null;

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
				if(this.iteratorBindings!=null && this.iteratorBindings.hasNext()){
					return this.iteratorBindings.next();
				}
				while (this.bindIt.hasNext()) {
					final Bindings bind = this.bindIt.next();
					if (bind != null) {
						try{
							final Object result = RuleFilter.this.expression.evaluate(bind, null, RuleFilter.this.equalityMap);
							if(result instanceof Tuple){
								// deal with bindable externals!
								@SuppressWarnings("unchecked")
								final Tuple<Variable, RuleList> resultTuple = (Tuple<Variable, RuleList>) result;
								this.iteratorBindings = new ImmutableIterator<Bindings>(){

									final Iterator<IExpression> iteratorRuleList = resultTuple.getSecond().getItems().iterator();

									@Override
									public boolean hasNext() {
										return this.iteratorRuleList.hasNext();
									}

									@Override
									public Bindings next() {
										if(this.hasNext()){
											final Bindings result = bind.clone();
											result.add(resultTuple.getFirst(), (Literal) this.iteratorRuleList.next().evaluate(result));
											number++;
											return result;
										} else {
											return null;
										}
									}
								};
								return this.computeNext();
							} else {
								final boolean booleanResult = RuleFilter.this.filter(bind, result);
								if (booleanResult) {
									this.number++;
									RuleFilter.this.onAccepted(bind);
									return bind;
								} else {
									RuleFilter.this.onFilteredOut(bind);
								}
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

	/**
	 * This method will be overridden by EqualityFilter!
	 *
	 * @param bind the currently investigated binding
	 * @param result the object of which this method determines the boolean effective value
	 * @return the boolean effective value of result
	 */
	protected boolean filter(final Bindings bind, final Object result){
		try {
			return Helper.booleanEffectiveValue(result);
		} catch (final TypeErrorException e) {
			throw new RIFException(e.getMessage());
		}
	}

	/**
	 * <p>onFilteredOut.</p>
	 *
	 * @param bind a {@link lupos.datastructures.bindings.Bindings} object.
	 */
	protected void onFilteredOut(final Bindings bind) {
	}

	/**
	 * <p>onAccepted.</p>
	 *
	 * @param bind a {@link lupos.datastructures.bindings.Bindings} object.
	 */
	protected void onAccepted(final Bindings bind) {
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String result = "Rulefilter\n" + this.expression.toString();

		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final Prefix prefixInstance) {
		String result = "Rulefilter\n" + this.expression.toString(prefixInstance);

		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}

		return result;
	}

	/**
	 * <p>getEqualities.</p>
	 *
	 * @return a {@link com.google.common.collect.Multimap} object.
	 */
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

	/** {@inheritDoc} */
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
