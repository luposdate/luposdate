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
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;

import org.apache.commons.lang.StringEscapeUtils;

public class HTMLFormatter extends HeadBodyFormatter {

	private final static String XML_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
										"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
										"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
										" <head>\n" +
										"  <title>Result of LUPOSDATE SPARQL Endpoint</title>\n" +
										" </head>\n" +
										" <body>\n" +
										"  <h1>Result of LUPOSDATE SPARQL Endpoint</h1>\n"+
										"  <table border=\"1\">\n   <tr>";

	private final static String XML_2 =	"   </tr>\n";
	private final static String XML_3 =	"  </table>\n </body>\n</html>";

	private final static String XML_Boolean_2 =	"<th>Boolean Result</th></tr>\n   <tr><th>";
	private final static String XML_Boolean_3 =	"</th></tr>\n  </table>\n </body>\n</html>";


	private final static String XML_Var_1 =	"    <th>";
	private final static String XML_Var_2 =	"</th>\n";

	private final static String XML_Result_1 =	"   <tr>\n";
	private final static String XML_Result_2 =	"   </tr>\n";

	private final static String XML_Binding_1 =	"    <th>";
	private final static String XML_Binding_2 =	"</th>\n";

	private final static String XML_QueryTriplesStart = "  <th><table border=\"1\">\n   <tr><th>subject</th><th>predicate</th><th>object</th></tr>\n";
	private final static String XML_QueryTriplesEnd = "  </table></th>\n";

	private final static String COLOR_VAR="7016A8";
	private final static String COLOR_QUERYTRIPLES="00FF00";
	private final static String COLOR_LITERAL="0000FF";
	private final static String COLOR_URI="00B200";
	private final static String COLOR_BLANKNODE="6F6F25";
	private final static String COLOR_LANG="8080FF";
	private final static String COLOR_BOOLEAN="B03060";
	private final static String COLOR_SEPARATOR="B03060";


	private final boolean colored;

	public HTMLFormatter(final boolean colored) {
		this(colored, false);
	}


	public HTMLFormatter(final boolean colored, final boolean queryTriples) {
		super((colored?"Colored HTML":"HTML")+(queryTriples?" with Query-Triples":""), queryTriples);
		this.colored = colored;
	}

	public void writeStartColor(final OutputStream os, final String color) throws IOException{
		if(this.colored){
			os.write("<font color=\"#".getBytes());
			os.write(color.getBytes());
			os.write("\">".getBytes());
		}
	}

	public void writeEndColor(final OutputStream os) throws IOException{
		if(this.colored){
			os.write("</font>".getBytes());
		}
	}

	@Override
	public void writeBooleanResult(final OutputStream os, final boolean result) throws IOException{
		os.write(HTMLFormatter.XML_1.getBytes());
		os.write(HTMLFormatter.XML_Boolean_2.getBytes());
		this.writeStartColor(os, HTMLFormatter.COLOR_BOOLEAN);
		os.write(Boolean.toString(result).getBytes());
		this.writeEndColor(os);
		os.write(HTMLFormatter.XML_Boolean_3.getBytes());
	}

	@Override
	public void writeStartHead(final OutputStream os) throws IOException{
		os.write(HTMLFormatter.XML_1.getBytes());
	}

	@Override
	public void writeVariableInHead(final OutputStream os, final Variable v) throws IOException{
		os.write(HTMLFormatter.XML_Var_1.getBytes());
		this.writeStartColor(os, HTMLFormatter.COLOR_VAR);
		os.write(v.getName().getBytes());
		this.writeEndColor(os);
		os.write(HTMLFormatter.XML_Var_2.getBytes());
	}

	@Override
	public void writeQueryTriplesHead(final OutputStream os) throws IOException {
		os.write(HTMLFormatter.XML_Var_1.getBytes());
		this.writeStartColor(os, HTMLFormatter.COLOR_QUERYTRIPLES);
		os.write("Query-Triples".getBytes());
		this.writeEndColor(os);
		os.write(HTMLFormatter.XML_Var_2.getBytes());
	}

	@Override
	public void writeEndHead(final OutputStream os) throws IOException{
		os.write(HTMLFormatter.XML_2.getBytes());
	}

	@Override
	public void writeStartResult(final OutputStream os) throws IOException{
		os.write(HTMLFormatter.XML_Result_1.getBytes());
	}

	@Override
	public void writeEndResult(final OutputStream os) throws IOException{
		os.write(HTMLFormatter.XML_Result_2.getBytes());
	}

	@Override
	public void writeQueryTriplesStart(final OutputStream os) throws IOException {
		os.write(HTMLFormatter.XML_QueryTriplesStart.getBytes());
	}

