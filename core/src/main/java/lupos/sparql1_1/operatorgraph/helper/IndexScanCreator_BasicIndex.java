/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
	
	public IndexScanCreator_BasicIndex(final Root root_param){
		this.root = root_param;
	}
	
	@Override
	public BasicOperator getRoot() {
		return this.root;
	}

	@Override
	public BasicOperator createIndexScanAndConnectWithRoot(
			OperatorIDTuple opID, Collection<TriplePattern> triplePatterns,
			Item graphConstraint) {
		final lupos.engine.operators.index.BasicIndexScan index = this.root.newIndexScan(opID, triplePatterns, graphConstraint);
		this.root.getSucceedingOperators().add(new OperatorIDTuple(index, 0));
		return index;
	}

	@Override
	public void createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(OperatorIDTuple opID, Item graphConstraint) {
		this.root.getSucceedingOperators().add(new OperatorIDTuple(new EmptyIndexScanSubmittingQueryResultWithOneEmptyBindings(opID, null, graphConstraint, this.root), 0));
	}

	@Override
	public void createEmptyIndexScanAndConnectWithRoot(OperatorIDTuple opID) {
		this.root.getSucceedingOperators().add(new OperatorIDTuple(new EmptyIndexScan(opID, null, this.root), 0));
	}

	@Override
	public Dataset getDataset() {
		return this.root.dataset;
	}

	@Override
	public void addDefaultGraph(String defaultgraph) {
		if (this.root.defaultGraphs == null)
			this.root.defaultGraphs = new LinkedList<String>();
		this.root.defaultGraphs.add(defaultgraph);					
	}

	@Override
	public void addNamedGraph(String namedgraph) {
		if (this.root.namedGraphs == null)
			this.root.namedGraphs = new LinkedList<String>();
		this.root.namedGraphs.add(namedgraph);					
	}
}
