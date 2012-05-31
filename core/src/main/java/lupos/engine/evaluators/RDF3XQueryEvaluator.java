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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.IntegerStringMapJava;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.trie.SuperTrie;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.adaptedRDF3X.IndexCollection;
import lupos.engine.operators.index.adaptedRDF3X.SixIndices;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;
import lupos.misc.Tuple;

public class RDF3XQueryEvaluator extends BasicIndexQueryEvaluator {
	
	public final static String INDICESINFOFILE="indices.info";

	public enum Optimizations {
		NONE, BINARY, MERGEJOIN, NARYMERGEJOIN;
	}

	protected Optimizations optimization;
	protected boolean loadindexinfo;
	protected String writeindexinfo;

	public RDF3XQueryEvaluator() throws Exception {
		// initialization must later be done...
	}

	public RDF3XQueryEvaluator(final String[] args) throws Exception {
		super(args);
	}
	
	public RDF3XQueryEvaluator(DEBUG debug, boolean multiplequeries, compareEvaluator compare, String compareoptions, int times, String dataset,
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
			final String writeindexinfo,
			final Optimizations optimization){
		super(debug, multiplequeries, compare, compareoptions, times, dataset,
				type, externalontology,inmemoryexternalontologyinference, rdfs, codemap, tmpDirs, loadindexinfo,
				parallelOperands, blockwise,
				limit, jointhreads, joinbuffer,
				heap, tosort, indexheap, mergeheapheight, mergeheaptype, chunk, mergethreads, yagomax,
				stringsearch, resulttype, storage, join, optional, sort, distinct,
				merge_join_optional, encoding,
				datastructure, datasetsort);
		init(loadindexinfo, writeindexinfo, optimization);
	}

	private void init(final boolean loadindexinfo,
			final String writeindexinfo,
			final Optimizations optimization) {
		this.loadindexinfo = loadindexinfo;
		this.writeindexinfo = writeindexinfo;
		this.optimization = optimization;
		if (optimization == Optimizations.MERGEJOIN)
			this.opt = BasicIndex.MERGEJOIN;
		else if (optimization == Optimizations.BINARY)
			this.opt = BasicIndex.Binary;
		else if (optimization == Optimizations.NARYMERGEJOIN)
			this.opt = BasicIndex.NARYMERGEJOIN;
		else if (optimization == Optimizations.NONE)
			this.opt = BasicIndex.NONE;
	}

	@Override
	public void setupArguments() {
		this.defaultOptimization = Optimizations.MERGEJOIN;	
		this.args.addBooleanOption(
						"loadindexinfo",
						"Instead of importing data, the indices of a previous run of RDF3XQueryEvaluator will be used. For this purpose, the input file must contain the file written via --writeindexinfo in the previous run. See also --writeindexinfo File.",
						false);
		this.args
				.addStringOption(
						"writeindexinfo",
						"Information about the used indices are written to a given file. This file can be later used to directly use the previously constructed indices instead of importing the data again. See also --readindexinfo.",
						"");
		super.setupArguments();
	}

	@Override
	public void init() throws Exception {
		super.init();
		init(this.args.getBool("loadindexinfo"), this.args
				.getString("writeindexinfo"),
				(Optimizations) this.args.getEnum("optimization"));
	}
	
