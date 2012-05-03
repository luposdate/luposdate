package lupos.rif.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.IndexCollection;
import lupos.engine.operators.index.Indices;
import lupos.misc.debug.DebugStep;

public class InitializeDatasetIndex extends BasicIndex {
	private final Collection<BindableIndex> listenerIndexes = new ArrayList<BindableIndex>();

	public InitializeDatasetIndex(IndexCollection indexCollection) {
		super(indexCollection);
		triplePatterns = Arrays.asList();
	}

	public void addBindableIndex(final BindableIndex index) {
		listenerIndexes.add(index);
	}

	public boolean isEmpty() {
		return listenerIndexes.isEmpty();
	}

	@Override
	public QueryResult process(int opt, Dataset dataset) {
		for (final BindableIndex index : listenerIndexes)
			index.process(0, dataset);
		return null;
	}
	
	public QueryResult processDebug(final int opt, final Dataset dataset,
			final DebugStep debugstep) {
		for (final BindableIndex index : listenerIndexes)
			index.processDebug(0, dataset, debugstep);
		return null;		
	}


	@Override
	public QueryResult join(Indices indices, Bindings bindings) {
		return null;
	}

	@Override
	public String toString() {
		return "DataSetIndex";
	}

}
