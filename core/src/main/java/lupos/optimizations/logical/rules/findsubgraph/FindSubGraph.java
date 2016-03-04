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
package lupos.optimizations.logical.rules.findsubgraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.optional.Optional;
import lupos.engine.operators.singleinput.Projection;
public class FindSubGraph {

	private FindSubGraph() {
	}

	/**
	 * This method checks if a given graph S is a subgraph of another graph T
	 * starting at given nodes. Only the operatortype is checked and the
	 * succeedingOperator/PrecedingOperator relationships between the nodes are
	 * checked, no other constraints restricting e.g. the attributes of
	 * operators. In order to deal with special cases: If the operand id in S is
	 * -1 then any operand id in T can occur. If the operator in S is of type
	 * Operator, then any operator can be in T in the corresponding node.
	 **
	 ** The method is not perfect as it returns only one solution in ambiguous
	 * cases (and may not find any solution in ambiguous cases). However, most
	 * probably, this method will work for our rules (to be checked!).
	 **
	 ** @param graph
	 *            The start node of the graph which is checked for containment
	 *            of the other subgraph.
	 ** @param startNode
	 *            the start node of the subgraph to be checked.
	 ** @param subGraphMap
	 *            for associating specific nodes in the subgraph with certain
	 *            names, which are used in the output. Every node in the
	 *            subgraph must have a name, otherwise the method is not
	 *            correct!
	 ** @return If graph does not contain startNode, then null is returned,
	 *         otherwise a map is returned, which associates the names to the
	 *         nodes in the whole graph.
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public static Map<String, BasicOperator> checkSubGraph(
			final BasicOperator graph,
			final Map<BasicOperator, String> subGraphMap,
			final BasicOperator startNode) {
		return checkSubGraph(graph, subGraphMap, startNode,
				new HashMap<String, BasicOperator>());
	}

	private static Map<String, BasicOperator> checkSubGraph(
			final BasicOperator graph,
			final Map<BasicOperator, String> subGraphMap,
			final BasicOperator startNode,
			final Map<String, BasicOperator> currentResult) {

		if (correspondingNodes(graph, startNode)) {

			// System.out.println("####"+startNode+" # "+subGraphMap);
			final String nodeName = subGraphMap.get(startNode);
			if (nodeName == null) {
				System.out
						.println("Every node in the subgraph to check should have a name...");
				return currentResult;
			}
			// is the node already visited?
			if (currentResult.containsKey(nodeName)) {
				if (graph.equals(currentResult.get(nodeName)))
					return currentResult;
				else
					return null;
			}

			// put node into (intermediate) result
			Map<String, BasicOperator> currentResult2 = new HashMap<String, BasicOperator>();
			currentResult2.putAll(currentResult);
			if (subGraphMap.containsKey(startNode))
				currentResult2.put(subGraphMap.get(startNode), graph);
			else {
				System.out
						.println("Every node in the subgraph to check should have a name...");
			}
			// visit all succeeding nodes
			for (final OperatorIDTuple operatorIDTuple : startNode
					.getSucceedingOperators()) {
				boolean found = false;
				for (final OperatorIDTuple operatorIDTuple2 : graph
						.getSucceedingOperators())
					// to do? maybe consider more cases in ambiguous cases
					if (operatorIDTuple.getId() == -1
							|| operatorIDTuple.getId() == operatorIDTuple2
									.getId()) {
						final Map<String, BasicOperator> currentResult3 = checkSubGraph(
								operatorIDTuple2.getOperator(), subGraphMap,
								operatorIDTuple.getOperator(), currentResult2);
						if (currentResult3 != null) {
							currentResult2 = currentResult3;
							found = true;
						}
					}
				if (!found)
					return null;
			}
			// visit all preceding nodes
			for (final BasicOperator operator : startNode
					.getPrecedingOperators()) {
				boolean found = false;
				for (final BasicOperator operator2 : graph
						.getPrecedingOperators()) {

					OperatorIDTuple operatorIDTuple = null;
					for (final OperatorIDTuple zOperatorIDTuple : operator
							.getSucceedingOperators()) {
						if (zOperatorIDTuple.getOperator().equals(startNode)) {
							operatorIDTuple = zOperatorIDTuple;
							break;
						}
					}
					if (operatorIDTuple == null
							|| !operatorIDTuple.getOperator().equals(startNode)) {
						System.out.println("Error in Operatorgraph:"
								+ operatorIDTuple);
						return null;
					}

					OperatorIDTuple operatorIDTuple2 = null;
					for (final OperatorIDTuple zOperatorIDTuple2 : operator2
							.getSucceedingOperators()) {
						if (zOperatorIDTuple2.getOperator().equals(graph)) {
							operatorIDTuple2 = zOperatorIDTuple2;
							break;
						}
					}
					if (operatorIDTuple2 == null
							|| !operatorIDTuple2.getOperator().equals(graph)) {
						System.out.println("Error in Operatorgraph");
						return null;
					}

					// to do? maybe consider more cases in ambiguous cases
					if (operatorIDTuple.getId() == -1
							|| operatorIDTuple.getId() == operatorIDTuple2
									.getId()) {
						final Map<String, BasicOperator> currentResult3 = checkSubGraph(
								operator2, subGraphMap, operator,
								currentResult2);
						if (currentResult3 != null) {
							currentResult2 = currentResult3;
							found = true;
						}
					}
				}
				if (!found)
					return null;
			}
			return currentResult2;
		} else
			return null;
	}

	/**
	 * <p>checkSubGraphGetAll.</p>
	 *
	 * @param graph a {@link lupos.engine.operators.BasicOperator} object.
	 * @param subGraphMap a {@link java.util.Map} object.
	 * @param startNode a {@link lupos.engine.operators.BasicOperator} object.
	 * @return a {@link java.util.List} object.
	 */
	public static List<Map<String, BasicOperator>> checkSubGraphGetAll(
			final BasicOperator graph,
			final Map<BasicOperator, String> subGraphMap,
			final BasicOperator startNode) {
		final List<Map<String, BasicOperator>> result = new LinkedList<Map<String, BasicOperator>>();
		checkSubGraphGetAll(graph, subGraphMap, startNode,
				new HashMap<String, BasicOperator>(), result);
		return result;
	}

