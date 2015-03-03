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
package lupos.distributed.query.operator;

import java.io.IOException;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.storage.IStorage;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * The indices class for accessing the data of the distributed query evaluator.
 * It just forwards the calls to an instance of IStorage to be given in the constructor.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class QueryClientIndices extends Indices {
	
	protected final IStorage storage;

	/**
	 * <p>Constructor for QueryClientIndices.</p>
	 *
	 * @param uriLiteral a {@link lupos.datastructures.items.literal.URILiteral} object.
	 * @param storage a {@link lupos.distributed.storage.IStorage} object.
	 */
	public QueryClientIndices(final URILiteral uriLiteral, final IStorage storage) {
		this.rdfName = uriLiteral;
		this.storage = storage;
	}

	/**
	 * <p>evaluateTriplePattern.</p>
	 *
	 * @param triplePattern a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public QueryResult evaluateTriplePattern(final TriplePattern triplePattern){
		try {
			return this.storage.evaluateTriplePattern(triplePattern);
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void add(Triple triple) {
		this.storage.addTriple(triple);
	}

	/** {@inheritDoc} */
	@Override
	public void remove(Triple triple) {
		this.storage.remove(triple);
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(Triple triple) {
		return this.storage.containsTriple(triple);
	}

	/** {@inheritDoc} */
	@Override
	public void init(DATA_STRUCT ds) {
	}

	/** {@inheritDoc} */
	@Override
	public void constructCompletely() {
		this.storage.endImportData();
	}

	/** {@inheritDoc} */
	@Override
	public void writeOutAllModifiedPages() throws IOException {
	}
}
