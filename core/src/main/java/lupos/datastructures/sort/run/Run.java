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
package lupos.datastructures.sort.run;

import java.util.Iterator;

/**
 * This class is used to wrap different types of runs to be used in ExternalParallelSort (e.g. tries (bag/set), arrays of string to be sorted by merge sort, quicksort etc.) 
 */
public abstract class Run {
	
	/**
	 * an id for generating unique file names for the tries stored on disk...
	 */
	private static int trieID = 0;
	
	/**
	 * @return the file name for a new trie stored on disk
	 */
	public static String getFilenameForNewRun(){
		return "Run_"+(trieID++);
	}
	
	/**
	 * Add a string to this run
	 * @param toBeAdded the string to be added
	 * @return true if adding this element consumes more space in main memory
	 */
	public abstract boolean add(String toBeAdded);
	
	/**
	 * sorts the run and returns a sorted run
	 */
	public abstract Run sort();
	
	/**
	 * Writes this run to disk in order to free main memory
	 * @return a new run representing the run on disk
	 */
	public abstract Run swapRun();
	
	/**
	 * @return true, if the run is empty, otherwise false
	 */
	public abstract boolean isEmpty();
	
	/**
	 * @return an iterator for iterating through the added elements
	 */
	public abstract Iterator<String> iterator();
}
