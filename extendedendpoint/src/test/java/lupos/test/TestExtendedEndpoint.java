package lupos.test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

/**
 * This class demonstrates how to submit a SPARQL query and rdf data to the extended endpoint and retrieve its result as JSON object...
 */
public class TestExtendedEndpoint {

	private final static String USER_AGENT = "Mozilla/5.0";

	private final static String endpointurl = "https://www.ifis.uni-luebeck.de/sparql-endpoint/nonstandard/sparql";
	private final static String query = "SELECT * WHERE { ?s ?p ?o. } LIMIT 10";
	private final static String data = "<s1> <p1> <o1>, <o2>; <p2> <o3>.\r\n<s2> <p3> <o4>.";

	public static void main(final String[] args) throws Exception {
		System.setProperty("jsse.enableSNIExtension", "false"); // workaround for a bug in java
		final JSONObject result = sendPost(TestExtendedEndpoint.endpointurl, TestExtendedEndpoint.query, TestExtendedEndpoint.data);
		System.out.println("Result:");
		System.out.println(result.toString(2));
	}

	private static JSONObject sendPost(final String url, final String query, final String data) throws Exception {

		final URL obj = new URL(url);
		final HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		//add request header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		final JSONObject request = new JSONObject();
		request.put("query", query);
		request.put("rdf", data);

		// Send post request
		con.setDoOutput(true);
		final DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(request.toString());
		wr.flush();
		wr.close();

		final int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + request.toString());
		System.out.println("Response Code : " + responseCode);

		final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		final StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		final JSONObject result = new JSONObject(response.toString());
		return result;
	}
}
