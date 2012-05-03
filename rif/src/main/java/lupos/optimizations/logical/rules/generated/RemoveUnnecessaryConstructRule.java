package lupos.optimizations.logical.rules.generated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;




public class RemoveUnnecessaryConstructRule extends Rule {

    private lupos.engine.operators.singleinput.Construct c = null;
    private lupos.rif.operator.PredicatePattern p = null;

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
            if(_sucOpIDTup_1_0.getOperator().getClass() != lupos.rif.operator.PredicatePattern.class) {
                continue;
            }

            this.p = (lupos.rif.operator.PredicatePattern) _sucOpIDTup_1_0.getOperator();

            return true;
        }

        return false;
    }


    public RemoveUnnecessaryConstructRule() {
        this.startOpClass = lupos.engine.operators.singleinput.Construct.class;
        this.ruleName = "Remove Unnecessary Construct";
    }

    protected boolean check(BasicOperator _op) {
        return this._checkPrivate0(_op);
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.c.removeSucceedingOperator(this.p);
        this.p.removePrecedingOperator(this.c);

        // add new operators...


        // add new connections...

        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.c, _startNodes);


        // additional replace method code...

    }
}
