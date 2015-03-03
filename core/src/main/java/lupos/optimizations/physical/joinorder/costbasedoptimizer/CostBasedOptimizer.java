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
package lupos.optimizations.physical.joinorder.costbasedoptimizer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Triple;
import lupos.misc.Tuple;
import lupos.optimizations.logical.statistics.VarBucket;
import lupos.optimizations.physical.joinorder.RearrangeJoinOrder;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.operatorgraphgenerator.OperatorGraphGenerator;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.plan.InnerNodePlan;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.plan.LeafNodePlan;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.plan.Plan;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.splitheuristic.SplitCartesianProduct;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.splitheuristic.SplitGraphWithMaxNumberOfMergeJoins;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.splitheuristic.SplitHeuristic;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.splitheuristic.SplitStarShapedJoinOrPath;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.splitheuristic.SplitTwoSubgraphs;

/**
 * This class contains the cost based optimizer.
 * The cost based optimizer first applies heuristics to split a set of triple patterns into disjunctive sets.
 * Hereby, the cost-based optimizer applies some heuristics always (e.g., splitting at cartesian products) and some only if there are still a large number of triple patterns to join.
 * Afterwards, the cost based optimizer tries out every possible join order of the triple patterns in the smaller sets of triple patterns and between them.
 */
public class CostBasedOptimizer implements RearrangeJoinOrder {

	/**
	 * the operator graph generator to be used to generate the operator graph from the plan
	 */
	private final OperatorGraphGenerator operatorGraphGenerator;

	/**
	 * Constructor to set all variable parameters
	 * @param operatorGraphGenerator the operator graph generator to be used to generate the operator graph from the plan
	 * @param applyAlwaysHeuristics the heuristics to be always applied on a set of triple patterns
	 * @param applyForManyTriplePatternsHeuristics the heuristics to be applied on a set of triple patterns, when the number of triple patterns is more than LIMIT_TRIPLEPATTERNS
	 */
	public CostBasedOptimizer(final OperatorGraphGenerator operatorGraphGenerator, final List<SplitHeuristic> applyAlwaysHeuristics, final List<SplitHeuristic> applyForManyTriplePatternsHeuristics){
		this.operatorGraphGenerator = operatorGraphGenerator;
		this.applyAlwaysHeuristics = applyAlwaysHeuristics;
		this.applyForManyTriplePatternsHeuristics = applyForManyTriplePatternsHeuristics;
	}

	/**
	 * This constructor initializes the cost based optimizer with the default heuristics to be applied on triple patterns
	 * @param operatorGraphGenerator the operator graph generator to be used to generate the operator graph from the plan
	 */
	public CostBasedOptimizer(final OperatorGraphGenerator operatorGraphGenerator){
		this(operatorGraphGenerator, CostBasedOptimizer.getApplyAlwaysHeuristics(), CostBasedOptimizer.getApplyForManyTriplePatternsHeuristics());
	}

	/**
	 * @return the default heuristics to be applied always on a set of triple patterns to be joined
	 */
	protected static List<SplitHeuristic> getApplyAlwaysHeuristics(){
		final List<SplitHeuristic> applyAlwaysHeuristics = new LinkedList<SplitHeuristic>();
		applyAlwaysHeuristics.add(new SplitCartesianProduct());
		return applyAlwaysHeuristics;
	}

	/**
	 * @return the default heuristics to be applied on a set of triple patterns to be joined, if the number of triple patterns is more than LIMIT_TRIPLEPATTERNS
	 */
	protected static List<SplitHeuristic> getApplyForManyTriplePatternsHeuristics(){
		final List<SplitHeuristic> applyForManyTriplePatternsHeuristics = new LinkedList<SplitHeuristic>();
		applyForManyTriplePatternsHeuristics.add(new SplitTwoSubgraphs());
		applyForManyTriplePatternsHeuristics.add(new SplitGraphWithMaxNumberOfMergeJoins());
		applyForManyTriplePatternsHeuristics.add(new SplitStarShapedJoinOrPath());
		return applyForManyTriplePatternsHeuristics;
	}


