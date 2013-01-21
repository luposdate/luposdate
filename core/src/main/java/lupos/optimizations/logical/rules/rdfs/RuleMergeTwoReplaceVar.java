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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.singleinput.ReplaceVar;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleMergeTwoReplaceVar extends Rule {

	public RuleMergeTwoReplaceVar() {
		super();
	}

	@Override
	protected void init() {
		final ReplaceVar rep1 = new ReplaceVar();
		final ReplaceVar rep2 = new ReplaceVar();

		rep1.setSucceedingOperator(new OperatorIDTuple(rep2, 0));
		rep2.setPrecedingOperator(rep1);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(rep1, "rep1");
		subGraphMap.put(rep2, "rep2");

		startNode = rep1;
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

		final ReplaceVar rep1 = (ReplaceVar) mso.get("rep1");
		final ReplaceVar rep2 = (ReplaceVar) mso.get("rep2");

		final ReplaceVar rep_new = new ReplaceVar();
		rep_new.setSubstitutionsVariableLeft(rep1
				.getSubstitutionsVariableLeft());
		rep_new.setSubstitutionsVariableRight(rep1
				.getSubstitutionsVariableRight());

		final LinkedList<Variable> rep1VarsLeft = rep1
				.getSubstitutionsVariableLeft();
		final LinkedList<Variable> rep1VarsRight = rep1
				.getSubstitutionsVariableRight();
		final LinkedList<Variable> rep2VarsRight = rep2
				.getSubstitutionsVariableRight();
		final LinkedList<Variable> rep2VarsLeft = rep2
				.getSubstitutionsVariableLeft();
		Variable var;
		int index = -1;
		for (int i = 0; i < rep2VarsRight.size(); i++) {
			var = rep2VarsRight.get(i);
			index = rep1VarsLeft.indexOf(var);
			// No transitivity
			if (index == -1) {
				rep_new.addSubstitution(rep2VarsLeft.get(i), rep2VarsRight
						.get(i)); // Duplicate prevention by addSubstitution
			}
			// (y,a),(x,y) => (y,a),(x,a)
			else {
				rep_new.addSubstitution(rep2VarsLeft.get(i), rep1VarsRight
						.get(index));
			}
		}

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) rep1
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) rep2
				.getSucceedingOperators();

		// Set precessors
		BasicOperator pre;
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			pre.addSucceedingOperator(new OperatorIDTuple(rep_new, pre
					.getOperatorIDTuple(rep1).getId()));
			pre.removeSucceedingOperator(rep1);
		}

		rep_new.setPrecedingOperators(pres);
		rep_new.setSucceedingOperators(succs);

		// Set successors
		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			succ = succs.get(i).getOperator();
			succ.addPrecedingOperator(rep_new);
			succ.removePrecedingOperator(rep2);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());

		deleted.add(rep1);
		deleted.add(rep2);
		added.add(rep_new);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
