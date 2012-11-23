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




public class ReplaceUsingJoinOptionalWithBasicIndexOptionalRule extends Rule {
    private int operandID;
    private lupos.engine.operators.BasicOperator suc = null;
    private lupos.engine.operators.BasicOperator leftOperand = null;
    private lupos.engine.operators.multiinput.optional.UsingJoinOptional optional = null;
    private lupos.engine.operators.index.BasicIndexScan indexScan = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.multiinput.optional.UsingJoinOptional)) {
            return false;
        }

        this.optional = (lupos.engine.operators.multiinput.optional.UsingJoinOptional) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();


        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            if(_precOp_1_0.getOperatorIDTuple(_op).getId() != 0) {
                continue;
            }

            if(_precOp_1_0.getSucceedingOperators().size() != 1) {
                break;
            }

            if(!(_precOp_1_0 instanceof lupos.engine.operators.BasicOperator)) {
                continue;
            }

            this.leftOperand = (lupos.engine.operators.BasicOperator) _precOp_1_0;

            for(BasicOperator _precOp_1_1 : _precedingOperators_1_0) {
                if(_precOp_1_1.getOperatorIDTuple(_op).getId() != 1) {
                    continue;
                }

                if(_precOp_1_1.getSucceedingOperators().size() != 1) {
                    break;
                }

                if(!(_precOp_1_1 instanceof lupos.engine.operators.index.BasicIndexScan)) {
                    continue;
                }

                this.indexScan = (lupos.engine.operators.index.BasicIndexScan) _precOp_1_1;

                List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();

                if(_succedingOperators_1_0.size() != 1) {
                    return false;
                }

                for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
                    if(!(_sucOpIDTup_1_0.getOperator() instanceof lupos.engine.operators.BasicOperator)) {
                        continue;
                    }

                    this.suc = (lupos.engine.operators.BasicOperator) _sucOpIDTup_1_0.getOperator();

                    return true;
                }
            }
        }

        return false;
    }


    public ReplaceUsingJoinOptionalWithBasicIndexOptionalRule() {
        this.startOpClass = lupos.engine.operators.multiinput.optional.UsingJoinOptional.class;
        this.ruleName = "Replace UsingJoinOptional With BasicIndexOptional";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            // for later repairing operandId
            this.operandID = this.optional.getOperatorIDTuple(this.suc).getId();
            return this.optional.getPrecedingOperators().size()==2;
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.leftOperand.removeSucceedingOperator(new OperatorIDTuple(this.optional, 0));
        this.optional.removePrecedingOperator(this.leftOperand);
        this.indexScan.removeSucceedingOperator(new OperatorIDTuple(this.optional, 1));
        this.optional.removePrecedingOperator(this.indexScan);
        this.optional.removeSucceedingOperator(this.suc);
        this.suc.removePrecedingOperator(this.optional);

        // add new operators...
        lupos.engine.operators.multiinput.optional.BasicIndexScanOptional optional_new = null;
        optional_new = new lupos.engine.operators.multiinput.optional.BasicIndexScanOptional();


        // add new connections...
        optional_new.addSucceedingOperator(this.suc);
        this.suc.addPrecedingOperator(optional_new);

        this.leftOperand.addSucceedingOperator(new OperatorIDTuple(optional_new, 0));
        optional_new.addPrecedingOperator(this.leftOperand);


        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.optional, _startNodes);
        this.deleteOperatorWithoutParentsRecursive(this.indexScan, _startNodes);


        // additional replace method code...
        optional_new.setBasicIndexScan(this.indexScan);
        optional_new.setUnionVariables(this.optional.getUnionVariables());
        optional_new.setIntersectionVariables(this.optional.getIntersectionVariables());
        if(this.operandID!=0){
          // repairing the operand ID
          optional_new.getOperatorIDTuple(this.suc).setId(this.operandID);
        }
        if(this.indexScan.getSucceedingOperators().size()==0){
          this.deleteOperator(this.indexScan, _startNodes);
        }
    }
}
