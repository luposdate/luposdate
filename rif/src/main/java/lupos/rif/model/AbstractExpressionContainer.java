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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.IRuleNode;

public abstract class AbstractExpressionContainer extends AbstractRuleNode
implements IExpression {

	public List<IExpression> exprs = new ArrayList<IExpression>();

	public AbstractExpressionContainer() {
		super();
	}

	public abstract void addExpr(IExpression expr);

	public boolean isEmpty() {
		return exprs.isEmpty();
	}

	public boolean containsOnlyVariables() {
		for (IExpression expr : exprs)
			if (!expr.containsOnlyVariables())
				return false;
		return true;
	}

	public Set<RuleVariable> getVariables() {
		Set<RuleVariable> variables = new HashSet<RuleVariable>();
		for (IExpression expr : exprs)
			variables.addAll(expr.getVariables());
		return variables;
	}

	public List<Uniterm> getPredicates() {
		List<Uniterm> terms = new ArrayList<Uniterm>();
		for (IExpression expr : exprs)
			terms.addAll(expr.getPredicates());
		return terms;
	}

	public List<IRuleNode> getChildren() {
		return new ArrayList<IRuleNode>(exprs);
	}
	
	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder(getLabel()).append("(");
		for(final IExpression expr : exprs)
			str.append(expr.toString()).append(" ");
		return str.append(")").toString();
	}
	
	@Override
	public String toString(final Prefix prefixInstance) {
		final StringBuilder str = new StringBuilder(getLabel()).append("(");
		for(final IExpression expr : exprs)
			str.append(expr.toString(prefixInstance)).append(" ");
		return str.append(")").toString();
	}
}