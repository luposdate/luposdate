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
package lupos.distributed.p2p.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.distributed.operator.SubgraphContainer;
import lupos.distributed.p2p.distributionstrategy.AlternativeKeyContainer;
import lupos.distributed.p2p.distributionstrategy.NinefoldInsertionDistribution;
import lupos.distributed.p2p.distributionstrategy.SimplePartitionDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * Rule that integrates two subgraph containers with same key, into one request
 * in one subgraph, that is sent to other nodes
 * 
 * @author Bjoern
 * 
 */
public class PartitionSubgraphRule extends Rule {

	private lupos.engine.operators.multiinput.join.Join join1 = null;
	private lupos.engine.operators.multiinput.Union union1 = null;
	private lupos.engine.operators.multiinput.Union union2 = null;
	private lupos.distributed.operator.SubgraphContainer<?> sg1 = null;
	private Root root;
	private Logger log = Logger.getLogger(getClass());

	@SuppressWarnings("rawtypes")
	private boolean _checkPrivate0(BasicOperator _op) {
		/*
		 * we only check on Subgraphs
		 */
		if (!(_op instanceof lupos.distributed.operator.SubgraphContainer)) {
			return false;
		}

		/*
		 * store the subgraph and search for an union in succeding-list
		 */
		this.sg1 = (lupos.distributed.operator.SubgraphContainer<?>) _op;
		List<OperatorIDTuple> _succedingOperators_1_0 = _op
				.getSucceedingOperators();
		for (OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
			if (!(_sucOpIDTup_1_0.getOperator() instanceof lupos.engine.operators.multiinput.Union)) {
				continue;
			}
			/*
			 * found an union, now search for a join in succeding-list of the
			 * union operator
			 */
			this.union1 = (lupos.engine.operators.multiinput.Union) _sucOpIDTup_1_0
					.getOperator();
			List<OperatorIDTuple> _succedingOperators_2_0 = _sucOpIDTup_1_0
					.getOperator().getSucceedingOperators();
			for (OperatorIDTuple _sucOpIDTup_2_0 : _succedingOperators_2_0) {
				if (!(_sucOpIDTup_2_0.getOperator() instanceof lupos.engine.operators.multiinput.join.Join)) {
					continue;
				}
				/*
				 * found the join! now go backwards and search for an union in
				 * precedings-list of the join operator, but do not use the
				 * union, we just came from (union1)
				 */
				this.join1 = (lupos.engine.operators.multiinput.join.Join) _sucOpIDTup_2_0
						.getOperator();
				List<BasicOperator> _precedingOperators_3_1 = _sucOpIDTup_2_0
						.getOperator().getPrecedingOperators();
				for (BasicOperator _precOp_3_1 : _precedingOperators_3_1) {
					if (!(_precOp_3_1 instanceof lupos.engine.operators.multiinput.Union)) {
						continue;
					}
					// prevent the same way back in operator graph, so that
					// union1 != union2
					if (((lupos.engine.operators.multiinput.Union) _precOp_3_1)
							.equals(this.union1)) {
						continue;
					}
					/*
					 * we just found thus union we searched for! Now get its
					 * preceedings and get any subgraph container (if we found
					 * any subgraph container, we just found the building we
					 * searched for. We do not need the 2nd sg container.
					 */
					this.union2 = (lupos.engine.operators.multiinput.Union) _precOp_3_1;
					List<BasicOperator> _precedingOperators_4_0 = _precOp_3_1
							.getPrecedingOperators();
					for (BasicOperator _precOp_4_0 : _precedingOperators_4_0) {
						if (!(_precOp_4_0 instanceof lupos.distributed.operator.SubgraphContainer)) {
							continue;
						}
						/*
						 * check whether the both subgraph-container fits for
						 * partition joining!
						 */
						if (!compareBothSubgraphContainer(
								this.sg1,
								(lupos.distributed.operator.SubgraphContainer) _precOp_4_0)) {
							continue;
						}
						// List<OperatorIDTuple> _succedingOperators_3_0 =
						// _sucOpIDTup_2_0.getOperator().getSucceedingOperators();
						// this._dim_0 = -1;
						// this.all_finishing = new
						// lupos.engine.operators.BasicOperator[_succedingOperators_3_0.size()];

						// for(OperatorIDTuple _sucOpIDTup_3_0 :
						// _succedingOperators_3_0) {
						// this._dim_0 += 1;
						//
						// if(!this._checkPrivate1(_sucOpIDTup_3_0.getOperator()))
						// {
						// return false;
						// }
						// }
						/*
						 * all went good, we have this needed structure of
						 * partitions
						 */
						return true;
					}
				}
			}
		}
		/*
		 * no matching with this subgraph container
		 */
		return false;
	}

