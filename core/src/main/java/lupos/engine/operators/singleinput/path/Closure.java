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
import java.util.Map;
import java.util.Set;

import lupos.datastructures.smallerinmemorylargerondisk.ReachabilityMap;
import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsCollection;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.smallerinmemorylargerondisk.SetImplementation;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.SingleInputOperator;

/**
 * This operator determines the transitive closure (applied in property paths for * and +) 
 */
public class Closure extends SingleInputOperator{
	
	private static final long serialVersionUID = 1L;
	private Variable subject;
	private Variable object;
	private Set<Bindings> closure;
	private ReachabilityMap<Literal,SetImplementation<Literal>> reachabilityMap;
	private Set<Literal> allowedSubjects;
	private Set<Literal> allowedObjects;
	
	public Closure(Variable subject, Variable object){
		this.subject = subject;
		this.object = object;
	}
	
	public Closure(Variable subject, Variable object, Set<Literal> allowedSubjects, Set<Literal> allowedObjects){
		this.subject = subject;
		this.object = object;
		this.allowedSubjects = allowedSubjects;
		this.allowedObjects = allowedObjects;
	}
	
	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		if(shouldBeLeftCalculation()){
			calculateFromLeft();
		}
		else{
			calculateFromRight();
		}
		QueryResult result = createQueryResult();
		if (result != null) {
			for (final OperatorIDTuple opId : this.succeedingOperators) {
				opId.processAll(result);
			}
			result.release();
		}		
		this.closure = null;
		this.reachabilityMap = null;
		return msg;
	}
	
	private QueryResult createQueryResult(){
		QueryResult result = QueryResult.createInstance();
		Iterator<Bindings> itb = this.closure.iterator();
		while(itb.hasNext()){
			Bindings bind = itb.next();
			result.add(bind);
		}
		return result;
	}
	
	@Override
	public String toString(){		
		String result = "Closure of "+this.subject+" -> "+this.object;
		if(this.reachabilityMap!=null){
			result += "\n";
			Iterator<Map.Entry<Literal,SetImplementation<Literal>>> it= this.reachabilityMap.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<Literal, SetImplementation<Literal>> entry = it.next();
				result+="Map for " + entry.getKey() + ": ";
				Iterator<Literal> it2 = entry.getValue().iterator();
				while(it2.hasNext()){
					result+=it2.next() + " ";
				}
				result+="\n";
			}
		}
		return result;
	}
	
	@Override
	public synchronized QueryResult process(final QueryResult bindings, final int operandID) {
		updateClosureAndMap(bindings);
		// bindings.release();
		return null;
	}
	
	private void calculateFromLeft(){
		SetImplementation<Bindings> lastIteration = new SetImplementation<Bindings>();
		lastIteration.addAll(this.closure);
		while(!lastIteration.isEmpty()){
			SetImplementation<Bindings> thisIteration = new SetImplementation<Bindings>();
			Iterator<Bindings> itsubject = lastIteration.iterator();
			while (itsubject.hasNext()){
				Bindings closureElement = itsubject.next();
				if(this.allowedSubjects==null || this.allowedSubjects.contains(closureElement.get(this.subject))){
					Set<Literal> potentialNewBindings = this.reachabilityMap.get(closureElement.get(this.object));
	
					if (potentialNewBindings != null){	
						SetImplementation<Literal> updatedMapEntry= new SetImplementation<Literal>(this.reachabilityMap.get(closureElement.get(this.subject)));
						Iterator<Literal> itobject = potentialNewBindings.iterator();
						while(itobject.hasNext()){
							Literal tempObject = itobject.next();
							Bindings newBind = new BindingsCollection();
							newBind.add(this.subject, closureElement.get(this.subject));
							newBind.add(this.object, tempObject);
							if(this.closure.add(newBind)){
								thisIteration.add(newBind);
								updatedMapEntry.add(tempObject);
							}
						}
						potentialNewBindings=null;
						this.reachabilityMap.put(closureElement.get(this.subject), updatedMapEntry);
					}
				}
			}
			lastIteration = thisIteration;
		}
	}
	
	private void calculateFromRight(){
		SetImplementation<Bindings> lastIteration = new SetImplementation<Bindings>();
		lastIteration.addAll(this.closure);
		while(!lastIteration.isEmpty()){
			SetImplementation<Bindings> thisIteration = new SetImplementation<Bindings>();
			Iterator<Bindings> itobject = lastIteration.iterator();
			while (itobject.hasNext()){
				Bindings closureElement = itobject.next();
				if(this.allowedObjects==null || this.allowedObjects.contains(closureElement.get(this.object))){
					Set<Literal> potentialNewBindings = this.reachabilityMap.get(closureElement.get(this.subject));
					if (potentialNewBindings != null){	
						SetImplementation<Literal> updatedMapEntry= new SetImplementation<Literal>(this.reachabilityMap.get(closureElement.get(this.object)));
						Iterator<Literal> itsubject = potentialNewBindings.iterator();
						while(itsubject.hasNext()){
							Literal tempSubject = itsubject.next();
							Bindings newBind = new BindingsCollection();
							newBind.add(this.subject, tempSubject);
							newBind.add(this.object, closureElement.get(this.object));
							if (this.closure.add(newBind)){
								thisIteration.add(newBind);
								updatedMapEntry.add(tempSubject);
							}
						}
						potentialNewBindings=null;
						this.reachabilityMap.put(closureElement.get(this.object), updatedMapEntry);
					}
				}
			}
			lastIteration = thisIteration;
		}
	}
	
	private void updateClosureAndMap(QueryResult bindings){
		if(this.closure==null){
			this.closure = new SetImplementation<Bindings>();
		}
		if(this.reachabilityMap == null){
			this.reachabilityMap = new ReachabilityMap<Literal, SetImplementation<Literal>>();
		}
	
		final Iterator<Bindings> itb = bindings.oneTimeIterator();
		while(itb.hasNext()){
			Bindings closureElement = itb.next();
			if(shouldBeLeftCalculation()){	
				SetImplementation<Literal> newEntry = this.reachabilityMap.get(closureElement.get(this.subject));
				if(newEntry==null){
					newEntry = new SetImplementation<Literal>();
				}
				newEntry.add(closureElement.get(this.object));
				this.reachabilityMap.put(closureElement.get(this.subject), newEntry);
			}
			else{
				SetImplementation<Literal> newEntry = this.reachabilityMap.get(closureElement.get(this.object));
				if(newEntry==null){
					newEntry = new SetImplementation<Literal>();
				}
				newEntry.add(closureElement.get(this.subject));
				this.reachabilityMap.put(closureElement.get(this.object), newEntry);
			}
			this.closure.add(closureElement);
		}
	}
	
	private boolean shouldBeLeftCalculation(){
		return this.allowedObjects==null || (this.allowedSubjects!=null && this.allowedSubjects.size()<=this.allowedObjects.size());
	}

	protected ParallelIterator<Bindings> getIterator() {
		final Iterator<Bindings> itb = this.closure.iterator();
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
}
