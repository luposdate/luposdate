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
package lupos.rif.operator;

import java.util.Arrays;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.messages.BindingsFactoryMessage;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.stream.TripleDeleter;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleConsumerDebug;
import lupos.misc.debug.DebugStep;
import lupos.misc.util.ImmutableIterator;
import lupos.rdf.Prefix;
import lupos.rif.builtin.RIFBuiltinFactory;
import lupos.rif.model.Constant;
import lupos.rif.model.External;
import lupos.rif.model.RuleVariable;
public class IteratorIndexScan extends BasicIndexScan implements TripleConsumer, TripleConsumerDebug, TripleDeleter {
	private static final long serialVersionUID = -2452758087959813203L;
	private final External external;
	protected BindingsFactory bindingsFactory;

	/**
	 * <p>Constructor for IteratorIndexScan.</p>
	 *
	 * @param root a {@link lupos.engine.operators.index.Root} object.
	 * @param iteratorPredicate a {@link lupos.rif.model.External} object.
	 */
	public IteratorIndexScan(final Root root, final External iteratorPredicate) {
		super(root);
		this.external = iteratorPredicate;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		result.getVariables().add(
				((RuleVariable) this.external.termParams.get(0)).getVariable());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final BindingsFactoryMessage msg){
		this.bindingsFactory = msg.getBindingsFactory();
		return msg;
	}

	private Iterator<Bindings> newBindingIterator() {
		final Iterator<Literal> litIt = this.getLiteralIterator();
		// Variable steht an erster Stelle des Pr�dikats
		final Variable varToBind = (Variable) this.external.termParams.get(0)
		.evaluate(this.bindingsFactory.createInstance());
		return new ImmutableIterator<Bindings>() {
			@Override
			public boolean hasNext() {
				return litIt.hasNext();
			}

			@Override
			public Bindings next() {
				final Bindings bind = IteratorIndexScan.this.bindingsFactory.createInstance();
				bind.add(varToBind, litIt.next());
				return bind;
			}
		};
	}

	private Iterator<Literal> getLiteralIterator() {
		// External in IteratorPredicates suchen und dann Parameter 1-x
		// (evaluiert) �bergeben
		final Literal[] args = new Literal[this.external.termParams.size() - 1];
		for (int i = 1; i < this.external.termParams.size(); i++) {
			args[i - 1] = (Literal) this.external.termParams.get(i).evaluate(
					this.bindingsFactory.createInstance());
		}
		return RIFBuiltinFactory.getIterator(
				(URILiteral) ((Constant) this.external.termName).getLiteral(), args);
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final Dataset dataset) {
		final Iterator<Bindings> bindIt = this.newBindingIterator();
		while (bindIt.hasNext()) {
			final Bindings bind = bindIt.next();
			for (final OperatorIDTuple oid : this.getSucceedingOperators()) {
				((Operator) oid.getOperator()).processAll(QueryResult
						.createInstance(Arrays.asList(bind).iterator()), oid
						.getId());
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuffer str = new StringBuffer("Iterator On")
		.append("\n");
		str.append(this.external.toString());
		return str.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final Prefix prefixInstance) {
		final StringBuffer str = new StringBuffer("Iterator On")
		.append("\n");
		str.append(this.external.toString(prefixInstance));
		return str.toString();
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
		return null;
	}

	private boolean firstTime = true;

	/** {@inheritDoc} */
	@Override
	public void deleteTriple(final Triple triple) {
	}

	/** {@inheritDoc} */
	@Override
	public void deleteTripleDebug(final Triple triple, final DebugStep debugstep) {
	}

	/** {@inheritDoc} */
	@Override
	public void consume(final Triple triple) {
		if(this.firstTime){
			this.process(null);
			this.firstTime = false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		if(this.firstTime){
			this.startProcessingDebug(null, debugstep);
			this.firstTime = false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean joinOrderToBeOptimized(){
		return false;
	}
}
