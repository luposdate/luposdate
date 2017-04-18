package lupos.engine.indexconstruction.implementation.dbbptree;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.items.IntArrayComparator;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.paged_dbbptree.IntArrayDBBPTreeStatistics;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.IntArrayNodeDeSerializer;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.NodeDeSerializer;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.indexconstruction.implementation.indices.DBBPTreeContainer;
import lupos.engine.indexconstruction.implementation.indices.IndicesWriter;
import lupos.engine.indexconstruction.implementation.sorter.IteratorFromRun;
import lupos.engine.indexconstruction.implementation.sorter.SecondaryConditionSorter;
import lupos.engine.indexconstruction.interfaces.IIndexContainer;
import lupos.engine.indexconstruction.interfaces.IIndicesGenerator;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.misc.Tuple;
import lupos.misc.util.ImmutableIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntArrayDBBPTreeIndicesGenerator implements IIndicesGenerator {

	private static final Logger log = LoggerFactory.getLogger(IntArrayDBBPTreeIndicesGenerator.class);

	private final String dir;
	private final Map<String, Object> configuration;
	private final List<Tuple<String, Long>> times;

	private volatile boolean allIndicesConstructed = false;

	public IntArrayDBBPTreeIndicesGenerator(final Map<String, Object> configuration, final List<Tuple<String, Long>> times) {
		this.configuration = configuration;
		this.dir = (String) configuration.get("dir");
		this.times = times;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void generateIndicesAndWriteOut(final int size) throws Exception {
		// generate indices (evaluation indices plus histogram indices)
		final long start = System.currentTimeMillis();
		final int numberOfOrders = CollationOrder.values().length;
		final DBBPTree<int[], int[]>[] evaluationIndices = new DBBPTree[numberOfOrders];
		final IntArrayDBBPTreeStatistics[] histogramIndices = new IntArrayDBBPTreeStatistics[numberOfOrders];
		final Thread[] threads = new Thread[numberOfOrders];

		int i=0;
		for(int primaryPos=0; primaryPos<3; primaryPos++) {
			final int finalPrimaryPos = primaryPos;
			final int other_condition1 = (primaryPos==0)?1:0;
			final int other_condition2 = (primaryPos==2)?1:2;
			final CollationOrder order1 = CollationOrder.valueOf(SecondaryConditionSorter.map[primaryPos] + SecondaryConditionSorter.map[other_condition1] + SecondaryConditionSorter.map[other_condition2]);
			final String prefixFilename = this.dir + SecondaryConditionSorter.map[primaryPos] + "_Final_Run_";
			final Comparator<int[]> comparator1 = new IntArrayComparator(order1);
			final int index1 = i;
			threads[i] = new Thread(){
				@Override
				public void run(){
					try{
						evaluationIndices[index1] = IntArrayDBBPTreeIndicesGenerator.generateEvaluationIndex(comparator1, order1, new GeneratorFromFinalRun(prefixFilename + SecondaryConditionSorter.map[other_condition1] + SecondaryConditionSorter.map[other_condition2], size, finalPrimaryPos, other_condition1, other_condition2));
						histogramIndices[index1] = IntArrayDBBPTreeIndicesGenerator.generateHistogramIndex(comparator1, order1, evaluationIndices[index1]);
					} catch(final IOException e){
						log.error(e.getMessage(), e);
					}
				}

			};
			threads[i].start();

			i++;
			final CollationOrder order2 = CollationOrder.valueOf(SecondaryConditionSorter.map[primaryPos] + SecondaryConditionSorter.map[other_condition2] + SecondaryConditionSorter.map[other_condition1]);
			final Comparator<int[]> comparator2 = new IntArrayComparator(order2);
			final int index2 = i;
			threads[i] = new Thread(){
				@Override
				public void run(){
					try{
						evaluationIndices[index2] = IntArrayDBBPTreeIndicesGenerator.generateEvaluationIndex(comparator2, order2, new GeneratorFromFinalRun(prefixFilename + SecondaryConditionSorter.map[other_condition2] + SecondaryConditionSorter.map[other_condition1], size, finalPrimaryPos, other_condition2, other_condition1));
						histogramIndices[index2] = IntArrayDBBPTreeIndicesGenerator.generateHistogramIndex(comparator2, order2, evaluationIndices[index2]);
					} catch(final IOException e){
						log.error(e.getMessage(), e);
					}
				}
			};
			threads[i].start();
			i++;
		}

		for(final Thread thread: threads){
			try {
				thread.join();
			} catch (final InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}

		while(!this.allIndicesConstructed){ // wait for the string array to be constructed
			try { // better would be with locks and notify-mechanism (but anyway the other thread should be already finished in nearly all cases)...
				Thread.sleep(200);
			} catch (final InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}

		final long end = System.currentTimeMillis();
		this.times.add(new Tuple<String, Long>("Materialize Evaluation Indices", end-start));

		final IIndexContainer<int[], int[]>[] indices = new IIndexContainer[evaluationIndices.length + histogramIndices.length];
		for(int j=0; j<evaluationIndices.length; j++){
			indices[j] = new DBBPTreeContainer<int[], int[]>(evaluationIndices[j]);
		}
		for(int j=0; j<histogramIndices.length; j++){
			indices[j+evaluationIndices.length] = new DBBPTreeContainer<int[], int[]>(histogramIndices[j]);
		}
		final IndicesWriter indicesWriter = new IndicesWriter(this.configuration, new DBBPTreeContainer<String, Integer>((lupos.datastructures.paged_dbbptree.DBBPTree) ((StringIntegerMapJava) LazyLiteral.getHm()).getOriginalMap()), (StringArray) LazyLiteral.getV(), indices, this.times);
		indicesWriter.writeOut();
	}

	 public static DBBPTree<int[], int[]> generateEvaluationIndex(final  Comparator<int[]> comparator, final CollationOrder order, final Generator<int[], int[]> generator) throws IOException {
     	final NodeDeSerializer<int[], int[]> nodeDeSerializer = new IntArrayNodeDeSerializer(order);
     	final DBBPTree<int[], int[]> tree = new DBBPTree<int[], int[]>(comparator, StringIntegerDBBPTreeDictionaryGenerator.k, StringIntegerDBBPTreeDictionaryGenerator.k_, nodeDeSerializer, int[].class, int[].class);
     	tree.generateDBBPTree(generator);
     	return tree;
	 }

	 public static IntArrayDBBPTreeStatistics generateHistogramIndex(final  Comparator<int[]> comparator, final CollationOrder order, final DBBPTree<int[], int[]> evaluationIndex) throws IOException {
		 final IntArrayDBBPTreeStatistics tree = new IntArrayDBBPTreeStatistics(comparator, StringIntegerDBBPTreeDictionaryGenerator.k, StringIntegerDBBPTreeDictionaryGenerator.k_, order);
		 tree.generateDBBPTree(evaluationIndex);
		 return tree;
	 }

	 /**
	  * This is just for generating B+-trees from the final run
	  */
	 public static class GeneratorFromFinalRun implements Generator<int[], int[]>{

		 private final String filename;
		 private final int size;
		 private int primaryMap;
		 private int secondaryMap;
		 private int tertiaryMap;

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
		 }

		 @Override
		 public int size() {
			 return this.size;
		 }

		 @Override
		 public Iterator<Entry<int[], int[]>> iterator() {
			 try {
				 return new ImmutableIterator<Entry<int[], int[]>>(){

					 IteratorFromRun it = new IteratorFromRun(GeneratorFromFinalRun.this.filename);
					 int[] triple = this.it.next();

					 @Override
					 public boolean hasNext() {
						 return this.triple!=null;
					 }

					 @Override
					 public Entry<int[], int[]> next() {
						 if(this.triple==null){
							 return null;
						 }
						 final int[] t = new int[]{
								 this.triple[GeneratorFromFinalRun.this.primaryMap],
								 this.triple[GeneratorFromFinalRun.this.secondaryMap],
								 this.triple[GeneratorFromFinalRun.this.tertiaryMap]};
						 try {
							 this.triple = this.it.next();
						 } catch (final IOException e) {
							 log.error(e.getMessage(), e);
							 this.triple = null;
						 }

						 return new MapEntry<int[], int[]>(t, t);
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