	private static void checkSubGraphGetAll(final BasicOperator graph,
			final Map<BasicOperator, String> subGraphMap,
			final BasicOperator startNode,
			final Map<String, BasicOperator> currentResult,
			final List<Map<String, BasicOperator>> result) {

		if (correspondingNodes(graph, startNode)) {

			// System.out.println("####"+startNode+" # "+subGraphMap);
			final String nodeName = subGraphMap.get(startNode);
			if (nodeName == null) {
				System.out
						.println("Every node in the subgraph to check should have a name...");
				result.add(currentResult);
				return;
			}
			// is the node already visited?
			if (currentResult.containsKey(nodeName)) {
				if (graph.equals(currentResult.get(nodeName)))
					result.add(currentResult);
				return;
			}

			// put node into (intermediate) result
			Map<String, BasicOperator> currentResult2 = new HashMap<String, BasicOperator>();
			currentResult2.putAll(currentResult);
			if (subGraphMap.containsKey(startNode))
				currentResult2.put(subGraphMap.get(startNode), graph);
			else {
				System.out
						.println("Every node in the subgraph to check should have a name...");
			}
			// visit all succeeding nodes
			for (final OperatorIDTuple operatorIDTuple : startNode
					.getSucceedingOperators()) {
				boolean found = false;
				for (final OperatorIDTuple operatorIDTuple2 : graph
						.getSucceedingOperators())
					// to do? maybe consider more cases in ambiguous cases
					if (operatorIDTuple.getId() == -1
							|| operatorIDTuple.getId() == operatorIDTuple2
									.getId()) {
						final Map<String, BasicOperator> currentResult3 = checkSubGraph(
								operatorIDTuple2.getOperator(), subGraphMap,
								operatorIDTuple.getOperator(), currentResult2);
						if (currentResult3 != null) {
							currentResult2 = currentResult3;
							found = true;
						}
					}
				if (!found)
					return;
			}
			// visit all preceding nodes
			for (final BasicOperator operator : startNode
					.getPrecedingOperators()) {
				boolean found = false;
				for (final BasicOperator operator2 : graph
						.getPrecedingOperators()) {

					OperatorIDTuple operatorIDTuple = null;
					for (final OperatorIDTuple zOperatorIDTuple : operator
							.getSucceedingOperators()) {
						if (zOperatorIDTuple.getOperator().equals(startNode)) {
							operatorIDTuple = zOperatorIDTuple;
							break;
						}
					}
					if (operatorIDTuple == null
							|| !operatorIDTuple.getOperator().equals(startNode)) {
						System.out.println("Error in Operatorgraph:"
								+ operatorIDTuple);
						return;
					}

					OperatorIDTuple operatorIDTuple2 = null;
					for (final OperatorIDTuple zOperatorIDTuple2 : operator2
							.getSucceedingOperators()) {
						if (zOperatorIDTuple2.getOperator().equals(graph)) {
							operatorIDTuple2 = zOperatorIDTuple2;
							break;
						}
					}
					if (operatorIDTuple2 == null
							|| !operatorIDTuple2.getOperator().equals(graph)) {
						System.out.println("Error in Operatorgraph");
						return;
					}

					// to do? maybe consider more cases in ambiguous cases
					if (operatorIDTuple.getId() == -1
							|| operatorIDTuple.getId() == operatorIDTuple2
									.getId()) {
						final Map<String, BasicOperator> currentResult3 = checkSubGraph(
								operator2, subGraphMap, operator,
								currentResult2);
						if (currentResult3 != null) {
							currentResult2 = currentResult3;
							result.add(currentResult2);
							found = true;
						}
					}
				}
				if (!found)
					return;
			}
		}
	}

