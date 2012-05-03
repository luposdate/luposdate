package lupos.rif.builtin;

import java.net.URISyntaxException;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.string.StringLiteral;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-predicate#")
public class StringPredicates {

	@Builtin(Name = "is-literal-string")
	public static BooleanLiteral is_string(final Argument arg) {
		return BooleanLiteral.create(BuiltinHelper.isOfXSType(
				(Literal) arg.arguments.get(0), "string").value
				|| LiteralPredicates.is_plainLiteral(arg).value);
	}

	@Builtin(Name = "is-literal-normalizedString")
	public static BooleanLiteral is_normalizedString(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"string", "normalizedString");
	}

	@Builtin(Name = "is-literal-token")
	public static BooleanLiteral is_token(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"string", "token");
	}

	@Builtin(Name = "is-literal-language")
	public static BooleanLiteral is_language(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"language");
	}

	@Builtin(Name = "is-literal-Name")
	public static BooleanLiteral is_name(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0), "Name");
	}

	@Builtin(Name = "is-literal-NCName")
	public static BooleanLiteral is_ncname(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"NCName");
	}

	@Builtin(Name = "is-literal-NMTOKEN")
	public static BooleanLiteral is_nmtoken(final Argument arg) {
		return BuiltinHelper.isOfXSType((Literal) arg.arguments.get(0),
				"NMTOKEN");
	}

	@Builtin(Name = "is-literal-not-string")
	public static BooleanLiteral is_not_string(final Argument arg) {
		return BooleanLiteral.not(is_string(arg));
	}

	@Builtin(Name = "is-literal-not-normalizedString")
	public static BooleanLiteral is_not_normalizedString(final Argument arg) {
		return BooleanLiteral.not(is_normalizedString(arg));
	}

	@Builtin(Name = "is-literal-not-token")
	public static BooleanLiteral is_not_token(final Argument arg) {
		return BooleanLiteral.not(is_token(arg));
	}

	@Builtin(Name = "is-literal-not-language")
	public static BooleanLiteral is_not_lang(final Argument arg) {
		return BooleanLiteral.not(is_language(arg));
	}

	@Builtin(Name = "is-literal-not-Name")
	public static BooleanLiteral is_not_name(final Argument arg) {
		return BooleanLiteral.not(is_name(arg));
	}

	@Builtin(Name = "is-literal-not-NCName")
	public static BooleanLiteral is_not_ncname(final Argument arg) {
		return BooleanLiteral.not(is_ncname(arg));
	}

	@Builtin(Name = "is-literal-not-NMTOKEN")
	public static BooleanLiteral is_not_nmtoken(final Argument arg) {
		return BooleanLiteral.not(is_nmtoken(arg));
	}

	@Builtin(Name = "iri-string", Bindable = true)
	public static BooleanLiteral iri_string(final Argument arg) {
		Literal left = null;
		if (!arg.arguments.get(0).isVariable())
			left = (Literal) arg.arguments.get(0);
		Literal right = null;
		if (!arg.arguments.get(1).isVariable())
			right = (Literal) arg.arguments.get(1);

		// versuchen beide zu URI-Literalen umzuformen
		if (left != null && !(left instanceof URILiteral)) {
			String uri = null;
			if (left instanceof TypedLiteral)
				uri = ((TypedLiteral) left).getContent();
			else if (right instanceof StringLiteral)
				uri = left.getName();
			else if (left instanceof CodeMapLiteral)
				uri = left.toString();
			try {
				left = LiteralFactory.createURILiteral("<"
						+ uri.substring(1, uri.length() - 1) + ">");
			} catch (URISyntaxException e) {
				return null;
			}
		}
		if (right != null && !(right instanceof URILiteral)) {
			String uri = null;
			if (right instanceof TypedLiteral)
				uri = ((TypedLiteral) right).getContent();
			else if (right instanceof StringLiteral)
				uri = right.getName();
			else if (right instanceof CodeMapLiteral)
				uri = right.toString();
			try {
				right = LiteralFactory.createURILiteral("<"
						+ uri.substring(1, uri.length() - 1) + ">");
			} catch (URISyntaxException e) {
				return null;
			}
		}

		if (left != null && right != null) {
			return BooleanLiteral.create(left.equals(right));
		} else if (left == null) {
			arg.binding.add((Variable) arg.arguments.get(0), right);
			return BooleanLiteral.TRUE;
		} else {
			arg.binding.add((Variable) arg.arguments.get(1), left);
			return BooleanLiteral.TRUE;
		}
	}

	@Builtin(Name = "contains")
	public static BooleanLiteral contains(final Argument arg) {
		String left = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		String right = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(1));
		boolean result = left.contains(right);
		return BooleanLiteral.create(result);
	}

	@Builtin(Name = "starts-with")
	public static BooleanLiteral starts_with(final Argument arg) {
		String left = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		String right = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(1));
		boolean result = left.startsWith(right);
		return BooleanLiteral.create(result);
	}

	@Builtin(Name = "ends-with")
	public static BooleanLiteral ends_with(final Argument arg) {
		String left = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		String right = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(1));
		boolean result = left.endsWith(right);
		return BooleanLiteral.create(result);
	}

	@Builtin(Name = "matches")
	public static BooleanLiteral matches(final Argument arg) {
		String left = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(0));
		String right = BuiltinHelper
				.stringFromLiteral((TypedLiteral) arg.arguments.get(1));
		boolean result = left.matches(right);
		return BooleanLiteral.create(result);
	}
}
