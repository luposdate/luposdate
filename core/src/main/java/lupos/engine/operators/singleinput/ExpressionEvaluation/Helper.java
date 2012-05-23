/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.engine.operators.singleinput.ExpressionEvaluation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.engine.operators.singleinput.TypeErrorException;
 
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression;

public class Helper {

	public static boolean booleanEffectiveValue(final Object o)
			throws TypeErrorException {
		if (o instanceof Boolean)
			return (Boolean) o;
		else if (o instanceof TypedLiteral) {
			final String type = ((TypedLiteral) o).getType();
			final String content = ((TypedLiteral) o).getContent();
			if (type.compareTo("<http://www.w3.org/2001/XMLSchema#boolean>") == 0) {
				return (content.compareTo("\"true\"") == 0 || content
						.compareTo("\"1\"") == 0);
			} else if (type
					.compareTo("<http://www.w3.org/2001/XMLSchema#string>") == 0) {
				return !(content.length() == 2);
			} else if (isNumeric(type)) {
				try {
					final BigDecimal bd = getBigDecimal(o);
					return !(bd.compareTo(new BigDecimal(0)) == 0);
				} catch (final Exception e) {
					throw new TypeErrorException();
				}
			}
		} else if (o instanceof CodeMapLiteral || o instanceof StringLiteral) {
			return !(o.toString().length() == 2);
		}
		throw new TypeErrorException();
	}

	public static boolean isNumeric(final String type) {
		if (isInteger(type))
			return true;
		else
			return isFloatingPoint(type);
	}

	public static boolean isFloatingPoint(final String type) {
		if (type.compareTo("<http://www.w3.org/2001/XMLSchema#double>") == 0
				|| type.compareTo("<http://www.w3.org/2001/XMLSchema#float>") == 0
				|| type.compareTo("<http://www.w3.org/2001/XMLSchema#decimal>") == 0)
			return true;
		else
			return false;
	}

	public static boolean isInteger(final String type) {
		return (type.compareTo("<http://www.w3.org/2001/XMLSchema#integer>") == 0
				|| type
						.compareTo("<http://www.w3.org/2001/XMLSchema#nonPositiveInteger>") == 0
				|| type
						.compareTo("<http://www.w3.org/2001/XMLSchema#negativeInteger>") == 0
				|| type.compareTo("<http://www.w3.org/2001/XMLSchema#long>") == 0
				|| type.compareTo("<http://www.w3.org/2001/XMLSchema#int>") == 0
				|| type.compareTo("<http://www.w3.org/2001/XMLSchema#short>") == 0
				|| type.compareTo("<http://www.w3.org/2001/XMLSchema#byte>") == 0
				|| type
						.compareTo("<http://www.w3.org/2001/XMLSchema#nonNegativeInteger>") == 0
				|| type
						.compareTo("<http://www.w3.org/2001/XMLSchema#unsignedLong>") == 0
				|| type
						.compareTo("<http://www.w3.org/2001/XMLSchema#unsignedInt>") == 0
				|| type
						.compareTo("<http://www.w3.org/2001/XMLSchema#unsignedShort>") == 0
				|| type
						.compareTo("<http://www.w3.org/2001/XMLSchema#unsignedByte>") == 0 || type
				.compareTo("<http://www.w3.org/2001/XMLSchema#positiveInteger>") == 0);
	}

	public static BigInteger getInteger(final Object a)
			throws TypeErrorException {
		if (a instanceof BigInteger)
			return (BigInteger) a;
		if (a instanceof TypedLiteral) {
			final TypedLiteral tl = (TypedLiteral) a;
			if (isInteger(tl.getType()))
				return new BigInteger(tl.getContent().substring(1,
						tl.getContent().length() - 1));
		}
		throw new TypeErrorException();
	}

	public static Float getFloat(final Object a) throws TypeErrorException {
		if (a instanceof Float)
			return (Float) a;
		if (a instanceof BigInteger)
			return ((BigInteger) a).floatValue();
		if (a instanceof Double)
			return ((Double) a).floatValue();
		if (a instanceof BigDecimal)
			return ((BigDecimal) a).floatValue();
		if (a instanceof TypedLiteral) {
			final TypedLiteral tl = (TypedLiteral) a;
			if (isInteger(tl.getType()))
				return (new BigInteger(tl.getContent().substring(1,
						tl.getContent().length() - 1))).floatValue();
			if (tl.getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#float>") == 0
					|| tl.getType().compareTo(
							"<http://www.w3.org/2001/XMLSchema#double>") == 0
					|| tl.getType().compareTo(
							"<http://www.w3.org/2001/XMLSchema#decimal>") == 0)
				return Float.parseFloat(tl.getContent().substring(1,
						tl.getContent().length() - 1));
		}
		throw new TypeErrorException();
	}

