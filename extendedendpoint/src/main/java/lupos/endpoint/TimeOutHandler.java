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
 */
package lupos.endpoint;

import java.io.IOException;

import lupos.endpoint.contexts.InterruptableHttpHandler;
import lupos.endpoint.contexts.QueryHandlerHelper;
import lupos.misc.TimeInterval;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * This class implements a quick-and-dirty timeout mechanism for the extended endpoint.
 * It should be replaced with a safer method, if the the underlying HttpHandler can be stopped "softly".
 */
public class TimeOutHandler implements HttpHandler {

	public static long TIMEOUT = 1000*60*3;
	private final InterruptableHttpHandler innerHandler;

	public TimeOutHandler(final InterruptableHttpHandler innerHandler){
		this.innerHandler = innerHandler;
	}

	@Override
	public void handle(final HttpExchange httpExchange) throws IOException {
		final Runner runner = new Runner(httpExchange);
		runner.start();
		try {
			runner.join(TimeOutHandler.TIMEOUT);
			if(!runner.succeeded){
				// timeout occurred!
				runner.stop(); // this is really not recommended!
				System.err.println("Timeout!");
				// send error message
				this.sendErrorMessage(httpExchange, "Timeout: Query processing aborted as processing takes over " + TimeInterval.toString(TimeOutHandler.TIMEOUT));
			}
		} catch (final InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public void sendErrorMessage(final HttpExchange httpExchange, final String error){
		final JSONObject response = new JSONObject();
		response.put("error", error);
		final int responseStatus = QueryHandlerHelper.HTTP_OK;
		try {
			QueryHandlerHelper.sendResponse(httpExchange, response, responseStatus);
		} catch (final IOException e1) {
			System.err.println(e1);
			e1.printStackTrace();
		}
	}

	private class Runner extends Thread {
		private final HttpExchange httpExchange;
		private volatile boolean succeeded = false;
		public Runner(final HttpExchange httpExchange){
			this.httpExchange = httpExchange;
		}
		@Override
		public void run(){
			try {
				this.succeeded = TimeOutHandler.this.innerHandler.handle(this.httpExchange);
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
				TimeOutHandler.this.sendErrorMessage(this.httpExchange, e.getMessage());
			}
		}
	}
}
