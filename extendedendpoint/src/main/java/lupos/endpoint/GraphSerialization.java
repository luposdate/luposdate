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

	public static JSONObject astToJson(GraphWrapperAST ast, AstFormat astFormat) {
		switch (astFormat) {
		case GRAPH:
			return graphWrapperToJsonGraph(ast);
		case NESTED:
			return graphWrapperToJsonNested(ast);
		default:
			throw new IllegalArgumentException();
		}
	}

	public static JSONObject rifAstToJson(GraphWrapperASTRIF ast,
			AstFormat astFormat) {
		switch (astFormat) {
		case GRAPH:
			return graphWrapperToJsonGraph(ast);
		case NESTED:
			return graphWrapperToJsonNested(ast);
		default:
			throw new IllegalArgumentException();
		}
	}

	public static JSONObject rulesAstToJson(GraphWrapperRules astRules,
			AstFormat astFormat) {
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
	private static JSONObject graphWrapperToJsonNested(GraphWrapper graph) {
		JSONObject json = new JSONObject();

		appendBasicNodeInformationToJson(graph, json);

		for (GraphWrapperIDTuple node : graph.getSucceedingElements()) {
			JSONObject jsonChild = graphWrapperToJsonNested(node.getOperator());
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
	public static JSONObject graphWrapperToJsonGraph(GraphWrapper ast) {
		JSONObject json = new JSONObject();

		Queue<GraphWrapper> nodeQueue = new LinkedList<>();
		Set<GraphWrapper> visited = new HashSet<>();
		GraphWrapper startNode = ast;
		nodeQueue.add(startNode);
		visited.add(startNode);
		int depth = 0;

		JSONObject startNodeJson = new JSONObject();
		appendBasicNodeInformationToJson(startNode, startNodeJson);
		startNodeJson.put("depth", depth);
		json.append("nodes", startNodeJson);

		JSONObject edgesJson = new JSONObject();

		// Breadth-first search
		while (!nodeQueue.isEmpty()) {
			GraphWrapper node = nodeQueue.remove();
			List<GraphWrapperIDTuple> successors = node.getSucceedingElements();

			if (!successors.isEmpty()) {
				depth++;
			}

			for (GraphWrapperIDTuple successorTuple : successors) {
				GraphWrapper successor = successorTuple.getOperator();
				if (!visited.contains(successor)) {
					// Not yet visited
					visited.add(successor);
					nodeQueue.add(successor);
					JSONObject nodeJson = new JSONObject();
					appendBasicNodeInformationToJson(successor, nodeJson);
					nodeJson.put("depth", depth);
					json.append("nodes", nodeJson);
				}
				JSONObject edgeJson = new JSONObject();
				edgeJson.put("operandPosition", successorTuple.getId());
				edgeJson.put("nodeId", "" + successor.hashCode());
				edgesJson.append("" + node.hashCode(), edgeJson);
			}
		}

		json.put("edges", edgesJson);
		return json;
	}

	private static JSONObject appendBasicNodeInformationToJson(
			GraphWrapper node, JSONObject json) {
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
	private static String getNodeClassification(GraphWrapperAST node) {
		Class<?> nodeClass = node.getElement().getClass();
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
