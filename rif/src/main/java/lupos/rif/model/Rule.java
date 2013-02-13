/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.IRuleVisitor;
import lupos.rif.IVariableScope;
import lupos.rif.RIFException;

public class Rule extends AbstractRuleNode implements IVariableScope {
	private final Set<Rule> recursiveConnections = Sets.newHashSet();
	private final Set<RuleVariable> vars = Sets.newHashSet();
	private IExpression head;
	private IExpression body;
	private List<IExpression> nots = new LinkedList<IExpression>();

	@Deprecated
	// only predicate in the head
	public Set<IExpression> getHeadExpressions() {
		final Set<IExpression> result = new HashSet<IExpression>();
		if (this.head instanceof Conjunction)
			result.addAll(((Conjunction) this.head).exprs);
		else
			result.add(this.head);
		return result;
	}

	@Override
	public Set<RuleVariable> getDeclaredVariables() {
		return this.vars;
	}

	@Override
	public void addVariable(RuleVariable var) {
		var.setParent(this);
		this.vars.add(var);
	}

	public boolean isImplication() {
		return this.body != null;
	}

	public void setHead(IExpression head) {
		this.head = head;
	}

	public IExpression getHead() {
		return this.head;
	}

	public void setBody(IExpression body) {
		this.body = body;
	}

	public IExpression getBody() {
		return this.body;
	}

	public Set<Rule> getRecursiveConnections() {
		return this.recursiveConnections;
	}

	@Override
	public List<IRuleNode> getChildren() {
		List<IRuleNode> ret = new ArrayList<IRuleNode>();
		ret.addAll(this.vars);
		ret.add(this.head);
		if (isImplication()){
			ret.add(this.body);
		}
		ret.addAll(this.nots);
		return ret;
	}

	public boolean containsRecursion(IExpression conclusion, Set<Rule> visited) {
		if (conclusion instanceof RulePredicate
				&& getHead() instanceof RulePredicate) {
			final RulePredicate predConclusion = (RulePredicate) conclusion;
			final RulePredicate predHead = (RulePredicate) getHead();
			if (predConclusion.termParams.size() == predHead.termParams.size()) {
				boolean breaking = false;
				if (predConclusion.termName instanceof Constant
						&& predHead.termName instanceof Constant
						&& !predHead.termName.equals(predConclusion.termName))
					breaking = true;
				if (!breaking) {
					for (int i = 0; i < predConclusion.termParams.size(); i++)
						if (predConclusion.termParams.get(i) instanceof Constant
								&& predHead.termParams.get(i) instanceof Constant
								&& !predHead.termParams.get(i).equals(
										predConclusion.termParams.get(i)))
							breaking = true;
				}
				if (!breaking)
					return true;
			}
		} else if (conclusion instanceof Equality
				&& getHead() instanceof Equality)
			// Do not remove any rule, which could create an equality
			return true;
		visited.add(this);
		for (final Rule rule : this.recursiveConnections)
			if (!visited.contains(rule)
					&& rule.containsRecursion(conclusion, visited))
				return true;
		return false;
	}

	@Override
	public String toString() {
		return getHead().toString();
	}

	@Override
	public String getLabel() {
		return isImplication() ? "Rule" : "Fact";
	}

	@Override
	public <R, A> R accept(IRuleVisitor<R, A> visitor, A arg)
			throws RIFException {
		return visitor.visit(this, arg);
	}

	public void addNot(IExpression iExpression) {
		this.nots.add(iExpression);
	}
	
	public List<IExpression> getNots(){
		return this.nots;
	}
	
	public void setNots(List<IExpression> nots){
		this.nots = nots;
	}
}
