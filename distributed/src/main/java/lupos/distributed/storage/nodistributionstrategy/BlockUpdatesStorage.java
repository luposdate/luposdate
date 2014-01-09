/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.distributed.storage.nodistributionstrategy;

import java.util.HashSet;

import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Triple;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.storage.IStorage;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class inserts imported triples block-wise...
 * There is no distribution strategy assumed.
 */
public abstract class BlockUpdatesStorage implements IStorage {

	/**
	 * The block of triples to be inserted...
	 */
	protected HashSet<Triple> toBeAdded = new HashSet<Triple>();

	/**
	 * specifies how many triples are inserted at one time
	 */
	protected int blocksize = 1000;

	/**
	 * for creating Bindings
	 */
	protected BindingsFactory bindingsFactory;

	public BlockUpdatesStorage(final BindingsFactory bindingsFactory){
		this.bindingsFactory = bindingsFactory;
	}

	@Override
	public void endImportData() {
		if(!this.toBeAdded.isEmpty()){
			// insert the whole block
			this.blockInsert();
			this.toBeAdded.clear();
		}
	}

	@Override
	public void addTriple(final Triple triple) {
		this.toBeAdded.add(triple);
		if(this.toBeAdded.size()>this.blocksize){
			// a block is full => insert the whole block
			this.blockInsert();
			this.toBeAdded.clear();
		}
	}

	@Override
	public boolean containsTriple(final Triple triple) {
		// first add remaining triples
		this.endImportData();
		return this.containsTripleAfterAdding(triple);
	}

	@Override
	public void remove(final Triple triple) {
		this.toBeAdded.remove(triple);
		// add remaining triples
		this.endImportData();
		this.removeAfterAdding(triple);
	}

	@Override
	public QueryResult evaluateTriplePattern(final TriplePattern triplePattern) {
		// first add remaining triples
		this.endImportData();
		return this.evaluateTriplePatternAfterAdding(triplePattern);
	}

	@Override
	public void setBindingsFactory(final BindingsFactory bindingsFactory) {
		this.bindingsFactory = bindingsFactory;
	}

	/**
	 * This method must implement the insertion of a block of triples (intermediately stored in toBeAdded)
	 */
	public abstract void blockInsert();

	/**
	 * Checks whether or not a triple is contained in the distributed indices.
	 * This method is called after all pending triples are inserted...
	 * @param triple the triple to be checked
	 * @return true, if the triple is contained, false otherwise
	 */
	public abstract boolean containsTripleAfterAdding(Triple triple);

	/**
	 * Removes a triple in the distributed indices.
	 * This method is called after all pending triples are inserted...
	 * @param triple the triple to e removed
	 */
	public abstract void removeAfterAdding(Triple triple);

	/**
	 * Evaluates one triple pattern on the distributed indices.
	 * This method is called after all pending triples are inserted...
	 * @param triplePattern the triple pattern to be evaluated
	 * @return the query result of the triple pattern
	 */
	public abstract QueryResult evaluateTriplePatternAfterAdding(TriplePattern triplePattern);
}
