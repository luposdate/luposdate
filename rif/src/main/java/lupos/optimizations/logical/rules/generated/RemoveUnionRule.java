package lupos.optimizations.logical.rules.generated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;




public class RemoveUnionRule extends Rule {

    private lupos.engine.operators.BasicOperator[] o2 = null;
    private lupos.engine.operators.multiinput.Union u = null;
    private lupos.engine.operators.BasicOperator[] o1 = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.multiinput.Union.class) {
            return false;
        }

        this.u = (lupos.engine.operators.multiinput.Union) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();


        this._dim_0 = -1;
        this.o1 = new lupos.engine.operators.BasicOperator[_precedingOperators_1_0.size()];

        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            this._dim_0 += 1;

            if(!this._checkPrivate1(_precOp_1_0)) {
                return false;
            }
        }

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();


        this._dim_0 = -1;
        this.o2 = new lupos.engine.operators.BasicOperator[_succedingOperators_1_0.size()];

        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            this._dim_0 += 1;

            if(!this._checkPrivate2(_sucOpIDTup_1_0.getOperator())) {
                return false;
            }
        }

        return true;
    }

    private boolean _checkPrivate1(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.o1[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }

    private boolean _checkPrivate2(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.o2[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }


    public RemoveUnionRule() {
        this.startOpClass = lupos.engine.operators.multiinput.Union.class;
        this.ruleName = "Remove Union";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            return !(this.u instanceof lupos.engine.operators.multiinput.MergeUnion);
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        for(BasicOperator over : this.u.getPrecedingOperators()) {
            over.addSucceedingOperators(com.google.common.collect.Lists.newArrayList(this.u.getSucceedingOperators()));
        }
        
        this.deleteOperator(this.u, _startNodes);
    }
}
