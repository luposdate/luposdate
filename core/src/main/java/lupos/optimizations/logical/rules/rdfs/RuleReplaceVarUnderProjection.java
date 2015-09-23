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

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.singleinput.Projection;
import lupos.engine.operators.singleinput.ReplaceVar;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;
public class RuleReplaceVarUnderProjection extends Rule {

	/** {@inheritDoc} */
	@Override
	protected void init() {
		final Projection projection = new Projection();
		final ReplaceVar replaceVar = new ReplaceVar();

		projection.setSucceedingOperator(new OperatorIDTuple(replaceVar, 0));
		replaceVar.setPrecedingOperator(projection);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(projection, "projection");
		subGraphMap.put(replaceVar, "replaceVar");

		startNode = replaceVar;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Projection projection = (Projection) mso.get("projection");
		final ReplaceVar replaceVar = (ReplaceVar) mso.get("replaceVar");

		// Clone ReplaceVar
		final ReplaceVar replaceVar_new = new ReplaceVar();
		replaceVar_new.setSubstitutionsVariableLeft(replaceVar
				.getSubstitutionsVariableLeft());
		replaceVar_new.setSubstitutionsVariableRight(replaceVar
				.getSubstitutionsVariableRight());

		replaceVar.removePrecedingOperator(projection);

		// Enhance projection variables by left tuple variables of ReplaceVar
		final LinkedList<Variable> vars = replaceVar
				.getSubstitutionsVariableLeft();
		for (int i = 0; i < vars.size(); i++) {
			if (!projection.getProjectedVariables().contains(vars.get(i))) {
				projection.addProjectionElement(vars.get(i));
			}
		}

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) projection
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) replaceVar
				.getSucceedingOperators();

		BasicOperator pre;
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			pre.addSucceedingOperator(new OperatorIDTuple(replaceVar_new, 0));
			pre.removeSucceedingOperator(projection);
		}

		replaceVar_new.setPrecedingOperators(pres);
		replaceVar_new
				.setSucceedingOperator(new OperatorIDTuple(projection, 0));

		projection.setPrecedingOperator(replaceVar_new);
		projection.setSucceedingOperators(succs);

		for (int i = 0; i < succs.size(); i++) {
			succs.get(i).getOperator().addPrecedingOperator(projection);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		deleted.add(replaceVar);
		added.add(replaceVar_new);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
