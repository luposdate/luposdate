package lupos.engine.indexconstruction.implementation;

import java.util.List;

import lupos.datastructures.patriciatrie.TrieSet;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.datastructures.sorteddata.MapIteratorProvider;
import lupos.engine.indexconstruction.implementation.sorter.SecondaryConditionSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalIDsMapper extends Thread {

	private static final Logger log = LoggerFactory.getLogger(GlobalIDsMapper.class);

	private static List<? extends TrieSet> tries;
	private static MapIteratorProvider<String, Integer> simap;
	private static String dir;
	private static int index = 0;

	public static void setData(final List<? extends TrieSet> tries, final MapIteratorProvider<String, Integer> simap, final String dir){
		GlobalIDsMapper.tries = tries;
		GlobalIDsMapper.simap = simap;
		GlobalIDsMapper.dir = dir;
	}

	public static synchronized int getNextIndex(){
		final int result = index;
		GlobalIDsMapper.index++;
		return result;
	}

	@Override
	public void run(){
		int runNumber = GlobalIDsMapper.getNextIndex();
		while(runNumber<tries.size()){
			final TrieSet trie = tries.get(runNumber);
			// determine mapping
			final int[] mapping = new int[trie.size()];
			final SIPParallelIterator<java.util.Map.Entry<String, Integer>, String> iterator = simap.iterator();

			int local_id =0;
			for(final String key: trie){
				final java.util.Map.Entry<String, Integer> entry = iterator.next(key);
				if(entry==null || entry.getKey().compareTo(key)!=0){
					log.error("Local string not in global dictionary! Cannot be without any other error => Abort!");
					System.exit(0);
				}
				mapping[local_id] = entry.getValue();
				local_id++;
			}

			iterator.close();

			// local trie is not needed any more!
			trie.release();
			// map all 6 different initial runs of this trie
			final Thread[] threads = new Thread[6];
			for(int primaryPos=0; primaryPos<3; primaryPos++) {
				final int other_condition1 = (primaryPos==0)?1:0;
				final int other_condition2 = (primaryPos==2)?1:2;
				final String prefixFilename = dir + SecondaryConditionSorter.map[primaryPos] + "_Run_"+runNumber+"_";
				threads[primaryPos*2] = new LocalToGlobalIDMapper(prefixFilename + SecondaryConditionSorter.map[other_condition1] + SecondaryConditionSorter.map[other_condition2], mapping);
				threads[primaryPos*2].start();
				threads[primaryPos*2 + 1] = new LocalToGlobalIDMapper(prefixFilename + SecondaryConditionSorter.map[other_condition2] + SecondaryConditionSorter.map[other_condition1], mapping);
				threads[primaryPos*2 + 1].start();
			}

			// wait for the six threads for finishing their job (otherwise maybe too much memory consumption)
			for(final Thread thread: threads){
				try {
					thread.join();
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
			runNumber = GlobalIDsMapper.getNextIndex();
		}
	}
}