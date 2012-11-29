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

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;

public class JSONFormatter extends HeadBodyFormatter {

	public JSONFormatter() {
		super("JSON", "application/sparql-results+json");
	}

	@Override
	public void writeBooleanResult(OutputStream os, boolean result)
			throws IOException {
		os.write("{\n \"head\" : { } ,\n \"boolean\" : ".getBytes());
		os.write(Boolean.toString(result).getBytes());
		os.write("\n}".getBytes());
	}

	@Override
	public void writeStartHead(OutputStream os) throws IOException {
		os.write("{\n \"head\": {\n  \"vars\": [".getBytes());
	}

	@Override
	public void writeFirstVariableInHead(final OutputStream os, final Variable v)
			throws IOException {
		os.write("\"".getBytes());
		os.write(v.getName().getBytes());
		os.write("\"".getBytes());
	}

	@Override
	public void writeVariableInHead(OutputStream os, Variable v)
			throws IOException {
		os.write(", ".getBytes());
		this.writeFirstVariableInHead(os, v);
	}

	@Override
	public void writeEndHead(OutputStream os) throws IOException {
		os.write("]\n },\n \"results\": {\n  \"bindings\": [".getBytes());
	}

	@Override
	public void writeFirstStartResult(final OutputStream os) throws IOException {
		os.write("\n   {".getBytes());
	}

	@Override
	public void writeStartResult(OutputStream os) throws IOException {
		os.write(", ".getBytes());
		this.writeFirstStartResult(os);
	}

	@Override
	public void writeEndResult(OutputStream os) throws IOException {
		os.write("\n   }".getBytes());
	}

	@Override
	public void writeEpilogue(OutputStream os) throws IOException {
		os.write("\n  ]\n }\n}".getBytes());
	}

	@Override
	public void writeFirstStartBinding(final OutputStream os, final Variable v)
			throws IOException {
		os.write("\n    \"".getBytes());
		os.write(v.getName().getBytes());
		os.write("\": { ".getBytes());
	}

	@Override
	public void writeStartBinding(OutputStream os, Variable v)
			throws IOException {
		os.write(",".getBytes());
		this.writeFirstStartBinding(os, v);
	}

	@Override
	public void writeEndBinding(OutputStream os) throws IOException {
		os.write(" }".getBytes());
	}

	@Override
	public void writeBlankNode(OutputStream os, AnonymousLiteral blankNode)
			throws IOException {
		os.write("\"type\": \"bnode\", \"value\": \"".getBytes());
		os.write(blankNode.getBlankNodeLabel().getBytes());
		os.write("\"".getBytes());
	}

	@Override
	public void writeURI(OutputStream os, URILiteral uri) throws IOException {
		os.write("\"type\": \"uri\", \"value\": \"".getBytes());
		os.write(uri.getString().getBytes());
		os.write("\"".getBytes());
	}

	@Override
	public void writeSimpleLiteral(OutputStream os, Literal literal)
			throws IOException {
		os.write("\"type\": \"literal\", \"value\": ".getBytes());
		os.write(literal.originalString().getBytes());
	}

	@Override
	public void writeTypedLiteral(OutputStream os, TypedLiteral literal)
			throws IOException {
		os.write("\"type\": \"typed-literal\", \"datatype\": \"".getBytes());
		os.write(literal.getTypeLiteral().getString().getBytes());
		os.write("\", \"value\": ".getBytes());
		os.write(literal.getOriginalContent().getBytes());
	}

	@Override
	public void writeLanguageTaggedLiteral(OutputStream os,
			LanguageTaggedLiteral literal) throws IOException {
		os.write("\"type\": \"literal\", \"xml:lang\": \"".getBytes());
		os.write(literal.getOriginalLanguage().getBytes());
		os.write("\",\"value\": ".getBytes());
		os.write(literal.getContent().getBytes());
	}

	@Override
	public String getMIMEType(QueryResult queryResult) {
		if (queryResult instanceof GraphResult) {
			return super.getMIMEType(queryResult);
		} else {
			return "application/sparql-results+json";
		}
	}
}
