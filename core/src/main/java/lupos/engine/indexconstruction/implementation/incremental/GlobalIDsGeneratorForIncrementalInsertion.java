package lupos.engine.indexconstruction.implementation.incremental;

import java.util.List;
import java.util.Map.Entry;

import lupos.datastructures.patriciatrie.ram.RBTrieMap;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.indexconstruction.interfaces.IConsumeTripleBlocksWithPatTrieMap;
import lupos.engine.indexconstruction.interfaces.IIndexContainer;
import lupos.engine.indexconstruction.interfaces.ISimpleConsumeTripleBlocks;
import lupos.misc.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalIDsGeneratorForIncrementalInsertion implements IConsumeTripleBlocksWithPatTrieMap {

	private static final Logger log = LoggerFactory.getLogger(GlobalIDsGeneratorForIncrementalInsertion.class);

	private final IIndexContainer<String, Integer> dictionary;
	private final StringArray stringArray;
	private final ISimpleConsumeTripleBlocks tripleBlocksConsumer;
	private final List<Tuple<String, Long>> times;
	private long sum1 = 0;
	private long sum2 = 0;

	// TODO: remove, just for debugging purposes
	private static int blocknumber = 0;

	public GlobalIDsGeneratorForIncrementalInsertion(final IIndexContainer<String, Integer> dictionary, final StringArray stringArray, final ISimpleConsumeTripleBlocks tripleBlocksConsumer, final List<Tuple<String, Long>> times){
		this.dictionary = dictionary;
		this.tripleBlocksConsumer = tripleBlocksConsumer;
		this.stringArray = stringArray;
		this.times = times;
	}

	@Override
	public synchronized void consumeTriplesBlocks(final int[][] triples, final int index, final RBTrieMap<Integer> trieMap) {
		if(index==0){
			return;
		}
		final long start = System.currentTimeMillis();
		final int[] mapping = new int[trieMap.size()];
		final SIPParallelIterator<java.util.Map.Entry<String, Integer>, String> iterator = this.dictionary.iterator();
		int next_id = this.dictionary.size() + 1; // start with index 1, as 0 is an error code in the dictionary
		java.util.Map.Entry<String, Integer> entry = null;
		for(final Entry<String, Integer> toBeMappedEntry: trieMap) {
			final String key = toBeMappedEntry.getKey();
			if(entry==null || entry.getKey().compareTo(key)<0){
				entry = iterator.next(key);
			}
			try {
				if(entry!=null && entry.getKey().compareTo(key)==0){
					mapping[toBeMappedEntry.getValue()] = entry.getValue();
				} else {
					mapping[toBeMappedEntry.getValue()] = next_id;
					next_id++;
				}
			} catch (final Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		iterator.close();
		for(int i=0; i<index; i++){
			final int[] triple = triples[i];
			triple[0] = mapping[triple[0]];
			triple[1] = mapping[triple[1]];
			triple[2] = mapping[triple[2]];
		}
		final long end = System.currentTimeMillis();
		this.sum1 += end-start;
		final long start2 = System.currentTimeMillis();
		final int max_old_id = this.dictionary.size() + 1; // start with index 1, as 0 is an error code in the dictionary
		final Thread[] threads = new Thread[2];
		threads[0] = new Thread(){
			@Override
			public void run(){
				for(final Entry<String, Integer> toBeMappedEntry: trieMap) {
					final int mapping_index = toBeMappedEntry.getValue();
					if(mapping[mapping_index]>=max_old_id){
						final String key = toBeMappedEntry.getKey();
						try {
							GlobalIDsGeneratorForIncrementalInsertion.this.dictionary.put(key, GlobalIDsGeneratorForIncrementalInsertion.this.dictionary.size() + 1); // start with index 1, as 0 is an error code in the dictionary
						} catch (final Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			}
		};
		threads[0].start();
		threads[1] = new Thread(){
			@Override
			public void run(){
				for(final Entry<String, Integer> toBeMappedEntry: trieMap) {
					final int mapping_index = toBeMappedEntry.getValue();
					if(mapping[mapping_index]>=max_old_id){
						final String key = toBeMappedEntry.getKey();
						try {
							GlobalIDsGeneratorForIncrementalInsertion.this.stringArray.add(key);
						} catch (final Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			}
		};
		threads[1].start();
		try {
			threads[0].join();
			threads[1].join();
		} catch (final InterruptedException e) {
			log.error(e.getMessage(), e);
		}
		final long end2 = System.currentTimeMillis();
		this.sum2+= end2-start2;
		this.tripleBlocksConsumer.consumeTriplesBlocks(triples, index);
	}

	@Override
	public void notifyEndOfProcessing() {
		this.times.add(new Tuple<String, Long>("No time: Number of entries in the dictionary", (long)this.dictionary.size()));
		this.times.add(new Tuple<String, Long>("Determine mapping of temporary to global IDs and map triples", this.sum1));
		this.times.add(new Tuple<String, Long>("Build Dictionary (both directions)", this.sum2));
		this.tripleBlocksConsumer.notifyEndOfProcessing();
	}
}
