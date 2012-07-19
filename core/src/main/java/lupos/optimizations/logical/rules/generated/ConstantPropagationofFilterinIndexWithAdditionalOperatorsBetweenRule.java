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




public class ConstantPropagationofFilterinIndexWithAdditionalOperatorsBetweenRule extends Rule {
    private lupos.datastructures.items.Variable var = null;
    private lupos.datastructures.items.literal.Literal constant = null;
    private int operandIDOfFilter;
    private lupos.engine.operators.singleinput.Filter f = null;
    private lupos.engine.operators.BasicOperator o = null;
    private lupos.engine.operators.BasicOperator j_begin = null;
    private lupos.engine.operators.BasicOperator j_end = null;
    private lupos.engine.operators.index.BasicIndex i = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.singleinput.Filter.class) {
            return false;
        }

        this.f = (lupos.engine.operators.singleinput.Filter) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();

        if(_precedingOperators_1_0.size() != 1) {
            return false;
        }

        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            if(_precOp_1_0.getSucceedingOperators().size() != 1) {
                break;
            }

            // --- handle JumpOver - begin ---
            this.j_end = (lupos.engine.operators.BasicOperator) _precOp_1_0;
            BasicOperator _searchIndex_1_0 = _precOp_1_0;
            boolean _continueFlag_1_0 = false;

            while(_searchIndex_1_0 != null && (!(_searchIndex_1_0 instanceof lupos.engine.operators.index.BasicIndex))) {
                if(!(_searchIndex_1_0 instanceof lupos.engine.operators.BasicOperator)) {
                    _continueFlag_1_0 = true;
                    break;
                }

                if(_searchIndex_1_0.getSucceedingOperators().size() != 1 || _searchIndex_1_0.getPrecedingOperators().size() != 1) {
                    _continueFlag_1_0 = true;
                    break;
                }

                _searchIndex_1_0 = _searchIndex_1_0.getPrecedingOperators().get(0);
            }

            if(_continueFlag_1_0) {
                continue;
            }

            this.j_begin = (lupos.engine.operators.BasicOperator) _searchIndex_1_0.getSucceedingOperators().get(0).getOperator();
            // --- handle JumpOver - end ---


            List<BasicOperator> _precedingOperators_2_0 = this.j_begin.getPrecedingOperators();

            if(_searchIndex_1_0 != this.j_begin) {
                if(_precedingOperators_2_0.size() != 1) {
                    continue;
                }
            }

            for(BasicOperator _precOp_2_0 : _precedingOperators_2_0) {
                if(_precOp_2_0.getSucceedingOperators().size() != 1) {
                    break;
                }

                if(!(_precOp_2_0 instanceof lupos.engine.operators.index.BasicIndex)) {
                    continue;
                }

                this.i = (lupos.engine.operators.index.BasicIndex) _precOp_2_0;

                List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();

                if(_succedingOperators_1_0.size() != 1) {
                    return false;
                }

                for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
                    if(!(_sucOpIDTup_1_0.getOperator() instanceof lupos.engine.operators.BasicOperator)) {
                        continue;
                    }

                    this.o = (lupos.engine.operators.BasicOperator) _sucOpIDTup_1_0.getOperator();

                    return true;
                }
            }
        }

        return false;
    }


    public ConstantPropagationofFilterinIndexWithAdditionalOperatorsBetweenRule() {
        this.startOpClass = lupos.engine.operators.singleinput.Filter.class;
        this.ruleName = "Constant Propagation of Filter in Index With Additional Operators Between";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            this.operandIDOfFilter = this.f.getOperatorIDTuple(o).getId();
            
            lupos.sparql1_1.Node n = this.f.getNodePointer();
            
            if(n.jjtGetNumChildren() > 0) {
                n = n.jjtGetChild(0);
            
                if(n instanceof lupos.sparql1_1.ASTEqualsNode) {
                    lupos.sparql1_1.Node left = n.jjtGetChild(0);
                    lupos.sparql1_1.Node right = n.jjtGetChild(1);
            
                    if(right instanceof lupos.sparql1_1.ASTVar) {
                        lupos.sparql1_1.Node tmp = left;
                        left = right;
                        right = tmp;
                    }
            
                    if(left instanceof lupos.sparql1_1.ASTVar) {
                        this.var = new lupos.datastructures.items.Variable(((lupos.sparql1_1.ASTVar) left).getName());
            
                        if(right instanceof lupos.sparql1_1.ASTQName
                           || right instanceof lupos.sparql1_1.ASTQuotedURIRef
                           || right instanceof lupos.sparql1_1.ASTFloatingPoint
                           || right instanceof lupos.sparql1_1.ASTInteger
                           || right instanceof lupos.sparql1_1.ASTStringLiteral
                           || right instanceof lupos.sparql1_1.ASTDoubleCircumflex
                   || right instanceof lupos.sparql1_1.ASTRDFLiteral) {
                            this.constant = lupos.datastructures.items.literal.LazyLiteral.getLiteral(right);
            
                            // Is it possible to loose the information of the original string representation?
                            if(this.constant instanceof lupos.datastructures.items.literal.TypedLiteralOriginalContent || this.constant instanceof lupos.datastructures.items.literal.LanguageTaggedLiteralOriginalLanguage) {
                                return false;
                            }
                            else if(this.constant instanceof lupos.datastructures.items.literal.TypedLiteral) {
                                if(lupos.engine.operators.singleinput.ExpressionEvaluation.Helper.isNumeric(((lupos.datastructures.items.literal.TypedLiteral) this.constant).getType())) {
                                    return false;
                                } else {
                                    return true;
                                }
                            } else {
                                return true;
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
        this.i.removeSucceedingOperator(this.j_begin);
        this.j_begin.removePrecedingOperator(this.i);
        this.f.removeSucceedingOperator(this.o);
        this.o.removePrecedingOperator(this.f);
        this.j_end.removeSucceedingOperator(this.f);
        this.f.removePrecedingOperator(this.j_end);

        // add new operators...
        lupos.engine.operators.singleinput.AddBinding b = null;
        b = new lupos.engine.operators.singleinput.AddBinding();


        // add new connections...
        this.j_end.addSucceedingOperator(this.o);
        this.o.addPrecedingOperator(this.j_end);

        this.i.addSucceedingOperator(b);
        b.addPrecedingOperator(this.i);

        b.addSucceedingOperator(this.j_begin);
        this.j_begin.addPrecedingOperator(b);


        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.f, _startNodes);


        // additional replace method code...
        this.i.replace(this.var, this.constant);
        this.i.getUnionVariables().remove(this.var);
        this.i.getIntersectionVariables().remove(this.var);
        
        b.setVar(this.var);
        b.setLiteral(this.constant);
        
        java.util.LinkedList<lupos.datastructures.items.Variable> unionVars=new java.util.LinkedList<lupos.datastructures.items.Variable>(this.i.getUnionVariables());
        unionVars.add(this.var);
        java.util.LinkedList<lupos.datastructures.items.Variable> intersectionVars=new java.util.LinkedList<lupos.datastructures.items.Variable>(unionVars);
        
        b.setUnionVariables(unionVars);
        b.setIntersectionVariables(intersectionVars);
        this.j_end.getOperatorIDTuple(o).setId(this.operandIDOfFilter);
    }
}
