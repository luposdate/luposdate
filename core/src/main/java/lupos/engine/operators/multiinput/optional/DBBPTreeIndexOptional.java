package lupos.engine.operators.multiinput.optional;

import lupos.engine.operators.multiinput.join.DBBPTreeIndexJoin;

public class DBBPTreeIndexOptional extends Optional {

	public DBBPTreeIndexOptional() {
		super(new DBBPTreeIndexJoin());
	}
}
