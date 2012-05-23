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
package lupos.rif.builtin;

import java.math.BigDecimal;
import java.text.DateFormat.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-function#")
public class TimeFunctions {

	@Builtin(Name = "year-from-dateTime")
	public static Literal year_from_dateTime(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Calendar cal = BuiltinHelper.getCalendarFromDateTime(str);
		return BuiltinHelper.createXSLiteral(cal.get(cal.YEAR), "integer");
	}

	@Builtin(Name = "month-from-dateTime")
	public static Literal month_from_dateTime(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Calendar cal = BuiltinHelper.getCalendarFromDateTime(str);
		return BuiltinHelper.createXSLiteral(cal.get(cal.MONTH) + 1, "integer");
	}

	@Builtin(Name = "day-from-dateTime")
	public static Literal day_from_dateTime(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Calendar cal = BuiltinHelper.getCalendarFromDateTime(str);
		return BuiltinHelper.createXSLiteral(cal.get(cal.DAY_OF_MONTH),
				"integer");
	}

	@Builtin(Name = "hours-from-dateTime")
	public static Literal hours_from_dateTime(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Calendar cal = BuiltinHelper.getCalendarFromDateTime(str);
		return BuiltinHelper.createXSLiteral(cal.get(cal.HOUR_OF_DAY),
				"integer");
	}

	@Builtin(Name = "minutes-from-dateTime")
	public static Literal minutes_from_dateTime(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Calendar cal = BuiltinHelper.getCalendarFromDateTime(str);
		return BuiltinHelper.createXSLiteral(cal.get(cal.MINUTE), "integer");
	}

	@Builtin(Name = "seconds-from-dateTime")
	public static Literal seconds_from_dateTime(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Calendar cal = BuiltinHelper.getCalendarFromDateTime(str);
		return BuiltinHelper.createXSLiteral(cal.get(cal.SECOND), "integer");
	}

	@Builtin(Name = "year-from-date")
	public static Literal year_from_date(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Calendar cal = BuiltinHelper.getCalendarFromDate(str);
		return BuiltinHelper.createXSLiteral(cal.get(cal.YEAR), "integer");
	}

	@Builtin(Name = "month-from-date")
	public static Literal month_from_date(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Calendar cal = BuiltinHelper.getCalendarFromDate(str);
		return BuiltinHelper.createXSLiteral(cal.get(cal.MONTH) + 1, "integer");
	}

	@Builtin(Name = "day-from-date")
	public static Literal day_from_date(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Calendar cal = BuiltinHelper.getCalendarFromDate(str);
		return BuiltinHelper.createXSLiteral(cal.get(cal.DAY_OF_MONTH),
				"integer");
	}

	@Builtin(Name = "hours-from-time")
	public static Literal hours_from_time(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Calendar cal = DatatypeConverter.parseTime(str);
		return BuiltinHelper.createXSLiteral(cal.get(cal.HOUR), "integer");
	}

	@Builtin(Name = "minutes-from-time")
	public static Literal minutes_from_time(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Calendar cal = DatatypeConverter.parseTime(str);
		return BuiltinHelper.createXSLiteral(cal.get(cal.MINUTE), "integer");
	}

	@Builtin(Name = "seconds-from-time")
	public static Literal seconds_from_time(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Calendar cal = DatatypeConverter.parseTime(str);
		return BuiltinHelper.createXSLiteral(cal.get(cal.SECOND), "integer");
	}

	@Builtin(Name = "timezone-from-dateTime")
	public static Literal timezone_from_dateTime(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		XMLGregorianCalendar cal = XMLGregorianCalendarImpl.parse(str);
		Duration dur = BuiltinHelper.getDurationFromCalendar(cal);
		return BuiltinHelper.createXSLiteral(
				dur.toString().substring(0, dur.toString().indexOf("H") + 1),
				"dayTimeDuration");
	}

	@Builtin(Name = "timezone-from-date")
	public static Literal timezone_from_date(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		XMLGregorianCalendar cal = XMLGregorianCalendarImpl.parse(str);
		Duration dur = BuiltinHelper.getDurationFromCalendar(cal);
		return BuiltinHelper.createXSLiteral(
				dur.toString().substring(0, dur.toString().indexOf("H") + 1),
				"dayTimeDuration");
	}

