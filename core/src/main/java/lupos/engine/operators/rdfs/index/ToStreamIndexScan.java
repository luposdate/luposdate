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
package lupos.engine.operators.rdfs.index;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class ToStreamIndexScan extends BasicIndexScan {

	public ToStreamIndexScan (final RDFSRoot root){
		super(root);
	}

	public ToStreamIndexScan (final OperatorIDTuple succeedingOperator, final Collection<TriplePattern> triplePattern, final RDFSRoot root)
	{
		super(root);
		this.succeedingOperators = new LinkedList<OperatorIDTuple>();

		if (succeedingOperator != null)
		{
			this.succeedingOperators.add(succeedingOperator);
		}
		this.triplePatterns = triplePattern;
	}


	/**
	 * Joins the triple pattern using the index maps and returns the result.<br>
	 * The succeeding operators are passed to the operator pipe to be processed.
	 * @param triplePattern - the triple pattern to be joined
	 * @param succeedingOperators - the succeeding operators to be passed
	 * @return the result of the performed join
	 */
	protected QueryResult process (	final Collection<TriplePattern> triplePattern,
									final List<OperatorIDTuple> succeedingOperators){
		throw new UnsupportedOperationException("join(	final Collection<TriplePattern> triplePattern, final List<OperatorIDTuple> succeedingOperators) is not supported by ToStreamIndexScan");
	}

	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
		throw new UnsupportedOperationException(
				"join(Indices indices, Bindings bindings) is not supported by ToStreamIndexScan");
	}
}
