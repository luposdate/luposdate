
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
package lupos.rif.visitor;

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
public class CheckMagicSetVisitor implements IRuleVisitor<Boolean, Object> {
	private int conjuntionCounter = 0;
	private int recursivePredicateCounter = 0;
	private boolean doDebug = false;

	/**
	 * <p>Constructor for CheckMagicSetVisitor.</p>
	 */
	public CheckMagicSetVisitor() {
	}

	/**
	 * <p>Constructor for CheckMagicSetVisitor.</p>
	 *
	 * @param debug a boolean.
	 */
	public CheckMagicSetVisitor(final boolean debug) {
		this();
		doDebug = debug;
	}

	private void debug(final String str) {
		if (doDebug)
			System.out.println("MagicSetChecker: " + str);
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(Document obj, Object arg) throws RIFException {
		if (obj.getConclusion() != null
				&& !obj.getRules().isEmpty()
				&& obj.getConclusion() instanceof RulePredicate
				&& ((RulePredicate) obj.getConclusion()).termName instanceof Constant) {
			boolean hasBoundArg = false;
			for (final IExpression expr : ((RulePredicate) obj.getConclusion()).termParams)
				if (expr instanceof Constant) {
					hasBoundArg = true;
					break;
				}
			if (!hasBoundArg) {
				debug("No bound Arg in Conclusion!");
				return false;
			}
			boolean recursivePredicate = false;
			for (final Rule rule : obj.getRules())
				if (!rule.accept(this, arg))
					return false;
				else
					recursivePredicate = recursivePredicate ? true
							: recursivePredicateCounter > 0;
			if (!recursivePredicate)
				debug("Only Rulesets with at least one recursion make sense.");
			return recursivePredicate;
		} else
			return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(Rule obj, Object arg) throws RIFException {
		recursivePredicateCounter = 0;
		// Head only RulePredicate
		if (obj.getHead() instanceof RulePredicate
				&& obj.getBody().accept(this, arg)) {
			return true;
		} else
			return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(RulePredicate obj, Object arg) throws RIFException {
		// only one recursive Predicate
		if (obj.isRecursive() && recursivePredicateCounter > 0) {
			debug("Only one Recursive Predicate allowed!");
			debug(obj.toString());
			return false;
		} else if (obj.termName instanceof RuleVariable) {
			debug("No Variable Predicatenames allowed!");
			debug(obj.toString());
			return false;
		} else {
			if (obj.isRecursive())
				recursivePredicateCounter++;
			return true;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(Conjunction obj, Object arg) throws RIFException {
		// only one conjunction allowed -> Horn Rules
		if (conjuntionCounter > 0)
			return false;
		else {
			conjuntionCounter++;
			try {
				for (final IExpression expr : obj.exprs)
					if (!expr.accept(this, arg))
						return false;
			} finally {
				conjuntionCounter--;
			}
			return true;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(ExistExpression obj, Object arg) throws RIFException {
		debug("ExitsExpression not supported!");
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(Disjunction obj, Object arg) throws RIFException {
		debug("Disjunction not supported!");
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(Equality obj, Object arg) throws RIFException {
		debug("Equalities not allowed!");
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(External obj, Object arg) throws RIFException {
		debug("Externals not allowed!");
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(RuleList obj, Object arg) throws RIFException {
		debug("Lists not allowed!");
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(RuleVariable obj, Object arg) throws RIFException {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean visit(Constant obj, Object arg) throws RIFException {
		return true;
	}

}
