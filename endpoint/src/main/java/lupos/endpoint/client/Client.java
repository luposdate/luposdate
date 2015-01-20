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
package lupos.endpoint.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.client.formatreader.CSVFormatReader;
import lupos.endpoint.client.formatreader.JSONFormatReader;
import lupos.endpoint.client.formatreader.MIMEFormatReader;
import lupos.endpoint.client.formatreader.TSVFormatReader;
import lupos.endpoint.client.formatreader.TripleFormatReader;
import lupos.endpoint.client.formatreader.XMLFormatReader;
import lupos.misc.FileHelper;
import lupos.misc.Tuple;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class Client {

	public static String DEFAULT_FORMAT = XMLFormatReader.MIMETYPE;

	public final static int LIMIT_OF_BYTES_FOR_GET = 4*1024;

	// enable or disable logging into console
	private final static boolean log = false;

	protected static HashMap<String, MIMEFormatReader> registeredFormatReaders;

	/**
	 * register the different MIME type format readers...
	 */
	static {
		Client.registeredFormatReaders = new HashMap<String, MIMEFormatReader>();
		Client.registerFormatReader(new CSVFormatReader());
		Client.registerFormatReader(new TSVFormatReader());
		Client.registerFormatReader(new JSONFormatReader());
		Client.registerFormatReader(new JSONFormatReader(true));
		Client.registerFormatReader(new XMLFormatReader());
		Client.registerFormatReader(new XMLFormatReader(true));
		Client.registerFormatReader(new TripleFormatReader("N3", "text/n3", "N3"));
		Client.registerFormatReader(new TripleFormatReader("Turtle", "text/turtle", "Turtle"));
		Client.registerFormatReader(new TripleFormatReader("RDF XML", "application/rdf+xml", "Rdfxml"));
	}

	public static void registerFormatReader(final MIMEFormatReader formatReader){
		Client.registeredFormatReaders.put(formatReader.getMIMEType(), formatReader);
	}

	public static QueryResult submitQuery(final String url, final String query, final BindingsFactory bindingsFactory) throws IOException {
		return Client.submitQuery(url, query, DEFAULT_FORMAT, bindingsFactory);
	}

	public static QueryResult submitQuery(final String url, final String query, final String formatKey, final BindingsFactory bindingsFactory) throws IOException {
		final Tuple<String, InputStream> response = submitQueryAndRetrieveStream(url, query, formatKey);
		final String contentType = response.getFirst();
		if(contentType==null){
			System.err.println("Content type missing in response of SPARQL endpoint!");
			Thread.dumpStack();
			return null;
		}
		MIMEFormatReader reader = Client.registeredFormatReaders.get(contentType);
		if(reader==null){
			final String[] contentTypeParts=contentType.split(";");
			for(final String contentTypeSecondTry: contentTypeParts){
				reader = Client.registeredFormatReaders.get(contentTypeSecondTry);
				if(reader!=null){
					return reader.getQueryResult(response.getSecond(), query, bindingsFactory);
				}
			}
			if(contentType.compareTo("text/plain")==0){
				final String errorMessage = "Error message received:\n" + FileHelper.readInputStreamToString(response.getSecond());
				System.err.println(errorMessage);
				throw new RuntimeException(errorMessage);
			}
			final String errorText = "Content type "+contentType+" is not supported!";
			System.err.println(errorText);
			throw new IOException(errorText + "Content:\n" + FileHelper.readInputStreamToString(response.getSecond()));
		}
		return reader.getQueryResult(response.getSecond(), query, bindingsFactory);
	}

	public static Tuple<String, InputStream> submitQueryAndRetrieveStream(final String url, final String query, final String formatKey) throws IOException {
		final List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("query", query));
		params.add(new BasicNameValuePair("format", formatKey));
		return doSubmit(url, params, formatKey);
	}

	public static Tuple<String, InputStream> doSubmit(final String url, final List<NameValuePair> content, final String requestHeader) throws IOException {
		int size=url.length();
		for(final NameValuePair entry: content){
			size += entry.getName().length()+entry.getValue().length()+2; // size determination of &/? key = value
		}
		return doSubmit(url, content, requestHeader, size<Client.LIMIT_OF_BYTES_FOR_GET);
	}


	public static Tuple<String, InputStream> doSubmit(final String url, final List<NameValuePair> content, final String requestHeader, final boolean useMethodGET) throws IOException {
		final HttpClient httpclient = new DefaultHttpClient();
		final HttpUriRequest httpurirequest;
		if(useMethodGET){
			// first build uri with get parameters...
			String urlAndParams = url;
			boolean firstTime = true;
			for(final NameValuePair param: content){
				if(firstTime){
					urlAndParams += "?";
					firstTime = false;
				} else {
					urlAndParams += "&";
				}
				urlAndParams += param.getName() + "=" + URLEncoder.encode(param.getValue(), "UTF-8");
			}
			final HttpGet httpget = new HttpGet(urlAndParams);
			httpget.setHeader("Accept", requestHeader);
			httpurirequest = httpget;
		} else {
			final HttpPost httppost = new HttpPost(url);
			httppost.setHeader("Accept", requestHeader);
			httppost.setEntity(new UrlEncodedFormEntity(content, org.apache.commons.lang.CharEncoding.UTF_8));
			httpurirequest = httppost;
		}
		final HttpResponse response = httpclient.execute(httpurirequest);
		final HttpEntity entity = response.getEntity();
		InputStream in = entity.getContent();
		if(Client.log){
			in = new InputStreamLogger(in);
		}
		return new Tuple<String, InputStream>(entity.getContentType().getValue(), new BufferedInputStream(in));
	}

	/**
	 * Sends an {@link InputStream} to a Endpoint-URL
	 * @param url the url
	 * @param stream the stream to be sent
	 * @param requestHeader the header for the request
	 * @return Tuple with content type and the answer as stream
	 * @throws IOException
	 */
	public static Tuple<String, InputStream> doSubmitStream(final String url, final InputStream stream, final String requestHeader) throws IOException {
		final HttpClient httpclient = new DefaultHttpClient();
		final HttpUriRequest httpurirequest;

		final HttpPost httppost = new HttpPost(url);
		httppost.setHeader("Accept", requestHeader);
		final InputStreamEntity ise = new InputStreamEntity(stream,-1);
		httppost.setEntity(ise);
		httpurirequest = httppost;

		final HttpResponse response = httpclient.execute(httpurirequest);
		final HttpEntity entity = response.getEntity();
		InputStream in = entity.getContent();

		if(Client.log){
			in = new InputStreamLogger(in);
		}
		return new Tuple<String, InputStream>(entity.getContentType().getValue(), new BufferedInputStream(in));
	}

	public static class InputStreamLogger extends InputStream {

		private final InputStream piped;

		public InputStreamLogger(final InputStream piped){
			this.piped = piped;
		}

		@Override
		public int read() throws IOException {
			final int result = this.piped.read();
			if(result>=0){
				for(final char c: Character.toChars(result)){
					System.out.print(c);
				}
			}
			return result;
		}

		@Override
		public void close() throws IOException {
			this.piped.close();
		}
	}
}
