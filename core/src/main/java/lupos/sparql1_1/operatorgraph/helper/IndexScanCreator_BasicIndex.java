
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
package lupos.sparql1_1.operatorgraph.helper;

import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.items.Item;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.EmptyIndexScan;
import lupos.engine.operators.index.EmptyIndexScanSubmittingQueryResultWithOneEmptyBindings;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TriplePattern;
public class IndexScanCreator_BasicIndex implements IndexScanCreatorInterface {

	protected final Root root;

	/**
	 * <p>Constructor for IndexScanCreator_BasicIndex.</p>
	 *
	 * @param root_param a lupos$engine$operators$index$Root object.
	 */
	public IndexScanCreator_BasicIndex(final Root root_param){
		this.root = root_param;
	}

	/** {@inheritDoc} */
	@Override
	public Root getRoot() {
		return this.root;
	}

	/** {@inheritDoc} */
	@Override
	public BasicOperator createIndexScanAndConnectWithRoot(
			final OperatorIDTuple opID, final Collection<TriplePattern> triplePatterns,
			final Item graphConstraint) {
		final lupos.engine.operators.index.BasicIndexScan index = this.root.newIndexScan(opID, triplePatterns, graphConstraint);
		this.root.getSucceedingOperators().add(new OperatorIDTuple(index, 0));
		return index;
	}

	/** {@inheritDoc} */
	@Override
	public void createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(final OperatorIDTuple opID, final Item graphConstraint) {
		this.root.getSucceedingOperators().add(new OperatorIDTuple(new EmptyIndexScanSubmittingQueryResultWithOneEmptyBindings(opID, graphConstraint, this.root), 0));
	}

	/** {@inheritDoc} */
	@Override
	public void createEmptyIndexScanAndConnectWithRoot(final OperatorIDTuple opID) {
		this.root.getSucceedingOperators().add(new OperatorIDTuple(new EmptyIndexScan(opID), 0));
	}

	/** {@inheritDoc} */
	@Override
	public Dataset getDataset() {
		return this.root.dataset;
	}

	/** {@inheritDoc} */
	@Override
	public void addDefaultGraph(final String defaultgraph) {
		if (this.root.defaultGraphs == null) {
			this.root.defaultGraphs = new LinkedList<String>();
		}
		this.root.defaultGraphs.add(defaultgraph);
	}

	/** {@inheritDoc} */
	@Override
	public void addNamedGraph(final String namedgraph) {
		if (this.root.namedGraphs == null) {
			this.root.namedGraphs = new LinkedList<String>();
		}
		this.root.namedGraphs.add(namedgraph);
	}
}
