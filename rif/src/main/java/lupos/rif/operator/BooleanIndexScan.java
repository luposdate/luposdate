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
package lupos.rif.operator;

import java.util.Arrays;
import java.util.HashSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.stream.TripleDeleter;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleConsumerDebug;
import lupos.misc.debug.DebugStep;
import lupos.rdf.Prefix;

public class BooleanIndexScan extends BasicIndexScan implements TripleConsumer, TripleConsumerDebug, TripleDeleter {

	public BooleanIndexScan() {
		super(null);
		triplePatterns = Arrays.asList();
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		result.getVariables().add(new Variable("@boolean"));
		intersectionVariables = new HashSet<Variable>(result.getVariables());
		unionVariables = intersectionVariables;
		return result;
	}
	
	private QueryResult createQueryResult(){
		final QueryResult result = QueryResult.createInstance();
		final Bindings bind = Bindings.createNewInstance();
		bind.add(new Variable("@boolean"), LiteralFactory.createLiteral("true"));
		result.add(bind);
		return result;
	}

	@Override
	public QueryResult process(final Dataset dataset) {
		// leitet ein QueryResult mit einem Binding weiter
		final QueryResult result = this.createQueryResult();
		for (final OperatorIDTuple succOperator : succeedingOperators) {
			((Operator) succOperator.getOperator()).processAll(result,
					succOperator.getId());
		}
		return result;
	}

	public QueryResult processDebug(final int opt, final Dataset dataset,
			final DebugStep debugstep) {
		// leitet ein QueryResult mit einem Binding weiter
		final QueryResult result = this.createQueryResult();
		Bindings bind = result.getFirst();
		for (final OperatorIDTuple succOperator : succeedingOperators) {
			if (result.size() > 0)
				debugstep.step(this, succOperator.getOperator(), bind);
			final QueryResultDebug debug = new QueryResultDebug(result,
					debugstep, this, succOperator.getOperator(), true);
			((Operator) succOperator.getOperator()).processAll(debug,
					succOperator.getId());
		}
		return result;
	}

	@Override
	public QueryResult join(Indices indices, Bindings bindings) {
		return null;
	}

	@Override
	public String toString() {
		return "BooleanIndex";
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		return toString();
	}
	
	private boolean firstTime = true;

	@Override
	public void deleteTriple(Triple triple) {
	}

	@Override
	public void deleteTripleDebug(Triple triple, DebugStep debugstep) {
	}

	@Override
	public void consume(Triple triple) {
		if(firstTime){
			process(null);
			firstTime = false;
		}
	}

	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		if(firstTime){
			processDebug(0, null, debugstep);
			firstTime = false;
		}
	}
}
