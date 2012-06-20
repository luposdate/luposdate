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

import lupos.engine.operators.singleinput.modifiers.distinct.HashSetNonBlockingDistinctWithIndexAccess;


public class HashSetNonBlockingDistinctWithIndexAccessRule extends Rule {

    private lupos.engine.operators.singleinput.modifiers.distinct.Distinct d = null;
    private lupos.engine.operators.index.BasicIndex i = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.index.BasicIndex)) {
            return false;
        }

        this.i = (lupos.engine.operators.index.BasicIndex) _op;

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();


        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            if(!(_sucOpIDTup_1_0.getOperator() instanceof lupos.engine.operators.singleinput.modifiers.distinct.Distinct)) {
                continue;
            }

            this.d = (lupos.engine.operators.singleinput.modifiers.distinct.Distinct) _sucOpIDTup_1_0.getOperator();

            return true;
        }

        return false;
    }


    public HashSetNonBlockingDistinctWithIndexAccessRule() {
        this.startOpClass = lupos.engine.operators.index.BasicIndex.class;
        this.ruleName = "HashSetNonBlockingDistinctWithIndexAccess";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            // check if there is only one index scan operator
            if(this.d instanceof HashSetNonBlockingDistinctWithIndexAccess){
            	return false;
            }
            for(BasicOperator prec: this.d.getPrecedingOperators()){
            	BasicOperator prec_tmp=prec;
            	while(!(prec_tmp instanceof lupos.engine.operators.index.BasicIndex) && prec_tmp.getPrecedingOperators().size()==1){
            		prec_tmp = prec_tmp.getPrecedingOperators().get(0);
            	}            	
            	if(prec_tmp instanceof lupos.engine.operators.index.BasicIndex){
            		if(!prec_tmp.equals(this.i)){
            			return false;
            		}
            	}
            }
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        HashSetNonBlockingDistinctWithIndexAccess distinct_new = new HashSetNonBlockingDistinctWithIndexAccess(this.i);
        this.d.replaceWith(distinct_new);
        this.deleteNodeFromStartNodeMapNullCheck(this.d, _startNodes);
        this.addNodeToStartNodeMapNullCheck(distinct_new, _startNodes);
        distinct_new.setPrecedingOperators(this.d.getPrecedingOperators());
        distinct_new.setSucceedingOperators(this.d.getSucceedingOperators());
        // now correct operandID of BasicIndex operand to 0 and the other to 1 (requirement of the HashSetNonBlockingDistinctWithIndexAccess-operator!)
        for(BasicOperator bo: distinct_new.getPrecedingOperators()){
        	BasicOperator prec_tmp=bo;
        	while(!(prec_tmp instanceof lupos.engine.operators.index.BasicIndex) && prec_tmp.getPrecedingOperators().size()==1){
        		prec_tmp = prec_tmp.getPrecedingOperators().get(0);
        	}            	        	        	
        	int operandID = (prec_tmp instanceof lupos.engine.operators.index.BasicIndex)? 0: 1;
        	bo.getOperatorIDTuple(distinct_new).setId(operandID);
        }
    }
}
