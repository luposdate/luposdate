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
import java.util.List;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.ReplaceVar;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;
public class RuleReplaceVarUnderTriplePattern extends Rule {

	/**
	 * <p>Constructor for RuleReplaceVarUnderTriplePattern.</p>
	 */
	public RuleReplaceVarUnderTriplePattern() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	protected void init() {
		final TriplePattern tp = new TriplePattern();
		final ReplaceVar repVar = new ReplaceVar();

		tp.setSucceedingOperator(new OperatorIDTuple(repVar, 0));
		repVar.setPrecedingOperator(tp);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(tp, "tp");
		subGraphMap.put(repVar, "repVar");

		startNode = repVar;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final ReplaceVar repVar = (ReplaceVar) mso.get("repVar");
		// 1. Are there only triple patterns as preceding operators?
		// 1. Do the triple patterns have only repVar as succeeding operator?
		// 2. Are all variables of the triple patterns used in the ReplaceVar
		// operator?
		for (final BasicOperator bo : repVar.getPrecedingOperators()) {
			if (!(bo instanceof TriplePattern))
				return false;
			else {
				final TriplePattern tp = (TriplePattern) bo;
				if (tp.getSucceedingOperators().size() > 1)
					return false;
				for (final Variable v : tp.getVariables()) {
					int found = 0;
					for (final Variable v2 : repVar
							.getSubstitutionsVariableRight()) {
						if (v2.equals(v)) {
							found++;
						}
					}
					if (found != 1)
						return false;
				}
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final ReplaceVar repVar = (ReplaceVar) mso.get("repVar");

		for (final OperatorIDTuple oidt : repVar.getSucceedingOperators())
			oidt.getOperator().removePrecedingOperator(repVar);

		for (final BasicOperator bo : repVar.getPrecedingOperators()) {
			final TriplePattern tp = (TriplePattern) bo; // this has been
			// checked in
			// checkPrecondition
			// Already checked in checkPrecondition: All variables in tp can be
			// replaced!
			for (final Variable v : tp.getVariables()) {
				final Variable rv = repVar.getReplacement(v);
				tp.replace(v, rv);
			}
			final List<OperatorIDTuple> list = new LinkedList<OperatorIDTuple>();
			list.addAll(repVar.getSucceedingOperators());
			tp.setSucceedingOperators(list);
			for (final OperatorIDTuple oidt : repVar.getSucceedingOperators())
				oidt.getOperator().addPrecedingOperator(tp);
		}

		// rootOperator.deleteParents();
		// rootOperator.setParents();
		// rootOperator.detectCycles();
		// should have been done manually: rootOperator.sendMessage(new
		// BoundVariablesMessage());
		deleted.add(repVar);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
