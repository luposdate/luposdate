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
package lupos.distributed.p2p.commandline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.p2p.commandline.MenuSelector.InputValidator;
import lupos.distributed.p2p.commandline.MenuSelector.MenuItem;
import lupos.distributed.p2p.distributionstrategy.NinefoldInsertionDistribution;
import lupos.distributed.p2p.distributionstrategy.SimplePartitionDistribution;
import lupos.distributed.p2p.distributionstrategy.TwoPartitionsDistribution;
import lupos.distributed.p2p.gui.PeerCreator;
import lupos.distributed.p2p.network.P2PNetworkCreator;
import lupos.distributed.p2p.query.withsubgraph.P2P_QueryClient_Instanciator.IConfigurator;
import lupos.distributed.p2p.storage.BlockStorageWithDistributionStrategy;
import lupos.distributed.p2p.storage.StorageWithDistributionStrategy;
import lupos.distributed.query.QueryClient;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.IDistributionKeyContainer;
import lupos.distributed.storage.distributionstrategy.tripleproperties.OneKeyDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.OneToThreeKeysDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.TwoKeysDistribution;
import lupos.engine.evaluators.BasicIndexQueryEvaluator;
import lupos.gui.Demo_Applet;
import lupos.gui.GUI;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperBasicOperator;
import lupos.gui.operatorgraph.viewer.Viewer;

import org.apache.log4j.PropertyConfigurator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import xpref.XPref;

/**
 * This class is a command line runner for creating and joining nodes and
 * inserting triples and querying sparql.
 *
 * @author Bjoern
 * @version $Id: $Id
 */
public class StartConsole implements Runnable {

	@Option(name="-blockSize", usage="Specifies the block size to be used, if '-storage' is set to a 'BlockStorage' implementation")
	private int storageBlockSize = 0;

	@Option(name="-log", aliases={"-logging","-log4j"}, usage="Specifies the Log4j-properties for logging.")
	public String logFile = null;

	@Option(name="-fs",usage="Uses a filestorage on the network implementation, if available, so that not all processing is in memory.")
	public boolean usingFileStorage = false;

	@Option(name = "-cfg", aliases={"-config","-conf"}, usage = "Loads configuration from file (Key-Value-File with the needed parameters)")
	public String configFile = null;

	@Option(name = "-network", usage = "Sets the network implementation")
	public String networkImplementation = P2PNetworkCreator.TOM_P2P;

	@Option(name = "-p", usage = "Sets the port listening to")
	public int port = 15000;

	@Option(name = "-b", usage = "Sets the broadcast master ip-address")
	public String b_ip = null;

	@Option(name = "-bp", usage = "Sets the broadcast master port for joining the master node")
	public Integer b_port = null;

	@Option(name = "-manual", usage = "Activates the manual mode for setting configuration, all data set via input")
	public boolean manualMode = false;

	@Option(name = "-distrib", usage = "Sets the classname of the distribution strategy to be used")
	public String distributionStrategyClass = SimplePartitionDistribution.class
			.getName();

	@Option(name = "-storage", usage = "Sets the classname of the storage to be used")
	public String storageClass = StorageWithDistributionStrategy.class.getName();

	@Option(name = "-help", aliases = { "\\?", "\\help","-h" }, usage = "Shows the help screen")
	public boolean showHelp = false;

	@Option(name = "-showOperator", usage="Shows the operator graph in UI")
	private boolean showOperatorgraph = false;

	@Option(name = "-noSG_Submission", usage="If set, no subgraphs are submitted")
	private boolean useNoSGSubmission = false;

	@Option(name = "-percentageBuffer", usage="Percent of the memory used as buffer in LuposDate.")
	private Integer percentageBufferUsage = null;

	@SuppressWarnings("rawtypes")
	private lupos.distributed.p2p.query.withsubgraph.P2P_QueryClient_Instanciator.IConfigurator storageClassConfiguration;

	@SuppressWarnings("rawtypes")
	private static Class[] knownDistributionStrategyClasses = {OneKeyDistribution.class,TwoKeysDistribution.class,OneToThreeKeysDistribution.class,SimplePartitionDistribution.class,TwoPartitionsDistribution.class,NinefoldInsertionDistribution.class};




