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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;
import lupos.rif.datatypes.ListLiteral;
import lupos.rif.datatypes.Predicate;

import com.google.common.collect.Multimap;
public class RulePredicate extends Uniterm {
	private boolean isRecursive = false;
	private final boolean triple;

	/**
	 * <p>Constructor for RulePredicate.</p>
	 *
	 * @param triple a boolean.
	 */
	public RulePredicate(final boolean triple) {
		super();
		this.triple=triple;
	}

	/**
	 * <p>Constructor for RulePredicate.</p>
	 *
	 * @param subject a {@link lupos.rif.IExpression} object.
	 * @param predicate a {@link lupos.rif.IExpression} object.
	 * @param object a {@link lupos.rif.IExpression} object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public RulePredicate(final IExpression subject, final IExpression predicate,
			final IExpression object) throws RIFException {
		this(true);
		this.termParams.add(subject);
		this.termName = predicate;
		this.termParams.add(object);
	}

	/**
	 * <p>Constructor for RulePredicate.</p>
	 *
	 * @param predName a {@link lupos.rif.IExpression} object.
	 * @param predParams a {@link lupos.rif.IExpression} object.
	 */
	public RulePredicate(final IExpression predName, final IExpression... predParams) {
		this(false);
		this.termName = predName;
		this.termParams = Arrays.asList(predParams);
	}

	/**
	 * <p>isTriple.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isTriple() {
		return this.triple;
		// return termParams.size() == 2;
	}

	/** {@inheritDoc} */
	@Override
	public <R, A> R accept(final IRuleVisitor<R, A> visitor, final A arg) throws RIFException {
		return visitor.visit(this, arg);
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
	public Object evaluate(final Bindings binding, final Object optionalResult, final Multimap<IExpression, IExpression> equalities) {
		if (equalities != null) {
			// RulePredicate evluieren und neubauen
			final RulePredicate pred = new RulePredicate(this.triple);
			pred.termName = new Constant((Literal) this.termName.evaluate(binding),
					pred);
			for (final IExpression expr : this.termParams) {
				final Object obj = expr.evaluate(binding);
				if (obj instanceof Variable) {
					throw new RIFException("Unbound Variable " + obj
							+ " while evaluating " + this.toString());
				} else if (obj instanceof Literal) {
					pred.termParams.add(new Constant((Literal) obj, pred));
				} else {
					pred.termParams.add((IExpression) obj);
				}
			}
			if (equalities.containsKey(pred)) {
				// Immer nur den ersten eintrag nehmen, der ein Literal
				// zur�ckgibt
				for (final IExpression expr : equalities.get(pred)) {
					if (expr instanceof Constant) {
						return ((Constant) expr).getLiteral();
					}
				}
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isBound(final RuleVariable var, final Collection<RuleVariable> boundVars) {
		boundVars.addAll(this.getVariables());
		if (this.getVariables().contains(var)) {
			return true;
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isPossibleAssignment() {
		return false;
	}

	/**
	 * <p>setRecursive.</p>
	 *
	 * @param isRecursive a boolean.
	 */
	public void setRecursive(final boolean isRecursive) {
		this.isRecursive = isRecursive;
	}

	/**
	 * <p>isRecursive.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isRecursive() {
		return this.isRecursive;
	}

	/**
	 * <p>toDataStructure.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	public Object toDataStructure() {
		if (this.isTriple()){
			final Literal subject = (this.termParams.get(0) instanceof Constant)?
					((Constant) this.termParams.get(0)).getLiteral():
					(Literal)((External) this.termParams.get(0)).evaluate(new BindingsMap());

			final Literal predicate = (this.termName instanceof Constant)?
					((Constant) this.termName).getLiteral():
					(Literal)((External) this.termName).evaluate(new BindingsMap());

			final Literal object = (this.termParams.get(1) instanceof Constant)?
					((Constant) this.termParams.get(1)).getLiteral():
					(Literal)((External) this.termParams.get(1)).evaluate(new BindingsMap());

			return new Triple(subject, predicate, object);
		} else {
			final Predicate pred = new Predicate();
			pred.setName(((Constant) this.termName).getLiteral());
			for (final IExpression expr : this.termParams) {
				if(expr instanceof RuleList){
					final List<IExpression> lie = ((RuleList) expr).getItems();
					final List<Literal> ll = new ArrayList<Literal>(lie.size());
					for(final IExpression ie: lie){
						final Object res = ie.evaluate(null);
						if(res instanceof RuleList){
							ll.add(((RuleList) res).createListLiteral());
						} else {
							ll.add((Literal) res);
						}
					}
					pred.getParameters().add(new ListLiteral(ll));
				} else {
					pred.getParameters().add(	(expr instanceof Constant)?
												(Literal)((Constant) expr).getLiteral():
												(Literal)((External) expr).evaluate(new BindingsMap()));
				}
			}
			return pred;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean equalsDataStructure(final Object obj) {
		if (this.isTriple() && obj instanceof Triple) {
			return this.toDataStructure().equals(obj);
		} else if (!this.isTriple() && obj instanceof Predicate) {
			return this.toDataStructure().equals(obj);
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (obj != null && obj instanceof RulePredicate) {
			final RulePredicate pred = (RulePredicate) obj;
			if (!pred.termName.equals(this.termName)) {
				return false;
			}
			if (pred.termParams.size() != this.termParams.size()) {
				return false;
			}
			for (int i = 0; i < this.termParams.size(); i++) {
				if (!this.termParams.get(i).equals(pred.termParams.get(i))) {
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
		if(this.isTriple()){
			final StringBuffer str = new StringBuffer();
			str.append(this.termParams.get(0).toString()).append("[");
			str.append(this.termName.toString()).append("->");
			str.append(this.termParams.get(1).toString()).append("]");
			return str.toString();
		} else {
			return super.getLabel();
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final Prefix prefixInstance) {
		if(this.isTriple()){
			final StringBuffer str = new StringBuffer();
			str.append(this.termParams.get(0).toString(prefixInstance)).append("[");
			str.append(this.termName.toString(prefixInstance)).append("->");
			str.append(this.termParams.get(1).toString(prefixInstance)).append("]");
			return str.toString();
		} else {
			return super.toString(prefixInstance);
		}
	}
}
