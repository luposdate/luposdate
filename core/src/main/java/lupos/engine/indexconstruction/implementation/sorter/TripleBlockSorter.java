package lupos.engine.indexconstruction.implementation.sorter;

import java.util.List;
import java.util.Map;

import lupos.engine.indexconstruction.interfaces.IConsumeTripleBlocks;
import lupos.misc.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripleBlockSorter implements IConsumeTripleBlocks {

	private static final Logger log = LoggerFactory.getLogger(TripleBlockSorter.class);

	private final String dir;
	private final List<Tuple<String, Long>> times;
	private long start;
	private long sum = 0L;

	private CountingSorter threadS;
	private CountingSorter threadP;
	private CountingSorter threadO;

	public TripleBlockSorter(final Map<String, Object> configuration, final List<Tuple<String, Long>> times){
		this.dir = (String) configuration.get("dir");
		this.times = times;
	}

	@Override
	public void consumeTriplesBlocks(final int[][] triples, final int index, final int maxID, final int runNumber) {
		this.start = System.currentTimeMillis();
		// sort id triples according to six collation orders and write them out as runs (in parallel)...
		this.threadS = new CountingSorter(triples, index, 0, this.dir + "S_Run_"+runNumber+"_", maxID);
		this.threadS.start();
		this.threadP = new CountingSorter(triples, index, 1, this.dir + "P_Run_"+runNumber+"_", maxID);
		this.threadP.start();
		this.threadO = new CountingSorter(triples, index, 2, this.dir + "O_Run_"+runNumber+"_", maxID);
		this.threadO.start();
	}

	@Override
	public void join() {
		try {
			this.threadS.join();
			this.threadP.join();
			this.threadO.join();
			final long end = System.currentTimeMillis();
			this.sum+= end-this.start;
		} catch (final InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void notifyEndOfProcessing() {
		this.times.add(new Tuple<String, Long>("Sort the blocks according to 6 collation orders and write out the runs...", this.sum));
	}
}
