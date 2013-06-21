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
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.bindings.BindingsArrayVarMinMax;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LazyLiteralOriginalContent;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StringVarBucketArrayNodeDeSerializer;
import lupos.datastructures.queryresult.IdIteratorQueryResult;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.RootChild;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.engine.operators.index.memoryindex.MemoryIndexScan;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.filter.expressionevaluation.Helper;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.engine.operators.tripleoperator.TriplePattern.BooleanAndUnknown;
import lupos.misc.Tuple;
import lupos.optimizations.logical.statistics.Entry;
import lupos.optimizations.logical.statistics.VarBucket;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.operatorgraphgenerator.RDF3XOperatorGraphGenerator;

/**
 * Instances of this class are used to process queries by using a special index
 * structure for enhancement.<br>
 * The index structure has to be initialized previously.
 *
 */
public abstract class BasicIndexScan extends RootChild {

	public final static int NONE = 0;
	public final static int MOSTRESTRICTIONS = 1;
	public final static int MOSTRESTRICTIONSLEASTENTRIES = 2;
	public final static int LEASTENTRIES = 3;
	public final static int MERGEJOIN = 4;
	public final static int BINARY = 5;
	public final static int MERGEJOINSORT = 6;
	public final static int NARYMERGEJOIN = 7;
	public final static int BINARYSTATICANALYSIS = 8;

	protected final Root root;

	// Item is Var or Literal (URILiteral)
	protected Item rdfGraph;

	protected Collection<TriplePattern> triplePatterns;

	public BasicIndexScan(final Root root) {
		super();
		this.root = root;
	}

	public BasicIndexScan(final Root root, final Collection<TriplePattern> triplePatterns) {
		this(root);
		this.setTriplePatterns(triplePatterns);
	}

	public BasicIndexScan(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, final Item rdfGraph, final Root root) {
		this.succeedingOperators = new LinkedList<OperatorIDTuple>();
		if (succeedingOperator != null) {
			this.succeedingOperators.add(succeedingOperator);
		}
		this.rdfGraph = rdfGraph;
		this.setTriplePatterns(triplePattern);
		this.root = root;
	}

	public BasicIndexScan(final List<OperatorIDTuple> succeedingOperators,
			final Collection<TriplePattern> triplePattern, final Item rdfGraph, final Root root) {
		this.succeedingOperators = succeedingOperators;
		this.rdfGraph = rdfGraph;
		this.setTriplePatterns(triplePattern);
		this.root=root;
	}

	public Root getRoot(){
		return this.root;
	}

	public void recomputeVariables(){
		this.intersectionVariables = new HashSet<Variable>();
		this.unionVariables = new HashSet<Variable>();
		if (this.triplePatterns != null) {
			for (final TriplePattern tp : this.triplePatterns) {
				tp.recomputeVariables();
				final Collection<Variable> varsOfTP = tp.getUnionVariables();
				this.intersectionVariables.addAll(varsOfTP);
				this.unionVariables.addAll(varsOfTP);
			}
		}
		final Item rdfGraphLocal = this.getGraphConstraint();
		if (rdfGraphLocal != null && rdfGraphLocal.isVariable()) {
			this.intersectionVariables.add((Variable) rdfGraphLocal);
			this.unionVariables.add((Variable) rdfGraphLocal);
		}
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		this.recomputeVariables();
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		result.getVariables().addAll(this.unionVariables);
		return result;
	}

	/**
	 * Joins the triple pattern using the index maps and returns the result.<br>
	 * The succeeding operators are passed to the operator pipe to be processed.
	 *
	 * @param opt
	 *            unused parameter
	 * @return the result of the performed join
	 */
	@Override
	public QueryResult process(final Dataset dataset) {
		final QueryResult queryResult = this.join(dataset);
		if (queryResult == null) {
			return null;
		}
		return queryResult;
	}

