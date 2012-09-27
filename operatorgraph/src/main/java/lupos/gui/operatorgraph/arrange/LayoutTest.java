/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
import java.util.LinkedList;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;

public class LayoutTest {
	
	/**
	 * Method gets the number of edges in the graph
	 * @param graph
	 * @return 	edge-amount
	 */
	private static int getEdgeNumber(OperatorGraph graph) {
		int amount = 0;
		HashMap <GraphWrapper, GraphBox> boxes = graph.getBoxes();
		
		for (GraphWrapper gw : boxes.keySet()) {
			LinkedList<GraphWrapperIDTuple> children = gw.getSucceedingElements();
			amount += children.size();
		}
		
		return amount;
	}
	
	/**
	 * Method gets the number of nodes in the graph
	 * @param graph
	 * @return 	node-amount
	 */
	private static int getNodeNumber(OperatorGraph graph) {
		HashMap <GraphWrapper, GraphBox> boxes = graph.getBoxes();
		return boxes.size();
	}
	
	/**
	 * Method gets the shortest edge-length in a graph
	 */
	private static double getShortestEdgeLength (OperatorGraph graph) {
		HashMap<GraphWrapper, GraphBox> boxes = graph.getBoxes();
		double length = Double.MAX_VALUE;
		
		for (GraphWrapper gw : boxes.keySet()) {
			LinkedList <GraphWrapper> descendants = gw.getPrecedingElements();
			if (descendants.size() != 0) {
				for (GraphWrapper pre : descendants) {
					int x = boxes.get(gw).getX() - boxes.get(pre).getX();
					int y = boxes.get(gw).getY() - boxes.get(pre).getY();
					double distance = Math.sqrt(Math.pow(x, 2)+Math.pow(y, 2));
					
					if (distance < length) length = distance;
						
				}
			}
		}
		return length;
	}

	/**
	 * Method gets the of edge-crossings, if an edge is vertical 
	 * @param edge		the vertical edge
	 * @param edges		the rest of edges in the graph 
	 * @param op		the graph
	 * @return			crossing-amount
	 */
	private static int infinitSlope(Edge edge, LinkedList<Edge> edges, OperatorGraph op) {
		int crossings = 0;
		
		HashMap<GraphWrapper, GraphBox> boxes = op.getBoxes();
		
		GraphBox source1 = boxes.get(edge.getSource());
		GraphBox target1 = boxes.get(edge.getTarget());
		for (Edge edge2 : edges) {
			GraphBox source2 = boxes.get(edge2.getSource());
			GraphBox target2 = boxes.get(edge2.getTarget());
			if ((target2.getX() - source2.getX()) != 0) {
				double m = (target2.getY() - source2.getY())/(target2.getX() - source2.getX()); // slope of line 2
				double b = target2.getY() - (m * target2.getX()); // crossing with y-axis off line 2
				
				double y = m * source1.getX() + b;
				double minY = Math.min(source1.getY(), target1.getY());
				double maxY = Math.max(source1.getY(), target1.getY());
				
				if ((y > minY) && (y < maxY)) {
					crossings++;
				}
			}
		}
		return crossings;
	}
	
	/**
	 * Method gets the edge-crossing, of an vertical edge with another edge 
	 * @param edge1		the vertical edge
	 * @param edge2		the another edge
	 * @param op		the graph
	 * @return			the edge-crossing
	 */
	private static int infinitSlope(Edge edge1, Edge edge2, OperatorGraph op) {
		int crossings = 0;
		
		HashMap<GraphWrapper, GraphBox> boxes = op.getBoxes();
		
		GraphBox source1 = boxes.get(edge1.getSource());
		GraphBox target1 = boxes.get(edge1.getTarget());
		GraphBox source2 = boxes.get(edge2.getSource());
		GraphBox target2 = boxes.get(edge2.getTarget());
		if ((target2.getX() - source2.getX()) != 0) {
			double m = (target2.getY() - source2.getY())/(target2.getX() - source2.getX()); // slope of line 2
			double b = target2.getY() - (m * target2.getX()); // crossing with y-axis off line 2
			
			double y = m * source1.getX() + b;
			double minY = Math.min(source1.getY(), target1.getY());
			double maxY = Math.max(source1.getY(), target1.getY());
			
			if ((y > minY) && (y < maxY)) {
				crossings++;
			}
		}
		return crossings;
	}
	
