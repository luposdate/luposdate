package lupos.engine.operators.singleinput.generate;

import java.util.HashMap;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;

public class GenerateBlankNodes {
	private static HashMap<String, HashMap<Literal, String>> blankNodeAccordingToDomain = new HashMap<String, HashMap<Literal, String>>();

	private static int idBlankNodes = 0;

	public static Literal getBlankNode(
			final String domainForBlankNodeGeneration, final Literal forWhat) {
		HashMap<Literal, String> domainHashMap = blankNodeAccordingToDomain
				.get(domainForBlankNodeGeneration);
		if (domainHashMap == null) {
			domainHashMap = new HashMap<Literal, String>();
		}
		String blankNode = domainHashMap.get(forWhat);
		if (blankNode == null) {
			blankNode = "_:_" + domainForBlankNodeGeneration + "_"
					+ idBlankNodes;
			idBlankNodes++;
			domainHashMap.put(forWhat, blankNode);
			blankNodeAccordingToDomain.put(domainForBlankNodeGeneration,
					domainHashMap);
		}
		return LiteralFactory.createAnonymousLiteral(blankNode);
	}

}
