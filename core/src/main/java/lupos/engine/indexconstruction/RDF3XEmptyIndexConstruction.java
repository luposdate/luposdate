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
package lupos.engine.indexconstruction;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

import lupos.datastructures.buffermanager.BufferManager;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.items.literal.string.StringURILiteral;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StringIntegerNodeDeSerializer;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.evaluators.RDF3XQueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Indices.DATA_STRUCT;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.engine.operators.index.adaptedRDF3X.SixIndices;
import lupos.io.helper.OutHelper;
import lupos.misc.TimeInterval;

/**
 * This class is for creating an empty index on disk for the RDF3X query evaluator
 */
public class RDF3XEmptyIndexConstruction {

	// the constants for the B+-tree
	private static final int k = 1000;
	private static final int k_ = 1000;

	/**
	 * Entry point to create an empty RDF3X disk-based index
	 * @param args the first command line argument should contain the directory in which the index is created...
	 */
	public static void main(final String[] args) {
		try {

			System.out.println("Program to construct an empty RDF3X Index for LUPOSDATE...");
			System.out.println("[help is printed when using less than 1 command line argument]");
			System.out.println("_______________________________________________________________");

			if (args.length < 1) {
				System.out.println("Usage:\njava -Xmx768M lupos.engine.indexconstruction.RDF3XEmptyIndexConstruction <directory for indices>");
				System.out.println("Example:\njava -Xmx768M lupos.engine.indexconstruction.RDF3XEmptyIndexConstruction /luposdateindex");
				return;
			}

			final Date start = new Date();
			System.out.println("Starting time: "+start);

			LiteralFactory.setType(LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
			Indices.setUsedDatastructure(DATA_STRUCT.DBBPTREE);

			final String[] dir = new String[] { args[0] };
			final String writeindexinfo = dir[0]+File.separator+RDF3XQueryEvaluator.INDICESINFOFILE;
			DBMergeSortedBag.setTmpDir(dir);
			DiskCollection.setTmpDir(dir);
			lupos.datastructures.paged_dbbptree.DBBPTree.setTmpDir(dir[0], true);

			final lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer> simap =
					new lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer>(k, k_, new StringIntegerNodeDeSerializer());
			LazyLiteral.setHm(new StringIntegerMapJava(simap));

			final StringArray ismap = new StringArray();
			LazyLiteral.setV(ismap);

			final URILiteral defaultGraph = new StringURILiteral("<http://localhost/default>");
			// just for inserting it into the codemap:
			defaultGraph.createThisLiteralNew();

			final Indices indices = new SixIndices(defaultGraph, true);

			// write out index info

			final OutputStream out = new BufferedOutputStream(new FileOutputStream(writeindexinfo));
			indices.constructCompletely();
			BufferManager.getBufferManager().writeAllModifiedPages();

			OutHelper.writeLuposInt(lupos.datastructures.paged_dbbptree.DBBPTree.getCurrentFileID(), out);

			((lupos.datastructures.paged_dbbptree.DBBPTree) ((StringIntegerMapJava) LazyLiteral.getHm()).getOriginalMap()).writeLuposObject(out);
			((StringArray) LazyLiteral.getV()).writeLuposStringArray(out);
			OutHelper.writeLuposInt(1, out);
			LiteralFactory.writeLuposLiteral(defaultGraph, out);
			indices.writeIndexInfo(out);
			OutHelper.writeLuposInt(0, out);
			out.close();
			final Date end = new Date();
			System.out.println("_______________________________________________________________\nDone, RDF3X index constructed!\nEnd time: "+end);

			final TimeInterval interval = new TimeInterval(start, end);
			System.out.println("Used time: " + interval);
			System.out.println("Number of imported triples: "+((SixIndices)indices).getIndex(CollationOrder.SPO).size());


		} catch(final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

}
