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

import java.util.Collection;

import lupos.datastructures.bindings.Bindings;
import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.IVariableScope;
import lupos.rif.RIFException;
import lupos.rif.builtin.BooleanLiteral;

import com.google.common.collect.Multimap;

public class Conjunction extends AbstractExpressionContainer {
	public void addExpr(IExpression expr) {
		if (expr instanceof Conjunction)
			for (IExpression obj : ((AbstractExpressionContainer) expr).exprs)
				addExpr(obj);
		else if (expr != null) {
			if (!exprs.contains(expr)) {
				expr.setParent(this);
				exprs.add(expr);
			}
		}
	}

	public <R, A> R accept(IRuleVisitor<R, A> visitor, A arg) throws RIFException {
		return visitor.visit(this, arg);
	}

	public String getLabel() {
		return "And";
	}

	public Object evaluate(Bindings binding) {
		return evaluate(binding, null);
	}

	public Object evaluate(Bindings binding, Object optionalResult) {
		return evaluate(binding, optionalResult, null);
	}

	public Object evaluate(Bindings binding, Object optionalResult, Multimap<IExpression, IExpression> equalities) {
		for (IExpression expr : exprs) {
			Object result = expr.evaluate(binding);
			if (result instanceof BooleanLiteral) {
				if (!((BooleanLiteral) result).value)
					return result;
			} else
				throw new RIFException(
				"In Conjunction, only Boolean Resulttypes are allowed!");
		}
		return BooleanLiteral.create(true);
	}

	public boolean isBound(RuleVariable var, Collection<RuleVariable> boundVars) {
		// mind. ein element darf kein scope sein und die variable muss gebunden
		// sein
		for (IExpression expr : exprs) {
			if (!(expr instanceof IVariableScope)
					&& expr.isBound(var, boundVars))
				return true;
		}
		return false;
	}

	public boolean isPossibleAssignment() {
		return false;
	}
}
