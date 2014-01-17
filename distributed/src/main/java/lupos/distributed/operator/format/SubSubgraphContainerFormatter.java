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
package lupos.distributed.operator.format;

import static com.google.common.base.Throwables.propagate;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import lupos.distributed.operator.ISubgraphExecutor;
import lupos.distributed.operator.SubgraphContainer;
import lupos.distributed.operator.format.operatorcreator.IOperatorCreator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.application.Application;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Root;

import org.json.JSONException;
import org.json.JSONObject;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;


/**
 * Implements the formatter for the Subgraph-Container (in a Subgraph-Container)
 *
 */
public class SubSubgraphContainerFormatter implements OperatorFormatter {

	/** The dataset. */
	private Dataset dataset;

	/**
	 * the operator creator for creating any operator
	 */
	private IOperatorCreator operatorCreator;

	/**
	 * the application
	 */
	private Application application;

	/**
	 * the subgraph executer, with which the included subgraph is executed
	 */
	private ISubgraphExecutor<?> executer;

	/**
	 * Gets the dataset.
	 *
	 * @return the dataset
	 */
	public Dataset getDataset() {
		return this.dataset;
	}

	/**
	 * Instantiates a new subgraph in subgraph formatter.
	 *
	 * @param dataset
	 *            the dataset
	 * @param executer
	 *            subgraph executer
	 */
	public SubSubgraphContainerFormatter(final Dataset dataset,
			final IOperatorCreator operatorCreator,
			final Application application, final ISubgraphExecutor<?> executer) {
		this.dataset = dataset;
		this.operatorCreator = operatorCreator;
		this.application = application;
		this.executer = executer;
	}

	/**
	 * Instantiates a new subgraph container formatter.
	 */
	public SubSubgraphContainerFormatter() {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public JSONObject serialize(final BasicOperator operator, final int node_id)
			throws JSONException {
		final JSONObject json = new JSONObject();
		/*
		 * store preceeding's and remove them from the subgraph, because they
		 * are not to be serialized (but they are added later)
		 */
		final List<BasicOperator> storePrecds = new LinkedList<BasicOperator>(((SubgraphContainer) operator)
						.getPrecedingOperators());
		for (final BasicOperator op : storePrecds) {
			((SubgraphContainer) operator).removePrecedingOperator(op);
		}
		/*
		 * store succeeding's and remove them from the subgraph (see above)
		 */
		final List<OperatorIDTuple> storeSuccs = new LinkedList<OperatorIDTuple>(((SubgraphContainer) operator)
						.getSucceedingOperators());
		for (final OperatorIDTuple op : storeSuccs) {
			((SubgraphContainer) operator).removeSucceedingOperator(op);
		}

		json.put("type", operator.getClass().getName());
		json.put("node_id", node_id);

		/*
		 * now serialize the subgraph container
		 */
		final SubgraphContainerFormatter serializer = new SubgraphContainerFormatter();
		final JSONObject serializedGraph = serializer.serialize(
				((SubgraphContainer) operator).getRootOfSubgraph(), 0);
		json.put("subgraph", serializedGraph);

		/*
		 * now serialize the subgraph container's key
		 */
		final Object key = ((SubgraphContainer) operator).getKey();
		try {
			final String sgKey = JsonWriter.objectToJson(key);
			json.put("key", sgKey);
		} catch (final IOException e) {
			json.put("key", "Not serializable.");
			propagate(new RuntimeException(String.format(
					"The key of subgraphContainer %s is not serialzable.",
					operator)));
		}

		// now add the connections ... we have removed above.
		for (final BasicOperator op : storePrecds) {
			((SubgraphContainer) operator).addPrecedingOperator(op);
		}
		for (final OperatorIDTuple op : storeSuccs) {
			((SubgraphContainer) operator).addSucceedingOperator(op);
		}
		return json;
	}

	@Override
	public BasicOperator deserialize(final JSONObject serializedOperator)
			throws JSONException {
		/*
		 * instanciate the subgraphcontainer formatter, which is used for
		 * subgraph execution
		 */
		final SubgraphContainerFormatter serializer = new SubgraphContainerFormatter(
				this.dataset, this.operatorCreator, this.application);

		final JSONObject _serializedOperator = serializedOperator
				.getJSONObject("subgraph");
		final Root r = serializer.deserialize(_serializedOperator);
		/*
		 * deserialize the key of the subgraph container
		 */
		final String subgraphKey = (String) serializedOperator.get("key");
		Object obj = null;
		try {
			obj = JsonReader.jsonToJava(subgraphKey);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		/*
		 * read out the subgraphType, otherwise set default
		 */
		String subgraphType = (String) serializedOperator.get("type");
		if (subgraphType == null) {
			subgraphType = SubgraphContainer.class.getName();
		}
		/*
		 * create the subgraph container with the given key and executer
		 */
		return this.invoke(subgraphType, r, obj, this.executer);
	}

	/*
	 * Invoke the constructor for creating the subgraph container with the given
	 * parameters.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SubgraphContainer<?> invoke(final String className, final Root r, final Object key,
			final ISubgraphExecutor<?> executer) {

		try {
			/*
			 * get the class, which has to be a subclass of SubgraphContainer
			 */
			final Class<?> c = Class.forName(className);
			if (!SubgraphContainer.class.isAssignableFrom(c)) {
				throw new RuntimeException(
						String.format(
								"The type \"%s\" of subgraph-container is not a class extended from lupos.distributed.operator.SubgraphContainer",
								className));
			} else {
				final Class<? extends SubgraphContainer> c1 = (Class<? extends SubgraphContainer>) c;
				try {
					final Constructor<? extends SubgraphContainer> construct = c1
							.getConstructor(Root.class, Object.class,
									ISubgraphExecutor.class);
					return construct.newInstance(r, key, executer);
				} catch (final NoSuchMethodException e) {
					throw new RuntimeException(
							String.format(
									"The class \"%s\" of subgraph-container has no valid constructor.",
									className));
				} catch (final SecurityException e) {
					propagate(e);
				} catch (final InstantiationException e) {
					propagate(e);
				} catch (final IllegalAccessException e) {
					propagate(e);
				} catch (final IllegalArgumentException e) {
					throw new RuntimeException(
							String.format(
									"The type \"%s\" of subgraph-container has no valid constructor.",
									className));
				} catch (final InvocationTargetException e) {
					propagate(e);
				}
			}
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(
					String.format(
							"The class \"%s\" of subgraph-container is not known in actual class path. Cannot deserialize this subgraph container.",
							className));
		}
		/*
		 * if not possible or error occurred.
		 */
		return null;
	}
}
