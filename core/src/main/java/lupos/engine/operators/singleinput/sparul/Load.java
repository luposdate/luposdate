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
package lupos.engine.operators.singleinput.sparul;

import java.util.Collection;

import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;

public class Load extends MultipleURIOperator {

	// the URI into which the data is loaded, is null in the case of the default
	// graph...
	protected URILiteral into;
	protected final Dataset dataset;
	protected final boolean isSilent;

	public Load(final Collection<URILiteral> cu, final URILiteral into, Dataset dataset, final boolean isSilent) {
		super(cu);
		this.into = into;
		this.dataset=dataset;
		this.isSilent = isSilent;
	}

	public void setInto(final URILiteral into) {
		this.into = into;
	}

	public URILiteral getInto() {
		return into;
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (into != null)
			System.err.println("Named graphs currently not supported!");
		else {
			try {
				for (final URILiteral u : cu) {
					final Indices indices = dataset.getIndicesFactory().createIndices((into==null)?u:into);
					dataset.indexingRDFGraph(u, indices, false, false);
					if (into == null) {
						// default graph!
						dataset.putIntoDefaultGraphs(u, indices);
					} else {
						dataset.putIntoNamedGraphs(into, indices);
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
				System.err.println(e);
				if(!isSilent)
					throw new Error("Error while loading: "+e.getMessage());
			}
		}
		this.dataset.buildCompletelyAllIndices();
		return null;
	}

	@Override
	public String toString() {
		String s = super.toString() + cu;
		if (into != null)
			s += " into " + into;
		return s;
	}
}
