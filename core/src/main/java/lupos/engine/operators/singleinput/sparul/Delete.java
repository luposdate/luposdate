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
package lupos.engine.operators.singleinput.sparul;

import java.util.Collection;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;

public class Delete extends MultipleURIOperator {

	protected final Dataset dataset;

	
	public Delete(final Collection<URILiteral> cu, Dataset dataset) {
		super(cu);
		this.dataset=dataset;
	}

	@Override
	public QueryResult process(QueryResult bindings, final int operandID) {
		if(bindings instanceof QueryResultDebug)
			bindings=((QueryResultDebug)bindings).getOriginalQueryResult();
		if (bindings instanceof GraphResult) {
			final GraphResult gr = (GraphResult) bindings;
			for (final Triple t : gr.getGraphResultTriples()) {
				if (cu == null || cu.size() == 0) {
					final Collection<Indices> ci = dataset.getDefaultGraphIndices();
					for (final Indices indices : ci) {
						if (indices.contains(t)) {
							indices.remove(t);
						}
					}
				} else {
					for (final URILiteral uri : cu) {
						Indices indices = dataset.getNamedGraphIndices(uri);
						if (indices != null)
							if (indices.contains(t)) {
								indices.remove(t);
							}
						indices = dataset.getDefaultGraphIndices(uri);
						if (indices != null)
							if (indices.contains(t)) {
								indices.remove(t);
							}

					}
				}
			}
		} else
			System.err.println("GraphResult expected instead of " + bindings.getClass());
		this.dataset.buildCompletelyAllIndices();
		return null;
	}
}
