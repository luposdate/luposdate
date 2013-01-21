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
package lupos.test;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.patriciatrie.Trie;
import lupos.datastructures.patriciatrie.TrieBag;
import lupos.datastructures.patriciatrie.disk.DBTrieBag;
import lupos.datastructures.patriciatrie.disk.DBTrieSet;
import lupos.datastructures.patriciatrie.diskseq.DBSeqTrieBag;
import lupos.datastructures.patriciatrie.diskseq.DBSeqTrieSet;
import lupos.datastructures.patriciatrie.exception.TrieNotCopyableException;
import lupos.datastructures.patriciatrie.exception.TrieNotMergeableException;
import lupos.datastructures.patriciatrie.ram.RBTrieMap;
import lupos.datastructures.patriciatrie.ram.RBTrieSet;

public class TestTrie {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws TrieNotCopyableException 
	 */
	public static void main(String[] args) throws IOException, TrieNotCopyableException {
		DBTrieBag trie = new DBTrieBag("d:/test");
		trie.add("hallo");
		trie.add("hello");
		trie.add("bus");
		trie.add("bus");
		trie.add("pass");
		trie.add("passport");
		trie.add("passport2");
		trie.add("passport");
		trie.add("bumm");
		trie.add("bumbum");
		trie.add("bus");
		trie.add("hallo");
		trie.add("bus");
		trie.add("bum");
		trie.add("bumbum");
		
		System.out.println("Trie1:\n"+trie);
		
		DBTrieBag trie2 = new DBTrieBag("d:/test2");
		
		trie2.add("person");
		trie2.add("bum");
		trie2.add("bum");
		trie2.add("bumm");
		trie2.add("bums");
		trie2.add("bus");
		trie2.add("p");
		
		System.out.println("\nTrie2:\n"+trie2);
		
		DBSeqTrieBag testSeq = new DBSeqTrieBag("d:/test3");

		LinkedList<TrieBag> listOfTries = new LinkedList<TrieBag>();
		
		listOfTries.add(trie);
		listOfTries.add(trie2);
		
		try {
			testSeq.merge(listOfTries);
		} catch (TrieNotMergeableException e) {
			e.printStackTrace();
		}
		System.out.println("\nResult of merging:\n"+testSeq);
		Iterator<String> it = testSeq.keyIterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
		
		RBTrieMap<Float> trieF = new RBTrieMap<Float>();
		trieF.put("k", new Float(1.5));
		Float result = trieF.get("k");
		System.out.println(result);
	}	
}
