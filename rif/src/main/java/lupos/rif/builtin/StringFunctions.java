
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.rif.builtin;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-function#")
public class StringFunctions {

	/**
	 * <p>compare.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	@Builtin(Name = "compare")
	public static Literal compare(final Argument arg) {
		final String left = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(0));
		final String right = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(1));
		final int result = left.compareTo(right);
		return BuiltinHelper.createXSLiteral(result < 0 ? -1 : result == 0 ? 0
				: 1, "integer");
	}

	/**
	 * <p>concat.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	@Builtin(Name = "concat")
	public static Literal concat(final Argument arg) {
		final String left = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(0));
		final String right = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(1));
		final String result = left + right;
		return BuiltinHelper.createXSLiteral("\"" + result + "\"", "string");
	}

	/**
	 * <p>join.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	@Builtin(Name = "string-join")
	public static Literal join(final Argument arg) {
		final String left = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(0));
		final String right = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(1));
		final String join = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(2));
		final String result = left + join + right;
		return BuiltinHelper.createXSLiteral("\"" + result + "\"", "string");
	}

	/**
	 * <p>substring.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	@Builtin(Name = "substring")
	public static Literal substring(final Argument arg) {
		final String str = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(0));
		final int start = (int) BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(1));
		int stop = str.length();
		if (arg.arguments.size() == 3) {
			stop = (int) BuiltinHelper
					.numberFromLiteral((TypedLiteral) arg.arguments.get(2)) - 1;
		}
		final String result = str.substring(start, stop);
		return BuiltinHelper.createXSLiteral("\"" + result + "\"", "string");
	}

	/**
	 * <p>str_length.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	@Builtin(Name = "string-length")
	public static Literal str_length(final Argument arg) {
		final String str = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(0));
		final int result = str.length();
		return BuiltinHelper.createXSLiteral(result, "integer");
	}

	/**
	 * <p>upper_case.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	@Builtin(Name = "upper-case")
	public static Literal upper_case(final Argument arg) {
		final String str = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(0));
		final String result = str.toUpperCase();
		return BuiltinHelper.createXSLiteral("\"" + result + "\"", "string");
	}

	/**
	 * <p>lower_case.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	@Builtin(Name = "lower-case")
	public static Literal lower_case(final Argument arg) {
		final String str = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(0));
		final String result = str.toLowerCase();
		return BuiltinHelper.createXSLiteral("\"" + result + "\"", "string");
	}

	/**
	 * <p>encode_for_uri.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	@Builtin(Name = "encode-for-uri")
	public static Literal encode_for_uri(final Argument arg) {
		final String str = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(0));
		final String result = BuiltinHelper.encodeURI(str);
		return BuiltinHelper.createXSLiteral("\"" + result + "\"", "string");
	}

	/**
	 * <p>substring_before.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	@Builtin(Name = "substring-before")
	public static Literal substring_before(final Argument arg) {
		final String left = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(0));
		final String right = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(1));
		final String result = left.substring(0, left.indexOf(right));
		return BuiltinHelper.createXSLiteral("\"" + result + "\"", "string");
	}

	/**
	 * <p>substring_after.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	@Builtin(Name = "substring-after")
	public static Literal substring_after(final Argument arg) {
		final String left = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(0));
		final String right = BuiltinHelper
				.stringFromLiteral((Literal) arg.arguments.get(1));
		final String result = left.substring(left.indexOf(right) + right.length());
		return BuiltinHelper.createXSLiteral("\"" + result + "\"", "string");
	}
}
