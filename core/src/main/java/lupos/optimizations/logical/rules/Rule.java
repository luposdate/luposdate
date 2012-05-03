package lupos.optimizations.logical.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.misc.BitVector;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.findsubgraph.FindSubGraph;

public abstract class Rule {

	// /**
	// * Whether or not optimization is used to minimize number of rule
	// * reapplications.
	// */
	// private static final boolean MINIMIZE_RULEREAPPLICATION = true;
	//
	// /**
	// * Length of the comparison bit vectors used to store and compare changes
	// to
	// * the operator graph that were made by already applied rules.
	// */
	// protected final static int BITVECTORLENGHT = 23;
	//
	// protected final static HashMap<Class<? extends BasicOperator>, Integer>
	// classToPosMap =
	//putToClassToPosMap(putToClassToPosMap(putToClassToPosMap(putToClassToPosMap
	// (putToClassToPosMap(putToClassToPosMap(putToClassToPosMap(
	// putToClassToPosMap
	// (putToClassToPosMap(putToClassToPosMap(putToClassToPosMap
	// (putToClassToPosMap
	// (putToClassToPosMap(putToClassToPosMap(putToClassToPosMap
	// (putToClassToPosMap
	// (putToClassToPosMap(putToClassToPosMap(putToClassToPosMap
	// (putToClassToPosMap
	// (putToClassToPosMap(putToClassToPosMap(putToClassToPosMap(null, 0,
	// lupos.engine.operators.BasicOperator.class,
	// lupos.engine.operators.Operator.class),
	// 1,
	// lupos.engine.operators.index.BasicIndex.class,
	// lupos.engine.operators.index.adaptiveindexforjoins.AdaptiveIndex.class,
	// lupos.engine.operators.index.EmptyIndex.class,
	// lupos.engine.operators.index.iceis2007.ICEIS2007Index.class,
	// lupos.engine.operators.index.adaptedRDF3X.RDF3XIndex.class,
	// lupos.engine.operators.index.relational.RelationalIndex.class,
	// lupos.engine.operators.rdfs.index.ToStreamIndex.class,
	// lupos.engine.operators.index.vldb2008.VLDB2008Index.class,
	//lupos.engine.operators.index.relational.OptimizedRelationalIndex.class),2,
	// lupos.engine.operators.multiinput.MultiInputOperator.class),
	// 3,
	// lupos.engine.operators.multiinput.join.Join.class,
	// lupos.engine.operators.multiinput.join.HashJoin.class,
	// lupos.engine.operators.multiinput.join.MergeJoinWithoutSorting.class,
	// lupos.engine.operators.multiinput.join.
	// MergeJoinWithoutSortingSeveralIterations.class,
	// lupos.engine.operators.multiinput.join.NAryMergeJoinWithoutSorting.class,
	// lupos.engine.operators.multiinput.join.NestedLoopJoin.class,
	// lupos.engine.operators.multiinput.join.parallel.ParallelListJoin.class,
	// lupos.engine.operators.multiinput.join.IndexJoin.class,
	// lupos.engine.operators.multiinput.join.DBBPTreeIndexJoin.class,
	// lupos.engine.operators.multiinput.join.HashMapIndexJoin.class,
	// lupos.engine.operators.multiinput.join.HybridIndexJoin.class,
	// lupos.engine.operators.multiinput.join.MergeJoin.class,
	// lupos.engine.operators.multiinput.join.DBMergeSortedBagMergeJoin.class,
	// lupos.engine.operators.multiinput.join.TreeBagMergeJoin.class,
	// lupos.engine.operators.multiinput.join.parallel.ParallelJoin.class,
	//lupos.engine.operators.multiinput.join.parallel.DBBPTreeIndexParallelJoin.
	// class,
	// lupos.engine.operators.multiinput.join.parallel.HashMapIndexParallelJoin.
	// class,
	// lupos.engine.operators.multiinput.join.parallel.HashParallelJoin.class,
	// lupos.engine.operators.multiinput.join.parallel.HybridIndexParallelJoin.
	// class,
	// lupos.engine.operators.multiinput.join.parallel.NestedLoopParallelJoin.
	// class,
	// lupos.engine.operators.multiinput.join.parallel.MergeParallelJoin.class,
	// lupos.engine.operators.multiinput.join.parallel.
	// DBMergeSortedBagMergeParallelJoin.class,
	// lupos.engine.operators.multiinput.join.parallel.
	// MergeParallelJoinWithoutSorting.class,
	// lupos.engine.operators.multiinput.join.parallel.TreeBagMergeParallelJoin.
	// class),
	// 4,
	//lupos.engine.operators.multiinput.optional.parallel.MergeParallelOptional.
	// class,
	// lupos.engine.operators.multiinput.optional.parallel.
	// DBMergeSortedBagParallelOptional.class,
	// lupos.engine.operators.multiinput.optional.parallel.
	// MergeWithoutSortingParallelOptional.class,
	// lupos.engine.operators.multiinput.optional.parallel.
	// TreeBagMergeParallelOptional.class,
	//lupos.engine.operators.multiinput.optional.parallel.ParallelOptional.class
	// ,
	// lupos.engine.operators.multiinput.optional.parallel.
	// DBBPTreeIndexParallelOptional.class,
	// lupos.engine.operators.multiinput.optional.parallel.
	// HashMapIndexParallelOptional.class,
	// lupos.engine.operators.multiinput.optional.parallel.HashParallelOptional.
	// class,
	// lupos.engine.operators.multiinput.optional.parallel.
	// HybridIndexParallelOptional.class,
	//lupos.engine.operators.multiinput.optional.parallel.NaiveParallelOptional.
	// class,
	// lupos.engine.operators.multiinput.optional.Optional.class,
	// lupos.engine.operators.multiinput.optional.DBBPTreeIndexOptional.class,
	//lupos.engine.operators.multiinput.optional.DBMergeSortedBagOptional.class,
	// lupos.engine.operators.multiinput.optional.HashMapIndexOptional.class,
	// lupos.engine.operators.multiinput.optional.HashOptional.class,
	// lupos.engine.operators.multiinput.optional.HybridIndexOptional.class,
	// lupos.engine.operators.multiinput.optional.MergeWithoutSortingOptional.
	// class,
	// lupos.engine.operators.multiinput.optional.NaiveOptional.class,
	// lupos.engine.operators.multiinput.optional.TreeBagOptional.class), 5,
	// lupos.engine.operators.multiinput.Union.class,
	// lupos.engine.operators.multiinput.MergeUnion.class),6,
	// lupos.engine.operators.singleinput.SingleInputOperator.class), 7,
	// lupos.engine.operators.singleinput.AddBinding.class,
	// lupos.engine.operators.singleinput.AddComputedBinding.class),8,
	// lupos.engine.operators.singleinput.AddBindingFromOtherVar.class), 9,
	// lupos.engine.operators.singleinput.Filter.class),10,
	// lupos.engine.operators.singleinput.generate.Generate.class),
	// 11,
	// lupos.engine.operators.singleinput.generate.GenerateAddEnv.class),12,
	// lupos.engine.operators.singleinput.modifiers.Limit.class),13,
	// lupos.engine.operators.singleinput.modifiers.Offset.class),14,
	// lupos.engine.operators.singleinput.Projection.class),15,
	// lupos.engine.operators.singleinput.ReduceEnv.class),16,
	// lupos.engine.operators.singleinput.ReduceEnvFilterVars.class),17,
	// lupos.engine.operators.singleinput.ReplaceLit.class),18,
	// lupos.engine.operators.singleinput.ReplaceVar.class),
	// 19,
	// lupos.engine.operators.singleinput.modifiers.distinct.Distinct.class,
	// lupos.engine.operators.singleinput.modifiers.distinct.BlockingDistinct.
	// class,
	//lupos.engine.operators.singleinput.modifiers.distinct.DBSetBlockingDistinct
	// .class,
	//lupos.engine.operators.singleinput.modifiers.distinct.HashBlockingDistinct
	// .class,
	// lupos.engine.operators.singleinput.modifiers.distinct.
	// HybridBlockingDistinct.class,
	// lupos.engine.operators.singleinput.modifiers.distinct.InMemoryDistinct.
	// class,
	//lupos.engine.operators.singleinput.modifiers.distinct.InMemoryListDistinct
	// .class,
	//lupos.engine.operators.singleinput.modifiers.distinct.LazyBlockingDistinct
	// .class,
	// lupos.engine.operators.singleinput.modifiers.distinct.SortedDataDistinct.
	// class),
	// 20,
	// lupos.engine.operators.singleinput.sort.fastsort.FastSort.class,
	// lupos.engine.operators.singleinput.sort.Sort.class),21,
	// lupos.engine.operators.tripleoperator.TriggerOneTime.class), 22,
	// lupos.engine.operators.tripleoperator.TriplePattern.class);
	//
	// /**
	// * List of bit masks containing one mask for every Operator class that has
	// * an assigned position inside a comparison bit vector and has derived
	// * subclasses. Each mask describes the relations of subclasses of the
	// * respective operator class. That is, inside the mask the bit positions
	// of
	// * all operators that are a (direct or indirect) subclass of the
	// respective
	// * operator class are set to 1. The list contains null for all Operator
	// * classes without any subclass.
	// *
	// * @see
	// lupos.optimizations.logical.rules.Rule#isReapplicationNecessary(BitVector
	// )
	// * @remark keep this list updated for new subclasses!
	// */
	// protected final static BitVector[] ListOfSubClassBitMasks =
	// generateListOfSubClassBitMasks(
	// lupos.engine.operators.BasicOperator.class,
	// lupos.engine.operators.Operator.class, RelationalIndex.class,
	// lupos.engine.operators.index.BasicIndex.class,
	// // lupos.engine.operators.index.IndexCollection.class,
	// MultiInputOperator.class, Join.class, Optional.class, Union.class);

