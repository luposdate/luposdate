package lupos.engine.indexconstruction.implementation.sorter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.heap.Heap.HEAPTYPE;
import lupos.engine.indexconstruction.interfaces.IIndicesGenerator;
import lupos.engine.indexconstruction.interfaces.IInitialRunsMerger;
import lupos.misc.FileHelper;
import lupos.misc.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialRunsMerger implements IInitialRunsMerger {

	private static final Logger log = LoggerFactory.getLogger(InitialRunsMerger.class);

	private final String dir;

	private final IIndicesGenerator indicesGenerator;

	private final List<Tuple<String, Long>> times;

	private volatile int numberOfTriples;

	public InitialRunsMerger(final Map<String, Object> configuration, final IIndicesGenerator indicesGenerator, final List<Tuple<String, Long>> times){
		this.dir = (String) configuration.get("dir");
		this.indicesGenerator = indicesGenerator;
		this.times = times;
	}

	@Override
	public void mergeInitialRuns(final int numberOfRuns){
		final long start = System.currentTimeMillis();
		// merge initial runs...
		int index = 0;
		final Thread[] threads = new Thread[6];
		final String finalDir = this.dir;
		for(int primaryPos=0; primaryPos<3; primaryPos++) {
			final int other_condition1 = (primaryPos==0)?1:0;
			final int other_condition2 = (primaryPos==2)?1:2;
			final int finalPrimaryPos = primaryPos;
			threads[index] = new Thread(){
				@Override
				public void run(){
					try {
						InitialRunsMerger.mergeRuns(finalDir, numberOfRuns, finalPrimaryPos, other_condition1, other_condition2);
					} catch (final IOException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
			};
			threads[index].start();
			index++;

			threads[index] = new Thread(){
				@Override
				public void run(){
					try {
						InitialRunsMerger.this.numberOfTriples = InitialRunsMerger.mergeRuns(finalDir, numberOfRuns, finalPrimaryPos, other_condition2, other_condition1);
					} catch (final IOException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
			};
			threads[index].start();
			index++;
		}
		for(final Thread thread: threads){
			try {
				thread.join();
			} catch (final InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
		final long end = System.currentTimeMillis();
		this.times.add(new Tuple<String, Long>("Merge intial runs", end-start));
		this.times.add(new Tuple<String, Long>("No time: Number of triples", (long) this.numberOfTriples));
		try {
			this.indicesGenerator.generateIndicesAndWriteOut(this.numberOfTriples);
		} catch (final Exception e) {
			log.error(e.getMessage(), e);
		}
		// finally delete the final runs...
		for(int primaryPos=0; primaryPos<3; primaryPos++) {
			final int other_condition1 = (primaryPos==0)?1:0;
			final int other_condition2 = (primaryPos==2)?1:2;
			final String prefix = finalDir + SecondaryConditionSorter.map[primaryPos] + "_Final_Run_";
			FileHelper.deleteFile(prefix + SecondaryConditionSorter.map[other_condition1] + SecondaryConditionSorter.map[other_condition2]);
			FileHelper.deleteFile(prefix + SecondaryConditionSorter.map[other_condition2] + SecondaryConditionSorter.map[other_condition1]);
		}
	}


	/**
	 * merges the initial runs into a final run...
	 *
	 * @param prefixFilename a {@link java.lang.String} object.
	 * @param numberOfRuns a int.
	 * @param primaryPos a int.
	 * @param secondaryPos a int.
	 * @param tertiaryPos a int.
	 * @throws java.io.IOException if any.
	 * @return a int.
	 */
	public static int mergeRuns(final String prefixFilename, final int numberOfRuns, final int primaryPos, final int secondaryPos, final int tertiaryPos) throws IOException {
		int size = 0;
		// use a heap for merging the smallest elements of the runs
		final Heap<HeapElementContainer> mergeHeap = Heap.createInstance(numberOfRuns, true, HEAPTYPE.OPTIMIZEDSEQUENTIAL);
		final IteratorFromRun[] iterators = new IteratorFromRun[numberOfRuns];
		for(int i=0; i<numberOfRuns; i++) {
			iterators[i] = new IteratorFromRun(prefixFilename + SecondaryConditionSorter.map[primaryPos] + "_Run_"+i+"_"+ SecondaryConditionSorter.map[secondaryPos] + SecondaryConditionSorter.map[tertiaryPos] + "_mapped");
			final int[] first = iterators[i].next();
			if(first!=null){
				mergeHeap.add(new HeapElementContainer(first, i));
			}
		}

		final FinalRunWriter out = new FinalRunWriter(prefixFilename + SecondaryConditionSorter.map[primaryPos] + "_Final_Run_"+ SecondaryConditionSorter.map[secondaryPos] + SecondaryConditionSorter.map[tertiaryPos]);
		int[] lastTriple = null;
		while(!mergeHeap.isEmpty()) {
			final HeapElementContainer element = mergeHeap.pop();
			final int run = element.getRun();
			final int[] toBeAdded = iterators[run].next();
			if(toBeAdded!=null){
				mergeHeap.add(new HeapElementContainer(toBeAdded, run));
			}
			final int[] triple = element.getTriple();
			if(lastTriple == null || lastTriple[0]!=triple[0] || lastTriple[1]!=triple[1] || lastTriple[2]!=triple[2]) {
				// store triple in final run
				out.write(triple);
				lastTriple = triple;
				size++;
			}
		}
		out.close();

		// delete all intermediate runs
		for(int i=0; i<numberOfRuns; i++) {
			FileHelper.deleteFile(prefixFilename + SecondaryConditionSorter.map[primaryPos] + "_Run_"+i+"_"+ SecondaryConditionSorter.map[secondaryPos] + SecondaryConditionSorter.map[tertiaryPos] + "_mapped");
		}
		return size;
	}

	/**
	 * This is a container for merging the initial runs...
	 */
	public static class HeapElementContainer implements Comparable<HeapElementContainer> {

		private final int[] triple;
		private final int run;

		public HeapElementContainer(final int[] triple, final int run){
			this.triple= triple;
			this.run = run;
		}

		@Override
		public final int compareTo(final HeapElementContainer o) {
			for(int pos=0; pos<3; pos++) {
				final int compare = this.triple[pos] - o.triple[pos];
				if(compare!=0){
					return compare;
				}
			}
			return 0;
		}

		public final int[] getTriple() {
			return this.triple;
		}

		public final int getRun() {
			return this.run;
		}
	}

}
