package lupos.engine.indexconstruction.implementation;

import java.util.List;
import java.util.Map.Entry;

import lupos.datastructures.patriciatrie.ram.RBTrieMap;
import lupos.engine.indexconstruction.interfaces.IConsumeTripleBlocksWithPatTrieMap;
import lupos.misc.Tuple;

public class LocalIDsGenerator implements IConsumeTripleBlocksWithPatTrieMap {

	private final IConsumeTripleBlocksWithPatTrieMap tripleBlocksWithLocalIDsConsumer;
	private final List<Tuple<String, Long>> times;
	private long sum = 0L;

	public LocalIDsGenerator(final IConsumeTripleBlocksWithPatTrieMap tripleBlocksWithLocalIDsConsumer, final List<Tuple<String, Long>> times){
		this.tripleBlocksWithLocalIDsConsumer = tripleBlocksWithLocalIDsConsumer;
		this.times = times;
	}

	@Override
	public void consumeTriplesBlocks(final int[][] triples, final int index, final RBTrieMap<Integer> trieMap) {
		final long start = System.currentTimeMillis();
		// generate a mapping from temporary to local ids
		final int[] mapping = new int[trieMap.size()];
		int local_id = 0;
		for(final Entry<String, Integer> entry: trieMap) {
			mapping[entry.getValue()] = local_id;
			local_id++;
		}
		// apply mapping to id triples in order to generate local id triples
		for(int i=0; i<index; i++) {
			final int[] triple = triples[i];
			for(int j=0; j<3; j++) {
				triple[j] = mapping[triple[j]];
			}
		}
		final long end = System.currentTimeMillis();
		this.sum += end-start;
		this.tripleBlocksWithLocalIDsConsumer.consumeTriplesBlocks(triples, index, trieMap);
	}


	@Override
	public void notifyEndOfProcessing() {
		this.times.add(new Tuple<String, Long>("Map from temporary to local IDs", this.sum));
		this.tripleBlocksWithLocalIDsConsumer.notifyEndOfProcessing();
	}

}