	protected BasicOperator startNode;
	protected Map<BasicOperator, String> subGraphMap;
	protected Map<String, BasicOperator> transformation;
	protected boolean findall = false;
	protected Set<BasicOperator> alreadyAppliedTo = new HashSet<BasicOperator>();

	public Rule() {
		init();
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	public String getDescription() {
		String s = getName();
		if (s == null)
			return null;
		s = s.replaceAll(" ", "");
		s = s.toLowerCase();
		return s + "Rule";
	}

	public Rule[] getRulesToApply(final RuleEngine ruleEngine) {
		return null;
	}

	protected boolean apply(
			final BasicOperator op,
			final Map<Class<? extends BasicOperator>, Set<BasicOperator>> mapStartNodes) {

		final Set<BasicOperator> startNodesToCheck = mapStartNodes
				.get(startNode.getClass());
		if (startNodesToCheck != null) {
			for (final BasicOperator startNodeToCheck : startNodesToCheck) {
				final Map<String, BasicOperator> mso = FindSubGraph
						.checkSubGraph(startNodeToCheck, subGraphMap, startNode);
				if (mso != null) {
					if (!alreadyAppliedTo.contains(mso.get(subGraphMap
							.get(startNode)))
							&& checkPrecondition(mso)) {
						alreadyAppliedTo.add(mso
								.get(subGraphMap.get(startNode)));
						final Tuple<Collection<BasicOperator>, Collection<BasicOperator>> updateMap = transformOperatorGraph(
								mso, op);
						if (updateMap != null) {
							for (final BasicOperator toDelete : updateMap
									.getSecond())
								RuleEngine.deleteFromNodeMap(toDelete,
										mapStartNodes);
							for (final BasicOperator toAdd : updateMap
									.getFirst())
								RuleEngine.addToNodeMap(toAdd, mapStartNodes);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	protected boolean applyDebug(
			final BasicOperator op,
			final Map<Class<? extends BasicOperator>, Set<BasicOperator>> mapStartNodes) {

		final Set<BasicOperator> startNodesToCheck = mapStartNodes
				.get(startNode.getClass());
		if (startNodesToCheck != null) {
			for (final BasicOperator startNodeToCheck : startNodesToCheck) {
				final Map<String, BasicOperator> mso = FindSubGraph
						.checkSubGraph(startNodeToCheck, subGraphMap, startNode);
				if (mso != null) {
					if (!alreadyAppliedTo.contains(mso.get(subGraphMap
							.get(startNode)))
							&& checkPrecondition(mso)) {
						alreadyAppliedTo.add(mso
								.get(subGraphMap.get(startNode)));
						System.out
								.println("Transform operator graph according rule "
										+ this.getClass().getSimpleName()
										+ " with name " + this.getName());
						final Tuple<Collection<BasicOperator>, Collection<BasicOperator>> updateMap = transformOperatorGraph(
								mso, op);
						if (updateMap != null) {
							for (final BasicOperator toDelete : updateMap
									.getSecond())
								RuleEngine.deleteFromNodeMap(toDelete,
										mapStartNodes);
							for (final BasicOperator toAdd : updateMap
									.getFirst())
								RuleEngine.addToNodeMap(toAdd, mapStartNodes);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	protected abstract void init();

	protected abstract boolean checkPrecondition(Map<String, BasicOperator> mso);

	private boolean inSubgraph(final Map<String, BasicOperator> mso,
			final OperatorIDTuple opid) {
		for (final BasicOperator op : mso.values()) {
			if (opid.getOperator() == op)
				return true;
		}
		return false;
	}

	protected Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Map<BasicOperator, BasicOperator> operators = new HashMap<BasicOperator, BasicOperator>();

		// myRule.transformBasicOperatorGraph(mso,rootOperator);
		for (final String label : transformation.keySet()) {
			if (mso.containsKey(label)) {
				final BasicOperator op1 = transformation.get(label);
				final BasicOperator op2 = mso.get(label);
				final List<OperatorIDTuple> succs = op2
						.getSucceedingOperators();
				for (int i = succs.size() - 1; i >= 0; i--) {
					final OperatorIDTuple opID = succs.get(i);
					if (inSubgraph(mso, opID)) {
						succs.remove(i);
						opID.getOperator().removePrecedingOperator(op2);
						if (opID.getOperator().getPrecedingOperators().size() == 0)
							deleted.add(opID.getOperator());
					}
				}
				op2.addSucceedingOperators(op1.getSucceedingOperators());
				operators.put(op1, op2);
			} else {
				final BasicOperator toAdd = transformation.get(label).clone();
				added.add(toAdd);
				operators.put(transformation.get(label), toAdd);
			}
		}
		for (final BasicOperator op : operators.keySet()) {
			final BasicOperator realOp = operators.get(op);
			for (int i = 0; i < realOp.getSucceedingOperators().size(); i++) {
				final OperatorIDTuple succ = realOp.getSucceedingOperators()
						.get(i);
				if (operators.containsKey(succ.getOperator())) {
					realOp.getSucceedingOperators().set(
							i,
							new OperatorIDTuple(operators.get(succ
									.getOperator()), succ.getId()));
					operators.get(succ.getOperator()).addPrecedingOperator(
							realOp);
				}
			}
		}
		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	protected BasicOperator setSucc(final BasicOperator op,
			final OperatorIDTuple... succ) {
		op.setSucceedingOperators(new LinkedList<OperatorIDTuple>());
		for (final OperatorIDTuple opid : succ) {
			op.addSucceedingOperator(opid);
		}
		return op;
	}

	/**
	 * This method performs the actual check to see if a rule needs to be
	 * reapplied after another rule made changes to the operator graph. This
	 * method is not intended to be called directly, instead it is called by the
	 * different subclasses of {@link lupos.optimizations.logical.rules.Rule}
	 * passing the individual comparison vector of the respective subclass as a
	 * paramater. This method exists to keep the logic for the reapplication
	 * check in one place, rather than having it implemented in each and every
	 * subclass.
	 * 
	 * @see lupos.optimizations.logical.rules.Rule#isReapplicationNecessary(BitVector)
	 * @param comparisonVector
	 *            bit vector containtin all operators whose modification inside
	 *            the operator graph leads to a reapplication of the rule in
	 *            question
	 * @param vectorOfChanges
	 *            bit vector containing operators that were changed inside the
	 *            operator graph
	 * @return true if reapplication is necessary
	 */
	protected static boolean performCheckForReapplication(
			final BitVector comparisonVector, final BitVector vectorOfChanges) {
		return vectorOfChanges.oneBitInCommon(comparisonVector);
	}

	/**
	 * Generate a bit vector that is used to optimize the number of rule
	 * reapplications. Every bit position corresponding to one of the classes in
	 * the parameter list is set to 1.
	 * 
	 * @see lupos.optimizations.logical.rules.Rule#isReapplicationNecessary(BitVector)
	 * @param classElements
	 *            list of classes whose positions inside the bitvector should be
	 *            set to 1
	 * @return the generated bit vector
	 */
	// protected static BitVector generateBitVector(
	// final Class<? extends BasicOperator>... classElements) {
	// BitVector subClassBitMask = null;
	// int vectorPos;
	// BitVector bv = new BitVector(BITVECTORLENGHT);
	//
	// for (final Class<? extends BasicOperator> classElem : classElements) {
	// vectorPos = getComparisonVectorPos(classElem);
	// bv.set(vectorPos);
	//
	// // check if subclass bit mask exists for this class, if so use it to
	// // set bits of all subclasses, too
	// subClassBitMask = ListOfSubClassBitMasks[vectorPos];
	// if (subClassBitMask != null) {
	// bv = bv.OR(subClassBitMask);
	// }
	// }
	// return bv;
	// }
	/*
	 * Generate list of bit masks describing subclass relations between operator
	 * classes. For each given operator class inside the parameter list, a bit
	 * mask will be created according to the subclasses of the respective
	 * operator class. For all operator classes that are not contained in the
	 * parameter list the generated list of bit masks will contain 'null'.
	 * 
	 * @see
	 * lupos.optimizations.logical.rules.Rule#setBitsOfClassAndSubClasses(Class,
	 * BitVector)
	 */
	// private static BitVector[] generateListOfSubClassBitMasks(
	// final Class<? extends BasicOperator>... classes) {
	// BitVector[] bitMaskList;
	// int currentPos;
	//
	// bitMaskList = new BitVector[BITVECTORLENGHT];
	//
	// for (final Class<? extends BasicOperator> currentClass : classes) {
	// currentPos = getComparisonVectorPos(currentClass);
	// bitMaskList[currentPos] = new BitVector(BITVECTORLENGHT);
	// setBitsOfClassAndSubClasses(currentClass, bitMaskList[currentPos]);
	// }
	//
	// return bitMaskList;
	// }
	/*
	 * Define subclass relations between operator classes, needed for generating
	 * bit masks describing these relations which are used for optimization of
	 * rule reapplication.
	 * 
	 * @see
	 * lupos.optimizations.logical.rules.Rule#generateListOfSubClassBitMasks
	 * (Class[])
	 */
	/*
	 * private static void setBitsOfClassAndSubClasses( final Class<? extends
	 * BasicOperator> operatorClass, BitVector subClassBitMask) {
	 * 
	 * if (subClassBitMask == null) { subClassBitMask = new
	 * BitVector(BITVECTORLENGHT); }
	 * 
	 * // set mask bit of the class itself
	 * subClassBitMask.set(getComparisonVectorPos(operatorClass));
	 * 
	 * // set mask bits of all direct and indirect subclasses by calling this //
	 * method recursively // keep this list updated for new subclasses! if
	 * (operatorClass.equals(BasicOperator.class)) { // BasicOperator
	 * setBitsOfClassAndSubClasses(lupos.engine.operators.Operator.class,
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.tripleoperator.TripleOperator.class, //
	 * subClassBitMask); } else if (operatorClass.equals(Operator.class)) { //
	 * Operator setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.index.BasicIndex.class, subClassBitMask); //
	 * setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.index.IndexCollection.class, // subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.MultiInputOperator.class,
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.parallel.ParallelOperand.class, //
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.parallel.QueryResultInBlocks.class, //
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.multiinput.join.parallel.ResultCollector.class, //
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.test.SequentialResultCollector.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.SingleInputOperator.class,
	 * subClassBitMask); } else if (operatorClass.equals(BasicIndex.class)) { //
	 * BasicIndex setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.index.adaptiveindexforjoins.AdaptiveIndex.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.index.EmptyIndex.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.index.iceis2007.ICEIS2007Index.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.index.adaptedRDF3X.RDF3XIndex.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.index.relational.RelationalIndex.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.rdfs.index.ToStreamIndex.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.index.vldb2008.VLDB2008Index.class,
	 * subClassBitMask); } else if (operatorClass.equals(RelationalIndex.class))
	 * { // RelationalIndex setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.index.relational.OptimizedRelationalIndex.class,
	 * subClassBitMask); // } else if (operatorClass //
	 * .equals(lupos.engine.operators.index.IndexCollection.class)) { // //
	 * IndexCollection // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.index.adaptedRDF3X.IndexCollection.class, //
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.index.adaptiveindexforjoins.IndexCollection.class,
	 * // subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.index.iceis2007.IndexCollection.class, //
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.index.relational.IndexCollection.class, //
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.index.vldb2008.IndexCollection.class, //
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.rdfs.index.IndexCollection.class, //
	 * subClassBitMask); } else if
	 * (operatorClass.equals(MultiInputOperator.class)) { // MultiInputOperator
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.join.Join.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.optional.Optional.class,
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators
	 * .multiinput.join.parallel.ParallelPreProcessJoin.class, //
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.Union.class, subClassBitMask); } else
	 * if (operatorClass.equals(Join.class)) { // Join
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.join.HashJoin.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.join.IndexJoin.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.join.MergeJoin.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.join.MergeJoinWithoutSorting.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput
	 * .join.MergeJoinWithoutSortingSeveralIterations.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.join.NAryMergeJoinWithoutSorting.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.join.NestedLoopJoin.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.join.parallel.ParallelJoin.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.join.parallel.ParallelListJoin.class,
	 * subClassBitMask); } // TODO: IndexJoin // TODO: MergeJoin // TODO:
	 * ParallelJoin // TODO: MergeParallelJoin // TODO: MergeParallelOptional //
	 * TODO: ParallelOptional else if (operatorClass.equals(Optional.class)) {
	 * // Optional setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.optional.DBBPTreeIndexOptional.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput
	 * .optional.DBMergeSortedBagOptional.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.optional.HashMapIndexOptional.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.optional.HashOptional.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.optional.HybridIndexOptional.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput
	 * .optional.MergeWithoutSortingOptional.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.optional.NaiveOptional.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.optional.TreeBagOptional.class,
	 * subClassBitMask); } else if (operatorClass.equals(Union.class)) { //
	 * Union setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.multiinput.MergeUnion.class, subClassBitMask); //
	 * } else if (operatorClass.equals(ResultCollector.class)) { // //
	 * ResultCollector // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.
	 * multiinput.join.parallel.MergeResultCollector.class, // subClassBitMask);
	 * } else if (operatorClass.equals(SingleInputOperator.class)) { //
	 * SingleInputOperator setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.AddBinding.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.AddBindingFromOtherVar.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.AddComputedBinding.class,
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.sparul.Clear.class, //
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.sort.Collector.class, //
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.Construct.class, // subClassBitMask);
	 * // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.sparul.CreateOrDrop.class, //
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.modifiers.distinct.Distinct.class,
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.EmptyEnv.class, // subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.sort.fastsort.FastSort.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.Filter.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.generate.Generate.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.generate.GenerateAddEnv.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.modifiers.Limit.class,
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.MakeBooleanResult.class, //
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.sparul.MultipleURIOperator.class, //
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.Projection.class, subClassBitMask); //
	 * setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.readtriplesdistinct
	 * .ReadTriplesDistinct.class, // subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.ReduceEnv.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.ReduceEnvFilterVars.class,
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.ReplaceLit.class, subClassBitMask);
	 * setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.ReplaceVar.class, subClassBitMask); //
	 * setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.modifiers.SortLimit.class, //
	 * subClassBitMask); // setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.SIPFilterOperator.class, //
	 * subClassBitMask); setBitsOfClassAndSubClasses(
	 * lupos.engine.operators.singleinput.sort.Sort.class, subClassBitMask); //
	 * setBitsOfClassAndSubClasses( //
	 * lupos.engine.operators.singleinput.SeveralSucceedingOperators.class, //
	 * subClassBitMask); } // TODO: add more subclasses }
	 */
	/*
	 * Return the assigned position of the given operator class inside the
	 * comparison bit vector. Bit vectors are used by Rule and RuleEngine to
	 * minimize the number of applications of logical optimization rules.
	 */
	/*
	 * private static int getComparisonVectorPos( final Class<? extends
	 * BasicOperator> className) { return classToPosMap.get(className); // //
	 * BasicOperator // if
	 * (className.equals(lupos.engine.operators.BasicOperator.class)) { //
	 * return 0; // } // // // Operator // else if
	 * (className.equals(lupos.engine.operators.Operator.class)) { // return 1;
	 * // } // // // ParallelOperand // else if (className //
	 * .equals(lupos.engine.operators.singleinput.parallel.ParallelOperand. //
	 * class)) { // return 2; // } // // // QueryResultInBlocks // else if
	 * (className
	 * //.equals(lupos.engine.operators.singleinput.parallel.QueryResultInBlocks
	 * // .class)) { // return 3; // } // // // SequentialResultCollector //
	 * else if // (className.equals(lupos.test.SequentialResultCollector.class))
	 * { // return 4; // } // // // BasicIndex: all index subclasses are
	 * classified as BasicIndex // else if (className //
	 * .equals(lupos.engine.operators.index.BasicIndex.class)) { // return 5; //
	 * } else if (className //
	 * .equals(lupos.engine.operators.index.adaptiveindexforjoins. //
	 * AdaptiveIndex.class)) { // return 5; // } else if (className //
	 * .equals(lupos.engine.operators.index.EmptyIndex.class)) { // return 5; //
	 * } else if (className //
	 * .equals(lupos.engine.operators.index.iceis2007.ICEIS2007Index.class)) //
	 * { // return 5; // } else if (className //
	 * .equals(lupos.engine.operators.index.adaptedRDF3X.RDF3XIndex.class)) // {
	 * // return 5; // } else if (className
	 * //.equals(lupos.engine.operators.index.relational.RelationalIndex.class)
	 * // ) { // return 5; // } else if (className //
	 * .equals(lupos.engine.operators.rdfs.index.ToStreamIndex.class)) { //
	 * return 5; // } else if (className //
	 * .equals(lupos.engine.operators.index.vldb2008.VLDB2008Index.class)) { //
	 * return 5; // } else if (className //
	 * .equals(lupos.engine.operators.index.relational. //
	 * OptimizedRelationalIndex.class)) { // return 5; // } // // //
	 * IndexCollection: all subclasses are classified as IndexCollection // else
	 * if (className //
	 * .equals(lupos.engine.operators.index.IndexCollection.class)) { // return
	 * 6; // } else if (className
	 * //.equals(lupos.engine.operators.index.adaptedRDF3X.IndexCollection.class
	 * // )) { // return 6; // } else if (className //
	 * .equals(lupos.engine.operators.index.adaptiveindexforjoins. //
	 * IndexCollection.class)) { // return 6; // } else if (className
	 * //.equals(lupos.engine.operators.index.iceis2007.IndexCollection.class))
	 * // { // return 6; // } else if (className
	 * //.equals(lupos.engine.operators.index.relational.IndexCollection.class)
	 * // ) { // return 6; // } else if (className //
	 * .equals(lupos.engine.operators.index.vldb2008.IndexCollection.class)) //
	 * { // return 6; // } else if (className //
	 * .equals(lupos.engine.operators.rdfs.index.IndexCollection.class)) { //
	 * return 6; // } // // // MultiInputOperator: // else if (className //
	 * .equals(lupos.engine.operators.multiinput.MultiInputOperator.class)) // {
	 * // return 7; // } // // // ParallelPreProcessJoin // else if (className
	 * // .equals(lupos.engine.operators.multiinput.join.parallel. //
	 * ParallelPreProcessJoin.class)) { // return 8; // } // // // Join: all
	 * subclasses are classified as Join // else if (className //
	 * .equals(lupos.engine.operators.multiinput.join.Join.class)) { // return
	 * 9; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.join.HashJoin.class)) { //
	 * return 9; // } else if (className
	 * //.equals(lupos.engine.operators.multiinput.join.MergeJoinWithoutSorting
	 * // .class)) { // return 9; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.join. //
	 * MergeJoinWithoutSortingSeveralIterations.class)) { // return 9; // } else
	 * if (className // .equals(lupos.engine.operators.multiinput.join. //
	 * NAryMergeJoinWithoutSorting.class)) { // return 9; // } else if
	 * (className //
	 * .equals(lupos.engine.operators.multiinput.join.NestedLoopJoin.class)) //
	 * { // return 9; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.join.parallel. //
	 * ParallelListJoin.class)) { // return 9; // } // // IndexJoin: all
	 * subclasses are classified as Join // else if (className //
	 * .equals(lupos.engine.operators.multiinput.join.IndexJoin.class)) { //
	 * return 9; // } else if (className
	 * //.equals(lupos.engine.operators.multiinput.join.DBBPTreeIndexJoin.class
	 * // )) { // return 9; // } else if (className
	 * //.equals(lupos.engine.operators.multiinput.join.HashMapIndexJoin.class)
	 * // ) { // return 9; // } else if (className
	 * //.equals(lupos.engine.operators.multiinput.join.HybridIndexJoin.class))
	 * // { // return 9; // } // // Mergejoin: all subclasses are classified as
	 * Join // else if (className //
	 * .equals(lupos.engine.operators.multiinput.join.MergeJoin.class)) { //
	 * return 9; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.join. //
	 * DBMergeSortedBagMergeJoin.class)) { // return 9; // } else if (className
	 * //.equals(lupos.engine.operators.multiinput.join.TreeBagMergeJoin.class)
	 * // ) { // return 9; // } // // ParallelJoin: all subclasses are
	 * classified as Join // else if (className //
	 * .equals(lupos.engine.operators.multiinput.join.parallel.ParallelJoin. //
	 * class)) { // return 9; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.join.parallel. //
	 * DBBPTreeIndexParallelJoin.class)) { // return 9; // } else if (className
	 * // .equals(lupos.engine.operators.multiinput.join.parallel. //
	 * HashMapIndexParallelJoin.class)) { // return 9; // } else if (className
	 * // .equals(lupos.engine.operators.multiinput.join.parallel. //
	 * HashParallelJoin.class)) { // return 9; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.join.parallel. //
	 * HybridIndexParallelJoin.class)) { // return 9; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.join.parallel. //
	 * NestedLoopParallelJoin.class)) { // return 9; // } // //
	 * MergeParallelJoin: all subclasses are classified as Join // else if
	 * (className // .equals(lupos.engine.operators.multiinput.join.parallel. //
	 * MergeParallelJoin.class)) { // return 9; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.join.parallel. //
	 * DBMergeSortedBagMergeParallelJoin.class)) { // return 9; // } else if
	 * (className // .equals(lupos.engine.operators.multiinput.join.parallel. //
	 * MergeParallelJoinWithoutSorting.class)) { // return 9; // } else if
	 * (className // .equals(lupos.engine.operators.multiinput.join.parallel. //
	 * TreeBagMergeParallelJoin.class)) { // return 9; // } // //
	 * MergeParallelOptional: all subclasses are classified as Optional // else
	 * if (className //
	 * .equals(lupos.engine.operators.multiinput.optional.parallel. //
	 * MergeParallelOptional.class)) { // return 10; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.optional.parallel. //
	 * DBMergeSortedBagParallelOptional.class)) { // return 10; // } else if
	 * (className //
	 * .equals(lupos.engine.operators.multiinput.optional.parallel. //
	 * MergeWithoutSortingParallelOptional.class)) { // return 10; // } else if
	 * (className //
	 * .equals(lupos.engine.operators.multiinput.optional.parallel. //
	 * TreeBagMergeParallelOptional.class)) { // return 10; // } // //
	 * ParallelOptional: all subclasses are classified as Optional // else if
	 * (className //
	 * .equals(lupos.engine.operators.multiinput.optional.parallel. //
	 * ParallelOptional.class)) { // return 10; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.optional.parallel. //
	 * DBBPTreeIndexParallelOptional.class)) { // return 10; // } else if
	 * (className //
	 * .equals(lupos.engine.operators.multiinput.optional.parallel. //
	 * HashMapIndexParallelOptional.class)) { // return 10; // } else if
	 * (className //
	 * .equals(lupos.engine.operators.multiinput.optional.parallel. //
	 * HashParallelOptional.class)) { // return 10; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.optional.parallel. //
	 * HybridIndexParallelOptional.class)) { // return 10; // } else if
	 * (className //
	 * .equals(lupos.engine.operators.multiinput.optional.parallel. //
	 * NaiveParallelOptional.class)) { // return 10; // } // // // Optional: all
	 * subclasses are classified as Optional // else if (className //
	 * .equals(lupos.engine.operators.multiinput.optional.Optional.class)) { //
	 * return 10; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.optional. //
	 * DBBPTreeIndexOptional.class)) { // return 10; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.optional. //
	 * DBMergeSortedBagOptional.class)) { // return 10; // } else if (className
	 * //.equals(lupos.engine.operators.multiinput.optional.HashMapIndexOptional
	 * // .class)) { // return 10; // } else if (className
	 * //.equals(lupos.engine.operators.multiinput.optional.HashOptional.class)
	 * // ) { // return 10; // } else if (className
	 * //.equals(lupos.engine.operators.multiinput.optional.HybridIndexOptional
	 * // .class)) { // return 10; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.optional. //
	 * MergeWithoutSortingOptional.class)) { // return 10; // } else if
	 * (className
	 * //.equals(lupos.engine.operators.multiinput.optional.NaiveOptional.class
	 * // )) { // return 10; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.optional.TreeBagOptional. //
	 * class)) { // return 10; // } // // // Union: all subclasses are
	 * classified as Union // else if (className //
	 * .equals(lupos.engine.operators.multiinput.Union.class)) { // return 11;
	 * // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.MergeUnion.class)) { // return
	 * 11; // } // // // ResultCollector: all subclasses are classified as
	 * ResultCollector // else if (className
	 * //.equals(lupos.engine.operators.multiinput.join.parallel.ResultCollector
	 * // .class)) { // return 12; // } else if (className //
	 * .equals(lupos.engine.operators.multiinput.join.parallel. //
	 * MergeResultCollector.class)) { // return 12; // } // // //
	 * SingleInputOperator // else if (className
	 * //.equals(lupos.engine.operators.singleinput.SingleInputOperator.class))
	 * // { // return 13; // } // // // AddBinding // else if (className //
	 * .equals(lupos.engine.operators.singleinput.AddBinding.class)) { // return
	 * 14; // } // // // AddcomoputedBinding -> AddBinding // else if (className
	 * // .equals(lupos.engine.operators.singleinput.AddComputedBinding.class))
	 * // { // return 14; // } // // // AddBindingFromOtherVar // else if
	 * (className
	 * //.equals(lupos.engine.operators.singleinput.AddBindingFromOtherVar.class
	 * // )) { // return 15; // } // // // Clear // else if (className //
	 * .equals(lupos.engine.operators.singleinput.sparul.Clear.class)) { //
	 * return 16; // } // // // Collector // else if (className //
	 * .equals(lupos.engine.operators.singleinput.sort.Collector.class)) { //
	 * return 17; // } // // // Construct // else if (className //
	 * .equals(lupos.engine.operators.singleinput.Construct.class)) { // return
	 * 18; // } // // // EmptyEnv // else if (className //
	 * .equals(lupos.engine.operators.singleinput.EmptyEnv.class)) { // return
	 * 19; // } // // // Filter // else if (className //
	 * .equals(lupos.engine.operators.singleinput.Filter.class)) { // return 20;
	 * // } // // // Generate // else if (className //
	 * .equals(lupos.engine.operators.singleinput.generate.Generate.class)) // {
	 * // return 21; // } // // // GenerateAddEnv // else if (className //
	 * .equals(lupos.engine.operators.singleinput.generate.GenerateAddEnv. //
	 * class)) { // return 22; // } // // // Limit // else if (className //
	 * .equals(lupos.engine.operators.singleinput.modifiers.Limit.class)) { //
	 * return 23; // } // // // MakeBooleanResult // else if (className //
	 * .equals(lupos.engine.operators.singleinput.MakeBooleanResult.class)) // {
	 * // return 24; // } // // // Offset // else if (className //
	 * .equals(lupos.engine.operators.singleinput.modifiers.Offset.class)) { //
	 * return 25; // } // // // Projection // else if (className //
	 * .equals(lupos.engine.operators.singleinput.Projection.class)) { // return
	 * 26; // } // // // ReduceEnv // else if (className //
	 * .equals(lupos.engine.operators.singleinput.ReduceEnv.class)) { // return
	 * 27; // } // // // ReduceEnvFilterVars // else if (className
	 * //.equals(lupos.engine.operators.singleinput.ReduceEnvFilterVars.class))
	 * // { // return 28; // } // // // ReplaceLit // else if (className //
	 * .equals(lupos.engine.operators.singleinput.ReplaceLit.class)) { // return
	 * 29; // } // // // ReplaceVar // else if (className //
	 * .equals(lupos.engine.operators.singleinput.ReplaceVar.class)) { // return
	 * 30; // } // // // SelectAnd_equals // else if (className //
	 * .equals(lupos.engine.operators.singleinput.filter.SelectAnd_equals. //
	 * class)) { // return 31; // } // // // SortLimit // else if (className
	 * //.equals(lupos.engine.operators.singleinput.modifiers.SortLimit.class))
	 * // { // return 32; // } // // // CreateOrDrop: all subclasses are
	 * classified as CreateOrDrop // else if (className
	 * //.equals(lupos.engine.operators.singleinput.sparul.CreateOrDrop.class))
	 * // { // return 33; // } else if (className //
	 * .equals(lupos.engine.operators.singleinput.sparul.Create.class)) { //
	 * return 33; // } else if (className //
	 * .equals(lupos.engine.operators.singleinput.sparul.Drop.class)) { //
	 * return 33; // } // // // Distinct: all subclasses are classified as
	 * Distinct // else if (className
	 * //.equals(lupos.engine.operators.singleinput.modifiers.distinct.Distinct
	 * // .class)) { // return 34; // }// to be added... // // // FastSort: all
	 * subclasses are classified as FastSort // else if (className
	 * //.equals(lupos.engine.operators.singleinput.sort.fastsort.FastSort.class
	 * // )) { // return 35; // }// to be added... // // // MultipleURIOperator:
	 * all subclasses are classified as // // MultipleURIOperator // else if
	 * (className
	 * //.equals(lupos.engine.operators.singleinput.sparul.MultipleURIOperator.
	 * // class)) { // return 36; // } // to be added... // // //
	 * ReadTriplesDistinct: all subclasses are classified as // //
	 * ReadTriplesDistinct // else if (className //
	 * .equals(lupos.engine.operators.singleinput.readtriplesdistinct. //
	 * ReadTriplesDistinct.class)) { // return 37; // } // to be added... // //
	 * // SIPFilterOperator: all subclasses are classified as //
	 * SIPFilterOperator // else if (className //
	 * .equals(lupos.engine.operators.singleinput.SIPFilterOperator.class)) // {
	 * // return 38; // } // to be added... // // // Sort: all subclasses are
	 * classified as Sort // else if (className //
	 * .equals(lupos.engine.operators.singleinput.sort.Sort.class)) { // return
	 * 39; // } // to be added... // // // TripleOperator // else if (className
	 * // .equals(lupos.engine.operators.tripleoperator.TripleOperator.class))
	 * // { // return 40; // } // // // TriggerOneTime // else if (className //
	 * .equals(lupos.engine.operators.tripleoperator.TriggerOneTime.class)) // {
	 * // return 41; // } // // // TriplePattern // else if (className //
	 * .equals(lupos.engine.operators.tripleoperator.TriplePattern.class)) { //
	 * return 42; // } // // // Indices: all subclasses are classified as
	 * Indices // else if //
	 * (className.equals(lupos.engine.operators.index.Indices.class)) { //
	 * return 43; // } // to be added... // // // SeveralSuccedingOperators //
	 * else if (className
	 * //.equals(lupos.engine.operators.singleinput.SeveralSucceedingOperators.
	 * // class)) { // return 44; // } // // else { // throw new
	 * InvalidParameterException("Missing constant for class " // + className);
	 * // } }
	 */
	protected static HashMap<Class<? extends BasicOperator>, Integer> putToClassToPosMap(
			HashMap<Class<? extends BasicOperator>, Integer> classToPosMap,
			final Integer pos, final Class<? extends BasicOperator>... classes) {
		if (classToPosMap == null)
			classToPosMap = new HashMap<Class<? extends BasicOperator>, Integer>();
		for (final Class<? extends BasicOperator> classToPut : classes) {
			classToPosMap.put(classToPut, pos);
		}
		return classToPosMap;
	}
}