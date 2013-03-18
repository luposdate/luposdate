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
package lupos.event.communication;

import java.io.*;
import java.util.Set;

import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.client.formatreader.*;
import lupos.endpoint.server.format.*;

/**
 * This class can be used to serialize a {@ Triple}
 */
public class SerializedQueryResult implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Formatter formatter = new XMLFormatter();
	private static final DefaultMIMEFormatReader formatReader = new XMLFormatReader();
	
	private String id;
	private final byte[] serialized;	
	
	
	public SerializedQueryResult(Set<Variable> vars, QueryResult result, String id) throws IOException {
		this.id = id;
		// serialize query result
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SerializedQueryResult.formatter.writeResult(baos, vars, result);
		this.serialized = baos.toByteArray();
	}
	
	public QueryResult getQueryResult() {
		ByteArrayInputStream bais = new ByteArrayInputStream(this.serialized);
		return SerializedQueryResult.formatReader.getQueryResult(bais);
	}
	
	public String getId() { 
		return this.id; 
	}
}
