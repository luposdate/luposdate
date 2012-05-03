package lupos.engine.operators.index;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.bindings.BindingsArrayVarMinMax;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LazyLiteralOriginalContent;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.paged_dbbptree.StandardNodeDeSerializer;
import lupos.datastructures.queryresult.IdIteratorQueryResult;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndex;
import lupos.engine.operators.index.memoryindex.MemoryIndex;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.engine.operators.tripleoperator.TriplePattern.BooleanAndUnknown;
import lupos.misc.Tuple;
import lupos.misc.debug.DebugStep;
import lupos.optimizations.logical.OptimizeJoinOrder;
import lupos.optimizations.logical.statistics.Entry;
import lupos.optimizations.logical.statistics.VarBucket;

/**
 * Instances of this class are used to process queries by using a special index
 * structure for enhancement.<br>
 * The index structure has to be initialized previously. It is stored in a
 * static way so new instantiations of this class to not yield a change in the
 * original index structure.
 * 
 */
public abstract class BasicIndex extends Operator {

	public final static int NONE = 0;
	public final static int MOSTRESTRICTIONS = 1;
	public final static int MOSTRESTRICTIONSLEASTENTRIES = 2;
	public final static int LEASTENTRIES = 3;
	public final static int MERGEJOIN = 4;
	public final static int Binary = 5;
	public final static int MERGEJOINSORT = 6;
	public final static int NARYMERGEJOIN = 7;
	
	protected final IndexCollection indexCollection;

	// Item is Var or Literal (URILiteral)
	protected Item rdfGraph;

	protected Collection<TriplePattern> triplePatterns;

	public BasicIndex(final IndexCollection indexCollection) {
		super();
		this.indexCollection = indexCollection;
	}

	public BasicIndex(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, final Item rdfGraph, final IndexCollection indexCollection) {
		this.succeedingOperators = new LinkedList<OperatorIDTuple>();
		if (succeedingOperator != null) {
			this.succeedingOperators.add(succeedingOperator);
		}
		setTriplePatterns(triplePattern);
		this.rdfGraph = rdfGraph;
		this.indexCollection = indexCollection;
	}

	public BasicIndex(final List<OperatorIDTuple> succeedingOperators,
			final Collection<TriplePattern> triplePattern, final Item rdfGraph, final IndexCollection indexCollection) {
		this.succeedingOperators = succeedingOperators;
		setTriplePatterns(triplePattern);
		this.rdfGraph = rdfGraph;
		this.indexCollection=indexCollection;
	}
	
	public IndexCollection getIndexCollection(){
		return this.indexCollection;
	}
	
