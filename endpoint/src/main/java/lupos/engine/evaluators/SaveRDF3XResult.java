
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
package lupos.engine.evaluators;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.server.format.Formatter;
import lupos.endpoint.server.format.XMLFormatter;
import lupos.misc.FileHelper;
import lupos.sparql1_1.operatorgraph.ServiceApproaches;
public class SaveRDF3XResult {
	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(final String[] args) throws Exception{
		System.out.println("lupos.engine.evaluators.SaveRDF3XResult processes a SPARQL query and saves its result");
		System.out.println("Usage: java lupos.engine.evaluators.SaveRDF3XResult <Directory of indices> <Query-File> <Output-File> [<Query-File 2> <Output-File 2> ...]");
		if(args.length<3 || args.length%2 == 0){
			System.err.println("Wrong number of arguments!");
		}
		final RDF3XQueryEvaluator evaluator = new RDF3XQueryEvaluator();
		evaluator.loadLargeScaleIndices(args[0]);
		final Formatter formatter = new XMLFormatter();
		ServiceApproaches.Semijoin_Approach.setup();
		for(int i=1; i<args.length; i+=2){
			final String query = FileHelper.readFile(args[i]);
			System.out.println("\nProcess SPARQL query:");
			System.out.println(query);
			final QueryResult queryResult = evaluator.getResult(query, true);
			System.out.println("\nSave result into file: "+args[i+1]);
			final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(args[i+1]));
			formatter.writeResult(outputStream, evaluator.getVariablesOfQuery(), queryResult);
			outputStream.close();
			evaluator.writeOutIndexFileAndModifiedPages(args[0]);
			queryResult.release();
		}
		System.out.println("\nDone!");
	}
}
