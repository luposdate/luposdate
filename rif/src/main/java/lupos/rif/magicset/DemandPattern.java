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
import java.util.LinkedList;
import java.util.List;

import lupos.rif.IExpression;
import lupos.rif.model.Constant;
import lupos.rif.model.Rule;
import lupos.rif.model.RulePredicate;
public class DemandPattern {

	private Rule rule;
	private String parametersBindingPattern;
	private final ArrayList<IExpression> preceedingHypotheses = new ArrayList<>();
	private RulePredicate predicate;
	private DemandPattern basedOn;

	/**
	 * <p>Getter for the field <code>basedOn</code>.</p>
	 *
	 * @return a {@link lupos.rif.magicset.DemandPattern} object.
	 */
	public DemandPattern getBasedOn() {
		return this.basedOn;
	}
	/**
	 * <p>Setter for the field <code>basedOn</code>.</p>
	 *
	 * @param pattern a {@link lupos.rif.magicset.DemandPattern} object.
	 */
	public void setBasedOn(final DemandPattern pattern) {
		this.basedOn = pattern;
	}

	/**
	 * <p>Getter for the field <code>rule</code>.</p>
	 *
	 * @return a {@link lupos.rif.model.Rule} object.
	 */
	public Rule getRule() {
		return this.rule;
	}
	/**
	 * <p>Setter for the field <code>rule</code>.</p>
	 *
	 * @param rule a {@link lupos.rif.model.Rule} object.
	 */
	public void setRule(final Rule rule) {
		this.rule = rule;
	}
	/**
	 * <p>Getter for the field <code>predicate</code>.</p>
	 *
	 * @return a {@link lupos.rif.model.RulePredicate} object.
	 */
	public RulePredicate getPredicate() {
		return this.predicate;
	}
	/**
	 * <p>Setter for the field <code>predicate</code>.</p>
	 *
	 * @param predicate a {@link lupos.rif.model.RulePredicate} object.
	 */
	public void setPredicate(final RulePredicate predicate) {
		this.predicate = predicate;
	}
	/**
	 * <p>Getter for the field <code>parametersBindingPattern</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getParametersBindingPattern() {
		return this.parametersBindingPattern;
	}
	/**
	 * <p>Setter for the field <code>parametersBindingPattern</code>.</p>
	 *
	 * @param parametersBindingPattern a {@link java.lang.String} object.
	 */
	public void setParametersBindingPattern(final String parametersBindingPattern) {
		this.parametersBindingPattern = parametersBindingPattern;
	}

	/**
	 * <p>getNumberOfParameters.</p>
	 *
	 * @return a int.
	 */
	public int getNumberOfParameters(){
		return this.parametersBindingPattern.length();
	}

	/**
	 * <p>Getter for the field <code>preceedingHypotheses</code>.</p>
	 *
	 * @return a {@link java.util.ArrayList} object.
	 */
	public ArrayList<IExpression> getPreceedingHypotheses(){
		return this.preceedingHypotheses;
	}

	/**
	 * <p>isFromConclusion.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isFromConclusion(){
		return this.rule == null;
	}

	/**
	 * <p>subsumes.</p>
	 *
	 * @param other a {@link lupos.rif.magicset.DemandPattern} object.
	 * @return a boolean.
	 */
	public boolean subsumes(final DemandPattern other){
		if (this.parametersBindingPattern.length() != other.parametersBindingPattern.length()) {
			return false;
		}
		boolean allParametersSubsumed = true;
		final char[] patternArray = this.parametersBindingPattern.toCharArray();
		final char[] otherArray = other.parametersBindingPattern.toCharArray();

		for (int i = 0; i < patternArray.length; i++) {
			if (patternArray[i]== 'f' ) {
				allParametersSubsumed = allParametersSubsumed && true;
			}else {
				if (otherArray[i]== 'f') {
					allParametersSubsumed = allParametersSubsumed && false;
				}else {
					IExpression thisParameter = null;
					IExpression otherParameter = null;
					if (i==0) {
						thisParameter = this.predicate.termName;
						otherParameter = other.predicate.termName;
					}else {
						thisParameter = this.predicate.termParams.get(i-1);
						otherParameter = other.predicate.termParams.get(i-1);
					}
					if (thisParameter instanceof Constant && otherParameter instanceof Constant) {
						if (thisParameter.equals(otherParameter)) {
							allParametersSubsumed = allParametersSubsumed && true;
						}else {
							allParametersSubsumed = allParametersSubsumed && false;
						}
					}else {
						allParametersSubsumed = allParametersSubsumed && true;
					}
				}
			}
		}

		final ToPresentationSyntaxStringVisitor toStringVisitor = new ToPresentationSyntaxStringVisitor();
		StringBuilder builder = new StringBuilder();
		for (final IExpression expression : other.preceedingHypotheses) {
			builder.append(expression.accept(toStringVisitor, null)).append("\n");
		}
		final String otherHypotheses = builder.toString();

		builder = new  StringBuilder();
		for (final IExpression expression : this.preceedingHypotheses) {
			builder.append(expression.accept(toStringVisitor, null)).append("\n");
		}
		final String thisHypotheses = builder.toString();

		return allParametersSubsumed && otherHypotheses.contains(thisHypotheses);
	}

	/**
	 * <p>isBindingPatternSubsuming.</p>
	 *
	 * @param pattern a {@link java.lang.String} object.
	 * @param other a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean isBindingPatternSubsuming(final String pattern, final String other){

		if (pattern.length() != other.length()) {
			return false;
		}
		boolean result = true;
		final char[] patternArray = pattern.toCharArray();
		final char[] otherArray = other.toCharArray();

		for (int i = 0; i < pattern.length(); i++) {
			if (otherArray[i] == 'f' && patternArray[i] == 'b') {
				result = false;
			}
		}
		return result;
	}

	/**
	 * <p>computeAllSubsumingBindingPatterns.</p>
	 *
	 * @param bindingPattern a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	public static List<String> computeAllSubsumingBindingPatterns(final String bindingPattern){
		final List<String> totalResult = new LinkedList<>();
		final List<String> lastResult = new LinkedList<>();
		final List<String> currentResult = new LinkedList<>();
		lastResult.add(bindingPattern);
		while (!lastResult.isEmpty()) {
			for (final String item : lastResult) {
				for (int i = 0; i < item.length(); i++) {
					if (item.charAt(i) == 'b') {
						final char[] array = item.toCharArray();
						array[i] = 'f';
						final String nextResult = String.copyValueOf(array);
						if (!currentResult.contains(nextResult)) {
							currentResult.add(nextResult);
						}
					}
				}
			}
			totalResult.addAll(lastResult);
			lastResult.clear();
			lastResult.addAll(currentResult);
			currentResult.clear();
		}
		totalResult.remove(bindingPattern);
		return totalResult;
	}
}