	/*
	 * Gets the key representation of any KeyContainer
	 */
	private static String getKey(KeyContainer<?> keyContainer) {
		return String.format("%s%s", keyContainer.type, keyContainer.key);
	}

	/*
	 * Gets the partition of an KeyContainer
	 */
	private String getPartition(KeyContainer<?> keyContainer) {
		String key = getKey(keyContainer);
		Pattern p = Pattern.compile("[SPO]\\d+$");
		Matcher m = p.matcher(key);
		while (m.find())
			return m.group();
		return null;
	}

	/*
	 * Compares an partition and two given subgraphs whether they match for
	 * parallel joining
	 */
	private boolean compareTripplePatternVariable(String keyToCheck,
			SubgraphContainer<?> sg1, SubgraphContainer<?> sg2) {
		/*
		 * get the index scan's and their triple pattern
		 */
		BasicIndexScan bis1 = this.getIndexScan(sg1.getRootOfSubgraph());
		BasicIndexScan bis2 = this.getIndexScan(sg2.getRootOfSubgraph());
		/*
		 * should never be null, but avoid faults by default :D
		 */
		if (bis1 == null || bis2 == null || keyToCheck.length() < 1)
			return false;

		/*
		 * get the triple pattern of both subgraph containers, iterate through
		 * the variables used in the first subgraph, then check whether the
		 * variables in the 2nd subgraph are used in first subgraph (so in key
		 * S0 the variable in subject of sg1 and sg2 have to be the same, in P1
		 * the predicate and so on)
		 */
		Collection<TriplePattern> tripple1 = bis1.getTriplePattern();
		Collection<TriplePattern> tripple2 = bis2.getTriplePattern();
		Set<Variable> boundVariabled = new HashSet<Variable>();

		/*
		 * just from textual representation into position in triple (S = subject
		 * = position:0)
		 */
		char keyCode = keyToCheck.charAt(0);
		int position = 0;
		switch (keyCode) {
		case 's':
		case 'S':
			position = 0;
			break;
		case 'p':
		case 'P':
			position = 1;
			break;
		case 'o':
		case 'O':
			position = 2;
			break;
		}

		/*
		 * get the variables on given position and store them temp.
		 */
		for (TriplePattern pattern : tripple1) {
			Item i = pattern.getPos(position);
			if (i instanceof Variable) {
				boundVariabled.add((Variable) i);
			}
		}
		/* now check 2nd triple patterns, whether the same variable is used */
		for (TriplePattern pattern : tripple2) {
			Item i = pattern.getPos(position);
			if (i instanceof Variable) {
				if (boundVariabled.contains(i)) {
					return true;
				}
			}
		}
		/*
		 * the given subgraphs does not match for local joining of partitions
		 * because their keys and tripple patterns are too different! ;(
		 */
		return false;
	}

	/*
	 * compares two subgraphs whether they match for local joining
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean compareBothSubgraphContainer(SubgraphContainer sg1,
			SubgraphContainer sg2) {
		// if the keys are KeyContainer
		if (sg1.getKey() instanceof KeyContainer<?>
				&& sg2.getKey() instanceof KeyContainer<?>) {
			/*
			 * get their keys and compare its partitions, whether local join can
			 * be processed
			 */
			KeyContainer<?> keyVal1 = (KeyContainer<?>) sg1.getKey();
			KeyContainer<?> keyVal2 = (KeyContainer<?>) sg2.getKey();
			String partitionKey1 = getPartition(keyVal1);
			String partitionKey2 = getPartition(keyVal2);
			if (partitionKey1 == null || partitionKey2 == null)
				return false;
			/*
			 * if the partition is the same (e.g. O0 and O0 or S1 and S1), and
			 * the triple pattern variable on the partition key is the same (so
			 * O0 and O0 have to be the same variable e.g ?o)
			 */
			if (partitionKey1.equals(partitionKey2)
					&& compareTripplePatternVariable(partitionKey1, sg1, sg2)) {
				/*
				 * remove the alternatives, because we do now join these
				 * subgraphs, and key-exchange afterwards would fail, because
				 * inserted subgraph has the original key
				 */
				if (keyVal1 instanceof AlternativeKeyContainer)
					((AlternativeKeyContainer<?>) keyVal1).removeAlternatives();
				if (keyVal2 instanceof AlternativeKeyContainer)
					((AlternativeKeyContainer<?>) keyVal1).removeAlternatives();
				return true;
			}

