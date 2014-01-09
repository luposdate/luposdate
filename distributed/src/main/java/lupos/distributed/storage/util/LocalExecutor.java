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
package lupos.distributed.storage.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.bindings.BindingsArrayPresortingNumbers;
import lupos.datastructures.bindings.BindingsArrayVarMinMax;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.operator.format.Helper;
import lupos.distributed.operator.format.SubgraphContainerFormatter;
import lupos.distributed.operator.format.operatorcreator.IOperatorCreator;
import lupos.endpoint.server.format.Formatter;
import lupos.endpoint.server.format.JSONFormatter;
import lupos.endpoint.server.format.XMLFormatter;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.application.CollectResult;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.index.adaptedRDF3X.SixIndices;
import lupos.engine.operators.messages.BindingsFactoryMessage;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.engine.operators.singleinput.sort.fastsort.FastSort;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.statistics.VarBucket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class provides methods to evaluate a subgraph given as serialized JSON string,
 * as well as to determine the histograms of a triple pattern given as serialized JSON string.
 */
public class LocalExecutor {

	/**
	 * This methods transforms a subgraph given as serialized JSON string into an operator graph,
	 * which is executed and its result is returned serialized as string in JSON format...
	 *
	 * @param subgraphSerializedAsJSONString the serialized JSON string for the subgraph
	 * @param dataset the dataset on which the subgraph must be evaluated
	 * @param operatorCreator the creator for the operators of the subgraph
	 * @return a tuple with the mime type of the serialized query result as well as the serialized query result in JSON format
	 * @throws JSONException
	 * @throws IOException
	 */
	public static Tuple<String, String> evaluateSubgraphAndReturnSerializedJSONResult(final String subgraphSerializedAsJSONString, final Dataset dataset, final IOperatorCreator operatorCreator) throws JSONException, IOException {
		return LocalExecutor.evaluateSubgraphAndReturnSerializedResult(subgraphSerializedAsJSONString, dataset, operatorCreator, new JSONFormatter());
	}

	/**
	 * This methods transforms a subgraph given as serialized JSON string into an operator graph,
	 * which is executed and its result is returned serialized as string in XML format...
	 *
	 * @param subgraphSerializedAsJSONString the serialized JSON string for the subgraph
	 * @param dataset the dataset on which the subgraph must be evaluated
	 * @param operatorCreator the creator for the operators of the subgraph
	 * @return a tuple with the mime type of the serialized query result as well as the serialized query result in XML format
	 * @throws JSONException
	 * @throws IOException
	 */
	public static Tuple<String, String> evaluateSubgraphAndReturnSerializedXMLResult(final String subgraphSerializedAsJSONString, final Dataset dataset, final IOperatorCreator operatorCreator) throws JSONException, IOException {
		return LocalExecutor.evaluateSubgraphAndReturnSerializedResult(subgraphSerializedAsJSONString, dataset, operatorCreator, new XMLFormatter());
	}


	/**
	 * This methods transforms a subgraph given as serialized JSON string into an operator graph,
	 * which is executed and its result is returned serialized as string...
	 *
	 * @param subgraphSerializedAsJSONString the serialized JSON string for the subgraph
	 * @param dataset the dataset on which the subgraph must be evaluated
	 * @param operatorCreator the creator for the operators of the subgraph
	 * @param formatter the formatter according to which the query result is serialized
	 * @return a tuple with the mime type of the serialized query result as well as the serialized query result
	 * @throws JSONException
	 * @throws IOException
	 */
	public static Tuple<String, String> evaluateSubgraphAndReturnSerializedResult(final String subgraphSerializedAsJSONString, final Dataset dataset, final IOperatorCreator operatorCreator, final Formatter formatter) throws JSONException, IOException {
		final Tuple<QueryResult, Set<Variable>> result = LocalExecutor.evaluateSubgraph(subgraphSerializedAsJSONString, dataset, operatorCreator);
		final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		final String mimeType = formatter.getMIMEType(result.getFirst());
		formatter.writeResult(arrayOutputStream, result.getSecond(), result.getFirst());
		arrayOutputStream.close();
		return new Tuple<String, String>(new String(arrayOutputStream.toByteArray()), mimeType);
	}


