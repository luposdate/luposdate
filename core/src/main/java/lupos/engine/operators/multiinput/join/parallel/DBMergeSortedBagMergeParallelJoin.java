package lupos.engine.operators.multiinput.join.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.multiinput.join.DBMergeSortedBagMergeJoin;
import lupos.engine.operators.multiinput.join.Join;

public class DBMergeSortedBagMergeParallelJoin extends MergeParallelJoin {
	private static final long serialVersionUID = 1L;

	public DBMergeSortedBagMergeParallelJoin() {
		super(initOperators(), false);
	}

	private static Collection<? extends Join> initOperators() {
		final List<Join> joiner = new ArrayList<Join>();
		for (int i = 0; i < DEFAULT_NUMBER_THREADS; i++) {
			joiner.add(new DBMergeSortedBagMergeJoin());
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
