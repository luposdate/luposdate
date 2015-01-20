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
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.Projection;
import lupos.engine.operators.singleinput.ReplaceVar;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleReplaceVarUnderJoin extends Rule {

	@Override
	protected void init() {
		final Join join = new Join();
		final ReplaceVar replaceVar = new ReplaceVar();

		join.setSucceedingOperator(new OperatorIDTuple(replaceVar, 0));
		replaceVar.setPrecedingOperator(join);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(join, "join");
		subGraphMap.put(replaceVar, "replaceVar");

		startNode = join;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Join join = (Join) mso.get("join");
		final ReplaceVar replaceVar = (ReplaceVar) mso.get("replaceVar");

		final LinkedList<Variable> replaceRightVars = replaceVar
				.getSubstitutionsVariableRight();
		final Object[] joinVars = join.getIntersectionVariables().toArray();
		// Only interesting if minimum one right ReduceEnv-Variable is not join
		// partner
		for (int i = 0; i < replaceRightVars.size(); i++) {
			if (!arrayContains(joinVars, replaceRightVars.get(i))) {
				// Because of complexity transform only joins which have only
				// the ReplaceVar as successor
				if (join.getSucceedingOperators().size() == 1) {
					return true;
				}
			}
		}
		return false;
	}

	private ReplaceVar getReplaceAfterPre(final ReplaceVar replaceVar,
			final ReplaceVar originalClone, final BasicOperator pre,
			final Join join) {
		final ReplaceVar replaceAfterPre = new ReplaceVar();
		final Object[] unionPre = pre.getUnionVariables().toArray();

		final LinkedList<Variable> replaceVarsLeft = originalClone
				.getSubstitutionsVariableLeft();
		final LinkedList<Variable> replaceVarsRight = originalClone
				.getSubstitutionsVariableRight();
		final Object[] intersectionJoin = join.getIntersectionVariables()
				.toArray();

		for (int i = 0; i < replaceVarsLeft.size(); i++) {
			final Variable replaceVarRight = replaceVarsRight.get(i);
			final Variable replaceVarLeft = replaceVarsLeft.get(i);
			// Join gets (p,lit1) (p,lit2) and ReplaceVar has (x,p) => no
			// trigger
			// After incorrect Transformation Join could get (x,lit1) (p,lit2)
			// => trigger
			if (!arrayContains(intersectionJoin, replaceVarRight)) {
				// Right variable be replaced over join
				if (arrayContains(unionPre, replaceVarRight)) {
					replaceAfterPre.addSubstitution(replaceVarLeft,
							replaceVarRight);
					replaceVar.removeSubstitutionVars(i);
				}
			}
		}
		return replaceAfterPre;
	}

	private Projection getProjectionAfterReplace(final ReplaceVar replacePre,
			final BasicOperator pre) {
		final Projection projectionPre = new Projection();

		final Object[] unionVars = pre.getUnionVariables().toArray();
		final LinkedList<Variable> replaceVarsLeft = replacePre
				.getSubstitutionsVariableLeft();
		final LinkedList<Variable> replaceVarsRight = replacePre
				.getSubstitutionsVariableRight();

		// Let the tupel from the new ReplaceVar through
		for (int i = 0; i < replaceVarsLeft.size(); i++) {
			projectionPre.addProjectionElement(replaceVarsLeft.get(i));
		}

		// Let all UNION-Variable through, but not the ones which the new
		// ReplaceVar has replaced (right side)
		final LinkedList<Variable> projVars = new LinkedList<Variable>();
		for (int i = 0; i < unionVars.length; i++) {
			final Variable var = (Variable) unionVars[i];
			if (!replaceVarsRight.contains(var)) {
				projVars.add(var);
			}
		}

		// Add the calculated variables to the new projection
		for (int i = 0; i < projVars.size(); i++) {
			projectionPre.addProjectionElement(projVars.get(i));
		}

		return projectionPre;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Join join = (Join) mso.get("join");
		final ReplaceVar replaceVar = (ReplaceVar) mso.get("replaceVar");

		// Generate a clone
		final ReplaceVar originalClone = new ReplaceVar();
		originalClone.setSubstitutionsVariableLeft(replaceVar
				.getSubstitutionsVariableLeft());
		originalClone.setSubstitutionsVariableRight(replaceVar
				.getSubstitutionsVariableRight());

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) join
				.getPrecedingOperators();

		BasicOperator pre;
		final LinkedList<Integer> indices = new LinkedList<Integer>();
		// Remark the operator-IDs of the precessors
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			indices.add(pre.getOperatorIDTuple(join).getId());
		}

		ReplaceVar rep;
		Projection projectionPre;
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);

			// Calculate the new ReplaceVar which will be replaced between the
			// i-th precessor and the join
			rep = getReplaceAfterPre(replaceVar, originalClone, pre, join);

			// Calculate projection after precessor and new ReplaceVar
			projectionPre = getProjectionAfterReplace(rep, pre);

			added.add(rep);
			added.add(projectionPre);

			pre.setSucceedingOperator(new OperatorIDTuple(rep, 0));
			rep.setPrecedingOperator(pre);
			rep.setSucceedingOperator(new OperatorIDTuple(projectionPre, 0));

			projectionPre.setPrecedingOperator(rep);
			projectionPre.setSucceedingOperator(new OperatorIDTuple(join,
					indices.get(i)));
			join.setPrecedingOperator(projectionPre);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
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
