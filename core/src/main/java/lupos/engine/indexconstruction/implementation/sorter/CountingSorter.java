package lupos.engine.indexconstruction.implementation.sorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sort the id-triples of the initial runs with counting sort according to the primary sort condition,
 * use quicksort to afterwards sort it according to the secondary and tertiary sort criteria
 * (e.g. first sort according to S, P and O, afterwards sort according to SPO and SOP based on the
 * sorting according to S, PSO and POS based on P, and OSP and OPS based on O.)
 */
public class CountingSorter extends Thread {

	private static final Logger log = LoggerFactory.getLogger(CountingSorter.class);

	private final int[][] blockOfIdTriples;
	private final int end;
	private final int pos;
	private final String filePrefix;
	private final int max_code;

	public CountingSorter(final int[][] blockOfIdTriples, final int end, final int pos, final String filePrefix, final int max_code) {
		this.blockOfIdTriples = blockOfIdTriples;
		this.end = end;
		this.pos = pos;
		this.filePrefix = filePrefix;
		this.max_code = max_code;
	}

	@Override
	public void run(){
		// start counting sort
		final int[] numberOfOccurences = new int[this.max_code];
		int numberOfBorders = 0;
		for(int i=0; i<this.end; i++){
			final int index = this.blockOfIdTriples[i][this.pos];
			if(numberOfOccurences[index]==0){
				numberOfBorders++;
			}
			numberOfOccurences[index]++;
		}
		// calculate addresses and borders...
		final int[] borders = new int[numberOfBorders];
		int index_borders = 0;
		if(numberOfOccurences[0]!=0) {
			borders[0] = numberOfOccurences[0];
			index_borders = 1;
		}
		for(int i=0; i < this.max_code - 1; i++){
			final boolean flag = (numberOfOccurences[i+1]>0);
			numberOfOccurences[i+1] = numberOfOccurences[i] + numberOfOccurences[i+1];
			if(flag) {
				borders[index_borders] = numberOfOccurences[i+1];
				index_borders++;
			}
		}
		// do sorting
		final int[][] blockOfSortedIdTriples = new int[this.end][];
		for(int i=0; i<this.end; i++){
			final int key = this.blockOfIdTriples[i][this.pos];
			blockOfSortedIdTriples[numberOfOccurences[key] - 1]=this.blockOfIdTriples[i];
			numberOfOccurences[key]--;
		}

		// now we have to do sorting according to the secondary and tertiary condition
		final int other_condition1 = (this.pos==0)?1:0;
		final int other_condition2 = (this.pos==2)?1:2;

		final Thread thread1 = new SecondaryConditionSorter(blockOfSortedIdTriples, this.pos, other_condition1, other_condition2, borders, this.filePrefix);
		thread1.start();
		final Thread thread2 = new SecondaryConditionSorter(blockOfSortedIdTriples, this.pos, other_condition2, other_condition1, borders, this.filePrefix);
		thread2.start();
		try {
			thread1.join();
			thread2.join();
		} catch (final InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}
}