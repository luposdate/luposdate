package lupos.engine.operators.multiinput.join;

import java.util.TreeMap;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.sorteddata.ElementCounter;
import lupos.datastructures.sorteddata.SortedBagImplementation;

public class TreeBagMergeJoin extends MergeJoin {

	public TreeBagMergeJoin() {
		init(new SortedBagImplementation<Bindings>(new TreeMap<Bindings, ElementCounter<Bindings>>(comp)), new SortedBagImplementation(new TreeMap<Bindings, ElementCounter<Bindings>>(comp)));
	}

}
