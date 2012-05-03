package lupos.rif.operator;

import java.util.Collection;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.debug.DebugStep;
import lupos.rdf.Prefix;

public abstract class BindableIndex extends BasicIndex {
	protected final BasicIndex index;
	protected Dataset dataSet;

	public BindableIndex(final BasicIndex index) {
		super(index.getIndexCollection());
		this.index = index;
	}

	@Override
	public QueryResult process(int opt, Dataset dataset) {
		dataSet = dataset;
		return null;
	}

	@Override
	public QueryResult process(QueryResult queryResult, int operandID) {
		final QueryResult result = QueryResult.createInstance();
		final Iterator<Bindings> bit = queryResult.oneTimeIterator();
		while (bit.hasNext()) {
			processIndexScan(result, bit.next());
		}
		return result;
	}
	
	@Override
	public QueryResult processDebug(int opt, Dataset dataset, DebugStep debugstep) {
		return process(opt, dataset);
	}

	protected abstract void processIndexScan(final QueryResult result,
			final Bindings bind);

	@Override
	public abstract Collection<TriplePattern> getTriplePattern();

	@Override
	public abstract Message preProcessMessage(BoundVariablesMessage msg);

	@Override
	public QueryResult join(Indices indices, Bindings bindings) {
		return null;
	}

	@Override
	public String toString() {
		return "Bindable - " + index.toString();
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		return "Bindable - " + index.toString(prefixInstance);
	}
}
