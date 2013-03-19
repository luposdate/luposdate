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
package lupos.datastructures.sort.run.trie;

import java.util.Iterator;

import lupos.datastructures.patriciatrie.TrieSet;
import lupos.datastructures.patriciatrie.diskseq.DBSeqTrieSet;
import lupos.datastructures.patriciatrie.exception.TrieNotCopyableException;
import lupos.datastructures.sort.run.Run;

public class TrieSetRun extends Run {

	private final TrieSet trie;
	
	public TrieSetRun(final TrieSet trie){
		this.trie = trie;
	}
	
	@Override
	public boolean add(String toBeAdded) {
		return this.trie.add(toBeAdded);
	}

	@Override
	public Run swapRun() {
		TrieSet diskbasedTrie = new DBSeqTrieSet(Run.getFilenameForNewRun());
		try {
			diskbasedTrie.copy(this.trie);
		} catch (TrieNotCopyableException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return new TrieSetRun(diskbasedTrie);
	}

	@Override
	public boolean isEmpty() {
		return this.trie.size()==0;
	}

	@Override
	public Run sort() {
		return this;
	}
	
	public TrieSet getTrie(){
		return this.trie;
	}

	@Override
	public Iterator<String> iterator() {		
		return this.trie.iterator();
	}

	@Override
	public int size() {
		return this.trie.size();
	}
	
	@Override
	public void release() {
		this.trie.release();
	}
}
