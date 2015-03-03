
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
package lupos.rif.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.IRuleVisitor;
import lupos.rif.IVariableScope;
import lupos.rif.RIFException;

import com.google.common.collect.Sets;
public class Rule extends AbstractRuleNode implements IVariableScope {
	private final Set<Rule> recursiveConnections = Sets.newHashSet();
	private final Set<RuleVariable> vars = Sets.newHashSet();
	private IExpression head;
	private IExpression body;
	private List<IExpression> nots = new LinkedList<IExpression>();

	/**
	 * <p>getHeadExpressions.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	@Deprecated
	// only predicate in the head
	public Set<IExpression> getHeadExpressions() {
		final Set<IExpression> result = new HashSet<IExpression>();
		if (this.head instanceof Conjunction) {
			result.addAll(((Conjunction) this.head).exprs);
		} else {
			result.add(this.head);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Set<RuleVariable> getDeclaredVariables() {
		return this.vars;
	}

	/** {@inheritDoc} */
	@Override
	public void addVariable(final RuleVariable var) {
		var.setParent(this);
		this.vars.add(var);
	}

	/**
	 * <p>isImplication.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isImplication() {
		return this.body != null;
	}

	/**
	 * <p>Setter for the field <code>head</code>.</p>
	 *
	 * @param head a {@link lupos.rif.IExpression} object.
	 */
	public void setHead(final IExpression head) {
		this.head = head;
	}

	/**
	 * <p>Getter for the field <code>head</code>.</p>
	 *
	 * @return a {@link lupos.rif.IExpression} object.
	 */
	public IExpression getHead() {
		return this.head;
	}

	/**
	 * <p>Setter for the field <code>body</code>.</p>
	 *
	 * @param body a {@link lupos.rif.IExpression} object.
	 */
	public void setBody(final IExpression body) {
		this.body = body;
	}

	/**
	 * <p>Getter for the field <code>body</code>.</p>
	 *
	 * @return a {@link lupos.rif.IExpression} object.
	 */
	public IExpression getBody() {
		return this.body;
	}

	/**
	 * <p>Getter for the field <code>recursiveConnections</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Rule> getRecursiveConnections() {
		return this.recursiveConnections;
	}

	/** {@inheritDoc} */
	@Override
	public List<IRuleNode> getChildren() {
		final List<IRuleNode> ret = new ArrayList<IRuleNode>();
		ret.addAll(this.vars);
		ret.add(this.head);
		if (this.isImplication()){
			ret.add(this.body);
		}
		ret.addAll(this.nots);
		return ret;
	}

	private boolean containsRecursionHelper(final IExpression conclusion){
		if(conclusion instanceof Conjunction){
			for(final IExpression conclusionElement: ((Conjunction) conclusion).exprs){
				if(this.containsRecursionHelper(conclusionElement)){
					return true;
				}
			}
		} else if(conclusion instanceof RulePredicate && this.getHead() instanceof RulePredicate) {
			final RulePredicate predConclusion = (RulePredicate) conclusion;
			final RulePredicate predHead = (RulePredicate) this.getHead();
			if (predConclusion.termParams.size() == predHead.termParams.size()) {
				boolean breaking = false;
				if (predConclusion.termName instanceof Constant
						&& predHead.termName instanceof Constant
						&& !predHead.termName.equals(predConclusion.termName)) {
					breaking = true;
				}
				if (!breaking) {
					for (int i = 0; i < predConclusion.termParams.size(); i++) {
						if (predConclusion.termParams.get(i) instanceof Constant
								&& predHead.termParams.get(i) instanceof Constant
								&& !predHead.termParams.get(i).equals(
										predConclusion.termParams.get(i))) {
							breaking = true;
						}
					}
				}
				if (!breaking) {
					return true;
				}
			}
		} else if (conclusion instanceof Equality && this.getHead() instanceof Equality){
			// Do not remove any rule, which could create an equality
			return true;
		}
		return false;
	}

	/**
	 * <p>containsRecursion.</p>
	 *
	 * @param conclusion a {@link lupos.rif.IExpression} object.
	 * @param visited a {@link java.util.Set} object.
	 * @return a boolean.
	 */
	public boolean containsRecursion(final IExpression conclusion, final Set<Rule> visited) {
		if(this.containsRecursionHelper(conclusion)){
			return true;
		}
		visited.add(this);
		for (final Rule rule : this.recursiveConnections){
			if (!visited.contains(rule) && rule.containsRecursion(conclusion, visited)){
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.getHead().toString();
	}

	/** {@inheritDoc} */
	@Override
	public String getLabel() {
		return this.isImplication() ? "Rule" : "Fact";
	}

	/** {@inheritDoc} */
	@Override
	public <R, A> R accept(final IRuleVisitor<R, A> visitor, final A arg)
			throws RIFException {
		return visitor.visit(this, arg);
	}

	/**
	 * <p>addNot.</p>
	 *
	 * @param iExpression a {@link lupos.rif.IExpression} object.
	 */
	public void addNot(final IExpression iExpression) {
		this.nots.add(iExpression);
	}

	/**
	 * <p>Getter for the field <code>nots</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<IExpression> getNots(){
		return this.nots;
	}

	/**
	 * <p>Setter for the field <code>nots</code>.</p>
	 *
	 * @param nots a {@link java.util.List} object.
	 */
	public void setNots(final List<IExpression> nots){
		this.nots = nots;
	}
}
