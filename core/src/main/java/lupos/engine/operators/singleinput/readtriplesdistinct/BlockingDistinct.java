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
package lupos.engine.operators.singleinput.readtriplesdistinct;

import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.debug.DebugStep;

public class BlockingDistinct extends ReadTriplesDistinct {

	protected Set<BindingsArrayReadTriples> bindings;

	public BlockingDistinct(final Set<BindingsArrayReadTriples> bindings) {
		this.bindings = bindings;
	}

	@Override
	public QueryResult process(final QueryResult queryResult,
			final int operandID) {
		final Iterator<Bindings> itb = queryResult.oneTimeIterator();
		while (itb.hasNext()) {
			final BindingsArrayReadTriples bart = (BindingsArrayReadTriples) itb
					.next();
			bart.sortReadTriples();
			this.bindings.add(bart);
		}
		return null;
	}

	protected ParallelIterator<Bindings> getIterator() {
		final Iterator<BindingsArrayReadTriples> itb = this.bindings.iterator();
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
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		final QueryResult qr = QueryResult.createInstance(this.bindings.iterator());
		if (this.succeedingOperators.size() > 1)
			qr.materialize();
		for (final OperatorIDTuple opId: this.succeedingOperators) {
			opId.processAll(qr);
		}
		this.bindings.clear();
		return msg;
	}

	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		final QueryResult qr = QueryResult.createInstance(this.bindings.iterator());
		if (this.succeedingOperators.size() > 1)
			qr.materialize();
		for (final OperatorIDTuple opId: this.succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(qr,
					debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
					.getId(), debugstep);
		}
		this.bindings.clear();
		return msg;
	}

	@Override
	public String toString() {
		return super.toString()+" for read triples";
	}

}