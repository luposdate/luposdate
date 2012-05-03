package lupos.rif.builtin;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-function#")
public class NumericFunctions {

	@Builtin(Name = "numeric-add", Bindable = true)
	public static Literal numeric_add(final Argument arg) {
		Double lVal = null;
		if (!arg.arguments.get(0).isVariable())
			lVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(0));
		Double rVal = null;
		if (!arg.arguments.get(1).isVariable())
			rVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(1));
		Double eqResult = null;
		if (arg.result != null)
			eqResult = BuiltinHelper
					.numberFromLiteral((TypedLiteral) arg.result);
		if (lVal != null && rVal != null) {
			Double result = lVal + rVal;
			return BuiltinHelper.getNumericLiteral(result);
		} else if (lVal == null) {
			Double result = eqResult - rVal;
			arg.binding.add((Variable) arg.arguments.get(0),
					BuiltinHelper.getNumericLiteral(result));
			return arg.result;
		} else {
			Double result = eqResult - lVal;
			arg.binding.add((Variable) arg.arguments.get(1),
					BuiltinHelper.getNumericLiteral(result));
			return arg.result;
		}
	}

	@Builtin(Name = "numeric-subtract", Bindable = true)
	public static Literal numeric_subtract(final Argument arg) {
		Double lVal = null;
		if (!arg.arguments.get(0).isVariable())
			lVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(0));
		Double rVal = null;
		if (!arg.arguments.get(1).isVariable())
			rVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(1));
		Double eqResult = null;
		if (arg.result != null)
			eqResult = BuiltinHelper
					.numberFromLiteral((TypedLiteral) arg.result);
		if (lVal != null && rVal != null) {
			Double result = lVal - rVal;
			return BuiltinHelper.getNumericLiteral(result);
		} else if (lVal == null) {
			Double result = eqResult + rVal;
			arg.binding.add((Variable) arg.arguments.get(0),
					BuiltinHelper.getNumericLiteral(result));
			return arg.result;
		} else {
			Double result = lVal - eqResult;
			arg.binding.add((Variable) arg.arguments.get(1),
					BuiltinHelper.getNumericLiteral(result));
			return arg.result;
		}
	}

	@Builtin(Name = "numeric-multiply", Bindable = true)
	public static Literal numeric_multiply(final Argument arg) {
		Double lVal = null;
		if (!arg.arguments.get(0).isVariable())
			lVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(0));
		Double rVal = null;
		if (!arg.arguments.get(1).isVariable())
			rVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(1));
		Double eqResult = null;
		if (arg.result != null)
			eqResult = BuiltinHelper
					.numberFromLiteral((TypedLiteral) arg.result);
		if (lVal != null && rVal != null) {
			Double result = lVal * rVal;
			return BuiltinHelper.getNumericLiteral(result);
		} else if (lVal == null) {
			Double result = eqResult / rVal;
			arg.binding.add((Variable) arg.arguments.get(0),
					BuiltinHelper.getNumericLiteral(result));
			return arg.result;
		} else {
			Double result = eqResult / lVal;
			arg.binding.add((Variable) arg.arguments.get(1),
					BuiltinHelper.getNumericLiteral(result));
			return arg.result;
		}
	}

	@Builtin(Name = "numeric-divide", Bindable = true)
	public static Literal numeric_divide(final Argument arg) {
		Double lVal = null;
		if (!arg.arguments.get(0).isVariable())
			lVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(0));
		Double rVal = null;
		if (!arg.arguments.get(1).isVariable())
			rVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(1));
		Double eqResult = null;
		if (arg.result != null)
			eqResult = BuiltinHelper
					.numberFromLiteral((TypedLiteral) arg.result);
		if (lVal != null && rVal != null) {
			Double result = lVal / rVal;
			return BuiltinHelper.getNumericLiteral(result);
		} else if (lVal == null) {
			Double result = eqResult * rVal;
			arg.binding.add((Variable) arg.arguments.get(0),
					BuiltinHelper.getNumericLiteral(result));
			return arg.result;
		} else {
			Double result = lVal / eqResult;
			arg.binding.add((Variable) arg.arguments.get(1),
					BuiltinHelper.getNumericLiteral(result));
			return arg.result;
		}
	}

	@Builtin(Name = "numeric-integer-divide", Bindable = true)
	public static Literal numeric_integer_divide(final Argument arg) {
		Double lVal = null;
		if (!arg.arguments.get(0).isVariable())
			lVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(0));
		Double rVal = null;
		if (!arg.arguments.get(1).isVariable())
			rVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(1));
		Double eqResult = null;
		if (arg.result != null)
			eqResult = BuiltinHelper
					.numberFromLiteral((TypedLiteral) arg.result);
		if (lVal != null && rVal != null) {
			Integer result = (int) lVal.doubleValue()
					/ (int) rVal.doubleValue();
			return BuiltinHelper.getNumericLiteral(result);
		} else if (lVal == null) {
			Integer result = (int) eqResult.doubleValue()
					* (int) rVal.doubleValue();
			arg.binding.add((Variable) arg.arguments.get(0),
					BuiltinHelper.getNumericLiteral(result));
			return arg.result;
		} else {
			Integer result = (int) lVal.doubleValue()
					/ (int) eqResult.doubleValue();
			arg.binding.add((Variable) arg.arguments.get(1),
					BuiltinHelper.getNumericLiteral(result));
			return arg.result;
		}
	}
	
	@Builtin(Name = "numeric-integer-mod", Bindable = true)
	public static Literal numeric_integer_mod(final Argument arg) {
		Double lVal = null;
		if (!arg.arguments.get(0).isVariable())
			lVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(0));
		Double rVal = null;
		if (!arg.arguments.get(1).isVariable())
			rVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(1));
		Double eqResult = null;
		if (arg.result != null)
			eqResult = BuiltinHelper
					.numberFromLiteral((TypedLiteral) arg.result);
		if (lVal != null && rVal != null) {
			Integer result = (int) lVal.doubleValue()
					% (int) rVal.doubleValue();
			return BuiltinHelper.getNumericLiteral(result);
		} else if (lVal == null) {
			//keine Umkehrfunktion mšglich
			return null;
		} else {
			Integer result = (int) lVal.doubleValue()
					- (int) eqResult.doubleValue();
			arg.binding.add((Variable) arg.arguments.get(1),
					BuiltinHelper.getNumericLiteral(result));
			return arg.result;
		}
	}
}
