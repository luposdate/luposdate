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
package lupos.datastructures.sort.run.trie;

import java.util.ArrayList;
import java.util.List;

import lupos.datastructures.patriciatrie.TrieBag;
import lupos.datastructures.patriciatrie.diskseq.DBSeqTrieBag;
import lupos.datastructures.patriciatrie.exception.TrieNotMergeableException;
import lupos.datastructures.patriciatrie.ram.RBTrieBag;
import lupos.datastructures.sort.run.Run;
import lupos.datastructures.sort.run.Runs;

/**
 * Tries are used to generate the initial runs.
 * Merging and swapping is done node-based.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class TrieBagRuns implements Runs {

	/** {@inheritDoc} */
	@Override
	public Run merge(List<Run> runs, final boolean inmemory) {
		ArrayList<TrieBag> triestoBeMerged = new ArrayList<TrieBag>(runs.size());
		for(Run run: runs){
			triestoBeMerged.add(((TrieBagRun)run).getTrie());
		}
		TrieBag result = (inmemory)? new RBTrieBag() : new DBSeqTrieBag(Run.getFilenameForNewRun());
		try {
			result.merge(triestoBeMerged);
		} catch (TrieNotMergeableException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return new TrieBagRun(result);
	}

	/** {@inheritDoc} */
	@Override
	public Run createRun() {
		return new TrieBagRun(TrieBag.createRamBasedTrieBag());
	}

	/** {@inheritDoc} */
	@Override
	public String toString(){
		return "Initial runs generated using trie bags (node-based merging)";
	}
}
