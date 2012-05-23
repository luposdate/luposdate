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
package lupos.engine.operators.index.memoryindex;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Item;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.OptimizeJoinOrder;

public class IndexCollection extends
		lupos.engine.operators.index.IndexCollection {
	@Override
	public BasicIndex newIndex(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, final Item data) {
		return new MemoryIndex(succeedingOperator, triplePattern, data, this);
	}

	@Override
	public IndexCollection newInstance(Dataset dataset) {
		this.dataset = dataset;
		return new IndexCollection();
	}

	public void optimizeJoinOrderAccordingToMostRestrictionsAndLeastEntries(
			final Dataset dataset) {
		for (final OperatorIDTuple oit : succeedingOperators) {
			final BasicOperator basicOperator = oit.getOperator();
			if (basicOperator instanceof MemoryIndex) {
				final MemoryIndex index = (MemoryIndex) basicOperator;
				index.optimizeJoinOrderAccordingToMostRestrictionsAndLeastEntries(dataset);
			}
		}
	}

	public void optimizeJoinOrderAccordingToLeastEntries(final Dataset dataset) {
		for (final OperatorIDTuple oit : succeedingOperators) {
			if (oit.getOperator() instanceof MemoryIndex) {
				final MemoryIndex index = (MemoryIndex) oit
						.getOperator();
				index.optimizeJoinOrderAccordingToLeastEntries(dataset);
			}
		}
	}

	@Override
	public void optimizeJoinOrder(final int opt, final Dataset dataset) {
		switch (opt) {
		case BasicIndex.MOSTRESTRICTIONSLEASTENTRIES:
			optimizeJoinOrderAccordingToMostRestrictionsAndLeastEntries(dataset);
			break;
		case BasicIndex.LEASTENTRIES:
			optimizeJoinOrderAccordingToLeastEntries(dataset);
			break;
		case BasicIndex.Binary:
			makeBinaryJoin(dataset);
			break;
		default:
			super.optimizeJoinOrder(opt, dataset);
		}
	}

	public void makeBinaryJoin(final Dataset dataset) {
		final List<OperatorIDTuple> c = new LinkedList<OperatorIDTuple>();

		for (final OperatorIDTuple oit : succeedingOperators) {
			if (oit.getOperator() instanceof MemoryIndex) {
				final MemoryIndex index = (MemoryIndex) oit
						.getOperator();
				final lupos.engine.operators.index.IndexCollection indexCollection = OptimizeJoinOrder
						.getBinaryJoinWithManyMergeJoins(new IndexCollection(),
								index,
								OptimizeJoinOrder.PlanType.RELATIONALINDEX,
								dataset);
				c.addAll(indexCollection.getSucceedingOperators());
			} else
				c.add(oit);
		}
		setSucceedingOperators(c);
		this.deleteParents();
		this.setParents();
		this.detectCycles();
		// has already been done before: this.sendMessage(new
		// BoundVariablesMessage());
	}
}