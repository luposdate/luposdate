package lupos.distributed.query;

import java.util.Collection;
import java.util.Date;

import lupos.datastructures.items.literal.URILiteral;
import lupos.distributed.query.indexscan.QueryClientIndices;
import lupos.distributed.query.indexscan.QueryClientRoot;
import lupos.engine.evaluators.BasicIndexQueryEvaluator;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Root;
import lupos.misc.Tuple;

public class QueryClient extends BasicIndexQueryEvaluator {

	public QueryClient() throws Exception {
		super();
	}
	
	public QueryClient(final String[] args) throws Exception {
		super(args);
	}

	@Override
	public Root createRoot() {
		return new QueryClientRoot(this.dataset);
	}
	
	@Override
	public long prepareInputData(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs) throws Exception {
		final Date a = new Date();
		super.prepareInputData(defaultGraphs, namedGraphs);
		this.dataset = new Dataset(defaultGraphs, namedGraphs, this.type,
				getMaterializeOntology(), this.opt, new Dataset.IndicesFactory() {
			@Override
			public Indices createIndices(final URILiteral uriLiteral) {
				return new QueryClientIndices(uriLiteral);
			}

			@Override
			public lupos.engine.operators.index.Root createRoot() {
				QueryClientRoot ic=new QueryClientRoot(QueryClient.this.dataset);
				return ic;
			}
		}, this.debug != DEBUG.NONE, this.inmemoryexternalontologyinference);
		this.dataset.buildCompletelyAllIndices();
		final long prepareTime = ((new Date()).getTime() - a.getTime());
		return prepareTime;
	}
	
	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			Collection<URILiteral> defaultGraphs,
			Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		final Date a = new Date();
		super.prepareInputDataWithSourcesOfNamedGraphs(defaultGraphs, namedGraphs);
		this.dataset = new Dataset(defaultGraphs, namedGraphs,
				getMaterializeOntology(), this.type, this.opt, new Dataset.IndicesFactory() {
			@Override
			public Indices createIndices(final URILiteral uriLiteral) {
				return new QueryClientIndices(uriLiteral);
			}

			@Override
			public lupos.engine.operators.index.Root createRoot() {
				QueryClientRoot ic=new QueryClientRoot(QueryClient.this.dataset);
				return ic;
			}
		}, this.debug != DEBUG.NONE, this.inmemoryexternalontologyinference);
		this.dataset.buildCompletelyAllIndices();
		final long prepareTime = ((new Date()).getTime() - a.getTime());
		return prepareTime;
	}
}
