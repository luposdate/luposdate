
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
package lupos.engine.operators.singleinput.parallel;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
public class QueryResultInBlocks extends Operator {

	/** Constant <code>MAXBUFFER=100</code> */
	protected final static int MAXBUFFER = 100;
	/** Constant <code>BLOCKSIZE=</code> */
	protected static int BLOCKSIZE;

	protected Runner runner = null;
	protected BoundedBuffer<QueryResult> queryresultbuffer = new BoundedBuffer<QueryResult>(MAXBUFFER);

	/**
	 * <p>Constructor for QueryResultInBlocks.</p>
	 */
	public QueryResultInBlocks() {
		BLOCKSIZE = ParallelOperand.getQueueLimit();
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final QueryResult queryResult,
			final int operandID) {
		try {
			this.queryresultbuffer.put(queryResult);
		} catch (final InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		if (this.runner == null) {
			this.runner = new Runner();
			this.runner.start();
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		this.queryresultbuffer.endOfData();
		// wait until thread finished!
		if (this.runner != null)
			try {
				this.runner.join();
			} catch (final InterruptedException e) {
				System.err.println();
				e.printStackTrace();
			}
		return msg;
	}

	public class Runner extends Thread {

		@Override
		public void run() {
			try {
				while (QueryResultInBlocks.this.queryresultbuffer.hasNext()) {
					final QueryResult queryResult = QueryResultInBlocks.this.queryresultbuffer.get();
					final Iterator<Bindings> ib = queryResult.oneTimeIterator();
					while (ib.hasNext()) {
						final QueryResult queryresult_new = QueryResult
								.createInstance();
						for (int i = 0; i < BLOCKSIZE && ib.hasNext(); i++) {
							queryresult_new.add(ib.next());
						}
						for (final OperatorIDTuple opId : QueryResultInBlocks.this.succeedingOperators) {
							opId.processAll(queryresult_new);
						}
					}
				}
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}

}
