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
import java.util.HashSet;

import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.RIFException;
import lupos.rif.SimpleRuleVisitor;
import lupos.rif.model.Conjunction;
import lupos.rif.model.Document;
import lupos.rif.model.Rule;
import lupos.rif.model.RulePredicate;

public class ExtendedRuleFilteringVisitor extends SimpleRuleVisitor {

	@Override
	public IRuleNode visit(final Document obj, final IRuleNode arg) throws RIFException {
		for (final Rule rule : new ArrayList<Rule>(obj.getRules())) {
			if (rule.isImplication() && obj.getConclusion() != null) {
				// If rule is not used for evaluating the conclusion, then skip it!
				// Assumption: Conclusion is only one RulePredicate
				if (obj.getConclusion() instanceof Conjunction) {
					final Conjunction conclusions = (Conjunction) obj.getConclusion();
					boolean isNeeded = false;
					for (final IExpression expression : conclusions.exprs) {
						if (rule.containsRecursion(expression,new HashSet<Rule>())) {
							isNeeded = true;
						}
					}
					if (!isNeeded) {
						obj.getRules().remove(rule);
					}
				} else if (obj.getConclusion() instanceof RulePredicate) {
					if (!rule.containsRecursion(obj.getConclusion(),new HashSet<Rule>())) {
						obj.getRules().remove(rule);
					}
				} else {
					throw new RIFException("Format not supported");
				}

			}
		}
		return obj;
	}

}
