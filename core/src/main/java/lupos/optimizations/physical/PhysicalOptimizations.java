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
package lupos.optimizations.physical;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.multiinput.join.HashMapIndexJoin;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.join.MergeJoinWithoutSorting;
import lupos.engine.operators.multiinput.join.MergeJoinWithoutSortingSeveralIterations;
import lupos.engine.operators.multiinput.mergeunion.MergeUnion;
import lupos.engine.operators.multiinput.minus.Minus;
import lupos.engine.operators.multiinput.optional.MergeWithoutSortingOptional;
import lupos.engine.operators.multiinput.optional.Optional;
import lupos.engine.operators.singleinput.AddComputedBinding;
import lupos.engine.operators.singleinput.EmptyEnv;
import lupos.engine.operators.singleinput.Projection;
import lupos.engine.operators.singleinput.ReplaceVar;
import lupos.engine.operators.singleinput.SIPFilterOperator;
import lupos.engine.operators.singleinput.SIPFilterOperatorIterator;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.singleinput.modifiers.SortLimit;
import lupos.engine.operators.singleinput.modifiers.distinct.Distinct;
import lupos.engine.operators.singleinput.modifiers.distinct.SortedDataDistinct;
import lupos.engine.operators.singleinput.sort.ImmediateSort;
import lupos.engine.operators.singleinput.sort.Sort;
import lupos.engine.operators.singleinput.sort.fastsort.FastSort;
import lupos.engine.operators.stream.Stream;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;

public class PhysicalOptimizations {
	private static HashMap<Class<? extends BasicOperator>, Class<? extends BasicOperator>> replacements = new HashMap<Class<? extends BasicOperator>, Class<? extends BasicOperator>>();
	private static HashMap<Class<? extends BasicOperator>, Class<? extends BasicOperator>> replacementsMergeJoinAndMergeOptional = new HashMap<Class<? extends BasicOperator>, Class<? extends BasicOperator>>();

	/**
	 * Adds a rule that replaces a superclass with its implementation (Both
	 * classes have to inherit from Operator)
	 *
	 * @param from
	 *            The (unqualified) name of the class to be replaced.
	 * @param to
	 *            The (unqualified) name of the class to replace it.
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static void addReplacement(final String prefix, final String from,
			final String to) {
		try {
			replacements.put((Class<? extends BasicOperator>) Class
					.forName("lupos.engine.operators." + prefix + from),
					(Class<? extends BasicOperator>) Class
							.forName("lupos.engine.operators." + prefix + to));
		} catch (final ClassNotFoundException ex) {
			System.err
					.println("One of the replacement rules contained a class that does not exist.");
			System.err.println(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public static void addReplacementMergeJoinAndMergeOptional(
			final String prefix, final String from, final String to) {
		try {
			replacementsMergeJoinAndMergeOptional
					.put(
							(Class<? extends BasicOperator>) Class
									.forName("lupos.engine.operators." + prefix
											+ from),
							(Class<? extends BasicOperator>) Class
									.forName("lupos.engine.operators." + prefix
											+ to));
		} catch (final ClassNotFoundException ex) {
			System.err
					.println("One of the replacement rules contained a class that does not exist.");
			System.err.println(ex);
		}
	}

	/**
	 * Call this to add replacement specific to the stream engine
	 */
	public static void streamReplacements() {
		addReplacement("tripleoperator.patternmatcher.", "PatternMatcher", "SimplePatternMatcher");
		addReplacement("singleinput.modifiers.distinct.", "Distinct", "LazyBlockingDistinct");
		// addReplacement("tripleoperator.patternmatcher.","PatternMatcher",
		// "HashPatternMatcher");
	}

	/**
	 * Call this to add replacement rules to support RDFS
	 */
	public static void rdfsReplacements() {
		addReplacement("tripleoperator.patternmatcher.", "PatternMatcher", "RDFSSimplePatternMatcher");
		addReplacement("singleinput.readtriplesdistinct.", "ReadTriplesDistinct", "DBSetBlockingDistinct");
	}

