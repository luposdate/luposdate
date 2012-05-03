package lupos.optimizations.logical.rules.generated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;




public class RemoveEmptyIndexRule extends Rule {

    private lupos.engine.operators.BasicOperator o = null;
    private lupos.engine.operators.index.BasicIndex i = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.index.BasicIndex)) {
            return false;
        }

        this.i = (lupos.engine.operators.index.BasicIndex) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();

        if(_precedingOperators_1_0.size() != 1) {
            return false;
        }

        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            if(!(_precOp_1_0 instanceof lupos.engine.operators.BasicOperator)) {
                continue;
            }

            this.o = (lupos.engine.operators.BasicOperator) _precOp_1_0;

            return true;
        }

        return false;
    }


    public RemoveEmptyIndexRule() {
        this.startOpClass = lupos.engine.operators.index.BasicIndex.class;
        this.ruleName = "Remove Empty Index";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            if(this.i instanceof lupos.rif.operator.PredicateIndex
                || this.i instanceof lupos.rif.operator.BooleanIndex
                || this.i instanceof lupos.engine.operators.index.EmptyIndex
                || this.i instanceof lupos.rif.operator.IteratorIndex
                || (!this.i.getPrecedingOperators().isEmpty()
                    && !this.i.getPrecedingOperators().get(0).getSucceedingOperators().isEmpty()
                    && this.i.getPrecedingOperators().get(0).getSucceedingOperators().get(0).getOperator() instanceof lupos.rif.operator.InsertTripleIndex)) {
            					return false;
            }
            
            lupos.datastructures.queryresult.QueryResult qr = null;
            				if (this.i instanceof lupos.engine.operators.index.memoryindex.MemoryIndex) {
            					final lupos.engine.operators.index.memoryindex.MemoryIndex temp = new lupos.engine.operators.index.memoryindex.MemoryIndex(this.i.getIndexCollection());
            					boolean found = false;
            					for (lupos.engine.operators.tripleoperator.TriplePattern pat : ((lupos.engine.operators.index.memoryindex.MemoryIndex) this.i).getTriplePattern()) {
            						temp.setTriplePatterns(java.util.Arrays.asList(pat));
            						lupos.datastructures.queryresult.QueryResult qrtemp = temp.join(this.i.getIndexCollection().dataset);
            						if (qrtemp != null && qrtemp.oneTimeIterator().hasNext()) {
            							found = true;
            						}
            					}
            					if (!found) {
            						return true;
            					}
                                return false;
            				} else {
            					qr = this.i.join(this.i.getIndexCollection().dataset);
                            }
            				if (qr != null) {
            					final java.util.Iterator<lupos.datastructures.bindings.Bindings> pib = qr.oneTimeIterator();
            					if (pib.hasNext()) {
            						if (pib instanceof lupos.datastructures.queryresult.ParallelIterator) {
            							((lupos.datastructures.queryresult.ParallelIterator) pib).close();
                                    }
            						return false;
            					}
            					if (pib instanceof lupos.datastructures.queryresult.ParallelIterator) {
            						((lupos.datastructures.queryresult.ParallelIterator) pib).close();
                                }
            					return true;
            				} else {
            					return true;
            				}
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.o.removeSucceedingOperator(this.i);
        this.i.removePrecedingOperator(this.o);

        // add new operators...


        // add new connections...

        // delete unreachable operators...
        this.deleteOperatorWithoutParentsRecursive(this.i, _startNodes);


        // additional replace method code...

    }
}
