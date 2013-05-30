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
package lupos.distributedendpoints.endpoint;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.operator.format.operatorcreator.IOperatorCreator;
import lupos.distributed.operator.format.operatorcreator.RDF3XCreator;
import lupos.distributed.storage.distributionstrategy.tripleproperties.OneKeyDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.OneToThreeKeysDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.TwoKeysDistribution;
import lupos.distributed.storage.util.LocalExecutor;
import lupos.endpoint.server.Endpoint;
import lupos.endpoint.server.Endpoint.OutputStreamLogger;
import lupos.endpoint.server.Endpoint.OutputStreamSizeLogger;
import lupos.endpoint.server.Endpoint.SPARQLExecution;
import lupos.endpoint.server.Endpoint.SPARQLExecutionImplementation;
import lupos.endpoint.server.Endpoint.SPARQLHandler;
import lupos.endpoint.server.format.Formatter;
import lupos.engine.evaluators.BasicIndexQueryEvaluator;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.RDF3XQueryEvaluator;
import lupos.misc.Tuple;

import com.sun.net.httpserver.HttpExchange;

/**
 * This class is for starting the endpoints with contexts according to the used distribution strategy
 */
public class StartEndpoint {

	/**
	 * Main entry point to start the endpoints...
	 * @param args see init() for the command line arguments
	 */
	public static void main(final String[] args) {
		final Tuple<String[], Integer> keyTypes = StartEndpoint.init(args);
		String base_dir = args[1];
		if(!base_dir.endsWith("/") && !base_dir.endsWith("\\")){
			base_dir += "/";
		}
		for(final String keyType: keyTypes.getFirst()) {
			// start for each type of the keys a different context
			final String directory = base_dir + keyType;
			final BasicIndexQueryEvaluator evaluator = Endpoint.createQueryEvaluator(directory);

			// evaluate context for SPARQL query processing...
			Endpoint.registerHandler("/sparql/" + keyType, new SPARQLHandler(new SPARQLExecutionImplementation(evaluator, directory)));

			final RDF3XCreator creator = new RDF3XCreator();
			// register context for evaluating subgraphs...
			Endpoint.registerHandler("/sparql/subgraph/" + keyType, new SPARQLHandler(new SubgraphExecutionImplementation(evaluator, directory, creator)));

			// register context for determining histograms...
			Endpoint.registerHandler("/sparql/histogram/" + keyType, new SPARQLHandler(new HistogramExecutionImplementation(evaluator, creator)));
		}
		Endpoint.registerStandardFormatter();
		Endpoint.initAndStartServer(keyTypes.getSecond());
	}

	/**
	 * Initializes the endpoint and returns the list of possible keys for the specified distribution strategy
	 * @param args command line arguments
	 * @return the list of possible keys for the specified distribution strategy
	 */
	public static Tuple<String[], Integer> init(final String[] args){
		final Tuple<String[], Integer> result = new Tuple<String[], Integer>(new String[]{ "" }, 8080);
		if (args.length >= 2) {
			if(args[0].compareTo("0")==0) {
				// already default !
			} else if(args[0].compareTo("1")==0) {
				result.setFirst(OneKeyDistribution.getPossibleKeyTypes());
			} else if(args[0].compareTo("2")==0) {
				result.setFirst(TwoKeysDistribution.getPossibleKeyTypes());
			} else if(args[0].compareTo("3")==0) {
				result.setFirst(OneToThreeKeysDistribution.getPossibleKeyTypes());
			}
		}
		if (args.length < 2 || result==null) {
			System.err.println("Usage:\njava -Xmx768M lupos.distributedendpoints.endpoint.StartEndpoint (0|1|2|3) <directory for indices> [portX] [output] [size]");
			System.err.println("0 for no distribution strategy");
			System.err.println("1 for one key distribution strategy");
			System.err.println("2 for two keys distribution strategy");
			System.err.println("3 for one to three keys distribution strategy");
			System.err.println("If \"portX\" is given, the port X (default 8080) is used, X must be a non-negative number.");
			System.err.println("If \"output\" is given, the response is written to console.");
			System.err.println("If \"size\" is given, the size of the received query and the size of the response is written to console.");
			System.exit(0);
		}
		for(int i=2; i<args.length; i++){
			if(args[i].compareTo("output")==0){
				Endpoint.log = true;
			} else if(args[i].compareTo("size")==0){
				Endpoint.sizelog = true;
			} else if(args[i].startsWith("port")){
				result.setSecond(Integer.parseInt(args[i].substring("port".length())));
			}
		}
		return result;
	}

	public static class SubgraphExecutionImplementation implements SPARQLExecution {

		protected final BasicIndexQueryEvaluator evaluator;
		protected final String dir;
		protected final IOperatorCreator operatorCreator;

		public SubgraphExecutionImplementation(final BasicIndexQueryEvaluator evaluator, final String dir, final IOperatorCreator operatorCreator){
			this.evaluator = evaluator;
			this.dir = dir;
			this.operatorCreator = operatorCreator;
		}

