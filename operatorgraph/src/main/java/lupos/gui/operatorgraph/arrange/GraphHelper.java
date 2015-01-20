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

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.misc.Tuple;

public class GraphHelper {

	/**
	 * If graph is out of display after a computed layout, this method fits it 
	 * back into the display
	 */
	public static void fitToWindow(final OperatorGraph operatorgraph) {
		HashMap<GraphWrapper, GraphBox> boxes = operatorgraph.getBoxes();
		int minX = 0; int minY = 0;
		
		// get the smallest x- and y-values
		for (GraphBox box : boxes.values()) {
			if (box.getX() < minX) minX = box.getX();
			if (box.getY() < minY) minY = box.getY();
		}
		
		// x-correction
		if (minX < 0) {
			for (GraphWrapper gw : boxes.keySet()) {
				GraphBox box = boxes.get(gw);
				box.setXWithoutUpdatingParentsSize(box.getX()+Math.abs(minX));
			}
		}
				
		// y-correction
		if (minY < 0) {
			for (GraphWrapper gw : boxes.keySet()) {
				GraphBox box = boxes.get(gw);
				box.setYWithoutUpdatingParentsSize(box.getY()+Math.abs(minY));
			}
		}
		
		minX = Integer.MAX_VALUE; minY = Integer.MAX_VALUE;
		
		// get the smallest x- and y-values
		for (GraphBox box : boxes.values()) {
			if (box.getX() < minX) minX = box.getX();
			if (box.getY() < minY) minY = box.getY();
		}
		
		// x-correction
		for (GraphWrapper gw : boxes.keySet()) {
			GraphBox box = boxes.get(gw);
			box.setXWithoutUpdatingParentsSize(box.getX()-minX);
		}
				
		// y-correction
		for (GraphWrapper gw : boxes.keySet()) {
			GraphBox box = boxes.get(gw);
			box.setYWithoutUpdatingParentsSize(box.getY()-minY);
		}
	}	
	
	/**
	 * Method gets the width of the graph
	 * @return
	 */
	protected static int graphWidth(final OperatorGraph operatorgraph) {
		int width = 0;
		HashMap<GraphWrapper, GraphBox> boxes = operatorgraph.getBoxes();
		
		for (GraphBox box : boxes.values()) {
			if ((box.getX() + box.width) > width) {
				width = box.getX() + box.width;
			}
		}		
		return width;
	}
	
	/**
	 * Method gets the avg. width and height of all boxes in the graph
	 * @return
	 */
	protected static Tuple<Double, Double> getAvgSizesOfBoxes(final OperatorGraph operatorgraph) {		
		long width = 0;
		long height = 0;
		HashMap<GraphWrapper, GraphBox> boxes = operatorgraph.getBoxes();
		
		if(boxes.size()==0){
			return new Tuple<Double, Double>(1.0, 1.0);
		}
		
		for (GraphBox box : boxes.values()) {
			width += box.width;
			height += box.width;
		}
		
		return new Tuple<Double, Double>(width/((double)boxes.size()), height/((double)boxes.size()));
	}
	
	/**
	 * Method gets the height of the graph
	 * @return
	 */
	protected static int graphHeight(final OperatorGraph operatorgraph) {
		int height = 0;
		HashMap<GraphWrapper, GraphBox> boxes = operatorgraph.getBoxes();
		
		for (GraphBox box : boxes.values()) {
			if ((box.getY() + box.height) > height) {
				height= box.getY() + box.height;
			}
		}		
		return height;
	}
	
	/**
	 * Method sets all node-positions to their starting-positions.
	 * Here x = -1 an y = -1
	 */
	protected static void restoreOriginalPositions(final OperatorGraph operatorgraph) {
		for (GraphBox box: operatorgraph.getBoxes().values()) {
			box.setX(-1); 
			box.setY(-1);
		}		
	}
}
