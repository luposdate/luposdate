/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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




public class ProjectionOverSortRule extends Rule {

    private lupos.engine.operators.singleinput.sort.Sort sort = null;
    private lupos.engine.operators.singleinput.Projection projection = null;
    private lupos.engine.operators.BasicOperator[] below = null;
    private lupos.engine.operators.BasicOperator[] above = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.singleinput.sort.Sort.class) {
            return false;
        }

        this.sort = (lupos.engine.operators.singleinput.sort.Sort) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();


        this._dim_0 = -1;
        this.above = new lupos.engine.operators.BasicOperator[_precedingOperators_1_0.size()];

        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            this._dim_0 += 1;

            if(!this._checkPrivate1(_precOp_1_0)) {
                return false;
            }
        }

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();


        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            if(_sucOpIDTup_1_0.getOperator().getClass() != lupos.engine.operators.singleinput.Projection.class) {
                continue;
            }

            this.projection = (lupos.engine.operators.singleinput.Projection) _sucOpIDTup_1_0.getOperator();

            List<OperatorIDTuple> _succedingOperators_2_0 = _sucOpIDTup_1_0.getOperator().getSucceedingOperators();


            this._dim_0 = -1;
            this.below = new lupos.engine.operators.BasicOperator[_succedingOperators_2_0.size()];

            for(OperatorIDTuple _sucOpIDTup_2_0 : _succedingOperators_2_0) {
                this._dim_0 += 1;

                if(!this._checkPrivate2(_sucOpIDTup_2_0.getOperator())) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private boolean _checkPrivate1(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.above[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }

    private boolean _checkPrivate2(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.below[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }


    public ProjectionOverSortRule() {
        this.startOpClass = lupos.engine.operators.singleinput.sort.Sort.class;
        this.ruleName = "Projection Over Sort";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            final java.util.Collection<lupos.datastructures.items.Variable> sortCrit = this.sort.getSortCriterium();
            if (sortCrit != null && this.projection.getProjectedVariables().containsAll(sortCrit))
            	return true;
            else return false;
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.sort.removeSucceedingOperator(this.projection);
        this.projection.removePrecedingOperator(this.sort);
        int[] _label_b = null;

        int _label_b_count = 0;
        _label_b = new int[this.below.length];

        for(lupos.engine.operators.BasicOperator _child : this.below) {
            _label_b[_label_b_count] = this.projection.getOperatorIDTuple(_child).getId();
            _label_b_count += 1;

            this.projection.removeSucceedingOperator(_child);
            _child.removePrecedingOperator(this.projection);
        }

        int[] _label_a = null;

        int _label_a_count = 0;
        _label_a = new int[this.above.length];

        for(lupos.engine.operators.BasicOperator _parent : this.above) {
            _label_a[_label_a_count] = _parent.getOperatorIDTuple(this.sort).getId();
            _label_a_count += 1;

            _parent.removeSucceedingOperator(this.sort);
            this.sort.removePrecedingOperator(_parent);
        }


        // add new operators...


        // add new connections...
        _label_a_count = 0;

        for(lupos.engine.operators.BasicOperator _parent : this.above) {
            _parent.addSucceedingOperator(new OperatorIDTuple(this.projection, _label_a[_label_a_count]));
            this.projection.addPrecedingOperator(_parent);

            _label_a_count += 1;
        }


        _label_b_count = 0;

        for(lupos.engine.operators.BasicOperator _child : this.below) {
            this.sort.addSucceedingOperator(new OperatorIDTuple(_child, _label_b[_label_b_count]));
            _child.addPrecedingOperator(this.sort);

            _label_b_count += 1;
        }


        this.projection.addSucceedingOperator(this.sort);
        this.sort.addPrecedingOperator(this.projection);


        // additional replace method code...
        final java.util.Collection<lupos.datastructures.items.Variable> cv = new java.util.LinkedList<lupos.datastructures.items.Variable>();
        cv.addAll(this.projection.getProjectedVariables());
        this.sort.setIntersectionVariables(cv);
        this.sort.setUnionVariables(cv);
    }
}
