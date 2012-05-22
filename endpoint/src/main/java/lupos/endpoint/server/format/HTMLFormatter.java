package lupos.endpoint.server.format;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;

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
	
	private final static String COLOR_VAR="7016A8";
	private final static String COLOR_LITERAL="0000FF";
	private final static String COLOR_URI="00B200";
	private final static String COLOR_BLANKNODE="6F6F25";
	private final static String COLOR_LANG="8080FF";
	private final static String COLOR_BOOLEAN="B03060";
	private final static String COLOR_SEPARATOR="B03060";
	
	
	private final boolean colored;

	public HTMLFormatter(final boolean colored) {
		super(colored?"Colored HTML":"HTML");
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
	public void writeBlankNode(final OutputStream os, AnonymousLiteral blankNode) throws IOException{
		this.writeStartColor(os, HTMLFormatter.COLOR_BLANKNODE);
		os.write(blankNode.originalString().getBytes());
		this.writeEndColor(os);
	}
	
	@Override
	public void writeURI(final OutputStream os, URILiteral uri) throws IOException{
		this.writeStartColor(os, HTMLFormatter.COLOR_URI);
		os.write("&lt;".getBytes());
		os.write(uri.getString().getBytes());
		os.write("&gt;".getBytes());
		this.writeEndColor(os);
	}
	
	@Override
	public void writeSimpleLiteral(final OutputStream os, Literal literal) throws IOException{
		this.writeStartColor(os, HTMLFormatter.COLOR_LITERAL);
		os.write(literal.originalString().getBytes());
		this.writeEndColor(os);
	}
	
	@Override
	public void writeTypedLiteral(final OutputStream os, TypedLiteral literal) throws IOException{
		this.writeStartColor(os, HTMLFormatter.COLOR_LITERAL);
		os.write(literal.getOriginalContent().getBytes());
		this.writeEndColor(os);
		this.writeStartColor(os, HTMLFormatter.COLOR_SEPARATOR);
		os.write("^^".getBytes());
		this.writeEndColor(os);
		this.writeURI(os, literal.getTypeLiteral());
	}
	
	@Override
	public void writeLanguageTaggedLiteral(final OutputStream os, LanguageTaggedLiteral literal) throws IOException{
		this.writeStartColor(os, HTMLFormatter.COLOR_LITERAL);
		os.write(literal.getContent().getBytes());
		this.writeEndColor(os);
		this.writeStartColor(os, HTMLFormatter.COLOR_LANG);
		os.write("@".getBytes());
		os.write(literal.getOriginalLanguage().getBytes());
		this.writeEndColor(os);
	}
	
	@Override
	public void writeResult(OutputStream os, Set<Variable> variables, QueryResult queryResult) throws IOException {
		if(queryResult instanceof GraphResult){
			os.write(HTMLFormatter.XML_1.getBytes());
			this.writeTableHeadEntry(os, "subject");
			this.writeTableHeadEntry(os, "predicate");
			this.writeTableHeadEntry(os, "object");
			os.write(HTMLFormatter.XML_2.getBytes());
			for(Triple triple: ((GraphResult)queryResult).getGraphResultTriples()){
				this.writeStartResult(os);
				for(Literal literal: triple){
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
	
	protected void writeTableHeadEntry(OutputStream os, String entry) throws IOException {
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
