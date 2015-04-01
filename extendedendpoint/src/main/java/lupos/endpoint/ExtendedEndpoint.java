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
