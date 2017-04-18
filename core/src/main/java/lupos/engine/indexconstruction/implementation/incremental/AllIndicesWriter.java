package lupos.engine.indexconstruction.implementation.incremental;

import java.util.List;
import java.util.Map;

import lupos.datastructures.stringarray.StringArray;
import lupos.engine.indexconstruction.implementation.indices.IndicesWriter;
import lupos.engine.indexconstruction.interfaces.IEndOfProcessingNotification;
import lupos.engine.indexconstruction.interfaces.IIndexContainer;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.misc.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllIndicesWriter implements IEndOfProcessingNotification {

	private static final Logger log = LoggerFactory.getLogger(AllIndicesWriter.class);

	private final Map<String, Object> configuration;
	private final IIndexContainer<String, Integer> dictionary;
	private final StringArray stringArray;
	private final IIndexContainer<int[], int[]>[] evaluationIndices;
	private final List<Tuple<String, Long>> times;

	public AllIndicesWriter(final Map<String, Object> configuration, final IIndexContainer<String, Integer> dictionary, final StringArray stringArray, final IIndexContainer<int[], int[]>[] evaluationIndices, final List<Tuple<String, Long>> times){
		this.configuration = configuration;
		this.dictionary = dictionary;
		this.stringArray = stringArray;
		this.evaluationIndices = evaluationIndices;
		this.times = times;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void notifyEndOfProcessing() {

		final IIndexContainer<int[], int[]>[] indices;
		if(this.evaluationIndices[0].createsHistogramIndex()){
			final long start = System.currentTimeMillis();
			indices = new IIndexContainer[this.evaluationIndices.length*2];

			final Thread[] threads = new Thread[this.evaluationIndices.length];

			for(int i=0; i<this.evaluationIndices.length; i++){
				final int i_final = i;
				threads[i] = new Thread(){
					@Override
					public void run(){
						indices[i_final] = AllIndicesWriter.this.evaluationIndices[i_final];
						try {
							indices[i_final+AllIndicesWriter.this.evaluationIndices.length] = AllIndicesWriter.this.evaluationIndices[i_final].createHistogramIndex(CollationOrder.values()[i_final]);
						} catch (final Exception e) {
							log.error(e.getMessage(), e);
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
			this.times.add(new Tuple<String, Long>("Materialize Histogram Indices", end-start));
		} else {
			indices = this.evaluationIndices;
		}

		final IndicesWriter indicesWriter = new IndicesWriter(this.configuration, this.dictionary, this.stringArray, indices, this.times);
		try {
			indicesWriter.writeOut();
		} catch (final Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
