package lupos.engine.operators.stream;

import lupos.datastructures.queryresult.QueryResult;

public class ConsoleNotifyStreamResult implements NotifyStreamResult {
	public void notifyStreamResult(final QueryResult result) {
		System.out.println("ConsoleNotifyStreamResult:");
		System.out.println(result.toString());
	}
}
