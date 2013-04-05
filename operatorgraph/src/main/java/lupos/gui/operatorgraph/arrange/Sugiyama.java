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
package lupos.gui.operatorgraph.arrange;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;

/**
 * The class Sugiyama implements the Sudiyama - Algorithm
 * which is used in the LUPOSDATE- Project 
 * 
 * @author Tobias Bielfeld
 *
 */
public final class Sugiyama {
	
	/**
	 * Method gets a HashMap with the levels of the layout as keys
	 * and lists of GraphWrapper which belong to the level
	 * 
	 * @param operatorgraph		all nodes of the graph
	 * @return	HashMap with layout-levels and their nodes
	 */
	protected static LinkedHashMap<Integer, LinkedList<GraphWrapper>> fillAllLevels(
			final OperatorGraph operatorgraph) {
		
		final HashMap<GraphWrapper, Integer> levels = new HashMap<GraphWrapper, Integer>();
		// compute for every layout-level the nodes
		// start with roots
		HashSet<GraphWrapper> visited = new HashSet<GraphWrapper>();
		for (final GraphWrapper gw : operatorgraph.getRootList(false)) {
			computeLevel(visited,
					new HashSet<GraphWrapper>(), gw, 0, operatorgraph, levels);
		}

		
		// get a HashMap for every Level in the list 
		final LinkedHashMap<Integer, LinkedList<GraphWrapper>> graphWrapperOfLevel = new LinkedHashMap<Integer, LinkedList<GraphWrapper>>();
		final HashMap<GraphWrapper, GraphBox> boxes = operatorgraph.getBoxes();

		// iterate over all GraphWrapper, which have a GraphBox
		for (final GraphWrapper gw : boxes.keySet()) {
			// get level of current node
			final int currentLevel = levels.get(gw);
			
			// get a list of GraphWrapper-objects of current level
			LinkedList<GraphWrapper> lgw = graphWrapperOfLevel
					.get(currentLevel);

			if (lgw == null) { // if list is empty
				lgw = new LinkedList<GraphWrapper>(); // initiate a new one
			}

			lgw.add(gw); // add current GraphWrapper to list

			// add GraphWrapper list to current level in HashMap
			graphWrapperOfLevel.put(currentLevel, lgw);
		}
		
		return graphWrapperOfLevel;
	}
	
	/**
	 * Method computes recursively the level 
	 * of a node and his children by depth-search 
	 **/
	protected static void computeLevel(
			final HashSet<GraphWrapper> visited,
			final HashSet<GraphWrapper> visitedNotCloned,
			final GraphWrapper op, final int level,
			final OperatorGraph operatorgraph,
			final HashMap<GraphWrapper, Integer> levels) {

		// if a node is visited
		if (visited.contains(op)) { 
			return; // abort
		}

		visited.add(op); // set the current node visited
		
		levels.put(op, level);

		// visit all children and compute their level recursive

		for (final GraphWrapperIDTuple child : op.getSucceedingElements()) {
			computeLevel(visited,
					visitedNotCloned, child.getOperator(), level+1,
					operatorgraph, levels);
		}
	}

	/**
	 * Method uses the barycenter-method to compute new positions for nodes in the graph
	 * and minimizes edge-crossings. 
	 * @param levels	HashMap with layout levels and their nodes
	 * @param level		current level
	 */
	protected static int baryCenter (LinkedHashMap <Integer, LinkedList<GraphWrapper>>levels,
									 int level) {
		int moves = 0;
		LinkedList<NodeWrapper> positions = new LinkedList<NodeWrapper>();
		LinkedList<GraphWrapper> newPlaced = new LinkedList<GraphWrapper>();
		LinkedList<GraphWrapper> currentLevel = levels.get(level);
		
		if (level == 0) {
			newPlaced = currentLevel;
		}
		else{
		
			// iterate over all nodes in the level
			for (int currentIndex = 0; currentIndex < currentLevel.size(); currentIndex++) {
				GraphWrapper currentNode = currentLevel.get(currentIndex); 
				int gridPositionsSum = 0; // Sum of positions of ancestors of current node
				int nodeAmount = 0;		// Amount of ancestors of current node			
				// List of all ancestors of the current node
				LinkedList <GraphWrapper> ancestors = currentNode.getPrecedingElements();
						
				// iterate over all ancestors
				for (int i = 0; i < ancestors.size();i++) {
					GraphWrapper ancestor = ancestors.get(i);

					gridPositionsSum += getGridPosition(levels, ancestor, level-1);

					// if current level is root-level
					if (gridPositionsSum < 0) return moves;
					nodeAmount++;
				}
			
				if (nodeAmount > 0) {
				// compute new position level
					double newPosition = gridPositionsSum/nodeAmount;
					NodeWrapper node = new NodeWrapper(currentNode, newPosition);
					positions.add(node);
				}
			}
		
		
			while (positions.size() != 0) {
				NodeWrapper smallestPos = positions.getFirst();
				for (int i = 1; i < positions.size(); i++) {
					NodeWrapper temp = positions.get(i);
					if (smallestPos.getGridPosition() > temp.getGridPosition())
						smallestPos = temp;
				}
				newPlaced.add(smallestPos.get_Node());
				positions.remove(smallestPos);
			}
		}
		levels.put(level, newPlaced);		
		
		return moves;
	}
	
	/**
	 * Method gets the position of a node in his level
	 * @param levels	HashMap with layout levels and their nodes
	 * @param a			node to get position
	 * @param level		the level to the node belongs
	 * @return			position of the node or -1, if node isn't in the level
	 */
	protected static int getGridPosition (LinkedHashMap <Integer, LinkedList<GraphWrapper>>levels,
									  GraphWrapper a, int level) {
		LinkedList<GraphWrapper> levelKnoten = levels.get(level);
		
		if (levelKnoten == null )
			return -1;
		
		int pos = levelKnoten.indexOf(a);
		return pos;
	}
	
	
	public static void arrange(final OperatorGraph operatorgraph){

		//long time1 = System.currentTimeMillis();
		
		
		LinkedList<GraphRow> rows = new LinkedList<GraphRow>();
		HashMap<GraphWrapper, GraphBox> boxes = operatorgraph.getBoxes();
		
		LinkedHashMap<Integer, LinkedList<GraphWrapper>> levels = fillAllLevels(operatorgraph);
		
		int moves = -1;

		while (moves != 0){
			moves = 0;
			for (int level = 0; level < levels.size(); level++) {
				moves += baryCenter(levels, level);
			}				
		}

		int level = 0;
		int y = (int)Math.ceil(operatorgraph.PADDING);
		while (levels.get(level) != null) {
			GraphRow row = new GraphRow(operatorgraph, y, levels.get(level).size());
			rows.add(row);

			row.addAllWithoutUpdatingParentsSize(levels.get(level), boxes);
			y += row.getHeight()+operatorgraph.SPACING_Y;
			level++;
		}
		
		int largestWidth = 0;
		for (GraphRow row : rows){
			if (row.getWidth() > largestWidth)
				largestWidth = row.getWidth();
		}
		for (GraphRow row : rows) {
			row.center(largestWidth/2);
		}		
		//time1 = System.currentTimeMillis() - time1;
		//System.out.println("Laufzeit = " + time1 + " ms");
	}		
}


