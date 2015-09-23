/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
public class CartesianProductAsLateAsPossibleRule extends Rule {
    int id1;
    int id3;
    private lupos.engine.operators.multiinput.join.Join Join1 = null;
    private lupos.engine.operators.multiinput.join.Join Join2 = null;
    private lupos.engine.operators.BasicOperator Op3 = null;
    private lupos.engine.operators.BasicOperator Op2 = null;
    private lupos.engine.operators.BasicOperator Op1 = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.Op3 = (lupos.engine.operators.BasicOperator) _op;

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();

        if(_succedingOperators_1_0.size() != 1) {
            return false;
        }

        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            if(!(_sucOpIDTup_1_0.getOperator() instanceof lupos.engine.operators.multiinput.join.Join)) {
                continue;
            }

            this.Join1 = (lupos.engine.operators.multiinput.join.Join) _sucOpIDTup_1_0.getOperator();

            List<BasicOperator> _precedingOperators_2_1 = _sucOpIDTup_1_0.getOperator().getPrecedingOperators();


            for(BasicOperator _precOp_2_1 : _precedingOperators_2_1) {
                if(_precOp_2_1.getSucceedingOperators().size() != 1) {
                    break;
                }

                if(!(_precOp_2_1 instanceof lupos.engine.operators.BasicOperator)) {
                    continue;
                }

                this.Op2 = (lupos.engine.operators.BasicOperator) _precOp_2_1;

                List<OperatorIDTuple> _succedingOperators_2_0 = _sucOpIDTup_1_0.getOperator().getSucceedingOperators();

                if(_succedingOperators_2_0.size() != 1) {
                    continue;
                }

                for(OperatorIDTuple _sucOpIDTup_2_0 : _succedingOperators_2_0) {
                    if(!(_sucOpIDTup_2_0.getOperator() instanceof lupos.engine.operators.multiinput.join.Join)) {
                        continue;
                    }

                    this.Join2 = (lupos.engine.operators.multiinput.join.Join) _sucOpIDTup_2_0.getOperator();

                    List<BasicOperator> _precedingOperators_3_1 = _sucOpIDTup_2_0.getOperator().getPrecedingOperators();


                    for(BasicOperator _precOp_3_1 : _precedingOperators_3_1) {
                        if(_precOp_3_1.getSucceedingOperators().size() != 1) {
                            break;
                        }

                        if(!(_precOp_3_1 instanceof lupos.engine.operators.BasicOperator)) {
                            continue;
                        }

                        this.Op1 = (lupos.engine.operators.BasicOperator) _precOp_3_1;

                        return true;
                    }
                }
            }
        }

        return false;
    }


    /**
     * <p>Constructor for CartesianProductAsLateAsPossibleRule.</p>
     */
    public CartesianProductAsLateAsPossibleRule() {
        this.startOpClass = lupos.engine.operators.BasicOperator.class;
        this.ruleName = "CartesianProductAsLateAsPossible";
    }

    /** {@inheritDoc} */
    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            if(this.Join1.getIntersectionVariables().size()>0 ||
            		this.Join1.getPrecedingOperators().size()!=2 ||
               		this.Join2.getPrecedingOperators().size()!=2 ||
            		this.Op2.equals(this.Op3) ||
            		!(this.Join1.getClass()==lupos.engine.operators.multiinput.join.HashJoin.class || this.Join1.getClass()==lupos.engine.operators.multiinput.join.HashMapIndexJoin.class) ||
            		!(this.Join2.getClass()==lupos.engine.operators.multiinput.join.HashJoin.class || this.Join2.getClass()==lupos.engine.operators.multiinput.join.HashMapIndexJoin.class) ){
            	return false;
            }
            final HashSet<lupos.datastructures.items.Variable> vars = new HashSet<lupos.datastructures.items.Variable>(this.Op1.getUnionVariables());
            vars.retainAll(this.Op2.getUnionVariables());
            if(vars.size()==0){
            	return false;
            }
            this.id1 = this.Op1.getSucceedingOperators().get(0).getId();
            this.id3 = this.Op3.getSucceedingOperators().get(0).getId();
        }

        return _result;
    }

    /** {@inheritDoc} */
    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        // remove obsolete connections...
        this.Op1.removeSucceedingOperator(this.Join2);
        this.Join2.removePrecedingOperator(this.Op1);
        this.Op3.removeSucceedingOperator(this.Join1);
        this.Join1.removePrecedingOperator(this.Op3);

        // add new operators...


        // add new connections...
        this.Op1.addSucceedingOperator(this.Join1);
        this.Join1.addPrecedingOperator(this.Op1);

        this.Op3.addSucceedingOperator(this.Join2);
        this.Join2.addPrecedingOperator(this.Op3);


        // additional replace method code...
        this.Op3.getSucceedingOperators().get(0).setId(this.id1);
        this.Op1.getSucceedingOperators().get(0).setId(this.id3);
                
        HashSet<lupos.datastructures.items.Variable> union1 = new HashSet<lupos.datastructures.items.Variable>(this.Op1.getUnionVariables());
        union1.addAll(this.Op2.getUnionVariables());
        HashSet<lupos.datastructures.items.Variable> intersection1 = new HashSet<lupos.datastructures.items.Variable>(this.Op1.getUnionVariables());
        intersection1.retainAll(this.Op2.getUnionVariables());
               
        this.Join1.setUnionVariables(union1);
        this.Join1.setIntersectionVariables(intersection1);
               
        HashSet<lupos.datastructures.items.Variable> union2 = new HashSet<lupos.datastructures.items.Variable>(this.Op3.getUnionVariables());
        union1.addAll(union1);
        HashSet<lupos.datastructures.items.Variable> intersection2 = new HashSet<lupos.datastructures.items.Variable>(this.Op3.getUnionVariables());
        intersection1.retainAll(union1);
           
        this.Join2.setUnionVariables(union2);
        this.Join2.setIntersectionVariables(intersection2);
    }
}
