
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.engine.operators.stream;

import java.util.Date;
import java.util.LinkedList;

import lupos.datastructures.items.TimestampedTriple;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.misc.debug.DebugStep;
import lupos.rdf.Prefix;
public class WindowInstancesNumber extends WindowInstances {

	private int numberOfInstances = 0;


	/**
	 * <p>Constructor for WindowInstancesNumber.</p>
	 *
	 * @param numberOfInstances a int.
	 * @param instanceClass a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public WindowInstancesNumber(final int numberOfInstances, Literal instanceClass) {
		super(instanceClass);
		if (numberOfInstances < 1) {
			System.err.println("X must be >=1 for WINDOW TYPE SLIDINGTRIPLES X");
			System.err.println("Assuming WINDOW TYPE SLIDINGTRIPLES 1...");
			this.numberOfInstances = 1;
		} else
			this.numberOfInstances = numberOfInstances;		
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final StartOfEvaluationMessage message) {
		this.tripleBuffer = new LinkedList<TimestampedTriple>();
		this.typeTripleBuffer = new LinkedList<TimestampedTriple>();
		return message;
	}

	/** {@inheritDoc} */
	@Override
	public void consume(final Triple triple) {
		final TimestampedTriple t = new TimestampedTriple(triple, (new Date()).getTime());
		
		if(isMatchingTypeTriple(t)) {
			// consume type-triple
			super.consume(t);
			
			// search for triples with same subject to consume
			for(Triple tmp : this.tripleBuffer) {
				if(haveSameSubject(tmp,t)) {
					super.consume(tmp);
				}
			}
			
			// add type triple to extra buffer			
			this.typeTripleBuffer.addLast(t);
			
			// keep only the last n type-triples
			if(this.typeTripleBuffer.size() > this.numberOfInstances) {
				TimestampedTriple removedTypeTriple = this.typeTripleBuffer.removeFirst();
				
				// delete old triples
				// 1. delete instance of the removed type-triple
				this.deleteInstance(removedTypeTriple);
				// 2. delete all triples that are at least as old as the removed type-triple
				while(!this.tripleBuffer.isEmpty() && this.tripleBuffer.peekFirst().getTimestamp() <= removedTypeTriple.getTimestamp()) {
					TimestampedTriple tmp = this.tripleBuffer.removeFirst();
					//System.out.println("delete: " + tmp.toString());
					super.deleteTriple(tmp);
				}
			}		
		} else {
			// consume triple if a type-triple with same subject exists
			for(Triple tmp : this.typeTripleBuffer) {
				if(haveSameSubject(tmp,t)) {
					//System.out.println("consume: " + t.toString());
					super.consume(t);
					break;
				}
			}
		}

		this.tripleBuffer.addLast(t);	
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		final TimestampedTriple t = new TimestampedTriple(triple, (new Date()).getTime());
		
		if(isMatchingTypeTriple(t)) {
			// consume type-triple
			super.consumeDebug(t, debugstep);
			
			// search for triples with same subject to consume
			for(Triple tmp : this.tripleBuffer) {
				if(haveSameSubject(tmp,t)) {
					super.consumeDebug(tmp, debugstep);
				}
			}
			
			// add type triple to extra buffer			
			this.typeTripleBuffer.addLast(t);
			
			// keep only the last n type-triples
			if(this.typeTripleBuffer.size() > this.numberOfInstances) {
				TimestampedTriple removedTypeTriple = this.typeTripleBuffer.removeFirst();
				
				// delete old triples
				// 1. delete instance of the removed type-triple
				this.deleteInstanceDebug(removedTypeTriple, debugstep);
				// 2. delete all triples that are at least as old as the removed type-triple
				while(!this.tripleBuffer.isEmpty() && this.tripleBuffer.peekFirst().getTimestamp() <= removedTypeTriple.getTimestamp()) {
					TimestampedTriple tmp = this.tripleBuffer.removeFirst();
					//System.out.println("delete: " + tmp.toString());
					super.deleteTripleDebug(tmp, debugstep);
				}
			}		
		} else {
			// consume triple if a type-triple with same subject exists
			for(Triple tmp : this.typeTripleBuffer) {
				if(haveSameSubject(tmp,t)) {
					//System.out.println("consume: " + t.toString());
					super.consumeDebug(t, debugstep);
					break;
				}
			}
		}

		this.tripleBuffer.addLast(t);	
	}
			
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return super.toString() + this.numberOfInstances;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString(Prefix prefixInstance) {
		return super.toString(prefixInstance) + " " + this.numberOfInstances;
	}
}