	/**
	 * This methods transforms a subgraph given as serialized JSON string into an operator graph,
	 * which is executed and its result is returned...
	 *
	 * @param subgraphSerializedAsJSONString the serialized JSON string for the subgraph
	 * @param dataset the dataset on which the subgraph must be evaluated
	 * @param operatorCreator the creator for the operators of the subgraph
	 * @return the query result and the set of variables of the query result of the evaluated operator graph
	 * @throws JSONException in case of any parse exceptions
	 */
	public static Tuple<QueryResult, Set<Variable>> evaluateSubgraph(final String subgraphSerializedAsJSONString, final Dataset dataset, final IOperatorCreator operatorCreator) throws JSONException {
		final CollectResult collectResult = new CollectResult(true);
		final SubgraphContainerFormatter formatter = new SubgraphContainerFormatter(dataset, operatorCreator, collectResult);
		final Root root = formatter.deserialize(new JSONObject(subgraphSerializedAsJSONString));

		// some initializations
		root.deleteParents();
		root.setParents();
		root.detectCycles();
		root.sendMessage(new BoundVariablesMessage());

		Class<? extends Bindings> instanceClass = null;
		if (Bindings.instanceClass == BindingsArrayVarMinMax.class
				|| Bindings.instanceClass == BindingsArrayPresortingNumbers.class) {
			// is BindingsArrayVarMinMax or BindingsArrayPresortingNumbers
			// necessary? Or is only BindingsArray sufficient?
			@SuppressWarnings("serial")
			final SimpleOperatorGraphVisitor sogv = new SimpleOperatorGraphVisitor() {
				public boolean found = false;

				@Override
				public Object visit(final BasicOperator basicOperator) {
					if (basicOperator instanceof FastSort) {
						this.found = true;
					}
					return null;
				}

				@Override
				public boolean equals(final Object o) {
					if (o instanceof Boolean) {
						return this.found == (Boolean) o;
					} else {
						return super.equals(o);
					}
				}
			};
			root.visit(sogv);
			if (sogv.equals(false)) {
				instanceClass = Bindings.instanceClass;
				Bindings.instanceClass = BindingsArray.class;
			}
		}

		final BindingsFactory bindingsFactory= BindingsFactory.createBindingsFactory(CommonCoreQueryEvaluator.getAllVariablesOfQuery(root));
		root.sendMessage(new BindingsFactoryMessage(bindingsFactory));

		// evaluate subgraph!
		root.sendMessage(new StartOfEvaluationMessage());
		root.startProcessing();
		root.sendMessage(new EndOfEvaluationMessage());

		if (instanceClass != null) {
			Bindings.instanceClass = instanceClass;
		}
		return new Tuple<QueryResult, Set<Variable>>(collectResult.getResult(), new HashSet<Variable>(LocalExecutor.getVariables(root)));
	}

	/**
	 * Determine the variables of the result operator by just going down the operator graph.
	 * Take care when extending the functionality: Cycles are currently not considered and will lead to infinite loop!
	 * @param root the root node
	 * @return the variables of the result operator!
	 */
	public static Collection<Variable> getVariables(final BasicOperator root){
		if(root.getSucceedingOperators().size()==0) {
			return root.getUnionVariables();
		} else {
			return LocalExecutor.getVariables(root.getSucceedingOperators().get(0).getOperator());
		}
	}

	/**
	 * Computes the minima and maxima of variables in a triple pattern
	 *
	 * @param minMaxRequestSerializedAsJSONString the json string of the request (variable set, triple pattern)
	 * @param dataset the dataset used by the evaluator
	 * @param operatorCreator the creator for creating operators
	 * @return the minima and maxima of the given variables serialized as json string
	 * @throws JSONException
	 */
	public static String getMinMaxSerializedAsJSONString(final String minMaxRequestSerializedAsJSONString, final Dataset dataset, final IOperatorCreator operatorCreator) throws JSONException {
		return LocalExecutor.getMinMaxSerializedAsJSONObject(minMaxRequestSerializedAsJSONString, dataset, operatorCreator).toString();
	}

