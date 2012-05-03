package lupos.engine.operators.index.memoryindex;

import java.util.HashSet;
import java.util.LinkedList;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class OptimizedRelationalIndex extends MemoryIndex {

	protected BasicIndex index1;
	protected BasicIndex index2;

	public void physicalOptimization() {
		lupos.optimizations.physical.PhysicalOptimizations.replaceOperators(
				index1, this);
		lupos.optimizations.physical.PhysicalOptimizations.replaceOperators(
				index2, this);
	}

	public OptimizedRelationalIndex(final BasicIndex index1,
			final BasicIndex index2, final lupos.engine.operators.index.IndexCollection indexCollection) {
		super(indexCollection);
		this.index1 = index1;
		this.index2 = index2;
		this.succeedingOperators = new LinkedList<OperatorIDTuple>();
		this.succeedingOperators.addAll(index1.getSucceedingOperators());
		this.succeedingOperators.addAll(index2.getSucceedingOperators());
		this.triplePatterns = new LinkedList<TriplePattern>();
		this.triplePatterns.addAll(index1.getTriplePattern());
		this.triplePatterns.addAll(index2.getTriplePattern());
	}

	/**
	 * Joins the triple pattern using the index maps and returns the result.<br>
	 * The succeeding operators are passed to the operator pipe to be processed.
	 * 
	 * @param triplePatterns
	 *            - the triple pattern to be joined
	 * @param succeedingOperators
	 *            - the succeeding operators to be passed
	 * @return the result of the performed join
	 */
	@Override
	public QueryResult process(final int opt, final Dataset dataset) {

		// join the triple patterns
		// final long start = new Date().getTime();
		final QueryResult queryResult = index1.process(opt, dataset);
		// RelationalIndexMain.joinDurations.add(new Date().getTime()-start);

		if (queryResult == null) {
			return null;
		}

		/*
		 * pass the succeeding operators which were externally provided to the
		 * operator pipe along with the new bindings which have been determined
		 * by the join
		 */
		// final long start = new Date().getTime();
		// for every binding found in the result of the previously performed
		// join of the triple elements ...
		for (final OperatorIDTuple succOperator : index1
				.getSucceedingOperators()) {

			((Operator) succOperator.getOperator()).processAll(queryResult,
					succOperator.getId());
		}
		// System.out.println("Time used to forward the  succOpps "+(new
		// Date().getTime()-start));
		// TODO: the method call, which is commented out is not correct...
		// index2.process(queryResult,BasicIndex.NONE);
		return queryResult;
	}

	@Override
	public void optimizeJoinOrderAccordingToMostRestrictionsAndLeastEntries(
			final Dataset dataset) {
		index1.optimizeJoinOrder(BasicIndex.MOSTRESTRICTIONSLEASTENTRIES,
				dataset);
		index2.optimizeJoinOrder(BasicIndex.MOSTRESTRICTIONSLEASTENTRIES,
				dataset);
	}

	@Override
	public void optimizeJoinOrderAccordingToLeastEntries(final Dataset dataset) {
		index1.optimizeJoinOrder(BasicIndex.LEASTENTRIES, dataset);
		index2.optimizeJoinOrder(BasicIndex.LEASTENTRIES, dataset);
	}

	@Override
	public void optimizeJoinOrderAccordingToMostRestrictions() {
		index1.optimizeJoinOrderAccordingToMostRestrictions();
		index2.optimizeJoinOrderAccordingToMostRestrictions();
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		final HashSet<Variable> intersectionVariables1 = new HashSet<Variable>();
		for (final TriplePattern tp : index1.getTriplePattern()) {
			intersectionVariables1.addAll(tp.getVariables());
		}
		final HashSet<Variable> intersectionVariables2 = new HashSet<Variable>();
		for (final TriplePattern tp : index2.getTriplePattern()) {
			intersectionVariables2.addAll(tp.getVariables());
		}
		unionVariables = (HashSet<Variable>) intersectionVariables1.clone();
		intersectionVariables1.retainAll(intersectionVariables2);
		result.getVariables().addAll(intersectionVariables1);
		intersectionVariables = intersectionVariables1;
		unionVariables.addAll(intersectionVariables2);
		return result;
	}

	@Override
	public void replace(final Variable var, final Item item) {
		index1.replace(var, item);
		index2.replace(var, item);
	}
}
