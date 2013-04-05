/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.tripleoperator.TriggerOneTime;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleDeleteTriggerOneTimeJoin extends Rule {

	@Override
	protected void init() {
		final TriggerOneTime trigger = new TriggerOneTime();
		final Join join = new Join();

		trigger.setSucceedingOperator(new OperatorIDTuple(join, -1));
		join.setPrecedingOperator(trigger);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(trigger, "trigger");
		subGraphMap.put(join, "join");

		startNode = join;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		return true;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final TriggerOneTime trigger = (TriggerOneTime) mso.get("trigger");
		final Join join = (Join) mso.get("join");
		join.removePrecedingOperator(trigger);
		if (join.getPrecedingOperators().size() <= 1) {
			// remove join
			for (final OperatorIDTuple oit : join.getSucceedingOperators()) {
				oit.getOperator().removePrecedingOperator(join);
			}
			for (final BasicOperator bo : join.getPrecedingOperators()) {
				bo.removeSucceedingOperator(join);
				bo.addSucceedingOperators(join.getSucceedingOperators());
				for (final OperatorIDTuple oit : join.getSucceedingOperators()) {
					oit.getOperator().addPrecedingOperator(bo);
				}
			}
			deleted.add(join);
		}
		// remove join from trigger operator
		trigger.removeSucceedingOperator(join);
		if (trigger.getSucceedingOperators().size() == 0) {
			// remove trigger operator
			for (final BasicOperator bo : trigger.getPrecedingOperators()) {
				bo.removeSucceedingOperator(trigger);
			}
			deleted.add(trigger);
		}
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
