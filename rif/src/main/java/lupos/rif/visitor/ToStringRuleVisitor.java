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
package lupos.rif.visitor;

import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;
import lupos.rif.model.Conjunction;
import lupos.rif.model.Constant;
import lupos.rif.model.Disjunction;
import lupos.rif.model.Document;
import lupos.rif.model.Equality;
import lupos.rif.model.ExistExpression;
import lupos.rif.model.External;
import lupos.rif.model.Rule;
import lupos.rif.model.RuleList;
import lupos.rif.model.RulePredicate;
import lupos.rif.model.RuleVariable;

public class ToStringRuleVisitor implements IRuleVisitor<String, Object> {
	public String visit(Document obj, Object arg) throws RIFException {
		StringBuilder str = new StringBuilder();
		str.append("Document(").append("\n");
		for (Rule rule : obj.getRules())
			str.append(rule.accept(this, arg)).append("\n");
		return str.append(")").toString();
	}

	public String visit(Conjunction obj, Object arg) throws RIFException {
		StringBuilder str = new StringBuilder();
		str.append("And").append("(");
		for (IExpression expr : obj.exprs)
			str.append(expr.accept(this, arg)).append("\n");
		return str.append(")").toString();
	}

	public String visit(Disjunction obj, Object arg) throws RIFException {
		StringBuilder str = new StringBuilder();
		str.append("Or").append("(");
		for (IExpression expr : obj.exprs)
			str.append(expr.accept(this, arg)).append("\n");
		return str.append(")").toString();
	}

	public String visit(ExistExpression obj, Object arg) throws RIFException {
		return "Exists(" + obj.expr.accept(this, arg) + ")";
	}

	public String visit(Rule obj, Object arg) throws RIFException {
		StringBuilder str = new StringBuilder();
		str.append(obj.getHead().accept(this, arg));
		if (obj.isImplication())
			str.append(" :- ").append(obj.getBody().accept(this, arg));
		return str.toString();
	}

	public String visit(RulePredicate obj, Object arg) throws RIFException {
		StringBuilder str = new StringBuilder();
		str.append(obj.termName.accept(this, arg)).append("(");
		for (IExpression expr : obj.termParams)
			str.append(expr.accept(this, arg)).append(" ");
		return str.append(")").toString();
	}

	public String visit(External obj, Object arg) throws RIFException {
		StringBuilder str = new StringBuilder();
		str.append("External(").append(obj.termName.accept(this, arg))
		.append("(");
		for (IExpression expr : obj.termParams)
			str.append(expr.accept(this, arg)).append(" ");
		return str.append("))").toString();
	}

	public String visit(Equality obj, Object arg) throws RIFException {
		return obj.leftExpr.accept(this, arg) + " " + "=" + " "
		+ obj.rightExpr.accept(this, arg);
	}

	public String visit(Constant obj, Object arg) throws RIFException {
		return obj.getLiteral().toString();
	}

	public String visit(RuleVariable obj, Object arg) throws RIFException {
		return obj.getVariable().toString();
	}

	public String visit(RuleList obj, Object arg) throws RIFException {
		return "TODO";
	}
}
