/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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




public class BinaryJoinRule extends Rule {
                public BasicOperator getBinaryJoin(lupos.engine.operators.multiinput.join.Join oldJoin, HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
                	java.util.LinkedList<BasicOperator> oldJoinPrec = new java.util.LinkedList<BasicOperator>();
                	oldJoinPrec.addAll(oldJoin.getPrecedingOperators());
                    java.util.Collection<BasicOperator> newOrder = this.optimizeJoinOrderAccordingToMostRestrictionsForMergeJoin(oldJoinPrec);
                    java.util.Collection<BasicOperator> remainingJoins = new java.util.LinkedList<BasicOperator>();
                    java.util.HashSet<BasicOperator> alreadyUsed = new java.util.HashSet<BasicOperator>();
                    
                    java.util.Iterator<BasicOperator> itp = newOrder.iterator();
                
                    while(itp.hasNext()) {
             
                        BasicOperator first = this.getNext(itp, alreadyUsed);
                        
                        if(first==null)
                        	break;
                        
                        if(itp.hasNext()) {
                        	feedAlreadyUsed(oldJoin, first.getOperatorIDTuple(oldJoin).getId(), alreadyUsed);
                        	
                            BasicOperator second = this.getNext(itp, alreadyUsed);
                            if(second==null){
                                remainingJoins.add(first);
                                break;
                            }
                            
                            lupos.engine.operators.multiinput.join.Join newJoin = new lupos.engine.operators.multiinput.join.Join();
                            this.addNodeToStartNodeMapNullCheck(newJoin, _startNodes);
                                
                            handleJoinOperand(oldJoin, first.getOperatorIDTuple(oldJoin).getId(), newJoin, 0, alreadyUsed);
                            handleJoinOperand(oldJoin, second.getOperatorIDTuple(oldJoin).getId(), newJoin, 1, alreadyUsed);
                                
                            HashSet<lupos.datastructures.items.Variable> hv = new HashSet<lupos.datastructures.items.Variable>();
                            for(BasicOperator bo: newJoin.getPrecedingOperators()){
                            	hv.addAll(bo.getUnionVariables());
                            }    
                            newJoin.setUnionVariables(hv);
                
                            newJoin.setIntersectionVariables(new HashSet<lupos.datastructures.items.Variable>());
                            newJoin.getIntersectionVariables().addAll(first.getUnionVariables());
                            for(BasicOperator bo: newJoin.getPrecedingOperators()){
                            	newJoin.getIntersectionVariables().retainAll(bo.getUnionVariables());
                            }
                            
                            remainingJoins.add(newJoin);
                        }
                        else {
                            remainingJoins.add(first);
                        }
                    }
                
                    while(remainingJoins.size() > 1) {
                        // choose best combination
                        java.util.Collection<BasicOperator> co = this.getNextJoin(remainingJoins);
                        java.util.Iterator<BasicOperator> io = co.iterator();
                        BasicOperator first = io.next();
                        BasicOperator second = io.next();
                
                        lupos.engine.operators.multiinput.join.Join join = new lupos.engine.operators.multiinput.join.Join();
                        this.addNodeToStartNodeMapNullCheck(join, _startNodes);
                        
                        if(first instanceof lupos.engine.operators.multiinput.join.Join && second instanceof lupos.engine.operators.multiinput.join.Join){        	        
        	                first.setSucceedingOperator(new OperatorIDTuple(join, 0));	    	        
        	                join.addPrecedingOperator(first);
                        } else {
                        	if(first instanceof lupos.engine.operators.multiinput.join.Join){
                        		BasicOperator tmp = first;
                        		first = second;
                        		second = tmp;
                        	}
                        	// second is now a join and first something else...
                            handleJoinOperand(oldJoin, first.getOperatorIDTuple(oldJoin).getId(), join, 0, alreadyUsed);                	
                        }
                        
                        second.setSucceedingOperator(new OperatorIDTuple(join, 1));    	        
                        join.addPrecedingOperator(second);
                        
                        HashSet<lupos.datastructures.items.Variable> hv = new HashSet<lupos.datastructures.items.Variable>();
                        for(BasicOperator bo: join.getPrecedingOperators()){
                        	hv.addAll(bo.getUnionVariables());
                        }    
                        join.setUnionVariables(hv);
            
                        join.setIntersectionVariables(new HashSet<lupos.datastructures.items.Variable>());
                        join.getIntersectionVariables().addAll(first.getUnionVariables());
                        for(BasicOperator bo: join.getPrecedingOperators()){
                        	join.getIntersectionVariables().retainAll(bo.getUnionVariables());
                        }
                
                        remainingJoins.remove(first);
                        remainingJoins.remove(second);
                        remainingJoins.add(join);
                    }
                
                    return remainingJoins.iterator().next();
                }
                
                private void feedAlreadyUsed(lupos.engine.operators.multiinput.join.Join oldJoin, int oldOperandID, java.util.HashSet<BasicOperator> alreadyUsed){
                	for(BasicOperator bo: oldJoin.getPrecedingOperators()){
                		OperatorIDTuple opID=bo.getOperatorIDTuple(oldJoin);
                		if(opID!=null && opID.getId()==oldOperandID){
                			alreadyUsed.add(bo);
                		}
                	}        	
                }
                
