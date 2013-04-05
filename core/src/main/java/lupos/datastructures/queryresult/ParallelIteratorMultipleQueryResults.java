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
package lupos.datastructures.queryresult;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;

/**
 * This class collects QueryResults.
 * Afterwards it can be used to iterate through all the collected QueryResults.
 * It can be used to instantiate an IteratorQueryResult (and therefore serve as basis for a new QueryResult integrating all collected QueryResult, but iterating through them only once via oneTimeIterator())
 */
public class ParallelIteratorMultipleQueryResults implements ParallelIterator<Bindings>{
	
	protected List<QueryResult> queryResults = new LinkedList<QueryResult>();
	protected Iterator<QueryResult> currentQueryResult = null;
	protected Iterator<Bindings> currentIterator = null;
	
	public void addQueryResult(final QueryResult queryResult){
		if(this.currentQueryResult!=null){
			throw new RuntimeException("Adding a queryresult, but ParallelIteratorMultipleQueryResults is already used for iterating...");
		}
		this.queryResults.add(queryResult);
	}
	
	@Override
	public boolean hasNext() {
		if(this.currentIterator==null){
			if(this.currentQueryResult==null){
				this.currentQueryResult = this.queryResults.iterator();
			}
			if(!this.currentQueryResult.hasNext()){
				return false;
			}
			this.currentIterator = this.currentQueryResult.next().oneTimeIterator(); 
		}		
		while(!this.currentIterator.hasNext()){
			if(!this.currentQueryResult.hasNext()){
				return false;
			}
			this.close(); // only closes the current iterator (which is at the end...)
			this.currentIterator = this.currentQueryResult.next().oneTimeIterator();
		} 
		return true;
	}

	@Override
	public Bindings next() {
		if(this.hasNext()){
			return this.currentIterator.next();
		} else {
			return null;
		}
	}

	@Override
	public void remove() {
		if(this.currentIterator!=null){
			this.currentIterator.remove();
		}
	}

	@Override
	public void close() {
		if(this.currentIterator!=null){
			if(this.currentIterator instanceof ParallelIterator)
				((ParallelIterator<Bindings>)this.currentIterator).close();
		}
	}
	
	public boolean isEmpty(){
		return this.queryResults.isEmpty();
	}
	
	/**
	 * This method returns a queryresult integrating all collected queryresults.
	 * If only one queryresult has been collected, only this is returned.
	 * 
	 * @return a queryresult integrating all collected queryresults
	 */
	public QueryResult getQueryResult(){
		if(this.queryResults.size()==1){
			return this.queryResults.get(0);
		}
		return QueryResult.createInstance(this);
	}
	
	public void release(){
		for(QueryResult qr: this.queryResults){
			qr.release();
		}
	}
	
	public void removeAll(QueryResult queryResult){
		for(QueryResult qr: this.queryResults){
			qr.removeAll(queryResult);
		}
	}
	
	public void materialize(){
		for(QueryResult qr: this.queryResults){
			qr.materialize();
		}
	}
}
