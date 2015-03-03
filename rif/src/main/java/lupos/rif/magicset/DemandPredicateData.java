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

import lupos.rif.IExpression;
import lupos.rif.model.Constant;
import lupos.rif.model.RuleVariable;
public class DemandPredicateData implements Comparable<DemandPredicateData>{
	/** Constant <code>predicateNameURLPrefix="http://example.com/subsumtive_demand#"</code> */
	public static final String predicateNameURLPrefix = "http://example.com/subsumtive_demand#";
	private static int instanceCount = 0;
	private final int instanceNumber;
	private String bindingPattern;
	private final List<IExpression> originalParameters;
	/**
	 * <p>Constructor for DemandPredicateData.</p>
	 */
	public DemandPredicateData() {
		this.originalParameters = new ArrayList<IExpression>();
		this.instanceNumber = instanceCount;
		instanceCount++;
	}

	/**
	 * <p>Getter for the field <code>bindingPattern</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getBindingPattern() {
		return this.bindingPattern;
	}
	/**
	 * <p>Setter for the field <code>bindingPattern</code>.</p>
	 *
	 * @param bindingPattern a {@link java.lang.String} object.
	 */
	public void setBindingPattern(final String bindingPattern) {
		this.bindingPattern = bindingPattern;
	}
	/**
	 * <p>Getter for the field <code>originalParameters</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<IExpression> getOriginalParameters() {
		return this.originalParameters;
	}

	/**
	 * <p>getVisibleParameters.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<IExpression> getVisibleParameters(){
		final ExpressionHelper helper = new ExpressionHelper();
		return helper.getBoundParameters(this.bindingPattern, this.originalParameters);
	}

	/**
	 * <p>isVariableBoundByPredicate.</p>
	 *
	 * @param variable a {@link lupos.rif.model.RuleVariable} object.
	 * @return a boolean.
	 */
	public boolean isVariableBoundByPredicate(final RuleVariable variable) {
		final ToPresentationSyntaxStringVisitor visitor = new ToPresentationSyntaxStringVisitor();
		final String variableString = (String)variable.accept(visitor, null);
		for (final IExpression parameter : this.getVisibleParameters()) {
			final String currentParameterString = (String)parameter.accept(visitor, null);
			if (variableString.equals(currentParameterString)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>canBeInferredBy.</p>
	 *
	 * @param ruleData a {@link lupos.rif.magicset.DemandPredicateRuleData} object.
	 * @return a boolean.
	 */
	public boolean canBeInferredBy(final DemandPredicateRuleData ruleData ) {
		if (ruleData.getHeadData() == null) {
			throw new IllegalArgumentException();
		}
		final DemandPredicateData headData = ruleData.getHeadData();
		if (headData.getBindingPattern().equals(this.getBindingPattern())) {
			final List<IExpression> headParameters = headData.getVisibleParameters();
			final List<IExpression> myParameters = this.getVisibleParameters();
			for (int i = 0; i < headParameters.size(); i++){
				if (headParameters.get(i) instanceof Constant
						&& myParameters.get(i) instanceof Constant
						&& !headParameters.get(i).equals(myParameters.get(i))){
					return false;
				}
			}
			return true;
		}else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {

		final ToPresentationSyntaxStringVisitor visitor = new ToPresentationSyntaxStringVisitor();
		final StringBuilder str = new StringBuilder();
		str.append("<");
		str.append(predicateNameURLPrefix);
		str.append(this.bindingPattern);
		str.append(">(");
		for (final IExpression parameter : this.getVisibleParameters() ) {
			str.append(parameter.accept(visitor, null)).append(" ");
		}
		str.append(")");
		return str.toString();
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final DemandPredicateData o) {

		// TODO Automatisch generierter Methodenstub
		return this.instanceNumber - o.instanceNumber;
	}
}
