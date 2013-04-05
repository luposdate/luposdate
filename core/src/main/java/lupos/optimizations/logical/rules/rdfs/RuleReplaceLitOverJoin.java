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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.ReplaceLit;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleReplaceLitOverJoin extends Rule {

	@Override
	protected void init() {
		final ReplaceLit replaceLit = new ReplaceLit();
		final Join join = new Join();

		replaceLit.setSucceedingOperator(new OperatorIDTuple(join, -1));
		join.setPrecedingOperator(replaceLit);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(replaceLit, "replaceLit");
		subGraphMap.put(join, "join");

		startNode = replaceLit;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Join join = (Join) mso.get("join");
		final ReplaceLit replaceLit = (ReplaceLit) mso.get("replaceLit");

		// final Object[] joinVars = join.getIntersectionVariables().toArray();
		final Object[] joinVars = join.getUnionVariables().toArray();
		final LinkedList<Variable> v = replaceLit.getSubstitutionsLiteralLeft();

		// If there is minimum one substitution which can be pulled down
		for (int i = 0; i < v.size(); i++) {
			// Otherwise join could trigger after transformation
			if (!arrayContains(joinVars, v.get(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final ReplaceLit replaceLit = (ReplaceLit) mso.get("replaceLit");
		final Join join = (Join) mso.get("join");

		final ReplaceLit replaceLitUnder = new ReplaceLit();
		final Collection<Variable> vars = new HashSet<Variable>();
		vars.addAll(join.getIntersectionVariables());
		replaceLitUnder.setIntersectionVariables(vars);
		replaceLitUnder.setUnionVariables(vars);

		final Object[] joinVars = join.getIntersectionVariables().toArray();
		final LinkedList<Variable> replaceLitLeft = replaceLit
				.getSubstitutionsLiteralLeft();
		final LinkedList<Literal> replaceLitRight = replaceLit
				.getSubstitutionsLiteralRight();
		Variable var;
		Literal lit;
		for (int i = replaceLitLeft.size() - 1; i >= 0; i--) {
			var = replaceLitLeft.get(i);
			// Split ReplaceLit and pull only not intersection variables
			// downwards
			if (!arrayContains(joinVars, var)) {
				lit = replaceLitRight.get(i);
				replaceLitUnder.addSubstitution(var, lit);
				replaceLit.removeSubstitutionVars(i);
				replaceLitUnder.getIntersectionVariables().add(var); // var is
				// also
				// added
				// to
				// unionVariables
				// as
				// they
				// are
				// the
				// same
				// objects
				// !
			}
		}

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) replaceLit
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) join
				.getSucceedingOperators();
		final int index = replaceLit.getOperatorIDTuple(join).getId();

		// If everything could be pushed downwards, the old ReplaceLit can be
		// deleted
		if (replaceLit.getSubstitutionsLiteralLeft().size() == 0) {
			BasicOperator pre;
			for (int i = 0; i < pres.size(); i++) {
				pre = pres.get(i);
				pre.addSucceedingOperator(new OperatorIDTuple(join, index));
				pre.removeSucceedingOperator(replaceLit);
				join.addPrecedingOperator(pre);
			}
			join.removePrecedingOperator(replaceLit);
			deleted.add(replaceLit);
		}

		// Insert the new ReplaceLit under the Join
		// (only if there is not already an equivalent ReplaceLit!)
		if (!((join.getSucceedingOperators().size() == 1)
				&& (join.getSucceedingOperators().get(0).getOperator() instanceof ReplaceLit) && (replaceLitUnder
				.equals(join.getSucceedingOperators().get(0).getOperator())))) {
			join.setSucceedingOperator(new OperatorIDTuple(replaceLitUnder, 0));

			replaceLitUnder.setPrecedingOperator(join);
			replaceLitUnder.setSucceedingOperators(succs);
			added.add(replaceLitUnder);
		}

		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			succ = succs.get(i).getOperator();
			succ.addPrecedingOperator(replaceLitUnder);
			succ.removePrecedingOperator(join);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		// should have already been done manually: rootOperator.sendMessage(new
		// BoundVariablesMessage());
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	private boolean arrayContains(final Object[] vars, final Variable var) {
		for (int i = 0; i < vars.length; i++) {
			System.out.println(vars[i].toString() + "," + var.toString());
			if (vars[i].equals(var))
				return true;
		}
		return false;
	}
}
