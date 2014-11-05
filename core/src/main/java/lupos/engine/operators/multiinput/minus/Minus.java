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
package lupos.engine.operators.multiinput.minus;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.ParallelIteratorMultipleQueryResults;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.MultiInputOperator;
import lupos.misc.debug.DebugStep;

public class Minus extends MultiInputOperator {

	protected ParallelIteratorMultipleQueryResults[] operands = {	new ParallelIteratorMultipleQueryResults(),
																	new ParallelIteratorMultipleQueryResults()};

	protected LinkedList<QueryResult> oldResultsOfLeftOperand = new LinkedList<QueryResult>();

	protected final boolean considerEmptyIntersection;

	public Minus(){
		this(true);
	}

	public Minus(final boolean considerEmptyIntersection){
		this.considerEmptyIntersection = considerEmptyIntersection;
	}

	@Override
	public synchronized QueryResult process(final QueryResult queryResult, final int operandID) {
		// wait for all query results and process them when
		// EndOfEvaluationMessage arrives
		if(!queryResult.isEmpty()){
			if(operandID==1){
				if(this.operands[operandID].isIterating()){
					// Minus is definitely in a cycle and gets new results for the right operand!
					// Check some special cases, where this is no problem!
					if(this.operands[operandID].contains(queryResult)){
						return null;
					} else {
						for(final Bindings b: queryResult){
							for(final QueryResult qr: this.oldResultsOfLeftOperand){
								if(qr.contains(b)){
									throw new RuntimeException("The result of this Minus operator has already been computed, but new results are added to the right operand, such that a previously computed result becomes invalid...");
								}
							}
						}
						// allow adding queryResult
						this.operands[1].addQueryResultAllowingAddingAfterIterating(queryResult);
						return null;
					}
				}
			}
			this.operands[operandID].addQueryResult(queryResult);
		}
		return null;
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		if (!this.operands[0].isEmpty() && !this.operands[1].isEmpty()) {
			final QueryResult result = QueryResult.createInstance();
			this.operands[0].materialize();
			this.operands[1].materialize();
			this.oldResultsOfLeftOperand.add(this.operands[0].getQueryResult());
			final Iterator<Bindings> iteratorLeftChild = this.operands[0].getQueryResult().oneTimeIterator();
			while (iteratorLeftChild.hasNext()) {
				final Bindings leftItem = iteratorLeftChild.next();
				boolean found = false;
				for(final Bindings rightItem : this.operands[1].getQueryResult()) {
					// compute intersection of the variable sets
					final Set<Variable> vars = rightItem.getVariableSet();
					vars.retainAll(leftItem.getVariableSet());

					// if intersection is empty then isEqual should always be false in the typical case (except for not in RIF rules),
					// workaround: check whether vars is empty
					if(vars.isEmpty() && this.considerEmptyIntersection){
						continue;
					}

					boolean isEqual = true;
					for (final Variable v : vars) {
						if ((v.getLiteral(leftItem).compareToNotNecessarilySPARQLSpecificationConform(v.getLiteral(rightItem))) != 0) {
							isEqual = false;
						}
					}

					if (isEqual){
						found = true;
						break;
					}
				}
				if (!found){
					result.add(leftItem);
				}
			}

			// if the operator is in a cycle: it is no problem if the left operand gets new bindings...
			this.operands[0] = new ParallelIteratorMultipleQueryResults();

			for (final OperatorIDTuple opId : this.succeedingOperators) {
				opId.processAll(result);
			}
		} else if (!this.operands[0].isEmpty()) { // happens when the group constraint which follows the minus is empty
			final QueryResult result = this.operands[0].getQueryResult();

			// if the operator is in a cycle: it is no problem if the left operand gets new bindings...
			this.operands[0] = new ParallelIteratorMultipleQueryResults();

			for (final OperatorIDTuple opId : this.succeedingOperators) {
				opId.processAll(result);
			}
		}
		return msg;
	}

	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg, final DebugStep debugstep) {
		if (!this.operands[0].isEmpty() && !this.operands[1].isEmpty()) {
			final QueryResult result = QueryResult.createInstance();
			final Iterator<Bindings> iteratorLeftChild = this.operands[0].getQueryResult().oneTimeIterator();
			while (iteratorLeftChild.hasNext()) {
				final Bindings leftItem = iteratorLeftChild.next();
				boolean found = false;
				for (final Bindings rightItem : this.operands[1].getQueryResult()) {
					// compute intersection of the variable sets
					final Set<Variable> vars = rightItem.getVariableSet();
					vars.retainAll(leftItem.getVariableSet());

					// if intersection is empty then isEqual should always be false in the typical case (except for not in RIF rules),
					// workaround: check whether vars is empty
					if(vars.isEmpty() && this.considerEmptyIntersection){
						continue;
					}

					boolean isEqual = true;
					for (final Variable v : vars) {
						if ((v.getLiteral(leftItem).compareToNotNecessarilySPARQLSpecificationConform(v.getLiteral(rightItem))) != 0) {
							isEqual = false;
						}
					}

					if (isEqual){
						found = true;
						break;
					}
				}
				if (!found){
					result.add(leftItem);
				}
			}

			for (final OperatorIDTuple opId : this.succeedingOperators) {
				opId.processAllDebug(new QueryResultDebug(result, debugstep, this, opId.getOperator(), true), debugstep);
			}
		} else if (!this.operands[0].isEmpty()) { // happens when the group constraint which follows the minus is empty
			for (final OperatorIDTuple opId : this.succeedingOperators) {
				opId.processAllDebug(this.operands[0].getQueryResult(), debugstep);
			}
		}
		return msg;
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage msg_result = new BoundVariablesMessage(msg);
		Set<Variable> variableSet = null;
		// the variables, which are always bound are the intersection
		// variables of the left hand side
		for (final BasicOperator parent : this.getPrecedingOperators()) {
			final OperatorIDTuple opID = parent.getOperatorIDTuple(this);
			if (opID.getId() == 0) {
				if (variableSet == null) {
					variableSet = new HashSet<Variable>();
					if(parent.getIntersectionVariables()!=null){
						variableSet.addAll(parent.getIntersectionVariables());
					} else {
						System.err.println("Minus: Intersection variables of parent node not set!");
					}
				} else {
					if(parent.getIntersectionVariables()!=null){
						variableSet.retainAll(parent.getIntersectionVariables());
					} else {
						System.err.println("Minus: Intersection variables of parent node not set!");
					}
				}
			}
		}

		this.intersectionVariables = variableSet;

		msg_result.setVariables(this.intersectionVariables);
		return msg_result;
	}

	@Override
	public String toString(){
		return super.toString()+" "+this.intersectionVariables;
	}
}
