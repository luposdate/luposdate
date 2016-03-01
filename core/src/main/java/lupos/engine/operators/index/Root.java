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
package lupos.engine.operators.index;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Item;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.RootChild;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.debug.DebugStep;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.MemoryIndexCostBasedOptimizer;
import lupos.optimizations.physical.joinorder.staticanalysis.jointree.BuildJoinTreeLeastNewVariables;
import lupos.optimizations.physical.joinorder.staticanalysis.withinindexscan.RearrangeTriplePatternsInIndexScanLeastEntries;
import lupos.optimizations.physical.joinorder.staticanalysis.withinindexscan.RearrangeTriplePatternsInIndexScanLeastNewVariables;
import lupos.optimizations.physical.joinorder.staticanalysis.withinindexscan.RearrangeTriplePatternsInIndexScanLeastNewVariablesAndLeastEntries;
public abstract class Root extends Operator {
	public List<String> defaultGraphs;
	public List<String> namedGraphs;
	public Dataset dataset;

	/**
	 * <p>Constructor for Root.</p>
	 */
	public Root() {
	}

	/**
	 * <p>Constructor for Root.</p>
	 *
	 * @param dataset a {@link lupos.engine.operators.index.Dataset} object.
	 */
	public Root(final Dataset dataset) {
		this.dataset = dataset;
	}

	/**
	 * <p>newIndexScan.</p>
	 *
	 * @param succeedingOperator a {@link lupos.engine.operators.OperatorIDTuple} object.
	 * @param triplePattern a {@link java.util.Collection} object.
	 * @param data a {@link lupos.datastructures.items.Item} object.
	 * @return a {@link lupos.engine.operators.index.BasicIndexScan} object.
	 */
	public abstract BasicIndexScan newIndexScan(OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, Item data);

	/**
	 * <p>startProcessing.</p>
	 */
	public void startProcessing() {
		if (this.succeedingOperators.isEmpty()) {
			return;
		}
		for (final OperatorIDTuple oit : this.succeedingOperators) {
			((RootChild) oit.getOperator()).startProcessing(this.dataset);
		}
	}


	/**
	 * <p>physicalOptimization.</p>
	 */
	public void physicalOptimization() {
		lupos.optimizations.physical.PhysicalOptimizations.replaceOperators(
				this, this);
	}

	/**
	 * <p>optimizeJoinOrder.</p>
	 *
	 * @param opt a int.
	 */
	public void optimizeJoinOrder(final int opt) {
		final List<OperatorIDTuple> c = new LinkedList<OperatorIDTuple>();

		for (final OperatorIDTuple oit : this.succeedingOperators) {
			if (oit.getOperator() instanceof BasicIndexScan) {
				final BasicIndexScan indexScan = (BasicIndexScan) oit.getOperator();

				if(indexScan.joinOrderToBeOptimized()){
					final lupos.engine.operators.index.Root root;
					switch (opt) {
					case BasicIndexScan.MOSTRESTRICTIONS:
						root = RearrangeTriplePatternsInIndexScanLeastNewVariables.rearrangeJoinOrder(indexScan);
						break;
					case BasicIndexScan.MOSTRESTRICTIONSLEASTENTRIES:
						root = RearrangeTriplePatternsInIndexScanLeastNewVariablesAndLeastEntries.rearrangeJoinOrder(indexScan);
						break;
					case BasicIndexScan.LEASTENTRIES:
						root = RearrangeTriplePatternsInIndexScanLeastNewVariables.rearrangeJoinOrder(indexScan);
						break;
					case BasicIndexScan.BINARY:
						root = MemoryIndexCostBasedOptimizer.rearrangeJoinOrder(indexScan);
						break;
					case BasicIndexScan.BINARYSTATICANALYSIS:
						root = BuildJoinTreeLeastNewVariables.rearrangeJoinOrder(indexScan);
						break;
					default:
						root = RearrangeTriplePatternsInIndexScanLeastEntries.rearrangeJoinOrder(indexScan);
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

	/**
	 * <p>remove.</p>
	 *
	 * @param i a {@link lupos.engine.operators.index.BasicIndexScan} object.
	 */
	public void remove(final BasicIndexScan i) {
		this.removeSucceedingOperator(i);
	}

	/**
	 * <p>newInstance.</p>
	 *
	 * @param dataset_param a {@link lupos.engine.operators.index.Dataset} object.
	 * @return a {@link lupos.engine.operators.index.Root} object.
	 */
	public abstract Root newInstance(Dataset dataset_param);

	/**
	 * <p>printGraphURLs.</p>
	 */
	public void printGraphURLs() {
		String graph;
		System.out.println();
		System.out.println("default graphs: ");
		if (this.defaultGraphs != null) {
			for (int i = 0; i < this.defaultGraphs.size(); i++) {
				graph = this.defaultGraphs.get(i);
				System.out.println(i + ": " + graph);
			}
		}
		System.out.println();
		System.out.println("named graphs: ");
		if (this.namedGraphs != null) {
			for (int i = 0; i < this.namedGraphs.size(); i++) {
				graph = this.namedGraphs.get(i);
				System.out.println(i + ": " + graph);
			}
		}
		System.out.println();
	}

	/**
	 * <p>startProcessingDebug.</p>
	 *
	 * @param debugstep a {@link lupos.misc.debug.DebugStep} object.
	 */
	public void startProcessingDebug(final DebugStep debugstep) {
		if (this.succeedingOperators.isEmpty()) {
			return;
		}
		for (final OperatorIDTuple oit : this.succeedingOperators) {
			((RootChild) oit.getOperator()).startProcessingDebug(this.dataset, debugstep);
		}
	}
}
