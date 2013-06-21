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




public class FactoroutANDinFilterRule extends Rule {
    private int operandIDOfFilter;
    private lupos.engine.operators.singleinput.filter.Filter b = null;
    private lupos.engine.operators.BasicOperator c = null;
    private lupos.engine.operators.BasicOperator a = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.singleinput.filter.Filter.class) {
            return false;
        }

        this.b = (lupos.engine.operators.singleinput.filter.Filter) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();


        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            if(!(_precOp_1_0 instanceof lupos.engine.operators.BasicOperator)) {
                continue;
            }

            this.a = (lupos.engine.operators.BasicOperator) _precOp_1_0;

            List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();

            if(_succedingOperators_1_0.size() != 1) {
                return false;
            }

            for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
                if(!(_sucOpIDTup_1_0.getOperator() instanceof lupos.engine.operators.BasicOperator)) {
                    continue;
                }

                this.c = (lupos.engine.operators.BasicOperator) _sucOpIDTup_1_0.getOperator();

                return true;
            }
        }

        return false;
    }


    public FactoroutANDinFilterRule() {
        this.startOpClass = lupos.engine.operators.singleinput.filter.Filter.class;
        this.ruleName = "Factor out AND in Filter";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            this.operandIDOfFilter = this.b.getOperatorIDTuple(c).getId();
            lupos.sparql1_1.Node n = this.b.getNodePointer();
            
            if(n.jjtGetNumChildren() > 0) {
                n = n.jjtGetChild(0);
            
                return (n instanceof lupos.sparql1_1.ASTAndNode);
            }
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.b.removeSucceedingOperator(this.c);
        this.c.removePrecedingOperator(this.b);

        // add new operators...
        lupos.engine.operators.singleinput.filter.Filter b2 = null;
        b2 = new lupos.engine.operators.singleinput.filter.Filter();


        // add new connections...
        this.b.addSucceedingOperator(b2);
        b2.addPrecedingOperator(this.b);

        b2.addSucceedingOperator(this.c);
        this.c.addPrecedingOperator(b2);


        // additional replace method code...
        lupos.sparql1_1.Node n = this.b.getNodePointer();
        
        n = n.jjtGetChild(0);
        lupos.sparql1_1.ASTFilterConstraint node1 = new lupos.sparql1_1.ASTFilterConstraint(	0);
        lupos.sparql1_1.ASTFilterConstraint node2 = new lupos.sparql1_1.ASTFilterConstraint(	1);
        node1.jjtAddChild(n.jjtGetChild(0), 0);
        node2.jjtAddChild(n.jjtGetChild(1), 0);
        n.jjtGetChild(0).jjtSetParent(node1);
        n.jjtGetChild(1).jjtSetParent(node2);
        
        this.b.setNodePointer(node1);
        b2.setNodePointer(node2);
        
        b2.setCollectionForExistNodes(this.b.getCollectionForExistNodes());
        b2.getUsedEvaluationVisitor().setEvaluator(this.b.getUsedEvaluationVisitor().getEvaluator());
        
        b2.setIntersectionVariables(this.b.getIntersectionVariables());
        b2.setUnionVariables(this.b.getUnionVariables());
        b2.getOperatorIDTuple(c).setId(this.operandIDOfFilter);
    }
}