	@Builtin(Name = "timezone-from-time")
	public static Literal timezone_from_time(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		XMLGregorianCalendar cal = XMLGregorianCalendarImpl.parse(str);
		Duration dur = BuiltinHelper.getDurationFromCalendar(cal);
		return BuiltinHelper.createXSLiteral(
				dur.toString().substring(0, dur.toString().indexOf("H") + 1),
				"dayTimeDuration");
	}

	@Builtin(Name = "years-from-duration")
	public static Literal years_from_duration(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Duration dur = BuiltinHelper.getYearMonthDurationFromString(str);
		return BuiltinHelper.createXSLiteral(dur.getSign()
				* (dur.getYears() + (dur.getMonths() / 12)), "integer");
	}

	@Builtin(Name = "months-from-duration")
	public static Literal months_from_duration(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Duration dur = BuiltinHelper.getYearMonthDurationFromString(str);
		return BuiltinHelper.createXSLiteral(dur.getSign()
				* (dur.getMonths() % 12), "integer");
	}

	@Builtin(Name = "days-from-duration")
	public static Literal days_from_duration(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Duration dur = BuiltinHelper.getDayTimeDurationFromString(str);
		return BuiltinHelper.createXSLiteral(dur.getSign() * (dur.getDays()),
				"integer");
	}

	@Builtin(Name = "hours-from-duration")
	public static Literal hours_from_duration(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Duration dur = BuiltinHelper.getDayTimeDurationFromString(str);
		return BuiltinHelper.createXSLiteral(dur.getSign() * (dur.getHours()),
				"integer");
	}

	@Builtin(Name = "minutes-from-duration")
	public static Literal minutes_from_duration(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Duration dur = BuiltinHelper.getDayTimeDurationFromString(str);
		return BuiltinHelper.createXSLiteral(
				dur.getSign() * (dur.getMinutes()), "integer");
	}

	@Builtin(Name = "seconds-from-duration")
	public static Literal seconds_from_duration(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Duration dur = BuiltinHelper.getDayTimeDurationFromString(str);
		return BuiltinHelper.getNumericLiteral(dur.getSign()
				* (dur.getField(DatatypeConstants.SECONDS).doubleValue()));
	}

	@Builtin(Name = "subtract-dateTimes")
	public static Literal substract_dateTimes(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		XMLGregorianCalendar cal1 = XMLGregorianCalendarImpl.parse(str);
		str = BuiltinHelper.stringFromLiteral((TypedLiteral) arg.arguments
				.get(1));
		XMLGregorianCalendar cal2 = XMLGregorianCalendarImpl.parse(str);
		Duration dur;
		try {
			dur = DatatypeFactory.newInstance().newDuration(
					cal1.toGregorianCalendar().getTimeInMillis()
							- cal2.toGregorianCalendar().getTimeInMillis());
		} catch (DatatypeConfigurationException e) {
			return null;
		}
		return BuiltinHelper.createXSLiteral(
				dur.toString().substring(0, dur.toString().indexOf("M") + 1),
				"dayTimeDuration");
	}

	@Builtin(Name = "subtract-dates")
	public static Literal substract_dates(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		XMLGregorianCalendar cal1 = XMLGregorianCalendarImpl.parse(str);
		str = BuiltinHelper.stringFromLiteral((TypedLiteral) arg.arguments
				.get(1));
		XMLGregorianCalendar cal2 = XMLGregorianCalendarImpl.parse(str);
		Duration dur;
		try {
			dur = DatatypeFactory.newInstance().newDuration(
					cal1.toGregorianCalendar().getTimeInMillis()
							- cal2.toGregorianCalendar().getTimeInMillis());
		} catch (DatatypeConfigurationException e) {
			return null;
		}
		return BuiltinHelper.createXSLiteral(
				dur.toString().substring(0, dur.toString().indexOf("D") + 1),
				"dayTimeDuration");
	}

