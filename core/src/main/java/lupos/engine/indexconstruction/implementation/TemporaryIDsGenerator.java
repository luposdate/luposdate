package lupos.engine.indexconstruction.implementation;

import java.util.List;
import java.util.Map;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.patriciatrie.ram.RBTrieMap;
import lupos.engine.indexconstruction.interfaces.IConsumeTripleBlocksWithPatTrieMap;
import lupos.engine.indexconstruction.interfaces.ITripleConsumerWithEndNotification;
import lupos.misc.Tuple;

public class TemporaryIDsGenerator implements ITripleConsumerWithEndNotification {

	private final RBTrieMap<Integer> map = new RBTrieMap<Integer>();
	private final int[][] blockOfIdTriples;
	private int index = 0;
	private final int LIMIT_TRIPLES_IN_MEMORY;
	private final IConsumeTripleBlocksWithPatTrieMap consumeTriplesWithTemporaryIDs;
	private final List<Tuple<String, Long>> times;
	private long start;
	private long sum = 0L;
	private long sum2 = 0L;
	private long numberOfBlocks =0;
	private long numberOfReadTriples = 0;

	public TemporaryIDsGenerator(final Map<String, Object> configuration, final IConsumeTripleBlocksWithPatTrieMap consumeTriplesWithTemporaryIDs, final List<Tuple<String, Long>> times){
		this.LIMIT_TRIPLES_IN_MEMORY = (int) configuration.get("LIMIT_TRIPLES_IN_MEMORY");
		this.blockOfIdTriples = new int[this.LIMIT_TRIPLES_IN_MEMORY][];
		this.consumeTriplesWithTemporaryIDs = consumeTriplesWithTemporaryIDs;
		this.times = times;
		this.start = System.currentTimeMillis();
	}

	@Override
	public void consume(final Triple triple) {
		this.numberOfReadTriples++;
		final int[] idtriple = new int[3];
		int i=0;
		for(final Literal literal: triple){
			idtriple[i] = this.insertIntoMap(literal.toString());
			if(literal.originalStringDiffers()){
				this.insertIntoMap(literal.originalString());
			}
			i++;
		}
		this.blockOfIdTriples[this.index] = idtriple;
		this.index++;
		if(this.index>=this.LIMIT_TRIPLES_IN_MEMORY){
			this.endOfBlock();
		}
	}

	private final int insertIntoMap(final String value) {
		Integer code = this.map.get(value);
		if(code==null) {
			code = this.map.size();
			final long start = System.currentTimeMillis();
			this.map.put(value, code);
			final long end = System.currentTimeMillis();
			this.sum2 += end-start;
		}
		return code;
	}

	private final void endOfBlock(){
		final long end = System.currentTimeMillis();
		this.sum += end-this.start; // measure used time without succeeding steps!
		if(this.index>0){
			this.consumeTriplesWithTemporaryIDs.consumeTriplesBlocks(this.blockOfIdTriples, this.index, this.map);
			this.start = System.currentTimeMillis(); // measure time without succeeding steps
			this.numberOfBlocks++;
			this.index = 0; // start a new block of triples...
			// free resources of map in main memory
			this.map.clear();
		}
	}

	@Override
	public void notifyEndOfProcessing() {
		this.endOfBlock();
		this.times.add(new Tuple<String, Long>("No time: Number of read triples (may contain duplicates)", this.numberOfReadTriples));
		this.times.add(new Tuple<String, Long>("No time: Number of processed triple blocks (= number of runs)", this.numberOfBlocks));
		this.times.add(new Tuple<String, Long>("Parsing, building local patricia tries and generating temporary IDs", this.sum));
		this.times.add(new Tuple<String, Long>("  Building local patricia tries (contained in the previous time)", this.sum2));
		this.consumeTriplesWithTemporaryIDs.notifyEndOfProcessing();
	}
}
