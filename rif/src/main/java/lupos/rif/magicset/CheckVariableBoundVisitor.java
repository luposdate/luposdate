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

public class CheckVariableBoundVisitor implements IRuleVisitor<Object, Object> {

	private final RuleVariable checkedVariable;
	private final ExpressionHelper expressionHelper;

	public CheckVariableBoundVisitor(final RuleVariable variable) {
		this.checkedVariable = variable;
		this.expressionHelper = new ExpressionHelper();
	}

	@Override
	public Object visit(final Document obj, final Object arg) throws RIFException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visit(final Rule obj, final Object arg) throws RIFException, IllegalArgumentException {
		if (arg == null) {
			throw new IllegalArgumentException();
		}
		final List<IExpression> boundParameters = this.expressionHelper.getBoundParameters((String)arg, obj);
		boolean isBound = false;
		for (final IExpression parameter : boundParameters) {
			if (!this.expressionHelper.isTermParameterSupported(parameter)) {
				throw new RIFException("Format not supported.");
			}
			if ((boolean)parameter.accept(this, null)) {
				isBound = true;
			}
		}
		if (!isBound) {
			try {
				isBound = (boolean)obj.getBody().accept(this, null);
			} catch (final RIFException e) {
				if (e.getMessage().equals("Reached variable to check.")) {
					isBound = false;
				}else {
					throw e;
				}
			}
		}
		return isBound;
	}

	@Override
	public Object visit(final ExistExpression obj, final Object arg) throws RIFException {
		throw new RIFException("Format not supported.");
	}

	@Override
	public Object visit(final Conjunction obj, final Object arg) throws RIFException {
		boolean isBound = false;
		IExpression currentExpression = obj.exprs.get(0);
		while (!isBound) {
			isBound = (boolean)currentExpression.accept(this, null);
			currentExpression = obj.exprs.get(obj.exprs.indexOf(currentExpression)+1);
		}
		return isBound;
	}

	@Override
	public Object visit(final Disjunction obj, final Object arg) throws RIFException {
		throw new RIFException("Format not supported.");
	}

	@Override
	public Object visit(final RulePredicate obj, final Object arg) throws RIFException {
		if (!this.expressionHelper.isTermParameterSupported(obj.termName)) {
			throw new RIFException("Format not supported.");
		}
		boolean isBound = false;
		if ((boolean)obj.termName.accept(this, null)) {
			isBound = true;
		}
		for (final IExpression expression : obj.termParams) {
			if (!this.expressionHelper.isTermParameterSupported(expression)) {
				throw new RIFException("Format not supported.");
			}
			if ((boolean)expression.accept(this, null)) {
				isBound = true;
			}
		}
		return isBound;
	}

	@Override
	public Object visit(final Equality obj, final Object arg) throws RIFException {
		boolean isBound = false;
		if ((boolean)obj.leftExpr.accept(this, null)) {
			isBound = true;
		}
		if ((boolean)obj.rightExpr.accept(this, null)) {
			isBound = true;
		}
		return isBound;
	}

	@Override
	public Object visit(final External obj, final Object arg) throws RIFException {
		if (!this.expressionHelper.isTermParameterSupported(obj.termName)) {
			throw new RIFException("Format not supported.");
		}
		boolean isBound = false;
		if ((boolean)obj.termName.accept(this, null)) {
			isBound = true;
		}
		for (final IExpression expression : obj.termParams) {
			if (!this.expressionHelper.isTermParameterSupported(expression)) {
				throw new RIFException("Format not supported.");
			}
			if ((boolean)expression.accept(this, null)) {
				isBound = true;
			}
		}
		return isBound;
	}

	@Override
	public Object visit(final RuleList obj, final Object arg) throws RIFException {
		throw new RIFException("Format not supported.");
	}

	@Override
	public Object visit(final RuleVariable obj, final Object arg) throws RIFException {
		if (obj == this.checkedVariable) {
			throw new RIFException("Reached variable to check.");
		}
		return obj.getVariable().toString().equals(this.checkedVariable.getVariable().toString());
	}

	@Override
	public Object visit(final Constant obj, final Object arg) throws RIFException {
		return false;
	}

}
