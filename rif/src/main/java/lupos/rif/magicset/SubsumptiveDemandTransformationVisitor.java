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
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

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
public class SubsumptiveDemandTransformationVisitor implements IRuleVisitor<Object, Object>{

	private Collection<Rule> allOriginalRules = null;
	public SortedMap<DemandPredicateData, Rule> extendedRules;
	private final ExpressionHelper expressionHelper = new ExpressionHelper();
	private final ToPresentationSyntaxStringVisitor toStringVisitor = new ToPresentationSyntaxStringVisitor();

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public Object visit(final Document obj, final Object arg) throws RIFException {
		if (obj.getConclusion() == null) {
			throw new RIFException("Format not supported");
		}
		this.allOriginalRules = obj.getRules();
		this.extendedRules = new TreeMap<>();
		Collection<DemandPattern> simulationData = null;
		if (arg == null) {
			simulationData = (Collection<DemandPattern>)obj.accept(new TopDownSimulationVisitor(), null);
		}else {
			simulationData = (Collection<DemandPattern>)arg;
		}

		final StringBuilder builder = new StringBuilder();
		builder.append("Document(").append("\n");
//		builder.append("Conclusion(");
//		builder.append(obj.getConclusion().accept(toStringVisitor, null))
//				.append(")\n");

		builder.append("Group(\n");

		for (final DemandPattern currentPattern : simulationData) {
			for (final Rule rule : this.allOriginalRules) {
				if (this.expressionHelper.canRuleInferPredicate(rule, currentPattern.getPredicate())) {
					final String transformedRule = (String)rule.accept(this, currentPattern);
					if (!builder.toString().contains(transformedRule)) {
						builder.append(transformedRule).append("\n");
					}
				}
			}
		}
		builder.append("\n");
		for (final DemandPattern pattern : simulationData) {
			if (pattern.isFromConclusion()) {
				builder.append(pattern.getPredicate().accept(this, pattern)).append("\n");
			}
		}
		builder.append("\n");
		for (final IExpression fact : obj.getFacts()) {
			builder.append(fact.accept(this.toStringVisitor, null)).append("\n");
		}
		builder.append("\n");
		for (final Entry<DemandPredicateData, Rule> entry : this.extendedRules.entrySet()) {
			final List<DemandPredicateRuleData> gainedData = (List<DemandPredicateRuleData>)entry.getValue().accept(this, entry.getKey());
			for (final DemandPredicateRuleData dataItem : gainedData) {
				if (this.isDemandpredicateRuleNeeded(builder.toString(), dataItem)) {
					builder.append(dataItem.toString()).append("\n");
				}
			}
		}
		return builder.append(")\n)\n").toString();
	}

