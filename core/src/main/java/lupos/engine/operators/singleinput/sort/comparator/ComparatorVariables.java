package lupos.engine.operators.singleinput.sort.comparator;

import java.util.Collection;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;

public class ComparatorVariables implements ComparatorBindings {
	
	protected final Collection<Variable> vars;
	
	public ComparatorVariables(final Collection<Variable> vars){
		this.vars = vars;
	}

	@Override
	public int compare(Bindings o1, Bindings o2) {
		for(Variable v: vars){
			final Literal l0 = o1.get(v);
			final Literal l1 = o2.get(v);
			final int ret = ComparatorAST.intComp(l0, l1);
			if (ret != 0)
				return ret;
		}
		return 0;
	}

	@Override
	public Collection<Variable> getSortCriterium() {
		return vars;
	}

}
