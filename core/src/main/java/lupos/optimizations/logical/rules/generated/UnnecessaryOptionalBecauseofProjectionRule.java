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
package lupos.optimizations.logical.rules.generated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;




public class UnnecessaryOptionalBecauseofProjectionRule extends Rule {

    private lupos.engine.operators.BasicOperator o0 = null;
    private lupos.engine.operators.BasicOperator o1 = null;
    private lupos.engine.operators.singleinput.Projection p = null;
    private lupos.engine.operators.multiinput.optional.Optional opt = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.singleinput.Projection.class) {
            return false;
        }

        this.p = (lupos.engine.operators.singleinput.Projection) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();

        if(_precedingOperators_1_0.size() != 1) {
            return false;
        }

        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            if(_precOp_1_0.getSucceedingOperators().size() != 1) {
                break;
            }

            if(!(_precOp_1_0 instanceof lupos.engine.operators.multiinput.optional.Optional)) {
                continue;
            }

            this.opt = (lupos.engine.operators.multiinput.optional.Optional) _precOp_1_0;

            List<BasicOperator> _precedingOperators_2_0 = _precOp_1_0.getPrecedingOperators();


            for(BasicOperator _precOp_2_0 : _precedingOperators_2_0) {
                if(_precOp_2_0.getOperatorIDTuple(_precOp_1_0).getId() != 0) {
                    continue;
                }

                if(_precOp_2_0.getSucceedingOperators().size() != 1) {
                    break;
                }

                if(!(_precOp_2_0 instanceof lupos.engine.operators.BasicOperator)) {
                    continue;
                }

                this.o0 = (lupos.engine.operators.BasicOperator) _precOp_2_0;

                for(BasicOperator _precOp_2_1 : _precedingOperators_2_0) {
                    if(_precOp_2_1.getOperatorIDTuple(_precOp_1_0).getId() != 1) {
                        continue;
                    }

                    if(_precOp_2_1.getSucceedingOperators().size() != 1) {
                        break;
                    }

                    if(!(_precOp_2_1 instanceof lupos.engine.operators.BasicOperator)) {
                        continue;
                    }

                    this.o1 = (lupos.engine.operators.BasicOperator) _precOp_2_1;

                    return true;
                }
            }
        }

        return false;
    }


    public UnnecessaryOptionalBecauseofProjectionRule() {
        this.startOpClass = lupos.engine.operators.singleinput.Projection.class;
        this.ruleName = "Unnecessary Optional Because of Projection";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            if(this.opt.getPrecedingOperators().size()>2){
            	return false;
            }
            if(this.o0.getUnionVariables().containsAll(this.p.getProjectedVariables())){
            	return true;
            } else {
            	return false;
            }
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.o0.removeSucceedingOperator(new OperatorIDTuple(this.opt, 0));
        this.opt.removePrecedingOperator(this.o0);
        this.opt.removeSucceedingOperator(this.p);
        this.p.removePrecedingOperator(this.opt);
        this.o1.removeSucceedingOperator(new OperatorIDTuple(this.opt, 1));
        this.opt.removePrecedingOperator(this.o1);

        // add new operators...


        // add new connections...
        this.o0.addSucceedingOperator(this.p);
        this.p.addPrecedingOperator(this.o0);


        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.o1, _startNodes);
        this.deleteOperatorWithoutParentsRecursive(this.opt, _startNodes);


        // additional replace method code...
        this.deleteOperatorWithParentsAndChildren(this.o1, _startNodes);
    }
}
