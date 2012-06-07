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
package lupos.rif.model;

import java.util.Arrays;
import java.util.Collection;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;
import lupos.rif.datatypes.Predicate;

import com.google.common.collect.Multimap;

public class RulePredicate extends Uniterm {
	private boolean isRecursive = false;
	private final boolean triple;
	
	public RulePredicate(final boolean triple) {
		super();
		this.triple=triple;
	}

	public RulePredicate(IExpression subject, IExpression predicate,
			IExpression object) throws RIFException {
		this(true);
		termParams.add(subject);
		termName = predicate;
		termParams.add(object);
	}

	public RulePredicate(IExpression predName, IExpression... predParams) {
		this(false);
		termName = predName;
		termParams = Arrays.asList(predParams);
	}

	public boolean isTriple() {
		return triple;
		// return termParams.size() == 2;
	}

	public <R, A> R accept(IRuleVisitor<R, A> visitor, A arg) throws RIFException {
		return visitor.visit(this, arg);
	}

	public Object evaluate(Bindings binding) {
		return evaluate(binding, null);
	}

	public Object evaluate(Bindings binding, Object optionalResult) {
		return evaluate(binding, optionalResult, null);
	}

	public Object evaluate(Bindings binding, Object optionalResult, Multimap<IExpression, IExpression> equalities) {
		if (equalities != null) {
			// RulePredicate evluieren und neubauen
			final RulePredicate pred = new RulePredicate(this.triple);
			pred.termName = new Constant((Literal) termName.evaluate(binding),
					pred);
			for (final IExpression expr : termParams) {
				final Object obj = expr.evaluate(binding);
				if (obj instanceof Variable)
					throw new RIFException("Unbound Variable " + obj
							+ " while evaluating " + toString());
				else if (obj instanceof Literal)
					pred.termParams.add(new Constant((Literal) obj, pred));
				else
					pred.termParams.add((IExpression) obj);
			}
			if (equalities.containsKey(pred))
				// Immer nur den ersten eintrag nehmen, der ein Literal
				// zur�ckgibt
				for (final IExpression expr : equalities.get(pred)) {
					if (expr instanceof Constant)
						return ((Constant) expr).getLiteral();
				}
		}
		return null;
	}

	public boolean isBound(RuleVariable var, Collection<RuleVariable> boundVars) {
		boundVars.addAll(getVariables());
		if (getVariables().contains(var)) {
			return true;
		} else
			return false;
	}

	public boolean isPossibleAssignment() {
		return false;
	}

	public void setRecursive(boolean isRecursive) {
		this.isRecursive = isRecursive;
	}

	public boolean isRecursive() {
		return this.isRecursive;
	}

	public Object toDataStructure() {
		if (isTriple()){
			Literal subject = (this.termParams.get(0) instanceof Constant)?
					((Constant) this.termParams.get(0)).getLiteral():
					(Literal)((External) this.termParams.get(0)).evaluate(Bindings.createNewInstance());

			Literal predicate = (this.termName instanceof Constant)?
					((Constant) this.termName).getLiteral():
					(Literal)((External) this.termName).evaluate(Bindings.createNewInstance());

			Literal object = (this.termParams.get(1) instanceof Constant)?
					((Constant) this.termParams.get(1)).getLiteral():
					(Literal)((External) this.termParams.get(1)).evaluate(Bindings.createNewInstance());

			return new Triple(subject, predicate, object);
		} else {
			final Predicate pred = new Predicate();
			pred.setName(((Constant) this.termName).getLiteral());
			for (IExpression expr : this.termParams) {
				pred.getParameters().add(	(expr instanceof Constant)?
											((Constant) expr).getLiteral():
											(Literal)((External) expr).evaluate(Bindings.createNewInstance()));
			}
			return pred;
		}
	}

	@Override
	public boolean equalsDataStructure(final Object obj) {
		if (isTriple() && obj instanceof Triple) {
			return toDataStructure().equals(obj);
		} else if (!isTriple() && obj instanceof Predicate)
			return toDataStructure().equals(obj);
		else
			return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof RulePredicate) {
			final RulePredicate pred = (RulePredicate) obj;
			if (!pred.termName.equals(termName))
				return false;
			if (pred.termParams.size() != termParams.size())
				return false;
			for (int i = 0; i < termParams.size(); i++) {
				if (!termParams.get(i).equals(pred.termParams.get(i)))
					return false;
			}
			return true;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	public String getLabel() {
		if(isTriple()){
			final StringBuffer str = new StringBuffer();
			str.append(termParams.get(0).toString()).append("[");
			str.append(termName.toString()).append("->");
			str.append(termParams.get(1).toString()).append("]");
			return str.toString();
		} else return super.getLabel();
	}

	public String toString(Prefix prefixInstance) {
		if(isTriple()){
			final StringBuffer str = new StringBuffer();
			str.append(termParams.get(0).toString(prefixInstance)).append("[");
			str.append(termName.toString(prefixInstance)).append("->");
			str.append(termParams.get(1).toString(prefixInstance)).append("]");
			return str.toString();
		} else return super.toString(prefixInstance);
	}
}
