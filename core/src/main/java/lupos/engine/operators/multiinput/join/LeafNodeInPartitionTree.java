package lupos.engine.operators.multiinput.join;

import lupos.datastructures.queryresult.QueryResult;

public class LeafNodeInPartitionTree extends NodeInPartitionTree {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5043409388999416078L;
	protected static final int maxNumberEntries = 100;
	public QueryResult partition;

	public LeafNodeInPartitionTree(final QueryResult partition) {
		this.partition = partition;
	}
}