/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.datastructures.sort.sorter;

import java.io.InputStream;
import java.util.LinkedList;

import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.sort.helper.DataToBoundedBuffer;
import lupos.datastructures.sort.helper.InitialRunGenerator;
import lupos.datastructures.sort.run.Run;
import lupos.datastructures.sort.run.Runs;
import lupos.datastructures.sort.run.trie.TrieBagRuns;

/**
 * Class for sorting a collection of strings or the RDF terms of RDF data using.
 * Initial runs (up to a certain size) are generated in main memory and swapped to disk.
 * Reading in and parsing the data as well as swapping to disk is done in an asynchronous way.
 * Finally all swapped runs are merged into one run...
 *
 * Runs can be trie sets, bags (using trie merge), or arrays (using normal merge or with duplicate elimination)
 *
 * @author groppe
 * @version $Id: $Id
 */
public class ExternalSorter implements Sorter {
	private final int NUMBER_ELEMENTS_IN_INITIAL_RUNS;
	private final LinkedList<Run> runsOnDisk = new LinkedList<Run>();
	private final Runs runs;
	private final static int NUMBER_OF_RUNS_IN_BUFFER_FOR_SWAPPING = 3;

	/**
	 * <p>Constructor for ExternalSorter.</p>
	 *
	 * @param runs a {@link lupos.datastructures.sort.run.Runs} object.
	 * @param NUMBER_ELEMENTS_IN_INITIAL_RUNS a int.
	 */
	public ExternalSorter(final Runs runs, final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
		this.NUMBER_ELEMENTS_IN_INITIAL_RUNS = NUMBER_ELEMENTS_IN_INITIAL_RUNS;
		this.runs = runs;
	}

	/**
	 * <p>Constructor for ExternalSorter.</p>
	 */
	public ExternalSorter(){
		this(new TrieBagRuns(), 1000);
	}

	/** {@inheritDoc} */
	@Override
	public Run sort(final InputStream dataFiles, final String format) throws Exception {
		final BoundedBuffer<String> buffer = new BoundedBuffer<String>();

		// initialize threads for generating initial runs
		final BoundedBuffer<Run> initialRunsLevel0 = new BoundedBuffer<Run>(NUMBER_OF_RUNS_IN_BUFFER_FOR_SWAPPING);

		final InitialRunGenerator initialRunGenerationThread = new InitialRunGenerator(buffer, initialRunsLevel0, this.NUMBER_ELEMENTS_IN_INITIAL_RUNS, this.runs);

		initialRunGenerationThread.start();

		// start the swap thread...
		final Swapper swapper = new Swapper(initialRunsLevel0);
		swapper.start();

		// read in and parse the data...
		DataToBoundedBuffer.dataToBoundedBuffer(dataFiles, format, buffer);

		// signal that all the data is parsed (and nothing will be put into the buffer any more)
		buffer.endOfData();

		// wait for threads to finish generating initial runs...
			try {
				initialRunGenerationThread.join();
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}

		// start remaining merging phase...
		// signal no initial run will be generated any more
		initialRunsLevel0.endOfData();

		// wait for swapper thread!
		try {
			swapper.join();
		} catch (final InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		}

		// final merge phase: merge all runs (which are stored on disk)
		Run result;
		if(this.runsOnDisk.isEmpty()){
			System.err.println("No runs there to be merged!");
			return null;
		} else if(this.runsOnDisk.size()==1){
			// already merge in main memory?
			result = this.runsOnDisk.get(0);
		} else {
			result = this.runs.merge(this.runsOnDisk, false);
			for(final Run run: this.runsOnDisk){
				run.release();
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public int getNumberOfRunsOnDisk(){
		return this.runsOnDisk.size();
	}

	/** {@inheritDoc} */
	@Override
	public String parametersToString(){
		return "Run-/Merging-Strategy:" + this.runs + "\nNUMBER_ELEMENTS_IN_INITIAL_RUNS      :" + this.NUMBER_ELEMENTS_IN_INITIAL_RUNS;
	}

	/**
	 * This class is just to asynchronously swap runs to disk...
	 */
	public final class Swapper extends Thread {

		private final BoundedBuffer<Run> initialRunsLevel0;

		public Swapper(final BoundedBuffer<Run> initialRunsLevel0) {
			this.initialRunsLevel0 = initialRunsLevel0;
		}

		@Override
		public void run(){
			try {
				while(true){
					Run runToBeSwapped = this.initialRunsLevel0.get();
					if(runToBeSwapped==null){
						break;
					}
					final Run runOnDisk = runToBeSwapped.swapRun();
					runToBeSwapped = null;
					System.gc();
					ExternalSorter.this.runsOnDisk.add(runOnDisk);
				}
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}
}
