/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
import java.util.Set;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;

public abstract class Formatter {
	private final String formatName;
	private final String key;
	
	public Formatter(final String formatName){
		this(formatName, formatName);
	}
	
	public Formatter(final String formatName, final String key){
		this.formatName = formatName;
		this.key = key.toLowerCase();
	}
	
	public String getMIMEType(QueryResult queryResult){
		if(queryResult instanceof GraphResult){
			return "text/n3";
		} else {
			System.err.println("lupos.endpoint.server.format.Formatter: QueryResult other than GraphResult should be handled by the class "+this.getClass().getCanonicalName());
			return null;
		}
	}
	
	public String getName(){
		return this.formatName;
	}
	
	public String getKey(){
		return this.key;
	}
	
	@SuppressWarnings("unused")
	public void writeResult(final OutputStream os, Set<Variable> variables, final QueryResult queryResult) throws IOException {
		if(queryResult instanceof GraphResult){
			byte[] carriageReturn = "\n".getBytes();
			for(Triple t: ((GraphResult)queryResult).getGraphResultTriples()){
				os.write(t.toN3String().getBytes());
				os.write(carriageReturn);
			}
		} else {
			System.err.println("lupos.endpoint.server.format.Formatter: QueryResult other than GraphResult should be handled by the class "+this.getClass().getCanonicalName());
		}
	}
}