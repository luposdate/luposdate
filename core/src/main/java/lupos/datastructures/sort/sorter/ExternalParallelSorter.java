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
import java.util.ArrayList;
import java.util.LinkedList;

import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.sort.helper.DataToBoundedBuffer;
import lupos.datastructures.sort.helper.InitialRunGenerator;
import lupos.datastructures.sort.run.Run;
import lupos.datastructures.sort.run.Runs;
import lupos.datastructures.sort.run.trie.TrieBagRuns;

/**
 * Class for sorting a collection of strings or the RDF terms of RDF data using.
 * This sorting algorithm is optimized for sorting in computers with large main memories.
 * Initial runs (up to a certain size) are generated by threads in main memory.
 * The initial runs are merged in main memory (in an own thread) and only intermediately stored on disk if there is no free main memory any more.
 * Finally all remaining runs are merged into one run...
 *
 * Runs can be trie sets, bags (using trie merge), or arrays (using normal merge or with duplicate elimination)
 *
 * @author groppe
 * @version $Id: $Id
 */
public class ExternalParallelSorter implements Sorter {
	
	/**
	 * Constructor to set a lot of parameters
	 *
	 * @param NUMBER_INITIAL_RUN_GENERATION_THREADS a int.
	 * @param NUMBER_ELEMENTS_IN_INITIAL_RUNS a int.
	 * @param NUMBER_OF_RUNS_TO_JOIN a int.
	 * @param FREE_MEMORY_LIMIT a long.
	 * @param runs a {@link lupos.datastructures.sort.run.Runs} object.
	 * @param deterministic a boolean.
	 */
	public ExternalParallelSorter(final int NUMBER_INITIAL_RUN_GENERATION_THREADS, final int NUMBER_ELEMENTS_IN_INITIAL_RUNS, final int NUMBER_OF_RUNS_TO_JOIN, final long FREE_MEMORY_LIMIT, final Runs runs, final boolean deterministic){
		this.NUMBER_INITIAL_RUN_GENERATION_THREADS = NUMBER_INITIAL_RUN_GENERATION_THREADS;
		this.NUMBER_ELEMENTS_IN_INITIAL_RUNS = NUMBER_ELEMENTS_IN_INITIAL_RUNS;
		this.NUMBER_OF_RUNS_TO_JOIN = NUMBER_OF_RUNS_TO_JOIN;
		this.PARAMETER_FOR_SWAPPING = FREE_MEMORY_LIMIT;
		this.runs = runs;
		this.deterministic = deterministic;
	}
	
	/**
	 * Default constructor using default parameters...
	 */
	public ExternalParallelSorter(){
		this(8, 1000, 2, 10, new TrieBagRuns(), true);
	}
	
	private final boolean deterministic;
	
	private final int NUMBER_INITIAL_RUN_GENERATION_THREADS;
	
	private final int NUMBER_ELEMENTS_IN_INITIAL_RUNS;
	
	private final int NUMBER_OF_RUNS_TO_JOIN;
	
	private final long PARAMETER_FOR_SWAPPING;

	private final LinkedList<Run> runsOnDisk = new LinkedList<Run>();
	
	private final Runs runs;
	
