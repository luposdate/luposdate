/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.engine.operators.multiinput.optional;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.rdf.Prefix;

public class BasicIndexOptional extends Optional {

	protected BasicIndexScan indexScanOperator;
	protected Item rdfGraph;

	public void setBasicIndex(final BasicIndexScan indexScanOperator){
		this.indexScanOperator = indexScanOperator;
		this.rdfGraph = BasicIndexOptional.this.indexScanOperator.getGraphConstraint();
	}
	
	@Override
	public QueryResult process(QueryResult bindings, int operandID){
		final Iterator<Bindings> it = bindings.oneTimeIterator();
		return QueryResult.createInstance(new ParallelIterator<Bindings>(){
			
			ParallelIterator<Bindings> localIterator = this.computeNextIterator();

			@Override
			public boolean hasNext() {
				if(this.localIterator == null){
					return false;
				} else if(this.localIterator.hasNext()){
					return true;
				} else {
					return it.hasNext();
				}
			}

			@Override
			public Bindings next() {
				if(this.localIterator == null){
					return null;
				} else if(!this.localIterator.hasNext()){
					this.localIterator.close();
					this.localIterator = this.computeNextIterator();
					if(this.localIterator == null){
						return null;
					}
				}
				return this.localIterator.next();
			}
			
			public ParallelIterator<Bindings> computeNextIterator(){
				final Bindings currentBindings = it.next();
				if(currentBindings==null){
					return null;
				}
				if(BasicIndexOptional.this.rdfGraph!=null && BasicIndexOptional.this.rdfGraph.isVariable()){
					Literal result = currentBindings.get((Variable)BasicIndexOptional.this.rdfGraph);
					if(result!=null){
						BasicIndexOptional.this.indexScanOperator.setGraphConstraint(result);
					} else {
						BasicIndexOptional.this.indexScanOperator.setGraphConstraint(BasicIndexOptional.this.rdfGraph);
					}
				}
				
				Collection<TriplePattern> tps = BasicIndexOptional.this.indexScanOperator.getTriplePattern();
				LinkedList<TriplePattern> tps_new = new LinkedList<TriplePattern>();
				for(TriplePattern tp: tps){
					Item[] items = new Item[3];
					for(int i=0; i<3; i++){
						Item currentItem = tp.getPos(i);
						if(currentItem.isVariable()){
							Literal result = currentBindings.get((Variable)currentItem);
							if(result!=null){
								items[i] = result;
							} else {
								items[i] = currentItem;
							}
						} else {
							items[i] = currentItem; 
						}
					}
					tps_new.add(new TriplePattern(items));					
				}
				BasicIndexOptional.this.indexScanOperator.setTriplePatterns(tps_new);
				if(BasicIndexOptional.this.indexScanOperator instanceof RDF3XIndexScan){
					((RDF3XIndexScan)BasicIndexOptional.this.indexScanOperator).setCollationOrder((Collection<Variable>)null);
				}
				final QueryResult queryResult = BasicIndexOptional.this.indexScanOperator.join(BasicIndexOptional.this.indexScanOperator.getIndexCollection().dataset);
				BasicIndexOptional.this.indexScanOperator.setTriplePatterns(tps);
				if(queryResult==null || queryResult.size()==0){
					return new ParallelIterator<Bindings>(){
						boolean sent=false;
						@Override
						public boolean hasNext() {
							return !this.sent;
						}
						@Override
						public Bindings next() {
							if(this.hasNext()){
								this.sent=true;
								return currentBindings;
							} else {
								return null;
							}
						}
						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}
						@Override
						public void close() {
							// nothing to do...
						}						
					};
				} else {
					return new AddConstantBindingParallelIterator(currentBindings, queryResult.oneTimeIterator());
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close() {				
				if(it instanceof ParallelIterator){
					((ParallelIterator<Bindings>)it).close();
				}
			}
			
			@Override
			public void finalize(){
				this.close();
			}			
		});
	}
	
	public static class AddConstantBindingParallelIterator implements ParallelIterator<Bindings>{

		protected final Bindings bindingsToAdd;
		protected final Iterator<Bindings> originalIterator;
		protected Bindings next = null;
		
		public AddConstantBindingParallelIterator(final Bindings bindingsToAdd, final Iterator<Bindings> originalIterator){
			this.bindingsToAdd = bindingsToAdd;
			this.originalIterator = originalIterator;
		}

		@Override
		public boolean hasNext() {
			if(this.next!=null)
				return true;
			this.next = computeNext();
			return (this.next!=null);
		}

		@Override
		public Bindings next() {
			if(this.next!=null){
				Bindings znext = this.next;
				this.next = null;
				return znext;
			} else {
				return computeNext();			
			}
		}
		
		public Bindings computeNext(){
			Bindings inter;
			boolean flag;
			do {
				flag = false;
				inter = this.originalIterator.next();
				if(inter==null)
					return null;
				for(Variable v: this.bindingsToAdd.getVariableSet()){
					Literal literal = inter.get(v);
					if(literal!=null){
						flag = (literal.compareToNotNecessarilySPARQLSpecificationConform(this.bindingsToAdd.get(v))!=0);
					} 
					inter.add(v, this.bindingsToAdd.get(v));
				}
				inter.addAllTriples(this.bindingsToAdd.getTriples());
			} while(flag);
			return inter;
		}

		@Override
		public void remove() {
			this.originalIterator.remove();
		}

		@Override
		public void close() {
			if(this.originalIterator instanceof ParallelIterator){
				((ParallelIterator<Bindings>)this.originalIterator).close();
			}
		}
		@Override
		public void finalize(){
			this.close();
		}
	}
	
	@Override
	public String toString(){
		return super.toString() + " on " +this.indexScanOperator.getTriplePattern();
	}
	
	@Override
	public String toString(Prefix prefixInstance){
		String result = "";
		for(TriplePattern tp: this.indexScanOperator.getTriplePattern()){
			if(result.length()>0){
				result+=", ";
			}
			result += tp.toString(prefixInstance);
		}
		return super.toString() + " on [" + result + "]";
	}
}
