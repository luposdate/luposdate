/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
