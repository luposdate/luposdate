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
package lupos.optimizations.logical.rules.generated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.datastructures.items.Variable;
import lupos.distributed.operator.ISubgraphExecutor;
import lupos.distributed.operator.SubgraphContainer;
import lupos.distributed.query.operator.withouthistogramsubmission.QueryClientIndexScan;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.TriplePatternNotSupportedError;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.singleinput.filter.*;
import lupos.engine.operators.singleinput.Result;
import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.sparql1_1.ParseException;

import org.json.JSONException;

public class AddSubGraphContainerRule extends Rule {

	public static IDistribution distribution;

	public static ISubgraphExecutor subgraphExecutor;

	private Filter getFilterFromIndexScan(final BasicOperator root) {
		final List<OperatorIDTuple> succs = root.getSucceedingOperators();
		if (succs.size() == 1) {
			for (final OperatorIDTuple succ : succs) {
				final BasicOperator op = succ.getOperator();
				if (op instanceof Filter) {
					return (Filter) op;
				}
			}
		}

		return null;

	}

	/**
	 * replace index scan operator with SubgraphContainer
	 * 
	 * @param _indexScan
	 *            the index scan operator
	 */
	private void replaceIndexScanOperatorWithSubGraphContainer(
			final BasicIndexScan _indexScan) {

		try {
			// get root
			final Root rootNodeOfOuterGraph = _indexScan.getRoot();

			Object[] keys = distribution.getKeysForQuerying(_indexScan
					.getTriplePattern().iterator().next());
			// TODO: 2) catch TriplePatternNotSupportedError and make union of
			// SubgraphContainer to all possible nodes...

			// remember original connections and connect new graph with these
			// connections
			final Collection<BasicOperator> preds = _indexScan
					.getPrecedingOperators();
			final List<OperatorIDTuple> succs = _indexScan
					.getSucceedingOperators();

			// create union - operation
			final Collection<Variable> v = _indexScan.getIntersectionVariables();
			final Union union = new Union();
			// the succeding of the union of all subgraphs is the succeding of the indexScan
			union.addSucceedingOperators(succs);
			union.setIntersectionVariables(v);
			union.setUnionVariables(v);
			
			/*
			 * here we collect all used subgraphs
			 */
			Collection<BasicOperator> subgraphs = new ArrayList<BasicOperator>();
			

			/*
			 * for each key for the given triple pattern ...
			 */
			int countingSubgraphs = 0;
			for (Object key : keys) {
				/*
				 * create new inner root
				 */
				final Root rootNodeOfSubGraph = rootNodeOfOuterGraph
						.newInstance(rootNodeOfOuterGraph.dataset);
				/*
				 * create subgraph
				 */
				final SubgraphContainer container = new SubgraphContainer(
						rootNodeOfSubGraph, key, subgraphExecutor);
				
				/*
				 * store new basic index scan instead of using it n-times, because same object used
				 */
				BasicIndexScan indexScan = new QueryClientIndexScan(_indexScan.getRoot(),_indexScan.getTriplePattern());
				
				/*
				 * store variables
				 */
				final HashSet<Variable> variables = new HashSet<Variable>(
						_indexScan.getIntersectionVariables());
				container.setUnionVariables(variables);
				container.setIntersectionVariables(variables);

				
				// generate new connections...
				final Filter filter = this.getFilterFromIndexScan(indexScan);
				if (filter != null) {
					if (indexScan.getUnionVariables().containsAll(
							filter.getUsedVariables())) {
						Filter newFilter;
						try {
							newFilter = new Filter(filter.toString().substring(
									0, filter.toString().length() - 2));
							indexScan
									.setSucceedingOperator(new OperatorIDTuple(
											newFilter, 0));
							newFilter
									.setSucceedingOperator(new OperatorIDTuple(
											new Result(), 0));
						} catch (final ParseException e) {
							e.printStackTrace();
						}

					} else {
						indexScan.setSucceedingOperator(new OperatorIDTuple(
								new Result(), 0));
					}
				} else {
					indexScan.setSucceedingOperator(new OperatorIDTuple(
							new Result(), 0));
				}

				// indexScan.setSucceedingOperator(new OperatorIDTuple(new
				// Result(),
				// 0));
				
				//connect indexScan in subgraph container
				rootNodeOfSubGraph.setSucceedingOperator(new OperatorIDTuple(
						indexScan, countingSubgraphs));
				rootNodeOfSubGraph.deleteParents();
				rootNodeOfSubGraph.setParents();

				// original connections set at new graph
				/*
				 * create Operator for union of all subgraphs 
				 */
				OperatorIDTuple unionIDOperator = new OperatorIDTuple(union, countingSubgraphs++);
				container.setSucceedingOperator(unionIDOperator);
				
				/*
				 * store this subgraph
				 */
				subgraphs.add(container);
			}
			
			//connect all subgraphs as predecessor of the new union
			union.addPrecedingOperators(subgraphs);

			
			/*
			 * create list of OperatorIDTuples connected to the subgraphs
			 */
			List<OperatorIDTuple> list = new ArrayList<OperatorIDTuple>();
			for (BasicOperator g : subgraphs) {
				list.add(new OperatorIDTuple(g, 0));
			}

			/*
			 * now connect the root with all new subgraphs
			 */
			for (final BasicOperator pred : preds) {
				/*
				 * and remove indexScan (because, this is moved to subgraph container)
				 */
				OperatorIDTuple a = pred.getOperatorIDTuple(_indexScan);
				if (a != null) {
					pred.removeSucceedingOperator(a);
				}
				//but add all subgraphs
				pred.addSucceedingOperators(list);
			}

			// iterate through the new predecessors of the successors of the
			// original index scan operators and set new SubgraphContainer
			for (final OperatorIDTuple succ : succs) {
				succ.getOperator().removePrecedingOperator(_indexScan);
				succ.getOperator().addPrecedingOperator(union);
			}
		} catch (final JSONException e1) {
			System.err.println(e1);
			e1.printStackTrace();
		} catch (final TriplePatternNotSupportedError e1) {
			System.err.println(e1);
			e1.printStackTrace();
		}
	}

