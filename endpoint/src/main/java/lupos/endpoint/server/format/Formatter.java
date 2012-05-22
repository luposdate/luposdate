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