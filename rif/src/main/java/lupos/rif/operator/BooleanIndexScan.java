
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
package lupos.rif.operator;

import java.util.Arrays;
import java.util.HashSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.QueryResult;
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
import lupos.rdf.Prefix;
public class BooleanIndexScan extends BasicIndexScan implements TripleConsumer, TripleConsumerDebug, TripleDeleter {

	protected BindingsFactory bindingsFactory;

	/**
	 * <p>Constructor for BooleanIndexScan.</p>
	 *
	 * @param root a {@link lupos.engine.operators.index.Root} object.
	 */
	public BooleanIndexScan(final Root root) {
		super(root);
		this.triplePatterns = Arrays.asList();
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		result.getVariables().add(new Variable("@boolean"));
		this.intersectionVariables = new HashSet<Variable>(result.getVariables());
		this.unionVariables = this.intersectionVariables;
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final BindingsFactoryMessage msg){
		this.bindingsFactory = msg.getBindingsFactory();
		return msg;
	}

	private QueryResult createQueryResult(){
		final QueryResult result = QueryResult.createInstance();
		final Bindings bind = this.bindingsFactory.createInstance();
		bind.add(new Variable("@boolean"), LiteralFactory.createLiteral("true"));
		result.add(bind);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final Dataset dataset) {
		// leitet ein QueryResult mit einem Binding weiter
		return this.createQueryResult();
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "BooleanIndex";
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final Prefix prefixInstance) {
		return this.toString();
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
			this.processAtSucceedingOperators(this.createQueryResult());
			this.firstTime = false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		if(this.firstTime){
			this.processAtSucceedingOperatorsDebug(this.createQueryResult(), debugstep);
			this.firstTime = false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean joinOrderToBeOptimized(){
		return false;
	}
}
