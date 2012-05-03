/**
 * 
 */
package lupos.engine.indexconstruction;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;

import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSetUsingStringSearch;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LazyLiteralOriginalContent;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.IntegerStringMapJava;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.paged_dbbptree.StandardNodeDeSerializer;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.datastructures.trie.SuperTrie;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.RDF3XQueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Indices.DATA_STRUCT;
import lupos.engine.operators.index.adaptedRDF3X.SixIndices;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.io.LuposObjectOutputStream;

/**
 * This class constructs the RDF3X indices on disk using a dictionary, which is
 * also constructed on disk...
 * 
 */
public class RDF3XIndexConstruction {
	private static final int k = 1000;
	private static final int k_ = 1000;

	private static void insertUsedStringRepresentations(final URILiteral u,
			final String dataFormat,
			final SortedSet<String> rdftermsRepresentations,
			final TripleConsumer tc) {
		rdftermsRepresentations.add(u.toString());
		try {
			CommonCoreQueryEvaluator
			.readTriples(dataFormat, u.openStream(), tc);
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * Constructs the large-scale indices for RDF3X. 
	 * The command line arguments are
	 * <datafile> <dataformat> <encoding> <directory for indices> [<datafile2> [<datafile3> ...]]
	 * If you want to import more than one file you can use the additional parameters <datafilei>!
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(final String[] args) {
		try {

			if (args.length < 4) {
				System.out
				.println("Usage:\njava -Xmx768M lupos.engine.indexconstruction.RDF3XIndexConstruction <datafile> <dataformat> <encoding> <directory for indices> [<datafile2> [<datafile3> ...]] ");
				return;
			}

			LiteralFactory
			.setType(LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
			Indices.setUsedDatastructure(DATA_STRUCT.DBBPTREE);

			final String datafile = args[0];
			final String dataFormat = args[1];
			CommonCoreQueryEvaluator.encoding = args[2];
			final String[] dir = new String[] { args[3] };
			final String writeindexinfo = dir[0]+"/"+RDF3XQueryEvaluator.INDICESINFOFILE;
			DBMergeSortedBag.setTmpDir(dir);
			DiskCollection.setTmpDir(dir);
			lupos.datastructures.paged_dbbptree.DBBPTree.setTmpDir(args[3],true);

			final Collection<URILiteral> defaultGraphs = new LinkedList<URILiteral>();
			final Collection<URILiteral> namedGraphs = new LinkedList<URILiteral>();
			defaultGraphs.add(LiteralFactory.createURILiteralWithoutLazyLiteral("<file:" + datafile+ ">"));
			
			for(int i=4; i<args.length; i++){
				defaultGraphs.add(LiteralFactory.createURILiteralWithoutLazyLiteral("<file:" + args[i]+ ">"));				
			}

			// Construct dictionary:

			final Thread codeMapConstructionThread = new Thread() {
				@Override
				public void run() {
						final DBMergeSortedSetUsingStringSearch rdftermsRepresentations = new DBMergeSortedSetUsingStringSearch(
								Indices.getHEAPHEIGHT(), String.class);

						final TripleConsumer tc = new TripleConsumer() {

							public void consume(final Triple triple) {
								for (final Literal l : triple) {
									// rdftermsRepresentations.add(l.
									// originalString());
									rdftermsRepresentations.add(l.toString());
									if (l.originalStringDiffers())
										rdftermsRepresentations.add(l
												.originalString());
								}
							}

						};
						for (final URILiteral u : defaultGraphs) {
							insertUsedStringRepresentations(u, dataFormat,
									rdftermsRepresentations, tc);
						}
						for (final URILiteral u : namedGraphs) {
							insertUsedStringRepresentations(u, dataFormat,
									rdftermsRepresentations, tc);
						}
						// now generate B+-tree for integer-string map and
						// string-integer
						// map of the codemap!
						final Generator<String, Integer> smsi = new Generator<String, Integer>() {

							public Iterator<java.util.Map.Entry<String, Integer>> iterator() {
								return new Iterator<java.util.Map.Entry<String, Integer>>() {
									Iterator<String> it = rdftermsRepresentations
									.iterator();
									int index = 1;

									public boolean hasNext() {
										return it.hasNext();
									}

									public java.util.Map.Entry<String, Integer> next() {
										if (!it.hasNext())
											return null;
										else {
											return new java.util.Map.Entry<String, Integer>() {
												String s = it.next();
												int localIndex = index++;

												public String getKey() {
													return s;
												}

												public Integer getValue() {
													return localIndex;
												}

												public Integer setValue(
														final Integer arg0) {
													throw new UnsupportedOperationException();
												}

											};
										}
									}

									public void remove() {
										throw new UnsupportedOperationException();
									}

								};
							}

							public int size() {
								return rdftermsRepresentations.size();
							}

						};
						final Generator<Integer, String> smis = new Generator<Integer, String>() {

							public Iterator<java.util.Map.Entry<Integer, String>> iterator() {
								return new Iterator<java.util.Map.Entry<Integer, String>>() {
									Iterator<String> it = rdftermsRepresentations
									.iterator();
									int index = 1;

									public boolean hasNext() {
										return it.hasNext();
									}

									public java.util.Map.Entry<Integer, String> next() {
										if (!it.hasNext())
											return null;
										else {
											return new java.util.Map.Entry<Integer, String>() {
												String s = it.next();
												int localIndex = index++;

												public Integer getKey() {
													return localIndex;
												}

												public String getValue() {
													return s;
												}

												public String setValue(
														final String arg0) {
													throw new UnsupportedOperationException();
												}

											};
										}
									}

									public void remove() {
										throw new UnsupportedOperationException();
									}

								};
							}

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
											new StandardNodeDeSerializer<String, Integer>(
													String.class, Integer.class));
									simap.generateDBBPTree(smsi);
									LazyLiteral.setHm(new StringIntegerMapJava(
											simap));
								} catch (final IOException e) {
									System.err.println(e);
									e.printStackTrace();
								}
							}
						};
						final Thread thread1 = new Thread() {
							@Override
							public void run() {
								lupos.datastructures.paged_dbbptree.DBBPTree<Integer, String> ismap;
								try {
									ismap = new lupos.datastructures.paged_dbbptree.DBBPTree<Integer, String>(
											k,
											k_,
											new StandardNodeDeSerializer<Integer, String>(
													Integer.class, String.class));
									ismap.generateDBBPTree(smis);
									LazyLiteral.setV(new IntegerStringMapJava(
											ismap));
								} catch (final IOException e) {
									System.err.println(e);
									e.printStackTrace();
								}
							}
						};
						thread0.start();
						thread1.start();
						try {
							thread0.join();
							thread1.join();
						} catch (final InterruptedException e) {
							System.err.println(e);
							e.printStackTrace();
						}
						rdftermsRepresentations.release();

				}
			};
			codeMapConstructionThread.start();

			final Indices indices = new SixIndices(defaultGraphs.iterator().next());

			try {
				codeMapConstructionThread.join();
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}

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

			new GenerateIDTriplesUsingStringSearch2(defaultGraphs, dataFormat,
					indices);
			
			// write out index info

			final LuposObjectOutputStream out = new LuposObjectOutputStream(
					new BufferedOutputStream(new FileOutputStream(
							writeindexinfo)));
			indices.constructCompletely();

			out.writeLuposInt(lupos.datastructures.paged_dbbptree.DBBPTree
					.getCurrentFileID());
			((lupos.datastructures.paged_dbbptree.DBBPTree) ((StringIntegerMapJava) LazyLiteral
					.getHm()).getOriginalMap()).writeLuposObject(out);
			((lupos.datastructures.paged_dbbptree.DBBPTree) ((IntegerStringMapJava) LazyLiteral
					.getV()).getOriginalMap()).writeLuposObject(out);
			out.writeLuposInt(1);
			LiteralFactory.writeLuposLiteral(defaultGraphs.iterator().next(), out);
			indices.writeIndexInfo(out);
			out.writeLuposInt(0);
			out.close();
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
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
		} else
			return "Error - no lazy literal";
	}

