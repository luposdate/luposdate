package lupos.engine.indexconstruction.implementation;

import java.util.List;
import java.util.Map;

import lupos.datastructures.patriciatrie.TrieSet;
import lupos.datastructures.patriciatrie.diskseq.DBSeqTrieSet;
import lupos.datastructures.patriciatrie.exception.TrieNotCopyableException;
import lupos.datastructures.patriciatrie.exception.TrieNotMergeableException;
import lupos.engine.indexconstruction.interfaces.IDictionaryGenerator;
import lupos.engine.indexconstruction.interfaces.IEndOfProcessingNotification;
import lupos.misc.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrieMerger implements IEndOfProcessingNotification {

	private static final Logger log = LoggerFactory.getLogger(TrieMerger.class);
	private final List<DBSeqTrieSet> listOfTries;
	private final String dir;
	private final IDictionaryGenerator[] dictionaryGenerators;
	private final List<Tuple<String, Long>> times;

	public TrieMerger(final Map<String, Object> configuration, final List<DBSeqTrieSet> listOfTries, final IDictionaryGenerator[] dictionaryGenerators, final List<Tuple<String, Long>> times){
		this.listOfTries = listOfTries;
		this.dir = (String) configuration.get("dir");
		this.dictionaryGenerators = dictionaryGenerators;
		this.times = times;
	}

	@Override
	public void notifyEndOfProcessing() {
		final long start = System.currentTimeMillis();
		// merge local dictionaries
		final TrieSet final_trie = new DBSeqTrieSet(this.dir + "FinalTrie");
		try {
			if(this.listOfTries.size()>1){
				final_trie.merge(this.listOfTries);
			} else {
				final_trie.copy(this.listOfTries.get(0));
			}
		} catch (TrieNotMergeableException | TrieNotCopyableException e) {
			log.error(e.getMessage(), e);
		}
		final long middle = System.currentTimeMillis();
		this.times.add(new Tuple<String, Long>("Merge tries", middle-start));

		for(final IDictionaryGenerator dictionaryGenerator: this.dictionaryGenerators){
			dictionaryGenerator.generateDictionary(final_trie);
		}

		for(final IDictionaryGenerator dictionaryGenerator: this.dictionaryGenerators){
			dictionaryGenerator.join();
		}

		final long middle2 = System.currentTimeMillis();
		this.times.add(new Tuple<String, Long>("No time: Number of dictionary entries", (long)final_trie.size()));
		this.times.add(new Tuple<String, Long>("Total time of constructing both dictionary indices in parallel", middle2-middle));

		final_trie.release();
		final long end = System.currentTimeMillis();
		this.times.add(new Tuple<String, Long>("Time to clean up final trie", end-middle2));
	}
}
