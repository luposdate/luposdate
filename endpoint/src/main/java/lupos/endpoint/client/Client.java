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
package lupos.endpoint.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.client.formatreader.CSVFormatReader;
import lupos.endpoint.client.formatreader.JSONFormatReader;
import lupos.endpoint.client.formatreader.MIMEFormatReader;
import lupos.endpoint.client.formatreader.TSVFormatReader;
import lupos.endpoint.client.formatreader.TripleFormatReader;
import lupos.endpoint.client.formatreader.XMLFormatReader;
import lupos.misc.Tuple;

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
		Client.registerFormatReader(new XMLFormatReader());
		Client.registerFormatReader(new TripleFormatReader("N3", "text/n3", "N3"));
		Client.registerFormatReader(new TripleFormatReader("Turtle", "text/turtle", "Turtle"));
		Client.registerFormatReader(new TripleFormatReader("RDF XML", "application/rdf+xml", "Rdfxml"));
	}
	
	public static void registerFormatReader(final MIMEFormatReader formatReader){
		Client.registeredFormatReaders.put(formatReader.getMIMEType(), formatReader);
	}

	public static QueryResult submitQuery(final String url, final String query) throws IOException {
		return Client.submitQuery(url, query, DEFAULT_FORMAT);
	}
	
	public static QueryResult submitQuery(final String url, final String query, final String formatKey) throws IOException {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("query", query));
		params.add(new BasicNameValuePair("format", formatKey));
		Tuple<String, InputStream> response = doSubmit(url, params);
		final String contentType = response.getFirst();
		if(contentType==null){
			System.err.println("Content type missing in response of SPARQL endpoint!");
			Thread.dumpStack();
			return null;
		}
		MIMEFormatReader reader = Client.registeredFormatReaders.get(contentType);
		if(reader==null){
			String[] contentTypeParts=contentType.split(";");
			for(String contentTypeSecondTry: contentTypeParts){
				reader = Client.registeredFormatReaders.get(contentTypeSecondTry);
				if(reader!=null){
					return reader.getQueryResult(response.getSecond());
				}
			}			
			System.err.println("Content type "+contentType+" is not supported!");
			Thread.dumpStack();
			System.err.println("Just try out application/sparql-results+xml as default...");
			reader = Client.registeredFormatReaders.get("application/sparql-results+xml");
			if(reader!=null){
				reader.getQueryResult(response.getSecond());
			} else {
				return null;
			}
		}		
		return reader.getQueryResult(response.getSecond());
	}

	public static Tuple<String, InputStream> doSubmit(final String url, List<NameValuePair> content) throws IOException {
		int size=url.length();
		for(NameValuePair entry: content){
			size += entry.getName().length()+entry.getValue().length()+2; // size determination of &/? key = value
		}
		return doSubmit(url, content, size<Client.LIMIT_OF_BYTES_FOR_GET);
	}

	
	public static Tuple<String, InputStream> doSubmit(final String url, final List<NameValuePair> content, final boolean useMethodGET) throws IOException {
		HttpClient httpclient = new DefaultHttpClient();
		final HttpUriRequest httpurirequest;
		if(useMethodGET){
			// first build uri with get parameters...
			String urlAndParams = url;
			boolean firstTime = true;
			for(NameValuePair param: content){
				if(firstTime){
					urlAndParams += "?";
					firstTime = false;
				} else {
					urlAndParams += "&";
				}
				urlAndParams += param.getName() + "=" + URLEncoder.encode(param.getValue(), "UTF-8");
			}
			httpurirequest = new HttpGet(urlAndParams);
		} else {
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(new UrlEncodedFormEntity(content, HTTP.UTF_8));
			httpurirequest = httppost;
		}
		HttpResponse response = httpclient.execute(httpurirequest);
		HttpEntity entity = response.getEntity();
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
			int result = this.piped.read();
			if(result>=0){
				for(char c: Character.toChars(result)){
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
