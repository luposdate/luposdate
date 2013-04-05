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
import lupos.engine.indexconstruction.RDF3XEmptyIndexConstruction;

/**
 * This class creates the indices for the different distribution strategies
 */
public class ClearIndex {

	/**
	 * main entry point of the program
	 * @param args see method help()
	 */
	public static void main(final String[] args) {
		if(args.length<2){
			ClearIndex.help();
		}
		String dir = args[1];
		if(!dir.endsWith("/") && !dir.endsWith("\\")){
			dir += "/";
		}

		if(args[0].compareTo("0") == 0) {
			// no distribution strategy => just create one index
			ClearIndex.createEmptyIndex(dir, "");
		} else {
			// distribution strategies are used => create so many indices as there are key types, all in different subfolders
			String[] keyTypes = null;
			if(args[0].compareTo("1") == 0) {
				keyTypes = OneKeyDistribution.getPossibleKeyTypes();
			} else if(args[0].compareTo("2") == 0){
				keyTypes = TwoKeysDistribution.getPossibleKeyTypes();
			} else if(args[0].compareTo("3") == 0){
				keyTypes = OneToThreeKeysDistribution.getPossibleKeyTypes();
			} else {
				ClearIndex.help();
			}
			for(final String keyType: keyTypes){
				ClearIndex.createEmptyIndex(dir, keyType);
			}
		}
	}

	/**
	 * creates an empty index under the folder base_dir/keyType
	 * @param base_dir
	 * @param keyType
	 */
	public static void createEmptyIndex(final String base_dir, final String keyType){
		final String[] dir_array = new String[] { base_dir + keyType };
		RDF3XEmptyIndexConstruction.main(dir_array);
	}

	/**
	 * prints out the help text and terminates the program
	 */
	public static void help(){
		System.out.println("java lupos.distributedendpoints.endpoint.ClearIndex (0|1|2|3) <directory>");
		System.out.println("0 for no distribution strategy");
		System.out.println("1 for one key distribution strategy");
		System.out.println("2 for two keys distribution strategy");
		System.out.println("3 for one to three keys distribution strategy");
		System.out.println("<directory> the base directory in which all the indices are created");
		// Stop processing
		System.exit(0);
	}
}
