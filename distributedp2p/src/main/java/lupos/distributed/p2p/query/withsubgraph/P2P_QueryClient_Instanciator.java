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
package lupos.distributed.p2p.query.withsubgraph;

import static com.google.common.base.Throwables.propagate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.distributed.p2p.distributionstrategy.SimplePartitionDistribution;
import lupos.distributed.p2p.network.AbstractP2PNetwork;
import lupos.distributed.p2p.network.P2PNetworkCreator;
import lupos.distributed.p2p.storage.StorageWithDistributionStrategy;
import lupos.distributed.query.QueryClient;
import lupos.distributed.query.QueryClientWithSubgraphTransmission;
import lupos.distributed.storage.IStorage;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;
import lupos.engine.evaluators.QueryEvaluator;
import lupos.optimizations.physical.PhysicalOptimizations;

/**
 * This is the query client to be configured in a static way. So for configuration
 * use {@link #lock()} and finally {@link #unlock()} to block and set the configuration.
 * With {@link #newInstance()} or {@link #newInstance(String[])} you can create the query client instance
 * that can be used.
 *
 * @author groppe
 * @version $Id: $Id
 */
public abstract class P2P_QueryClient_Instanciator extends QueryEvaluator<lupos.sparql1_1.Node>{

	/**
	 * new instance (not allowed to call)
	 *
	 * @throws java.lang.Exception throws always a Runtimejava.lang.Exception
	 */
	@Deprecated
	public P2P_QueryClient_Instanciator() throws Exception {
		throw new RuntimeException("Please use instanciating via P2P_QueryClient.newInstace(). This is only a QueryClient to be used in visual lupos query editor.");
	}

	/*
	 * do we need subgraph submission?
	 */
	private static boolean useSubgraphSubmission = true;
	@SuppressWarnings("rawtypes")
	private static IConfigurator<? super StorageWithDistributionStrategy> storageConfiguration;
	
	
	
	
	/**
	 * Sets the configuration for the storage
	 *
	 * @param cfg the configuration
	 */
	@SuppressWarnings("rawtypes")
	public static void setStorageConfiguration(IConfigurator<? super StorageWithDistributionStrategy> cfg) {
		storageConfiguration = cfg;
	}
	
	/**
	 * This interface is to be used to manually configure an instance, for example for {@link StorageWithDistributionStrategy}. 
	 * Because the settings have to be applied on a real existing instance, that is created in a hidden way,
	 * here is the possibility to configure the instance before using, but after instantiating.
	 * @author Bjoern
	 *
	 * @param <T> the type of instance to configure
	 */
	public static interface IConfigurator<T> {
		public void doConfiguration(T storage);
	}
	
