package lupos.rif.builtin;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.URILiteral;

public class NonStandardPredicates {
	@Builtin(Name = "is-literal")
	public static BooleanLiteral is_literal(Argument arg) {
		Item arg0 = arg.arguments.get(0);
		if(arg0 instanceof LazyLiteral){
			arg0 = ((LazyLiteral) arg0).getLiteral();
		}
		return BooleanLiteral.create(!(arg0 instanceof AnonymousLiteral || arg0 instanceof URILiteral));
	}

	@Builtin(Name = "is-blanknode")
	public static BooleanLiteral is_blanknode(Argument arg) {
		Item arg0 = arg.arguments.get(0);
		if(arg0 instanceof LazyLiteral){
			arg0 = ((LazyLiteral) arg0).getLiteral();
		}
		return BooleanLiteral.create(arg0 instanceof AnonymousLiteral);
	}

	@Builtin(Name = "is-uri")
	public static BooleanLiteral is_uri(Argument arg) {
		Item arg0 = arg.arguments.get(0);
		if(arg0 instanceof LazyLiteral){
			arg0 = ((LazyLiteral) arg0).getLiteral();
		}
		return BooleanLiteral.create(arg0 instanceof URILiteral);
	}
}