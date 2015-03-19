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
