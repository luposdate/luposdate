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
    private lupos.engine.operators.singleinput.Filter f = null;
    private lupos.engine.operators.tripleoperator.TriplePattern tp = null;
    private lupos.engine.operators.BasicOperator o = null;
    private lupos.engine.operators.BasicOperator j_begin = null;
    private lupos.engine.operators.BasicOperator j_end = null;

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

            while(_searchIndex_1_0 != null && (!(_searchIndex_1_0 instanceof lupos.engine.operators.tripleoperator.TriplePattern))) {
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

                if(!(_precOp_2_0 instanceof lupos.engine.operators.tripleoperator.TriplePattern)) {
                    continue;
                }

                this.tp = (lupos.engine.operators.tripleoperator.TriplePattern) _precOp_2_0;

                List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();


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


    public ConstantPropagationofFilterinTriplePatternRule() {
        this.startOpClass = lupos.engine.operators.singleinput.Filter.class;
        this.ruleName = "Constant Propagation of Filter in Triple Pattern";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            if(this.tp.getSucceedingOperators().size() != 1 || this.f.getPrecedingOperators().size() != 1) {
                return false;
            }
            
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
                                if(lupos.engine.operators.singleinput.ExpressionEvaluation.Helper.isNumeric(((lupos.datastructures.items.literal.TypedLiteral) constant).getType())) {
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

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.f.removeSucceedingOperator(this.o);
        this.o.removePrecedingOperator(this.f);
        this.j_end.removeSucceedingOperator(this.f);
        this.f.removePrecedingOperator(this.j_end);

        // add new operators...
        lupos.engine.operators.singleinput.AddBinding b = null;
        b = new lupos.engine.operators.singleinput.AddBinding();


        // add new connections...
        b.addSucceedingOperator(this.o);
        this.o.addPrecedingOperator(b);

        this.j_end.addSucceedingOperator(b);
        b.addPrecedingOperator(this.j_end);


        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.f, _startNodes);


        // additional replace method code...
        java.util.Set<lupos.datastructures.items.Variable> replacedVars = this.tp.replace(this.var, this.constant);
        
        b.setVar(this.var);
        b.setLiteral(this.constant);
        
        BasicOperator tmp = this.tp;
        
        while(!tmp.equals(b)) {
            tmp.getUnionVariables().removeAll(replacedVars);
            tmp.getIntersectionVariables().removeAll(replacedVars);
        
            tmp = tmp.getSucceedingOperators().get(0).getOperator();
        }
        
        b.setUnionVariables(this.f.getUnionVariables());
        b.setIntersectionVariables(this.f.getIntersectionVariables());
    }
}
