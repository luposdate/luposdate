package lupos.engine.operators.application;

import java.util.Date;

import lupos.datastructures.queryresult.QueryResult;

public class MeasureTime implements Application {
	private long start;
	private long time;
	private long firstResult = -1;

	public void call(final QueryResult res) {
		if (firstResult == -1) {
			firstResult = new Date().getTime() - start;
		}
	}

	public void start(final Type type) {
		start = new Date().getTime();
	}

	public void stop() {
		time = new Date().getTime() - start;
	}

	public long getTimeForFirstResult() {
		return firstResult;
	}

	public long getTimeForQuery() {
		return time;
	}

	public void deleteResult(final QueryResult res) {
	}

	public void deleteResult() {
	}
}
