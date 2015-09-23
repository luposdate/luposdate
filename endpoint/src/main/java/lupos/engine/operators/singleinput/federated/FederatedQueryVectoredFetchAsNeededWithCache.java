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
package lupos.engine.operators.singleinput.federated;

import java.io.IOException;
import java.util.HashMap;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.sparql1_1.Node;

public class FederatedQueryVectoredFetchAsNeededWithCache extends FederatedQueryVectoredFetchAsNeeded {

	protected HashMap<Integer, Integer> cachedResults = new HashMap<Integer, Integer>();
	protected HashMap<Bindings, Integer> previousBindings = new HashMap<Bindings, Integer>();
	/**
	 * <p>Constructor for FederatedQueryFetchAsNeeded.</p>
	 *
	 * @param federatedQuery a {@link lupos.sparql1_1.Node} object.
	 */
	public FederatedQueryVectoredFetchAsNeededWithCache(final Node federatedQuery) {
		super(federatedQuery);
	}

	@Override
	public QueryResult getQueryResultFromEndpoint(final URILiteral endpointURI, final QueryResult queryresult) throws IOException{
		final QueryResult result = super.getQueryResultFromEndpoint(endpointURI, queryresult);
		this.cachedResults.clear();
		return result;
	}

	@Override
	public QueryResult getQueryResultToJoin(final HashMap<Integer, QueryResult> mapBindingsToIndex, final int index){
		QueryResult result = mapBindingsToIndex.get(index);
		if(result==null){
			final Integer alternativeIndex = this.cachedResults.get(index);
			if(alternativeIndex != null){
				result = mapBindingsToIndex.get(alternativeIndex);
			}
		}
		return result;
	}

	@Override
	public String toStringQuery(final QueryResult queryresult) {
		final String result = super.toStringQuery(queryresult);
		this.previousBindings.clear();
		return result;
	}


	@Override
	public boolean bindingsToBeConsidered(final Bindings bindings, final int index){
		final Bindings keyBindings = this.bindingsFactory.createInstance();
		for(final Variable v: this.variablesInServiceCall){
			keyBindings.add(v, bindings.get(v));
		}
		final Integer previousIndex = this.previousBindings.get(keyBindings);
		if(previousIndex == null){
			this.previousBindings.put(keyBindings, index);
			return true;
		} else {
			this.cachedResults.put(index, previousIndex);
			return false;
		}
	}
}
