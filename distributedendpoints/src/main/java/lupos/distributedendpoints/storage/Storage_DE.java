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
package lupos.distributedendpoints.storage;

import lupos.datastructures.items.Triple;
import lupos.datastructures.queryresult.QueryResult;
import lupos.distributed.query.operator.histogramsubmission.AbstractHistogramExecutor;
import lupos.distributed.storage.nodistributionstrategy.BlockUpdatesStorage;
import lupos.distributedendpoints.storage.util.EndpointManagement;
import lupos.distributedendpoints.storage.util.QueryBuilder;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class contains the storage layer for our distributed SPARQL endpoint query evaluator.
 * This class handles the communication with the SPARQL endpoints during data manipulation and distributed querying.
 *
 * All registered endpoints are asked for the evaluation of the triple patterns within a SPARQL query.
 * It is assumed that the data is not distributed in an intelligent way and that any registered endpoint
 * can have data for any triple pattern.
 * Also non-luposdate SPARQL endpoints are supported.
 */
public class Storage_DE extends BlockUpdatesStorage {

	/**
	 *  for managing the registered endpoints and submitting queries to them
	 */
	protected final EndpointManagement endpointManagement;

	/**
	 * this flag is true if data has been inserted, otherwise it is false
	 */
	protected boolean insertedData = false;

	/**
	 * Constructor: The endpoint management is initialized (which reads in the configuration file with registered endpoints)
	 */
	public Storage_DE(){
		this.endpointManagement = new EndpointManagement();
	}

	@Override
	public void blockInsert(){
		this.endpointManagement.submitSPARULQueryToArbitraryEndpoint(QueryBuilder.buildInsertQuery(this.toBeAdded));
		this.insertedData = true;
	}

	@Override
	public boolean containsTripleAfterAdding(final Triple triple) {
		return !this.endpointManagement.submitSPARQLQuery(QueryBuilder.buildQuery(triple)).isEmpty();
	}

	@Override
	public void removeAfterAdding(final Triple triple) {
		this.endpointManagement.submitSPARULQuery(QueryBuilder.buildDeleteQuery(triple));
		this.endpointManagement.waitForThreadPool();
	}

	@Override
	public QueryResult evaluateTriplePatternAfterAdding(final TriplePattern triplePattern) {
		return this.endpointManagement.submitSPARQLQuery(QueryBuilder.buildQuery(triplePattern));
	}

	@Override
	public void endImportData() {
		if(!this.toBeAdded.isEmpty()){
			super.endImportData();
		}
		this.endpointManagement.waitForThreadPool();
		if(this.insertedData){
			// send request for rebuilding the statistics!
			this.endpointManagement.submitHistogramRequest(AbstractHistogramExecutor.createRebuildStatisticsRequestString());
			this.insertedData = false;
		}
	}

	/**
	 * @return the endpoint management object for submitting to the registered endpoints
	 */
	public EndpointManagement getEndpointManagement(){
		return this.endpointManagement;
	}
}