                private void handleJoinOperand(lupos.engine.operators.multiinput.join.Join oldJoin, int oldOperandID, lupos.engine.operators.multiinput.join.Join newJoin, int newOperandID, java.util.HashSet<BasicOperator> alreadyUsed){
                	java.util.LinkedList<lupos.engine.operators.BasicOperator> tmp = new java.util.LinkedList<lupos.engine.operators.BasicOperator>();
                	tmp.addAll(oldJoin.getPrecedingOperators());
                	for(BasicOperator bo: tmp){
                		OperatorIDTuple opID=bo.getOperatorIDTuple(oldJoin);
                		if(opID!=null && opID.getId()==oldOperandID){
                			alreadyUsed.add(bo);
                			bo.replaceOperatorIDTuple(opID, new OperatorIDTuple(newJoin, newOperandID));
                			newJoin.addPrecedingOperator(bo);
                		}
                	}
                }
                
            private BasicOperator getNext(java.util.Iterator<BasicOperator> itp, java.util.HashSet<BasicOperator> alreadyUsed){
                        BasicOperator first = itp.next();
                        while(itp.hasNext() && alreadyUsed.contains(first)){
                        	first = itp.next();
                        }
                        
                        if(alreadyUsed.contains(first))
                        	return null;
                        
                        alreadyUsed.add(first);
                        
                        return first;
            }
        
        private java.util.Collection<BasicOperator> optimizeJoinOrderAccordingToMostRestrictionsForMergeJoin(List<BasicOperator> remaining) {
            java.util.Collection<BasicOperator> newOrder = new java.util.LinkedList<BasicOperator>();
        
            while(remaining.size() > 1) {
                BasicOperator best1 = null;
                BasicOperator best2 = null;
                int minOpenPositions = 4;
        
                for(BasicOperator bo1 : remaining) {
                    for(BasicOperator bo2 : remaining) {
                        if(!bo1.equals(bo2)) {
                            java.util.Collection<lupos.datastructures.items.Variable> v = new java.util.LinkedList<lupos.datastructures.items.Variable>(); 
                            v.addAll(bo1.getUnionVariables());
                            v.retainAll(bo2.getUnionVariables());
        
                            int openPositions = bo1.getUnionVariables().size() - v.size();
        
                            if(openPositions < minOpenPositions) {
                                minOpenPositions = openPositions;
                                best1 = bo1;
                                best2 = bo2;
                            }
                        }
                    }
                }
        
                newOrder.add(best1);
                newOrder.add(best2);
        
                remaining.remove(best1);
                remaining.remove(best2);
            }
        
            if(remaining.size() == 1) {
                for(BasicOperator bo1 : remaining) {
                    newOrder.add(bo1);
                }
            }
        
            return newOrder;
        }
        
        private java.util.Collection<BasicOperator> getNextJoin(java.util.Collection<BasicOperator> remainingJoins) {
            java.util.Collection<BasicOperator> co = new java.util.LinkedList<BasicOperator>();
            BasicOperator best1 = null;
            BasicOperator best2 = null;
            int minCommonVariables = -1;
        
            for(BasicOperator o1 : remainingJoins) {
                for(BasicOperator o2 : remainingJoins) {
                    if(!o1.equals(o2)) {
                        java.util.Collection<lupos.datastructures.items.Variable> v = new java.util.LinkedList<lupos.datastructures.items.Variable>();
                        v.addAll(o1.getUnionVariables());
                        v.retainAll(o2.getUnionVariables());
        
                        int commonVariables = v.size();
        
                        if(commonVariables > minCommonVariables) {
                            minCommonVariables = commonVariables;
                            best1 = o1;
                            best2 = o2;
                        }
                    }
                }
            }
        
            co.add(best1);
            co.add(best2);
        
            return co;
        }
    private lupos.engine.operators.BasicOperator o2 = null;
    private lupos.engine.operators.BasicOperator[] o1 = null;
    private lupos.engine.operators.multiinput.join.Join join = null;
    private int _dim_0 = -1;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.multiinput.join.Join.class) {
            return false;
        }

        this.join = (lupos.engine.operators.multiinput.join.Join) _op;

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
        if(!(_op instanceof lupos.engine.operators.BasicOperator)) {
            return false;
        }

        this.o1[this._dim_0] = (lupos.engine.operators.BasicOperator) _op;

        return true;
    }


    public BinaryJoinRule() {
        this.startOpClass = lupos.engine.operators.multiinput.join.Join.class;
        this.ruleName = "Binary Join";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            if(this.join instanceof lupos.engine.operators.multiinput.optional.parallel.ParallelOptional || this.join instanceof lupos.engine.operators.multiinput.optional.parallel.MergeParallelOptional) {
                return false;
            }
            
            return (this.join.getNumberOfOperands() > 2);
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        BasicOperator finalJoin = this.getBinaryJoin(this.join, _startNodes);
        finalJoin.setSucceedingOperators(this.join.getSucceedingOperators());
        
        for(OperatorIDTuple opIDt : finalJoin.getSucceedingOperators()) {
            opIDt.getOperator().removePrecedingOperator(this.join);
            opIDt.getOperator().addPrecedingOperator(finalJoin);
        }
        
        this.deleteNodeFromStartNodeMapNullCheck(this.join, _startNodes);
    }
}