	/**
	 * Call this to add replacement rules to use diskbased operators
	 */
	public static void diskbasedReplacements() {
		addReplacement("multiinput.join.", "Join", "HashMapIndexJoin");
		//addReplacement("multiinput.join.","Join","DBMergeSortedBagMergeJoin");
		// addReplacement("multiinput.optional.","Optional",
		// "DBMergeSortedBagOptional");
		addReplacement("multiinput.optional.", "Optional", "HashOptional");
		addReplacement("singleinput.modifiers.distinct.", "Distinct", "NonBlockingFastDistinct");
		addReplacement("singleinput.sort.", "Sort", "DBMergeSortedBagSort");
	}

	/**
	 * Call this to add replacement rules to use memory operators
	 */
	public static void memoryReplacements() {
		// addReplacement("multiinput.join.","Join","IndexJoin");
		addReplacement("multiinput.join.", "Join", "HashMapIndexJoin");
		//addReplacement("multiinput.optional.","Optional","HashMapIndexOptional"
		// );
		addReplacement("multiinput.optional.", "Optional", "HashMapIndexOptional");
		addReplacement("singleinput.modifiers.distinct.", "Distinct", "NonBlockingFastDistinct");
		addReplacement("singleinput.sort.", "Sort", "TreeMapSort");
	}

	/**
	 * Call this to add replacement rules to use hybrid operators (too large
	 * data sets are stored on disk, otherwise in main memory)
	 */
	public static void hybridReplacements() {
		// addReplacement("multiinput.join.","Join","HybridIndexJoin");
		addReplacement("multiinput.join.", "Join", "HashMapIndexJoin");
		//addReplacement("multiinput.optional.","Optional","HybridIndexOptional"
		// );
		addReplacement("multiinput.optional.", "Optional", "HashOptional");
		addReplacement("singleinput.modifiers.distinct.", "Distinct", "NonBlockingFastDistinct");
		addReplacement("singleinput.sort.", "Sort", "HybridSortedBagSort");
	}