	protected static class GenerateIDTriplesUsingStringSearch2 {

		protected GenerateIDTriplesUsingStringSearch2(
				final Collection<URILiteral> graphURIs, final String dataFormat,
				final TripleConsumer tc) throws Exception {

			final SuperTrie searchtree = SuperTrie.createInstance();

			final DiskCollection<Triple> triples = new DiskCollection<Triple>(
					Triple.class);

			try {
				
				TripleConsumer tripleConsumer=new TripleConsumer() {
					
					public void consume(final Triple triple) {
						for (final Literal l : triple) {
							searchtree.add(l.toString());
							if (l.originalStringDiffers())
								searchtree.add(l.originalString());
						}
						triples.add(triple);
						if (searchtree.isFull()) {
							handleRun(searchtree, triples, tc);
						}
					}

				};
				
				for(URILiteral graphURI:graphURIs){
					CommonCoreQueryEvaluator.readTriples(dataFormat,
							graphURI.openStream(), tripleConsumer);
				}
				if (searchtree.size() > 0) {
					handleRun(searchtree, triples, tc);
					triples.release();
				}
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}

		private void handleRun(final SuperTrie searchtree,
				final Collection<Triple> triples, final TripleConsumer tc) {
			final int[] map = getMap(searchtree);

			for (final Triple triple : triples) {
				final Triple dummy = new Triple(triple.getPos(0),
						triple.getPos(1), triple.getPos(2));
				for (int pos = 0; pos < 3; pos++) {
					if (triple.getPos(pos).originalStringDiffers())
						dummy.setPos(
								pos,
								new LazyLiteralOriginalContent(
										map[searchtree.getIndex(triple.getPos(
												pos).toString())],
												map[searchtree.getIndex(triple.getPos(
														pos).originalString())]));
					else
						dummy.setPos(
								pos,
								new LazyLiteral(map[searchtree.getIndex(triple
										.getPos(pos).toString())]));
				}
				tc.consume(dummy);
			}

			// clear the searchtree and the triples collection for the next
			// "Run"
			searchtree.clear();
			triples.clear();
		}

		private int[] getMap(final SuperTrie searchtree) {
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
