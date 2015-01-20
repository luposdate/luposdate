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
 */
package lupos.engine.operators.stream;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleOperator;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.misc.debug.DebugStep;

public abstract class Window extends TripleOperator implements TripleDeleter {

	public PatternMatcher getPatternMatcher() {
		if (this.getSucceedingOperators().size() == 1) {
			final BasicOperator bo = this.getSucceedingOperators().get(0)
					.getOperator();
			if (bo instanceof PatternMatcher)
				return (PatternMatcher) bo;
		}
		System.out.println("Error in Window-Operator!");
		return null;
	}

	@Override
	public void consume(final Triple triple) {
		for (final OperatorIDTuple oid : this.getSucceedingOperators()) {
			((TripleConsumer) oid.getOperator()).consume(triple);
		}
	}

	@Override
	public void deleteTriple(final Triple triple) {
		for (final OperatorIDTuple oid : this.succeedingOperators) {
			((TripleDeleter) oid.getOperator()).deleteTriple(triple);
		}
	}
	
	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		for (final OperatorIDTuple oid : this.getSucceedingOperators()) {
			debugstep.step(this, oid.getOperator(), triple);
			((TripleOperator) oid.getOperator()).consume(triple);
		}
	}
	
	@Override
	public void deleteTripleDebug(final Triple triple, final DebugStep debugstep) {
		for (final OperatorIDTuple oid : this.succeedingOperators) {
			debugstep.stepDelete(this, oid.getOperator(), triple);
			((TripleDeleter) oid.getOperator()).deleteTripleDebug(triple,
					debugstep);
		}
	}
}
