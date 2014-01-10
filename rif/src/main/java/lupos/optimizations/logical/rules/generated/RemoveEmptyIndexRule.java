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

import lupos.engine.operators.BasicOperator;
import lupos.optimizations.logical.rules.generated.runtime.Rule;




public class RemoveEmptyIndexRule extends Rule {

    private lupos.engine.operators.BasicOperator o = null;
    private lupos.engine.operators.index.BasicIndexScan i = null;

    private boolean _checkPrivate0(final BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.index.BasicIndexScan)) {
            return false;
        }

        this.i = (lupos.engine.operators.index.BasicIndexScan) _op;

        final List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();

        if(_precedingOperators_1_0.size() != 1) {
            return false;
        }

        for(final BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            if(!(_precOp_1_0 instanceof lupos.engine.operators.BasicOperator)) {
                continue;
            }

            this.o = _precOp_1_0;

            return true;
        }

        return false;
    }


    public RemoveEmptyIndexRule() {
        this.startOpClass = lupos.engine.operators.index.BasicIndexScan.class;
        this.ruleName = "Remove Empty Index";
    }

    @Override
	protected boolean check(final BasicOperator _op) {
        final boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            if(this.i instanceof lupos.rif.operator.PredicateIndexScan
                || this.i instanceof lupos.rif.operator.BooleanIndexScan
                || this.i instanceof lupos.rif.operator.IteratorIndexScan
                || (!this.i.getPrecedingOperators().isEmpty()
                    && !this.i.getPrecedingOperators().get(0).getSucceedingOperators().isEmpty()
                    && this.i.getPrecedingOperators().get(0).getSucceedingOperators().get(0).getOperator() instanceof lupos.rif.operator.InsertTripleIndexScan)) {
            					return false;
            }

            lupos.datastructures.queryresult.QueryResult qr = null;
            				if (this.i instanceof lupos.engine.operators.index.memoryindex.MemoryIndexScan) {
            					final lupos.engine.operators.index.memoryindex.MemoryIndexScan temp = new lupos.engine.operators.index.memoryindex.MemoryIndexScan(this.i.getRoot());
            					temp.setBindingsFactory(this.i.getBindingsFactory());
            					boolean found = false;
            					for (final lupos.engine.operators.tripleoperator.TriplePattern pat : ((lupos.engine.operators.index.memoryindex.MemoryIndexScan) this.i).getTriplePattern()) {
            						temp.setTriplePatterns(java.util.Arrays.asList(pat));
            						final lupos.datastructures.queryresult.QueryResult qrtemp = temp.join(this.i.getRoot().dataset);
            						if (qrtemp != null && qrtemp.oneTimeIterator().hasNext()) {
            							found = true;
            						}
            					}
            					if (!found) {
            						return true;
            					}
                                return false;
            				} else {
            					qr = this.i.join(this.i.getRoot().dataset);
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

    @Override
	protected void replace(final HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
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
