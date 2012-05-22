package lupos.endpoint.client.formatreader;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.client.formatreader.tsv.ASTOneResult;
import lupos.endpoint.client.formatreader.tsv.ASTValue;
import lupos.endpoint.client.formatreader.tsv.ASTVar;
import lupos.endpoint.client.formatreader.tsv.ASTVars;
import lupos.endpoint.client.formatreader.tsv.Node;
import lupos.endpoint.client.formatreader.tsv.ParseException;
import lupos.endpoint.client.formatreader.tsv.SimpleNode;
import lupos.endpoint.client.formatreader.tsv.TSVParser;

public class TSVFormatReader extends MIMEFormatReader {

	public final static String MIMETYPE = "text/tsv";

	public TSVFormatReader() {
		super("TSV", TSVFormatReader.MIMETYPE);
	}

	@Override
	public String getMIMEType() {
		return TSVFormatReader.MIMETYPE;
	}

	@Override
	public QueryResult getQueryResult(InputStream inputStream) {
		QueryResult result = QueryResult.createInstance();
		
		try {
			SimpleNode root = TSVParser.parse(inputStream);
			LinkedList<Variable> vars = new LinkedList<Variable>();
			for(int i=0; i<root.jjtGetNumChildren(); i++){
				Node child = root.jjtGetChild(i);
				if(child instanceof ASTVars){
					for(int j=0; j<child.jjtGetNumChildren(); j++){
						Node childchild = child.jjtGetChild(j);
						if(childchild instanceof ASTVar){
							vars.add(new Variable(((ASTVar)childchild).getName()));
						}						
					}					
				}
			}
			for(int i=0; i<root.jjtGetNumChildren(); i++){
				Node child = root.jjtGetChild(i);
				if(child instanceof ASTOneResult){
					Bindings bindings = Bindings.createNewInstance();
					Iterator<Variable> varIt = vars.iterator();
					for(int j=0; j<child.jjtGetNumChildren() && varIt.hasNext(); j++){
						Variable var = varIt.next();
						Node childchild = child.jjtGetChild(j);
						if(childchild instanceof ASTValue && childchild.jjtGetNumChildren()>0){
							bindings.add(var, TSVParser.getLiteral(childchild.jjtGetChild(0)));
						}
					}
					result.add(bindings);
				}
			}			
		} catch (ParseException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		
		return result;	}

}
