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
package lupos.engine.evaluators;

import java.util.Collection;
import java.util.Date;

import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.trie.SuperTrie;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.memoryindex.MemoryIndexRoot;
import lupos.engine.operators.index.memoryindex.SevenMemoryIndices;
import lupos.misc.Tuple;

public class MemoryIndexQueryEvaluator extends BasicIndexQueryEvaluator {

	protected enum Optimizations {
		NONE, MOSTRESTRICTIONS, MOSTRESTRICTIONSLEASTENTRIES, LEASTENTRIES, BINARY;
	}

	public MemoryIndexQueryEvaluator() throws Exception {
		super();
	}

	public MemoryIndexQueryEvaluator(final String[] arguments) throws Exception {
		super(arguments);
	}
	
	public MemoryIndexQueryEvaluator(DEBUG debug, boolean multiplequeries, compareEvaluator compare, String compareoptions, int times, String dataset,
			final String type, final String externalontology,
			final boolean inmemoryexternalontologyinference, final RDFS rdfs,
			final LiteralFactory.MapType codemap, final String[] tmpDirs,
			final boolean loadindexinfo,
			final PARALLELOPERANDS parallelOperands, final boolean blockwise,
			final int limit, final int jointhreads, final int joinbuffer,
			final Heap.HEAPTYPE heap, final ToSort.TOSORT tosort,
			final int indexheap, final int mergeheapheight,
			final Heap.HEAPTYPE mergeheaptype, final int chunk,
			final int mergethreads, final int yagomax,
			final SuperTrie.TRIETYPE stringsearch,
			final QueryResult.TYPE resulttype, final STORAGE storage,
			final JOIN join, final JOIN optional, final SORT sort,
			final DISTINCT distinct,
			final MERGE_JOIN_OPTIONAL merge_join_optional, final String encoding,
			final lupos.engine.operators.index.Indices.DATA_STRUCT datastructure,
			final Dataset.SORT datasetsort,
			final Optimizations optimization){
		super(debug, multiplequeries, compare, compareoptions, times, dataset,
				type, externalontology,inmemoryexternalontologyinference, rdfs, codemap, tmpDirs, loadindexinfo,
				parallelOperands, blockwise,
				limit, jointhreads, joinbuffer,
				heap, tosort, indexheap, mergeheapheight, mergeheaptype, chunk, mergethreads, yagomax,
				stringsearch, resulttype, storage, join, optional, sort, distinct,
				merge_join_optional, encoding,
				datastructure, datasetsort);
		init(datastructure, optimization);
	}

	private void init(final Indices.DATA_STRUCT datastructure,
			final Optimizations optimization) {
		Indices.setUsedDatastructure(datastructure);
		switch (optimization) {
		case MOSTRESTRICTIONS:
			opt = BasicIndexScan.MOSTRESTRICTIONS;
			break;
		case MOSTRESTRICTIONSLEASTENTRIES:
			opt = BasicIndexScan.MOSTRESTRICTIONSLEASTENTRIES;
			break;
		case LEASTENTRIES:
			opt = BasicIndexScan.LEASTENTRIES;
			break;
		case BINARY:
			opt = BasicIndexScan.Binary;
			break;
		default:
			opt = BasicIndexScan.NONE;
			break;
		}
	}

	@Override
	public void init() throws Exception {
		super.init();
		// IndexMaps.setUsedDatastructure((IndexMaps.DATA_STRUCT)args.getEnum(
		// "datastructure"));
		init((Indices.DATA_STRUCT) args.getEnum("datastructure"),
				(Optimizations) this.args.getEnum("optimization"));
	}

	@Override
	public void setupArguments() {
		defaultOptimization = Optimizations.MOSTRESTRICTIONSLEASTENTRIES;
		super.setupArguments();
	}

	// moved to lupos.rdf.Indices
	@Override
	public long prepareInputData(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs) throws Exception {
		final Date a = new Date();
		super.prepareInputData(defaultGraphs, namedGraphs);
		dataset = new Dataset(defaultGraphs, namedGraphs, type,
				getMaterializeOntology(), opt, new Dataset.IndicesFactory() {
			public Indices createIndices(final URILiteral uriLiteral) {
				return new SevenMemoryIndices(uriLiteral);
			}

			public lupos.engine.operators.index.Root createRoot() {
				MemoryIndexRoot ic=new MemoryIndexRoot();
				ic.dataset=dataset;
				return ic;
			}
		}, debug != DEBUG.NONE, inmemoryexternalontologyinference);
		dataset.buildCompletelyAllIndices();
		final long prepareTime = ((new Date()).getTime() - a.getTime());
		return prepareTime;
	}

	public MemoryIndexRoot getRoot() {
		return (MemoryIndexRoot) root;
	}

	public static void main(final String[] args) {
		_main(args, MemoryIndexQueryEvaluator.class);
	}

	@Override
	public lupos.engine.operators.index.Root createRoot() {
		MemoryIndexRoot ic=new MemoryIndexRoot();
		ic.dataset=dataset;
		return ic;
	}

	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			Collection<URILiteral> defaultGraphs,
			Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		final Date a = new Date();
		super.prepareInputDataWithSourcesOfNamedGraphs(defaultGraphs, namedGraphs);
		dataset = new Dataset(defaultGraphs, namedGraphs,
				getMaterializeOntology(), type, opt, new Dataset.IndicesFactory() {
			public Indices createIndices(final URILiteral uriLiteral) {
				return new SevenMemoryIndices(uriLiteral);
			}

			public lupos.engine.operators.index.Root createRoot() {
				MemoryIndexRoot ic=new MemoryIndexRoot();
				ic.dataset=dataset;
				return ic;
			}
		}, debug != DEBUG.NONE, inmemoryexternalontologyinference);
		dataset.buildCompletelyAllIndices();
		final long prepareTime = ((new Date()).getTime() - a.getTime());
		return prepareTime;
	}
}