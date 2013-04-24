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
package lupos.distributedendpoints.storage.util;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;
import lupos.endpoint.client.Client;
import lupos.engine.operators.multiinput.join.parallel.ResultCollector;
import lupos.misc.FileHelper;
import lupos.misc.Tuple;

public class EndpointManagement {

	/**
	 * contains the registered SPARQL endpoints...
	 */
	protected String[] urlsOfEndpoints;

	protected ExecutorService threadpool = Executors.newCachedThreadPool();

	/**
	 * Reads in the registered SPARQL endpoints from the configuration file /endpoints.txt.
	 * Each line of this file must contain the URL of a SPARQL endpoint.
	 */
	public EndpointManagement(){
		try {
			this.urlsOfEndpoints = FileHelper.readInputStreamToCollection(FileHelper.getInputStreamFromJarOrFile("/endpoints.config")).toArray(new String[0]);
			for(int i=0; i<this.urlsOfEndpoints.length; i++) {
				if(!this.urlsOfEndpoints[i].endsWith("/")) {
					// this is necessary when using distribution strategies as different contexts must be addressed for different key types
					this.urlsOfEndpoints[i] += "/";
				}
			}
		} catch (final FileNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
			this.urlsOfEndpoints = new String[0];
		}
	}

	/**
	 * Returns the number of registered endpoints
	 * @return the number of registered endpoints
	 */
	public int numberOfEndpoints(){
		return this.urlsOfEndpoints.length;
	}

	/**
	 * submits a SPARUL query to all registered SPARQL endpoints
	 * @param query the query to be submitted
	 */
	public void submitSPARULQuery(final String query){
		for(int i=0; i<this.urlsOfEndpoints.length; i++){
			this.submitSPARULQuery(query, i);
		}
	}

	/**
	 * submits asynchronously a SPARUL query to an arbitrary of the registered SPARQL endpoints
	 * @param query the query to be submitted
	 */
	public void submitSPARULQueryToArbitraryEndpoint(final String query){
		// choose randomly one endpoint to which the sparul request is submitted to
		this.submitSPARULQuery(query, (int)(Math.random()*this.urlsOfEndpoints.length));
	}

	/**
	 * submits asynchronously a SPARUL query to a specific registered SPARQL endpoint
	 * @param query the query to be submitted
	 * @param number the number of the endpoint to which the query is sent to
	 */
	public void submitSPARULQuery(final String query, final int number){
		this.submitSPARULQuery(query, this.urlsOfEndpoints[number]);
	}

	/**
	 * submits asynchronously a SPARUL query to a specific registered SPARQL endpoint
	 * @param query the query to be submitted
	 * @param key the key container containing the number of the endpoint to which the query is sent to
	 */
	public void submitSPARULQuery(final String query, final KeyContainer<Integer> key){
		this.submitSPARULQuery(query, EndpointManagement.addContext(this.urlsOfEndpoints[key.key], key));
	}


