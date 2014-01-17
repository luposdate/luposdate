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
package lupos.distributed.p2p.commandline;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.p2p.distributionstrategy.NinefoldInsertionDistribution;
import lupos.distributed.p2p.network.P2PNetworkCreator;
import lupos.distributed.query.QueryClient;
import lupos.distributedendpoints.gui.PeerCreator;
import lupos.engine.operators.BasicOperator;
import lupos.gui.Demo_Applet;
import lupos.gui.GUI;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperBasicOperator;
import lupos.gui.operatorgraph.viewer.Viewer;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import xpref.XPref;

/**
 * Commandline tool, and starting main for processing and evaluating sparql-queries
 * in peer to peer network.<br>
 * <br>The following functionality is given<ul>
 * <li>Insert n3-files for storing triples in p2p-network
 * <li>Quering sparql-files for evaluating in p2p-network
 * <li>Specify network implementation and peer count
 * <li>Switching between started clients for multi processing
 * </ul>
 * @author Bjoern
 * @deprecated Use {@link StartConsole} instead, that is the new console-menu-application
 *
 */
@Deprecated
public class CommandLine {

	@Option(name = "-network", usage = "Set the network implementation")
	public String networkImplementation = P2PNetworkCreator.TOM_P2P;

	@Option(name = "-pc", usage = "Sets number of peers")
	public int peerCount = 1;

	@Option(name = "-p", usage = "Sets the first port in range [port,peerCount]")
	public int portStart = 17000;

	@Option(name = "-c", usage = "Sets the console mode for input after clients started.")
	public boolean scanMode = true;

	@Option(name = "-dp", usage = "Sets the default peer (0 until {numberOfPeers}).")
	private int actualPeer = 0;

	@Option(name = "-if", usage = "The file to be imported afterwards in default peer.")
	public String insertFile = null;

	@Option(name = "-qf", usage = "The file to be queried afterwards in default peer.")
	public String queryFile = null;

	/**
	 * Program entry method
	 * @param args arguments given
	 */
	public static void main(final String[] args) {
		final CommandLine bean = new CommandLine();
		final CmdLineParser parser = new CmdLineParser(bean);
		try {
			parser.parseArgument(args);
			bean.run();
			System.out.println("Ending program.");
		} catch (final CmdLineException e) {
			/* handling of wrong arguments */
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
		}
		System.exit(0);
	}

	/*
	 * store peers here
	 */
	private PeerCreator[] peers = null;

	private final boolean showOperatorgraph = true;

