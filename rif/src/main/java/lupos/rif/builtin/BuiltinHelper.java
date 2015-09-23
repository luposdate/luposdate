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

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.rif.IExpression;
import lupos.rif.RIFException;
import lupos.rif.datatypes.ListLiteral;
import lupos.rif.model.RuleList;
public class BuiltinHelper {

	/**
	 * <p>getSizeOfList.</p>
	 *
	 * @param list a {@link java.lang.Object} object.
	 * @return a int.
	 */
	public static int getSizeOfList(final Object list){
		if (list instanceof RuleList) {
			return ((RuleList) list).getItems().size();
		} else if (list instanceof ListLiteral) {
				return ((ListLiteral) list).getEntries().size();
		} else {
			throw new RuntimeException("A list was expected, but got "+list);
		}
	}

	/**
	 * <p>getEntryOfList.</p>
	 *
	 * @param list a {@link java.lang.Object} object.
	 * @param index a int.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a {@link lupos.datastructures.items.Item} object.
	 */
	public static Item getEntryOfList(final Object list, final int index, final Bindings b){
		if (list instanceof RuleList) {
			final IExpression expr = ((RuleList) list).getItems().get(index);
			return (Item) expr.evaluate(b);
		} else if (list instanceof ListLiteral) {
				return ((ListLiteral) list).getEntries().get(index);
		} else {
			throw new RuntimeException("A list was expected, but got "+list);
		}
	}


	/**
	 * <p>getYearMonthDurationFromString.</p>
	 *
	 * @param duration a {@link java.lang.String} object.
	 * @return a {@link javax.xml.datatype.Duration} object.
	 */
	public static Duration getYearMonthDurationFromString(final String duration) {
		Duration dur;
		try {
			dur = DatatypeFactory.newInstance().newDurationYearMonth(duration);
		} catch (final DatatypeConfigurationException e) {
			return null;
		}
		return dur;
	}

	/**
	 * <p>getDayTimeDurationFromString.</p>
	 *
	 * @param duration a {@link java.lang.String} object.
	 * @return a {@link javax.xml.datatype.Duration} object.
	 */
	public static Duration getDayTimeDurationFromString(final String duration) {
		Duration dur;
		try {
			dur = DatatypeFactory.newInstance().newDurationDayTime(duration);
		} catch (final DatatypeConfigurationException e) {
			return null;
		}
		return dur;
	}

	/**
	 * <p>getDurationFromCalendar.</p>
	 *
	 * @param cal a {@link javax.xml.datatype.XMLGregorianCalendar} object.
	 * @return a {@link javax.xml.datatype.Duration} object.
	 */
	public static Duration getDurationFromCalendar(final XMLGregorianCalendar cal) {
		Duration dur;
		try {
			dur = DatatypeFactory.newInstance().newDurationDayTime(
					cal.getTimezone() * 60000);
		} catch (final DatatypeConfigurationException e) {
			return null;
		}
		return dur;
	}

	/**
	 * <p>getCalendarFromDateTime.</p>
	 *
	 * @param dateTime a {@link java.lang.String} object.
	 * @return a {@link java.util.Calendar} object.
	 */
	public static Calendar getCalendarFromDateTime(final String dateTime) {
		final String str = dateTime.replaceAll("T", "");
		final Calendar cal = GregorianCalendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
		try {
			final Date date = sdf.parse(str);
			cal.setTime(date);
		} catch (final ParseException e) {
			return null;
		}
		return cal;
	}

