package lupos.engine.operators.singleinput.sparul;

import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.singleinput.SingleInputOperator;

public class Clear extends MultipleURIOperator {
	
	protected final Dataset dataset;
	protected final boolean isSilent;
	
	public Clear(final Dataset dataset, final boolean isSilent){
		super();
		this.dataset = dataset;
		this.isSilent = isSilent;
	}
	
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (cu == null) {
			// clear default graphs
			final Collection<Indices> ci = dataset.getDefaultGraphIndices();
			for (final Indices indices : ci) {
				indices.clear();
			}
		} else {
			for(URILiteral uri: cu){
				boolean deleted = false;
				Indices indices = dataset.getNamedGraphIndices(uri);
				if (indices != null){
					indices.clear();
					deleted = true;
				}
				indices = dataset.getDefaultGraphIndices(uri);
				if (indices != null){
					indices.clear();
					deleted = true;
				}
				if(!isSilent && !deleted)
					throw new Error("Graph "+ uri + "to be deleted does not exist!");
			}
		}
		return null;
	}
}
