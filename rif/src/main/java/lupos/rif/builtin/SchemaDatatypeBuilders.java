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

import java.net.URI;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;

@Namespace(value = "http://www.w3.org/2001/XMLSchema#")
public class SchemaDatatypeBuilders {

	@Builtin(Name = "boolean")
	public static BooleanLiteral buildBoolean(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral)
			return BooleanLiteral.create(BuiltinHelper
					.getBoolean((TypedLiteral) arg.arguments.get(0)));
		else
			return null;
	}

	@Builtin(Name = "anyURI")
	public static Literal buildAnyUri(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			try {
				URI.create(content.substring(1, content.length() - 1));
				return BuiltinHelper.createXSLiteral(content, "anyURI");
			} catch (IllegalArgumentException e) {
				return null;
			}
		} else
			return null;
	}

	@Builtin(Name = "base64Binary")
	public static Literal buildBase64Binary(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "base64Binary");
		} else
			return null;
	}

	@Builtin(Name = "double")
	public static Literal buildDouble(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "double");
		} else
			return null;
	}

	@Builtin(Name = "float")
	public static Literal buildFloat(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "float");
		} else
			return null;
	}

	@Builtin(Name = "hexBinary")
	public static Literal buildHexBinary(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "hexBinary");
		} else
			return null;
	}

	@Builtin(Name = "decimal")
	public static Literal buildDecimal(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "decimal");
		} else
			return null;
	}

	@Builtin(Name = "integer")
	public static Literal buildInteger(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "integer");
		} else
			return null;
	}

	@Builtin(Name = "long")
	public static Literal buildLong(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "long");
		} else
			return null;
	}

	@Builtin(Name = "int")
	public static Literal buildInt(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "int");
		} else
			return null;
	}

	@Builtin(Name = "short")
	public static Literal buildShort(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "short");
		} else
			return null;
	}

	@Builtin(Name = "byte")
	public static Literal buildByte(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "byte");
		} else
			return null;
	}

	@Builtin(Name = "nonNegativeInteger")
	public static Literal buildNonNegativeInteger(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1),
					"nonNegativeInteger");
		} else
			return null;
	}

	@Builtin(Name = "positiveInteger")
	public static Literal buildpositiveInteger(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1),
					"positiveInteger");
		} else
			return null;
	}

	@Builtin(Name = "unsignedLong")
	public static Literal buildUnsignedLong(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "unsignedLong");
		} else
			return null;
	}

	@Builtin(Name = "unsignedInt")
	public static Literal buildUnsignedInt(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "unsignedInt");
		} else
			return null;
	}

	@Builtin(Name = "unsignedShort")
	public static Literal buildUnsignedShort(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper
					.createXSLiteral(
							content.substring(1, content.length() - 1),
							"unsignedShort");
		} else
			return null;
	}

	@Builtin(Name = "unsignedByte")
	public static Literal buildUnsignedByte(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "unsignedByte");
		} else
			return null;
	}

	@Builtin(Name = "nonPositiveInteger")
	public static Literal buildNonPositiveInteger(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1),
					"nonPositiveInteger");
		} else
			return null;
	}

	@Builtin(Name = "negativeInteger")
	public static Literal buildNegativeInteger(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1),
					"negativeInteger");
		} else
			return null;
	}

	@Builtin(Name = "string")
	public static Literal buildString(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "string");
		} else
			return null;
	}

	@Builtin(Name = "normalizedString")
	public static Literal buildNormalizedString(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1),
					"normalizedString");
		} else
			return null;
	}

	@Builtin(Name = "token")
	public static Literal buildToken(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "token");
		} else
			return null;
	}

	@Builtin(Name = "language")
	public static Literal buildLanguage(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "language");
		} else
			return null;
	}

	@Builtin(Name = "Name")
	public static Literal buildName(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "Name");
		} else
			return null;
	}

	@Builtin(Name = "NCName")
	public static Literal buildNCName(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "NCName");
		} else
			return null;
	}

	@Builtin(Name = "NMTOKEN")
	public static Literal buildNMTOKEN(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "NMTOKEN");
		} else
			return null;
	}

	@Builtin(Name = "date")
	public static Literal buildDate(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "date");
		} else
			return null;
	}

	@Builtin(Name = "dateTime")
	public static Literal buildDateTime(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "dateTime");
		} else
			return null;
	}

	@Builtin(Name = "dateTimeStamp")
	public static Literal buildDateTimeStamp(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper
					.createXSLiteral(
							content.substring(1, content.length() - 1),
							"dateTimeStamp");
		} else
			return null;
	}

	@Builtin(Name = "time")
	public static Literal buildTime(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1), "time");
		} else
			return null;
	}

	@Builtin(Name = "dayTimeDuration")
	public static Literal buildDayTimeDuration(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1),
					"dayTimeDuration");
		} else
			return null;
	}

	@Builtin(Name = "yearMonthDuration")
	public static Literal buildYearMonthDuration(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createXSLiteral(
					content.substring(1, content.length() - 1),
					"yearMonthDuration");
		} else
			return null;
	}
}
