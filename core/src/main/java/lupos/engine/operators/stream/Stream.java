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
package lupos.engine.operators.stream;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.TimestampedTriple;
import lupos.datastructures.items.Triple;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.application.CollectResult;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleOperator;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.misc.debug.DebugStep;

public abstract class Stream extends TripleOperator {

	protected LinkedList<NotifyStreamResult> notifyStreamResults = new LinkedList<NotifyStreamResult>();
	protected CollectResult collectResult;

	public Stream(final CollectResult cr) {
		this.collectResult = cr;
	}

	public List<PatternMatcher> getPatternMatchers() {
		final LinkedList<PatternMatcher> llpm = new LinkedList<PatternMatcher>();
		for (final OperatorIDTuple oid : this.getSucceedingOperators()) {
			if (oid.getOperator() instanceof PatternMatcher)
				llpm.add((PatternMatcher) oid.getOperator());
			else if (oid.getOperator() instanceof Window) {
				llpm.add(((Window) oid.getOperator()).getPatternMatcher());
			}
		}
		return llpm;
	}

	@Override
	public void consume(final Triple triple) {
		for (final OperatorIDTuple oid : this.getSucceedingOperators()) {
			((TripleConsumer) oid.getOperator()).consume(new TimestampedTriple(
					triple, (new Date()).getTime()));
		}
	}

	public void addNotifyStreamResult(
			final NotifyStreamResult notifyStreamResult) {
		this.notifyStreamResults.add(notifyStreamResult);
	}

	protected void notifyStreamResults() {
		this.sendMessage(new ComputeIntermediateResultMessage());
		for (final NotifyStreamResult nsr : this.notifyStreamResults)
			nsr.notifyStreamResult(this.collectResult.getResult());
	}
	
	protected void notifyStreamResultsDebug(DebugStep debugstep) {
		this.sendMessageDebug(new ComputeIntermediateResultMessage(),debugstep);
		for (final NotifyStreamResult nsr : this.notifyStreamResults){
			nsr.notifyStreamResult(this.collectResult.getResult());
		}
	}
	
	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		for (final OperatorIDTuple oid : this.getSucceedingOperators()) {
			final TimestampedTriple timestampedTriple = new TimestampedTriple(
					triple, (new Date()).getTime());
			debugstep.step(this, oid.getOperator(), timestampedTriple);
			((TripleOperator) oid.getOperator()).consumeDebug(
					timestampedTriple, debugstep);
		}
	}
	
	public static Stream createDebugInstance(final Stream stream,
			final DebugStep debugstep) {
		if (stream instanceof StreamTriples) {
			return new StreamTriplesDebug((StreamTriples) stream, debugstep);
		} else if (stream instanceof StreamDuration) {
			return new StreamDurationDebug((StreamDuration) stream, debugstep);
		} else {
			System.err
					.println("StreamTriples or StreamDuration class expected, but got "
							+ stream.getClass());
			return null;
		}
	}
}