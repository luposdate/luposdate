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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.bindings.BindingsArrayPresortingNumbers;
import lupos.datastructures.bindings.BindingsArrayVarMinMax;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.operator.format.SubgraphContainerFormatter;
import lupos.distributed.operator.format.operatorcreator.IOperatorCreator;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.application.CollectResult;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.engine.operators.singleinput.sort.fastsort.FastSort;
import lupos.misc.Tuple;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class provides a method to evaluate a subgraph given as serialized JSON string
 */
public class SubgraphLocalExecutor {

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
	@SuppressWarnings("deprecation")
	public static Tuple<QueryResult, Set<Variable>> evaluateSubgraph(final String subgraphSerializedAsJSONString, final Dataset dataset, final IOperatorCreator operatorCreator) throws JSONException {
		final CollectResult collectResult = new CollectResult(true);
		final SubgraphContainerFormatter formatter = new SubgraphContainerFormatter(dataset, operatorCreator, collectResult);
		final Root root = formatter.deserialize(new JSONObject(subgraphSerializedAsJSONString));

		// some initializations
		root.deleteParents();
		root.setParents();
		root.detectCycles();
		root.sendMessage(new BoundVariablesMessage());

		BindingsArray.forceVariables(CommonCoreQueryEvaluator.getAllVariablesOfQuery(root));

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

		// evaluate subgraph!
		root.sendMessage(new StartOfEvaluationMessage());
		root.startProcessing();
		root.sendMessage(new EndOfEvaluationMessage());

		if (instanceClass != null) {
			Bindings.instanceClass = instanceClass;
		}
		return new Tuple<QueryResult, Set<Variable>>(collectResult.getResult(), new HashSet<Variable>(SubgraphLocalExecutor.getVariables(root)));
	}

	/**
	 * Determine the variables of the result operator by just going down the operator graph.
	 * Take care when extending the functionality: Cycles are not considered and will lead to infinite loop!
	 * @param root the root node
	 * @return the variables of the result operator!
	 */
	public static Collection<Variable> getVariables(final BasicOperator root){
		if(root.getSucceedingOperators().size()==0) {
			return root.getUnionVariables();
		} else {
			return SubgraphLocalExecutor.getVariables(root.getSucceedingOperators().get(0).getOperator());
		}
	}
}