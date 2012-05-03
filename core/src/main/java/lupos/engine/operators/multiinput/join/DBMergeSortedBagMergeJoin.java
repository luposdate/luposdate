package lupos.engine.operators.multiinput.join;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;

public class DBMergeSortedBagMergeJoin extends MergeJoin {
	
	private static int HEAPHEIGHT=5;

	public DBMergeSortedBagMergeJoin() {
		init(new DBMergeSortedBag<Bindings>(HEAPHEIGHT,comp,Bindings.class), new DBMergeSortedBag<Bindings>(HEAPHEIGHT,comp,Bindings.class));
	}

}
