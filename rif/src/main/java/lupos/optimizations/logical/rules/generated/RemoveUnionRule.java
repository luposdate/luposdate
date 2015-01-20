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




public class RemoveUnionRule extends Rule {

    private lupos.engine.operators.BasicOperator[] o2 = null;
    private lupos.engine.operators.multiinput.Union u = null;
    private lupos.engine.operators.BasicOperator[] o1 = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.multiinput.Union.class) {
            return false;
        }

        this.u = (lupos.engine.operators.multiinput.Union) _op;

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


        this._dim_0 = -1;
        this.o2 = new lupos.engine.operators.BasicOperator[_succedingOperators_1_0.size()];

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

        this.o1[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }

    private boolean _checkPrivate2(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.o2[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }


    public RemoveUnionRule() {
        this.startOpClass = lupos.engine.operators.multiinput.Union.class;
        this.ruleName = "Remove Union";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            return !(this.u instanceof lupos.engine.operators.multiinput.mergeunion.MergeUnion);
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        for(BasicOperator over : this.u.getPrecedingOperators()) {
        	for(OperatorIDTuple opID: this.u.getSucceedingOperators()){
        		over.addSucceedingOperator(new OperatorIDTuple(opID));
            	opID.getOperator().addPrecedingOperator(over);
        	}
        }
        
        this.u.removeFromOperatorGraphWithoutConnectingPrecedingWithSucceedingOperators();
        this.deleteNodeFromStartNodeMapNullCheck(this.u, _startNodes);
    }
}
