package lupos.engine.operators.multiinput.join;

import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.smallerinmemorylargerondisk.MapImplementation;

public class HybridIndexJoin extends IndexJoin {

	@Override
	public void init() {
		lba = new MapImplementation[2];
		lba[0] = new MapImplementation<String, QueryResult>();
		lba[1] = new MapImplementation<String, QueryResult>();
	}
}
