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
package lupos.endpoint.server.format;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;

/**
 * This class is for returning the query-triples of a query 
 */
public class QueryTriplesFormatter extends Formatter {

	public QueryTriplesFormatter() {
		super("Query-Triples", "text/n3");
	}
	
	@Override
	public String getMIMEType(QueryResult queryResult){
		return "text/n3";
	}

	
	@Override
	public void writeResult(final OutputStream os, Set<Variable> variables, final QueryResult queryResult) throws IOException {
		byte[] carriageReturn = "\n".getBytes();
		
		// use HashSet for eliminating duplicates!
		HashSet<Triple> triples = new HashSet<Triple>(); 
		
		// collect all query-triples!
		Iterator<Bindings> it = queryResult.oneTimeIterator();
		while(it.hasNext()){
			Bindings bindings = it.next();
			for(Triple triple: bindings.getTriples()){
				triples.add(triple);
			}
		}
		// now write out all triples without any duplicates
		for(Triple triple: triples){
			os.write(triple.toN3String().getBytes());
			os.write(carriageReturn);
		}
	}

	@Override
	public boolean isWriteQueryTriples() {
		return true;
	}
}