	private static String getP2PImplementationMenu() {
		final MenuSelector<String> menu = new MenuSelector<String>();
		menu.setTitle("Bitte wählen Sie die Implementierung des P2P-Netzes:");
		for (final String ss : P2PNetworkCreator.getKnownImplementations() ) {
			menu.addNumberedChoice(ss);
		}
		final MenuItem<String> result = menu.run();
		if (result == null) {
			return null;
		}
		return  result.getValue();
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void run() {
		Scanner s = null;
		if (this.manualMode) {
			/*
			 * read in input from console for configuration
			 */
			s = new Scanner(System.in);
			try {
				/*
				 * setup network implementation
				 */
				this.networkImplementation = getP2PImplementationMenu();
				/*
				 * setup port
				 */
				this.port = this.getPortMenu();

				/*
				 * setup broadcast
				 */
				final Boolean use_bootstrap = this.getBooleanMenu("Soll der neue Knoten mit einem existierenden P2P-Netzwerk verbunden werden?",false);

				if (use_bootstrap != null && use_bootstrap) {
					/*
					 * setup broadcast server
					 */
					final MenuSelector<String> stringMenu = new MenuSelector<String>();
					stringMenu.setTitle("Bitte gib den Servernamen/die IP-Adresse des Master-Peers an:");
					stringMenu.addCustomInput(new InputValidator<String>() {
						@Override
						public String isValid(final String input)
								throws RuntimeException {
							if (input != null) {
								return input;
							} else {
								throw new RuntimeException("Keine Eingabe.");
							}
						}
					});
					final MenuItem<String> result = stringMenu.run();
					if (result != null && result.isCustomInput()) {
						this.b_ip = result.getValue();
					}
					this.b_port = this.getPortMenu("Bitten geben Sie an, an welchem Port der Master-Peer auf eine Verbindung wartet.");
				}
				/*
				 * setup distribution strategy
				 */
				this.distributionStrategyClass = getDistributionStrategyMenu().getName();
				/* use pre-compiled test case ? */

				/*
				 * setup broadcast
				 */
				final Boolean use_Block = this.getBooleanMenu("Soll das Einfügen blockweise geschehen?",true);
				if (use_Block) {
					final MenuSelector<Integer> stringMenu = new MenuSelector<Integer>();
					stringMenu.setTitle("Bitte gib die Blockgröße an:");
					stringMenu.setDefaultValue("100",100);
					stringMenu.addCustomInput(new InputValidator<Integer>() {
						@Override
						public Integer isValid(final String input) throws RuntimeException {
							try {
								final Integer i = Integer.parseInt(input);
								if (i < 0 || i > Integer.MAX_VALUE) {
									throw new NumberFormatException("");
								}
								return i;
							} catch (final NumberFormatException e) {
								throw new RuntimeException(String.format("Die Eingabe %s liegt nicht im entsprechenden Bereich",input));
							}
						}
					});
					final MenuItem<Integer> result = stringMenu.run();
					int blockSize = 100;
					if (result != null && result.isCustomInput()) {
						blockSize = result.getValue();
					}
					final int bSize = blockSize;
					this.storageClass = BlockStorageWithDistributionStrategy.class.getName();
					this.storageClassConfiguration = new IConfigurator<BlockStorageWithDistributionStrategy>() {


						@Override
						public String toString() {
							return "blockSize = " + bSize;
						}
						@Override
						public void doConfiguration(
								final BlockStorageWithDistributionStrategy storage) {
							storage.setBlockSize(bSize);

						}
					};

				} else {
					this.storageClass = StorageWithDistributionStrategy.class.getName();
				}


			} catch (final Exception e) {
				/*
				 * ignore it :)
				 */
			}
		}

		/*
		 * if a config file is set, read-in the parameters
		 */
		if (this.configFile != null) {
			if (new File(this.configFile).exists()) {
				final Properties cfg = new Properties();
				try {
					cfg.load(new FileReader(this.configFile));
					if (cfg.containsKey("-network")) {
						this.networkImplementation = cfg.getProperty("-network");
					}
					if (cfg.containsKey("-p")) {
						this.port = Integer.parseInt(cfg.getProperty("-p"));
					}
					if (cfg.containsKey("-b")) {
						this.b_ip = (cfg.getProperty("-b"));
					}
					if (cfg.containsKey("-bp")) {
						this.b_port = Integer.parseInt(cfg.getProperty("-bp"));
					}
					if (cfg.containsKey("-distrib")) {
						this.distributionStrategyClass = (cfg
								.getProperty("-distrib"));
					}
					if (cfg.containsKey("-fs")) {
						this.usingFileStorage = true;
					}
					if (cfg.containsKey("-showOperator")) {
						this.showOperatorgraph = Boolean.parseBoolean(cfg.getProperty("-showOperator"));
					}
					if (cfg.containsKey("-storage")) {
						this.storageClass = cfg.getProperty("-storage");
					}
					if (cfg.containsKey("-percentageBuffer")) {
						this.percentageBufferUsage = Integer.parseInt(cfg.getProperty("-percentageBuffer"));
					}
					if (cfg.containsKey("-sgSubmission")) {
						this.useNoSGSubmission = Boolean.parseBoolean(cfg.getProperty("-sgSubmission"));
					}
					if (cfg.containsKey("-blockSize")) {
						this.storageBlockSize = Integer.parseInt(cfg.getProperty("-blockSize"));
					}
				} catch (final FileNotFoundException e) {
					System.err.println("Error opening config file: "
							+ this.configFile + ". File does not exist.");
				} catch (final IOException e) {
					System.err.println("Error accessing config file: "
							+ this.configFile + ". " + e.getMessage());
				} catch (final NumberFormatException e) {
					System.err.println("Error converting input into number: "
							+ e.getMessage());
				}
			}
		}

		if (this.logFile != null) {
			if (new File(this.logFile).exists()) {
				PropertyConfigurator.configureAndWatch( this.logFile, 60*1000 );
			}
		}
		if (this.percentageBufferUsage != null) {
			if (this.percentageBufferUsage >= 0 && this.percentageBufferUsage <= 100) {
				final long memoryForBuffer = (Runtime.getRuntime().maxMemory() * this.percentageBufferUsage) / 100;
				lupos.datastructures.buffermanager.BufferManager_RandomAccess.setMaxBytesInBuffer(memoryForBuffer);
				System.err.println("Given " + memoryForBuffer + " bytes");
			} else {
				System.err.println("Given % to use for buffering, is out of range 0-100.");
			}
		}

		System.out.println("Enable UI for graph visualization: " + this.showOperatorgraph);
		/*
		 * setup the peer
		 */
		final Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cPORT, this.port);
		if (this.usingFileStorage) {
		arguments.put(
				P2PNetworkCreator.P2PConfigurationConstants.cSTORAGE_PATH,
				"H:\\db\\result");
		}
		if (this.b_ip != null && this.b_port != null) {
			arguments.put(
					P2PNetworkCreator.P2PConfigurationConstants.cMASTER_IP,
					this.b_ip);
			arguments.put(
					P2PNetworkCreator.P2PConfigurationConstants.cMASTER_PORT,
					this.b_port);
		}

		IDistributionKeyContainer<?> distribution;
		// distributionStrategyClass = P2PHierarchyDistribution.class.getName();
		//
		// distributionStrategyClass =
		// OneToThreeKeysDistribution.class.getName();
		// distributionStrategyClass = OneKeyDistribution.class.getName();
		// distributionStrategyClass = P2PHierarchyDistribution.class.getName();
		//
		try {
			/*
			 * sets the distribution
			 */
			distribution = (IDistributionKeyContainer<?>) Class.forName(
					this.distributionStrategyClass).newInstance();
			final PeerCreator pc = new PeerCreator().setP2PNetwork(
					this.networkImplementation, arguments).setDistributionStrategy(
					distribution);
			pc.setUseSubgraphSubmission(!this.useNoSGSubmission);


			if (this.storageBlockSize != 0) {
				final int bSize = this.storageBlockSize;
				this.storageClassConfiguration = new IConfigurator<BlockStorageWithDistributionStrategy>() {
					@Override
					public String toString() {
						return "blockSize = " + bSize;
					}
					@Override
					public void doConfiguration(
							final BlockStorageWithDistributionStrategy storage) {
						storage.setBlockSize(bSize);

					}
				};
			}
			if (this.storageClass != null) {
				try {
					final Class<? extends StorageWithDistributionStrategy> c = (Class<? extends StorageWithDistributionStrategy>) Class.forName(this.storageClass);
					pc.setStorage(c,this.storageClassConfiguration);
				} catch (ClassCastException | ClassNotFoundException e) {
					System.err.println(String.format("Storage-implementation %s not found or not valid.",this.storageClass));
				}
			}


			pc.start();
			System.out.println("Started.");

			/*
			 * start the node, activates input mode on console
			 */
			if (!this.manualMode) {
				this.doQuerying(s, pc);
			} else {
				this.doQuerying(s, pc);
			}
		} catch (final InstantiationException e) {
			System.err
					.println("Error creating instance of distribution strategy:");
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			System.err
					.println("Error creating instance of distribution strategy:");
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			System.err
					.println("Error creating instance of distribution strategy:");
			e.printStackTrace();
		}
	}


