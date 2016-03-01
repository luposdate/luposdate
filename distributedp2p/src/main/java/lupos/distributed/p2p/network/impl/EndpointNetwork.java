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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.p2p.network.P2PTripleNetwork;
import lupos.distributed.storage.IStorage;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;
import lupos.distributedendpoints.storage.util.QueryBuilder;
import lupos.endpoint.client.Client;
import lupos.endpoint.client.formatreader.XMLFormatReader;
import lupos.endpoint.server.Endpoint;
import lupos.endpoint.server.Endpoint.SPARQLExecutionImplementation;
import lupos.endpoint.server.Endpoint.SPARQLHandler;
import lupos.endpoint.server.format.Formatter;
import lupos.engine.evaluators.BasicIndexQueryEvaluator;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.indexconstruction.FastRDF3XIndexConstruction;
import lupos.engine.indexconstruction.RDF3XEmptyIndexConstruction;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Okay, this is a fake P2P-network, because there is no valid p2p structure,
 * but only a few static configured endpoints that are accessible via URI.
 *
 * @version $Id: $Id
 */
public class EndpointNetwork extends P2PTripleNetwork {
	private BasicIndexQueryEvaluator evaluator;
	private IStorage localStorage;
	private static Logger log = Logger.getLogger(EndpointNetwork.class);
	private int port;
	private String dir;
	private final BindingsFactory bindings = BindingsFactory.createBindingsFactory();

