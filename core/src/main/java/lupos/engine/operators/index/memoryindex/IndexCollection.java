package lupos.engine.operators.index.memoryindex;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Item;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.OptimizeJoinOrder;

public class IndexCollection extends
		lupos.engine.operators.index.IndexCollection {
	@Override
	public BasicIndex newIndex(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, final Item data) {
		return new MemoryIndex(succeedingOperator, triplePattern, data, this);
	}

	@Override
	public IndexCollection newInstance(Dataset dataset) {
		this.dataset = dataset;
		return new IndexCollection();
	}

	public void optimizeJoinOrderAccordingToMostRestrictionsAndLeastEntries(
			final Dataset dataset) {
		for (final OperatorIDTuple oit : succeedingOperators) {
			final BasicOperator basicOperator = oit.getOperator();
			if (basicOperator instanceof MemoryIndex) {
				final MemoryIndex index = (MemoryIndex) basicOperator;
				index.optimizeJoinOrderAccordingToMostRestrictionsAndLeastEntries(dataset);
			}
		}
	}

	public void optimizeJoinOrderAccordingToLeastEntries(final Dataset dataset) {
		for (final OperatorIDTuple oit : succeedingOperators) {
			if (oit.getOperator() instanceof MemoryIndex) {
				final MemoryIndex index = (MemoryIndex) oit
						.getOperator();
				index.optimizeJoinOrderAccordingToLeastEntries(dataset);
			}
		}
	}

	@Override
	public void optimizeJoinOrder(final int opt, final Dataset dataset) {
		switch (opt) {
		case BasicIndex.MOSTRESTRICTIONSLEASTENTRIES:
			optimizeJoinOrderAccordingToMostRestrictionsAndLeastEntries(dataset);
			break;
		case BasicIndex.LEASTENTRIES:
			optimizeJoinOrderAccordingToLeastEntries(dataset);
			break;
		case BasicIndex.Binary:
			makeBinaryJoin(dataset);
			break;
		default:
			super.optimizeJoinOrder(opt, dataset);
		}
	}

	public void makeBinaryJoin(final Dataset dataset) {
		final List<OperatorIDTuple> c = new LinkedList<OperatorIDTuple>();

		for (final OperatorIDTuple oit : succeedingOperators) {
			if (oit.getOperator() instanceof MemoryIndex) {
				final MemoryIndex index = (MemoryIndex) oit
						.getOperator();
				final lupos.engine.operators.index.IndexCollection indexCollection = OptimizeJoinOrder
						.getBinaryJoinWithManyMergeJoins(new IndexCollection(),
								index,
								OptimizeJoinOrder.PlanType.RELATIONALINDEX,
								dataset);
				c.addAll(indexCollection.getSucceedingOperators());
			} else
				c.add(oit);
		}
		setSucceedingOperators(c);
		this.deleteParents();
		this.setParents();
		this.detectCycles();
		// has already been done before: this.sendMessage(new
		// BoundVariablesMessage());
	}
}