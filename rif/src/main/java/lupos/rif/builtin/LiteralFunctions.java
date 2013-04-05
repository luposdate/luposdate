/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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

import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteral;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-function#")
public class LiteralFunctions {

	@Builtin(Name = "PlainLiteral-from-string-lang")
	public static LanguageTaggedLiteral fromStringLang(final Argument arg) {
		if (arg.arguments.size() == 2
				&& arg.arguments.get(0) instanceof TypedLiteral
				&& arg.arguments.get(1) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0)).getContent();
			final String lang = ((TypedLiteral) arg.arguments.get(1)).getContent();
			return LiteralFactory.createLanguageTaggedLiteralWithoutLazyLiteral(content, lang.substring(1, lang.length() - 1));
		} else
			return null;
	}

	@Builtin(Name = "string-from-PlainLiteral")
	public static Literal stringFromPlainLiteral(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof Literal) {
			String content = null;
			if (arg.arguments.get(0) instanceof LanguageTaggedLiteral)
				content = ((LanguageTaggedLiteral) arg.arguments.get(0))
						.getContent();
			else
				content = ((TypedLiteral) arg.arguments.get(0)).getContent();
			return BuiltinHelper.createXSLiteral(content, "string");
		} else
			return null;
	}

	@Builtin(Name = "lang-from-PlainLiteral")
	public static Literal langFromPlainLiteral(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			if (!LiteralPredicates.is_plainLiteral(arg).value)
				return BuiltinHelper.createXSLiteral("", "string");

			String content = ((TypedLiteral) arg.arguments.get(0)).getContent();

			return BuiltinHelper.createXSLiteral(
					content.split("@")[1].replaceAll("\"", ""), "lang");
		} else
			return null;
	}

	@Builtin(Name = "PlainLiteral-compare")
	public static Literal PlainLiteral_compare(final Argument arg) {
		if (arg.arguments.size() == 2
				&& arg.arguments.get(0) instanceof Literal
				&& arg.arguments.get(1) instanceof Literal) {
			String content1 = ((Literal) arg.arguments.get(0)).originalString();
			String content2 = ((Literal) arg.arguments.get(1)).originalString();
			int result = content1.compareTo(content2) < 0 ? -1 : content1.compareTo(content2) == 0 ? 0 : 1;
			return BuiltinHelper.createXSLiteral(result,
					"integer");
		} else
			return null;
	}
}
