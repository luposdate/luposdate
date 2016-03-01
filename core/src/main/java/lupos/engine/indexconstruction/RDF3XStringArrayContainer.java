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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;

import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.io.helper.InputHelper;

public class RDF3XStringArrayContainer {

	protected final HashMap<URILiteral, GraphIndices> defaultGraphs = new HashMap<URILiteral, GraphIndices>();
	protected final HashMap<URILiteral, GraphIndices> namedGraphs = new HashMap<URILiteral, GraphIndices>();

	public final static String INDICESINFOFILE = "indices.info";

	public RDF3XStringArrayContainer(final String path) throws IOException, ClassNotFoundException, URISyntaxException {
		this(new FileInputStream(path + File.separator + INDICESINFOFILE), new String[]{path});
	}

	public RDF3XStringArrayContainer(final InputStream in, final String[] dirs) throws IOException, ClassNotFoundException, URISyntaxException {
		// set directories from which the indices are read...
		DBMergeSortedBag.setTmpDir(dirs);
		DiskCollection.setTmpDir(dirs);
		lupos.datastructures.paged_dbbptree.DBBPTree.setTmpDir(dirs[0],false);

		// reading in file id...
		DBBPTree.setCurrentFileID(InputHelper.readLuposInt(in));

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
	}


	@SuppressWarnings("unchecked")
	public SIPParallelIterator<String[], String[]> prefixSearchInDefaultGraphs(final int collationOrder, final String[] key){
		if(this.defaultGraphs.isEmpty()){
			return null;
		}
		if(this.defaultGraphs.size()>1){
			throw new UnsupportedOperationException("Currently only exactly one default graph supported!");
		}
		return (SIPParallelIterator<String[], String[]>) this.defaultGraphs.values().iterator().next().evaluationIndices[collationOrder].prefixSearch(key);
	}

	@SuppressWarnings("unchecked")
	private static GraphIndices readSixIndices(final InputStream in) throws ClassNotFoundException, IOException, URISyntaxException{
		final URILiteral uri = (URILiteral) LiteralFactory.readLuposLiteral(in);
		final DBBPTree<String[], String[]>[] evaluationIndices = new DBBPTree[CollationOrder.values().length];
		for(int i=0; i<CollationOrder.values().length; i++){
			evaluationIndices[i] = DBBPTree.readLuposObject(in);
			evaluationIndices[i].setName(CollationOrder.values()[i].toString());
		}
		return new GraphIndices(uri, evaluationIndices);
	}

	public static class GraphIndices {
		public final URILiteral uri;
		public final DBBPTree<String[], String[]>[] evaluationIndices;

		public GraphIndices(final URILiteral uri, final DBBPTree<String[], String[]>[] evaluationIndices){
			this.uri = uri;
			this.evaluationIndices = evaluationIndices;
		}
	}
}
