
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
package lupos.rif.visitor;

import java.util.ArrayList;
import java.util.List;

import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.IRuleVisitor;
import lupos.rif.IVariableScope;
import lupos.rif.RIFException;
import lupos.rif.SimpleRuleVisitor;
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
public class SubstituteFunctionCallsVisitor implements
		IRuleVisitor<IRuleNode, Object> {
	private String aliasString = "ALIASVAR_";
	private int aliasCtr = 0;
	private IVariableScope currentVariableScope = null;

	/** {@inheritDoc} */
	public IRuleNode visit(Document obj, Object arg) throws RIFException {
		for (Rule rule : obj.getRules())
			if (rule.isImplication())
				rule.accept(this, arg);
		return obj;
	}

	/** {@inheritDoc} */
	public IRuleNode visit(Conjunction obj, Object arg) throws RIFException {
		// Conjunction koennen Rueckgabe sein, neu berechen
		List<IExpression> exprs = new ArrayList<IExpression>(obj.exprs);
		obj.exprs.clear();
		for (IExpression expr : exprs)
			// keine einzelnen Externals
			if (expr instanceof External)
				obj.addExpr(expr);
			else
				obj.addExpr((IExpression) expr.accept(this, obj));
		return obj;
	}

	/** {@inheritDoc} */
	public IRuleNode visit(Disjunction obj, Object arg) throws RIFException {
		// Conjunction koennen Rueckgabe sein, neu berechen
		List<IExpression> exprs = new ArrayList<IExpression>(obj.exprs);
		obj.exprs.clear();
		for (IExpression expr : exprs)
			// keine einzelnen Externals
			if (expr instanceof External)
				obj.addExpr(expr);
			else
				obj.addExpr((IExpression) expr.accept(this, obj));
		return obj;
	}

	/** {@inheritDoc} */
	public IRuleNode visit(ExistExpression obj, Object arg) throws RIFException {
		IVariableScope temp = currentVariableScope;
		currentVariableScope = obj;
		if (!(obj.expr instanceof External))
			obj.expr = (IExpression) obj.expr.accept(this, obj);
		currentVariableScope = temp;
		return obj;
	}

	/** {@inheritDoc} */
	public IRuleNode visit(Rule obj, Object arg) throws RIFException {
		currentVariableScope = obj;
		aliasCtr = 0;
		obj.setHead((IExpression) obj.getHead().accept(this, obj));

		// Equalities (mit Aliasvariablen) aus Head entfernen
		final List<Equality> equalities = new ArrayList<Equality>();
		final SimpleRuleVisitor extractEquality = new SimpleRuleVisitor() {
			@Override
			public IRuleNode visit(Equality obj, IRuleNode arg)
					throws RIFException {
				if (obj.isPossibleAssignment()
						&& obj.leftExpr instanceof RuleVariable
						&& ((RuleVariable) obj.leftExpr).getVariable()
								.getName().contains(aliasString)) {
					((Conjunction) obj.getParent()).exprs.remove(obj);
					equalities.add(obj);
				}
				return null;
			}
		};
		obj.getHead().accept(extractEquality, null);

		if (!(obj.getBody() instanceof External))
			obj.setBody((IExpression) obj.getBody().accept(this, obj));

		// dem Body hinzufuegen
		if (!equalities.isEmpty()) {
			Conjunction conj = new Conjunction();
			conj.setParent(obj);
			for (Equality eq : equalities)
				conj.addExpr(eq);
			conj.addExpr(obj.getBody());
			obj.setBody(conj);
		}
		return obj;
	}

	/** {@inheritDoc} */
	public IRuleNode visit(External obj, Object arg) throws RIFException {
		final Conjunction conjunction = new Conjunction();
		conjunction.setParent((IRuleNode) arg);

		RuleVariable alias = new RuleVariable(aliasString + aliasCtr++);
		conjunction.addExpr(alias);

		Equality comp = new Equality();
		comp.leftExpr = new RuleVariable(alias.getName());
		comp.leftExpr.setParent(comp);
		comp.rightExpr = obj;
		obj.setParent(comp);
		conjunction.addExpr(comp);

		currentVariableScope.addVariable(new RuleVariable(alias.getName()));
		return conjunction;
	}

	/** {@inheritDoc} */
	public IRuleNode visit(RulePredicate obj, Object arg) throws RIFException {
		Conjunction conjunction = null;
		// Funktionsaufrufe mit Variable ersetzen
		ArrayList<IExpression> params = new ArrayList<IExpression>(
				obj.termParams);
		// Relationen koennen keine Externals sein.
		for (IExpression expr : obj.termParams) {
			final IRuleNode result = expr.accept(this, obj);
			if (result instanceof Conjunction) {
				conjunction = conjunction == null ? new Conjunction()
						: conjunction;
				for (IExpression item : ((Conjunction) result).exprs)
					if (item instanceof Equality)
						conjunction.addExpr(item);
					else
						params.set(params.indexOf(expr), item);
			}
		}
		obj.termParams = params;
		if (conjunction != null)
			conjunction.addExpr(obj);
		return conjunction == null ? obj : conjunction;
	}

	/** {@inheritDoc} */
	public IRuleNode visit(RuleList obj, Object arg) throws RIFException {
		Conjunction conjunction = null;
		ArrayList<IExpression> items = new ArrayList<IExpression>(
				obj.getItems());
		for (IExpression expr : obj.getItems()) {
			final IRuleNode result = expr.accept(this, obj);
			if (result instanceof Conjunction) {
				conjunction = conjunction == null ? new Conjunction()
						: conjunction;
				for (IExpression item : ((Conjunction) result).exprs)
					if (item instanceof Equality)
						conjunction.addExpr(item);
					else
						items.set(items.indexOf(expr), item);
			}
		}
		obj.getItems().clear();
		obj.getItems().addAll(items);
		if (conjunction != null)
			conjunction.addExpr(obj);
		return conjunction != null ? conjunction : obj;
	}

	/** {@inheritDoc} */
	public IRuleNode visit(Equality obj, Object arg) throws RIFException {
		return obj;
	}

	/** {@inheritDoc} */
	public IRuleNode visit(Constant obj, Object arg) throws RIFException {
		return obj;
	}

	/** {@inheritDoc} */
	public IRuleNode visit(RuleVariable obj, Object arg) throws RIFException {
		return obj;
	}
}
