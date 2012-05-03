package lupos.optimizations.logical.rules.generated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;




public class SplitGenerateRule extends Rule {

    private lupos.engine.operators.singleinput.generate.Generate g = null;
    private lupos.engine.operators.tripleoperator.TriplePattern[] t = null;
    private lupos.engine.operators.BasicOperator o1 = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.o1 = (lupos.engine.operators.BasicOperator) _op;

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();


        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            if(_sucOpIDTup_1_0.getOperator().getPrecedingOperators().size() != 1) {
                break;
            }

            if(_sucOpIDTup_1_0.getOperator().getClass() != lupos.engine.operators.singleinput.generate.Generate.class) {
                continue;
            }

            this.g = (lupos.engine.operators.singleinput.generate.Generate) _sucOpIDTup_1_0.getOperator();

            List<OperatorIDTuple> _succedingOperators_2_0 = _sucOpIDTup_1_0.getOperator().getSucceedingOperators();


            this._dim_0 = -1;
            this.t = new lupos.engine.operators.tripleoperator.TriplePattern[_succedingOperators_2_0.size()];

            for(OperatorIDTuple _sucOpIDTup_2_0 : _succedingOperators_2_0) {
                this._dim_0 += 1;

                if(!this._checkPrivate1(_sucOpIDTup_2_0.getOperator())) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private boolean _checkPrivate1(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.tripleoperator.TriplePattern)) {
            return false;
        }

        this.t[this._dim_0] = (lupos.engine.operators.tripleoperator.TriplePattern) _op;

        return true;
    }


    public SplitGenerateRule() {
        this.startOpClass = lupos.engine.operators.BasicOperator.class;
        this.ruleName = "Split Generate";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            return this.g.getSucceedingOperators().size() > 1;
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.o1.removeSucceedingOperator(this.g);
        this.g.removePrecedingOperator(this.o1);
        int[] _label_a = null;

        int _label_a_count = 0;
        _label_a = new int[this.t.length];

        for(lupos.engine.operators.tripleoperator.TriplePattern _child : this.t) {
            _label_a[_label_a_count] = this.g.getOperatorIDTuple(_child).getId();
            _label_a_count += 1;

            this.g.removeSucceedingOperator(_child);
            _child.removePrecedingOperator(this.g);
        }


        // add new operators...
        lupos.engine.operators.singleinput.generate.Generate[] g_new = null;
        g_new = new lupos.engine.operators.singleinput.generate.Generate[this.t.length];

        for(this._dim_0 = 0; this._dim_0 < g_new.length; this._dim_0 += 1) {
            g_new[this._dim_0] = new lupos.engine.operators.singleinput.generate.Generate();
        }


        // add new connections...
        for(this._dim_0 = 0; this._dim_0 < g_new.length; this._dim_0 += 1) {
            g_new[this._dim_0].addSucceedingOperator(this.t[this._dim_0]);
            this.t[this._dim_0].addPrecedingOperator(g_new[this._dim_0]);
        }

        _label_a_count = 0;

        for(lupos.engine.operators.singleinput.generate.Generate _child : g_new) {
            this.o1.addSucceedingOperator(new OperatorIDTuple(_child, _label_a[_label_a_count]));
            _child.addPrecedingOperator(this.o1);

            _label_a_count += 1;
        }



        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.g, _startNodes);


        // additional replace method code...
        for(lupos.engine.operators.singleinput.generate.Generate tmp_g : g_new) {
            tmp_g.setValueOrVariable(this.g.getValueOrVariable());
        }
    }
}
