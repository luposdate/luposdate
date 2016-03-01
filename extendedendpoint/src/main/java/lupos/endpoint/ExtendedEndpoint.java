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
package lupos.endpoint;

import lupos.datastructures.items.literal.LiteralFactory;
import lupos.endpoint.contexts.ExtendedQueryHandler;
import lupos.endpoint.contexts.GraphsQueryHandler;
import lupos.endpoint.contexts.InfoQueryHandler;
import lupos.endpoint.server.Endpoint;
import lupos.engine.indexconstruction.FastRDF3XIndexConstruction;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoin;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoinNonStandardSPARQL;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.File;
import lupos.sparql1_1.operatorgraph.ServiceApproaches;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtendedEndpoint {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ExtendedEndpoint.class.getName());
	private final static String INDEX_DIR = "luposdate-index";
	private final static String INDEX_FILE = "src/main/resources/sp2b.n3";

	private ExtendedEndpoint() {
	}

	public static void main(final String[] args) throws Exception {

		// init the SPARQL evaluators, the luposdate endpoint could be extended
		// in the future to offer a configuration dialogue changing e.g. these
		// parameters...
		EvaluationHelper.init(ServiceApproaches.No_Support,
				FederatedQueryBitVectorJoin.APPROACH.SHA256,
				LiteralFactory.semanticInterpretationOfLiterals,
				FederatedQueryBitVectorJoin.substringSize,
				FederatedQueryBitVectorJoinNonStandardSPARQL.bitvectorSize);
		// register the standard luposdate sparql evaluators...
		EvaluationHelper.registerEvaluators();

		final int port = Endpoint.init(new String[] { INDEX_DIR });

		boolean startW3CEndpoint = false;
		boolean rebuildIndex = false;

		for(final String par: args){
			if(par.equalsIgnoreCase("--startW3CEndpoint")){
				startW3CEndpoint = true;
			} else if(par.equalsIgnoreCase("--rebuild-index")){
				rebuildIndex = true;
			}
		}

		if(startW3CEndpoint){
			final File f = new File(INDEX_DIR);
			if (rebuildIndex) {
				LOGGER.info("Rebuild index requested.");
				FastRDF3XIndexConstruction.main(new String[] { INDEX_FILE, "N3",
						"UTF-8", "NONE", INDEX_DIR, "500000" });
			} else if (!f.exists() || !f.isDirectory()) {
				LOGGER.info("No index found, so it will be created.");
				FastRDF3XIndexConstruction.main(new String[] { INDEX_FILE, "N3",
						"UTF-8", "NONE", INDEX_DIR, "500000" });
			} else {
				LOGGER.info("Using index directory: {}", INDEX_DIR);
			}
			Endpoint.registerStandardContexts(INDEX_DIR);
		}

		Endpoint.registerStandardFormatter();
		ExtendedEndpoint.registerNonStandardContexts();
		// Endpoint.registerStopContext(ExtendedEndpoint.delayForStoppingInSeconds);
		Endpoint.initAndStartServer(port);
		Endpoint.listenForStopSignal();
	}

	public static void registerNonStandardContexts() {
		Endpoint.registerHandler("/nonstandard/sparql",
				new TimeOutHandler(new ExtendedQueryHandler(false)));
		Endpoint.registerHandler("/nonstandard/sparql/info",
				new TimeOutHandler(new InfoQueryHandler(false)));
		Endpoint.registerHandler("/nonstandard/sparql/graphs",
				new TimeOutHandler(new GraphsQueryHandler(false)));
		Endpoint.registerHandler("/nonstandard/rif",
				new TimeOutHandler(new ExtendedQueryHandler(true)));
		Endpoint.registerHandler("/nonstandard/rif/info",
				new TimeOutHandler(new InfoQueryHandler(true)));
		Endpoint.registerHandler("/nonstandard/rif/graphs",
				new TimeOutHandler(new GraphsQueryHandler(true)));
	}
}
