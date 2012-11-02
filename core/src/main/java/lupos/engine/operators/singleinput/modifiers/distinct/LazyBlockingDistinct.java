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
package lupos.engine.operators.singleinput.modifiers.distinct;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.ParallelIteratorMultipleQueryResults;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.debug.DebugStep;

/**
 * This class is similar to BlockingDistinct, but adds bindings to its set only
 * after EndOfStreamMessage or ComputeIntermediateResultMessage. In this way, no
 * errors occur for Stream-processing with windows!
 * 
 */
public class LazyBlockingDistinct extends Distinct {

	protected final Set<Bindings> bindings;
	protected ParallelIteratorMultipleQueryResults operandsData;

	public LazyBlockingDistinct() {
		this.bindings = new HashSet<Bindings>();
	}

	public LazyBlockingDistinct(final Set<Bindings> bindings) {
		this.bindings = bindings;
	}

	@Override
	public synchronized QueryResult process(final QueryResult queryResult,
			final int operandID) {
		this.operandsData.addQueryResult(queryResult);
		return null;
	}

	protected ParallelIterator<Bindings> getIterator() {
		final Iterator<Bindings> itb = this.bindings.iterator();
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
		this.bindings.clear();
		if (this.operandsData != null) {
			final Iterator<Bindings> itb2 = this.operandsData.getQueryResult().oneTimeIterator();
			while (itb2.hasNext())
				this.bindings.add(itb2.next());
		}
		final QueryResult qr = QueryResult.createInstance(getIterator());
		if (this.succeedingOperators.size() > 1)
			qr.materialize();
		for (final OperatorIDTuple opId : this.succeedingOperators) {
			opId.processAll(qr);
		}
		return msg;
	}

	@Override
	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		this.deleteAllAtSucceedingOperators();
		preProcessMessage(new EndOfEvaluationMessage());
		return msg;
	}

	@Override
	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		this.operandsData.removeAll(queryResult);
		return null;
	}

	@Override
	public void deleteQueryResult(final int operandID) {
		this.bindings.clear();
		if (this.operandsData != null)
			this.operandsData.release();
		this.operandsData = null;
	}

	@Override
	protected boolean isPipelineBreaker() {
		return true;
	}

	@Override
	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		this.deleteAllDebugAtSucceedingOperators(debugstep);
		preProcessMessageDebug(new EndOfEvaluationMessage(), debugstep);
		return msg;
	}
	
	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		this.bindings.clear();
		if (this.operandsData != null) {
			final Iterator<Bindings> itb2 = this.operandsData.getQueryResult().oneTimeIterator();
			while (itb2.hasNext())
				this.bindings.add(itb2.next());
		}
		final QueryResult qr = QueryResult.createInstance(getIterator());
		if (this.succeedingOperators.size() > 1)
			qr.materialize();
		for (final OperatorIDTuple opId : this.succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(qr,
					debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
					.getId(), debugstep);
		}
		return msg;
	}
}