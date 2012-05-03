package lupos.engine.operators.singleinput;

import java.util.LinkedList;

import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.application.Application;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;

public class Result extends SingleInputOperator {
	protected LinkedList<Application> apps = new LinkedList<Application>();

	public void addApplication(final Application app) {
		apps.add(app);
	}
	
	public void clearApplications(){
		apps.clear();
	}

	public void addFirstApplication(final Application app) {
		apps.addFirst(app);
	}

	public void start() {
		Application.Type type = Application.Type.SELECT;
		for (final BasicOperator bo : precedingOperators) {
			if (bo instanceof MakeBooleanResult)
				type = Application.Type.ASK;
			else if (bo instanceof Construct)
				type = Application.Type.CONSTRUCT;
		}
		for (final Application app : apps)
			app.start(type);
	}

	public Message preProcessMessage(final StartOfEvaluationMessage msg) {
		start();
		return msg;
	}

	public Message preProcessMessage(final EndOfEvaluationMessage eos) {
		close();
		return eos;
	}

	public void close() {
		for (final Application app : apps)
			app.stop();
	}

	protected void callAll(final QueryResult res) {
		for (final Application app : apps) {
			app.call(res);
		}
		res.release();
	}

	public synchronized QueryResult process(final QueryResult res,
			final int operandID) {
		if (res != null) {
			callAll(res);
			res.release();
		}
		return null;
	}

	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		for (final Application app : apps) {
			app.deleteResult(queryResult);
		}
		return null;
	}

	public void deleteQueryResult(final int operandID) {
		for (final Application app : apps) {
			app.deleteResult();
		}
	}

}
