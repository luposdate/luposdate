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
package lupos.rif.magicset;

/**
 * This package implements the subsumptive demand transformation, which is a magic sets variant, according to
 * Tekle, K. T., and Liu, Y. A. More Efficient Datalog Queries: Subsumptive Tabling Beats Magic Sets. In Proceedings of the 2011 ACM SIGMOD International Conference on Management of Data (New York, NY, USA, 2011), SIGMOD '11, ACM, pp. 661-672.
 * http://delivery.acm.org/10.1145/1990000/1989393/p661-tekle.pdf?ip=141.83.117.164&id=1989393&acc=ACTIVE%20SERVICE&key=2BA2C432AB83DA15%2E184BABF16494B778%2E4D4702B0C3E38B35%2E4D4702B0C3E38B35&CFID=619520676&CFTOKEN=61822385&__acm__=1421657747_173e331cd6b13874d6e88db2fed691e7
 * http://www3.cs.stonybrook.edu/~liu/papers/RuleQueryBeat-SIGMOD11.pdf
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.RIFException;
import lupos.rif.model.Conjunction;
import lupos.rif.model.Constant;
import lupos.rif.model.Disjunction;
import lupos.rif.model.Equality;
import lupos.rif.model.ExistExpression;
import lupos.rif.model.Rule;
import lupos.rif.model.RulePredicate;
import lupos.rif.model.RuleVariable;

public class ExpressionHelper {

	public boolean canRuleInferPredicate(final Rule rule, final RulePredicate predicate){
		if (rule.getHead() instanceof RulePredicate) {
			final RulePredicate predHead = (RulePredicate) rule.getHead();
			if (predicate.termParams.size() == predHead.termParams.size()) {
				if (predicate.termName instanceof Constant
						&& predHead.termName instanceof Constant
						&& !predHead.termName.equals(predicate.termName)){
					return false;
				}
				for (int i = 0; i < predicate.termParams.size(); i++){
					if (predicate.termParams.get(i) instanceof Constant
							&& predHead.termParams.get(i) instanceof Constant
							&& !predHead.termParams.get(i).equals(predicate.termParams.get(i))){
						return false;
					}
				}
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
	}

	public boolean isPredicateIntensional(final RulePredicate predicate, final Collection<Rule> allRules){
		for (final Rule rule : allRules){
			if (this.canRuleInferPredicate(rule, predicate)) {
				return true;
			}
		}
		return false;
	}

	public int getNumberOfParameters(final RulePredicate predicate){
		 return predicate.termParams.size() +1;
	}

	public boolean isConclusionSupported(final IExpression expression){
		return (expression instanceof RulePredicate
				|| expression instanceof Disjunction
				|| expression instanceof Conjunction
				|| expression instanceof Equality
				|| expression instanceof ExistExpression);
	}

	public boolean isTermParameterSupported(final IExpression expression){
		return (expression instanceof RuleVariable || expression instanceof Constant);
	}

	public List<IExpression> getBoundParameters(final String bindingPattern, final Rule rule) throws RIFException{
		if (rule.getHead() instanceof RulePredicate) {
			return this.getBoundParameters(bindingPattern, (RulePredicate)rule.getHead());
		}else {
			throw new RIFException("Format not supported: The head of a rule has to to be a predicate");
		}
	}

	public List<IExpression> getBoundParameters(final String bindingPattern, final RulePredicate predicate){
		final List<IExpression> parameters = new ArrayList<>();
		parameters.add(predicate.termName);
		parameters.addAll(predicate.termParams);
		return this.getBoundParameters(bindingPattern, parameters);
	}

	public List<IExpression> getBoundParameters(final String bindingPattern, final List<IExpression> parameters){
		final List<IExpression> result = new ArrayList<>();
		for (int i = 0; i < bindingPattern.length(); i++) {
			if (bindingPattern.charAt(i) == 'b') {
				result.add(parameters.get(i));
			}
		}
		return result;
	}

	public Rule getParentRule(final IRuleNode node){
		final IRuleNode result = this.getParent(node, Rule.class);
		if (result != null) {
			return (Rule)result;
		}else {
			return null;
		}
	}

	public IRuleNode getParent(final IRuleNode expression, final Class<? extends IRuleNode> requestedClass){
		IRuleNode parent = expression.getParent();
		Class<? extends IRuleNode> parentClass = null;
		if (parent != null) {
			parentClass = parent.getClass();
		}
		while (parentClass != null && !parentClass.equals(requestedClass)) {
			parent = parent.getParent();
			if (parent != null) {
				parentClass = parent.getClass();
			}else {
				parentClass = null;
			}
		}
		return parent;
	}
}

