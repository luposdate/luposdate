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




public class ConstructToGenerateRule extends Rule {

    private lupos.engine.operators.tripleoperator.TriplePattern t = null;
    private lupos.engine.operators.singleinput.Construct c = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.singleinput.Construct.class) {
            return false;
        }

        this.c = (lupos.engine.operators.singleinput.Construct) _op;

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();

        if(_succedingOperators_1_0.size() != 1) {
            return false;
        }

        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            if(_sucOpIDTup_1_0.getOperator().getClass() != lupos.engine.operators.tripleoperator.TriplePattern.class) {
                continue;
            }

            this.t = (lupos.engine.operators.tripleoperator.TriplePattern) _sucOpIDTup_1_0.getOperator();

            return true;
        }

        return false;
    }


    public ConstructToGenerateRule() {
        this.startOpClass = lupos.engine.operators.singleinput.Construct.class;
        this.ruleName = "Construct To Generate";
    }

    protected boolean check(BasicOperator _op) {
        return this._checkPrivate0(_op);
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        for(lupos.engine.operators.tripleoperator.TriplePattern tp : this.c.getTemplates()) {
            lupos.engine.operators.singleinput.generate.Generate generate = new lupos.engine.operators.singleinput.generate.Generate(this.t, tp.getItems());
            generate.setPrecedingOperators(new java.util.ArrayList<lupos.engine.operators.BasicOperator>(this.c.getPrecedingOperators()));
        
            this.addNodeToStartNodeMapNullCheck(generate, _startNodes);
        
            for(lupos.engine.operators.BasicOperator prec : generate.getPrecedingOperators()) {
                prec.addSucceedingOperator(generate);
            }
        
            this.t.addPrecedingOperator(generate);
        }
        
        for (final OperatorIDTuple oidtuple : this.c.getSucceedingOperators()) {
            oidtuple.getOperator().removePrecedingOperator(this.c);
        }
        for (final BasicOperator prec : this.c.getPrecedingOperators()) {
            prec.removeSucceedingOperator(this.c);
        }
        this.deleteNodeFromStartNodeMapNullCheck(this.c, _startNodes);
    }
}
