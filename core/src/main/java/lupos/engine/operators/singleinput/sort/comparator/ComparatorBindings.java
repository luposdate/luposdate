package lupos.engine.operators.singleinput.sort.comparator;

import java.util.Collection;
import java.util.Comparator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;

public interface ComparatorBindings extends Comparator<Bindings> {
	public Collection<Variable> getSortCriterium();
}
