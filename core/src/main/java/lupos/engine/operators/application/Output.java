package lupos.engine.operators.application;

import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.QueryResult;

public class Output implements Application {
	private boolean iHasResult = false;

	public void call(final QueryResult res) {
		if (!(res instanceof BooleanResult && iHasResult)) {
			iHasResult = true;
			System.out.println(res);
		}
	}

	public void start(final Type type) {
		System.out.println("Results:");
	}

	public void stop() {
		if (!iHasResult)
			System.out.println("I no has resultz????");
	}

	public void deleteResult(final QueryResult res) {
		if (res != null) {
			System.out.println("To be deleted: " + res);
		}
	}

	public void deleteResult() {
		System.out.println("All results so far deleted!");
	}
}