	/**
	 * This list contains those heuristics to split the set of triple patterns, which are always applied
	 */
	protected final List<SplitHeuristic> applyAlwaysHeuristics;
	/**
	 * This list contains those heuristics to split the set of triple patterns, which are only applied, if the number of triple patterns to join is above a certain limit (LIMIT_TRIPLEPATTERNS),
	 * otherwise all combinations of joins is tried out.
	 */
	protected final List<SplitHeuristic> applyForManyTriplePatternsHeuristics;

	/**
	 * the maximum number of triple patterns for which every join order is tried out without using any heuristics to split the set of triple patterns to join...
	 */
	protected final static int LIMIT_TRIPLEPATTERNS = 7;

	/**
	 * This constant specifies the number of threads, which are used to initialize the leaf node plans and also to try out all combinations
	 */
	private final static int MAXNUMBERTHREADS = 0;

	/**
	 * used to lock the access to the best plan (otherwise problems like lost updates could occur when using several threads for trying out all combinations of the join order)
	 */
	private final ReentrantLock lockBestPlan = new ReentrantLock();

	/**
	 * use only MAXNUMBERTHREADS threads and lock therefore the access to numberThreads with this lock!
	 */
	private final ReentrantLock lockNumberOfThreads = new ReentrantLock();

	/**
	 * the currently used number of threads during trying out all combinations of the join order...
	 */
	private int numberThreads = 0;


	@Override
	public void rearrangeJoinOrder(final Root newRoot, final BasicIndexScan indexScan) {
		final Triple<List<LeafNodePlan>, HashMap<Variable, Literal>, HashMap<Variable, Literal>> initialInfo = this.getInitialPlansAndMinimaAndMaxima(indexScan.getTriplePattern(), indexScan);
		final Plan plan = this.getPlan(initialInfo.getFirst());
		final BasicOperator op = this.operatorGraphGenerator.generateOperatorGraph(plan, newRoot, indexScan, new LinkedList<Variable>(), initialInfo.getSecond(), initialInfo.getThird(), new HashMap<TriplePattern, Map<Variable, VarBucket>>());
		op.setSucceedingOperators(indexScan.getSucceedingOperators());
	}

	/**
	 * Determines the best estimated plan for joining a set of triple patterns
	 * @param initialPlans the plans for the leaf nodes (each for a single triple pattern)
	 * @return the best estimated plan
	 */
	public Plan getPlan(final List<LeafNodePlan> initialPlans){
		// apply the heuristics which are always applied (e.g. to split at a cartesian product)
		List<List<LeafNodePlan>> splittedPlans = new LinkedList<List<LeafNodePlan>>();
		splittedPlans.add(initialPlans);
		for(final SplitHeuristic applyAlways: this.applyAlwaysHeuristics){
			final List<List<LeafNodePlan>> newSplittedPlans = new LinkedList<List<LeafNodePlan>>();
			for(final List<LeafNodePlan> checkedToBeSplitted: splittedPlans){
				newSplittedPlans.addAll(applyAlways.split(checkedToBeSplitted));
			}
			splittedPlans = newSplittedPlans;
		}
		// split further if there are many triple patterns to join and try out join orders of the splitted parts
		return this.getPlanBySplittingSplittedPartsForManyTriplePatterns(splittedPlans);
	}

	/**
	 * Check already splitted plans to be further splitted and try out join orders (has effects if we have more than two splitted plans)
	 * @param splittedPlans
	 * @return
	 */
	public Plan getPlanBySplittingSplittedPartsForManyTriplePatterns(final List<List<LeafNodePlan>> splittedPlans){
		final List<Plan> resultingPlans = new LinkedList<Plan>();
		for(final List<LeafNodePlan> splittedPart: splittedPlans){
			resultingPlans.add(this.getPlanBySplittingForManyTriplePatterns(splittedPart));
		}
		return this.tryOutJoinOrders(resultingPlans);
	}

