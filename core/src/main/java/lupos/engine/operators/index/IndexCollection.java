package lupos.engine.operators.index;

import java.util.Collection;
import java.util.List;

import lupos.datastructures.items.Item;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.debug.DebugStep;

public abstract class IndexCollection extends Operator {
	public List<String> defaultGraphs;
	public List<String> namedGraphs;
	public Dataset dataset;

	public abstract BasicIndex newIndex(OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, Item data);

	public QueryResult process(final int opt, final Dataset dataset) {
		if (succeedingOperators.size() == 0)
			return null;
		for (final OperatorIDTuple oit : succeedingOperators) {
			((BasicIndex) oit.getOperator()).process(opt, dataset);
		}
		return null;
	}


	public void physicalOptimization() {
		lupos.optimizations.physical.PhysicalOptimizations.replaceOperators(
				this, this);
	}

	public void optimizeJoinOrder(final int opt, final Dataset dataset) {
		switch (opt) {
		case BasicIndex.MOSTRESTRICTIONS:
			optimizeJoinOrderAccordingToMostRestrictions();
			break;
		}
	}

	public void optimizeJoinOrderAccordingToMostRestrictions() {
		for (final OperatorIDTuple oit : succeedingOperators) {
			final BasicIndex index = (BasicIndex) oit.getOperator();
			index.optimizeJoinOrderAccordingToMostRestrictions();
		}
	}

	public void remove(final BasicIndex i) {
		removeSucceedingOperator(i);
	}

	public abstract IndexCollection newInstance(Dataset dataset);

	public void printGraphURLs() {
		String graph;
		System.out.println();
		System.out.println("default graphs: ");
		if (defaultGraphs != null)
			for (int i = 0; i < defaultGraphs.size(); i++) {
				graph = defaultGraphs.get(i);
				System.out.println(i + ": " + graph);
			}
		System.out.println();
		System.out.println("named graphs: ");
		if (namedGraphs != null)
			for (int i = 0; i < namedGraphs.size(); i++) {
				graph = namedGraphs.get(i);
				System.out.println(i + ": " + graph);
			}
		System.out.println();
	}
	
	public QueryResult processDebug(final int opt, final Dataset dataset,
			final DebugStep debugstep) {
		if (succeedingOperators.size() == 0)
			return null;
		for (final OperatorIDTuple oit : succeedingOperators) {
			((BasicIndex) oit.getOperator()).processDebug(opt, dataset,
					debugstep);
		}
		return null;
	}
}