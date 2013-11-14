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
package lupos.engine.operators.singleinput.path;


import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsCollection;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.datastructures.smallerinmemorylargerondisk.ReachabilityMap;
import lupos.datastructures.smallerinmemorylargerondisk.SetImplementation;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.misc.Tuple;
import lupos.misc.debug.DebugStep;
import lupos.rdf.Prefix;

/**
 * This operator determines the transitive closure (applied in property paths for * and +)
 */
public class Closure extends SingleInputOperator{

	private static final long serialVersionUID = 1L;
	private final Variable subject;
	private final Variable object;
	private Set<Literal> allowedSubjects;
	private Set<Literal> allowedObjects;
	private QueryResult operand = null;

	public Closure(final Variable subject, final Variable object){
		this.subject = subject;
		this.object = object;
	}

	public Closure(final Variable subject, final Variable object, final Set<Literal> allowedSubjects, final Set<Literal> allowedObjects){
		this.subject = subject;
		this.object = object;
		this.allowedSubjects = allowedSubjects;
		this.allowedObjects = allowedObjects;
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		this.computeResult();
		return msg;
	}

	public void computeResult(){
		if(this.operand!=null){
			final QueryResult result = this.getClosure(this.operand);
			if(result!=null){
				for (final OperatorIDTuple opId : this.succeedingOperators) {
					opId.processAll(result);
				}
				result.release();
			}
		}
	}

	public QueryResult getClosure(final QueryResult input){
		final Tuple<SetImplementation<Bindings>, ReachabilityMap<Literal, SetImplementation<Literal>>> closureAndReachabilityMap = this.generateClosureAndMap(input);
		final SetImplementation<Bindings> closure = closureAndReachabilityMap.getFirst();
		if(this.shouldBeLeftCalculation()){
			this.calculateFromLeft(closure, closureAndReachabilityMap.getSecond());
		} else {
			this.calculateFromRight(closure, closureAndReachabilityMap.getSecond());
		}
		if (closure != null) {
			return QueryResult.createInstance(closure.iterator());
		} else {
			return null;
		}
	}

	@Override
	public String toString(){
		String result = "Closure of "+this.subject+" -> "+this.object;
		if(this.allowedSubjects!=null){
			result+="\nAllowed Subjects: "+this.allowedSubjects;
		}
		if(this.allowedObjects!=null){
			result+="\nAllowed Objects: "+this.allowedObjects;
		}
		return result;
	}

	@Override
	public String toString(final Prefix prefix){
		String result = "Closure of "+this.subject+" -> "+this.object;
		if(this.allowedSubjects!=null){
			result+="\nAllowed Subjects: " + Closure.setWitPrefix(this.allowedSubjects, prefix);
		}
		if(this.allowedObjects!=null){
			result+="\nAllowed Objects: " + Closure.setWitPrefix(this.allowedObjects, prefix);
		}
		return result;
	}

	public static String setWitPrefix(final Set<Literal> literals, final Prefix prefix){
		final StringBuilder result = new StringBuilder();
		result.append("[");
		boolean firstTime = true;
		for(final Literal literal: literals){
			if(firstTime){
				firstTime=false;
			} else {
				result.append(", ");
			}
			result.append(literal.toString(prefix));
		}
		result.append("]");
		return result.toString();
	}

	@Override
	public synchronized QueryResult process(final QueryResult bindings, final int operandID) {
		if(this.operand!=null){
			final Iterator<Bindings> it = bindings.oneTimeIterator();
			while(it.hasNext()){
				this.operand.add(it.next());
			}
			if(it instanceof ParallelIterator){
				((ParallelIterator<Bindings>) it).close();
			}
		} else {
			this.operand = bindings;
		}
		return null;
	}

	private void calculateFromLeft(final SetImplementation<Bindings> closure, final ReachabilityMap<Literal, SetImplementation<Literal>> reachabilityMap){
		if(closure!=null){
			SetImplementation<Bindings> lastIteration = new SetImplementation<Bindings>();
			lastIteration.addAll(closure);
			while(!lastIteration.isEmpty()){
				final SetImplementation<Bindings> thisIteration = new SetImplementation<Bindings>();
				final Iterator<Bindings> itsubject = lastIteration.iterator();
				while (itsubject.hasNext()){
					final Bindings closureElement = itsubject.next();
					if(this.allowedSubjects==null || this.allowedSubjects.contains(closureElement.get(this.subject))){
						Set<Literal> potentialNewBindings = reachabilityMap.get(closureElement.get(this.object));

						if (potentialNewBindings != null){
							final SetImplementation<Literal> updatedMapEntry= new SetImplementation<Literal>(reachabilityMap.get(closureElement.get(this.subject)));
							final Iterator<Literal> itobject = potentialNewBindings.iterator();
							while(itobject.hasNext()){
								final Literal tempObject = itobject.next();
								final Bindings newBind = new BindingsCollection();
								newBind.add(this.subject, closureElement.get(this.subject));
								newBind.add(this.object, tempObject);
								if(closure.add(newBind)){
									thisIteration.add(newBind);
									updatedMapEntry.add(tempObject);
								}
							}
							potentialNewBindings=null;
							reachabilityMap.put(closureElement.get(this.subject), updatedMapEntry);
						}
					}
				}
				lastIteration = thisIteration;
			}
		}
	}

