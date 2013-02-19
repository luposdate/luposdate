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




public class BoundInFilterunderIndexRule extends Rule {
    private boolean deleteAll;
    private lupos.engine.operators.singleinput.Filter f = null;
    private lupos.engine.operators.BasicOperator[] o = null;
    private lupos.engine.operators.BasicOperator f2_begin = null;
    private lupos.engine.operators.BasicOperator f2_end = null;
    private lupos.engine.operators.index.BasicIndexScan i = null;
    private int _dim_0 = -1;

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
            this.f2_end = (lupos.engine.operators.BasicOperator) _precOp_1_0;
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

            this.f2_begin = (lupos.engine.operators.BasicOperator) _searchIndex_1_0.getSucceedingOperators().get(0).getOperator();
            // --- handle JumpOver - end ---


            List<BasicOperator> _precedingOperators_2_0 = this.f2_begin.getPrecedingOperators();

            if(_searchIndex_1_0 != this.f2_begin) {
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


    public BoundInFilterunderIndexRule() {
        this.startOpClass = lupos.engine.operators.singleinput.Filter.class;
        this.ruleName = "Bound In Filter under Index";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            lupos.sparql1_1.Node n = this.f.getNodePointer();
            
            boolean negated = false;
            
            if(n.jjtGetNumChildren() > 0) {
                n = n.jjtGetChild(0);
            
                while(n instanceof lupos.sparql1_1.ASTNotNode){
                    negated = !negated;
                n = n.jjtGetChild(0);
                }
                if(n instanceof lupos.sparql1_1.ASTBoundFuncNode){
                    n = n.jjtGetChild(0);
                    if(n instanceof lupos.sparql1_1.ASTVar) {
                        lupos.datastructures.items.Variable var = new lupos.datastructures.items.Variable(((lupos.sparql1_1.ASTVar) n).getName());
                        this.deleteAll = (this.i.getVarsInTriplePatterns().contains(var)) == negated;
                        return true;
                    }
                }
            }
            return false;
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        this.f.removeFromOperatorGraph();
        if(this.deleteAll){
            this.i.removeFromOperatorGraphWithoutConnectingPrecedingWithSucceedingOperators();
            this.deleteNodeFromStartNodeMapNullCheck(this.i, _startNodes);
            for(lupos.engine.operators.BasicOperator child : this.o) {
                this.deleteOperatorWithoutParentsRecursive(child, _startNodes);
            }
        }
    }
}