	public static Double getDouble(final Object a) throws TypeErrorException {
		if (a instanceof Double)
			return (Double) a;
		if (a instanceof Float)
			return ((Float) a).doubleValue();
		if (a instanceof BigInteger)
			return ((BigInteger) a).doubleValue();
		if (a instanceof BigDecimal)
			return ((BigDecimal) a).doubleValue();
		if (a instanceof TypedLiteral) {
			final TypedLiteral tl = (TypedLiteral) a;
			if (isInteger(tl.getType()))
				return (new BigInteger(tl.getContent().substring(1,
						tl.getContent().length() - 1))).doubleValue();
			if (tl.getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#float>") == 0)
				return (double) Float.parseFloat(tl.getContent().substring(1,
						tl.getContent().length() - 1));
			if (tl.getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#double>") == 0
					|| tl.getType().compareTo(
							"<http://www.w3.org/2001/XMLSchema#decimal>") == 0)
				return Double.parseDouble(tl.getContent().substring(1,
						tl.getContent().length() - 1));
		}
		throw new TypeErrorException();
	}

	public static BigDecimal getBigDecimal(final Object a)
			throws TypeErrorException {
		if (a instanceof BigDecimal)
			return (BigDecimal) a;
		if (a instanceof Double)
			return new BigDecimal((Double) a);
		if (a instanceof Float)
			return new BigDecimal((Float) a);
		if (a instanceof BigInteger)
			return new BigDecimal((BigInteger) a);
		if (a instanceof TypedLiteral) {
			final TypedLiteral tl = (TypedLiteral) a;
			if (isInteger(tl.getType()))
				return new BigDecimal(new BigInteger(tl.getContent().substring(
						1, tl.getContent().length() - 1)));
			if (tl.getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#float>") == 0)
				return new BigDecimal(Float.parseFloat(tl.getContent()
						.substring(1, tl.getContent().length() - 1)));
			if (tl.getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#double>") == 0)
				return new BigDecimal(Double.parseDouble(tl.getContent()
						.substring(1, tl.getContent().length() - 1)));
			if (tl.getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#decimal>") == 0)
				return new BigDecimal(tl.getContent().substring(1,
						tl.getContent().length() - 1));
		}
		throw new TypeErrorException();
	}

	public static Date getDate(final String a) throws TypeErrorException,
			java.text.ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat();
		try {
			sdf.applyPattern("yyyy-MM-dd'Z'");
			return sdf.parse(a);
		} catch (Exception e1) {
			try {
				sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
				return sdf.parse(a);
			} catch (Exception e2) {
				try {
					sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss");
					return sdf.parse(a);
				} catch (Exception e3) {
					try {
						sdf.applyPattern("yyyy-MM-dd'Z'");
						return sdf.parse(a);
					} catch (Exception e4) {
						sdf.applyPattern("yyyy-MM-dd");
						return sdf.parse(a);
					}
				}
			}
		}
	}

	public static String getTz(final String a) throws TypeErrorException,
			java.text.ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat();
		try {
			sdf.applyPattern("yyyy-MM-dd'Z'");
			sdf.parse(a);
			return "\"Z\"";
		} catch (Exception e1) {
			try {
				sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
				sdf.parse(a);
				return "\"Z\"";
			} catch (Exception e2) {
				try {
					sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss");
					sdf.parse(a);
					if (a.length() == 19)
						return "\"\"";
					else
						return "\"" + a.substring(19) + "\"";
				} catch (Exception e3) {
					try {
						sdf.applyPattern("yyyy-MM-dd'Z'");
						sdf.parse(a);
						return "\"Z\"";
					} catch (Exception e4) {
						sdf.applyPattern("yyyy-MM-dd");
						sdf.parse(a);
						return "\"\"";
					}
				}
			}
		}
	}

	public static String getTzAndTypeCheck(final Object a)
			throws TypeErrorException {
		try {
			return getTz(getDateString(a));
		} catch (Exception e) {
		}
		throw new TypeErrorException();
	}

	public static String getTimezoneAndTypeCheck(final Object a)
			throws TypeErrorException {
		try {
			String timezone = getTz(getDateString(a));
			if (timezone.compareTo("\"Z\"") == 0) {
				return "\"PT0S\"";
			} else if (timezone.compareTo("\"\"") == 0) {
				throw new TypeErrorException();
			} else {
				timezone = Helper
						.unquote(timezone);
				String result = "\"";
				if (timezone.startsWith("-")) {
					result += "-";
					timezone = timezone.substring(1);
				} else if (timezone.startsWith("+")) {
					result += "+";
					timezone = timezone.substring(1);
				}
				result += "PT";
				Character c = timezone.charAt(0);
				if (Character.isDigit(c)) {
					if (c != '0') {
						result += c.toString();
					}
					timezone = timezone.substring(1);
					c = timezone.charAt(0);
					if (Character.isDigit(c)) {
						result += c.toString();
					}
					result += "H";
				}
				result += "\"";
				return result;
			}

		} catch (Exception e) {
		}
		throw new TypeErrorException();
	}

	public static String getDateString(final Object a)
			throws TypeErrorException {
		try {
			if (isDate(a)) {
				if (a instanceof Date) {
					SimpleDateFormat sdf = new SimpleDateFormat();
					sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss");
					return sdf.format((Date) a);
				} else {
					return getContent((TypedLiteral) a);
				}
			}
		} catch (Exception e) {
		}
		throw new TypeErrorException();
	}

	public static String getContent(TypedLiteral a) throws TypeErrorException {
		try {
			return (a).getContent().substring(1, (a).getContent().length() - 1);
		} catch (final Exception e) {
			throw new TypeErrorException();
		}
	}

	public static Date getDate(final TypedLiteral a) throws TypeErrorException {
		try {
			return getDate(getContent(a));
		} catch (final Exception e) {
			throw new TypeErrorException();
		}
	}

	public static Date getDateAndTypeCheck(final Object a)
			throws TypeErrorException {
		if (isDate(a)) {
			if (a instanceof Date)
				return (Date) a;
			else
				return getDate((TypedLiteral) a);
		} else
			throw new TypeErrorException();
	}

	/**
	 * compares two values (Strings, ints, or doubles)
	 * 
	 * @param a
	 *            : fist value to compare
	 * @param b
	 *            : second value to compare
	 * @return boolean: true if a=b, else false
	 * @throws TypeErrorException
	 */
	public static boolean equals(Object a, Object b) throws TypeErrorException {
		try {
			if (a.equals(b))
				return true;
		} catch (final Exception e) {
		}
		if (a instanceof LazyLiteral)
			a = ((LazyLiteral) a).getLiteral();
		if (b instanceof LazyLiteral)
			b = ((LazyLiteral) b).getLiteral();
		typeCheck(a, b);
		if ((a instanceof String || a instanceof Character)
				&& (b instanceof String || b instanceof Character)) {
			final String tmp_a = "".concat(String.valueOf(a));
			final String tmp_b = "".concat(String.valueOf(b));
			return tmp_a.equals(tmp_b);
		} else {
			if ((a instanceof String || a instanceof Character)
					&& (b instanceof Literal)
					|| (b instanceof String || b instanceof Character)
					&& (a instanceof Literal)) {
				return getString(a).equals(getString(b));
			} else if (a instanceof TypedLiteral
					&& (b instanceof BigInteger || b instanceof BigDecimal)
					|| b instanceof TypedLiteral
					&& (a instanceof BigInteger || a instanceof BigDecimal)) {
				final Object type = getCoercionType(a, b);
				if (type == BigInteger.class) {
					return getInteger(a).compareTo(getInteger(b)) == 0;
				} else if (type == Float.class) {
					return getFloat(a).compareTo(getFloat(b)) == 0;
				} else if (type == Double.class) {
					return getDouble(a).compareTo(getDouble(b)) == 0;
				} else if (type == BigDecimal.class) {
					return getBigDecimal(a).compareTo(getBigDecimal(b)) == 0;
				} else
					throw new TypeErrorException();
			} else if (a instanceof TypedLiteral && b instanceof TypedLiteral) {

				// if ((((TypedLiteral) a).getType().compareTo(
				// "<http://www.w3.org/2001/XMLSchema#date>") == 0
				// || ((TypedLiteral) a).getType().compareTo(
				// "<http://www.w3.org/2001/XMLSchema#dateTime>") == 0 ||
				// ((TypedLiteral) a)
				// .getType().compareTo(
				// "<http://www.w3.org/2001/XMLSchema#time>") == 0)
				// && (((TypedLiteral) b).getType().compareTo(
				// "<http://www.w3.org/2001/XMLSchema#date>") == 0
				// || ((TypedLiteral) b)
				// .getType()
				// .compareTo(
				// "<http://www.w3.org/2001/XMLSchema#dateTime>") == 0 ||
				// ((TypedLiteral) b)
				// .getType()
				// .compareTo(
				// "<http://www.w3.org/2001/XMLSchema#time>") == 0)) {
				// return (getDate((TypedLiteral) a).compareTo(
				// getDate((TypedLiteral) b)) == 0);
				// } else {
				return ((TypedLiteral) a).getContent().compareTo(
						((TypedLiteral) b).getContent()) == 0;
				// }
			}
		}

		try {
			return a.equals(b);
		} catch (final Exception e) {
			return a == b;
		}
	}

	public static void typeCheck(final Object a, final Object b)
			throws TypeErrorException {
		if ((a instanceof BigInteger || a instanceof BigDecimal
				|| a instanceof Float || a instanceof Double)
				&& !(b instanceof BigInteger || b instanceof BigDecimal
						|| b instanceof Float || b instanceof Double)) {
			if (b instanceof TypedLiteral) {
				if (!(isNumeric(((TypedLiteral) b).getType())))
					throw new TypeErrorException();
			} else
				throw new TypeErrorException();
		} else if ((b instanceof BigInteger || b instanceof BigDecimal
				|| b instanceof Float || b instanceof Double)
				&& !(a instanceof BigInteger || a instanceof BigDecimal
						|| a instanceof Float || a instanceof Double)) {
			if (a instanceof TypedLiteral) {
				if (!(isNumeric(((TypedLiteral) a).getType())))
					throw new TypeErrorException();
			} else
				throw new TypeErrorException();
		} else if (a instanceof String && !(b instanceof String)) {
			if (b instanceof TypedLiteral) {
				if (((TypedLiteral) b).getType().compareTo(
						"<http://www.w3.org/2001/XMLSchema#string>") != 0)
					throw new TypeErrorException();
			} else if (b.getClass() != CodeMapLiteral.class
					&& b.getClass() != StringLiteral.class)
				throw new TypeErrorException();
		} else if (b instanceof String && !(a instanceof String)) {
			if (a instanceof TypedLiteral) {
				if (((TypedLiteral) a).getType().compareTo(
						"<http://www.w3.org/2001/XMLSchema#string>") != 0)
					throw new TypeErrorException();
			} else if (a.getClass() != CodeMapLiteral.class
					&& a.getClass() != StringLiteral.class)
				throw new TypeErrorException();
		} else if (a instanceof TypedLiteral && b instanceof TypedLiteral) {
			final String typea = ((TypedLiteral) a).getType();
			final String typeb = ((TypedLiteral) b).getType();
			if (typea.compareTo(typeb) != 0) {
				if (isNumeric(typea) && isNumeric(typeb))
					return;
				else {
					// check compatible types
					if (!((typea
							.compareTo("<http://www.w3.org/2001/XMLSchema#dateTime>") == 0
							|| typea
									.compareTo("<http://www.w3.org/2001/XMLSchema#time>") == 0 || typea
							.compareTo("<http://www.w3.org/2001/XMLSchema#date>") == 0) && (typeb
							.compareTo("<http://www.w3.org/2001/XMLSchema#dateTime>") == 0
							|| typeb
									.compareTo("<http://www.w3.org/2001/XMLSchema#time>") == 0 || typeb
							.compareTo("<http://www.w3.org/2001/XMLSchema#date>") == 0)))
						throw new TypeErrorException();
				}
			}
		} else if ((a instanceof CodeMapLiteral || a instanceof StringLiteral)
				&& b instanceof TypedLiteral) {
			if (((TypedLiteral) b).getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#string>") != 0)
				throw new TypeErrorException();
		} else if ((b instanceof CodeMapLiteral || b instanceof StringLiteral)
				&& a instanceof TypedLiteral) {
			if (((TypedLiteral) a).getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#string>") != 0)
				throw new TypeErrorException();
		}
	}

	public static Object getCoercionType(final Object a, final Object b)
			throws TypeErrorException {
		final Object typea = getType(a);
		final Object typeb = getType(b);
		if (typea == BigInteger.class && typeb == BigInteger.class)
			return BigInteger.class;
		else if ((typea == BigDecimal.class || typea == BigInteger.class)
				&& (typeb == BigDecimal.class || typeb == BigInteger.class))
			return BigDecimal.class;
		else if ((typea == Float.class || typea == BigInteger.class || typea == BigDecimal.class)
				&& (typeb == Float.class || typeb == BigInteger.class || typeb == BigDecimal.class))
			return Float.class;
		else if ((typea == Float.class || typea == Double.class
				|| typea == BigInteger.class || typea == BigDecimal.class)
				&& (typeb == Float.class || typeb == Double.class
						|| typeb == BigInteger.class || typeb == BigDecimal.class))
			return Double.class;
		throw new TypeErrorException();
	}

	public static Object getType(final Object a) throws TypeErrorException {
		if (a instanceof BigInteger)
			return BigInteger.class;
		if (a instanceof Double)
			return Double.class;
		if (a instanceof Float)
			return Float.class;
		if (a instanceof BigDecimal)
			return BigDecimal.class;
		if (a instanceof TypedLiteral) {
			final TypedLiteral tl = (TypedLiteral) a;
			if (isInteger(tl.getType()))
				return BigInteger.class;
			if (tl.getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#double>") == 0)
				return Double.class;
			if (tl.getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#float>") == 0)
				return Float.class;
			if (tl.getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#decimal>") == 0)
				return BigDecimal.class;
		}
		throw new TypeErrorException();
	}

	public static boolean NOTequals(Object a, Object b)
			throws TypeErrorException {
		if (a instanceof LazyLiteral && b instanceof LazyLiteral)
			return !((LazyLiteral) a).equals(b);
		if (a instanceof LazyLiteral)
			a = ((LazyLiteral) a).getLiteral();
		if (b instanceof LazyLiteral)
			b = ((LazyLiteral) b).getLiteral();
		if ((a instanceof TypedLiteral) && (b instanceof TypedLiteral)) {
			if (!(((TypedLiteral) a).getType()
					.startsWith("<http://www.w3.org/2001/XMLSchema#"))
					|| !(((TypedLiteral) b).getType()
							.startsWith("<http://www.w3.org/2001/XMLSchema#"))) {
				return false;
			}
			try {
				final Object type = getCoercionType(a, b);
				if (type == BigInteger.class) {
					getInteger(a);
					getInteger(b);
				} else if (type == Double.class) {
					getDouble(a);
					getDouble(b);
				} else if (type == Float.class) {
					getFloat(a);
					getFloat(b);
				} else if (type == BigDecimal.class) {
					getBigDecimal(a);
					getBigDecimal(b);
				}
			} catch (final TypeErrorException tee) {
			} catch (final NumberFormatException nfe) {
				return false;
			}
		}
		if (a instanceof TypedLiteral && b instanceof TypedLiteral) {
			if ((((TypedLiteral) a).getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#date>") == 0
					|| ((TypedLiteral) a).getType().compareTo(
							"<http://www.w3.org/2001/XMLSchema#dateTime>") == 0 || ((TypedLiteral) a)
					.getType().compareTo(
							"<http://www.w3.org/2001/XMLSchema#time>") == 0)
					&& (((TypedLiteral) b).getType().compareTo(
							"<http://www.w3.org/2001/XMLSchema#date>") == 0
							|| ((TypedLiteral) b)
									.getType()
									.compareTo(
											"<http://www.w3.org/2001/XMLSchema#dateTime>") == 0 || ((TypedLiteral) b)
							.getType().compareTo(
									"<http://www.w3.org/2001/XMLSchema#time>") == 0)) {
				return (getDate((TypedLiteral) a).compareTo(
						getDate((TypedLiteral) b)) != 0);
			}
		}
		return !equals(a, b);
	}

	public static boolean le(final Object a, final Object b)
			throws TypeErrorException {
		return equals(a, b) || less(a, b);
	}

	public static boolean ge(final Object a, final Object b)
			throws TypeErrorException {
		return equals(a, b) || greater(a, b);
	}

	public static boolean less(final Object a, final Object b)
			throws TypeErrorException {
		return greater(b, a);
	}

	public static boolean greater(Object a, Object b) throws TypeErrorException {
		if (a instanceof LazyLiteral)
			a = ((LazyLiteral) a).getLiteral();
		if (b instanceof LazyLiteral)
			b = ((LazyLiteral) b).getLiteral();
		typeCheck(a, b);
		if ((a.getClass() == b.getClass())
				|| (a instanceof TypedLiteral && b instanceof TypedLiteral)) {
			try {
				if (a instanceof TypedLiteral) {
					final String typea = ((TypedLiteral) a).getType();
					final String typeb = ((TypedLiteral) b).getType();
					if (/* the following is clear! */
					(typea.compareTo("<http://www.w3.org/2001/XMLSchema#date>") == 0
							|| typea
									.compareTo("<http://www.w3.org/2001/XMLSchema#dateTime>") == 0 || typea
							.compareTo("<http://www.w3.org/2001/XMLSchema#time>") == 0)
							&& (typeb
									.compareTo("<http://www.w3.org/2001/XMLSchema#date>") == 0
									|| typeb
											.compareTo("<http://www.w3.org/2001/XMLSchema#dateTime>") == 0 || typeb
									.compareTo("<http://www.w3.org/2001/XMLSchema#time>") == 0)) {

						if /* The following is questionable */
						(typea.compareTo(typeb) != 0)
							throw new TypeErrorException();

						final Date da = getDate((TypedLiteral) a);
						final Date db = getDate((TypedLiteral) b);
						return (da.compareTo(db) > 0);
					}
					try {
						final Object type = getCoercionType(a, b);
						if (type == BigInteger.class) {
							return getInteger(a).compareTo(getInteger(b)) > 0;
						} else if (type == Double.class) {
							return getDouble(a) > getDouble(b);
						} else if (type == Float.class) {
							return getFloat(a) > getFloat(b);
						} else if (type == BigDecimal.class) {
							return getBigDecimal(a).compareTo(getBigDecimal(b)) > 0;
						}
					} catch (final TypeErrorException tee) {
					}
				}
				if (a instanceof Literal) {
					return (a.toString().compareTo(b.toString()) > 0);
				}
				if (a instanceof String) {
					return (((String) a).compareTo((String) b)) > 0;
				}
				if (a instanceof Character) {
					return (((Character) a).compareTo((Character) b)) > 0;
				}
				if (a instanceof BigInteger) {
					return ((BigInteger) a).compareTo((BigInteger) b) > 0;
				}
				if (a instanceof Double) {
					return (Double) a > (Double) b;
				}
				if (a instanceof Float) {
					return (Float) a > (Float) b;
				}
				if (a instanceof BigDecimal) {
					return ((BigDecimal) a).compareTo((BigDecimal) b) > 0;
				}
			} catch (final TypeErrorException e) {
				throw e;
			} catch (final Exception e) {
				System.out.println("not comparable via \"greater than\"\n" + e);
			}
		} else {
			if ((a instanceof Number && b instanceof TypedLiteral)
					|| (b instanceof Number && a instanceof TypedLiteral)
					|| (a instanceof Number && b instanceof Number)) {
				final Object type = getCoercionType(a, b);
				if (type == BigInteger.class) {
					return getInteger(a).compareTo(getInteger(b)) > 0;
				} else if (type == Double.class) {
					return getDouble(a) > getDouble(b);
				} else if (type == Float.class) {
					return getFloat(a) > getFloat(b);
				} else if (type == BigDecimal.class) {
					return getBigDecimal(a).compareTo(getBigDecimal(b)) > 0;
				}
			} else if ((a instanceof String || a instanceof Character
					|| a instanceof CodeMapLiteral || a instanceof StringLiteral)
					&& (b instanceof String || b instanceof Character
							|| b instanceof CodeMapLiteral || b instanceof StringLiteral)) {
				final String aa = getString(a);
				final String bb = getString(b);
				return String.valueOf(aa).compareTo(String.valueOf(bb)) > 0;
			}
			if ((a instanceof StringLiteral && b instanceof TypedLiteral)
					|| (b instanceof StringLiteral && a instanceof TypedLiteral)) {
				throw new TypeErrorException();
			}

			else {
				throw new TypeErrorException();
			}
		}
		return false;
	}

	public static String getString(final Object o) {
		if (o instanceof TypedLiteral) {
			final TypedLiteral tl = (TypedLiteral) o;
			if (isNumeric(tl.getType()))
				return tl.toString();
			else if (tl.getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#string>") == 0)
				return tl.getContent().substring(1,
						tl.getContent().length() - 1);
			else
				return tl.toString();
		} else if (o instanceof Literal || o instanceof String) {
			if ((o.toString().startsWith("\"") && o.toString().endsWith("\""))
					|| (o.toString().startsWith("'") && o.toString().endsWith(
							"'")))
				return o.toString().substring(1, o.toString().length() - 1);
			else
				return o.toString();
		} else
			return o.toString();
	}

	public static Literal getLiteral(final Object resultOfEvalTree) {
		if (resultOfEvalTree instanceof Literal)
			return (Literal) resultOfEvalTree;
		try {
			if (resultOfEvalTree instanceof BigInteger
					|| resultOfEvalTree instanceof Integer) {
				return LiteralFactory.createTypedLiteral("\""
						+ resultOfEvalTree.toString() + "\"",
						"<http://www.w3.org/2001/XMLSchema#integer>");
			} else if (resultOfEvalTree instanceof Double) {
				return LiteralFactory.createTypedLiteral("\""
						+ resultOfEvalTree.toString() + "\"",
						"<http://www.w3.org/2001/XMLSchema#double>");
			} else if (resultOfEvalTree instanceof Float) {
				return LiteralFactory.createTypedLiteral("\""
						+ resultOfEvalTree.toString() + "\"",
						"<http://www.w3.org/2001/XMLSchema#float>");
			} else if (resultOfEvalTree instanceof BigDecimal) {
				return LiteralFactory.createTypedLiteral("\""
						+ resultOfEvalTree.toString() + "\"",
						"<http://www.w3.org/2001/XMLSchema#decimal>");
			} else if (resultOfEvalTree instanceof String) {
				return LiteralFactory.createTypedLiteral(resultOfEvalTree
						.toString(),
						"<http://www.w3.org/2001/XMLSchema#string>");
			} else if (resultOfEvalTree instanceof Boolean) {
				return LiteralFactory.createTypedLiteral("\""
						+ resultOfEvalTree.toString() + "\"",
						"<http://www.w3.org/2001/XMLSchema#boolean>");
			} else if (resultOfEvalTree instanceof Date) {
				return LiteralFactory.createTypedLiteral("\""
						+ resultOfEvalTree.toString() + "\"",
						"<http://www.w3.org/2001/XMLSchema#dateTime>");
			}
		} catch (final URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	public static boolean matchXerces(final String s, final String pattern,
			final String flags) {
		final RegularExpression regexPattern = makePatternXerces(pattern, flags);
		if (regexPattern == null)
			return false;
		else
			return regexPattern.matches(s);
	}
	
	public static String replaceXerces(String s, final String pattern,
			final String flags, final String replacement) {
		final RegularExpression regexPattern = makePatternXerces(pattern, flags);
		if (regexPattern == null)
			return s;
		else {
			Match m = new Match();
			if(regexPattern.matches(s,m)){
				String result = "";
				while(regexPattern.matches(s,m)){
					int numberOfGroups = m.getNumberOfGroups();
					if(numberOfGroups>0){
						String interReplacement = new String(replacement);
						if(numberOfGroups>1){
							for(int i=numberOfGroups-1; i>0; i--){ // start at the end to not have problems that $1 is replaced for a string $10 but $10 should be replaced (the same leading prefixes!)
								int start = m.getBeginning(i);
								int end = m.getEnd(i);								
								String r = (start>=0 && end>=0)?s.substring(start, end):"";
								interReplacement=interReplacement.replaceFirst("[$]"+i, r);
							}
						}
						result+=s.substring(0, m.getBeginning(0))+interReplacement;
						s=s.substring(m.getEnd(0));
					}
				}
				return result+s;
			} else return s;
		}
	}


	private static RegularExpression makePatternXerces(final String patternStr,
			final String flags) {
		// Input : only m s i x
		// Check/modify flags.
		// Always "u", never patternStr
		// x: Remove whitespace characters (#x9, #xA, #xD and #x20) unless in []
		// classes
		try {
			return new RegularExpression(patternStr, flags);
		} catch (final Exception pEx) {
			System.err.println("Regex: Pattern exception: " + pEx);
			return null;
		}
	}

	public static Object cast(final String type, final Object content)
			throws TypeErrorException {
		// get the real content as string...
		String realContent;
		if (content instanceof TypedLiteral)
			realContent = ((TypedLiteral) content).getContent();
		else if (content instanceof CodeMapLiteral
				|| content instanceof StringLiteral)
			realContent = content.toString();
		else
			// e.g. if(content instanceof Number)
			realContent = "\"" + content.toString() + "\"";

		final String realContentWithoutDoubleQuote = realContent.substring(1,
				realContent.length() - 1);
		// now check if the content can be casted...
		if (isInteger(type)) {
			try {
				final BigInteger bi = new BigInteger(
						realContentWithoutDoubleQuote);
				// there are no more restrictions for
				// type.compareTo("<http://www.w3.org/2001/XMLSchema#integer>")
				// == 0
				if (type
						.compareTo("<http://www.w3.org/2001/XMLSchema#nonPositiveInteger>") == 0
						&& bi.compareTo(new BigInteger("0")) > 0)
					throw new TypeErrorException();
				else if (type
						.compareTo("<http://www.w3.org/2001/XMLSchema#negativeInteger>") == 0
						&& bi.compareTo(new BigInteger("0")) >= 0)
					throw new TypeErrorException();
				else if (type
						.compareTo("<http://www.w3.org/2001/XMLSchema#long>") == 0
						&& bi.compareTo(new BigInteger("-9223372036854775808")) < 0
						&& bi.compareTo(new BigInteger("9223372036854775807")) > 0)
					throw new TypeErrorException();
				else if (type
						.compareTo("<http://www.w3.org/2001/XMLSchema#int>") == 0
						&& bi.compareTo(new BigInteger("-2147483648")) < 0
						&& bi.compareTo(new BigInteger("2147483647")) > 0)
					throw new TypeErrorException();
				else if (type
						.compareTo("<http://www.w3.org/2001/XMLSchema#short>") == 0
						&& bi.compareTo(new BigInteger("-32768")) < 0
						&& bi.compareTo(new BigInteger("32767")) > 0)
					throw new TypeErrorException();
				else if (type
						.compareTo("<http://www.w3.org/2001/XMLSchema#byte>") == 0
						&& bi.compareTo(new BigInteger("-128")) < 0
						&& bi.compareTo(new BigInteger("127")) > 0)
					throw new TypeErrorException();
				else if (type
						.compareTo("<http://www.w3.org/2001/XMLSchema#nonNegativeInteger>") == 0
						&& bi.compareTo(new BigInteger("0")) < 0)
					throw new TypeErrorException();
				else if (type
						.compareTo("<http://www.w3.org/2001/XMLSchema#unsignedLong>") == 0
						&& bi.compareTo(new BigInteger("0")) < 0
						&& bi.compareTo(new BigInteger("18446744073709551615")) > 0)
					throw new TypeErrorException();
				else if (type
						.compareTo("<http://www.w3.org/2001/XMLSchema#unsignedInt>") == 0
						&& bi.compareTo(new BigInteger("0")) < 0
						&& bi.compareTo(new BigInteger("4294967295")) > 0)
					throw new TypeErrorException();
				else if (type
						.compareTo("<http://www.w3.org/2001/XMLSchema#unsignedShort>") == 0
						&& bi.compareTo(new BigInteger("0")) < 0
						&& bi.compareTo(new BigInteger("65535")) > 0)
					throw new TypeErrorException();
				else if (type
						.compareTo("<http://www.w3.org/2001/XMLSchema#unsignedByte>") == 0
						&& bi.compareTo(new BigInteger("0")) < 0
						&& bi.compareTo(new BigInteger("255")) > 0)
					throw new TypeErrorException();
				else if (type
						.compareTo("<http://www.w3.org/2001/XMLSchema#positiveInteger>") == 0
						&& bi.compareTo(new BigInteger("0")) <= 0)
					throw new TypeErrorException();
			} catch (final NumberFormatException nfe) {
				throw new TypeErrorException();
			}

		} else if (type.compareTo("<http://www.w3.org/2001/XMLSchema#float>") == 0) {
			try {
				Float.parseFloat(realContentWithoutDoubleQuote);
			} catch (final NumberFormatException nfe) {
				throw new TypeErrorException();
			}
		} else if (type.compareTo("<http://www.w3.org/2001/XMLSchema#double>") == 0) {
			try {
				Double.parseDouble(realContentWithoutDoubleQuote);
			} catch (final NumberFormatException nfe) {
				throw new TypeErrorException();
			}
		} else if (type.compareTo("<http://www.w3.org/2001/XMLSchema#decimal>") == 0) {
			try {
				new BigDecimal(realContentWithoutDoubleQuote);
				// any exponent is not allowed for the representation of
				// decimals! (The rest has been checked in the previous line!)
				if (realContentWithoutDoubleQuote.contains("e")
						|| realContentWithoutDoubleQuote.contains("E")) {
					throw new TypeErrorException();
				}
			} catch (final NumberFormatException nfe) {
				throw new TypeErrorException();
			}
		} else if (isDate(type)) {
			try {
				getDate(realContentWithoutDoubleQuote);
			} catch (final Exception e) {
				throw new TypeErrorException();
			}
		} else if (type.compareTo("<http://www.w3.org/2001/XMLSchema#boolean>") == 0) {
			if (!(realContentWithoutDoubleQuote.compareTo("true") == 0
					|| realContentWithoutDoubleQuote.compareTo("false") == 0
					|| realContentWithoutDoubleQuote.compareTo("0") == 0 || realContentWithoutDoubleQuote
					.compareTo("1") == 0))
				throw new TypeErrorException();
		}

		// created casted TypedLiteral!
		try {
			return LiteralFactory.createTypedLiteral(realContent, type);
		} catch (final URISyntaxException e) {
			throw new TypeErrorException();
		}
	}

	public static boolean isDate(final Object a) {
		if (a instanceof Date)
			return true;
		else if (a instanceof TypedLiteral) {
			final String type = ((TypedLiteral) a).getType();
			if (type.compareTo("<http://www.w3.org/2001/XMLSchema#date>") == 0
					|| type
							.compareTo("<http://www.w3.org/2001/XMLSchema#dateTime>") == 0
					|| type
							.compareTo("<http://www.w3.org/2001/XMLSchema#time>") == 0)
				return true;
		} else if (a instanceof String) {
			final String type = (String) a;
			if (type.compareTo("<http://www.w3.org/2001/XMLSchema#date>") == 0
					|| type
							.compareTo("<http://www.w3.org/2001/XMLSchema#dateTime>") == 0
					|| type
							.compareTo("<http://www.w3.org/2001/XMLSchema#time>") == 0)
				return true;
		}
		return false;
	}

	public static String trim(final String s) {
		if (s.startsWith("'''") && s.endsWith("'''"))
			return s.substring(3, s.length() - 3);
		else if (s.startsWith("\"\"\"") && s.endsWith("\"\"\""))
			return s.substring(3, s.length() - 3);
		else if (s.startsWith("\"") && s.endsWith("\""))
			return s.substring(1, s.length() - 1);
		else if (s.startsWith("'") && s.endsWith("'"))
			return s.substring(1, s.length() - 1);
		else
			return s;
	}

	public static String getOriginalValueString(final Object o) {
		if (o instanceof Literal)
			return ((Literal) o).originalString();
		else
			return o.toString();
	}

	public static Object addNumericValues(Object leftObject, Object rightObject)
			throws TypeErrorException {
		Object type = Helper.getCoercionType(leftObject, rightObject);
		if (type == BigInteger.class) {
			BigInteger left = Helper.getInteger(leftObject);
			BigInteger right = Helper.getInteger(rightObject);
			return left.add(right);
		} else if (type == BigDecimal.class) {
			BigDecimal left = Helper.getBigDecimal(leftObject);
			BigDecimal right = Helper.getBigDecimal(rightObject);
			return left.add(right);
		} else if (type == Float.class) {
			Float left = Helper.getFloat(leftObject);
			Float right = Helper.getFloat(rightObject);
			return left + right;
		} else if (type == Double.class) {
			Double left = Helper.getDouble(leftObject);
			Double right = Helper.getDouble(rightObject);
			return left + right;
		}
		throw new TypeErrorException();
	}

	public static Object subtractNumericValues(Object leftObject,
			Object rightObject) throws TypeErrorException {
		Object type = Helper.getCoercionType(leftObject, rightObject);
		if (type == BigInteger.class) {
			BigInteger left = Helper.getInteger(leftObject);
			BigInteger right = Helper.getInteger(rightObject);
			return left.subtract(right);
		} else if (type == BigDecimal.class) {
			BigDecimal left = Helper.getBigDecimal(leftObject);
			BigDecimal right = Helper.getBigDecimal(rightObject);
			return left.subtract(right);
		} else if (type == Float.class) {
			Float left = Helper.getFloat(leftObject);
			Float right = Helper.getFloat(rightObject);
			return left - right;
		} else if (type == Double.class) {
			Double left = Helper.getDouble(leftObject);
			Double right = Helper.getDouble(rightObject);
			return left - right;
		}
		throw new TypeErrorException();
	}

	public static Object multiplyNumericValues(Object leftObject,
			Object rightObject) throws TypeErrorException {
		Object type = Helper.getCoercionType(leftObject, rightObject);
		if (type == BigInteger.class) {
			BigInteger left = Helper.getInteger(leftObject);
			BigInteger right = Helper.getInteger(rightObject);
			return left.multiply(right);
		} else if (type == BigDecimal.class) {
			BigDecimal left = Helper.getBigDecimal(leftObject);
			BigDecimal right = Helper.getBigDecimal(rightObject);
			return left.multiply(right);
		} else if (type == Float.class) {
			Float left = Helper.getFloat(leftObject);
			Float right = Helper.getFloat(rightObject);
			return left * right;
		} else if (type == Double.class) {
			Double left = Helper.getDouble(leftObject);
			Double right = Helper.getDouble(rightObject);
			return left * right;
		}
		throw new TypeErrorException();
	}

	/**
	 * Divide two Values and returns the result as an BigDecimal for BigInteger
	 * and BigDecimal and as Float for Float and as Double for Double
	 * 
	 * @param leftObject
	 * @param rightObject
	 * @return
	 * @throws TypeErrorException
	 */
	public static Object divideNumericValues(Object leftObject,
			Object rightObject) throws TypeErrorException {
		try {
			Object type = Helper.getCoercionType(leftObject, rightObject);
			if (type == BigInteger.class || type == BigDecimal.class) {
				BigDecimal left = Helper.getBigDecimal(leftObject);
				BigDecimal right = Helper.getBigDecimal(rightObject);
				try{
					return left.divide(right);
				} catch(ArithmeticException e){
					return left.divide(right, 5, BigDecimal.ROUND_HALF_UP);
				}
			} else if (type == Float.class) {
				Float left = Helper.getFloat(leftObject);
				Float right = Helper.getFloat(rightObject);
				return left / right;
			} else if (type == Double.class) {
				Double left = Helper.getDouble(leftObject);
				Double right = Helper.getDouble(rightObject);
				return left / right;
			}
		} catch(Exception e)
		{}
		throw new TypeErrorException();
	}

	public static Object unlazy(final Object o) {
		if (o instanceof LazyLiteral)
			return ((LazyLiteral) o).getLiteral();
		else
			return o;
	}

	public static String getSimpleString(final Object arg0)
			throws TypeErrorException {
		if (!(arg0.getClass() == Literal.class
				|| arg0.getClass() == StringLiteral.class
				|| arg0.getClass() == CodeMapLiteral.class || arg0 instanceof String
						)) {
			throw new TypeErrorException();
		}
		return getString(arg0);
	}

	public static String quote(String s) {
		return "\"" + s + "\"";
	}

	public static boolean isNumeric(Object o) {
		if (o instanceof Number)
			return true;
		if (o instanceof TypedLiteral) {
			return isNumeric(((TypedLiteral) o).getType());
		}
		return false;
	}

	public static String unquote(String quotedString) {
		return quotedString.substring(1, quotedString.length() - 1);
	}

	public static String getContent(Object o) throws TypeErrorException {
		if (o instanceof String || o instanceof StringLiteral
				|| o instanceof CodeMapLiteral)
			return o.toString();
		else if (o instanceof TypedLiteral) {
			return ((TypedLiteral) o).getContent();
		} else if (o instanceof LanguageTaggedLiteral) {
			return ((LanguageTaggedLiteral) o).getContent();
		}
		throw new TypeErrorException();
	}

	public static Object createWithSameType(String content, Object literal)
			throws TypeErrorException {
		if (literal instanceof TypedLiteral) {
			try {
				return LiteralFactory.createTypedLiteral(quote(content),
						((TypedLiteral) literal).getTypeLiteral());
			} catch (URISyntaxException e) {
				throw new TypeErrorException();
			}
		} else if (literal instanceof LanguageTaggedLiteral) {
			return LiteralFactory.createLanguageTaggedLiteral(quote(content),
					((LanguageTaggedLiteral) literal).getOriginalLanguage());
		} else {
			return quote(content);
		}
	}

	public static String applyHashFunction(String hashFunction, String inputParameter) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(hashFunction);
			messageDigest.reset();
			messageDigest.update(inputParameter.getBytes());
			byte[] result = messageDigest.digest();
			StringBuffer hexString = new StringBuffer();
			hexString.append("\"");
			for (int i = 0; i < result.length; i++) {
	
				if ((0xFF & result[i]) < 16) {
					hexString.append("0"
							+ Integer.toHexString(0xFF & result[i]));
				} else {
					hexString.append(Integer.toHexString(0xFF & result[i]));
				}
			}
			hexString.append("\"");
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e.getMessage());
		}
	
	}
	
	public static int getLengthOfHashFunction(String hashFunction) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(hashFunction);
			messageDigest.reset();
			return messageDigest.getDigestLength()*2;
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e.getMessage());
		}
	
	}
}
