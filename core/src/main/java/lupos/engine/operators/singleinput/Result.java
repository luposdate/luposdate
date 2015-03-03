
/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
 *
 * @author groppe
 * @version $Id: $Id
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

	/**
	 * <p>addApplication.</p>
	 *
	 * @param app a {@link lupos.engine.operators.application.Application} object.
	 */
	public void addApplication(final Application app) {
		apps.add(app);
	}
	
	/**
	 * <p>clearApplications.</p>
	 */
	public void clearApplications(){
		apps.clear();
	}

	/**
	 * <p>addFirstApplication.</p>
	 *
	 * @param app a {@link lupos.engine.operators.application.Application} object.
	 */
	public void addFirstApplication(final Application app) {
		apps.addFirst(app);
	}

	/**
	 * <p>start.</p>
	 */
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

	/**
	 * <p>preProcessMessage.</p>
	 *
	 * @param msg a {@link lupos.engine.operators.messages.StartOfEvaluationMessage} object.
	 * @return a {@link lupos.engine.operators.messages.Message} object.
	 */
	public Message preProcessMessage(final StartOfEvaluationMessage msg) {
		start();
		return msg;
	}

	/**
	 * <p>preProcessMessage.</p>
	 *
	 * @param eos a {@link lupos.engine.operators.messages.EndOfEvaluationMessage} object.
	 * @return a {@link lupos.engine.operators.messages.Message} object.
	 */
	public Message preProcessMessage(final EndOfEvaluationMessage eos) {
		close();
		return eos;
	}

	/**
	 * <p>close.</p>
	 */
	public void close() {
		for (final Application app : apps)
			app.stop();
	}

	/**
	 * <p>callAll.</p>
	 *
	 * @param res a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	protected void callAll(final QueryResult res) {
		for (final Application app : apps) {
			app.call(res);
		}
	}

	/** {@inheritDoc} */
	public synchronized QueryResult process(final QueryResult res,
			final int operandID) {
		if (res != null) {
			callAll(res);
		}
		return null;
	}

	/** {@inheritDoc} */
	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		for (final Application app : apps) {
			app.deleteResult(queryResult);
		}
		return null;
	}

	/** {@inheritDoc} */
	public void deleteQueryResult(final int operandID) {
		for (final Application app : apps) {
			app.deleteResult();
		}
	}

}
