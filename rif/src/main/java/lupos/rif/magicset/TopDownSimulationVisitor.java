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
 *
 * @author groppe
 * @version $Id: $Id
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
public class TopDownSimulationVisitor implements IRuleVisitor<Object, Object> {

	private Collection<Rule> allRules = null;
	private final ExpressionHelper expressionHelper = new ExpressionHelper();

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public Object visit(final Document obj, final Object arg) throws RIFException {
		if (!this.expressionHelper.isConclusionSupported(obj.getConclusion())) {
			throw new RIFException("Format not supported");
		}

		this.allRules = obj.getRules();
		final LinkedList<DemandPattern> demandPatterns = new LinkedList<DemandPattern>();
		final LinkedList<DemandPattern> addedPatterns = new LinkedList<DemandPattern>();

		final List<DemandPattern> firstResult = (List<DemandPattern>)obj.getConclusion().accept(this, null);
		demandPatterns.addAll(firstResult);
		addedPatterns.addAll(firstResult);

		while (!addedPatterns.isEmpty()) {
			final DemandPattern currentPattern = addedPatterns.getFirst();
			addedPatterns.remove(currentPattern);

			for (final Rule rule : obj.getRules()) {
				if (this.expressionHelper.canRuleInferPredicate(rule, currentPattern.getPredicate())) {
					final List<DemandPattern> currentResult = (List<DemandPattern>)rule.accept(this, currentPattern);
					for (final DemandPattern newPattern : currentResult) {
						boolean isSubsumed = false;
						for (final DemandPattern existingPattern : demandPatterns) {
							if (existingPattern.subsumes(newPattern)) {
								isSubsumed = true;
							}
						}
						if (!isSubsumed) {
							newPattern.setBasedOn(currentPattern);
							demandPatterns.add(newPattern);
							addedPatterns.add(newPattern);
						}
					}
				}
			}
		}
		return demandPatterns;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Rule obj, final Object arg) throws RIFException {
		if (arg == null || !(arg instanceof DemandPattern)) {
			throw new IllegalArgumentException();
		}
		if (!obj.getNots().isEmpty()) {
			throw new RIFException("Format not supported");
		}
		@SuppressWarnings("unchecked")
		final
		List<DemandPattern> currentResult = (List<DemandPattern>)obj.getBody().accept(this, arg);
		for (final DemandPattern pattern : currentResult) {
			pattern.setRule(obj);
		}
		return currentResult;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ExistExpression obj, final Object arg) throws RIFException {
		throw new RIFException("Format not supported");
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public Object visit(final Conjunction obj, final Object arg) throws RIFException {

		if (arg == null) {
			final List<DemandPattern> totalResult = new ArrayList<>();
			for (final IExpression expression : obj.exprs) {
				totalResult.addAll((List<DemandPattern>)expression.accept(this, arg));
			}
			return totalResult;
		}else {
			if (!(arg instanceof DemandPattern)) {
				throw new IllegalArgumentException();
			}
			final ArrayList<IExpression> visitedHypotheses = new ArrayList<>();
			final List<DemandPattern> totalResult = new ArrayList<>();
			for (final IExpression expression : obj.exprs) {
				final List<DemandPattern> currentResult = (List<DemandPattern>)expression.accept(this, arg);
				for (final DemandPattern pattern : currentResult) {
					pattern.getPreceedingHypotheses().addAll(visitedHypotheses);
					totalResult.add(pattern);
				}
				visitedHypotheses.add(expression);
			}
			return totalResult;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Disjunction obj, final Object arg) throws RIFException {
		throw new RIFException("Format not supported");
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final RulePredicate obj, final Object arg) throws RIFException {
		if ((!this.expressionHelper.isTermParameterSupported(obj.termName))) {
			throw new RIFException("Format not supported");
		}
		if (!this.expressionHelper.isPredicateIntensional(obj, this.allRules)) {
			return new LinkedList<>();
		}
		final StringBuilder  builder = new StringBuilder();
		builder.append(obj.termName.accept(this, arg));
		for (final IExpression item : obj.termParams) {
			if ((!this.expressionHelper.isTermParameterSupported(item))) {
				throw new RIFException("Format not supported");
			}
			builder.append(item.accept(this, arg));
		}
		final DemandPattern result = new DemandPattern();
		result.setParametersBindingPattern(builder.toString());
		result.setPredicate(obj);
		final ArrayList<DemandPattern> ret = new ArrayList<>();
		ret.add(result);
		return ret;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public Object visit(final Equality obj, final Object arg) throws RIFException {
		final List<DemandPattern> totalResult = new ArrayList<DemandPattern>();
		if (obj.leftExpr instanceof RulePredicate) {
			totalResult.addAll((List<DemandPattern>)obj.leftExpr.accept(this, arg));
		}
		if (obj.rightExpr instanceof RulePredicate) {
			totalResult.addAll((List<DemandPattern>)obj.rightExpr.accept(this, arg));
		}
		return totalResult;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final External obj, final Object arg) throws RIFException {
		return new ArrayList<>();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final RuleList obj, final Object arg) throws RIFException {
		throw new RIFException("Format not supported");
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final RuleVariable obj, final Object arg) throws RIFException {
		final Rule parentRule = this.expressionHelper.getParentRule(obj);
		if (parentRule != null) {
			if (arg == null) {
				throw new IllegalArgumentException("The second parameter has to be a rule binding pattern string.");
			}
			if ((boolean)parentRule.accept(new CheckVariableBoundVisitor(obj),
					((DemandPattern)arg).getParametersBindingPattern())) {
				return "b";
			}else {
				return "f";
			}
		}else {
			return "f";
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Constant obj, final Object arg) throws RIFException {
		return "b";
	}

}
