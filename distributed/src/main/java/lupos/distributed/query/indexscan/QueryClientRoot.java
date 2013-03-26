package lupos.distributed.query.indexscan;

import java.util.Collection;

import lupos.datastructures.items.Item;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class QueryClientRoot extends Root {
	
	public QueryClientRoot(final Dataset dataset){
		super();
		this.dataset = dataset;
	}

	@Override
	public BasicIndexScan newIndexScan(OperatorIDTuple succeedingOperator,
			Collection<TriplePattern> triplePatterns, Item data) {
		return new QueryClientIndexScan(succeedingOperator, triplePatterns, data, this);
	}

	@Override
	public Root newInstance(Dataset dataset_param) {
		return new QueryClientRoot(dataset_param);
	}
}
