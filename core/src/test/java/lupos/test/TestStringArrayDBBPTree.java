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
package lupos.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.engine.indexconstruction.RDF3XStringArrayContainer;


public class TestStringArrayDBBPTree {

	// path as used in FastRDF3XIntArrayIndexConstruction
	private final static String path = "c:/luposdateindex";

	public static void main(final String[] args) throws ClassNotFoundException, IOException, URISyntaxException {
		final RDF3XStringArrayContainer indices = new RDF3XStringArrayContainer(path);

		// get result of triple pattern <http://localhost/persons/Paul_Erdoes> ?p ?o

		// now choose SPO collation order (ordinal value is 0) and prefix key <http://localhost/persons/Paul_Erdoes>
		// Unused components in the prefix key must be set to -1
		final SIPParallelIterator<String[], String[]> it = indices.prefixSearchInDefaultGraphs(0, new String[]{"<http://localhost/persons/Paul_Erdoes>",null,null});
		// final SIPParallelIterator<String[], String[]> it = indices.prefixSearchInDefaultGraphs(0, new String[]{null,null,null});
		int i=0;
		// first print out all matching id-triples:
		while(it.hasNext()){
			System.out.println(i+": "+Arrays.toString(it.next()));
			i++;
		}
		it.close();
	}
}
