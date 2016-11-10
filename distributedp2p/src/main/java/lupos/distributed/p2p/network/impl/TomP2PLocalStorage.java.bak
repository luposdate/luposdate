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
package lupos.distributed.p2p.network.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number480;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageGeneric;
import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.p2p.distributionstrategy.AlternativeKeyContainer;
import lupos.distributed.p2p.network.impl.TomP2P.ITomP2PLog;
import lupos.distributed.p2p.storage.StorageWithDistributionStrategy;
import lupos.distributed.storage.IStorage;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.TriplePatternNotSupportedError;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;
import lupos.engine.operators.multiinput.join.parallel.ResultCollector;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This is the class for the P2P-networks private storage on each node. Here we
 * can retrieve local triples from node's storage to answer subgraph-requests.
 *
 * @author Bjoern
 * @param <T>
 *            KeyContainerType, esp. String
 * @version $Id: $Id
 */
public class TomP2PLocalStorage<T> implements IStorage {

	final static ITomP2PLog l = new TomP2PLog();
	private StorageGeneric p2pStorage;
	private IDistribution<KeyContainer<T>> distribution;
	private BindingsFactory bindings = BindingsFactory.createBindingsFactory();

	/**
	 * Create a new local Storage for this peer, to get only triples stored on
	 * this p2p node, not in full network.
	 *
	 * @param s
	 *            The storage used by TomP2P
	 */
	public TomP2PLocalStorage(StorageGeneric s) {
		p2pStorage = s;
	}

	/**
	 * <p>Setter for the field <code>distribution</code>.</p>
	 *
	 * @param distribution a {@link lupos.distributed.storage.distributionstrategy.IDistribution} object.
	 * @return a {@link lupos.distributed.p2p.network.impl.TomP2PLocalStorage} object.
	 */
	public TomP2PLocalStorage<?> setDistribution(
			IDistribution<KeyContainer<T>> distribution) {
		this.distribution = distribution;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public void endImportData() {
	}

	/** {@inheritDoc} */
	@Override
	public void addTriple(Triple triple) {
		// you shouldn't add triples to local storage
		throw new RuntimeException(
				"Is not allowed to add data to local storage.");

	}

	/** {@inheritDoc} */
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
	 * Get all triples stored at given key (as Collection)
	 *
	 * @param locationKey key
	 * @return all triples stored at this key
	 */
	public Collection<Triple> getTriples(String locationKey) {
		Number160 k = Number160.createHash(locationKey);
		Map<Number480, Data> all = p2pStorage.subMap(k);
		List<Triple> tmp = new LinkedList<Triple>();
		for (Data d : all.values()) {
			try {
				if (d.getObject() instanceof Triple) {
					tmp.add((Triple) d.getObject());
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return tmp;
	}

	/**
	 * Get all triples stored at given key (as List)
	 * @param locationKey key
	 * @return all triples stored at this key
	 */
	private List<Triple> getTriple(String key) {
		Number160 locationKey = Number160.createHash(key);
		;
		List<Triple> result = new ArrayList<Triple>();

		Map<Number480, Data> all = p2pStorage.subMap(locationKey);
		l.log("GET", String.format("GOT %d items in %s (%s)", all.values()
				.size(), key, locationKey), 10);
		for (Data d : all.values()) {
			try {
				if (d.getObject() instanceof Triple) {
					result.add((Triple) d.getObject());
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void remove(Triple triple) {
		throw new RuntimeException(
				"Not allowed to remove triple in local storage.");
	}

	private QueryResult evaluateTriplePatternAfterAdding(
			KeyContainer<T> keyContainer, TriplePattern triplePattern) {
		String newKey = StorageWithDistributionStrategy.getKey(keyContainer);
		List<Triple> foundTriples = this.getTriple(newKey);
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

	/** {@inheritDoc} */
	@SuppressWarnings("rawtypes")
	@Override
	public QueryResult evaluateTriplePattern(final TriplePattern triplePattern)
			throws Exception {
		try {
			final KeyContainer<T>[] keys = this.distribution
					.getKeysForQuerying(triplePattern);
			if (keys.length == 1
					&& (keys[0] instanceof AlternativeKeyContainer && (!((AlternativeKeyContainer) keys[0])
							.hasAlternative()))) {
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
						@SuppressWarnings("unchecked")
						@Override
						public void run() {
							resultCollector.process(TomP2PLocalStorage.this
									.evaluateTriplePatternAfterAdding(key,
											triplePattern), 0);
							if (key instanceof AlternativeKeyContainer
									&& ((AlternativeKeyContainer) key)
											.hasAlternative()) {
								AlternativeKeyContainer alternateKey = (AlternativeKeyContainer) key;
								for (KeyContainer kc : alternateKey
										.getAlternatives()) {
									resultCollector
											.process(
													TomP2PLocalStorage.this
															.evaluateTriplePatternAfterAdding(
																	kc,
																	triplePattern),
													0);
								}
							}
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "TomP2P LocalStorage";
	}

	/** {@inheritDoc} */
	@Override
	public void setBindingsFactory(BindingsFactory bindingsFactory) {
		this.bindings = bindingsFactory;
	}
}
