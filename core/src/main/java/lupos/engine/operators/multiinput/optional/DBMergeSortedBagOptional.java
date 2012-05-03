package lupos.engine.operators.multiinput.optional;

import lupos.engine.operators.multiinput.join.DBMergeSortedBagMergeJoin;

public class DBMergeSortedBagOptional extends Optional {

	public DBMergeSortedBagOptional() {
		super(new DBMergeSortedBagMergeJoin());
	}
}
