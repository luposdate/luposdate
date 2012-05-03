package lupos.engine.operators.rdfs.index;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;

public class IndexCollection extends
		lupos.engine.operators.index.IndexCollection {

	@Override
	public BasicIndex newIndex(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, final Item data) {
		return new ToStreamIndex(succeedingOperator, triplePattern, this);
	}

	@Override
	public lupos.engine.operators.index.IndexCollection newInstance(Dataset dataset) {
		this.dataset = dataset;
		return new IndexCollection();
	}

	public void addToPatternMatcher(final PatternMatcher pm) {
		for (final OperatorIDTuple oit : succeedingOperators) {
			final ToStreamIndex tsi = (ToStreamIndex) oit.getOperator();
			if (tsi.getTriplePattern().size() == 1)
				for (final TriplePattern tp : tsi.getTriplePattern()) {
					pm.add(tp);
					tp.setPrecedingOperator(pm);
				}
			else {
				final Join j = new Join();
				j.setSucceedingOperator(tsi.getSucceedingOperators().get(0));
				int i = 0;
				final HashSet<Variable> unionVariables = new HashSet<Variable>();
				final HashSet<Variable> intersectionVariables = new HashSet<Variable>();
				intersectionVariables.addAll(tsi.getTriplePattern().iterator()
						.next().getUnionVariables());
				for (final TriplePattern tp : tsi.getTriplePattern()) {
					final LinkedList<OperatorIDTuple> succeedingOperatorsTP = new LinkedList<OperatorIDTuple>();
					succeedingOperatorsTP.add(new OperatorIDTuple(j, i));
					tp.addSucceedingOperators(succeedingOperatorsTP);
					j.addPrecedingOperator(tp);
					pm.add(tp);
					tp.addPrecedingOperator(pm);
					unionVariables.addAll(tp.getUnionVariables());
					intersectionVariables.retainAll(tp.getUnionVariables());
					i++;
				}
				j.setUnionVariables(unionVariables);
				j.setIntersectionVariables(intersectionVariables);
			}
		}
	}
}
