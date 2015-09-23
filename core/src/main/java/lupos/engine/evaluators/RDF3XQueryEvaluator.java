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
package lupos.engine.evaluators;

import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XRoot;
import lupos.engine.operators.index.adaptedRDF3X.SixIndices;
import lupos.misc.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
public class RDF3XQueryEvaluator extends BasicIndexQueryEvaluator {

	private static final Logger log = LoggerFactory.getLogger(RDF3XQueryEvaluator.class);

	/** Constant <code>INDICESINFOFILE="indices.info"</code> */
	public final static String INDICESINFOFILE="indices.info";

	public enum Optimizations {
		NONE, BINARY, MERGEJOIN, NARYMERGEJOIN;
	}

	protected Optimizations optimization;
	protected boolean loadindexinfo;
	protected String writeindexinfo;

	/**
	 * <p>Constructor for RDF3XQueryEvaluator.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public RDF3XQueryEvaluator() throws Exception {
		// initialization must later be done...
	}

	/**
	 * <p>Constructor for RDF3XQueryEvaluator.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public RDF3XQueryEvaluator(final String[] args) throws Exception {
		super(args);
	}

	/**
	 * <p>Constructor for RDF3XQueryEvaluator.</p>
	 *
	 * @param debug a DEBUG object.
	 * @param multiplequeries a boolean.
	 * @param compare a compareEvaluator object.
	 * @param compareoptions a {@link java.lang.String} object.
	 * @param times a int.
	 * @param dataset a {@link java.lang.String} object.
	 * @param type a {@link java.lang.String} object.
	 * @param externalontology a {@link java.lang.String} object.
	 * @param inmemoryexternalontologyinference a boolean.
	 * @param rdfs a RDFS object.
	 * @param codemap a {@link lupos.datastructures.items.literal.LiteralFactory.MapType} object.
	 * @param tmpDirs an array of {@link java.lang.String} objects.
	 * @param loadindexinfo a boolean.
	 * @param parallelOperands a PARALLELOPERANDS object.
	 * @param blockwise a boolean.
	 * @param limit a int.
	 * @param jointhreads a int.
	 * @param joinbuffer a int.
	 * @param heap a {@link lupos.datastructures.dbmergesortedds.heap.Heap.HEAPTYPE} object.
	 * @param tosort a {@link lupos.datastructures.dbmergesortedds.tosort.ToSort.TOSORT} object.
	 * @param indexheap a int.
	 * @param mergeheapheight a int.
	 * @param mergeheaptype a {@link lupos.datastructures.dbmergesortedds.heap.Heap.HEAPTYPE} object.
	 * @param chunk a int.
	 * @param mergethreads a int.
	 * @param yagomax a int.
	 * @param resulttype a {@link lupos.datastructures.queryresult.QueryResult.TYPE} object.
	 * @param storage a STORAGE object.
	 * @param join a JOIN object.
	 * @param optional a JOIN object.
	 * @param sort a SORT object.
	 * @param distinct a DISTINCT object.
	 * @param merge_join_optional a MERGE_JOIN_OPTIONAL object.
	 * @param encoding a {@link java.lang.String} object.
	 * @param datastructure a {@link lupos.engine.operators.index.Indices.DATA_STRUCT} object.
	 * @param datasetsort a {@link lupos.engine.operators.index.Dataset.SORT} object.
	 * @param writeindexinfo a {@link java.lang.String} object.
	 * @param optimization a {@link lupos.engine.evaluators.RDF3XQueryEvaluator.Optimizations} object.
	 */
	public RDF3XQueryEvaluator(final DEBUG debug, final boolean multiplequeries, final compareEvaluator compare, final String compareoptions, final int times, final String dataset,
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
				resulttype, storage, join, optional, sort, distinct,
				merge_join_optional, encoding,
				datastructure, datasetsort);
		this.init(loadindexinfo, writeindexinfo, optimization);
	}

	private void init(final boolean loadindexinfo,
			final String writeindexinfo,
			final Optimizations optimization) {
		this.loadindexinfo = loadindexinfo;
		this.writeindexinfo = writeindexinfo;
		this.optimization = optimization;
		if (optimization == Optimizations.MERGEJOIN) {
			this.opt = BasicIndexScan.MERGEJOIN;
		} else if (optimization == Optimizations.BINARY) {
			this.opt = BasicIndexScan.BINARY;
		} else if (optimization == Optimizations.NARYMERGEJOIN) {
			this.opt = BasicIndexScan.NARYMERGEJOIN;
		} else if (optimization == Optimizations.NONE) {
			this.opt = BasicIndexScan.NONE;
		}
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public void init() throws Exception {
		super.init();
		this.init(this.args.getBool("loadindexinfo"), this.args
				.getString("writeindexinfo"),
				(Optimizations) this.args.getEnum("optimization"));
	}

	/**
	 * <p>loadLargeScaleIndices.</p>
	 *
	 * @param dir a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public void loadLargeScaleIndices(final String dir) throws Exception{
		final String datafile=dir+File.separator+INDICESINFOFILE;

		this.setupArguments();
		this.getArgs().set("debug", DEBUG.NONE);
		this.getArgs().set("result", QueryResult.TYPE.MEMORY);
		this.getArgs().set("codemap", LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
		this.getArgs().set("distinct", CommonCoreQueryEvaluator.DISTINCT.FASTPAGEDHASHSET);
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
		log.info("Load indices...");
		this.prepareInputData(defaultGraphs, new LinkedList<URILiteral>());
		log.debug("Indices loaded with {} in the codemap and {} triples in the evaluation indices! ",
				LazyLiteral.getV().size(),				((SixIndices) this.dataset.getDefaultGraphIndices().iterator().next()).size());

	}

	/** {@inheritDoc} */
	@Override
	public long prepareInputData(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs) throws Exception {
		final Date a = new Date();
		super.prepareInputData(defaultGraphs, namedGraphs);
		long timeUsed;
		if (this.loadindexinfo) {
			final InputStream in =
					new BufferedInputStream(defaultGraphs.iterator().next().openStream());
			this.dataset = new Dataset(
					this.type,
					this.getMaterializeOntology(),
					this.opt,
					new Dataset.IndicesFactory() {
								@Override
								public Indices createIndices(
										final URILiteral uriLiteral) {
									return new SixIndices(uriLiteral, false);
								}

								@Override
								public lupos.engine.operators.index.Root createRoot() {
									final RDF3XRoot ic = new RDF3XRoot();
									ic.dataset = RDF3XQueryEvaluator.this.dataset;
									return ic;
								}
							}, in);
			in.close();
			timeUsed = ((new Date()).getTime() - a.getTime());
		} else {
			this.dataset = new Dataset(
					defaultGraphs,
					namedGraphs,
					this.type,
					this.getMaterializeOntology(),
					this.opt,
					new Dataset.IndicesFactory() {
								@Override
								public Indices createIndices(
										final URILiteral uriLiteral) {
									return new SixIndices(uriLiteral);
								}

								@Override
								public lupos.engine.operators.index.Root createRoot() {
									final RDF3XRoot ic = new RDF3XRoot();
									ic.dataset = RDF3XQueryEvaluator.this.dataset;
									return ic;
								}
							}, this.debug != DEBUG.NONE,
					this.inmemoryexternalontologyinference);
			this.dataset.buildCompletelyAllIndices();
			timeUsed = ((new Date()).getTime() - a.getTime());
		}
		if (this.writeindexinfo.compareTo("") != 0) {
			final OutputStream out = new BufferedOutputStream(new FileOutputStream(this.writeindexinfo));
			this.dataset.writeIndexInfo(out, null);
			out.close();
		}
		return timeUsed;
	}

	/**
	 * <p>writeOutIndexFile.</p>
	 *
	 * @param dir a {@link java.lang.String} object.
	 * @throws java.io.FileNotFoundException if any.
	 * @throws java.io.IOException if any.
	 */
	public void writeOutIndexFile(final String dir) throws FileNotFoundException, IOException {
		final OutputStream out = new BufferedOutputStream(new FileOutputStream(dir+File.separator+RDF3XQueryEvaluator.INDICESINFOFILE));
		this.dataset.writeIndexInfo(out, 13);
		out.close();
	}

	/**
	 * <p>writeOutIndexFileAndModifiedPages.</p>
	 *
	 * @param dir a {@link java.lang.String} object.
	 * @throws java.io.FileNotFoundException if any.
	 * @throws java.io.IOException if any.
	 */
	public void writeOutIndexFileAndModifiedPages(final String dir) throws FileNotFoundException, IOException {
		CommonCoreQueryEvaluator.writeOutModifiedPages(this, dir);
	}

	/** {@inheritDoc} */
	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			final Collection<URILiteral> defaultGraphs,
			final Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		final Date a = new Date();
		super.prepareInputDataWithSourcesOfNamedGraphs(defaultGraphs, namedGraphs);
		long timeUsed;
		if (this.loadindexinfo) {
			final InputStream in = defaultGraphs.iterator().next().openStream();
			this.dataset = new Dataset(
					this.type,
					this.getMaterializeOntology(),
					this.opt,
					new Dataset.IndicesFactory() {
								@Override
								public Indices createIndices(
										final URILiteral uriLiteral) {
									return new SixIndices(uriLiteral, false);
								}

								@Override
								public lupos.engine.operators.index.Root createRoot() {
									final RDF3XRoot ic = new RDF3XRoot();
									ic.dataset = RDF3XQueryEvaluator.this.dataset;
									return ic;
								}
							}, in);
			in.close();
			timeUsed = ((new Date()).getTime() - a.getTime());
		} else {
			this.dataset = new Dataset(
					defaultGraphs,
					namedGraphs,
					this.getMaterializeOntology(),
					this.type,
					this.opt,
					new Dataset.IndicesFactory() {
								@Override
								public Indices createIndices(
										final URILiteral uriLiteral) {
									return new SixIndices(uriLiteral);
								}

								@Override
								public lupos.engine.operators.index.Root createRoot() {
									final RDF3XRoot ic = new RDF3XRoot();
									ic.dataset = RDF3XQueryEvaluator.this.dataset;
									return ic;
								}
							}, this.debug != DEBUG.NONE,
					this.inmemoryexternalontologyinference);
			this.dataset.buildCompletelyAllIndices();
			timeUsed = ((new Date()).getTime() - a.getTime());
		}
		if (this.writeindexinfo.compareTo("") != 0) {
			final OutputStream out = new BufferedOutputStream(new FileOutputStream(this.writeindexinfo));
			this.dataset.writeIndexInfo(out, null);
			out.close();
		}
		return timeUsed;
	}


	/**
	 * <p>getRoot.</p>
	 *
	 * @return a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XRoot} object.
	 */
	public RDF3XRoot getRoot() {
		return (RDF3XRoot) this.root;
	}

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		_main(args, RDF3XQueryEvaluator.class);
	}

	/** {@inheritDoc} */
	@Override
	public lupos.engine.operators.index.Root createRoot() {
		final RDF3XRoot ic = new RDF3XRoot();
		ic.dataset = this.dataset;
		return ic;
	}
}
