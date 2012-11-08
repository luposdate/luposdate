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
package lupos.sparql1_1.operatorgraph.helper;

import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.items.Item;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.EmptyIndex;
import lupos.engine.operators.index.EmptyIndexSubmittingQueryResultWithOneEmptyBindings;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class IndexScanCreator_BasicIndex implements IndexScanCreatorInterface {
	
	protected final Root indexCollection;
	
	public IndexScanCreator_BasicIndex(final Root indexCollection){
		this.indexCollection = indexCollection;
	}

	public Root getIndexCollection(){
		return this.indexCollection;
	}
	
	@Override
	public BasicOperator getRoot() {
		return this.indexCollection;
	}

	@Override
	public BasicOperator createIndexScanAndConnectWithRoot(
			OperatorIDTuple opID, Collection<TriplePattern> triplePatterns,
			Item graphConstraint) {
		final lupos.engine.operators.index.BasicIndexScan index = indexCollection.newIndex(opID, triplePatterns, graphConstraint);
		indexCollection.getSucceedingOperators().add(new OperatorIDTuple(index, 0));
		return index;
	}

	@Override
	public void createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(OperatorIDTuple opID, Item graphConstraint) {
		indexCollection.getSucceedingOperators().add(new OperatorIDTuple(new EmptyIndexSubmittingQueryResultWithOneEmptyBindings(opID, null, graphConstraint, indexCollection), 0));
	}

	@Override
	public void createEmptyIndexScanAndConnectWithRoot(OperatorIDTuple opID) {
		indexCollection.getSucceedingOperators().add(new OperatorIDTuple(new EmptyIndex(opID, null, indexCollection), 0));
	}

	@Override
	public Dataset getDataset() {
		return indexCollection.dataset;
	}

	@Override
	public void addDefaultGraph(String defaultgraph) {
		if (indexCollection.defaultGraphs == null)
			indexCollection.defaultGraphs = new LinkedList<String>();
		indexCollection.defaultGraphs.add(defaultgraph);					
	}

	@Override
	public void addNamedGraph(String namedgraph) {
		if (indexCollection.namedGraphs == null)
			indexCollection.namedGraphs = new LinkedList<String>();
		indexCollection.namedGraphs.add(namedgraph);					
	}
}
