
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
package lupos.engine.operators.tripleoperator;

import java.util.List;

import lupos.datastructures.items.Triple;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.misc.debug.DebugStep;
public class TripleOperator extends Operator implements TripleConsumer, TripleConsumerDebug {

	/**
	 * <p>Constructor for TripleOperator.</p>
	 */
	public TripleOperator() {
	}

	/**
	 * <p>Constructor for TripleOperator.</p>
	 *
	 * @param succeedingOperators a {@link java.util.List} object.
	 */
	public TripleOperator(final List<OperatorIDTuple> succeedingOperators) {
		super(succeedingOperators);
	}

	/**
	 * <p>Constructor for TripleOperator.</p>
	 *
	 * @param succeedingOperator a {@link lupos.engine.operators.OperatorIDTuple} object.
	 */
	public TripleOperator(final OperatorIDTuple succeedingOperator) {
		super(succeedingOperator);
	}

	/** {@inheritDoc} */
	@Override
	public void consume(final Triple triple) {
		throw (new UnsupportedOperationException("This Operator(" + this + ") should have been replaced before being used."));
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final QueryResult queryResult, final int operandID) {
		if(queryResult instanceof GraphResult){
			for(final Triple triple: ((GraphResult) queryResult).getGraphResultTriples()){
				this.consume(triple);
			}
		} else {
			throw (new UnsupportedOperationException("This Operator(" + this + ") should can only process GraphResult, but not " + queryResult + " of type " + queryResult.getClass().getName() + "."));
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		this.consume(triple);
	}
}
