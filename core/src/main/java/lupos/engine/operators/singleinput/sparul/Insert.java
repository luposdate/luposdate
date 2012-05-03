package lupos.engine.operators.singleinput.sparul;

import java.util.Collection;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.string.StringURILiteral;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;

public class Insert extends MultipleURIOperator {

	protected final Dataset dataset;

	public Insert(final Collection<URILiteral> cu, Dataset dataset) {
		super(cu);
		this.dataset=dataset;
	}

	@Override
	public QueryResult process(QueryResult bindings, final int operandID) {
		if(bindings instanceof QueryResultDebug)
			bindings=((QueryResultDebug)bindings).getOriginalQueryResult();
		if (bindings instanceof GraphResult) {
			final GraphResult gr = (GraphResult) bindings;
			for (final Triple t : gr.getGraphResultTriples()) {
				if (cu == null || cu.size() == 0) {
					final Collection<Indices> ci = this.dataset.getDefaultGraphIndices();
					for (final Indices indices : ci) {
						if (!indices.contains(t)) {
							indices.add(t);
						}
					}
				} else {
					for (final URILiteral uri : cu) {
						boolean flag = false;
						Indices indices = this.dataset.getNamedGraphIndices(uri);
						if (indices != null){
							flag = true;
							if (!indices.contains(t)) {
								indices.add(t);
							}
						}
						indices = this.dataset.getDefaultGraphIndices(uri);
						if (indices != null){
							flag = true;
							if (!indices.contains(t)) {
								indices.add(t);
							}
						}
						if(!flag){
							try {
								this.dataset.addNamedGraph(uri, new StringURILiteral("<inlinedata:"+t.getSubject()+" "+t.getPredicate()+" "+t.getObject()+".>"), false, false);
							} catch (Exception e) {
								System.err.println(e);
								e.printStackTrace();
							}
						}
					}
				}
			}
		} else
			System.err.println("GraphResult expected instead of " + bindings.getClass());
		this.dataset.buildCompletelyAllIndices();
		return null;
	}
}
