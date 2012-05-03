package lupos.engine.operators.multiinput.optional.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lupos.engine.operators.multiinput.MultiInputOperator;
import lupos.engine.operators.multiinput.optional.HashOptional;

public class HashParallelOptional extends ParallelOptional {
	private static final long serialVersionUID = 1L;

	public HashParallelOptional() {
		super(initOperators(), true);
	}

	private static Collection<? extends MultiInputOperator> initOperators() {
		final List<MultiInputOperator> joiner = new ArrayList<MultiInputOperator>();
		for (int i = 0; i < DEFAULT_NUMBER_THREADS; i++) {
			joiner.add(new HashOptional());
		}
		return joiner;
	}

}
