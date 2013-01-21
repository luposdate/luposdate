/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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

@Namespace(value = "http://www.w3.org/2007/rif-builtin-predicate#")
public class NumericPredicates {

	@Builtin(Name = "numeric-equal", Bindable = true)
	public static BooleanLiteral numeric_equal(final Argument arg) {
		Double lVal = null;
		if (!arg.arguments.get(0).isVariable())
			lVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(0));
		Double rVal = null;
		if (!arg.arguments.get(1).isVariable())
			rVal = BuiltinHelper.numberFromLiteral((TypedLiteral) arg.arguments
					.get(1));
		if (lVal != null && rVal != null)
			return BooleanLiteral.create(lVal.doubleValue() == rVal
					.doubleValue());
		else if (lVal == null) {
			arg.binding.add((Variable) arg.arguments.get(0),
					(Literal) arg.arguments.get(1));
		} else
			arg.binding.add((Variable) arg.arguments.get(1),
					(Literal) arg.arguments.get(0));
		return BooleanLiteral.TRUE;
	}

	@Builtin(Name = "numeric-not-equal")
	public static BooleanLiteral numeric_not_equal(final Argument arg) {
		return BooleanLiteral.not(numeric_equal(arg));
	}

	@Builtin(Name = "numeric-less-than")
	public static BooleanLiteral numeric_less_than(final Argument arg) {
		Double lVal = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(0));
		Double rVal = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(1));
		return BooleanLiteral.create(lVal.doubleValue() < rVal.doubleValue());
	}

	@Builtin(Name = "numeric-less-than-or-equal")
	public static BooleanLiteral numeric_less_than_or_equal(final Argument arg) {
		Double lVal = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(0));
		Double rVal = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(1));
		return BooleanLiteral.create(lVal.doubleValue() <= rVal.doubleValue());
	}

	@Builtin(Name = "numeric-greater-than")
	public static BooleanLiteral numeric_greater_than(final Argument arg) {
		Double lVal = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(0));
		Double rVal = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(1));
		return BooleanLiteral.create(lVal.doubleValue() > rVal.doubleValue());
	}

	@Builtin(Name = "numeric-greater-than-or-equal")
	public static BooleanLiteral numeric_greater_than_or_equal(
			final Argument arg) {
		Double lVal = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(0));
		Double rVal = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(1));
		return BooleanLiteral.create(lVal.doubleValue() >= rVal.doubleValue());
	}

	@Builtin(Name = "numeric-between", Iterable = true)
	public static BooleanLiteral numeric_between(final Argument arg) {
		Double Val = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(0));
		Double lVal = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(1));
		Double rVal = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(2));
		return BooleanLiteral.create(lVal < Val && Val < rVal);
	}

	@Builtin(Name = "numeric-between-enclosing", Iterable = true)
	public static BooleanLiteral numeric_between_enclosing(final Argument arg) {
		Double Val = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(0));
		Double lVal = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(1));
		Double rVal = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(2));
		return BooleanLiteral.create(lVal <= Val && Val <= rVal);
	}

	@Builtin(Name = "is-literal-double")
	public static BooleanLiteral is_double(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"double");
	}

	@Builtin(Name = "is-literal-float")
	public static BooleanLiteral is_float(final Argument arg) {
		return BuiltinHelper
				.isOfXSType((Literal) arg.arguments.get(0), "float");
	}

	@Builtin(Name = "is-literal-decimal")
	public static BooleanLiteral is_decimal(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "decimal");
	}

	@Builtin(Name = "is-literal-integer")
	public static BooleanLiteral is_integer(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer");
	}

	@Builtin(Name = "is-literal-long")
	public static BooleanLiteral is_long(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "long");
	}

	@Builtin(Name = "is-literal-int")
	public static BooleanLiteral is_int(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "int");
	}

	@Builtin(Name = "is-literal-short")
	public static BooleanLiteral is_short(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "short");
	}

	@Builtin(Name = "is-literal-byte")
	public static BooleanLiteral is_byte(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "byte");
	}

	@Builtin(Name = "is-literal-nonNegativeInteger")
	public static BooleanLiteral is_nonNegativeInteger(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "nonNegativeInteger");
	}

	@Builtin(Name = "is-literal-positiveInteger")
	public static BooleanLiteral is_positiveInteger(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "positiveInteger");
	}

	@Builtin(Name = "is-literal-unsignedLong")
	public static BooleanLiteral is_unsignedLong(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "unsignedLong");
	}

	@Builtin(Name = "is-literal-unsignedInt")
	public static BooleanLiteral is_unsignedInt(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "unsignedInt");
	}

	@Builtin(Name = "is-literal-unsignedShort")
	public static BooleanLiteral is_unsignedShort(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "unsignedShort");
	}

	@Builtin(Name = "is-literal-unsignedByte")
	public static BooleanLiteral is_unsignedByte(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "unsignedByte");
	}

	@Builtin(Name = "is-literal-nonPositiveInteger")
	public static BooleanLiteral is_nonPositiveInteger(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "nonPositiveInteger");
	}

	@Builtin(Name = "is-literal-negativeInteger")
	public static BooleanLiteral is_negativeInteger(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"integer", "negativeInteger");
	}

	@Builtin(Name = "is-literal-not-double")
	public static BooleanLiteral is_not_double(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "double"));
	}

	@Builtin(Name = "is-literal-not-float")
	public static BooleanLiteral is_not_float(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "float"));
	}

	@Builtin(Name = "is-literal-not-hexBinary")
	public static BooleanLiteral is_not_hexBinary(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "hexBinary"));
	}

	@Builtin(Name = "is-literal-not-decimal")
	public static BooleanLiteral is_not_decimal(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "decimal"));
	}

	@Builtin(Name = "is-literal-not-integer")
	public static BooleanLiteral is_not_integer(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "integer"));
	}

	@Builtin(Name = "is-literal-not-long")
	public static BooleanLiteral is_not_long(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "long"));
	}

	@Builtin(Name = "is-literal-not-int")
	public static BooleanLiteral is_not_int(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "integer"));
	}

	@Builtin(Name = "is-literal-not-short")
	public static BooleanLiteral is_not_short(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "short"));
	}

	@Builtin(Name = "is-literal-not-byte")
	public static BooleanLiteral is_not_byte(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "byte"));
	}

	@Builtin(Name = "is-literal-not-nonNegativeInteger")
	public static BooleanLiteral is_not_nonNegativeInteger(final Argument arg) {
		return BooleanLiteral.not(is_nonNegativeInteger(arg));
	}

	@Builtin(Name = "is-literal-not-positiveInteger")
	public static BooleanLiteral is_not_positiveInteger(final Argument arg) {
		return BooleanLiteral.not(is_positiveInteger(arg));
	}

	@Builtin(Name = "is-literal-not-unsignedLong")
	public static BooleanLiteral is_not_unsignedLong(final Argument arg) {
		return BooleanLiteral.not(is_unsignedLong(arg));
	}

	@Builtin(Name = "is-literal-not-unsignedInt")
	public static BooleanLiteral is_not_unsignedInt(final Argument arg) {
		return BooleanLiteral.not(is_unsignedInt(arg));
	}

	@Builtin(Name = "is-literal-not-unsignedShort")
	public static BooleanLiteral is_not_unsignedShort(final Argument arg) {
		return BooleanLiteral.not(is_unsignedShort(arg));
	}

	@Builtin(Name = "is-literal-not-unsignedByte")
	public static BooleanLiteral is_not_unsignedByte(final Argument arg) {
		return BooleanLiteral.not(is_unsignedByte(arg));
	}

	@Builtin(Name = "is-literal-not-nonPositiveInteger")
	public static BooleanLiteral is_not_nonPositiveInteger(final Argument arg) {
		return BooleanLiteral.not(is_nonPositiveInteger(arg));
	}

	@Builtin(Name = "is-literal-not-negativeInteger")
	public static BooleanLiteral is_not_negativeInteger(final Argument arg) {
		return BooleanLiteral.not(is_negativeInteger(arg));
	}

}