	public void loadLargeScaleIndices(final String dir) throws Exception{
		String datafile=dir+File.separator+INDICESINFOFILE;
		
		this.setupArguments();
		this.getArgs().set("debug", DEBUG.NONE);
		this.getArgs().set("result", QueryResult.TYPE.MEMORY);
		this.getArgs().set("codemap", LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
		this.getArgs().set("distinct", CommonCoreQueryEvaluator.DISTINCT.DBSETBLOCKING);
		this.getArgs().set("optional", CommonCoreQueryEvaluator.JOIN.HASH);
		this.getArgs().set("join", CommonCoreQueryEvaluator.JOIN.HASH);
		this.getArgs().set("datastructure", Indices.DATA_STRUCT.DBBPTREE);
		this.getArgs().set("tmpDir", dir);
		this.getArgs().set("sortduringindexconstruction", Dataset.SORT.STRINGSEARCHTREE);
		this.getArgs().set("loadindexinfo", true);
		this.init();

		// load indices!
		final URILiteral rdfURL = LiteralFactory.createStringURILiteral("<file:" + datafile + ">");
		final LinkedList<URILiteral> defaultGraphs = new LinkedList<URILiteral>();
		defaultGraphs.add(rdfURL);
		System.out.println("Load indices...");
		this.prepareInputData(defaultGraphs, new LinkedList<URILiteral>());
		System.out.println("Indices loaded with "+LazyLiteral.getV().size()+" in the codemap and "+this.dataset.getDefaultGraphIndices().iterator().next().numberOfTriples()+" triples in the evaluation indices!");
	}

	@Override
	public long prepareInputData(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs) throws Exception {
		final Date a = new Date();
		super.prepareInputData(defaultGraphs, namedGraphs);
		long timeUsed;
		if (this.loadindexinfo) {
			final LuposObjectInputStream in = new LuposObjectInputStream(
					new BufferedInputStream(defaultGraphs.iterator().next()
							.openStream()), null);
			this.dataset = new Dataset(
					type,
					getMaterializeOntology(),
					opt,
					new Dataset.IndicesFactory() {
								public Indices createIndices(
										final URILiteral uriLiteral) {
									return new SixIndices(uriLiteral, false);
								}

								public lupos.engine.operators.index.IndexCollection createIndexCollection() {
									IndexCollection ic = new IndexCollection();
									ic.dataset = dataset;
									return ic;
								}
							}, in);
			in.close();
			timeUsed = ((new Date()).getTime() - a.getTime());
		} else {
			this.dataset = new Dataset(
					defaultGraphs,
					namedGraphs,
					type,
					getMaterializeOntology(),
					opt,
					new Dataset.IndicesFactory() {
								public Indices createIndices(
										final URILiteral uriLiteral) {
									return new SixIndices(uriLiteral);
								}

								public lupos.engine.operators.index.IndexCollection createIndexCollection() {
									IndexCollection ic = new IndexCollection();
									ic.dataset = dataset;
									return ic;
								}
							}, debug != DEBUG.NONE,
					inmemoryexternalontologyinference);
			this.dataset.buildCompletelyAllIndices();
			timeUsed = ((new Date()).getTime() - a.getTime());
		}
		if (this.writeindexinfo.compareTo("") != 0) {
			final LuposObjectOutputStream out = new LuposObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.writeindexinfo)));
			this.dataset.writeIndexInfo(out, null);
			out.close();
		}
		return timeUsed;
	}
	
	public void writeOutIndexFile(String dir) throws FileNotFoundException, IOException {
		final LuposObjectOutputStream out = new LuposObjectOutputStream(new BufferedOutputStream(new FileOutputStream(dir+File.separator+RDF3XQueryEvaluator.INDICESINFOFILE)));
		this.dataset.writeIndexInfo(out, 14);
		out.close();
	}

	public void writeOutIndexFileAndModifiedPages(String dir) throws FileNotFoundException, IOException {
		CommonCoreQueryEvaluator.writeOutModifiedPages(this, dir);
	}

	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			Collection<URILiteral> defaultGraphs,
			Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		final Date a = new Date();
		super.prepareInputDataWithSourcesOfNamedGraphs(defaultGraphs, namedGraphs);
		long timeUsed;
		if (loadindexinfo) {
			final LuposObjectInputStream in = new LuposObjectInputStream(
					new BufferedInputStream(defaultGraphs.iterator().next()
							.openStream()), null);
			dataset = new Dataset(
					type,
					getMaterializeOntology(),
					opt,
					new Dataset.IndicesFactory() {
								public Indices createIndices(
										final URILiteral uriLiteral) {
									return new SixIndices(uriLiteral, false);
								}

								public lupos.engine.operators.index.IndexCollection createIndexCollection() {
									IndexCollection ic = new IndexCollection();
									ic.dataset = dataset;
									return ic;
								}
							}, in);
			in.close();
			timeUsed = ((new Date()).getTime() - a.getTime());
		} else {
			dataset = new Dataset(
					defaultGraphs,
					namedGraphs,
					getMaterializeOntology(),
					type,
					opt,
					new Dataset.IndicesFactory() {
								public Indices createIndices(
										final URILiteral uriLiteral) {
									return new SixIndices(uriLiteral);
								}

								public lupos.engine.operators.index.IndexCollection createIndexCollection() {
									IndexCollection ic = new IndexCollection();
									ic.dataset = dataset;
									return ic;
								}
							}, debug != DEBUG.NONE,
					inmemoryexternalontologyinference);
			dataset.buildCompletelyAllIndices();
			timeUsed = ((new Date()).getTime() - a.getTime());
		}
		if (writeindexinfo.compareTo("") != 0) {
			final LuposObjectOutputStream out = new LuposObjectOutputStream(new BufferedOutputStream(new FileOutputStream(writeindexinfo)));
			dataset.writeIndexInfo(out, null);
			out.close();
		}
		return timeUsed;
	}


	public IndexCollection getIndexCollection() {
		return (IndexCollection) indexCollection;
	}

	public static void main(final String[] args) {
		_main(args, RDF3XQueryEvaluator.class);
	}

	@Override
	public lupos.engine.operators.index.IndexCollection createIndexCollection() {
		IndexCollection ic = new IndexCollection();
		ic.dataset = dataset;
		return ic;
	}
}