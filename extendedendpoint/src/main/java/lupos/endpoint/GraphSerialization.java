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
package lupos.endpoint;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperAST;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperASTRIF;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperRules;

import org.json.JSONObject;

public class GraphSerialization {
	public enum AstFormat {
		GRAPH, NESTED
	}

	public static JSONObject astToJson(final GraphWrapperAST ast, final AstFormat astFormat) {
		switch (astFormat) {
		case GRAPH:
			return graphWrapperToJsonGraph(ast);
		case NESTED:
			return graphWrapperToJsonNested(ast);
		default:
			throw new IllegalArgumentException();
		}
	}

	public static JSONObject rifAstToJson(final GraphWrapperASTRIF ast,
			final AstFormat astFormat) {
		switch (astFormat) {
		case GRAPH:
			return graphWrapperToJsonGraph(ast);
		case NESTED:
			return graphWrapperToJsonNested(ast);
		default:
			throw new IllegalArgumentException();
		}
	}

	public static JSONObject rulesAstToJson(final GraphWrapperRules astRules,
			final AstFormat astFormat) {
		switch (astFormat) {
		case GRAPH:
			return graphWrapperToJsonGraph(astRules);
		case NESTED:
			return graphWrapperToJsonNested(astRules);
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Produces a nested JSON serialization for a generic graph. The graph must be acyclic.
	 *
	 * @param graph
	 *            generic (acyclic) graph wrapper
	 * @return JSON serialization
	 */
	private static JSONObject graphWrapperToJsonNested(final GraphWrapper graph) {
		final JSONObject json = new JSONObject();

		appendBasicNodeInformationToJson(graph, json);

		for (final GraphWrapperIDTuple node : graph.getSucceedingElements()) {
			final JSONObject jsonChild = graphWrapperToJsonNested(node.getOperator());
			jsonChild.put("operandPosition", node.getId());
			json.append("children", jsonChild);
		}

		return json;
	}

	/**
	 * Produces a JSON serialization for a generic graph. It's basically a tuple
	 * (nodes, adjacency list). It discovers the graph by breadth-first search.
	 *
	 * @param ast
	 *            generic graph wrapper
	 * @return JSON serialization
	 */
	public static JSONObject graphWrapperToJsonGraph(final GraphWrapper ast) {
		final JSONObject json = new JSONObject();

		final Queue<GraphWrapper> nodeQueue = new LinkedList<>();
		final Set<GraphWrapper> visited = new HashSet<>();
		final GraphWrapper startNode = ast;
		nodeQueue.add(startNode);
		visited.add(startNode);
		int depth = 0;

		final JSONObject startNodeJson = new JSONObject();
		appendBasicNodeInformationToJson(startNode, startNodeJson);
		startNodeJson.put("depth", depth);
		json.append("nodes", startNodeJson);

		final JSONObject edgesJson = new JSONObject();

		// Breadth-first search
		while (!nodeQueue.isEmpty()) {
			final GraphWrapper node = nodeQueue.remove();
			final List<GraphWrapperIDTuple> successors = node.getSucceedingElements();

			if (!successors.isEmpty()) {
				depth++;
			}

			for (final GraphWrapperIDTuple successorTuple : successors) {
				final GraphWrapper successor = successorTuple.getOperator();
				if (!visited.contains(successor)) {
					// Not yet visited
					visited.add(successor);
					nodeQueue.add(successor);
					final JSONObject nodeJson = new JSONObject();
					appendBasicNodeInformationToJson(successor, nodeJson);
					nodeJson.put("depth", depth);
					json.append("nodes", nodeJson);
				}
				final JSONObject edgeJson = new JSONObject();
				edgeJson.put("operandPosition", successorTuple.getId());
				edgeJson.put("nodeId", "" + successor.hashCode());
				edgesJson.append("" + node.hashCode(), edgeJson);
			}
		}

		json.put("edges", edgesJson);
		return json;
	}

	private static JSONObject appendBasicNodeInformationToJson(
			final GraphWrapper node, final JSONObject json) {
		json.put("type", node.getElement().getClass().getSimpleName());
		json.put("description", node.toString());
		json.put("id", node.hashCode());
		if (node instanceof GraphWrapperAST) {
			json.put("classification",
					getNodeClassification((GraphWrapperAST) node));
		}
		return json;
	}

	/**
	 * Returns the classification of a node as a String. Recognized
	 * classifications are QueryHead, OperatorNode, FunctionNode,
	 * HighLevelOperator, TerminalNode and NonTerminalNode.
	 *
	 * @param node
	 *            the node
	 * @return classification of a node as a String
	 */
	private static String getNodeClassification(final GraphWrapperAST node) {
		final Class<?> nodeClass = node.getElement().getClass();
		if (GraphWrapperAST.isQueryHead(nodeClass)) {
			return "QueryHead";
		} else if (GraphWrapperAST.isOperatorNode(nodeClass)) {
			return "OperatorNode";
		} else if (GraphWrapperAST.isFunctionNode(nodeClass)) {
			return "FunctionNode";
		} else if (GraphWrapperAST.isHighLevelOperator(nodeClass)) {
			return "HighLevelOperator";
		} else if (GraphWrapperAST.isTerminalNode(nodeClass)) {
			return "TerminalNode";
		} else if (GraphWrapperAST.isNonTerminalNode(nodeClass)) {
			return "NonTerminalNode";
		}
		return "UnknownNode";
	}
}
