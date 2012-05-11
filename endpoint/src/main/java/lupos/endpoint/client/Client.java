package lupos.endpoint.client;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import lupos.datastructures.queryresult.QueryResult;

public class Client {
	
	public static QueryResult submitQuery(final String url, final String query /* format missing */) throws IOException{
		// TODO
		InputStream response = doSubmit(url, "query="+URLEncoder.encode(query)+"&format=" /* TODO*/, true);
		return null;
	}
	
	public static InputStream doSubmit(final String url, String content, final boolean useMethodGET) throws IOException {
		URL siteUrl = new URL(url);
		
		HttpURLConnection conn = (HttpURLConnection) siteUrl.openConnection();
		conn.setRequestMethod(useMethodGET ? "GET": "POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		
		out.writeBytes(content);
		out.flush();
		out.close();
		return new BufferedInputStream(conn.getInputStream());
	}
}
