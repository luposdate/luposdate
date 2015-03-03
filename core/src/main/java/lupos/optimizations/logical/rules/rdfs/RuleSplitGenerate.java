
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;
public class RuleSplitGenerate extends Rule {

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
		// While Generate has more than one successor
		return (generate.getSucceedingOperators().size() > 1)
				&& generate.getPrecedingOperators().size() == 1;
	}

	/** {@inheritDoc} */
	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Generate generate = (Generate) mso.get("generate");

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) generate
				.getPrecedingOperators();
		if (pres.size() > 1) {
			throw (new UnsupportedOperationException(
					"Generate has more predecessors => Correct RuleSplitGenerate!!!"));
		} else {
			final List<OperatorIDTuple> succs = generate
					.getSucceedingOperators();

			final BasicOperator pre = pres.get(0);

			Generate generate_new;

			pre.removeSucceedingOperator(generate);
			deleted.add(generate);

			// For each successor
			for (int i = 0; i < succs.size(); i++) {
				// generate a new Generate and connect it to the i-th successor
				generate_new = new Generate((TriplePattern) succs.get(i)
						.getOperator(), generate.getValueOrVariable());
				added.add(generate_new);
				// connect the new one instead of the old Generate to the
				// predecessors
				generate_new.setPrecedingOperators(pres);
				pre.addSucceedingOperator(new OperatorIDTuple(generate_new, 0));
			}

			rootOperator.deleteParents();
			rootOperator.setParents();
			rootOperator.detectCycles();
			rootOperator.sendMessage(new BoundVariablesMessage());
		}
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