	private Boolean getBooleanMenu(final String titel, final Boolean defaultValue) {
		final MenuSelector<Boolean> menu = new MenuSelector<Boolean>();
		menu.setTitle(titel);
		menu.addChoice("Y", true);
		menu.addChoice("N", false);
		final String symbol = (defaultValue == false) ? "N" : "Y";
		if (defaultValue != null) {
			menu.setDefaultValue(symbol,defaultValue);
		}
		final MenuItem<Boolean> result = menu.run();
		if (result != null) {
			return result.getValue();
		}
		return null;
	}

	private Integer getPortMenu() {
		return this.getPortMenu("Bitte geben Sie an, auf welchem Port der Peer hören soll [1000-65000]");
	}

	private Integer getPortMenu(final String inputMessage) {
		final MenuSelector<Integer> menu = new MenuSelector<Integer>();
		menu.setTitle(inputMessage);
		menu.setDefaultValue("11111",11111);
		menu.addCustomInput(new InputValidator<Integer>() {
			@Override
			public Integer isValid(final String input) throws RuntimeException {
				try {
					final Integer i = Integer.parseInt(input);
					if (i < 1000 || i > 65000) {
						throw new NumberFormatException("");
					}
					return i;
				} catch (final NumberFormatException e) {
					throw new RuntimeException(String.format("Die Eingabe %s liegt nicht im entsprechenden Bereich",input));
				}
			}
		});
		final MenuItem<Integer> result = menu.run();
		if (result == null) {
			return null;
		}
		return result.getValue();
	}