	/**
	 * replace index scan operator with SubgraphContainer
	 * 
	 * @param indexScan
	 *            the index scan operator
	 */
	private void replaceIndexScanOperatorWithSubGraphContainer_old(
			final BasicIndexScan indexScan) {

		try {
			final Root rootNodeOfOuterGraph = indexScan.getRoot();
			final Root rootNodeOfSubGraph = rootNodeOfOuterGraph
					.newInstance(rootNodeOfOuterGraph.dataset);

			// TODO: 1) for several keys: union of different SubgraphContainer!
			Object[] keys = distribution.getKeysForQuerying(indexScan
					.getTriplePattern().iterator().next());
			// TODO: 2) catch TriplePatternNotSupportedError and make union of
			// SubgraphContainer to all possible nodes...
			final SubgraphContainer container = new SubgraphContainer(
					rootNodeOfSubGraph, keys[0], subgraphExecutor);
			final HashSet<Variable> variables = new HashSet<Variable>(
					indexScan.getIntersectionVariables());

			container.setUnionVariables(variables);
			container.setIntersectionVariables(variables);

			// remember original connections and connect new graph with these
			// connections
			final Collection<BasicOperator> preds = indexScan
					.getPrecedingOperators();
			final List<OperatorIDTuple> succs = indexScan
					.getSucceedingOperators();

			for (final BasicOperator pred : preds) {
				pred.getOperatorIDTuple(indexScan).setOperator(container);
			}

			// generate new connections...

			final Filter filter = this.getFilterFromIndexScan(indexScan);

			if (filter != null) {
				if (indexScan.getUnionVariables().containsAll(
						filter.getUsedVariables())) {
					Filter newFilter;
					try {
						newFilter = new Filter(filter.toString().substring(0,
								filter.toString().length() - 2));
						indexScan.setSucceedingOperator(new OperatorIDTuple(
								newFilter, 0));
						newFilter.setSucceedingOperator(new OperatorIDTuple(
								new Result(), 0));
					} catch (final ParseException e) {
						e.printStackTrace();
					}

				} else {
					indexScan.setSucceedingOperator(new OperatorIDTuple(
							new Result(), 0));
				}
			} else {
				indexScan.setSucceedingOperator(new OperatorIDTuple(
						new Result(), 0));
			}

			// indexScan.setSucceedingOperator(new OperatorIDTuple(new Result(),
			// 0));
			rootNodeOfSubGraph.setSucceedingOperator(new OperatorIDTuple(
					indexScan, 0));

			rootNodeOfSubGraph.setParents();

			// original connections set at new graph
			container.setSucceedingOperators(succs);

			// iterate through the new predecessors of the successors of the
			// original index scan operators and set new SubgraphContainer
			for (final OperatorIDTuple succ : succs) {
				succ.getOperator().removePrecedingOperator(indexScan);
				succ.getOperator().addPrecedingOperator(container);
			}

		} catch (final JSONException e1) {
			System.err.println(e1);
			e1.printStackTrace();
		} catch (final TriplePatternNotSupportedError e1) {
			System.err.println(e1);
			e1.printStackTrace();
		}
	}

	private lupos.engine.operators.index.BasicIndexScan indexScan = null;

	private boolean _checkPrivate0(final BasicOperator _op) {
		if (!(_op instanceof lupos.engine.operators.index.BasicIndexScan)) {
			return false;
		}

		this.indexScan = (lupos.engine.operators.index.BasicIndexScan) _op;

		return true;
	}

	public AddSubGraphContainerRule() {
		this.startOpClass = lupos.engine.operators.index.BasicIndexScan.class;
		this.ruleName = "AddSubGraphContainer";
	}

	@Override
	protected boolean check(final BasicOperator _op) {
		return this._checkPrivate0(_op);
	}

	@Override
	protected void replace(
			final HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
		this.replaceIndexScanOperatorWithSubGraphContainer(this.indexScan);

	}
}
