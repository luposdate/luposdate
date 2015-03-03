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
import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.dbmergesortedds.Entry;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.sort.helper.DataToBoundedBuffer;
import lupos.datastructures.sort.run.Run;

/**
 * This sorting strategy implements Replacement Selection
 *
 * @author groppe
 * @version $Id: $Id
 */
public class ReplacementSelectionSorter implements Sorter {
	
	private final boolean set; 
	private final int height; // the height of the heap used to generate the initial runs (number of elements in main memory: 2^(height+1) - 1)
	private final int mergeHeapHeight_param; // the height of the heap used during merging (number of elements in main memory: 2^(mergeHeapHeight_param+1) - 1)
	private int numberOfRunsOndisk = 0; // the number of runs stored on disk (updated after sorting)
	
	/**
	 * <p>Constructor for ReplacementSelectionSorter.</p>
	 *
	 * @param set a boolean.
	 * @param height a int.
	 * @param mergeHeapHeight_param a int.
	 */
	public ReplacementSelectionSorter(final boolean set, final int height, final int mergeHeapHeight_param){
		this.set = set;
		this.height = height;
		this.mergeHeapHeight_param = mergeHeapHeight_param;
	}
	

	/** {@inheritDoc} */
	@Override
	public Run sort(InputStream dataFiles, String format) throws Exception {
		// initialize the data structure for replacement selection!
		SortConfiguration<Entry<String>> config = new SortConfiguration<Entry<String>>();
		config.useReplacementSelection(this.height, this.mergeHeapHeight_param);
		final DBMergeSortedBag<String> replacementSelectionDatastructure = (this.set)?
				new DBMergeSortedSet<String>(config, String.class):
				new DBMergeSortedBag<String>(config, String.class);
		final BoundedBuffer<String> buffer = new BoundedBuffer<String>(); // the buffer into which the read data is asynchronously put...
		Thread adder = new Thread(){
			// This thread just adds the asynchronously read strings to replacementSelectionDatastructure
			@Override
			public void run(){
				try {
					while(true){
						String toBeAdded = buffer.get();
						if(toBeAdded==null){
							break;
						}
						replacementSelectionDatastructure.add(toBeAdded);
					}
				} catch (InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		};
		adder.start();
		DataToBoundedBuffer.dataToBoundedBuffer(dataFiles, format, buffer);
		buffer.endOfData(); // signal that all has read
		adder.join(); // wait for the adder to finish its job (=adding all elements)
		replacementSelectionDatastructure.sort();
		this.numberOfRunsOndisk = replacementSelectionDatastructure.getNewId()-1; // this is a dirty trick to determine the number of runs stored on disk (as ids are generated for each new run)
		// the final Run just must have the functionality to return the iterator from replacementSelectionDatastructure
		return new Run(){

			@Override
			public boolean add(String toBeAdded) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Run sort() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Run swapRun() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isEmpty() {
				return replacementSelectionDatastructure.isEmpty();
			}

			@Override
			public Iterator<String> iterator() {
				return replacementSelectionDatastructure.iterator();
			}

			@Override
			public int size() {
				return replacementSelectionDatastructure.size();
			}			
		};
	}

	/** {@inheritDoc} */
	@Override
	public int getNumberOfRunsOnDisk() {
		return this.numberOfRunsOndisk;
	}

	/** {@inheritDoc} */
	@Override
	public String parametersToString() {
		return (this.set?"SET":"BAG")+"\nHeight of the heap:"+this.height+"\nHeight of the merge heap:"+this.mergeHeapHeight_param;
	}
}
