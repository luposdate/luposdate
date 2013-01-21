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
package lupos.engine.operators.singleinput.sort;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.singleinput.SingleInputOperator;

/**
 * The Collector-Class collects incomming Bindings or QueryResults and forwards 
 * this collection of bindings to the current in forward stored class, 
 * if it is larger than a given limit. 
 * Be sure to call forward at the end of inputstream (look at forward describtion), as it
 * has a quite similar importance like flush() using an Stream.
 */
public class Collector extends SingleInputOperator {

	private QueryResult store = QueryResult.createInstance();
	private final int limit;	
	private SingleInputOperator forward;
	private final int operandID;
	/**
	 * @param inp, a SingleInputOperator who will get the forwarded QueryResults
	 * @param limit, the minimal number of bindings at wich they will be forwarded<br>
	 * if limit < 0 the public method forward has to be called to forward bindings
	 * @param operandID...
	 */
	public Collector( SingleInputOperator inp, int limit, int operandID ) {
		forward = inp;
		this.limit = limit;
		this.operandID = operandID;
	}
	
	/**
	 * Adds a binding to the list wich will be forwarded soon
	 * @param b Bindings to add
	 * @return QueryResult, which has been processed by forward if limit has been reached <br>
	 * else null
	 */
	public QueryResult addBinding( Bindings b ){
		store.add( b );
		if( limit > 0 && store.size() > limit ){
			QueryResult ret = forward.process( store , operandID);
			store.reset();
			return ret;
		}
		return null;
	}
	
	/**
	 * Adds more than one Binding to the list which will be processed
	 * @param qr QueryResult to add
	 * @return processed QueryResult, if limit has been reached<br>
	 * null if not
	 */
	public QueryResult addAll( QueryResult qr ){
		store.addAll( qr );
		if( limit > 0 && store.size() > limit ){
			QueryResult ret = forward.process( store, operandID );
			store.reset();
			return ret;
		}
		return null;
	}
	
	/**
	 * forces to forward the current holded list of Bindings to the forward operator, even if
	 * limit has not been reached. DO this at the end of every inputstream as you propably have something
	 * stored here, what has not been processed, but as end of inputStream has been reached, limit will 
	 * never be reached => no processing.
	 * @return processed QueryResult
	 */
	public QueryResult forward(){
		QueryResult ret = forward.process( store, operandID );
		store.reset();
		return ret;
	}

}
