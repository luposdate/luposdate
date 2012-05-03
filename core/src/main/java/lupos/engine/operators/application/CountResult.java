package lupos.engine.operators.application;

import lupos.datastructures.queryresult.QueryResult;

public class CountResult implements Application {

	private int numberResults = 0;

	public void call(final QueryResult res) {
		numberResults += res.oneTimeSize();
	}

	public void start(final Type type) {
		numberResults = 0;
	}

	public void stop() {
	}

	public int getNumberResults() {
		return numberResults;
	}

	public void deleteResult(final QueryResult res) {
	}

	public void deleteResult() {
	}
}
