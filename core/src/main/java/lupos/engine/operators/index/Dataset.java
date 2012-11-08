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
package lupos.engine.operators.index;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Map.Entry;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSetUsingStringSearch;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSetUsingStringSearchReplacementSelection;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.codemap.CodeMapURILiteral;
import lupos.datastructures.items.literal.codemap.IntegerStringMap;
import lupos.datastructures.items.literal.codemap.IntegerStringMapJava;
import lupos.datastructures.items.literal.codemap.StringIntegerMap;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.items.literal.codemap.TProcedureEntry;
import lupos.datastructures.items.literal.string.StringURILiteral;
import lupos.datastructures.paged_dbbptree.StandardNodeDeSerializer;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;
import lupos.misc.Tuple;

public class Dataset {

	public enum SORT {
		NORMAL, STRINGSEARCHTREE, STRINGSEARCHTREEREPLACEMENTSELECTION
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

		public lupos.engine.operators.index.Root createIndexCollection();
	}

	private static final int k = 500; // 5; // 200;
	private static final int k_ = 500; // 5; // 200;

	private static SORT SortingApproach = SORT.NORMAL;

	private static boolean cachedDBBPTree = true;

	private Map<URILiteral, Indices> defaultGraphData = new HashMap<URILiteral, Indices>();

	private Map<URILiteral, Indices> namedGraphData = new HashMap<URILiteral, Indices>();

	private IndicesFactory indicesFactory;

	private String dataFormat;

	private ONTOLOGY materialize;

	private int opt;

	private Thread codeMapConstructionThread = null;

	public IndicesFactory getIndicesFactory() {
		return indicesFactory;
	}

