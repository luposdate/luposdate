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
public class CombineUnionsRule extends Rule {

    private lupos.engine.operators.BasicOperator[] o1 = null;
    private lupos.engine.operators.multiinput.Union u2 = null;
    private lupos.engine.operators.multiinput.Union u1 = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.multiinput.Union.class) {
            return false;
        }

        this.u2 = (lupos.engine.operators.multiinput.Union) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();


        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            if(_precOp_1_0.getClass() != lupos.engine.operators.multiinput.Union.class) {
                continue;
            }

            this.u1 = (lupos.engine.operators.multiinput.Union) _precOp_1_0;

            List<BasicOperator> _precedingOperators_2_0 = _precOp_1_0.getPrecedingOperators();


            this._dim_0 = -1;
            this.o1 = new lupos.engine.operators.BasicOperator[_precedingOperators_2_0.size()];

            for(BasicOperator _precOp_2_0 : _precedingOperators_2_0) {
                this._dim_0 += 1;

                if(!this._checkPrivate1(_precOp_2_0)) {
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

        this.o1[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }


    /**
     * <p>Constructor for CombineUnionsRule.</p>
     */
    public CombineUnionsRule() {
        this.startOpClass = lupos.engine.operators.multiinput.Union.class;
        this.ruleName = "Combine Unions";
    }

    /** {@inheritDoc} */
    protected boolean check(BasicOperator _op) {
        return this._checkPrivate0(_op);
    }

    /** {@inheritDoc} */
    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        for(lupos.engine.operators.BasicOperator _parent : this.o1) {
            _parent.removeSucceedingOperator(this.u1);
            this.u1.removePrecedingOperator(_parent);
        }

        this.u1.removeSucceedingOperator(this.u2);
        this.u2.removePrecedingOperator(this.u1);

        // add new operators...


        // add new connections...
        for(lupos.engine.operators.BasicOperator _parent : this.o1) {
            _parent.addSucceedingOperator(this.u2);
            this.u2.addPrecedingOperator(_parent);
        }



        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.u1, _startNodes);


        // additional replace method code...
        int id = 0;
                
        for(BasicOperator precOp : this.u2.getPrecedingOperators()) {
            precOp.getOperatorIDTuple(this.u2).setId(id++);
        }
    }
}
