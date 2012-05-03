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

	public CheckMagicSetVisitor() {
	}

	public CheckMagicSetVisitor(final boolean debug) {
		this();
		doDebug = debug;
	}

	private void debug(final String str) {
		if (doDebug)
			System.out.println("MagicSetChecker: " + str);
	}

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

	@Override
	public Boolean visit(ExistExpression obj, Object arg) throws RIFException {
		debug("ExitsExpression not supported!");
		return false;
	}

	@Override
	public Boolean visit(Disjunction obj, Object arg) throws RIFException {
		debug("Disjunction not supported!");
		return false;
	}

	@Override
	public Boolean visit(Equality obj, Object arg) throws RIFException {
		debug("Equalities not allowed!");
		return false;
	}

	@Override
	public Boolean visit(External obj, Object arg) throws RIFException {
		debug("Externals not allowed!");
		return false;
	}

	@Override
	public Boolean visit(RuleList obj, Object arg) throws RIFException {
		debug("Lists not allowed!");
		return false;
	}

	@Override
	public Boolean visit(RuleVariable obj, Object arg) throws RIFException {
		return true;
	}

	@Override
	public Boolean visit(Constant obj, Object arg) throws RIFException {
		return true;
	}

}
