package lupos.engine.operators.multiinput.optional;

import lupos.engine.operators.multiinput.join.TreeBagMergeJoin;

public class TreeBagOptional extends Optional {

	public TreeBagOptional() {
		super(new TreeBagMergeJoin());
	}
}
