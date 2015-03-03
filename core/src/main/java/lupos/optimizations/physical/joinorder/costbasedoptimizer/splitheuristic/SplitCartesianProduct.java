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
package lupos.optimizations.physical.joinorder.costbasedoptimizer.splitheuristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Variable;
import lupos.misc.Tuple;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.plan.LeafNodePlan;

/**
 * This class splits the list of leaf nodes whenever there is a cartesian product...
 *
 * @author groppe
 * @version $Id: $Id
 */
public class SplitCartesianProduct implements SplitHeuristic {

	/** {@inheritDoc} */
	@Override
	public List<List<LeafNodePlan>> split(List<LeafNodePlan> initialPlans) {
		// Are there any cartesian products, where we can split the plan?
		return cartesianProducts(initialPlans);
	}

	/**
	 * Determines all the cartesian products and returns them
	 *
	 * @param initialPlans all leaf nodes in the plans representing single triple patterns to join
	 * @return groups of leaf nodes, if there are several groups, then there exist cartesian products between these groups
	 */
	protected static List<List<LeafNodePlan>> cartesianProducts(final List<LeafNodePlan> initialPlans) {
		final List<ListOrReference> listOrReferences = new ArrayList<ListOrReference>();
		final HashMap<Variable, Integer> map = new HashMap<Variable, Integer>(); // this map maps variables to the group of leaf nodes in which they occur
		for (final LeafNodePlan leafNode : initialPlans) {
			final HashSet<Integer> existingLists = new HashSet<Integer>();
			for (final Variable v : leafNode.getVariables()) {
				if (map.containsKey(v)){
					existingLists.add(map.get(v));
				}
			}
			int index;
			if (existingLists.size() == 0) {
				// make new list
				index = listOrReferences.size();
				final LinkedList<LeafNodePlan> newList = new LinkedList<LeafNodePlan>();
				newList.add(leafNode);
				listOrReferences.add(new ListOrReference(newList));
			} else if (existingLists.size() > 1) {
				// merge lists! (these several lists are grouped together as they have common join variables)
				final Iterator<Integer> intIt = existingLists.iterator();
				final Tuple<List<LeafNodePlan>, Integer> mergedInTuple = unfold(listOrReferences, intIt.next());
				final List<LeafNodePlan> mergedInList = mergedInTuple.getFirst();
				index = mergedInTuple.getSecond();
				mergedInList.add(leafNode);
				while (intIt.hasNext()) {
					final Tuple<List<LeafNodePlan>, Integer> toBeMergedTuple = unfold(listOrReferences, intIt.next());
					mergedInList.addAll(toBeMergedTuple.getFirst());
					listOrReferences.set(toBeMergedTuple.getSecond(), new ListOrReference(index));
				}
			} else {
				// use existing list (if there is only one list already!)
				index = existingLists.iterator().next();
				unfold(listOrReferences, index).getFirst().add(leafNode);
			}
			// all variables of this leaf node belong to the determined or newly created list!
			for (final Variable v : leafNode.getVariables()) {
				map.put(v, index);
			}
		}
		// compute the result
		final List<List<LeafNodePlan>> result = new LinkedList<List<LeafNodePlan>>();
		for (final ListOrReference listOrReference : listOrReferences) {
			if (listOrReference.isList()){
				result.add(listOrReference.getList());
			}
		}
		return result;
	}
	
	/**
	 * This method is used to unfold the element at position index (with possible references to other lists)
	 *
	 * @param listOrReferences the list of lists or references to other lists
	 * @param index the index position in the list to unfold
	 * @return the unfolded list
	 */
	protected static Tuple<List<LeafNodePlan>, Integer> unfold(final List<ListOrReference> listOrReferences, final int index){
		return listOrReferences.get(index).unfold(listOrReferences, index);
	}
	
	/**
	 * This class hold either a list of leaf node plans or a reference to another ListOrReference element (expressed by an index position) 
	 */
	private static class ListOrReference {
		/**
		 * the list of leaf nodes
		 */
		private List<LeafNodePlan> list = null;
		/**
		 * the index position as reference to another ListOrReference element
		 */
		private int index = -1;
		
		/**
		 * Constructor to store a list of leaf nodes
		 * @param list the list of leaf nodes
		 */
		public ListOrReference(final List<LeafNodePlan> list){
			this.list = list;
		}

		/**
		 * Constructor to store the index position as reference to another ListOrReference element
		 * @param index the index position as reference to another ListOrReference element
		 */
		public ListOrReference(final int index){
			this.index = index;
		}
		
		/**
		 * Is this element a list or reference?
		 * @return true, if this element is a list, false otherwise
		 */
		public boolean isList(){
			return this.list!=null;
		}
		
		/**
		 * This method returns the list of leaf nodes 
		 * @return the list of leaf nodes
		 */
		public List<LeafNodePlan> getList(){
			return this.list;
		}
		
		/**
		 * Unfolds the list by following the references until finally a list of leaf nodes is found
		 * @param listOrReferences the list of ListOrReference elements
		 * @param lastIndex the index position (in listOrReferences) of the currently considered element
		 * @return the unfolded list of leaf nodes and the index position (in listOrReferences) of this list
		 */
		public Tuple<List<LeafNodePlan>, Integer> unfold(final List<ListOrReference> listOrReferences, final int lastIndex){
			if(this.list!=null){
				return new Tuple<List<LeafNodePlan>, Integer>(this.list, lastIndex);
			} else {
				if(this.index>=0){
					return listOrReferences.get(this.index).unfold(listOrReferences, this.index);
				} else {
					return null;
				}
			}
		}
	}
}
