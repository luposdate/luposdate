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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.p2p.network.AbstractP2PNetwork;
import lupos.distributed.storage.IStorage;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.TriplePatternNotSupportedError;
import lupos.engine.operators.multiinput.join.parallel.ResultCollector;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;

/**
 * This is the {@link lupos.distributed.storage.IStorage} used for P2P distribution. The class holds
 * connection to the p2p network for adding, removing and getting items.
 *
 * @author Bjoern
 * @param <T>
 *            The type of distribution used in {@link lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer}.
 * @see BlockStorageWithDistributionStrategy
 * @version $Id: $Id
 */
public class StorageWithDistributionStrategy<T> implements IStorage {

	/**
	 * The distribution strategy
	 */
	protected final IDistribution<KeyContainer<T>> distribution;
	/**
	 * the p2p network
	 */
	protected AbstractP2PNetwork<Triple> p2p;

	/**
	 * Returns the used distribution strategy
	 *
	 * @return the distribution strategy
	 */
	public IDistribution<KeyContainer<T>> getDistribution() {
		return this.distribution;
	}

	/**
	 * New storage for luposdate connected with a p2p-network implementation and
	 * the used distribution strategy with new bindings factory
	 *
	 * @param p2pImplementation
	 *            the p2p implementation
	 * @param distribution
	 *            the distribution strategy
	 */
	@Deprecated
	public StorageWithDistributionStrategy(
			AbstractP2PNetwork<Triple> p2pImplementation,
			IDistribution<KeyContainer<T>> distribution) {
		if (distribution == null || p2pImplementation == null)
			throw new RuntimeException(
					"No valid arguments in STORAGE-implementation.");
		this.distribution = distribution;
		this.p2p = p2pImplementation;
		this.bindingsFactory = BindingsFactory.createBindingsFactory();
	}
	
	/**
	 * New storage for luposdate connected with a p2p-network implementation and
	 * the used distribution strategy.
	 *
	 * @param p2pImplementation
	 *            the p2p implementation
	 * @param distribution
	 *            the distribution strategy
	 *            @param bindingsFactory the bindings factory
	 */
	public StorageWithDistributionStrategy(
			AbstractP2PNetwork<Triple> p2pImplementation,
			IDistribution<KeyContainer<T>> distribution,
			final BindingsFactory bindingsFactory) {
		if (distribution == null || p2pImplementation == null)
			throw new RuntimeException(
					"No valid arguments in STORAGE-implementation.");
		this.distribution = distribution;
		this.p2p = p2pImplementation;
		this.bindingsFactory = bindingsFactory;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void endImportData() {
		/*
		 * used if block-wise import, maybe for later versions
		 */
	}

	/** {@inheritDoc} */
	@Override
	public void addTriple(Triple triple) {
		KeyContainer<T>[] keys = this.distribution.getKeysForStoring(triple);
		for (final KeyContainer<T> key : keys) {
			/*
			 * store each key
			 */
			String newKey = getKey(key);
			addTriple(newKey, triple);
		}
	}

	/**
	 * <p>addTriple.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param t a {@link lupos.datastructures.items.Triple} object.
	 */
	protected void addTriple(String key, Triple t) {
		this.p2p.add(key, t);
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsTriple(Triple triple) {
		KeyContainer<T>[] keys = this.distribution.getKeysForStoring(triple);
		for (final KeyContainer<T> key : keys) {
			String newKey = getKey(key);
			// we need here only to check one existing, or?
			return this.p2p.contains(newKey, triple);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void remove(Triple triple) {
		KeyContainer<T>[] keys = this.distribution.getKeysForStoring(triple);
		for (final KeyContainer<T> key : keys) {
			String newKey = getKey(key);
			// we need here only to check one existing, or?
			/*
			 * we have to prevent double insertion of same triples!, then this
			 * suffers
			 */
			remove(newKey, triple);
		}
	}

	/**
	 * <p>remove.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param t a {@link lupos.datastructures.items.Triple} object.
	 */
	protected void remove(String key, Triple t) {
		this.p2p.remove(key, t);
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult evaluateTriplePattern(final TriplePattern triplePattern)
			throws Exception {
		l.debug("evaluateTriplePattern " + triplePattern);
		try {
			final KeyContainer<T>[] keys = this.distribution
					.getKeysForQuerying(triplePattern);
			if (keys.length == 1) {
				return this.evaluateTriplePatternAfterAdding(keys[0],
						triplePattern);
			} else {
				// fix
				final ResultCollector resultCollector = new ResultCollector();
	
				// asynchronously retrieve the results...
				resultCollector.setNumberOfThreads(keys.length);
				final Thread[] threads = new Thread[keys.length];
				for (int i = 0; i < keys.length; i++) {
					final KeyContainer<T> key = keys[i];
					threads[i] = new Thread() {
						@Override
						public void run() {
							resultCollector.process(
									StorageWithDistributionStrategy.this
											.evaluateTriplePatternAfterAdding(
													key, triplePattern), 0);
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
			return this.evaluateTriplePatternAfterAdding(triplePattern);
		}

	}

	/**
	 * <p>evaluateTriplePatternAfterAdding.</p>
	 *
	 * @param triplePattern a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @throws lupos.distributed.storage.distributionstrategy.TriplePatternNotSupportedError if any.
	 */
	public QueryResult evaluateTriplePatternAfterAdding(
			final TriplePattern triplePattern)
			throws TriplePatternNotSupportedError {
		throw new TriplePatternNotSupportedError(this.distribution,
				triplePattern);
	}

	/**
	 * produces a new key with the given keycontainer
	 *
	 * @param keyContainer
	 *            the key container
	 * @return new key = %type%.concat(%key%)
	 */
	public static String getKey(KeyContainer<?> keyContainer) {
		return String.format("%s%s", keyContainer.type, keyContainer.key);
	}

	private QueryResult evaluateTriplePatternAfterAdding(
			KeyContainer<T> keyContainer, TriplePattern triplePattern) {
		String newKey = getKey(keyContainer);
		List<Triple> foundTriples = this.p2p.get(newKey);
		return tripleResultAsQueryResult(triplePattern, foundTriples);
	}

	private QueryResult tripleResultAsQueryResult(TriplePattern triplePattern,
			List<Triple> foundTriples) {
		QueryResult result = QueryResult.createInstance();
		if (foundTriples != null)
			for (Triple t : foundTriples) {
				Bindings b = addVariablesToBindings(triplePattern.getItems(), t);
				if (b != null)
					result.add(b);
			}
		return result;
	}

	private Bindings addVariablesToBindings(Item[] items, Triple t) {
		
		Bindings b = bindingsFactory.createInstance();
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

	private static Logger l = LoggerFactory
			.getLogger(StorageWithDistributionStrategy.class);

	
	/**
	 * for creating bindings...
	 */
	protected BindingsFactory bindingsFactory;
	
	
	/** {@inheritDoc} */
	@Override
	public void setBindingsFactory(BindingsFactory bindingsFactory) {
		this.bindingsFactory = bindingsFactory;
	}
}
