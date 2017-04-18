package lupos.engine.indexconstruction.implementation.lsmtree;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.lsmtree.LSMTree;
import lupos.datastructures.lsmtree.LSMTreeAsStringIntegerMap;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.disk.store.StoreIntTriple.IntTripleComparator;
import lupos.datastructures.lsmtree.level.factory.DiskLevelFactory;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.indexconstruction.ConstructIndex;
import lupos.engine.indexconstruction.implementation.dbbptree.IntArrayDBBPTreeIndicesGenerator.GeneratorFromFinalRun;
import lupos.engine.indexconstruction.implementation.indices.IndicesWriter;
import lupos.engine.indexconstruction.implementation.indices.LSMTreeContainer;
import lupos.engine.indexconstruction.implementation.sorter.SecondaryConditionSorter;
import lupos.engine.indexconstruction.interfaces.IIndexContainer;
import lupos.engine.indexconstruction.interfaces.IIndicesGenerator;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.misc.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LSMTreeIndicesGenerator implements IIndicesGenerator {

	private static final Logger log = LoggerFactory.getLogger(LSMTreeIndicesGenerator.class);

	private final String dir;
	private final Map<String, Object> configuration;
	private final List<Tuple<String, Long>> times;

	private volatile boolean allIndicesConstructed = false;


	public LSMTreeIndicesGenerator(final Map<String, Object> configuration, final List<Tuple<String, Long>> times) {
		this.configuration = configuration;
		this.dir = (String) configuration.get("dir");
		this.times = times;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public void generateIndicesAndWriteOut(final int size) throws Exception {
		// generate indices (evaluation indices plus histogram indices)
		final long start = System.currentTimeMillis();
		final int numberOfOrders = CollationOrder.values().length;
		final LSMTree<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>>[] evaluationIndices = new LSMTree[numberOfOrders];
		final Thread[] threads = new Thread[numberOfOrders];

		int i=0;
		for(int primaryPos=0; primaryPos<3; primaryPos++) {
			final int finalPrimaryPos = primaryPos;
			final int other_condition1 = (primaryPos==0)?1:0;
			final int other_condition2 = (primaryPos==2)?1:2;
			final CollationOrder order1 = CollationOrder.valueOf(SecondaryConditionSorter.map[primaryPos] + SecondaryConditionSorter.map[other_condition1] + SecondaryConditionSorter.map[other_condition2]);
			final String prefixFilename = this.dir + SecondaryConditionSorter.map[primaryPos] + "_Final_Run_";
			final int index1 = i;
			final String dir1 = this.dir + File.separator + "lsm-tree" + File.separator + order1.toString() + File.separator;
			threads[i] = new Thread(){
				@Override
				public void run(){
					try{
						evaluationIndices[index1] = LSMTreeIndicesGenerator.generateEvaluationIndex(dir1, order1, new GeneratorFromFinalRun(prefixFilename + SecondaryConditionSorter.map[other_condition1] + SecondaryConditionSorter.map[other_condition2], size, finalPrimaryPos, other_condition1, other_condition2));
					} catch(final IOException | ClassNotFoundException | URISyntaxException e){
						log.error(e.getMessage(), e);
					}
				}

			};
			threads[i].start();

			i++;
			final CollationOrder order2 = CollationOrder.valueOf(SecondaryConditionSorter.map[primaryPos] + SecondaryConditionSorter.map[other_condition2] + SecondaryConditionSorter.map[other_condition1]);
			final int index2 = i;
			final String dir2 = this.dir + File.separator + "lsm-tree" + File.separator + order2.toString() + File.separator;
			threads[i] = new Thread(){
				@Override
				public void run(){
					try{
						evaluationIndices[index2] = LSMTreeIndicesGenerator.generateEvaluationIndex(dir2, order2, new GeneratorFromFinalRun(prefixFilename + SecondaryConditionSorter.map[other_condition2] + SecondaryConditionSorter.map[other_condition1], size, finalPrimaryPos, other_condition2, other_condition1));
					} catch(final IOException | ClassNotFoundException | URISyntaxException e){
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
		final IIndexContainer<int[], int[]>[] indices = new IIndexContainer[evaluationIndices.length];
		for(int j=0; j<evaluationIndices.length; j++){
			indices[j] = new LSMTreeContainer<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>>(evaluationIndices[j]);
		}
		final IndicesWriter indicesWriter = new IndicesWriter(this.configuration, new LSMTreeContainer<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>>(((LSMTreeAsStringIntegerMap) LazyLiteral.getHm()).getLSMTree()), (StringArray) LazyLiteral.getV(), indices, this.times);
		indicesWriter.writeOut();
	}

	 public static LSMTree<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>> generateEvaluationIndex(final String dir, final CollationOrder order, final Generator<int[], int[]> generator) throws ClassNotFoundException, IOException, URISyntaxException {
		final ILevelFactory<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>> lsmtreefactory = new DiskLevelFactory<int[], int[]>(new IntTripleComparator(order), ConstructIndex.createStoreIntArray(order), dir, StringIntegerLSMTreeDictionaryGenerator.m, StringIntegerLSMTreeDictionaryGenerator.k, ConstructIndex.createMemoryLevelFactory());
		final LSMTree<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>> lsmtree=new LSMTree<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>>(order.name(), lsmtreefactory);
		lsmtree.addRun(generator);
		return lsmtree;
	 }

	@Override
	public void notifyAllIndicesConstructed() {
		this.allIndicesConstructed = true;
	}
}
