
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
package lupos.distributed.query;

import java.util.Collection;
import java.util.Date;

import lupos.datastructures.items.literal.URILiteral;
import lupos.distributed.query.operator.QueryClientIndices;
import lupos.distributed.query.operator.histogramsubmission.IHistogramExecutor;
import lupos.distributed.query.operator.histogramsubmission.QueryClientRootWithHistogramSubmission;
import lupos.distributed.query.operator.withouthistogramsubmission.QueryClientRoot;
import lupos.distributed.storage.IStorage;
import lupos.engine.evaluators.BasicIndexQueryEvaluator;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Root;
import lupos.misc.Tuple;
public class QueryClient extends BasicIndexQueryEvaluator {

	protected final IStorage storage;

	protected IHistogramExecutor histogramExecutor;

	/**
	 * <p>Constructor for QueryClient.</p>
	 *
	 * @param storage a {@link lupos.distributed.storage.IStorage} object.
	 * @throws java.lang.Exception if any.
	 */
	public QueryClient(final IStorage storage) throws Exception {
		this(storage, (IHistogramExecutor) null);
	}

	/**
	 * <p>Constructor for QueryClient.</p>
	 *
	 * @param storage a {@link lupos.distributed.storage.IStorage} object.
	 * @param histogramExecutor a {@link lupos.distributed.query.operator.histogramsubmission.IHistogramExecutor} object.
	 * @throws java.lang.Exception if any.
	 */
	public QueryClient(final IStorage storage, final IHistogramExecutor histogramExecutor) throws Exception {
		super();
		this.storage = storage;
		if(this.histogramExecutor==null){
			this.histogramExecutor = histogramExecutor;
		} // else ignore if histogramExecutor is already set in init()
		this.initOptimization();
		this.storage.setBindingsFactory(this.bindingsFactory);
	}

	/**
	 * <p>Constructor for QueryClient.</p>
	 *
	 * @param storage a {@link lupos.distributed.storage.IStorage} object.
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public QueryClient(final IStorage storage, final String[] args) throws Exception {
		this(storage, null, args);
	}

	/**
	 * <p>Constructor for QueryClient.</p>
	 *
	 * @param storage a {@link lupos.distributed.storage.IStorage} object.
	 * @param histogramExecutor a {@link lupos.distributed.query.operator.histogramsubmission.IHistogramExecutor} object.
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public QueryClient(final IStorage storage, final IHistogramExecutor histogramExecutor, final String[] args) throws Exception {
		super(args);
		this.storage = storage;
		if(this.histogramExecutor==null){
			this.histogramExecutor = histogramExecutor;
		} // else ignore if histogramExecutor is already set in init()
		this.initOptimization();
		this.storage.setBindingsFactory(this.bindingsFactory);
	}

	/**
	 * <p>initOptimization.</p>
	 */
	protected void initOptimization() {
		if(this.histogramExecutor == null){
			// avoid evaluation of triple patterns for query optimization,
			// in other words: use a static analysis for reorder triple patterns for optimized query evaluation
			// (fetch-as-needed is used)
			this.opt = BasicIndexScan.MOSTRESTRICTIONS;
		} else {
			// use histograms to find best join order
			this.opt = BasicIndexScan.BINARY;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Root createRoot() {
		if(this.histogramExecutor == null) {
			return new QueryClientRoot(this.dataset);
		} else {
			return new QueryClientRootWithHistogramSubmission(this.dataset, this.histogramExecutor);
		}
	}

	/** {@inheritDoc} */
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
				return QueryClient.this.createRoot();
			}
		}, this.debug != DEBUG.NONE, this.inmemoryexternalontologyinference);
		this.dataset.buildCompletelyAllIndices();
		final long prepareTime = ((new Date()).getTime() - a.getTime());
		return prepareTime;
	}

	/** {@inheritDoc} */
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
				return QueryClient.this.createRoot();
			}
		}, this.debug != DEBUG.NONE, this.inmemoryexternalontologyinference);
		this.dataset.buildCompletelyAllIndices();
		final long prepareTime = ((new Date()).getTime() - a.getTime());
		return prepareTime;
	}
}
