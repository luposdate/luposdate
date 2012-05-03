package lupos.engine.operators.multiinput.optional.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.multiinput.MultiInputOperator;
import lupos.engine.operators.multiinput.optional.MergeWithoutSortingOptional;

public class MergeWithoutSortingParallelOptional extends MergeParallelOptional {
	private static final long serialVersionUID = 1L;

	public MergeWithoutSortingParallelOptional() {
		super(initOperators(), true);
	}

	private static Collection<? extends MultiInputOperator> initOperators() {
		final List<MultiInputOperator> joiner = new ArrayList<MultiInputOperator>();
		for (int i = 0; i < DEFAULT_NUMBER_THREADS; i++) {
			joiner.add(new MergeWithoutSortingOptional());
		}
		return joiner;
	}

	/**
	 * the actual join
	 */
	@Override
	protected QueryResult join() {
		col.setIntersectionVariables(this.getIntersectionVariables());
		return super.join();
	}
}
