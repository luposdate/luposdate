package lupos.endpoint.contexts;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.EvaluationHelper;
import lupos.endpoint.EvaluationHelper.GENERATION;
import lupos.endpoint.EvaluationHelper.SPARQLINFERENCE;
import lupos.endpoint.EvaluationHelper.SPARQLINFERENCEMATERIALIZATION;
import lupos.endpoint.server.Endpoint;
import lupos.endpoint.server.format.Formatter;
import lupos.endpoint.server.format.JSONFormatter;
import lupos.misc.Triple;
import lupos.misc.Tuple;
import lupos.rif.datatypes.Predicate;
import lupos.rif.datatypes.RuleResult;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.TokenMgrError;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.json.JSONArray;
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
public class ExtendedQueryHandler implements InterruptableHttpHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedQueryHandler.class.getName());

	private final boolean RIF_EVALUATION;

	public ExtendedQueryHandler(final boolean rifEvaluation) {
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
			ExecutionParameters parameters = null;
			try {
				final String requestBody = IOUtils.toString(t.getRequestBody());
				LOGGER.info("Handling requestBody {}", requestBody);
				parameters = ExecutionParameters.getParametersFromJson(requestBody);
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
			Tuple<String, QueryResult[]> result = null;
			try {
				// Use the magic getQueryResult method
				// We get all parameters from the request (or the default
				// values) except for two.
				// Second parameter indicates if we are processing RIF.
				// Fifth parameters indicates that we don't want to store
				// inferred triples in the DB (We process everything in memory).
				result = EvaluationHelper
						.getQueryResult(
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
			final boolean emptyResult = result.getSecond().length == 0;

			final JSONObject responseTmp = new JSONObject();

			// Format the output
			if (emptyResult) {
				for (final Formatter formatter : parameters.FORMATTERS) {
					if (formatter instanceof JSONFormatter) {
						responseTmp.append(formatter.getName(), new JSONObject());
					} else {
						responseTmp.append(formatter.getName(), "");
					}
				}
			}

			for (final QueryResult queryResult : result.getSecond()) {
				for (final Formatter formatter : parameters.FORMATTERS) {
					final OutputStream os = new ByteArrayOutputStream();

					formatter.writeResult(os, queryResult.getVariableSet(),
							queryResult);
					// In case of JSON formatting, we'll insert it non-escaped
					// as a
					// nested JSON object
					if (formatter instanceof JSONFormatter) {
						System.out.println(os.toString());
						if (queryResult instanceof GraphResult) {
							final JSONObject graphResult = new JSONObject();
							graphResult.put("rdf", os.toString());
							responseTmp.append(formatter.getName(), graphResult);
						} else {
							responseTmp.append(formatter.getName(),
									new JSONObject(os.toString()));
						}
					} else {
						responseTmp.append(formatter.getName(), os.toString());
					}
				}
				// Add additional (formatter independent) information
				if (queryResult instanceof GraphResult) {
					final GraphResult graphResult = (GraphResult) queryResult;
					final JSONFormatter jsonFormatter = (JSONFormatter) Endpoint.getRegisteredFormatters().get("json");

					for (final lupos.datastructures.items.Triple triple : graphResult.getGraphResultTriples()) {
						final JSONObject tripleJson = new JSONObject();
						final OutputStream osSubject = new ByteArrayOutputStream();
						final OutputStream osPredicate = new ByteArrayOutputStream();
						final OutputStream osObject = new ByteArrayOutputStream();

						jsonFormatter.writeLiteral(osSubject, triple.getSubject());
						jsonFormatter.writeLiteral(osPredicate, triple.getPredicate());
						jsonFormatter.writeLiteral(osObject, triple.getObject());

						tripleJson.put("subject", new JSONObject('{' + osSubject.toString() + '}'));
						tripleJson.put("predicate", new JSONObject('{' + osPredicate.toString() + '}'));
						tripleJson.put("object", new JSONObject('{' + osObject.toString() + '}'));
						responseTmp.append("triples", tripleJson);
					}
				} else if (queryResult instanceof RuleResult) {
					final RuleResult ruleResult = (RuleResult) queryResult;
					final JSONFormatter jsonFormatter = (JSONFormatter) Endpoint.getRegisteredFormatters().get("json");

					for (final Predicate predicate : ruleResult.getPredicateResults()) {
						final JSONObject predicateJson = new JSONObject();

						final OutputStream osLiteralName = new ByteArrayOutputStream();
						jsonFormatter.writeLiteral(osLiteralName, predicate.getName());

						predicateJson.put("predicateName", new JSONObject('{' + osLiteralName.toString() + '}'));

						for (final Literal parameter : predicate.getParameters()) {
							final OutputStream osParameter = new ByteArrayOutputStream();
							jsonFormatter.writeLiteral(osParameter, parameter);
							predicateJson.append("parameters", new JSONObject('{' + osParameter.toString() + '}'));
						}

						responseTmp.append("predicates", predicateJson);
					}
				}
			}
			response = responseTmp;
			responseStatus = QueryHandlerHelper.HTTP_OK;
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

class ExecutionParameters {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ExecutionParameters.class.getName());

	public final String QUERY;
	public final String RDF;
	public final int EVALUATOR_INDEX;
	public final SPARQLINFERENCE INFERENCE;
	public final GENERATION INFERENCE_GENERATION;
	public final boolean OWL2RL_INCONSISTENCY_CHECK;
	public final String RIF;
	public final Set<Formatter> FORMATTERS;

	private ExecutionParameters(final String query, final String rdf, final int evaluatorIndex,
			final SPARQLINFERENCE inference, final GENERATION inferenceGeneration,
			final boolean owl2rlInconsistencyCheck, final String rif,
			final Set<Formatter> formatters) {
		this.QUERY = query;
		this.RDF = rdf;
		this.EVALUATOR_INDEX = evaluatorIndex;
		this.INFERENCE = inference;
		this.INFERENCE_GENERATION = inferenceGeneration;
		this.OWL2RL_INCONSISTENCY_CHECK = owl2rlInconsistencyCheck;
		this.RIF = rif;
		this.FORMATTERS = formatters;
	}

	/**
	 * Factory method for ExecutionParameters. It parses a JSON request and
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
	public static ExecutionParameters getParametersFromJson(final String json) {
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

			if (request.has("formats")) {
				formatters.clear();
				final JSONArray formats = request.getJSONArray("formats");
				for (int i = 0; i < formats.length(); i++) {
					final String formatName = formats.getString(i).toLowerCase();
					final Formatter formatter = Endpoint.getRegisteredFormatters()
							.get(formatName);
					if (formatter != null) {
						formatters.add(formatter);
					} else {
						LOGGER.info(
								"There is no formatter registered for request output format {}",
								formatName);
						throw new RuntimeException(
								String.format(
										"There is no formatter for request ouput format %s.",
										formatName));
					}
				}
			}
		} catch (final JSONException e) {
			LOGGER.info("Received malformed JSON");
			throw e;
		}

		return new ExecutionParameters(query, rdf, evaluatorIndex, inference,
				inferenceGeneration, owl2rlInconsistencyCheck, rif, formatters);
	}
}