	/**
	 * Method computes the amount of edge-crossing in a graph and displays the
	 * result on command-line
	 */
	public static String minEdgeCrossing_Test(OperatorGraph graph) {
		HashMap<GraphWrapper, GraphBox> boxes = graph.getBoxes();
		LinkedList <Edge> edges = new LinkedList <Edge>();
		int crossCounter = 0;
		
		// get all edges of the graph
		for (GraphWrapper gw : boxes.keySet()) {
			LinkedList<GraphWrapperIDTuple> children = gw.getSucceedingElements();
			for (GraphWrapperIDTuple child : children) {
				Edge edge = new Edge(gw, child.getOperator(), 0);
				edges.add(edge);
			}
		}
		int edgecount = 0;
		LinkedList <Edge> visited = new LinkedList <Edge>();
		for (Edge edge1 : edges) {
			edgecount ++;
			GraphBox sourceE1 = boxes.get(edge1.getSource());
			GraphBox targetE1 = boxes.get(edge1.getTarget());
			// edge 1 is vertical
			if((targetE1.getX()-sourceE1.getX())== 0.0) {
				crossCounter = infinitSlope(edge1, edges, graph); 
			}
			else {	
				double m1 = ((targetE1.getY())-(sourceE1.getY()))/(targetE1.getX()-sourceE1.getX()); // slope of line 1
				double b1 = (targetE1.getY()) - (m1 * targetE1.getX()); // slope of line 1

				for (Edge edge2 : edges) {
					if ((!edge2.equals(edge1)) && (!visited.contains(edge2)) && (m1 != 0.0)){
						GraphBox sourceE2 = boxes.get(edge2.getSource());
						GraphBox targetE2 = boxes.get(edge2.getTarget());
						// edge 2 is vertical
						if((targetE2.getX() - sourceE2.getX()) == 0.0) {
							crossCounter += infinitSlope(edge2, edge1, graph); 
							
						} else {
							double m2 = ((targetE2.getY())-(sourceE2.getY()))/(targetE2.getX()-sourceE2.getX()); // slope of line 2
							double b2 = (targetE2.getY()) - (m2 * targetE2.getX()); // crossing with y-axis of line 2 
							double x = ((b2-b1)/(m1-m2)); double y = ((m1*x) + b1); // cross-point-coordinates of the 2 lines
							
							double maxXe1 = Math.max(sourceE1.getX(), targetE1.getX()); double maxXe2 = Math.max(sourceE2.getX(), targetE2.getX());
							double minXe1 = Math.min(sourceE1.getX(), targetE1.getX()); double minXe2 = Math.min(sourceE2.getX(), targetE2.getX());
							double maxYe1 = Math.max(sourceE1.getY(), targetE1.getY()); double maxYe2 = Math.max(sourceE2.getY(), targetE2.getY());
							double minYe1 = Math.min(sourceE1.getY(), targetE1.getY()); double minYe2 = Math.min(sourceE2.getY(), targetE2.getY());
							
							
							// test if cross-point is part of edge 1
							if ((x < maxXe1) && (x > minXe1) && (y < maxYe1) && (y > minYe1)) {
								if ((x < maxXe2) && (x > minXe2) && (y < maxYe2) && (y > minYe2)) {
									crossCounter++;
								}
							}
						}
						visited.add(edge2);
					}
				}
			}
			visited.add(edge1);
		}
		
		return "Test: Crossing Edges:\nThe number of edges crossing is " + crossCounter + " of total " + edgecount+ " edges.\n";
	}
	
	/**
	 * Method gets the edge-length between the nodes a and b
	 * @param graph		the graph
	 * @param a			first node
	 * @param b			second node
	 * @return			edge length
	 */
	private static double edgeLength(OperatorGraph graph, GraphWrapper a, GraphWrapper b) {
		double length = 0.0;
		HashMap<GraphWrapper, GraphBox> boxes = graph.getBoxes();
		
		GraphBox boxA = boxes.get(a); GraphBox boxB = boxes.get(b);
		double x = boxA.getX()-boxB.getX(); double y = boxA.getY()-boxB.getY();
		length = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		
		return length;
	}
	
	/**
	 * Method computes standard variance and average length of edges in the graph and displays the
	 * result on command-line
	 * @param graph
	 */
	public static String fixedEdgeLength_Test(OperatorGraph graph) {
		double ariMiddle = 0.0;
		double var = 0.0;
		double stdAbw = 0.0;
		int edges = 0;
		LinkedList<Double> length = new LinkedList <Double>();
		
		HashMap<GraphWrapper, GraphBox> boxes = graph.getBoxes();
		
		// compute average edge-length
		for (GraphWrapper gw : boxes.keySet()){
			LinkedList<GraphWrapper> children = gw.getPrecedingElements();
			for (GraphWrapper child : children) {
				double edgeLen = edgeLength(graph, gw, child);
				length.add(edgeLen);
				ariMiddle += edgeLen;
				edges++;
			}
		}
		
		ariMiddle = ariMiddle / edges;
		
		//compute variance
		for (int i = 0; i < length.size(); i++) {
			var += Math.pow((length.get(i)-ariMiddle), 2);
		}
		
		var = var / (edges-1);
		
		// compute standard variance
		stdAbw = Math.sqrt(var);
		
		double pro = (stdAbw*100)/ariMiddle;
		
		String result = "Test: fixed length of edges:\n";
		result+="The average edge length in the graph is " + ariMiddle+".\n";
		result+="The sample standard deviation is " + stdAbw+".\n";
		result+="This is "+ pro +"%.\n";
		
		return result;
	}

