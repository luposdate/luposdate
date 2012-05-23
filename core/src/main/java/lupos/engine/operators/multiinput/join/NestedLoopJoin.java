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
package lupos.engine.operators.multiinput.join;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.multiinput.optional.OptionalResult;

public class NestedLoopJoin extends Join {
	private QueryResult[] lba;

	public NestedLoopJoin() {
	}

	public NestedLoopJoin(final int numberOfOperands) {
		super();
		init();
	}

	public void init() {
		lba = new QueryResult[this.getNumberOfOperands()];
		for (int i = 0; i < this.getNumberOfOperands(); i++) {
			lba[i] = QueryResult.createInstance();
		}
	}

	@Override
	public void cloneFrom(final BasicOperator op) {
		super.cloneFrom(op);
		init();
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final QueryResult qr = QueryResult.createInstance();
		final Iterator<Bindings> itb = bindings.oneTimeIterator();
		while (itb.hasNext())
			lba[operandID].add(itb.next());

		for (final Bindings binding : bindings) {

			for (int i = 0; i < this.getNumberOfOperands(); i++) {
				if (lba[i].isEmpty()) {
					return null;
				}
			}
			qr.addAll(combineAndProcess(operandID, binding, 0, QueryResult
					.createInstance()));
		}
		if (qr.size() > 0)
			return qr;
		else
			return null;
	}

	@Override
	public OptionalResult processJoin(final QueryResult bindings,
			final int operandID) {
		final OptionalResult or = new OptionalResult();
		final QueryResult qr = QueryResult.createInstance();
		final QueryResult joinPartnerFromLeftOperand = QueryResult
				.createInstance();
		lba[operandID].addAll(bindings);

		for (final Bindings binding : bindings) {

			for (int i = 0; i < this.getNumberOfOperands(); i++) {
				if (lba[i].isEmpty()) {
					return or;
				}
			}
			// like qr.addAll(combineAndProcess(operandID, binding , 0,
			// QueryResult.createInstance())), but for determining the
			// joinPartnerFromLeftOperand
			if (operandID == 0) {
				final QueryResult bl = QueryResult.createInstance();
				bl.add(binding);
				final QueryResult qr2 = combineAndProcess(operandID, binding,
						1, bl);
				qr.addAll(qr2);
				if (qr2 != null && qr2.size() > 0)
					joinPartnerFromLeftOperand.add(binding);
			} else {
				final Iterator<Bindings> it = lba[0].iterator();
				while (it.hasNext()) {
					final Bindings b = it.next();
					final QueryResult bl = QueryResult.createInstance();
					bl.add(b);
					final QueryResult joinResult = combineAndProcess(operandID,
							binding, 1, bl);
					if (joinResult.size() > 0) {
						qr.addAll(joinResult);
						joinPartnerFromLeftOperand.add(b);
					}
				}
			}
		}
		or.setJoinPartnerFromLeftOperand(joinPartnerFromLeftOperand);
		or.setJoinResult(qr);
		return or;
	}

	private QueryResult combineAndProcess(final int pos,
			final Bindings binding, final int currentPos,
			final QueryResult bindings) {
		final QueryResult qr = QueryResult.createInstance();
		if (pos == currentPos) {
			bindings.add(binding);
			qr
					.addAll(combineAndProcess(pos, binding, currentPos + 1,
							bindings));
		} else if (currentPos < this.getNumberOfOperands()) {
			final Iterator<Bindings> it = lba[currentPos].iterator();
			while (it.hasNext()) {
				final Bindings b = it.next();
				final QueryResult bl = bindings.clone();
				bl.add(b);
				qr.addAll(combineAndProcess(pos, binding, currentPos + 1, bl));
			}
		}
		if (currentPos == this.getNumberOfOperands()) {
			qr.addAll(joinBindings(bindings));
		}
		if (qr.size() == 0)
			return null;
		else
			return qr;
	}
}
