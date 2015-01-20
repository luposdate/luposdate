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
package lupos.distributed.operator.format;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import lupos.distributed.operator.AsynchronSubgraphContainer;
import lupos.distributed.operator.ISubgraphExecutor;
import lupos.distributed.operator.SubgraphContainer;
import lupos.distributed.operator.format.operatorcreator.IOperatorCreator;
import lupos.distributed.query.operator.withouthistogramsubmission.QueryClientIndexScan;
import lupos.distributed.query.operator.withouthistogramsubmission.QueryClientRoot;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.application.Application;
import lupos.engine.operators.application.CollectResult;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.filter.Filter;
import lupos.engine.operators.singleinput.Result;
import lupos.misc.Tuple;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The Class SubGraphContainerFormatter.
 * @since 12/2013 <ul><li>subgraph container with included subgraph container<li>subgraph container with >1 succeeding operator, so also non-linear subgraphs are supported now!</ul> 
 */
public class SubgraphContainerFormatter implements OperatorFormatter {

	private int id_counter;

	private Root root;

	private Dataset dataset;

	private Application application;

	private IOperatorCreator operatorCreator;

	/*
	 * Is used if a subgraph container is included in the subgraph container to execute this container
	 */
	private ISubgraphExecutor<?> executer;

	public SubgraphContainerFormatter() {
	}
	
	/**
	 * Now constructor for the {@link SubgraphContainerFormatter}, which stores an {@link ISubgraphExecutor} 
	 * which is used to execute an included {@link SubgraphContainer}.
	 * @param executer The executer
	 */
	public SubgraphContainerFormatter(ISubgraphExecutor<?> executer) {
		this.executer = executer;
	}

	public SubgraphContainerFormatter(final Dataset dataset,
			final IOperatorCreator operatorCreator,
			final Application application) {
		this.dataset = dataset;
		this.application = application;
		this.operatorCreator = operatorCreator;
	}

	public SubgraphContainerFormatter(Dataset dataset2,
			IOperatorCreator operatorCreator2, CollectResult collectResult,
			ISubgraphExecutor<?> sgExecuter) {
		this(dataset2,operatorCreator2,collectResult);
		this.executer = sgExecuter;
	}

	@Override
	public JSONObject serialize(final BasicOperator operator, final int node_id)
			throws JSONException {
		final Collection<JSONObject> nodesJSON = newLinkedList();
		final Collection<JSONObject> edgesJSON = newLinkedList();

		this.id_counter = 0;

		this.serializeNode(new OperatorIDTuple(operator, 0), nodesJSON,
				edgesJSON, this.id_counter);
		final JSONObject serializedSubGraph = new JSONObject();

		try {
			serializedSubGraph.put("nodes", nodesJSON);
			serializedSubGraph.put("edges", edgesJSON);
		} catch (final JSONException e) {
			throw propagate(e);
		}

		return serializedSubGraph;
	}

	/*
	 * this is a map, where the operator and the node_is is stored (this is a map to notice forward/backward connections in operator graph)
	 */
	private HashMap<BasicOperator, Integer> map = new HashMap<BasicOperator, Integer>();
	/*
	 * this is a map, where the operator and the Tuple(node_id,edge_id) (this is a map to store connections to operator, that are visited later (forward connection))
	 */
	private HashMap<BasicOperator, Tuple<Integer,Integer>> addLater = new HashMap<BasicOperator, Tuple<Integer,Integer>>();
	/*
	 * only add an edge once! so check the edges here (we have to do that, because the contains-method
	 * in JSONObject does not do its job!)
	 */
	private List<String> containsEdge = new ArrayList<String>();

	/*
	 * adds an edge to the given JSONObject (if the edge is not added before!)
	 */
	private void addEdgeTo(final Collection<JSONObject> edgesJSON,
			JSONObject edge) {
		try {
			/*
			 * because of the lack of the JSONObject.contains()-method, just
			 * serialize and store the edge in own list!
			 */
			String key = String.format("%s-%s", edge.get("from"),
					edge.get("to"));
			if (containsEdge.contains(key))
				return;
			containsEdge.add(key);
			/*
			 * add the edge to the JSONObject and return 
			 */
			edgesJSON.add(edge);
		} catch (Exception e) {
			propagate(e);
		}
	}