	/**
	 * <p>getCalendarFromDate.</p>
	 *
	 * @param dateTime a {@link java.lang.String} object.
	 * @return a {@link java.util.Calendar} object.
	 */
	public static Calendar getCalendarFromDate(final String dateTime) {
		final Calendar cal = GregorianCalendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			final Date date = sdf.parse(dateTime);
			cal.setTime(date);
		} catch (final ParseException e) {
			return null;
		}
		return cal;
	}

	/**
	 * <p>getNumericLiteral.</p>
	 *
	 * @param result a {@link java.lang.Double} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal getNumericLiteral(final Double result) {
		if (!result.toString().endsWith(".0")) {
			return BuiltinHelper.createXSLiteral(result, "double");
		} else {
			return BuiltinHelper.createXSLiteral((int) result.doubleValue(),
					"integer");
		}
	}

	/**
	 * <p>getNumericLiteral.</p>
	 *
	 * @param result a {@link java.lang.Integer} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal getNumericLiteral(final Integer result) {
		return BuiltinHelper.createXSLiteral(result, "integer");
	}

	/**
	 * <p>numberFromLiteral.</p>
	 *
	 * @param literal a {@link lupos.datastructures.items.literal.TypedLiteral} object.
	 * @return a double.
	 */
	public static double numberFromLiteral(final TypedLiteral literal) {
		final String str = literal.getContent();
		return Double.parseDouble(str.substring(1, str.length() - 1));
	}

	/**
	 * <p>stringFromLiteral.</p>
	 *
	 * @param literal a {@link lupos.datastructures.items.literal.Literal} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String stringFromLiteral(Literal literal) {
		if(literal instanceof LazyLiteral){
			literal = ((LazyLiteral)literal).getLiteral();
		}
		final String str = (literal instanceof TypedLiteral)?((TypedLiteral)literal).getContent() : literal.toString();
		return str.substring(1, str.length() - 1);
	}

	/**
	 * <p>createXSLiteral.</p>
	 *
	 * @param value a {@link java.lang.Object} object.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal createXSLiteral(Object value, final String type) {
		if (type.equals("string") && value.toString().length() > 2) {
			value = value.toString().substring(1, value.toString().length() - 1);
		}
		try {
			return LiteralFactory.createTypedLiteralWithoutLazyLiteral("\""
					+ value.toString() + "\"",
					"<http://www.w3.org/2001/XMLSchema#" + type + ">");
		} catch (final URISyntaxException e) {
			throw new RIFException(e.getMessage());
		}
	}

	/**
	 * <p>createRDFLiteral.</p>
	 *
	 * @param value a {@link java.lang.Object} object.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal createRDFLiteral(final Object value, final String type) {
		try {
			return LiteralFactory
					.createTypedLiteralWithoutLazyLiteral(
							"\"" + value.toString() + "\"",
							"<http://www.w3.org/1999/02/22-rdf-syntax-ns#"
									+ type + ">");
		} catch (final URISyntaxException e) {
			throw new RIFException(e.getMessage());
		}
	}

	/**
	 * <p>getInteger.</p>
	 *
	 * @param item a {@link lupos.datastructures.items.literal.TypedLiteral} object.
	 * @return a int.
	 */
	public static int getInteger(final TypedLiteral item) {
		final String content = item.getContent();
		return Integer.parseInt(content.substring(1, content.length() - 1));
	}

	/**
	 * <p>getBoolean.</p>
	 *
	 * @param item a {@link lupos.datastructures.items.literal.TypedLiteral} object.
	 * @return a boolean.
	 */
	public static boolean getBoolean(final TypedLiteral item) {
		final String content = item.getContent();
		return Boolean.parseBoolean(content.substring(1, content.length() - 1));
	}

	/**
	 * <p>isOfXSType.</p>
	 *
	 * @param l a {@link lupos.datastructures.items.literal.Literal} object.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	public static BooleanLiteral isOfXSType(final Literal l, final String... type) {
		if (l != null && l instanceof TypedLiteral) {
			for (final String tp : type) {
				if (((TypedLiteral) l).getType().equalsIgnoreCase(
						"<http://www.w3.org/2001/XMLSchema#" + tp + ">")) {
					return BooleanLiteral.TRUE;
				}
			}
		}
		return BooleanLiteral.FALSE;
	}

	/**
	 * <p>isOfRDFType.</p>
	 *
	 * @param l a {@link lupos.datastructures.items.literal.Literal} object.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link lupos.rif.builtin.BooleanLiteral} object.
	 */
	public static BooleanLiteral isOfRDFType(final Literal l, final String... type) {
		if (l != null && l instanceof TypedLiteral) {
			for (final String tp : type) {
				if (((TypedLiteral) l).getType().equalsIgnoreCase(
						"<http://www.w3.org/1999/02/22-rdf-syntax-ns#" + tp
								+ ">")) {
					return BooleanLiteral.TRUE;
				}
			}
		}
		return BooleanLiteral.FALSE;
	}

	private static String mark = "-_.!~*'()\"";

	/**
	 * <p>encodeURI.</p>
	 *
	 * @param argString a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String encodeURI(final String argString) {
		final StringBuilder uri = new StringBuilder(); // Encoded URL
		// thanks Marco!

		final char[] chars = argString.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			final char c = chars[i];
			if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
					|| (c >= 'A' && c <= 'Z') || mark.indexOf(c) != -1) {
				uri.append(c);
			} else {
				uri.append("%");
				uri.append(Integer.toHexString(c));
			}
		}
		return uri.toString();
	}

}