	private void calculateFromRight(final SetImplementation<Bindings> closure, final ReachabilityMap<Literal, SetImplementation<Literal>> reachabilityMap){
		if(closure!=null){
			SetImplementation<Bindings> lastIteration = new SetImplementation<Bindings>();
			lastIteration.addAll(closure);
			while(!lastIteration.isEmpty()){
				final SetImplementation<Bindings> thisIteration = new SetImplementation<Bindings>();
				final Iterator<Bindings> itobject = lastIteration.iterator();
				while (itobject.hasNext()){
					final Bindings closureElement = itobject.next();
					if(this.allowedObjects==null || this.allowedObjects.contains(closureElement.get(this.object))){
						Set<Literal> potentialNewBindings = reachabilityMap.get(closureElement.get(this.subject));
						if (potentialNewBindings != null){
							final SetImplementation<Literal> updatedMapEntry= new SetImplementation<Literal>(reachabilityMap.get(closureElement.get(this.object)));
							final Iterator<Literal> itsubject = potentialNewBindings.iterator();
							while(itsubject.hasNext()){
								final Literal tempSubject = itsubject.next();
								final Bindings newBind = new BindingsCollection();
								newBind.add(this.subject, tempSubject);
								newBind.add(this.object, closureElement.get(this.object));
								if (closure.add(newBind)){
									thisIteration.add(newBind);
									updatedMapEntry.add(tempSubject);
								}
							}
							potentialNewBindings=null;
							reachabilityMap.put(closureElement.get(this.object), updatedMapEntry);
						}
					}
				}
				lastIteration = thisIteration;
			}
		}
	}

	private Tuple<SetImplementation<Bindings>, ReachabilityMap<Literal, SetImplementation<Literal>>> generateClosureAndMap(final QueryResult bindings){
		final SetImplementation<Bindings> closure = new SetImplementation<Bindings>();
		final ReachabilityMap<Literal, SetImplementation<Literal>> reachabilityMap = new ReachabilityMap<Literal, SetImplementation<Literal>>();

		final Iterator<Bindings> itb = bindings.oneTimeIterator();
		while(itb.hasNext()){
			final Bindings closureElement = itb.next();
			if(this.shouldBeLeftCalculation()){
				SetImplementation<Literal> newEntry = reachabilityMap.get(closureElement.get(this.subject));
				if(newEntry==null){
					newEntry = new SetImplementation<Literal>();
				}
				newEntry.add(closureElement.get(this.object));
				reachabilityMap.put(closureElement.get(this.subject), newEntry);
			}
			else{
				SetImplementation<Literal> newEntry = reachabilityMap.get(closureElement.get(this.object));
				if(newEntry==null){
					newEntry = new SetImplementation<Literal>();
				}
				newEntry.add(closureElement.get(this.subject));
				reachabilityMap.put(closureElement.get(this.object), newEntry);
			}
			closure.add(closureElement);
		}
		return new Tuple<SetImplementation<Bindings>, ReachabilityMap<Literal, SetImplementation<Literal>>>(closure, reachabilityMap);
	}

	private boolean shouldBeLeftCalculation(){
		return this.allowedObjects==null || (this.allowedSubjects!=null && this.allowedSubjects.size()<=this.allowedObjects.size());
	}

	protected ParallelIterator<Bindings> getIterator() {
		if(this.operand == null){
			return new ParallelIterator<Bindings>() {
				@Override
				public boolean hasNext() {
					return false;
				}
				@Override
				public Bindings next() {
					return null;
				}
				@Override
				public void remove() {
				}
				@Override
				public void close() {
				}
			};
		}

		final QueryResult result = this.getClosure(this.operand);

		final Iterator<Bindings> itb = result.iterator();
		return new ParallelIterator<Bindings>() {

			@Override
			public void close() {
				// derived classes may override the above method in order to
				// release some resources here!
			}

			@Override
			public boolean hasNext() {
				return itb.hasNext();
			}

			@Override
			public Bindings next() {
				return itb.next();
			}

			@Override
			public void remove() {
				itb.remove();
			}

		};
	}

	@Override
	protected boolean isPipelineBreaker() {
		return true;
	}

	@Override
	public QueryResult deleteQueryResult(final QueryResult queryResult, final int operandID) {
		final Iterator<Bindings> itb = queryResult.oneTimeIterator();
		while (itb.hasNext()){
			this.operand.remove(itb.next());
		}
		return null;
	}

	@Override
	public void deleteQueryResult(final int operandID) {
		this.operand.release();
		this.operand = null;
	}

	@Override
	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		this.computeResult();
		return msg;
	}


	@Override
	public Message preProcessMessageDebug(final ComputeIntermediateResultMessage msg, final DebugStep debugstep) {
		this.computeDebugStep(debugstep);
		return msg;
	}

	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg, final DebugStep debugstep) {
		this.computeDebugStep(debugstep);
		return msg;
	}

	public void computeDebugStep(final DebugStep debugstep){
		final QueryResult qr = QueryResult.createInstance(this.getIterator());
		if (this.succeedingOperators.size() > 1){
			qr.materialize();
		}
		for (final OperatorIDTuple opId : this.succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(qr, debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId.getId(), debugstep);
		}
	}
}