	@Override
	public void writeQueryTriplesEnd(final OutputStream os) throws IOException {
		os.write(HTMLFormatter.XML_QueryTriplesEnd.getBytes());
	}

	@Override
	public void writeQueryTripleStart(final OutputStream os) throws IOException {
		this.writeStartResult(os);
	}

	@Override
	public void writeQueryTripleEnd(final OutputStream os) throws IOException {
		this.writeEndResult(os);
	}

	@Override
	public void writeQueryTripleStartComponent(final OutputStream os) throws IOException {
		os.write(HTMLFormatter.XML_Binding_1.getBytes());
	}

	@Override
	public void writeQueryTripleEndComponent(final OutputStream os) throws IOException {
		os.write(HTMLFormatter.XML_Binding_2.getBytes());
	}

	@Override
	public void writeEpilogue(final OutputStream os) throws IOException{
		os.write(HTMLFormatter.XML_3.getBytes());
	}

	@Override
	public void writeStartBinding(final OutputStream os, final Variable v) throws IOException{
		os.write(HTMLFormatter.XML_Binding_1.getBytes());
	}

	@Override
	public void writeEndBinding(final OutputStream os) throws IOException{
		os.write(HTMLFormatter.XML_Binding_2.getBytes());
	}

	@Override
	public void writeBlankNode(final OutputStream os, final AnonymousLiteral blankNode) throws IOException{
		this.writeStartColor(os, HTMLFormatter.COLOR_BLANKNODE);
		os.write(blankNode.originalString().getBytes());
		this.writeEndColor(os);
	}

	@Override
	public void writeURI(final OutputStream os, final URILiteral uri) throws IOException{
		this.writeStartColor(os, HTMLFormatter.COLOR_URI);
		os.write("&lt;".getBytes());
		os.write(StringEscapeUtils.escapeHtml(uri.getString()).getBytes());
		os.write("&gt;".getBytes());
		this.writeEndColor(os);
	}

	@Override
	public void writeSimpleLiteral(final OutputStream os, final Literal literal) throws IOException{
		this.writeStartColor(os, HTMLFormatter.COLOR_LITERAL);
		os.write(StringEscapeUtils.escapeHtml(literal.originalString()).getBytes());
		this.writeEndColor(os);
	}

	@Override
	public void writeTypedLiteral(final OutputStream os, final TypedLiteral literal) throws IOException{
		this.writeStartColor(os, HTMLFormatter.COLOR_LITERAL);
		os.write(literal.getOriginalContent().getBytes());
		this.writeEndColor(os);
		this.writeStartColor(os, HTMLFormatter.COLOR_SEPARATOR);
		os.write("^^".getBytes());
		this.writeEndColor(os);
		this.writeURI(os, literal.getTypeLiteral());
	}

	@Override
	public void writeLanguageTaggedLiteral(final OutputStream os, final LanguageTaggedLiteral literal) throws IOException{
		this.writeStartColor(os, HTMLFormatter.COLOR_LITERAL);
		os.write(StringEscapeUtils.escapeHtml(literal.getContent()).getBytes());
		this.writeEndColor(os);
		this.writeStartColor(os, HTMLFormatter.COLOR_LANG);
		os.write("@".getBytes());
		os.write(StringEscapeUtils.escapeHtml(literal.getOriginalLanguage()).getBytes());
		this.writeEndColor(os);
	}

	@Override
	public void writeResult(final OutputStream os, final Collection<Variable> variables, final QueryResult queryResult) throws IOException {
		if(queryResult instanceof GraphResult){
			os.write(HTMLFormatter.XML_1.getBytes());
			this.writeTableHeadEntry(os, "subject");
			this.writeTableHeadEntry(os, "predicate");
			this.writeTableHeadEntry(os, "object");
			os.write(HTMLFormatter.XML_2.getBytes());
			for(final Triple triple: ((GraphResult)queryResult).getGraphResultTriples()){
				this.writeStartResult(os);
				for(final Literal literal: triple){
					os.write(HTMLFormatter.XML_Binding_1.getBytes());
					this.writeLiteral(os, literal);
					this.writeEndBinding(os);
				}
				this.writeEndResult(os);
			}
			os.write(HTMLFormatter.XML_3.getBytes());
		} else {
			super.writeResult(os, variables, queryResult);
		}
	}

	protected void writeTableHeadEntry(final OutputStream os, final String entry) throws IOException {
		os.write("<th>".getBytes());
		this.writeStartColor(os, HTMLFormatter.COLOR_VAR);
		os.write(entry.getBytes());
		this.writeEndColor(os);
		os.write("</th>".getBytes());
	}

	@Override
	public String getMIMEType(final QueryResult queryResult) {
		return "text/html";
	}
}
