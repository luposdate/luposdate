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
package lupos.optimizations.logical.rules.externalontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleFactorOutUnionInGenerate extends Rule {

	@Override
	protected void init() {
		final Union union = new Union();
		final Generate generate = new Generate();

		union.setSucceedingOperator(new OperatorIDTuple(generate, -1));
		generate.setPrecedingOperator(union);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(union, "union");
		subGraphMap.put(generate, "generate");

		startNode = union;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		// only one union?
		// int number = 0;
		// for (final BasicOperator bo : ((Join) mso.get("join"))
		// .getPrecedingOperators()) {
		// if (bo instanceof Union)
		// number++;
		// }
		// return (number == 1);
		return true;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Union union = (Union) mso.get("union");
		final Generate generate = (Generate) mso.get("generate");
		final List<BasicOperator> unionOperands = union.getPrecedingOperators();
		generate.removePrecedingOperator(union);
		deleted.add(union);
		boolean firstTime = true;
		if (generate.getPrecedingOperators().size() > 0) {
			firstTime = false;
		}
		for (final BasicOperator toMove : unionOperands) {
			Generate generateNew;
			if (firstTime) {
				// use existing generate operator
				generateNew = generate;
				firstTime = false;
			} else {
				// clone join operator plus its other operands
				generateNew = new Generate();
				generateNew.cloneFrom(generate);
				added.add(generateNew);
			}
			generateNew.setPrecedingOperator(toMove);

			toMove.setSucceedingOperator(new OperatorIDTuple(generateNew, 0));
		}
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
