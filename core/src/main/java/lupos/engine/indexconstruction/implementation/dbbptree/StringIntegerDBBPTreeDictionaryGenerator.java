package lupos.engine.indexconstruction.implementation.dbbptree;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StringIntegerNodeDeSerializer;
import lupos.datastructures.patriciatrie.TrieSet;
import lupos.engine.indexconstruction.interfaces.IDictionaryGenerator;
import lupos.engine.indexconstruction.interfaces.IGlobalIDsGenerator;
import lupos.misc.Tuple;
import lupos.misc.util.ImmutableIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringIntegerDBBPTreeDictionaryGenerator implements IDictionaryGenerator {

	private static final Logger log = LoggerFactory.getLogger(StringIntegerDBBPTreeDictionaryGenerator.class);

	public static int k = 1000;
	public static int k_ = 1000;

	private Thread thread;
	private lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer> simap;
	private final IGlobalIDsGenerator globalIDsGenerator;
	private final List<Tuple<String, Long>> times;
	private long start;

	public StringIntegerDBBPTreeDictionaryGenerator(final IGlobalIDsGenerator globalIDsGenerator, final List<Tuple<String, Long>> times){
		this.globalIDsGenerator = globalIDsGenerator;
		this.times = times;
	}

	@Override
	public void generateDictionary(final TrieSet final_trie){
		this.start = System.currentTimeMillis();
		// create real dictionary
		final Generator<String, Integer> smsi = new Generator<String, Integer>() {

			@Override
			public Iterator<java.util.Map.Entry<String, Integer>> iterator() {
				return new ImmutableIterator<java.util.Map.Entry<String, Integer>>() {

					Iterator<String> it = final_trie.iterator();
					int index = 1;

					@Override
					public boolean hasNext() {
						return this.it.hasNext();
					}

					@Override
					public Entry<String, Integer> next() {
						return new MapEntry<String, Integer>(this.it.next(), this.index++);
					}
				};
			}

			@Override
			public int size() {
				return final_trie.size();
			}
		};

		try {
			this.simap = new lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer>(k, k_, new StringIntegerNodeDeSerializer());

			this.thread = new Thread() {
				@Override
				public void run() {
					try {
						StringIntegerDBBPTreeDictionaryGenerator.this.simap.generateDBBPTree(smsi);
						LazyLiteral.setHm(new StringIntegerMapJava(StringIntegerDBBPTreeDictionaryGenerator.this.simap));
					} catch (final IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			};

			// TODO make thread-safe!
			LiteralFactory.setTypeWithoutInitializing(LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
			this.thread.run();
		} catch (final IOException e1) {
			System.err.println(e1);
			e1.printStackTrace();
		}
	}

	@Override
	public void join(){
		try {
			this.thread.join();
			final long end = System.currentTimeMillis();
			this.times.add(new Tuple<String, Long>("Constructing Dictionary (String->Integer). Note: Parallel execution to constructing the other dictionary index!", end-this.start));
		} catch (final InterruptedException e) {
			log.error(e.getMessage(), e);
		}
		this.globalIDsGenerator.generateGlobalIDs(this.simap);
	}
}