	private boolean isDemandpredicateRuleNeeded(final String existingDocument, final DemandPredicateRuleData toBeChecked ) {
		final String demandRule = toBeChecked.toString();
		if (existingDocument.contains(demandRule)) {
			return false;
		}
		boolean result = false;
		for (final Entry<DemandPredicateData, Rule> entry : this.extendedRules.entrySet()) {
			if (entry.getKey().canBeInferredBy(toBeChecked)) {
				result = true;
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Rule obj, final Object arg) throws RIFException {
		if (arg == null ) {
			throw new IllegalArgumentException();
		}
		if (!obj.getNots().isEmpty()) {
			throw new RIFException("Format not supported");
		}
		if (arg instanceof DemandPattern) {
			final StringBuilder str = new StringBuilder();
			if (!obj.getDeclaredVariables().isEmpty()) {
				str.append("Forall ");
				for (final IExpression variable : obj.getDeclaredVariables()) {
					str.append(variable.accept(this.toStringVisitor, null)).append(" ");
				}
				str.append("(\n");
			}
			str.append(obj.getHead().accept(this.toStringVisitor, null));
			str.append(" :- And(");
			str.append(obj.getHead().accept(this, arg)).append("\n");
			if (obj.getBody() instanceof Conjunction) {
				for (final IExpression expression : ((Conjunction)obj.getBody()).exprs) {
					str.append(expression.accept(this.toStringVisitor, null)).append("\n");
				}
			}else {
				str.append(obj.getBody().accept(this.toStringVisitor, null)).append("\n");
			}
			str.append(")\n");
			if (!obj.getDeclaredVariables().isEmpty()) {
				str.append(")\n");
			}
			return str.toString();
		}else if (arg instanceof DemandPredicateData) {
			@SuppressWarnings("unchecked")
			final
			List<DemandPredicateRuleData> result = (List<DemandPredicateRuleData>)obj.getBody().accept(this, arg);
			for (final DemandPredicateRuleData item : result) {
				item.setDeclaredVariablesWithCheck(obj.getDeclaredVariables());
			}
			return result;
		}else {
			throw new IllegalArgumentException();
		}

	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final RulePredicate obj, final Object arg) throws RIFException {
		if (arg == null ) {
			throw new IllegalArgumentException();
		}
		if (arg instanceof DemandPattern) {
			final DemandPattern pattern = (DemandPattern)arg;
			final DemandPredicateData predicate = new DemandPredicateData();
			predicate.setBindingPattern(pattern.getParametersBindingPattern());
			predicate.getOriginalParameters().add(obj.termName);
			predicate.getOriginalParameters().addAll(obj.termParams);
			final Rule parentRule = this.expressionHelper.getParentRule(obj);
			if (parentRule != null) {
				this.extendedRules.put(predicate, parentRule);
			}
			return predicate.toString();
		}else if (arg instanceof DemandPredicateData) {
			final List<DemandPredicateRuleData> result = new ArrayList<DemandPredicateRuleData>();
			if (!this.expressionHelper.isPredicateIntensional(obj, this.allOriginalRules)) {
				return result;
			}
			final DemandPredicateRuleData data = new DemandPredicateRuleData();
			data.setDemandHypothesisData((DemandPredicateData)arg);
			final DemandPredicateData headData = new DemandPredicateData();

			if ((!this.expressionHelper.isTermParameterSupported(obj.termName))) {
				throw new RIFException("Format not supported");
			}
			final StringBuilder bindingPatternBuilder = new StringBuilder();
			bindingPatternBuilder.append(obj.termName.accept(this, arg));
			for (final IExpression item : obj.termParams) {
				if ((!this.expressionHelper.isTermParameterSupported(item))) {
					throw new RIFException("Format not supported");
				}
				bindingPatternBuilder.append(item.accept(this, arg));
			}

			headData.setBindingPattern(bindingPatternBuilder.toString());
			headData.getOriginalParameters().add(obj.termName);
			headData.getOriginalParameters().addAll(obj.termParams);

			data.setHeadData(headData);
			if (!data.getHeadData().toString().equals(data.getDemandHypothesisData().toString())) {
				result.add(data);
			}
			return result;
		}else {
			throw new IllegalArgumentException();
		}

	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Conjunction obj, final Object arg) throws RIFException {
		if (arg == null || !(arg instanceof DemandPredicateData)) {
			throw new IllegalArgumentException();
		}
		final List<DemandPredicateRuleData> totalResult = new ArrayList<>();
		final LinkedList<IExpression> visitedHypotheses = new LinkedList<>();
		for (final IExpression expression : obj.exprs) {
			@SuppressWarnings("unchecked")
			final
			List<DemandPredicateRuleData> currentResult = (List<DemandPredicateRuleData>)expression.accept(this, arg);
			for (final DemandPredicateRuleData dataItem : currentResult) {
				dataItem.getOtherHypotheses().addAll(visitedHypotheses);
				totalResult.add(dataItem);
			}
			visitedHypotheses.add(expression);
		}
		return totalResult;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ExistExpression obj, final Object arg) throws RIFException {
		throw new RIFException("Format not supported");
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Disjunction obj, final Object arg) throws RIFException {
		throw new RIFException("Format not supported");
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public Object visit(final Equality obj, final Object arg) throws RIFException {
		if (arg == null || !(arg instanceof DemandPredicateData)) {
			throw new IllegalArgumentException();
		}
		final List<DemandPredicateRuleData> totalResult = new ArrayList<>();
		Object result = obj.leftExpr.accept(this, arg);
		if (result instanceof List<?>) {
			totalResult.addAll((List<DemandPredicateRuleData>)result);
		}
		result = obj.rightExpr.accept(this, arg);
		if (result instanceof List<?>) {
			totalResult.addAll((List<DemandPredicateRuleData>)result);
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
		if (arg == null || !(arg instanceof DemandPredicateData)) {
			throw new IllegalArgumentException();
		}
		final DemandPredicateData data = (DemandPredicateData) arg;
		final Rule parentRule = this.expressionHelper.getParentRule(obj);
		if (parentRule != null) {
			if (data.isVariableBoundByPredicate(obj)) {
				return "b";
			}
			if ((boolean)parentRule.accept(new CheckVariableBoundVisitor(obj),data.getBindingPattern())) {
				return "b";
			}
		}
		return "f";
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Constant obj, final Object arg) throws RIFException {
		return "b";
	}

}
