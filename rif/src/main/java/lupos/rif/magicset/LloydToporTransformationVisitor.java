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
public class LloydToporTransformationVisitor implements IRuleVisitor<Object, Object>{

	private final ToPresentationSyntaxStringVisitor toStringVisitor = new ToPresentationSyntaxStringVisitor();

	private final String conclusion;

	/**
	 * <p>Constructor for LloydToporTransformationVisitor.</p>
	 *
	 * @param conclusion an external given conclusion (can be null if there is no external given conclusion)
	 */
	public LloydToporTransformationVisitor(final String conclusion){
		this.conclusion = conclusion;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Document obj, final Object arg) throws RIFException {
		if (obj.getConclusion() == null && this.conclusion==null) {
			throw new RIFException("Format not supported");
		}
		final StringBuilder builder = new StringBuilder();
		builder.append("Document(").append("\n");
		builder.append("Conclusion(");
		if(this.conclusion!=null) {
			builder.append(this.conclusion);
		} else {
			builder.append(obj.getConclusion().accept(this.toStringVisitor, null));
		}
		builder.append(")\n");
		builder.append("Group(\n");
		for (final Rule rule : obj.getRules()) {
			builder.append((String)rule.accept(this, null));
		}
		builder.append("\n");
		for (final IExpression fact : obj.getFacts()) {
			builder.append(fact.accept(this.toStringVisitor, null)).append("\n");
		}
		builder.append(")\n");
		return builder.append(")\n").toString();


	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Rule obj, final Object arg) throws RIFException {
		final ArrayList<IExpression> headExpressions = new ArrayList<>();
		final ArrayList<IExpression> bodyExpressions = new ArrayList<>();
		final StringBuilder str = new StringBuilder();


		if (obj.getHead() instanceof Conjunction) {
			headExpressions.addAll(((Conjunction)obj.getHead()).exprs);
		}else {
			headExpressions.add(obj.getHead());
		}
		if (obj.isImplication()) {
			if (obj.getBody() instanceof Disjunction) {
				bodyExpressions.addAll(((Disjunction)obj.getBody()).exprs);
			}else {
				bodyExpressions.add(obj.getBody());
			}
			for (final IExpression headFormula : headExpressions) {
				for (final IExpression bodyFormula : bodyExpressions) {
					final String innerFormula = headFormula.accept(this.toStringVisitor, arg).toString() + " :- " + bodyFormula.accept(this.toStringVisitor, arg).toString();
					if (!obj.getDeclaredVariables().isEmpty()) {
						str.append("Forall ");
						for (final IExpression variable : obj.getDeclaredVariables()) {
							final String varText = variable.accept(this.toStringVisitor, null).toString();
							if (innerFormula.contains(varText)) {
								str.append(varText).append(" ");
							}
						}
						str.append("(\n");
					}
					str.append(innerFormula).append("\n");
					if (!obj.getDeclaredVariables().isEmpty()) {
						str.append(")\n");
					}
				}
			}
		}else {
			for (final IExpression headFormula : headExpressions) {
				str.append(headFormula.accept(this.toStringVisitor, arg)).append("\n");
			}
		}
		return str.toString();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ExistExpression obj, final Object arg) throws RIFException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Conjunction obj, final Object arg) throws RIFException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Disjunction obj, final Object arg) throws RIFException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final RulePredicate obj, final Object arg) throws RIFException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Equality obj, final Object arg) throws RIFException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final External obj, final Object arg) throws RIFException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final RuleList obj, final Object arg) throws RIFException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final RuleVariable obj, final Object arg) throws RIFException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final Constant obj, final Object arg) throws RIFException {
		throw new UnsupportedOperationException();
	}

}