	/**
	 * main entry point for accessing tools for the endpoint network<br>
	 * You can
	 * <ul>
	 * <li>Distribute a N3 file to a set of endpoints
	 * <li>Creating a super fast index of a N3 file.
	 * <ul>
	 *
	 * @param args
	 *            arguments
	 */
	public static void main(final String[] args) {

		System.out.println("Distribute N3-file to a set of subfiles.");
		System.out.println("");
		String input = null;
		String outputDir = null;
		if (args.length >= 2) {
			input = args[0];
			outputDir = args[1];
		}
		if (args.length == 3 || args.length == 4 | args.length == 5) {
			try {
				Integer onlyProcessURL = null;
				@SuppressWarnings("unchecked")
				final
				IDistribution<KeyContainer<?>> distribution = (IDistribution<KeyContainer<?>>) Class
						.forName(args[2]).newInstance();
				if (args.length == 4) {
					try {
						onlyProcessURL = Integer.parseInt(args[3]);
					} catch (final NumberFormatException e) {
						onlyProcessURL = null;
						final String cfgFile = args[3];
						init(Paths.get(cfgFile));
					}
				}
				if (args.length == 5) {
					onlyProcessURL = Integer.parseInt(args[4]);
				}
				System.out.println("Start processing ...");
				DataFileDistribution.process(input, outputDir, distribution,
						onlyProcessURL);
			} catch (final InstantiationException e) {
				e.printStackTrace();
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			} catch (final ClassNotFoundException e) {
				System.out.println("DistributionStrategy class " + args[2]
						+ " not found.");
			}
		} else if (args.length == 2) {
			if (!Files.isDirectory(Paths.get(outputDir))) {
				try {
					Files.createDirectories(Paths.get(outputDir));
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			final String[] arguments = new String[] { input, "N3", "UTF8", "NONE",
					outputDir };
			// arguments : <datafile> <dataformat> <encoding>
			// <NONE|BZIP2|HUFFMAN|GZIP> <directory for indices>
			FastRDF3XIndexConstruction.main(arguments);
		} else {
			System.out.println("-------------------");
			System.out.println("Argumentlist:");
			System.out
					.println("java -Xmx768M "
							+ EndpointNetwork.class.getName()
							+ " {inputFile} {outputDirectory} {distributionClassname} [{config-file} / {url-number to be processed}]");
			System.out
					.println("Reading a big RDF-file and distributing it to a set of sub-files for the endpoints configured. For better disk management the url-number in the given configuration list (including 0) can be optional set, to only distribute triples for this endpoint");
			System.out.println("java -Xmx768M "
					+ EndpointNetwork.class.getName()
					+ " {inputFile} {outputDirectory}");
			System.out
					.println("Reads in a big RDF-file and make index construction for RDF3X-usage.");
			System.out.println("-------------------");
		}
	}

	/**
	 * Static class which is used to process the distribution of a RDF-file, to
	 * be distributed to the endpoints which are configured in endpoint.cfg .
	 *
	 * @author Bjoern
	 *
	 */
	public static class DataFileDistribution {

		private DataFileDistribution() {
		}

		private static String getKey(final KeyContainer<?> keyContainer) {
			return String.format("%s%s", keyContainer.type, keyContainer.key);
		}

		/**
		 * Starts processing
		 *
		 * @param inputFile
		 *            the input RDF-triple file (exp. N3)
		 * @param outputPath
		 *            the path, where the output data is put (in
		 *            sub-directories)
		 * @param distributionStrategy
		 *            the distribution strategy to be used for distribution
		 * @param onlyProcessURL
		 *            if set, only the x'th endpoint URL is processed
		 */
		public static void process(final String inputFile, final String outputPath,
				final IDistribution<KeyContainer<?>> distributionStrategy,
				final Integer onlyProcessURL) {
			/*
			 * atomic counting
			 */
			final AtomicInteger counter = new AtomicInteger(0);
			/*
			 * one writer per endpoint
			 */
			final FileWriter[] oss19 = new FileWriter[EndpointNetwork.uriList
					.size()];
			/*
			 * create directory, if not existing
			 */
			if (!Files.isDirectory(Paths.get(outputPath))) {
				try {
					Files.createDirectories(Paths.get(outputPath));
				} catch (final IOException e) {
					throw new RuntimeException("Cannot create directory: "
							+ outputPath);
				}
			}

			try {
				/*
				 * foreach endpoint, create a file, where the RDF triples are
				 * stored (here: the filename is the endpoint-url)
				 */
				for (int i = 0; i < EndpointNetwork.uriList.size(); i++) {
					if (onlyProcessURL == null || onlyProcessURL == i) {
						oss19[i] = new FileWriter(getFilename(outputPath,
								uriList.get(i)));
					}
				}
				/*
				 * Consume each triple of input RDF file
				 */
				final TripleConsumer tc = new TripleConsumer() {
					@Override
					public void consume(final Triple triple) {
						try {

							for (int i = 0; i < 3; i++) {
								// if used blank_nodes or nulltype .. replace so
								// that
								// these triples can be used in
								// "index-generation"
								if (triple.getPos(i) == null) {
									triple.setPos(i, LiteralFactory
											.createStringLiteral("<nulltype>"));
								}
								if (triple.getPos(i).isBlank()) {
									triple.setPos(
											i,
											LiteralFactory
													.createStringURILiteral("http://blank#"
															+ triple.getPos(i)
																	.toString()
															+ ""));
								}
							}

							/*
							 * counter and statistics
							 */
							final int size = counter.incrementAndGet();
							if (size % 100000 == 0) {
								System.out.println("Processed: " + size
										+ " triples.");
							}
							/*
							 * get a triple and its keys
							 */
							final String n3Triple = triple.toN3String();
							final KeyContainer<?>[] keys = distributionStrategy
									.getKeysForStoring(triple);
							for (final KeyContainer<?> k : keys) {
								/*
								 * get the endpoint URL and store
								 */
								final URI u = EndpointNetwork.getURIbyKey(getKey(k));
								final int pos = EndpointNetwork.uriList.indexOf(u);
								if (onlyProcessURL == null
										|| onlyProcessURL == pos) {
									oss19[pos].write(n3Triple + '\n');
								}
							}
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
				};

				/*
				 * read the triples, to be removed out of our network
				 */
				try (FileInputStream fr = new FileInputStream(new File(
						inputFile))) {
					LiteralFactory.setType(MapType.NOCODEMAP);
					CommonCoreQueryEvaluator.readTriples("N3", fr, tc);
					System.out.println("Total number of triples read: "
							+ counter.get());
				} catch (final Exception e) {
					e.printStackTrace();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				/*
				 * close all writers
				 */
				for (final FileWriter fw : oss19) {
					if (fw != null) {
						try {
							fw.close();
						} catch (final IOException e) {
						}
					}
				}
			}
		}

		private static String getFilename(final String outputPath, final URI uri) {
			/*
			 * create a valid filename by the given endpint URL
			 */
			final String f = uri.toString().replaceAll("//", "_")
					.replaceAll(":", "_")
					+ ".n3";
			return Paths.get(outputPath, f).toString();
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return String.format("EndpointNetwork on port %d [%s]", this.port, this.dir);
	}

	/** {@inheritDoc} */
	@Override
	public IStorage getLocalStorage(final IDistribution<?> distibution) {
		if (this.localStorage != null) {
			return this.localStorage;
		}
		/*
		 * new local storage
		 */
		return (this.localStorage = new IStorage() {

			private BindingsFactory bindings = BindingsFactory.createBindingsFactory();

			@Override
			public void setBindingsFactory(final BindingsFactory bindingsFactory) {
				this.bindings = bindingsFactory;
			}


			@Override
			public void endImportData() {
			}

			@Override
			public void addTriple(final Triple triple) {
				// Not allowed in local storage
			}

			@Override
			public boolean containsTriple(final Triple triple) {
				return false;
			}

			@Override
			public void remove(final Triple triple) {
				// Not allowed in local storage
			}

			@SuppressWarnings("unused")
			@Override
			public QueryResult evaluateTriplePattern(final TriplePattern triplePattern)
					throws Exception {
				/*
				 * evaluate the given pattern with the local storage
				 */
				synchronized (EndpointNetwork.this.evaluator) {
					//Bindings.instanceClass = bindings;
					/*
					 * create the query
					 */
					final String q = QueryBuilder.buildQuery(triplePattern);

					/*
					 * evaluate
					 */
					if (LiteralFactory.getMapType() != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
						LiteralFactory
								.setTypeWithoutInitializing(MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
						LiteralFactory
								.setType(MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
					}
					final QueryResult queryResult = (EndpointNetwork.this.evaluator instanceof CommonCoreQueryEvaluator) ? ((CommonCoreQueryEvaluator<?>) EndpointNetwork.this.evaluator)
							.getResult(q, true)
							: EndpointNetwork.this.evaluator.getResult(q);
					log.debug(String
							.format("[%s on port %d] Evaluation query in local storage: %s: %d items found",
									EndpointNetwork.this.evaluator.getClass().getSimpleName(), EndpointNetwork.this.port,
									q, queryResult.size()));

					/*
					 * return result
					 */
					if (queryResult != null) {
						return queryResult;
					}
					/*
					 * otherwise try other way ...
					 */
					EndpointNetwork.this.evaluator.compileQuery(q);
					EndpointNetwork.this.evaluator.logicalOptimization();
					EndpointNetwork.this.evaluator.physicalOptimization();
					return EndpointNetwork.this.evaluator.getResult();
				}
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasLocalStorage() {
		return true;
	}

	/**
	 * Create new endpoint network with the given main directory and port
	 * listening to
	 *
	 * @param directory
	 *            the main working directory for disk-based storage
	 * @param port
	 *            the port listing to
	 * @throws java.net.BindException
	 *             error if port already used
	 */
	@SuppressWarnings("restriction")
	public EndpointNetwork(String directory, final int port) throws BindException {
		this.port = port;
		String base_dir = directory;
		// directory should end with "/"
		if (!base_dir.endsWith("/") && !base_dir.endsWith("\\")) {
			base_dir += "/";
		}
		directory = base_dir + port + "/";
		final Path dP = Paths.get(directory);
		if (!Files.isDirectory(dP)) {
			try {
				/*
				 * create index on first run!
				 */
				Files.createDirectories(dP);
				// Fast RDF3X IndexConstructu
				RDF3XEmptyIndexConstruction
						.main(new String[] { dP.toString() });
			} catch (final IOException e) {
			}
		}

		/*
		 * set parameters
		 */
		LiteralFactory
				.setTypeWithoutInitializing(MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
		LiteralFactory.setType(MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);

		// create the evaluatr
		this.dir = dP.toString();
		this.evaluator = Endpoint.createQueryEvaluator(directory);

		// evaluate context for SPARQL query processing...
		// this endpoint-handler is only used for adding / removing, not for
		// querying!!
		Endpoint.registerHandler("/sparql/", new SPARQLHandler(
				new SPARQLExecutionImplementation(this.evaluator, directory) {
					@Override
					public void execute(final String queryParameter,
							final Formatter formatter, final HttpExchange t)
							throws IOException {
						if (LiteralFactory.getMapType() != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
							LiteralFactory
									.setTypeWithoutInitializing(MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
							LiteralFactory
									.setType(MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
						}
						super.execute(queryParameter, formatter, t);
					}

				}));

		/*
		 * register our own handler, that receives subgraph messages
		 */
		Endpoint.registerHandler("/message/", new HttpHandler() {
			/**
			 * Read out a query parameter
			 *
			 * @param responseParts
			 *            the queries as array
			 * @param parameter
			 *            the parameter to search for
			 * @return the content part after query
			 * @throws UnsupportedEncodingException
			 *             error
			 */
			protected String getParameter(final String[] responseParts,
					final String parameter) throws UnsupportedEncodingException {
				for (final String item : responseParts) {
					if (item.startsWith(parameter)) {
						return URLDecoder.decode(
								item.substring(parameter.length()), "UTF-8");
					}
				}
				return null;
			}

			@Override
			public void handle(final HttpExchange t) throws IOException {
				/*
				 * get the sender's ip-address (hostname)
				 */
				String host = t.getRemoteAddress().getHostString();
				if (!host.startsWith("http://")) {
					host = "http://" + host;
				}
				log.debug(String.format("Receiving subgraph-request from: %s",
						host));
				/*
				 * get the queries
				 */
				final String response = Endpoint.getResponse(t);
				final String[] responseParts = response.split("[&]");
				if (responseParts.length > 0) {
					/*
					 * the port the request came from (has to be given as query
					 * parameter, because when using windows, the sender's port
					 * is a random one, and not the one, that can be used for
					 * answer! but the host is right, so now we know the
					 * endpoint-url to answer!
					 */
					final String port = this.getParameter(responseParts, "from=");
					host += ":" + port;
					/*
					 * now get the query-part (subgraph message)
					 */
					final String queryParameter = this.getParameter(responseParts,
							"query=");

					if (queryParameter != null) {
						/*
						 * forward this message to the subgraph executer ...
						 */
						EndpointNetwork.this.onMessage(queryParameter, host);
						/*
						 * and return result
						 */
						t.getResponseHeaders()
								.add("Content-type", "text/plain");
						Endpoint.sendString(t, "OK");
					} else { /*
							 * if no parameter given
							 */
						t.getResponseHeaders()
								.add("Content-type", "text/plain");
						final String answer = "Bad Request: query parameter missing";
						Endpoint.sendString(t, answer);
						return;
					}
				}
			};
		});
		/*
		 * This is the handler that receives streams with XML results
		 */
		Endpoint.registerHandler("/answer/", new HttpHandler() {

			@Override
			public void handle(final HttpExchange t) throws IOException {
				/*
				 * get the sender's ip-address (hostname)
				 */
				String host = t.getRemoteAddress().getHostString();
				if (!host.startsWith("http://")) {
					host = "http://" + host;
				}

				log.debug(String.format(
						"Receiving subgraph-request answer from: %s", host));
				EndpointNetwork.this.onMessage(t.getRequestBody(), host);

				System.out.println("send ok");
				t.getResponseHeaders().add("Content-type", "text/plain");
				Endpoint.sendString(t, "OK");
			};
		});
		/*
		 * register formatter and start endpoint!
		 */
		Endpoint.registerStandardFormatter();
		Endpoint.initAndStartServer(port);
	}

	private static ArrayList<URI> uriList;

	/** {@inheritDoc} */
	@Override
	public List<Triple> get(final String key) {
		/*
		 * we will not execute this, if using subgraph-submission
		 */
		final List<Triple> lst = new LinkedList<Triple>();
		final QueryResult qr = this.sendDirect(key, "SELECT * WHERE {?s ?p ?o}");
		for (final Iterator<Bindings> it = qr.oneTimeIterator(); it.hasNext();) {
			final Bindings bind = it.next();
			final List<Triple> t = bind.getTriples();
			if (t != null) {
				lst.addAll(t);
			}
		}
		return lst;
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final String key, final Triple triple) {
		/*
		 * remove the given item at the given key
		 */
		this.sendDirect(key, QueryBuilder.buildDeleteQuery(triple));
	}

	/** {@inheritDoc} */
	@Override
	public void addAll(final String key, final Triple... v) {
		final Set<Triple> values = new HashSet<Triple>();
		for (final Triple t : v) {
			values.add(t);
		}
		this.sendDirect(key, QueryBuilder.buildInsertQuery(values));
	}

	/** {@inheritDoc} */
	@Override
	public void add(final String key, final Triple value) {
		final Set<Triple> values = new HashSet<Triple>();
		values.add(value);
		this.sendDirect(key, QueryBuilder.buildInsertQuery(values));
	}

	/**
	 * Read in the endpoint configuration file once at start
	 */
	static {
		init(null);
	}

	/*
	 * retrieve the URI that manages the given key
	 */
	static {
		init(null);
	}

	/*
	 * retrieve the URI that manages the given key
	 */
	private static URI getURIbyKey(final String key) {
		/*
		 * we do MD5 on the key (because this is a cryptographic hash function,
		 * that distributes the key, so that two keys that seem to be same, are
		 * well distributed over all peers (endpoint url's)
		 */
		MessageDigest digest;
		byte[] hash;
		try {
			digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(key.getBytes());
			hash = digest.digest();
		} catch (final NoSuchAlgorithmException e) {
			hash = key.getBytes();
		}
		/*
		 * and sum up the bytes
		 */
		long sum = 0;
		for (int i = 0; i < hash.length; i++) {
			sum += hash[i];
		}
		/*
		 * now we retrieve deterministic the url for that key
		 */
		final int i = Math.abs((int) (sum % uriList.size()));
		return uriList.get(i);
	}

	private static void init(Path f) {
		/*
		 * load the endpoint config file with the URLs for distribution
		 */
		f = (f == null) ? Paths.get(".", "endpoint.cfg") : f;
		if (!Files.exists(f)) {
			System.err
					.println("Cannot use endpoint network, because no configuration found.");
			System.exit(1);
		}
		try {
			/*
			 * each line has one URL
			 */
			final List<String> items = Files
					.readAllLines(f, Charset.defaultCharset());
			final ArrayList<URI> uri = new ArrayList<URI>();
			for (final String item : items) {
				try { // check whether line was an URL
					uri.add(new URI(item));
				} catch (final URISyntaxException e) {
					log.warn(String
							.format("The url %s of endpoint config isn't an uri. Ignored.",
									item));
				}
			}
			EndpointNetwork.uriList = uri;
			log.debug("Has read the following url: (total:" + uriList.size()
					+ ")");
			for (final URI u : uriList) {
				log.debug("\t" + u);
			}
			log.debug("");
		} catch (final IOException e) {
			log.error(
					"Cannot use endpoint network, because configuration cannot be read.",
					e);
			System.exit(1);
		}
	}

	/*
	 * Sends a sparql query to the given key (this is only used for adding and
	 * removing, so where no subgraph submission is required.
	 */
	private QueryResult sendDirect(final String key, final String sparqlQuery) {
		/*
		 * get the URI format (http://HOST:PORT/sparql/?query=...
		 */
		try {
			final URI uri = getURIbyKey(key);
			String url = uri.toString();
			if (!url.endsWith("/")) {
				url += "/";
			}
			url += "sparql/";
			log.debug(String.format("Send direct to %s: %s", url, sparqlQuery));
			return Client.submitQuery(url, sparqlQuery,this.bindings);
		} catch (final Exception e) {
			log.error(String.format("Error submitting query %s to key %s :",
					sparqlQuery, key), e);
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void sendMessage(final String key, final String message) {
		/*
		 * sends a subgraph (or individual message) to
		 * http://HOST:PORT/message/?query=
		 */
		String uri = getURIbyKey(key).toString();
		if (!uri.endsWith("/")) {
			uri += "/";
		}
		final String url = uri + "message/";
		try {
			log.debug(String.format("Send message to %s: %s", url, message));
			this.submitQueryAndRetrieveStream(url, message, XMLFormatReader.MIMETYPE);
		} catch (final Exception e) {
			log.error(
					String.format("Error submitting sg-message %s to key %s :",
							message, key), e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void sendMessageTo(String url, final InputStream message) {
		if (!url.endsWith("/")) {
			url += "/";
		}
		url = url + "answer/";
		try {
			log.debug(String.format("Send stream-message to %s.", url));
			Client.doSubmitStream(url, message, XMLFormatReader.MIMETYPE);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void sendMessageTo(String uri, final String message) {
		/*
		 * sends a subgraph (or individual message) to
		 * http://HOST:PORT/message/?query=
		 */
		if (!uri.endsWith("/")) {
			uri += "/";
		}
		final String url = uri + "message/";
		try {
			log.debug(String.format("Send message to %s: %s", url, message));
			this.submitQueryAndRetrieveStream(url, message, XMLFormatReader.MIMETYPE);
		} catch (final Exception e) {
			log.error(
					String.format("Error submitting sg-message %s to uri %s :",
							url, message), e);
		}
	}

	/*
	 * set the parameters and submits the request
	 */
	private Tuple<String, InputStream> submitQueryAndRetrieveStream(
			final String url, final String query, final String formatKey) {
		final List<NameValuePair> params = new LinkedList<NameValuePair>();
		/*
		 * set the parameters
		 */
		params.add(new BasicNameValuePair("query", query));
		params.add(new BasicNameValuePair("format", formatKey));
		params.add(new BasicNameValuePair("from", String.valueOf(this.port))); // the
																			// actual
																			// port
		try {
			return Client.doSubmit(url, params, formatKey);
		} catch (final IOException e) {
			log.error(String.format(
					"Error submitting sg-message %s to uri %s in %s:", query,
					url, formatKey), e);
			return new Tuple<String, InputStream>("ERROR", null);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean supportsStreaming() {
		/* yes, we do! */
		return true;
	}

}
