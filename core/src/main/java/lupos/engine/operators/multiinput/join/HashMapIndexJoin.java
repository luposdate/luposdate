package lupos.engine.operators.multiinput.join;

import java.util.HashMap;

import lupos.datastructures.queryresult.QueryResult;

public class HashMapIndexJoin extends IndexJoin {

	@Override
	public void init() {
		lba = new HashMap[2];
		lba[0] = new HashMap<String, QueryResult>();
		lba[1] = new HashMap<String, QueryResult>();
	}

}
