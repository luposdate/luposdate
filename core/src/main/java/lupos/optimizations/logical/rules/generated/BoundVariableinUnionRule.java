/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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




public class BoundVariableinUnionRule extends Rule {

    private lupos.engine.operators.singleinput.filter.Filter f = null;
    private lupos.engine.operators.BasicOperator o2 = null;
    private lupos.engine.operators.multiinput.Union u = null;
    private lupos.engine.operators.BasicOperator o3 = null;
    private lupos.engine.operators.BasicOperator jump_filter_end = null;
    private lupos.engine.operators.BasicOperator[] o = null;
    private lupos.engine.operators.multiinput.join.Join j = null;
    private lupos.engine.operators.BasicOperator jump_filter_begin = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.multiinput.Union.class) {
            return false;
        }

        this.u = (lupos.engine.operators.multiinput.Union) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();


        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            if(_precOp_1_0.getClass() != lupos.engine.operators.singleinput.filter.Filter.class) {
                continue;
            }

            this.f = (lupos.engine.operators.singleinput.filter.Filter) _precOp_1_0;

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

                while(_searchIndex_2_0 != null && (_searchIndex_2_0.getClass() != lupos.engine.operators.BasicOperator.class)) {
                    if(_searchIndex_2_0.getClass() != lupos.engine.operators.singleinput.filter.Filter.class) {
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

                    if(_precOp_3_0.getClass() != lupos.engine.operators.BasicOperator.class) {
                        continue;
                    }

                    this.o2 = (lupos.engine.operators.BasicOperator) _precOp_3_0;

                    List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();


                    for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
                        if(_sucOpIDTup_1_0.getOperator().getClass() != lupos.engine.operators.multiinput.join.Join.class) {
                            continue;
                        }

                        this.j = (lupos.engine.operators.multiinput.join.Join) _sucOpIDTup_1_0.getOperator();

                        List<BasicOperator> _precedingOperators_2_1 = _sucOpIDTup_1_0.getOperator().getPrecedingOperators();


                        for(BasicOperator _precOp_2_1 : _precedingOperators_2_1) {
                            if(_precOp_2_1.getSucceedingOperators().size() != 1) {
                                break;
                            }

                            if(!(_precOp_2_1 instanceof lupos.engine.operators.BasicOperator)) {
                                continue;
                            }

                            this.o3 = (lupos.engine.operators.BasicOperator) _precOp_2_1;

                            List<OperatorIDTuple> _succedingOperators_2_0 = _sucOpIDTup_1_0.getOperator().getSucceedingOperators();


                            this._dim_0 = -1;
                            this.o = new lupos.engine.operators.BasicOperator[_succedingOperators_2_0.size()];

                            for(OperatorIDTuple _sucOpIDTup_2_0 : _succedingOperators_2_0) {
                                this._dim_0 += 1;

                                if(!this._checkPrivate1(_sucOpIDTup_2_0.getOperator())) {
                                    return false;
                                }
                            }

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean _checkPrivate1(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.o[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }


    public BoundVariableinUnionRule() {
        this.startOpClass = lupos.engine.operators.multiinput.Union.class;
        this.ruleName = "Bound Variable in Union";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            if(this.u.getOperatorIDTuple(this.j).getId()==this.o3.getOperatorIDTuple(this.j).getId()){
            	return false;
            }
            
            lupos.engine.operators.singleinput.filter.Filter filterOutestFilter = this.f;
            java.util.Collection<lupos.datastructures.items.Variable> variablesInnerUnion = this.o2.getIntersectionVariables();
            java.util.Collection<lupos.datastructures.items.Variable> variablesOuterUnion = this.o3.getUnionVariables();
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
            
                if(nextOp.equals(this.o2)) {
                    checkFurther = false;
                }
                else {
                    filterOutestFilter = (lupos.engine.operators.singleinput.filter.Filter) nextOp;
                }
            }
            return false;
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.u.removeSucceedingOperator(this.j);
        this.j.removePrecedingOperator(this.u);
        this.o3.removeSucceedingOperator(this.j);
        this.j.removePrecedingOperator(this.o3);
        for(lupos.engine.operators.BasicOperator _child : this.o) {
            this.j.removeSucceedingOperator(_child);
            _child.removePrecedingOperator(this.j);
        }

        this.o2.removeSucceedingOperator(this.jump_filter_begin);
        this.jump_filter_begin.removePrecedingOperator(this.o2);

        // add new operators...
        lupos.engine.operators.multiinput.join.Join j_new = null;
        j_new = new lupos.engine.operators.multiinput.join.Join();


        // add new connections...
        for(lupos.engine.operators.BasicOperator _child : this.o) {
            this.u.addSucceedingOperator(_child);
            _child.addPrecedingOperator(this.u);
        }


        j_new.addSucceedingOperator(this.jump_filter_begin);
        this.jump_filter_begin.addPrecedingOperator(j_new);

        this.o2.addSucceedingOperator(j_new);
        j_new.addPrecedingOperator(this.o2);

        this.o3.addSucceedingOperator(j_new);
        j_new.addPrecedingOperator(this.o3);


        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.j, _startNodes);


        // additional replace method code...
        j_new.setUnionVariables(new HashSet<lupos.datastructures.items.Variable>());
        j_new.getUnionVariables().addAll(this.o2.getUnionVariables());
        j_new.getUnionVariables().addAll(this.o3.getUnionVariables());
        
        j_new.setIntersectionVariables(new HashSet<lupos.datastructures.items.Variable>());
        j_new.getIntersectionVariables().addAll(this.o2.getUnionVariables());
        j_new.getIntersectionVariables().retainAll(this.o3.getUnionVariables());
        
        this.o2.getOperatorIDTuple(j_new).setId(0);
        this.o3.getOperatorIDTuple(j_new).setId(1);
        
        for(BasicOperator precOp : this.u.getPrecedingOperators()) {
            if(!precOp.equals(this.f)) {
                BasicOperator tmpOp = precOp;
        
                while(tmpOp instanceof lupos.engine.operators.singleinput.filter.Filter) {
                    tmpOp = tmpOp.getPrecedingOperators().get(0);
                }
        
        
                lupos.engine.operators.multiinput.join.Join newJoin = new lupos.engine.operators.multiinput.join.Join();
        
                newJoin.addPrecedingOperator(tmpOp);
                newJoin.addPrecedingOperator(this.o3);
        
                for(OperatorIDTuple opIDt : tmpOp.getSucceedingOperators()) {
                    opIDt.getOperator().removePrecedingOperator(tmpOp);
                    opIDt.getOperator().addPrecedingOperator(newJoin);
                }
        
                newJoin.setSucceedingOperators(tmpOp.getSucceedingOperators());
                tmpOp.setSucceedingOperator(new OperatorIDTuple(newJoin, 0));
                this.o3.addSucceedingOperator(new OperatorIDTuple(newJoin, 1));
        
        
                HashSet<lupos.datastructures.items.Variable> intersectionVariables = new HashSet<lupos.datastructures.items.Variable>();
                intersectionVariables.addAll(tmpOp.getUnionVariables());
                intersectionVariables.retainAll(this.o3.getUnionVariables());
        
                HashSet<lupos.datastructures.items.Variable> unionVariables = new HashSet<lupos.datastructures.items.Variable>();
                unionVariables.addAll(tmpOp.getUnionVariables());
                unionVariables.addAll(this.o3.getUnionVariables());
        
                newJoin.setIntersectionVariables(intersectionVariables);
                newJoin.setUnionVariables(unionVariables);
        
        
                tmpOp = tmpOp.getSucceedingOperators().get(0).getOperator();
        
                while(!tmpOp.equals(this.u)) {
                    tmpOp.setUnionVariables(unionVariables);
                    tmpOp.setIntersectionVariables(intersectionVariables);
        
                    tmpOp = tmpOp.getSucceedingOperators().get(0).getOperator();
                }
            }
        }
    }
}
