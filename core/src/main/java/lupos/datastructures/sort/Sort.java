/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.datastructures.sort;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.sort.run.Run;
import lupos.datastructures.sort.run.SORTTYPE;
import lupos.datastructures.sort.sorter.ExternalParallelSorter;
import lupos.datastructures.sort.sorter.ExternalSorter;
import lupos.datastructures.sort.sorter.ReplacementSelectionSorter;
import lupos.datastructures.sort.sorter.Sorter;
import lupos.engine.evaluators.QueryEvaluator;
import lupos.misc.FileHelper;
import lupos.misc.TimeInterval;

public class Sort {
	public enum SORTER {
		PARALLEL {

			@Override
			public Sorter createInstance(String[] args, int pos) {
				if(args.length==pos){
					return new ExternalParallelSorter();
				} else if(args.length==pos+6){
					final int NUMBER_ELEMENTS_IN_INITIAL_RUNS = Integer.parseInt(args[pos+3]);
					final String detPar = args[pos+1].toUpperCase();
					final boolean isDeterministic = 	detPar.compareTo("D")==0 				|| 
														detPar.compareTo("DET")==0 				|| 
														detPar.compareTo("DETERMINISTIC")==0;
					return new ExternalParallelSorter(Integer.parseInt(args[pos+2]), NUMBER_ELEMENTS_IN_INITIAL_RUNS, Integer.parseInt(args[pos+4]), Long.parseLong(args[pos+5]), SORTTYPE.valueOf(args[pos]).createRuns(NUMBER_ELEMENTS_IN_INITIAL_RUNS), isDeterministic);
				} else {
					return null;
				}
			}

			@Override
			public String getHelpText(final String indent) {				
				return "[SORTTYPE (D|F) NUMBER_INITIAL_RUN_GENERATION_THREADS NUMBER_ELEMENTS_IN_INITIAL_RUNS NUMBER_OF_RUNS_TO_JOIN PARAMETER_FOR_SWAPPING]\n" + indent + "SORTTYPE can be "+Arrays.toString(SORTTYPE.values());
			}

			@Override
			public String getExampleText() {
				return "D BAG 8 10000 2 10";
			}
			
		},
		ASYNCHRONOUS {

			@Override
			public Sorter createInstance(String[] args, int pos) {
				if(args.length==pos){
					return new ExternalSorter();
				} else if(args.length==pos+2){
					final int NUMBER_ELEMENTS_IN_INITIAL_RUNS = Integer.parseInt(args[pos+1]);
					return new ExternalSorter(SORTTYPE.valueOf(args[pos]).createRuns(NUMBER_ELEMENTS_IN_INITIAL_RUNS), NUMBER_ELEMENTS_IN_INITIAL_RUNS);
				} else {
					return null;
				}
			}

			@Override
			public String getHelpText(final String indent) {
				return "[SORTTYPE NUMBER_ELEMENTS_IN_INITIAL_RUNS]\n" + indent + "SORTTYPE can be "+Arrays.toString(SORTTYPE.values());
			}

			@Override
			public String getExampleText() {
				return "BAG 10000";
			}
			
		},
		REPLACEMENTSELECTION{

			@Override
			public Sorter createInstance(String[] args, int pos) {
				if(args.length==pos+3){
					return new ReplacementSelectionSorter(args[pos].toUpperCase().compareTo("SET")==0, Integer.parseInt(args[pos+1]), Integer.parseInt(args[pos+2]));
				} else {
					return null;
				}
			}

			@Override
			public String getHelpText(final String indent) {
				return "(SET|BAG) HEIGHT HEIGHT_OF_MERGEHEAP\n" + indent + "HEIGHT is the height of the heap during generating the initial runs\n" + indent + "HEIGHT_OF_MERGEHEAP is the height of the heap used during merging\n" + indent + "The number of elements stored in main memory is (2^(HEIGHT+1))-1 and (2^(HEIGHT_OF_MERGEHEAP))-1 respectively";
			}

			@Override
			public String getExampleText() {
				return "BAG 12 5";
			}
			
		};
		
		public abstract Sorter createInstance(final String[] args, final int pos);
		public abstract String getHelpText(final String indent);
		public abstract String getExampleText();
	}
	
