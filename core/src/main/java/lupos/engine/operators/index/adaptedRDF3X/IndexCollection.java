package lupos.engine.operators.index.adaptedRDF3X;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Item;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.OptimizeJoinOrder;

public class IndexCollection extends
		lupos.engine.operators.index.IndexCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1624295267286626002L;

	@Override
	public BasicIndex newIndex(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, final Item data) {
		return new RDF3XIndex(succeedingOperator, triplePattern, data, this);
	}

	@Override
	public lupos.engine.operators.index.IndexCollection newInstance(Dataset dataset) {
		this.dataset=dataset;
		return new IndexCollection();
	}

	@Override
	public void optimizeJoinOrder(final int opt, final Dataset dataset) {
		if (opt == BasicIndex.Binary || opt == BasicIndex.MERGEJOIN
				|| opt == BasicIndex.MERGEJOINSORT
				|| opt == BasicIndex.NARYMERGEJOIN)
			makeBinaryJoin(opt, dataset);
	}

	public void makeBinaryJoin(final int opt, final Dataset dataset) {
		final List<OperatorIDTuple> c = new LinkedList<OperatorIDTuple>();

		for (final OperatorIDTuple oit : succeedingOperators) {
			if (oit.getOperator() instanceof RDF3XIndex) {
				final RDF3XIndex index = (RDF3XIndex) oit.getOperator();
				if (opt == BasicIndex.NARYMERGEJOIN) {
					final lupos.engine.operators.index.IndexCollection indexCollection = OptimizeJoinOrder
							.getPlanWithNAryMergeJoins(
									new IndexCollection(),
									index,
									(opt == BasicIndex.MERGEJOINSORT) ? OptimizeJoinOrder.PlanType.RDF3XSORT
											: OptimizeJoinOrder.PlanType.RDF3X,
									dataset);
					c.addAll(indexCollection.getSucceedingOperators());
				} else {
					final lupos.engine.operators.index.IndexCollection indexCollection = (opt == BasicIndex.Binary) ? index
							.getBinaryJoin()
							: OptimizeJoinOrder
									.getBinaryJoinWithManyMergeJoins(
											new IndexCollection(),
											index,
											(opt == BasicIndex.MERGEJOINSORT) ? OptimizeJoinOrder.PlanType.RDF3XSORT
													: OptimizeJoinOrder.PlanType.RDF3X,
											dataset);
					c.addAll(indexCollection.getSucceedingOperators());
				}
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
