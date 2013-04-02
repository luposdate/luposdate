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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.items.Triple;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.storage.IStorage;
import lupos.engine.operators.multiinput.join.parallel.ResultCollector;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class inserts imported triples block-wise according to a given distribution strategy...
 * @param <K> the type of the keys as retrieved from the distribution strategy
 */
public abstract class BlockUpdatesStorageWithDistributionStrategy<K> implements IStorage {
	
	/**
	 * The block of triples to be inserted...
	 */
	protected HashMap<K, List<Triple>> toBeAdded = new HashMap<K, List<Triple>>();
	
	/**
	 * The distribution strategy
	 */
	protected final IDistribution<K> distribution;
	
	/**
	 * specifies how many triples are inserted at one time
	 */
	protected int blocksize = 1000;
	
	/**
	 * Constructor for the storage module
	 * @param distribution The distribution strategy to be used in this storage
	 */
	public BlockUpdatesStorageWithDistributionStrategy(final IDistribution<K> distribution){
		this.distribution = distribution;
	}

	@Override
	public void endImportData() {
		if(!this.toBeAdded.isEmpty()){
			// insert the whole block
			this.blockInsert();
		}
	}

	@Override
	public void addTriple(Triple triple) {
		K[] keys = distribution.getKeysForStoring(triple);
		for(K key: keys){
			List<Triple> triplesOfKey = this.toBeAdded.get(key);
			if(triplesOfKey == null){
				triplesOfKey = new ArrayList<Triple>(this.blocksize);
				this.toBeAdded.put(key, triplesOfKey);
			}
			triplesOfKey.add(triple);
			if(triplesOfKey.size()>this.blocksize){
				// a block is full => insert the whole block
				this.storeBlock(key, triplesOfKey);
				// delete block from buffer
				this.toBeAdded.remove(key);
			}
		}
	}

	@Override
	public boolean containsTriple(final Triple triple) {
		// first add remaining triples
		this.endImportData();
		final K[] keys = this.distribution.getKeysForStoring(triple);
		if(keys.length == 1) {
			return this.containsTripleAfterAdding(keys[0], triple);
		} else {
			// The containment of the triple is checked in parallel.
			// If one result is true, this is already returned,
			// otherwise it is waited until all have been checked
			// (and false is returned)
			ExecutorService executor = Executors.newFixedThreadPool(keys.length);
			@SuppressWarnings("unchecked")
			Future<Boolean>[] results = new Future[keys.length];
			for(int i=0; i<keys.length; i++) {
				final int index = i;
				results[i] = executor.submit(
						new Callable<Boolean>(){
							@Override
							public Boolean call() throws Exception {
								return containsTripleAfterAdding(keys[index], triple);
							}
				});
			}

			ParallelBooleanOr parallelBooleanOr = new ParallelBooleanOr(results);
			
			return parallelBooleanOr.getResult();
		}
	}

