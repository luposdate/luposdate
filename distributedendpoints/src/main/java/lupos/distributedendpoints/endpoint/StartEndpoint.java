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
package lupos.distributedendpoints.endpoint;

import lupos.distributed.storage.distributionstrategy.tripleproperties.OneKeyDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.OneToThreeKeysDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.TwoKeysDistribution;
import lupos.endpoint.server.Endpoint;
import lupos.endpoint.server.Endpoint.SPARQLExecutionImplementation;
import lupos.endpoint.server.Endpoint.SPARQLHandler;

/**
 * This class is for starting the endpoints with contexts according to the used distribution strategy
 */
public class StartEndpoint {

	/**
	 * Main entry point to start the endpoints...
	 * @param args see init() for the command line arguments
	 */
	public static void main(final String[] args) {
		final String[] keyTypes = StartEndpoint.init(args);
		String base_dir = args[1];
		if(!base_dir.endsWith("/") && !base_dir.endsWith("\\")){
			base_dir += "/";
		}
		for(final String keyType: keyTypes) {
			// start for each type of the keys a different context
			final String directory = base_dir + keyType;
			Endpoint.registerHandler("/sparql/" + keyType, new SPARQLHandler(new SPARQLExecutionImplementation(Endpoint.createQueryEvaluator(directory), directory)));
		}
		Endpoint.registerStandardFormatter();
		Endpoint.initAndStartServer();
	}

	/**
	 * Initializes the endpoint and returns the list of possible keys for the specified distribution strategy
	 * @param args command line arguments
	 * @return the list of possible keys for the specified distribution strategy
	 */
	public static String[] init(final String[] args){
		String[] result = null;
		if (args.length >= 2) {
			if(args[0].compareTo("0")==0) {
				result = new String[]{ "" };
			} else if(args[0].compareTo("1")==0) {
				result = OneKeyDistribution.getPossibleKeyTypes();
			} else if(args[0].compareTo("2")==0) {
				result = TwoKeysDistribution.getPossibleKeyTypes();
			} else if(args[0].compareTo("3")==0) {
				result = OneToThreeKeysDistribution.getPossibleKeyTypes();
			}
		}
		if (args.length < 2 || result==null) {
			System.err.println("Usage:\njava -Xmx768M lupos.distributedendpoints.endpoint.StartEndpoint (0|1|2|3) <directory for indices> [output] [size]");
			System.err.println("0 for no distribution strategy");
			System.err.println("1 for one key distribution strategy");
			System.err.println("2 for two keys distribution strategy");
			System.err.println("3 for one to three keys distribution strategy");
			System.err.println("If \"output\" is given, the response is written to console.");
			System.err.println("If \"size\" is given, the size of the received query and the size of the response is written to console.");
			System.exit(0);
		}
		for(int i=2; i<args.length; i++){
			if(args[i].compareTo("output")==0){
				Endpoint.log = true;
			} else if(args[i].compareTo("size")==0){
				Endpoint.sizelog = true;
			}
		}
		return result;
	}
}