	public static BasicOperator replaceOperators(final BasicOperator op,
			final BasicOperator root) {
		final SimpleOperatorGraphVisitor sogv = new SimpleOperatorGraphVisitor() {
			@Override
			public Object visit(final BasicOperator basicOperator) {
				try {
					if (!(root instanceof PatternMatcher)
							&& !(root instanceof Stream)) {
						if (basicOperator instanceof Distinct) {
							// root.deleteParents();
							// root.setParents();
							// root.detectCycles();
							// root.sendMessage(new BoundVariablesMessage());

							// check if SortedDataDistinct can be used!
							if (basicOperator.getPrecedingOperators().size() == 1
									&& operatorCanReceiveSortedData(basicOperator
											.getPrecedingOperators().get(0),
											basicOperator.getUnionVariables())) {
								operatorMustReceiveSortedData(root, basicOperator
										.getPrecedingOperators().get(0),
										basicOperator.getUnionVariables());
								final BasicOperator newOperator = new SortedDataDistinct();
								newOperator.cloneFrom(basicOperator);
								basicOperator.replaceWith(newOperator);
								return newOperator;
							}

						} else if (basicOperator.getClass() == Join.class) {
							// root.deleteParents();
							// root.setParents();
							// root.detectCycles();
							// root.sendMessage(new BoundVariablesMessage());

							// check if MergeJoinWithoutSorting can be used
							boolean flag = true;
							if (basicOperator.getIntersectionVariables().size() > 0) {
								for (final BasicOperator bo : basicOperator
										.getPrecedingOperators()) {
									flag = flag
									&& operatorCanReceiveSortedData(
											bo,
											basicOperator
											.getIntersectionVariables());
								}
							} else {
								flag = false;
							}
							if (flag) {
								final LinkedList<BasicOperator> llbo = new LinkedList<BasicOperator>();
								llbo.addAll(basicOperator.getPrecedingOperators());
								for (final BasicOperator bo : llbo) {
									operatorMustReceiveSortedData(root, bo,
											basicOperator
											.getIntersectionVariables());
								}
								final BasicOperator newOperator = new MergeJoinWithoutSorting();
								newOperator.cloneFrom(basicOperator);
								basicOperator.replaceWith(newOperator);
								return newOperator;
							} else {
								// do not insert SIP operator in cycles!
								if(!(basicOperator.getCycleOperands()!=null && basicOperator.getCycleOperands().size()>0)){
									// insert SIP operator for joins other than
									// MergeJoinWithoutSorting!
									int min = -1;
									int minIndex = -1;
									int i = 0;
									for (final BasicOperator bo : basicOperator
											.getPrecedingOperators()) {
										final List<TriplePattern> listTps = determineTriplePatterns(
												basicOperator,
												new LinkedList<TriplePattern>(),
												new HashSet<BasicOperator>());
										if (listTps.size() > 0) {
											final TriplePattern tp = listTps.get(0);
											int current = 0;
											for (final OperatorIDTuple oid : root
													.getSucceedingOperators()) {
												if (oid.getOperator() instanceof BasicIndexScan) {
													if (((BasicIndexScan) oid.getOperator())
															.getTriplePattern()
															.contains(tp)) {
														if (min == -1 || min > current) {
															min = current;
															minIndex = i;
														}
													}
												}
												current++;
											}
										}
										i++;
									}
									if (minIndex > -1) {
										final BasicOperator bo = basicOperator.getPrecedingOperators().get(minIndex);
										if (bo != null && bo.getSucceedingOperators().size()==1) {
											if (!this.severalTimesQueryResults(bo, new HashSet<BasicOperator>())) {
												List<TriplePattern> tpsOfOthers = null;
												for (final BasicOperator others : basicOperator
														.getPrecedingOperators()) {
													if (!others.equals(bo)) {
														if (tpsOfOthers == null) {
															tpsOfOthers = determineTriplePatterns(
																	others,
																	new LinkedList<TriplePattern>(),
																	new HashSet<BasicOperator>());
														} else {
															tpsOfOthers
															.addAll(determineTriplePatterns(
																	others,
																	new LinkedList<TriplePattern>(),
																	new HashSet<BasicOperator>()));
														}
													}
												}
												this.tpsOfSucceedingJoins(basicOperator, tpsOfOthers);
												if (tpsOfOthers != null) {
													final SIPFilterOperator sip_op = (replacements
															.get(Join.class) == HashMapIndexJoin.class) ? new SIPFilterOperatorIterator(
																	tpsOfOthers,
																	basicOperator
																	.getIntersectionVariables())

													: new SIPFilterOperator(tpsOfOthers, basicOperator.getIntersectionVariables());

													final List<Variable> intersectionVariables = new LinkedList<Variable>();
													final List<Variable> unionVariables = new LinkedList<Variable>();
													intersectionVariables.addAll(bo.getIntersectionVariables());
													unionVariables.addAll(bo.getUnionVariables());
													sip_op.setIntersectionVariables(intersectionVariables);
													sip_op.setUnionVariables(unionVariables);
													sip_op.addSucceedingOperators(bo.getSucceedingOperators());
													sip_op.setPrecedingOperator(bo);
													bo.setSucceedingOperator(new OperatorIDTuple(sip_op, 0));
													basicOperator.removePrecedingOperator(bo);
													basicOperator.addPrecedingOperator(sip_op);
												}
											}
										}
									}
								}
							}
						} else if (basicOperator.getClass() == Optional.class) {
							// root.deleteParents();
							// root.setParents();
							// root.detectCycles();
							// root.sendMessage(new BoundVariablesMessage());

							// check if MergeWithoutSortingOptional can be used
							boolean flag = true;
							if (basicOperator.getIntersectionVariables().size() > 0) {
								for (final BasicOperator bo : basicOperator
										.getPrecedingOperators()) {
									flag = flag
									&& operatorCanReceiveSortedData(
											bo,
											basicOperator
											.getIntersectionVariables());
								}
							} else {
								flag = false;
							}
							if (flag) {
								final LinkedList<BasicOperator> llbo = new LinkedList<BasicOperator>();
								llbo.addAll(basicOperator.getPrecedingOperators());
								for (final BasicOperator bo : llbo) {
									operatorMustReceiveSortedData(root, bo,
											basicOperator.getIntersectionVariables());
								}
								final BasicOperator newOperator = new MergeWithoutSortingOptional();
								newOperator.cloneFrom(basicOperator);
								basicOperator.replaceWith(newOperator);
								return newOperator;
							}
						} else if (basicOperator instanceof FastSort) {
							if (basicOperator.getPrecedingOperators().size() == 1
									&& !(basicOperator.getPrecedingOperators().get(0) instanceof SIPFilterOperator)
											&& basicOperator.getSucceedingOperators().size() == 1) {
								if (basicOperator.getSucceedingOperators().get(0)
										.getOperator() instanceof Join) {
									final Join join = (Join) basicOperator
									.getSucceedingOperators().get(0)
									.getOperator();
									int min = -1;
									int minIndex = -1;
									int i = 0;
									for (final BasicOperator bo : join
											.getPrecedingOperators()) {
										final TriplePattern tp = determineTriplePatterns(
												join,
												new LinkedList<TriplePattern>(),
												new HashSet<BasicOperator>())
												.get(0);
										int current = 0;
										for (final OperatorIDTuple oid : root
												.getSucceedingOperators()) {
											if (oid.getOperator() instanceof BasicIndexScan) {
												if (((BasicIndexScan) oid.getOperator())
														.getTriplePattern()
														.contains(tp)) {
													if (min == -1 || min > current) {
														min = current;
														minIndex = i;
													}
												}
											}
											current++;
										}
										i++;
									}
									final BasicOperator bo = join.getPrecedingOperators().get(minIndex);
									if (bo != null
											&& bo instanceof FastSort
											&& bo.getPrecedingOperators().size() == 1
											&& !(bo.getPrecedingOperators().get(0) instanceof SIPFilterOperator)) {
										if (!this.severalTimesQueryResults(bo,
												new HashSet<BasicOperator>())) {
											List<TriplePattern> tpsOfOthers = null;
											for (final BasicOperator others : join
													.getPrecedingOperators()) {
												if (!others.equals(bo)) {
													if (tpsOfOthers == null) {
														tpsOfOthers = determineTriplePatterns(
																others,
																new LinkedList<TriplePattern>(),
																new HashSet<BasicOperator>());
													} else {
														tpsOfOthers
														.addAll(determineTriplePatterns(
																others,
																new LinkedList<TriplePattern>(),
																new HashSet<BasicOperator>()));
													}
												}
											}
											this.tpsOfSucceedingJoins(join, tpsOfOthers);
											final SIPFilterOperator sip_op =
												// (replacements
												// .get(Join.class) ==
												// HashMapIndexJoin.class) ?
												new SIPFilterOperatorIterator(
														tpsOfOthers,
														join.getIntersectionVariables())
											// : new
											// SIPFilterOperator(tpsOfOthers,join
											// .getIntersectionVariables())
											;
											final List<Variable> intersectionVariables = new LinkedList<Variable>();
											final List<Variable> unionVariables = new LinkedList<Variable>();
											intersectionVariables.addAll(bo.getIntersectionVariables());
											unionVariables.addAll(bo.getUnionVariables());
											sip_op.setIntersectionVariables(intersectionVariables);
											sip_op.setUnionVariables(unionVariables);
											if (bo instanceof FastSort) {
												final BasicOperator bo2 = bo
												.getPrecedingOperators()
												.get(0);
												sip_op.addSucceedingOperators(bo2
														.getSucceedingOperators());
												sip_op.setPrecedingOperator(bo2);
												bo2
												.setSucceedingOperator(new OperatorIDTuple(
														sip_op, 0));
												bo.removePrecedingOperator(bo2);
												bo.addPrecedingOperator(sip_op);
											} else {
												sip_op.addSucceedingOperators(bo.getSucceedingOperators());
												sip_op.setPrecedingOperator(bo);
												bo.setSucceedingOperator(new OperatorIDTuple(sip_op, 0));
												join.removePrecedingOperator(bo);
												join.addPrecedingOperator(sip_op);
											}
										}
									}
								}
							}
						} else if (basicOperator instanceof Sort) {
							BasicOperator sortlimit = basicOperator;
							if (basicOperator.getPrecedingOperators().size() == 1) {
								final BasicOperator prec = basicOperator
								.getPrecedingOperators().get(0);
								if (prec instanceof SortLimit) {
									sortlimit = prec;
								}
							}
							final Collection<Variable> sortCriterium = ((Sort) basicOperator)
							.getSortCriterium();
							boolean flag;
							if (sortCriterium != null
									&& (LiteralFactory.getMapType() != LiteralFactory.MapType.LAZYLITERAL)
									&& (LiteralFactory.getMapType() != LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
								flag = true;
								for (final BasicOperator bo : sortlimit
										.getPrecedingOperators()) {
									flag = flag
									&& operatorCanReceiveSortedData(bo,
											sortCriterium);
								}
							} else {
								flag = false;
							}
							if (flag) {
								final LinkedList<BasicOperator> llbo = new LinkedList<BasicOperator>();
								llbo.addAll(sortlimit.getPrecedingOperators());
								for (final BasicOperator bo : llbo) {
									bo.removeSucceedingOperator(sortlimit);
									bo.addSucceedingOperators(basicOperator
											.getSucceedingOperators());
									for (final OperatorIDTuple oID : basicOperator
											.getSucceedingOperators()) {
										oID.getOperator().removePrecedingOperator(
												basicOperator);
										oID.getOperator().addPrecedingOperator(bo);
									}
									operatorMustReceiveSortedData(root, bo,
											sortCriterium);
								}
								return null;
							}
						}
					}
				} catch(final CyclesDuringDeterminationofTriplePatternsException e){
				}

				final Class<? extends BasicOperator> newClass = replacements
				.get(basicOperator.getClass());
				BasicOperator newOperator = basicOperator;
				if (newClass != null) {
					try {
						newOperator = newClass.newInstance();
					} catch (final Exception ex) {
						ex.printStackTrace();
						System.err.println(ex);
					}
					newOperator.cloneFrom(basicOperator);
					basicOperator.replaceWith(newOperator);
					if (basicOperator.getClass() == Join.class
							&& newOperator instanceof MergeJoinWithoutSortingSeveralIterations) {
						// Insert necessary sort operators here...
						final LinkedList<BasicOperator> llbo = new LinkedList<BasicOperator>();
						llbo.addAll(newOperator.getPrecedingOperators());
						for (final BasicOperator bo : llbo) {
							final List<Variable> sortCriterium = new LinkedList<Variable>();
							sortCriterium.addAll(basicOperator
									.getIntersectionVariables());
							if (!(root instanceof PatternMatcher)
									&& !(root instanceof Stream)
									&& operatorCanReceiveSortedData(
											bo,
											basicOperator
											.getIntersectionVariables())) {
								operatorMustReceiveSortedData(root, bo,
										basicOperator
										.getIntersectionVariables());
							} else {
								final ImmediateSort immediateSort = new ImmediateSort(
										sortCriterium);
								final List<Variable> vars = new LinkedList<Variable>();
								vars.addAll(basicOperator
										.getIntersectionVariables());
								immediateSort.setUnionVariables(vars);
								immediateSort.setIntersectionVariables(vars);
								immediateSort.addPrecedingOperator(bo);
								immediateSort
								.addSucceedingOperator(new OperatorIDTuple(
										newOperator, bo
										.getOperatorIDTuple(
												newOperator)
												.getId()));
								bo.getOperatorIDTuple(newOperator).setOperator(
										immediateSort);
								newOperator.removePrecedingOperator(bo);
							}
						}
					}
				}
				return newOperator;
			}

			private void tpsOfSucceedingJoins(final BasicOperator bo,
					List<TriplePattern> tpsOfOthers) throws CyclesDuringDeterminationofTriplePatternsException {
				if (bo.getSucceedingOperators().size() == 1) {
					final BasicOperator sbo = bo.getSucceedingOperators()
					.get(0).getOperator();
					if (sbo instanceof Join) {
						if (sbo.getPrecedingOperators().size() == 2) {
							for (final BasicOperator op : sbo
									.getPrecedingOperators()) {
								if (!op.equals(bo)) {
									if (tpsOfOthers == null) {
										tpsOfOthers = determineTriplePatterns(
												op,
												new LinkedList<TriplePattern>(),
												new HashSet<BasicOperator>());
									} else {
										tpsOfOthers
										.addAll(determineTriplePatterns(
												op,
												new LinkedList<TriplePattern>(),
												new HashSet<BasicOperator>()));
									}
								}
							}
						}
						this.tpsOfSucceedingJoins(sbo, tpsOfOthers);
					}
				}
			}

			private boolean severalTimesQueryResults(
					final BasicOperator basicOperator,
					final Set<BasicOperator> alreadyVisited) {
				if (alreadyVisited.contains(basicOperator)) {
					// loop detected!
					return true;
				}
				alreadyVisited.add(basicOperator);
				if (basicOperator instanceof Union) {
					if (!(basicOperator instanceof MergeUnion)) {
						return true;
					}
				} else if (basicOperator instanceof BasicIndexScan) {
					return false;
				} else {
					if (basicOperator.getPrecedingOperators() != null) {
						for (final BasicOperator predecessor : basicOperator
								.getPrecedingOperators()) {
							if (this.severalTimesQueryResults(predecessor,
									alreadyVisited)) {
								return true;
							}
						}
					}
				}
				return false;
			}
		};
		final BasicOperator newRoot = (BasicOperator) op.visit(sogv);

		// now replace any merge joins and merge optionals with maybe their
		// parallel versions...

		final SimpleOperatorGraphVisitor sogvMergeJoinsAndOptionals = new SimpleOperatorGraphVisitor() {
			@Override
			public Object visit(final BasicOperator basicOperator) {
				final Class<? extends BasicOperator> newClass = replacementsMergeJoinAndMergeOptional
				.get(basicOperator.getClass());
				BasicOperator newOperator = basicOperator;
				if (newClass != null) {
					try {
						newOperator = newClass.newInstance();
					} catch (final Exception ex) {
						ex.printStackTrace();
						System.err.println(ex);
					}
					newOperator.cloneFrom(basicOperator);
					basicOperator.replaceWith(newOperator);
				}
				return newOperator;
			}
		};
		return (BasicOperator) newRoot.visit(sogvMergeJoinsAndOptionals);
	}

	public static boolean operatorCanReceiveSortedData(
			BasicOperator basicOperator, Collection<Variable> sortCriterium) {
		if (sortCriterium == null || sortCriterium.size() == 0) {
			// this case occurs e.g. int the case of computing cartesian
			// products...
			return true;
		}
		// special cases are considered...
		// TODO: more general way!
		while (basicOperator.remainsSortedData(sortCriterium)) {
			sortCriterium=basicOperator.transformSortCriterium(sortCriterium);
			if (basicOperator.getPrecedingOperators().size() > 1) {
				return false;
			}
			basicOperator = basicOperator.getPrecedingOperators().get(0);
		}

		if ((!basicOperator.getIntersectionVariables().containsAll(
				sortCriterium))
				&& (!(basicOperator instanceof Join && basicOperator
						.getUnionVariables().containsAll(sortCriterium)))
						&& !(basicOperator instanceof Optional
								&& getLeftOperand(basicOperator) != null && getLeftOperand(
										basicOperator).getUnionVariables().containsAll(
												sortCriterium))) {
			return false;
		}

		if (basicOperator instanceof RDF3XIndexScan) {
			return true;
		} else if (basicOperator.getClass() == Union.class) {
			for (final BasicOperator before : basicOperator
					.getPrecedingOperators()) {
				if (!operatorCanReceiveSortedData(before, sortCriterium)) {
					return false;
				}
			}
			return true;
		} else if (basicOperator.getClass() == MergeJoinWithoutSorting.class
				|| basicOperator.getClass() == MergeWithoutSortingOptional.class
				|| basicOperator.getClass() == FastSort.class) {
			// is the input data sorted in the right way?
			final Iterator<Variable> it_sortCriteriumVars = sortCriterium.iterator();
			final Iterator<Variable> it_sortCriteriumJoin =
					(basicOperator.getClass() == FastSort.class) ?
							((FastSort) basicOperator).getSortCriterium().iterator()
							: basicOperator.getIntersectionVariables().iterator();
			while (it_sortCriteriumJoin.hasNext()) {
				if (!it_sortCriteriumVars.hasNext()) {
					return false;
				}
				final Variable v1 = it_sortCriteriumJoin.next();
				final Variable v2 = it_sortCriteriumVars.next();
				if (!v1.equals(v2)) {
					return false;
				}
			}
			if (it_sortCriteriumVars.hasNext()) {
				return false;
			}
			return true;
		} else if (basicOperator instanceof Sort) {
			final Collection<Variable> cv = ((Sort) basicOperator)
					.getSortCriterium();
			if (cv == null) {
				return false;
			}
			final Iterator<Variable> itv = cv.iterator();
			for (final Variable v : sortCriterium) {
				if (!itv.hasNext()) {
					return false;
				}
				if (!v.equals(itv.next())) {
					return false;
				}
			}
			if (itv.hasNext()) {
				return false;
			}
			return true;
		} else if (basicOperator.getClass() == Optional.class) {
			if(basicOperator.getIntersectionVariables().containsAll(sortCriterium)) {
				if(basicOperator.getPrecedingOperators().size() == 2) {
					return 	operatorCanReceiveSortedData(basicOperator.getPrecedingOperators().get(0), sortCriterium) &&
							operatorCanReceiveSortedData(basicOperator.getPrecedingOperators().get(1), sortCriterium);
				}
			}
		}
		return false;
	}

	private static BasicOperator getLeftOperand(final BasicOperator bo) {
		BasicOperator result = null;
		for (final BasicOperator boi : bo.getPrecedingOperators()) {
			final OperatorIDTuple oid = boi.getOperatorIDTuple(bo);
			if (oid.getId() == 0) {
				if (result != null) {
					// several left operands currently not supported here!
					return null;
				}
				result = boi;
			}
		}
		return result;
	}

	public static boolean operatorMustReceiveSortedData(
			final BasicOperator root, BasicOperator basicOperator,
			Collection<Variable> sortCriterium) {
		if (sortCriterium == null || sortCriterium.size() == 0) {
			// this case occurs e.g. int the case of computing cartesian
			// products...
			return true;
		}
		// special cases are considered...
		// TODO: more general way!
		while (basicOperator.remainsSortedData(sortCriterium)) {
			sortCriterium=basicOperator.transformSortCriterium(sortCriterium);
			if (basicOperator.getPrecedingOperators().size() > 1) {
				return false;
			}
			basicOperator = basicOperator.getPrecedingOperators().get(0);
		}

		if (basicOperator instanceof RDF3XIndexScan) {
			((RDF3XIndexScan) basicOperator).setCollationOrder(sortCriterium);
			return true;
		} else if (basicOperator.getClass() == Union.class) {
			final LinkedList<BasicOperator> llbo = new LinkedList<BasicOperator>();
			llbo.addAll(basicOperator.getPrecedingOperators());
			for (final BasicOperator before : llbo) {
				operatorMustReceiveSortedData(root, before, sortCriterium);
			}
			final BasicOperator newOperator = new MergeUnion(sortCriterium);
			newOperator.cloneFrom(basicOperator);
			basicOperator.replaceWith(newOperator);
			return true;
		} else if (basicOperator.getClass() == MergeJoinWithoutSorting.class
				|| basicOperator.getClass() == MergeWithoutSortingOptional.class
				|| basicOperator.getClass() == FastSort.class) {
			// is the input data sorted in the right way?
			boolean flag = true;
			final Iterator<Variable> it_sortCriteriumVars = sortCriterium
			.iterator();
			final Iterator<Variable> it_sortCriteriumJoin =
					(basicOperator.getClass() == FastSort.class) ?
							((FastSort) basicOperator).getSortCriterium().iterator()
							: basicOperator.getIntersectionVariables().iterator();
			while (it_sortCriteriumJoin.hasNext()) {
				if (!it_sortCriteriumVars.hasNext()) {
					flag = false;
					break;
				}
				final Variable v1 = it_sortCriteriumJoin.next();
				final Variable v2 = it_sortCriteriumVars.next();
				if (!v1.equals(v2)) {
					flag = false;
					break;
				}
			}
			if (it_sortCriteriumVars.hasNext()) {
				flag = false;
			}
			if (flag) {
				return true;
			}
		} else if (basicOperator instanceof Sort) {
			// it has already been checked that it is sorted in the right way!
			return true;
		} else if (basicOperator.getClass() == Optional.class) {
			if(basicOperator.getIntersectionVariables().containsAll(sortCriterium)) {
				if(basicOperator.getPrecedingOperators().size() == 2) {
					final BasicOperator newOperator = new MergeWithoutSortingOptional();
					newOperator.cloneFrom(basicOperator);
					basicOperator.replaceWith(newOperator);
					return 	operatorMustReceiveSortedData(root, basicOperator.getPrecedingOperators().get(0), sortCriterium) &&
							operatorMustReceiveSortedData(root, basicOperator.getPrecedingOperators().get(1), sortCriterium);
				}
			}
		}
		return false;
	}

	public static boolean varsInTriplePatterns(
			final List<TriplePattern> list_tp,
			final Collection<Variable> sortCriterium) {
		for (final Variable v : sortCriterium) {
			boolean flag = false;
			for (final TriplePattern tp : list_tp) {
				if (tp.getVariables().contains(v)) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		return true;
	}

	protected static List<TriplePattern> determineTriplePatterns(
			final BasicOperator basicOperator, final List<TriplePattern> list,
			final Set<BasicOperator> alreadyVisited) throws CyclesDuringDeterminationofTriplePatternsException {
		if(basicOperator.getCycleOperands()!=null && basicOperator.getCycleOperands().size()>0) {
			throw new CyclesDuringDeterminationofTriplePatternsException();
		}
		if (alreadyVisited.contains(basicOperator)) {
			return list;
		}
		alreadyVisited.add(basicOperator);
		// exclude operators, which variables are not necessarily bound in the result
		if(basicOperator instanceof Optional || basicOperator instanceof Minus || basicOperator instanceof Projection || basicOperator instanceof ReplaceVar || basicOperator instanceof EmptyEnv || basicOperator instanceof AddComputedBinding) {
			return list;
		}
		if(basicOperator.getSucceedingOperators().size()>1) {
			return list;
		}
		if (basicOperator instanceof BasicIndexScan) {
			list.addAll(((BasicIndexScan) basicOperator).getTriplePattern());
		} else if (basicOperator instanceof TriplePattern) {
			for(final BasicOperator prec: basicOperator.getPrecedingOperators()){
				if(prec instanceof Generate) {
					throw new CyclesDuringDeterminationofTriplePatternsException();
				}
			list.add((TriplePattern) basicOperator);
			}
		} else {
			if (basicOperator.getPrecedingOperators() != null) {
				for (final BasicOperator predecessor : basicOperator
						.getPrecedingOperators()) {
					determineTriplePatterns(predecessor, list, alreadyVisited);
				}
			}
		}
		return list;
	}

	public static List<TriplePattern> determineUnionFreeTriplePatterns(
			final BasicOperator basicOperator, final List<TriplePattern> list,
			final Set<BasicOperator> alreadyVisited) {
		if (alreadyVisited.contains(basicOperator)) {
			return list;
		}
		alreadyVisited.add(basicOperator);
		if(basicOperator.getSucceedingOperators().size()>1) {
			return list;
		}
		if (basicOperator instanceof BasicIndexScan) {
			list.addAll(((BasicIndexScan) basicOperator).getTriplePattern());
		} else if (basicOperator instanceof Union
				|| basicOperator instanceof Optional || basicOperator instanceof Minus || basicOperator instanceof Projection || basicOperator instanceof ReplaceVar || basicOperator instanceof EmptyEnv || basicOperator instanceof AddComputedBinding) {
			// do not consider triple patterns of unions!
			// as it might lead to errors in MergeJoinSort!
			return list;
		} else {
			if (basicOperator.getPrecedingOperators() != null) {
				for (final BasicOperator predecessor : basicOperator
						.getPrecedingOperators()) {
					determineUnionFreeTriplePatterns(predecessor, list,
							alreadyVisited);
				}
			}
		}
		return list;
	}

	public static class CyclesDuringDeterminationofTriplePatternsException extends Exception {
	}
}
