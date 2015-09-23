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
package lupos.rif.visitor;

import java.util.ArrayList;
import java.util.List;

import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
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
public class NormalizeRuleVisitor implements IRuleVisitor<IRuleNode, IRuleNode> {

	public boolean generateDNF = true;

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Conjunction} object.
	 * @param arg a {@link lupos.rif.IRuleNode} object.
	 * @return a {@link lupos.rif.IRuleNode} object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public IRuleNode visit(Conjunction obj, IRuleNode arg) throws RIFException {
		if (obj.exprs.size() == 1)
			return obj.exprs.get(0).accept(this, arg);
		// Umwandlung in Disjunktive Normalform
		if (this.generateDNF) {
			// 1. Oder nach hinten verschieben
			// nur wenn ein OR enthalten ist anwendbar
			int orCtr = 0;
			for (IExpression expr : new ArrayList<IExpression>(obj.exprs))
				if (expr instanceof Disjunction) {
					obj.exprs.remove(expr);
					obj.exprs.add(expr);
					orCtr++;
				}
			// 2. DNF von Links aufbauen
			if (orCtr == 1) {
				IExpression dnf = applyLeftDistributiveLaw(obj);
				if (dnf != null)
					return dnf;
			}
		}
		//
		List<IExpression> newExprs = new ArrayList<IExpression>();
		for (IExpression expr : obj.exprs) {
			IExpression temp = (IExpression) expr.accept(this, obj);
			if (temp instanceof Conjunction)
				for (IExpression innerExpr : ((Conjunction) temp).exprs) {
					innerExpr.setParent(obj);
					newExprs.add(innerExpr);
				}
			else
				newExprs.add(temp);
		}
		obj.exprs = newExprs;
		return obj;
	}

	private IExpression applyLeftDistributiveLaw(Conjunction andFormula) {
		List<IExpression> newConjuncts = new ArrayList<IExpression>();
		List<IExpression> conjuncts = new ArrayList<IExpression>(
				andFormula.exprs);

		boolean hasChanged = false;

		// And(F1 ... Fn)
		for (int i = 0; i < conjuncts.size(); i++) {
			int j = i + 1;
			IExpression conjunct = conjuncts.get(i);

			if (j < conjuncts.size()) {
				// And(F1 F2 ... Fi Or(G1, ..., Gm) Fi+2 Fi+3 ... Fn)
				if (conjuncts.get(j) instanceof Disjunction) {
					Disjunction orConjunct = (Disjunction) conjuncts.get(j);

					List<IExpression> andFormulas = new ArrayList<IExpression>();

					// And(Fi G1) ... And(Fi Gm)
					for (IExpression disjunct : orConjunct.exprs) {
						List<IExpression> formulas = new ArrayList<IExpression>();
						// TODO: Parent
						formulas.add((IExpression) conjunct.accept(this, null));
						formulas.add((IExpression) disjunct.accept(this, null));

						Conjunction temp = new Conjunction();
						temp.exprs.addAll(formulas);
						andFormulas.add(temp);
					}

					// Or(And(Fi G1) ... And(Fi Gm))
					Disjunction orFormula = new Disjunction();
					orFormula.exprs.addAll(andFormulas);

					// normalize(Or(And(Fi G1) ... And(Fi Gm)))
					// TODO: Parent
					newConjuncts
							.add((IExpression) orFormula.accept(this, null));

					hasChanged = true;

					i++;
				} else {
					// TODO: Parent
					newConjuncts.add((IExpression) conjunct.accept(this, null));
				}
			}
		}

		if (hasChanged) {
			if (newConjuncts.size() > 1) {
				// normalize(And(...)) is either an atomic, an or formula or a
				// and formula.
				// TODO: Parent
				Conjunction conj = new Conjunction();
				conj.exprs.addAll(newConjuncts);
				return (IExpression) conj.accept(this, null);
			} else {
				// Either an atomic formula or an or formula.
				return newConjuncts.get(0);
			}
		}

		return null;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Disjunction} object.
	 * @param arg a {@link lupos.rif.IRuleNode} object.
	 * @return a {@link lupos.rif.IRuleNode} object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public IRuleNode visit(Disjunction obj, IRuleNode arg) throws RIFException {
		if (obj.exprs.size() == 1)
			return obj.exprs.get(0).accept(this, arg);
		List<IExpression> newExprs = new ArrayList<IExpression>();
		for (IExpression expr : obj.exprs) {
			IExpression temp = (IExpression) expr.accept(this, obj);
			if (temp instanceof Disjunction)
				for (IExpression innerExpr : ((Disjunction) temp).exprs) {
					innerExpr.setParent(obj);
					newExprs.add(innerExpr);
				}
			else
				newExprs.add(temp);
		}
		obj.exprs = newExprs;
		return obj;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Document} object.
	 * @param arg a {@link lupos.rif.IRuleNode} object.
	 * @return a {@link lupos.rif.IRuleNode} object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public IRuleNode visit(Document obj, IRuleNode arg) throws RIFException {
		List<Rule> newRules = new ArrayList<Rule>();
		List<IExpression> newFacts = new ArrayList<IExpression>();
		for (IExpression expr : obj.getFacts())
			if (expr instanceof Conjunction)
				for (IExpression exp : ((Conjunction) expr).exprs) {
					newFacts.add(exp);
				}
			else
				newFacts.add(expr);
		obj.getFacts().clear();
		obj.getFacts().addAll(newFacts);
		for (Rule rule : obj.getRules())
			if (rule.isImplication())
				newRules.add((Rule) rule.accept(this, obj));
			else
				newRules.add(rule);
		obj.getRules().clear();
		obj.getRules().addAll(newRules);
		return obj;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Rule} object.
	 * @param arg a {@link lupos.rif.IRuleNode} object.
	 * @return a {@link lupos.rif.IRuleNode} object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public IRuleNode visit(Rule obj, IRuleNode arg) throws RIFException {
		obj.setParent(arg);
		obj.setHead((IExpression) obj.getHead().accept(this, obj));
		obj.setBody((IExpression) obj.getBody().accept(this, obj));
		List<IExpression> listOfNots = new ArrayList<IExpression>(obj.getNots().size());
		for(IExpression iExpression: obj.getNots()){
			listOfNots.add((IExpression) iExpression.accept(this, obj));
		}
		obj.setNots(listOfNots);
		return obj;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.RulePredicate} object.
	 * @param arg a {@link lupos.rif.IRuleNode} object.
	 * @return a {@link lupos.rif.IRuleNode} object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public IRuleNode visit(RulePredicate obj, IRuleNode arg)
			throws RIFException {
		obj.setParent(arg);
		obj.termName = (IExpression) obj.termName.accept(this, obj);
		List<IExpression> newParams = new ArrayList<IExpression>();
		for (IExpression expr : obj.termParams)
			newParams.add((IExpression) expr.accept(this, obj));
		obj.termParams = newParams;
		return obj;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.External} object.
	 * @param arg a {@link lupos.rif.IRuleNode} object.
	 * @return a {@link lupos.rif.IRuleNode} object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public IRuleNode visit(External obj, IRuleNode arg) throws RIFException {
		obj.setParent(arg);
		obj.termName = (IExpression) obj.termName.accept(this, obj);
		List<IExpression> newParams = new ArrayList<IExpression>();
		for (IExpression expr : obj.termParams)
			newParams.add((IExpression) expr.accept(this, obj));
		obj.termParams = newParams;
		return obj;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.ExistExpression} object.
	 * @param arg a {@link lupos.rif.IRuleNode} object.
	 * @return a {@link lupos.rif.IRuleNode} object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public IRuleNode visit(ExistExpression obj, IRuleNode arg)
			throws RIFException {
		obj.setParent(arg);
		obj.expr = (IExpression) obj.expr.accept(this, obj);
		return obj;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.RuleVariable} object.
	 * @param arg a {@link lupos.rif.IRuleNode} object.
	 * @return a {@link lupos.rif.IRuleNode} object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public IRuleNode visit(RuleVariable obj, IRuleNode arg) throws RIFException {
		obj.setParent(arg);
		return obj;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Constant} object.
	 * @param arg a {@link lupos.rif.IRuleNode} object.
	 * @return a {@link lupos.rif.IRuleNode} object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public IRuleNode visit(Constant obj, IRuleNode arg) throws RIFException {
		obj.setParent(arg);
		return obj;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Equality} object.
	 * @param arg a {@link lupos.rif.IRuleNode} object.
	 * @return a {@link lupos.rif.IRuleNode} object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public IRuleNode visit(Equality obj, IRuleNode arg) throws RIFException {
		obj.setParent(arg);
		return obj;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.RuleList} object.
	 * @param arg a {@link lupos.rif.IRuleNode} object.
	 * @return a {@link lupos.rif.IRuleNode} object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public IRuleNode visit(RuleList obj, IRuleNode arg) throws RIFException {
		for (IExpression expr : obj.getItems())
			expr.accept(this, arg);
		return obj;
	}
}