	@Builtin(Name = "subtract-times")
	public static Literal substract_times(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		XMLGregorianCalendar cal1 = XMLGregorianCalendarImpl.parse(str);
		str = BuiltinHelper.stringFromLiteral((TypedLiteral) arg.arguments
				.get(1));
		XMLGregorianCalendar cal2 = XMLGregorianCalendarImpl.parse(str);
		Duration dur;
		try {
			dur = DatatypeFactory.newInstance().newDuration(
					cal1.toGregorianCalendar().getTimeInMillis()
							- cal2.toGregorianCalendar().getTimeInMillis());
		} catch (DatatypeConfigurationException e) {
			return null;
		}
		return BuiltinHelper.createXSLiteral(
				dur.toString().substring(0, dur.toString().indexOf("M") + 1),
				"dayTimeDuration");
	}

	@Builtin(Name = "add-yearMonthDurations")
	public static Literal add_yearMonthDurations(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Duration dur1 = BuiltinHelper.getYearMonthDurationFromString(str);
		str = BuiltinHelper.stringFromLiteral((TypedLiteral) arg.arguments
				.get(1));
		Duration dur2 = BuiltinHelper.getYearMonthDurationFromString(str);
		Duration dur = dur1.add(dur2);
		try {
			dur = DatatypeFactory.newInstance().newDurationYearMonth(
					dur.getSign() > 0, dur.getYears() + (dur.getMonths() / 12),
					dur.getMonths() % 12);
		} catch (DatatypeConfigurationException e) {
			return null;
		}
		return BuiltinHelper.createXSLiteral(dur, "yearMonthDuration");
	}

	@Builtin(Name = "subtract-yearMonthDurations")
	public static Literal substract_yearMonthDurations(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Duration dur1 = BuiltinHelper.getYearMonthDurationFromString(str);
		str = BuiltinHelper.stringFromLiteral((TypedLiteral) arg.arguments
				.get(1));
		Duration dur2 = BuiltinHelper.getYearMonthDurationFromString(str);
		Duration dur = dur1.subtract(dur2);
		try {
			dur = DatatypeFactory.newInstance().newDurationYearMonth(
					dur.getSign() > 0, dur.getYears() + (dur.getMonths() / 12),
					dur.getMonths() % 12);
		} catch (DatatypeConfigurationException e) {
			return null;
		}
		return BuiltinHelper.createXSLiteral(dur.toString()
				.replaceAll("0Y", ""), "yearMonthDuration");
	}

	@Builtin(Name = "multiply-yearMonthDuration")
	public static Literal multiply_yearMonthDurations(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Duration dur1 = BuiltinHelper.getYearMonthDurationFromString(str);
		double value = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(1));
		Duration dur = dur1.normalizeWith(GregorianCalendar.getInstance())
				.multiply(BigDecimal.valueOf(value));
		try {
			dur = DatatypeFactory.newInstance().newDurationYearMonth(
					dur.getSign() > 0, dur.getDays() / 365,
					(int) Math.ceil((dur.getDays() % 365.0) / 31.0));
		} catch (DatatypeConfigurationException e) {
			return null;
		}
		return BuiltinHelper.createXSLiteral(dur.toString()
				.replaceAll("0Y", ""), "yearMonthDuration");
	}

	@Builtin(Name = "divide-yearMonthDuration")
	public static Literal divide_yearMonthDurations(final Argument arg) {
		String str = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		Duration dur1 = BuiltinHelper.getYearMonthDurationFromString(str);
		double value = BuiltinHelper
				.numberFromLiteral((TypedLiteral) arg.arguments.get(1));
		Duration dur = dur1.normalizeWith(GregorianCalendar.getInstance())
				.multiply(BigDecimal.valueOf(1.0 / value));
		try {
			dur = DatatypeFactory.newInstance().newDurationYearMonth(
					dur.getSign() > 0, dur.getDays() / 365,
					(int) Math.ceil((dur.getDays() % 365.0) / 31.0));
		} catch (DatatypeConfigurationException e) {
			return null;
		}
		return BuiltinHelper.createXSLiteral(dur.toString()
				.replaceAll("0Y", ""), "yearMonthDuration");
	}
}
