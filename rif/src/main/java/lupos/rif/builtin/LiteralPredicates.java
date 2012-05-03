package lupos.rif.builtin;

import lupos.datastructures.items.literal.Literal;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-predicate#")
public class LiteralPredicates {
	@Builtin(Name = "literal-equal")
	public static BooleanLiteral literal_equal(Argument arg) {
		return BooleanLiteral.create(arg.arguments.get(0).equals(
				arg.arguments.get(1)));
	}

	@Builtin(Name = "literal-not-identical")
	public static BooleanLiteral literal_not_equal(Argument arg) {
		return BooleanLiteral.not(literal_equal(arg));
	}

	@Builtin(Name = "literal-not-equal")
	public static BooleanLiteral literal_not_equal2(Argument arg) {
		return literal_not_equal(arg);
	}

	@Builtin(Name = "is-literal-anyURI")
	public static BooleanLiteral is_anyUri(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"anyURI");
	}

	@Builtin(Name = "is-literal-not-anyURI")
	public static BooleanLiteral is_not_anyUri(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "anyURI"));
	}

	@Builtin(Name = "is-literal-XMLLiteral")
	public static BooleanLiteral is_xmlLiteral(final Argument arg) {
		return BuiltinHelper.isOfRDFType((Literal) arg.arguments.get(0),
				"XMLLiteral");
	}

	@Builtin(Name = "is-literal-not-XMLLiteral")
	public static BooleanLiteral is_not_xmlLiteral(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfRDFType(
				(Literal) arg.arguments.get(0), "XMLLiteral"));
	}

	@Builtin(Name = "is-literal-PlainLiteral")
	public static BooleanLiteral is_plainLiteral(final Argument arg) {
		return BuiltinHelper.isOfRDFType((Literal) arg.arguments.get(0),
				"PlainLiteral");
	}

	@Builtin(Name = "is-literal-not-PlainLiteral")
	public static BooleanLiteral is_not_plainLiteral(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfRDFType(
				(Literal) arg.arguments.get(0), "PlainLiteral"));
	}

	@Builtin(Name = "is-literal-hexBinary")
	public static BooleanLiteral is_hexBinary(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"hexBinary");
	}

	@Builtin(Name = "is-literal-base64Binary")
	public static BooleanLiteral is_64Binary(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"base64Binary");
	}

	@Builtin(Name = "is-literal-not-base64Binary")
	public static BooleanLiteral is_not_64Binary(final Argument arg) {
		return BooleanLiteral.not(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "base64Binary"));
	}
}
