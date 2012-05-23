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
package lupos.engine.operators.tripleoperator.patternmatcher;

import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.engine.operators.stream.TripleDeleter;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleConsumerDebug;
import lupos.engine.operators.tripleoperator.TripleOperator;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.debug.DebugStep;

public class PatternMatcher extends TripleOperator implements TripleConsumer,
		TripleDeleter {

	private static final long serialVersionUID = 6553461929216505005L;

	public PatternMatcher() {
	}

	public PatternMatcher(final TripleConsumer[] operators) {
		set(operators);
	}

	public void set(final TripleConsumer[] operators) {
		final List<OperatorIDTuple> succeedingOperators = new LinkedList<OperatorIDTuple>();
		int i = 0;
		for (final TripleConsumer o : operators)
			succeedingOperators.add(new OperatorIDTuple((BasicOperator)o, i++));

		setSucceedingOperators(succeedingOperators);
	}

	public TriplePattern[] getTriplePatterns() {
		final TripleConsumer[] o = getOperators();
		int numberTPs = 0;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof TriplePattern)
				numberTPs++;
		}
		int index = 0;
		final TriplePattern[] tp = new TriplePattern[numberTPs];
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof TriplePattern)
				tp[index++] = (TriplePattern) o[i];
		}
		return tp;
	}

	public TripleConsumer[] getOperators() {
		final TripleConsumer[] o = new TripleConsumer[getSucceedingOperators().size()];
		int i = 0;
		for (final OperatorIDTuple oit : getSucceedingOperators())
			o[i++] = (TripleConsumer) oit.getOperator();
		return o;
	}
	
	@Override
	public void cloneFrom(final BasicOperator op) {
		super.cloneFrom(op);
		set(((PatternMatcher) op).getOperators());
	}

	@Override
	public void consume(final Triple triple) {
		throw (new UnsupportedOperationException("This Operator(" + this
				+ ") should have been replaced before being used."));
	}

	public void add(final TriplePattern tp) {
		addSucceedingOperator(new OperatorIDTuple(tp, 0));
	}

	public void deleteTriple(final Triple triple) {
		for (final OperatorIDTuple opOuter : getSucceedingOperators()) {
			((TriplePattern) opOuter.getOperator()).deleteTriple(triple);
		}
	}
	
	public void deleteTripleDebug(final Triple triple, final DebugStep debugstep) {
		for (final OperatorIDTuple opOuter : getSucceedingOperators()) {
			debugstep.stepDelete(this, opOuter.getOperator(), triple);
			((TriplePattern) opOuter.getOperator()).deleteTripleDebug(triple,
					debugstep);
		}
	}
	
	public static PatternMatcher createDebugInstance(final PatternMatcher pm,
			final DebugStep debugstep) {
		if (pm instanceof RDFSSimplePatternMatcher)
			return new RDFSSimplePatternMatcherDebug(
					(RDFSSimplePatternMatcher) pm, debugstep);
		else if (pm instanceof SimplePatternMatcher)
			return new SimplePatternMatcherDebug((SimplePatternMatcher) pm,
					debugstep);
		else if (pm instanceof HashPatternMatcher)
			return new HashPatternMatcherDebug((HashPatternMatcher) pm,
					debugstep);
		return null;
	}
	
	@Override
	public Message postProcessMessage(final StartOfEvaluationMessage msg) {
		Triple dummyTriple = new Triple();
		for(TripleConsumer to: this.getOperators()){
			if(!(to instanceof TriplePattern)){
				to.consume(dummyTriple);
			}
		}
		return msg;
	}
	
	@Override
	public Message postProcessMessageDebug(final StartOfEvaluationMessage msg, DebugStep debugstep) {
		Triple dummyTriple = new Triple();
		for(TripleConsumer to: this.getOperators()){
			if(!(to instanceof TriplePattern)){
				debugstep.step(this,(BasicOperator)to, dummyTriple);
				((TripleConsumerDebug)to).consumeDebug(dummyTriple, debugstep);
			}
		}
		return msg;
	}
}