	/*
	 * run the main programm, commandline execution process
	 */
	private void run() {
		this.peers = new PeerCreator[this.peerCount];
		int port = this.portStart;
		PeerCreator p = new PeerCreator();
		/*
		 * starting the peer clients, with the specified incremented port
		 */
		for (int i = 0; i < this.peerCount; i++) {
			/* set the port via argument */
			final Map<String, Object> arguments = new HashMap<String, Object>();
			arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cPORT, port);
			if (i > 0) {
				arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cMASTER_PORT, this.portStart);
				arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cMASTER_IP, "127.0.0.1");
			}
			System.out.println(String.format("Starting %s on port %d",
					this.networkImplementation, port));
			/*
			 * start client
			 */
			p = new PeerCreator().setP2PNetwork(this.networkImplementation,
					arguments).setDistributionStrategy( new NinefoldInsertionDistribution()) .start();
			this.peers[i] = p;
			port++;
		}
		/*
		 * if n3-file is specified, import
		 */
		if (this.insertFile != null) {
			/*
			 * if file is a list of file, commar seperated, split and trim
			 */
			if (this.insertFile.indexOf(",") != -1) {
				final String[] filenames = this.insertFile.split(",");
				for (final String file : filenames) {
					this.insert(file.trim());
				}
			} else {
				this.insert(this.insertFile);
			}
		}
		/*
		 * if sparql-file for querying is specified
		 */
		if (this.queryFile != null) {
			/*
			 * if a set of files is specified via commar seperated list
			 */
			if (this.insertFile.indexOf(",") != -1) {
				final String[] filenames = this.insertFile.split(",");
				for (final String file : filenames) {
					this.insert(file.trim());
				}
			} else {
				this.query(this.queryFile);
			}
		}

		/*
		 * if wait-mode is on, wait for hard-end, but let the clients online
		 */
		if (!this.scanMode) {
			p.waitInfinite();
		} else {
			// open up standard input
			System.out
					.println("Console-mode started. Please enter your command or enter \"h\" for help, or exit via \"q\".");
			/*
			 * read in the standard input, until breaked by command "q"
			 */
			try (Scanner s = new Scanner(System.in)) {
				R: while (true) {

					String l = null;

					try {
						/*
						 * read the line
						 */
						l = s.nextLine();
						/*
						 * quitting
						 */
						if (l.equals("q")) {
							break R;
						}
						/*
						 * print help
						 */
						if (l.equals("h")) {
							this.help();
						}
						/*
						 * insert n3 file
						 */
						if (l.startsWith("insert")) {
							final String filename = l.substring("insert".length())
									.trim();
							if (l.indexOf(",") != -1) {
								final String[] filenames = filename.split(",");
								for (final String file : filenames) {
									this.insert(file.trim());
								}
							} else {
								this.insert(filename);
							}
						}
						/*
						 * query sparql file
						 */
						if (l.startsWith("query")) {
							final String filename = l.substring("query".length())
									.trim();
							if (l.indexOf(",") != -1) {
								final String[] filenames = filename.split(",");
								for (final String file : filenames) {
									this.query(file.trim());
								}
							} else {
								this.query(filename);
							}
						}
						/*
						 * select the query-client
						 */
						if (l.startsWith("select")) {
							final Matcher matcher = Pattern.compile("\\d+")
									.matcher(l);
							if (matcher.find()) {
								final int id = Integer.parseInt(matcher.group());
								if (id >= 0 && id < this.peers.length) {
									this.actualPeer = id;
									System.out.println("Actual peer: "
											+ this.actualPeer);
								} else {
									System.out
											.println("Choose a number between 0 (incl.) and "
													+ this.peers.length + " (excl.)");
								}
							} else {
								System.out.println("Actual client: " + this.actualPeer);
							}
						}
					} catch (final Exception ioe) {
						ioe.printStackTrace();
					}
				}
				System.out.println("Quitting...");
			}
		}
	}

	/*
	 * querying a file (sparql)
	 */
	@SuppressWarnings("resource")
	private void query(final String filename) {
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
			 * compile query with actual selected peer and
			 * do optimization
			 */
			long time = ((QueryClient) this.peers[this.actualPeer]
					.getEvaluator()).compileQuery(query);
			time += this.peers[this.actualPeer].getEvaluator().logicalOptimization();

			if (this.showOperatorgraph ) {
				try {
					XPref.getInstance(Demo_Applet.class
							.getResource("/preferencesMenu.xml"));
				} catch (final Exception e) {
					XPref.getInstance(new URL("file:"
							+ GUI.class.getResource("/preferencesMenu.xml")
									.getFile()));
				}
				new Viewer(new GraphWrapperBasicOperator( this.peers[this.actualPeer].getEvaluator().getRootNode()),
						"Logical Opt. Query: " + query, true, false);
			}

			new Scanner(System.in).nextLine();

			time += this.peers[this.actualPeer].getEvaluator().physicalOptimization();

			final BasicOperator rootNode = this.peers[this.actualPeer].getEvaluator().getRootNode();
			System.out.println(rootNode);
			if (this.showOperatorgraph ) {
				try {
					XPref.getInstance(Demo_Applet.class
							.getResource("/preferencesMenu.xml"));
				} catch (final Exception e) {
					XPref.getInstance(new URL("file:"
							+ GUI.class.getResource("/preferencesMenu.xml")
									.getFile()));
				}
				new Viewer(new GraphWrapperBasicOperator( this.peers[this.actualPeer].getEvaluator().getRootNode()),
						"Physical Opt: " + query, true, false);
			}



			/*
			 * result is back, so print it out, and the processing time
			 */
			final QueryResult result = ((QueryClient) this.peers[this.actualPeer]
					.getEvaluator()).getResult();
			System.out.println("End of query in " + time + "ms.");
			System.out.println(result);
		} catch (final Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}

	}

	/*
	 * inserts a n3-file to the datastore
	 */
	private void insert(final String filename) {
		try {
			System.out.println("Starting import of: " + filename);
			/*
			 * insert the file, process the time needed
			 */
			final long time = ((QueryClient) this.peers[this.actualPeer].getEvaluator())
					.prepareInputData(new String(filename));
			System.out.println("End of import in " + time + "ms.");
		} catch (final Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	/*
	 * prints out the help screen
	 */
	private void help() {
		System.out.println("Help-Screen / Command Overview");
		System.out.println("----------------------------------");
		System.out.println("h\t\t:\t Help screen");
		System.out.println("q\t\t:\t Quit");
		System.out.println("select {num}\t:\t Select client {num}");
		System.out
				.println("insert {file}\t:\t Inserts n3-{file} to actual client");
		System.out
				.println("query {file}\t:\t Queryies sparql-{file} to actual client");
		System.out.println();
	}

}