	@Override
	public void remove(final Triple triple) {
		final K[] keys = this.distribution.getKeysForStoring(triple);
		for(K key: keys){
			List<Triple> triplesOfKey = this.toBeAdded.get(key);
			if(triplesOfKey!=null){
				triplesOfKey.remove(triple);
			}
		}
		// add remaining triples
		this.endImportData();
		if(keys.length == 1){
			this.removeAfterAdding(keys[0], triple);
		} else {
			// remove triples in parallel...
			final Thread[] threads = new Thread[keys.length];
			for(int i=0; i<keys.length; i++) {
				final K key = keys[i];
				threads[i]  = new Thread() {
					@Override
					public void run() {
						removeAfterAdding(key, triple);
					}
				};
				threads[i].start();
			}
			for(int i=0; i<keys.length; i++) {
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public QueryResult evaluateTriplePattern(final TriplePattern triplePattern) throws Exception {
		// first add remaining triples
		this.endImportData();
		K[] keys = this.distribution.getKeysForQuerying(triplePattern);
		if(keys.length == 1) {
			return this.evaluateTriplePatternAfterAdding(keys[0], triplePattern);
		} else {
			// asynchronously retrieve the results...
			final ResultCollector resultCollector = new ResultCollector();
			resultCollector.setNumberOfThreads(keys.length);
			Thread[] threads = new Thread[keys.length];
			for(int i=0; i<keys.length; i++) {
				final K key = keys[i];
				threads[i] = new Thread() {
					@Override
					public void run() {
						resultCollector.process(evaluateTriplePatternAfterAdding(key, triplePattern), 0);
						resultCollector.incNumberOfThreads();
					}
				};
				threads[i].start();
			}			
			return resultCollector.getResult();
		}
	}
	
	/**
	 * This method implements the insertion of a block of triples (intermediately stored in toBeAdded)
	 */
	public void blockInsert() {
		for(Entry<K, List<Triple>> entry: this.toBeAdded.entrySet()){
			this.storeBlock(entry.getKey(), entry.getValue());
		}
		this.toBeAdded.clear();
	}

	/**
	 * This method must implement the insertion of a list of triples under a given key
	 * @param key the key under which the triples are stored
	 * @param triples the triples to be stored
	 */
	public abstract void storeBlock(final K key, List<Triple> triples);

	/**
	 * Checks whether or not a triple is contained in the distributed indices.
	 * This method is called after all pending triples are inserted...
	 * @param key the key under which the triple might be stored
	 * @param triple the triple to be checked
	 * @return true, if the triple is contained, false otherwise
	 */
	public abstract boolean containsTripleAfterAdding(K key, Triple triple);

	/**
	 * Removes a triple in the distributed indices.
	 * This method is called after all pending triples are inserted...
	 * @param key the key under which the triple must be removed
	 * @param triple the triple to e removed
	 */
	public abstract void removeAfterAdding(K key, Triple triple);

	/**
	 * Evaluates one triple pattern on the distributed indices.
	 * This method is called after all pending triples are inserted...
	 * @param key the key under which solutions for this triple pattern might be stored
	 * @param triplePattern the triple pattern to be evaluated
	 * @return the query result of the triple pattern
	 */
	public abstract QueryResult evaluateTriplePatternAfterAdding(K key, TriplePattern triplePattern);
	
	/**
	 * A class for waiting one of future boolean results becoming true (or determining false if all are false)
	 */
	public static class ParallelBooleanOr {
		
		/**
		 * the lock
		 */
		private ReentrantLock lock = new ReentrantLock();
		
		/**
		 * the condition variable for waiting for a specific condition and signaling if the condition might be reached
		 */
		private Condition condition = lock.newCondition();
		
		/**
		 * the future boolean results
		 */
		private final Future<Boolean>[] results;
		
		/**
		 * will be true if one of the future results is true
		 */
		private boolean result = false;
		
		/**
		 * The number of remaining tasks
		 */
		private int numberOfRemainingTasks;
		
		/**
		 * Constructor
		 * @param results the future boolean results to be checked
		 */
		public ParallelBooleanOr(final Future<Boolean>[] results){
			this.results = results;
			this.numberOfRemainingTasks = results.length;
		}
		
		/**
		 * waits for one true result or returns false if all results are false...
		 * @return true if one of results is true, otherwise false
		 */
		public boolean getResult(){
			Thread[] threads = new Thread[results.length];
			for(int i=0; i<threads.length; i++) {
				final int index = i;
				threads[i] = new Thread(){
					@Override
					public void run(){
						try {
							Boolean localResult = results[index].get();
							lock.lock();
							try {
								if(localResult.booleanValue()) {
									result = true;
								}
								numberOfRemainingTasks--;
								condition.signalAll();
							} finally {
								lock.unlock();
							}
						} catch (InterruptedException e) {
							System.err.println(e);
							e.printStackTrace();
						} catch (ExecutionException e) {
							System.err.println(e);
							e.printStackTrace();
						}
					}
				};
				threads[i].start();
			}
			lock.lock();
			try {
				// wait for one true result or until all jobs are done
				while(!result && numberOfRemainingTasks>0) {
					try {
						condition.await();
					} catch (InterruptedException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
			} finally {
				lock.unlock();
			}
			return result;
		}
	}
}
