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
package lupos.datastructures.patriciatrie.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import lupos.datastructures.dbmergesortedds.StandardComparator;
import lupos.datastructures.patriciatrie.TrieSet;
import lupos.misc.util.AbstractSortedSet;

/**
 * This class is a wrapper, such that a given TrieSet object implements the SortedSet<String> interface.
 */
public class TrieSetImplementation extends AbstractSortedSet<String>{
	
	protected final TrieSet trie;
	
	public TrieSetImplementation(final TrieSet trie){
		this.trie = trie;
	}

	@Override
	public int size() {
		return this.trie.size();
	}

	@Override
	public boolean isEmpty() {		
		return this.size()==0;
	}

	@Override
	public boolean contains(Object o) {
		if(o instanceof String){
			return this.trie.getIndex((String)o)>=0;
		} else {
			return false;
		}
	}

	@Override
	public Iterator<String> iterator() {
		return this.trie.iterator();
	}

	@Override
	public boolean add(String key) {
		return this.trie.add(key);
	}

	@Override
	public boolean remove(Object o) {
		if(o instanceof String){
			return this.trie.remove((String) o);
		} else {
			return false;
		}
	}

	@Override
	public void clear() {
		this.trie.clear();
	}

	@Override
	public Comparator<? super String> comparator() {
		return new StandardComparator<String>();
	}

	@Override
	public String last() {
		return this.trie.get(this.size()-1);
	}

	@Override
	public SortedSet<String> subSet(String fromElement, String toElement,
			boolean inclusiveLastElement) {
		throw new UnsupportedOperationException();
	}
}
