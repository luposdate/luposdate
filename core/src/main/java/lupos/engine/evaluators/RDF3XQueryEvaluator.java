package lupos.engine.evaluators;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
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
			opt = BasicIndex.MERGEJOIN;
		else if (optimization == Optimizations.BINARY)
			opt = BasicIndex.Binary;
		else if (optimization == Optimizations.NARYMERGEJOIN)
			opt = BasicIndex.NARYMERGEJOIN;
		else if (optimization == Optimizations.NONE)
			opt = BasicIndex.NONE;
	}

	@Override
	public void setupArguments() {
		defaultOptimization = Optimizations.MERGEJOIN;	
		args
				.addBooleanOption(
						"loadindexinfo",
						"Instead of importing data, the indices of a previous run of RDF3XQueryEvaluator will be used. For this purpose, the input file must contain the file written via --writeindexinfo in the previous run. See also --writeindexinfo File.",
						false);
		args
				.addStringOption(
						"writeindexinfo",
						"Information about the used indices are written to a given file. This file can be later used to directly use the previously constructed indices instead of importing the data again. See also --readindexinfo.",
						"");
		super.setupArguments();
	}

	@Override
	public void init() throws Exception {
		super.init();
		init(args.getBool("loadindexinfo"), args
				.getString("writeindexinfo"),
				(Optimizations) this.args.getEnum("optimization"));
	}
	
	public void loadLargeScaleIndices(final String dir) throws Exception{
		String datafile=dir+"/"+INDICESINFOFILE;
		
		this.setupArguments();
		this.getArgs().set("debug", DEBUG.NONE);
		this.getArgs().set("result", QueryResult.TYPE.MEMORY);
		this.getArgs()
				.set("codemap",
						LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
		this.getArgs().set("distinct",
				CommonCoreQueryEvaluator.DISTINCT.DBSETBLOCKING);
		this.getArgs().set("optional",
				CommonCoreQueryEvaluator.JOIN.HASH);
		this.getArgs().set("join", CommonCoreQueryEvaluator.JOIN.HASH);
		this.getArgs().set("datastructure",
				Indices.DATA_STRUCT.DBBPTREE);
		this.getArgs().set("tmpDir", dir);
		this.getArgs().set("sortduringindexconstruction",
				Dataset.SORT.STRINGSEARCHTREE);
		this.getArgs().set("loadindexinfo", true);
		this.init();

		// load indices!
		final URILiteral rdfURL = LiteralFactory
				.createStringURILiteral("<file:" + datafile + ">");
		final LinkedList<URILiteral> defaultGraphs = new LinkedList<URILiteral>();
		defaultGraphs.add(rdfURL);
		System.out.println("Load indices!");
		this.prepareInputData(defaultGraphs,
				new LinkedList<URILiteral>());

	}

	@Override
	public long prepareInputData(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs) throws Exception {
		final Date a = new Date();
		super.prepareInputData(defaultGraphs, namedGraphs);
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
			dataset.buildCompletelyAllIndices();
			timeUsed = ((new Date()).getTime() - a.getTime());
		}
		if (writeindexinfo.compareTo("") != 0) {
			final LuposObjectOutputStream out = new LuposObjectOutputStream(
					new BufferedOutputStream(new FileOutputStream(
							writeindexinfo)));
			dataset.writeIndexInfo(out);
			out.close();
		}
		return timeUsed;
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
			final LuposObjectOutputStream out = new LuposObjectOutputStream(
					new BufferedOutputStream(new FileOutputStream(
							writeindexinfo)));
			dataset.writeIndexInfo(out);
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