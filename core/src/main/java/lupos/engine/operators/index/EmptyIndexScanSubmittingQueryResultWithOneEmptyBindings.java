
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
package lupos.engine.operators.index;

import java.net.URISyntaxException;
import java.util.Collection;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BindingsFactoryMessage;
import lupos.engine.operators.messages.Message;
import lupos.rdf.Prefix;
public class EmptyIndexScanSubmittingQueryResultWithOneEmptyBindings extends EmptyIndexScan {

	protected final Root root;
	protected final Item rdfGraph;
	protected BindingsFactory bindingsFactory;

	/**
	 *
	 */
	private static final long serialVersionUID = -6813056199050211285L;

	/**
	 * <p>Constructor for EmptyIndexScanSubmittingQueryResultWithOneEmptyBindings.</p>
	 *
	 * @param succeedingOperator a {@link lupos.engine.operators.OperatorIDTuple} object.
	 * @param graphConstraint a {@link lupos.datastructures.items.Item} object.
	 * @param root_param a {@link lupos.engine.operators.index.Root} object.
	 */
	public EmptyIndexScanSubmittingQueryResultWithOneEmptyBindings(final OperatorIDTuple succeedingOperator,
			final Item graphConstraint,
			final lupos.engine.operators.index.Root root_param) {
		super(succeedingOperator);
		this.root = root_param;
		this.rdfGraph = graphConstraint;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final BindingsFactoryMessage msg){
		this.bindingsFactory = msg.getBindingsFactory();
		return msg;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Creating a new query result with an empty binding to handle an empty BIND
	 * statement
	 */
	@Override
	public QueryResult process(final Dataset dataset) {
		final QueryResult queryResult = QueryResult.createInstance();
		if(this.rdfGraph!=null && this.rdfGraph.isVariable()){
			final Variable graphConstraint = (Variable) this.rdfGraph;
			if (this.root.namedGraphs != null && this.root.namedGraphs.size() > 0) {
				// Convert the named graphs' names into URILiterals
				// to be applicable later on
				for (final String name : this.root.namedGraphs) {
					final Bindings graphConstraintBindings = this.bindingsFactory.createInstance();
					try {
						graphConstraintBindings.add(graphConstraint, LiteralFactory.createURILiteralWithoutLazyLiteral(name));
					} catch (final URISyntaxException e) {
						System.err.println(e);
						e.printStackTrace();
					}
					queryResult.add(graphConstraintBindings);
				}
				} else {
					final Collection<Indices> dataSetIndices = dataset.getNamedGraphIndices();
					if (dataSetIndices != null) {
						for (final Indices indices : dataSetIndices) {
							final Bindings graphConstraintBindings = this.bindingsFactory.createInstance();
							graphConstraintBindings.add(graphConstraint, indices.getRdfName());
							queryResult.add(graphConstraintBindings);
						}
					}
				}
		} else {
			queryResult.add(this.bindingsFactory.createInstance());
		}

		return queryResult;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return super.toString()+"\nReturning queryResult with one empty bindings";
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final Prefix prefix) {
		return super.toString(prefix)+"\nReturning queryResult with one empty bindings";
	}

	// just for using this also for the stream engine
	protected boolean firstTime = true;

	/** {@inheritDoc} */
	@Override
	public void consume(final Triple triple) {
		if(this.firstTime){
			this.firstTime = false;
			this.processAtSucceedingOperators(QueryResult.createInstance(this.bindingsFactory.createInstance()));
		}
	}
}
