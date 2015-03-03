
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.distributed.query.operator.histogramsubmission;

import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.TriplePatternNotSupportedError;
import lupos.engine.operators.tripleoperator.TriplePattern;
public abstract class AbstractDistributionHistogramExecutor<K> extends AbstractHistogramExecutor {

	protected final IDistribution<K> distribution;

	/**
	 * <p>Constructor for AbstractDistributionHistogramExecutor.</p>
	 *
	 * @param distribution a {@link lupos.distributed.storage.distributionstrategy.IDistribution} object.
	 */
	public AbstractDistributionHistogramExecutor(final IDistribution<K> distribution){
		this.distribution = distribution;
	}

	/** {@inheritDoc} */
	@Override
	public String[] sendJSONRequests(final String request, final TriplePattern triplePattern) {
		try {
			final K[] keys = this.distribution.getKeysForQuerying(triplePattern);
			final String[] result = new String[keys.length];
			final Thread[] threads = new Thread[keys.length];
			int i=0;
			for(final K key: keys){
				final int index = i;
				// ask all nodes asynchronously
				threads[index] = new Thread(){
					@Override
					public void run(){
						result[index] = AbstractDistributionHistogramExecutor.this.sendJSONRequest(request, key);
					}
				};
				threads[index].start();
				i++;
			}
			// wait for all nodes
			for(final Thread thread: threads){
				try {
					thread.join();
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
			return result;
		} catch(final TriplePatternNotSupportedError e){
			// in case that the triple pattern type is not supported  try out if it has been implemented to broadcast the request
			final String[] result = this.sendJSONRequest(request);
			if(result!=null){
				return result;
			} else {
				throw e;
			}
		}
	}

	/**
	 * Must be overridden to implement that the request (for histogram or min/max computations) is transmitted over network to the node with a certain key
	 *
	 * @param request the request for histograms or min/max computations serialized as json string
	 * @param key the address of the node to which the request is sent to.
	 * @return the result string (serialized as json string) from the node addressed by key
	 */
	public abstract String sendJSONRequest(String request, K key);

	/**
	 * This method is called whenever the type of triple pattern is not supported (e.g. triple pattern with only variables).
	 * Can be overridden to broadcast the request to all nodes.
	 * The default is returning null, which leads to throwing the TriplePatternNotSupportedError.
	 *
	 * @param request the request to be broadcasted
	 * @return the response of all the nodes...
	 */
	public String[] sendJSONRequest(final String request){
		return null;
	}
}
