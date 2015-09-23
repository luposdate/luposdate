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
package lupos.endpoint.contexts;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lupos.endpoint.EvaluationHelper;
import lupos.endpoint.GraphSerialization;
import lupos.endpoint.EvaluationHelper.GENERATION;
import lupos.endpoint.EvaluationHelper.SPARQLINFERENCE;
import lupos.endpoint.EvaluationHelper.SPARQLINFERENCEMATERIALIZATION;
import lupos.endpoint.server.Endpoint;
import lupos.endpoint.server.format.Formatter;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperBasicOperatorByteArray;
import lupos.misc.Triple;
import lupos.misc.Tuple;
import lupos.rdf.Prefix;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.TokenMgrError;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.n3.turtle.TurtleParseException;
import com.hp.hpl.jena.query.QueryParseException;
import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class GraphsQueryHandler implements InterruptableHttpHandler {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GraphsQueryHandler.class.getName());

	private final boolean RIF_EVALUATION;

	public GraphsQueryHandler(final boolean rifEvaluation) {
		super();
		this.RIF_EVALUATION = rifEvaluation;
	}

	@Override
	public boolean handle(final HttpExchange t) throws IOException {
		LOGGER.info("Handling {} from {}", t.getRequestMethod(), t
				.getRequestHeaders().getFirst("Host"));

		JSONObject response = null;
		// We default to error status in order to guarantee that HTTP_OK is only
		// responded if everything went well
		int responseStatus = QueryHandlerHelper.HTTP_INTERNAL_SERVER_ERROR;

		try {
			// Only process POST requests
			final String requestMethod = t.getRequestMethod();
			if (!requestMethod.equalsIgnoreCase("POST")) {
				LOGGER.info("Received {}, but only POST is allowed",
						requestMethod);
				t.getResponseHeaders().add("Allow", "POST");
				response = new JSONObject();
				response.put("error", "Only POSTs will be processed.");
				responseStatus = QueryHandlerHelper.HTTP_METHOD_NOT_ALLOWED;
				return true;
			}

			// Parse the request
			GraphsQueryParameters parameters = null;
			try {
				final String requestBody = IOUtils.toString(t.getRequestBody());
				parameters = GraphsQueryParameters
						.getParametersFromJson(requestBody);
			} catch (final RuntimeException e) {
				LOGGER.info("Bad request: {}", e.getMessage());
				if (e.getCause() != null) {
					LOGGER.info("Cause: {}", e.getCause().toString());
				}
				response = new JSONObject();
				response.put("error", e.getMessage());
				responseStatus = QueryHandlerHelper.HTTP_BAD_REQUEST;
				return true;
			}

			LOGGER.info("Received query: {}", parameters.QUERY);
			LOGGER.info("Received rdf: {}", parameters.RDF);
			LOGGER.info("Using evaluator with index: {}",
					parameters.EVALUATOR_INDEX);
			LOGGER.info("Using inference mode: {}", parameters.INFERENCE);
			if (!parameters.RIF.equals("")) {
				LOGGER.info("Received rif rules: {}", parameters.RIF);
			}

			if (this.RIF_EVALUATION) {
				LOGGER.info("Starting processing RIF");
			} else {
				LOGGER.info("Starting processing SPARQL");
			}
			Tuple<Prefix, List<Triple<String, String, GraphWrapperBasicOperatorByteArray>>> result = null;
			try {
				// Use the magic getOperatorGraphs method
				// We get all parameters from the request (or the default
				// values) except for two.
				// Second parameter indicates if we are processing RIF.
				// Fifth parameters indicates that we don't want to store
				// inferred triples in the DB (We process everything in memory).
				result = EvaluationHelper
						.getOperatorGraphs(
								parameters.EVALUATOR_INDEX,
								this.RIF_EVALUATION,
								parameters.INFERENCE,
								parameters.INFERENCE_GENERATION,
								SPARQLINFERENCEMATERIALIZATION.COMBINEDQUERYOPTIMIZATION,
								parameters.OWL2RL_INCONSISTENCY_CHECK,
								parameters.RDF, parameters.RIF,
								parameters.QUERY);
			} catch (TokenMgrError | ParseException | QueryParseException
					| MalformedQueryException
					| lupos.rif.generated.parser.ParseException
					| lupos.rif.generated.parser.TokenMgrError e) {
				LOGGER.info("Malformed query: {}", e.getMessage());
				final Triple<Integer, Integer, String> detailedError = EvaluationHelper
						.dealWithThrowableFromQueryParser(e);
				final Integer line = detailedError.getFirst();
				final Integer column = detailedError.getSecond();
				final String error = detailedError.getThird();

				final JSONObject errorJson = new JSONObject();
				if (line != -1) {
					errorJson.put("line", line);
				}
				if (column != -1) {
					errorJson.put("column", column);
				}
				errorJson.put("errorMessage", error);
				response = new JSONObject();
				response.put("queryError", errorJson);
				// We send HTTP_OK, because the actual HTTP request was correct
				responseStatus = QueryHandlerHelper.HTTP_OK;
				return true;
			} catch (TurtleParseException | RDFParseException e) {
				LOGGER.info("Malformed rdf: {}", e.getMessage());
				final Triple<Integer, Integer, String> detailedError = EvaluationHelper
						.dealWithThrowableFromRDFParser(e);
				final Integer line = detailedError.getFirst();
				final Integer column = detailedError.getSecond();
				final String error = detailedError.getThird();

				final JSONObject errorJson = new JSONObject();
				if (line != -1) {
					errorJson.put("line", line);
				}
				if (column != -1) {
					errorJson.put("column", column);
				}
				errorJson.put("errorMessage", error);
				response = new JSONObject();
				response.put("rdfError", errorJson);
				responseStatus = QueryHandlerHelper.HTTP_OK;
				return true;
			}
			LOGGER.info("Finished processing");

			final JSONObject responseTmp = new JSONObject();
			if (result == null) {
				responseTmp.put("info", "No operator graphs available.");
			} else {
				final Prefix prefixes = result.getFirst();
				final List<Triple<String, String, GraphWrapperBasicOperatorByteArray>> optimizationSteps = result
						.getSecond();

				// Serializing prefixes
				final JSONObject prefixesJson = new JSONObject();
				prefixesJson.put("pre-defined", prefixes.getPredefinedList());
				prefixesJson.put("prefixes", prefixes.getPrefixList());
				prefixesJson.put("names", prefixes.getPrefixNames());
				responseTmp.put("prefix", prefixesJson);

				// Serializing operator graphs
				final JSONObject optimizationsJson = new JSONObject();
				for (final Triple<String, String, GraphWrapperBasicOperatorByteArray> optimizationStep : optimizationSteps) {
					final String description = optimizationStep.getFirst();
					final String ruleName = optimizationStep.getSecond();
					final GraphWrapperBasicOperatorByteArray operatorGraph = optimizationStep
							.getThird();

					final JSONObject stepJson = new JSONObject();
					stepJson.put("description", description);
					stepJson.put("ruleName", ruleName);
					stepJson.put("operatorGraph", GraphSerialization
							.graphWrapperToJsonGraph(operatorGraph));

					optimizationsJson.append("steps", stepJson);
				}
				responseTmp.put("optimization", optimizationsJson);
			}
			responseStatus = QueryHandlerHelper.HTTP_OK;
			response = responseTmp;
		} catch (final Exception e) {
			LOGGER.error("Encountered exception {}", e.toString(), e);
			response = new JSONObject();
			response.put("error", e.toString());
			responseStatus = QueryHandlerHelper.HTTP_OK;
		} finally {
			QueryHandlerHelper.sendResponse(t, response, responseStatus);
		}
		return (t!=null);
	}

}

