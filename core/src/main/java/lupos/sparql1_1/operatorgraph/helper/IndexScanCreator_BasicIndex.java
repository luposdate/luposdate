package lupos.sparql1_1.operatorgraph.helper;

import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.items.Item;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.EmptyIndex;
import lupos.engine.operators.index.EmptyIndexSubmittingQueryResultWithOneEmptyBindings;
import lupos.engine.operators.index.IndexCollection;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class IndexScanCreator_BasicIndex implements IndexScanCreatorInterface {
	
	protected final IndexCollection indexCollection;
	
	public IndexScanCreator_BasicIndex(final IndexCollection indexCollection){
		this.indexCollection = indexCollection;
	}

	public IndexCollection getIndexCollection(){
		return this.indexCollection;
	}
	
	@Override
	public BasicOperator getRoot() {
		return this.indexCollection;
	}

	@Override
	public BasicOperator createIndexScanAndConnectWithRoot(
			OperatorIDTuple opID, Collection<TriplePattern> triplePatterns,
			Item graphConstraint) {
		final lupos.engine.operators.index.BasicIndex index = indexCollection.newIndex(opID, triplePatterns, graphConstraint);
		indexCollection.getSucceedingOperators().add(new OperatorIDTuple(index, 0));
		return index;
	}

	@Override
	public void createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(OperatorIDTuple opID, Item graphConstraint) {
		indexCollection.getSucceedingOperators().add(new OperatorIDTuple(new EmptyIndexSubmittingQueryResultWithOneEmptyBindings(opID, null, graphConstraint, indexCollection), 0));
	}

	@Override
	public void createEmptyIndexScanAndConnectWithRoot(OperatorIDTuple opID) {
		indexCollection.getSucceedingOperators().add(new OperatorIDTuple(new EmptyIndex(opID, null, indexCollection), 0));
	}

	@Override
	public Dataset getDataset() {
		return indexCollection.dataset;
	}

	@Override
	public void addDefaultGraph(String defaultgraph) {
		if (indexCollection.defaultGraphs == null)
			indexCollection.defaultGraphs = new LinkedList<String>();
		indexCollection.defaultGraphs.add(defaultgraph);					
	}

	@Override
	public void addNamedGraph(String namedgraph) {
		if (indexCollection.namedGraphs == null)
			indexCollection.namedGraphs = new LinkedList<String>();
		indexCollection.namedGraphs.add(namedgraph);					
	}
}