	/**
	 * Method tests if graph-nodes are uniform distributed over the screen.
	 * @param op	the graph
	 */
	public static String uniformDistribution_Test(OperatorGraph op) {
		HashMap<GraphWrapper, GraphBox> boxes = op.getBoxes();
		double width = GraphHelper.graphWidth(op);
		double height = GraphHelper.graphHeight(op);
		int nodeAmount = boxes.size();
		int fieldNumber = 0;
		
		// compute number of grid-fields
		fieldNumber = (int)Math.floor(Math.sqrt(nodeAmount));
		
		int [][] grid = new int[fieldNumber][fieldNumber];
		
		// compute width and height of grid-fields
		int fieldSpaceX = (int)Math.ceil(width/fieldNumber);
		int fieldSpaceY = (int)Math.ceil(height/fieldNumber);
		int x = fieldSpaceX;
		int y = fieldSpaceY;
		
		// compute how many nodes are in the grid-fields
		for (int i = 0; i < fieldNumber; i++) {
			for (int j = 0; j < fieldNumber; j++) {
				for (GraphBox box: boxes.values()) {
					if ((box.getX() + box.width < x) && (box.getX() >= x-fieldSpaceX)
							&& (box.getY() + box.height < y) && (box.getY() >= y-fieldSpaceY)) {
						grid[i][j]++;
					}
				}
				y += fieldSpaceY;
			}
			x += fieldSpaceX;
			y = fieldSpaceY;
		}
		
		// test if nodes are uniformly distributed
		int notUniform = 0;
		boolean equ = true; 
		for (int i = 0; i < fieldNumber; i++) {
			for (int j = 0; j < fieldNumber; j++) {
				if ((grid[i][j] > 2) || (grid[i][j] < 1)) {
					equ = false; 
					notUniform++;  
				}
			}
		}
		
		String result = "Test: uniform distribution of nodes:\n";
		result+="Size of grid: " + fieldNumber + " * " + fieldNumber+"\n";
		System.out.println("");
		if (equ == false) {
			int fields = fieldNumber * fieldNumber;
			// compute percent of uniform node distribution
			double p = 100-((notUniform * 100) / fields);
			result+="The graph is about " + p + "% uniformly filled with nodes...\n";		
		} else {
			result+="All grid fields contain 1 or 2 nodes.\n"; 
			result+="The graph is 100% uniformly filled with nodes...\n";		
		}
		return result;
	}

	/**
	 * Method computes the average edge-length to compare it with the shortest
	 * edge-length and shows the difference an the command line 
	 * @param graph
	 */
	public static String closeness_Test(OperatorGraph graph) {
		
		double ariMiddle = 0.0;
		int edges = 0;
		LinkedList<Double> length = new LinkedList <Double>();
		double shortest = getShortestEdgeLength(graph);
		
		HashMap<GraphWrapper, GraphBox> boxes = graph.getBoxes();
		// compute average edge-length
		for (GraphWrapper gw : boxes.keySet()){
			LinkedList<GraphWrapper> children = gw.getPrecedingElements();
			for (GraphWrapper child : children) {
				double edgeLen = edgeLength(graph, gw, child);
				length.add(edgeLen);
				ariMiddle += edgeLen;
				edges++;
			}
		}
		
		ariMiddle = ariMiddle / edges;
		
		double diff =  ariMiddle - shortest ;
		
		// compute percent of difference between average and shortest edge-length
		double p = Math.abs(100-((ariMiddle*100)/shortest));
		
		String result = "Test: Closeness:\n";
		result+="The differenz between the average edge length and the shortest edge length is " + diff+".\n";
		result+="This is a difference of "+p+"%.\n";
		
		return result;
	}

