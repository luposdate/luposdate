package lupos.engine.operators.singleinput.sparul;

import java.net.URISyntaxException;

import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.string.StringURILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.singleinput.SingleInputOperator;

public class Create extends SingleInputOperator {

	protected final boolean silent;
	
	protected URILiteral uri;
	protected final Dataset dataset;
	
	public Create(Dataset dataset, final boolean isSilent){
		this.dataset=dataset;
		this.silent=isSilent;
	}
	
	public void setURI(URILiteral uri){
		this.uri=uri;
	}
	
	public URILiteral getURI(){
		return uri;
	}

	public QueryResult process(QueryResult bindings, final int operandID) {
		Indices indices = dataset.getNamedGraphIndices(uri);
		if (indices != null){
			if(silent) return null;
			else throw new Error("Named Graph "+uri+" already exists");
		}
//		indices = dataset.getDefaultGraphIndices(uri);
//		if (indices != null && !silent)
//			throw new Error("Default Graph "+uri+" already exists");
		try {
			dataset.addNamedGraph(uri, new StringURILiteral("<inlinedata: >"), false, false);
		} catch (URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}		
		return null;
	}
}
