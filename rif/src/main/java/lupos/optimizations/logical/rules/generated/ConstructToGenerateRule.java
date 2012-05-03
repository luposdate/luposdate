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
