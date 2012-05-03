package lupos.engine.operators.multiinput.join.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lupos.engine.operators.multiinput.join.HashJoin;
import lupos.engine.operators.multiinput.join.Join;

public class HashParallelJoin extends ParallelJoin {
	private static final long serialVersionUID = 1L;

	public HashParallelJoin() {
		super(initOperators(), false);
	}

	private static Collection<? extends Join> initOperators() {
		final List<Join> joiner = new ArrayList<Join>();
		for (int i = 0; i < DEFAULT_NUMBER_THREADS; i++) {
			joiner.add(new HashJoin());
		}
		return joiner;
	}

}
