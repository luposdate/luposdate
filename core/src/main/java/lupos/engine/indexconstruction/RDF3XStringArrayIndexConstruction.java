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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import lupos.compression.Compression;
import lupos.datastructures.buffermanager.BufferManager;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.datastructures.items.StringArrayComparator;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.NodeDeSerializer;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StringArrayNodeDeSerializer;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.RDF3XQueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Indices.DATA_STRUCT;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.io.helper.OutHelper;
import lupos.misc.TimeInterval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDF3XStringArrayIndexConstruction {

	private static final Logger log = LoggerFactory.getLogger(RDF3XStringArrayIndexConstruction.class);

	public static void main(final String[] args) {
		log.info("Starting program to construct an RDF3X String Index for LUPOSDATE...");
		log.debug("[help is printed when using less than 5 command line arguments]");
		log.debug("_______________________________________________________________");

		if (args.length < 5) {
			log.error("Usage: java -Xmx768M lupos.engine.indexconstruction.RDF3XStringArrayIndexConstruction <datafile> <dataformat> <encoding> <NONE|BZIP2|HUFFMAN|GZIP> <directory for indices> [LIMIT_TRIPLES_IN_MEMORY [<datafile2> [<datafile3> ...]]]");
			log.error("Example: java -Xmx768M lupos.engine.indexconstruction.FastRDF3XStringArrayIndexConstruction data.n3 N3 UTF-8 NONE /luposdateindex 500000");
			return;
		}

		try {
			// analyze command line parameters
			final Date start = new Date();
			log.debug("Starting time: {}", start);

			LiteralFactory.setType(LiteralFactory.MapType.NOCODEMAP);
			Indices.setUsedDatastructure(DATA_STRUCT.DBBPTREE);

			final String datafile = args[0];
			final String dataFormat = args[1];
			CommonCoreQueryEvaluator.encoding = args[2];

			final String compressor = args[3];
			if(compressor.compareTo("BZIP2")==0){
				SortConfiguration.setDEFAULT_COMPRESSION(Compression.BZIP2);
			} else if(compressor.compareTo("HUFFMAN")==0){
				SortConfiguration.setDEFAULT_COMPRESSION(Compression.HUFFMAN);
			} else if(compressor.compareTo("GZIP")==0){
				SortConfiguration.setDEFAULT_COMPRESSION(Compression.GZIP);
			} else {
				SortConfiguration.setDEFAULT_COMPRESSION(Compression.NONE);
			}

			String dir = args[4];
			if(!dir.endsWith("\\") && !dir.endsWith("/")) {
				dir += "/";
			}
			// make directory such that we can store something inside!
			final File f_dir = new File(dir);
			f_dir.mkdirs();

			final String[] dirArray = new String[] { dir };
			final String writeindexinfo = dirArray[0]+File.separator+RDF3XQueryEvaluator.INDICESINFOFILE;
			DBMergeSortedBag.setTmpDir(dirArray);
			DiskCollection.setTmpDir(dirArray);
			lupos.datastructures.paged_dbbptree.DBBPTree.setTmpDir(dir, true);

			final Collection<URILiteral> defaultGraphs = new LinkedList<URILiteral>();
			defaultGraphs.add(LiteralFactory.createURILiteralWithoutLazyLiteral("<file:" + datafile+ ">"));

			if(args.length>5) {
				FastRDF3XIndexConstruction.LIMIT_TRIPLES_IN_MEMORY = Integer.parseInt(args[5]);
			}
			for(int i=6; i<args.length; i++) {
				defaultGraphs.add(LiteralFactory.createURILiteralWithoutLazyLiteral("<file:" + args[i]+ ">"));
			}

			final OutputStream out = new BufferedOutputStream(new FileOutputStream(writeindexinfo));
			OutHelper.writeLuposInt(lupos.datastructures.paged_dbbptree.DBBPTree.getCurrentFileID(), out);

			OutHelper.writeLuposInt(1, out); // only one default graph!
			LiteralFactory.writeLuposLiteral(defaultGraphs.iterator().next(), out); // write uri of deault graph

			for(int order=0; order<6; order++){
				final CollationOrder collationOrder = CollationOrder.values()[order];
				@SuppressWarnings("rawtypes")
				final DBMergeSortedSet<String[]> set = new DBMergeSortedSet<String[]>(new SortConfiguration(), new StringArrayComparator(collationOrder), String[].class);

				for(final URILiteral uri: defaultGraphs) {
					try {
						CommonCoreQueryEvaluator.readTriples(dataFormat, uri.openStream(), (t)->set.add(new String[]{t.getSubject().originalString(), t.getPredicate().originalString(), t.getObject().originalString()}));
					} catch (final Exception e) {
						log.error(e.getMessage(), e);
					}
				}
				set.sort();
				final StringArrayComparator comparator = new StringArrayComparator(collationOrder);
		     	final NodeDeSerializer<String[], String[]> nodeDeSerializer = new StringArrayNodeDeSerializer(collationOrder);
		     	final DBBPTree<String[], String[]> tree = new DBBPTree<String[], String[]>(comparator, FastRDF3XIndexConstruction.k, FastRDF3XIndexConstruction.k_, nodeDeSerializer, String[].class, String[].class);
		     	tree.generateDBBPTree(new Generator<String[], String[]>(){
		     			@Override
						public int size(){
		     				return set.size();
		     			}

		     			@Override
						public Iterator<Entry<String[], String[]>> iterator(){
		     				final Iterator<String[]> it = set.iterator();
		     				return new Iterator<Entry<String[], String[]>>(){

								@Override
								public boolean hasNext() {
									return it.hasNext();
								}

								@Override
								public Entry<String[], String[]> next() {
									final String[] next = it.next();
									if(next==null){
										return null;
									}
									return new MapEntry<String[], String[]>(next, next);
								}

		     				};
		     			}
		     	});
		     	BufferManager.getBufferManager().writeAllModifiedPages();
		     	tree.writeLuposObject(out);
			}
			OutHelper.writeLuposInt(0, out); // zero named graphs!
			out.close();
			final Date end = new Date();
			log.debug("_______________________________________________________________");
			log.info("Done, RDF3X String index constructed!");
			log.debug("End time: {}", end);

			log.debug("Used time: {}", new TimeInterval(start, end));
		} catch(final Exception e){
			e.printStackTrace();
			System.err.println(e);
		}
	}

}