	/**
	 * Computes the minima and maxima of variables in a triple pattern
	 *
	 * @param minMaxRequestSerializedAsJSONString the json string of the request (variable set, triple pattern)
	 * @param dataset the dataset used by the evaluator
	 * @param operatorCreator the creator for creating operators
	 * @return the minima and maxima of the given variables as JSON object
	 * @throws JSONException
	 */
	public static JSONObject getMinMaxSerializedAsJSONObject(final String minMaxRequestSerializedAsJSONString, final Dataset dataset, final IOperatorCreator operatorCreator) throws JSONException {
		 final Map<Variable, Tuple<Literal, Literal>> interResult = LocalExecutor.getMinMax(minMaxRequestSerializedAsJSONString, dataset, operatorCreator);
		 final JSONArray array = new JSONArray();
		 if(interResult != null){
			 for(final Entry<Variable, Tuple<Literal, Literal>> entry: interResult.entrySet()) {
				 final JSONObject entryJson = new JSONObject();
				 entryJson.put("variable", Helper.createVarAsJSONObject(entry.getKey()));
				 entryJson.put("minimum", Helper.createLiteralAsJSONObject(entry.getValue().getFirst()));
				 entryJson.put("maximum", Helper.createLiteralAsJSONObject(entry.getValue().getSecond()));
				 array.put(entryJson);
			 }
		 }
		 final JSONObject result = new JSONObject();
		 result.put("result", array);
		 return result;
	}

	/**
	 * Computes the minima and maxima of variables in a triple pattern
	 *
	 * @param minMaxRequestSerializedAsJSONString the json string of the request (variable set, triple pattern)
	 * @param dataset the dataset used by the evaluator
	 * @param operatorCreator the creator for creating operators
	 * @return the minima and maxima of the given variables
	 * @throws JSONException
	 */
	public static Map<Variable, Tuple<Literal, Literal>> getMinMax(final String minMaxRequestSerializedAsJSONString, final Dataset dataset, final IOperatorCreator operatorCreator) throws JSONException {
		final JSONObject json = new JSONObject(minMaxRequestSerializedAsJSONString);
		final Collection<Variable> vars = Helper.createVariablesFromJSON(json);
		final TriplePattern tp = Helper.createTriplePatternFromJSON(json);
		final Collection<TriplePattern> tps = new LinkedList<TriplePattern>();
		tps.add(tp);
		final BasicIndexScan indexScan = operatorCreator.createIndexScan(operatorCreator.createRoot(dataset), tps);
		final BindingsFactory bindingsFactory = BindingsFactory.createBindingsFactory(tp.getVariables());
		indexScan.setBindingsFactory(bindingsFactory);
		tp.setBindingsFactory(bindingsFactory);

		return indexScan.getMinMax(tp, vars);
	}

	/**
	 * Computes the histograms of given variables based on a given triple pattern
	 *
	 * @param histogramRequestSerializedAsJSONString the triple pattern, the variables to be considered and their minima and maxima, serialized as json string
	 * @param dataset the dataset used by the evaluator
	 * @param operatorCreator the creator for creating operators
	 * @return the histograms of the given triple pattern serialized as json string
	 * @throws JSONException
	 */
	public static String getHistogramsAsJSONString(final String histogramRequestSerializedAsJSONString, final Dataset dataset, final IOperatorCreator operatorCreator) throws JSONException {
		return LocalExecutor.getHistogramsAsJSONObject(histogramRequestSerializedAsJSONString, dataset, operatorCreator).toString();
	}

	/**
	 * Computes the histograms of given variables based on a given triple pattern
	 *
	 * @param histogramRequestSerializedAsJSONString the triple pattern, the variables to be considered and their minima and maxima, serialized as json string
	 * @param dataset the dataset used by the evaluator
	 * @param operatorCreator the creator for creating operators
	 * @return the histograms of the given triple pattern as json object
	 * @throws JSONException
	 */
	public static JSONObject getHistogramsAsJSONObject(final String histogramRequestSerializedAsJSONString, final Dataset dataset, final IOperatorCreator operatorCreator) throws JSONException {
		return Helper.createMapJSONObject(LocalExecutor.getHistograms(histogramRequestSerializedAsJSONString, dataset, operatorCreator));
	}

