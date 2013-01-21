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
package lupos.endpoint.server.format;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang.StringEscapeUtils;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.singleinput.ExpressionEvaluation.Helper;

public class XMLFormatter extends HeadBodyFormatter {
	
	private final static String XML_1 = 	"<?xml version=\"1.0\"?>\n"+
											"<sparql xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema#\" xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"+
											" <head>\n";
	private final static String XML_2 =	" </head>\n <results>\n";
	private final static String XML_3 =	" </results>\n</sparql>";	

	private final static String XML_Boolean_2 =	" </head>\n <boolean>";
	private final static String XML_Boolean_3 =	"</boolean>\n</sparql>";	

	
	private final static String XML_Var_1 =	"  <variable name=\"";
	private final static String XML_Var_2 =	"\"/>\n";

	private final static String XML_Result_1 =	"   <result>\n";
	private final static String XML_Result_2 =	"   </result>\n";

	private final static String XML_Binding_1 =	"    <binding name=\"";
	private final static String XML_Binding_2 =	"\">\n";
	private final static String XML_Binding_3 =	"    </binding>\n";

	private final static String XML_BNode_1 =	"     <bnode>";
	private final static String XML_BNode_2 =	"</bnode>\n";

	private final static String XML_URI_1 =	"     <uri>";
	private final static String XML_URI_2 =	"</uri>\n";

	private final static String XML_LITERAL_1 =	"     <literal";
	private final static String XML_LITERAL_2 =	">";
	private final static String XML_LITERAL_3 =	"</literal>\n";


	public XMLFormatter() {
		super("XML", "application/sparql-results+xml");
	}
	
	@Override
	public void writeBooleanResult(final OutputStream os, final boolean result) throws IOException{
		os.write(XMLFormatter.XML_1.getBytes());
		os.write(XMLFormatter.XML_Boolean_2.getBytes());
		os.write(Boolean.toString(result).getBytes());
		os.write(XMLFormatter.XML_Boolean_3.getBytes());			
	}
	
	@Override
	public void writeStartHead(final OutputStream os) throws IOException{
		os.write(XMLFormatter.XML_1.getBytes());
	}
	
	@Override
	public void writeVariableInHead(final OutputStream os, final Variable v) throws IOException{
		os.write(XMLFormatter.XML_Var_1.getBytes());
		os.write(v.getName().getBytes());
		os.write(XMLFormatter.XML_Var_2.getBytes());
	}

	@Override
	public void writeEndHead(final OutputStream os) throws IOException{
		os.write(XMLFormatter.XML_2.getBytes());
	}
	
	@Override
	public void writeStartResult(final OutputStream os) throws IOException{
		os.write(XMLFormatter.XML_Result_1.getBytes());
	}

	@Override
	public void writeEndResult(final OutputStream os) throws IOException{
		os.write(XMLFormatter.XML_Result_2.getBytes());
	}
	
	@Override
	public void writeEpilogue(final OutputStream os) throws IOException{
		os.write(XMLFormatter.XML_3.getBytes());
	}
	
	@Override
	public void writeStartBinding(final OutputStream os, final Variable v) throws IOException{
		os.write(XMLFormatter.XML_Binding_1.getBytes());
		os.write(v.getName().getBytes());
		os.write(XMLFormatter.XML_Binding_2.getBytes());
	}
	
	@Override
	public void writeEndBinding(final OutputStream os) throws IOException{
		os.write(XMLFormatter.XML_Binding_3.getBytes());
	}
	
	@Override
	public void writeBlankNode(final OutputStream os, AnonymousLiteral blankNode) throws IOException{
		// blank node => <bnode>
		os.write(XMLFormatter.XML_BNode_1.getBytes());
		os.write(blankNode.getBlankNodeLabel().getBytes());
		os.write(XMLFormatter.XML_BNode_2.getBytes());						
	}
	
	@Override
	public void writeURI(final OutputStream os, URILiteral uri) throws IOException{
		// uri => <uri>
		os.write(XMLFormatter.XML_URI_1.getBytes());
		os.write(StringEscapeUtils.escapeXml(uri.getString()).getBytes());
		os.write(XMLFormatter.XML_URI_2.getBytes());
	}
	
	@Override
	public void writeSimpleLiteral(final OutputStream os, Literal literal) throws IOException{
		// literal => <literal>
		os.write(XMLFormatter.XML_LITERAL_1.getBytes());
		// <literal>S</literal>
		os.write(XMLFormatter.XML_LITERAL_2.getBytes());
		os.write(StringEscapeUtils.escapeXml(Helper.unquote(literal.originalString())).getBytes());
		os.write(XMLFormatter.XML_LITERAL_3.getBytes());
	}
	
	@Override
	public void writeTypedLiteral(final OutputStream os, TypedLiteral literal) throws IOException{
		// literal => <literal>
		os.write(XMLFormatter.XML_LITERAL_1.getBytes());
		// <literal datatype="datatype">content</literal>
		os.write(" datatype=\"".getBytes());
		os.write(StringEscapeUtils.escapeXml(literal.getTypeLiteral().getString()).getBytes());
		os.write("\"".getBytes());		
		os.write(XMLFormatter.XML_LITERAL_2.getBytes());
		os.write(StringEscapeUtils.escapeXml(Helper.unquote(literal.getOriginalContent())).getBytes());
		os.write(XMLFormatter.XML_LITERAL_3.getBytes());
	}
	
	@Override
	public void writeLanguageTaggedLiteral(final OutputStream os, LanguageTaggedLiteral literal) throws IOException{
		// literal => <literal>
		os.write(XMLFormatter.XML_LITERAL_1.getBytes());
		// <literal xml:lang="lang">content</literal>								
		os.write(" xml:lang=\"".getBytes());
		os.write(StringEscapeUtils.escapeXml(literal.getOriginalLanguage()).getBytes());
		os.write("\"".getBytes());							
		os.write(XMLFormatter.XML_LITERAL_2.getBytes());
		os.write(StringEscapeUtils.escapeXml(Helper.unquote(literal.getContent())).getBytes());
		os.write(XMLFormatter.XML_LITERAL_3.getBytes());		
	}

	@Override
	public String getMIMEType(final QueryResult queryResult) {
		if(queryResult instanceof GraphResult){
			return super.getMIMEType(queryResult);
		} else {
			return "application/sparql-results+xml";
		}
	}		
}
