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
package lupos.endpoint.caching;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.items.Triple;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.server.Endpoint;
import lupos.endpoint.server.Endpoint.OutputStreamLogger;
import lupos.endpoint.server.Endpoint.SPARQLExecution;
import lupos.endpoint.server.Endpoint.SPARQLHandler;
import lupos.endpoint.server.format.Formatter;
import lupos.engine.evaluators.BasicIndexQueryEvaluator;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.RDF3XQueryEvaluator;

import com.sun.net.httpserver.HttpExchange;

/**
 * This class is the server-side-implementation of the delta approach:
 * The delta approach uses a cache of triples at the client and the server always sends
 * missing triples to the client for a new query.
 * For this purpose, the server holds a set of triples already sent to the client,
 * retrieves the query triples for a new query and sends all those triples to the client,
 * which the client did not receive before.
 * The clients just adds the triples, which it receives from the server, and
 * evaluates the query on it.
 */
public class DeltaEndpoint {

	public static void main(final String[] args) throws Exception {
		// init according to command line arguments
		final int port = Endpoint.init(args);
		// register the sparqldelta context
		Endpoint.registerHandler("/sparqldelta", new SPARQLHandler(new SPARQLExecutionDeltaImplementation(Endpoint.createQueryEvaluator(args[0]), args[0])));
		Endpoint.registerStandardFormatter();
		// run the endpoint!
		Endpoint.initAndStartServer(port);
	}

	public static class SPARQLExecutionDeltaImplementation implements SPARQLExecution {

		/** The already sent triples to the different clients.
		 *  A client is identified by its InetSocketAddress.
		 */
		protected HashMap<InetSocketAddress, HashSet<Triple>> deltaIndices = new HashMap<InetSocketAddress, HashSet<Triple>>();

		protected final BasicIndexQueryEvaluator evaluator;
		protected final String dir;

		public SPARQLExecutionDeltaImplementation(final BasicIndexQueryEvaluator evaluator, final String dir){
			this.evaluator = evaluator;
			this.dir = dir;
		}

		@Override
		public void execute(final String queryParameter, final Formatter formatter, final HttpExchange t) throws IOException {
			try {
				synchronized(this.evaluator){ // avoid any inference of several queries in parallel!
					System.out.println("Evaluating query using the delta approach:\n"+queryParameter);
					// log query-triples by using BindingsArrayReadTriples as class for storing the query solutions!
					Bindings.instanceClass = BindingsArrayReadTriples.class;
					final QueryResult queryResult = (this.evaluator instanceof CommonCoreQueryEvaluator)?((CommonCoreQueryEvaluator)this.evaluator).getResult(queryParameter, true):this.evaluator.getResult(queryParameter);
					final String mimeType = "text/n3";
					System.out.println("Done, sending response using MIME type "+mimeType);
					t.getResponseHeaders().add("Content-type", mimeType);
					t.getResponseHeaders().add("Transfer-encoding", "chunked");
					t.sendResponseHeaders(200, 0);
					OutputStream os = t.getResponseBody();
					if(Endpoint.log){
						os = new OutputStreamLogger(os);
					}

					// get the client's address
					final InetSocketAddress clientAddress = t.getRemoteAddress();
					// get the delta index for this client
					HashSet<Triple> deltaIndex = this.deltaIndices.get(clientAddress);
					if(deltaIndex==null){
						// initialize the delta index if it is not already there
						deltaIndex = new HashSet<Triple>();
					}
					// iterate through the solution and its query triples
					final Iterator<Bindings> itBindings = queryResult.oneTimeIterator();
					while(itBindings.hasNext()){
						final Bindings bindings = itBindings.next();
						for(final Triple triple: bindings.getTriples()){
							// Add the triple to the delta index and check if it is already in the delta index at the same time!
							// (Previously sent triples are eliminated as well as duplicates within the query triples of the same query result.)
							if(deltaIndex.add(triple)){
								// if the triple is not already in the delta index, then send it to the client!
								os.write(triple.toN3String().getBytes());
							}
						}
					}

					this.deltaIndices.put(clientAddress, deltaIndex);

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
}
