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
public class LiteralPredicates {
	/**
	 * <p>literal_equal.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "literal-equal")
	public static BooleanLiteral literal_equal(Argument arg) {
		return BooleanLiteral.create(arg.arguments.get(0).equals(
				arg.arguments.get(1)));
	}

	/**
	 * <p>literal_not_equal.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "literal-not-identical")
	public static BooleanLiteral literal_not_equal(Argument arg) {
		return BooleanLiteral.not(literal_equal(arg));
	}

	/**
	 * <p>literal_not_equal2.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "literal-not-equal")
	public static BooleanLiteral literal_not_equal2(Argument arg) {
		return literal_not_equal(arg);
	}

	/**
	 * <p>is_anyUri.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-anyURI")
	public static BooleanLiteral is_anyUri(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"anyURI");
	}

	/**
	 * <p>is_not_anyUri.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-not-anyURI")
	public static BooleanLiteral is_not_anyUri(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "anyURI"));
	}

	/**
	 * <p>is_xmlLiteral.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-XMLLiteral")
	public static BooleanLiteral is_xmlLiteral(final Argument arg) {
		return BuiltinHelper.isOfRDFType((Literal) arg.arguments.get(0),
				"XMLLiteral");
	}

	/**
	 * <p>is_not_xmlLiteral.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-not-XMLLiteral")
	public static BooleanLiteral is_not_xmlLiteral(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfRDFType(
				(Literal) arg.arguments.get(0), "XMLLiteral"));
	}

	/**
	 * <p>is_plainLiteral.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-PlainLiteral")
	public static BooleanLiteral is_plainLiteral(final Argument arg) {
		return BuiltinHelper.isOfRDFType((Literal) arg.arguments.get(0),
				"PlainLiteral");
	}

	/**
	 * <p>is_not_plainLiteral.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-not-PlainLiteral")
	public static BooleanLiteral is_not_plainLiteral(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfRDFType(
				(Literal) arg.arguments.get(0), "PlainLiteral"));
	}

	/**
	 * <p>is_hexBinary.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-hexBinary")
	public static BooleanLiteral is_hexBinary(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"hexBinary");
	}

	/**
	 * <p>is_64Binary.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-base64Binary")
	public static BooleanLiteral is_64Binary(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"base64Binary");
	}

	/**
	 * <p>is_not_64Binary.</p>
	 *
	 * @param arg a {@link lupos.rif.builtin.Argument} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	@Builtin(Name = "is-literal-not-base64Binary")
	public static BooleanLiteral is_not_64Binary(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "base64Binary"));
	}
}
