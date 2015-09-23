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
package lupos.endpoint.server.format;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
public abstract class Formatter {
	private final String formatName;
	private final String key;

	/**
	 * <p>Constructor for Formatter.</p>
	 *
	 * @param formatName a {@link java.lang.String} object.
	 */
	public Formatter(final String formatName){
		this(formatName, formatName);
	}

	/**
	 * <p>Constructor for Formatter.</p>
	 *
	 * @param formatName a {@link java.lang.String} object.
	 * @param key a {@link java.lang.String} object.
	 */
	public Formatter(final String formatName, final String key){
		this.formatName = formatName;
		this.key = key.toLowerCase();
	}

	/**
	 * <p>getMIMEType.</p>
	 *
	 * @param queryResult a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getMIMEType(final QueryResult queryResult){
		if(queryResult instanceof GraphResult){
			return "text/n3";
		} else {
			System.err.println("lupos.endpoint.server.format.Formatter: QueryResult other than GraphResult should be handled by the class "+this.getClass().getCanonicalName());
			return null;
		}
	}

	/**
	 * <p>getName.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName(){
		return this.formatName;
	}

	/**
	 * <p>Getter for the field <code>key</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getKey(){
		return this.key;
	}

	/**
	 * <p>writeResult.</p>
	 *
	 * @param os a {@link java.io.OutputStream} object.
	 * @param variables a {@link java.util.Collection} object.
	 * @param queryResult a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeResult(final OutputStream os, final Collection<Variable> variables, final QueryResult queryResult) throws IOException {
		if(queryResult instanceof GraphResult){
			final byte[] carriageReturn = "\n".getBytes();
			for(final Triple t: ((GraphResult)queryResult).getGraphResultTriples()){
				os.write(t.toN3String().getBytes());
				os.write(carriageReturn);
			}
		} else {
			System.err.println("lupos.endpoint.server.format.Formatter: QueryResult other than GraphResult should be handled by the class "+this.getClass().getCanonicalName());
		}
	}

	/**
	 * This method is overwritten by those formatters, which support returning query-triples
	 *
	 * @return true if returning query-triples is supported, otherwise false
	 */
	public boolean isWriteQueryTriples() {
		return false;
	}
}
