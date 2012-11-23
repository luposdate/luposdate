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
package lupos.optimizations.physical.joinorder.jointree.splitheuristic;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.statistics.Statistics;
import lupos.optimizations.logical.statistics.VarBucket;
import lupos.optimizations.physical.joinorder.jointree.plan.LeafNodePlan;

/**
 * This class splits a huge star-shaped join or a path 
 */
public class SplitStarShapedJoinOrPath implements SplitHeuristic {

	@Override
	public List<List<LeafNodePlan>> split(List<LeafNodePlan> initialPlans) {
		// We split the plan at star-shaped joins!
		final HashMap<Item, LinkedList<LeafNodePlan>> starJoins = new HashMap<Item, LinkedList<LeafNodePlan>>();
		final HashMap<Item, LinkedList<LeafNodePlan>> helperStarJoins = new HashMap<Item, LinkedList<LeafNodePlan>>();
		for (final LeafNodePlan lnp : initialPlans) {
			TriplePattern tp = lnp.getTriplePatterns().iterator().next();
			final Item subject = tp.getPos(0);
			LinkedList<LeafNodePlan> list = starJoins.get(subject);
			if (list == null){
				list = new LinkedList<LeafNodePlan>();
			}	
			list.add(lnp);
			starJoins.put(subject, list);
			final Item predicate = tp.getPos(1);
			if (predicate instanceof Variable) {
				list = helperStarJoins.get(predicate);
				if (list == null)
					list = new LinkedList<LeafNodePlan>();
				list.add(lnp);
				helperStarJoins.put(predicate, list);
			}
			final Item object = tp.getPos(2);
			if (object instanceof Variable) {
				list = helperStarJoins.get(object);
				if (list == null)
					list = new LinkedList<LeafNodePlan>();
				list.add(lnp);
				helperStarJoins.put(object, list);
			}
		}
		final Collection<LinkedList<LeafNodePlan>> lltp = new LinkedList<LinkedList<LeafNodePlan>>();
		lltp.addAll(starJoins.values());
		for (final LinkedList<LeafNodePlan> ltp : lltp) {
			if (ltp.size() == 1) {
				// look if the object or predicate can be joined with other
				// triple patterns!
				final LeafNodePlan lnp = ltp.get(0);
				final TriplePattern tp = lnp.getTriplePatterns().iterator().next();
				if (tp.getPos(2) instanceof Variable) {
					if (starJoins.get(tp.getPos(2)) != null) {
						if (!tp.getPos(2).equals(tp.getPos(0))) {
							starJoins.get(tp.getPos(2)).add(lnp);
							starJoins.remove(tp.getPos(0));
							continue;
						}
					}
					if (helperStarJoins.get(tp.getPos(0)) != null) {
						boolean flagCont = false;
						for (final LeafNodePlan lnp2 : helperStarJoins.get(tp.getPos(0))) {
							final TriplePattern tp2 = lnp2.getTriplePatterns().iterator().next();
							if (!tp2.equals(tp)) {
								starJoins.get(tp2.getPos(0)).add(lnp);
								starJoins.remove(tp.getPos(0));
								flagCont = true;
								break;
							}
						}
						if (flagCont)
							continue;
					}
					if (helperStarJoins.get(tp.getPos(2)) != null) {
						boolean flagCont = false;
						for (final LeafNodePlan lnp2 : helperStarJoins.get(tp.getPos(2))) {
							final TriplePattern tp2 = lnp2.getTriplePatterns().iterator().next();
							if (!tp2.equals(tp)) {
								starJoins.get(tp2.getPos(0)).add(lnp);
								starJoins.remove(tp.getPos(0));
								flagCont = true;
								break;
							}
						}
						if (flagCont)
							continue;
					}
				}
				if (tp.getPos(1) instanceof Variable) {
					if (starJoins.get(tp.getPos(1)) != null) {
						if (!tp.getPos(1).equals(tp.getPos(0))) {
							starJoins.get(tp.getPos(1)).add(lnp);
							starJoins.remove(tp.getPos(0));
							continue;
						}
					}
					if (helperStarJoins.get(tp.getPos(0)) != null) {
						boolean flagCont = false;
						for (final LeafNodePlan lnp2 : helperStarJoins.get(tp.getPos(0))) {
							final TriplePattern tp2 = lnp2.getTriplePatterns().iterator().next();
							if (!tp2.equals(tp)) {
								starJoins.get(tp2.getPos(0)).add(lnp);
								starJoins.remove(tp.getPos(0));
								flagCont = true;
								break;
							}
						}
						if (flagCont)
							continue;
					}
					if (helperStarJoins.get(tp.getPos(1)) != null) {
						boolean flagCont = false;
						for (final LeafNodePlan lnp2 : helperStarJoins.get(tp.getPos(1))) {
							final TriplePattern tp2 = lnp2.getTriplePatterns().iterator().next();
							if (!tp2.equals(tp)) {
								starJoins.get(tp2.getPos(0)).add(lnp);
								starJoins.remove(tp.getPos(0));
								flagCont = true;
								break;
							}
						}
						if (flagCont)
							continue;
					}
				}
			}
		}
		@SuppressWarnings("unchecked")
		final LinkedList<LeafNodePlan>[] solution = new LinkedList[starJoins.size()];
		final Iterator<LinkedList<LeafNodePlan>> starJoinsIterator = starJoins
				.values().iterator();
		for (int i = 0; i < starJoins.size(); i++){
			solution[i] = starJoinsIterator.next();
		}

		if (solution.length == 1) {
			// We have to split one huge star-shaped join!

			// find out the two triple patterns with the smallest
			// cardinalities to split the join!
			final LeafNodePlan[] initialPlansArray = initialPlans.toArray(new LeafNodePlan[0]);
			int max0;
			int max1;
			if (initialPlansArray[0].getCardinality() <= initialPlansArray[1].getCardinality()) {
				max0 = 0;
				max1 = 1;
			} else {
				max0 = 1;
				max1 = 0;
			}
			for (int i = 2; i < initialPlansArray.length; i++) {
				if (initialPlansArray[i].getCardinality() < initialPlansArray[max1].getCardinality()) {
					if (initialPlansArray[i].getCardinality() < initialPlansArray[max0].getCardinality()) {
						max1 = max0;
						max0 = i;
					} else
						max1 = i;
				}
			}
			final LinkedList<LeafNodePlan> left = new LinkedList<LeafNodePlan>();
			left.add(initialPlansArray[max0]);
			final LinkedList<LeafNodePlan> right = new LinkedList<LeafNodePlan>();
			right.add(initialPlansArray[max1]);
			// now split the join by putting the current triple pattern to
			// the one, which will have the smallest
			// cardinality when joined with max0 or with max1!
			for (int i = 0; i < initialPlansArray.length; i++) {
				if (i == max0 || i == max1){
					continue;
				}
				
				Map<Variable, VarBucket> selectivityMax0 = Statistics.estimateJoinSelectivity(initialPlansArray[max0].getSelectivity(), initialPlansArray[i].getSelectivity());
				// compute the cardinality of the join result as well as its cost 
				double cardMax0 = 0.0;
				if (selectivityMax0 != null) {
					if (selectivityMax0.size() > 0) {
						cardMax0 = selectivityMax0.values().iterator().next().getSum();
					}
				}				
				
				Map<Variable, VarBucket> selectivityMax1 = Statistics.estimateJoinSelectivity(initialPlansArray[max1].getSelectivity(), initialPlansArray[i].getSelectivity());
				// compute the cardinality of the join result as well as its cost 
				double cardMax1 = 0.0;
				if (selectivityMax1 != null) {
					if (selectivityMax1.size() > 0) {
						cardMax1 = selectivityMax1.values().iterator().next().getSum();
					}
				}				
				
				if(cardMax0 < cardMax1){
					left.add(initialPlansArray[i]);
				} else {
					right.add(initialPlansArray[i]);
				}				
			}
			if (left.size() == 1 || right.size() == 1) {
				// greedy: just have two similar big subgraphs! Just distribute the leaf nodes with small sizes in the two subgraphs!
				Collections.sort(initialPlans,
						new Comparator<LeafNodePlan>() {
							@Override
							public int compare(final LeafNodePlan o1, final LeafNodePlan o2) {
								if (o1.getCardinality() == o2.getCardinality()){
									return 0;
								} else if (o1.getCardinality() < o2.getCardinality()){
									return -1;
								} else {
									return 1;
								}
							}
						});
				left.clear();
				right.clear();
				boolean leftIsNext = true;
				for(LeafNodePlan lnp: initialPlans){
					if(leftIsNext){
						left.add(lnp);
					} else {
						right.add(lnp);
					}
					leftIsNext = !leftIsNext;
				}
				return this.generateResult(left, right);				
			} else {
				return this.generateResult(left, right);
			}
		} else {

			int max = 0;

			for (int i = 1; i < solution.length; i++) {
				if (solution[i].size() > solution[max].size())
					max = i;
			}
			if (solution[max].size() == 1) {
				// join is a path join

				// generate plans for paths by dividing the path in the middle:
				HashMap<Item, LeafNodePlan> subjects = new HashMap<Item, LeafNodePlan>(); 
				for(final LeafNodePlan lnp: initialPlans){
					TriplePattern tp=lnp.getTriplePatterns().iterator().next();
					subjects.put(tp.getPos(0), lnp);
				}
				LeafNodePlan startingLeafNode = null;
				// find the starting triple pattern of the path:
				for(final LeafNodePlan lnp: initialPlans){
					TriplePattern tp=lnp.getTriplePatterns().iterator().next();
					if(!subjects.containsKey(tp.getPos(2))){
						// found starting triple pattern of the path!
						startingLeafNode = lnp;
						break;
					}
				}
				// join path is a ring?! => just take one of the leaf nodes as starting point!
				if(startingLeafNode==null){
					startingLeafNode = initialPlans.get(0);
				}
				LinkedList<LeafNodePlan> splittedPart = new LinkedList<LeafNodePlan>();
				splittedPart.add(startingLeafNode);
				LeafNodePlan current = startingLeafNode;
				// now follow the path
				for(int i=1; i<initialPlans.size()/2; i++){
					TriplePattern tp = current.getTriplePatterns().iterator().next();
					LeafNodePlan lnp = subjects.get(tp.getPos(2));
					if(lnp!=null){
						splittedPart.add(lnp);
						current = lnp;
					} else {
						throw new RuntimeException("Expected that the join is a path join, but obviously it is not!");
					}
				}
				
				return generateResultByDividing(initialPlans, splittedPart);

			}

			// join has at least one star-shaped join:
			// split the huge join by separating a huge
			// star-shaped join from the rest!
			return generateResultByDividing(initialPlans, solution[max]);			
		}
	}