	public void recomputeVariables(){
		this.intersectionVariables = new HashSet<Variable>();
		this.unionVariables = new HashSet<Variable>();
		if (this.triplePatterns != null)
			for (final TriplePattern tp : this.triplePatterns) {
				tp.recomputeVariables();
				Collection<Variable> varsOfTP = tp.getUnionVariables();
				this.intersectionVariables.addAll(varsOfTP);
				this.unionVariables.addAll(varsOfTP);
			}
		final Item rdfGraph = getGraphConstraint();
		if (rdfGraph != null && rdfGraph.isVariable()) {
			this.intersectionVariables.add((Variable) rdfGraph);
			this.unionVariables.add((Variable) rdfGraph);
		}
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		recomputeVariables();
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
	public QueryResult process(final int opt, final Dataset dataset) {
		final QueryResult queryResult = join(dataset);
		if (queryResult == null) {
			return null;
		}

		/*
		 * pass the succeeding operators which were externally provided to the
		 * operator pipe along with the new bindings which have been determined
		 * by the join
		 */
		if (succeedingOperators.size() > 1)
			queryResult.materialize();
		// for every binding found in the result of the previously performed
		// join of the triple elements ...
		for (final OperatorIDTuple succOperator : succeedingOperators) {
			// and pass the new QueryResult object along with the current
			// succeeding operator's
			// identifier to the OperatorPipe's process method
			((Operator) succOperator.getOperator()).processAll(queryResult,
					succOperator.getId());
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
			final Item graphConstraintItem = getGraphConstraint();

			// get a collection of indices using the determined graph constraint
			final Collection<Indices> indicesC = dataset.indexingRDFGraphs(
					graphConstraintItem, false, false, this.indexCollection);
			if ((indicesC != null) && !(indicesC.size() == 0)) {

				// if the graph constraint is not null (which means that a named
				// graph is used)
				if (graphConstraintItem != null) {

					if (graphConstraintItem instanceof Variable) {

						final Variable graphConstraint = (Variable) graphConstraintItem;

						// check if named graphs were provided at query time
						if (indexCollection.namedGraphs != null
								&& indexCollection.namedGraphs.size() > 0) {

							// Convert the named graphs' names into URILiterals
							// to be applicable
							// later on
							for (final String name : indexCollection.namedGraphs) {

								final Indices indices = dataset
								.getNamedGraphIndices(LiteralFactory
										.createURILiteralWithoutLazyLiteral(name));

								final Bindings graphConstraintBindings = Bindings
								.createNewInstance();
								final URILiteral rdfName = indices.getRdfName();
								graphConstraintBindings.add(
										graphConstraint, rdfName);
								if (queryResult == null)
									queryResult = QueryResult.createInstance(new AddConstantBindingIterator(graphConstraint, rdfName, join(indices,
											graphConstraintBindings).oneTimeIterator()));
								else
									queryResult.addAll(QueryResult.createInstance(new AddConstantBindingIterator(graphConstraint, rdfName, join(indices,
											graphConstraintBindings).oneTimeIterator())));

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
									if (queryResult == null)
										queryResult = QueryResult.createInstance(new AddConstantBindingIterator(graphConstraint, indices.getRdfName(), join(indices,
												graphConstraintBindings).oneTimeIterator()));
									else
										queryResult.addAll(QueryResult.createInstance(new AddConstantBindingIterator(graphConstraint, indices.getRdfName(), join(indices,
												graphConstraintBindings).oneTimeIterator())));
								}
							}
						}
					}

					// if the graph constraint is an URILiteral fetch the
					// matching indices object
					// but do not bind anything
					else {

						for (final Indices indices : indicesC) {

							if (queryResult == null)
								queryResult = join(indices, Bindings
										.createNewInstance());
							else
								queryResult.addAll(join(indices, Bindings
										.createNewInstance()));
						}
					}
				}

				// otherwise default graphs are used
				else {
					for (final Indices indices : indicesC) {
						if (queryResult == null)
							queryResult = join(indices, Bindings
									.createNewInstance());
						else
							queryResult.addAll(join(indices, Bindings
									.createNewInstance()));
					}
				}
			}
			return queryResult;
		} catch (final Exception e) {
			System.err.println("Error while joining triple patterns: "+ e);
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
		for (final TriplePattern tp : triplePatterns)
			tp.replace(var, item);

	}

	public void optimizeJoinOrder(final int opt, final Dataset dataset) {
		switch (opt) {
		case MOSTRESTRICTIONS:
			optimizeJoinOrderAccordingToMostRestrictions();
			break;
		}
	}

	public void optimizeJoinOrderAccordingToMostRestrictions() {
		if (triplePatterns == null)
			return;
		final HashSet<String> usedVariables = new HashSet<String>();
		final Collection<TriplePattern> remainingTP = new LinkedList<TriplePattern>();
		remainingTP.addAll(triplePatterns);
		final Collection<TriplePattern> newTriplePattern = new LinkedList<TriplePattern>();
		while (remainingTP.size() > 0) {
			int minOpenPositions = 4;
			TriplePattern best = null;
			for (final TriplePattern tp : remainingTP) {
				final HashSet<String> v = tp.getVariableNames();
				v.retainAll(usedVariables);
				final int openPositions = tp.getVariableNames().size()
						- v.size();
				if (openPositions < minOpenPositions) {
					minOpenPositions = openPositions;
					best = tp;
				}
			}
			usedVariables.addAll(best.getVariableNames());
			newTriplePattern.add(best);
			remainingTP.remove(best);
		}
		setTriplePatterns(newTriplePattern);
	}

	public Collection<TriplePattern> getTriplePattern() {
		return triplePatterns;
	}

	public void setTriplePatterns(final Collection<TriplePattern> triplePatterns) {
		this.triplePatterns = triplePatterns;
		final HashSet<Variable> hsv = new HashSet<Variable>();
		intersectionVariables = hsv;
		unionVariables = hsv;
	}

	public void setGraphConstraint(final Item graph) {
		rdfGraph = graph;
	}

	public Item getGraphConstraint() {
		return rdfGraph;
	}

	@Override
	public String toString() {
		String s = "Index Scan on";

		if (this.triplePatterns != null && this.triplePatterns.size()>0)
			for (final TriplePattern tp : this.triplePatterns)
				s += "\n" + tp.toString();
		else
			s += " no triple pattern";

		if (rdfGraph != null) {
			s += "\nGraph" + rdfGraph;
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
		for (final TriplePattern tp : triplePatterns) {
			if (tp.getPos(0).equals(var) || tp.getPos(1).equals(var))
				return true;
			if (tp.getPos(2).equals(var)) {
				final BooleanAndUnknown bau = tp
						.getObjectOriginalStringMayDiffer();
				if (bau == BooleanAndUnknown.UNKNOWN) {
					final Collection<TriplePattern> ztp = this
							.getTriplePattern();
					final Collection<TriplePattern> ctp = new LinkedList<TriplePattern>();
					try {
						ctp.add(tp);
						this.setTriplePatterns(ctp);
						if (this instanceof RDF3XIndex) {
							((RDF3XIndex) this)
									.setCollationOrder(OptimizeJoinOrder
											.getCollationOrder(tp,
													new LinkedList<Variable>()));

						} 
						final QueryResult qr = this.join(indexCollection.dataset);
						if (qr == null) {
							this.setTriplePatterns(ztp);
							return true;
						}
						final Iterator<Bindings> itb = qr.oneTimeIterator();
						try {
							while (itb.hasNext()) {
								final Bindings b = itb.next();
								if (b.get(var).originalStringDiffers())
									return false;
							}
						} finally {
							if (itb instanceof ParallelIterator)
								((ParallelIterator) itb).close();
						}
					} finally {
						this.setTriplePatterns(ztp);
					}
				}
				if (bau == BooleanAndUnknown.TRUE)
					return false;
				else
					return true;
			}
		}
		return true;
	}

	public Tuple<Literal, Literal> getMinMax(final Variable v,
			final TriplePattern tp, final Dataset dataset) {
		if (!(this instanceof RDF3XIndex))
			return null;
		final Collection<TriplePattern> ztp = this.getTriplePattern();
		final Collection<TriplePattern> ctp = new LinkedList<TriplePattern>();
		ctp.add(tp);
		this.setTriplePatterns(ctp);
		final Collection<Variable> cv = new LinkedList<Variable>();
		cv.add(v);
		if (this instanceof RDF3XIndex) {
			((RDF3XIndex) this).setCollationOrder(OptimizeJoinOrder
					.getCollationOrder(tp, cv));

		}
		final QueryResult qr = this.join(dataset);
		if (qr == null) {
			this.setTriplePatterns(ztp);
			return null;
		}
		final Iterator<Bindings> itb = qr.oneTimeIterator();
		if (!itb.hasNext()) {
			this.setTriplePatterns(ztp);
			return null;
		}
		final Literal min = itb.next().get(v);
		if (itb instanceof ParallelIterator)
			((ParallelIterator<Bindings>) itb).close();

		final Tuple<Literal, Literal> result = new Tuple<Literal, Literal>(min,
				null);
		this.setTriplePatterns(ztp);
		return result;
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
									1,
									new StandardNodeDeSerializer<String, VarBucket[]>(
											String.class, VarBucket[].class)));
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
				if (tp.getPos(i) instanceof LazyLiteralOriginalContent)
					key += "\""
							+ ((LazyLiteralOriginalContent) tp.getPos(i))
									.getCodeOriginalContent() + "\"";
				else if (tp.getPos(i) instanceof LazyLiteral)
					key += "\"" + ((LazyLiteral) tp.getPos(i)).getCode() + "\"";
				else
					key += tp.getPos(i).toString();
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
			final TriplePattern tp, final Dataset dataset,
			final Class<? extends Bindings> classBindings,
			final Collection<Variable> joinPartners,
			final HashMap<Variable, Literal> minima,
			final HashMap<Variable, Literal> maxima) {
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
		if (this instanceof RDF3XIndex) {
			((RDF3XIndex) this).setCollationOrder(OptimizeJoinOrder
					.getCollationOrder(tp, joinPartners));
			((RDF3XIndex) this).setMinimaMaxima(minima, maxima);

		} 
		final QueryResult qrSize = this.join(dataset);
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
					if (minArray[pos] == null || minArray[pos] > ll.getCode())
						minArray[pos] = ll.getCode();
					if (maxArray[pos] == null || maxArray[pos] < ll.getCode())
						maxArray[pos] = ll.getCode();
				}
				size++;

