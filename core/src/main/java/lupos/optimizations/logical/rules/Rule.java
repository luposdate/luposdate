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
package lupos.optimizations.logical.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.misc.BitVector;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.findsubgraph.FindSubGraph;

public abstract class Rule {

	protected BasicOperator startNode;
	protected Map<BasicOperator, String> subGraphMap;
	protected Map<String, BasicOperator> transformation;
	protected boolean findall = false;
	protected Set<BasicOperator> alreadyAppliedTo = new HashSet<BasicOperator>();

	public Rule() {
		this.init();
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	public String getDescription() {
		String s = this.getName();
		if (s == null) {
			return null;
		}
		s = s.replaceAll(" ", "");
		s = s.toLowerCase();
		return s + "Rule";
	}

	public Rule[] getRulesToApply(final RuleEngine ruleEngine) {
		return null;
	}

	protected boolean apply(
			final BasicOperator op,
			final Map<Class<? extends BasicOperator>, Set<BasicOperator>> mapStartNodes) {

		final Set<BasicOperator> startNodesToCheck = mapStartNodes
				.get(this.startNode.getClass());
		if (startNodesToCheck != null) {
			for (final BasicOperator startNodeToCheck : startNodesToCheck) {
				final Map<String, BasicOperator> mso = FindSubGraph
						.checkSubGraph(startNodeToCheck, this.subGraphMap, this.startNode);
				if (mso != null) {
					if (!this.alreadyAppliedTo.contains(mso.get(this.subGraphMap
							.get(this.startNode)))
							&& this.checkPrecondition(mso)) {
						this.alreadyAppliedTo.add(mso
								.get(this.subGraphMap.get(this.startNode)));
						final Tuple<Collection<BasicOperator>, Collection<BasicOperator>> updateMap = this.transformOperatorGraph(
								mso, op);
						if (updateMap != null) {
							for (final BasicOperator toDelete : updateMap
									.getSecond()) {
								RuleEngine.deleteFromNodeMap(toDelete,
										mapStartNodes);
							}
							for (final BasicOperator toAdd : updateMap
									.getFirst()) {
								RuleEngine.addToNodeMap(toAdd, mapStartNodes);
							}
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	protected boolean applyDebug(
			final BasicOperator op,
			final Map<Class<? extends BasicOperator>, Set<BasicOperator>> mapStartNodes) {

		final Set<BasicOperator> startNodesToCheck = mapStartNodes
				.get(this.startNode.getClass());
		if (startNodesToCheck != null) {
			for (final BasicOperator startNodeToCheck : startNodesToCheck) {
				final Map<String, BasicOperator> mso = FindSubGraph
						.checkSubGraph(startNodeToCheck, this.subGraphMap, this.startNode);
				if (mso != null) {
					if (!this.alreadyAppliedTo.contains(mso.get(this.subGraphMap
							.get(this.startNode)))
							&& this.checkPrecondition(mso)) {
						this.alreadyAppliedTo.add(mso
								.get(this.subGraphMap.get(this.startNode)));
						System.out
								.println("Transform operator graph according rule "
										+ this.getClass().getSimpleName()
										+ " with name " + this.getName());
						final Tuple<Collection<BasicOperator>, Collection<BasicOperator>> updateMap = this.transformOperatorGraph(
								mso, op);
						if (updateMap != null) {
							for (final BasicOperator toDelete : updateMap
									.getSecond()) {
								RuleEngine.deleteFromNodeMap(toDelete,
										mapStartNodes);
							}
							for (final BasicOperator toAdd : updateMap
									.getFirst()) {
								RuleEngine.addToNodeMap(toAdd, mapStartNodes);
							}
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	protected abstract void init();

	protected abstract boolean checkPrecondition(Map<String, BasicOperator> mso);

	private boolean inSubgraph(final Map<String, BasicOperator> mso,
			final OperatorIDTuple opid) {
		for (final BasicOperator op : mso.values()) {
			if (opid.getOperator() == op) {
				return true;
			}
		}
		return false;
	}

	protected Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Map<BasicOperator, BasicOperator> operators = new HashMap<BasicOperator, BasicOperator>();

		// myRule.transformBasicOperatorGraph(mso,rootOperator);
		for (final String label : this.transformation.keySet()) {
			if (mso.containsKey(label)) {
				final BasicOperator op1 = this.transformation.get(label);
				final BasicOperator op2 = mso.get(label);
				final List<OperatorIDTuple> succs = op2
						.getSucceedingOperators();
				for (int i = succs.size() - 1; i >= 0; i--) {
					final OperatorIDTuple opID = succs.get(i);
					if (this.inSubgraph(mso, opID)) {
						succs.remove(i);
						opID.getOperator().removePrecedingOperator(op2);
						if (opID.getOperator().getPrecedingOperators().size() == 0) {
							deleted.add(opID.getOperator());
						}
					}
				}
				op2.addSucceedingOperators(op1.getSucceedingOperators());
				operators.put(op1, op2);
			} else {
				final BasicOperator toAdd = this.transformation.get(label).clone();
				added.add(toAdd);
				operators.put(this.transformation.get(label), toAdd);
			}
		}
		for (final BasicOperator op : operators.keySet()) {
			final BasicOperator realOp = operators.get(op);
			for (int i = 0; i < realOp.getSucceedingOperators().size(); i++) {
				final OperatorIDTuple succ = realOp.getSucceedingOperators()
						.get(i);
				if (operators.containsKey(succ.getOperator())) {
					realOp.getSucceedingOperators().set(
							i,
							new OperatorIDTuple(operators.get(succ
									.getOperator()), succ.getId()));
					operators.get(succ.getOperator()).addPrecedingOperator(
							realOp);
				}
			}
		}
		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		if (deleted.size() > 0 || added.size() > 0) {
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		} else {
			return null;
		}
	}

	protected BasicOperator setSucc(final BasicOperator op,
			final OperatorIDTuple... succ) {
		op.setSucceedingOperators(new LinkedList<OperatorIDTuple>());
		for (final OperatorIDTuple opid : succ) {
			op.addSucceedingOperator(opid);
		}
		return op;
	}

	/**
	 * This method performs the actual check to see if a rule needs to be
	 * reapplied after another rule made changes to the operator graph. This
	 * method is not intended to be called directly, instead it is called by the
	 * different subclasses of {@link lupos.optimizations.logical.rules.Rule}
	 * passing the individual comparison vector of the respective subclass as a
	 * paramater. This method exists to keep the logic for the reapplication
	 * check in one place, rather than having it implemented in each and every
	 * subclass.
	 *
	 * @see lupos.optimizations.logical.rules.Rule#isReapplicationNecessary(BitVector)
	 * @param comparisonVector
	 *            bit vector containtin all operators whose modification inside
	 *            the operator graph leads to a reapplication of the rule in
	 *            question
	 * @param vectorOfChanges
	 *            bit vector containing operators that were changed inside the
	 *            operator graph
	 * @return true if reapplication is necessary
	 */
	protected static boolean performCheckForReapplication(
			final BitVector comparisonVector, final BitVector vectorOfChanges) {
		return vectorOfChanges.oneBitInCommon(comparisonVector);
	}

	protected static HashMap<Class<? extends BasicOperator>, Integer> putToClassToPosMap(
			HashMap<Class<? extends BasicOperator>, Integer> classToPosMap,
			final Integer pos, final Class<? extends BasicOperator>... classes) {
		if (classToPosMap == null) {
			classToPosMap = new HashMap<Class<? extends BasicOperator>, Integer>();
		}
		for (final Class<? extends BasicOperator> classToPut : classes) {
			classToPosMap.put(classToPut, pos);
		}
		return classToPosMap;
	}
}