	/**
	 * This method generates the result by adding the parameters left and right to a list
	 * @param left one part of the leaf node plans to join
	 * @param right the other part of the leaf node plans to join
	 * @return a list containing left and right
	 */
	private LinkedList<List<LeafNodePlan>> generateResult(List<LeafNodePlan> left, List<LeafNodePlan> right){
		LinkedList<List<LeafNodePlan>> result = new LinkedList<List<LeafNodePlan>>();
		result.add(left);
		result.add(right);
		return result;
	}
	
	/**
	 * This method generates the result by adding the parameter splittedPart and other leaf node plans from initialPlans to a list
	 * @param initialPlans all leaf node plans
	 * @param splittedPart the part of initialPlans to split
	 * @return a list containing splittedPart  and (initialPlans - splittedPart) as elements 
	 */
	private LinkedList<List<LeafNodePlan>> generateResultByDividing(List<LeafNodePlan> initialPlans, List<LeafNodePlan> splittedPart){
		LinkedList<List<LeafNodePlan>> result = new LinkedList<List<LeafNodePlan>>();
		result.add(splittedPart);
		
		LinkedList<LeafNodePlan> otherPart = new LinkedList<LeafNodePlan>(initialPlans);
		otherPart.removeAll(splittedPart);
		result.add(otherPart);
		
		return result;
	}
}
