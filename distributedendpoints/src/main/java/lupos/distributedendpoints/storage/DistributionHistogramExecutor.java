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

import lupos.distributed.query.operator.histogramsubmission.AbstractDistributionHistogramExecutor;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.IDistributionKeyContainer;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;
import lupos.distributedendpoints.storage.util.EndpointManagement;

public class DistributionHistogramExecutor extends AbstractDistributionHistogramExecutor<KeyContainer<Integer>> {

	protected final EndpointManagement endpointManagement;

	public DistributionHistogramExecutor(final IDistribution<KeyContainer<Integer>> distribution, final EndpointManagement endpointManagement) {
		super(distribution);
		this.endpointManagement = endpointManagement;
	}

	@Override
	public String sendJSONRequest(final String request, final KeyContainer<Integer> key) {
		return this.endpointManagement.submitHistogramRequest(request, key);
	}

	@Override
	public String[] sendJSONRequest(final String request){
		if(this.distribution instanceof IDistributionKeyContainer){
			final String[] keyTypes = ((IDistributionKeyContainer<Integer>)this.distribution).getKeyTypes();
			final String[][] result = new String[keyTypes.length][];
			for(int i=0; i<keyTypes.length; i++){
				result[i]=this.endpointManagement.submitHistogramRequestWithKeyType(request, keyTypes[i]);
			}
			// flatten it...
			int sum=0;
			for(final String[] array: result){
				sum+=array.length;
			}
			final String[] returnResult = new String[sum];
			int index =0;
			for(final String[] array: result){
				for(final String element: array){
					returnResult[index]= element;
					index++;
				}
			}
			return returnResult;
		} else {
			return super.sendJSONRequest(request);
		}
	}
}
