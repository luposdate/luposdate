package lupos.rif.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.rif.datatypes.RuleResult;

public class BindablePredicateIndex extends BindableIndex {
	private final PredicatePattern predicatePattern;

	public BindablePredicateIndex(final PredicateIndex index,
			final PredicatePattern pattern) {
		super(index);
		this.predicatePattern = pattern;
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = (BoundVariablesMessage) predicatePattern
				.preProcessMessage(msg);
		result.getVariables().removeAll(msg.getVariables());
		unionVariables = new HashSet<Variable>(result.getVariables());
		intersectionVariables = new HashSet<Variable>(unionVariables);
		return result;
	}

	@Override
	protected void processIndexScan(QueryResult result, Bindings bind) {
		final Item[] newItems = new Item[predicatePattern.getPatternItems()
				.size()];
		int i = 0;
		for (final Item item : predicatePattern.getPatternItems()) {
			Item toSet = null;
			if (item.isVariable() && bind.getVariableSet().contains(item))
				toSet = item.getLiteral(bind);
			else
				toSet = item;
			newItems[i++] = toSet;
		}
		final PredicatePattern newPattern = new PredicatePattern(
				predicatePattern.getPredicateName(), newItems);
		// Scan durchführen
		RuleResult ruleResult = (RuleResult) index.process(0, dataSet);
		QueryResult tempResult = newPattern.process(ruleResult, 0);
		result.add(tempResult);
	}

	@Override
	public Collection<TriplePattern> getTriplePattern() {
		return new ArrayList<TriplePattern>();
	}

}