	private void serializeNode(final OperatorIDTuple node,
			final Collection<JSONObject> nodesJSON,
			final Collection<JSONObject> edgesJSON, final int parent_id) {
		//Logger.getLogger(getClass()).debug("serializeNode: " + node + " with id: " + parent_id);
		this.id_counter++;

		final int edge_id = node.getId();

		final BasicOperator op = node.getOperator();
		boolean newEntry = false;
		
		/*
		 * store all Operators in our new map!
		 */
		if (!map.containsKey(op)) {
			map.put(op, id_counter);
			/*
			 * this is a new entry! (so no re-visit node, that is just serialized)
			 */
			newEntry = true;
		}

		/*
		 * for our forward-connections in the operator graph (we stored an edge to  a not known node, that is 
		 * now added, so we know its new id and can serialize the edge!)
		 */
		if (addLater.containsKey(op)) {
			/*
			 * an object is now serialized, that is used before! now we know its
			 * id, so we can add the edge from (op_id) to (already stored
			 * succeeding id)
			 */
			Tuple<Integer,Integer> data = addLater.get(op);
			JSONObject edge = new JSONObject();
			try {
				edge.put("from", id_counter);
				edge.put("to", data.getFirst());
				edge.put("edge_id", data.getSecond());
				addEdgeTo(edgesJSON, edge);
			} catch (Exception e) {
				propagate(e);
			}
		}
		if (parent_id > 0) {
			final JSONObject edge = new JSONObject();
			try {
				int counterID = this.id_counter;
				if (map.containsKey(op)) {
					/*
					 * get the node id of the operator
					 */
					counterID = map.get(op);
					if (op.getPrecedingOperators().size() == 0) {
						//if there is no preceding, it should be the root (node_id = 1)
						edge.put("from", 1);
						edge.put("to", counterID);
						edge.put("edge_id", edge_id);
						addEdgeTo(edgesJSON, edge);
					} else {
						//otherwise create edges to the current operator by
						//its preceding operators
						for (BasicOperator is : op.getPrecedingOperators()) {
							/*
							 * if the preceding is already known (an node_is is set),
							 * add the edge, otherwise store the information
							 * to be added, if the not known operator is stored!
							 */
							if (map.containsKey(is)) {
								edge.put("from", map.get(is));
								edge.put("to", counterID);
								edge.put("edge_id", edge_id);
								addEdgeTo(edgesJSON, edge);
							} else {
								/*
								 * we found an operator that is still not known! but we have
								 * to store the edge to the future serialized node!
								 */
								int tmpEdge_id = is.getOperatorIDTuple(op).getId();
								addLater.put(is, new Tuple<Integer,Integer>(counterID,tmpEdge_id));
							}
						}
					}

				} 
			} catch (final JSONException e) {
				e.printStackTrace();
			}
		}

		OperatorFormatter serializer;
		if (op instanceof BasicIndexScan) {
			serializer = new IndexScanFormatter();
		} else if (op instanceof Root) {
			serializer = new RootFormatter();
		} else if (op instanceof Result) {
			serializer = new ResultFormatter();
		} else if (op instanceof Filter) {
			serializer = new FilterFormatter();
		} else if (op instanceof Join) {
			/*
			 * added the Join formatter for joins in subgraph-containers
			 */
			serializer = new JoinFormatter();
		} else if (op instanceof SubgraphContainer) {
			/*
			 * added the formatter for an included subgraph in the subgraph 
			 */
			serializer = new SubSubgraphContainerFormatter();
		} else {
			throw new RuntimeException("Something is wrong here. Forgot case?");
		}

		try {
			/*
			 * only add new entry, because otherwise we serialize an node twice ;(
			 */
			if (newEntry) {
				nodesJSON.add(serializer.serialize(op, this.id_counter));
			} 
		} catch (final NullPointerException e) {
			throw new IllegalArgumentException(
					"This operator is not serializable", e);
		} catch (final JSONException e) {
			throw propagate(e);
		}

		for (final OperatorIDTuple successor : op.getSucceedingOperators()) {
			this.serializeNode(successor, nodesJSON, edgesJSON, this.id_counter);
		}
	}