				for (int i = 0; i < minArray.length; i++) {
					if (minArray[i] != null)
						tp.addMinMaxLazyLiteral(i, minArray[i], maxArray[i]);
				}
			}
		} else {
			int maxId = 1;
			if (qrSize instanceof IdIteratorQueryResult)
				maxId = ((IdIteratorQueryResult) qrSize).getMaxId();

			final int[][] min = new int[maxId][];
			final int[][] max = new int[maxId][];

			final Iterator<Bindings> itbSize = qrSize.oneTimeIterator();
			while (itbSize.hasNext()) {
				final Bindings b = itbSize.next();
				final Triple t = b.getTriples().iterator().next();
				int id = 0;
				if (qrSize instanceof IdIteratorQueryResult)
					id = ((IdIteratorQueryResult) qrSize).getIDOfLastBinding();				
				size++;
			}
			if (min != null) {
				for (int id = 0; id < maxId; id++) {
					if (min[id] != null)
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
			if (this instanceof RDF3XIndex) {
				((RDF3XIndex) this).setCollationOrder(OptimizeJoinOrder
						.getCollationOrder(tp, cv));
			}

			QueryResult qr = this.join(dataset);

			if (this instanceof MemoryIndex) {
				// additional sorting phase according to variable v needed
				// for
				// relational index approach!
				final DBMergeSortedBag<Bindings> sort = new DBMergeSortedBag<Bindings>(
						HEAPHEIGHT, new Comparator<Bindings>() {
							public int compare(final Bindings arg0,
									final Bindings arg1) {
								return arg0
										.get(v)
										.compareToNotNecessarilySPARQLSpecificationConform(
												arg1.get(v));
							}
						}, Bindings.class);
				final Iterator<Bindings> itb = qr.oneTimeIterator();
				while (itb.hasNext())
					sort.add(itb.next());
				qr = QueryResult.createInstance(sort.iterator());
			}
			if (qr == null)
				return result;

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
					} else
						vb.selectivityOfInterval.add(currentEntry);
				}
			}
			qr.release();
			result.put(v, vb);
			if (intermediate != null)
				intermediate.put(v, vb);
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
			if (intermediate != null)
				storeVarBuckets(tp, intermediate, key);
			else
				storeVarBuckets(tp, result, key);
		}
		return result;
	}

	public Map<Variable, VarBucket> getVarBuckets(final TriplePattern tp,
			final Dataset dataset,
			final Class<? extends Bindings> classBindings,
			final Collection<Variable> joinPartners,
			final HashMap<Variable, Literal> minima,
			final HashMap<Variable, Literal> maxima) {
		return getVarBucketsOriginal(tp, dataset, classBindings, joinPartners,
				minima, maxima);
	}
	
	/**
	 * Joins the triple pattern using the index maps and returns the result.<br>
	 * The succeeding operators are passed to the operator pipe to be processed.
	 * 
	 * @param opt
	 *            unused parameter
	 * @return the result of the performed join
	 */
	public QueryResult processDebug(final int opt, final Dataset dataset,
			final DebugStep debugstep) {
		final QueryResult queryResult = join(dataset);
		if (queryResult == null) {
			return null;
		}

		/*
		 * pass the succeeding operators which were externally provided to the
		 * operator pipe along with the new bindings which have been determined
		 * by the join
		 */
		if (succeedingOperators.size() > 1)
			queryResult.materialize();
		// for every binding found in the result of the previously performed
		// join of the triple elements ...
		for (final OperatorIDTuple succOperator : succeedingOperators) {
			// and pass the new QueryResult object along with the current
			// succeeding operator's
			// identifier to the OperatorPipe's process method
			final QueryResultDebug qrDebug = new QueryResultDebug(queryResult,
					debugstep, this, succOperator.getOperator(), true);
			((Operator) succOperator.getOperator()).processAllDebug(qrDebug,
					succOperator.getId(), debugstep);
		}
		return queryResult;
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
			} catch (URISyntaxException e) {
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
			if(next!=null)
				return true;
			next = computeNext();
			return (next!=null);
		}

		@Override
		public Bindings next() {
			if(next!=null){
				Bindings znext = next;
				next = null;
				return znext;
			} else return computeNext();			
		}
		
		public Bindings computeNext(){
			Bindings inter;
			do {
				inter = this.originalIterator.next();
				if(inter==null)
					return null;
			} while(inter.get(var)!= null && inter.get(var).compareToNotNecessarilySPARQLSpecificationConform(literal)!=0 && inter.get(var).compareToNotNecessarilySPARQLSpecificationConform(emptyURI)!=0);
			// comparison with emptyURI for running W3C testcases successfully (import of relative URI was meant to be the URI of the named graph!)
			inter.add(var, literal);
			return inter;
		}

		@Override
		public void remove() {
			originalIterator.remove();
		}
		
	}
}
