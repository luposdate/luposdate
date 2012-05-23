/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.engine.operators.multiinput.join;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.optional.OptionalResult;
import lupos.misc.debug.DebugStep;

public class HashJoin extends Join {

	protected QueryResult left = null;
	protected QueryResult right = null;

	public void init() {
	}

	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		if (operandID == 0) {
			if (left != null)
				left.add(bindings);
			else
				left = bindings;
		} else if (operandID == 1) {
			if (right != null)
				right.add(bindings);
			else
				right = bindings;
		} else
			System.err.println("HashJoin is a binary operator, but received the operand number "
							+ operandID);
		return null;
	}

	@Override
	public synchronized OptionalResult processJoin(final QueryResult bindings,
			final int operandID) {
		if (operandID == 0) {
			if (left != null)
				left.add(bindings);
			else
				left = bindings;
		} else if (operandID == 1) {
			if (right != null)
				right.add(bindings);
			else
				right = bindings;
		} else
			System.err.println("Embedded HashJoin operator in Optional is a binary operator, but received the operand number "
							+ operandID);
		return null;
	}

	@Override
	public OptionalResult joinBeforeEndOfStream() {
		if (left != null) {
			if (right != null)
				return joinOptionalResult(left, right);
			else {
				final OptionalResult or = new OptionalResult();
				left.materialize();
				or.setJoinPartnerFromLeftOperand(left);
				or.setJoinResult(left);
			}
		}
		return null;
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		if (left != null && right != null) {
			final QueryResult qr = join(left, right);
			if (qr != null) {
				this.realCardinality = qr.size();
				for (final OperatorIDTuple opId : succeedingOperators) {
					opId.processAll(qr);
				}
			}
		}
		if (left != null)
			left.release();
		if (right != null)
			right.release();
		left = QueryResult.createInstance();
		right = QueryResult.createInstance();
		return msg;
	}

	public QueryResult join(final QueryResult left, final QueryResult right) {
		final QueryResult smaller;
		final QueryResult larger;
		if (left.size() < right.size()) {
			smaller = left;
			larger = right;
		} else {
			smaller = right;
			larger = left;
		}
		// I) building phase
		// Ia) now build partitions of the smaller bag
		final LinkedList<HashFunction> hashFunctions = new LinkedList<HashFunction>();
		final NodeInPartitionTree rootSmaller = buildPartitionsOfSmallerBag(
				smaller, hashFunctions, 0);
		// Ib) now build partitions of the larger bag in the same way as the
		// smaller bag
		final NodeInPartitionTree rootLarger = buildPartitionsOfLargerBag(
				larger, rootSmaller, hashFunctions, 0);
		// II) Probing phase: now join the corresponding partitions of the
		// smaller with the larger bag...
		final QueryResult result = new QueryResult(0);
		probe(rootSmaller, rootLarger, result);
		// System.out.println("HashJoin: Result sizes: Left:"+left.size()+
		// " Right:"+right.size()+" Result:"+result.size());
		if (result.size() > 0)
			return result;
		else
			return null;
	}

	protected NodeInPartitionTree buildPartitionsOfSmallerBag(
			final QueryResult smaller, final List<HashFunction> hashFunctions,
			final int position) {
		if (smaller.size() <= LeafNodeInPartitionTree.maxNumberEntries)
			return new LeafNodeInPartitionTree(smaller);
		final QueryResult[] partitions = new QueryResult[InnerNodeInPartitionTree.numberChildren];
		for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
			partitions[i] = QueryResult.createInstance(0 /*
														 * LeafNodeInPartitionTree.
														 * maxNumberEntries
														 */);
		}
		while (hashFunctions.size() <= position) {
			hashFunctions.add(new HashFunction());
		}
		final HashFunction h = hashFunctions.get(position);
		for (final Bindings b : smaller) {
			final Collection<Literal> key = HashFunction.getKey(b,
					intersectionVariables);
			if (key != null) {
				partitions[(int) h.hash(key)
						% InnerNodeInPartitionTree.numberChildren].add(b);
			} else {
				for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
					partitions[i].add(b);
				}
			}
		}
		// detect special case: all Bindings have the same key, where the
		// partitions do not become smaller!
		for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
			// System.out.println(i + ":" + partitions[i].size() + " <-> "
			// + smaller.size());
			if (partitions[i].size() == smaller.size())
				return new LeafNodeInPartitionTree(smaller);
		}
		final InnerNodeInPartitionTree innerNode = new InnerNodeInPartitionTree();
		for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
			final NodeInPartitionTree node = buildPartitionsOfSmallerBag(
					partitions[i], hashFunctions, position + 1);
			if (node instanceof InnerNodeInPartitionTree) {
				partitions[i].release();
				partitions[i] = null;
			}
			innerNode.nodes.add(node);
		}
		return innerNode;
	}

	protected NodeInPartitionTree buildPartitionsOfLargerBag(
			final QueryResult larger, final NodeInPartitionTree rootSmaller,
			final List<HashFunction> hashFunctions, final int position) {
		if (rootSmaller instanceof LeafNodeInPartitionTree)
			return new LeafNodeInPartitionTree(larger);
		final InnerNodeInPartitionTree innerNodeOfSmallerBag = (InnerNodeInPartitionTree) rootSmaller;
		final QueryResult[] partitions = new QueryResult[InnerNodeInPartitionTree.numberChildren];
		for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
			partitions[i] = QueryResult.createInstance(0 // LeafNodeInPartitionTree
					// .maxNumberEntries
					);
		}
		final HashFunction h = hashFunctions.get(position);
		for (final Bindings b : larger) {
			final Collection<Literal> key = HashFunction.getKey(b,
					intersectionVariables);
			if (key != null) {
				partitions[(int) h.hash(key)
						% InnerNodeInPartitionTree.numberChildren].add(b);
			} else {
				for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
					partitions[i].add(b);
				}
			}
		}
		final InnerNodeInPartitionTree innerNode = new InnerNodeInPartitionTree();
		final Iterator<NodeInPartitionTree> it_smallerbag = innerNodeOfSmallerBag.nodes
				.iterator();
		for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
			final NodeInPartitionTree node = buildPartitionsOfLargerBag(
					partitions[i], it_smallerbag.next(), hashFunctions,
					position + 1);
			if (node instanceof InnerNodeInPartitionTree) {
				partitions[i].release();
				partitions[i] = null;
			}
			innerNode.nodes.add(node);
		}
		return innerNode;
	}

	protected void probe(final NodeInPartitionTree rootSmaller,
			final NodeInPartitionTree rootLarger, final QueryResult result) {
		if (rootSmaller instanceof LeafNodeInPartitionTree) {
			if (!(rootLarger instanceof LeafNodeInPartitionTree)) {
				System.err.println("Partition error." + HashJoin.class);
				return;
			}
			final LeafNodeInPartitionTree smaller = (LeafNodeInPartitionTree) rootSmaller;
			final Collection<Bindings> cb;
			if (smaller.partition.size() <= LeafNodeInPartitionTree.maxNumberEntries) {
				cb = new LinkedList<Bindings>();
				// load smaller partition into main memory!
				for (final Bindings b : smaller.partition) {
					cb.add(b);
				}
			} else
				cb = smaller.partition.getCollection();

			for (final Bindings b1 : ((LeafNodeInPartitionTree) rootLarger).partition) {
				// join with smaller partition, which is already in main memory!
				for (final Bindings b2 : cb) {
					Join.joinBindings(result, b1.clone(), b2);
				}
			}
			smaller.partition.release();
			smaller.partition = null;
			((LeafNodeInPartitionTree) rootLarger).partition.release();
			((LeafNodeInPartitionTree) rootLarger).partition = null;
		} else {
			final Iterator<NodeInPartitionTree> first_it = ((InnerNodeInPartitionTree) rootSmaller).nodes
					.iterator();
			final Iterator<NodeInPartitionTree> second_it = ((InnerNodeInPartitionTree) rootLarger).nodes
					.iterator();
			for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
				probe(first_it.next(), second_it.next(), result);
			}
		}
	}

	protected void probeOptional(final NodeInPartitionTree rootSmaller,
			final NodeInPartitionTree rootLarger,
			final boolean smallerIsLeftOperand, final OptionalResult or) {
		if (rootSmaller instanceof LeafNodeInPartitionTree) {
			if (!(rootLarger instanceof LeafNodeInPartitionTree)) {
				System.err.println("Partition error. "+ HashJoin.class);
				return;
			}

			final LeafNodeInPartitionTree smaller = (LeafNodeInPartitionTree) rootSmaller;
			// load smaller partition into main memory!
			final Collection<Bindings> cb = new LinkedList<Bindings>();
			for (final Bindings b : smaller.partition) {
				cb.add(b);
			}

			for (final Bindings b1 : ((LeafNodeInPartitionTree) rootLarger).partition) {
				for (final Bindings b2 : cb) {
					final int size = or.getJoinResult().size();
					Join.joinBindings(or.getJoinResult(), b1.clone(), b2);
					if (or.getJoinResult().size() > size) {
						if (smallerIsLeftOperand)
							or.getJoinPartnerFromLeftOperand().add(b2);
						else
							or.getJoinPartnerFromLeftOperand().add(b1);
					}
				}
			}
			smaller.partition.release();
			smaller.partition = null;
			((LeafNodeInPartitionTree) rootLarger).partition.release();
			((LeafNodeInPartitionTree) rootLarger).partition = null;
		} else {
			final Iterator<NodeInPartitionTree> first_it = ((InnerNodeInPartitionTree) rootSmaller).nodes
					.iterator();
			final Iterator<NodeInPartitionTree> second_it = ((InnerNodeInPartitionTree) rootLarger).nodes
					.iterator();
			for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
				probeOptional(first_it.next(), second_it.next(),
						smallerIsLeftOperand, or);
			}
		}
	}

	private OptionalResult joinOptionalResult(final QueryResult left,
			final QueryResult right) {
		if (left == null || right == null)
			return null;
		final QueryResult smaller;
		final QueryResult larger;
		if (left.size() < right.size()) {
			smaller = left;
			larger = right;
		} else {
			smaller = right;
			larger = left;
		}
		// I) building phase
		// Ia) now build partitions of the smaller bag
		final LinkedList<HashFunction> hashFunctions = new LinkedList<HashFunction>();
		final NodeInPartitionTree rootSmaller = buildPartitionsOfSmallerBag(
				smaller, hashFunctions, 0);
		// Ib) now build partitions of the larger bag in the same way as the
		// smaller bag
		final NodeInPartitionTree rootLarger = buildPartitionsOfLargerBag(
				larger, rootSmaller, hashFunctions, 0);
		// II) Probing phase: now join the corresponding partitions of the
		// smaller with the larger bag...
		final OptionalResult or = new OptionalResult(new QueryResult(0),
				new QueryResult(0));
		probeOptional(rootSmaller, rootLarger, left.size() < right.size(), or);
		return or;
	}

	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		if (operandID == 0)
			left.removeAll(queryResult);
		else
			right.removeAll(queryResult);
		return null;
	}

	public void deleteAll(final int operandID) {
		if (operandID == 0) {
			left.release();
			left = null;
		} else {
			right.release();
			right = null;
		}
	}

	protected boolean isPipelineBreaker() {
		return true;
	}
	
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		if (left != null && right != null) {
			final QueryResult qr = join(left, right);
			if (qr != null) {
				this.realCardinality = qr.size();
				for (final OperatorIDTuple opId : succeedingOperators) {
					final QueryResultDebug qrDebug = new QueryResultDebug(qr,
							debugstep, this, opId.getOperator(), true);
					((Operator) opId.getOperator()).processAllDebug(qrDebug,
							opId.getId(), debugstep);
				}
			}
		}
		if (left != null)
			left.release();
		if (right != null)
			right.release();
		left = QueryResult.createInstance();
		right = QueryResult.createInstance();
		return msg;
	}
}
