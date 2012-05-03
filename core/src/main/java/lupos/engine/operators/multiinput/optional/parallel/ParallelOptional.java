package lupos.engine.operators.multiinput.optional.parallel;

import java.util.Collection;

import lupos.engine.operators.multiinput.MultiInputOperator;
import lupos.engine.operators.multiinput.join.parallel.ParallelJoin;

public class ParallelOptional extends ParallelJoin {

	public ParallelOptional(
			final Collection<? extends MultiInputOperator> operators,
			final boolean optional) {
		super(operators, optional);
	}

}
