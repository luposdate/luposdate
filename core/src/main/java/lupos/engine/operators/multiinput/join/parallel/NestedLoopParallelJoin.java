package lupos.engine.operators.multiinput.join.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.join.NestedLoopJoin;

public class NestedLoopParallelJoin extends ParallelJoin {
	private static final long serialVersionUID = 1L;

	public NestedLoopParallelJoin() {
		super(initOperators(), false);
	}

	private static Collection<? extends Join> initOperators() {
		final List<Join> joiner = new ArrayList<Join>();
		for (int i = 0; i < DEFAULT_NUMBER_THREADS; i++) {
			joiner.add(new NestedLoopJoin());
		}
		return joiner;
	}

}
