package lupos.rif.operator;

import java.util.Arrays;
import java.util.HashSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.stream.TripleDeleter;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleConsumerDebug;
import lupos.misc.debug.DebugStep;
import lupos.rdf.Prefix;

public class BooleanIndex extends BasicIndex implements TripleConsumer, TripleConsumerDebug, TripleDeleter {

	public BooleanIndex() {
		super(null);
		triplePatterns = Arrays.asList();
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		result.getVariables().add(new Variable("@boolean"));
		intersectionVariables = new HashSet<Variable>(result.getVariables());
		unionVariables = intersectionVariables;
		return result;
	}
	
	private QueryResult createQueryResult(){
		final QueryResult result = QueryResult.createInstance();
		final Bindings bind = Bindings.createNewInstance();
		bind.add(new Variable("@boolean"), LiteralFactory.createLiteral("true"));
		result.add(bind);
		return result;
	}

	@Override
	public QueryResult process(final int opt, final Dataset dataset) {
		// leitet ein QueryResult mit einem Binding weiter
		final QueryResult result = this.createQueryResult();
		for (final OperatorIDTuple succOperator : succeedingOperators) {
			((Operator) succOperator.getOperator()).processAll(result,
					succOperator.getId());
		}
		return result;
	}

	public QueryResult processDebug(final int opt, final Dataset dataset,
			final DebugStep debugstep) {
		// leitet ein QueryResult mit einem Binding weiter
		final QueryResult result = this.createQueryResult();
		Bindings bind = result.getFirst();
		for (final OperatorIDTuple succOperator : succeedingOperators) {
			if (result.size() > 0)
				debugstep.step(this, succOperator.getOperator(), bind);
			final QueryResultDebug debug = new QueryResultDebug(result,
					debugstep, this, succOperator.getOperator(), true);
			((Operator) succOperator.getOperator()).processAll(debug,
					succOperator.getId());
		}
		return result;
	}

	@Override
	public QueryResult join(Indices indices, Bindings bindings) {
		return null;
	}

	@Override
	public String toString() {
		return "BooleanIndex";
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		return toString();
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
			process(0, null);
			firstTime = false;
		}
	}

	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		if(firstTime){
			processDebug(0, null, debugstep);
			firstTime = false;
		}
	}
}
