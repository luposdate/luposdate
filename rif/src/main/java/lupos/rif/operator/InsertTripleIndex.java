package lupos.rif.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.IndexCollection;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.singleinput.sparul.Insert;
import lupos.engine.operators.stream.TripleDeleter;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleConsumerDebug;
import lupos.misc.debug.DebugStep;
import lupos.rdf.Prefix;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreator_BasicIndex;

public class InsertTripleIndex extends BasicIndex implements TripleConsumer, TripleConsumerDebug, TripleDeleter {
	final protected Set<Triple> facts = new HashSet<Triple>();

	public InsertTripleIndex(IndexScanCreatorInterface indexScanCreator) {
		super(null);		
		triplePatterns = Arrays.asList();
		if(indexScanCreator instanceof IndexScanCreator_BasicIndex){
			Insert insert = new Insert(new ArrayList<URILiteral>(), indexScanCreator.getDataset());
			addSucceedingOperator(insert);
		} else {
			addSucceedingOperator(indexScanCreator.getRoot());
		}
	}

	public void addTripleFact(final Triple fact) {
		facts.add(fact);
	}

	@Override
	public QueryResult process(final int opt, final Dataset dataset) {
		// Leitet ein GraphResult mit den Triple-Fakten weiter
		final GraphResult result = new GraphResult();
		for (final Triple triple : facts)
			result.addGraphResultTriple(triple);
		for (final OperatorIDTuple oid : succeedingOperators)
			((Operator) oid.getOperator()).processAll(result, oid.getId());
		return result;
	}

	public QueryResult processDebug(final int opt, final Dataset dataset,
			final DebugStep debugstep) {
		// Leitet ein GraphResult mit den Triple-Fakten weiter
		final GraphResult result = new GraphResult();
		for (final Triple triple : facts)
			result.addGraphResultTriple(triple);
		for (final OperatorIDTuple oid : succeedingOperators) {
			final QueryResultDebug debug = new QueryResultDebug(result,
					debugstep, this, oid.getOperator(), true);
			((Operator) oid.getOperator()).processAllDebug(debug, oid.getId(),
					debugstep);
		}
		return result;
	}

	@Override
	public String toString() {
		final StringBuffer str = new StringBuffer("TripleFacts").append("\n");
		for (final Triple tr : facts)
			str.append(tr.toString()).append("\n");
		return str.toString();
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		final StringBuffer str = new StringBuffer("TripleFacts").append("\n");
		for (final Triple tr : facts)
			str.append(tr.toString(prefixInstance)).append("\n");
		return str.toString();
	}

	@Override
	public QueryResult join(Indices indices, Bindings bindings) {
		return null;
	}

	private boolean firstTime = true;

	@Override
	public void deleteTriple(Triple triple) {
	}

	@Override
	public void deleteTripleDebug(Triple triple, DebugStep debugstep) {
	}

	@Override
	public void consume(Triple triple) {
		if(firstTime){
			firstTime = false;
			for (final Triple t : facts){
				for(OperatorIDTuple opID: this.getSucceedingOperators()){
					((TripleConsumer)opID.getOperator()).consume(t);
				}
			}			
		}
	}

	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		if(firstTime){
			firstTime = false;
			for (final Triple t : facts){
				for(OperatorIDTuple opID: this.getSucceedingOperators()){
					BasicOperator to = opID.getOperator();
					debugstep.step(this, to, t);
					((TripleConsumerDebug)to).consumeDebug(t, debugstep);
				}
			}			
		}
	}
}
