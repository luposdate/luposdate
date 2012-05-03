package lupos.rif.builtin;

import lupos.rif.IExpression;
import lupos.rif.model.RuleList;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-predicate#")
public class ListPredicates {

	@Builtin(Name = "is-list")
	public static BooleanLiteral is_list(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof RuleList)
			return BooleanLiteral.TRUE;
		else
			return BooleanLiteral.FALSE;
	}

	@Builtin(Name = "list-contains")
	public static BooleanLiteral list_contains(final Argument arg) {
		if (arg.arguments.size() == 2
				&& arg.arguments.get(0) instanceof RuleList
				&& arg.arguments.get(1) instanceof RuleList) {
			final RuleList list1 = (RuleList) arg.arguments.get(0);
			final RuleList list2 = (RuleList) arg.arguments.get(1);
			for (final IExpression expr : list1.getItems()) {
				if (expr.equals(list2))
					return BooleanLiteral.TRUE;
			}
			return BooleanLiteral.FALSE;
		} else
			return BooleanLiteral.FALSE;
	}

}
