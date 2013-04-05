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
package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.ReplaceLit;
import lupos.engine.operators.singleinput.ReplaceVar;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleReplaceVarUnderReplaceLit extends Rule {

	public RuleReplaceVarUnderReplaceLit() {
		super();
	}

	@Override
	protected void init() {
		final ReplaceLit repLit = new ReplaceLit();
		final ReplaceVar repVar = new ReplaceVar();

		repLit.setSucceedingOperator(new OperatorIDTuple(repVar, 0));
		repVar.setPrecedingOperator(repLit);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(repLit, "repLit");
		subGraphMap.put(repVar, "repVar");

		startNode = repVar;
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
		final ReplaceLit repLit = (ReplaceLit) mso.get("repLit");
		final ReplaceVar repVar = (ReplaceVar) mso.get("repVar");

		final ReplaceVar repVar_new = new ReplaceVar();
		repVar_new.setSubstitutionsVariableLeft(repVar
				.getSubstitutionsVariableLeft());
		repVar_new.setSubstitutionsVariableRight(repVar
				.getSubstitutionsVariableRight());
		repVar_new.setIntersectionVariables(repVar.getIntersectionVariables());
		repVar_new.setUnionVariables(repVar.getUnionVariables());

		final ReplaceLit repLit_new = new ReplaceLit();
		repLit_new.setSubstitutionsLiteralLeft(repLit
				.getSubstitutionsLiteralLeft());
		repLit_new.setSubstitutionsLiteralRight(repLit
				.getSubstitutionsLiteralRight());
		repLit_new.setIntersectionVariables(repLit.getIntersectionVariables());
		repLit_new.setUnionVariables(repLit.getUnionVariables());

		final LinkedList<Variable> repVarLeft = repVar
				.getSubstitutionsVariableLeft();
		final LinkedList<Variable> repVarRight = repVar
				.getSubstitutionsVariableRight();
		final LinkedList<Variable> repLitLeft = repLit
				.getSubstitutionsLiteralLeft();
		final LinkedList<Literal> repLitRight = repLit
				.getSubstitutionsLiteralRight();
		Variable var;
		int index = -1;
		for (int i = 0; i < repVarRight.size(); i++) {
			var = repVarRight.get(i);
			index = repLitLeft.indexOf(var);
			// (x,<a>),(y,x) => (x,<a>),(y,<a>)
			if (index > -1) {
				final Variable var2 = repVarLeft.get(i);
				repVar_new.removeSubstitution(var2, var);
				repLit_new.addSubstitution(var2, repLitRight.get(index));
			}
		}

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) repLit
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) repVar
				.getSucceedingOperators();

		BasicOperator pre;
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			pre.addSucceedingOperator(new OperatorIDTuple(repVar_new, 0));
			pre.removeSucceedingOperator(repLit);
		}

		repVar_new.setPrecedingOperators(pres);
		repVar_new.setSucceedingOperator(new OperatorIDTuple(repLit_new, 0));

		repLit_new.setPrecedingOperator(repVar_new);
		repLit_new.setSucceedingOperators(succs);

		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			succ = succs.get(i).getOperator();
			succ.addPrecedingOperator(repLit_new);
			succ.removePrecedingOperator(repVar);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		// should have been done manually: rootOperator.sendMessage(new
		// BoundVariablesMessage());
		added.add(repVar_new);
		added.add(repLit_new);
		deleted.add(repVar);
		deleted.add(repLit);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

}