	/**
	 * Determines further splitted plans if the number of triple patterns is large (&gt; LIMIT_TRIPLEPATTERNS) and tries out each join order
	 * @param initialPlans the plans to be splitted further
	 * @return a best estimated plan
	 */
	public Plan getPlanBySplittingForManyTriplePatterns(final List<LeafNodePlan> initialPlans){
		if(initialPlans.size() > LIMIT_TRIPLEPATTERNS){
			for(final SplitHeuristic heuristic: this.applyForManyTriplePatternsHeuristics){
				final List<List<LeafNodePlan>> splittedPlans = heuristic.split(initialPlans);
				if(splittedPlans.size()>1){
					return this.getPlanBySplittingSplittedPartsForManyTriplePatterns(splittedPlans);
				} // otherwise try next split heuristic...
			}
		}
		// try out each join order
		return this.tryOutJoinOrders(initialPlans);
	}

	/**
	 * try out the join order between a list of plans
	 * @param initialPlans the plans to join
	 * @return the best estimated join order
	 */
	public Plan tryOutJoinOrders(final List<? extends Plan> initialPlans){
		// for one plan just return the plan
		if(initialPlans.size()==1){
			return initialPlans.get(0);
		}
		// for two plans just return a join of both
		if(initialPlans.size()==2){
			return new InnerNodePlan(initialPlans.get(0), initialPlans.get(1));
		}

		// initialize table for dynamic programming
		@SuppressWarnings("unchecked")
		final HashMap<Long, Plan>[] bestPlans = new HashMap[initialPlans.size()];
		bestPlans[0] = new HashMap<Long, Plan>();
		// Which initial plans are already joined, can be seen by the used key:
		// If the i-th bit is set, then the i-th initial plan has already been joined.
		long key = 1;
		// The initial plans themselves are put into first row of the table
		for (final Plan plan : initialPlans) {
			bestPlans[0].put(key, plan);
			key *= 2; // the next bit is set in key (the other are cleared)
		}
		for (int i = 1; i < initialPlans.size(); i++) {
			// compute the next row in the table...
			// i + 1 contains the number of initial plans which are considered to be joined...
			bestPlans[i] = new HashMap<Long, Plan>();
			// start with an initial key factor of 1
			// no plans are joined so far
			// maximum number of initial plans to join is i + 1 for this row
			this.allCombinations(1, 0, 0, 0, 0, i + 1, initialPlans, bestPlans);
		}
		final Plan result = bestPlans[initialPlans.size() - 1].get(bestPlans[initialPlans.size() - 1].keySet().iterator().next());
		result.findMaxMergeJoins();
		return result;
	}

