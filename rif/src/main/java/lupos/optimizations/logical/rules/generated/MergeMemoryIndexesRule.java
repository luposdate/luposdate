package lupos.optimizations.logical.rules.generated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;




public class MergeMemoryIndexesRule extends Rule {

    private lupos.engine.operators.BasicOperator o2 = null;
    private lupos.engine.operators.BasicOperator[] o1 = null;
    private lupos.engine.operators.multiinput.join.Join j = null;
    private lupos.engine.operators.index.memoryindex.MemoryIndex[] i = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.multiinput.join.Join)) {
            return false;
        }

        this.j = (lupos.engine.operators.multiinput.join.Join) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();


        this._dim_0 = -1;
        this.o1 = new lupos.engine.operators.BasicOperator[_precedingOperators_1_0.size()];
        this.i = new lupos.engine.operators.index.memoryindex.MemoryIndex[_precedingOperators_1_0.size()];

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
            if(!(_sucOpIDTup_1_0.getOperator() instanceof lupos.engine.operators.BasicOperator)) {
                continue;
            }

            this.o2 = (lupos.engine.operators.BasicOperator) _sucOpIDTup_1_0.getOperator();

            return true;
        }

        return false;
    }

    private boolean _checkPrivate1(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.index.memoryindex.MemoryIndex)) {
            return false;
        }

        this.i[this._dim_0] = (lupos.engine.operators.index.memoryindex.MemoryIndex) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();

        if(_precedingOperators_1_0.size() != 1) {
            return false;
        }

        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            if(!(_precOp_1_0 instanceof lupos.engine.operators.BasicOperator)) {
                continue;
            }

            this.o1[this._dim_0] = (lupos.engine.operators.BasicOperator) _precOp_1_0;

            return true;
        }

        return false;
    }


    public MergeMemoryIndexesRule() {
        this.startOpClass = lupos.engine.operators.multiinput.join.Join.class;
        this.ruleName = "Merge Memory Indexes";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            if(this._dim_0<=1)
               return false;
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
                // remove obsolete connections...
                for(lupos.engine.operators.index.memoryindex.MemoryIndex _parent : this.i) {
                    _parent.removeSucceedingOperator(this.j);
                    this.j.removePrecedingOperator(_parent);
                }
        
                for(this._dim_0 = 0; this._dim_0 < this.o1.length; this._dim_0 += 1) {
                    this.o1[this._dim_0].removeSucceedingOperator(this.i[this._dim_0]);
                    this.i[this._dim_0].removePrecedingOperator(this.o1[this._dim_0]);
                }
                this.j.removeSucceedingOperator(this.o2);
                this.o2.removePrecedingOperator(this.j);
        
                // add new operators...
                lupos.engine.operators.index.memoryindex.MemoryIndex i_new = null;
                i_new = new lupos.engine.operators.index.memoryindex.MemoryIndex(this.i[0].getIndexCollection());
        
        
                // add new connections...
                i_new.addSucceedingOperator(this.o2);
                this.o2.addPrecedingOperator(i_new);
        
                this.o1[0].addSucceedingOperator(i_new);
                i_new.addPrecedingOperator(this.o1[0]);
        
        
                // delete unreachable operators...
                this.deleteOperatorWithoutParentsRecursive(this.j, _startNodes);
                for(this._dim_0 = 0; this._dim_0 < i.length; this._dim_0 += 1) {
                    this.deleteOperatorWithoutParentsRecursive(this.i[this._dim_0], _startNodes);
                }
        
        
        
                // additional replace method code...
                i_new.setTriplePatterns(new java.util.LinkedList<lupos.engine.operators.tripleoperator.TriplePattern>());
                for(lupos.engine.operators.index.memoryindex.MemoryIndex index : this.i) {
                    i_new.getTriplePattern().addAll(index.getTriplePattern());
                
                    i_new.getIntersectionVariables().addAll(index.getIntersectionVariables());
                    i_new.getUnionVariables().addAll(index.getUnionVariables());
                }
    }
}
