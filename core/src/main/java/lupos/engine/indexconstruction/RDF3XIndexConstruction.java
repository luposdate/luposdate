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
/**
 *
 */
package lupos.engine.indexconstruction;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;

import lupos.compression.Compression;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSetUsingTrie;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LazyLiteralOriginalContent;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StringIntegerNodeDeSerializer;
import lupos.datastructures.patriciatrie.TrieSet;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.RDF3XQueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Indices.DATA_STRUCT;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.engine.operators.index.adaptedRDF3X.SixIndices;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.io.helper.OutHelper;
import lupos.misc.TimeInterval;
import lupos.misc.util.ImmutableIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class constructs the RDF3X indices on disk using a dictionary, which is
 * also constructed on disk...
 *
 * @author groppe
 * @version $Id: $Id
 */
public class RDF3XIndexConstruction {

	private static final Logger log = LoggerFactory.getLogger(RDF3XIndexConstruction.class);

	private static final int k = 1000;
	private static final int k_ = 1000;

	/** Constant <code>LIMIT_ELEMENTS_IN_TRIE=50000000</code> */
	public static long LIMIT_ELEMENTS_IN_TRIE = 50000000;

	private RDF3XIndexConstruction() {
	}

	/**
	 * <p>insertUsedStringRepresentations.</p>
	 *
	 * @param u a {@link lupos.datastructures.items.literal.URILiteral} object.
	 * @param dataFormat a {@link java.lang.String} object.
	 * @param rdftermsRepresentations a {@link java.util.SortedSet} object.
	 * @param tc a {@link lupos.engine.operators.tripleoperator.TripleConsumer} object.
	 */
	public static void insertUsedStringRepresentations(final URILiteral u,
			final String dataFormat,
			final SortedSet<String> rdftermsRepresentations,
			final TripleConsumer tc) {
		rdftermsRepresentations.add(u.toString());
		try {
			CommonCoreQueryEvaluator.readTriples(dataFormat, u.openStream(), tc);
		} catch (final Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Constructs the large-scale indices for RDF3X.
	 * The command line arguments are
	 * datafile dataformat encoding NONE|BZIP2|HUFFMAN|GZIP directory_for_indices [LIMIT_ELEMENTS_IN_MEMORY [datafile2 [datafile3 ...]]]
	 * If you want to import more than one file you can use the additional parameters datafilei!
	 *
	 * @param args
	 *            command line arguments
	 */
	public static void main(final String[] args) {
		try {

			log.info("Starting program to construct an RDF3X Index for LUPOSDATE...");
			log.debug("[help is printed when using less than 5 command line arguments]");
			log.debug("_______________________________________________________________");

			if (args.length < 5) {
				log.error("Usage: java -Xmx768M lupos.engine.indexconstruction.RDF3XIndexConstruction <datafile> <dataformat> <encoding> <NONE|BZIP2|HUFFMAN|GZIP> <directory for indices> [LIMIT_ELEMENTS_IN_MEMORY [<datafile2> [<datafile3> ...]]]");
				log.error("Example: java -Xmx768M lupos.engine.indexconstruction.RDF3XIndexConstruction data.n3 N3 UTF-8 NONE /luposdateindex 500000");
				return;
			}

			final Date start = new Date();
			log.debug("Starting time: {}", start);

			LiteralFactory.setType(LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
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

			final String[] dir = new String[] { args[4] };
			final String writeindexinfo = dir[0]+File.separator+RDF3XQueryEvaluator.INDICESINFOFILE;
			DBMergeSortedBag.setTmpDir(dir);
			DiskCollection.setTmpDir(dir);
			lupos.datastructures.paged_dbbptree.DBBPTree.setTmpDir(args[4],true);

			final Collection<URILiteral> defaultGraphs = new LinkedList<URILiteral>();
			final Collection<URILiteral> namedGraphs = new LinkedList<URILiteral>();
			defaultGraphs.add(LiteralFactory.createURILiteralWithoutLazyLiteral("<file:" + datafile+ ">"));

			if(args.length>5){
				LIMIT_ELEMENTS_IN_TRIE = Long.parseLong(args[5]);
				DBMergeSortedSetUsingTrie.LIMIT_ELEMENTS_IN_SET = LIMIT_ELEMENTS_IN_TRIE;
			}
			for(int i=6; i<args.length; i++){
				defaultGraphs.add(LiteralFactory.createURILiteralWithoutLazyLiteral("<file:" + args[i]+ ">"));
			}

			// Construct dictionary:

			final Thread codeMapConstructionThread = new Thread() {
				@Override
				public void run() {
						final DBMergeSortedSetUsingTrie rdftermsRepresentations = new DBMergeSortedSetUsingTrie(new SortConfiguration(), String.class);

						final TripleConsumer tc = new TripleConsumer() {

							@Override
							public void consume(final Triple triple) {
								for (final Literal l : triple) {
									rdftermsRepresentations.add(l.toString());
									if (l.originalStringDiffers()) {
										rdftermsRepresentations.add(l.originalString());
									}
								}
							}

						};
						for (final URILiteral u : defaultGraphs) {
							insertUsedStringRepresentations(u, dataFormat, rdftermsRepresentations, tc);
						}
						for (final URILiteral u : namedGraphs) {
							insertUsedStringRepresentations(u, dataFormat, rdftermsRepresentations, tc);
						}
						// now generate B+-tree for integer-string map and
						// string-integer
						// map of the codemap!
						final Generator<String, Integer> smsi = new Generator<String, Integer>() {

							@Override
							public Iterator<java.util.Map.Entry<String, Integer>> iterator() {
								return new ImmutableIterator<java.util.Map.Entry<String, Integer>>() {
									Iterator<String> it = rdftermsRepresentations.iterator();
									int index = 1;

									@Override
									public boolean hasNext() {
										return this.it.hasNext();
									}

									@Override
									public java.util.Map.Entry<String, Integer> next() {
										if (!this.it.hasNext()) {
											return null;
										} else {
											return new java.util.Map.Entry<String, Integer>() {
												String s = it.next();
												int localIndex = index++;

												@Override
												public String getKey() {
													return this.s;
												}

												@Override
												public Integer getValue() {
													return this.localIndex;
												}

												@Override
												public Integer setValue(
														final Integer arg0) {
													throw new UnsupportedOperationException();
												}

											};
										}
									}
								};
							}

							@Override
							public int size() {
								return rdftermsRepresentations.size();
							}

						};

						rdftermsRepresentations.sort();

						final Thread thread0 = new Thread() {
							@Override
							public void run() {
								lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer> simap;
								try {
									simap = new lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer>(
											k,
											k_,
											new StringIntegerNodeDeSerializer());
									simap.generateDBBPTree(smsi);
									LazyLiteral.setHm(new StringIntegerMapJava(
											simap));
								} catch (final IOException e) {
									log.error(e.getMessage(), e);
								}
							}
						};
						final Thread thread1 = new Thread() {
							@Override
							public void run() {
								StringArray ismap;
								try {
									ismap = new StringArray();
									ismap.generate(rdftermsRepresentations.iterator());
									LazyLiteral.setV(ismap);
								} catch (final IOException e) {
									log.error(e.getMessage(), e);
								}
							}
						};
						thread0.start();
						thread1.start();
						try {
							thread0.join();
							thread1.join();
						} catch (final InterruptedException e) {
							log.error(e.getMessage(), e);
						}
						rdftermsRepresentations.release();

				}
			};
			codeMapConstructionThread.start();

			try {
				codeMapConstructionThread.join();
			} catch (final InterruptedException e) {
				log.error(e.getMessage(), e);
			}

			final Date intermediate = new Date();
			final TimeInterval codemapInterval = new TimeInterval(start, intermediate);
			log.info("Codemap constructed in: {}", codemapInterval);
			log.info("Codemap contains {} entries!", LazyLiteral.getHm().size());

			// for debugging purposes:
//			final TripleConsumer interTripleConsumer = new TripleConsumer() {
//				public void consume(final Triple triple) {
//
//					// the generated codes using the dictionary:
//					System.out.println("(" + getCode(triple.getSubject())
//							+ ", " + getCode(triple.getPredicate()) + ", "
//							+ getCode(triple.getObject()) + ")");
//
//					indices.consume(triple);
//				}
//
//			};
//
//			new GenerateIDTriplesUsingStringSearch2(rdfURL, dataFormat,
//					interTripleConsumer);

			final Indices indices = new SixIndices(defaultGraphs.iterator().next());
			new GenerateIDTriplesUsingStringSearch2(defaultGraphs, dataFormat, indices);

			// write out index info

			final OutputStream out = new BufferedOutputStream(new FileOutputStream(writeindexinfo));
			indices.constructCompletely();

			OutHelper.writeLuposInt(lupos.datastructures.paged_dbbptree.DBBPTree.getCurrentFileID(), out);

			((lupos.datastructures.paged_dbbptree.DBBPTree) ((StringIntegerMapJava) LazyLiteral.getHm()).getOriginalMap()).writeLuposObject(out);
			((StringArray) LazyLiteral.getV()).writeLuposStringArray(out);
			OutHelper.writeLuposInt(1, out);
			LiteralFactory.writeLuposLiteral(defaultGraphs.iterator().next(), out);
			indices.writeIndexInfo(out);
			OutHelper.writeLuposInt(0, out);
			out.close();
			final Date end = new Date();
			log.debug("_______________________________________________________________");
			log.info("Done, RDF3X index constructed!");
			log.debug("End time: {}", end);

			log.debug("Used time: {}", new TimeInterval(start, end));
			log.debug("Number of imported triples: {}", ((SixIndices)indices).getIndex(CollationOrder.SPO).size());
		} catch (final Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static String getCode(final Literal literal) {
		if (literal instanceof LazyLiteral) {
			String result = "" + ((LazyLiteral) literal).getCode();
			if (literal instanceof LazyLiteralOriginalContent) {
				result += "(code original content:"
					+ ((LazyLiteralOriginalContent) literal)
					.getCodeOriginalContent() + ")";
			}
			return result;
		} else {
			return "Error - no lazy literal";
		}
	}

	public static class GenerateIDTriplesUsingStringSearch2 {

		public GenerateIDTriplesUsingStringSearch2(
				final Collection<URILiteral> graphURIs, final String dataFormat,
				final TripleConsumer tc) throws Exception {

			final TrieSet searchtree = TrieSet.createRamBasedTrieSet();

			final DiskCollection<Triple> triples = new DiskCollection<Triple>(Triple.class);

			try {

				final TripleConsumer tripleConsumer=new TripleConsumer() {

					@Override
					public void consume(final Triple triple) {
						for (final Literal l : triple) {
							searchtree.add(l.toString());
							if (l.originalStringDiffers()) {
								searchtree.add(l.originalString());
							}
						}
						triples.add(triple);
						if (searchtree.size()>LIMIT_ELEMENTS_IN_TRIE) {
							GenerateIDTriplesUsingStringSearch2.this.handleRun(searchtree, triples, tc);
						}
					}

				};

				for(final URILiteral graphURI:graphURIs){
					CommonCoreQueryEvaluator.readTriples(dataFormat,
							graphURI.openStream(), tripleConsumer);
				}
				if (searchtree.size() > 0) {
					this.handleRun(searchtree, triples, tc);
					triples.release();
				}
			} catch (final IOException e) {
				log.error(e.getMessage(), e);
			}
		}

		private void handleRun(final TrieSet searchtree,
				final Collection<Triple> triples, final TripleConsumer tc) {
			final int[] map = this.getMap(searchtree);

			for (final Triple triple : triples) {
				final Triple dummy = new Triple(triple.getPos(0),
						triple.getPos(1), triple.getPos(2));
				for (int pos = 0; pos < 3; pos++) {
					if (triple.getPos(pos).originalStringDiffers()) {
						dummy.setPos(
								pos,
								new LazyLiteralOriginalContent(
										map[searchtree.getIndex(triple.getPos(
												pos).toString())],
												map[searchtree.getIndex(triple.getPos(
														pos).originalString())]));
					} else {
						dummy.setPos(
								pos,
								new LazyLiteral(map[searchtree.getIndex(triple
										.getPos(pos).toString())]));
					}
				}
				tc.consume(dummy);
			}

			// clear the searchtree and the triples collection for the next
			// "Run"
			searchtree.clear();
			triples.clear();
		}

		private int[] getMap(final TrieSet searchtree) {
			// build map from local dictionary to global
			// dictionary...
			final int[] map = new int[searchtree.size()];

			// get global map:
			final Iterator<java.util.Map.Entry<String, Integer>> iterator = ((StringIntegerMapJava) LazyLiteral
					.getHm()).getMap().entrySet().iterator();
			java.util.Map.Entry<String, Integer> current = iterator.next();

			int index = 0;
			for (final String s : searchtree) {
				if (iterator instanceof SIPParallelIterator) {
					while (s.compareTo(current.getKey()) != 0) {
						current = ((SIPParallelIterator<java.util.Map.Entry<String, Integer>, String>) iterator)
						.next(s);
					}
				} else {
					while (s.compareTo(current.getKey()) != 0) {
						current = iterator.next();
					}
				}
				map[index++] = current.getValue();
			}
			return map;
		}
	}
}
