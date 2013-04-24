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
package lupos.distributed.operator;

import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.operator.format.SubgraphContainerFormatter;
import lupos.engine.operators.RootChild;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.rdf.Prefix;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This container contains all operators that shall be send to a node for execution.
 *
 * @param <K> the type of key used to address the node where this operator graph is sent to.
 */
public class SubgraphContainer<K> extends RootChild {

	/**
	 * The root node of the sub graph.
	 */
	private final Root rootNodeOfSubGraph;

	/**
	 * the key which identifies to which node the operator graph is sent to
	 */
	private final K key;

	/**
	 * the executor to submit a subgraph and retrieve its query result...
	 */
	private final ISubgraphExecutor<K> subgraphExecutor;

	/**
	 * Instantiates a new sub graph container.
	 *
	 * @param rootNodeOfSubGraph
	 * 		the root node of sub graph
	 * @param key the key which identifies to which node the operator graph is sent to
	 * @throws JSONException
	 */
	public SubgraphContainer(final Root rootNodeOfSubGraph, final K key, final ISubgraphExecutor<K> subgraphExecutor) throws JSONException {
		this.key = key;
		this.rootNodeOfSubGraph = rootNodeOfSubGraph;
		this.subgraphExecutor = subgraphExecutor;
	}

	/**
	 * Gets called when the operator is to be executed. When called this method sends the sub graph to the responsible
	 * nodes for execution and waits for the result to return.
	 *
	 * @param dataset
	 * 		the data set
	 *
	 * @return the result of the query execution
	 */
	@Override
	public QueryResult process(final Dataset dataset) {
		final SubgraphContainerFormatter serializer = new SubgraphContainerFormatter();
		try {
			final JSONObject serializedGraph = serializer.serialize(this.rootNodeOfSubGraph, 0);
			final QueryResult result = this.subgraphExecutor.evaluate(this.key,  serializedGraph.toString());
			result.materialize(); // just for now read all from the stream sent by the endpoint, otherwise it may be blocked! (may be removed if each endpoint can work completely in parallel!)
			return result;
		} catch (final JSONException e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		return "SubgraphContainer";
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		return this.toString();
	}

	public Root getRootOfSubgraph(){
		return this.rootNodeOfSubGraph;
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage newMsg = new BoundVariablesMessage(msg);
		newMsg.setVariables(this.getUnionVariables());
		return newMsg;
	}
}
