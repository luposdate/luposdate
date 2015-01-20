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
package lupos.distributed.p2p.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Triple;
import lupos.distributed.p2p.network.AbstractP2PNetwork;
import lupos.distributed.storage.IStorage;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;

/**
 * This is a {@link IStorage} implementation that is used for P2P networks, where
 * the triples are added and removed block-wise
 * @author Bjoern
 *
 * @param <T> The Type of Distribution Strategy, @see {@link StorageWithDistributionStrategy}
 */
public class BlockStorageWithDistributionStrategy<T> extends
		StorageWithDistributionStrategy<T> {

	/**
	 * Constructor as in {@link StorageWithDistributionStrategy} but for new
	 * instance of a {@link BindingsFactory}
	 * @param p2pImplementation P2P network
	 * @param distribution distribution strategy
	 */
	@Deprecated
	public BlockStorageWithDistributionStrategy(
			AbstractP2PNetwork<Triple> p2pImplementation,
			IDistribution<KeyContainer<T>> distribution) {
		super(p2pImplementation, distribution);
	}

	/**
	 * Constructor as in {@link StorageWithDistributionStrategy}
	 * @param p2pImplementation P2P network
	 * @param distribution distribution strategy
	 */
	public BlockStorageWithDistributionStrategy(
			AbstractP2PNetwork<Triple> p2pImplementation,
			IDistribution<KeyContainer<T>> distribution,
			BindingsFactory bindingsFactory) {
		super(p2pImplementation, distribution,bindingsFactory);
	}
	
	/**
	 * The block of triples to be inserted...
	 */
	protected Map<String,HashSet<Triple>> toBeAdded = new HashMap<String,HashSet<Triple>>();
	private Logger l = Logger.getLogger(BlockStorageWithDistributionStrategy.class);

	/**
	 * specifies how many triples are inserted at one time
	 */
	protected int blocksize = 100;
	
	@Override
	public void endImportData() {
		//iterate through each locationkey
		Set<String> keys = this.toBeAdded.keySet();
		for (String key : keys) {
			// if there are any items less to be inserted
			HashSet<Triple> set = this.toBeAdded.get(key);
			if(set != null && !set.isEmpty()){
				// insert the whole block
				this.blockInsert(key,set);
			}
		}
		this.toBeAdded.clear();
		super.endImportData();
	}

	@Override
	protected void remove(String key, Triple t) {
		mode_adding = false;
		addTriple(key,t);
	}
	
	private boolean mode_adding = true;
	
	@Override
	protected void addTriple(String key, Triple t) {
		// get the Set corresponding with this key
		HashSet<Triple> set = this.toBeAdded.get(key);
		// if no item available, create empty Set
		if (set == null) set = new HashSet<Triple>();
		// add the value
		set.add(t);
		// now check its size
		if(set.size()>this.blocksize){
			// a block is full => insert the whole block
			this.blockInsert(key,set);
			//and reset this with an empty Set
			set = new HashSet<Triple>();
		}
		toBeAdded.put(key, set);
	}

	
	private void blockInsert(String key, HashSet<Triple> set) {
		if (mode_adding) {
			l.debug(String.format("Block insert into key=%s %d triples",key,set.size()));
			this.p2p.addAll(key, set.toArray(new Triple[set.size()]));
		} else { 
			l.debug(String.format("Block remove from key=%s %d triples",key,set.size()));
			this.p2p.removeAll(key, set.toArray(new Triple[set.size()]));
		}
	}


	@Override
	public boolean containsTriple(Triple triple) {
		this.endImportData();
		return super.containsTriple(triple);
	}

	/**
	 * Sets the blockSize, that is to be used
	 * @param bSize the size
	 */
	public void setBlockSize(int bSize) {
		this.blocksize = bSize;
	}
	
	@Override
	public String toString() {
		return String.format("BlockStorage n=%d",blocksize);
	}
	
}