	public QueryResult join(final Dataset dataset) {
		try {

			// create a query result object which will gather the results of the
			// inner join
			QueryResult queryResult = null;

			// get the graph constraint from the super class.
			// If it is null, a default graph is used, if not null a named one
			// is used
			final Item graphConstraintItem = this.getGraphConstraint();

			// get a collection of indices using the determined graph constraint
			final Collection<Indices> indicesC = dataset.indexingRDFGraphs(
					graphConstraintItem, false, false, this.root);
			if ((indicesC != null) && !(indicesC.size() == 0)) {

				// if the graph constraint is not null (which means that a named
				// graph is used)
				if (graphConstraintItem != null) {

					if (graphConstraintItem instanceof Variable) {

						final Variable graphConstraint = (Variable) graphConstraintItem;

						// check if named graphs were provided at query time
						if (this.root.namedGraphs != null
								&& this.root.namedGraphs.size() > 0) {

							// Convert the named graphs' names into URILiterals
							// to be applicable
							// later on
							for (final String name : this.root.namedGraphs) {

								final Indices indices = dataset
								.getNamedGraphIndices(LiteralFactory
										.createURILiteralWithoutLazyLiteral(name));

								final Bindings graphConstraintBindings = Bindings
								.createNewInstance();
								final URILiteral rdfName = indices.getRdfName();
								graphConstraintBindings.add(
										graphConstraint, rdfName);
								if (queryResult == null) {
									queryResult = QueryResult.createInstance(new AddConstantBindingIterator(graphConstraint, rdfName, this.join(indices,
											graphConstraintBindings).oneTimeIterator()));
								} else {
									queryResult.addAll(QueryResult.createInstance(new AddConstantBindingIterator(graphConstraint, rdfName, this.join(indices,
											graphConstraintBindings).oneTimeIterator())));
								}

							}

						}

						// otherwise there might have been named graphs added
						// during the evaluation
						else {

							// get all indices of named graphs and bind them to
							// the graph constraint
							final Collection<Indices> dataSetIndices = dataset
							.getNamedGraphIndices();
							if (dataSetIndices != null) {

								for (final Indices indices : dataSetIndices) {
									final Bindings graphConstraintBindings = Bindings
									.createNewInstance();
									graphConstraintBindings.add(
											graphConstraint, indices
											.getRdfName());
									if (queryResult == null) {
										queryResult = QueryResult.createInstance(new AddConstantBindingIterator(graphConstraint, indices.getRdfName(), this.join(indices,
												graphConstraintBindings).oneTimeIterator()));
									} else {
										queryResult.addAll(QueryResult.createInstance(new AddConstantBindingIterator(graphConstraint, indices.getRdfName(), this.join(indices,
												graphConstraintBindings).oneTimeIterator())));
									}
								}
							}
						}
					}

					// if the graph constraint is an URILiteral fetch the
					// matching indices object
					// but do not bind anything
					else {

						for (final Indices indices : indicesC) {

							if (queryResult == null) {
								queryResult = this.join(indices, Bindings
										.createNewInstance());
							} else {
								queryResult.addAll(this.join(indices, Bindings
										.createNewInstance()));
							}
						}
					}
				}

				// otherwise default graphs are used
				else {
					for (final Indices indices : indicesC) {
						if (queryResult == null) {
							queryResult = this.join(indices, Bindings
									.createNewInstance());
						} else {
							queryResult.addAll(this.join(indices, Bindings
									.createNewInstance()));
						}
					}
				}
			}
			return queryResult;
		} catch (final Exception e) {
			System.err.println("Error while joining triple patterns: "+ e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Performs a join over a collection of triple elements and a provided
	 * bindings object over data of a certain index structure. The collection of
	 * triple patterns is to be retrieved from the super class.
	 *
	 * @param indices
	 *            the index structure which contains the data
	 * @param bindings
	 *            a bindings resulting from the usage of a named graph
	 * @return the result of the join
	 */
	public abstract QueryResult join(Indices indices, Bindings bindings);

	public void replace(final Variable var, final Item item) {
		for (final TriplePattern tp : this.triplePatterns){
			tp.replace(var, item);
		}
	}

	public Collection<TriplePattern> getTriplePattern() {
		return this.triplePatterns;
	}

	public void setTriplePatterns(final Collection<TriplePattern> triplePatterns) {
		this.triplePatterns = triplePatterns;
		this.recomputeVariables();
	}

	public Set<Variable> getVarsInTriplePatterns(){
		final HashSet<Variable> vars = new HashSet<Variable>();
		for(final TriplePattern tp: this.getTriplePattern()){
			for(final Item item: tp){
				if(item.isVariable()){
					vars.add((Variable) item);
				}
			}
		}
		return vars;
	}

	public void setGraphConstraint(final Item graph) {
		this.rdfGraph = graph;
	}

	public Item getGraphConstraint() {
		return this.rdfGraph;
	}

	@Override
	public String toString() {
		String s = "Index Scan on";

		if (this.triplePatterns != null && this.triplePatterns.size()>0) {
			for (final TriplePattern tp : this.triplePatterns) {
				s += "\n" + tp.toString();
			}
		} else {
			s += " no triple pattern";
		}

		if (this.rdfGraph != null) {
			s += "\nGraph" + this.rdfGraph;
		}

		return s;
	}

	@Override
	public String toString(final lupos.rdf.Prefix prefixInstance) {
		final StringBuffer s = new StringBuffer("Index Scan on");

		if (this.triplePatterns != null && this.triplePatterns.size()>0) {
			for (final TriplePattern tp : this.triplePatterns) {
				s.append("\n" + tp.toString(prefixInstance));
			}
		} else {
			s.append(" no triple pattern");
		}

		if (this.rdfGraph != null) {
			s.append("\nGraph" + this.rdfGraph.toString());
		}

		return s.toString();
	}

	public boolean occurInSubjectOrPredicateOrObjectOriginalStringDoesNotDiffer(
			final Variable var) {
		for (final TriplePattern tp : this.triplePatterns) {
			if (tp.getPos(0).equals(var) || tp.getPos(1).equals(var)) {
				return true;
			}
			if (tp.getPos(2).equals(var)) {
				final BooleanAndUnknown bau = tp.getObjectOriginalStringMayDiffer();
				if (bau == BooleanAndUnknown.UNKNOWN) {
					final Collection<TriplePattern> ztp = this.getTriplePattern();
					final Collection<TriplePattern> ctp = new LinkedList<TriplePattern>();
					try {
						ctp.add(tp);
						this.setTriplePatterns(ctp);
						if (this instanceof RDF3XIndexScan) {
							((RDF3XIndexScan) this)
							.setCollationOrder(RDF3XOperatorGraphGenerator.getCollationOrder(tp, new LinkedList<Variable>()));

						}
						final QueryResult qr = this.join(this.root.dataset);
						if (qr == null) {
							this.setTriplePatterns(ztp);
							return true;
						}
						final Iterator<Bindings> itb = qr.oneTimeIterator();
						try {
							while (itb.hasNext()) {
								final Bindings b = itb.next();
								Literal literal = b.get(var);
								if (literal.originalStringDiffers()){
									return false;
								} else if(!LiteralFactory.semanticInterpretationOfLiterals){
									if(literal instanceof LazyLiteral){
										literal = ((LazyLiteral) literal).getLiteral();
									}
									if(literal instanceof LanguageTaggedLiteral){
										final String language = ((LanguageTaggedLiteral)literal).getLanguage();
										if(language.compareTo(language.toUpperCase())!=0){
											return false;
										}
									} else if(literal instanceof TypedLiteral){
										if(Helper.isNumeric(literal)){
											final String content = Helper.unquote(((TypedLiteral) literal).getContent());
											if(content.startsWith("+")){
												return false;
											}
											try{
												final Object type = Helper.getType(literal);
												String content2;
												if(type instanceof BigInteger){
													content2 = Helper.getInteger(literal).toString();
												} else if(type instanceof Double){
													content2 = Helper.getDouble(literal).toString();
												} else if(type instanceof Float){
													content2 = Helper.getFloat(literal).toString();
												} else {
													content2 = Helper.getBigDecimal(literal).toString();
												}
												if(content2.compareTo(content)!=0){
													return false;
												}
											} catch(final Exception e) {
												return false;
											}
										}
									}
								}
							}
						} finally {
							if (itb instanceof ParallelIterator) {
								((ParallelIterator<Bindings>) itb).close();
							}
						}
					} finally {
						this.setTriplePatterns(ztp);
					}
				}
				if (bau == BooleanAndUnknown.TRUE) {
					return false;
				} else {
					return true;
				}
			}
		}
		return true;
	}

	/**
	 * This method is overridden by subclasses, e.g. RDF3XIndexScan
	 * @param triplePattern the triple pattern to be considered
	 * @param variables the variables the minimum and maximum value of which is determined
	 * @return the minimum and maximum values of the given variables, null if determining the minimum and maximum is not supported
	 */
	public Map<Variable, Tuple<Literal, Literal>> getMinMax(final TriplePattern triplePattern, final Collection<Variable> variables) {
		return null;
	}

	protected static final int MaxNumberBuckets = 500;

	private final static int HEAPHEIGHT = 10;

	protected final static Map<String, VarBucket[]> histograms = createHistogramMap();

	protected static Map<String, VarBucket[]> createHistogramMap() {
		try {
			return (Indices.usedDatastructure != Indices.DATA_STRUCT.DBBPTREE) ? null
					: Collections
							.synchronizedMap(new lupos.datastructures.paged_dbbptree.DBBPTree<String, VarBucket[]>(
									1000,
									1000,
									new StringVarBucketArrayNodeDeSerializer()));
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	public static int getMaxNumberBuckets() {
		return MaxNumberBuckets;
	}

	protected static String getKey(final TriplePattern tp) {
		final HashMap<Variable, Variable> alreadyUsedVariables = new HashMap<Variable, Variable>();
		String key = "";
		for (int i = 0; i < 3; i++) {
			if (tp.getPos(i).isVariable()) {
				Variable v = alreadyUsedVariables.get(tp.getPos(i));
				if (v == null) {
					v = new Variable("" + alreadyUsedVariables.size());
					alreadyUsedVariables.put((Variable) tp.getPos(i), v);
				}
				key += v.toString();
			} else {
				if (tp.getPos(i) instanceof LazyLiteralOriginalContent) {
					key += "\""
							+ ((LazyLiteralOriginalContent) tp.getPos(i))
									.getCodeOriginalContent() + "\"";
				} else if (tp.getPos(i) instanceof LazyLiteral) {
					key += "\"" + ((LazyLiteral) tp.getPos(i)).getCode() + "\"";
				} else {
					key += tp.getPos(i).toString();
				}
			}
		}
		return key;
	}

	protected static Map<Variable, VarBucket> getVarBuckets(
			final TriplePattern tp, final VarBucket[] vba) {
		final Map<Variable, VarBucket> map = new HashMap<Variable, VarBucket>();
		final HashSet<Variable> alreadyUsedVariables = new HashSet<Variable>();
		int index = 0;
		for (int i = 0; i < 3; i++) {
			if (tp.getPos(i).isVariable()) {
				if (!alreadyUsedVariables.contains(tp.getPos(i))) {
					alreadyUsedVariables.add((Variable) tp.getPos(i));
					if (vba[index] != null) {
						map.put((Variable) tp.getPos(i), vba[index]);
					}
					index++;
				}
			}
		}
		return map;
	}

	protected void storeVarBuckets(final TriplePattern tp,
			final Map<Variable, VarBucket> map, final String key) {
		final HashSet<Variable> alreadyUsedVariables = new HashSet<Variable>();
		final VarBucket[] vba = new VarBucket[tp.getVariables().size()];
		int index = 0;
		for (int i = 0; i < 3; i++) {
			if (tp.getPos(i).isVariable()) {
				if (!alreadyUsedVariables.contains(tp.getPos(i))) {
					alreadyUsedVariables.add((Variable) tp.getPos(i));
					vba[index] = map.get(tp.getPos(i));
					index++;
				}
			}
		}
		// System.out.println("No hit for " + tp + " with key " + key);
		histograms.put(key, vba);
	}

	public final Map<Variable, VarBucket> getVarBucketsOriginal(
			final TriplePattern tp,
			final Class<? extends Bindings> classBindings,
			final Collection<Variable> joinPartners,
			final Map<Variable, Literal> minima,
			final Map<Variable, Literal> maxima) {
		final HashSet<Variable> joinPartnersTP = new HashSet<Variable>();
		joinPartnersTP.addAll(joinPartners);
		joinPartnersTP.retainAll(tp.getVariables());
		Map<Variable, VarBucket> intermediate = null;
		String key = null;
		if (Indices.usedDatastructure == Indices.DATA_STRUCT.DBBPTREE) {
			key = getKey(tp);
			// System.out.println(key);
			final VarBucket[] vba = histograms.get(key);
			if (vba != null) {
				intermediate = getVarBuckets(tp, vba);
				if (intermediate.keySet().containsAll(joinPartnersTP)) {
					boolean flag = true;
					for (final Variable v : joinPartners) {
						final Literal min = minima.get(v);
						if (intermediate.get(v) == null
								|| min != null
								&& (intermediate.get(v).minimum == null || !min
										.equals(intermediate.get(v).minimum))) {
							flag = false;
							break;
						}
						final Literal max = maxima.get(v);
						if (max != null
								&& (intermediate.get(v).maximum == null || !max
										.equals(intermediate.get(v).maximum))) {
							flag = false;
							break;
						}
					}
					if (flag) {
						intermediate.keySet().retainAll(joinPartnersTP);
						if (classBindings == BindingsArrayVarMinMax.class) {
							for (final Variable v : intermediate.keySet()) {
								final VarBucket vb = intermediate.get(v);
								final Literal l[] = new Literal[vb.selectivityOfInterval
										.size()];
								int indexLiteral = 0;
								for (final Entry entry : vb.selectivityOfInterval) {
									l[indexLiteral] = entry.literal;
									indexLiteral++;
								}
								tp.addHistogram(v, l, vb.getSum());
							}
						}
						return intermediate;
					}
				}
			}
		}
		// first determine the result size of the triple pattern!
		final Map<Variable, VarBucket> result = new HashMap<Variable, VarBucket>();
		final Collection<TriplePattern> ztp = this.getTriplePattern();
		final Collection<TriplePattern> ctp = new LinkedList<TriplePattern>();
		ctp.add(tp);
		this.setTriplePatterns(ctp);
		if (this instanceof RDF3XIndexScan) {
			((RDF3XIndexScan) this).setCollationOrder(RDF3XOperatorGraphGenerator.getCollationOrder(tp, joinPartners));
			((RDF3XIndexScan) this).setMinimaMaxima(minima, maxima);

		}
		final QueryResult qrSize = this.join(this.root.dataset);
		if (qrSize == null) {
			// System.out.println("No result for " + tp);
			return null;
		}

		int size = 0;

		if (classBindings == BindingsArrayVarMinMax.class) {
			final Map<Variable, Integer> hmvi = BindingsArray.getPosVariables();
			final Integer[] minArray = new Integer[hmvi.size()];
			final Integer[] maxArray = new Integer[hmvi.size()];

			final Iterator<Bindings> itbSize = qrSize.oneTimeIterator();
			while (itbSize.hasNext()) {
				final Bindings b = itbSize.next();
				for (final Variable v : b.getVariableSet()) {
					final LazyLiteral ll = (LazyLiteral) b.get(v);
					final int pos = hmvi.get(v);
					if (minArray[pos] == null || minArray[pos] > ll.getCode()) {
						minArray[pos] = ll.getCode();
					}
					if (maxArray[pos] == null || maxArray[pos] < ll.getCode()) {
						maxArray[pos] = ll.getCode();
					}
				}
				size++;

				for (int i = 0; i < minArray.length; i++) {
					if (minArray[i] != null) {
						tp.addMinMaxLazyLiteral(i, minArray[i], maxArray[i]);
					}
				}
			}
		} else {
			int maxId = 1;
			if (qrSize instanceof IdIteratorQueryResult) {
				maxId = ((IdIteratorQueryResult) qrSize).getMaxId();
			}

			final int[][] min = new int[maxId][];
			final int[][] max = new int[maxId][];

			final Iterator<Bindings> itbSize = qrSize.oneTimeIterator();
			while (itbSize.hasNext()) {
				final Bindings b = itbSize.next();
				final Triple t = b.getTriples().iterator().next();
				int id = 0;
				if (qrSize instanceof IdIteratorQueryResult) {
					id = ((IdIteratorQueryResult) qrSize).getIDOfLastBinding();
				}
				size++;
			}
			for (int id = 0; id < maxId; id++) {
				if (min[id] != null) {
					for (int i = 0; i < min[id].length; i++) {
						tp.addMinMaxPresortingNumbers(i, min[id].length,
								id, min[id][i], max[id][i]);
					}
				}
			}
		}

		qrSize.release();

		if (size == 0) {
			System.out.println("No result for " + tp);
			return null;
		}

		// System.out.println("(Statistics) " + tp + ", " + size
		// + " triples retrieved");
		tp.setCardinality(size);

		for (final Variable v : joinPartnersTP) {
			if (intermediate != null && intermediate.containsKey(v)) {
				boolean flag = true;
				final Literal min = minima.get(v);
				if (intermediate.get(v) == null
						|| min != null
						&& (intermediate.get(v).minimum == null || !min
								.equals(intermediate.get(v).minimum))) {
					flag = false;
				}
				final Literal max = maxima.get(v);
				if (max != null
						&& (intermediate.get(v).maximum == null || !max
								.equals(intermediate.get(v).maximum))) {
					flag = false;
				}
				if (flag) {
					result.put(v, intermediate.get(v));
					continue;
				}
			}
			// get result of triple pattern in the correct sorted way!
			final Collection<Variable> cv = new LinkedList<Variable>();
			cv.add(v);
			if (this instanceof RDF3XIndexScan) {
				((RDF3XIndexScan) this).setCollationOrder(CollationOrder.getCollationOrder(tp, cv));
			}

			QueryResult qr = this.join(this.root.dataset);

			if (this instanceof MemoryIndexScan) {
				// additional sorting phase according to variable v needed
				// for memory index approach!
				final DBMergeSortedBag<Bindings> sort = new DBMergeSortedBag<Bindings>(
						new SortConfiguration(), new Comparator<Bindings>() {
							@Override
							public int compare(final Bindings arg0, final Bindings arg1) {
								return arg0.get(v).compareToNotNecessarilySPARQLSpecificationConform(arg1.get(v));
							}
						}, Bindings.class);
				final Iterator<Bindings> itb = qr.oneTimeIterator();
				while (itb.hasNext()) {
					sort.add(itb.next());
				}
				qr = QueryResult.createInstance(sort.iterator());
			}
			if (qr == null) {
				return result;
			}

			if (size == 0) {
				qr.release();
				return result;
			}
			final double bucketSize = (double) size / MaxNumberBuckets;
			final Iterator<Bindings> ib = qr.oneTimeIterator();
			final VarBucket vb = new VarBucket();
			vb.minimum = minima.get(v);
			vb.maximum = maxima.get(v);
			Entry currentEntry = new Entry();
			Literal lastLiteral = null;
			while (ib.hasNext()) {
				final Bindings b = ib.next();
				final Literal next = b.get(v);
				if (lastLiteral == null
						|| lastLiteral
								.compareToNotNecessarilySPARQLSpecificationConform(next) != 0) {
					currentEntry.distinctLiterals += 1.0;
					lastLiteral = next;
				}
				currentEntry.selectivity += 1.0;
				if (currentEntry.selectivity >= bucketSize) {
					currentEntry.literal = b.get(v);
					if (ib.hasNext()) {
						Bindings b2 = ib.next();
						while (ib.hasNext()
								&& b2
										.get(v)
										.compareToNotNecessarilySPARQLSpecificationConform(
												currentEntry.literal) == 0) {
							currentEntry.selectivity += 1.0;
							b2 = ib.next();
						}
						vb.selectivityOfInterval.add(currentEntry);
						currentEntry = new Entry();
						currentEntry.distinctLiterals = 1.0;
						currentEntry.selectivity = 1.0;
						if (!ib.hasNext()
								&& b2
										.get(v)
										.compareToNotNecessarilySPARQLSpecificationConform(
												next) != 0) {
							currentEntry.literal = b2.get(v);
							vb.selectivityOfInterval.add(currentEntry);
						}
					} else {
						vb.selectivityOfInterval.add(currentEntry);
					}
				}
			}
			qr.release();
			result.put(v, vb);
			if (intermediate != null) {
				intermediate.put(v, vb);
			}
		}
		this.setTriplePatterns(ztp);

		if (classBindings == BindingsArrayVarMinMax.class) {
			for (final Variable v : result.keySet()) {
				final VarBucket vb = result.get(v);
				final Literal l[] = new Literal[vb.selectivityOfInterval.size()];
				int indexLiteral = 0;
				for (final Entry entry : vb.selectivityOfInterval) {
					l[indexLiteral] = entry.literal;
					indexLiteral++;
				}
				tp.addHistogram(v, l, vb.getSum());
			}
		}
		if (Indices.usedDatastructure == Indices.DATA_STRUCT.DBBPTREE) {
			if (intermediate != null) {
				this.storeVarBuckets(tp, intermediate, key);
			} else {
				this.storeVarBuckets(tp, result, key);
			}
		}
		return result;
	}

	public Map<Variable, VarBucket> getVarBuckets(final TriplePattern tp,
			final Class<? extends Bindings> classBindings,
			final Collection<Variable> joinPartners,
			final Map<Variable, Literal> minima,
			final Map<Variable, Literal> maxima) {
		return this.getVarBucketsOriginal(tp, classBindings, joinPartners, minima, maxima);
	}

	/**
	 * Returns whether or not the join order of the triple patterns inside this index scan operator should be optimized.
	 * This is for almost all index scan operator types true (but not for PredicateIndexScan)
	 * @return whether or not the join order of the triple patterns inside this index scan operator should be optimized
	 */
	public boolean joinOrderToBeOptimized(){
		return true;
	}

	public static class AddConstantBindingIterator implements Iterator<Bindings>{

		protected final Variable var;
		protected final Literal literal;
		protected final Iterator<Bindings> originalIterator;
		protected Bindings next = null;
		protected static Literal emptyURI;

		{
			try {
				emptyURI = LiteralFactory.createURILiteral("<>");
			} catch (final URISyntaxException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}

		public AddConstantBindingIterator(final Variable var, final Literal literal, final Iterator<Bindings> originalIterator){
			this.var = var;
			this.literal = literal;
			this.originalIterator = originalIterator;
		}

		@Override
		public boolean hasNext() {
			if(this.next!=null) {
				return true;
			}
			this.next = this.computeNext();
			return (this.next!=null);
		}

		@Override
		public Bindings next() {
			if(this.next!=null){
				final Bindings znext = this.next;
				this.next = null;
				return znext;
			} else {
				return this.computeNext();
			}
		}

		public Bindings computeNext(){
			Bindings inter;
			do {
				inter = this.originalIterator.next();
				if(inter==null) {
					return null;
				}
			} while(inter.get(this.var)!= null && inter.get(this.var).compareToNotNecessarilySPARQLSpecificationConform(this.literal)!=0 && inter.get(this.var).compareToNotNecessarilySPARQLSpecificationConform(emptyURI)!=0);
			// comparison with emptyURI for running W3C testcases successfully (import of relative URI was meant to be the URI of the named graph!)
			inter.add(this.var, this.literal);
			return inter;
		}

		@Override
		public void remove() {
			this.originalIterator.remove();
		}
	}
}
