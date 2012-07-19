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
package lupos.engine.operators.stream;

import java.util.Date;

import lupos.datastructures.items.TimestampedTriple;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.misc.debug.DebugStep;
import lupos.rdf.Prefix;

public class WindowInstancesNumber extends WindowInstances {

	private int numberOfTriples = 0;
	// ring buffer for storing the triples of the window:
	private TimestampedTriple[] triplesInWindow;
	private int start = -1;
	private int end = 0;

	public WindowInstancesNumber(final int numberOfTriples, Literal instanceClass) {
		super(instanceClass);
		if (numberOfTriples < 1) {
			System.err
					.println("X must be >=1 for WINDOW TYPE SLIDINGTRIPLES X");
			System.err.println("Assuming WINDOW TYPE SLIDINGTRIPLES 1...");
			this.numberOfTriples = 1;
		} else
			this.numberOfTriples = numberOfTriples;		
		// equal comparison between literals: this.instanceClass.compareToNotNecessarilySPARQLSpecificationConform(otherLiteral)==0
	}

	@Override
	public Message preProcessMessage(final StartOfEvaluationMessage message) {
		triplesInWindow = new TimestampedTriple[numberOfTriples];
		start = -1;
		end = 0;
		return message;
	}

	@Override
	public void consume(final Triple triple) {
		if (end == start) {
			// ring buffer is full
			this.deleteTriple(triplesInWindow[start]);
			start = (start + 1) % numberOfTriples;
		} else {
			if (start == -1)
				start = 0;
		}
		final TimestampedTriple t = new TimestampedTriple(triple, (new Date())
				.getTime());
		triplesInWindow[end] = t;
		end = (end + 1) % numberOfTriples;
		super.consume(t);
	}
	
	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		if (end == start) {
			// ring buffer is full
			this.deleteTripleDebug(triplesInWindow[start], debugstep);
			start = (start + 1) % numberOfTriples;
		} else {
			if (start == -1)
				start = 0;
		}
		final TimestampedTriple t = new TimestampedTriple(triple, (new Date())
				.getTime());
		triplesInWindow[end] = t;
		end = (end + 1) % numberOfTriples;
		super.consumeDebug(t, debugstep);
	}

	@Override
	public String toString() {
		return super.toString() + numberOfTriples;
	}
	
	@Override
	public String toString(Prefix prefixInstance) {
		return super.toString(prefixInstance) + " " + numberOfTriples;
	}
}