class GraphsQueryParameters {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GraphsQueryParameters.class.getName());

	public final String QUERY;
	public final String RDF;
	public final int EVALUATOR_INDEX;
	public final SPARQLINFERENCE INFERENCE;
	public final GENERATION INFERENCE_GENERATION;
	public final boolean OWL2RL_INCONSISTENCY_CHECK;
	public final String RIF;

	private GraphsQueryParameters(final String query, final String rdf, final int evaluatorIndex,
			final SPARQLINFERENCE inference, final GENERATION inferenceGeneration,
			final boolean owl2rlInconsistencyCheck, final String rif) {
		this.QUERY = query;
		this.RDF = rdf;
		this.EVALUATOR_INDEX = evaluatorIndex;
		this.INFERENCE = inference;
		this.INFERENCE_GENERATION = inferenceGeneration;
		this.OWL2RL_INCONSISTENCY_CHECK = owl2rlInconsistencyCheck;
		this.RIF = rif;
	}

	/**
	 * Factory method for GraphsQueryParameters. It parses a JSON request and
	 * returns all parameters needed for execution.
	 *
	 * @param json
	 *            the JSON object
	 * @return all parameters needed for execution
	 * @throws JSONException
	 *             if JSON was malformed
	 * @throws RuntimeException
	 *             if something else went wrong
	 */
	public static GraphsQueryParameters getParametersFromJson(final String json) {
		// Parse the JSON body and retrieve request keys
		// At least "query" and "rdf" must be there
		String query = null;
		String rdf = null;
		int evaluatorIndex = EvaluationHelper
				.getEvaluatorIndexByName("MemoryIndex");
		SPARQLINFERENCE inference = SPARQLINFERENCE.NONE;
		GENERATION inferenceGeneration = GENERATION.FIXED;
		boolean owl2rlInconsistencyCheck = false;
		String rif = "";
		final Set<Formatter> formatters = new HashSet<>();
		formatters.add(Endpoint.getRegisteredFormatters().get("json"));

		try {
			final JSONObject request = new JSONObject(json);

			if (!request.has("query")) {
				LOGGER.info("Missing key \"query\" in request body");
				throw new RuntimeException(
						"Key \"query\" must be present in body.");
			}
			query = request.getString("query");

			if (!request.has("rdf")) {
				LOGGER.info("Missing key \"rdf\" in request body");
				throw new RuntimeException(
						"Key \"rdf\" must be present in body.");
			}

			rdf = request.getString("rdf");

			if (request.has("evaluator")) {
				final String evaluator = request.getString("evaluator");
				try {
					evaluatorIndex = EvaluationHelper
							.getEvaluatorIndexByName(evaluator);
				} catch (final RuntimeException e) {
					LOGGER.info("Requested Evaluator {} not registered",
							evaluator);
					throw new RuntimeException(String.format(
							"Evaluator %s not registered.", evaluator));
				}
			}

			if (request.has("inference")) {
				final String requestedInferenceMode = request.getString("inference");
				try {
					inference = SPARQLINFERENCE.valueOf(requestedInferenceMode);
				} catch (final IllegalArgumentException e) {
					LOGGER.info("Requested inference mode {} not known",
							requestedInferenceMode);
					throw new RuntimeException(String.format(
							"Inference mode %s not known.",
							requestedInferenceMode), e);
				}
			}

			if (request.has("inferenceGeneration")) {
				final String requestedInferenceGeneration = request
						.getString("inferenceGeneration");
				try {
					inferenceGeneration = GENERATION
							.valueOf(requestedInferenceGeneration);
				} catch (final IllegalArgumentException e) {
					LOGGER.info(
							"Requested inference generation mode {} not known",
							requestedInferenceGeneration);
					throw new RuntimeException(String.format(
							"Inference generation mode %s not known.",
							requestedInferenceGeneration), e);
				}
			}

			if (request.has("owl2rlInconsistencyCheck")) {
				owl2rlInconsistencyCheck = request
						.getBoolean("owl2rlInconsistencyCheck");
			}

			if (request.has("rif")) {
				rif = request.getString("rif");
			}
		} catch (final JSONException e) {
			LOGGER.info("Received malformed JSON");
			throw e;
		}

		return new GraphsQueryParameters(query, rdf, evaluatorIndex, inference,
				inferenceGeneration, owl2rlInconsistencyCheck, rif);
	}
}
