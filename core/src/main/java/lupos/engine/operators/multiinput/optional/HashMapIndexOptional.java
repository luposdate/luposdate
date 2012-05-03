package lupos.engine.operators.multiinput.optional;

import lupos.engine.operators.multiinput.join.HashMapIndexJoin;

public class HashMapIndexOptional extends Optional {

	public HashMapIndexOptional() {
		super(new HashMapIndexJoin());
	}
}
