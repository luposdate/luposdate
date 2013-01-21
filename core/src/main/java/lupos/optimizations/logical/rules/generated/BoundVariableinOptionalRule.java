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




public class BoundVariableinOptionalRule extends Rule {

    private lupos.engine.operators.singleinput.Filter f = null;
    private lupos.engine.operators.BasicOperator o2 = null;
    private lupos.engine.operators.BasicOperator o1 = null;
    private lupos.engine.operators.BasicOperator jump_filter_end = null;
    private lupos.engine.operators.BasicOperator jump_filter_begin = null;
    private lupos.engine.operators.multiinput.optional.Optional opt = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.multiinput.optional.Optional.class) {
            return false;
        }

        this.opt = (lupos.engine.operators.multiinput.optional.Optional) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();


        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            if(_precOp_1_0.getOperatorIDTuple(_op).getId() != 1) {
                continue;
            }

            if(_precOp_1_0.getClass() != lupos.engine.operators.singleinput.Filter.class) {
                continue;
            }

            this.f = (lupos.engine.operators.singleinput.Filter) _precOp_1_0;

            List<BasicOperator> _precedingOperators_2_0 = _precOp_1_0.getPrecedingOperators();

            if(_precedingOperators_2_0.size() != 1) {
                continue;
            }

            for(BasicOperator _precOp_2_0 : _precedingOperators_2_0) {
                if(_precOp_2_0.getSucceedingOperators().size() != 1) {
                    break;
                }

                // --- handle JumpOver - begin ---
                this.jump_filter_end = (lupos.engine.operators.BasicOperator) _precOp_2_0;
                BasicOperator _searchIndex_2_0 = _precOp_2_0;
                boolean _continueFlag_2_0 = false;

                while(_searchIndex_2_0 != null && (_searchIndex_2_0.getClass() == lupos.engine.operators.singleinput.Filter.class)) {
                    if(_searchIndex_2_0.getClass() != lupos.engine.operators.singleinput.Filter.class) {
                        _continueFlag_2_0 = true;
                        break;
                    }

                    if(_searchIndex_2_0.getSucceedingOperators().size() != 1 || _searchIndex_2_0.getPrecedingOperators().size() != 1) {
                        _continueFlag_2_0 = true;
                        break;
                    }

                    _searchIndex_2_0 = _searchIndex_2_0.getPrecedingOperators().get(0);
                }

                if(_continueFlag_2_0) {
                    continue;
                }

                this.jump_filter_begin = (lupos.engine.operators.BasicOperator) _searchIndex_2_0.getSucceedingOperators().get(0).getOperator();
                // --- handle JumpOver - end ---


                List<BasicOperator> _precedingOperators_3_0 = this.jump_filter_begin.getPrecedingOperators();

                if(_searchIndex_2_0 != this.jump_filter_begin) {
                    if(_precedingOperators_3_0.size() != 1) {
                        continue;
                    }
                }

                for(BasicOperator _precOp_3_0 : _precedingOperators_3_0) {
                    if(_precOp_3_0.getSucceedingOperators().size() != 1) {
                        break;
                    }

                    if(!(_precOp_3_0 instanceof lupos.engine.operators.BasicOperator)) {
                        continue;
                    }

                    this.o1 = (lupos.engine.operators.BasicOperator) _precOp_3_0;

                    for(BasicOperator _precOp_1_1 : _precedingOperators_1_0) {
                        if(_precOp_1_1.getOperatorIDTuple(_op).getId() != 0) {
                            continue;
                        }

                        if(_precOp_1_1.getSucceedingOperators().size() != 1) {
                            break;
                        }

                        if(!(_precOp_1_1 instanceof lupos.engine.operators.BasicOperator)) {
                            continue;
                        }

                        this.o2 = (lupos.engine.operators.BasicOperator) _precOp_1_1;

                        return true;
                    }
                }
            }
        }

        return false;
    }


    public BoundVariableinOptionalRule() {
        this.startOpClass = lupos.engine.operators.multiinput.optional.Optional.class;
        this.ruleName = "Bound Variable in Optional";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            if(this.o1 instanceof lupos.engine.operators.multiinput.join.Join && this.o1.getPrecedingOperators().contains(this.o2)) {
                return false;
            }
            
            lupos.engine.operators.singleinput.Filter filterOutestFilter = this.f;
            java.util.Collection<lupos.datastructures.items.Variable> variablesInnerUnion = this.o1.getIntersectionVariables();
            java.util.Collection<lupos.datastructures.items.Variable> variablesOuterUnion = this.o2.getUnionVariables();
            boolean checkFurther = true;
            
            while(checkFurther) {
                if(!variablesInnerUnion.containsAll(filterOutestFilter.getUsedVariables())) {
                    for(lupos.datastructures.items.Variable v : filterOutestFilter.getUsedVariables()) {
                        if(!variablesInnerUnion.contains(v) && variablesOuterUnion.contains(v)) {
                            return true;
                        }
                    }
                }
            
                BasicOperator nextOp = filterOutestFilter.getPrecedingOperators().get(0);
            
                if(nextOp.equals(this.o1)) {
                    checkFurther = false;
                }
                else {
                    filterOutestFilter = (lupos.engine.operators.singleinput.Filter) nextOp;
                }
            }
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.o2.removeSucceedingOperator(new OperatorIDTuple(this.opt, 0));
        this.opt.removePrecedingOperator(this.o2);
        this.o1.removeSucceedingOperator(this.jump_filter_begin);
        this.jump_filter_begin.removePrecedingOperator(this.o1);

        // add new operators...
        lupos.engine.operators.multiinput.join.Join j = null;
        j = new lupos.engine.operators.multiinput.join.Join();


        // add new connections...
        this.o2.addSucceedingOperator(new OperatorIDTuple(this.opt, 0));
        this.opt.addPrecedingOperator(this.o2);

        this.o2.addSucceedingOperator(j);
        j.addPrecedingOperator(this.o2);

        j.addSucceedingOperator(this.jump_filter_begin);
        this.jump_filter_begin.addPrecedingOperator(j);

        this.o1.addSucceedingOperator(j);
        j.addPrecedingOperator(this.o1);


        // additional replace method code...
        this.o1.getOperatorIDTuple(j).setId(0);
        this.o2.getOperatorIDTuple(j).setId(1);
        
        HashSet<lupos.datastructures.items.Variable> intersectionVariables = new HashSet<lupos.datastructures.items.Variable>();
        intersectionVariables.addAll(this.o1.getUnionVariables());
        intersectionVariables.retainAll(this.o2.getUnionVariables());
        
        HashSet<lupos.datastructures.items.Variable> unionVariables = new HashSet<lupos.datastructures.items.Variable>();
        unionVariables.addAll(this.o1.getUnionVariables());
        unionVariables.addAll(this.o2.getUnionVariables());
        
        BasicOperator tmpOp = j;
        
        while(!tmpOp.equals(this.opt)) {
            tmpOp.setUnionVariables(unionVariables);
            tmpOp.setIntersectionVariables(intersectionVariables);
        
            tmpOp = tmpOp.getSucceedingOperators().get(0).getOperator();
        }
        
        this.opt.setUnionVariables(unionVariables);
        this.opt.setIntersectionVariables(intersectionVariables);
    }
}