	/**
	 * Recursive method to try out all combinations of join orderings between the different given plans.
	 * Dynamic Programming is used.
	 * @param keyFactor the factor 2^i, where i is the current bit
	 * @param keyLeft the plans joined for the left operand
	 * @param keyRight the plans joined for the right operand
	 * @param currentLeft the number of plans considered in the left operand
	 * @param currentRight the number of plans considered in the right operand
	 * @param max the maximum number of plans to be joined for this row of the table for dynamic programming
	 * @param initialPlans the remaining plans to be considered for joining
	 * @param bestPlans the table of best plans for dynamic programming
	 */
	private void allCombinations(final long keyFactor, final long keyLeft,
			final long keyRight, final int currentLeft, final int currentRight,
			final int max, final List<? extends Plan> initialPlans,
			final HashMap<Long, Plan>[] bestPlans) {
		if (initialPlans.size() == 0 || currentLeft + currentRight >= max) {
			// Recursion end reached!
			// Correct number of already joined initial plans?
			// Does the left and right operand have any initial plans?
			if (currentLeft + currentRight != max || currentLeft == 0 || currentRight == 0){
				return;
			}
			// find the best plans for the left and right operands of the currently considered join by looking into the table for dynamic programming
			final Plan left = bestPlans[currentLeft - 1].get(keyLeft);
			final Plan right = bestPlans[currentRight - 1].get(keyRight);
			final Plan combined = new InnerNodePlan(left.clone(), right.clone());
			this.lockBestPlan.lock();
			try {
				// do we have new best plan for joining the initial plans of the left and right operand?
				final Plan currentBest = bestPlans[max - 1].get(keyLeft + keyRight);
				if (currentBest == null || currentBest.compareTo(combined) > 0){
					bestPlans[max - 1].put(keyLeft + keyRight, combined);
				}
				return;
			} finally {
				this.lockBestPlan.unlock();
			}
		}
		final LinkedList<Plan> temp = new LinkedList<Plan>();
		temp.addAll(initialPlans);
		temp.remove(0);
		final long nextKeyFactor = keyFactor * 2;

		final LinkedList<Thread> listOfThreads = new LinkedList<Thread>();

		// try out: next triple pattern should remain unjoined
		final Thread thread0 = new Thread() {
			@Override
			public void run() {
				CostBasedOptimizer.this.allCombinations(nextKeyFactor, keyLeft, keyRight, currentLeft, currentRight, max, temp, bestPlans);
			}
		};
		this.startThread(thread0, listOfThreads);

		// try out: next triple pattern should be already joined in the left operand
		final Thread thread1 = new Thread() {
			@Override
			public void run() {
				CostBasedOptimizer.this.allCombinations(nextKeyFactor, keyLeft + keyFactor, keyRight, currentLeft + 1, currentRight, max, temp, bestPlans);
			}
		};
		this.startThread(thread1, listOfThreads);

		// try out: next triple pattern should be already joined in the right operand
		final Thread thread2 = new Thread() {
			@Override
			public void run() {
				CostBasedOptimizer.this.allCombinations(nextKeyFactor, keyLeft, keyRight + keyFactor, currentLeft, currentRight + 1, max, temp, bestPlans);
			}
		};
		this.startThread(thread2, listOfThreads);

		// wait for thread0, thread1 and thread2 to finish (if they have been started as thread they are contained in listOfThreads)
		for (final Thread thread : listOfThreads) {
			try {
				thread.join();
				this.lockNumberOfThreads.lock();
				try {
					this.numberThreads--;
				} finally {
					this.lockNumberOfThreads.unlock();
				}
			} catch (final InterruptedException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}


	/**
	 * This method determines the initial plans (consisting of leaf nodes) as well as the minimum and maximum values for the variables to be joined
	 * @param triplePatterns the triple patterns to optimize
	 * @param indexScan the index scan operator, which is utilized to determine the histogram and the minimum and maximum values for the join variables
	 * @return the initial plans, the minimum and maximum values of the variables to be joined
	 */
	public Triple<List<LeafNodePlan>, HashMap<Variable, Literal>, HashMap<Variable, Literal>> getInitialPlansAndMinimaAndMaxima(final Collection<TriplePattern> triplePatterns, final BasicIndexScan indexScan){
		// for the result...
		final List<LeafNodePlan> initialPlans = Collections.synchronizedList(new LinkedList<LeafNodePlan>());
		// used threads to set up the initial plans
		final LinkedList<Thread> intialPlansThreads = new LinkedList<Thread>();
		// TODO check if we still need to use BindingsArrayReadTriples!
		final Class<? extends Bindings> classBindings = Bindings.instanceClass;
		Bindings.instanceClass = BindingsArrayReadTriples.class;
		final BindingsFactory bindingsFactoryOld = indexScan.getBindingsFactory();
		// determine all join partners of the triple partners
		// afterwards only generate histograms for the join partners
		// For this purpose, first count the occurrences of the variables in the triple patterns
		final HashMap<Variable, Integer> countVarOccurence = new HashMap<Variable, Integer>();
		for (final TriplePattern tp : triplePatterns) {
			for (final Variable v : tp.getVariables()) {
				final Integer count = countVarOccurence.get(v);
				if(count==null){ // first time the variable appears...
					countVarOccurence.put(v, 1);
				} else {
					countVarOccurence.put(v, count + 1);
				}
			}
		}

		indexScan.setBindingsFactory(BindingsFactory.createBindingsFactory(countVarOccurence.keySet()));

		// now add those variables, which appear more than one time to the list of join partners!
		final List<Variable> joinPartners = new LinkedList<Variable>();
		for (final Entry<Variable, Integer> entry: countVarOccurence.entrySet()) {
			if(entry.getValue()>1){
				joinPartners.add(entry.getKey());
			}
		}

		// determine minimum and maximum of the join partners!
		final HashMap<Variable, Literal> minima = new HashMap<Variable, Literal>();
		final HashMap<Variable, Literal> maxima = new HashMap<Variable, Literal>();
		for (final TriplePattern tp : triplePatterns) { // compare the minimum and maximum values of each triple patterns!
			final HashSet<Variable> vars = tp.getVariables();
			vars.retainAll(joinPartners);
			if(vars.size()>0) {
				final Map<Variable, Tuple<Literal, Literal>> localExtrema = indexScan.getMinMax(tp, vars);
				if(localExtrema!=null){
					for(final Entry<Variable, Tuple<Literal, Literal>> entry: localExtrema.entrySet()){
						final Variable var = entry.getKey();
						final Literal min = minima.get(var);
						final Literal otherMin = entry.getValue().getFirst();
						if(min==null || min.compareToNotNecessarilySPARQLSpecificationConform(otherMin)>0) {
							minima.put(var, otherMin);
						}

						final Literal max = maxima.get(var);
						final Literal otherMax = entry.getValue().getSecond();
						if(max==null || max.compareToNotNecessarilySPARQLSpecificationConform(otherMax)<0) {
							maxima.put(var, otherMax);
						}
					}
				}
			}
		}

		int numberThreadsLocal = 0;

		// now generate initial plans of the leaf nodes!
		for (final TriplePattern tp : triplePatterns) {
			final Thread thread = new Thread() {
				final TriplePattern tp2 = tp;
				final BasicIndexScan index2 = indexScan.clone();

				@Override
				public void run() {
					this.index2.setTriplePatterns(new LinkedList<TriplePattern>());
					// determine e.g. histograms of the leaf nodes in parallel
					final LeafNodePlan leafNodePlan = new LeafNodePlan(this.tp2, this.index2, classBindings, joinPartners, minima, maxima);
					initialPlans.add(leafNodePlan);
				}
			};
			final boolean newThreadStarted = startThread(thread, intialPlansThreads, numberThreadsLocal);
			if(newThreadStarted){
				numberThreadsLocal++;
			} else {
				// check if another thread is already finished and reduce the number of threads... => this is necessary to start next time a thread again...
				final Iterator<Thread> iteratorOfThreads = intialPlansThreads.iterator();
				while(iteratorOfThreads.hasNext()) {
					final Thread threadToCheck = iteratorOfThreads.next();
					if(!threadToCheck.isAlive()){
						numberThreadsLocal--;
						iteratorOfThreads.remove();
					}
				}
			}
		}
		// wait for each thread to finish
		for (final Thread thread : intialPlansThreads) {
			try {
				thread.join();
			} catch (final InterruptedException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
		Bindings.instanceClass = classBindings;
		indexScan.setBindingsFactory(bindingsFactoryOld);
		return new Triple<List<LeafNodePlan>, HashMap<Variable, Literal>, HashMap<Variable, Literal>>(initialPlans, minima, maxima);
	}

	/**
	 * this method is used to start a new thread if less than CostBasedOptimizer.MAXNUMBERTHREADS threads are already started
	 * @param thread the thread to start
	 * @param listOfThreads the list of threads!
	 * @param numberThreads the number of threads already started!
	 * @return true if a new thread is started, otherwise false
	 */
	private static boolean startThread(final Thread thread, final LinkedList<Thread> listOfThreads, final int numberThreads) {
		if (numberThreads < CostBasedOptimizer.MAXNUMBERTHREADS) {
			thread.start();
			listOfThreads.add(thread);
			return true; // new thread is started!
		} else {
			thread.run();
			return false; // no new thread is started! => sequential processing
		}
	}

	/**
	 * this method is used to start a new thread if less than CostBasedOptimizer.MAXNUMBERTHREADS threads are already started
	 * @param thread the thread to start
	 * @param listOfThreads the list of threads!
	 */
	private void startThread(final Thread thread, final LinkedList<Thread> listOfThreads) {
		this.lockNumberOfThreads.lock();
		try {
			if (this.numberThreads < CostBasedOptimizer.MAXNUMBERTHREADS) {
				thread.start();
				listOfThreads.add(thread);
				this.numberThreads++;
			} else {
				thread.run();
			}
		} finally {
			this.lockNumberOfThreads.unlock();
		}
	}
}
