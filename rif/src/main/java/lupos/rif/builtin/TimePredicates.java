package lupos.rif.builtin;

import lupos.datastructures.items.literal.Literal;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-predicate#")
public class TimePredicates {

	@Builtin(Name = "is-literal-date")
	public static BooleanLiteral is_date(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0), "date",
				"dateTime");
	}

	@Builtin(Name = "is-literal-dateTime")
	public static BooleanLiteral is_dateTime(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"dateTime", "date");
	}

	@Builtin(Name = "is-literal-dateTimeStamp")
	public static BooleanLiteral is_dateTimeStamp(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"dateTimeStamp");
	}

	@Builtin(Name = "is-literal-time")
	public static BooleanLiteral is_time(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0), "time");
	}

	@Builtin(Name = "is-literal-dayTimeDuration")
	public static BooleanLiteral is_dayTimeDuration(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"dayTimeDuration");
	}

	@Builtin(Name = "is-literal-yearMonthDuration")
	public static BooleanLiteral is_yearMonthDuration(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"yearMonthDuration");
	}

	@Builtin(Name = "is-literal-not-date")
	public static BooleanLiteral is_not_date(final Argument arg) {
		return BooleanLiteral.not(is_date(arg));
	}

	@Builtin(Name = "is-literal-not-dateTime")
	public static BooleanLiteral is_not_dateTime(final Argument arg) {
		return BooleanLiteral.not(is_dateTime(arg));
	}

	@Builtin(Name = "is-literal-not-dateTimeStamp")
	public static BooleanLiteral is_not_dateTimeStamp(final Argument arg) {
		return BooleanLiteral.not(is_dateTimeStamp(arg));
	}

	@Builtin(Name = "is-literal-not-time")
	public static BooleanLiteral is_not_time(final Argument arg) {
		return BooleanLiteral.not(is_time(arg));
	}

	@Builtin(Name = "is-literal-not-dayTimeDuration")
	public static BooleanLiteral is_not_dayTimeDuration(final Argument arg) {
		return BooleanLiteral.not(is_dayTimeDuration(arg));
	}

	@Builtin(Name = "is-literal-not-yearMonthDuration")
	public static BooleanLiteral is_not_yearMonthDuration(final Argument arg) {
		return BooleanLiteral.not(is_yearMonthDuration(arg));
	}

}
