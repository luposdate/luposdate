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
import java.util.Map.Entry;

import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.engine.indexconstruction.RDF3XIntArrayLSMTreeContainer;

public class TestIntArrayLSMTree {

	// path as used in ConstructIndex
	private final static String path = "c:/luposdateindex";

	public static void main(final String[] args) throws ClassNotFoundException, IOException, URISyntaxException {
		final RDF3XIntArrayLSMTreeContainer indices = new RDF3XIntArrayLSMTreeContainer(path);

		// get result of triple pattern <http://localhost/persons/Paul_Erdoes> ?p ?o

		// first lookup dictionary to get code of <http://localhost/persons/Paul_Erdoes>:
		final int code = indices.lookup("<http://localhost/persons/Paul_Erdoes>");
		System.out.println("Code of <http://localhost/persons/Paul_Erdoes>:" + code);

		// now choose SPO collation order (ordinal value is 0) and prefix key <http://localhost/persons/Paul_Erdoes>
		// Unused components in the prefix key must be set to -1
		final SIPParallelIterator<Entry<int[], int[]>, int[]> it = indices.prefixSearchInDefaultGraphs(0, new int[]{code,-1,-1});
		int i=0;
		// first print out all matching id-triples:
		while(it.hasNext()){
			System.out.println(i+": "+Arrays.toString(it.next().getValue()));
			i++;
		}
		it.close();
		// now once again, but using the dictionary to print out the real strings...
		final SIPParallelIterator<Entry<int[], int[]>, int[]> it2 = indices.prefixSearchInDefaultGraphs(0, new int[]{code,-1,-1});
		int j=0;
		while(it2.hasNext()){
			final int[] triple = it2.next().getKey();
			System.out.println(j+": ?p = " + indices.lookup(triple[1]) + ", ?o = " + indices.lookup(triple[2]));
			j++;
		}
		it2.close();
	}
}
