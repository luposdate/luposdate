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
package lupos.optimizations.logical.rules.externalontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.AddBinding;
import lupos.engine.operators.singleinput.filter.Filter;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;
import lupos.sparql1_1.ParseException;
public class RuleConstantPropagationOverJoin extends Rule {

	/** {@inheritDoc} */
	@Override
	protected void init() {
		final AddBinding add = new AddBinding(null, null);
		final Join join = new Join();

		add.setSucceedingOperator(new OperatorIDTuple(join, -1));
		join.setPrecedingOperator(add);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(add, "add");
		subGraphMap.put(join, "join");

		startNode = add;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		// should be fulfilled!
		for (final BasicOperator bo : mso.get("join").getPrecedingOperators()) {
			if (bo.getSucceedingOperators().size() > 1)
				return false;
		}
		return mso.get("add").getPrecedingOperators().size() == 1;
	}

	/** {@inheritDoc} */
	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final AddBinding add = (AddBinding) mso.get("add");
		final Join join = (Join) mso.get("join");
		boolean afterJoin = true;
		final List<BasicOperator> joinPrec = new LinkedList<BasicOperator>();
		joinPrec.addAll(join.getPrecedingOperators());
		for (final BasicOperator bo : joinPrec) {
			// check if Filter(Add.Var = Add.Constant) must be put to a join
			// operand in order to fulfill the join condition
			if (!bo.equals(add)
					&& bo.getUnionVariables().contains(add.getVar())) {
				try {
					final Filter filter = new Filter("FILTER( "
							+ add.getVar().toString() + " = "
							+ add.getLiteral().toString() + " )");
					filter.setIntersectionVariables(bo.getUnionVariables());
					filter.setUnionVariables(bo.getUnionVariables());
					// we assume that
					filter.setSucceedingOperators(bo.getSucceedingOperators());
					bo.setSucceedingOperator(new OperatorIDTuple(filter, 0));
					join.removePrecedingOperator(bo);
					join.addPrecedingOperator(filter);
					filter.setPrecedingOperator(bo);
					afterJoin = false;
					added.add(filter);
				} catch (final ParseException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		}
		// remove add
		for (final BasicOperator bo : add.getPrecedingOperators()) {
			bo.removeSucceedingOperator(add);
			bo.addSucceedingOperators(add.getSucceedingOperators());
			join.addPrecedingOperator(bo);
		}
		join.removePrecedingOperator(add);
		join.getIntersectionVariables().remove(add.getVar());
		// must add be put after the join?
		if (afterJoin) {
			add.setSucceedingOperators(join.getSucceedingOperators());
			add.setPrecedingOperator(join);
			join.setSucceedingOperator(new OperatorIDTuple(add, 0));
			for (final OperatorIDTuple oid : add.getSucceedingOperators()) {
				oid.getOperator().removePrecedingOperator(join);
				oid.getOperator().addPrecedingOperator(add);
			}
		} else
			deleted.add(add);
		alreadyAppliedTo = new HashSet<BasicOperator>();
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
