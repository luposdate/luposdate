/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.distributed.query;

import java.util.Collection;
import java.util.Date;

import lupos.datastructures.items.literal.URILiteral;
import lupos.distributed.query.operator.QueryClientIndices;
import lupos.distributed.query.operator.QueryClientRoot;
import lupos.distributed.storage.IStorage;
import lupos.engine.evaluators.BasicIndexQueryEvaluator;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Root;
import lupos.misc.Tuple;

public class QueryClient extends BasicIndexQueryEvaluator {

	protected final IStorage storage;

	public QueryClient(final IStorage storage) throws Exception {
		super();
		this.storage = storage;
	}

	public QueryClient(final IStorage storage, final String[] args) throws Exception {
		super(args);
		this.storage = storage;
	}

	@Override
	public void init() throws Exception {
		super.init();
		// avoid evaluation of triple patterns for query optimization,
		// in other words: use a static analysis for reorder triple patterns for optimized query evaluation
		// (fetch-as-needed is used)
		this.opt = BasicIndexScan.MOSTRESTRICTIONS;
	}

	@Override
	public Root createRoot() {
		return new QueryClientRoot(this.dataset);
	}

	@Override
	public long prepareInputData(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs) throws Exception {
		final Date a = new Date();
		super.prepareInputData(defaultGraphs, namedGraphs);
		this.dataset = new Dataset(defaultGraphs, namedGraphs, this.type,
				this.getMaterializeOntology(), this.opt, new Dataset.IndicesFactory() {
			@Override
			public Indices createIndices(final URILiteral uriLiteral) {
				return new QueryClientIndices(uriLiteral, QueryClient.this.storage);
			}

			@Override
			public lupos.engine.operators.index.Root createRoot() {
				final QueryClientRoot ic=new QueryClientRoot(QueryClient.this.dataset);
				return ic;
			}
		}, this.debug != DEBUG.NONE, this.inmemoryexternalontologyinference);
		this.dataset.buildCompletelyAllIndices();
		final long prepareTime = ((new Date()).getTime() - a.getTime());
		return prepareTime;
	}

	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			final Collection<URILiteral> defaultGraphs,
			final Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		final Date a = new Date();
		super.prepareInputDataWithSourcesOfNamedGraphs(defaultGraphs, namedGraphs);
		this.dataset = new Dataset(defaultGraphs, namedGraphs,
				this.getMaterializeOntology(), this.type, this.opt, new Dataset.IndicesFactory() {
			@Override
			public Indices createIndices(final URILiteral uriLiteral) {
				return new QueryClientIndices(uriLiteral, QueryClient.this.storage);
			}

			@Override
			public lupos.engine.operators.index.Root createRoot() {
				final QueryClientRoot ic=new QueryClientRoot(QueryClient.this.dataset);
				return ic;
			}
		}, this.debug != DEBUG.NONE, this.inmemoryexternalontologyinference);
		this.dataset.buildCompletelyAllIndices();
		final long prepareTime = ((new Date()).getTime() - a.getTime());
		return prepareTime;
	}
}
