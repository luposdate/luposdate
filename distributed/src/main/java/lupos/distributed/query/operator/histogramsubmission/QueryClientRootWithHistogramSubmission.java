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
package lupos.distributed.query.operator.histogramsubmission;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Item;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.QueryClientCostBasedOptimizer;

/**
 * Represents the root node in the operator graph for distributed query processing.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class QueryClientRootWithHistogramSubmission extends Root {

	protected final IHistogramExecutor histogramExecutor;

	/**
	 * <p>Constructor for QueryClientRootWithHistogramSubmission.</p>
	 *
	 * @param dataset a {@link lupos.engine.operators.index.Dataset} object.
	 * @param histogramExecutor a {@link lupos.distributed.query.operator.histogramsubmission.IHistogramExecutor} object.
	 */
	public QueryClientRootWithHistogramSubmission(final Dataset dataset, final IHistogramExecutor histogramExecutor){
		super();
		this.dataset = dataset;
		this.histogramExecutor = histogramExecutor;
	}

	/** {@inheritDoc} */
	@Override
	public BasicIndexScan newIndexScan(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePatterns, final Item data) {
		return new QueryClientIndexScanWithHistogramSubmission(succeedingOperator, triplePatterns, data, this);
	}

	/** {@inheritDoc} */
	@Override
	public Root newInstance(final Dataset dataset_param) {
		return new QueryClientRootWithHistogramSubmission(dataset_param, this.histogramExecutor);
	}

	/** {@inheritDoc} */
	@Override
	public void optimizeJoinOrder(final int opt) {
		if(opt != BasicIndexScan.BINARY){
			super.optimizeJoinOrder(opt);
			return;
		}

		final List<OperatorIDTuple> c = new LinkedList<OperatorIDTuple>();

		for (final OperatorIDTuple oit : this.succeedingOperators) {
			if (oit.getOperator() instanceof BasicIndexScan) {
				final BasicIndexScan indexScan = (BasicIndexScan) oit.getOperator();

				if(indexScan.joinOrderToBeOptimized()){
					final lupos.engine.operators.index.Root root;
					switch (opt) {
					default:
					case BasicIndexScan.BINARY:
						root = QueryClientCostBasedOptimizer.rearrangeJoinOrder(indexScan);
						break;
					}

					c.addAll(root.getSucceedingOperators());
				} else {
					c.add(oit);
				}
			} else {
				// Operators not being index scan operators should remain!
				c.add(oit);
			}
		}
		this.setSucceedingOperators(c);
		this.deleteParents();
		this.setParents();
		this.detectCycles();
		// has already been done before: this.sendMessage(new BoundVariablesMessage());
	}
}
