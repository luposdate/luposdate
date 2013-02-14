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
package lupos.endpoint.client;

import lupos.engine.evaluators.MemoryIndexQueryEvaluator;
import lupos.engine.evaluators.RDF3XQueryEvaluator;
import lupos.engine.evaluators.StreamQueryEvaluator;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoin;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoinNonStandardSPARQL;
import lupos.sparql1_1.operatorgraph.ServiceApproaches;

public class CommandLineEvaluator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Usage:\njava upos.endpoint.client.CommandLineEvaluator (ServiceApproaches:No_Support|Trivial_Approach|Fetch_As_Needed|Fetch_As_Needed_With_Cache|Semijoin_Approach|Bitvector_Join_Approach|Join_At_Endpoint) (MD5|SHA1|SHA256|SHA384|SHA512|Value|NonStandardSPARQL) (Size of Bitvector) (MEMORY|RDF3X|STREAM) (command line arguments of underlying evaluator...)");
		if(args.length<4){
			return;
		}
		ServiceApproaches serviceApproach = ServiceApproaches.valueOf(args[0]);
		FederatedQueryBitVectorJoin.APPROACH bitVectorApproach = FederatedQueryBitVectorJoin.APPROACH.valueOf(args[1]);
		bitVectorApproach.setup();
		serviceApproach.setup();
		int bitvectorsize = Integer.parseInt(args[2]);
		FederatedQueryBitVectorJoin.substringSize = bitvectorsize;
		FederatedQueryBitVectorJoinNonStandardSPARQL.bitvectorSize = bitvectorsize;

		String[] args2 = new String[args.length-4];
		System.arraycopy(args, 4, args2, 0, args.length-4);

		String evaluator = args[3].toLowerCase();
		if(evaluator.compareTo("memory")==0){
			MemoryIndexQueryEvaluator.main(args2);
		} else if(evaluator.compareTo("rdf3x")==0){
			RDF3XQueryEvaluator.main(args2);
		} else if(evaluator.compareTo("stream")==0){
			StreamQueryEvaluator.main(args2);
		} else System.err.println("No support of evaluator: "+evaluator); 
	}

}
