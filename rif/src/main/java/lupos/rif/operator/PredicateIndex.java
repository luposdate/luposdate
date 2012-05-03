package lupos.rif.operator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.stream.TripleDeleter;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleConsumerDebug;
import lupos.misc.debug.DebugStep;
import lupos.misc.debug.DebugStepRIF;
import lupos.rdf.Prefix;
import lupos.rif.datatypes.Predicate;
import lupos.rif.datatypes.RuleResult;

public class PredicateIndex extends BasicIndex implements TripleConsumer, TripleConsumerDebug, TripleDeleter {
	final public Set<Predicate> predFacts = new HashSet<Predicate>();

	public PredicateIndex() {
		super(null);
		triplePatterns = Arrays.asList();
	}

	public void addPredicateFact(final Predicate fact) {
		predFacts.add(fact);
	}

	@Override
	public QueryResult process(final int opt, final Dataset dataset) {
		final RuleResult gr = new RuleResult();
		gr.getPredicateResults().addAll(predFacts);
		for (final OperatorIDTuple succOperator : succeedingOperators)
			((Operator) succOperator.getOperator()).processAll(gr,
					succOperator.getId());
		return gr;
	}

	public QueryResult processDebug(final int opt, final Dataset dataset,
			final DebugStep debugstep) {
		final RuleResult gr = new RuleResult();
		gr.getPredicateResults().addAll(predFacts);
		for (final OperatorIDTuple succOperator : succeedingOperators) {
			if (!gr.isEmpty())
				((DebugStepRIF)debugstep).step(this, succOperator.getOperator(), gr);
			final QueryResultDebug debug = new QueryResultDebug(gr, debugstep,
					this, succOperator.getOperator(), true);
			((Operator) succOperator.getOperator()).processAllDebug(debug,
					succOperator.getId(), debugstep);
		}
		return gr;
	}

	@Override
	public String toString() {
		final StringBuffer str = new StringBuffer("PredicateFacts")
				.append("\n");
		for (final Predicate pr : predFacts)
			str.append(pr.toString()).append("\n");
		return str.toString();
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		final StringBuffer str = new StringBuffer("PredicateFacts")
				.append("\n");
		for (final Predicate pr : predFacts)
			str.append(pr.toString(prefixInstance)).append("\n");
		return str.toString();
	}

	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
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
