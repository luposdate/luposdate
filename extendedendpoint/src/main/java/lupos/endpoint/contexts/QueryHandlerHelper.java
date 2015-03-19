package lupos.endpoint.contexts;

import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;

public class QueryHandlerHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryHandlerHelper.class.getName());

	public static final int HTTP_OK = 200;
	public static final int HTTP_BAD_REQUEST = 400;
	public static final int HTTP_METHOD_NOT_ALLOWED = 405;
	public static final int HTTP_INTERNAL_SERVER_ERROR = 500;

	public static void setDefaultHeaders(final HttpExchange t) {
		t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		t.getResponseHeaders().add("Content-Type", "application/json");
	}

	public static void sendResponse(final HttpExchange t, final JSONObject response, final int responseStatus) throws IOException{
		if(response!=null){
			QueryHandlerHelper.setDefaultHeaders(t);
			final byte[] bytesToSend = response.toString().getBytes();
			t.sendResponseHeaders(responseStatus, bytesToSend.length);
			t.getResponseBody().write(response.toString().getBytes());
			t.getResponseBody().close();
			LOGGER.info("Responded {} with {}", responseStatus, response.toString());
		}
	}
}
