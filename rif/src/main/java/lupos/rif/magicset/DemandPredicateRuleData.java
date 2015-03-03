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
import java.util.List;
import java.util.Set;

import lupos.rif.IExpression;
import lupos.rif.model.RuleVariable;
public class DemandPredicateRuleData {
	private DemandPredicateData headData;
	private DemandPredicateData demandHypothesisData;
	private final List<IExpression> otherHypotheses = new ArrayList<>();
	private List<RuleVariable> declaredVariables = new ArrayList<>();

	/**
	 * <p>Getter for the field <code>declaredVariables</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<RuleVariable> getDeclaredVariables() {
		return this.declaredVariables;
	}

	/**
	 * <p>Getter for the field <code>headData</code>.</p>
	 *
	 * @return a {@link lupos.rif.magicset.DemandPredicateData} object.
	 */
	public DemandPredicateData getHeadData() {
		return this.headData;
	}

	/**
	 * <p>Setter for the field <code>headData</code>.</p>
	 *
	 * @param headData a {@link lupos.rif.magicset.DemandPredicateData} object.
	 */
	public void setHeadData(final DemandPredicateData headData) {
		this.headData = headData;
	}

	/**
	 * <p>Getter for the field <code>demandHypothesisData</code>.</p>
	 *
	 * @return a {@link lupos.rif.magicset.DemandPredicateData} object.
	 */
	public DemandPredicateData getDemandHypothesisData() {
		return this.demandHypothesisData;
	}

	/**
	 * <p>Setter for the field <code>demandHypothesisData</code>.</p>
	 *
	 * @param demandHypothesisData a {@link lupos.rif.magicset.DemandPredicateData} object.
	 */
	public void setDemandHypothesisData(final DemandPredicateData demandHypothesisData) {
		this.demandHypothesisData = demandHypothesisData;
	}

	/**
	 * <p>Getter for the field <code>otherHypotheses</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<IExpression> getOtherHypotheses() {
		return this.otherHypotheses;
	}

	/**
	 * <p>setDeclaredVariablesWithCheck.</p>
	 *
	 * @param possibleVariables a {@link java.util.Set} object.
	 */
	public void setDeclaredVariablesWithCheck(final Set<RuleVariable> possibleVariables) {
		this.declaredVariables.clear();
		final ToPresentationSyntaxStringVisitor visitor = new ToPresentationSyntaxStringVisitor();
		final List<RuleVariable> checkedVariables = new ArrayList<RuleVariable>();
		for (final RuleVariable var : possibleVariables) {
			final String varString = (String)var.accept(visitor, null);
			if (this.toString().contains(varString)) {
				checkedVariables.add(var);
			}
		}
		this.declaredVariables = checkedVariables;
	}

    /** {@inheritDoc} */
    @Override
    public String toString() {
    	final ToPresentationSyntaxStringVisitor visitor = new ToPresentationSyntaxStringVisitor();
    	final StringBuilder builder = new StringBuilder();
    	final ExpressionHelper helper = new ExpressionHelper();
    	if (!this.declaredVariables.isEmpty()) {
    		builder.append("Forall ");
			for (final IExpression variable : this.declaredVariables) {
				builder.append(variable.accept(visitor, null)).append(" ");
			}
			builder.append("(\n");
		}
    	builder.append(this.headData.toString()).append(" :- ");
    	if (this.otherHypotheses.isEmpty()) {
			builder.append(this.demandHypothesisData.toString()).append("\n");
		}else {
			builder.append("And(");
			builder.append(this.demandHypothesisData.toString()).append("\n");
			for (final IExpression expression : this.otherHypotheses) {
				builder.append(expression.accept(visitor, null)).append("\n");
			}
			builder.append(")\n");
		}
    	for (final String bindingPattern
    			: DemandPattern.computeAllSubsumingBindingPatterns(this.headData.getBindingPattern())) {
			builder.append("not <");
			builder.append(DemandPredicateData.predicateNameURLPrefix);
			builder.append(bindingPattern);
			builder.append(">(");
			for (final IExpression boundParameter : helper.getBoundParameters(bindingPattern, this.headData.getOriginalParameters())) {
				builder.append(boundParameter.accept(visitor, null)).append(" ");
			}
			builder.append(")\n");
		}
    	if (!this.declaredVariables.isEmpty()) {
    		builder.append(")\n");
		}
    	return builder.toString();
    }

}
