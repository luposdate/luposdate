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

import lupos.datastructures.items.Variable;


public class FactoroutORinFilterRule extends Rule {
    int operandID;
    private lupos.engine.operators.singleinput.Filter f = null;
    private lupos.engine.operators.BasicOperator op = null;
    private lupos.engine.operators.BasicOperator o = null;
    private lupos.engine.operators.BasicOperator j_begin = null;
    private lupos.engine.operators.BasicOperator j_end = null;
    private lupos.engine.operators.index.BasicIndexScan i = null;

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

            while(_searchIndex_1_0 != null && (!(_searchIndex_1_0 instanceof lupos.engine.operators.index.BasicIndexScan))) {
                if(_searchIndex_1_0.getClass() != lupos.engine.operators.singleinput.Filter.class) {
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

                if(!(_precOp_2_0 instanceof lupos.engine.operators.index.BasicIndexScan)) {
                    continue;
                }

                this.i = (lupos.engine.operators.index.BasicIndexScan) _precOp_2_0;

                List<BasicOperator> _precedingOperators_3_0 = _precOp_2_0.getPrecedingOperators();


                for(BasicOperator _precOp_3_0 : _precedingOperators_3_0) {
                    if(!(_precOp_3_0 instanceof lupos.engine.operators.BasicOperator)) {
                        continue;
                    }

                    this.op = (lupos.engine.operators.BasicOperator) _precOp_3_0;

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
        }

        return false;
    }


    public FactoroutORinFilterRule() {
        this.startOpClass = lupos.engine.operators.singleinput.Filter.class;
        this.ruleName = "Factor out OR in Filter";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            lupos.sparql1_1.Node n = this.f.getNodePointer();
            
            this.operandID = this.f.getOperatorIDTuple(this.o).getId();
            
            if(this.i.getSucceedingOperators().size()>1)
               return false;
                        
            if(n.jjtGetNumChildren() > 0) {
                n = n.jjtGetChild(0);
            
                return (n instanceof lupos.sparql1_1.ASTOrNode);
            }
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.f.removeSucceedingOperator(this.o);
        this.o.removePrecedingOperator(this.f);

        // add new operators...
        lupos.engine.operators.multiinput.Union u = null;
        u = new lupos.engine.operators.multiinput.Union();
        lupos.engine.operators.singleinput.Filter f2 = null;
        f2 = new lupos.engine.operators.singleinput.Filter();


        // add new connections...
        u.addSucceedingOperator(this.o);
        this.o.addPrecedingOperator(u);

        this.f.addSucceedingOperator(u);
        u.addPrecedingOperator(this.f);

        f2.addSucceedingOperator(u);
        u.addPrecedingOperator(f2);


        // additional replace method code...
        // --- update index - begin ---
        lupos.engine.operators.index.BasicIndexScan i2 = (lupos.engine.operators.index.BasicIndexScan) this.i.clone();
        this.addNodeToStartNodeMapNullCheck(i2, _startNodes);
        
        java.util.LinkedList<lupos.engine.operators.tripleoperator.TriplePattern> lltp = new java.util.LinkedList<lupos.engine.operators.tripleoperator.TriplePattern>();
        
        for(lupos.engine.operators.tripleoperator.TriplePattern tp : this.i.getTriplePattern()) {
            lltp.add(tp.clone());
        }
        
        i2.setTriplePatterns(lltp);        
        
        this.op.addSucceedingOperator(i2);
        i2.setPrecedingOperator(this.op);
        // --- update index - end ---
        
        
        // --- update filter - begin ---
        lupos.sparql1_1.Node n = this.f.getNodePointer();
        
        n = n.jjtGetChild(0);
        lupos.sparql1_1.ASTFilterConstraint node1 = new lupos.sparql1_1.ASTFilterConstraint(0);
        lupos.sparql1_1.ASTFilterConstraint node2 = new lupos.sparql1_1.ASTFilterConstraint(1);
        node1.jjtAddChild(n.jjtGetChild(0), 0);
        node2.jjtAddChild(n.jjtGetChild(1), 0);
        n.jjtGetChild(0).jjtSetParent(node1);
        n.jjtGetChild(1).jjtSetParent(node2);
        
        this.f.setNodePointer(node1);
        f2.setNodePointer(node2);
        
        f2.setCollectionForExistNodes(this.f.getCollectionForExistNodes());
        f2.setEvaluator(this.f.getUsedEvaluationVisitor().getEvaluator());
        f2.getUsedEvaluationVisitor().setEvaluator(this.f.getUsedEvaluationVisitor().getEvaluator());
        
        
        f2.setIntersectionVariables(this.f.getIntersectionVariables());
        f2.setUnionVariables(this.f.getUnionVariables());
        // --- update filter - end ---
        
        
        // --- clone jump-over operators - begin ---
        BasicOperator tmpOp = this.i.getSucceedingOperators().get(0).getOperator();
        BasicOperator parentOp = i2;
        
        while(!tmpOp.equals(this.f)) {
            BasicOperator newOp = tmpOp.clone();
            this.addNodeToStartNodeMapNullCheck(newOp, _startNodes);
        
            parentOp.setSucceedingOperator(new lupos.engine.operators.OperatorIDTuple(newOp, 0));
            newOp.setPrecedingOperator(parentOp);
        
            parentOp = newOp;
            tmpOp = tmpOp.getSucceedingOperators().get(0).getOperator();
        }
        
        parentOp.setSucceedingOperator(new lupos.engine.operators.OperatorIDTuple(f2, 0));
        f2.addPrecedingOperator(parentOp);
        // --- clone jump-over operators - end ---
        u.getOperatorIDTuple(this.o).setId(this.operandID);
        u.setIntersectionVariables(new HashSet<Variable>(this.f.getIntersectionVariables()));
        u.setUnionVariables(new HashSet<Variable>(this.f.getUnionVariables()));
    }
}
