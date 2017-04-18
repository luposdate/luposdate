package lupos.engine.indexconstruction.interfaces;

import lupos.datastructures.patriciatrie.ram.RBTrieMap;

public interface IConsumeTripleBlocksWithPatTrieMap extends IEndOfProcessingNotification {
	public void consumeTriplesBlocks(int[][] triples, int index, RBTrieMap<Integer> trieMap);
}
