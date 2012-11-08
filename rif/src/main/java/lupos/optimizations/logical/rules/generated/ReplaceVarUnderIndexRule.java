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




public class ReplaceVarUnderIndexRule extends Rule {
    private int varCounter = 0;
    private lupos.engine.operators.singleinput.ReplaceVar r = null;
    private lupos.engine.operators.BasicOperator o = null;
    private lupos.engine.operators.index.BasicIndexScan[] i = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.singleinput.ReplaceVar.class) {
            return false;
        }

        this.r = (lupos.engine.operators.singleinput.ReplaceVar) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();


        this._dim_0 = -1;
        this.i = new lupos.engine.operators.index.BasicIndexScan[_precedingOperators_1_0.size()];

        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            this._dim_0 += 1;

            if(!this._checkPrivate1(_precOp_1_0)) {
                return false;
            }
        }

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();

        if(_succedingOperators_1_0.size() != 1) {
            return false;
        }

        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            if(_sucOpIDTup_1_0.getOperator().getPrecedingOperators().size() != 1) {
                break;
            }

            if(!(_sucOpIDTup_1_0.getOperator() instanceof lupos.engine.operators.BasicOperator)) {
                continue;
            }

            this.o = (lupos.engine.operators.BasicOperator) _sucOpIDTup_1_0.getOperator();

            return true;
        }

        return false;
    }

    private boolean _checkPrivate1(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.index.BasicIndexScan)) {
            return false;
        }

        this.i[this._dim_0] = (lupos.engine.operators.index.BasicIndexScan) _op;

        return true;
    }


    public ReplaceVarUnderIndexRule() {
        this.startOpClass = lupos.engine.operators.singleinput.ReplaceVar.class;
        this.ruleName = "ReplaceVar Under Index";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            java.util.Set<lupos.datastructures.items.Variable> vars = new java.util.HashSet<lupos.datastructures.items.Variable>(this.r.getSubstitutionsVariableRight());
            
            if(vars.size() != this.r.getSubstitutionsVariableRight().size()) {
                return false;
            }
            
            if(this.o instanceof lupos.engine.operators.singleinput.Result) {
                return false;
            }
            
            for(lupos.engine.operators.index.BasicIndexScan tmp_i : this.i) {
                if(tmp_i.getSucceedingOperators().size() > 1) {
                    return false;
                }
            }
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        int[] _label_a = null;

        int _label_a_count = 0;
        _label_a = new int[this.i.length];

        for(lupos.engine.operators.index.BasicIndexScan _parent : this.i) {
            _label_a[_label_a_count] = _parent.getOperatorIDTuple(this.r).getId();
            _label_a_count += 1;

            _parent.removeSucceedingOperator(this.r);
            this.r.removePrecedingOperator(_parent);
        }

        this.r.removeSucceedingOperator(this.o);
        this.o.removePrecedingOperator(this.r);

        // add new operators...


        // add new connections...
        _label_a_count = 0;

        for(lupos.engine.operators.index.BasicIndexScan _parent : this.i) {
            _parent.addSucceedingOperator(new OperatorIDTuple(this.o, _label_a[_label_a_count]));
            this.o.addPrecedingOperator(_parent);

            _label_a_count += 1;
        }



        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.r, _startNodes);


        // additional replace method code...
        for(lupos.engine.operators.index.BasicIndexScan index : this.i) {
            java.util.Map<String, lupos.datastructures.items.Variable> varMap = new java.util.HashMap<String, lupos.datastructures.items.Variable>();
            java.util.Map<lupos.datastructures.items.Variable, String> aliasMap = new java.util.HashMap<lupos.datastructures.items.Variable, String>();
        
            for(lupos.datastructures.items.Variable var : index.getUnionVariables()) {
                String aliasName = "RepVar_" + this.varCounter++ + "_" + var.getName();
                varMap.put(aliasName, var);
                aliasMap.put(var, aliasName);
            }
        
            for(lupos.engine.operators.tripleoperator.TriplePattern tp : index.getTriplePattern()) {
                for(lupos.datastructures.items.Variable var : tp.getVariables()) {
                    tp.replace(var, new lupos.datastructures.items.Variable(aliasMap.get(var)));
                }
            }
        
            for(lupos.engine.operators.tripleoperator.TriplePattern tp : index.getTriplePattern()) {
                for(lupos.datastructures.items.Variable v : tp.getVariables()) {
                    lupos.datastructures.items.Variable rv = this.r.getReplacement(varMap.get(v.getName()));
        
                    if(rv != null) {
                        tp.replace(v, rv);
                    }
                }
            }
        }
    }
}
