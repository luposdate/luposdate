package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.VariableInInferenceRule;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleConnectGenPat extends Rule {

	private final boolean doNotConnectInferenceRules;

	public RuleConnectGenPat(final boolean doNotConnectInferenceRules) {
		this.doNotConnectInferenceRules = doNotConnectInferenceRules;
	}

	@Override
	protected void init() {
		final Generate generate = new Generate();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(generate, "generate");

		startNode = generate;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Generate generate = (Generate) mso.get("generate");
		// Connect still not switched Generates
		return (generate.getSucceedingOperators().size() > 0)
				&& (!(generate.getSucceedingOperators().get(0).getOperator() instanceof TriplePattern));
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Generate generate = (Generate) mso.get("generate");

		// PatternMatcher has only TriplePattern as sons
		final List<OperatorIDTuple> pats = rootOperator
				.getSucceedingOperators();

		final LinkedList<OperatorIDTuple> possiblePats = new LinkedList<OperatorIDTuple>();
		// Find possible TriplePattern objects
		TriplePattern pat;
		for (int a = 0; a < pats.size(); a++) {
			if (pats.get(a).getOperator() instanceof TriplePattern) {
				pat = (TriplePattern) pats.get(a).getOperator();
				if (matchPossible(generate.getValueOrVariable(),
						pat.getItems(), doNotConnectInferenceRules)) {
					possiblePats.add(new OperatorIDTuple(pat, 0));
					pat.addPrecedingOperator(generate);
				}
			}
		}
		rootOperator.removePrecedingOperator(generate);
		// Set new successors of Generate object
		if (possiblePats.size() > 0) {
			// System.out.println(generate.toString() + "----"
			// + possiblePats.toString());
			generate.setSucceedingOperators(possiblePats);
		} else {
			generate.setSucceedingOperators(new LinkedList<OperatorIDTuple>());
		}

		// rootOperator.deleteParents();
		// rootOperator.setParents();
		// rootOperator.detectCycles();
		// rootOperator.sendMessage(new BoundVariablesMessage());
		return null;
	}

	/**
	 * @param generateItems
	 *            Generate pattern
	 * @param patItems
	 *            TriplePattern
	 * @return Whether match between parameter objects are possible
	 */
	private static boolean matchPossible(final Item[] generateItems,
			final Item[] patItems, final boolean doNotConnectInferenceRules) {
		Literal patLit;
		Literal generateLit;
		for (int b = 0; b < 3; b++) {
			// If there is one Generate literal, which is not equal to
			// corresponding TriplePattern literal, then no match is possible
			if ((!generateItems[b].isVariable()) && (!patItems[b].isVariable())) {
				generateLit = (Literal) generateItems[b];
				patLit = (Literal) patItems[b];
				if (!generateLit.equals(patLit)) {
					return false;
				}
			}
			if (doNotConnectInferenceRules) {
				// check: if the triple pattern is a triple pattern of an
				// inference
				// rule,
				// then we do not connect them, as all triples based on only
				// ontology information are before inferred (when the rules for
				// the ontology are applied)!
				if (patItems[b].isVariable()
						&& patItems[b] instanceof VariableInInferenceRule)
					return false;
			}
		}
		// If TriplePattern has two identical variables then generate should not
		// contain different literals at these positions
		final HashMap<Variable, Literal> varPositions = new HashMap<Variable, Literal>();
		for (int b = 0; b < 3; b++) {
			if (patItems[b].isVariable()) {
				final Variable v = (Variable) patItems[b];
				if ((varPositions.get(v) == null)
						&& (!generateItems[b].isVariable())) { // new variable
					// found => store the binding for next time
					varPositions.put(v, (Literal) generateItems[b]);
				} else if ((varPositions.get(v) != null)
						&& (!generateItems[b].isVariable())) {
					final Literal lit = varPositions.get(v);
					final Literal newLit = (Literal) generateItems[b];
					if (!lit.equals(newLit)) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
