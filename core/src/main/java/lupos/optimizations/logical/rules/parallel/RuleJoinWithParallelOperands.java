package lupos.optimizations.logical.rules.parallel;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.join.MergeJoinWithoutSorting;
import lupos.engine.operators.multiinput.optional.MergeWithoutSortingOptional;
import lupos.engine.operators.singleinput.parallel.ParallelOperand;
import lupos.engine.operators.singleinput.parallel.QueryResultInBlocks;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

/**
 * Implements a graph transformation which inserts a {@link ParallelOperand}
 * between each {@link Join} operator and its arguments, effectively evaluating
 * in a separate thread and thus distributing it across possibly multiple
 * processors.
 * 
 * @see ParallelOperand
 */
public class RuleJoinWithParallelOperands extends Rule {

	protected static boolean BLOCKWISE = false;

	public static boolean isBLOCKWISE() {
		return BLOCKWISE;
	}

	public static void setBLOCKWISE(final boolean blockwise) {
		BLOCKWISE = blockwise;
	}

	@Override
	protected void init() {
		final Operator a = new Join();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(a, "join");

		startNode = a;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final BasicOperator join = mso.get("join");
		if (isBLOCKWISE()
				&& (join instanceof MergeJoinWithoutSorting || join instanceof MergeWithoutSortingOptional)) {
			// blockwise does not work for merge joins/optionals!
			return false;
		} else
			return true;
	}

	@Override
	protected Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		OperatorIDTuple tuple;
		final BasicOperator join = mso.get("join");
		final List<BasicOperator> pre = join.getPrecedingOperators();

		join.setPrecedingOperators(new LinkedList<BasicOperator>());

		for (final BasicOperator op : pre) {
			if (op instanceof ParallelOperand
					|| op instanceof QueryResultInBlocks) {
				join.addPrecedingOperator(op);
				continue;
			}

			final Operator par = new ParallelOperand();
			par.addPrecedingOperator(op);
			added.add(par);

			final Operator maybequeryresultinblocks;
			if (isBLOCKWISE()) {
				maybequeryresultinblocks = new QueryResultInBlocks();
				added.add(maybequeryresultinblocks);
				par.setSucceedingOperator(new OperatorIDTuple(
						maybequeryresultinblocks, 0));
				maybequeryresultinblocks.addPrecedingOperator(par);
			} else {
				maybequeryresultinblocks = par;
			}
			tuple = op.getOperatorIDTuple(join);
			op.replaceOperatorIDTuple(tuple, new OperatorIDTuple(par, 0));

			join.addPrecedingOperator(maybequeryresultinblocks);
			maybequeryresultinblocks.addSucceedingOperator(new OperatorIDTuple(
					join, tuple.getId()));
		}

		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	@Override
	public String getName() {
		return "JoinWithParallelOperands";
	}
}
