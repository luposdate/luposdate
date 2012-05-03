package lupos.sparql1_1.operatorgraph.helper;

import java.util.Collection;

import lupos.datastructures.items.Item;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.tripleoperator.TriplePattern;

public interface IndexScanCreatorInterface {
	public BasicOperator createIndexScanAndConnectWithRoot(OperatorIDTuple opID, Collection<TriplePattern> triplePatterns, Item graphConstraint);
	public void createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(OperatorIDTuple opID, Item graphConstraint);
	public void createEmptyIndexScanAndConnectWithRoot(OperatorIDTuple opID);
	public BasicOperator getRoot();
	
	// not supported by every evaluator!
	public Dataset getDataset();
	public void addDefaultGraph(String defaultgraph);
	public void addNamedGraph(String namedgraph);
}
