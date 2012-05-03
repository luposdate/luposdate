package lupos.engine.operators.application;

import lupos.datastructures.queryresult.QueryResult;

public interface Application {
	public enum Type {
		SELECT, ASK, CONSTRUCT, DESCRIBE;
	}

	public void call(QueryResult res);

	public void start(Type type);

	public void stop();

	public void deleteResult(QueryResult res);

	public void deleteResult();
}
