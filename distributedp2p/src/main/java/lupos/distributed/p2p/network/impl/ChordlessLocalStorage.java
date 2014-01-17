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
package lupos.distributed.p2p.network.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import cx.ath.troja.chordless.ChordSet;
import cx.ath.troja.chordless.dhash.Entry;
import cx.ath.troja.chordless.dhash.storage.LockingStorage;
import cx.ath.troja.chordless.dhash.storage.NoSuchEntryException;
import cx.ath.troja.nja.Identifier;
import de.rwglab.p2pts.DHashService;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.p2p.storage.StorageWithDistributionStrategy;
import lupos.distributed.storage.IStorage;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.TriplePatternNotSupportedError;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;
import lupos.engine.operators.multiinput.join.parallel.ResultCollector;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This is the local storage for Chord, so each node has its own local storage
 * to get the entries which are stored locally on this node. This storage is
 * used in subgraph container, sent for sub-queries.
 * 
 * @author Bjoern
 * 
 * @param <T>
 *            The type of {@link KeyContainer}
 */
public class ChordlessLocalStorage<T> implements IStorage {
	private LockingStorage p2pStorage;
	private IDistribution<KeyContainer<T>> distribution;
	private DHashService p;
	private BindingsFactory bindings = BindingsFactory.createBindingsFactory();

	@Override
	public void setBindingsFactory(BindingsFactory bindingsFactory) {
		this.bindings = bindingsFactory;
	}
	
	/**
	 * Create a new local Storage for this peer, to get only triples stored on
	 * this p2p node, not in full network.
	 * 
	 * @param p
	 * 
	 * @param lockingStorage
	 *            The storage used by TomP2P
	 */
	public ChordlessLocalStorage(DHashService p, LockingStorage lockingStorage) {
		this.p = p;
		p2pStorage = lockingStorage;
	}

	/**
	 * Sets the distribution to be used on the local storage on this node
	 * 
	 * @param distribution
	 *            type of distribution strategy
	 * @return the local storage
	 */
	public ChordlessLocalStorage<T> setDistribution(
			IDistribution<KeyContainer<T>> distribution) {
		this.distribution = distribution;
		return this;
	}

	@Override
	public void endImportData() {
	}

	@Override
	public void addTriple(Triple triple) {
		// you shouldn't add triples to local storage
		throw new RuntimeException(
				"Is not allowed to add data to local storage.");
	}

	@Override
	public boolean containsTriple(Triple triple) {
		KeyContainer<T>[] keys = this.distribution.getKeysForStoring(triple);
		for (final KeyContainer<T> key : keys) {
			String newKey = StorageWithDistributionStrategy.getKey(key);
			// we need here only to check one existing, or?
			return getTriples(newKey).contains(triple);
		}
		return false;
	}

	/**
	 * Get the triples stored on the given key
	 * 
	 * @param locationKey
	 *            the key
	 * @return collection of found triples
	 */
	private Collection<Triple> getTriples(final String locationKey) {
		/*
		 * in chord, we have to ask for local triples in persist-executer,
		 * so implement as Future
		 */
		Future<Collection<Triple>> futureResult = this.p.getDhash()
				.getPersistExecutor()
				.submit(new Callable<Collection<Triple>>() {

					@SuppressWarnings("unchecked")
					@Override
					public Collection<Triple> call() throws Exception {
						try {
							/*
							 * get the entry, if available
							 */
							Entry res = p2pStorage.get(Identifier
									.generate(locationKey));
							if (res.getValueClassName().equals("cx.ath.troja.chordless.ChordSet")) {
								cx.ath.troja.chordless.ChordSet<Triple> s = (ChordSet<Triple>) res
										.getValue();
								return s;
							} else {
								Logger.getLogger(getClass()).warn("Unknown type: " + res.getValueClassName());
							}
							return new LinkedList<Triple>();
						} catch (NoSuchEntryException e) {
							/*
							 * no entry found in local storage, so return empty list!
							 */
							return new LinkedList<Triple>();
						}
					}
				});
		/*
		 * now wait for result of local storage in future-object
		 */
		try {
			return futureResult.get();
		} catch (InterruptedException e) {
			Logger.getLogger(getClass()).error(String.format("Interruption error while retrieving item with key=%s",locationKey),e);
		} catch (ExecutionException e) {
			Logger.getLogger(getClass()).error(String.format("Execution error while retrieving item with key=%s",locationKey),e);
		}
		return new LinkedList<Triple>();
	}

	/*
	 * get triple with the specified key as list
	 */
	private List<Triple> getTriple(String key) {
		List<Triple> t = new LinkedList<Triple>();
		for (Triple tr : getTriples(key)) {
			t.add(tr);
		}
		return t;
	}

	@Override
	public void remove(Triple triple) {
		throw new RuntimeException(
				"Not allowed to remove triple in local storage.");
	}

	
	private QueryResult evaluateTriplePatternAfterAdding(
			KeyContainer<T> keyContainer, TriplePattern triplePattern) {
		String newKey = StorageWithDistributionStrategy.getKey(keyContainer);
		List<Triple> foundTriples = this.getTriple(newKey);
		Logger.getLogger(getClass()).debug(
				String.format("%s - Got in %s%s the pattern %s : %s", this,
						keyContainer.type, keyContainer.key, triplePattern,
						Arrays.toString(foundTriples.toArray())));
		return tripleResultAsQueryResult(triplePattern, foundTriples);
	}

	private Bindings addVariablesToBindings(Item[] items, Triple t) {
		Bindings b = this.bindings.createInstance();
		for (int i = 0; i < items.length; i++) {
			Item item = items[i];
			if (item.getClass() == Variable.class) {
				Variable v = (Variable) item;
				b.add(v, t.getPos(i));

			} else {
				if (t.getPos(i)
						.compareToNotNecessarilySPARQLSpecificationConform(
								(Literal) item) != 0) {
					return null;
				}
			}
		}
		return b;
	}

	private QueryResult tripleResultAsQueryResult(TriplePattern triplePattern,
			List<Triple> foundTriples) {
		QueryResult result = QueryResult.createInstance();
		for (Triple t : foundTriples) {
			Bindings b = addVariablesToBindings(triplePattern.getItems(), t);
			if (b != null)
				result.add(b);
		}
		return result;
	}

	@Override
	public QueryResult evaluateTriplePattern(final TriplePattern triplePattern)
			throws Exception {
		try {
			final KeyContainer<T>[] keys = this.distribution
					.getKeysForQuerying(triplePattern);
			if (keys.length == 1) {
				return this.evaluateTriplePatternAfterAdding(keys[0],
						triplePattern);
			} else {
				// asynchronously retrieve the results...
				final ResultCollector resultCollector = new ResultCollector();
				resultCollector.setNumberOfThreads(keys.length);
				final Thread[] threads = new Thread[keys.length];
				for (int i = 0; i < keys.length; i++) {
					final KeyContainer<T> key = keys[i];
					threads[i] = new Thread() {
						@Override
						public void run() {
							resultCollector.process(ChordlessLocalStorage.this
									.evaluateTriplePatternAfterAdding(key,
											triplePattern), 0);
							resultCollector.incNumberOfThreads();
						}
					};
					threads[i]
							.setName(String
									.format("Parallel pattern evaluation: key=%s%s, pattern=%s",
											key.type, key.key, triplePattern));
					threads[i].start();
				}
				return resultCollector.getResult();
			}
		} catch (final TriplePatternNotSupportedError e) {
			throw new TriplePatternNotSupportedError(this.distribution,
					triplePattern);
		}
	}

	@Override
	public String toString() {
		return "Chordless LocalStorage";
	}
}