	/*
	 * Invoke the constructor for creating the subgraph container with the given
	 * parameters.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static StorageWithDistributionStrategy invoke(String className, AbstractP2PNetwork r, IDistribution key,
			BindingsFactory bf) {
		
		try {
			/*
			 * get the class, which has to be a subclass of SubgraphContainer
			 */
			Class<?> c = Class.forName(className);
			if (!StorageWithDistributionStrategy.class.isAssignableFrom(c)) {
				throw new RuntimeException(
						String.format(
								"The type \"%s\" is not a class extended from %s",
								className,StorageWithDistributionStrategy.class));
			} else {
				Class<? extends StorageWithDistributionStrategy> c1 = (Class<? extends StorageWithDistributionStrategy>) c;
				try {
					Constructor<? extends StorageWithDistributionStrategy> construct = c1
							.getConstructor(AbstractP2PNetwork.class, IDistribution.class,BindingsFactory.class);
					return construct.newInstance(r, key,bf);
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(
							String.format(
									"The class \"%s\" has no valid constructor.",
									className));
				} catch (SecurityException e) {
					propagate(e);
				} catch (InstantiationException e) {
					propagate(e);
				} catch (IllegalAccessException e) {
					propagate(e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(
							String.format(
									"The type \"%s\" of subgraph-container has no valid constructor.",
									className));
				} catch (InvocationTargetException e) {
					propagate(e);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(
					String.format(
							"The class \"%s\" of subgraph-container is not known in actual class path. Cannot deserialize this subgraph container.",
							className));
		}
		/*
		 * if not possible or error occurred.
		 */
		return null;
	}
	
	/**
	 * <p>newInstance.</p>
	 *
	 * @return a {@link lupos.distributed.query.QueryClient} object.
	 */
	public static QueryClient newInstance() {
		return newInstance(new String[0]);
	}
	
	private static Logger log = Logger.getLogger(P2P_QueryClient_Instanciator.class);
	
	/**
	 * Creates a new instance of a query client
	 *
	 * @return a new instance
	 * @param args an array of {@link java.lang.String} objects.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static QueryClient newInstance(String[] args) {
		try {
			
			/*set literalfactory */
			Bindings.instanceClass = BindingsMap.class;
			LiteralFactory.setType(MapType.NOCODEMAP);
			LiteralFactory.setTypeWithoutInitializing(MapType.NOCODEMAP);
			
			
			final int instance = instanceCounter.incrementAndGet();
			if (bindings != null) Bindings.instanceClass = bindings;
			BindingsFactory bf = BindingsFactory.createBindingsFactory();
			
			
			
			String debugString = "\n";
			debugString += (String.format("Creating new P2P query client instance: %d \n",instance));
			AbstractP2PNetwork p2pInstance = networkInstance != null ? networkInstance : getP2PNetworkImplementation();
			if (networkInstance == null) {
				debugString += (String.format("P2P network: %s \nP2P configuration: %s \nP2P instance: %s \nhas local storage %s \n",p2pImplementationConstant,
							(p2pImplementationConfiguration != null) ? getP2PConfig(p2pImplementationConfiguration) : "{}",p2pInstance,
									p2pInstance.hasLocalStorage()));
			} else {
				debugString += (String.format("P2P network: %s \nP2P instance: %s \nhas local storage: %s \n",p2pInstance.getClass(),
						p2pInstance,p2pInstance.hasLocalStorage()));				
			}
			IDistribution distribution = getDistribution();
			debugString += (String.format("DistributionStrategy: %s [%s] \n",distribution,distribution.getClass()));	
			
			P2P_SubgraphExecuter sgExecuter = new P2P_SubgraphExecuter() {
				@Override
				public String toString() {
					return String.format("SubgraphExecuter for query client instance %s on node: %s \n",instance, p2p);
				}
			};
			
			StorageWithDistributionStrategy storageInstance = invoke(storageClass.getName(),p2pInstance,distribution,bf);
			if (storageConfiguration != null) {
				storageConfiguration.doConfiguration(storageInstance);
				debugString += (String.format("Storage: %s [%s] with manual configuration: %s \n",storageInstance,storageInstance.getClass(),storageConfiguration));	
				
			} else {
				debugString += (String.format("Storage: %s [%s] \n",storageInstance,storageInstance.getClass()));
			}
			
			P2P_SG_QueryClient_WithSubgraph queryClientInstance = new P2P_SG_QueryClient_WithSubgraph(storageInstance,sgExecuter) {
				@Override
				public String toString() {
					return String.format("QueryClient [%s]",instance);
				}
			};
			if (useSubgraphSubmission)
				debugString += (String.format("Query Client: %s with subgraph submission \n",queryClientInstance));
			else
				debugString += (String.format("Query Client: %s without subgraph submission \n",queryClientInstance));
			queryClientInstance.setUseSubgraphSubmission(useSubgraphSubmission);
			/*
			 * if a local storage is supported, use this for subgraph executer
			 */
			IStorage localStorage = (p2pInstance.hasLocalStorage()) ? p2pInstance.getLocalStorage(distribution) : storageInstance;
			sgExecuter.setStorage(localStorage);
			sgExecuter.setP2PNetwork(p2pInstance);
			/*
			 * create an query evaluator for the local storage, which is evaluated in local storage
			 */
			QueryEvaluator localEvaluator = new QueryClientWithSubgraphTransmission(localStorage,distribution,sgExecuter) {
				@Override
				public String toString() {
					return "LocalQueryExecuter for instance " + instance;
				}
			};
			Collection<URILiteral> defaultGraphs = new LinkedList<URILiteral>();
			// init with empty dataset ...
			LiteralFactory.setType(MapType.NOCODEMAP);
			defaultGraphs.add(LiteralFactory
					.createURILiteralWithoutLazyLiteral("<inlinedata:>"));
			Collection<URILiteral> namedGraphs = new LinkedList<URILiteral>();
			localEvaluator.prepareInputData(defaultGraphs, namedGraphs);
			queryClientInstance.prepareInputData(defaultGraphs, namedGraphs);
			try {
				PhysicalOptimizations.memoryReplacements();
			} catch (Exception e) {
				//ignore
			}
			//connect local storage evaluater in subgraph executer
			sgExecuter.setEvaluator(localEvaluator);
			log.info(debugString);
			return queryClientInstance;
		} catch (Exception e) {
			throw new RuntimeException("Cannot instanciate class:" + e.getMessage() );
		}
	}
	
	private static String getP2PConfig(
			Map<String, Object> p2pImplementationConfiguration2) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Entry<String, Object> e : p2pImplementationConfiguration2.entrySet()) {
			sb.append("\n\t").append(e.getKey()).append(" = ").append(e.getValue());
		}
		sb.append("\n}");
		return sb.toString();
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private P2P_SubgraphExecuter sg;
	@SuppressWarnings("rawtypes")
	private static AbstractP2PNetwork p2p;
	
	private static AbstractP2PNetwork<?> getP2PNetworkImplementation() {
		try {
			/*
			 * Instantiating the P2P network, specified via static setter
			 */
			if (p2pImplementationConfiguration != null) {
				return  P2PNetworkCreator.get(p2pImplementationConstant,p2pImplementationConfiguration);
			}else {
				return  P2PNetworkCreator.get(p2pImplementationConstant);
			}
		} catch (RuntimeException e) {
			Logger.getLogger(P2P_QueryClient_Instanciator.class).error("Error getting P2P network instance.",e);
			return null;
		}
	}

	/*
	 * returns the used distribution strategy
	 */
	private static IDistribution<KeyContainer<String>> getDistribution() {
		return p2pDistribution;
	}

	private static Map<String,Object> p2pImplementationConfiguration = new HashMap<String,Object>();
	
	/**
	 * Returns the map of configuration to be used for instanciation
	 *
	 * @return the map
	 */
	public static Map<String, Object> getP2PImplementationConfiguration() {
		return p2pImplementationConfiguration;
	}

	private static String p2pImplementationConstant = P2PNetworkCreator.TOM_P2P;
	private static IDistribution<KeyContainer<String>> p2pDistribution = new SimplePartitionDistribution();
	/*
	 * Lock to be used if changing static parameters during instantiating query client
	 */
	private static Lock l = new ReentrantLock();
	@SuppressWarnings("rawtypes")
	private static Class<? extends StorageWithDistributionStrategy> storageClass = StorageWithDistributionStrategy.class;
	
	/**
	 * Creates a lock, so that constants could be set for instantiation
	 */
	public static void lock() {
		l.lock();
	}
	
	/**
	 * Sets, whether subgraph submission is to be used or without
	 *
	 * @param enabled subgraph submission enabled
	 */
	public static void setSubgraphSubmission(boolean enabled) {
		useSubgraphSubmission = enabled;
	}
	
	/**
	 * Sets the P2P implementation constant
	 *
	 * @param p2pNetworkConstant the unique P2P key to be used in {@link lupos.distributed.p2p.network.P2PNetworkCreator#get(String)}
	 */
	public static void setP2PImplementationConstant(String p2pNetworkConstant) {
		p2pImplementationConstant = p2pNetworkConstant;
	}
	
	/**
	 * Sets the P2P configuration for the network
	 *
	 * @param cfg the configuration to set!
	 */
	public static void setP2PImplementationConfiguration(Map<String,Object> cfg) {
		p2pImplementationConfiguration = cfg;
	}
	
	/**
	 * Sets the distribution used in this query client.
	 *
	 * @param p2pDistributionType the distribution strategy
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void setP2PDistributionStrategy(IDistribution p2pDistributionType) {
		p2pDistribution = p2pDistributionType;
	}
	
	
	/**
	 * <p>setStorageType.</p>
	 *
	 * @param storage a {@link java.lang.Class} object.
	 */
	@SuppressWarnings("rawtypes")
	public static void setStorageType(Class<? extends StorageWithDistributionStrategy> storage) {
		if (storage != null)
			storageClass = storage;
	}
	
	
	/**
	 * Removes the lock set in {@link #lock()}
	 */
	public static void unlock() {
		l.unlock();
	}
	
	
	/*
	 * static counter incrementing if creating new instance
	 */
	private static AtomicInteger instanceCounter = new AtomicInteger();
	private static AbstractP2PNetwork<?> networkInstance;
	private static Class<? extends Bindings> bindings;

	/**
	 * Sets the network implementation to be used
	 *
	 * @param p2pNetwork the p2p network
	 */
	public static void setP2PNetwork(AbstractP2PNetwork<?> p2pNetwork) {
		networkInstance = p2pNetwork;
	}

	
	/**
	 * Sets the needed bindings class to be used
	 *
	 * @param b a {@link java.lang.Class} object.
	 */
	public static void setBindings(Class<? extends Bindings> b) {
		bindings = b;
	}

	
}