	private static boolean correspondingNodes(final BasicOperator graph,
			final BasicOperator startNode) {
		if (graph == null || startNode == null)
			return false;
		// System.out.println(startNode.getClass().getName()+" <-> "+graph.
		// getClass().getName());
		if (startNode.getClass().getName().compareTo(
				"lupos.engine.operators.Operator") == 0)
			return true;
		if (startNode.getClass().getName().compareTo(
				"lupos.engine.operators.BasicOperator") == 0)
			return true;
		if (startNode.getClass().getName().compareTo(
				"lupos.engine.operators.multiinput.join.Join") == 0
				&& graph instanceof Join &&
				// exclude special case:
				graph.getClass().getPackage().getName().compareTo(
						"lupos.engine.operators.multiinput.optional.parallel") != 0)
			return true;
		if (startNode.getClass().getName().compareTo(
				"lupos.engine.operators.multiinput.optional.Optional") == 0
				&& (graph instanceof Optional ||
				// include special case:
				graph.getClass().getPackage().getName().compareTo(
						"lupos.engine.operators.multiinput.optional.parallel") == 0))
			return true;
		if (startNode.getClass().getName()
				.compareTo(graph.getClass().getName()) == 0)
			return true;
		return false;
	}

	/**
	 * <p>findSubGraphs.</p>
	 *
	 * @param root a {@link lupos.engine.operators.BasicOperator} object.
	 * @param startNode a {@link lupos.engine.operators.BasicOperator} object.
	 * @param subGraphMap a {@link java.util.Map} object.
	 * @return a {@link java.util.Collection} object.
	 */
	public static Collection<Map<String, BasicOperator>> findSubGraphs(
			final BasicOperator root, final BasicOperator startNode,
			final Map<BasicOperator, String> subGraphMap) {
		final FindSubGraphMapsHelper sogv = new FindSubGraphMapsHelper(
				startNode, subGraphMap);
		root.visit(sogv);
		return sogv.getFoundSubGraphs();
	}

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		// The whole graph:
		final Operator wa = new Join();
		final Operator wb = new Projection();
		wa.setSucceedingOperator(new OperatorIDTuple(wb, 0));
		wb.setPrecedingOperator(wa);

		// Define left side of rule
		final Operator a = new Join();
		final Operator b = new Projection();
		a.setSucceedingOperator(new OperatorIDTuple(b, -1));
		b.setPrecedingOperator(a);
		final Map<BasicOperator, String> subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(a, "a");
		subGraphMap.put(b, "b");

		final Map<String, BasicOperator> result = checkSubGraph(wa,
				subGraphMap, a);
		System.out.println(result);
	}
}
