/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.endpoint.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.server.format.CSVFormatter;
import lupos.endpoint.server.format.Formatter;
import lupos.endpoint.server.format.HTMLFormatter;
import lupos.endpoint.server.format.JSONFormatter;
import lupos.endpoint.server.format.PlainFormatter;
import lupos.endpoint.server.format.QueryTriplesFormatter;
import lupos.endpoint.server.format.TSVFormatter;
import lupos.endpoint.server.format.XMLFormatter;
import lupos.engine.evaluators.BasicIndexQueryEvaluator;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.RDF3XQueryEvaluator;
import lupos.engine.operators.singleinput.federated.BitVectorFilterFunction;

import com.google.common.collect.Sets;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class Endpoint {

	// enable or disable logging into console
	public static boolean log = false;
	// enable or disable logging the sizes of the query and its response into console
	public static boolean sizelog = false;

	private final static Map<String, Formatter> registeredFormatter = Collections.synchronizedMap(new HashMap<String, Formatter>());

	private final static Map<String, HttpHandler> registeredhandler = Collections.synchronizedMap(new HashMap<String, HttpHandler>());

	private static HTMLForm htmlForm = new StandardHTMLForm();

	public static Class<? extends Bindings> defaultBindingsClass = Bindings.instanceClass;

	public static void registerFormatter(final Formatter formatter){
		Endpoint.registeredFormatter.put(formatter.getKey().toLowerCase(), formatter);
		Endpoint.registeredFormatter.put(formatter.getName().toLowerCase(), formatter);
	}

	public static Map<String, Formatter> getRegisteredFormatters(){
		return Endpoint.registeredFormatter;
	}

	public static void registerHandler(final String context, final HttpHandler httpHandler){
		Endpoint.registeredhandler.put(context, httpHandler);
	}

	public static HTMLForm getHTMLForm(){
		return Endpoint.htmlForm;
	}

	public static void setHTMLForm(final HTMLForm htmlForm){
		Endpoint.htmlForm = htmlForm;
	}

	public static Class<? extends Bindings> getDefaultBindingsClass() {
		return defaultBindingsClass;
	}

	/**
	 * HTTP Request Example
	 * http://localhost:8080/sparql?query=PREFIX+rdf%3A%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E+SELECT+*+WHERE%7B+%3Fs+rdf%3Atype+%3Fo.+%7D&format=application%2Fsparql-results%2Bxml
	 * for query
	 * PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT * WHERE{?s rdf:type ?o. }
	 */
	public static void main(final String[] args) throws Exception {
		final int port = Endpoint.init(args);
		Endpoint.registerStandardFormattersAndContexts(args[0]);
		Endpoint.initAndStartServer(port);
	}

	public static int init(final String[] args){
		if (args.length < 1) {
			System.err.println("Usage:\njava -Xmx768M lupos.endpoint.server.Endpoint <directory for indices> [portX] [output] [size]");
			System.err.println("(The indices can be constructed using lupos.engine.indexconstruction.FastRDF3XIndexConstruction)");
			System.err.println("If \"portX\" is given, the port X (default 8080) is used, X must be a non-negative number.");
			System.err.println("If \"output\" is given, the response is written to console.");
			System.err.println("If \"size\" is given, the size of the received query and the size of the response is written to console.");
			System.exit(0);
		}
		int port = 8080;
		for(int i=1; i<args.length; i++){
			if(args[i].compareTo("output")==0){
				Endpoint.log = true;
			} else if(args[i].compareTo("size")==0){
				Endpoint.sizelog = true;
			} else if(args[i].startsWith("port")){
				port = Integer.parseInt(args[i].substring("port".length()));
			}
		}
		return port;
	}

	public static void initAndStartServer(final int port){
		try {
			final String localHost = InetAddress.getLocalHost().getHostName();
			System.out.println("Starting LUPOSDATE Endpoint on host: "+localHost);
			System.out.println("Canonical host name: "+InetAddress.getLocalHost().getCanonicalHostName());
			for (final InetAddress ia : InetAddress.getAllByName(localHost)){
				System.out.println("IP: "+ia);
			}

			Endpoint.startServer(port);
			System.out.println("Endpoint ready to receive requests...");
			System.out.println("_____________________________________");
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public static BasicIndexQueryEvaluator createQueryEvaluator(final String directory){
		try {
			final RDF3XQueryEvaluator evaluator = new RDF3XQueryEvaluator();
			evaluator.loadLargeScaleIndices(directory);
			defaultBindingsClass = Bindings.instanceClass;
			return evaluator;
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * register the standard formatters and contexts of the server...
	 */
	public static void registerStandardFormattersAndContexts(final String directory) {
		Endpoint.registerStandardFormatter();
		Endpoint.registerStandardContexts(directory);
	}

	public static void registerStandardFormatter(){
		Endpoint.registerFormatter(new XMLFormatter());
		Endpoint.registerFormatter(new XMLFormatter(true));
		Endpoint.registerFormatter(new PlainFormatter());
		Endpoint.registerFormatter(new JSONFormatter());
		Endpoint.registerFormatter(new JSONFormatter(true));
		Endpoint.registerFormatter(new CSVFormatter());
		Endpoint.registerFormatter(new TSVFormatter());
		Endpoint.registerFormatter(new HTMLFormatter(false));
		Endpoint.registerFormatter(new HTMLFormatter(true));
		Endpoint.registerFormatter(new HTMLFormatter(false, true));
		Endpoint.registerFormatter(new HTMLFormatter(true, true));
		Endpoint.registerFormatter(new QueryTriplesFormatter());
	}

	public static void registerStandardContexts(final String directory){
		Endpoint.registerHandler("/sparql", new SPARQLHandler(new SPARQLExecutionImplementation(Endpoint.createQueryEvaluator(directory), directory)));
		Endpoint.registerHandler("/", new HTMLFormHandler());
	}

	public static void startServer(final int port){
		try {
			final HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

			for(final Entry<String, HttpHandler> entry: Endpoint.registeredhandler.entrySet()){
				server.createContext(entry.getKey(), entry.getValue());
			}

			server.setExecutor(null);
			server.start();
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public static String getResponse(final HttpExchange t) throws IOException {
		final String requestMethod = t.getRequestMethod();
		String response;
		if (requestMethod.equalsIgnoreCase("POST")) {
			final InputStream bodyStream = t.getRequestBody();
			final StringBuilder builder = new StringBuilder();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(bodyStream, "UTF-8"));
			String readLine;
			boolean firstTime=true;
			while ((readLine = reader.readLine()) != null) {
				if(firstTime){
					firstTime=false;
				} else {
					builder.append("\n");
				}
				builder.append(readLine);
			}
			response=builder.toString();
		} else if (requestMethod.equalsIgnoreCase("GET")) {
			response = t.getRequestURI().getRawQuery();
		} else {
			response = "";
		}
		return response;
	}

	public static interface SPARQLExecution {
		public void execute(final String queryParameter, final Formatter formatter, final HttpExchange t) throws IOException;
	}

	public static class SPARQLExecutionImplementation implements SPARQLExecution {

		protected final BasicIndexQueryEvaluator evaluator;
		protected final String dir;

		public SPARQLExecutionImplementation(final BasicIndexQueryEvaluator evaluator, final String dir){
			this.evaluator = evaluator;
			this.dir = dir;
		}

		@Override
		public void execute(final String queryParameter, final Formatter formatter, final HttpExchange t) throws IOException {
			if(Endpoint.sizelog){
				System.out.println("Size of the received query (number of characters): "+queryParameter.length());
			}
			try {
				synchronized(this.evaluator){ // avoid any inference of several queries in parallel!
					System.out.println("Evaluating query:\n"+queryParameter);
					if((this.evaluator instanceof CommonCoreQueryEvaluator) && formatter.isWriteQueryTriples()){
						// log query-triples by using BindingsArrayReadTriples as class for storing the query solutions!
						Bindings.instanceClass = BindingsArrayReadTriples.class;
					} else {
						Bindings.instanceClass = Endpoint.defaultBindingsClass;
					}
					final QueryResult queryResult = (this.evaluator instanceof CommonCoreQueryEvaluator)?((CommonCoreQueryEvaluator)this.evaluator).getResult(queryParameter, true):this.evaluator.getResult(queryParameter);
					final String mimeType = formatter.getMIMEType(queryResult);
					System.out.println("Done, sending response using MIME type "+mimeType);
					t.getResponseHeaders().add("Content-type", mimeType);
					t.getResponseHeaders().add("Transfer-encoding", "chunked");
					t.sendResponseHeaders(200, 0);
					OutputStream os = t.getResponseBody();
					if(Endpoint.log){
						os = new OutputStreamLogger(os);
					}
					if(Endpoint.sizelog){
						os = new OutputStreamSizeLogger(os);
					}
					formatter.writeResult(os, this.evaluator.getVariablesOfQuery(), queryResult);
					os.close();
					if(this.evaluator instanceof RDF3XQueryEvaluator){
						((RDF3XQueryEvaluator)this.evaluator).writeOutIndexFileAndModifiedPages(this.dir);
					}
				}
				return;
			} catch (final Error e) {
				System.err.println(e);
				e.printStackTrace();
				t.getResponseHeaders().add("Content-type", "text/plain");
				final String answer = "Error:\n"+e.getMessage();
				System.out.println(answer);
				Endpoint.sendString(t, answer);
				return;
			} catch (final Exception e){
				System.err.println(e);
				e.printStackTrace();
				t.getResponseHeaders().add("Content-type", "text/plain");
				final String answer = "Error:\n"+e.getMessage();
				System.out.println(answer);
				Endpoint.sendString(t, answer);
				return;
			}
		}
	}

	public static class SPARQLHandler implements HttpHandler {

		private final SPARQLExecution sparqlExecution;

		public SPARQLHandler(final SPARQLExecution sparqlExecution){
			super();
			this.sparqlExecution = sparqlExecution;
			BitVectorFilterFunction.register();
		}

		private final static String format = "format=";
		private final static String query = "query=";

		@Override
		public void handle(final HttpExchange t) throws IOException {
			System.out.println("\n-> Receiving request from: "+t.getRequestHeaders().get("Host"));
			final String response = Endpoint.getResponse(t);
			final String[] responseParts = response.split("[&]");
			if(responseParts.length>0){
				// first check whether or not a format is given (default is XML as defined by W3C)
				final String formatParameter = Endpoint.getParameter(responseParts, format, "XML");
				final Formatter formatter = registeredFormatter.get(formatParameter.toLowerCase());
				if(formatter == null){
					t.getResponseHeaders().add("Content-type", "text/plain");
					final String answer = "Bad Request: format " + formatParameter + " not supported";
					System.out.println(answer);
					Endpoint.sendString(t, answer);
					return;
				}
				// now look for a query parameter
				final String queryParameter = getParameter(responseParts, query);
				if(queryParameter!=null){
					this.sparqlExecution.execute(queryParameter, formatter, t);
				} else {
					t.getResponseHeaders().add("Content-type", "text/plain");
					final String answer = "Bad Request: query parameter missing";
					System.out.println(answer);
					Endpoint.sendString(t, answer);
					return;
				}
			}
			Endpoint.htmlForm.sendHTMLForm(t);
		}
	}

	public static class HTMLFormHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange t) throws IOException {
			Endpoint.htmlForm.sendHTMLForm(t);
		}
	}

	protected static String getParameter(final String[] responseParts, final String parameter) throws UnsupportedEncodingException{
		for(final String item: responseParts){
			if(item.startsWith(parameter)){
				return URLDecoder.decode(item.substring(parameter.length()), "UTF-8");
			}
		}
		return null;
	}

	protected static String getParameter(final String[] responseParts, final String parameter, final String defaultValue) throws UnsupportedEncodingException{
		final String result = Endpoint.getParameter(responseParts, parameter);
		if(result!=null){
			return result;
		} else {
			return defaultValue;
		}
	}

	public static void sendString(final HttpExchange t, final String toSend) throws IOException{
		t.sendResponseHeaders(200, toSend.length());
		final OutputStream os = t.getResponseBody();
		os.write(toSend.getBytes());
		os.close();
	}

	public static interface HTMLForm{
		public void sendHTMLForm(final HttpExchange t) throws IOException;
	}

	public static class StandardHTMLForm implements HTMLForm {

		private static String HTML_FORM_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
											"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
											"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n"+
											"<head>\n <title>LUPOSDATE SPARQL Endpoint</title>\n</head>\n"+
											"<body>\n <h1>LUPOSDATE SPARQL Endpoint</h1>\n\n"+
											" <form method=\"get\" action=\"sparql\">\n  <p>Type in your SPARQL query:<br/>\n   "+
											"<textarea name=\"query\" cols=\"50\" rows=\"10\">SELECT * WHERE { ?s ?p ?o. } LIMIT 10</textarea>\n  </p>\n" +
											"  <p>\n   Result Format:<br/>\n   <select name=\"format\" size=\"1\">\n   ";
		private static String HTML_FORM_2 = "</select>\n  </p>\n  <p>\n   <input type=\"submit\" value=\" Submit Query \"/>\n  </p>\n "+
											"</form>\n</body>\n</html>";

		private static String HTML_OPTION_1 = " <option value=\"";
		private static String HTML_OPTION_2 = "\">";
		private static String HTML_OPTION_3 = "</option>\n   ";


		@Override
		public void sendHTMLForm(final HttpExchange t) throws IOException {
			final StringBuilder toSend = new StringBuilder(StandardHTMLForm.HTML_FORM_1);
			// do duplicate elimination!
			final Formatter[] formatters=Sets.newHashSet(Endpoint.getRegisteredFormatters().values()).toArray(new Formatter[0]);
			// sort according to the name of the formatter
			Arrays.sort(formatters, new Comparator<Formatter>(){
				@Override
				public int compare(final Formatter o1, final Formatter o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			for(final Formatter formatter: formatters){
				toSend.append(StandardHTMLForm.HTML_OPTION_1);
				toSend.append(formatter.getKey());
				toSend.append(StandardHTMLForm.HTML_OPTION_2);
				toSend.append(formatter.getName());
				toSend.append(StandardHTMLForm.HTML_OPTION_3);
			}
			toSend.append(StandardHTMLForm.HTML_FORM_2);
			Endpoint.sendString(t, toSend.toString());
		}
	}

	public static class OutputStreamLogger extends OutputStream {

		private final OutputStream piped;

		public OutputStreamLogger(final OutputStream piped){
			this.piped = piped;
		}

		@Override
		public void write(final int b) throws IOException {
			if(b>=0){
				for(final char c: Character.toChars(b)){
					System.out.print(c);
				}
			}
			this.piped.write(b);
		}

		@Override
		public void close() throws IOException{
			this.piped.close();
		}
	}

	public static class OutputStreamSizeLogger extends OutputStream {

		private final OutputStream piped;
		private int size = 0;

		public OutputStreamSizeLogger(final OutputStream piped){
			this.piped = piped;
		}

		@Override
		public void write(final int b) throws IOException {
			if(b>=0){
				this.size++;
			}
			this.piped.write(b);
		}

		@Override
		public void close() throws IOException{
			System.out.println("Size of response in bytes: "+this.size);
			this.piped.close();
		}
	}
}