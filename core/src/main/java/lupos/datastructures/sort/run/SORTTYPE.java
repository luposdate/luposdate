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

import lupos.datastructures.dbmergesortedds.tosort.ToSort.TOSORT;
import lupos.datastructures.sort.run.memorysort.MemorySortRuns;
import lupos.datastructures.sort.run.trie.TrieBagRuns;
import lupos.datastructures.sort.run.trie.TrieSetRuns;
import lupos.datastructures.sort.run.trieWithStringMerging.TrieBagRunsWithStringMerging;
import lupos.datastructures.sort.run.trieWithStringMerging.TrieSetRunsWithStringMerging;

public enum SORTTYPE {
	SET {
		@Override
		public Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
			return new TrieSetRuns();
		}
	}, 
	BAG {
		@Override
		public Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
			return new TrieBagRuns();
		}
	}, 
	SETWITHSTRINGMERGING {
		@Override
		public Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
			return new TrieSetRunsWithStringMerging();
		}
	}, 
	BAGWITHSTRINGMERGING {
		@Override
		public Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
			return new TrieBagRunsWithStringMerging();
		}
	}, 
	MERGESORTSET {
		@Override
		public Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
			return new MemorySortRuns(TOSORT.MERGESORT, NUMBER_ELEMENTS_IN_INITIAL_RUNS, true);
		}
	}, 
	MERGESORTBAG {
		@Override
		public Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
			return new MemorySortRuns(TOSORT.MERGESORT, NUMBER_ELEMENTS_IN_INITIAL_RUNS, false);
		}
	}, 
	PARALLELMERGESORTSET {
		@Override
		public Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
			return new MemorySortRuns(TOSORT.PARALLELMERGESORT, NUMBER_ELEMENTS_IN_INITIAL_RUNS, true);
		}
	}, 
	PARALLELMERGESORTBAG {
		@Override
		public Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
			return new MemorySortRuns(TOSORT.PARALLELMERGESORT, NUMBER_ELEMENTS_IN_INITIAL_RUNS, false);
		}
	}, 
	QUICKSORTSET {
		@Override
		public Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
			return new MemorySortRuns(TOSORT.QUICKSORT, NUMBER_ELEMENTS_IN_INITIAL_RUNS, true);
		}
	}, 
	QUICKSORTBAG {
		@Override
		public Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
			return new MemorySortRuns(TOSORT.QUICKSORT, NUMBER_ELEMENTS_IN_INITIAL_RUNS, false);
		}
	}, 
	HEAPSORTSET {
		@Override
		public Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
			return new MemorySortRuns(TOSORT.HEAPSORT, NUMBER_ELEMENTS_IN_INITIAL_RUNS, true);
		}
	}, 
	HEAPSORTBAG {
		@Override
		public Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS){
			return new MemorySortRuns(TOSORT.HEAPSORT, NUMBER_ELEMENTS_IN_INITIAL_RUNS, false);
		}
	};
	
	public abstract Runs createRuns(final int NUMBER_ELEMENTS_IN_INITIAL_RUNS);
}