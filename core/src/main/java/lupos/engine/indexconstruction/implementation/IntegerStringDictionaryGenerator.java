package lupos.engine.indexconstruction.implementation;

import java.io.IOException;
import java.util.List;

import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.patriciatrie.TrieSet;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.indexconstruction.interfaces.IDictionaryGenerator;
import lupos.engine.indexconstruction.interfaces.IIndicesGenerator;
import lupos.misc.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerStringDictionaryGenerator implements IDictionaryGenerator {

	private static final Logger log = LoggerFactory.getLogger(IntegerStringDictionaryGenerator.class);

	private final IIndicesGenerator indicesGenerator;
	private final List<Tuple<String, Long>> times;
	private long start;

	private Thread thread;


	public IntegerStringDictionaryGenerator(final IIndicesGenerator indicesGenerator, final List<Tuple<String, Long>> times){
		this.indicesGenerator = indicesGenerator;
		this.times = times;
	}

	@Override
	public void generateDictionary(final TrieSet final_trie){
		this.start = System.currentTimeMillis();
		this.thread = new Thread() {
			@Override
			public void run() {
				StringArray ismap;
				try {
					ismap = new StringArray();
					ismap.generate(final_trie.iterator());
					LazyLiteral.setV(ismap);
					IntegerStringDictionaryGenerator.this.indicesGenerator.notifyAllIndicesConstructed();
				} catch (final IOException e) {
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
			this.times.add(new Tuple<String, Long>("Constructing Dictionary (Integer->String). Note: Parallel execution to constructing the other dictionary index!", end-this.start));
		} catch (final InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}
}
