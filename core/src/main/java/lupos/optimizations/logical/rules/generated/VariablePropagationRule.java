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




public class VariablePropagationRule extends Rule {
    private lupos.datastructures.items.Variable var = null;
    private lupos.datastructures.items.Variable otherVar = null;
    private int operandIDOfFilter;
    private lupos.engine.operators.singleinput.Filter f = null;
    private lupos.engine.operators.BasicOperator o = null;
    private lupos.engine.operators.BasicOperator j_begin = null;
    private lupos.engine.operators.BasicOperator j_end = null;
    private lupos.engine.operators.index.BasicIndexScan i = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.index.BasicIndexScan)) {
            return false;
        }

        this.i = (lupos.engine.operators.index.BasicIndexScan) _op;

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();

        if(_succedingOperators_1_0.size() != 1) {
            return false;
        }

        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            if(_sucOpIDTup_1_0.getOperator().getPrecedingOperators().size() != 1) {
                break;
            }

            // --- handle JumpOver - begin ---
            this.j_begin = (lupos.engine.operators.BasicOperator) _sucOpIDTup_1_0.getOperator();
            BasicOperator _searchIndex_1_0 = _sucOpIDTup_1_0.getOperator();
            boolean _continueFlag_1_0 = false;

            while(_searchIndex_1_0 != null && (_searchIndex_1_0.getClass() != lupos.engine.operators.singleinput.Filter.class)) {
                if(_searchIndex_1_0.getClass() != lupos.engine.operators.singleinput.Filter.class) {
                    _continueFlag_1_0 = true;
                    break;
                }

                if(_searchIndex_1_0.getSucceedingOperators().size() != 1 || _searchIndex_1_0.getPrecedingOperators().size() != 1) {
                    _continueFlag_1_0 = true;
                    break;
                }

                _searchIndex_1_0 = _searchIndex_1_0.getSucceedingOperators().get(0).getOperator();
            }

            if(_continueFlag_1_0) {
                continue;
            }

            this.j_end = (lupos.engine.operators.BasicOperator) _searchIndex_1_0.getPrecedingOperators().get(0);
            // --- handle JumpOver - end ---


            List<OperatorIDTuple> _succedingOperators_2_0 = this.j_end.getSucceedingOperators();

            if(_succedingOperators_2_0.size() != 1) {
                continue;
            }

            for(OperatorIDTuple _sucOpIDTup_2_0 : _succedingOperators_2_0) {
                if(_sucOpIDTup_2_0.getOperator().getPrecedingOperators().size() != 1) {
                    break;
                }

                if(_sucOpIDTup_2_0.getOperator().getClass() != lupos.engine.operators.singleinput.Filter.class) {
                    continue;
                }

                this.f = (lupos.engine.operators.singleinput.Filter) _sucOpIDTup_2_0.getOperator();

                List<OperatorIDTuple> _succedingOperators_3_0 = _sucOpIDTup_2_0.getOperator().getSucceedingOperators();


                for(OperatorIDTuple _sucOpIDTup_3_0 : _succedingOperators_3_0) {
                    if(!(_sucOpIDTup_3_0.getOperator() instanceof lupos.engine.operators.BasicOperator)) {
                        continue;
                    }

                    this.o = (lupos.engine.operators.BasicOperator) _sucOpIDTup_3_0.getOperator();

                    return true;
                }
            }
        }

        return false;
    }


    public VariablePropagationRule() {
        this.startOpClass = lupos.engine.operators.index.BasicIndexScan.class;
        this.ruleName = "Variable Propagation";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            lupos.sparql1_1.Node n = this.f.getNodePointer();
            
            if(n.jjtGetNumChildren() > 0) {
                n = n.jjtGetChild(0);
            
                if(n instanceof lupos.sparql1_1.ASTEqualsNode) {
                    lupos.sparql1_1.Node left = n.jjtGetChild(0);
                    lupos.sparql1_1.Node right = n.jjtGetChild(1);
            
                    if(left instanceof lupos.sparql1_1.ASTVar) {
                        this.var = new lupos.datastructures.items.Variable(((lupos.sparql1_1.ASTVar) left).getName());
            
                        if(right instanceof lupos.sparql1_1.ASTVar) {
                            this.otherVar = new lupos.datastructures.items.Variable(((lupos.sparql1_1.ASTVar) right).getName());
            
                            if(this.i.occurInSubjectOrPredicateOrObjectOriginalStringDoesNotDiffer(this.var) || this.i.occurInSubjectOrPredicateOrObjectOriginalStringDoesNotDiffer(this.otherVar)) {
                                this.operandIDOfFilter = this.f.getOperatorIDTuple(o).getId();
                                return true;
                            }
                            else {
                                // in objects, there might occur typed literals e.g.
                                // numbers, which are value equal, but do not have the
                                // same identity, e.g. 01^^xsd:int and 1^^xsd:int.
                                // => var or otherVar must occur in the subject or
                                // predicate of a triple pattern, such that they are
                                // URIs or blank nodes, which have an unique
                                // representation!
                                return false;
                            }
                        }
                        else {
                            return false;
                        }
                    }
                    else {
                        return false;
                    }
                }
                else {
                    return false;
               }
            }
            else {
                return false;
            }
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.f.removeSucceedingOperator(this.o);
        this.o.removePrecedingOperator(this.f);
        this.j_end.removeSucceedingOperator(this.f);
        this.f.removePrecedingOperator(this.j_end);

        // add new operators...
        lupos.engine.operators.singleinput.AddBindingFromOtherVar b = null;
        b = new lupos.engine.operators.singleinput.AddBindingFromOtherVar();


        // add new connections...
        b.addSucceedingOperator(this.o);
        this.o.addPrecedingOperator(b);

        this.j_end.addSucceedingOperator(b);
        b.addPrecedingOperator(this.j_end);


        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.f, _startNodes);


        // additional replace method code...
        this.i.replace(this.var, this.otherVar);
        
        b.setVar(this.var);
        b.setOtherVar(this.otherVar);
        
        BasicOperator tmp = this.i;
        
        while(!tmp.equals(b)) {
            tmp.getUnionVariables().remove(this.var);
            tmp.getIntersectionVariables().remove(this.var);
        
            tmp = tmp.getSucceedingOperators().get(0).getOperator();
        }
        
        b.setUnionVariables(this.f.getUnionVariables());
        b.setIntersectionVariables(this.f.getIntersectionVariables());
        b.getOperatorIDTuple(o).setId(this.operandIDOfFilter);
    }
}
