package lupos.engine.operators.multiinput.join.parallel;

import java.util.Collection;

import lupos.engine.operators.multiinput.MultiInputOperator;

public abstract class MergeParallelJoin extends ParallelJoin {

	public MergeParallelJoin(
			final Collection<? extends MultiInputOperator> operators,
			final boolean optional) {
		super(operators, new MergeResultCollector(), optional);
	}
}
