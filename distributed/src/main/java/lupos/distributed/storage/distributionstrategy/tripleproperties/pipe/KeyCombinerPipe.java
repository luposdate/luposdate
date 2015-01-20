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
package lupos.distributed.storage.distributionstrategy.tripleproperties.pipe;

import lupos.datastructures.items.Triple;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.TriplePatternNotSupportedError;
import lupos.distributed.storage.distributionstrategy.tripleproperties.IDistributionKeyContainer;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class transforms KeyContainer consisting of the key type and the key itself
 * to one key of type String (by appending the key type and the key).
 *
 * It can be used as pipe between distribution strategies
 * whenever the storage nodes are accessible via a key (e.g., like in P2P networks)
 * (and LUPOPSDATE has no "direct" access to the storage nodes).
 */
public class KeyCombinerPipe implements IDistribution<String> {

	/**
	 * The inner distribution strategy
	 */
	protected final IDistributionKeyContainer<String> distribution;

	/**
	 * Constructor
	 * @param distribution the inner distribution strategy producing key containers which are transformed to string keys
	 */
	public KeyCombinerPipe(final IDistributionKeyContainer<String> distribution) {
		this.distribution = distribution;
	}

	@Override
	public String[] getKeysForStoring(final Triple triple) {
		return KeyCombinerPipe.transformToStringKeys(this.distribution.getKeysForStoring(triple));
	}

	@Override
	public String[] getKeysForQuerying(final TriplePattern triplePattern)
			throws TriplePatternNotSupportedError {
		return KeyCombinerPipe.transformToStringKeys(this.distribution.getKeysForQuerying(triplePattern));
	}

	/**
	 * Transforms an array of key containers to an array of string keys
	 * @param keys the key containers
	 * @return the transformed string keys
	 */
	protected static String[] transformToStringKeys(final KeyContainer<String>[] keys){
		final String[] result = new String[keys.length];
		for(int i=0; i<keys.length; i++){
			result[i] = keys[i].type + keys[i].key;
		}
		return result;
	}
}
