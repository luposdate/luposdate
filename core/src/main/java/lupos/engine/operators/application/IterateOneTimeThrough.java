package lupos.engine.operators.application;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;

public class IterateOneTimeThrough implements Application {

	@Override
	public void call(QueryResult res) {
		this.oneTimeIteration(res);
	}

	@Override
	public void start(Type type) { // nothing to do...
	}

	@Override
	public void stop() { // nothing to do...
	}

	@Override
	public void deleteResult(QueryResult res) {
		this.oneTimeIteration(res);
	}

	@Override
	public void deleteResult() { // nothing to do...
	}

	private void oneTimeIteration(QueryResult res){
		Iterator<Bindings> it = res.oneTimeIterator();
		while(it.hasNext()){
			it.next();
		}
		if(it instanceof ParallelIterator){
			((ParallelIterator<Bindings>) it).close();
		}

	}
}
