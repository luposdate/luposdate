
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

import lupos.datastructures.items.Item;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.tripleoperator.TriplePattern;
public interface IndexScanCreatorInterface {
	/**
	 * <p>createIndexScanAndConnectWithRoot.</p>
	 *
	 * @param opID a {@link lupos.engine.operators.OperatorIDTuple} object.
	 * @param triplePatterns a {@link java.util.Collection} object.
	 * @param graphConstraint a {@link lupos.datastructures.items.Item} object.
	 * @return a {@link lupos.engine.operators.BasicOperator} object.
	 */
	public BasicOperator createIndexScanAndConnectWithRoot(OperatorIDTuple opID, Collection<TriplePattern> triplePatterns, Item graphConstraint);
	/**
	 * <p>createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot.</p>
	 *
	 * @param opID a {@link lupos.engine.operators.OperatorIDTuple} object.
	 * @param graphConstraint a {@link lupos.datastructures.items.Item} object.
	 */
	public void createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(OperatorIDTuple opID, Item graphConstraint);
	/**
	 * <p>createEmptyIndexScanAndConnectWithRoot.</p>
	 *
	 * @param opID a {@link lupos.engine.operators.OperatorIDTuple} object.
	 */
	public void createEmptyIndexScanAndConnectWithRoot(OperatorIDTuple opID);
	/**
	 * <p>getRoot.</p>
	 *
	 * @return a {@link lupos.engine.operators.BasicOperator} object.
	 */
	public BasicOperator getRoot();

	// not supported by every evaluator!
	/**
	 * <p>getDataset.</p>
	 *
	 * @return a {@link lupos.engine.operators.index.Dataset} object.
	 */
	public Dataset getDataset();
	/**
	 * <p>addDefaultGraph.</p>
	 *
	 * @param defaultgraph a {@link java.lang.String} object.
	 */
	public void addDefaultGraph(String defaultgraph);
	/**
	 * <p>addNamedGraph.</p>
	 *
	 * @param namedgraph a {@link java.lang.String} object.
	 */
	public void addNamedGraph(String namedgraph);
}
