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

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;

import java.util.Collection;
import java.util.LinkedList;

import lupos.engine.operators.singleinput.modifiers.Offset;
import lupos.engine.operators.singleinput.modifiers.SortLimit;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorAST;
import lupos.datastructures.items.Variable;


public class SortLimitoverSortRule extends Rule {
    private final static int MAXLIMIT = 1000;
    private int limitSortLimit = 0;
    private lupos.engine.operators.singleinput.modifiers.Limit limit = null;
    private lupos.engine.operators.singleinput.sort.Sort sort = null;
    private lupos.engine.operators.BasicOperator[] above = null;
    private lupos.engine.operators.BasicOperator offset_end = null;
    private lupos.engine.operators.BasicOperator offset_begin = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.singleinput.modifiers.Limit)) {
            return false;
        }

        this.limit = (lupos.engine.operators.singleinput.modifiers.Limit) _op;

        List<BasicOperator> _precedingOperators_1_0 = _op.getPrecedingOperators();

        if(_precedingOperators_1_0.size() != 1) {
            return false;
        }

        for(BasicOperator _precOp_1_0 : _precedingOperators_1_0) {
            if(_precOp_1_0.getSucceedingOperators().size() != 1) {
                break;
            }

            // --- handle JumpOver - begin ---
            this.offset_end = (lupos.engine.operators.BasicOperator) _precOp_1_0;
            BasicOperator _searchIndex_1_0 = _precOp_1_0;
            boolean _continueFlag_1_0 = false;

            while(_searchIndex_1_0 != null && (!(_searchIndex_1_0 instanceof lupos.engine.operators.singleinput.sort.Sort))) {
                if(!(_searchIndex_1_0 instanceof lupos.engine.operators.singleinput.modifiers.Offset)) {
                    _continueFlag_1_0 = true;
                    break;
                }

                if(_searchIndex_1_0.getSucceedingOperators().size() != 1 || _searchIndex_1_0.getPrecedingOperators().size() != 1) {
                    _continueFlag_1_0 = true;
                    break;
                }

                _searchIndex_1_0 = _searchIndex_1_0.getPrecedingOperators().get(0);
            }

            if(_continueFlag_1_0) {
                continue;
            }

            this.offset_begin = (lupos.engine.operators.BasicOperator) _searchIndex_1_0.getSucceedingOperators().get(0).getOperator();
            // --- handle JumpOver - end ---


            List<BasicOperator> _precedingOperators_2_0 = this.offset_begin.getPrecedingOperators();

            if(_searchIndex_1_0 != this.offset_begin) {
                if(_precedingOperators_2_0.size() != 1) {
                    continue;
                }
            }

            for(BasicOperator _precOp_2_0 : _precedingOperators_2_0) {
                if(_precOp_2_0.getSucceedingOperators().size() != 1) {
                    break;
                }

                if(!(_precOp_2_0 instanceof lupos.engine.operators.singleinput.sort.Sort)) {
                    continue;
                }

                this.sort = (lupos.engine.operators.singleinput.sort.Sort) _precOp_2_0;

                List<BasicOperator> _precedingOperators_3_0 = _precOp_2_0.getPrecedingOperators();


                this._dim_0 = -1;
                this.above = new lupos.engine.operators.BasicOperator[_precedingOperators_3_0.size()];

                for(BasicOperator _precOp_3_0 : _precedingOperators_3_0) {
                    this._dim_0 += 1;

                    if(!this._checkPrivate1(_precOp_3_0)) {
                        return false;
                    }
                }

                return true;
            }
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


    public SortLimitoverSortRule() {
        this.startOpClass = lupos.engine.operators.singleinput.modifiers.Limit.class;
        this.ruleName = "SortLimit over Sort";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            for(BasicOperator bo:above){
            	if(bo instanceof SortLimit){
            		return false;
            	}
            }
            this.limitSortLimit = this.limit.getLimit();
            if(this.offset_begin!=null && this.offset_begin instanceof Offset){
            	this.limitSortLimit += ((Offset)this.offset_begin).getOffset();
            }
            if(this.limitSortLimit > MAXLIMIT){
            	return false;
            }
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
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
        lupos.engine.operators.singleinput.modifiers.SortLimit sortLimit = null;
        sortLimit = new lupos.engine.operators.singleinput.modifiers.SortLimit();


        // add new connections...
        _label_a_count = 0;

        for(lupos.engine.operators.BasicOperator _parent : this.above) {
            _parent.addSucceedingOperator(new OperatorIDTuple(sortLimit, _label_a[_label_a_count]));
            sortLimit.addPrecedingOperator(_parent);

            _label_a_count += 1;
        }


        sortLimit.addSucceedingOperator(new OperatorIDTuple(this.sort, 0));
        this.sort.addPrecedingOperator(sortLimit);


        // additional replace method code...
        sortLimit.setLimit(this.limitSortLimit);
        sortLimit.setComparator(this.sort.getComparator());
        
        final Collection<Variable> cv = new LinkedList<Variable>();
        cv.addAll(this.sort.getIntersectionVariables());
        sortLimit.setIntersectionVariables(cv);
        sortLimit.setUnionVariables(cv);
    }
}
