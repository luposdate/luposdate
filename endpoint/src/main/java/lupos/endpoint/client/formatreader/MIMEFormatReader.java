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
package lupos.endpoint.client.formatreader;

import java.io.InputStream;

import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.queryresult.QueryResult;
public abstract class MIMEFormatReader {
	private final String formatName;
	private final String key;

	/**
	 * <p>Constructor for MIMEFormatReader.</p>
	 *
	 * @param formatName a {@link java.lang.String} object.
	 */
	public MIMEFormatReader(final String formatName){
		this(formatName, formatName);
	}

	/**
	 * <p>Constructor for MIMEFormatReader.</p>
	 *
	 * @param formatName a {@link java.lang.String} object.
	 * @param key a {@link java.lang.String} object.
	 */
	public MIMEFormatReader(final String formatName, final String key){
		this.formatName = formatName;
		this.key = key.toLowerCase();
	}

	/**
	 * <p>getMIMEType.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public abstract String getMIMEType();

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
	 * <p>getQueryResult.</p>
	 *
	 * @param inputStream a {@link java.io.InputStream} object.
	 * @param query a {@link java.lang.String} object.
	 * @param bindingsFactory a {@link lupos.datastructures.bindings.BindingsFactory} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public abstract QueryResult getQueryResult(final InputStream inputStream, final String query, BindingsFactory bindingsFactory);
}
