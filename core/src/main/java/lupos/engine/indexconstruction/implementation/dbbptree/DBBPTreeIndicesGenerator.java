package lupos.engine.indexconstruction.implementation.dbbptree;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.buffermanager.BufferManager;
import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.indexconstruction.implementation.sorter.IteratorFromRun;
import lupos.engine.indexconstruction.implementation.sorter.SecondaryConditionSorter;
import lupos.engine.indexconstruction.interfaces.IIndicesGenerator;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.engine.operators.index.adaptedRDF3X.SixIndices;
import lupos.io.helper.OutHelper;
import lupos.misc.Tuple;
import lupos.misc.util.ImmutableIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBBPTreeIndicesGenerator implements IIndicesGenerator {

	private static final Logger log = LoggerFactory.getLogger(DBBPTreeIndicesGenerator.class);

	private final String dir;
	private final String writeindexinfo;
	private URILiteral defaultGraph;
	private final List<Tuple<String, Long>> times;

	private volatile boolean allIndicesConstructed = false;


	public DBBPTreeIndicesGenerator(final Map<String, Object> configuration, final List<Tuple<String, Long>> times) {
		this.dir = (String) configuration.get("dir");
		this.writeindexinfo = (String) configuration.get("writeindexinfo");
		final String firstfile = ((String[])configuration.get("files"))[0];
		try {
			this.defaultGraph = LiteralFactory.createURILiteralWithoutLazyLiteral("<file:" + firstfile+ ">");
		} catch (final URISyntaxException e) {
			log.error(e.getMessage(), e);
			this.defaultGraph = null;
		}
		this.times = times;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void generateIndicesAndWriteOut(final int size) throws IOException{
		// generate indices (evaluation indices plus histogram indices)
		final long start = System.currentTimeMillis();
		final SixIndices indices = new SixIndices(this.defaultGraph);
		final Thread[] threads = new Thread[6];
		int index = 0;
		for(int primaryPos=0; primaryPos<3; primaryPos++) {
			final int other_condition1 = (primaryPos==0)?1:0;
			final int other_condition2 = (primaryPos==2)?1:2;
			final CollationOrder order1 = CollationOrder.valueOf(SecondaryConditionSorter.map[primaryPos] + SecondaryConditionSorter.map[other_condition1] + SecondaryConditionSorter.map[other_condition2]);
			final String prefixFilename = this.dir + SecondaryConditionSorter.map[primaryPos] + "_Final_Run_";
			final int primaryPosFixed = primaryPos;
			threads[index] = new Thread(){
				@Override
				public void run(){
					try {
						indices.generate(order1, new GeneratorFromFinalRun(prefixFilename + SecondaryConditionSorter.map[other_condition1] + SecondaryConditionSorter.map[other_condition2], size, primaryPosFixed, other_condition1, other_condition2));
						indices.generateStatistics(order1);
					} catch (final IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			};
			threads[index].start();
			index++;

			final CollationOrder order2 = CollationOrder.valueOf(SecondaryConditionSorter.map[primaryPos] + SecondaryConditionSorter.map[other_condition2] + SecondaryConditionSorter.map[other_condition1]);
			threads[index] = new Thread(){
				@Override
				public void run(){
					try {
						indices.generate(order2, new GeneratorFromFinalRun(prefixFilename + SecondaryConditionSorter.map[other_condition2] + SecondaryConditionSorter.map[other_condition1], size, primaryPosFixed, other_condition2, other_condition1));
						indices.generateStatistics(order2);
					} catch (final IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			};
			threads[index].start();
			index++;
		}

		for(final Thread thread: threads){
			try {
				thread.join();
			} catch (final InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}

		indices.constructCompletely();

		// write out index info

		final OutputStream out = new BufferedOutputStream(new FileOutputStream(this.writeindexinfo));

		BufferManager.getBufferManager().writeAllModifiedPages();

		OutHelper.writeLuposInt(lupos.datastructures.paged_dbbptree.DBBPTree.getCurrentFileID(), out);

		((lupos.datastructures.paged_dbbptree.DBBPTree) ((StringIntegerMapJava) LazyLiteral.getHm()).getOriginalMap()).writeLuposObject(out);

		while(!this.allIndicesConstructed){ // wait for the string array to be constructed
			try { // better would be with locks and notify-mechanism (but anyway the other thread should be already finished in nearly all cases)...
				Thread.sleep(200);
			} catch (final InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}

		((StringArray) LazyLiteral.getV()).writeLuposStringArray(out);
		OutHelper.writeLuposInt(1, out);
		LiteralFactory.writeLuposLiteral(this.defaultGraph, out);
		indices.writeIndexInfo(out);
		OutHelper.writeLuposInt(0, out);
		out.close();
		final long end = System.currentTimeMillis();
		this.times.add(new Tuple<String, Long>("Materialize Evaluation Indices", end-start));
	}

	/**
	 * This is just for generating B+-trees from the final run
	 */
	public static class GeneratorFromFinalRun implements Generator<TripleKey, Triple>{

		private final String filename;
		private final int size;
		private int primaryMap;
		private int secondaryMap;
		private int tertiaryMap;
		private final CollationOrder order;

		public GeneratorFromFinalRun(final String filename, final int size, final int primaryPos, final int secondaryPos, final int tertiaryPos) {
			this.filename = filename;
			this.size = size;
			switch(primaryPos){
				default:
				case 0:
					this.primaryMap = 0;
					break;
				case 1:
					this.secondaryMap = 0;
					break;
				case 2:
					this.tertiaryMap = 0;
					break;
			}
			switch(secondaryPos){
				default:
				case 0:
					this.primaryMap = 1;
					break;
				case 1:
					this.secondaryMap = 1;
					break;
				case 2:
					this.tertiaryMap = 1;
					break;
			}
			switch(tertiaryPos){
				default:
				case 0:
					this.primaryMap = 2;
					break;
				case 1:
					this.secondaryMap = 2;
					break;
				case 2:
					this.tertiaryMap = 2;
					break;
			}
			this.order = CollationOrder.createCollationOrder(primaryPos, secondaryPos);
		}

		@Override
		public int size() {
			return this.size;
		}

		@Override
		public Iterator<Entry<TripleKey, Triple>> iterator() {
			try {
				return new ImmutableIterator<Entry<TripleKey, Triple>>(){

					IteratorFromRun it = new IteratorFromRun(GeneratorFromFinalRun.this.filename);
					int[] triple = this.it.next();

					@Override
					public boolean hasNext() {
						return this.triple!=null;
					}

					@Override
					public Entry<TripleKey, Triple> next() {
						if(this.triple==null){
							return null;
						}
						final Triple t = new Triple(new LazyLiteral(this.triple[GeneratorFromFinalRun.this.primaryMap]),
								new LazyLiteral(this.triple[GeneratorFromFinalRun.this.secondaryMap]),
								new LazyLiteral(this.triple[GeneratorFromFinalRun.this.tertiaryMap]));

						final TripleKey key = new TripleKey(t, GeneratorFromFinalRun.this.order);

						try {
							this.triple = this.it.next();

						} catch (final IOException e) {
							log.error(e.getMessage(), e);
							this.triple = null;
						}

						return new MapEntry<TripleKey, Triple>(key, t);
					}
				};
			} catch (final IOException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}
	}

	@Override
	public void notifyAllIndicesConstructed() {
		this.allIndicesConstructed = true;
	}
}