			/*
			 * the 2nd subgraph has alternative keys, so check each alternative,
			 * whether it fit to the key of the first subgraph
			 */
			if (sg2.getKey() instanceof AlternativeKeyContainer<?>) {
				AlternativeKeyContainer<?> k2 = (AlternativeKeyContainer<?>) sg2
						.getKey();
				for (KeyContainer<?> key2 : k2.getAlternatives()) {
					String partitionKey2alt = getPartition(key2);
					if (partitionKey2alt == null)
						continue;
					if (partitionKey1.equals(partitionKey2alt)
							&& compareTripplePatternVariable(partitionKey2alt,
									sg1, sg2)) {
						/*
						 * now change this key, and remove the alternatives, so
						 * that this subgraph-key is not changed later again
						 */
						sg2.changeKey(key2);
						if (k2 instanceof AlternativeKeyContainer)
							((AlternativeKeyContainer<?>) k2)
									.removeAlternatives();
						if (sg1.getKey() instanceof AlternativeKeyContainer)
							((AlternativeKeyContainer<?>) sg1.getKey())
									.removeAlternatives();
						return true;
					}
				}
			}
			/*
			 * the 1st subgraph has alternative keys, so check each alternative,
			 * whether it fit to the key of the second subgraph
			 */
			if (sg1.getKey() instanceof AlternativeKeyContainer<?>) {
				AlternativeKeyContainer<?> k1 = (AlternativeKeyContainer<?>) sg1
						.getKey();
				for (KeyContainer<?> key1 : k1.getAlternatives()) {
					String partitionKey1alt = getPartition(key1);
					if (partitionKey1alt == null)
						continue;
					if (partitionKey1alt.equals(partitionKey2)
							&& compareTripplePatternVariable(partitionKey2,
									sg1, sg2)) {
						sg1.changeKey(key1);
						if (sg1.getKey() instanceof AlternativeKeyContainer)
							((AlternativeKeyContainer<?>) sg1.getKey())
									.removeAlternatives();
						if (k1 instanceof AlternativeKeyContainer)
							((AlternativeKeyContainer<?>) k1)
									.removeAlternatives();
						return true;
					}
				}
			}

