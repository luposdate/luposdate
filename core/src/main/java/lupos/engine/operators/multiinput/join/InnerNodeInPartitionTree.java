package lupos.engine.operators.multiinput.join;

import lupos.datastructures.dbmergesortedds.DiskCollection;

public class InnerNodeInPartitionTree extends NodeInPartitionTree {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4123052907267333958L;
	protected static final int numberChildren = 100;
	public DiskCollection<NodeInPartitionTree> nodes;

	public InnerNodeInPartitionTree() {
		nodes = new DiskCollection<NodeInPartitionTree>(
				NodeInPartitionTree.class);
	}

	public InnerNodeInPartitionTree(
			final DiskCollection<NodeInPartitionTree> nodes) {
		this.nodes = nodes;
	}
}
