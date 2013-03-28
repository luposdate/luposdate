/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.distributedendpoints.storage;

import java.io.FileNotFoundException;
import java.io.IOException;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.storage.BlockUpdatesStorage;
import lupos.endpoint.client.Client;
import lupos.engine.operators.multiinput.join.parallel.ResultCollector;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.FileHelper;

/**
 * This class contains the storage layer for our distributed SPARQL endpoint query evaluator.
 * This class handles the communication with the SPARQL endpoints during data manipulation and distributed querying.
 */
public class Storage_DE extends BlockUpdatesStorage {
	
	/**
	 * contains the registered SPARQL endpoints...
	 */
	protected String[] urlsOfEndpoints;
	
	/**
	 * Reads in the registered SPARQL endpoints from the configuration file /endpoints.txt.
	 * Each line of this file must contain the URL of a SPARQL endpoint.
	 */
	public Storage_DE(){
		try {
			this.urlsOfEndpoints = FileHelper.readInputStreamToCollection(FileHelper.getInputStreamFromJarOrFile("/endpoints.config")).toArray(new String[0]);
		} catch (FileNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
			this.urlsOfEndpoints = new String[0];
		}
	}

	@Override
	public void blockInsert(){
		// insert all triples of toBeAdded by using one INSERT DATA query!
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT DATA { ");
		
		for(Triple triple: this.toBeAdded) {
			sb.append(Storage_DE.toN3StringReplacingBlankNodesWithIRIs(triple));
			sb.append(" ");
		}
		sb.append(" }");
		this.submitSPARULQueryToArbitraryEndpoint(sb.toString());
	}


	@Override
	public boolean containsTripleAfterAdding(Triple triple) {
		return !this.submitSPARQLQuery("SELECT * WHERE { " + Storage_DE.toN3StringReplacingBlankNodesWithIRIs(triple) + " }").isEmpty();
	}

	@Override
	public void removeAfterAdding(Triple triple) {
		// Triples containing blank nodes cannot be deleted
		this.submitSPARULQuery("DELETE DATA { " + Storage_DE.toN3StringReplacingBlankNodesWithIRIs(triple) + " }");
	}

	@Override
	public QueryResult evaluateTriplePatternAfterAdding(TriplePattern triplePattern) {
		return this.submitSPARQLQuery("SELECT * WHERE { " + triplePattern.toN3String() + " }");
	}
	
	/**
	 * submits a SPARUL query to all registered SPARQL endpoints
	 * @param query
	 */
	public void submitSPARULQuery(final String query){
		for(final String url: this.urlsOfEndpoints){
			Thread thread = new Thread(){
				@Override
				public void run() {
					try {
						Storage_DE.submitSPARQLQuery(url, query);
					} catch (IOException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
			};
			thread.start();
		}
	}
	
	/**
	 * submits asynchronously a SPARUL query to an arbitrary of the registered SPARQL endpoints
	 * @param query
	 */
	public void submitSPARULQueryToArbitraryEndpoint(final String query){
		// choose randomly one endpoint to which the sparul request is submitted to
		final String url = this.urlsOfEndpoints[(int)(Math.random()*this.urlsOfEndpoints.length)];
		Thread thread = new Thread(){
			@Override
			public void run() {
				try {
					Storage_DE.submitSPARQLQuery(url, query);
				} catch (IOException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}
	
	/**
	 * submits a query parallel to all registered endpoints
	 * @param query the query to be submitted
	 * @return the query result containing the result of all endpoints (collected in an asynchronous way)
	 */
	public QueryResult submitSPARQLQuery(final String query){
		final ResultCollector resultCollector = new ResultCollector();
		resultCollector.setNumberOfThreads(this.urlsOfEndpoints.length);
		for(final String url: this.urlsOfEndpoints){
			Thread thread = new Thread(){
				@Override
				public void run() {
					try {
						resultCollector.process(Storage_DE.submitSPARQLQuery(url, query) ,0);
					} catch (IOException e) {
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
	
	private final static QueryResult submitSPARQLQuery(final String url, final String query) throws IOException {
		return Client.submitQuery(url, query);
	}
	
	/**
	 * Blank nodes cannot occur in SPARUL insertions and deletions.
	 * This method therefore replaces blank nodes in triples...
	 * @param triple the triple
	 * @return A N3 string representation of the triple, where blank nodes have been replaced with iris
	 */
	public static String toN3StringReplacingBlankNodesWithIRIs(Triple triple){
		StringBuilder sb = new StringBuilder();
		
		for(Literal literal: triple){
			sb.append(Storage_DE.toN3StringReplacingBlankNodesWithIRIs(literal));
			sb.append(" ");
		}
		sb.append(".");
		
		return sb.toString();
	}
	
	/**
	 * Blank nodes cannot occur in SPARUL insertions and deletions.
	 * This method therefore returns an iri for a given blank node or just the string representation otherwise
	 * @param literal the literal
	 * @return string representation of the literal (for blank nodes an iri is returned)
	 */
	public static String toN3StringReplacingBlankNodesWithIRIs(final Literal literal){
		if(literal instanceof AnonymousLiteral){
			return "<http://www.ifis.uni-luebeck.de/blank_node/" + ((AnonymousLiteral)literal).getBlankNodeLabel() + ">";
		} else {
			return literal.toString();
		}
	}
}
