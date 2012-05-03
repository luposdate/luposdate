package lupos.rif.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class BindableTripleIndex extends BindableIndex {

	public BindableTripleIndex(final BasicIndex index) {
		super(index);
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = (BoundVariablesMessage) index
				.preProcessMessage(msg);
		result.getVariables().removeAll(msg.getVariables());
		unionVariables = new HashSet<Variable>(result.getVariables());
		intersectionVariables = new HashSet<Variable>(unionVariables);
		return result;
	}

	protected void processIndexScan(final QueryResult result,
			final Bindings bind) {
		final Collection<TriplePattern> pattern = new ArrayList<TriplePattern>(
				index.getTriplePattern());
		final Collection<TriplePattern> bindPattern = new ArrayList<TriplePattern>();
		for (final TriplePattern tp : pattern) {
			final TriplePattern newPat = new TriplePattern();
			int i = 0;
			for (final Item item : tp.getItems()) {
				Item toSet = null;
				if (item.isVariable() && bind.getVariableSet().contains(item))
					toSet = item.getLiteral(bind);
				else
					toSet = item;
				newPat.setPos(toSet, i++);
			}
			bindPattern.add(newPat);
		}
		index.getTriplePattern().clear();
		index.getTriplePattern().addAll(bindPattern);
		// Scan durchführen
		QueryResult tempResult = index.process(0, dataSet);
		result.add(tempResult);
		// TriplePattern zurücksetzen
		index.getTriplePattern().clear();
		index.getTriplePattern().addAll(pattern);
	}

	@Override
	public Collection<TriplePattern> getTriplePattern() {
		return index.getTriplePattern();
	}

}
