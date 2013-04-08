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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;

import java.util.Collection;

import lupos.sparql1_1.ParseException;
import lupos.datastructures.items.Variable;
import lupos.distributed.operator.SubgraphContainer;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.singleinput.Filter;
import lupos.engine.operators.singleinput.Result;
import lupos.distributed.storage.distributionstrategy.TriplePatternNotSupportedError;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.operator.ISubgraphExecutor;

import org.json.JSONException;



public class AddSubGraphContainerRule extends Rule {
    
    	public static IDistribution distribution;
    
    	public static ISubgraphExecutor subgraphExecutor;
    
    	private Filter getFilterFromIndexScan(BasicOperator root) {
    		List<OperatorIDTuple> succs = root.getSucceedingOperators();
    		if (succs.size() == 1) {
    			for (OperatorIDTuple succ : succs) {
    				BasicOperator op = succ.getOperator();
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
    	 * @param indexScan
    	 *            the index scan operator
    	 */
    	private void replaceIndexScanOperatorWithSubGraphContainer(
    			BasicIndexScan indexScan) {
    
        		try {
        			final Root rootNodeOfOuterGraph = indexScan.getRoot();
        			final Root rootNodeOfSubGraph = rootNodeOfOuterGraph.newInstance(rootNodeOfOuterGraph.dataset);
    
        			// TODO for several keys: union of different SubgraphContainer!
        			final SubgraphContainer container = new SubgraphContainer(rootNodeOfSubGraph, distribution.getKeysForQuerying(indexScan.getTriplePattern().iterator().next())[0], subgraphExecutor);
        			final HashSet<Variable> variables = new HashSet<Variable>(indexScan.getIntersectionVariables());
    
        			container.setUnionVariables(variables);
        			container.setIntersectionVariables(variables);
    
        			// remember original connections and connect new graph with these connections
        			final Collection<BasicOperator> preds = indexScan.getPrecedingOperators();
        			final List<OperatorIDTuple> succs = indexScan.getSucceedingOperators();
    
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
        						newFilter.setSucceedingOperator(new OperatorIDTuple(new Result(), 0));
        					} catch (final ParseException e) {
        						e.printStackTrace();
        					}
    
        				} else {
        					indexScan.setSucceedingOperator(new OperatorIDTuple(
        							new Result(), 0));
        				}
        			} else {
        				indexScan
        				.setSucceedingOperator(new OperatorIDTuple(new Result(), 0));
        			}
    
        			// indexScan.setSucceedingOperator(new OperatorIDTuple(new Result(),
        			// 0));
        			rootNodeOfSubGraph.setSucceedingOperator(new OperatorIDTuple(indexScan, 0));
    
        			rootNodeOfSubGraph.setParents();
    
        			// original connections set at new graph
        			container.setSucceedingOperators(succs);
    
        			// iterate through the new predecessors of the successors of the original index scan operators and set new SubgraphContainer
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

    private boolean _checkPrivate0(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.index.BasicIndexScan)) {
            return false;
        }

        this.indexScan = (lupos.engine.operators.index.BasicIndexScan) _op;

        return true;
    }


    public AddSubGraphContainerRule() {
        this.startOpClass = lupos.engine.operators.index.BasicIndexScan.class;
        this.ruleName = "AddSubGraphContainer";
    }

    protected boolean check(BasicOperator _op) {
        return this._checkPrivate0(_op);
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        this.replaceIndexScanOperatorWithSubGraphContainer(this.indexScan);
    }
}
