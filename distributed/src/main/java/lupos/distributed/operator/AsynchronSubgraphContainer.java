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
package lupos.distributed.operator;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.operator.format.SubgraphContainerFormatter;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is an adaption of the original {@link lupos.distributed.operator.SubgraphContainer} that
 * processes asynchrony, so that it return immediately {@code null}, but
 * processes the result to its succeeding operators if the result is back in
 * {@link #preProcessMessage(EndOfEvaluationMessage)}.
 *
 * @author Bjoern
 * @version $Id: $Id
 */
public class AsynchronSubgraphContainer<K> extends SubgraphContainer<K> {

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return (super.toString().replace("SubgraphContainer",
				"AsynchronSubgraphContainer"));
	}

	/**
	 * New asychron working subgraph container
	 *
	 * @param rootNodeOfSubGraph
	 *            the inner root of the subgraph
	 * @param key
	 *            the key for distribution
	 * @param subgraphExecutor
	 *            the subgraph executer, for executing inner packed subgraph
	 *            container.
	 * @throws org.json.JSONException
	 *             Error during JSON processing
	 */
	public AsynchronSubgraphContainer(Root rootNodeOfSubGraph, K key,
			ISubgraphExecutor<K> subgraphExecutor) throws JSONException {
		super(rootNodeOfSubGraph, key, subgraphExecutor);
	}

	/**
	 * Creates an {@link lupos.distributed.operator.AsynchronSubgraphContainer} by an already existing
	 * {@link lupos.distributed.operator.SubgraphContainer}, but without connection to preceding and
	 * succeeding operators of the {@link lupos.distributed.operator.SubgraphContainer}
	 *
	 * @param c
	 *            the {@link lupos.distributed.operator.SubgraphContainer} to clone to an
	 *            {@link lupos.distributed.operator.AsynchronSubgraphContainer}.
	 * @return the new {@link lupos.distributed.operator.SubgraphContainer} with asynchrony processing
	 */
	public static AsynchronSubgraphContainer<?> cloneFrom(SubgraphContainer<?> c) {
		try {
			/*
			 * create a new instance with the same root, key and executer and
			 * copy all necessary parameter
			 */
			AsynchronSubgraphContainer as = new AsynchronSubgraphContainer(
					c.getRootOfSubgraph(), c.getKey(), c.subgraphExecutor);
			as.setCycleOperands(c.getCycleOperands());
			as.setIntersectionVariables(c.getIntersectionVariables());
			as.setUnionVariables(c.getUnionVariables());
			return as;
		} catch (JSONException e) {
			throw new RuntimeException(
					"Error creating asynchron subgraph container");
		}
	}

	/*
	 * Object which waits for QueryResult of processing
	 */
	private Future<QueryResult> waitForResult;
	private ExecutorService executor;

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final Dataset dataset) {
		/*
		 * Do processing here in a separate thread, and return null, so that the
		 * next operation can be processed parallel.
		 */
		Callable<QueryResult> c = new Callable<QueryResult>() {
			@Override
			public QueryResult call() throws Exception {
				//Get the processing result asynchron
				final SubgraphContainerFormatter serializer = new SubgraphContainerFormatter();
				try {
					final JSONObject serializedGraph = serializer
							.serialize(AsynchronSubgraphContainer.this
									.getRootOfSubgraph(), 0);
					final QueryResult result = AsynchronSubgraphContainer.this.subgraphExecutor
							.evaluate(AsynchronSubgraphContainer.this.getKey(),
									serializedGraph.toString(),bindingsFactory);
					result.materialize();
					result.materialize(); // just for now read all from the
											// stream sent by the endpoint,
											// otherwise it may be blocked! (may
											// be removed if each endpoint can
											// work completely in parallel!)
					
					return result;
				} catch (final JSONException e) {
					System.err.println(e);
					e.printStackTrace();
					return null;
				}
			}
		};
		/*
		 * start processing in a new thread
		 */
		executor = Executors.newSingleThreadExecutor();
		waitForResult = executor.submit(c);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(EndOfEvaluationMessage msg) {
		/*
		 * if the message arrives before or immediatly after the
		 * process()-method, the variable is maybe not set, so wait until the
		 * Future is set!
		 */
		while (waitForResult == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		/*
		 * now we want to wait for the result of the subgraph
		 */
		QueryResult result;
		try {
			result = waitForResult.get();
		} catch (InterruptedException e) {
			result = null;
			e.printStackTrace();
		} catch (ExecutionException e) {
			result = null;
			e.printStackTrace();
		}
		// for now, forward the result to all succeeding's ....
		if (result != null) result.materialize();
		for (final OperatorIDTuple opId : this.succeedingOperators) {
			if (result != null) 
				opId.processAll(result);
		}
		if (executor != null) executor.shutdown();
		return super.preProcessMessage(msg);
	}
}
