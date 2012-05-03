package lupos.optimizations.logical.rules.parallel;

import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.optional.Optional;
import lupos.engine.operators.singleinput.parallel.ParallelOperand;

/**
 * Implements a graph transformation which inserts a {@link ParallelOperand}
 * between each {@link Join} operator and its arguments, effectively evaluating
 * in a separate thread and thus distributing it across possibly multiple
 * processors.
 * 
 * @see ParallelOperand
 */
public class RuleJoinLastParallelOperands extends RuleJoinWithParallelOperands {

	private boolean checkRecursiveForJoinOrOptional(final BasicOperator op) {
		for (final OperatorIDTuple sop : op.getSucceedingOperators()) {
			final FindJoinOrOptionalOperatorVisitor frov = new FindJoinOrOptionalOperatorVisitor();
			sop.getOperator().visit(frov);
			if (frov.found())
				return true;
		}
		return false;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final BasicOperator join = mso.get("join");

		return !checkRecursiveForJoinOrOptional(join)
				&& super.checkPrecondition(mso);
	}

	@Override
	public String getName() {
		return "JoinLastParallelOperands";
	}

	private class FindJoinOrOptionalOperatorVisitor implements SimpleOperatorGraphVisitor {

		private boolean found = false;

		public Object visit(final BasicOperator basicOperator) {
			if (basicOperator instanceof Join
					|| basicOperator instanceof Optional)
				found = true;
			return null;
		}

		public boolean found() {
			return found;
		}

	}
}
