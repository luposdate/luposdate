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
public class ConstantPropagationofFilterinTriplePatternRule extends Rule {
    private lupos.datastructures.items.Variable var = null;
    private lupos.datastructures.items.literal.Literal constant = null;
    private lupos.engine.operators.singleinput.filter.Filter f = null;
    private lupos.engine.operators.tripleoperator.TriplePattern tp = null;
    private lupos.engine.operators.BasicOperator[] o = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.singleinput.filter.Filter.class) {
            return false;
        }

        this.f = (lupos.engine.operators.singleinput.filter.Filter) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();

        if(_precedingOperators_1_0.size() != 1) {
            return false;
        }

        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            if(_precOp_1_0.getSucceedingOperators().size() != 1) {
                break;
            }

            if(_precOp_1_0.getClass() != lupos.engine.operators.tripleoperator.TriplePattern.class) {
                continue;
            }

            this.tp = (lupos.engine.operators.tripleoperator.TriplePattern) _precOp_1_0;

            List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();


            this._dim_0 = -1;
            this.o = new lupos.engine.operators.BasicOperator[_succedingOperators_1_0.size()];

            for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
                this._dim_0 += 1;

                if(!this._checkPrivate1(_sucOpIDTup_1_0.getOperator())) {
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

        this.o[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }


    /**
     * <p>Constructor for ConstantPropagationofFilterinTriplePatternRule.</p>
     */
    public ConstantPropagationofFilterinTriplePatternRule() {
        this.startOpClass = lupos.engine.operators.singleinput.filter.Filter.class;
        this.ruleName = "Constant Propagation of Filter in Triple Pattern";
    }

    /** {@inheritDoc} */
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
            
                    if(right instanceof lupos.sparql1_1.ASTVar) {
                        lupos.sparql1_1.Node tmp = left;
                        left = right;
                        right = tmp;
                    }
            
                    if(left instanceof lupos.sparql1_1.ASTVar) {
                        String varname = ((lupos.sparql1_1.ASTVar) left).getName();
                        this.var = new lupos.datastructures.items.Variable(varname);
            
                        if(!this.tp.getVariables().contains(this.var)){
                          return false;
                        }
            
                        if(!this.tp.getVariables().contains(this.var) && !this.tp.getVariables().contains(new lupos.datastructures.items.VariableInInferenceRule(varname))) {
                            // TODO: delete triple pattern as it will never have a result!
                            System.err.println("Can be optimized by extending RuleReplaceConstantOfFilterInTriplePattern: delete triple pattern with succeeding unsatisfiable filter expression!");
            
                            return false;
                        }
            
                        if(right instanceof lupos.sparql1_1.ASTQName
                           || right instanceof lupos.sparql1_1.ASTRDFLiteral
                           || right instanceof lupos.sparql1_1.ASTQuotedURIRef
                           || right instanceof lupos.sparql1_1.ASTFloatingPoint
                           || right instanceof lupos.sparql1_1.ASTInteger
                           || right instanceof lupos.sparql1_1.ASTStringLiteral
                           || right instanceof lupos.sparql1_1.ASTDoubleCircumflex) {
                            this.constant = lupos.datastructures.items.literal.LazyLiteral.getLiteral(right);
            
                            // Is it possible to loose the information of the original string representation?
                            if(this.constant instanceof lupos.datastructures.items.literal.TypedLiteralOriginalContent || constant instanceof lupos.datastructures.items.literal.LanguageTaggedLiteralOriginalLanguage) {
                                	return false;
                            }
                            else if(this.constant instanceof lupos.datastructures.items.literal.TypedLiteral) {
                                if(lupos.engine.operators.singleinput.filter.expressionevaluation.Helper.isNumeric(((lupos.datastructures.items.literal.TypedLiteral) constant).getType())) {
                                    return false;
                                }
                                else {
                                    return true;
                                }
                            }
                            else {
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

    /** {@inheritDoc} */
    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        int[] _label_a = null;

        int _label_a_count = 0;
        _label_a = new int[this.o.length];

        for(lupos.engine.operators.BasicOperator _child : this.o) {
            _label_a[_label_a_count] = this.f.getOperatorIDTuple(_child).getId();
            _label_a_count += 1;

            this.f.removeSucceedingOperator(_child);
            _child.removePrecedingOperator(this.f);
        }

        this.tp.removeSucceedingOperator(this.f);
        this.f.removePrecedingOperator(this.tp);

        // add new operators...
        lupos.engine.operators.singleinput.AddBinding b = null;
        b = new lupos.engine.operators.singleinput.AddBinding();


        // add new connections...
        _label_a_count = 0;

        for(lupos.engine.operators.BasicOperator _child : this.o) {
            b.addSucceedingOperator(new OperatorIDTuple(_child, _label_a[_label_a_count]));
            _child.addPrecedingOperator(b);

            _label_a_count += 1;
        }


        this.tp.addSucceedingOperator(b);
        b.addPrecedingOperator(this.tp);


        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.f, _startNodes);


        // additional replace method code...
        this.constant = this.constant.createThisLiteralNew();
        java.util.Set<lupos.datastructures.items.Variable> replacedVars = this.tp.replace(this.var, this.constant);
        this.tp.getIntersectionVariables().removeAll(replacedVars);
        this.tp.getUnionVariables().removeAll(replacedVars);
        
        b.setVar(this.var);
        b.setLiteral(this.constant);
        
        java.util.LinkedList<lupos.datastructures.items.Variable> unionVars=new java.util.LinkedList<lupos.datastructures.items.Variable>(this.tp.getUnionVariables());
        unionVars.add(this.var);
        java.util.LinkedList<lupos.datastructures.items.Variable> intersectionVars=new java.util.LinkedList<lupos.datastructures.items.Variable>(unionVars);
        
        b.setUnionVariables(unionVars);
        b.setIntersectionVariables(intersectionVars);
    }
}
