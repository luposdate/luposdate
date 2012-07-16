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




public class ReplaceHashMapIndexJoinwithHashMapIndexJoinOnLeftOperandRule extends Rule {
    public static boolean isInCycleOrSeveralOperandsWitSameID(BasicOperator bo){
      if(bo.getCycleOperands()!=null && bo.getCycleOperands().size()>0){
        return true;
      }
     HashSet<Integer> operandsIDOccurs = new HashSet<Integer>();
      for(BasicOperator prec: bo.getPrecedingOperators()){
      int operandsID = prec.getOperatorIDTuple(bo).getId();
      if(operandsIDOccurs.contains(operandsID)){
       return true;
      }
      operandsIDOccurs.add(operandsID);
        if(ReplaceHashMapIndexJoinwithHashMapIndexJoinOnLeftOperandRule.isInCycleOrSeveralOperandsWitSameID(prec)){
          return true;
        }
      }
      return false;
    }
    private lupos.engine.operators.BasicOperator[] o_under = null;
    private lupos.engine.operators.BasicOperator[] o = null;
    private lupos.engine.operators.multiinput.join.HashMapIndexJoin j = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.multiinput.join.HashMapIndexJoin.class) {
            return false;
        }

        this.j = (lupos.engine.operators.multiinput.join.HashMapIndexJoin) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();


        this._dim_0 = -1;
        this.o = new lupos.engine.operators.BasicOperator[_precedingOperators_1_0.size()];

        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            this._dim_0 += 1;

            if(!this._checkPrivate1(_precOp_1_0)) {
                return false;
            }
        }

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();


        this._dim_0 = -1;
        this.o_under = new lupos.engine.operators.BasicOperator[_succedingOperators_1_0.size()];

        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            this._dim_0 += 1;

            if(!this._checkPrivate2(_sucOpIDTup_1_0.getOperator())) {
                return false;
            }
        }

        return true;
    }

    private boolean _checkPrivate1(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.o[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }

    private boolean _checkPrivate2(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.o_under[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }


    public ReplaceHashMapIndexJoinwithHashMapIndexJoinOnLeftOperandRule() {
        this.startOpClass = lupos.engine.operators.multiinput.join.HashMapIndexJoin.class;
        this.ruleName = "Replace HashMapIndexJoin with HashMapIndexJoinOnLeftOperand";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            return !(ReplaceHashMapIndexJoinwithHashMapIndexJoinOnLeftOperandRule.isInCycleOrSeveralOperandsWitSameID(this.j));
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        int[] _label_a = null;

        int _label_a_count = 0;
        _label_a = new int[this.o.length];

        for(lupos.engine.operators.BasicOperator _parent : this.o) {
            _label_a[_label_a_count] = _parent.getOperatorIDTuple(this.j).getId();
            _label_a_count += 1;

            _parent.removeSucceedingOperator(this.j);
            this.j.removePrecedingOperator(_parent);
        }

        int[] _label_b = null;

        int _label_b_count = 0;
        _label_b = new int[this.o_under.length];

        for(lupos.engine.operators.BasicOperator _child : this.o_under) {
            _label_b[_label_b_count] = this.j.getOperatorIDTuple(_child).getId();
            _label_b_count += 1;

            this.j.removeSucceedingOperator(_child);
            _child.removePrecedingOperator(this.j);
        }


        // add new operators...
        lupos.engine.operators.multiinput.join.HashMapIndexOnLeftOperandJoin new_j = null;
        new_j = new lupos.engine.operators.multiinput.join.HashMapIndexOnLeftOperandJoin();


        // add new connections...
        _label_b_count = 0;

        for(lupos.engine.operators.BasicOperator _child : this.o_under) {
            new_j.addSucceedingOperator(new OperatorIDTuple(_child, _label_b[_label_b_count]));
            _child.addPrecedingOperator(new_j);

            _label_b_count += 1;
        }


        _label_a_count = 0;

        for(lupos.engine.operators.BasicOperator _parent : this.o) {
            _parent.addSucceedingOperator(new OperatorIDTuple(new_j, _label_a[_label_a_count]));
            new_j.addPrecedingOperator(_parent);

            _label_a_count += 1;
        }



        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.j, _startNodes);


        // additional replace method code...
        new_j.setIntersectionVariables(this.j.getIntersectionVariables());
        new_j.setUnionVariables(this.j.getUnionVariables());
    }
}
