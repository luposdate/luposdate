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
package lupos.datastructures.patriciatrie;

import java.util.List;

import lupos.datastructures.patriciatrie.exception.TrieNotMergeableException;
import lupos.datastructures.patriciatrie.node.NodeHelper;
import lupos.datastructures.patriciatrie.node.NodeWithValue;
import lupos.misc.Tuple;
public abstract class TrieMap<T> extends TrieWithValue<T> {

	/**
	 * Insert a value for a given key
	 *
	 * @param key the key for the value
	 * @param value the value for a given key
	 * @return the old value if a value for the given key is overwritten, <strong>null</strong> if no value for the given key was inside the patricia trie so far
	 */
	@SuppressWarnings("unchecked")
	public T put(final String key, final T value) {
		
		if (this.getRootNode() == null) {
			this.setRootNode(createNodeInstance());
		}
		Tuple<T, Boolean> result = NodeHelper.put((NodeWithValue<T>)this.getRootNode(), key, value); 
		return (result==null)? null: result.getFirst();
	}
	
	/**
	 * method to get the value of a key
	 *
	 * @param key the key of the value to be searched for
	 * @return the value of the key
	 */
	@SuppressWarnings("unchecked")
	public T get(final String key) {
		if (this.getRootNode() == null){
			return null;
		}
		
		T result = NodeHelper.get((NodeWithValue<T>)this.getRootNode(), key);
		return result;
	}
	
	/**
	 * Removes a key from the trie.
	 *
	 * @param key
	 *            Key to remove
	 * @return <strong>null</strong> if the key could not be removed (if the trie did not contain that key),
	 *         the old value otherwise.
	 */
	public T removeKey(final String key) {
		if (this.getRootNode() == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Tuple<T, Boolean> result = NodeHelper.removeKey((NodeWithValue<T>) this.getRootNode(), key);
		return (result==null)?null : result.getFirst();
	}
	
	/**
	 * Merges a list of tries into this trie.
	 *
	 * @param tries
	 *            List of tries
	 * @throws lupos.datastructures.patriciatrie.exception.TrieNotMergeableException if any.
	 */
	public void merge(final List<Trie> tries) throws TrieNotMergeableException {
		throw new TrieNotMergeableException();
	}

}
