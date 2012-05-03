package lupos.optimizations.logical.rules.generated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;




public class RemoveUnnecessaryConstructPredicateRule extends Rule {

    private lupos.engine.operators.tripleoperator.TriplePattern t = null;
    private lupos.rif.operator.ConstructPredicate c = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.rif.operator.ConstructPredicate.class) {
            return false;
        }

        this.c = (lupos.rif.operator.ConstructPredicate) _op;

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


    public RemoveUnnecessaryConstructPredicateRule() {
        this.startOpClass = lupos.rif.operator.ConstructPredicate.class;
        this.ruleName = "Remove Unnecessary ConstructPredicate";
    }

    protected boolean check(BasicOperator _op) {
        return this._checkPrivate0(_op);
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.c.removeSucceedingOperator(this.t);
        this.t.removePrecedingOperator(this.c);

        // add new operators...


        // add new connections...

        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.c, _startNodes);


        // additional replace method code...

    }
}