	/** {@inheritDoc} */
	@Override
	public Run sort(final InputStream dataFiles, final String format) throws Exception {
		final BoundedBuffer<String> buffer = new BoundedBuffer<String>();
		
		// initialize threads for generating initial runs
		final BoundedBuffer<Run> initialRunsLevel0 = new BoundedBuffer<Run>(Math.max(this.NUMBER_INITIAL_RUN_GENERATION_THREADS, this.NUMBER_OF_RUNS_TO_JOIN)*3);
		
		InitialRunGenerator[] initialRunGenerationThreads = new InitialRunGenerator[this.NUMBER_INITIAL_RUN_GENERATION_THREADS];
		
		for(int i=0; i<this.NUMBER_INITIAL_RUN_GENERATION_THREADS; i++){
			initialRunGenerationThreads[i] = new InitialRunGenerator(buffer, initialRunsLevel0, this.NUMBER_ELEMENTS_IN_INITIAL_RUNS, this.runs);
			initialRunGenerationThreads[i].start();
		}
		
		// start the merge thread...
		final ArrayList<LinkedList<Run>> levels = new ArrayList<LinkedList<Run>>();
		Merger merger = (this.deterministic)?
				new DeterministicMerger(initialRunsLevel0, levels):
				new MergerFreeMemory(initialRunsLevel0, levels);
		merger.start();

		// read in and parse the data...
		DataToBoundedBuffer.dataToBoundedBuffer(dataFiles, format, buffer);
		
		// signal that the all data is parsed (and nothing will be put into the buffer any more) 
		buffer.endOfData();
		
		// wait for threads to finish generating initial runs...  
		for(int i=0; i<this.NUMBER_INITIAL_RUN_GENERATION_THREADS; i++){
			try {
				initialRunGenerationThreads[i].join();
			} catch (InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		
		// start remaining merging phase...
		// signal no initial run will be generated any more
		initialRunsLevel0.endOfData();
		
		// wait for merger thread!
		try {
			merger.join();
		} catch (InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		
		// final merge phase: merge all runs (which are still in memory or stored on disk)
		// first collect all remaining runs!
		LinkedList<Run> runstoBeFinallyMerged = new LinkedList<Run>(this.runsOnDisk);
		for(LinkedList<Run> level: levels){
			runstoBeFinallyMerged.addAll(level);
		}
		Run result; 
		if(runstoBeFinallyMerged.isEmpty()){
			System.err.println("No runs there to be merged!");
			return null;
		} else if(runstoBeFinallyMerged.size()==1){
			// already merge in main memory?
			result = runstoBeFinallyMerged.get(0);
		} else {
			result = this.runs.merge(runstoBeFinallyMerged, false);
			for(Run run: this.runsOnDisk){
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
		return "Run-/Merging-Strategy:" + this.runs + 
			"\n" + (this.deterministic?"Deterministic":"Free-Memory Based") + " Swapping" +
			"\nNUMBER_INITIAL_RUN_GENERATION_THREADS:" + this.NUMBER_INITIAL_RUN_GENERATION_THREADS +			
			"\nNUMBER_ELEMENTS_IN_INITIAL_RUNS      :" + this.NUMBER_ELEMENTS_IN_INITIAL_RUNS +			
			"\nNUMBER_OF_RUNS_TO_JOIN               :" + this.NUMBER_OF_RUNS_TO_JOIN +			
			"\nPARAMETER_FOR_SWAPPING               :" + this.PARAMETER_FOR_SWAPPING;
	}
	
	public abstract class Merger extends Thread {
		
		protected final BoundedBuffer<Run> initialRunsLevel0;
		protected final ArrayList<LinkedList<Run>> levels;
		
		public Merger(final BoundedBuffer<Run> initialRunsLevel0, final ArrayList<LinkedList<Run>> levels){
			this.initialRunsLevel0 = initialRunsLevel0; 
			this.levels = levels;
		}
		
		@Override
		public void run(){

			try {
				while(true){
					// get as many runs to merge as specified
					Object[] bagsToMerge = this.initialRunsLevel0.get(ExternalParallelSorter.this.NUMBER_OF_RUNS_TO_JOIN, ExternalParallelSorter.this.NUMBER_OF_RUNS_TO_JOIN);
					
					if(bagsToMerge!=null && bagsToMerge.length>0){
						Run run = null;
						if(bagsToMerge.length>1){
							ArrayList<Run> toBeMerged = new ArrayList<Run>(bagsToMerge.length);
							for(Object bag: bagsToMerge){
								toBeMerged.add((Run) bag);
							}
							run = ExternalParallelSorter.this.runs.merge(toBeMerged, true);
						} else if(bagsToMerge.length==1) {
							run = (Run) bagsToMerge[0];
						}
						this.addToLevel(0, run);
					} else {
						// no new initial runs any more => merge thread finishes
						break;
					}
				}
			} catch (InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		
		/**
		 * This method adds merged runs (at a certain level) and merges the runs at this level if possible.
		 * Using levels has the effect that only ties of similar sizes are merged (and not large ones with small ones, which is not so fast (? to be checked!) because of an asymmetric situation)
		 * @param addLevel the level to which the merged run is added
		 * @param toBeAdded the merged run to be added
		 */
		public void addToLevel(final int addLevel, final Run toBeAdded) {
			
			if(this.checkSwapping(addLevel, toBeAdded)){
				return;
			}
			
			while(this.levels.size()<=addLevel){
				this.levels.add(new LinkedList<Run>());
			}
			LinkedList<Run> level = this.levels.get(addLevel);
			level.add(toBeAdded);
			while(level.size()>=ExternalParallelSorter.this.NUMBER_OF_RUNS_TO_JOIN){
				// we should merge the runs at this level
				
				ArrayList<Run> listOfRunsToBeMerged = new ArrayList<Run>(ExternalParallelSorter.this.NUMBER_OF_RUNS_TO_JOIN);
				
				while(listOfRunsToBeMerged.size()<ExternalParallelSorter.this.NUMBER_OF_RUNS_TO_JOIN){
					listOfRunsToBeMerged.add(level.remove());					
				}
				
				Run resultOfMerge = ExternalParallelSorter.this.runs.merge(listOfRunsToBeMerged, true);
				
				// put the merged run to one level higher (and recursively merge there the runs if enough are there)
				this.addToLevel(addLevel + 1, resultOfMerge);				
			}
			
			
		}
		
		/**
		 * Checks if the run to be added should be swapped to disk...
		 * @param addLevel the level to which the run should be added
		 * @param toBeAdded the run to be added
		 * @return true if the run to be added is swapped to disk, otherwise false (also in the case that another run is swapped to disk)
		 */
		public abstract boolean checkSwapping(final int addLevel, final Run toBeAdded);

	}
	
	public class MergerFreeMemory extends Merger{

		public MergerFreeMemory(BoundedBuffer<Run> initialRunsLevel0, ArrayList<LinkedList<Run>> levels) {
			super(initialRunsLevel0, levels);
		}

		/**
		 * check free memory and intermediately store one of the biggest runs on disk if there is not enough memory any more 
		 */
		@Override
		public boolean checkSwapping(int addLevel, Run toBeAdded) {
			boolean flag = false;
			// is there still enough memory available?
			if(Runtime.getRuntime().freeMemory()<ExternalParallelSorter.this.PARAMETER_FOR_SWAPPING){
				// get one of the biggest runs for storing it on disk and free it in memory...
				int levelnr=this.levels.size();
				do {
					levelnr--;
				} while(levelnr>0 && this.levels.get(levelnr).isEmpty());
				final Run runOnDisk;
				if(levelnr==0 || addLevel>=levelnr){
					System.err.println("ExternalParallelMergeSort: Heap space to low or FREE_MEMORY_LIMIT to high...");
					runOnDisk = toBeAdded;
					flag = true;
				} else {
					LinkedList<Run> lastLevel = this.levels.get(levelnr);
					runOnDisk = lastLevel.remove().swapRun();
					System.gc();
				}
				ExternalParallelSorter.this.runsOnDisk.add(runOnDisk);
			}
			return flag;
		}	
	}
	
	public class DeterministicMerger extends Merger{
		
		public DeterministicMerger(BoundedBuffer<Run> initialRunsLevel0, ArrayList<LinkedList<Run>> levels) {
			super(initialRunsLevel0, levels);
		}

		/**
		 * swap if a certain level is reached... 
		 */
		@Override
		public boolean checkSwapping(int addLevel, Run toBeAdded) {
			if(addLevel == ExternalParallelSorter.this.PARAMETER_FOR_SWAPPING){
				ExternalParallelSorter.this.runsOnDisk.add(toBeAdded.swapRun());
				return true;
			} else {
				return false;
			}
		}	
	}
}
