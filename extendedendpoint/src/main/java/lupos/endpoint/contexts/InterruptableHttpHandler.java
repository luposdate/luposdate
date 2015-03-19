package lupos.endpoint.contexts;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

public interface InterruptableHttpHandler {
	/**
	 * The same as HttpHandler except that the method must return true if all has been processed, otherwise (in case of interruption) false
	 * @param t for getting the sent data and sending the response...
	 * @return true if all has been processed, otherwise (in case of interruption) false
	 * @throws IOException
	 */
	public boolean handle(final HttpExchange t) throws IOException;
}
