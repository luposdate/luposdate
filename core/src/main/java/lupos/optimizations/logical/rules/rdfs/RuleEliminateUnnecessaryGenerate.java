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
import java.util.Map;

import lupos.datastructures.items.Item;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;
public class RuleEliminateUnnecessaryGenerate extends Rule {

	/** {@inheritDoc} */
	@Override
	protected void init() {
		final TriplePattern tp = new TriplePattern();
		final Generate generate = new Generate();

		tp.setSucceedingOperator(new OperatorIDTuple(generate, 0));
		generate.setPrecedingOperator(tp);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(tp, "triplepattern");
		subGraphMap.put(generate, "generate");

		startNode = generate;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Generate generate = (Generate) mso.get("generate");
		final TriplePattern tp = (TriplePattern) mso.get("triplepattern");

		final Item[] itemsGenerate = generate.getValueOrVariable();
		final Item[] itemsTriplePattern = tp.getItems();

		for (int i = 0; i < 3; i++) {
			if (!itemsGenerate[i].equals(itemsTriplePattern[i]))
				return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Generate generate = (Generate) mso.get("generate");
		final TriplePattern tp = (TriplePattern) mso.get("triplepattern");
		// remove the generate operator...
		for (final OperatorIDTuple oit : generate.getSucceedingOperators()) {
			oit.getOperator().removePrecedingOperator(generate);
		}
		for (final BasicOperator bo : generate.getPrecedingOperators()) {
			bo.removeSucceedingOperator(generate);
		}
		deleted.add(generate);
		// generate.setPrecedingOperators(null);
		// generate.setSucceedingOperators(null);

		// are there no other operators than generate???
		if (tp.getSucceedingOperators().isEmpty()) {
			for (final BasicOperator bo : tp.getPrecedingOperators()) {
				bo.removeSucceedingOperator(tp);
			}
			// tp.setPrecedingOperators(null);
			// tp.setSucceedingOperators(null);
			deleted.add(tp);
		}
		if (!deleted.isEmpty() || !added.isEmpty())
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
