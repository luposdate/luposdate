/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.rif.builtin;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-function#")
public class NumericFunctions {

	private NumericFunctions() {
	}

	/**
	 * <p>numeric_add.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
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

	/**
	 * <p>numeric_subtract.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
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

	/**
	 * <p>numeric_multiply.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
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

	/**
	 * <p>numeric_divide.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
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

	/**
	 * <p>numeric_integer_divide.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
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
	
	/**
	 * <p>numeric_integer_mod.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
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
			// no inverse function possible...
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
