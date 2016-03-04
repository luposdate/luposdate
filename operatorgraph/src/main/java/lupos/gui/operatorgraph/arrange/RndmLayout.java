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
import java.util.Random;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.misc.Tuple;

/**
 * The class RndLayout implements a Random - Layout
 * which is used in the LUPOSDATE- Project
 *
 * @author Tobias Bielfeld
 * @version $Id: $Id
 */
public class RndmLayout {

	private RndmLayout() {
	}

	/**
	 * <p>arrange.</p>
	 *
	 * @param operatorgraph a {@link lupos.gui.operatorgraph.OperatorGraph} object.
	 */
	public static void arrange(final OperatorGraph operatorgraph){
		//long time1 = System.currentTimeMillis();
		
		Tuple<Double, Double> avgSizes = GraphHelper.getAvgSizesOfBoxes(operatorgraph);
		
		HashMap <GraphWrapper, GraphBox> boxes = operatorgraph.getBoxes();
		
		final double sqrt_size = Math.sqrt(boxes.size());
		
		int field_width = (int) Math.ceil(sqrt_size * (avgSizes.getFirst()+operatorgraph.SPACING_X));
		int field_height = (int) Math.ceil(sqrt_size * (avgSizes.getSecond()+operatorgraph.SPACING_Y));
		
		Random rnd = new Random();
				
		for(GraphWrapper gw : boxes.keySet()) {
			GraphBox box = boxes.get(gw);
			
			// Get some random positions
			int x = rnd.nextInt(field_width);
			int y = rnd.nextInt(field_height);
			
			box.setXWithoutUpdatingParentsSize(x);
			box.setYWithoutUpdatingParentsSize(y);			
		}
		//time1 = System.currentTimeMillis() - time1;
		//System.out.println("Laufzeit = " + time1 + " ms");
	}
}
