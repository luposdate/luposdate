package lupos.engine.operators.multiinput.optional.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.multiinput.MultiInputOperator;
import lupos.engine.operators.multiinput.optional.DBMergeSortedBagOptional;

public class DBMergeSortedBagParallelOptional extends MergeParallelOptional {
	private static final long serialVersionUID = 1L;

	public DBMergeSortedBagParallelOptional() {
		super(initOperators(), true);
	}

	private static Collection<? extends MultiInputOperator> initOperators() {
		final List<MultiInputOperator> joiner = new ArrayList<MultiInputOperator>();
		for (int i = 0; i < DEFAULT_NUMBER_THREADS; i++) {
			joiner.add(new DBMergeSortedBagOptional());
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
