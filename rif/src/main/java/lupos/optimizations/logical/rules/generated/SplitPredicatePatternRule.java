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
package lupos.optimizations.logical.rules.generated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;




public class SplitPredicatePatternRule extends Rule {

    private lupos.engine.operators.BasicOperator o2 = null;
    private lupos.engine.operators.BasicOperator[] o1 = null;
    private lupos.rif.operator.PredicatePattern p = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.rif.operator.PredicatePattern.class) {
            return false;
        }

        this.p = (lupos.rif.operator.PredicatePattern) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();


        this._dim_0 = -1;
        this.o1 = new lupos.engine.operators.BasicOperator[_precedingOperators_1_0.size()];

        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            this._dim_0 += 1;

            if(!this._checkPrivate1(_precOp_1_0)) {
                return false;
            }
        }

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();

        if(_succedingOperators_1_0.size() != 1) {
            return false;
        }

        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            if(!(_sucOpIDTup_1_0.getOperator() instanceof lupos.engine.operators.BasicOperator)) {
                continue;
            }

            this.o2 = (lupos.engine.operators.BasicOperator) _sucOpIDTup_1_0.getOperator();

            return true;
        }

        return false;
    }

    private boolean _checkPrivate1(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.o1[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }


    public SplitPredicatePatternRule() {
        this.startOpClass = lupos.rif.operator.PredicatePattern.class;
        this.ruleName = "Split PredicatePattern";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            return this.p.getPrecedingOperators().size() > 1;
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.p.removeSucceedingOperator(this.o2);
        this.o2.removePrecedingOperator(this.p);
        int[] _label_a = null;

        int _label_a_count = 0;
        _label_a = new int[this.o1.length];

        for(lupos.engine.operators.BasicOperator _parent : this.o1) {
            _label_a[_label_a_count] = _parent.getOperatorIDTuple(this.p).getId();
            _label_a_count += 1;

            _parent.removeSucceedingOperator(this.p);
            this.p.removePrecedingOperator(_parent);
        }


        // add new operators...
        lupos.rif.operator.PredicatePattern[] p_new = null;
        p_new = new lupos.rif.operator.PredicatePattern[this.o1.length];

        for(this._dim_0 = 0; this._dim_0 < p_new.length; this._dim_0 += 1) {
            p_new[this._dim_0] = new lupos.rif.operator.PredicatePattern();
        }


        // add new connections...
        _label_a_count = 0;

        for(lupos.rif.operator.PredicatePattern _parent : p_new) {
            _parent.addSucceedingOperator(new OperatorIDTuple(this.o2, _label_a[_label_a_count]));
            this.o2.addPrecedingOperator(_parent);

            _label_a_count += 1;
        }


        for(this._dim_0 = 0; this._dim_0 < this.o1.length; this._dim_0 += 1) {
            this.o1[this._dim_0].addSucceedingOperator(p_new[this._dim_0]);
            p_new[this._dim_0].addPrecedingOperator(this.o1[this._dim_0]);
        }


        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.p, _startNodes);


        // additional replace method code...
        for(lupos.rif.operator.PredicatePattern tmp_p : p_new) {
            tmp_p.setUnionVariables(new java.util.HashSet<lupos.datastructures.items.Variable>(this.p.getUnionVariables()));
            tmp_p.setIntersectionVariables(new java.util.HashSet<lupos.datastructures.items.Variable>(this.p.getIntersectionVariables()));
            tmp_p.setPredicateName(this.p.getPredicateName());
            tmp_p.setPatternItems(this.p.getPatternItems());
        }
    }
}
