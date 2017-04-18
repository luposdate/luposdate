package lupos.engine.indexconstruction.implementation;

import java.util.List;
import java.util.Map;

import lupos.datastructures.patriciatrie.diskseq.DBSeqTrieSet;
import lupos.datastructures.sorteddata.MapIteratorProvider;
import lupos.engine.indexconstruction.interfaces.IGlobalIDsGenerator;
import lupos.engine.indexconstruction.interfaces.IInitialRunsMerger;
import lupos.misc.Tuple;

public class GlobalIDsGenerator implements IGlobalIDsGenerator {

	private final List<DBSeqTrieSet> listOfTries;
	private final String dir;
	private final IInitialRunsMerger initialRunsMerger;
	private final List<Tuple<String, Long>> times;

	public final static int NUMBER_OF_THREADS = 8;

	public GlobalIDsGenerator(final Map<String, Object> configuration, final List<DBSeqTrieSet> listOfTries, final IInitialRunsMerger initialRunsMerger, final List<Tuple<String, Long>> times){
		this.listOfTries = listOfTries;
		this.dir = (String) configuration.get("dir");
		this.initialRunsMerger = initialRunsMerger;
		this.times = times;
	}

	@Override
	public void generateGlobalIDs(final MapIteratorProvider<String, Integer> simap){
		final long start = System.currentTimeMillis();
		final int numberOfRuns = this.listOfTries.size();

		GlobalIDsMapper.setData(this.listOfTries, simap, this.dir);

		final GlobalIDsMapper[] mappers = new GlobalIDsMapper[GlobalIDsGenerator.NUMBER_OF_THREADS];
		for(int i=0; i<mappers.length; i++){
			mappers[i] = new GlobalIDsMapper();
			mappers[i].start();
		}

		for(final GlobalIDsMapper mapper: mappers){
			try {
				mapper.join();
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		final long end = System.currentTimeMillis();
		this.times.add(new Tuple<String, Long>("Generate Global IDs", end-start));
		this.initialRunsMerger.mergeInitialRuns(numberOfRuns);
	}

}
