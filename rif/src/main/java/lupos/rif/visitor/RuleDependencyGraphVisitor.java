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

import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.RIFException;
import lupos.rif.SimpleRuleVisitor;
import lupos.rif.model.Constant;
import lupos.rif.model.Document;
import lupos.rif.model.Equality;
import lupos.rif.model.Rule;
import lupos.rif.model.RulePredicate;
import lupos.rif.model.RuleVariable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class RuleDependencyGraphVisitor extends SimpleRuleVisitor {
	private final Multimap<String, Rule> predicateMap = HashMultimap.create();
	private Rule currentRule = null;

	@Override
	public IRuleNode visit(Document obj, IRuleNode arg) throws RIFException {
		predicateMap.clear();
		for (final Rule rule : obj.getRules())
		{
			rule.getRecursiveConnections().clear();
			if (rule.getHead() instanceof RulePredicate) {
				final IExpression expr = (IExpression) ((RulePredicate) rule
						.getHead()).termName;
				if (expr instanceof Constant)
					predicateMap.put(expr.toString(), rule);
				else if (expr instanceof RuleVariable)
					predicateMap.put("?", rule);
			} else if (rule.getHead() instanceof Equality)
				predicateMap.put("=", rule);
		}
		return super.visit(obj, arg);
	}

	@Override
	public IRuleNode visit(Rule obj, IRuleNode arg) throws RIFException {
		currentRule = obj;
		if (obj.getBody() != null)
			return obj.getBody().accept(this, arg);
		else
			return null;
	}

	@Override
	public IRuleNode visit(RulePredicate obj, IRuleNode arg)
			throws RIFException {
		obj.setRecursive(false);
		final IExpression expr = (IExpression) obj.termName;
		if (expr instanceof Constant)
			for (final Rule rule : predicateMap.get(expr.toString())) {
				obj.setRecursive(true);
				rule.getRecursiveConnections().add(currentRule);
			}
		else if (expr instanceof RuleVariable)
			for (final Rule rule : predicateMap.get("?")) {
				obj.setRecursive(true);
				rule.getRecursiveConnections().add(currentRule);
			}
		return null;
	}

	@Override
	public IRuleNode visit(Equality obj, IRuleNode arg) throws RIFException {
		for (final Rule rule : predicateMap.get("="))
			rule.getRecursiveConnections().add(currentRule);
		return null;
	}

}
