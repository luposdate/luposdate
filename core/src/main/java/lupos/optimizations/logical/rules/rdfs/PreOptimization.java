package lupos.optimizations.logical.rules.rdfs;

import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class PreOptimization {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PreOptimization() {

	}

	public LinkedList<Generate> calculateGenerates(
			final BasicOperator rootOperator) {
		final LinkedList<Generate> generates = new LinkedList<Generate>();

		return generates;
	}

	public void connectGenPat(final BasicOperator rootOperator) {
		final List<OperatorIDTuple> pats = rootOperator
				.getSucceedingOperators();
		final LinkedList<Generate> generates = RDFSRuleEngine0.generates;

		for (int i = 0; i < generates.size(); i++) {
			System.out.println("Connects Generate number " + i);
			final Generate generate = generates.get(i);
			final LinkedList<OperatorIDTuple> possiblePats = new LinkedList<OperatorIDTuple>();

			TriplePattern pat;
			for (int a = 0; a < pats.size(); a++) {
				pat = (TriplePattern) pats.get(a).getOperator();
				if (matchPossible(generate.getValueOrVariable(), pat.getItems())) {
					possiblePats.add(new OperatorIDTuple(pat, 0));
					pat.addPrecedingOperator(generate);
				}
			}
			if (possiblePats.size() > 0) {
				try {
					Thread.sleep(50);
				} catch (final InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(generate.toString() + "----"
						+ possiblePats.toString());
				generate.setSucceedingOperators(possiblePats);
				for (int p = 0; p < possiblePats.size(); p++) {
					pat = (TriplePattern) possiblePats.get(p).getOperator();
					pat.addPrecedingOperator(generate);
				}
			} else {
				generate
						.setSucceedingOperators(new LinkedList<OperatorIDTuple>());
			}
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
	}

	/**
	 * @param generateItems
	 *            Generate pattern
	 * @param patItems
	 *            TriplePattern
	 * @return Whether match between parameter objects are possible
	 */

	private static boolean matchPossible(final Item[] generateItems,
			final Item[] patItems) { // If there is one Generate literal, which
		// is not equal to corresponding
		// TriplePattern literal, then no match is possible
		Literal patLit;
		Literal generateLit;
		for (int b = 0; b < 3; b++) {
			if ((!generateItems[b].isVariable()) && (!patItems[b].isVariable())) {
				generateLit = (Literal) generateItems[b];
				patLit = (Literal) patItems[b];
				if (!generateLit.equals(patLit)) {
					return false;
				}
			}
		}
		return true;
	}
}
