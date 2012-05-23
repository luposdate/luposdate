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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class MagicSetTransformationVisitor implements
		IRuleVisitor<Object, Object> {
	private boolean doDebug = false;
	private Document rifDoc;
	private final String magicPrefix = "magic_";
	private final Multimap<String, Rule> ruleMap = HashMultimap.create();

	private RulePredicate seed;
	private Set<String> goals = new HashSet<String>();
	private Stack<String> goalStack = new Stack<String>();

	public MagicSetTransformationVisitor() {
	}

	public MagicSetTransformationVisitor(final boolean debug) {
		this();
		doDebug = debug;
	}

	private void debug(final String str) {
		if (doDebug)
			System.out.println("MS-Transformation: " + str);
	}

	@Override
	public Object visit(Document obj, Object arg) throws RIFException {
		rifDoc = obj;
		// 1. Regeln anhand Predicatename indizieren
		for (final Rule rule : obj.getRules()) {
			final RulePredicate pred = (RulePredicate) rule.getHead();
			ruleMap.put(pred.termName.toString(), rule);
		}

		// 2. Erstes Goal ist die Conclusion, rekursive Bearbeitung anfangen
		// Adornment bestimmen

		StringBuilder adornment = new StringBuilder();
		for (final IExpression expr : ((RulePredicate) obj.getConclusion()).termParams)
			if (expr instanceof Constant)
				adornment.append("b");
			else if (expr instanceof RuleVariable)
				adornment.append("f");

		obj.getConclusion().accept(this, adornment.toString());

		return obj;
	}

	@Override
	public Object visit(Rule obj, Object arg) throws RIFException {
		final Map<RuleVariable, Boolean> boundVariables = new HashMap<RuleVariable, Boolean>();
		for (final RuleVariable var : obj.getDeclaredVariables())
			boundVariables.put(var, false);
		// 1. bestimmen der gebundenen Variablen
		final RulePredicate head = (RulePredicate) obj.getHead();
		for (int i = 0; i < head.termParams.size(); i++)
			// TODO: †berprŸfen ob im Head konstanten vorkommen usw.
			if (arg.toString().charAt(i) == 'b')
				boundVariables.put((RuleVariable) head.termParams.get(i), true);
		final List<IExpression> hornClause = new ArrayList<IExpression>();
		if (obj.getBody() instanceof Conjunction)
			hornClause.addAll(((Conjunction) obj.getBody()).exprs);
		else
			hornClause.add(obj.getBody());
		final List<RulePredicate> predicates = sortInformationPassing(
				hornClause, boundVariables);

		debug("RULE -> " + obj.toString());

		for (final IExpression expr : predicates) {
			// Body besteht aus nur einem RulePredicate
			// Adornment bestimmen
			final RulePredicate body = (RulePredicate) expr;
			final StringBuilder adornment = new StringBuilder();
			for (final IExpression ex : body.termParams)
				if (ex instanceof Constant
						|| (ex instanceof RuleVariable && boundVariables
								.get(ex)))
					adornment.append("b");
				else if (ex instanceof RuleVariable) {
					adornment.append("f");
					boundVariables.put((RuleVariable) ex, true);
				}
			String nextGoal = body.termName.toString() + "_"
					+ adornment.toString();
			if(goals.contains(nextGoal) && body.isRecursive()){
				//Rekursiver Aufruf -> Magic-Regel erstellen
			}
			body.accept(this, adornment.toString());
		}

		return null;
	}

	@Override
	public Object visit(RulePredicate obj, Object arg) throws RIFException {
		String goal = obj.termName.toString() + "_" + arg.toString();
		debug("GOAL -> " + goal);

		// Alle passenden Regeln suchen und abarbeiten
		goalStack.push(goal);
		for (final Rule rule : ruleMap.get(obj.termName.toString()))
			rule.accept(this, arg);
		goalStack.pop();

		return null;
	}

	@Override
	public Object visit(Conjunction obj, Object arg) throws RIFException {
		return obj.exprs;
	}

	@Override
	public Object visit(ExistExpression obj, Object arg) throws RIFException {
		return null;
	}

	@Override
	public Object visit(Disjunction obj, Object arg) throws RIFException {
		return null;
	}

	@Override
	public Object visit(Equality obj, Object arg) throws RIFException {
		return null;
	}

	@Override
	public Object visit(External obj, Object arg) throws RIFException {
		return null;
	}

	@Override
	public Object visit(RuleList obj, Object arg) throws RIFException {
		return null;
	}

	@Override
	public Object visit(RuleVariable obj, Object arg) throws RIFException {
		return obj;
	}

	@Override
	public Object visit(Constant obj, Object arg) throws RIFException {
		return obj;
	}

	private List<RulePredicate> sortInformationPassing(
			List<IExpression> accept, Map<RuleVariable, Boolean> boundVars) {
		List<RulePredicate> result = new ArrayList<RulePredicate>(accept.size());
		for (final IExpression expr : accept)
			result.add((RulePredicate) expr);

		// Ziel: mšglichst bei rekursiven Aufrufen immer mindestens eins
		// gebunden, aber nicht alle gebunden
		// Ansatz: Alle mšglichen Stellungen ausprobieren und erste sinnvolle
		// Stellung zurŸckgeben
		int counter = result.size();
		while (counter > 0) {
			// 1. Neue Liste erstellen, durch linksverschiebung
			final Map<RuleVariable, Boolean> cpVarMap = Maps.newHashMap();
			cpVarMap.putAll(boundVars);
			final RulePredicate[] temp = new RulePredicate[result.size()];
			for (int i = 0; i < result.size(); i++)
				temp[i == 0 ? result.size() - 1 : i - 1] = result.get(i);

			// Simulation durchfŸhren bis zum rekursiven PrŠdikat
			for (final RulePredicate pred : temp) {

				final StringBuilder adornment = new StringBuilder();
				for (final IExpression ex : pred.termParams)
					if (ex instanceof Constant
							|| (ex instanceof RuleVariable && cpVarMap.get(ex)))
						adornment.append("b");
					else if (ex instanceof RuleVariable) {
						adornment.append("f");
						cpVarMap.put((RuleVariable) ex, true);
					}

				// Wenn rekursives PrŠdikat und ein bound ohne alle bound, dann
				// ist es das ergebniss
				if (pred.isRecursive())
					if (adornment.toString().contains("b")
							&& adornment.toString().contains("f"))
						return Lists.newArrayList(temp);
					else {
						result = Lists.newArrayList(temp);
						break;
					}
			}

			counter--;
		}

		return result;
	}

}
