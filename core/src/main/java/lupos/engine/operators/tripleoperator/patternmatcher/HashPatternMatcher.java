package lupos.engine.operators.tripleoperator.patternmatcher;

import java.util.HashMap;
import java.util.Vector;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class HashPatternMatcher extends PatternMatcher {
	protected HashMap<String, Vector<Integer>> keysOfTriplePatterns;

	@Override
	public void set(final TripleConsumer[] operators) {
		super.set(operators);
		keysOfTriplePatterns = new HashMap<String, Vector<Integer>>();
		for (int i = 0; i < operators.length; i++) {
			final String key = ((TriplePattern) operators[i]).getLiteralKey();

			// System.out.println("Key of TP:"+key);

			Vector<Integer> vtp = keysOfTriplePatterns.get(key);
			if (vtp == null) {
				vtp = new Vector<Integer>();
			}
			vtp.add(new Integer(i));
			keysOfTriplePatterns.put(key, vtp);
		}
	}

	@Override
	public void consume(final Triple triple) {

		final Vector<Integer> MatchingTriplePatterns = new Vector<Integer>();
		// consider all 8 possibilities:
		final String[] combinations = {
				"||",
				triple.getSubject().toString() + "||",
				"|" + triple.getPredicate().toString() + "|",
				triple.getSubject().toString() + "|"
						+ triple.getPredicate().toString() + "|",
				"||" + triple.getObject().toString(),
				triple.getSubject().toString() + "||"
						+ triple.getObject().toString(),
				"|" + triple.getPredicate().toString() + "|"
						+ triple.getObject().toString(),
				triple.getSubject().toString() + "|"
						+ triple.getPredicate().toString() + "|"
						+ triple.getObject().toString() };
		Vector<Integer> inter;
		for (final String combination : combinations) {
			inter = keysOfTriplePatterns.get(combination);
			if (inter != null)
				MatchingTriplePatterns.addAll(inter);
		}
		final TriplePattern[] tp = getTriplePatterns();
		for (int i = 0; i < MatchingTriplePatterns.size(); i++) {
			final int num = MatchingTriplePatterns.get(i).intValue();
			tp[num].consume(triple);
		}
	}
}
