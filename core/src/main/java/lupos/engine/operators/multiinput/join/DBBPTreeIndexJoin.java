package lupos.engine.operators.multiinput.join;

import java.io.IOException;

import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.paged_dbbptree.StandardNodeDeSerializer;
import lupos.datastructures.queryresult.QueryResult;

public class DBBPTreeIndexJoin extends IndexJoin {

	@Override
	public void init() {
		lba = new DBBPTree[2];
		try {
			lba[0] = new DBBPTree<String, QueryResult>(30, 30, new StandardNodeDeSerializer<String, QueryResult>(String.class, QueryResult.class));
			lba[1] = new DBBPTree<String, QueryResult>(30, 30, new StandardNodeDeSerializer<String, QueryResult>(String.class, QueryResult.class));
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}
}
