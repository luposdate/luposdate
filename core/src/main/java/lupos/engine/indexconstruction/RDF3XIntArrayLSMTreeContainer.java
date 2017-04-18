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
package lupos.engine.indexconstruction;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.lsmtree.LSMTree;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.disk.store.StoreIntTriple.IntTripleComparator;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.io.helper.InputHelper;

public class RDF3XIntArrayLSMTreeContainer {

	protected final LSMTree<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>> dictionaryStringInteger;
	protected final StringArray dictionaryIntegerString;

	protected final HashMap<URILiteral, GraphIndices> defaultGraphs = new HashMap<URILiteral, GraphIndices>();
	protected final HashMap<URILiteral, GraphIndices> namedGraphs = new HashMap<URILiteral, GraphIndices>();

	public final static String INDICESINFOFILE = "indices.info";

	public final static IntTripleComparator[] comparators = new IntTripleComparator[]{
		new IntTripleComparator(CollationOrder.values()[0]),
		new IntTripleComparator(CollationOrder.values()[1]),
		new IntTripleComparator(CollationOrder.values()[2]),
		new IntTripleComparator(CollationOrder.values()[3]),
		new IntTripleComparator(CollationOrder.values()[4]),
		new IntTripleComparator(CollationOrder.values()[5])
	};

	public RDF3XIntArrayLSMTreeContainer(final String path) throws IOException, ClassNotFoundException, URISyntaxException {
		this(new BufferedInputStream(new FileInputStream(path + File.separator + INDICESINFOFILE)), new String[]{path});
	}

	public RDF3XIntArrayLSMTreeContainer(final InputStream in, final String[] dirs) throws IOException, ClassNotFoundException, URISyntaxException {
		IntTripleComparator.register();
		// set directories from which the indices are read...
		DBMergeSortedBag.setTmpDir(dirs);
		DiskCollection.setTmpDir(dirs);
		lupos.datastructures.paged_dbbptree.DBBPTree.setTmpDir(dirs[0],false);

		// reading in the dictionary String -> Integer
		this.dictionaryStringInteger = LSMTree.readLuposObject(in);

		// reading in the dictionary Integer -> String
		StringArray.setFileID(1);
		this.dictionaryIntegerString = StringArray.readLuposStringArray(in);

		// reading in default graphs...
		int number = InputHelper.readLuposInt(in);
		for (int i = 0; i < number; i++) {
			final GraphIndices graphIndices = readSixIndices(in);
			this.defaultGraphs.put(graphIndices.uri, graphIndices);
		}
		// reading in named graphs...
		number = InputHelper.readLuposInt(in);
		for (int i = 0; i < number; i++) {
			final GraphIndices graphIndices = readSixIndices(in);
			this.namedGraphs.put(graphIndices.uri, graphIndices);
		}
		in.close();
	}

	public Integer lookup(final String rdfTerm){
		try {
			return this.dictionaryStringInteger.get(rdfTerm);
		} catch (ClassNotFoundException | IOException | URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	public String lookup(final int code){
		return this.dictionaryIntegerString.get(code);
	}

	public SIPParallelIterator<Entry<int[], int[]>, int[]> prefixSearchInDefaultGraphs(final int collationOrder, final int[] key){
		if(this.defaultGraphs.isEmpty()){
			return null;
		}
		if(this.defaultGraphs.size()>1){
			throw new UnsupportedOperationException("Currently only exactly one default graph supported!");
		}
		return this.defaultGraphs.values().iterator().next().evaluationIndices[collationOrder].prefixSearchSIPParallelIterator(comparators[collationOrder],key);
	}

	@SuppressWarnings("unchecked")
	private static GraphIndices readSixIndices(final InputStream in) throws ClassNotFoundException, IOException, URISyntaxException{
		final URILiteral uri = (URILiteral) LiteralFactory.readLuposLiteral(in);
		final LSMTree<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>>[] evaluationIndices = new LSMTree[CollationOrder.values().length];
		for(int i=0; i<CollationOrder.values().length; i++){
			evaluationIndices[i] = LSMTree.readLuposObject(in);
		}
		return new GraphIndices(uri, evaluationIndices);
	}

	public static class GraphIndices {
		public final URILiteral uri;
		public final LSMTree<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>>[] evaluationIndices;

		public GraphIndices(final URILiteral uri, final LSMTree<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>>[] evaluationIndices){
			this.uri = uri;
			this.evaluationIndices = evaluationIndices;
		}
	}
}
