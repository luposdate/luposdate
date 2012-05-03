package lupos.rif.operator;

import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.datatypes.EqualityResult;
import lupos.rif.model.Equality;
import lupos.rif.visitor.ReplaceVarsVisitor;

import com.google.common.collect.Multimap;

public class EqualityFilter extends RuleFilter {

	private final Set<Bindings> filteredBindings = new HashSet<Bindings>();
	private boolean saveFilteredBindings = true;
	private final ReplaceVarsVisitor replace = new ReplaceVarsVisitor();

	public EqualityFilter(IExpression expression,
			Multimap<IExpression, IExpression> eqMap) {
		super(expression, eqMap);
	}

	@Override
	public QueryResult process(QueryResult bindings, int operandID) {
		try {
			if (bindings instanceof EqualityResult
					&& !filteredBindings.isEmpty()) {
				final QueryResult qr = QueryResult.createInstance();
				for (final Bindings bind : filteredBindings)
					qr.add(bind);
				saveFilteredBindings = false;
				return super.process(qr, operandID);
			} else
				return super.process(bindings, operandID);
		} finally {
			saveFilteredBindings = true;
		}
	}

	@Override
	protected boolean filter(Bindings bind) {
		boolean result = super.filter(bind);
		if (result)
			return result;
		else {
			replace.bindings = bind;
			final Equality replacedEq = (Equality) expression.accept(replace,
					null);
			return equalityMap.get(replacedEq.leftExpr).contains(
					replacedEq.rightExpr)
					|| equalityMap.get(replacedEq.rightExpr).contains(
							replacedEq.leftExpr);
		}
	}

	@Override
	protected void onAccepted(Bindings bind) {
		if (!saveFilteredBindings)
			filteredBindings.remove(bind);
	}

	@Override
	protected void onFilteredOut(final Bindings bind) {
		if (saveFilteredBindings)
			filteredBindings.add(bind);
	}

	public String toString() {
		String result = "Equalityfilter\n" + expression.toString();

		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}

		return result;
	}

	public String toString(final Prefix prefixInstance) {
		String result = "Equalityfilter\n"
				+ expression.toString(prefixInstance);

		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}

		return result;
	}

}
