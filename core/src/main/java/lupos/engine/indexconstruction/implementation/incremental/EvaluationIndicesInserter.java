package lupos.engine.indexconstruction.implementation.incremental;

import java.util.List;

import lupos.engine.indexconstruction.interfaces.IIndexContainer;
import lupos.engine.indexconstruction.interfaces.ISimpleConsumeTripleBlocks;
import lupos.misc.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluationIndicesInserter implements ISimpleConsumeTripleBlocks {

	private static final Logger log = LoggerFactory.getLogger(EvaluationIndicesInserter.class);

	private final IIndexContainer<int[], int[]>[] indexGenerators;
	private final List<Tuple<String, Long>> times;
	private long sum;

	public EvaluationIndicesInserter(final IIndexContainer<int[], int[]>[] indexGenerators, final List<Tuple<String, Long>> times){
		this.indexGenerators = indexGenerators;
		this.times = times;
	}

	@Override
	public void consumeTriplesBlocks(final int[][] triples, final int index) {
		final long start = System.currentTimeMillis();
		final Thread[] threads = new Thread[this.indexGenerators.length];
		for(int i=0; i<threads.length; i++){
			final int i_final = i;
			threads[i] = new Thread(){
				@Override
				public void run(){
					for(int j=0; j<index; j++){
						try {
							final int[] triple = triples[j];
							EvaluationIndicesInserter.this.indexGenerators[i_final].put(triple, triple);
						} catch (final Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			};
			threads[i].start();
		}
		for(final Thread thread: threads){
			try {
				thread.join();
			} catch (final InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
		final long end = System.currentTimeMillis();
		this.sum += end-start;
	}

	@Override
	public void notifyEndOfProcessing() {
		this.times.add(new Tuple<String, Long>("Total time for insertion triples into evaluation indices", this.sum));
	}
}
