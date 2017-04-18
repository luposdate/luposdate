package lupos.datastructures.lsmtree;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.disk.store.STRINGKEY;
import lupos.datastructures.lsmtree.level.disk.store.StoreKeyValue;
import lupos.datastructures.lsmtree.level.factory.DiskLevelFactory;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;
import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StandardNodeDeSerializer;
import lupos.datastructures.patriciatrie.TrieSet;
import lupos.datastructures.simplifiedfractaltree.SimplifiedFractalTree;
import lupos.datastructures.simplifiedfractaltree.StringKey;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.io.Registration;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Experiment compares lsm-tree to other data structures
 * for args[0] use lsm, dbbp, cola or trie as input
 * for args[1] use put, get or remove
 *
 * use *.ttl data files from
 * http://wiki.dbpedia.org/Downloads2015-10
 * and store files in folder data
 *
 * @author Maike Herting
 *
 */

public class Experiments {



	public static void main(final String[] args) throws FileNotFoundException, Exception {

		PropertyConfigurator.configure("log4j.properties");
		Logger.getRootLogger().setLevel(Level.OFF);
		final String type = "MULTIPLEN3";
		final String file = "data/all.txt";

		final String mode = (args.length > 0) ? args[0] : "lsm";
		final String operation = (args.length > 1) ? args[1] : "put";

		if (mode.compareTo("lsm") == 0) {
			// LSMTree
			final ILevelFactory<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>> testfactory = new DiskLevelFactory<String, Integer>(
					new StoreKeyValue<String, Integer>(String.class, Integer.class));
			final LSMTree<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>> lsm = new LSMTree<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>>(testfactory);
			CommonCoreQueryEvaluator.readTriples(type, new BufferedInputStream(new FileInputStream(file)),
					new TripleConsumer() {
						int counter = 0;
						long time;
						long sum;

						@Override
						public void consume(final Triple triple) {
							for (final Literal literal : triple) {
								this.counter++;
								final long start = System.nanoTime();

								try {
									lsm.put(literal.toString(), 1);
								} catch (ClassNotFoundException | IOException | URISyntaxException e) {
									System.err.println(e);
									e.printStackTrace();
								}

								final long end = System.nanoTime();

								this.time = (end - start);
								this.sum+= (end-start);
								if (operation.compareTo("put") == 0){
									System.out.println(this.counter + " " + this.time + " " +this.sum);
								}
							}
						}

					});

			if (operation.compareTo("get") == 0 || operation.compareTo("remove") == 0) {
				CommonCoreQueryEvaluator.readTriples(type, new BufferedInputStream(new FileInputStream(file)),
						new TripleConsumer() {
							int counter = 0;
							long time;
							long sum;

							@Override
							public void consume(final Triple triple) {
								for (final Literal literal : triple) {
									this.counter++;
									final long start = System.nanoTime();

									if (operation.compareTo("get") == 0) {
										try {
											lsm.get(literal.toString());
										} catch (ClassNotFoundException | IOException | URISyntaxException e) {
											System.err.println(e);
											e.printStackTrace();
										}
									} else if (operation.compareTo("remove") == 0) {
										try {
											lsm.remove(literal.toString());
										} catch (ClassNotFoundException | IOException | URISyntaxException e) {
											System.err.println(e);
											e.printStackTrace();
										}
									}

									final long end = System.nanoTime();

									this.time = (end - start);
									this.sum+= (end-start);

									System.out.println(this.counter + " " + this.time + " "+ this.sum);

								}
							}
						});

			}

		} else if (mode.compareTo("dbbp") == 0) {
			// B+Tree
			final DBBPTree<String, Integer> dbbptree = new lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer>(
					500, 500, new StandardNodeDeSerializer<String, Integer>(String.class, Integer.class));
			CommonCoreQueryEvaluator.readTriples(type, new BufferedInputStream(new FileInputStream(file)),
					new TripleConsumer() {
						int counter = 0;
						long time;
						long sum;

						@Override
						public void consume(final Triple triple) {
							for (final Literal literal : triple) {
								this.counter++;
								final long start = System.nanoTime();

								dbbptree.put(literal.toString(), 1);

								final long end = System.nanoTime();

								this.time = (end - start);
								this.sum+= (end-start);
								if (operation.compareTo("put") == 0){
									System.out.println(this.counter + " " + this.time + " " + this.sum);
								}
							}
						}

					});

			if (operation.compareTo("get") == 0 || operation.compareTo("remove") == 0) {
				CommonCoreQueryEvaluator.readTriples(type, new BufferedInputStream(new FileInputStream(file)),
						new TripleConsumer() {
							int counter = 0;
							long time;
							long sum;

							@Override
							public void consume(final Triple triple) {
								for (final Literal literal : triple) {
									this.counter++;
									final long start = System.nanoTime();

									if (operation.compareTo("get") == 0) {
										dbbptree.get(literal.toString());
									} else if (operation.compareTo("remove") == 0) {
										dbbptree.remove(literal.toString());
									}

									final long end = System.nanoTime();

									this.time = (end - start);
									this.sum+= (end-start);

									System.out.println(this.counter + " " + this.time + " " + this.sum);

								}
							}

						});
			}

		} else if (mode.compareTo("cola") == 0) {
			// COLA
			Registration.addDeSerializer(new STRINGKEY());
			final SimplifiedFractalTree<StringKey, Integer> cola = new SimplifiedFractalTree<StringKey, Integer>();
			//final SimplifiedFractalTree_Lazy<StringKey, Integer> cola = new SimplifiedFractalTree_Lazy<StringKey, Integer>();
			CommonCoreQueryEvaluator.readTriples(type, new BufferedInputStream(new FileInputStream(file)),
					new TripleConsumer() {
						int counter = 0;
						long time;
						long sum;

						@Override
						public void consume(final Triple triple) {
							for (final Literal literal : triple) {
								this.counter++;
								final long start = System.nanoTime();

								cola.put(new StringKey(literal.toString()), 1);

								final long end = System.nanoTime();
								this.time = (end - start);
								this.sum+= (end-start);
								if (operation.compareTo("put") == 0){
									System.out.println(this.counter + " " + this.time + " " + this.sum);
								}
							}
						}

					});
			if (operation.compareTo("get") == 0 || operation.compareTo("remove") == 0) {
				CommonCoreQueryEvaluator.readTriples(type, new BufferedInputStream(new FileInputStream(file)),
						new TripleConsumer() {
							int counter = 0;
							long time;
							long sum;

							@Override
							public void consume(final Triple triple) {
								for (final Literal literal : triple) {
									this.counter++;
									final long start = System.nanoTime();

									if (operation.compareTo("get") == 0) {
										cola.get(new StringKey(literal.toString()));
									} else if (operation.compareTo("remove") == 0) {
										cola.remove(new StringKey(literal.toString()), 1);
									}

									final long end = System.nanoTime();
									this.time = (end - start);
									this.sum+= (end-start);

									System.out.println(this.counter + " " + this.time + " " + this.sum);

								}
							}

						});
			}

		} else if (mode.compareTo("trie") == 0) {
			// PatriciaTrie
			//final TrieSet trie = TrieSet.createDiskBasedTrieSet("Trie");
			final TrieSet trie = TrieSet.createRamBasedTrieSet();
			CommonCoreQueryEvaluator.readTriples(type, new BufferedInputStream(new FileInputStream(file)),
					new TripleConsumer() {
						int counter = 0;
						long time;
						long sum;

						@Override
						public void consume(final Triple triple) {
							for (final Literal literal : triple) {
								this.counter++;
								final long start = System.nanoTime();

								trie.add(literal.toString());

								final long end = System.nanoTime();

								this.time = (end - start);
								// Summe:
								this.sum+= (end-start);
								if (operation.compareTo("put") == 0){
									System.out.println(this.counter + " " + this.time + " " + this.sum);
								}
							}
						}

					});
			if (operation.compareTo("get") == 0 || operation.compareTo("remove") == 0) {
				CommonCoreQueryEvaluator.readTriples(type, new BufferedInputStream(new FileInputStream(file)),
						new TripleConsumer() {
							int counter = 0;
							long time;
							long sum;

							@Override
							public void consume(final Triple triple) {
								for (final Literal literal : triple) {
									this.counter++;
									final long start = System.nanoTime();

									if (operation.compareTo("get") == 0) {
										trie.getIndex(literal.toString());
									} else if (operation.compareTo("remove") == 0) {
										trie.remove(literal.toString());
									}

									final long end = System.nanoTime();

									this.time = (end - start);
									this.sum+= (end-start);

									System.out.println(this.counter + " " + this.time + " " + this.sum);

								}
							}

						});
			}
		}
	}

}
