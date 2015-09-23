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
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.multiinput.join.Join;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;
public class RuleSplitJoinOperandsWithSameId extends Rule {

	/**
	 * <p>Constructor for RuleSplitJoinOperandsWithSameId.</p>
	 */
	public RuleSplitJoinOperandsWithSameId() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	protected void init() {
		final Join join = new Join();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(join, "join");

		startNode = join;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Join join = (Join) mso.get("join");
		int numberLeftOperands = 0;
		int numberRightOperands = 0;
		for (final BasicOperator prec : join.getPrecedingOperators()) {
			if (prec.getOperatorIDTuple(join).getId() == 1)
				numberRightOperands++;
			else
				numberLeftOperands++;
		}
		if (numberRightOperands > 1 || numberLeftOperands > 1)
			return true;
		else
			return false;
	}

	/** {@inheritDoc} */
	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Join join = (Join) mso.get("join");
		final LinkedList<BasicOperator> leftOperands = new LinkedList<BasicOperator>();
		final LinkedList<BasicOperator> rightOperands = new LinkedList<BasicOperator>();
		for (final BasicOperator prec : join.getPrecedingOperators()) {
			final OperatorIDTuple oidt = prec.getOperatorIDTuple(join);
			if (prec.getOperatorIDTuple(join).getId() == 0)
				leftOperands.add(prec);
			else
				rightOperands.add(prec);
		}
		transformOperands(0, leftOperands, join, added);
		transformOperands(1, rightOperands, join, added);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	private void transformOperands(final int id,
			final LinkedList<BasicOperator> operands, final Join join,
			final Collection<BasicOperator> added) {
		if (operands.size() > 1) {
			final LinkedList<Variable> vars = new LinkedList<Variable>();
			final Union union = new Union();
			added.add(union);
			union.setSucceedingOperator(new OperatorIDTuple(join, id));
			join.addPrecedingOperator(union);
			int i = 0;
			for (final BasicOperator prec : operands) {
				join.removePrecedingOperator(prec);
				prec.removeSucceedingOperator(join);
				vars.addAll(prec.getUnionVariables());
				prec.addSucceedingOperator(new OperatorIDTuple(union, i));
				i++;
			}
			union.setIntersectionVariables(vars);
			union.setUnionVariables(vars);
		}
	}
}
