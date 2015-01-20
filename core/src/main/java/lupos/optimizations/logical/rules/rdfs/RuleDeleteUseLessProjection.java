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
import lupos.engine.operators.singleinput.Projection;
import lupos.engine.operators.singleinput.Result;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleDeleteUseLessProjection extends Rule {

	@Override
	protected void init() {
		final Projection projection = new Projection();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(projection, "projection");

		startNode = projection;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Projection projection = (Projection) mso.get("projection");
		if (projection.getSucceedingOperators().get(0).getOperator() instanceof Result) {
			return false;
		}
		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) projection
				.getPrecedingOperators();

		final Object[] projVars = projection.getProjectedVariables().toArray();

		final LinkedList<Variable> unionPres = new LinkedList<Variable>();

		BasicOperator pre;
		Object[] union;
		// calculate UNION-Variables of the precessors
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			union = pre.getUnionVariables().toArray();
			for (int u = 0; u < union.length; u++) {
				if (!unionPres.contains(union[u])) {
					unionPres.add((Variable) union[u]);
				}
			}
		}

		// check whether Projection projects everything
		for (int a = 0; a < unionPres.size(); a++) {
			if (!arrayContains(projVars, unionPres.get(a))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Projection projection = (Projection) mso.get("projection");

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) projection
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) projection
				.getSucceedingOperators();

		BasicOperator pre;
		OperatorIDTuple idTuple;
		// Connect all precessors to all successors
		for (int i = 0; i < pres.size(); i++) {
			for (int a = 0; a < succs.size(); a++) {
				idTuple = succs.get(a);
				pre = pres.get(i);
				pre.addSucceedingOperator(new OperatorIDTuple(idTuple
						.getOperator(), idTuple.getId()));
				pre.removeSucceedingOperator(projection);
			}
		}

		BasicOperator succ;
		// And all successors to all precessors
		for (int i = 0; i < succs.size(); i++) {
			for (int a = 0; a < pres.size(); a++) {
				succ = succs.get(i).getOperator();
				succ.addPrecedingOperator(pres.get(a));
				succ.removePrecedingOperator(projection);
			}
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		// should be no problem to leave this out: rootOperator.sendMessage(new
		// BoundVariablesMessage());
		deleted.add(projection);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	private boolean arrayContains(final Object[] vars, final Variable var) {
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].equals(var))
				return true;
		}
		return false;
	}
}
