package lupos.engine.indexconstruction.implementation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import lupos.datastructures.patriciatrie.diskseq.DBSeqTrieSet;
import lupos.datastructures.patriciatrie.exception.TrieNotCopyableException;
import lupos.datastructures.patriciatrie.ram.RBTrieMap;
import lupos.engine.indexconstruction.interfaces.IConsumeTripleBlocks;
import lupos.engine.indexconstruction.interfaces.IConsumeTripleBlocksWithPatTrieMap;
import lupos.misc.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavePatTrie implements IConsumeTripleBlocksWithPatTrieMap {

	private final Collection<DBSeqTrieSet> listOfTries;
	private final IConsumeTripleBlocks consumeTripleBlocks;
	private final String dir;
	private final List<Tuple<String, Long>> times;
	private long sum = 0L;
	private long sum2 = 0L;

	private static int runNumber = 0;

	private static final Logger log = LoggerFactory.getLogger(SavePatTrie.class);

	public SavePatTrie(final Map<String, Object> configuration, final Collection<DBSeqTrieSet> listOfTries, final IConsumeTripleBlocks consumeTripleBlocks, final List<Tuple<String, Long>> times){
		this.listOfTries = listOfTries;
		this.dir = (String) configuration.get("dir");
		this.consumeTripleBlocks = consumeTripleBlocks;
		this.times = times;
	}

	@Override
	public void consumeTriplesBlocks(final int[][] triples, final int index, final RBTrieMap<Integer> trieMap) {
		final long start = System.currentTimeMillis();
		final int localRunNumber;
		final DBSeqTrieSet disk_set;
		synchronized(this.listOfTries){
			localRunNumber = runNumber;
			runNumber++;
			disk_set = new DBSeqTrieSet(this.dir+"Set_"+localRunNumber);
			this.listOfTries.add(disk_set);
		}

		this.consumeTripleBlocks.consumeTriplesBlocks(triples, index, trieMap.size(), localRunNumber);

		// write out patricia trie
		try {
			disk_set.copy(trieMap);
		} catch (final TrieNotCopyableException e) {
			log.error(e.getMessage(), e);
		}
		final long end = System.currentTimeMillis();
		this.sum+= end-start;

		this.consumeTripleBlocks.join();
		this.sum2+= System.currentTimeMillis()-start;
	}

	@Override
	public void notifyEndOfProcessing() {
		this.times.add(new Tuple<String, Long>("Save the local patricia tries. Time is parallel to succeeding step!", this.sum));
		this.consumeTripleBlocks.notifyEndOfProcessing();
		this.times.add(new Tuple<String, Long>("Save the local patricia tries and succeeding step together", this.sum2));
	}
}
