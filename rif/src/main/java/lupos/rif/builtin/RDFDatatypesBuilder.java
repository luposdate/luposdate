package lupos.rif.builtin;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;

@Namespace(value = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
public class RDFDatatypesBuilder {

	@Builtin(Name = "XMLLiteral")
	public static Literal buildXmlLiteral(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createRDFLiteral(content, "XMLLiteral");
		} else
			return null;
	}

	@Builtin(Name = "PlainLiteral")
	public static Literal buildplainLiteral(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof TypedLiteral) {
			final String content = ((TypedLiteral) arg.arguments.get(0))
					.getContent();
			return BuiltinHelper.createRDFLiteral(content, "PlainLiteral");
		} else
			return null;
	}

}
