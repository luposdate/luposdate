/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
	}

	public synchronized QueryResult process(final QueryResult res,
			final int operandID) {
		if (res != null) {
			callAll(res);
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
