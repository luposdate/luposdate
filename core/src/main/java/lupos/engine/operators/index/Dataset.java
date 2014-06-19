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
package lupos.engine.operators.index;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSetUsingTrie;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.codemap.CodeMapURILiteral;
import lupos.datastructures.items.literal.codemap.IntegerStringMap;
import lupos.datastructures.items.literal.codemap.StringIntegerMap;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.items.literal.codemap.TProcedureEntry;
import lupos.datastructures.items.literal.string.StringURILiteral;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StringIntegerNodeDeSerializer;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.Tuple;
import lupos.misc.util.ImmutableIterator;

public class Dataset {

	/**
	 * TRUE:  Create Named Graph/Default Graph if not existing but in the case of access
	 * FALSE: NOP
	 */
	public static boolean CREATEGRAPHSIFNOTEXISTING = false;

	public enum SORT {
		NORMAL, STRINGSEARCHTREE
	}

	public enum ONTOLOGY {
		NONE, RDFS, RUDIMENTARYRDFS, ALTERNATIVERDFS, ONTOLOGYRDFS, ONTOLOGYRUDIMENTARYRDFS, ONTOLOGYALTERNATIVERDFS
	};

	/**
	 * @author groppe
	 *
	 */
	public interface IndicesFactory {
		Indices createIndices(URILiteral uriLiteral);

		public lupos.engine.operators.index.Root createRoot();
	}

	private static final int k = 500; // 5; // 200;
	private static final int k_ = 500; // 5; // 200;

	private static SORT SortingApproach = SORT.NORMAL;

	private static boolean cachedDBBPTree = true;

	private final Map<URILiteral, Indices> defaultGraphData = new HashMap<URILiteral, Indices>();

	private final Map<URILiteral, Indices> namedGraphData = new HashMap<URILiteral, Indices>();

	private IndicesFactory indicesFactory;

	private String dataFormat;

	private ONTOLOGY materialize;

	private int opt;

	private Thread codeMapConstructionThread = null;

	public IndicesFactory getIndicesFactory() {
		return this.indicesFactory;
	}

	public Dataset(final String dataFormat, final ONTOLOGY materialize,
			final int opt, final IndicesFactory indicesFactory,
			final InputStream in) throws IOException,
			ClassNotFoundException, URISyntaxException {
		this.indicesFactory = indicesFactory;
		this.dataFormat = dataFormat;
		this.materialize = materialize;
		this.opt = opt;

		this.readIndexInfo(in);
	}

