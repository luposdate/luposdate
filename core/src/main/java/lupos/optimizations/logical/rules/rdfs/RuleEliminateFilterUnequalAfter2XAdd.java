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
import java.util.List;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.AddBinding;
import lupos.engine.operators.singleinput.filter.Filter;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleEliminateFilterUnequalAfter2XAdd extends Rule {

	private boolean eliminateOnlyFilter;

	public RuleEliminateFilterUnequalAfter2XAdd() {
		super();
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Filter filter = (Filter) mso.get("filter");
		if (filter.getPrecedingOperators().size() > 1)
			return false;
		final AddBinding add1 = (AddBinding) mso.get("add1");
		if (add1.getPrecedingOperators().size() > 1)
			return false;
		final AddBinding add2 = (AddBinding) mso.get("add2");

		lupos.sparql1_1.Node filterNode = filter.getNodePointer();
		if (filterNode.jjtGetNumChildren() > 0) {
			filterNode = filterNode.jjtGetChild(0);

			// filter must be an unequality expression of 2 variables...
			if (filterNode instanceof lupos.sparql1_1.ASTNotEqualsNode) {
				final lupos.sparql1_1.Node leftNode = filterNode.jjtGetChild(0);
				final lupos.sparql1_1.Node rightNode = filterNode.jjtGetChild(1);
				if (rightNode instanceof lupos.sparql1_1.ASTVar
						&& leftNode instanceof lupos.sparql1_1.ASTVar) {

					// both variables must be the same ones as in the 2 Add
					// operators...
					final Variable leftFilterVar = new Variable(
							((lupos.sparql1_1.ASTVar) leftNode).getName());
					final Variable rightFilterVar = new Variable(
							((lupos.sparql1_1.ASTVar) rightNode).getName());
					if ((add1.getVar().equalsNormalOrVariableInInferenceRule(
							leftFilterVar) && add2.getVar()
							.equalsNormalOrVariableInInferenceRule(
									rightFilterVar))
							|| (add1.getVar()
									.equalsNormalOrVariableInInferenceRule(
											rightFilterVar) && add2.getVar()
									.equalsNormalOrVariableInInferenceRule(
											leftFilterVar))) {

						// if literals of the variables in the add operators are
						// unequal, rule must be applied
						if (add1.getLiteral().compareTo(add2.getLiteral()) != 0) {
							eliminateOnlyFilter = true;
						} else
							eliminateOnlyFilter = false;
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	protected void init() {
		final AddBinding add1 = new AddBinding(null, null);
		final AddBinding add2 = new AddBinding(null, null);
		final Filter filter = new Filter();

		add1.setSucceedingOperator(new OperatorIDTuple(add2, 0));
		add2.setPrecedingOperator(add1);
		add2.setSucceedingOperator(new OperatorIDTuple(filter, 0));
		filter.setPrecedingOperator(add2);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(add1, "add1");
		subGraphMap.put(add2, "add2");
		subGraphMap.put(filter, "filter");

		startNode = add1;
	}

	@SuppressWarnings("serial")
	@Override
	protected Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();

		final Filter filter = (Filter) mso.get("filter");

		// delete the filter operator
		final BasicOperator aboveFilter = filter.getPrecedingOperators().get(0);

		if (eliminateOnlyFilter) {
			deleted.add(filter);

			final List<OperatorIDTuple> list = new LinkedList<OperatorIDTuple>();
			list.addAll(filter.getSucceedingOperators());
			for (final OperatorIDTuple oid : list) {
				oid.getOperator().removePrecedingOperator(filter);
				oid.getOperator().getPrecedingOperators().addAll(
						filter.getPrecedingOperators());
				for (final BasicOperator precFilter : filter
						.getPrecedingOperators()) {
					precFilter.removeSucceedingOperator(filter);
					precFilter.addSucceedingOperator(new OperatorIDTuple(oid
							.getOperator(), oid.getId()));
				}
			}
		} else {
			filter.visit(new SimpleOperatorGraphVisitor() {
				boolean ignore = false;

				public Object visit(final BasicOperator basicOperator) {
					if (ignore || (basicOperator instanceof PatternMatcher)
							|| (basicOperator instanceof Union))
						ignore = true;
					else {
						deleted.add(basicOperator);
						if (basicOperator instanceof Join) {
							if (basicOperator.getPrecedingOperators().size() <= 2) {
								for (BasicOperator prec : basicOperator
										.getPrecedingOperators()) {
									if (!deleted.contains(prec)
											&& prec.getSucceedingOperators()
													.size() == 1) {
										deleted.add(prec);
										while (!(prec instanceof PatternMatcher)
												&& prec.getPrecedingOperators()
														.size() == 1) {
											prec = prec.getPrecedingOperators()
													.get(0);
											if (prec.getSucceedingOperators()
													.size() > 1)
												break;
											deleted.add(prec);
										}
									}
								}
							}
						}
					}
					return null;
				}
			});

			BasicOperator parent = aboveFilter;
			while (parent.getSucceedingOperators().size() <= 1
					&& parent.getPrecedingOperators().size() == 1) {
				deleted.add(parent);
				parent = parent.getPrecedingOperators().get(0);
			}

			for (final BasicOperator basicOperator : deleted) {
				final List<BasicOperator> list = new LinkedList<BasicOperator>();
				list.addAll(basicOperator.getPrecedingOperators());

				for (final BasicOperator prec : list) {
					prec.removeSucceedingOperator(basicOperator);
					basicOperator.removePrecedingOperator(prec);
				}
			}
		}

		if (deleted.size() > 0 || added.size() > 0) {
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		} else {
			return null;
		}
	}

}