	public Dataset(final String dataFormat, final ONTOLOGY materialize,
			final int opt, final IndicesFactory indicesFactory,
			final LuposObjectInputStream in) throws IOException,
			ClassNotFoundException {
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

			codeMapConstructionThread = new Thread() {
				@Override
				public void run() {					
					final SortedSet<String> rdftermsRepresentations = (SortingApproach == SORT.NORMAL) ? new DBMergeSortedSet<String>(
							Indices.getHEAPHEIGHT(), String.class)
							: (SortingApproach == SORT.STRINGSEARCHTREE) ? new DBMergeSortedSetUsingStringSearch(
									Indices.getHEAPHEIGHT(), String.class)
							: new DBMergeSortedSetUsingStringSearchReplacementSelection(
									Indices.getHEAPHEIGHT(),
									String.class);
									if(toAddToRdftermsRepresentations!=null){
										for(String s: toAddToRdftermsRepresentations){
											rdftermsRepresentations.add(s);
										}								
									}
									final TripleConsumer tc = new TripleConsumer() {

										public void consume(final Triple triple) {
											for (final Literal l : triple) {
												// rdftermsRepresentations.add(l.
												// originalString());
												rdftermsRepresentations.add(l.toString());
												if (l.originalStringDiffers())
													rdftermsRepresentations.add(l
															.originalString());
											}
										}

									};
									for (final URILiteral u : defaultGraphs) {
										insertUsedStringRepresentations(u, dataFormat,
												rdftermsRepresentations, tc);
									}
									for (final URILiteral u : namedGraphs) {
										insertUsedStringRepresentations(u, dataFormat,
												rdftermsRepresentations, tc);
									}
									// now generate B+-tree for integer-string map and
									// string-integer
									// map of the codemap!
									final Generator<String, Integer> smsi = new Generator<String, Integer>() {

										public Iterator<java.util.Map.Entry<String, Integer>> iterator() {
											return new Iterator<java.util.Map.Entry<String, Integer>>() {
												Iterator<String> it = rdftermsRepresentations
												.iterator();
												int index = 1;

												public boolean hasNext() {
													return it.hasNext();
												}

												public java.util.Map.Entry<String, Integer> next() {
													if (!it.hasNext())
														return null;
													else {
														return new java.util.Map.Entry<String, Integer>() {
															String s = it.next();
															int localIndex = index++;

															public String getKey() {
																return s;
															}

															public Integer getValue() {
																return localIndex;
															}

															public Integer setValue(
																	final Integer arg0) {
																throw new UnsupportedOperationException();
															}

														};
													}
												}

												public void remove() {
													throw new UnsupportedOperationException();
												}

											};
										}

										public int size() {
											return rdftermsRepresentations.size();
										}

									};
									final Generator<Integer, String> smis = new Generator<Integer, String>() {

										public Iterator<java.util.Map.Entry<Integer, String>> iterator() {
											return new Iterator<java.util.Map.Entry<Integer, String>>() {
												Iterator<String> it = rdftermsRepresentations
												.iterator();
												int index = 1;

												public boolean hasNext() {
													return it.hasNext();
												}

												public java.util.Map.Entry<Integer, String> next() {
													if (!it.hasNext())
														return null;
													else {
														return new java.util.Map.Entry<Integer, String>() {
															String s = it.next();
															int localIndex = index++;

															public Integer getKey() {
																return localIndex;
															}

															public String getValue() {
																return s;
															}

															public String setValue(
																	final String arg0) {
																throw new UnsupportedOperationException();
															}

														};
													}
												}

												public void remove() {
													throw new UnsupportedOperationException();
												}

											};
										}

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
														new StandardNodeDeSerializer<String, Integer>(
																String.class,
																Integer.class));
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
											lupos.datastructures.paged_dbbptree.DBBPTree<Integer, String> ismap;
											try {
												ismap = new lupos.datastructures.paged_dbbptree.DBBPTree<Integer, String>(
														k,
														k_,
														new StandardNodeDeSerializer<Integer, String>(
																Integer.class,
																String.class));
												ismap.setName("Dictionary: String->Integer");
												ismap.generateDBBPTree(smis);
												LazyLiteral
												.setV(new IntegerStringMapJava(
														ismap));
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
			codeMapConstructionThread.start();
		}

		for (final URILiteral u : defaultGraphs) {
			indexingRDFGraph(u, defaultGraphData, debug, inMemoryExternalOntologyComputation);
		}
	}

	public Dataset(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs, final String dataFormat,
			final ONTOLOGY materialize, final int opt,
			final IndicesFactory indicesFactory, final boolean debug,
			final boolean inMemoryExternalOntologyComputation) throws Exception {

		init(defaultGraphs, namedGraphs, dataFormat, materialize, opt, indicesFactory, debug, inMemoryExternalOntologyComputation, null);	
		
		for (final URILiteral u : namedGraphs) {
			indexingRDFGraph(u, namedGraphData, debug, inMemoryExternalOntologyComputation);
		}
	}
	
	public Dataset(final Collection<URILiteral> defaultGraphs,
			final Collection<Tuple<URILiteral, URILiteral>> namedGraphs, 
			final ONTOLOGY materialize, final String dataFormat, final int opt,
			final IndicesFactory indicesFactory, final boolean debug,
			final boolean inMemoryExternalOntologyComputation) throws Exception {
		
		Collection<URILiteral> urisOfNamedGraphs = new LinkedList<URILiteral>();
		Collection<String> urisAsStrings = new LinkedList<String>();
		for(Tuple<URILiteral, URILiteral> tuple: namedGraphs){
			// first is source, second is content!
			urisOfNamedGraphs.add(tuple.getSecond());
			urisAsStrings.add(tuple.getFirst().toString());
		}

		init(defaultGraphs, urisOfNamedGraphs, dataFormat, materialize, opt, indicesFactory, debug, inMemoryExternalOntologyComputation, urisAsStrings);
		
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
		for(Entry<URILiteral, Indices> entry: namedGraphData.entrySet()){
			if(entry.getKey().equals(rdfName))
				return entry.getValue();
		}
		return null;
		// return namedGraphData.get(rdfName);
	}
	
	public void removeNamedGraphIndices(final URILiteral rdfName) {
		// Because CodeMapURILiteral and StringURILiterals do not have the same hash value!
		Iterator<Entry<URILiteral, Indices>> it = namedGraphData.entrySet().iterator();
		while(it.hasNext()){
			Entry<URILiteral, Indices> entry = it.next();
			if(entry.getKey().equals(rdfName))
				it.remove();
		}
	}

	public Set<URILiteral> getNamedGraphs() {
		return namedGraphData.keySet();
	}

	public Collection<Indices> getNamedGraphIndices() {
		return namedGraphData.values();
	}

	public Indices getDefaultGraphIndices(final URILiteral rdfName) {
		// Because CodeMapURILiteral and StringURILiterals do not have the same hash value!
		for(Entry<URILiteral, Indices> entry: defaultGraphData.entrySet()){
			if(entry.getKey().equals(rdfName))
				return entry.getValue();
		}
		return null;
		// return defaultGraphData.get(rdfName);
	}
	
	public void removeDefaultGraphIndices(final URILiteral rdfName) {
		// Because CodeMapURILiteral and StringURILiterals do not have the same hash value!
		Iterator<Entry<URILiteral, Indices>> it = defaultGraphData.entrySet().iterator();
		while(it.hasNext()){
			Entry<URILiteral, Indices> entry = it.next();
			if(entry.getKey().equals(rdfName))
				it.remove();
		}
	}

	public Set<URILiteral> getDefaultGraphs() {
		return defaultGraphData.keySet();
	}

	public Collection<Indices> getDefaultGraphIndices() {
		return defaultGraphData.values();
	}

	public long buildCompletelyAllIndices() {
		final Date a = new Date();
		for (final Indices indices : defaultGraphData.values()) {
			indices.constructCompletely();
		}
		for (final Indices indices : namedGraphData.values()) {
			indices.constructCompletely();
		}
		final Date b = new Date();
		return b.getTime() - a.getTime();
	}
	
	public void writeOutAllModifiedPages() throws IOException{
		for (final Indices indices : defaultGraphData.values()) {
			indices.writeOutAllModifiedPages();
		}
		for (final Indices indices : namedGraphData.values()) {
			indices.writeOutAllModifiedPages();
		}		
	}

	public Collection<Indices> indexingRDFGraphs(final Item graphConstraint,
			final boolean debug,
			final boolean inMemoryExternalOntologyComputation, Root indexCollection) {

		final LinkedList<Indices> indicesC = new LinkedList<Indices>();
		Indices indices;

		try {
			// default graph
			if (graphConstraint == null) {
				// default RDF graph is given by SPARQL query
				final List<String> graphs = indexCollection.defaultGraphs;
				if (graphs != null && graphs.size() != 0) {
					for (final String graph : graphs) {
						indices = indexingRDFGraph(LiteralFactory
								.createURILiteralWithoutLazyLiteral("<"+graph+">"),
								indicesC, debug,
								inMemoryExternalOntologyComputation).getSecond();
					}
					return indicesC;
				}

				// default RDF graph is given from command line
				if (defaultGraphData.size() == 0)
					return null;
				return defaultGraphData.values();
			}

			// named graph
			if (!(graphConstraint.isVariable())) {
				Tuple<Boolean, Indices> tuple = indexingRDFGraph((URILiteral) graphConstraint,
						indicesC, debug, inMemoryExternalOntologyComputation);
				if(tuple.getFirst())
					namedGraphData.put((URILiteral) graphConstraint, tuple.getSecond());
				return indicesC;
			}

			if (graphConstraint.isVariable()) {
				// optimization here
				// ...

				// named RDF graphs are given in SPARQL query
				final List<String> graphs = indexCollection.namedGraphs;
				if (graphs != null && graphs.size() != 0) {
					for (final String graph : graphs) {
						Tuple<Boolean, Indices> tuple = indexingRDFGraph(LiteralFactory.createURILiteralWithoutLazyLiteral("<"+graph+">"), indicesC, debug, inMemoryExternalOntologyComputation);
						if(tuple.getFirst())
							namedGraphData.put(LiteralFactory.createURILiteralWithoutLazyLiteral("<"+graph+">"), tuple.getSecond());
					}
					return indicesC;
				}

				return namedGraphData.values();
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
			indices = indicesFactory.createIndices(graphURI);
			this.namedGraphData.put(graphURI, indices);
		}
		if(graphSource.originalString().compareTo("<inlinedata:>")!=0)
			indices.loadData(graphSource, dataFormat, materialize, indicesFactory,opt, this, debug, inMemoryExternalOntologyComputation);
	}

	public Tuple<Boolean,Indices> indexingRDFGraph(final URILiteral graphURI,
			final LinkedList<Indices> indicesC, final boolean debug,
			final boolean inMemoryExternalOntologyComputation) throws Exception {
		final boolean newIndices;
		Indices indices = this.defaultGraphData.get(graphURI);
		if(indices==null)
			indices=this.getNamedGraphIndices(graphURI);
		if (indices == null) {
			indices = indicesFactory.createIndices(graphURI);
			indicesC.add(indices);
			indexingRDFGraph(graphURI, indices, debug,
					inMemoryExternalOntologyComputation);
			newIndices = true;
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
			indices = indicesFactory.createIndices(graphURI);
			graphs.put(graphURI, indices);
			indexingRDFGraph(graphURI, indices, debug,
					inMemoryExternalOntologyComputation);
		} else
			graphs.put(graphURI, indices);
		return indices;
	}

	public void indexingRDFGraph(final URILiteral graphURI,
			final Indices indices, final boolean debug,
			final boolean inMemoryExternalOntologyComputation) throws Exception {
		if(graphURI.originalString().compareTo("<inlinedata:>")!=0)
			indices.loadData(graphURI, dataFormat, materialize, indicesFactory,opt, this, debug, inMemoryExternalOntologyComputation);
	}

	// maybe this method should is in other class
	public static QueryResult Binding(final Collection<Triple> tec,
			final TriplePattern tp, final QueryResult queryResult,
			final Indices indices, final BasicIndexScan index) {

		final QueryResult qResult = QueryResult.createInstance();
		QueryResult qr;
		for (final Bindings currentBindings : queryResult) {
			qr = variableBinding(tec, tp, currentBindings, indices, index);
			if (qr != null)
				qResult.addAll(qr);
		}
		return qResult;
	}

	public static QueryResult variableBinding(final Collection<Triple> tec,
			final TriplePattern tp, final Bindings currentBindings,
			final Indices indices, final BasicIndexScan index) {

		final QueryResult qresult = QueryResult.createInstance();

		// join the elements of the collection
		if (indices == null) {
			if (tec == null || tec.size() == 0)
				return null;
			// compute the variable names to be bound
			final Variable[] var = { null, null, null };
			for (int i = 0; i < 3; i++) {
				final Item item = tp.getPos(i);
				// a new binding is only necessary if the variable is still
				// unbound
				if (item.isVariable()
						&& currentBindings.get((Variable) item) == null) {
					var[i] = (Variable) tp.getPos(i);
				}
			}
			for (final Triple triple : tec) {
				final Bindings cB = currentBindings.clone();
				for (int i = 0; i < var.length; i++) {
					if (var[i] != null) {
						cB.add(var[i], triple.getPos(i));
					}
				}
				cB.addTriple(triple);
				qresult.add(cB);
			}
			// replace the previous query results with the new ones
			return qresult;
		}

		// Variable varGraph = indices.getVariable();
		final Item varGraph = index.getGraphConstraint();
		if (varGraph != null && varGraph.isVariable()) {
			final URILiteral graph = indices.getRdfName();
			final URILiteral value = (URILiteral) currentBindings
					.get((Variable) varGraph);
			if (value != null) {
				if (value.compareTo(graph) != 0)
					return null;
			}
		}

		// join the elements of the collection
		if (tec != null) {

			// compute the variable names to be bound
			final Variable[] var = { null, null, null };
			for (int i = 0; i < 3; i++) {
				final Item item = tp.getPos(i);
				// a new binding is only necessary if the variable is still
				// unbound
				if (item.isVariable()
						&& currentBindings.get((Variable) item) == null) {
					var[i] = (Variable) tp.getPos(i);
				}
			}

			for (final Triple triple : tec) {

				final Bindings cB = currentBindings.clone();
				for (int i = 0; i < var.length; i++) {
					if (var[i] != null) {
						cB.add(var[i], triple.getPos(i));
					}
				}
				cB.addTriple(triple);
				if (varGraph != null && varGraph.isVariable()) {
					Literal graphName;
					try {
						graphName = LiteralFactory.createURILiteral("<"
								+ indices.getRdfName() + ">");
						cB.add((Variable) varGraph, graphName);
					} catch (final URISyntaxException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
				qresult.add(cB);
			}
			return qresult;
		}
		return null;
	}

	// public void prepareDataset(final BasicIndex index) {
	// final Collection<TriplePattern> triplePatterns = index
	// .getTriplePattern();
	// for (final TriplePattern triplePattern : triplePatterns) {
	// indexingRDFGraphs(index.getGraphConstraint());
	// }
	// }

	public void putIntoDefaultGraphs(final URILiteral u, final Indices indices) {
		defaultGraphData.put(u, indices);
	}

	public void putIntoNamedGraphs(final URILiteral u, final Indices indices) {
		namedGraphData.put(u, indices);
	}

	public int getIdMax() {
		return defaultGraphData.size() + namedGraphData.size();
	}

	private void readIntegerStringMapAndStringIntegerMap(
			final IntegerStringMap ism, final StringIntegerMap sim,
			final LuposObjectInputStream in) throws IOException {
		final int number = in.readLuposInt();
		for (int i = 0; i < number; i++) {
			final int code = in.readLuposInt();
			final String value = in.readLuposString();
			ism.put(code, value);
			sim.put(value, code);
		}
	}

	public void readIndexInfo(final LuposObjectInputStream in)
			throws IOException, ClassNotFoundException {
		
		lupos.datastructures.paged_dbbptree.DBBPTree.setCurrentFileID(in
					.readLuposInteger());
		if (LiteralFactory.getMapType() == MapType.LAZYLITERAL
				|| LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
			lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer> dbbptreeSI =lupos.datastructures.paged_dbbptree.DBBPTree.readLuposObject(in);
				dbbptreeSI.setName("Dictionary: String->Integer");
				LazyLiteral.setHm(new StringIntegerMapJava(dbbptreeSI));
				
				lupos.datastructures.paged_dbbptree.DBBPTree<Integer, String> dbbptreeIS = lupos.datastructures.paged_dbbptree.DBBPTree.readLuposObject(in);
				dbbptreeIS.setName("Dictionary: Integer->String");
				LazyLiteral.setV(new IntegerStringMapJava(dbbptreeIS));
		} else {
			if (LiteralFactory.getMapType() != MapType.NOCODEMAP
					&& LiteralFactory.getMapType() != MapType.LAZYLITERAL
					&& LiteralFactory.getMapType() != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
					&& LiteralFactory.getMapType() != MapType.PREFIXCODEMAP) {
				readIntegerStringMapAndStringIntegerMap(CodeMapLiteral.getV(),
						CodeMapLiteral.getHm(), in);
			}
			if (LiteralFactory.getMapType() != MapType.NOCODEMAP
					&& LiteralFactory.getMapType() != MapType.LAZYLITERAL
					&& LiteralFactory.getMapType() != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
				readIntegerStringMapAndStringIntegerMap(CodeMapURILiteral
						.getV(), CodeMapURILiteral.getHm(), in);
			}
		}
		int number = in.readLuposInt();
		for (int i = 0; i < number; i++) {
			final URILiteral uri = (URILiteral) LiteralFactory
					.readLuposLiteral(in);
			final Indices indices = indicesFactory.createIndices(uri);
			indices.readIndexInfo(in);
			defaultGraphData.put(uri, indices);
		}
		number = in.readLuposInt();
		for (int i = 0; i < number; i++) {
			final URILiteral uri = (URILiteral) LiteralFactory
					.readLuposLiteral(in);
			final Indices indices = indicesFactory.createIndices(uri);
			indices.readIndexInfo(in);
			namedGraphData.put(uri, indices);
		}
	}

	private void writeIntegerStringMap(final IntegerStringMap ism,
			final LuposObjectOutputStream out) throws IOException {
		out.writeLuposInt(ism.size());
		ism.forEachEntry(new TProcedureEntry<Integer,String>() {
			public boolean execute(final Integer arg0, final String arg1) {
				try {
					out.writeLuposInt(arg0);
					out.writeLuposString(arg1);
				} catch (final IOException e) {
					System.err.println(e);
					e.printStackTrace();
				}
				return true;
			}
		});
	}

	public void writeIndexInfo(final LuposObjectOutputStream out, final Integer currentFileID)
			throws IOException {
		this.buildCompletelyAllIndices();
		if(currentFileID==null){
			out.writeLuposInt(lupos.datastructures.paged_dbbptree.DBBPTree.getCurrentFileID());
		} else {
			out.writeLuposInt(currentFileID);
		}

		if (LiteralFactory.getMapType() == MapType.LAZYLITERAL
				|| LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
				((lupos.datastructures.paged_dbbptree.DBBPTree) ((StringIntegerMapJava) LazyLiteral
						.getHm()).getOriginalMap()).writeLuposObject(out);
				((lupos.datastructures.paged_dbbptree.DBBPTree) ((IntegerStringMapJava) LazyLiteral
						.getV()).getOriginalMap()).writeLuposObject(out);
		} else {
			if (LiteralFactory.getMapType() != MapType.NOCODEMAP
					&& LiteralFactory.getMapType() != MapType.LAZYLITERAL
					&& LiteralFactory.getMapType() != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
					&& LiteralFactory.getMapType() != MapType.PREFIXCODEMAP) {
				writeIntegerStringMap(CodeMapLiteral.getV(), out);
			}
			if (LiteralFactory.getMapType() != MapType.NOCODEMAP
					&& LiteralFactory.getMapType() != MapType.LAZYLITERAL
					&& LiteralFactory.getMapType() != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
				writeIntegerStringMap(CodeMapURILiteral.getV(), out);
			}
		}
		out.writeLuposInt(defaultGraphData.size());
		for (final Entry<URILiteral, Indices> entry : defaultGraphData.entrySet()) {
			LiteralFactory.writeLuposLiteral(entry.getKey(), out);
			entry.getValue().writeIndexInfo(out);
		}
		out.writeLuposInt(namedGraphData.size());
		for (final Entry<URILiteral, Indices> entry : namedGraphData.entrySet()) {
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
		if (codeMapConstructionThread != null)
			try {
				codeMapConstructionThread.join();
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
	}
}
