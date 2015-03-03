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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.singleinput.Result;
import lupos.optimizations.logical.rules.generated.PushFilterRule;
import lupos.optimizations.logical.rules.generated.runtime.Rule;

import org.apache.log4j.Logger;



/**
 * This rule moves a filter into a subgraph, because the {@link lupos.optimizations.logical.rules.generated.PushFilterRule} does not
 * access the subgraph.
 *
 * @author Bjoern
 * @version $Id: $Id
 */
public class MoveFilterToSubgraph extends Rule {

    private lupos.distributed.operator.SubgraphContainer<?> sg1 = null;
    private lupos.engine.operators.singleinput.filter.Filter filter = null;
    private final Logger log = Logger.getLogger(this.getClass());


    private boolean _checkPrivate0(final BasicOperator _op) {
    	/*
    	 * search for an Subgraph Container with the only succeeding operator "Filter"
    	 */
        if(!(_op instanceof lupos.distributed.operator.SubgraphContainer)) {
            return false;
        }

        if (this.sg1 != null && this.sg1.equals(_op)) {
			return false;
		}
        this.sg1 = (lupos.distributed.operator.SubgraphContainer<?>) _op;

        final List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();
        if(_succedingOperators_1_0.size() != 1) {
            return false;
        }

        for(final OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            if(_sucOpIDTup_1_0.getOperator().getClass() != lupos.engine.operators.singleinput.filter.Filter.class) {
                continue;
            }
            this.filter = (lupos.engine.operators.singleinput.filter.Filter) _sucOpIDTup_1_0.getOperator();
            return true;
        }
        return false;
    }

    /**
     * New instance for this rule
     */
    public MoveFilterToSubgraph() {
        this.startOpClass = lupos.distributed.operator.SubgraphContainer.class;
        this.ruleName = "MoveFilterToSubgraph";
    }

    /** {@inheritDoc} */
    @Override
    protected boolean check(final BasicOperator _op) {
        return this._checkPrivate0(_op);
    }

 	/** {@inheritDoc} */
 	@Override
    protected void replace(final HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
    	/*
    	 * search for a result in subgraph container, if not found, cancel the task!
    	 */
    	 final Result r = this.getResult(this.sg1.getRootOfSubgraph());
         if (r == null) {
			return;
		}

    	// remove obsolete connections...

         this.log.debug(String.format("Rule %s: Move %s intto %s",this.ruleName,this.filter,this.sg1));

        // add new operators...

        final List<OperatorIDTuple> succs = this.filter.getSucceedingOperators();
        List<BasicOperator> preds = this.filter.getPrecedingOperators();

        for (final OperatorIDTuple op : succs) {
        	op.getOperator().removePrecedingOperator(this.filter);
        	op.getOperator().addPrecedingOperator(this.sg1);
        	this.sg1.addSucceedingOperator(op.getOperator());
        }
        for (final OperatorIDTuple op : succs) {
        	this.filter.removeSucceedingOperator(op);
        }
        for (final BasicOperator op : preds) {
        	this.filter.removePrecedingOperator(op);
        	op.removeSucceedingOperator(this.filter);
        }

        // now move to subgraph

        preds = new LinkedList<BasicOperator>(r.getPrecedingOperators());
        for (final BasicOperator bo : preds) {
        	//remove ? -> Result
        	bo.removeSucceedingOperator(r);
        	r.removePrecedingOperator(bo);
        	//add ? -> Filter
        	bo.addSucceedingOperator(this.filter);
        }

        //add Filter -> Result
        this.filter.addSucceedingOperator(r);
        r.addPrecedingOperator(this.filter);


        // add new connections...

        // delete unreachable operators...
        //this.deleteOperatorWithoutParentsRecursive(this.filter, _startNodes);
        this.sg1.removeSucceedingOperator(this.filter);
        this.filter.removePrecedingOperator(this.sg1);

        // additional replace method code...

    }

	/*
	 * Search for a Result-Operator in succeeding's list of root
	 */
	@SuppressWarnings("serial")
	private Result getResult(final BasicOperator root) {
		//better way:
		final SimpleOperatorGraphVisitor sov = new SimpleOperatorGraphVisitor() {
			@Override
			public Object visit(final BasicOperator basicOperator) {
				if (basicOperator instanceof Result) {
					return basicOperator;
				}
				return null;
			}
		};
		return (Result) root.visit(sov);

//		final List<OperatorIDTuple> succs = root.getSucceedingOperators();
//		if (succs == null | succs.size() == 0)
//			return null;
//		for (final OperatorIDTuple succ : succs) {
//			final BasicOperator op = succ.getOperator();
//			if (op instanceof Result) {
//				return (Result) op;
//			} else {
//				Result res = null;
//				if ((res = getResult(op)) != null)
//					return res;
//			}
//		}
//		return null;
	}
}