		@Override
		public void execute(final String subgraphSerializedAsJSONString, final Formatter formatter, final HttpExchange t) throws IOException {
			if(Endpoint.sizelog){
				System.out.println("Size of the received subgraph (number of characters of serialized subgraph): "+subgraphSerializedAsJSONString.length());
			}
			try {
				synchronized(this.evaluator){ // avoid any inference of executing several subgraphs in parallel!
					System.out.println("Evaluating subgraph:\n"+subgraphSerializedAsJSONString);
					if((this.evaluator instanceof CommonCoreQueryEvaluator) && formatter.isWriteQueryTriples()){
						// log query-triples by using BindingsArrayReadTriples as class for storing the query solutions!
						Bindings.instanceClass = BindingsArrayReadTriples.class;
					} else {
						Bindings.instanceClass = Endpoint.defaultBindingsClass;
					}

					final Tuple<QueryResult, Set<Variable>> queryResult = LocalExecutor.evaluateSubgraph(subgraphSerializedAsJSONString, (this.evaluator instanceof BasicIndexQueryEvaluator)?this.evaluator.getDataset() : null, this.operatorCreator);

					final String mimeType = formatter.getMIMEType(queryResult.getFirst());
					System.out.println("Done, sending response using MIME type "+mimeType);
					t.getResponseHeaders().add("Content-type", mimeType);
					t.getResponseHeaders().add("Transfer-encoding", "chunked"); // currently chunked transmission blocks the processing=> enable again if parallel execution works!
					t.sendResponseHeaders(200, 0);
					OutputStream os = t.getResponseBody();
					if(Endpoint.log){
						os = new OutputStreamLogger(os);
					}
					if(Endpoint.sizelog){
						os = new OutputStreamSizeLogger(os);
					}
					formatter.writeResult(os, queryResult.getSecond(), queryResult.getFirst());
					os.close();
					if(this.evaluator instanceof RDF3XQueryEvaluator){
						((RDF3XQueryEvaluator)this.evaluator).writeOutIndexFileAndModifiedPages(this.dir);
					}
				}
				return;
			} catch (final Error e) {
				System.err.println(e);
				e.printStackTrace();
				t.getResponseHeaders().add("Content-type", "text/plain");
				final String answer = "Error:\n"+e.getMessage();
				System.out.println(answer);
				Endpoint.sendString(t, answer);
				return;
			} catch (final Exception e){
				System.err.println(e);
				e.printStackTrace();
				t.getResponseHeaders().add("Content-type", "text/plain");
				final String answer = "Error:\n"+e.getMessage();
				System.out.println(answer);
				Endpoint.sendString(t, answer);
				return;
			}
		}
	}

	public static class HistogramExecutionImplementation implements SPARQLExecution {

		protected final BasicIndexQueryEvaluator evaluator;
		protected final IOperatorCreator operatorCreator;

		public HistogramExecutionImplementation(final BasicIndexQueryEvaluator evaluator, final IOperatorCreator operatorCreator){
			this.evaluator = evaluator;
			this.operatorCreator = operatorCreator;
		}

		@Override
		public void execute(final String histogramRequestSerializedAsJSONString, final Formatter formatter, final HttpExchange t) throws IOException {
			if(Endpoint.sizelog){
				System.out.println("Size of the received histogram request (number of characters of serialized request): "+histogramRequestSerializedAsJSONString.length());
			}
			try {
				synchronized(this.evaluator){ // avoid any inference of executing several subgraphs in parallel!
					System.out.println("Determining Histogram:\n"+histogramRequestSerializedAsJSONString);
					Bindings.instanceClass = BindingsMap.class;

					final String result = LocalExecutor.getHistogramOrMinMax(histogramRequestSerializedAsJSONString, (this.evaluator instanceof BasicIndexQueryEvaluator)?this.evaluator.getDataset() : null, this.operatorCreator);

					final String mimeType = "application/json";
					System.out.println("Done, sending response using MIME type "+mimeType);
					t.getResponseHeaders().add("Content-type", mimeType);
					t.getResponseHeaders().add("Transfer-encoding", "chunked");
					t.sendResponseHeaders(200, 0);
					OutputStream os = t.getResponseBody();
					if(Endpoint.log){
						os = new OutputStreamLogger(os);
					}
					if(Endpoint.sizelog){
						os = new OutputStreamSizeLogger(os);
					}
					os.write(result.getBytes("UTF-8"));
					os.close();
				}
				return;
			} catch (final Error e) {
				System.err.println(e);
				e.printStackTrace();
				t.getResponseHeaders().add("Content-type", "text/plain");
				final String answer = "Error:\n"+e.getMessage();
				System.out.println(answer);
				Endpoint.sendString(t, answer);
				return;
			} catch (final Exception e){
				System.err.println(e);
				e.printStackTrace();
				t.getResponseHeaders().add("Content-type", "text/plain");
				final String answer = "Error:\n"+e.getMessage();
				System.out.println(answer);
				Endpoint.sendString(t, answer);
				return;
			}
		}
	}
}
