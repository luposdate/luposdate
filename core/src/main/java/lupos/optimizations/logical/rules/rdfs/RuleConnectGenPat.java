/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

	/**
	 * <p>Constructor for RuleConnectGenPat.</p>
	 *
	 * @param doNotConnectInferenceRules a boolean.
	 */
	public RuleConnectGenPat(final boolean doNotConnectInferenceRules) {
		this.doNotConnectInferenceRules = doNotConnectInferenceRules;
	}

	/** {@inheritDoc} */
	@Override
	protected void init() {
		final Generate generate = new Generate();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(generate, "generate");

		startNode = generate;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Generate generate = (Generate) mso.get("generate");
		// Connect still not switched Generates
		return (!generate.getSucceedingOperators().isEmpty())
				&& (!(generate.getSucceedingOperators().get(0).getOperator() instanceof TriplePattern));
	}

	/** {@inheritDoc} */
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
		if (!possiblePats.isEmpty()) {
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
