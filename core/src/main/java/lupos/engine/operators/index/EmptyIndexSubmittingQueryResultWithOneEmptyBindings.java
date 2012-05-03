package lupos.engine.operators.index;

import java.net.URISyntaxException;
import java.util.Collection;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.rdf.Prefix;

public class EmptyIndexSubmittingQueryResultWithOneEmptyBindings extends EmptyIndex {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6813056199050211285L;

	public EmptyIndexSubmittingQueryResultWithOneEmptyBindings(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, Item graphConstraint,
			final lupos.engine.operators.index.IndexCollection indexCollection) {
		super(succeedingOperator, triplePattern, indexCollection);
		this.rdfGraph = graphConstraint;
	}

	/**
	 * Creating a new query result with an empty binding to handle an empty BIND
	 * statement
	 * 
	 * @param int
	 * @param Dataset
	 */
	@Override
	public QueryResult process(final int opt, final Dataset dataset) {
		final QueryResult queryResult = QueryResult.createInstance();
		if(this.rdfGraph!=null && this.rdfGraph.isVariable()){
			Variable graphConstraint = (Variable) this.rdfGraph;
			if (indexCollection.namedGraphs != null && indexCollection.namedGraphs.size() > 0) {
				// Convert the named graphs' names into URILiterals
				// to be applicable later on
				for (final String name : indexCollection.namedGraphs) {
					final Bindings graphConstraintBindings = Bindings.createNewInstance();
					try {
						graphConstraintBindings.add(graphConstraint, LiteralFactory.createURILiteralWithoutLazyLiteral(name));
					} catch (URISyntaxException e) {
						System.err.println(e);
						e.printStackTrace();
					}
					queryResult.add(graphConstraintBindings);
				}
				} else {
					final Collection<Indices> dataSetIndices = dataset.getNamedGraphIndices();
					if (dataSetIndices != null) {
						for (final Indices indices : dataSetIndices) {
							final Bindings graphConstraintBindings = Bindings.createNewInstance();
							graphConstraintBindings.add(graphConstraint, indices.getRdfName());
							queryResult.add(graphConstraintBindings);
						}
					}
				}
		} else {
			queryResult.add(Bindings.createNewInstance());
		}

		for (final OperatorIDTuple succOperator : succeedingOperators) {

			((Operator) succOperator.getOperator()).processAll(queryResult,
					succOperator.getId());
		}
		return queryResult;
	}

	@Override
	public String toString() {
		return super.toString()+"\nReturning queryResult with one empty bindings";
	}

	@Override
	public String toString(Prefix prefix) {
		return super.toString(prefix)+"\nReturning queryResult with one empty bindings";
	}	
}
