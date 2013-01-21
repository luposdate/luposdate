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

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-predicate#")
public class BooleanPredicates {

	@Builtin(Name = "is-literal-boolean")
	public static BooleanLiteral is_boolean(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral)
			return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
					"boolean");
		else
			return BooleanLiteral.FALSE;
	}

	@Builtin(Name = "is-literal-not-boolean")
	public static BooleanLiteral is_not_boolean(final Argument arg) {
		return BooleanFunctions.not(RIFBuiltinFactory
				.createArgument(is_boolean(arg)));
	}

	@Builtin(Name = "boolean-equal")
	public static BooleanLiteral is_boolean_equal(final Argument arg) {
		if (arg.arguments.size() == 2
				&& arg.arguments.get(0) instanceof TypedLiteral
				&& arg.arguments.get(1) instanceof TypedLiteral)
			return BooleanLiteral
					.create(BuiltinHelper
							.getBoolean((TypedLiteral) arg.arguments.get(1)) == BuiltinHelper
							.getBoolean((TypedLiteral) arg.arguments.get(0)));
		else
			return BooleanLiteral.FALSE;
	}

	@Builtin(Name = "boolean-less-than")
	public static BooleanLiteral is_boolean_less(final Argument arg) {
		if (arg.arguments.size() == 2
				&& arg.arguments.get(0) instanceof TypedLiteral
				&& arg.arguments.get(1) instanceof TypedLiteral)
			return BooleanLiteral.create(BuiltinHelper
					.getBoolean((TypedLiteral) arg.arguments.get(0)) == false
					&& BuiltinHelper.getBoolean((TypedLiteral) arg.arguments
							.get(1)) == true);
		else
			return BooleanLiteral.FALSE;
	}

	@Builtin(Name = "boolean-greater-than")
	public static BooleanLiteral is_boolean_greater(final Argument arg) {
		if (arg.arguments.size() == 2
				&& arg.arguments.get(0) instanceof TypedLiteral
				&& arg.arguments.get(1) instanceof TypedLiteral)
			return BooleanLiteral.create(BuiltinHelper
					.getBoolean((TypedLiteral) arg.arguments.get(1)) == false
					&& BuiltinHelper.getBoolean((TypedLiteral) arg.arguments
							.get(0)) == true);
		else
			return BooleanLiteral.FALSE;
	}

}
