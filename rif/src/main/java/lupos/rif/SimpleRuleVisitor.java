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
package lupos.rif;

import java.util.ArrayList;

import lupos.rif.model.Conjunction;
import lupos.rif.model.Constant;
import lupos.rif.model.Disjunction;
import lupos.rif.model.Document;
import lupos.rif.model.Equality;
import lupos.rif.model.ExistExpression;
import lupos.rif.model.External;
import lupos.rif.model.Rule;
import lupos.rif.model.RuleList;
import lupos.rif.model.RuleVariable;
import lupos.rif.model.RulePredicate;

public abstract class SimpleRuleVisitor implements
		IRuleVisitor<IRuleNode, IRuleNode> {

	public IRuleNode visit(Document obj, IRuleNode arg) throws RIFException {
		for (Rule rule : obj.getRules())
			rule.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(Rule obj, IRuleNode arg) throws RIFException {
		obj.getHead().accept(this, obj);
		obj.getBody().accept(this, obj);
		return obj;
	}

	public IRuleNode visit(ExistExpression obj, IRuleNode arg)
			throws RIFException {
		obj.expr.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(Conjunction obj, IRuleNode arg) throws RIFException {
		for (IExpression expr : new ArrayList<IExpression>(obj.exprs))
			expr.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(Disjunction obj, IRuleNode arg) throws RIFException {
		for (IExpression expr : new ArrayList<IExpression>(obj.exprs))
			expr.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(RulePredicate obj, IRuleNode arg)
			throws RIFException {
		obj.termName.accept(this, obj);
		for (IExpression params : obj.termParams)
			params.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(Equality obj, IRuleNode arg) throws RIFException {
		obj.leftExpr.accept(this, obj);
		obj.rightExpr.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(External obj, IRuleNode arg) throws RIFException {
		obj.termName.accept(this, obj);
		for (IExpression params : obj.termParams)
			params.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(RuleList obj, IRuleNode arg) throws RIFException {
		for (IExpression expr : obj.getItems())
			expr.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(RuleVariable obj, IRuleNode arg) throws RIFException {
		return obj;
	}

	public IRuleNode visit(Constant obj, IRuleNode arg) throws RIFException {
		return obj;
	}
}
