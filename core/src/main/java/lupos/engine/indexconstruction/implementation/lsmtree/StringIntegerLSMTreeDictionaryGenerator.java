package lupos.engine.indexconstruction.implementation.lsmtree;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.lsmtree.LSMTree;
import lupos.datastructures.lsmtree.LSMTreeAsStringIntegerMap;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.disk.store.StoreKeyValue;
import lupos.datastructures.lsmtree.level.factory.DiskLevelFactory;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.patriciatrie.TrieSet;
import lupos.engine.indexconstruction.ConstructIndex;
import lupos.engine.indexconstruction.interfaces.IDictionaryGenerator;
import lupos.engine.indexconstruction.interfaces.IGlobalIDsGenerator;
import lupos.misc.Tuple;
import lupos.misc.util.ImmutableIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringIntegerLSMTreeDictionaryGenerator implements IDictionaryGenerator {

	private static final Logger log = LoggerFactory.getLogger(StringIntegerLSMTreeDictionaryGenerator.class);

	public static int m = 512;
	public static int k = 32;

	private Thread thread;
	private LSMTree<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>> simap;
	private final String dir;
	private final IGlobalIDsGenerator globalIDsGenerator;
	private final List<Tuple<String, Long>> times;
	private long start;

	public StringIntegerLSMTreeDictionaryGenerator(final Map<String, Object> configuration, final IGlobalIDsGenerator globalIDsGenerator, final List<Tuple<String, Long>> times){
		this.globalIDsGenerator = globalIDsGenerator;
		this.times = times;
		this.dir = (String) configuration.get("dir") + File.separator + "lsm-tree" + File.separator + "dict" + File.separator;
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

		this.simap = new LSMTree<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>>("Dictionary", new DiskLevelFactory<String, Integer>(Comparator.<String>naturalOrder(), new StoreKeyValue<String, Integer>(String.class, Integer.class), this.dir, m, k, ConstructIndex.createMemoryLevelFactory()));

		this.thread = new Thread() {
			@Override
			public void run() {
				try {
					StringIntegerLSMTreeDictionaryGenerator.this.simap.addRun(smsi);
					LazyLiteral.setHm(new LSMTreeAsStringIntegerMap(StringIntegerLSMTreeDictionaryGenerator.this.simap, smsi.size()));
				} catch (final IOException | ClassNotFoundException | URISyntaxException e) {
					log.error(e.getMessage(), e);
				}
			}
		};

		// TODO make thread-safe!
		LiteralFactory.setTypeWithoutInitializing(LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
		this.thread.run();
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