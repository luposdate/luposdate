package lupos.engine.operators.multiinput.optional;

import lupos.engine.operators.multiinput.join.HashJoin;

public class HashOptional extends Optional {

	public HashOptional() {
		super(new HashJoin());
	}
}
