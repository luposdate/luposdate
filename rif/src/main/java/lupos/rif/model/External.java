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
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;
import lupos.rif.builtin.Argument;
import lupos.rif.builtin.RIFBuiltinFactory;

import com.google.common.collect.Multimap;

public class External extends Uniterm {
	private Object cachedResult;
	private boolean doBind = false;

	public External() {
		super();
	}

	public External(Uniterm from) {
		this();
		this.termName = from.termName;
		this.termName.setParent(this);
		this.termParams.addAll(from.termParams);
		for (IExpression expr : termParams)
			expr.setParent(this);
	}

	@Override
	public String getLabel() {
		return "External: " + super.getLabel();
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
		final URILiteral name = (URILiteral) ((Constant) termName).getLiteral();
		// Definiert?
		if (!RIFBuiltinFactory.isDefined(name))
			throw new RIFException("Builtin " + name.toString()
					+ " not defined!");
		// Bindung notwendig?
		// if (!doBind
		// && !(Sets
		// .intersection(getVariables(), binding.getVariableSet())
		// .size() != binding.getVariableSet().size()))
		// doBind = true;
		if (cachedResult == null) {
			final List<Item> params = new ArrayList<Item>();
			for (IExpression expr : termParams)
				params.add((Item) expr.evaluate(binding, null, equalities));
			final Argument arg = RIFBuiltinFactory.createArgument(binding,
					(Literal) optionalResult, params.toArray(new Item[] {}));
			if (getVariables().isEmpty())
				return (cachedResult = RIFBuiltinFactory.callBuiltin(name, arg));
			else
				return RIFBuiltinFactory.callBuiltin(name, arg);
		} else
			return cachedResult;
	}

	public boolean isBound(RuleVariable var, Collection<RuleVariable> boundVars) {
		if (getVariables().contains(var)
				&& !boundVars.contains(var)
				&& (RIFBuiltinFactory
						.canBind((URILiteral) ((Constant) termName)
								.getLiteral()))
								|| RIFBuiltinFactory
								.isIterable((URILiteral) ((Constant) termName)
										.getLiteral())) {
			boundVars.add(var);
			return true;
		} else
			return false;
	}

	public boolean isPossibleAssignment() {
		return false;
	}

	@Override
	public boolean equalsDataStructure(final Object triple) {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof External) {
			final External pred = (External) obj;
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
}
