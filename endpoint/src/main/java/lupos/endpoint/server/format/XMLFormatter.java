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
		super("XML", "application/sparql-results+xml"); //$NON-NLS-1$
	}
	
	public void writeBooleanResult(final OutputStream os, final boolean result) throws IOException{
		os.write(XMLFormatter.XML_1.getBytes());
		os.write(XMLFormatter.XML_Boolean_2.getBytes());
		os.write(Boolean.toString(result).getBytes());
		os.write(XMLFormatter.XML_Boolean_3.getBytes());			
	}
	
	public void writeStartHead(final OutputStream os) throws IOException{
		os.write(XMLFormatter.XML_1.getBytes());
	}
	
	public void writeVariableInHead(final OutputStream os, final Variable v) throws IOException{
		os.write(XMLFormatter.XML_Var_1.getBytes());
		os.write(v.getName().getBytes());
		os.write(XMLFormatter.XML_Var_2.getBytes());
	}

	public void writeEndHead(final OutputStream os) throws IOException{
		os.write(XMLFormatter.XML_2.getBytes());
	}
	
	public void writeStartResult(final OutputStream os) throws IOException{
		os.write(XMLFormatter.XML_Result_1.getBytes());
	}

	public void writeEndResult(final OutputStream os) throws IOException{
		os.write(XMLFormatter.XML_Result_2.getBytes());
	}
	
	public void writeEpilogue(final OutputStream os) throws IOException{
		os.write(XMLFormatter.XML_3.getBytes());
	}
	
	public void writeStartBinding(final OutputStream os, final Variable v) throws IOException{
		os.write(XMLFormatter.XML_Binding_1.getBytes());
		os.write(v.getName().getBytes());
		os.write(XMLFormatter.XML_Binding_2.getBytes());
	}
	
	public void writeEndBinding(final OutputStream os) throws IOException{
		os.write(XMLFormatter.XML_Binding_3.getBytes());
	}
	
	public void writeBlankNode(final OutputStream os, AnonymousLiteral blankNode) throws IOException{
		// blank node => <bnode>
		os.write(XMLFormatter.XML_BNode_1.getBytes());
		os.write(blankNode.originalString().getBytes());
		os.write(XMLFormatter.XML_BNode_2.getBytes());						
	}
	
	public void writeURI(final OutputStream os, URILiteral uri) throws IOException{
		// uri => <uri>
		os.write(XMLFormatter.XML_URI_1.getBytes());
		os.write(uri.getString().getBytes());
		os.write(XMLFormatter.XML_URI_2.getBytes());
	}
	
	public void writeSimpleLiteral(final OutputStream os, Literal literal) throws IOException{
		// literal => <literal>
		os.write(XMLFormatter.XML_LITERAL_1.getBytes());
		// <literal>S</literal>
		os.write(XMLFormatter.XML_LITERAL_2.getBytes());
		os.write(Helper.unquote(literal.originalString()).getBytes());
		os.write(XMLFormatter.XML_LITERAL_3.getBytes());
	}
	
	public void writeTypedLiteral(final OutputStream os, TypedLiteral literal) throws IOException{
		// literal => <literal>
		os.write(XMLFormatter.XML_LITERAL_1.getBytes());
		// <literal datatype="datatype">content</literal>
		os.write(" datatype=\"".getBytes());
		os.write(literal.getTypeLiteral().getString().getBytes());
		os.write("\"".getBytes());		
		os.write(XMLFormatter.XML_LITERAL_2.getBytes());
		os.write(Helper.unquote(literal.getOriginalContent()).getBytes());
		os.write(XMLFormatter.XML_LITERAL_3.getBytes());
	}
	
	public void writeLanguageTaggedLiteral(final OutputStream os, LanguageTaggedLiteral literal) throws IOException{
		// literal => <literal>
		os.write(XMLFormatter.XML_LITERAL_1.getBytes());
		// <literal xml:lang="lang">content</literal>								
		os.write(" xml:lang=\"".getBytes());
		os.write(literal.getOriginalLanguage().getBytes());
		os.write("\"".getBytes());							
		os.write(XMLFormatter.XML_LITERAL_2.getBytes());
		os.write(Helper.unquote(literal.getContent()).getBytes());
		os.write(XMLFormatter.XML_LITERAL_3.getBytes());		
	}

	@Override
	public String getMIMEType(final QueryResult queryResult) {
		if(queryResult instanceof GraphResult){
			return "application/rdf+xml";
		} else {
			return "application/sparql-results+xml";
		}
	}		
}
