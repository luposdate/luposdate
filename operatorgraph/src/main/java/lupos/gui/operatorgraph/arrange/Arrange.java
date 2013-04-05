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

import lupos.gui.operatorgraph.OperatorGraph;

public enum Arrange {
	LAYERED() {

		@Override
		public void arrange(final OperatorGraph operatorgraph) {
			GraphHelper.restoreOriginalPositions(operatorgraph);
			LayeredDrawing.arrange(operatorgraph);
		}

		@Override
		public String toString() {
			return "Layered Drawing";
		}

	},
	
	SUGIYAMA() {

		@Override
		public void arrange(final OperatorGraph operatorgraph) {
			GraphHelper.restoreOriginalPositions(operatorgraph);
			Sugiyama.arrange(operatorgraph);
		}

		@Override
		public String toString() {
			return "Sugiyama";
		}

	},
	
	SPRING_EMBEDDER_AFTER_RANDOM() {

		@Override
		public void arrange(final OperatorGraph operatorgraph) {
			// to get some initial-values for spring embedder
			RndmLayout.arrange(operatorgraph);
			SpringEmbedder.arrange(operatorgraph);
		}

		@Override
		public String toString() {
			return "Spring-Embedder after Random";
		}

		@Override
		public boolean arrangeAfterZooming(){
			return false;
		}
	},
	
	SPRING_EMBEDDER() {

		@Override
		public void arrange(final OperatorGraph operatorgraph) {
			// use current graph as initial-values for spring embedder
			SpringEmbedder.arrange(operatorgraph);
		}

		@Override
		public String toString() {
			return "Spring-Embedder on current graph";
		}

		@Override
		public boolean arrangeAfterZooming(){
			return false;
		}
	},
	
	EDGEBASED() {

		@Override
		public void arrange(final OperatorGraph operatorgraph) {
			GraphHelper.restoreOriginalPositions(operatorgraph);
			EdgeBased.arrange(operatorgraph);
		}

		@Override
		public String toString() {
			return "Edge-based";
		}

	},
	
	RANDOM() {

		@Override
		public void arrange(final OperatorGraph operatorgraph) {
			GraphHelper.restoreOriginalPositions(operatorgraph);
			RndmLayout.arrange(operatorgraph);	
		}

		@Override
		public String toString() {
			return "Random";
		}

		@Override
		public boolean arrangeAfterZooming(){
			return false;
		}
	};

	public abstract void arrange(final OperatorGraph operatorgraph);
	
	/**
	 * With this method it is decided whether just arranged is called after zooming (for stable layout algorithms) or the new positions of the graph boxes are determined by calculations with a factor.
	 * @return true if just arrange is called after zooming
	 */
	public boolean arrangeAfterZooming(){
		return true;
	}
}
