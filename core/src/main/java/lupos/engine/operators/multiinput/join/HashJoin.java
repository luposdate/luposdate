
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.engine.operators.multiinput.join;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.ParallelIteratorMultipleQueryResults;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.optional.OptionalResult;
import lupos.misc.debug.DebugStep;
public class HashJoin extends Join {

	protected ParallelIteratorMultipleQueryResults[] operands = {	new ParallelIteratorMultipleQueryResults(),
																	new ParallelIteratorMultipleQueryResults()};

	/**
	 * <p>init.</p>
	 */
	public void init() { // add init code here
	}

	/** {@inheritDoc} */
	@Override
	public synchronized QueryResult process(final QueryResult queryResult,
			final int operandID) {
		this.operands[operandID].addQueryResult(queryResult);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized OptionalResult processJoin(final QueryResult queryResult,
			final int operandID) {
		this.operands[operandID].addQueryResult(queryResult);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public OptionalResult joinBeforeEndOfStream() {
		if (!this.operands[0].isEmpty()) {
			if (!this.operands[1].isEmpty()) {
				return this.joinOptionalResult(this.operands[0].getQueryResult(), this.operands[1].getQueryResult());
			} else {
				final OptionalResult or = new OptionalResult();
				final QueryResult left = this.operands[0].getQueryResult();
				left.materialize();
				or.setJoinPartnerFromLeftOperand(left);
				or.setJoinResult(left);
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		if (!this.operands[0].isEmpty() && !this.operands[1].isEmpty()) {
			final QueryResult qr = this.join(this.operands[0].getQueryResult(), this.operands[1].getQueryResult());
			if (qr != null) {
				this.realCardinality = qr.size();
				for (final OperatorIDTuple opId : this.succeedingOperators) {
					opId.processAll(qr);
				}
			}
		}
		this.operands[0].release();
		this.operands[1].release();
		this.operands[0] = new ParallelIteratorMultipleQueryResults();
		this.operands[1] = new ParallelIteratorMultipleQueryResults();
		return msg;
	}

	/**
	 * <p>join.</p>
	 *
	 * @param left a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param right a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
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
		final NodeInPartitionTree rootSmaller = this.buildPartitionsOfSmallerBag(smaller, hashFunctions, 0);
		// Ib) now build partitions of the larger bag in the same way as the
		// smaller bag
		final NodeInPartitionTree rootLarger = this.buildPartitionsOfLargerBag(larger, rootSmaller, hashFunctions, 0);
		// II) Probing phase: now join the corresponding partitions of the
		// smaller with the larger bag...
		// Resources are released during probing...
		final QueryResult result = new QueryResult(0);
		this.probe(rootSmaller, rootLarger, result);
		// System.out.println("HashJoin: Result sizes: Left:"+left.size()+
		// " Right:"+right.size()+" Result:"+result.size());
		if (result.size() > 0) {
			return result;
		} else {
			return null;
		}
	}

	/**
	 * <p>buildPartitionsOfSmallerBag.</p>
	 *
	 * @param smaller a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param hashFunctions a {@link java.util.List} object.
	 * @param position a int.
	 * @return a {@link lupos.engine.operators.multiinput.join.NodeInPartitionTree} object.
	 */
	protected NodeInPartitionTree buildPartitionsOfSmallerBag(
			final QueryResult smaller, final List<HashFunction> hashFunctions,
			final int position) {
		if (smaller.size() <= LeafNodeInPartitionTree.maxNumberEntries) {
			return new LeafNodeInPartitionTree(smaller);
		}
		final QueryResult[] partitions = new QueryResult[InnerNodeInPartitionTree.numberChildren];
		for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
			partitions[i] = QueryResult.createInstance(0);
		}
		while (hashFunctions.size() <= position) {
			hashFunctions.add(new HashFunction());
		}
		final HashFunction h = hashFunctions.get(position);
		for (final Bindings b : smaller) {
			final Collection<Literal> key = HashFunction.getKey(b,
					this.intersectionVariables);
			if (key != null) {
				partitions[(int) h.hash(key) % InnerNodeInPartitionTree.numberChildren].add(b);
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
			if (partitions[i].size() == smaller.size()) {
				return new LeafNodeInPartitionTree(smaller);
			}
		}
		final InnerNodeInPartitionTree innerNode = new InnerNodeInPartitionTree();
		for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
			final NodeInPartitionTree node = this.buildPartitionsOfSmallerBag(
					partitions[i], hashFunctions, position + 1);
			if (node instanceof InnerNodeInPartitionTree) {
				partitions[i].release();
				partitions[i] = null;
			}
			innerNode.nodes.add(node);
		}
		return innerNode;
	}

	/**
	 * <p>buildPartitionsOfLargerBag.</p>
	 *
	 * @param larger a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param rootSmaller a {@link lupos.engine.operators.multiinput.join.NodeInPartitionTree} object.
	 * @param hashFunctions a {@link java.util.List} object.
	 * @param position a int.
	 * @return a {@link lupos.engine.operators.multiinput.join.NodeInPartitionTree} object.
	 */
	protected NodeInPartitionTree buildPartitionsOfLargerBag(
			final QueryResult larger, final NodeInPartitionTree rootSmaller,
			final List<HashFunction> hashFunctions, final int position) {
		if (rootSmaller instanceof LeafNodeInPartitionTree) {
			return new LeafNodeInPartitionTree(larger);
		}
		final InnerNodeInPartitionTree innerNodeOfSmallerBag = (InnerNodeInPartitionTree) rootSmaller;
		final QueryResult[] partitions = new QueryResult[InnerNodeInPartitionTree.numberChildren];
		for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
			partitions[i] = QueryResult.createInstance(0);
		}
		final HashFunction h = hashFunctions.get(position);
		for (final Bindings b : larger) {
			final Collection<Literal> key = HashFunction.getKey(b, this.intersectionVariables);
			if (key != null) {
				partitions[(int) h.hash(key) % InnerNodeInPartitionTree.numberChildren].add(b);
			} else {
				for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
					partitions[i].add(b);
				}
			}
		}
		final InnerNodeInPartitionTree innerNode = new InnerNodeInPartitionTree();
		final Iterator<NodeInPartitionTree> it_smallerbag = innerNodeOfSmallerBag.nodes.iterator();
		for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
			final NodeInPartitionTree node = this.buildPartitionsOfLargerBag(
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

	/**
	 * <p>probe.</p>
	 *
	 * @param rootSmaller a {@link lupos.engine.operators.multiinput.join.NodeInPartitionTree} object.
	 * @param rootLarger a {@link lupos.engine.operators.multiinput.join.NodeInPartitionTree} object.
	 * @param result a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	protected void probe(final NodeInPartitionTree rootSmaller,
			final NodeInPartitionTree rootLarger, final QueryResult result) {
		if (rootSmaller instanceof LeafNodeInPartitionTree) {
			if (!(rootLarger instanceof LeafNodeInPartitionTree)) {
				System.err.println("Partition error." + HashJoin.class);
				return;
			}
			final LeafNodeInPartitionTree smaller = (LeafNodeInPartitionTree) rootSmaller;
			final Collection<Bindings> cb;
//			if (smaller.partition.size() <= LeafNodeInPartitionTree.maxNumberEntries) {
//				cb = new ArrayList<Bindings>(smaller.partition.size());
//				// load smaller partition into main memory!
//				for (final Bindings b : smaller.partition) {
//					cb.add(b);
//				}
//			} else {
				cb = smaller.partition.getCollection();
//			}

			for (final Bindings b1 : ((LeafNodeInPartitionTree) rootLarger).partition) {
				// join with smaller partition, which is already in main memory!
				for (final Bindings b2 : cb) {
					Join.joinBindings(result, b1.clone(), b2);
				}
			}
			smaller.release();
			rootLarger.release();
		} else {
			final Iterator<NodeInPartitionTree> first_it = ((InnerNodeInPartitionTree) rootSmaller).nodes.iterator();
			final Iterator<NodeInPartitionTree> second_it = ((InnerNodeInPartitionTree) rootLarger).nodes.iterator();
			for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
				this.probe(first_it.next(), second_it.next(), result);
			}
			rootSmaller.release();
			rootLarger.release();
		}
	}

	/**
	 * <p>probeOptional.</p>
	 *
	 * @param rootSmaller a {@link lupos.engine.operators.multiinput.join.NodeInPartitionTree} object.
	 * @param rootLarger a {@link lupos.engine.operators.multiinput.join.NodeInPartitionTree} object.
	 * @param smallerIsLeftOperand a boolean.
	 * @param or a {@link lupos.engine.operators.multiinput.optional.OptionalResult} object.
	 */
	protected void probeOptional(final NodeInPartitionTree rootSmaller,
			final NodeInPartitionTree rootLarger,
			final boolean smallerIsLeftOperand, final OptionalResult or) {
		if (rootSmaller instanceof LeafNodeInPartitionTree) {
			if (!(rootLarger instanceof LeafNodeInPartitionTree)) {
				System.err.println("Partition error. "+ HashJoin.class);
				return;
			}

			final LeafNodeInPartitionTree smaller = (LeafNodeInPartitionTree) rootSmaller;
			final Collection<Bindings> cb;
//			if (smaller.partition.size() <= LeafNodeInPartitionTree.maxNumberEntries) {
//				cb = new ArrayList<Bindings>(smaller.partition.size());
//				// load smaller partition into main memory!
//				for (final Bindings b : smaller.partition) {
//					cb.add(b);
//				}
//			} else {
				cb = smaller.partition.getCollection();
//			}

			for (final Bindings b1 : ((LeafNodeInPartitionTree) rootLarger).partition) {
				for (final Bindings b2 : cb) {
					final int size = or.getJoinResult().size();
					Join.joinBindings(or.getJoinResult(), b1.clone(), b2);
					if (or.getJoinResult().size() > size) {
						if (smallerIsLeftOperand) {
							or.getJoinPartnerFromLeftOperand().add(b2);
						} else {
							or.getJoinPartnerFromLeftOperand().add(b1);
						}
					}
				}
			}
			smaller.release();
			rootLarger.release();
		} else {
			final Iterator<NodeInPartitionTree> first_it = ((InnerNodeInPartitionTree) rootSmaller).nodes.iterator();
			final Iterator<NodeInPartitionTree> second_it = ((InnerNodeInPartitionTree) rootLarger).nodes.iterator();
			for (int i = 0; i < InnerNodeInPartitionTree.numberChildren; i++) {
				this.probeOptional(first_it.next(), second_it.next(), smallerIsLeftOperand, or);
			}
			rootSmaller.release();
			rootLarger.release();
		}
	}

	private OptionalResult joinOptionalResult(final QueryResult left,
			final QueryResult right) {
		if (left == null || right == null) {
			return null;
		}
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
		final NodeInPartitionTree rootSmaller = this.buildPartitionsOfSmallerBag(smaller, hashFunctions, 0);
		// Ib) now build partitions of the larger bag in the same way as the
		// smaller bag
		final NodeInPartitionTree rootLarger = this.buildPartitionsOfLargerBag(larger, rootSmaller, hashFunctions, 0);
		// II) Probing phase: now join the corresponding partitions of the
		// smaller with the larger bag...
		// Resources are released during probing...
		final OptionalResult or = new OptionalResult(new QueryResult(0), new QueryResult(0));
		this.probeOptional(rootSmaller, rootLarger, left.size() < right.size(), or);
		return or;
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult deleteQueryResult(final QueryResult queryResult, final int operandID) {
		this.operands[operandID].removeAll(queryResult);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void deleteAll(final int operandID) {
		this.operands[operandID].release();
		this.operands[operandID]= new ParallelIteratorMultipleQueryResults();
	}

	/** {@inheritDoc} */
	@Override
	protected boolean isPipelineBreaker() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		if (!this.operands[0].isEmpty() && !this.operands[1].isEmpty()) {
			final QueryResult qr = this.join(this.operands[0].getQueryResult(), this.operands[1].getQueryResult());
			if (qr != null) {
				this.realCardinality = qr.size();
				for (final OperatorIDTuple opId : this.succeedingOperators) {
					final QueryResultDebug qrDebug = new QueryResultDebug(qr, debugstep, this, opId.getOperator(), true);
					((Operator) opId.getOperator()).processAllDebug(qrDebug, opId.getId(), debugstep);
				}
			}
		}
		this.operands[0].release();
		this.operands[1].release();
		this.operands[0]= new ParallelIteratorMultipleQueryResults();
		this.operands[1]= new ParallelIteratorMultipleQueryResults();
		return msg;
	}
}
