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

import lupos.endpoint.EvaluationHelper;
import lupos.endpoint.GraphSerialization;
import lupos.endpoint.GraphSerialization.AstFormat;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperAST;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperASTRIF;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperRules;
import lupos.misc.Triple;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.TokenMgrError;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.query.MalformedQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryParseException;
import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class InfoQueryHandler implements InterruptableHttpHandler {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(InfoQueryHandler.class.getName());

	public final boolean RIF_EVALUATION;

	public InfoQueryHandler(final boolean rifEvaluation) {
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
			InfoQueryParameters parameters = null;
			try {
				final String requestBody = IOUtils.toString(t.getRequestBody());
				parameters = InfoQueryParameters
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
			LOGGER.info("Using evaluator with index: {}",
					parameters.EVALUATOR_INDEX);
			LOGGER.info("Requested AST format is: {}", parameters.AST_FORMAT);

			if (this.RIF_EVALUATION) {
				LOGGER.info("Starting processing RIF");
			} else {
				LOGGER.info("Starting processing SPARQL");
			}
			Triple<GraphWrapper, String, GraphWrapper> result = null;
			try {
				// Use the magic getCompileInfo method
				result = EvaluationHelper.getCompileInfo(
						parameters.EVALUATOR_INDEX, this.RIF_EVALUATION,
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
			}
			LOGGER.info("Finished processing");

			final JSONObject responseTmp = new JSONObject();
			if (result == null) {
				responseTmp.put("info",
						"Compiler does not provide additional information.");
			} else {
				if (this.RIF_EVALUATION) {
					final GraphWrapperASTRIF ast = (GraphWrapperASTRIF) result
							.getFirst();
					final GraphWrapperRules astRules = (GraphWrapperRules) result
							.getThird();
					if (ast != null) {
						responseTmp.put("AST", GraphSerialization.rifAstToJson(
								ast, parameters.AST_FORMAT));
					}
					if (astRules != null) {
						responseTmp.put("rulesAST",
								GraphSerialization.rulesAstToJson(astRules,
										parameters.AST_FORMAT));
					}
				} else {
					final GraphWrapperAST ast = (GraphWrapperAST) result.getFirst();
					final String coreQuery = result.getSecond();
					final GraphWrapperAST coreAst = (GraphWrapperAST) result
							.getThird();
					if (ast != null) {
						responseTmp.put("AST", GraphSerialization.astToJson(ast,
								parameters.AST_FORMAT));
					}
					if (coreQuery != null) {
						responseTmp.put("coreSPARQL", result.getSecond());
					}
					if (coreAst != null) {
						responseTmp.put("coreAST", GraphSerialization.astToJson(
								coreAst, parameters.AST_FORMAT));
					}
				}
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

class InfoQueryParameters {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(InfoQueryParameters.class.getName());

	public final String QUERY;
	public final int EVALUATOR_INDEX;
	public final AstFormat AST_FORMAT;

	private InfoQueryParameters(final String query, final int evaluatorIndex,
			final AstFormat astFormat) {
		this.QUERY = query;
		this.EVALUATOR_INDEX = evaluatorIndex;
		this.AST_FORMAT = astFormat;
	}

	/**
	 * Factory method for AstQueryParameters. It parses a JSON request and
	 * returns all parameters needed for retrieving a corresponding AST.
	 *
	 * @param json
	 *            the JSON object
	 * @return all parameters needed for retrieving AST
	 * @throws JSONException
	 *             if JSON was malformed
	 * @throws RuntimeException
	 *             if something else went wrong
	 */
	public static InfoQueryParameters getParametersFromJson(final String json) {
		// Parse the JSON and retrieve request keys
		// Key "query" is mandatory
		String query = null;
		int evaluatorIndex = EvaluationHelper
				.getEvaluatorIndexByName("MemoryIndex");
		AstFormat astFormat = AstFormat.NESTED;

		try {
			final JSONObject request = new JSONObject(json);

			if (!request.has("query")) {
				LOGGER.info("Missing key \"query\" in request body");
				throw new RuntimeException(
						"Key \"query\" must be present in body.");
			}
			query = request.getString("query");

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

			if (request.has("astFormat")) {
				final String format = request.getString("astFormat");
				try {
					astFormat = AstFormat.valueOf(format.toUpperCase());
				} catch (final IllegalArgumentException e) {
					LOGGER.info("Requested AST format {} is unknown.", format);
					throw new RuntimeException(String.format(
							"AST format %s is unknown.", format));
				}
			}
		} catch (final JSONException e) {
			LOGGER.info("Received malformed JSON");
			throw e;
		}

		return new InfoQueryParameters(query, evaluatorIndex, astFormat);
	}
}
