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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Variable;
import lupos.distributed.operator.format.operatorcreator.IOperatorCreator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.multiinput.join.Join;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Formatter for the operation "join" to be used in serialized
 * SubgraphContainer.
 *
 * @author Bjoern
 * @version $Id: $Id
 */
public class JoinFormatter implements OperatorFormatter {

	/**
	 * Instantiates a new join formatter.
	 *
	 * @param operatorCreator
	 *            the operator creator for creating the join operator
	 */
	public JoinFormatter(final IOperatorCreator operatorCreator) {
	}

	/**
	 * Instantiates a new join formatter.
	 */
	public JoinFormatter() {
	}

	/** {@inheritDoc} */
	@Override
	public JSONObject serialize(final BasicOperator operator, final int node_id) {
		final Join join = (Join) operator;
		try {
			/*
			 * store intersection variables
			 */
			final JSONObject json = Helper.createVariablesJSONObject(join
					.getIntersectionVariables());
			json.put("type", join.getClass().getName());
			json.put("node_id", node_id);
			/*
			 * store the union variables
			 */
			Collection<Variable> variables = join.getUnionVariables();
			final Collection<JSONObject> varsJSON = new LinkedList<JSONObject>();
			for (final Variable var : variables) {
				varsJSON.add(Helper.createVarAsJSONObject(var));
			}
			json.put("union", varsJSON);
			
			return json;
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return new JSONObject();
	}

	
	/** {@inheritDoc} */
	@Override
	public BasicOperator deserialize(final JSONObject serializedOperator)
			throws JSONException {
		/*
		 * get the intersection variables
		 */
		Collection<Variable> cc = Helper
				.createVariablesFromJSON(serializedOperator);

		final List<Variable> unionVariables = new LinkedList<Variable>();
		/*
		 * get the union variables
		 */
		final JSONArray triplePatternsJsonArray = (JSONArray) serializedOperator
				.get("union");
		for (int i = 0; i < triplePatternsJsonArray.length(); i++) {
			unionVariables.add(Helper
					.createVariableFromJSON(triplePatternsJsonArray
							.getJSONObject(i)));
		}
		/*
		 * create the join
		 */
		Join result =  new Join();
		result.setIntersectionVariables(cc);
		result.setUnionVariables(unionVariables);
		return result;
	}

	/**
	 * Sets the index collection.
	 *
	 * @param root
	 *            the new index collection
	 */
	public void setRoot(final Root root) {
	}

}
