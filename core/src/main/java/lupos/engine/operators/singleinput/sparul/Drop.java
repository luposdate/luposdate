package lupos.engine.operators.singleinput.sparul;

import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;

public class Drop extends MultipleURIOperator {

	final boolean isSilent;
	final Dataset dataset;
	
	public Drop(Dataset dataset, final boolean isSilent) {
		this.isSilent=isSilent;
		this.dataset=dataset;
	}
	
	public QueryResult process(QueryResult bindings, final int operandID) {
		for(URILiteral uri: cu){
			Indices indices = dataset.getNamedGraphIndices(uri);
			if (indices == null){
				indices = dataset.getDefaultGraphIndices(uri);
				if (indices == null){
					if(isSilent) return null;
					else throw new Error("Graph "+uri+" does not exist");
				}
			}
			dataset.removeNamedGraphIndices(uri);
			dataset.removeDefaultGraphIndices(uri);
		}
		return null;
	}	
}
