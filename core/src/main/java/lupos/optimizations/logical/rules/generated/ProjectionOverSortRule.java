package lupos.optimizations.logical.rules.generated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;




public class ProjectionOverSortRule extends Rule {

    private lupos.engine.operators.singleinput.sort.Sort sort = null;
    private lupos.engine.operators.singleinput.Projection projection = null;
    private lupos.engine.operators.BasicOperator[] below = null;
    private lupos.engine.operators.BasicOperator[] above = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.singleinput.sort.Sort.class) {
            return false;
        }

        this.sort = (lupos.engine.operators.singleinput.sort.Sort) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();


        this._dim_0 = -1;
        this.above = new lupos.engine.operators.BasicOperator[_precedingOperators_1_0.size()];

        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            this._dim_0 += 1;

            if(!this._checkPrivate1(_precOp_1_0)) {
                return false;
            }
        }

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();


        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            if(_sucOpIDTup_1_0.getOperator().getClass() != lupos.engine.operators.singleinput.Projection.class) {
                continue;
            }

            this.projection = (lupos.engine.operators.singleinput.Projection) _sucOpIDTup_1_0.getOperator();

            List<OperatorIDTuple> _succedingOperators_2_0 = _sucOpIDTup_1_0.getOperator().getSucceedingOperators();


            this._dim_0 = -1;
            this.below = new lupos.engine.operators.BasicOperator[_succedingOperators_2_0.size()];

            for(OperatorIDTuple _sucOpIDTup_2_0 : _succedingOperators_2_0) {
                this._dim_0 += 1;

                if(!this._checkPrivate2(_sucOpIDTup_2_0.getOperator())) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private boolean _checkPrivate1(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.above[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }

    private boolean _checkPrivate2(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.below[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }


    public ProjectionOverSortRule() {
        this.startOpClass = lupos.engine.operators.singleinput.sort.Sort.class;
        this.ruleName = "Projection Over Sort";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            final java.util.Collection<lupos.datastructures.items.Variable> sortCrit = this.sort.getSortCriterium();
            if (sortCrit != null && this.projection.getProjectedVariables().containsAll(sortCrit))
            	return true;
            else return false;
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.sort.removeSucceedingOperator(this.projection);
        this.projection.removePrecedingOperator(this.sort);
        int[] _label_b = null;

        int _label_b_count = 0;
        _label_b = new int[this.below.length];

        for(lupos.engine.operators.BasicOperator _child : this.below) {
            _label_b[_label_b_count] = this.projection.getOperatorIDTuple(_child).getId();
            _label_b_count += 1;

            this.projection.removeSucceedingOperator(_child);
            _child.removePrecedingOperator(this.projection);
        }

        int[] _label_a = null;

        int _label_a_count = 0;
        _label_a = new int[this.above.length];

        for(lupos.engine.operators.BasicOperator _parent : this.above) {
            _label_a[_label_a_count] = _parent.getOperatorIDTuple(this.sort).getId();
            _label_a_count += 1;

            _parent.removeSucceedingOperator(this.sort);
            this.sort.removePrecedingOperator(_parent);
        }


        // add new operators...


        // add new connections...
        _label_a_count = 0;

        for(lupos.engine.operators.BasicOperator _parent : this.above) {
            _parent.addSucceedingOperator(new OperatorIDTuple(this.projection, _label_a[_label_a_count]));
            this.projection.addPrecedingOperator(_parent);

            _label_a_count += 1;
        }


        _label_b_count = 0;

        for(lupos.engine.operators.BasicOperator _child : this.below) {
            this.sort.addSucceedingOperator(new OperatorIDTuple(_child, _label_b[_label_b_count]));
            _child.addPrecedingOperator(this.sort);

            _label_b_count += 1;
        }


        this.projection.addSucceedingOperator(this.sort);
        this.sort.addPrecedingOperator(this.projection);


        // additional replace method code...
        final java.util.Collection<lupos.datastructures.items.Variable> cv = new java.util.LinkedList<lupos.datastructures.items.Variable>();
        cv.addAll(this.projection.getProjectedVariables());
        this.sort.setIntersectionVariables(cv);
        this.sort.setUnionVariables(cv);
    }
}