			/*
			 * the last case: both (key 1 and key 2) have alternative keys, so
			 * check all combinations
			 */
			if (sg1.getKey() instanceof AlternativeKeyContainer<?>
					&& sg2.getKey() instanceof AlternativeKeyContainer<?>) {
				AlternativeKeyContainer<?> k1 = (AlternativeKeyContainer<?>) sg1
						.getKey();
				AlternativeKeyContainer<?> k2 = (AlternativeKeyContainer<?>) sg2
						.getKey();
				/*
				 * iterate through all alternatives in first subgraph
				 */
				for (KeyContainer<?> key1 : k1.getAlternatives()) {
					/*
					 * get the partition
					 */
					final String partitionKey1alt = getPartition(key1);
					if (partitionKey1alt == null)
						continue;
					/*
					 * iterate though all alternatives in seconds subgraph
					 */
					for (KeyContainer<?> key2 : k2.getAlternatives()) {
						/*
						 * get the partition
						 */
						final String partitionKey2alt = getPartition(key2);
						if (partitionKey2alt == null)
							continue;
						/*
						 * check same as above
						 */
						if (partitionKey1alt.equals(partitionKey2alt)
								&& compareTripplePatternVariable(
										partitionKey1alt, sg1, sg2)) {
							sg1.changeKey(key1);
							sg2.changeKey(key2);
							if (sg1.getKey() instanceof AlternativeKeyContainer)
								((AlternativeKeyContainer<?>) sg1.getKey())
										.removeAlternatives();
							if (sg2.getKey() instanceof AlternativeKeyContainer)
								((AlternativeKeyContainer<?>) sg2.getKey())
										.removeAlternatives();
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Creates the partition rule which is to be used to offer "local joining"
	 * in subgraphs on partitions used in {@link SimplePartitionDistribution} or
	 * {@link NinefoldInsertionDistribution}.
	 */
	public PartitionSubgraphRule() {
		this.startOpClass = lupos.distributed.operator.SubgraphContainer.class;
		this.ruleName = "DistributionStrategyOptimization";
	}

	@Override
	protected boolean check(BasicOperator _op) {
		/*
		 * this is just a trick to store the root, because Operators which are
		 * suceedings of the root, does not have any preceding operator. To
		 * remove these items, you have to access the root and remove this
		 * Operator.
		 */
		if (_op instanceof Root)
			this.root = (Root) _op;
		return this._checkPrivate0(_op);
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	@Override
	protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
		/*
		 * just make sure, that the formation in operator graph is right!
		 */
		BasicOperator opp = sg1.getPrecedingOperators().size() > 0 ? sg1
				.getPrecedingOperators().get(0) : null;

		/*
		 * we just accept subgraphs which succeeding operator is the union
		 * operator
		 */
		for (BasicOperator prec : union1.getPrecedingOperators()) {
			if (!(prec instanceof SubgraphContainer))
				return;
		}
		for (BasicOperator prec : union2.getPrecedingOperators()) {
			if (!(prec instanceof SubgraphContainer))
				return;
		}
		/*
		 * the size of partitions should be the same!
		 */
		if (union1.getPrecedingOperators().size() != union2
				.getPrecedingOperators().size())
			return;

		// add new operators...
		Join join_new = new lupos.engine.operators.multiinput.join.Join();
		join_new.setIntersectionVariables(join1.getIntersectionVariables());
		join_new.setUnionVariables(union1.getUnionVariables());

		Union union_new = new lupos.engine.operators.multiinput.Union();
		union_new.setIntersectionVariables(union1.getIntersectionVariables());
		union_new.setUnionVariables(union1.getUnionVariables());

		int countingUnions = 0;
		firstLoop: for (;;) {
			// for each 1st sg container
			for (BasicOperator prec : union1.getPrecedingOperators()) {
				boolean match = false;
				// find the matching second one
				for (BasicOperator prec2 : union2.getPrecedingOperators()) {
					if (!compareBothSubgraphContainer((SubgraphContainer) prec,
							(SubgraphContainer) prec2))
						continue;
					match = true;
					/*
					 * store with better variable name
					 */
					SubgraphContainer sg1 = (SubgraphContainer) prec;
					SubgraphContainer sg2 = (SubgraphContainer) prec2;

					// remove succedding UNION-operator for both subgraphs
					for (OperatorIDTuple eachSucc : prec
							.getSucceedingOperators()) {
						prec.removeSucceedingOperator(eachSucc);
						eachSucc.getOperator().removePrecedingOperator(prec);
					}
					for (OperatorIDTuple eachSucc : prec2
							.getSucceedingOperators()) {
						prec2.removeSucceedingOperator(eachSucc);
						eachSucc.getOperator().removePrecedingOperator(prec2);
					}

					// remove 2nd sg
					for (BasicOperator bo : sg2.getPrecedingOperators()) {
						bo.removeSucceedingOperator(sg2);
						sg2.removePrecedingOperator(bo);
					}

					/*
					 * join with is to be included into the subgraph container
					 */
					Join smallJoin = new Join();
					smallJoin.cloneFrom(join_new);

					// remove so that the for-loop will end!
					union1.removePrecedingOperator(prec);
					union2.removePrecedingOperator(prec2);

					/*
					 * get the index scan in first subgraph
					 */
					BasicIndexScan bis = getIndexScan(sg1.getRootOfSubgraph());
					if (bis == null)
						continue;
					/*
					 * you have to clone this list, because if changing
					 * something, the list is updated immediately, but we want
					 * to access the removed items later!
					 */
					List<OperatorIDTuple> _bisSucc = bis
							.getSucceedingOperators();
					List<OperatorIDTuple> bisSucc = new ArrayList<>(
							_bisSucc.size());
					for (OperatorIDTuple toClone : _bisSucc) {
						bisSucc.add(toClone);
					}

					/*
					 * now add the 2nd subgraph container in the first subgraph
					 * container
					 */
					sg1.getRootOfSubgraph().addSucceedingOperator(sg2);

					/*
					 * remove old connections of the 2nd subgraph (because it
					 * should be included into the subgraph)
					 */
					for (OperatorIDTuple op : bisSucc) {
						bis.removeSucceedingOperator(op);
						op.getOperator().removePrecedingOperator(bis);
					}
					/*
					 * connect the basic index scan and the 2nd subgraph
					 * container in the join-operator in the 1st subgraph
					 * container
					 */
					bis.addSucceedingOperator(smallJoin, 0);
					smallJoin.addPrecedingOperator(sg2);
					sg2.addSucceedingOperator(smallJoin, 1);
					smallJoin.addPrecedingOperator(bis);
					/*
					 * now connect the join with the succeeding operators of the
					 * old basic index scan (here we use the hack, to clone the
					 * succeeding list of the index scan, because when we
					 * removed the connection and added the join, the list would
					 * have no content)
					 */
					for (OperatorIDTuple op : bisSucc) {
						smallJoin.addSucceedingOperator(op);
						op.getOperator().addPrecedingOperator(smallJoin);
					}
					/*
					 * now connect the UNION with the result of the 1st subgraph
					 * container. In this UNION all partitions are to be
					 * combined.
					 */
					union_new.addPrecedingOperator(sg1);
					OperatorIDTuple unionIDOperator = new OperatorIDTuple(
							union_new, countingUnions++);
					sg1.addSucceedingOperator(unionIDOperator);

					/*
					 * this is to be executed, if the 2nd subgraph is directly
					 * added as succeeding of the root, because in this case,
					 * the 2nd subgraph container has no preceding (i don't know
					 * why this is done this way)
					 */
					if (this.root != null) {
						this.root.removeSucceedingOperator(sg2);
					}

					/*
					 * add the intersections variables of the 2nd sg to the
					 * first one
					 */
					Collection<Variable> sg1interSection = sg1
							.getIntersectionVariables();
					Collection<Variable> sg2interSection = sg2
							.getIntersectionVariables();
					if (sg1interSection == null)
						sg1interSection = new HashSet<>();
					if (sg2interSection != null)
						sg1interSection.addAll(sg2interSection);
					sg1.setIntersectionVariables(sg1interSection);
					/*
					 * add the union variables of the 2nd sg to the first one
					 */
					Collection<Variable> sg1union = sg1.getUnionVariables();
					Collection<Variable> sg2union = sg2.getUnionVariables();
					if (sg1union == null)
						sg1union = new HashSet<>();
					if (sg2union != null)
						sg1union.addAll(sg2union);
					sg1.setUnionVariables(sg1union);

					log.debug(String.format(
							"Rule %s: Local join between %s and %s in %s",
							this.ruleName, sg1, sg2, sg1));

					continue firstLoop;
				}
				/*
				 * we have no match -> no partitions which can be used for local
				 * join -> exit!
				 */
				if (!match)
					return;
			}
			break;
		}

		/*
		 * stuff from the rule builder ...
		 */

		// remove obsolete connections...
		this.union1.removeSucceedingOperator(this.join1);
		this.join1.removePrecedingOperator(this.union1);

		// remove all connections to the follower of our processing tree
		// and add the new union
		List<OperatorIDTuple> succeddingsOfAll = this.join1
				.getSucceedingOperators();
		for (OperatorIDTuple _child : succeddingsOfAll) {
			_child.getOperator().removePrecedingOperator(this.join1);
			union_new.addSucceedingOperator(_child);
			_child.getOperator().addPrecedingOperator(union_new);
		}
		// delete unreachable operators...
		this.deleteOperatorWithoutParentsRecursive(this.join1, _startNodes);
		this.deleteOperatorWithoutParentsRecursive(this.union1, _startNodes);
		this.deleteOperatorWithoutParentsRecursive(this.union2, _startNodes);
		this.join1.removeFromOperatorGraph();
		this.union1.removeFromOperatorGraph();
		this.union2.removeFromOperatorGraph();
	}

	/*
	 * returns the first found basic index scan in the operators succeeding list
	 */
	private BasicIndexScan getIndexScan(final BasicOperator root) {
		final List<OperatorIDTuple> succs = root.getSucceedingOperators();
		if (succs == null | succs.size() == 0)
			return null;
		for (final OperatorIDTuple succ : succs) {
			final BasicOperator op = succ.getOperator();
			if (op instanceof BasicIndexScan) {
				return (BasicIndexScan) op;
			} else {
				BasicIndexScan res = null;
				/*
				 * some recursive call for the succeeding's of the succeeding
				 * operator
				 */
				if ((res = getIndexScan(op)) != null)
					return res;
			}
		}
		/*
		 * nothing found in child's list
		 */
		return null;

		// As alternative:

		// SimpleOperatorGraphVisitor sov = new SimpleOperatorGraphVisitor() {
		// @Override
		// public Object visit(BasicOperator basicOperator) {
		// if (basicOperator instanceof BasicIndexScan) return basicOperator;
		// return null;
		// }
		// };
		// return (BasicIndexScan) root.visit(sov);
	}
}
