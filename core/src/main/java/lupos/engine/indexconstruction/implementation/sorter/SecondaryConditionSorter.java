package lupos.engine.indexconstruction.implementation.sorter;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lupos.io.helper.OutHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sort routines for sorting according to the secondary and tertiary sort criterium...
 */
public class SecondaryConditionSorter extends Thread {

	private final int[][] blockOfSortedIdTriples;
	private final int primary_pos;
	private final int secondary_pos;
	private final int tertiary_pos;
	private final int[] borders;
	private final String fileName;

	private static final Logger log = LoggerFactory.getLogger(SecondaryConditionSorter.class);

	/**
	 *  just for mapping from 0 to 2 to S, P and O
	 */
	public final static String[] map = new String[]{"S", "P", "O"};

	private static final int NUMBER_OF_THREADS = 8;


	public SecondaryConditionSorter(final int[][] blockOfSortedIdTriples, final int primary_pos, final int secondary_pos, final int tertiary_pos, final int[] borders, final String filePrefix) {
		this.blockOfSortedIdTriples = blockOfSortedIdTriples;
		this.primary_pos = primary_pos;
		this.secondary_pos = secondary_pos;
		this.tertiary_pos = tertiary_pos;
		this.borders = borders;
		this.fileName = filePrefix + SecondaryConditionSorter.map[this.secondary_pos] + SecondaryConditionSorter.map[this.tertiary_pos] ;
	}

	@Override
	public void run() {
		final int[][] blockOfFinallySortedIdTriples = new int[this.blockOfSortedIdTriples.length][];
		final ExecutorService executor = Executors.newFixedThreadPool(SecondaryConditionSorter.NUMBER_OF_THREADS);
		int last_index = 0;
		for(int i=0; i<this.borders.length; i++){
			final int current_end = this.borders[i];
			executor.submit(new BasicSorter(this.blockOfSortedIdTriples, blockOfFinallySortedIdTriples, this.secondary_pos, this.tertiary_pos, last_index, current_end));
			last_index = current_end;
		}
		executor.shutdown();
		try {
			executor.awaitTermination(10, TimeUnit.DAYS);
		} catch (final InterruptedException e) {
			log.error(e.getMessage(), e);
		};

		// write out initial run:
		try (final OutputStream out = new BufferedOutputStream(new FileOutputStream(this.fileName))) {
			// write run in a compressed way:
			int start = 0;
			int previousPrimaryCode = 0;
			for(int i=0; i<this.borders.length; i++) {
				final int end = this.borders[i];
				previousPrimaryCode = SecondaryConditionSorter.writeBlock(blockOfFinallySortedIdTriples, start, end, this.primary_pos, this.secondary_pos, this.tertiary_pos, previousPrimaryCode, out);
				start = end;
			}
		} catch (final FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (final IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Write out a 'block'. A block has the same id at the primary position, and is sorted according to the secondary and tertiary sort criteria.
	 *
	 * @param block
	 * @param start
	 * @param end
	 * @param primaryPos
	 * @param secondaryPos
	 * @param tertiaryPos
	 * @param previousPrimaryCode
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public static int writeBlock(final int[][] block, final int start, final int end, final int primaryPos, final int secondaryPos, final int tertiaryPos, final int previousPrimaryCode, final OutputStream out) throws IOException{
		final int primaryCode = block[start][primaryPos];
		// difference encoding for the primary position
		OutHelper.writeLuposIntVariableBytes(primaryCode - previousPrimaryCode, out);

		// how many times this primary code is repeated?
		OutHelper.writeLuposIntVariableBytes(end-start, out);

		int previousSecondaryCode = 0;
		int previousTertiaryCode = 0;
		for(int j=start; j<end; j++) {
			final int secondaryCode = block[j][secondaryPos];
			// use difference encoding also for the secondary position
			final int differenceSecondaryPosition = secondaryCode - previousSecondaryCode;

			OutHelper.writeLuposIntVariableBytes(differenceSecondaryPosition, out);
			if(differenceSecondaryPosition>0){
				// difference encoding cannot be used for the tertiary position, as the secondary position changed
				previousTertiaryCode = 0;
			}
			final int tertiaryCode = block[j][tertiaryPos];
			OutHelper.writeLuposIntVariableBytes(tertiaryCode - previousTertiaryCode, out);

			previousSecondaryCode = secondaryCode;
			previousTertiaryCode = tertiaryCode;
		}
		return primaryCode;
	}
}
