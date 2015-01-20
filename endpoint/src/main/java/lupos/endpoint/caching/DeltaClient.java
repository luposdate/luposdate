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
package lupos.endpoint.caching;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.client.Client;
import lupos.endpoint.client.CommandLineEvaluator;
import lupos.endpoint.client.formatreader.MIMEFormatReader;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.MemoryIndexQueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.tripleoperator.TripleConsumer;

/**
 * This class sets up the client to use the delta approach for caching query triples...
 * @seealso lupos.endpoint.caching.DeltaEndpoint
 */
public class DeltaClient {

	private final static String textn3 = "text/n3";

	static {
		try {
			final MemoryIndexQueryEvaluator evaluator = new MemoryIndexQueryEvaluator();
			evaluator.prepareInputData(new LinkedList<URILiteral>(), new LinkedList<URILiteral>());

			Client.registerFormatReader(new MIMEFormatReader("delta", DeltaClient.textn3){

				@Override
				public String getMIMEType() {
					return DeltaClient.textn3;
				}

				@Override
				public QueryResult getQueryResult(final InputStream inputStream, final String query, final BindingsFactory bindingsFactory) {
					try {
						CommonCoreQueryEvaluator.readTriples("N3", inputStream,
								new TripleConsumer(){
							@Override
							public void consume(final Triple triple) {
								// add the received triples to the index
								final Collection<Indices> ci = evaluator.getDataset().getDefaultGraphIndices();
								for (final Indices indices : ci) {
									indices.add(triple);
								}
							}
						});
						// wait for completing adding...
						evaluator.getDataset().buildCompletelyAllIndices();
						// evaluate the query on the current index...
						return evaluator.getResult(query);
					} catch (final Exception e) {
						System.err.println(e);
						e.printStackTrace();
					}
					return null;
				}

			});
			// use delta approach!
			Client.DEFAULT_FORMAT = DeltaClient.textn3;
		} catch (final Exception e1) {
			System.err.println(e1);
			e1.printStackTrace();
		}
	}

	/**
	 * This main method calls the main method of CommandLineEvaluator (after the DeltaClient has been initialized).
	 * The delta approach for caching and reusing query triples is used for any communication with an endpoint
	 * (which must be DeltaEndpoint, otherwise the approach will not work).
	 * @param args Command line arguments
	 */
	public static void main(final String[] args) {
		CommandLineEvaluator.main(args);
	}
}
