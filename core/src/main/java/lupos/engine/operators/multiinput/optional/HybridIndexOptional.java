package lupos.engine.operators.multiinput.optional;

import lupos.engine.operators.multiinput.join.HybridIndexJoin;

public class HybridIndexOptional extends Optional {

	public HybridIndexOptional() {
		super(new HybridIndexJoin());
	}
}