	/**
	 * Method compares the average edge-length and the average node-distance with the
	 * optimal Sugiyama-distance.
	 * @param graph
	 */
	public static String smallestSeparation_Test(OperatorGraph graph) {
		HashMap <GraphWrapper, GraphBox> boxes = graph.getBoxes();
		int nodeAmount = boxes.size();
		double scaleConst = 1.0;
		int width = GraphHelper.graphWidth(graph);
		int height = GraphHelper.graphHeight(graph);
		LinkedList <Double> distances = new LinkedList<Double>();
		// compute optimal distance after Sugiyama
		double optiDist = scaleConst * Math.sqrt((width*height)/nodeAmount);
		
		double ariMiddle = 0.0;
		int edges = 0;
		LinkedList<Double> length = new LinkedList <Double>();
		
		for (GraphWrapper gw : boxes.keySet()){
			LinkedList<GraphWrapper> children = gw.getPrecedingElements();
			for (GraphWrapper child : children) {
				double edgeLen = edgeLength(graph, gw, child);
				length.add(edgeLen);
				ariMiddle += edgeLen;
				edges++;
			}
		}
		
		ariMiddle = ariMiddle / edges;
		
		
		double avrDist = 0.0;
		for (GraphWrapper node1 : boxes.keySet()) {
			LinkedList <GraphWrapperIDTuple> childrenTuple = node1.getSucceedingElements();
			LinkedList <GraphWrapper> children = new LinkedList <GraphWrapper>();
			for (GraphWrapperIDTuple child : childrenTuple) {
				children.add(child.getOperator());
			}
			GraphBox box1 = boxes.get(node1);
			for (GraphWrapper node2 : boxes.keySet()) {
				if ((!node2.equals(node1))) {
					GraphBox box2 = boxes.get(node2);
					// compute distance between node1 and node 2
					int x = box2.getX() - box1.getX(); int y = box2.getY() - box1.getY();
					double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
					distances.add(distance);
					avrDist += distance;
				}
			}
		}
		
		avrDist = avrDist/distances.size();
		
		String result = "Test: Smallest Separation:\n";
		result+="The optimal distance based on Sugiyama is " + optiDist+".\n";
		result+="The average distance between not connected nodes is "+avrDist+".\n";
		result+="The average edge length is "+ariMiddle+".\n";
		
		return result;
	}
	
	/**
	 * Method tests if a graph-layout is symmetric to an axis and
	 * shows the result on command-line
	 * @param graph
	 */
	public static String symmetry_Test(OperatorGraph graph) {
		int width = GraphHelper.graphWidth(graph);
		double symLine = width/2;
		
		HashMap <GraphWrapper, GraphBox> boxes = graph.getBoxes();
		LinkedList<GraphBox> left = new LinkedList<GraphBox>();
		LinkedList<GraphBox> right = new LinkedList<GraphBox>();
		
		String result = "Test: Axially Symmetry:\n";
		// get boxes of left and right side of symmetry-line
		for(GraphBox box : boxes.values()) {
			if ((box.getX()+box.width) <= symLine) {
				left.add(box);
			} else if(box.getX() >= symLine) {
				right.add(box);
			} else { // test if node is centered on symmetry-line
				if ((box.getX() + (box.width/2)) != symLine) {
					result+="Node not in the middle of the symmetry axe!\n";
					result+="Node with x = " + box.getX() + "    y = " + box.getY()+".\n";
				}
			}
		}
		
		// test if node-amount on left and right side of symmetry-line is equal
		if (left.size() != right.size()) {
			result+="Diffenrent number of nodes on both sides of the symmetry axe:\n";
			result+="Left: " + left.size() + "    Right: " + right.size()+"\n";
		}
		
		// compute symmetric node-counter-parts
		int symCounter = 0;
		boolean sym = true;
		for (GraphBox box1 : left) {
			double symSpace = symLine - (box1.getX() + box1.width);
			int y = box1.getY();
			for (GraphBox box2 : right) {
				if (y == box2.getY()) {
					if ((symLine + symSpace) == box2.getX()) {
						sym = true; 
						symCounter++;
						break;
					}
					else{
						sym = false;
					}
				}
				sym = false;
			}			
		}
		
		// compute number of symmetric nodes
		final int sumSizes = left.size() + right.size();
		double p = (sumSizes==0)? 100 : (2*symCounter*100) / sumSizes;
		
		if (sym == false) {
			result+="The graph is "+p+"% axially symmetric!\n";
		} else {
			result+="The graph is 100% axially symmetric!\n";
		}
		return result;
	}
	
	public static String test(final OperatorGraph operatorgraph){
		String result = "Number of nodes: "+getNodeNumber(operatorgraph)+"\n"
						+ "Anzahl Kanten: "+getEdgeNumber(operatorgraph)+"\n\n";
		
		result+=uniformDistribution_Test(operatorgraph)+"\n";
		result+=fixedEdgeLength_Test (operatorgraph)+"\n";
		result+=closeness_Test (operatorgraph)+"\n";
		result+=smallestSeparation_Test (operatorgraph)+"\n";
		result+=symmetry_Test(operatorgraph)+"\n";
		// result+=minEdgeCrossing_Test(operatorgraph)+"\n";
		
		return result;
	}
}