	/**
	 * Computes the histograms of given variables based on a given triple pattern
	 *
	 * @param histogramRequestSerializedAsJSONString the triple pattern, the variables to be considered and their minima and maxima, serialized as json string
	 * @param dataset the dataset used by the evaluator
	 * @param operatorCreator the creator for creating operators
	 * @return the histograms of the given triple pattern
	 * @throws JSONException
	 */
	public static Map<Variable, VarBucket> getHistograms(final String histogramRequestSerializedAsJSONString, final Dataset dataset, final IOperatorCreator operatorCreator) throws JSONException {
		final JSONObject json = new JSONObject(histogramRequestSerializedAsJSONString);
		final Collection<Variable> vars = Helper.createVariablesFromJSON(json);
		final TriplePattern tp = Helper.createTriplePatternFromJSON(json);
		final Map<Variable, Literal> minima = Helper.createMapFromJSON(json.getJSONObject("minima"));
		final Map<Variable, Literal> maxima = Helper.createMapFromJSON(json.getJSONObject("maxima"));
		final Collection<TriplePattern> tps = new LinkedList<TriplePattern>();
		tps.add(tp);
		final BasicIndexScan indexScan = operatorCreator.createIndexScan(operatorCreator.createRoot(dataset), tps);
		final BindingsFactory bindingsFactory = BindingsFactory.createBindingsFactory(tp.getVariables());
		indexScan.setBindingsFactory(bindingsFactory);
		tp.setBindingsFactory(bindingsFactory);

		return indexScan.getVarBuckets(tp, Bindings.instanceClass, vars, minima, maxima);
	}

	/**
	 * Computes a histogram or a min/max request depending on the information given in the json string
	 *
	 * @param requestSerializedAsJSONString the request type as well as its parameters
	 * @param dataset the dataset used by the evaluator
	 * @param operatorCreator the creator for creating operators
	 * @return the histograms or the min/max values depending on the quest type, serialized as json string
	 * @throws JSONException
	 */
	public static String getHistogramOrMinMax(final String requestSerializedAsJSONString, final Dataset dataset, final IOperatorCreator operatorCreator) throws JSONException {
		final JSONObject json = new JSONObject(requestSerializedAsJSONString);
		if(json.has("histogram_request")){
			return LocalExecutor.getHistogramsAsJSONString(json.getJSONObject("histogram_request").toString(), dataset, operatorCreator);
		} else if(json.has("min_max_request")) {
			return LocalExecutor.getMinMaxSerializedAsJSONString(json.getJSONObject("min_max_request").toString(), dataset, operatorCreator);
		} else if(json.has("rebuild_statistics_request")){
			return LocalExecutor.buildHistograms(dataset);
		} else {
			return "Error: unknown type of request!";
		}
	}

	/**
	 * (Re-) Builds the statistics for the RDF3X evaluator.
	 * After huge updates of the triples, a rebuilding may be necessary to have a precise join order optimization.
	 *
	 * @param dataset the dataset the statistics of which needs to be rebuilt
	 * @return "ready" in case of success
	 */
	public static String buildHistograms(final Dataset dataset){
		LocalExecutor.buildHistograms(dataset.getDefaultGraphIndices());
		LocalExecutor.buildHistograms(dataset.getNamedGraphIndices());
		return "ready";
	}

	/**
	 * (Re-) Builds the statistics for the RDF3X evaluator.
	 * After huge updates of the triples, a rebuilding may be necessary to have a precise join order optimization.
	 *
	 * @param collectionOfIndices the indices the statistics of which need to be rebuilt
	 */
	private static void buildHistograms(final Collection<Indices> collectionOfIndices){
		for(final Indices indices: collectionOfIndices){
			if(indices instanceof SixIndices){
				((SixIndices)indices).generateStatistics();
			}
		}
	}
}