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
package lupos.rif.operator;

import java.util.Arrays;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.IndexCollection;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.stream.TripleDeleter;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleConsumerDebug;
import lupos.misc.debug.DebugStep;
import lupos.rdf.Prefix;
import lupos.rif.builtin.RIFBuiltinFactory;
import lupos.rif.model.Constant;
import lupos.rif.model.External;
import lupos.rif.model.RuleVariable;

public class IteratorIndex extends BasicIndex implements TripleConsumer, TripleConsumerDebug, TripleDeleter {
	private static final long serialVersionUID = -2452758087959813203L;
	private final External external;

	public IteratorIndex(final External iteratorPredicate) {
		super(null);
		external = iteratorPredicate;
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		result.getVariables().add(
				((RuleVariable) external.termParams.get(0)).getVariable());
		return result;
	}

	private Iterator<Bindings> newBindingIterator() {
		final Iterator<Literal> litIt = getLiteralIterator();
		// Variable steht an erster Stelle des Pr�dikats
		final Variable varToBind = (Variable) external.termParams.get(0)
		.evaluate(Bindings.createNewInstance());
		return new Iterator<Bindings>() {
			public boolean hasNext() {
				return litIt.hasNext();
			}

			public Bindings next() {
				final Bindings bind = Bindings.createNewInstance();
				bind.add(varToBind, litIt.next());
				return bind;
			}

			public void remove() {}
		};
	}

	private Iterator<Literal> getLiteralIterator() {
		// External in IteratorPredicates suchen und dann Parameter 1-x
		// (evaluiert) �bergeben
		Literal[] args = new Literal[external.termParams.size() - 1];
		for (int i = 1; i < external.termParams.size(); i++)
			args[i - 1] = (Literal) external.termParams.get(i).evaluate(
					Bindings.createNewInstance());
		return RIFBuiltinFactory.getIterator(
				(URILiteral) ((Constant) external.termName).getLiteral(), args);
	}

	@Override
	public QueryResult process(int opt, Dataset dataset) {
		final Iterator<Bindings> bindIt = newBindingIterator();
		while (bindIt.hasNext()) {
			final Bindings bind = bindIt.next();
			for (final OperatorIDTuple oid : getSucceedingOperators())
				((Operator) oid.getOperator()).processAll(QueryResult
						.createInstance(Arrays.asList(bind).iterator()), oid
						.getId());
		}
		return null;
	}
	
	@Override
	public QueryResult processDebug(final int opt, final Dataset dataset,
			final DebugStep debugstep) {
		final Iterator<Bindings> bindIt = newBindingIterator();
		while (bindIt.hasNext()) {
			final Bindings bind = bindIt.next();
			for (final OperatorIDTuple oid : getSucceedingOperators())
				((Operator) oid.getOperator()).processAllDebug(QueryResult
						.createInstance(Arrays.asList(bind).iterator()), oid
						.getId(), debugstep);
		}
		return null;		
	}

	@Override
	public String toString() {
		final StringBuffer str = new StringBuffer("IteratorIndex On")
		.append("\n");
		str.append(external.toString());
		return str.toString();
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		final StringBuffer str = new StringBuffer("IteratorIndex On")
		.append("\n");
		str.append(external.toString(prefixInstance));
		return str.toString();
	}

	@Override
	public QueryResult join(Indices indices, Bindings bindings) {
		return null;
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
			process(0, null);
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