	/*
	 * inserts a n3-file to the datastore
	 */
	private void insertTripleFile(
			final BasicIndexQueryEvaluator basicIndexQueryEvaluator, String filename) {
		try {
			filename = Paths.get(filename).toString();
			System.out.println("Starting import of: " + filename);
			/*
			 * insert the file, process the time needed
			 */
			final long time = ((QueryClient) basicIndexQueryEvaluator)
					.prepareInputData(new String(filename));
			System.out.println("End of import in " + time + "ms.");
		} catch (final Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/*
	 * querying a file (sparql)
	 */
	private void query(final BasicIndexQueryEvaluator basicIndexQueryEvaluator,
			final String filename) {
		try {
			System.out.println("Starting query of file: " + filename);
			/*
			 * read in the file
			 */
			final List<String> lines = Files.readAllLines(
					new File(filename).toPath(), Charset.defaultCharset());
			String query = "";
			for (final String line : lines) {
				query += line + "\r\n";
			}
			System.out.println("Query: " + query);
			/*
			 * compile query with actual selected peer and do optimization
			 */
			long time = ((QueryClient) basicIndexQueryEvaluator)
					.compileQuery(query);
			time += basicIndexQueryEvaluator.logicalOptimization();
			time += basicIndexQueryEvaluator.physicalOptimization();

			if (this.showOperatorgraph) {
				try {
					XPref.getInstance(Demo_Applet.class
							.getResource("/preferencesMenu.xml"));
				} catch (final Exception e) {
					XPref.getInstance(new URL("file:"
							+ GUI.class.getResource("/preferencesMenu.xml")
									.getFile()));
				}
				new Viewer(new GraphWrapperBasicOperator(
						basicIndexQueryEvaluator.getRootNode()), "Query: "
						+ query, true, false);
			}

			time += ((QueryClient) basicIndexQueryEvaluator).evaluateQuery();
			/*
			 * result is back, so print it out, and the processing time
			 */
			final QueryResult result = ((QueryClient) basicIndexQueryEvaluator)
					.getResult();
			System.out.println("End of query in " + time + "ms.");
			System.out.println(result);
		} catch (final Exception e) {
			System.out.println("Error during query: " + e.getMessage());
		}

	}

	/**
	 * <p>getFileMenu.</p>
	 *
	 * @param title a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getFileMenu(final String title) {
		final MenuSelector<String> menu = new MenuSelector<String>();
		menu.addCustomInput(new InputValidator<String>() {
			@Override
			public String isValid(final String input) throws RuntimeException {
				if (checkFile(input)) {
					return input;
				} else {
					throw new RuntimeException("File does not exits: " + input);
				}
			}
		});
		menu.addChoice("A", "ABORT", "Abbruch");
		final MenuItem<String> result = menu.run();
		if (result.isCustomInput()) {
			return result.getValue();
		} else if (result.getValue().equals("ABORT")) {
			return null;
		} else {
			return null;
		}
	}

	/*
	 * functionality to do after the node has started (querying, inserting)
	 */
	private void doQuerying(Scanner s, final PeerCreator pc) {
		if (s == null) {
			s = new Scanner(System.in);
		}
		try {
			OUTER:
			while (true) {
				try {
					final MenuSelector<String> menu = new MenuSelector<String>();
					menu.setTitle("Peer gestartet. Was soll nun erledigt werden?");
					menu.addChoice("1", "INSERT", "Einfügen von Triplen");
					menu.addChoice("2", "QUERY", "Abfragen / Querying");
					menu.addChoice("3", "QUIT", "Abbruch / Beenden");
					final MenuItem<String> result = menu.run(s);
					/*
					 * insertion mode
					 */
					if (result != null) {
						switch (result.getValue()) {
						case "INSERT":
							final String insert = getFileMenu("Bitte geben Sie den Dateinamen der Datei an.");
							this.insertTripleFile(pc.getEvaluator(), insert);
							break;
						case "QUERY":
							final String query = getFileMenu("Bitte geben Sie den Dateinamen der Datei an.");
							this.query(pc.getEvaluator(), query);
							break;
						case "QUIT":
							System.out.println("Stopping network.");
							pc.getP2PNetworkImplementatino().shutdown();
							break OUTER;
						}
					}
				} catch (final Exception e) {
					e.printStackTrace();
					System.out.println("Error: " + e.getMessage());
				}
			}
		} finally {
			try {
				s.close();
			} catch (final Exception e) {
			}
		}
	}

	/*
	 * does the file exists?
	 */
	private static boolean checkFile(final String query) {
		if (!Files.exists(Paths.get(query))) {
			// you cann change here default lookup paths ...
			return false;
		}
		return true;
	}

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		/*
		 * Start and parse the arguments
		 */
		final StartConsole bean = new StartConsole();
		final CmdLineParser parser = new CmdLineParser(bean);
		try {
			parser.parseArgument(args);
			if (bean.showHelp) {
				parser.printUsage(System.out);
			} else {
				bean.run();
			}
			System.out.println("Ending program.");
		} catch (final CmdLineException e) {
			/* handling of wrong arguments */
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
		}
		System.exit(0);
	}


		@SuppressWarnings({ "rawtypes", "unchecked" })
		static Class<?extends IDistribution> getDistributionStrategyMenu() {
		final MenuSelector<Class<?extends IDistribution>> menu = new MenuSelector<Class<?extends IDistribution>>();
		menu.setTitle("Bitte wählen Sie eine Verteilungsstrategie aus, oder geben Sie eine Java-Klasse an, welche eine Verteilungsstrategie spezifiziert:");
		for (final Class c : knownDistributionStrategyClasses ) {
			menu.addNumberedChoice(c,c.getSimpleName());
		}
		menu.addCustomInput(new InputValidator<Class<? extends IDistribution>>() {
			@Override
			public Class<? extends IDistribution> isValid(final String input) throws RuntimeException {
				try {
					return (Class<? extends IDistribution>) Class.forName(input);
				} catch (final Exception e) {
					throw new RuntimeException(String.format("Die Klasse %s konnte im aktuellen Classpath nicht gefunden werden, oder ist keine Klasse die von IDistribution abgeleitet wurde.",input));
				}
			}
		});
		final MenuItem<Class<? extends IDistribution>> result = menu.run();
		if (result == null) {
			return null;
		}
		return result.getValue();
	}
}


