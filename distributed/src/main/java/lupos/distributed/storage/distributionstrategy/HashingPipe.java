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
package lupos.distributed.storage.distributionstrategy;

import java.util.HashSet;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class transforms a set of keys to ids of the storage node.
 * @param <K> the type of keys to be transformed to ids of the storage node
 */
public class HashingPipe<K> implements IDistribution<Integer> {
	
	/**
	 * The underlying distribution strategy
	 */
	protected final IDistribution<K> distribution;

	/**
	 * the maximum number of storage nodes
	 */
	protected final int maximumNodes;
	
	/**
	 * Constructor
	 * @param distribution the underlying distribution strategy
	 * @param maximumNodes the maximum number of storage nodes
	 */
	public HashingPipe(final IDistribution<K> distribution, final int maximumNodes){
		this.distribution = distribution;
		this.maximumNodes = maximumNodes;
	}

	@Override
	public Integer[] getKeysForStoring(Triple triple) {
		return HashingPipe.transformsKeys(this.distribution.getKeysForStoring(triple), this.maximumNodes);
	}

	@Override
	public Integer[] getKeysForQuerying(TriplePattern triplePattern)
			throws TriplePatternNotSupportedException {
		return HashingPipe.transformsKeys(this.distribution.getKeysForQuerying(triplePattern), this.maximumNodes);
	}

	/**
	 * transforms a key array to an array of ids of the storage node (in the range of 0 to maximumNodes - 1)
	 * @param keys the key array to be transformed to an id array of storage nodes
	 * @param maximumNodes the maximum number of nodes
	 * @return the id array of storage nodes
	 */
	protected static<K> Integer[] transformsKeys(final K[] keys, final int maximumNodes) {
		// use hash set to eliminate duplicates!
		HashSet<Integer> set = new HashSet<Integer>();
		for(K key: keys){
			set.add(key.hashCode() % maximumNodes);
		}
		return set.toArray(new Integer[0]);
	}
}