	private void init(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs, final String dataFormat,
			final ONTOLOGY materialize, final int opt,
			final IndicesFactory indicesFactory, final boolean debug,
			final boolean inMemoryExternalOntologyComputation, final Collection<String> toAddToRdftermsRepresentations) throws Exception {
		if(defaultGraphs.size()==0){
			defaultGraphs.add(new StringURILiteral("<inlinedata:>"));
		}
		this.indicesFactory = indicesFactory;
		this.dataFormat = dataFormat;
		this.materialize = materialize;
		this.opt = opt;

		if (LiteralFactory.getMapType() == MapType.LAZYLITERAL
				|| LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {

			this.codeMapConstructionThread = new Thread() {
				@Override
				public void run() {
					final SortedSet<String> rdftermsRepresentations = (SortingApproach == SORT.NORMAL) ? new DBMergeSortedSet<String>(
							new SortConfiguration(), String.class)
							: new DBMergeSortedSetUsingTrie(
									new SortConfiguration(), String.class);

							if(toAddToRdftermsRepresentations!=null){
								for(final String s: toAddToRdftermsRepresentations){
									rdftermsRepresentations.add(s);
								}
							}
							final TripleConsumer tc = new TripleConsumer() {

								@Override
								public void consume(final Triple triple) {
									for (final Literal l : triple) {
										// rdftermsRepresentations.add(l.
										// originalString());
										rdftermsRepresentations.add(l.toString());
										if (l.originalStringDiffers()) {
											rdftermsRepresentations.add(l
													.originalString());
										}
									}
								}

							};
							for (final URILiteral u : defaultGraphs) {
								Dataset.this.insertUsedStringRepresentations(u, dataFormat,
										rdftermsRepresentations, tc);
							}
							for (final URILiteral u : namedGraphs) {
								Dataset.this.insertUsedStringRepresentations(u, dataFormat,
										rdftermsRepresentations, tc);
							}
							// now generate B+-tree for integer-string map and
							// string-integer
							// map of the codemap!
							final Generator<String, Integer> smsi = new Generator<String, Integer>() {

								@Override
								public Iterator<java.util.Map.Entry<String, Integer>> iterator() {
									return new ImmutableIterator<java.util.Map.Entry<String, Integer>>() {
										Iterator<String> it = rdftermsRepresentations
										.iterator();
										int index = 1;

										@Override
										public boolean hasNext() {
											return this.it.hasNext();
										}

										@Override
										public java.util.Map.Entry<String, Integer> next() {
											if (!this.it.hasNext()) {
												return null;
											} else {
												return new java.util.Map.Entry<String, Integer>() {
													String s = it.next();
													int localIndex = index++;

													@Override
													public String getKey() {
														return this.s;
													}

													@Override
													public Integer getValue() {
														return this.localIndex;
													}

													@Override
													public Integer setValue(
															final Integer arg0) {
														throw new UnsupportedOperationException();
													}

												};
											}
										}
									};
								}

								@Override
								public int size() {
									return rdftermsRepresentations.size();
								}

							};

							if (rdftermsRepresentations instanceof DBMergeSortedSet) {
								((DBMergeSortedSet) rdftermsRepresentations).sort();
							}
							final Thread thread0 = new Thread() {
								@Override
								public void run() {
									lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer> simap;
									try {
										simap = new lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer>(
												k,
												k_,
												new StringIntegerNodeDeSerializer());
										simap.setName("Dictionary: String->Integer");
										simap.generateDBBPTree(smsi);
										LazyLiteral
										.setHm(new StringIntegerMapJava(
												simap));
									} catch (final IOException e) {
										System.err.println(e);
										e.printStackTrace();
									}
								}
							};
							final Thread thread1 = new Thread() {
								@Override
								public void run() {
									StringArray ismap;
									try {
										ismap = new StringArray();
										ismap.generate(rdftermsRepresentations.iterator());
										LazyLiteral.setV(ismap);
									} catch (final IOException e) {
										System.err.println(e);
										e.printStackTrace();
									}
								}
							};
							thread0.start();
							thread1.start();
							try {
								thread0.join();
								thread1.join();
							} catch (final InterruptedException e) {
								System.err.println(e);
								e.printStackTrace();
							}
							if (rdftermsRepresentations instanceof DBMergeSortedSet) {
								((DBMergeSortedSet) rdftermsRepresentations)
								.release();
							}
				}
			};
			this.codeMapConstructionThread.start();
		}

		for (final URILiteral u : defaultGraphs) {
			this.indexingRDFGraph(u, this.defaultGraphData, debug, inMemoryExternalOntologyComputation);
		}
	}

	public Dataset(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs, final String dataFormat,
			final ONTOLOGY materialize, final int opt,
			final IndicesFactory indicesFactory, final boolean debug,
			final boolean inMemoryExternalOntologyComputation) throws Exception {

		this.init(defaultGraphs, namedGraphs, dataFormat, materialize, opt, indicesFactory, debug, inMemoryExternalOntologyComputation, null);

		for (final URILiteral u : namedGraphs) {
			this.indexingRDFGraph(u, this.namedGraphData, debug, inMemoryExternalOntologyComputation);
		}
	}

	public Dataset(final Collection<URILiteral> defaultGraphs,
			final Collection<Tuple<URILiteral, URILiteral>> namedGraphs,
			final ONTOLOGY materialize, final String dataFormat, final int opt,
			final IndicesFactory indicesFactory, final boolean debug,
			final boolean inMemoryExternalOntologyComputation) throws Exception {

		final Collection<URILiteral> urisOfNamedGraphs = new LinkedList<URILiteral>();
		final Collection<String> urisAsStrings = new LinkedList<String>();
		for(final Tuple<URILiteral, URILiteral> tuple: namedGraphs){
			// first is source, second is content!
			urisOfNamedGraphs.add(tuple.getSecond());
			urisAsStrings.add(tuple.getFirst().toString());
		}

		this.init(defaultGraphs, urisOfNamedGraphs, dataFormat, materialize, opt, indicesFactory, debug, inMemoryExternalOntologyComputation, urisAsStrings);

		for (final Tuple<URILiteral, URILiteral> tuple : namedGraphs) {
			this.addNamedGraph(tuple.getFirst(), tuple.getSecond(), debug, inMemoryExternalOntologyComputation);
		}
	}

	private void insertUsedStringRepresentations(final URILiteral u,
			final String dataFormat,
			final SortedSet<String> rdftermsRepresentations,
			final TripleConsumer tc) {
		// rdftermsRepresentations.add(u.originalString());
		rdftermsRepresentations.add(u.toString());
		try {
			CommonCoreQueryEvaluator
					.readTriples(dataFormat, u.openStream(), tc);
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public Indices getNamedGraphIndices(final URILiteral rdfName) {
		// Because CodeMapURILiteral and StringURILiterals do not have the same hash value!
		for(final Entry<URILiteral, Indices> entry: this.namedGraphData.entrySet()){
			if(entry.getKey().equals(rdfName)) {
				return entry.getValue();
			}
		}
		return null;
		// return namedGraphData.get(rdfName);
	}

	public void removeNamedGraphIndices(final URILiteral rdfName) {
		// Because CodeMapURILiteral and StringURILiterals do not have the same hash value!
		final Iterator<Entry<URILiteral, Indices>> it = this.namedGraphData.entrySet().iterator();
		while(it.hasNext()){
			final Entry<URILiteral, Indices> entry = it.next();
			if(entry.getKey().equals(rdfName)) {
				it.remove();
			}
		}
	}

	public Set<URILiteral> getNamedGraphs() {
		return this.namedGraphData.keySet();
	}

	public Collection<Indices> getNamedGraphIndices() {
		return this.namedGraphData.values();
	}

	public Indices getDefaultGraphIndices(final URILiteral rdfName) {
		// Because CodeMapURILiteral and StringURILiterals do not have the same hash value!
		for(final Entry<URILiteral, Indices> entry: this.defaultGraphData.entrySet()){
			if(entry.getKey().equals(rdfName)) {
				return entry.getValue();
			}
		}
		return null;
		// return defaultGraphData.get(rdfName);
	}

	public void removeDefaultGraphIndices(final URILiteral rdfName) {
		// Because CodeMapURILiteral and StringURILiterals do not have the same hash value!
		final Iterator<Entry<URILiteral, Indices>> it = this.defaultGraphData.entrySet().iterator();
		while(it.hasNext()){
			final Entry<URILiteral, Indices> entry = it.next();
			if(entry.getKey().equals(rdfName)) {
				it.remove();
			}
		}
	}

	public Set<URILiteral> getDefaultGraphs() {
		return this.defaultGraphData.keySet();
	}

	public Collection<Indices> getDefaultGraphIndices() {
		return this.defaultGraphData.values();
	}

	public long buildCompletelyAllIndices() {
		final Date a = new Date();
		for (final Indices indices : this.defaultGraphData.values()) {
			indices.constructCompletely();
		}
		for (final Indices indices : this.namedGraphData.values()) {
			indices.constructCompletely();
		}
		final Date b = new Date();
		return b.getTime() - a.getTime();
	}

	public void writeOutAllModifiedPages() throws IOException{
		for (final Indices indices : this.defaultGraphData.values()) {
			indices.writeOutAllModifiedPages();
		}
		for (final Indices indices : this.namedGraphData.values()) {
			indices.writeOutAllModifiedPages();
		}
	}

	public Collection<Indices> indexingRDFGraphs(final Item graphConstraint,
			final boolean debug,
			final boolean inMemoryExternalOntologyComputation, final Root root) {

		final LinkedList<Indices> indicesC = new LinkedList<Indices>();
		Indices indices;

		try {
			// default graph
			if (graphConstraint == null) {
				// default RDF graph is given by SPARQL query
				final List<String> graphs = root.defaultGraphs;
				if (graphs != null && graphs.size() != 0) {
					for (final String graph : graphs) {
						indices = this.indexingRDFGraph(LiteralFactory
								.createURILiteralWithoutLazyLiteral("<"+graph+">"),
								indicesC, debug,
								inMemoryExternalOntologyComputation).getSecond();
					}
					return indicesC;
				}

				// default RDF graph is given from command line
				if (this.defaultGraphData.size() == 0) {
					return null;
				}
				return this.defaultGraphData.values();
			}

			// named graph
			if (!(graphConstraint.isVariable())) {
				final Tuple<Boolean, Indices> tuple = this.indexingRDFGraph((URILiteral) graphConstraint,
						indicesC, debug, inMemoryExternalOntologyComputation);
				if(tuple.getFirst()) {
					this.namedGraphData.put((URILiteral) graphConstraint, tuple.getSecond());
				}
				return indicesC;
			}

			if (graphConstraint.isVariable()) {
				// optimization here
				// ...

				// named RDF graphs are given in SPARQL query
				final List<String> graphs = root.namedGraphs;
				if (graphs != null && graphs.size() != 0) {
					for (final String graph : graphs) {
						final Tuple<Boolean, Indices> tuple = this.indexingRDFGraph(LiteralFactory.createURILiteralWithoutLazyLiteral("<"+graph+">"), indicesC, debug, inMemoryExternalOntologyComputation);
						if(tuple.getFirst()) {
							this.namedGraphData.put(LiteralFactory.createURILiteralWithoutLazyLiteral("<"+graph+">"), tuple.getSecond());
						}
					}
					return indicesC;
				}

				return this.namedGraphData.values();
			}
			return null;
		} catch (final Exception e) {
			System.err.println("Error while loading and indexing RDF graph"+ e);
			return null;
		}
	}

	public void addNamedGraph(final URILiteral graphURI, final URILiteral graphSource, final boolean debug, final boolean inMemoryExternalOntologyComputation) throws Exception{
		Indices indices = this.namedGraphData.get(graphURI);
		if(indices == null){
			indices = this.indicesFactory.createIndices(graphURI);
			this.namedGraphData.put(graphURI, indices);
		}
		if(graphSource.originalString().compareTo("<inlinedata:>")!=0) {
			indices.loadData(graphSource, this.dataFormat, this.materialize, this.indicesFactory,this.opt, this, debug, inMemoryExternalOntologyComputation);
		}
	}

	public Tuple<Boolean,Indices> indexingRDFGraph(final URILiteral graphURI,
			final LinkedList<Indices> indicesC, final boolean debug,
			final boolean inMemoryExternalOntologyComputation) throws Exception {
		final boolean newIndices;
		Indices indices = this.defaultGraphData.get(graphURI);
		if(indices==null) {
			indices=this.getNamedGraphIndices(graphURI);
		}
		if (indices == null) {
			if(Dataset.CREATEGRAPHSIFNOTEXISTING){
				indices = this.indicesFactory.createIndices(graphURI);
				indicesC.add(indices);
				this.indexingRDFGraph(graphURI, indices, debug, inMemoryExternalOntologyComputation);
				newIndices = true;
			} else {
				newIndices = false;
			}
		} else {
			indicesC.add(indices);
			newIndices = false;
		}
		return new Tuple<Boolean, Indices>(newIndices, indices);
	}

	public Indices indexingRDFGraph(final URILiteral graphURI,
			final Map<URILiteral, Indices> graphs, final boolean debug,
			final boolean inMemoryExternalOntologyComputation) throws Exception {
		Indices indices = graphs.get(graphURI);
		if (indices == null) {
			indices = this.indicesFactory.createIndices(graphURI);
			graphs.put(graphURI, indices);
			this.indexingRDFGraph(graphURI, indices, debug,
					inMemoryExternalOntologyComputation);
		} else {
			graphs.put(graphURI, indices);
		}
		return indices;
	}

	public void indexingRDFGraph(final URILiteral graphURI,
			final Indices indices, final boolean debug,
			final boolean inMemoryExternalOntologyComputation) throws Exception {
		if(graphURI.originalString().compareTo("<inlinedata:>")!=0) {
			indices.loadData(graphURI, this.dataFormat, this.materialize, this.indicesFactory,this.opt, this, debug, inMemoryExternalOntologyComputation);
		}
	}

	public void putIntoDefaultGraphs(final URILiteral u, final Indices indices) {
		this.defaultGraphData.put(u, indices);
	}

	public void putIntoNamedGraphs(final URILiteral u, final Indices indices) {
		this.namedGraphData.put(u, indices);
	}

	public int getIdMax() {
		return this.defaultGraphData.size() + this.namedGraphData.size();
	}

	private void readIntegerStringMapAndStringIntegerMap(
			final IntegerStringMap ism, final StringIntegerMap sim,
			final InputStream in) throws IOException {
		final int number = InputHelper.readLuposInt(in);
		for (int i = 0; i < number; i++) {
			final int code = InputHelper.readLuposInt(in);
			final String value = InputHelper.readLuposString(in);
			ism.put(code, value);
			sim.put(value, code);
		}
	}

	public void readIndexInfo(final InputStream in)
			throws IOException, ClassNotFoundException, URISyntaxException {

		lupos.datastructures.paged_dbbptree.DBBPTree.setCurrentFileID(InputHelper.readLuposInt(in));
		if (LiteralFactory.getMapType() == MapType.LAZYLITERAL
				|| LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
			final lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer> dbbptreeSI =lupos.datastructures.paged_dbbptree.DBBPTree.readLuposObject(in);
				dbbptreeSI.setName("Dictionary: String->Integer");
				LazyLiteral.setHm(new StringIntegerMapJava(dbbptreeSI));

				//lupos.datastructures.paged_dbbptree.DBBPTree<Integer, String> dbbptreeIS = lupos.datastructures.paged_dbbptree.DBBPTree.readLuposObject(in);
				//dbbptreeIS.setName("Dictionary: Integer->String");
				//LazyLiteral.setV(new IntegerStringMapJava(dbbptreeIS));
				StringArray.setFileID(1);
				LazyLiteral.setV(StringArray.readLuposStringArray(in));
		} else {
			if (LiteralFactory.getMapType() != MapType.NOCODEMAP
					&& LiteralFactory.getMapType() != MapType.LAZYLITERAL
					&& LiteralFactory.getMapType() != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
					&& LiteralFactory.getMapType() != MapType.PREFIXCODEMAP) {
				this.readIntegerStringMapAndStringIntegerMap(CodeMapLiteral.getV(),
						CodeMapLiteral.getHm(), in);
			}
			if (LiteralFactory.getMapType() != MapType.NOCODEMAP
					&& LiteralFactory.getMapType() != MapType.LAZYLITERAL
					&& LiteralFactory.getMapType() != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
				this.readIntegerStringMapAndStringIntegerMap(CodeMapURILiteral
						.getV(), CodeMapURILiteral.getHm(), in);
			}
		}
		int number = InputHelper.readLuposInt(in);
		for (int i = 0; i < number; i++) {
			final URILiteral uri = (URILiteral) LiteralFactory.readLuposLiteral(in);
			final Indices indices = this.indicesFactory.createIndices(uri);
			indices.readIndexInfo(in);
			this.defaultGraphData.put(uri, indices);
		}
		number = InputHelper.readLuposInt(in);
		for (int i = 0; i < number; i++) {
			final URILiteral uri = (URILiteral) LiteralFactory.readLuposLiteral(in);
			final Indices indices = this.indicesFactory.createIndices(uri);
			indices.readIndexInfo(in);
			this.namedGraphData.put(uri, indices);
		}
	}

	private void writeIntegerStringMap(final IntegerStringMap ism,
			final OutputStream out) throws IOException {
		OutHelper.writeLuposInt(ism.size(), out);
		ism.forEachEntry(new TProcedureEntry<Integer,String>() {
			@Override
			public boolean execute(final Integer arg0, final String arg1) {
				try {
					OutHelper.writeLuposInt(arg0, out);
					OutHelper.writeLuposString(arg1, out);
				} catch (final IOException e) {
					System.err.println(e);
					e.printStackTrace();
				}
				return true;
			}
		});
	}

	public void writeIndexInfo(final OutputStream out, final Integer currentFileID)
			throws IOException {
		this.buildCompletelyAllIndices();
		if(currentFileID==null){
			OutHelper.writeLuposInt(lupos.datastructures.paged_dbbptree.DBBPTree.getCurrentFileID(), out);
		} else {
			OutHelper.writeLuposInt(currentFileID, out);
		}

		if (LiteralFactory.getMapType() == MapType.LAZYLITERAL
				|| LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
				((lupos.datastructures.paged_dbbptree.DBBPTree) ((StringIntegerMapJava) LazyLiteral
						.getHm()).getOriginalMap()).writeLuposObject(out);
				//((lupos.datastructures.paged_dbbptree.DBBPTree) ((IntegerStringMapJava) LazyLiteral
				//		.getV()).getOriginalMap()).writeLuposObject(out);
				((StringArray)LazyLiteral.getV()).writeLuposStringArray(out);
		} else {
			if (LiteralFactory.getMapType() != MapType.NOCODEMAP
					&& LiteralFactory.getMapType() != MapType.LAZYLITERAL
					&& LiteralFactory.getMapType() != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
					&& LiteralFactory.getMapType() != MapType.PREFIXCODEMAP) {
				this.writeIntegerStringMap(CodeMapLiteral.getV(), out);
			}
			if (LiteralFactory.getMapType() != MapType.NOCODEMAP
					&& LiteralFactory.getMapType() != MapType.LAZYLITERAL
					&& LiteralFactory.getMapType() != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
				this.writeIntegerStringMap(CodeMapURILiteral.getV(), out);
			}
		}
		OutHelper.writeLuposInt(this.defaultGraphData.size(), out);
		for (final Entry<URILiteral, Indices> entry : this.defaultGraphData.entrySet()) {
			LiteralFactory.writeLuposLiteral(entry.getKey(), out);
			entry.getValue().writeIndexInfo(out);
		}
		OutHelper.writeLuposInt(this.namedGraphData.size(), out);
		for (final Entry<URILiteral, Indices> entry : this.namedGraphData.entrySet()) {
			LiteralFactory.writeLuposLiteral(entry.getKey(), out);
			entry.getValue().writeIndexInfo(out);
		}
	}

	public static SORT getSortingApproach() {
		return SortingApproach;
	}

	public static void setSortingApproach(final SORT SortingApproach) {
		Dataset.SortingApproach = SortingApproach;
	}

	public void waitForCodeMapConstruction() {
		if (this.codeMapConstructionThread != null) {
			try {
				this.codeMapConstructionThread.join();
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}
}