	@Override
	public Root deserialize(final JSONObject serializedOperator)
			throws JSONException {
		this.root = null;
		
		final HashMap<Integer, BasicOperator> nodes = this
				.deserializeNodes(serializedOperator);

		final JSONArray edgesJson = (JSONArray) serializedOperator.get("edges");
		SubgraphContainerFormatter.deserializeEdges(edgesJson, nodes);

		return this.root;
	}

	private static void deserializeEdges(final JSONArray edgesJson,
			final HashMap<Integer, BasicOperator> nodes) throws JSONException {

		final HashMap<BasicOperator, List<OperatorIDTuple>> succeedingOperators = newHashMap();
		final HashMap<BasicOperator, List<BasicOperator>> precedingOperators = newHashMap();

		for (int i = 0; i < edgesJson.length(); i++) {

			final JSONObject edgeJson = edgesJson.getJSONObject(i);

			final BasicOperator from = nodes.get(edgeJson.getInt("from"));
			final BasicOperator to = nodes.get(edgeJson.getInt("to"));

			if (succeedingOperators.get(from) == null) {
				succeedingOperators
						.put(from, new LinkedList<OperatorIDTuple>());
			}

			if (precedingOperators.get(to) == null) {
				precedingOperators.put(to, new LinkedList<BasicOperator>());
			}

			succeedingOperators.get(from).add(
					new OperatorIDTuple(to, edgeJson.getInt("edge_id")));
			precedingOperators.get(to).add(from);
		}

		for (final Entry<BasicOperator, List<OperatorIDTuple>> from : succeedingOperators
				.entrySet()) {
			from.getKey().setSucceedingOperators(from.getValue());
		}

		for (final Entry<BasicOperator, List<BasicOperator>> to : precedingOperators
				.entrySet()) {
			to.getKey().setPrecedingOperators(to.getValue());
		}
	}

	private HashMap<Integer, BasicOperator> deserializeNodes(
			final JSONObject rootJson) throws JSONException {

		final HashMap<Integer, BasicOperator> nodes = newHashMap();
		final JSONArray nodesJson = (JSONArray) rootJson.get("nodes");

		final HashMap<String, OperatorFormatter> formatters = this
				.createFormatters();

		for (int i = 0; i < nodesJson.length(); i++) {

			final JSONObject nodeJson = nodesJson.getJSONObject(i);

			// get corresponding formatter from map
			
			final OperatorFormatter formatter = formatters.get(nodeJson
					.getString("type"));

			// add deserialized node to list
			
			final BasicOperator node = formatter.deserialize(nodeJson);
			nodes.put(nodeJson.getInt("node_id"), node);

			if (node instanceof Root) {
				final IndexScanFormatter indexScanFormatter = (IndexScanFormatter) formatters
						.get(BasicIndexScan.class.getName());
				indexScanFormatter.setRoot((Root) node);
			}

			try {
				if (nodeJson.getBoolean("root")) {
					this.root = (Root) node;
				}
			} catch (final JSONException e) {
				// ignore
			}
		}

		return nodes;
	}

	private HashMap<String, OperatorFormatter> createFormatters() {
		final HashMap<String, OperatorFormatter> formatters = newHashMap();
		formatters.put(Root.class.getName(), new RootFormatter(this.dataset,
				this.operatorCreator));
		formatters.put(BasicIndexScan.class.getName(), new IndexScanFormatter(
				this.operatorCreator));
		formatters.put(QueryClientRoot.class.getName(), new RootFormatter(
				this.dataset, this.operatorCreator));
		formatters.put(QueryClientIndexScan.class.getName(),
				new IndexScanFormatter(this.operatorCreator));
		/*
		 * added the join-formatter
		 */
		formatters.put(Join.class.getName(), new JoinFormatter(
				this.operatorCreator));
		formatters.put(Filter.class.getName(), new FilterFormatter());
		formatters.put(Result.class.getName(), new ResultFormatter(
				this.application));
		/*
		 * this is for de-serialization, so we need this constructor which has an
		 * ISubgraphExecuter included for executing the included subgraph container
		 */
		formatters.put(SubgraphContainer.class.getName(),
				new SubSubgraphContainerFormatter(this.dataset,
						this.operatorCreator,this.application,this.executer));
		formatters.put(AsynchronSubgraphContainer.class.getName(),
				new SubSubgraphContainerFormatter(this.dataset,
						this.operatorCreator,this.application,this.executer));
		return formatters;
	}
}