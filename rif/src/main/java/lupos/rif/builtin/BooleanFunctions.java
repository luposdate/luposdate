package lupos.rif.builtin;

import lupos.datastructures.items.literal.TypedLiteral;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-function#")
public class BooleanFunctions {

	@Builtin(Name = "not")
	public static BooleanLiteral not(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral
				&& BooleanPredicates.is_boolean(arg).value)
			return BooleanLiteral.create(!BuiltinHelper
					.getBoolean((TypedLiteral) arg.arguments.get(0)));
		else
			return null;
	}
}
