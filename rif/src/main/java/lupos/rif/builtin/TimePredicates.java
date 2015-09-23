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

import lupos.datastructures.items.literal.Literal;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-predicate#")
public class TimePredicates {

	/**
	 * <p>is_date.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-date")
	public static BooleanLiteral is_date(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0), "date",
				"dateTime");
	}

	/**
	 * <p>is_dateTime.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-dateTime")
	public static BooleanLiteral is_dateTime(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"dateTime", "date");
	}

	/**
	 * <p>is_dateTimeStamp.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-dateTimeStamp")
	public static BooleanLiteral is_dateTimeStamp(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"dateTimeStamp");
	}

	/**
	 * <p>is_time.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-time")
	public static BooleanLiteral is_time(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0), "time");
	}

	/**
	 * <p>is_dayTimeDuration.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-dayTimeDuration")
	public static BooleanLiteral is_dayTimeDuration(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"dayTimeDuration");
	}

	/**
	 * <p>is_yearMonthDuration.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-yearMonthDuration")
	public static BooleanLiteral is_yearMonthDuration(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"yearMonthDuration");
	}

	/**
	 * <p>is_not_date.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-not-date")
	public static BooleanLiteral is_not_date(final Argument arg) {
		return BooleanLiteral.not(is_date(arg));
	}

	/**
	 * <p>is_not_dateTime.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-not-dateTime")
	public static BooleanLiteral is_not_dateTime(final Argument arg) {
		return BooleanLiteral.not(is_dateTime(arg));
	}

	/**
	 * <p>is_not_dateTimeStamp.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-not-dateTimeStamp")
	public static BooleanLiteral is_not_dateTimeStamp(final Argument arg) {
		return BooleanLiteral.not(is_dateTimeStamp(arg));
	}

	/**
	 * <p>is_not_time.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-not-time")
	public static BooleanLiteral is_not_time(final Argument arg) {
		return BooleanLiteral.not(is_time(arg));
	}

	/**
	 * <p>is_not_dayTimeDuration.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-not-dayTimeDuration")
	public static BooleanLiteral is_not_dayTimeDuration(final Argument arg) {
		return BooleanLiteral.not(is_dayTimeDuration(arg));
	}

	/**
	 * <p>is_not_yearMonthDuration.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-not-yearMonthDuration")
	public static BooleanLiteral is_not_yearMonthDuration(final Argument arg) {
		return BooleanLiteral.not(is_yearMonthDuration(arg));
	}

}
