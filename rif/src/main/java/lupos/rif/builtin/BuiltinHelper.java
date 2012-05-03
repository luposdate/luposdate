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

import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.rif.RIFException;

public class BuiltinHelper {

	public static Duration getYearMonthDurationFromString(final String duration) {
		Duration dur;
		try {
			dur = DatatypeFactory.newInstance().newDurationYearMonth(duration);
		} catch (DatatypeConfigurationException e) {
			return null;
		}
		return dur;
	}

	public static Duration getDayTimeDurationFromString(final String duration) {
		Duration dur;
		try {
			dur = DatatypeFactory.newInstance().newDurationDayTime(duration);
		} catch (DatatypeConfigurationException e) {
			return null;
		}
		return dur;
	}

	public static Duration getDurationFromCalendar(XMLGregorianCalendar cal) {
		Duration dur;
		try {
			dur = DatatypeFactory.newInstance().newDurationDayTime(
					cal.getTimezone() * 60000);
		} catch (DatatypeConfigurationException e) {
			return null;
		}
		return dur;
	}

	public static Calendar getCalendarFromDateTime(final String dateTime) {
		String str = dateTime.replaceAll("T", "");
		Calendar cal = GregorianCalendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
		try {
			Date date = sdf.parse(str);
			cal.setTime(date);
		} catch (ParseException e) {
			return null;
		}
		return cal;
	}

	public static Calendar getCalendarFromDate(final String dateTime) {
		Calendar cal = GregorianCalendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date = sdf.parse(dateTime);
			cal.setTime(date);
		} catch (ParseException e) {
			return null;
		}
		return cal;
	}

	public static Literal getNumericLiteral(Double result) {
		if (!result.toString().endsWith(".0"))
			return BuiltinHelper.createXSLiteral(result, "double");
		else
			return BuiltinHelper.createXSLiteral((int) result.doubleValue(),
					"integer");
	}

	public static Literal getNumericLiteral(Integer result) {
		return BuiltinHelper.createXSLiteral(result, "integer");
	}

	public static double numberFromLiteral(final TypedLiteral literal) {
		String str = literal.getContent();
		return Double.parseDouble(str.substring(1, str.length() - 1));
	}

	public static String stringFromLiteral(final TypedLiteral literal) {
		String str = literal.getContent();
		return str.substring(1, str.length() - 1);
	}

	public static Literal createXSLiteral(Object value, String type) {
		if (type.equals("string") && value.toString().length() > 2)
			value = value.toString()
					.substring(1, value.toString().length() - 1);
		try {
			return LiteralFactory.createTypedLiteralWithoutLazyLiteral("\""
					+ value.toString() + "\"",
					"<http://www.w3.org/2001/XMLSchema#" + type + ">");
		} catch (URISyntaxException e) {
			throw new RIFException(e.getMessage());
		}
	}

	public static Literal createRDFLiteral(Object value, String type) {
		try {
			return LiteralFactory
					.createTypedLiteralWithoutLazyLiteral(
							"\"" + value.toString() + "\"",
							"<http://www.w3.org/1999/02/22-rdf-syntax-ns#"
									+ type + ">");
		} catch (URISyntaxException e) {
			throw new RIFException(e.getMessage());
		}
	}

	public static int getInteger(TypedLiteral item) {
		final String content = item.getContent();
		return Integer.parseInt(content.substring(1, content.length() - 1));
	}

	public static boolean getBoolean(TypedLiteral item) {
		final String content = item.getContent();
		return Boolean.parseBoolean(content.substring(1, content.length() - 1));
	}

	public static BooleanLiteral isOfXSType(Literal l, String... type) {
		if (l != null && l instanceof TypedLiteral) {
			for (String tp : type)
				if (((TypedLiteral) l).getType().equalsIgnoreCase(
						"<http://www.w3.org/2001/XMLSchema#" + tp + ">"))
					return BooleanLiteral.TRUE;
		}
		return BooleanLiteral.FALSE;
	}

	public static BooleanLiteral isOfRDFType(Literal l, String... type) {
		if (l != null && l instanceof TypedLiteral) {
			for (String tp : type)
				if (((TypedLiteral) l).getType().equalsIgnoreCase(
						"<http://www.w3.org/1999/02/22-rdf-syntax-ns#" + tp
								+ ">"))
					return BooleanLiteral.TRUE;
		}
		return BooleanLiteral.FALSE;
	}

	private static String mark = "-_.!~*'()\"";

	public static String encodeURI(String argString) {
		StringBuilder uri = new StringBuilder(); // Encoded URL
		// thanks Marco!

		char[] chars = argString.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
					|| (c >= 'A' && c <= 'Z') || mark.indexOf(c) != -1) {
				uri.append(c);
			} else {
				uri.append("%");
				uri.append(Integer.toHexString((int) c));
			}
		}
		return uri.toString();
	}

}
