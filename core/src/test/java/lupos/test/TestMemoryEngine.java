package lupos.test;

import java.util.LinkedList;

import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.evaluators.MemoryIndexQueryEvaluator;

public class TestMemoryEngine {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			LiteralFactory.setType(LiteralFactory.MapType.HASHMAP);
			MemoryIndexQueryEvaluator evaluator = new MemoryIndexQueryEvaluator();
			evaluator.prepareInputData(new LinkedList<URILiteral>(), new LinkedList<URILiteral>());
			TestRDF3XEngine.test(evaluator);
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

}
