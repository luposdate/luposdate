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
package lupos.gui.operatorgraph.arrange;

import java.util.HashMap;
import java.util.LinkedList;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;

/**
 * The class SpringEmbedder implements the Spring Embedder - Algorithm
 * which is used in the LUPOSDATE- Project
 *
 * @author Tobias Bielfeld
 * @version $Id: $Id
 */
public class SpringEmbedder {
	
	private static final double springConst = 500.0;
	private static final double repellingConst = 100.0;
	// natural length of the edge between a and b
	private static double springLength = 200;
	private static final double DAMPER = 0.1;
	private static final double ACCEPTED = 0.001;
	
	private static long TIMEINTERVAL = 2500; // maximum time interval after which layouting is aborted 

	private SpringEmbedder() {
	}

	/**
	 * Method computes the repulsing force between two
	 * nodes a and b. The result will be stored in force.
	 */
	private static double computeRepulsionForce (GraphWrapper a, GraphWrapper b, OperatorGraph op, Force repulsion) {
		HashMap<GraphWrapper, GraphBox> boxes = op.getBoxes();
		GraphBox boxA = boxes.get(a); GraphBox boxB = boxes.get(b);
		int x = boxB.getX() - boxA.getX(); int y = boxB.getY() - boxA.getY();
		
		// compute distance between a and b
		double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

		// compute unit vector with direction from b to a 
		Force e = new Force(x/distance, y/distance);
		
		// compute repulsion force with law of Coulomb
		double factor = -((SpringEmbedder.repellingConst*op.getZoomFactor()) / Math.pow(distance, 2)); 
		
		repulsion.setX_Force(factor * e.getX_Force());
		repulsion.setY_Force(factor * e.getY_Force());
		return factor;
	}
	
	/**
	 * Method computes the force of attraction between 
	 * nodes which are connected by an edge. The result 
	 * will be stored in force.
	 */
	private static double computeAttractionForce(GraphWrapper a, GraphWrapper b, OperatorGraph op, Force attraction){
		HashMap<GraphWrapper, GraphBox> boxes = op.getBoxes();
		GraphBox boxA = boxes.get(a); GraphBox boxB = boxes.get(b);
		
		int x = boxA.getX() - boxB.getX(); int y = boxA.getY() - boxB.getY();
		
		// compute distance between a and b
		double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		
		// compute unit vector with direction from a to b 
		Force e = new Force(x/distance, y/distance);
		
		// compute attraction force with law of Hooke
		double factor =	 -(SpringEmbedder.springConst*op.getZoomFactor())*Math.log(distance/(SpringEmbedder.springLength*op.getZoomFactor()));

		attraction.setX_Force(factor * e.getX_Force());
		attraction.setY_Force(factor * e.getY_Force());

		return factor;
	}	
	
	/**
	 * <p>arrange.</p>
	 *
	 * @param operatorgraph a {@link lupos.gui.operatorgraph.OperatorGraph} object.
	 */
	public static void arrange(final OperatorGraph operatorgraph) {
				
		HashMap<GraphWrapper, GraphBox> nodes = operatorgraph.getBoxes();
		HashMap<GraphWrapper, Force> repforces = new HashMap<GraphWrapper, Force>();
		HashMap<GraphWrapper, Force> attforces = new HashMap<GraphWrapper, Force>();
		
		long time1 = System.currentTimeMillis();
		
		while (true) {

			double maxForce = 0.1;
			for (GraphWrapper u : nodes.keySet()) {
				Force repulsion = new Force (0.0,0.0);
				LinkedList <GraphWrapperIDTuple> successors = u.getSucceedingElements();
				LinkedList<GraphWrapper> sucElements = new LinkedList<GraphWrapper>();
				LinkedList<GraphWrapper> preElements = u.getPrecedingElements();
				for (GraphWrapperIDTuple tup : successors) {
					sucElements.add(tup.getOperator());
				}
				
				// compute repulsion force 
				for (GraphWrapper v : nodes.keySet()) {
					if((!u.equals(v)) && (!sucElements.contains(v)) && (!preElements.contains(v))) {
						Force tmp = new Force(0.0, 0.0);
						computeRepulsionForce(u, v, operatorgraph, tmp);
						repulsion.setX_Force(repulsion.getX_Force() + tmp.getX_Force());
						repulsion.setY_Force(repulsion.getY_Force() + tmp.getY_Force());
					}
				}
				
				// get all nodes, which are connected with u
				LinkedList<GraphWrapperIDTuple> successor = u.getSucceedingElements();
				LinkedList<GraphWrapper> ancestors = u.getPrecedingElements();
				
				// compute attraction force
				Force attraction = new Force(0.0, 0.0);
				for (GraphWrapperIDTuple suc : successor) {
					Force tmp = new Force (0.0,0.0);
					computeAttractionForce(u, suc.getOperator(), operatorgraph, tmp);							
					
					attraction.setX_Force(attraction.getX_Force() + tmp.getX_Force());
					attraction.setY_Force(attraction.getY_Force() + tmp.getY_Force());
				}
				
				for (GraphWrapper anc : ancestors) {
					Force tmp = new Force (0.0, 0.0);
					computeAttractionForce(u, anc, operatorgraph, tmp);
					attraction.setX_Force(attraction.getX_Force() + tmp.getX_Force());
					attraction.setY_Force(attraction.getY_Force() + tmp.getY_Force());
				}
				
				double xForce = repulsion.getX_Force();
				double yForce = repulsion.getY_Force();
				Force u_repForce = new Force (xForce, yForce);
				repforces.put(u, u_repForce);
							
				xForce = attraction.getX_Force();
				yForce = attraction.getY_Force();
				Force u_attForce = new Force (xForce, yForce);
				attforces.put(u, u_attForce);

			}
			
			// set boxes on display
			for (GraphWrapper u : nodes.keySet()) {
				
				GraphBox box = nodes.get(u);
				int x = (int)(box.getX()+((repforces.get(u).getX_Force()>=0)?Math.ceil(SpringEmbedder.DAMPER * repforces.get(u).getX_Force()):Math.floor(SpringEmbedder.DAMPER * repforces.get(u).getX_Force())));
	            int y = (int)(box.getY()+((repforces.get(u).getY_Force()>=0)?Math.ceil(SpringEmbedder.DAMPER * repforces.get(u).getY_Force()):Math.floor(SpringEmbedder.DAMPER * repforces.get(u).getY_Force())));
				
				box.setXWithoutUpdatingParentsSize(x);
				box.setYWithoutUpdatingParentsSize(y);
			
				
				box = nodes.get(u);
				x = (int)(box.getX()+((attforces.get(u).getX_Force()>=0)?Math.ceil(SpringEmbedder.DAMPER * attforces.get(u).getX_Force()):Math.floor(SpringEmbedder.DAMPER * attforces.get(u).getX_Force())));
	            y = (int)(box.getY()+((attforces.get(u).getY_Force()>=0)?Math.ceil(SpringEmbedder.DAMPER * attforces.get(u).getY_Force()):Math.floor(SpringEmbedder.DAMPER * attforces.get(u).getY_Force())));
				
				box.setXWithoutUpdatingParentsSize(x);
				box.setYWithoutUpdatingParentsSize(y); 	
			}
			
			final long time2 = System.currentTimeMillis();
			if(time2-time1>TIMEINTERVAL){ // algo already took too long => abort!
				break;
			}
			
			if (maxForce < SpringEmbedder.ACCEPTED) {
				break;
			}
		}
		//time1 = System.currentTimeMillis() - time1;
		//System.out.println("Laufzeit = " + time1 + " ms");
	}	
}

