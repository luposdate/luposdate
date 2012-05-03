package lupos.engine.operators.multiinput.optional.parallel;

import java.util.Collection;

import lupos.engine.operators.multiinput.MultiInputOperator;
import lupos.engine.operators.multiinput.join.parallel.MergeParallelJoin;

public abstract class MergeParallelOptional extends MergeParallelJoin {

	public MergeParallelOptional(
			final Collection<? extends MultiInputOperator> operators,
			final boolean optional) {
		super(operators, optional);
	}

}