	/**
	 * Main method to measure the execution time for different external sorting algorithms.
	 * @param args command line arguments
	 * @throws Exception in case of any errors
	 */
	public static void main(final String[] args) throws Exception{
		System.out.println("Sorting a large collection of Strings or RDF terms of large RDF data...");
		if(args.length<4){
			System.out.println(Sort.getHelpText());
			return;
		}
		SORTER sorter = SORTER.valueOf(args[0]);
		if(sorter==null){
			System.out.println(Sort.getHelpText());
			return;
		}
		Sorter algo = sorter.createInstance(args, 4);
		if(algo==null){
			System.out.println(Sort.getHelpText());
			return;
		}
		
		final int times = Integer.parseInt(args[3]);

		// just to use for deleting temporary files...  
		File file = new File("");
		String absolutePath = file.getAbsolutePath() + File.separator;

		System.out.println("\nParameters:\n-----------\nMain Strategy:" + sorter.name() + "\n" + algo.parametersToString() + "\n");
		
		long[] execution_times = new long[times];
		long total_time = 0;
		for(int t=0; t<times; t++){
			sorter = SORTER.valueOf(args[0]);
			if(sorter==null){
				System.out.println(Sort.getHelpText());
				return;
			}
			algo = sorter.createInstance(args, 4);
			if(algo==null){
				System.out.println(Sort.getHelpText());
				return;
			}
			
			Date start = new Date();
			System.out.println("\n"+t+": Start processing:"+start+"\n");
			
			Run result = algo.sort(new BufferedInputStream(new FileInputStream(args[1])), args[2]);
			
			// just access all elements in the bag by iterating one time through
			Iterator<String> it = result.iterator();
			int i=0;
			while(it.hasNext()){
				it.next();
				i++;
				// System.out.println((++i)+":"+it.next());
			}
			result.release();
			Date end = new Date();
			
			System.out.println("\n"+t+": End processing:"+end);		
			System.out.println("\nNumber of sorted RDF terms/Strings:"+i);
			System.out.println("Number of runs swapped to disk:" + algo.getNumberOfRunsOnDisk());
			
			execution_times[t] = end.getTime()-start.getTime();
			total_time += execution_times[t];
			
			FileHelper.deleteFilesStartingWithPattern(absolutePath, "Run");
			DiskCollection.removeCollectionsFromDisk();
			DBMergeSortedBag.removeBagsFromDisk();
		}
		
		long avg = total_time / times; 
			
		System.out.println("\nDuration:   " + QueryEvaluator.toString(execution_times) + " = " + (((double) total_time / times) / 1000) + " seconds\n          = " + new TimeInterval(avg));
		System.out.println("Sample Standard Deviation: " + (QueryEvaluator.computeSampleStandardDeviation(execution_times) / 1000) + " seconds");
		System.out.println("Standard Deviation of the Sample: " + (QueryEvaluator.computeStandardDeviationOfTheSample(execution_times) / 1000) + " seconds");
	}
	
	public static String getHelpText(){
		String result = "Call Sort in the following way:\n\njava lupos.datastructures.sort.Sort ALGO DATAFILE FORMAT TIMES SORTARGS\n\n";
		result += "ALGO can be one of " + Arrays.toString(SORTER.values()) + "\n";
		result += "DATAFILE contains the file with data (containing strings or RDF data)\n";
		result += "FORMAT can be STRING for a large collection of strings in one file, MULTIPLESTRING for a list of files containing strings to be read, BZIP2STRING and MULTIPLEBZIP2STRING for analogous, but BZIP2 compressed files, or an RDF format like N3\n";
		result += "TIMES is the number of repetitions to calculate an average execution time\n\n";
		result += "ALGO                   | SORTARGS\n";
		result += "--------------------------------------------------------------------------------------------------------------------------------------------------\n";
		for(SORTER sorter: SORTER.values()){
			result += sorter.name() + spaces(23-sorter.name().length()) + "| " + sorter.getHelpText("                       | ")+"\n";
		}
		result += "\nExamples:\n";
		for(SORTER sorter: SORTER.values()){
			result +="java -server -XX:+UseParallelGC -XX:+AggressiveOpts -Xms60G -Xmx60G lupos.datastructures.sort.sorter.Sort " + sorter.name() + " SomeFiles.txt MULTIPLEN3 10 " +sorter.getExampleText() + "\n";
		}
		return result;
	}
	
	private static String spaces(final int number){
		StringBuilder s=new StringBuilder();
		for(int i=0; i<number; i++){
			s.append(" ");
		}
		return s.toString();
	}
}