	/**
	 * submits asynchronously a SPARUL query to a specific SPARQL endpoint
	 * @param query the query to be submitted
	 * @param url the url of the endpoint to which the query is sent to
	 */
	protected void submitSPARULQuery(final String query, final String url){
		final Thread thread = new Thread(){
			@Override
			public void run() {
				try {
					EndpointManagement.submitSPARQLQuery(url, query);
				} catch (final IOException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		};
		this.threadpool.submit(thread);
	}

	/**
	 * Waits for the thread pool to terminate.
	 * This method is to ensure that all the data is inserted at the different endpoints before being queried...
	 */
	public void waitForThreadPool() {
		this.threadpool.shutdown();
		try {
			// just use an extremely high timeout...
			this.threadpool.awaitTermination(7, TimeUnit.DAYS);
			this.threadpool = Executors.newCachedThreadPool();
		} catch (final InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * submits a SPARQL query to a specific registered SPARQL endpoint
	 * @param query the given query to be submitted
	 * @param number the number of the endpoint to which the query is sent to
	 * @return the query result of the submitted query
	 */
	public QueryResult submitSPARQLQuery(final String query, final int number){
		final String url = this.urlsOfEndpoints[number];
		try {
			return EndpointManagement.submitSPARQLQuery(url, query);
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * submits a SPARQL query to a specific registered SPARQL endpoint
	 * @param query the given query to be submitted
	 * @param key the key container containing the the number of the endpoint to which the query is sent to
	 * @return the query result of the submitted query
	 */
	public QueryResult submitSPARQLQuery(final String query, final KeyContainer<Integer> key){
		final String url = EndpointManagement.addContext(this.urlsOfEndpoints[key.key], key);
		try {
			return EndpointManagement.submitSPARQLQuery(url, query);
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * submits a subgraph to a specific registered SPARQL endpoint
	 * @param subgraph the given subgraph to be submitted
	 * @param key the key container containing the the number of the endpoint to which the query is sent to
	 * @return the query result of the submitted subgraph
	 */
	public QueryResult submitSubgraphQuery(final String subgraph, final KeyContainer<Integer> key){
		final String url = EndpointManagement.addContext(EndpointManagement.addContext(this.urlsOfEndpoints[key.key], "subgraph/"), key);
		try {
			return EndpointManagement.submitSPARQLQuery(url, subgraph);
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * submits a histogram request to a specific registered SPARQL endpoint
	 * @param histogram the given subgraph to be submitted
	 * @param key the key container containing the the number of the endpoint to which the query is sent to
	 * @return the query result of the submitted subgraph
	 */
	public String submitHistogramRequest(final String histogram, final KeyContainer<Integer> key){
		final String url = EndpointManagement.addContext(EndpointManagement.addContext(this.urlsOfEndpoints[key.key], "histogram/"), key);
		return EndpointManagement.submitHistogramRequest(histogram, url);
	}

	/**
	 * Submits a histogram request to a given url
	 *
	 * @param histogram the histogram request
	 * @param url the url to which the request is sent to
	 * @return the response of the histogram request
	 */
	public static String submitHistogramRequest(final String histogram, final String url) {
		try {
			final Tuple<String, InputStream> response = Client.submitQueryAndRetrieveStream(url, histogram, "application/sparql-results+json"); //mime type would be better application/json, but for that we do not have registered formatter, such that errors would occur
			return EndpointManagement.getStringFromInputStream(response.getSecond());
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Reads an UTF-8 encoded string from an input stream and returns it
	 *
	 * @param in the input stream from which the string is read
	 * @return the string
	 * @throws IOException
	 */
	public static String getStringFromInputStream(final InputStream in) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		int next;
		while((next=in.read())>=0){
			out.write(next);
		}
		in.close();
		out.close();

		return new String(out.toByteArray(), "UTF-8");
	}


	/**
	 * submits a query parallel to all registered endpoints
	 * @param query the query to be submitted
	 * @return the query result containing the result of all endpoints (collected in an asynchronous way)
	 */
	public QueryResult submitSPARQLQuery(final String query){
		return EndpointManagement.submitSPARQLQuery(query, this.urlsOfEndpoints);
	}

	/**
	 * submits a query parallel to all given endpoints
	 * @param query the query to be submitted
	 * @param urlsOfEndpoints the urls of the endpoints to which the query will be submitted
	 * @return the query result containing the result of all given endpoints (collected in an asynchronous way)
	 */
	protected static QueryResult submitSPARQLQuery(final String query, final String[] urlsOfEndpoints){
		final ResultCollector resultCollector = new ResultCollector();
		resultCollector.setNumberOfThreads(urlsOfEndpoints.length);
		for(final String url: urlsOfEndpoints){
			final Thread thread = new Thread(){
				@Override
				public void run() {
					try {
						resultCollector.process(EndpointManagement.submitSPARQLQuery(url, query) ,0);
					} catch (final IOException e) {
						System.err.println(e);
						e.printStackTrace();
					}
					resultCollector.incNumberOfThreads();
				}
			};
			thread.start();
		}
		return resultCollector.getResult();
	}

	/**
	 * Submits a histogram request to all registered endpoints
	 *
	 * @param histogram the histogram request to be submitted to all endpoints
	 * @return the histograms of all endpoints
	 */
	public String[] submitHistogramRequest(final String histogram) {
		final String[] urlsWithContext = new String[this.urlsOfEndpoints.length];
		for(int i=0; i<this.urlsOfEndpoints.length; i++){
			urlsWithContext[i] = EndpointManagement.addContext(this.urlsOfEndpoints[i], "histogram/");
		}
		return EndpointManagement.submitHistogramRequest(histogram, urlsWithContext);
	}

	/**
	 * submits histogram requests in parallel to all given endpoints
	 *
	 * @param histogram the histogram request to be submitted
	 * @param urlsOfEndpoints the endpoints to be queries
	 * @return the result of all the endpoints
	 */
	protected static String[] submitHistogramRequest(final String histogram, final String[] urlsOfEndpoints){
		final Thread[] threads = new Thread[urlsOfEndpoints.length];
		final String[] result = new String[urlsOfEndpoints.length];
		// ask nodes in parallel for their histograms
		for(int i=0; i<urlsOfEndpoints.length; i++){
			final int index = i;

			threads[index] = new Thread(){
				@Override
				public void run(){
					result[index] = EndpointManagement.submitHistogramRequest(histogram, urlsOfEndpoints[index]);
				}
			};

			threads[index].start();
		}
		for(final Thread thread: threads){
			try {
				thread.join();
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * submits histogram requests in parallel to all given endpoints according to a given key type
	 *
	 * @param histogram the histogram request
	 * @param keyType the key type
	 * @return the result of the addressed endpoints
	 */
	public String[] submitHistogramRequestWithKeyType(final String histogram, final String keyType) {
		final String[] urls = new String[this.urlsOfEndpoints.length];
		for(int i=0; i<this.urlsOfEndpoints.length; i++){
			urls[i] = EndpointManagement.addContext(EndpointManagement.addContext(this.urlsOfEndpoints[i], "histogram/"), keyType);
		}
		return EndpointManagement.submitHistogramRequest(histogram, urls);
	}


	/**
	 * submits a query parallel to all registered endpoints (according to one key type to avoid duplicates)
	 * @param query the query to be submitted
	 * @param keyType the key type to be used
	 * @return the query result containing the result of all endpoints (collected in an asynchronous way)
	 */
	public QueryResult submitSPARQLQueryWithKeyType(final String query, final String keyType){
		final String[] urls = new String[this.urlsOfEndpoints.length];
		for(int i=0; i<this.urlsOfEndpoints.length; i++){
			urls[i] = EndpointManagement.addContext(this.urlsOfEndpoints[i], keyType);
		}
		return EndpointManagement.submitSPARQLQuery(query, urls);
	}

	/**
	 * submits a query to a SPARQL endpoint with a given url
	 * @param url the url of the SPARQL endpoint
	 * @param query the query to be submitted
	 * @return the retrieved query result
	 * @throws IOException in case of any errors
	 */
	private final static QueryResult submitSPARQLQuery(final String url, final String query) throws IOException {
		return Client.submitQuery(url, query);
	}

	/**
	 * Adds the context according to the key type to a given url
	 * @param url the url of the SPARQL endpoint
	 * @param key the key container with the key type
	 * @return url/key type
	 */
	private static String addContext(final String url, final KeyContainer<Integer> key){
		return url + key.type;
	}

	/**
	 * Adds the context according to the key type to a given url
	 * @param url the url of the SPARQL endpoint
	 * @param keyType the key type
	 * @return url/key type
	 */
	private static String addContext(final String url, final String keyType){
		return url + keyType;
	}
}
